package dev.l2j.tesla.gameserver.model.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.ItemTable;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance.ItemLocation;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance.ItemState;
import dev.l2j.tesla.gameserver.model.item.kind.Item;

public abstract class ItemContainer
{
	protected static final CLogger LOGGER = new CLogger(ItemContainer.class.getName());
	
	private static final String RESTORE_ITEMS = "SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time FROM items WHERE owner_id=? AND (loc=?)";
	
	protected final Set<ItemInstance> _items = new ConcurrentSkipListSet<>();
	
	protected ItemContainer()
	{
	}
	
	protected abstract Creature getOwner();
	
	protected abstract ItemLocation getBaseLocation();
	
	public String getName()
	{
		return "ItemContainer";
	}
	
	/**
	 * @return the owner objectId of the inventory.
	 */
	public int getOwnerId()
	{
		return (getOwner() == null) ? 0 : getOwner().getObjectId();
	}
	
	/**
	 * @return the quantity of items in the inventory.
	 */
	public int getSize()
	{
		return _items.size();
	}
	
	/**
	 * @return the list of items in inventory.
	 */
	public Set<ItemInstance> getItems()
	{
		return _items;
	}
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param itemIds a list of item Ids to check.
	 * @return true if at least one items exists in player's inventory, false otherwise
	 */
	public boolean hasAtLeastOneItem(int... itemIds)
	{
		for (int itemId : itemIds)
		{
			if (getItemByItemId(itemId) != null)
				return true;
		}
		return false;
	}
	
	/**
	 * @param itemId : the itemId to check.
	 * @return a List holding the items list (empty list if not found)
	 */
	public List<ItemInstance> getItemsByItemId(int itemId)
	{
		final List<ItemInstance> list = new ArrayList<>();
		for (ItemInstance item : _items)
		{
			if (item.getItemId() == itemId)
				list.add(item);
		}
		return list;
	}
	
	/**
	 * @param itemId : the itemId to check.
	 * @return the item by using its itemId, or null if not found in inventory.
	 */
	public ItemInstance getItemByItemId(int itemId)
	{
		for (ItemInstance item : _items)
		{
			if (item.getItemId() == itemId)
				return item;
		}
		return null;
	}
	
	/**
	 * @param objectId : the objectId to check.
	 * @return the item by using its objectId, or null if not found in inventory
	 */
	public ItemInstance getItemByObjectId(int objectId)
	{
		for (ItemInstance item : _items)
		{
			if (item.getObjectId() == objectId)
				return item;
		}
		return null;
	}
	
	/**
	 * @param itemId : the itemId to check.
	 * @param enchantLevel : enchant level to match on, or -1 for ANY enchant level.
	 * @return int corresponding to the number of items matching the above conditions.
	 */
	public int getInventoryItemCount(int itemId, int enchantLevel)
	{
		return getInventoryItemCount(itemId, enchantLevel, true);
	}
	
	/**
	 * @param itemId : the itemId to check.
	 * @param enchantLevel : enchant level to match on, or -1 for ANY enchant level.
	 * @param includeEquipped : include equipped items.
	 * @return the count of items matching the above conditions.
	 */
	public int getInventoryItemCount(int itemId, int enchantLevel, boolean includeEquipped)
	{
		int count = 0;
		
		for (ItemInstance item : _items)
		{
			if (item.getItemId() == itemId && (item.getEnchantLevel() == enchantLevel || enchantLevel < 0) && (includeEquipped || !item.isEquipped()))
			{
				if (item.isStackable())
					return item.getCount();
				
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Adds item to inventory
	 * @param process : String identifier of process triggering this action.
	 * @param item : ItemInstance to add.
	 * @param actor : The player requesting the item addition.
	 * @param reference : The WorldObject referencing current action (like NPC selling item or previous item in transformation,...)
	 * @return the ItemInstance corresponding to the new or updated item.
	 */
	public ItemInstance addItem(String process, ItemInstance item, Player actor, WorldObject reference)
	{
		ItemInstance olditem = getItemByItemId(item.getItemId());
		
		// If stackable item is found in inventory just add to current quantity
		if (olditem != null && olditem.isStackable())
		{
			int count = item.getCount();
			olditem.changeCount(process, count, actor, reference);
			olditem.setLastChange(ItemState.MODIFIED);
			
			// And destroys the item
			item.destroyMe(process, actor, reference);
			item.updateDatabase();
			item = olditem;
			
			// Updates database
			if (item.getItemId() == 57 && count < 10000 * Config.RATE_DROP_ADENA)
			{
				// Small adena changes won't be saved to database all the time
				if (Rnd.get(10) < 2)
					item.updateDatabase();
			}
			else
				item.updateDatabase();
		}
		// If item hasn't be found in inventory, create new one
		else
		{
			item.setOwnerId(process, getOwnerId(), actor, reference);
			item.setLocation(getBaseLocation());
			item.setLastChange(ItemState.ADDED);
			
			// Add item in inventory
			addItem(item);
			
			// Updates database
			item.updateDatabase();
		}
		
		refreshWeight();
		return item;
	}
	
	/**
	 * Adds an item to inventory.
	 * @param process : String identifier of process triggering this action.
	 * @param itemId : The itemId of the ItemInstance to add.
	 * @param count : The quantity of items to add.
	 * @param actor : The player requesting the item addition.
	 * @param reference : The WorldObject referencing current action (like NPC selling item or previous item in transformation,...)
	 * @return the ItemInstance corresponding to the new or updated item.
	 */
	public ItemInstance addItem(String process, int itemId, int count, Player actor, WorldObject reference)
	{
		ItemInstance item = getItemByItemId(itemId);
		
		// If stackable item is found in inventory just add to current quantity
		if (item != null && item.isStackable())
		{
			item.changeCount(process, count, actor, reference);
			item.setLastChange(ItemState.MODIFIED);
			
			// Updates database
			if (itemId == 57 && count < 10000 * Config.RATE_DROP_ADENA)
			{
				// Small adena changes won't be saved to database all the time
				if (Rnd.get(10) < 2)
					item.updateDatabase();
			}
			else
				item.updateDatabase();
		}
		// If item hasn't be found in inventory, create new one
		else
		{
			final Item template = ItemTable.getInstance().getTemplate(itemId);
			if (template == null)
				return null;
			
			for (int i = 0; i < count; i++)
			{
				item = ItemInstance.create(itemId, template.isStackable() ? count : 1, actor, reference);
				item.setOwnerId(getOwnerId());
				item.setLocation(getBaseLocation());
				item.setLastChange(ItemState.ADDED);
				
				// Add item in inventory
				addItem(item);
				
				// Updates database
				item.updateDatabase();
				
				// If stackable, end loop as entire count is included in 1 instance of item
				if (template.isStackable() || !Config.MULTIPLE_ITEM_DROP)
					break;
			}
		}
		
		refreshWeight();
		return item;
	}
	
	/**
	 * Transfers item to another inventory
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int objectid of the item to be transfered
	 * @param count : int Quantity of items to be transfered
	 * @param target
	 * @param actor : Player Player requesting the item transfer
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public ItemInstance transferItem(String process, int objectId, int count, ItemContainer target, Player actor, WorldObject reference)
	{
		if (target == null)
			return null;
		
		ItemInstance sourceitem = getItemByObjectId(objectId);
		if (sourceitem == null)
			return null;
		
		ItemInstance targetitem = sourceitem.isStackable() ? target.getItemByItemId(sourceitem.getItemId()) : null;
		
		synchronized (sourceitem)
		{
			// check if this item still present in this container
			if (getItemByObjectId(objectId) != sourceitem)
				return null;
			
			// Check if requested quantity is available
			if (count > sourceitem.getCount())
				count = sourceitem.getCount();
			
			// If possible, move entire item object
			if (sourceitem.getCount() == count && targetitem == null)
			{
				removeItem(sourceitem);
				target.addItem(process, sourceitem, actor, reference);
				targetitem = sourceitem;
			}
			else
			{
				if (sourceitem.getCount() > count) // If possible, only update counts
					sourceitem.changeCount(process, -count, actor, reference);
				else
				// Otherwise destroy old item
				{
					removeItem(sourceitem);
					sourceitem.destroyMe(process, actor, reference);
				}
				
				if (targetitem != null) // If possible, only update counts
					targetitem.changeCount(process, count, actor, reference);
				else
					// Otherwise add new item
					targetitem = target.addItem(process, sourceitem.getItemId(), count, actor, reference);
			}
			
			// Updates database
			sourceitem.updateDatabase();
			
			if (targetitem != sourceitem && targetitem != null)
				targetitem.updateDatabase();
			
			if (sourceitem.isAugmented())
				sourceitem.getAugmentation().removeBonus(actor);
			
			refreshWeight();
			target.refreshWeight();
		}
		return targetitem;
	}
	
	/**
	 * Destroy item from inventory and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param item : ItemInstance to be destroyed
	 * @param actor : Player Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	public ItemInstance destroyItem(String process, ItemInstance item, Player actor, WorldObject reference)
	{
		return destroyItem(process, item, item.getCount(), actor, reference);
	}
	
	/**
	 * Destroy item from inventory and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param item : ItemInstance to be destroyed
	 * @param count
	 * @param actor : Player Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	public ItemInstance destroyItem(String process, ItemInstance item, int count, Player actor, WorldObject reference)
	{
		synchronized (item)
		{
			// Adjust item quantity
			if (item.getCount() > count)
			{
				item.changeCount(process, -count, actor, reference);
				item.setLastChange(ItemState.MODIFIED);
				
				// don't update often for untraced items
				if (process != null || Rnd.get(10) == 0)
					item.updateDatabase();
				
				refreshWeight();
				
				return item;
			}
			
			if (item.getCount() < count)
				return null;
			
			boolean removed = removeItem(item);
			if (!removed)
				return null;
			
			item.destroyMe(process, actor, reference);
			
			item.updateDatabase();
			refreshWeight();
		}
		return item;
	}
	
	/**
	 * Destroy item from inventory by using its <B>objectID</B> and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param actor : Player Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	public ItemInstance destroyItem(String process, int objectId, int count, Player actor, WorldObject reference)
	{
		ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
			return null;
		
		return destroyItem(process, item, count, actor, reference);
	}
	
	/**
	 * Destroy item from inventory by using its <B>itemId</B> and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param actor : Player Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	public ItemInstance destroyItemByItemId(String process, int itemId, int count, Player actor, WorldObject reference)
	{
		ItemInstance item = getItemByItemId(itemId);
		if (item == null)
			return null;
		
		return destroyItem(process, item, count, actor, reference);
	}
	
	/**
	 * Destroy all items from inventory and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param actor : Player Player requesting the item destroy
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void destroyAllItems(String process, Player actor, WorldObject reference)
	{
		for (ItemInstance item : _items)
			destroyItem(process, item, actor, reference);
	}
	
	/**
	 * @return the amount of adena (itemId 57)
	 */
	public int getAdena()
	{
		for (ItemInstance item : _items)
		{
			if (item.getItemId() == 57)
				return item.getCount();
		}
		return 0;
	}
	
	/**
	 * Adds item to inventory for further adjustments.
	 * @param item : ItemInstance to be added from inventory
	 */
	protected void addItem(ItemInstance item)
	{
		item.actualizeTime();
		
		_items.add(item);
	}
	
	/**
	 * Removes item from inventory for further adjustments.
	 * @param item : ItemInstance to be removed from inventory
	 * @return
	 */
	protected boolean removeItem(ItemInstance item)
	{
		return _items.remove(item);
	}
	
	/**
	 * Refresh the weight of equipment loaded
	 */
	protected void refreshWeight()
	{
	}
	
	/**
	 * Delete item object from world
	 */
	public void deleteMe()
	{
		if (getOwner() != null)
		{
			for (ItemInstance item : _items)
			{
				item.updateDatabase();
				World.getInstance().removeObject(item);
			}
		}
		_items.clear();
	}
	
	/**
	 * Update database with items in inventory
	 */
	public void updateDatabase()
	{
		if (getOwner() != null)
		{
			for (ItemInstance item : _items)
				item.updateDatabase();
		}
	}
	
	/**
	 * Get back items in container from database
	 */
	public void restore()
	{
		final Player owner = (getOwner() == null) ? null : getOwner().getActingPlayer();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(RESTORE_ITEMS))
		{
			ps.setInt(1, getOwnerId());
			ps.setString(2, getBaseLocation().name());
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					// Restore the item.
					final ItemInstance item = ItemInstance.restoreFromDb(getOwnerId(), rs);
					if (item == null)
						continue;
					
					// Add the item to world objects list.
					World.getInstance().addObject(item);
					
					// If stackable item is found in inventory just add to current quantity
					if (item.isStackable() && getItemByItemId(item.getItemId()) != null)
						addItem("Restore", item, owner, null);
					else
						addItem(item);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't restore container for {}.", e, getOwnerId());
		}
		refreshWeight();
	}
	
	public boolean validateCapacity(int slots)
	{
		return true;
	}
	
	public boolean validateWeight(int weight)
	{
		return true;
	}
}
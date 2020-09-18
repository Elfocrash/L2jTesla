package dev.l2j.tesla.gameserver.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.gameserver.data.manager.CastleManager;
import dev.l2j.tesla.gameserver.data.manager.CursedWeaponManager;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.entity.Castle;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

/**
 * Destroys item on ground after specified time. When server is about to shutdown/restart, saves all dropped items in to SQL. Loads them during server start.
 */
public final class ItemsOnGroundTaskManager implements Runnable
{
	private static final CLogger LOGGER = new CLogger(ItemsOnGroundTaskManager.class.getName());
	
	private static final String LOAD_ITEMS = "SELECT object_id,item_id,count,enchant_level,x,y,z,time FROM items_on_ground";
	private static final String DELETE_ITEMS = "DELETE FROM items_on_ground";
	private static final String SAVE_ITEMS = "INSERT INTO items_on_ground(object_id,item_id,count,enchant_level,x,y,z,time) VALUES(?,?,?,?,?,?,?,?)";
	
	private final Map<ItemInstance, Long> _items = new ConcurrentHashMap<>();
	
	public ItemsOnGroundTaskManager()
	{
		// Run task each 5 seconds.
		ThreadPool.scheduleAtFixedRate(this, 5000, 5000);
		
		// Load all items.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement st = con.prepareStatement(LOAD_ITEMS);
             PreparedStatement st2 = con.prepareStatement(DELETE_ITEMS);
             ResultSet rs = st.executeQuery())
		{
			// Get current time.
			final long time = System.currentTimeMillis();
			
			while (rs.next())
			{
				// Create new item.
				final ItemInstance item = new ItemInstance(rs.getInt(1), rs.getInt(2));
				World.getInstance().addObject(item);
				
				// Check and set count.
				final int count = rs.getInt(3);
				if (item.isStackable() && count > 1)
					item.setCount(count);
				
				// Check and set enchant.
				final int enchant = rs.getInt(4);
				if (enchant > 0)
					item.setEnchantLevel(enchant);
				
				// Spawn item in the world.
				item.spawnMe(rs.getInt(5), rs.getInt(6), rs.getInt(7));
				
				// If item is on a Castle ground, verify if it's an allowed ticket. If yes, add it to associated castle.
				final Castle castle = CastleManager.getInstance().getCastle(item);
				if (castle != null && castle.getTicket(item.getItemId()) != null)
					castle.addDroppedTicket(item);
				
				// Get interval, add item to the list.
				final long interval = rs.getLong(8);
				_items.put(item, (interval == 0) ? 0L : time + interval);
			}
			
			// Delete all items from database.
			st2.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Error while loading items on ground data.", e);
		}
		LOGGER.info("Restored {} items on ground.", _items.size());
	}
	
	@Override
	public final void run()
	{
		// List is empty, skip.
		if (_items.isEmpty())
			return;
		
		// Get current time.
		final long time = System.currentTimeMillis();
		
		// Loop all items.
		for (Entry<ItemInstance, Long> entry : _items.entrySet())
		{
			// Get and validate destroy time.
			final long destroyTime = entry.getValue();
			
			// Item can't be destroyed or time hasn't passed yet, skip.
			if (destroyTime == 0 || time < destroyTime)
				continue;
			
			// Destroy item and remove from task.
			final ItemInstance item = entry.getKey();
			item.decayMe();
		}
	}
	
	/**
	 * Adds {@link ItemInstance} to the ItemAutoDestroyTask.
	 * @param item : {@link ItemInstance} to be added and checked.
	 * @param actor : {@link Creature} who dropped the item.
	 */
	public final void add(ItemInstance item, Creature actor)
	{
		// Actor doesn't exist or item is protected, don't bother with the item (e.g. Cursed Weapons).
		if (actor == null || item.isDestroyProtected())
			return;
		
		long dropTime = 0;
		
		// Item has special destroy time, use it.
		Integer special = Config.SPECIAL_ITEM_DESTROY_TIME.get(item.getItemId());
		if (special != null)
			dropTime = special;
		// Get base destroy time for herbs, items, equipable items.
		else if (item.isHerb())
			dropTime = Config.HERB_AUTO_DESTROY_TIME;
		else if (item.isEquipable())
			dropTime = Config.EQUIPABLE_ITEM_AUTO_DESTROY_TIME;
		else
		{
			// If item is on a Castle ground, verify if it's an allowed ticket. If yes, the associated timer is always 0.
			final Castle castle = CastleManager.getInstance().getCastle(item);
			dropTime = (castle != null && castle.getTicket(item.getItemId()) != null) ? 0 : Config.ITEM_AUTO_DESTROY_TIME;
		}
		
		// Item was dropped by playable, apply the multiplier.
		if (actor instanceof Playable)
			dropTime *= Config.PLAYER_DROPPED_ITEM_MULTIPLIER;
		
		// If drop time exists, set real drop time.
		if (dropTime != 0)
			dropTime += System.currentTimeMillis();
		
		// Put item to drop list.
		_items.put(item, dropTime);
	}
	
	/**
	 * Removes {@link ItemInstance} from the ItemAutoDestroyTask.
	 * @param item : {@link ItemInstance} to be removed.
	 */
	public final void remove(ItemInstance item)
	{
		_items.remove(item);
	}
	
	public final void save()
	{
		// List is empty, return.
		if (_items.isEmpty())
		{
			LOGGER.info("No items on ground to save.");
			return;
		}
		
		// Store whole items list to database.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(SAVE_ITEMS))
		{
			// Get current time.
			final long time = System.currentTimeMillis();
			
			for (Entry<ItemInstance, Long> entry : _items.entrySet())
			{
				// Get item and destroy time interval.
				final ItemInstance item = entry.getKey();
				
				// Cursed Items not saved to ground, prevent double save.
				if (CursedWeaponManager.getInstance().isCursed(item.getItemId()))
					continue;
				
				st.setInt(1, item.getObjectId());
				st.setInt(2, item.getItemId());
				st.setInt(3, item.getCount());
				st.setInt(4, item.getEnchantLevel());
				st.setInt(5, item.getX());
				st.setInt(6, item.getY());
				st.setInt(7, item.getZ());
				
				final long left = entry.getValue();
				st.setLong(8, (left == 0) ? 0 : left - time);
				
				st.addBatch();
			}
			st.executeBatch();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't save items on ground.", e);
		}
		LOGGER.info("Saved {} items on ground.", _items.size());
	}
	
	public static final ItemsOnGroundTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemsOnGroundTaskManager INSTANCE = new ItemsOnGroundTaskManager();
	}
}
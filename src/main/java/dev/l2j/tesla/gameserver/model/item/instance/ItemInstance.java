package dev.l2j.tesla.gameserver.model.item.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.idfactory.IdFactory;
import dev.l2j.tesla.gameserver.model.L2Augmentation;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.entity.Castle;
import dev.l2j.tesla.gameserver.model.item.MercenaryTicket;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.skills.basefuncs.Func;
import dev.l2j.tesla.gameserver.taskmanager.ItemsOnGroundTaskManager;
import dev.l2j.tesla.commons.concurrent.ThreadPool;

import dev.l2j.tesla.gameserver.data.ItemTable;
import dev.l2j.tesla.gameserver.data.manager.CastleManager;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.items.EtcItemType;
import dev.l2j.tesla.gameserver.enums.items.ItemType;
import dev.l2j.tesla.gameserver.enums.items.ShotType;
import dev.l2j.tesla.gameserver.model.item.kind.Armor;
import dev.l2j.tesla.gameserver.model.item.kind.EtcItem;
import dev.l2j.tesla.gameserver.model.item.kind.Item;
import dev.l2j.tesla.gameserver.model.item.kind.Weapon;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.DropItem;
import dev.l2j.tesla.gameserver.network.serverpackets.GetItem;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.SpawnItem;

/**
 * This class manages items.
 */
public final class ItemInstance extends WorldObject implements Runnable, Comparable<ItemInstance>
{
	private static final Logger ITEM_LOG = Logger.getLogger("item");
	
	private static final String DELETE_AUGMENTATION = "DELETE FROM augmentations WHERE item_id = ?";
	private static final String RESTORE_AUGMENTATION = "SELECT attributes,skill_id,skill_level FROM augmentations WHERE item_id=?";
	private static final String UPDATE_AUGMENTATION = "REPLACE INTO augmentations VALUES(?,?,?,?)";
	
	private static final String UPDATE_ITEM = "UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,custom_type1=?,custom_type2=?,mana_left=?,time=? WHERE object_id = ?";
	private static final String INSERT_ITEM = "INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	private static final String DELETE_ITEM = "DELETE FROM items WHERE object_id=?";
	
	private static final String DELETE_PET_ITEM = "DELETE FROM pets WHERE item_obj_id=?";
	
	public static enum ItemState
	{
		UNCHANGED,
		ADDED,
		MODIFIED,
		REMOVED
	}
	
	public static enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		PET,
		PET_EQUIP,
		LEASE,
		FREIGHT
	}
	
	private static final long REGULAR_LOOT_PROTECTION_TIME = 15000;
	private static final long RAID_LOOT_PROTECTION_TIME = 300000;
	
	private int _ownerId;
	private int _dropperObjectId = 0;
	
	private int _count;
	private int _initCount;
	
	private long _time;
	private boolean _decrease = false;
	
	private final int _itemId;
	private final Item _item;
	
	/** Location of the item : Inventory, PaperDoll, WareHouse */
	private ItemLocation _loc;
	
	/** Slot where item is stored */
	private int _locData;
	
	private int _enchantLevel;
	
	private L2Augmentation _augmentation = null;
	
	/** Shadow item */
	private int _mana = -1;
	
	/** Custom item types (used loto, race tickets) */
	private int _type1;
	private int _type2;
	
	private boolean _destroyProtected;
	
	private ItemState _lastChange = ItemState.MODIFIED;
	
	private boolean _existsInDb; // if a record exists in DB.
	private boolean _storedInDb; // if DB data is up-to-date.
	
	private final ReentrantLock _dbLock = new ReentrantLock();
	private ScheduledFuture<?> _dropProtection;
	
	private int _shotsMask = 0;
	
	/**
	 * Constructor of the ItemInstance from the objectId and the itemId.
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId : int designating the ID of the item
	 */
	public ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		_itemId = itemId;
		_item = ItemTable.getInstance().getTemplate(itemId);
		
		if (_itemId == 0 || _item == null)
			throw new IllegalArgumentException();
		
		super.setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_type1 = 0;
		_type2 = 0;
		_mana = _item.getDuration() * 60;
	}
	
	/**
	 * Constructor of the ItemInstance from the objetId and the description of the item given by the L2Item.
	 * @param objectId : int designating the ID of the object in the world
	 * @param item : L2Item containing informations of the item
	 */
	public ItemInstance(int objectId, Item item)
	{
		super(objectId);
		_itemId = item.getItemId();
		_item = item;
		
		setName(_item.getName());
		setCount(1);
		
		_loc = ItemLocation.VOID;
		_mana = _item.getDuration() * 60;
	}
	
	@Override
	public synchronized void run()
	{
		_ownerId = 0;
		_dropProtection = null;
	}
	
	/**
	 * Sets the ownerID of the item
	 * @param process : String Identifier of process triggering this action
	 * @param owner_id : int designating the ID of the owner
	 * @param creator : Player Player requesting the item creation
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void setOwnerId(String process, int owner_id, Player creator, WorldObject reference)
	{
		setOwnerId(owner_id);
		
		if (Config.LOG_ITEMS)
		{
			final LogRecord record = new LogRecord(Level.INFO, "CHANGE:" + process);
			record.setLoggerName("item");
			record.setParameters(new Object[]
			{
				creator,
				this,
				reference
			});
			ITEM_LOG.log(record);
		}
	}
	
	/**
	 * Sets the ownerID of the item
	 * @param owner_id : int designating the ID of the owner
	 */
	public void setOwnerId(int owner_id)
	{
		if (owner_id == _ownerId)
			return;
		
		_ownerId = owner_id;
		_storedInDb = false;
	}
	
	/**
	 * Returns the ownerID of the item
	 * @return int : ownerID of the item
	 */
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	/**
	 * Sets the location of the item
	 * @param loc : ItemLocation (enumeration)
	 */
	public void setLocation(ItemLocation loc)
	{
		setLocation(loc, 0);
	}
	
	/**
	 * Sets the location of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param loc : ItemLocation (enumeration)
	 * @param loc_data : int designating the slot where the item is stored or the village for freights
	 */
	public void setLocation(ItemLocation loc, int loc_data)
	{
		if (loc == _loc && loc_data == _locData)
			return;
		
		_loc = loc;
		_locData = loc_data;
		_storedInDb = false;
	}
	
	public ItemLocation getLocation()
	{
		return _loc;
	}
	
	/**
	 * Sets the quantity of the item.<BR>
	 * <BR>
	 * @param count the new count to set
	 */
	public void setCount(int count)
	{
		if (getCount() == count)
			return;
		
		_count = count >= -1 ? count : 0;
		_storedInDb = false;
	}
	
	/**
	 * Returns the quantity of item
	 * @return int
	 */
	public int getCount()
	{
		return _count;
	}
	
	/**
	 * Sets the quantity of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param process : String Identifier of process triggering this action
	 * @param count : int
	 * @param creator : Player Player requesting the item creation
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void changeCount(String process, int count, Player creator, WorldObject reference)
	{
		if (count == 0)
			return;
		
		if (count > 0 && getCount() > Integer.MAX_VALUE - count)
			setCount(Integer.MAX_VALUE);
		else
			setCount(getCount() + count);
		
		if (getCount() < 0)
			setCount(0);
		
		_storedInDb = false;
		
		if (Config.LOG_ITEMS && process != null)
		{
			final LogRecord record = new LogRecord(Level.INFO, "CHANGE:" + process);
			record.setLoggerName("item");
			record.setParameters(new Object[]
			{
				creator,
				this,
				reference
			});
			ITEM_LOG.log(record);
		}
	}
	
	/**
	 * Returns if item is equipable
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return !(_item.getBodyPart() == 0 || _item.getItemType() == EtcItemType.ARROW || _item.getItemType() == EtcItemType.LURE);
	}
	
	/**
	 * Returns if item is equipped
	 * @return boolean
	 */
	public boolean isEquipped()
	{
		return _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP;
	}
	
	/**
	 * Returns the slot where the item is stored
	 * @return int
	 */
	public int getLocationSlot()
	{
		assert _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP || _loc == ItemLocation.FREIGHT;
		return _locData;
	}
	
	/**
	 * Returns the characteristics of the item
	 * @return L2Item
	 */
	public Item getItem()
	{
		return _item;
	}
	
	public int getCustomType1()
	{
		return _type1;
	}
	
	public int getCustomType2()
	{
		return _type2;
	}
	
	public void setCustomType1(int newtype)
	{
		_type1 = newtype;
	}
	
	public void setCustomType2(int newtype)
	{
		_type2 = newtype;
	}
	
	public boolean isOlyRestrictedItem()
	{
		return getItem().isOlyRestrictedItem();
	}
	
	/**
	 * Returns the type of item
	 * @return Enum
	 */
	public ItemType getItemType()
	{
		return _item.getItemType();
	}
	
	/**
	 * Returns the ID of the item
	 * @return int
	 */
	public int getItemId()
	{
		return _itemId;
	}
	
	/**
	 * Returns true if item is an EtcItem
	 * @return boolean
	 */
	public boolean isEtcItem()
	{
		return (_item instanceof EtcItem);
	}
	
	/**
	 * Returns true if item is a Weapon/Shield
	 * @return boolean
	 */
	public boolean isWeapon()
	{
		return (_item instanceof Weapon);
	}
	
	/**
	 * Returns true if item is an Armor
	 * @return boolean
	 */
	public boolean isArmor()
	{
		return (_item instanceof Armor);
	}
	
	/**
	 * Returns the characteristics of the L2EtcItem
	 * @return EtcItem
	 */
	public EtcItem getEtcItem()
	{
		if (_item instanceof EtcItem)
			return (EtcItem) _item;
		
		return null;
	}
	
	/**
	 * Returns the characteristics of the Weapon
	 * @return Weapon
	 */
	public Weapon getWeaponItem()
	{
		if (_item instanceof Weapon)
			return (Weapon) _item;
		
		return null;
	}
	
	/**
	 * Returns the characteristics of the L2Armor
	 * @return Armor
	 */
	public Armor getArmorItem()
	{
		if (_item instanceof Armor)
			return (Armor) _item;
		
		return null;
	}
	
	/**
	 * Returns the quantity of crystals for crystallization
	 * @return int
	 */
	public final int getCrystalCount()
	{
		return _item.getCrystalCount(_enchantLevel);
	}
	
	/**
	 * @return the reference price of the item.
	 */
	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}
	
	/**
	 * @return the name of the item.
	 */
	public String getItemName()
	{
		return _item.getName();
	}
	
	/**
	 * @return the last change of the item.
	 */
	public ItemState getLastChange()
	{
		return _lastChange;
	}
	
	/**
	 * Sets the last change of the item
	 * @param lastChange : int
	 */
	public void setLastChange(ItemState lastChange)
	{
		_lastChange = lastChange;
	}
	
	/**
	 * @return if item is stackable.
	 */
	public boolean isStackable()
	{
		return _item.isStackable();
	}
	
	/**
	 * @return if item is dropable.
	 */
	public boolean isDropable()
	{
		return isAugmented() ? false : _item.isDropable();
	}
	
	/**
	 * @return if item is destroyable.
	 */
	public boolean isDestroyable()
	{
		return isQuestItem() ? false : _item.isDestroyable();
	}
	
	/**
	 * @return if item is tradable
	 */
	public boolean isTradable()
	{
		return isAugmented() ? false : _item.isTradable();
	}
	
	/**
	 * @return if item is sellable.
	 */
	public boolean isSellable()
	{
		return isAugmented() ? false : _item.isSellable();
	}
	
	/**
	 * @param isPrivateWareHouse : make additionals checks on tradable / shadow items.
	 * @return if item can be deposited in warehouse or freight.
	 */
	public boolean isDepositable(boolean isPrivateWareHouse)
	{
		// equipped, hero and quest items
		if (isEquipped() || !_item.isDepositable())
			return false;
		
		if (!isPrivateWareHouse)
		{
			// augmented not tradable
			if (!isTradable() || isShadowItem())
				return false;
		}
		return true;
	}
	
	/**
	 * @return if item is consumable.
	 */
	public boolean isConsumable()
	{
		return _item.isConsumable();
	}
	
	/**
	 * @param player : the player to check.
	 * @param allowAdena : if true, count adenas.
	 * @param allowNonTradable : if true, count non tradable items.
	 * @return if item is available for manipulation.
	 */
	public boolean isAvailable(Player player, boolean allowAdena, boolean allowNonTradable)
	{
		return ((!isEquipped()) // Not equipped
			&& (getItem().getType2() != Item.TYPE2_QUEST) // Not Quest Item
			&& (getItem().getType2() != Item.TYPE2_MONEY || getItem().getType1() != Item.TYPE1_SHIELD_ARMOR) // not money, not shield
			&& (player.getSummon() == null || getObjectId() != player.getSummon().getControlItemId()) // Not Control item of currently summoned pet
			&& (player.getActiveEnchantItem() != this) // Not momentarily used enchant scroll
			&& (allowAdena || getItemId() != 57) // Not adena
			&& (player.getCurrentSkill().getSkill() == null || player.getCurrentSkill().getSkill().getItemConsumeId() != getItemId()) && (!player.isCastingSimultaneouslyNow() || player.getLastSimultaneousSkillCast() == null || player.getLastSimultaneousSkillCast().getItemConsumeId() != getItemId()) && (allowNonTradable || isTradable()));
	}
	
	@Override
	public void onAction(Player player)
	{
		if (player.isFlying())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Mercenaries tickets case.
		if (_item.getItemType() == EtcItemType.CASTLE_GUARD)
		{
			if (player.isInParty())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			final Castle castle = CastleManager.getInstance().getCastle(player);
			if (castle == null)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			final MercenaryTicket ticket = castle.getTicket(_itemId);
			if (ticket == null)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!player.isCastleLord(castle.getCastleId()))
			{
				player.sendPacket(SystemMessageId.THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_CANNOT_CANCEL_POSITIONING);
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		player.getAI().setIntention(IntentionType.PICK_UP, this);
	}
	
	@Override
	public void onActionShift(Player player)
	{
		if (player.isGM())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/admin/iteminfo.htm");
			html.replace("%objid%", getObjectId());
			html.replace("%itemid%", getItemId());
			html.replace("%ownerid%", getOwnerId());
			html.replace("%loc%", getLocation().toString());
			html.replace("%class%", getClass().getSimpleName());
			player.sendPacket(html);
		}
		super.onActionShift(player);
	}
	
	/**
	 * @return the level of enchantment of the item.
	 */
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	/**
	 * Sets the level of enchantment of the item
	 * @param enchantLevel : number to apply.
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		if (_enchantLevel == enchantLevel)
			return;
		
		_enchantLevel = enchantLevel;
		_storedInDb = false;
	}
	
	/**
	 * @return whether this item is augmented or not ; true if augmented.
	 */
	public boolean isAugmented()
	{
		return _augmentation != null;
	}
	
	/**
	 * @return the augmentation object for this item.
	 */
	public L2Augmentation getAugmentation()
	{
		return _augmentation;
	}
	
	/**
	 * Sets a new augmentation.
	 * @param augmentation : the augmentation object to apply.
	 * @return return true if successfull.
	 */
	public boolean setAugmentation(L2Augmentation augmentation)
	{
		// there shall be no previous augmentation..
		if (_augmentation != null)
			return false;
		
		_augmentation = augmentation;
		updateItemAttributes();
		return true;
	}
	
	/**
	 * Remove the augmentation.
	 */
	public void removeAugmentation()
	{
		if (_augmentation == null)
			return;
		
		_augmentation = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			 PreparedStatement ps = con.prepareStatement(DELETE_AUGMENTATION))
		{
			ps.setInt(1, getObjectId());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't remove augmentation for {}.", e, toString());
		}
	}
	
	private void restoreAttributes()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(RESTORE_AUGMENTATION))
		{
			ps.setInt(1, getObjectId());
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
					_augmentation = new L2Augmentation(rs.getInt("attributes"), rs.getInt("skill_id"), rs.getInt("skill_level"));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't restore augmentation for {}.", e, toString());
		}
	}
	
	private void updateItemAttributes()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_AUGMENTATION))
		{
			ps.setInt(1, getObjectId());
			
			if (_augmentation == null)
			{
				ps.setInt(2, -1);
				ps.setInt(3, -1);
				ps.setInt(4, -1);
			}
			else
			{
				ps.setInt(2, _augmentation.getAttributes());
				
				if (_augmentation.getSkill() == null)
				{
					ps.setInt(3, 0);
					ps.setInt(4, 0);
				}
				else
				{
					ps.setInt(3, _augmentation.getSkill().getId());
					ps.setInt(4, _augmentation.getSkill().getLevel());
				}
			}
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't update attributes for {}.", e, toString());
		}
	}
	
	/**
	 * @return true if this item is a shadow item. Shadow items have a limited life-time.
	 */
	public boolean isShadowItem()
	{
		return _mana >= 0;
	}
	
	/**
	 * Sets the mana for this shadow item.
	 * @param period
	 * @return return remaining mana of this shadow item
	 */
	public int decreaseMana(int period)
	{
		_storedInDb = false;
		
		return _mana -= period;
	}
	
	/**
	 * @return the remaining mana of this shadow item (left life-time).
	 */
	public int getMana()
	{
		return _mana / 60;
	}
	
	/**
	 * Returns false cause item can't be attacked
	 * @return boolean false
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	/**
	 * This function basically returns a set of functions from L2Item/L2Armor/Weapon, but may add additional functions, if this particular item instance is enhanched for a particular player.
	 * @param player : Creature designating the player
	 * @return Func[]
	 */
	public List<Func> getStatFuncs(Creature player)
	{
		return getItem().getStatFuncs(this, player);
	}
	
	/**
	 * Updates database.<BR>
	 * <BR>
	 * <U><I>Concept : </I></U><BR>
	 * <B>IF</B> the item exists in database :
	 * <UL>
	 * <LI><B>IF</B> the item has no owner, or has no location, or has a null quantity : remove item from database</LI>
	 * <LI><B>ELSE</B> : update item in database</LI>
	 * </UL>
	 * <B> Otherwise</B> :
	 * <UL>
	 * <LI><B>IF</B> the item hasn't a null quantity, and has a correct location, and has a correct owner : insert item in database</LI>
	 * </UL>
	 */
	public void updateDatabase()
	{
		_dbLock.lock();
		
		try
		{
			if (_existsInDb)
			{
				if (_ownerId == 0 || _loc == ItemLocation.VOID || (getCount() == 0 && _loc != ItemLocation.LEASE))
					removeFromDb();
				else
					updateInDb();
			}
			else
			{
				if (_ownerId == 0 || _loc == ItemLocation.VOID || (getCount() == 0 && _loc != ItemLocation.LEASE))
					return;
				
				insertIntoDb();
			}
		}
		finally
		{
			_dbLock.unlock();
		}
	}
	
	/**
	 * @param ownerId : objectID of the owner.
	 * @param rs : the ResultSet of the item.
	 * @return a ItemInstance stored in database from its objectID
	 */
	public static ItemInstance restoreFromDb(int ownerId, ResultSet rs)
	{
		ItemInstance inst = null;
		int objectId, itemId, slot, enchant, type1, type2, manaLeft, count;
		long time;
		ItemLocation loc;
		
		try
		{
			objectId = rs.getInt(1);
			itemId = rs.getInt("item_id");
			count = rs.getInt("count");
			loc = ItemLocation.valueOf(rs.getString("loc"));
			slot = rs.getInt("loc_data");
			enchant = rs.getInt("enchant_level");
			type1 = rs.getInt("custom_type1");
			type2 = rs.getInt("custom_type2");
			manaLeft = rs.getInt("mana_left");
			time = rs.getLong("time");
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't restore an item owned by {}.", e, ownerId);
			return null;
		}
		
		final Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
			return null;
		
		inst = new ItemInstance(objectId, item);
		inst._ownerId = ownerId;
		inst.setCount(count);
		inst._enchantLevel = enchant;
		inst._type1 = type1;
		inst._type2 = type2;
		inst._loc = loc;
		inst._locData = slot;
		inst._existsInDb = true;
		inst._storedInDb = true;
		
		// Setup life time for shadow weapons
		inst._mana = manaLeft;
		inst._time = time;
		
		// load augmentation
		if (inst.isEquipable())
			inst.restoreAttributes();
		
		return inst;
	}
	
	/**
	 * Init a dropped ItemInstance and add it in the world as a visible object.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _objects of World </B></FONT><BR>
	 * <BR>
	 * @param dropper : the character who dropped the item.
	 * @param x : X location of the item.
	 * @param y : Y location of the item.
	 * @param z : Z location of the item.
	 */
	public final void dropMe(Creature dropper, int x, int y, int z)
	{
		ThreadPool.execute(new ItemDropTask(this, dropper, x, y, z));
	}
	
	public class ItemDropTask implements Runnable
	{
		private int _x, _y, _z;
		private final Creature _dropper;
		private final ItemInstance _itm;
		
		public ItemDropTask(ItemInstance item, Creature dropper, int x, int y, int z)
		{
			_x = x;
			_y = y;
			_z = z;
			_dropper = dropper;
			_itm = item;
		}
		
		@Override
		public final void run()
		{
			assert _itm.getRegion() == null;
			
			if (_dropper != null)
			{
				Location dropDest = GeoEngine.getInstance().canMoveToTargetLoc(_dropper.getX(), _dropper.getY(), _dropper.getZ(), _x, _y, _z);
				_x = dropDest.getX();
				_y = dropDest.getY();
				_z = dropDest.getZ();
			}
			
			_itm.setDropperObjectId(_dropper != null ? _dropper.getObjectId() : 0); // Set the dropper Id for the knownlist packets in sendInfo
			_itm.spawnMe(_x, _y, _z);
			
			ItemsOnGroundTaskManager.getInstance().add(_itm, _dropper);
			
			_itm.setDropperObjectId(0); // Set the dropper Id back to 0 so it no longer shows the drop packet
		}
	}
	
	/**
	 * Remove a ItemInstance from the visible world and send server->client GetItem packets.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _objects of World.</B></FONT><BR>
	 * <BR>
	 * @param player Player that pick up the item
	 */
	public final void pickupMe(Creature player)
	{
		player.broadcastPacket(new GetItem(this, player.getObjectId()));
		
		// Unregister dropped ticket from castle, if that item is on a castle area and is a valid ticket.
		final Castle castle = CastleManager.getInstance().getCastle(player);
		if (castle != null && castle.getTicket(_itemId) != null)
			castle.removeDroppedTicket(this);
		
		if (!Config.DISABLE_TUTORIAL && (_itemId == 57 || _itemId == 6353))
		{
			Player actor = player.getActingPlayer();
			if (actor != null)
			{
				QuestState qs = actor.getQuestState("Tutorial");
				if (qs != null)
					qs.getQuest().notifyEvent("CE" + _itemId + "", null, actor);
			}
		}
		
		// Calls directly setRegion(null), we don't have to care about.
		setIsVisible(false);
	}
	
	/**
	 * Update the database with values of the item
	 */
	private void updateInDb()
	{
		assert _existsInDb;
		
		if (_storedInDb)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_ITEM))
		{
			ps.setInt(1, _ownerId);
			ps.setInt(2, getCount());
			ps.setString(3, _loc.name());
			ps.setInt(4, _locData);
			ps.setInt(5, getEnchantLevel());
			ps.setInt(6, getCustomType1());
			ps.setInt(7, getCustomType2());
			ps.setInt(8, _mana);
			ps.setLong(9, getTime());
			ps.setInt(10, getObjectId());
			ps.executeUpdate();
			
			_existsInDb = true;
			_storedInDb = true;
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't update {}. ", e, toString());
		}
	}
	
	/**
	 * Insert the item in database
	 */
	private void insertIntoDb()
	{
		assert !_existsInDb && getObjectId() != 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_ITEM))
		{
			ps.setInt(1, _ownerId);
			ps.setInt(2, _itemId);
			ps.setInt(3, getCount());
			ps.setString(4, _loc.name());
			ps.setInt(5, _locData);
			ps.setInt(6, getEnchantLevel());
			ps.setInt(7, getObjectId());
			ps.setInt(8, _type1);
			ps.setInt(9, _type2);
			ps.setInt(10, _mana);
			ps.setLong(11, getTime());
			ps.executeUpdate();
			
			_existsInDb = true;
			_storedInDb = true;
			
			if (_augmentation != null)
				updateItemAttributes();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't insert {}.", e, toString());
		}
	}
	
	/**
	 * Delete item from database
	 */
	private void removeFromDb()
	{
		assert _existsInDb;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement(DELETE_ITEM))
			{
				ps.setInt(1, getObjectId());
				ps.executeUpdate();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_AUGMENTATION))
			{
				ps.setInt(1, getObjectId());
				ps.executeUpdate();
			}
			
			_existsInDb = false;
			_storedInDb = false;
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete {}.", e, toString());
		}
	}
	
	/**
	 * @return the item in String format.
	 */
	@Override
	public String toString()
	{
		return "(" + getObjectId() + ") " + getName();
	}
	
	public synchronized boolean hasDropProtection()
	{
		return _dropProtection != null;
	}
	
	public synchronized void setDropProtection(int ownerId, boolean isRaidParty)
	{
		_ownerId = ownerId;
		_dropProtection = ThreadPool.schedule(this, (isRaidParty) ? RAID_LOOT_PROTECTION_TIME : REGULAR_LOOT_PROTECTION_TIME);
	}
	
	public synchronized void removeDropProtection()
	{
		if (_dropProtection != null)
		{
			_dropProtection.cancel(true);
			_dropProtection = null;
		}
		
		_ownerId = 0;
	}
	
	public void setDestroyProtected(boolean destroyProtected)
	{
		_destroyProtected = destroyProtected;
	}
	
	public boolean isDestroyProtected()
	{
		return _destroyProtected;
	}
	
	public boolean isNightLure()
	{
		return ((_itemId >= 8505 && _itemId <= 8513) || _itemId == 8485);
	}
	
	public void setCountDecrease(boolean decrease)
	{
		_decrease = decrease;
	}
	
	public boolean getCountDecrease()
	{
		return _decrease;
	}
	
	public void setInitCount(int InitCount)
	{
		_initCount = InitCount;
	}
	
	public int getInitCount()
	{
		return _initCount;
	}
	
	public void restoreInitCount()
	{
		if (_decrease)
			_count = _initCount;
	}
	
	public long getTime()
	{
		return _time;
	}
	
	public void actualizeTime()
	{
		_time = System.currentTimeMillis();
	}
	
	public boolean isPetItem()
	{
		return getItem().isPetItem();
	}
	
	public boolean isPotion()
	{
		return getItem().isPotion();
	}
	
	public boolean isElixir()
	{
		return getItem().isElixir();
	}
	
	public boolean isHerb()
	{
		return getItem().getItemType() == EtcItemType.HERB;
	}
	
	public boolean isHeroItem()
	{
		return getItem().isHeroItem();
	}
	
	public boolean isQuestItem()
	{
		return getItem().isQuestItem();
	}
	
	@Override
	public void decayMe()
	{
		ItemsOnGroundTaskManager.getInstance().remove(this);
		
		super.decayMe();
	}
	
	/**
	 * Create an {@link ItemInstance} corresponding to the itemId and count, add it to the server and logs the activity.
	 * @param itemId : The itemId of the item to be created.
	 * @param count : The quantity of items to be created for stackable items.
	 * @param actor : The {@link Player} requesting the item creation.
	 * @param reference : The {@link WorldObject} referencing current action like NPC selling item or previous item in transformation.
	 * @return a new ItemInstance corresponding to the itemId and count.
	 */
	public static ItemInstance create(int itemId, int count, Player actor, WorldObject reference)
	{
		// Create and Init the ItemInstance corresponding to the Item Identifier
		ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		
		// Add the ItemInstance object to _objects of World.
		World.getInstance().addObject(item);
		
		// Set Item parameters
		if (item.isStackable() && count > 1)
			item.setCount(count);
		
		if (Config.LOG_ITEMS)
		{
			final LogRecord record = new LogRecord(Level.INFO, "CREATE");
			record.setLoggerName("item");
			record.setParameters(new Object[]
			{
				actor,
				item,
				reference
			});
			ITEM_LOG.log(record);
		}
		
		return item;
	}
	
	/**
	 * Destroys this {@link ItemInstance} from server, and release its objectId.
	 * @param process : The identifier of process triggering this action (used by logs).
	 * @param actor : The {@link Player} requesting the item destruction.
	 * @param reference : The {@link WorldObject} referencing current action like NPC selling item or previous item in transformation.
	 */
	public void destroyMe(String process, Player actor, WorldObject reference)
	{
		setCount(0);
		setOwnerId(0);
		setLocation(ItemLocation.VOID);
		setLastChange(ItemState.REMOVED);
		
		World.getInstance().removeObject(this);
		IdFactory.getInstance().releaseId(getObjectId());
		
		if (Config.LOG_ITEMS)
		{
			final LogRecord record = new LogRecord(Level.INFO, "DELETE:" + process);
			record.setLoggerName("item");
			record.setParameters(new Object[]
			{
				actor,
				this,
				reference
			});
			ITEM_LOG.log(record);
		}
		
		// if it's a pet control item, delete the pet as well
		if (getItemType() == EtcItemType.PET_COLLAR)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(DELETE_PET_ITEM))
			{
				ps.setInt(1, getObjectId());
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't delete {}.", e, toString());
			}
		}
	}
	
	public void setDropperObjectId(int id)
	{
		_dropperObjectId = id;
	}
	
	@Override
	public void sendInfo(Player activeChar)
	{
		if (_dropperObjectId != 0)
			activeChar.sendPacket(new DropItem(this, _dropperObjectId));
		else
			activeChar.sendPacket(new SpawnItem(this));
	}
	
	public List<Quest> getQuestEvents()
	{
		return _item.getQuestEvents();
	}
	
	@Override
	public boolean isChargedShot(ShotType type)
	{
		return (_shotsMask & type.getMask()) == type.getMask();
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged)
	{
		if (charged)
			_shotsMask |= type.getMask();
		else
			_shotsMask &= ~type.getMask();
	}
	
	public void unChargeAllShots()
	{
		_shotsMask = 0;
	}
	
	@Override
	public int compareTo(ItemInstance item)
	{
		final int time = Long.compare(item.getTime(), _time);
		if (time != 0)
			return time;
		
		return Integer.compare(item.getObjectId(), getObjectId());
	}
}
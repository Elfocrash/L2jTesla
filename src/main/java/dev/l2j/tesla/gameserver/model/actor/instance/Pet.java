package dev.l2j.tesla.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.handler.IItemHandler;
import dev.l2j.tesla.gameserver.handler.ItemHandler;
import dev.l2j.tesla.gameserver.idfactory.IdFactory;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.PetDataEntry;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.actor.template.PetTemplate;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.model.holder.Timestamp;
import dev.l2j.tesla.gameserver.model.itemcontainer.Inventory;
import dev.l2j.tesla.gameserver.model.itemcontainer.PetInventory;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.taskmanager.DecayTaskManager;
import dev.l2j.tesla.gameserver.taskmanager.ItemsOnGroundTaskManager;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.data.manager.CursedWeaponManager;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.LootRule;
import dev.l2j.tesla.gameserver.enums.ZoneId;
import dev.l2j.tesla.gameserver.enums.items.ArmorType;
import dev.l2j.tesla.gameserver.enums.items.EtcItemType;
import dev.l2j.tesla.gameserver.enums.items.WeaponType;
import dev.l2j.tesla.gameserver.model.actor.stat.PetStat;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.kind.Item;
import dev.l2j.tesla.gameserver.model.item.kind.Weapon;
import dev.l2j.tesla.gameserver.network.serverpackets.InventoryUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.PetInventoryUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.PetItemList;
import dev.l2j.tesla.gameserver.network.serverpackets.StatusUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.StopMove;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;

/**
 * A pet is a instance extending {@link Summon}, linked to a {@link Player}. A pet is different than a Servitor in multiple ways:
 * <ul>
 * <li>It got its own inventory</li>
 * <li>It can earn xp and levels</li>
 * <li>Their lifetime isn't limited (but they got a food gauge)</li>
 * </ul>
 * It can be mountable, like Wyverns or Striders. A children class of Pet, {@link BabyPet} can also buff their owner. Finally a last type of pet is the Sin Eater, a creature used to remove PK kills.
 */
public class Pet extends Summon
{
	private static final String LOAD_PET = "SELECT item_obj_id, name, level, curHp, curMp, exp, sp, fed FROM pets WHERE item_obj_id=?";
	private static final String STORE_PET = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,item_obj_id) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name),level=VALUES(level),curHp=VALUES(curHp),curMp=VALUES(curMp),exp=VALUES(exp),sp=VALUES(sp),fed=VALUES(fed)";
	private static final String DELETE_PET = "DELETE FROM pets WHERE item_obj_id=?";
	
	private final Map<Integer, Timestamp> _reuseTimeStamps = new ConcurrentHashMap<>();
	
	private final PetInventory _inventory;
	private final int _controlItemId;
	private final boolean _isMountable;
	
	private int _curFed;
	private int _curWeightPenalty = 0;
	
	private long _expBeforeDeath = 0;
	
	private Future<?> _feedTask;
	
	private PetDataEntry _petData;
	
	public Pet(int objectId, NpcTemplate template, Player owner, ItemInstance control)
	{
		super(objectId, template, owner);
		
		getPosition().set(owner.getX() + 50, owner.getY() + 100, owner.getZ());
		
		_inventory = new PetInventory(this);
		_controlItemId = control.getObjectId();
		_isMountable = template.getNpcId() == 12526 || template.getNpcId() == 12527 || template.getNpcId() == 12528 || template.getNpcId() == 12621;
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new PetStat(this));
	}
	
	@Override
	public PetStat getStat()
	{
		return (PetStat) super.getStat();
	}
	
	@Override
	public PetTemplate getTemplate()
	{
		return (PetTemplate) super.getTemplate();
	}
	
	@Override
	public PetInventory getInventory()
	{
		return _inventory;
	}
	
	@Override
	public int getControlItemId()
	{
		return _controlItemId;
	}
	
	@Override
	public boolean isMountable()
	{
		return _isMountable;
	}
	
	@Override
	public int getSummonType()
	{
		return 2;
	}
	
	@Override
	public void onAction(Player player)
	{
		// Refresh the Player owner reference if objectId is matching, but object isn't.
		if (player.getObjectId() == getOwner().getObjectId() && player != getOwner())
			setOwner(player);
		
		super.onAction(player);
	}
	
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return _inventory.getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}
	
	@Override
	public Weapon getActiveWeaponItem()
	{
		final ItemInstance weapon = getActiveWeaponInstance();
		if (weapon == null)
			return null;
		
		return (Weapon) weapon.getItem();
	}
	
	@Override
	public boolean destroyItem(String process, int objectId, int count, WorldObject reference, boolean sendMessage)
	{
		final ItemInstance item = _inventory.destroyItem(process, objectId, count, getOwner(), reference);
		if (item == null)
		{
			if (sendMessage)
				getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return false;
		}
		
		// Send Pet inventory update packet
		final PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		getOwner().sendPacket(petIU);
		
		if (sendMessage)
		{
			if (count > 1)
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item.getItemId()).addItemNumber(count));
			else
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item.getItemId()));
		}
		return true;
	}
	
	@Override
	public boolean destroyItemByItemId(String process, int itemId, int count, WorldObject reference, boolean sendMessage)
	{
		final ItemInstance item = _inventory.destroyItemByItemId(process, itemId, count, getOwner(), reference);
		if (item == null)
		{
			if (sendMessage)
				getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return false;
		}
		
		// Send Pet inventory update packet
		final PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		getOwner().sendPacket(petIU);
		
		if (sendMessage)
		{
			if (count > 1)
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item.getItemId()).addItemNumber(count));
			else
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item.getItemId()));
		}
		return true;
	}
	
	@Override
	public void doPickupItem(WorldObject object)
	{
		if (isDead())
			return;
		
		getAI().setIntention(IntentionType.IDLE);
		
		// The object must be an item.
		if (!(object instanceof ItemInstance))
			return;
		
		broadcastPacket(new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading()));
		
		final ItemInstance target = (ItemInstance) object;
		
		// Can't pickup cursed weapons.
		if (CursedWeaponManager.getInstance().isCursed(target.getItemId()))
		{
			getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target.getItemId()));
			return;
		}
		
		// Can't pickup shots and arrows.
		if (target.getItem().getItemType() == EtcItemType.ARROW || target.getItem().getItemType() == EtcItemType.SHOT)
		{
			getOwner().sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return;
		}
		
		synchronized (target)
		{
			if (!target.isVisible())
				return;
			
			if (!_inventory.validateCapacity(target))
			{
				getOwner().sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
				return;
			}
			
			if (!_inventory.validateWeight(target, target.getCount()))
			{
				getOwner().sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
				return;
			}
			
			if (target.getOwnerId() != 0 && !getOwner().isLooterOrInLooterParty(target.getOwnerId()))
			{
				if (target.getItemId() == 57)
					getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(target.getCount()));
				else if (target.getCount() > 1)
					getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(target.getItemId()).addNumber(target.getCount()));
				else
					getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target.getItemId()));
				
				return;
			}
			
			if (target.hasDropProtection())
				target.removeDropProtection();
			
			// If owner is in party and it isnt finders keepers, distribute the item instead of stealing it -.-
			final Party party = getOwner().getParty();
			if (party != null && party.getLootRule() != LootRule.ITEM_LOOTER)
				party.distributeItem(getOwner(), target);
			else
				target.pickupMe(this);
			
			// Item must be removed from ItemsOnGroundManager if it is active.
			ItemsOnGroundTaskManager.getInstance().remove(target);
		}
		
		// Auto use herbs - pick up
		if (target.getItemType() == EtcItemType.HERB)
		{
			final IItemHandler handler = ItemHandler.getInstance().getHandler(target.getEtcItem());
			if (handler != null)
				handler.useItem(this, target, false);
			
			target.destroyMe("Consume", getOwner(), null);
			broadcastStatusUpdate();
		}
		else
		{
			// if item is instance of L2ArmorType or WeaponType broadcast an "Attention" system message
			if (target.getItemType() instanceof ArmorType || target.getItemType() instanceof WeaponType)
			{
				SystemMessage msg;
				if (target.getEnchantLevel() > 0)
					msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PET_PICKED_UP_S2_S3).addCharName(getOwner()).addNumber(target.getEnchantLevel()).addItemName(target.getItemId());
				else
					msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PET_PICKED_UP_S2).addCharName(getOwner()).addItemName(target.getItemId());
				
				getOwner().broadcastPacketInRadius(msg, 1400);
			}
			
			SystemMessage sm2;
			if (target.getItemId() == 57)
				sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_ADENA).addItemNumber(target.getCount());
			else if (target.getEnchantLevel() > 0)
				sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_S2).addNumber(target.getEnchantLevel()).addItemName(target.getItemId());
			else if (target.getCount() > 1)
				sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S2_S1_S).addItemName(target.getItemId()).addItemNumber(target.getCount());
			else
				sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1).addItemName(target.getItemId());
			
			getOwner().sendPacket(sm2);
			getInventory().addItem("Pickup", target, getOwner(), this);
			getOwner().sendPacket(new PetItemList(this));
		}
		
		if (getFollowStatus())
			followOwner();
	}
	
	@Override
	public void deleteMe(Player owner)
	{
		getInventory().deleteMe();
		super.deleteMe(owner);
		destroyControlItem(owner); // this should also delete the pet from the db
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		stopFeed();
		getOwner().sendPacket(SystemMessageId.MAKE_SURE_YOU_RESSURECT_YOUR_PET_WITHIN_20_MINUTES);
		DecayTaskManager.getInstance().add(this, 1200);
		
		// Dont decrease exp if killed in duel or arena
		final Player owner = getOwner();
		if (owner != null && !owner.isInDuel() && (!isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.SIEGE)))
			deathPenalty();
		
		return true;
	}
	
	@Override
	public void doRevive()
	{
		getOwner().removeReviving();
		
		super.doRevive();
		
		// stopDecay
		DecayTaskManager.getInstance().cancel(this);
		startFeed();
		
		if (!checkHungryState())
			setRunning();
		
		getAI().setIntention(IntentionType.ACTIVE, null);
	}
	
	@Override
	public void doRevive(double revivePower)
	{
		// Restore the pet's lost experience depending on the % return of the skill used
		restoreExp(revivePower);
		doRevive();
	}
	
	@Override
	public final int getWeapon()
	{
		final ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (weapon != null)
			return weapon.getItemId();
		
		return 0;
	}
	
	@Override
	public final int getArmor()
	{
		final ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if (weapon != null)
			return weapon.getItemId();
		
		return 0;
	}
	
	@Override
	public void setName(String name)
	{
		final ItemInstance controlItem = getControlItem();
		if (controlItem.getCustomType2() == (name == null ? 1 : 0))
		{
			// Name isn't setted yet.
			controlItem.setCustomType2(name != null ? 1 : 0);
			controlItem.updateDatabase();
			
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(controlItem);
			getOwner().sendPacket(iu);
		}
		super.setName(name);
	}
	
	@Override
	public void store()
	{
		if (_controlItemId == 0)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(STORE_PET))
		{
			ps.setString(1, getName());
			ps.setInt(2, getStat().getLevel());
			ps.setDouble(3, getStatus().getCurrentHp());
			ps.setDouble(4, getStatus().getCurrentMp());
			ps.setLong(5, getStat().getExp());
			ps.setInt(6, getStat().getSp());
			ps.setInt(7, getCurrentFed());
			ps.setInt(8, _controlItemId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			WorldObject.LOGGER.error("Couldn't store pet data for {}.", e, getObjectId());
		}
		
		final ItemInstance itemInst = getControlItem();
		if (itemInst != null && itemInst.getEnchantLevel() != getStat().getLevel())
		{
			itemInst.setEnchantLevel(getStat().getLevel());
			itemInst.updateDatabase();
		}
	}
	
	@Override
	public synchronized void unSummon(Player owner)
	{
		// First, stop feed task.
		stopFeed();
		
		// Then drop inventory.
		if (!isDead() && getInventory() != null)
			getInventory().deleteMe();
		
		// Finally drop pet itself.
		super.unSummon(owner);
		
		// Drop pet from world's pet list.
		if (!isDead())
			World.getInstance().removePet(owner.getObjectId());
	}
	
	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		getStat().addExpAndSp(Math.round(addToExp * ((getNpcId() == 12564) ? Config.SINEATER_XP_RATE : Config.PET_XP_RATE)), addToSp);
	}
	
	@Override
	public long getExpForThisLevel()
	{
		return getStat().getExpForLevel(getLevel());
	}
	
	@Override
	public long getExpForNextLevel()
	{
		return getStat().getExpForLevel(getLevel() + 1);
	}
	
	@Override
	public final int getLevel()
	{
		return getStat().getLevel();
	}
	
	@Override
	public final int getSkillLevel(int skillId)
	{
		// Unknown skill. Return 0.
		if (getSkill(skillId) == null)
			return 0;
		
		// Max level for pet is 80, max level for pet skills is 12 => ((80 - 8) / 6) = 12.
		return Math.max(1, Math.min((getLevel() - 8) / 6, SkillTable.getInstance().getMaxLevel(skillId)));
	}
	
	@Override
	public final int getMaxLoad()
	{
		return PetTemplate.MAX_LOAD;
	}
	
	@Override
	public int getSoulShotsPerHit()
	{
		return getPetData().getSsCount();
	}
	
	@Override
	public int getSpiritShotsPerHit()
	{
		return getPetData().getSpsCount();
	}
	
	@Override
	public void updateAndBroadcastStatus(int val)
	{
		refreshOverloaded();
		super.updateAndBroadcastStatus(val);
	}
	
	@Override
	public void addTimeStamp(L2Skill skill, long reuse)
	{
		_reuseTimeStamps.put(skill.getReuseHashCode(), new Timestamp(skill, reuse));
	}
	
	public Collection<Timestamp> getReuseTimeStamps()
	{
		return _reuseTimeStamps.values();
	}
	
	public Map<Integer, Timestamp> getReuseTimeStamp()
	{
		return _reuseTimeStamps;
	}
	
	public PetDataEntry getPetData()
	{
		return _petData;
	}
	
	public void setPetData(int level)
	{
		_petData = getTemplate().getPetDataEntry(level);
	}
	
	public ItemInstance getControlItem()
	{
		return getOwner().getInventory().getItemByObjectId(_controlItemId);
	}
	
	public int getCurrentFed()
	{
		return _curFed;
	}
	
	public void setCurrentFed(int num)
	{
		_curFed = Math.min(num, getPetData().getMaxMeal());
	}
	
	/**
	 * Transfers item to another inventory
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : ObjectId of the item to be transfered
	 * @param count : int Quantity of items to be transfered
	 * @param target : The Inventory to target
	 * @param actor : Player Player requesting the item transfer
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public ItemInstance transferItem(String process, int objectId, int count, Inventory target, Player actor, WorldObject reference)
	{
		final ItemInstance oldItem = checkItemManipulation(objectId, count);
		if (oldItem == null)
			return null;
		
		final boolean wasWorn = oldItem.isPetItem() && oldItem.isEquipped();
		
		final ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, actor, reference);
		if (newItem == null)
			return null;
		
		// Send pet inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		if (oldItem.getCount() > 0 && oldItem != newItem)
			petIU.addModifiedItem(oldItem);
		else
			petIU.addRemovedItem(oldItem);
		sendPacket(petIU);
		
		// Send player inventory update packet
		InventoryUpdate playerIU = new InventoryUpdate();
		if (newItem.getCount() > count)
			playerIU.addModifiedItem(newItem);
		else
			playerIU.addNewItem(newItem);
		sendPacket(playerIU);
		
		// Update player current load aswell
		StatusUpdate playerSU = new StatusUpdate(getOwner());
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, getOwner().getCurrentLoad());
		sendPacket(playerSU);
		
		if (wasWorn)
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_OFF_S1).addItemName(newItem));
		
		return newItem;
	}
	
	public ItemInstance checkItemManipulation(int objectId, int count)
	{
		final ItemInstance item = getInventory().getItemByObjectId(objectId);
		if (item == null)
			return null;
		
		if (count < 1 || (count > 1 && !item.isStackable()))
			return null;
		
		if (count > item.getCount())
			return null;
		
		return item;
	}
	
	/**
	 * Remove the {@link Pet} reference from {@link World}, then the control item from the {@link Player} owner inventory. Finally, delete the pet from database.
	 * @param owner : The owner from whose inventory we should delete the item.
	 */
	public void destroyControlItem(Player owner)
	{
		// Remove the pet instance from world.
		World.getInstance().removePet(owner.getObjectId());
		
		// Delete the item from owner inventory.
		owner.destroyItem("PetDestroy", _controlItemId, 1, getOwner(), false);
		
		// Delete the pet from the database.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_PET))
		{
			ps.setInt(1, _controlItemId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			WorldObject.LOGGER.error("Couldn't delete pet data for {}.", e, getObjectId());
		}
	}
	
	public static Pet restore(ItemInstance control, NpcTemplate template, Player owner)
	{
		Pet pet;
		if (template.isType("BabyPet"))
			pet = new BabyPet(IdFactory.getInstance().getNextId(), template, owner, control);
		else
			pet = new Pet(IdFactory.getInstance().getNextId(), template, owner, control);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement(LOAD_PET))
			{
				ps.setInt(1, control.getObjectId());
				
				try (ResultSet rs = ps.executeQuery())
				{
					if (rs.next())
					{
						pet.setName(rs.getString("name"));
						
						pet.getStat().setLevel(rs.getByte("level"));
						pet.getStat().setExp(rs.getLong("exp"));
						pet.getStat().setSp(rs.getInt("sp"));
						
						pet.getStatus().setCurrentHp(rs.getDouble("curHp"));
						pet.getStatus().setCurrentMp(rs.getDouble("curMp"));
						
						if (rs.getDouble("curHp") < 0.5)
						{
							pet.setIsDead(true);
							pet.stopHpMpRegeneration();
						}
						
						pet.setCurrentFed(rs.getInt("fed"));
					}
					else
					{
						pet.getStat().setLevel((template.getNpcId() == 12564) ? (byte) pet.getOwner().getLevel() : template.getLevel());
						pet.getStat().setExp(pet.getExpForThisLevel());
						pet.getStatus().setCurrentHp(pet.getMaxHp());
						pet.getStatus().setCurrentMp(pet.getMaxMp());
						pet.setCurrentFed(pet.getPetData().getMaxMeal());
						pet.store();
					}
				}
			}
		}
		catch (Exception e)
		{
			WorldObject.LOGGER.error("Couldn't restore pet data for {}.", e, owner.getName());
			return null;
		}
		return pet;
	}
	
	public synchronized void stopFeed()
	{
		if (_feedTask != null)
		{
			_feedTask.cancel(false);
			_feedTask = null;
		}
	}
	
	public synchronized void startFeed()
	{
		// stop feeding task if its active
		stopFeed();
		
		if (!isDead() && getOwner().getSummon() == this)
			_feedTask = ThreadPool.scheduleAtFixedRate(new FeedTask(), 10000, 10000);
	}
	
	/**
	 * Restore the specified % of experience this {@link Pet} has lost.
	 * @param restorePercent : The percent of experience to restore.
	 */
	public void restoreExp(double restorePercent)
	{
		if (_expBeforeDeath > 0)
		{
			getStat().addExp(Math.round((_expBeforeDeath - getStat().getExp()) * restorePercent / 100));
			
			_expBeforeDeath = 0;
		}
	}
	
	private void deathPenalty()
	{
		int lvl = getStat().getLevel();
		double percentLost = -0.07 * lvl + 6.5;
		
		// Calculate the Experience loss
		long lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);
		
		// Get the Experience before applying penalty
		_expBeforeDeath = getStat().getExp();
		
		// Set the new Experience value of the L2PetInstance
		getStat().addExp(-lostExp);
	}
	
	public int getCurrentLoad()
	{
		return _inventory.getTotalWeight();
	}
	
	public int getInventoryLimit()
	{
		return Config.INVENTORY_MAXIMUM_PET;
	}
	
	public void refreshOverloaded() // TODO find a way to apply effect without adding skill. For now it's desactivated.
	{
		int maxLoad = getMaxLoad();
		if (maxLoad > 0)
		{
			int weightproc = getCurrentLoad() * 1000 / maxLoad;
			int newWeightPenalty;
			
			if (weightproc < 500)
				newWeightPenalty = 0;
			else if (weightproc < 666)
				newWeightPenalty = 1;
			else if (weightproc < 800)
				newWeightPenalty = 2;
			else if (weightproc < 1000)
				newWeightPenalty = 3;
			else
				newWeightPenalty = 4;
			
			if (_curWeightPenalty != newWeightPenalty)
			{
				_curWeightPenalty = newWeightPenalty;
				if (newWeightPenalty > 0)
				{
					// addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty), false);
					setIsOverloaded(getCurrentLoad() >= maxLoad);
				}
				else
				{
					// removeSkill(4270, false);
					setIsOverloaded(false);
				}
			}
		}
	}
	
	/**
	 * @return true if the auto feed limit is reached, false otherwise or if there is no need to feed.
	 */
	public boolean checkAutoFeedState()
	{
		return getCurrentFed() < (_petData.getMaxMeal() * getTemplate().getAutoFeedLimit());
	}
	
	/**
	 * @return true if the hungry limit is reached, false otherwise or if there is no need to feed.
	 */
	public boolean checkHungryState()
	{
		return getCurrentFed() < (_petData.getMaxMeal() * getTemplate().getHungryLimit());
	}
	
	/**
	 * @return true if the unsummon limit is reached, false otherwise or if there is no need to feed.
	 */
	public boolean checkUnsummonState()
	{
		return getCurrentFed() < (_petData.getMaxMeal() * getTemplate().getUnsummonLimit());
	}
	
	public boolean canWear(Item item)
	{
		final int npcId = getTemplate().getNpcId();
		
		if (npcId > 12310 && npcId < 12314 && item.getBodyPart() == Item.SLOT_HATCHLING)
			return true;
		
		if (npcId == 12077 && item.getBodyPart() == Item.SLOT_WOLF)
			return true;
		
		if (npcId > 12525 && npcId < 12529 && item.getBodyPart() == Item.SLOT_STRIDER)
			return true;
		
		if (npcId > 12779 && npcId < 12783 && item.getBodyPart() == Item.SLOT_BABYPET)
			return true;
		
		return false;
	}
	
	/**
	 * Manage {@link Pet} feeding task.
	 * <ul>
	 * <li>Feed or kill the pet depending on hunger level.</li>
	 * <li>If pet has food in inventory and feed level drops below 55% then consume food from inventory.</li>
	 * <li>Send a broadcastStatusUpdate packet for this pet.</li>
	 * </ul>
	 */
	protected class FeedTask implements Runnable
	{
		@Override
		public void run()
		{
			if (getOwner() == null || getOwner().getSummon() == null || getOwner().getSummon().getObjectId() != getObjectId())
			{
				stopFeed();
				return;
			}
			
			setCurrentFed((getCurrentFed() > getFeedConsume()) ? getCurrentFed() - getFeedConsume() : 0);
			
			ItemInstance food = getInventory().getItemByItemId(getTemplate().getFood1());
			if (food == null)
				food = getInventory().getItemByItemId(getTemplate().getFood2());
			
			if (food != null && checkAutoFeedState())
			{
				IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
				if (handler != null)
				{
					getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(food));
					handler.useItem(Pet.this, food, false);
				}
			}
			else if (getCurrentFed() == 0)
			{
				getOwner().sendPacket(SystemMessageId.YOUR_PET_IS_VERY_HUNGRY);
				if (Rnd.get(100) < 30)
				{
					stopFeed();
					getOwner().sendPacket(SystemMessageId.STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT);
					deleteMe(getOwner());
					return;
				}
			}
			else if (getCurrentFed() < (0.10 * getPetData().getMaxMeal()))
			{
				getOwner().sendPacket(SystemMessageId.YOUR_PET_IS_VERY_HUNGRY_PLEASE_BE_CAREFUL);
				if (Rnd.get(100) < 3)
				{
					stopFeed();
					getOwner().sendPacket(SystemMessageId.STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT);
					deleteMe(getOwner());
					return;
				}
			}
			
			if (checkHungryState())
				setWalking();
			else
				setRunning();
			
			broadcastStatusUpdate();
		}
		
		private int getFeedConsume()
		{
			return (isAttackingNow()) ? getPetData().getMealInBattle() : getPetData().getMealInNormal();
		}
	}
}
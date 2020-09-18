package dev.l2j.tesla.gameserver.scripting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.cache.HtmCache;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.DropData;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.itemcontainer.PcInventory;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.ExShowQuestMark;
import dev.l2j.tesla.gameserver.network.serverpackets.InventoryUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.PlaySound;
import dev.l2j.tesla.gameserver.network.serverpackets.QuestList;
import dev.l2j.tesla.gameserver.network.serverpackets.StatusUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.TutorialCloseHtml;
import dev.l2j.tesla.gameserver.network.serverpackets.TutorialEnableClientEvent;
import dev.l2j.tesla.gameserver.network.serverpackets.TutorialShowHtml;
import dev.l2j.tesla.gameserver.network.serverpackets.TutorialShowQuestionMark;

public final class QuestState
{
	protected static final CLogger LOGGER = new CLogger(QuestState.class.getName());
	
	public static final String SOUND_ACCEPT = "ItemSound.quest_accept";
	public static final String SOUND_ITEMGET = "ItemSound.quest_itemget";
	public static final String SOUND_MIDDLE = "ItemSound.quest_middle";
	public static final String SOUND_FINISH = "ItemSound.quest_finish";
	public static final String SOUND_GIVEUP = "ItemSound.quest_giveup";
	public static final String SOUND_JACKPOT = "ItemSound.quest_jackpot";
	public static final String SOUND_FANFARE = "ItemSound.quest_fanfare_2";
	public static final String SOUND_BEFORE_BATTLE = "Itemsound.quest_before_battle";
	
	private static final String QUEST_SET_VAR = "REPLACE INTO character_quests (charId,name,var,value) VALUES (?,?,?,?)";
	private static final String QUEST_DEL_VAR = "DELETE FROM character_quests WHERE charId=? AND name=? AND var=?";
	private static final String QUEST_DELETE = "DELETE FROM character_quests WHERE charId=? AND name=?";
	private static final String QUEST_COMPLETE = "DELETE FROM character_quests WHERE charId=? AND name=? AND var<>'<state>'";
	
	public static final byte DROP_DIVMOD = 0;
	public static final byte DROP_FIXED_RATE = 1;
	public static final byte DROP_FIXED_COUNT = 2;
	public static final byte DROP_FIXED_BOTH = 3;
	
	private final Player _player;
	private final Quest _quest;
	private byte _state;
	private final Map<String, String> _vars = new HashMap<>();
	
	/**
	 * Constructor of the QuestState : save the quest in the list of quests of the player.<BR/>
	 * <BR/>
	 * <U><I>Actions :</U></I><BR/>
	 * <LI>Save informations in the object QuestState created (Quest, Player, Completion, State)</LI>
	 * <LI>Add the QuestState in the player's list of quests by using setQuestState()</LI>
	 * <LI>Add drops gotten by the quest</LI> <BR/>
	 * @param quest : quest associated with the QuestState
	 * @param player : Player pointing out the player
	 * @param state : state of the quest
	 */
	public QuestState(Player player, Quest quest, byte state)
	{
		_player = player;
		_quest = quest;
		_state = state;
		
		_player.setQuestState(this);
	}
	
	/**
	 * Return the Player
	 * @return Player
	 */
	public Player getPlayer()
	{
		return _player;
	}
	
	/**
	 * Return the quest
	 * @return Quest
	 */
	public Quest getQuest()
	{
		return _quest;
	}
	
	/**
	 * Return the state of the quest
	 * @return State
	 */
	public byte getState()
	{
		return _state;
	}
	
	/**
	 * Return true if quest just created, false otherwise
	 * @return
	 */
	public boolean isCreated()
	{
		return (_state == Quest.STATE_CREATED);
	}
	
	/**
	 * Return true if quest completed, false otherwise
	 * @return boolean
	 */
	public boolean isCompleted()
	{
		return (_state == Quest.STATE_COMPLETED);
	}
	
	/**
	 * Return true if quest started, false otherwise
	 * @return boolean
	 */
	public boolean isStarted()
	{
		return (_state == Quest.STATE_STARTED);
	}
	
	/**
	 * Set state of the quest.
	 * <ul>
	 * <li>Remove drops from previous state</li>
	 * <li>Set new state of the quest</li>
	 * <li>Add drop for new state</li>
	 * <li>Update information in database</li>
	 * <li>Send packet QuestList to client</li>
	 * </ul>
	 * @param state
	 */
	public void setState(byte state)
	{
		if (_state != state)
		{
			_state = state;
			
			setQuestVarInDb("<state>", String.valueOf(_state));
			
			_player.sendPacket(new QuestList(_player));
		}
	}
	
	/**
	 * Destroy element used by quest when quest is exited
	 * @param repeatable
	 */
	public void exitQuest(boolean repeatable)
	{
		if (!isStarted())
			return;
		
		// Remove quest from player's notifyDeath list.
		_player.removeNotifyQuestOfDeath(this);
		
		// Remove/Complete quest.
		if (repeatable)
		{
			_player.delQuestState(this);
			_player.sendPacket(new QuestList(_player));
		}
		else
			setState(Quest.STATE_COMPLETED);
		
		// Remove quest variables.
		_vars.clear();
		
		// Remove registered quest items.
		int[] itemIdList = _quest.getItemsIds();
		if (itemIdList != null)
		{
			for (int itemId : itemIdList)
				takeItems(itemId, -1);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement((repeatable) ? QUEST_DELETE : QUEST_COMPLETE))
		{
			ps.setInt(1, _player.getObjectId());
			ps.setString(2, _quest.getName());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete quest.", e);
		}
	}
	
	/**
	 * Add player to get notification of characters death
	 */
	public void addNotifyOfDeath()
	{
		if (_player != null)
			_player.addNotifyQuestOfDeath(this);
	}
	
	/**
	 * Return value of parameter "val" after adding the couple (var,val) in class variable "vars".<BR>
	 * <BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Initialize class variable "vars" if is null</LI>
	 * <LI>Initialize parameter "val" if is null</LI>
	 * <LI>Add/Update couple (var,val) in class variable FastMap "vars"</LI>
	 * <LI>If the key represented by "var" exists in FastMap "vars", the couple (var,val) is updated in the database. The key is known as existing if the preceding value of the key (given as result of function put()) is not null.<BR>
	 * If the key doesn't exist, the couple is added/created in the database</LI>
	 * @param var : String indicating the name of the variable for quest
	 * @param value : String indicating the value of the variable for quest
	 */
	public void set(String var, String value)
	{
		if (var == null || var.isEmpty() || value == null || value.isEmpty())
			return;
		
		// Returns previous value associated with specified key, or null if there was no mapping for key.
		String old = _vars.put(var, value);
		
		setQuestVarInDb(var, value);
		
		if ("cond".equals(var))
		{
			try
			{
				int previousVal = 0;
				try
				{
					previousVal = Integer.parseInt(old);
				}
				catch (Exception ex)
				{
					previousVal = 0;
				}
				setCond(Integer.parseInt(value), previousVal);
			}
			catch (Exception e)
			{
				LOGGER.error("{}, {} cond [{}] is not an integer. Value stored, but no packet was sent.", e, _player.getName(), _quest.getName(), value);
			}
		}
	}
	
	/**
	 * Add parameter used in quests.
	 * @param var : String pointing out the name of the variable for quest
	 * @param value : String pointing out the value of the variable for quest
	 */
	public void setInternal(String var, String value)
	{
		if (var == null || var.isEmpty() || value == null || value.isEmpty())
			return;
		
		_vars.put(var, value);
	}
	
	/**
	 * Internally handles the progression of the quest so that it is ready for sending appropriate packets to the client<BR>
	 * <BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Check if the new progress number resets the quest to a previous (smaller) step</LI>
	 * <LI>If not, check if quest progress steps have been skipped</LI>
	 * <LI>If skipped, prepare the variable completedStateFlags appropriately to be ready for sending to clients</LI>
	 * <LI>If no steps were skipped, flags do not need to be prepared...</LI>
	 * <LI>If the passed step resets the quest to a previous step, reset such that steps after the parameter are not considered, while skipped steps before the parameter, if any, maintain their info</LI>
	 * @param cond : int indicating the step number for the current quest progress (as will be shown to the client)
	 * @param old : int indicating the previously noted step For more info on the variable communicating the progress steps to the client, please see
	 */
	private void setCond(int cond, int old)
	{
		// if there is no change since last setting, there is nothing to do here
		if (cond == old)
			return;
		
		int completedStateFlags = 0;
		
		// cond 0 and 1 do not need completedStateFlags. Also, if cond > 1, the 1st step must
		// always exist (i.e. it can never be skipped). So if cond is 2, we can still safely
		// assume no steps have been skipped.
		// Finally, more than 31 steps CANNOT be supported in any way with skipping.
		if (cond < 3 || cond > 31)
			unset("__compltdStateFlags");
		else
			completedStateFlags = getInt("__compltdStateFlags");
		
		// case 1: No steps have been skipped so far...
		if (completedStateFlags == 0)
		{
			// check if this step also doesn't skip anything. If so, no further work is needed
			// also, in this case, no work is needed if the state is being reset to a smaller value
			// in those cases, skip forward to informing the client about the change...
			
			// ELSE, if we just now skipped for the first time...prepare the flags!!!
			if (cond > (old + 1))
			{
				// set the most significant bit to 1 (indicates that there exist skipped states)
				// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
				// what the cond says)
				completedStateFlags = 0x80000001;
				
				// since no flag had been skipped until now, the least significant bits must all
				// be set to 1, up until "old" number of bits.
				completedStateFlags |= ((1 << old) - 1);
				
				// now, just set the bit corresponding to the passed cond to 1 (current step)
				completedStateFlags |= (1 << (cond - 1));
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		// case 2: There were exist previously skipped steps
		else
		{
			// if this is a push back to a previous step, clear all completion flags ahead
			if (cond < old)
			{
				completedStateFlags &= ((1 << cond) - 1); // note, this also unsets the flag indicating that there exist skips
				
				// now, check if this resulted in no steps being skipped any more
				if (completedStateFlags == ((1 << cond) - 1))
					unset("__compltdStateFlags");
				else
				{
					// set the most significant bit back to 1 again, to correctly indicate that this skips states.
					// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
					// what the cond says)
					completedStateFlags |= 0x80000001;
					set("__compltdStateFlags", String.valueOf(completedStateFlags));
				}
			}
			// if this moves forward, it changes nothing on previously skipped steps...so just mark this
			// state and we are done
			else
			{
				completedStateFlags |= (1 << (cond - 1));
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		
		// send a packet to the client to inform it of the quest progress (step change)
		_player.sendPacket(new QuestList(_player));
		
		if (_quest.isRealQuest() && cond > 0)
			_player.sendPacket(new ExShowQuestMark(_quest.getQuestId()));
	}
	
	/**
	 * Remove the variable of quest from the list of variables for the quest.<BR>
	 * <BR>
	 * <U><I>Concept : </I></U> Remove the variable of quest represented by "var" from the class variable FastMap "vars" and from the database.
	 * @param var : String designating the variable for the quest to be deleted
	 */
	public void unset(String var)
	{
		if (_vars.remove(var) != null)
			removeQuestVarInDb(var);
	}
	
	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : name of the variable of quest
	 * @return String
	 */
	public String get(String var)
	{
		return _vars.get(var);
	}
	
	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : String designating the variable for the quest
	 * @return int
	 */
	public int getInt(String var)
	{
		final String variable = _vars.get(var);
		if (variable == null || variable.isEmpty())
			return 0;
		
		int value = 0;
		try
		{
			value = Integer.parseInt(variable);
		}
		catch (Exception e)
		{
			LOGGER.error("{}: variable {} isn't an integer: {}.", e, _player.getName(), var, value);
		}
		
		return value;
	}
	
	/**
	 * Set in the database the quest for the player.
	 * @param var : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	private void setQuestVarInDb(String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(QUEST_SET_VAR))
		{
			ps.setInt(1, _player.getObjectId());
			ps.setString(2, _quest.getName());
			ps.setString(3, var);
			ps.setString(4, value);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't insert quest.", e);
		}
	}
	
	/**
	 * Delete a variable of player's quest from the database.
	 * @param var : String designating the variable characterizing the quest
	 */
	private void removeQuestVarInDb(String var)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(QUEST_DEL_VAR))
		{
			ps.setInt(1, _player.getObjectId());
			ps.setString(2, _quest.getName());
			ps.setString(3, var);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete quest.", e);
		}
	}
	
	/**
	 * Check for an item in player's inventory.
	 * @param itemId the ID of the item to check for
	 * @return {@code true} if the item exists in player's inventory, {@code false} otherwise
	 */
	public boolean hasQuestItems(int itemId)
	{
		return _player.getInventory().getItemByItemId(itemId) != null;
	}
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param itemIds a list of item IDs to check for
	 * @return {@code true} if all items exist in player's inventory, {@code false} otherwise
	 */
	public boolean hasQuestItems(int... itemIds)
	{
		final PcInventory inv = _player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) == null)
				return false;
		}
		return true;
	}
	
	/**
	 * Check if player possesses at least one given item.
	 * @param itemIds a list of item IDs to check for
	 * @return {@code true} if at least one item exists in player's inventory, {@code false} otherwise
	 */
	public boolean hasAtLeastOneQuestItem(int... itemIds)
	{
		return _player.getInventory().hasAtLeastOneItem(itemIds);
	}
	
	/**
	 * @param itemId : ID of the item wanted to be count
	 * @return the quantity of one sort of item hold by the player
	 */
	public int getQuestItemsCount(int itemId)
	{
		int count = 0;
		
		for (ItemInstance item : _player.getInventory().getItems())
			if (item != null && item.getItemId() == itemId)
				count += item.getCount();
			
		return count;
	}
	
	/**
	 * @param loc A paperdoll slot to check.
	 * @return the id of the item in the loc paperdoll slot.
	 */
	public int getItemEquipped(int loc)
	{
		return _player.getInventory().getPaperdollItemId(loc);
	}
	
	/**
	 * Return the level of enchantment on the weapon of the player(Done specifically for weapon SA's)
	 * @param itemId : ID of the item to check enchantment
	 * @return int
	 */
	public int getEnchantLevel(int itemId)
	{
		final ItemInstance enchanteditem = _player.getInventory().getItemByItemId(itemId);
		if (enchanteditem == null)
			return 0;
		
		return enchanteditem.getEnchantLevel();
	}
	
	/**
	 * Give items to the player's inventory.
	 * @param itemId : Identifier of the item.
	 * @param itemCount : Quantity of items to add.
	 */
	public void giveItems(int itemId, int itemCount)
	{
		giveItems(itemId, itemCount, 0);
	}
	
	/**
	 * Give items to the player's inventory.
	 * @param itemId : Identifier of the item.
	 * @param itemCount : Quantity of items to add.
	 * @param enchantLevel : Enchant level of items to add.
	 */
	public void giveItems(int itemId, int itemCount, int enchantLevel)
	{
		// Incorrect amount.
		if (itemCount <= 0)
			return;
		
		// Add items to player's inventory.
		final ItemInstance item = _player.getInventory().addItem("Quest", itemId, itemCount, _player, _player);
		if (item == null)
			return;
		
		// Set enchant level for the item.
		if (enchantLevel > 0)
			item.setEnchantLevel(enchantLevel);
		
		// Send message to the client.
		if (itemId == 57)
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
			smsg.addItemNumber(itemCount);
			_player.sendPacket(smsg);
		}
		else
		{
			if (itemCount > 1)
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(itemId);
				smsg.addItemNumber(itemCount);
				_player.sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				smsg.addItemName(itemId);
				_player.sendPacket(smsg);
			}
		}
		
		// Send status update packet.
		StatusUpdate su = new StatusUpdate(_player);
		su.addAttribute(StatusUpdate.CUR_LOAD, _player.getCurrentLoad());
		_player.sendPacket(su);
	}
	
	/**
	 * Remove items from the player's inventory.
	 * @param itemId : Identifier of the item.
	 * @param itemCount : Quantity of items to destroy.
	 */
	public void takeItems(int itemId, int itemCount)
	{
		// Find item in player's inventory.
		final ItemInstance item = _player.getInventory().getItemByItemId(itemId);
		if (item == null)
			return;
		
		// Tests on count value and set correct value if necessary.
		if (itemCount < 0 || itemCount > item.getCount())
			itemCount = item.getCount();
		
		// Disarm item, if equipped.
		if (item.isEquipped())
		{
			ItemInstance[] unequiped = _player.getInventory().unEquipItemInBodySlotAndRecord(item);
			InventoryUpdate iu = new InventoryUpdate();
			for (ItemInstance itm : unequiped)
				iu.addModifiedItem(itm);
			
			_player.sendPacket(iu);
			_player.broadcastUserInfo();
		}
		
		// Destroy the quantity of items wanted.
		_player.destroyItemByItemId("Quest", itemId, itemCount, _player, true);
	}
	
	/**
	 * Drop items to the player's inventory. Rate is 100%, amount is affected by Config.RATE_QUEST_DROP.
	 * @param itemId : Identifier of the item to be dropped.
	 * @param count : Quantity of items to be dropped.
	 * @param neededCount : Quantity of items needed to complete the task. If set to 0, unlimited amount is collected.
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropItemsAlways(int itemId, int count, int neededCount)
	{
		return dropItems(itemId, count, neededCount, DropData.MAX_CHANCE, DROP_FIXED_RATE);
	}
	
	/**
	 * Drop items to the player's inventory. Rate and amount is affected by DIVMOD of Config.RATE_QUEST_DROP.
	 * @param itemId : Identifier of the item to be dropped.
	 * @param count : Quantity of items to be dropped.
	 * @param neededCount : Quantity of items needed to complete the task. If set to 0, unlimited amount is collected.
	 * @param dropChance : Item drop rate (100% chance is defined by the L2DropData.MAX_CHANCE = 1.000.000).
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropItems(int itemId, int count, int neededCount, int dropChance)
	{
		return dropItems(itemId, count, neededCount, dropChance, DROP_DIVMOD);
	}
	
	/**
	 * Drop items to the player's inventory.
	 * @param itemId : Identifier of the item to be dropped.
	 * @param count : Quantity of items to be dropped.
	 * @param neededCount : Quantity of items needed to complete the task. If set to 0, unlimited amount is collected.
	 * @param dropChance : Item drop rate (100% chance is defined by the L2DropData.MAX_CHANCE = 1.000.000).
	 * @param type : Item drop behavior: DROP_DIVMOD (rate and), DROP_FIXED_RATE, DROP_FIXED_COUNT or DROP_FIXED_BOTH
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropItems(int itemId, int count, int neededCount, int dropChance, byte type)
	{
		// Get current amount of item.
		final int currentCount = getQuestItemsCount(itemId);
		
		// Required amount reached already?
		if (neededCount > 0 && currentCount >= neededCount)
			return true;
		
		int amount = 0;
		switch (type)
		{
			case DROP_DIVMOD:
				dropChance *= Config.RATE_QUEST_DROP;
				amount = count * (dropChance / DropData.MAX_CHANCE);
				if (Rnd.get(DropData.MAX_CHANCE) < dropChance % DropData.MAX_CHANCE)
					amount += count;
				break;
			
			case DROP_FIXED_RATE:
				if (Rnd.get(DropData.MAX_CHANCE) < dropChance)
					amount = (int) (count * Config.RATE_QUEST_DROP);
				break;
			
			case DROP_FIXED_COUNT:
				if (Rnd.get(DropData.MAX_CHANCE) < dropChance * Config.RATE_QUEST_DROP)
					amount = count;
				break;
			
			case DROP_FIXED_BOTH:
				if (Rnd.get(DropData.MAX_CHANCE) < dropChance)
					amount = count;
				break;
		}
		
		boolean reached = false;
		if (amount > 0)
		{
			// Limit count to reach required amount.
			if (neededCount > 0)
			{
				reached = (currentCount + amount) >= neededCount;
				amount = (reached) ? neededCount - currentCount : amount;
			}
			
			// Inventory slot check.
			if (!_player.getInventory().validateCapacityByItemId(itemId))
				return false;
			
			// Give items to the player.
			giveItems(itemId, amount, 0);
			
			// Play the sound.
			playSound(reached ? SOUND_MIDDLE : SOUND_ITEMGET);
		}
		
		return neededCount > 0 && reached;
	}
	
	/**
	 * Drop multiple items to the player's inventory. Rate and amount is affected by DIVMOD of Config.RATE_QUEST_DROP.
	 * @param rewardsInfos : Infos regarding drops (itemId, count, neededCount, dropChance).
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropMultipleItems(int[][] rewardsInfos)
	{
		return dropMultipleItems(rewardsInfos, DROP_DIVMOD);
	}
	
	/**
	 * Drop items to the player's inventory.
	 * @param rewardsInfos : Infos regarding drops (itemId, count, neededCount, dropChance).
	 * @param type : Item drop behavior: DROP_DIVMOD (rate and), DROP_FIXED_RATE, DROP_FIXED_COUNT or DROP_FIXED_BOTH
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropMultipleItems(int[][] rewardsInfos, byte type)
	{
		// Used for the sound.
		boolean sendSound = false;
		
		// Used for the reached state.
		boolean reached = true;
		
		// For each reward type, calculate the probability of drop.
		for (int[] info : rewardsInfos)
		{
			final int itemId = info[0];
			final int currentCount = getQuestItemsCount(itemId);
			final int neededCount = info[2];
			
			// Required amount reached already?
			if (neededCount > 0 && currentCount >= neededCount)
				continue;
			
			final int count = info[1];
			
			int dropChance = info[3];
			int amount = 0;
			
			switch (type)
			{
				case DROP_DIVMOD:
					dropChance *= Config.RATE_QUEST_DROP;
					amount = count * (dropChance / DropData.MAX_CHANCE);
					if (Rnd.get(DropData.MAX_CHANCE) < dropChance % DropData.MAX_CHANCE)
						amount += count;
					break;
				
				case DROP_FIXED_RATE:
					if (Rnd.get(DropData.MAX_CHANCE) < dropChance)
						amount = (int) (count * Config.RATE_QUEST_DROP);
					break;
				
				case DROP_FIXED_COUNT:
					if (Rnd.get(DropData.MAX_CHANCE) < dropChance * Config.RATE_QUEST_DROP)
						amount = count;
					break;
				
				case DROP_FIXED_BOTH:
					if (Rnd.get(DropData.MAX_CHANCE) < dropChance)
						amount = count;
					break;
			}
			
			if (amount > 0)
			{
				// Limit count to reach required amount.
				if (neededCount > 0)
					amount = ((currentCount + amount) >= neededCount) ? neededCount - currentCount : amount;
				
				// Inventory slot check.
				if (!_player.getInventory().validateCapacityByItemId(itemId))
					continue;
				
				// Give items to the player.
				giveItems(itemId, amount, 0);
				
				// Send sound.
				sendSound = true;
			}
			
			// Illimited needed count or current count being inferior to needed count means the state isn't reached.
			if (neededCount <= 0 || ((currentCount + amount) < neededCount))
				reached = false;
		}
		
		// Play the sound.
		if (sendSound)
			playSound((reached) ? SOUND_MIDDLE : SOUND_ITEMGET);
		
		return reached;
	}
	
	/**
	 * Reward player with items. The amount is affected by Config.RATE_QUEST_REWARD or Config.RATE_QUEST_REWARD_ADENA.
	 * @param itemId : Identifier of the item.
	 * @param itemCount : Quantity of item to reward before applying multiplier.
	 */
	public void rewardItems(int itemId, int itemCount)
	{
		if (itemId == 57)
			giveItems(itemId, (int) (itemCount * Config.RATE_QUEST_REWARD_ADENA), 0);
		else
			giveItems(itemId, (int) (itemCount * Config.RATE_QUEST_REWARD), 0);
	}
	
	/**
	 * Reward player with EXP and SP. The amount is affected by Config.RATE_QUEST_REWARD_XP and Config.RATE_QUEST_REWARD_SP
	 * @param exp : Experience amount.
	 * @param sp : Skill point amount.
	 */
	public void rewardExpAndSp(long exp, int sp)
	{
		_player.addExpAndSp((long) (exp * Config.RATE_QUEST_REWARD_XP), (int) (sp * Config.RATE_QUEST_REWARD_SP));
	}
	
	// TODO: More radar functions need to be added when the radar class is complete.
	// BEGIN STUFF THAT WILL PROBABLY BE CHANGED
	
	public void addRadar(int x, int y, int z)
	{
		_player.getRadarList().addMarker(x, y, z);
	}
	
	public void removeRadar(int x, int y, int z)
	{
		_player.getRadarList().removeMarker(x, y, z);
	}
	
	public void clearRadar()
	{
		_player.getRadarList().removeAllMarkers();
	}
	
	// END STUFF THAT WILL PROBABLY BE CHANGED
	
	/**
	 * Send a packet in order to play sound at client terminal
	 * @param sound
	 */
	public void playSound(String sound)
	{
		_player.sendPacket(new PlaySound(sound));
	}
	
	public void showQuestionMark(int number)
	{
		_player.sendPacket(new TutorialShowQuestionMark(number));
	}
	
	public void playTutorialVoice(String voice)
	{
		_player.sendPacket(new PlaySound(2, voice, _player));
	}
	
	public void showTutorialHTML(String html)
	{
		_player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/quests/Tutorial/" + html)));
	}
	
	public void closeTutorialHtml()
	{
		_player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
	}
	
	public void onTutorialClientEvent(int number)
	{
		_player.sendPacket(new TutorialEnableClientEvent(number));
	}
}

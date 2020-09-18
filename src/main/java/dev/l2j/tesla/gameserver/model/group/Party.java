package dev.l2j.tesla.gameserver.model.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.model.partymatching.PartyMatchRoom;
import dev.l2j.tesla.gameserver.model.partymatching.PartyMatchRoomList;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.math.MathUtil;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.manager.DimensionalRiftManager;
import dev.l2j.tesla.gameserver.data.manager.DuelManager;
import dev.l2j.tesla.gameserver.data.manager.FestivalOfDarknessManager;
import dev.l2j.tesla.gameserver.enums.LootRule;
import dev.l2j.tesla.gameserver.enums.MessageType;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.Servitor;
import dev.l2j.tesla.gameserver.model.actor.npc.RewardInfo;
import dev.l2j.tesla.gameserver.model.actor.player.BlockList;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.rift.DimensionalRift;
import dev.l2j.tesla.gameserver.network.serverpackets.CreatureSay;
import dev.l2j.tesla.gameserver.network.serverpackets.ExCloseMPCC;
import dev.l2j.tesla.gameserver.network.serverpackets.ExOpenMPCC;
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket;
import dev.l2j.tesla.gameserver.network.serverpackets.PartyMemberPosition;
import dev.l2j.tesla.gameserver.network.serverpackets.PartySmallWindowAdd;
import dev.l2j.tesla.gameserver.network.serverpackets.PartySmallWindowAll;
import dev.l2j.tesla.gameserver.network.serverpackets.PartySmallWindowDelete;
import dev.l2j.tesla.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;

public class Party extends AbstractGroup
{
	private static final double[] BONUS_EXP_SP =
	{
		1,
		1,
		1.30,
		1.39,
		1.50,
		1.54,
		1.58,
		1.63,
		1.67,
		1.71
	};
	
	private static final int PARTY_POSITION_BROADCAST = 12000;
	
	private final List<Player> _members = new CopyOnWriteArrayList<>();
	private final LootRule _lootRule;
	
	private boolean _pendingInvitation;
	private long _pendingInviteTimeout;
	private int _itemLastLoot;
	
	private CommandChannel _commandChannel;
	private DimensionalRift _rift;
	
	private Future<?> _positionBroadcastTask;
	protected PartyMemberPosition _positionPacket;
	
	public Party(Player leader, Player target, LootRule lootRule)
	{
		super(leader);
		
		_members.add(leader);
		_members.add(target);
		
		leader.setParty(this);
		target.setParty(this);
		
		_lootRule = lootRule;
		
		recalculateLevel();
		
		// Send new member party window for all members.
		target.sendPacket(new PartySmallWindowAll(target, this));
		leader.sendPacket(new PartySmallWindowAdd(target, this));
		
		// Send messages.
		target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY).addCharName(leader));
		leader.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_PARTY).addCharName(target));
		
		// Update icons.
		for (Player member : _members)
		{
			member.updateEffectIcons(true);
			member.broadcastUserInfo();
		}
		
		_positionBroadcastTask = ThreadPool.scheduleAtFixedRate(new PositionBroadcast(), PARTY_POSITION_BROADCAST / 2, PARTY_POSITION_BROADCAST);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Party))
			return false;
		
		if (obj == this)
			return true;
		
		return isLeader(((Party) obj).getLeader());
	}
	
	@Override
	public final List<Player> getMembers()
	{
		return _members;
	}
	
	@Override
	public int getMembersCount()
	{
		return _members.size();
	}
	
	@Override
	public boolean containsPlayer(WorldObject player)
	{
		return _members.contains(player);
	}
	
	@Override
	public void broadcastPacket(final L2GameServerPacket packet)
	{
		for (Player member : _members)
			member.sendPacket(packet);
	}
	
	@Override
	public void broadcastCreatureSay(final CreatureSay msg, final Player broadcaster)
	{
		for (Player member : _members)
		{
			if (!BlockList.isBlocked(member, broadcaster))
				member.sendPacket(msg);
		}
	}
	
	@Override
	public void recalculateLevel()
	{
		int newLevel = 0;
		for (Player member : _members)
		{
			if (member.getLevel() > newLevel)
				newLevel = member.getLevel();
		}
		setLevel(newLevel);
	}
	
	@Override
	public void disband()
	{
		// Cancel current rift session.
		DimensionalRiftManager.getInstance().onPartyEdit(this);
		
		// Cancel party duel based on leader, as it will affect all players anyway.
		DuelManager.getInstance().onPartyEdit(getLeader());
		
		// Delete the CommandChannel, or remove Party from it.
		if (_commandChannel != null)
		{
			broadcastPacket(ExCloseMPCC.STATIC_PACKET);
			
			if (_commandChannel.isLeader(getLeader()))
				_commandChannel.disband();
			else
				_commandChannel.removeParty(this);
		}
		
		for (Player member : _members)
		{
			member.setParty(null);
			member.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
			
			if (member.isFestivalParticipant())
				FestivalOfDarknessManager.getInstance().updateParticipants(member, this);
			
			if (member.getFusionSkill() != null)
				member.abortCast();
			
			for (Creature character : member.getKnownType(Creature.class))
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == member)
					character.abortCast();
				
			member.sendPacket(SystemMessageId.PARTY_DISPERSED);
		}
		_members.clear();
		
		if (_positionBroadcastTask != null)
		{
			_positionBroadcastTask.cancel(false);
			_positionBroadcastTask = null;
		}
	}
	
	/**
	 * Check if another player can start invitation process.
	 * @return boolean if party waits for invitation respond.
	 */
	public boolean getPendingInvitation()
	{
		return _pendingInvitation;
	}
	
	/**
	 * Set invitation process flag and store time for expiration happens when player join or decline to join.
	 * @param val : set the invitation process flag to that value.
	 */
	public void setPendingInvitation(boolean val)
	{
		_pendingInvitation = val;
		_pendingInviteTimeout = System.currentTimeMillis() + Player.REQUEST_TIMEOUT * 1000;
	}
	
	/**
	 * Check if player invitation is expired.
	 * @return boolean if time is expired.
	 * @see Player#isRequestExpired()
	 */
	public boolean isInvitationRequestExpired()
	{
		return _pendingInviteTimeout <= System.currentTimeMillis();
	}
	
	/**
	 * Get a random member from this party.
	 * @param itemId : the ID of the item for which the member must have inventory space.
	 * @param target : the object of which the member must be within a certain range (must not be null).
	 * @return a random member from this party or {@code null} if none of the members have inventory space for the specified item.
	 */
	private Player getRandomMember(int itemId, Creature target)
	{
		final List<Player> availableMembers = new ArrayList<>();
		for (Player member : _members)
		{
			if (member.getInventory().validateCapacityByItemId(itemId) && MathUtil.checkIfInRange(Config.PARTY_RANGE, target, member, true))
				availableMembers.add(member);
		}
		return (availableMembers.isEmpty()) ? null : Rnd.get(availableMembers);
	}
	
	/**
	 * Get the next item looter for this party.
	 * @param itemId : the ID of the item for which the member must have inventory space.
	 * @param target : the object of which the member must be within a certain range (must not be null).
	 * @return the next looter from this party or {@code null} if none of the members have inventory space for the specified item.
	 */
	private Player getNextLooter(int itemId, Creature target)
	{
		for (int i = 0; i < getMembersCount(); i++)
		{
			if (++_itemLastLoot >= getMembersCount())
				_itemLastLoot = 0;
			
			final Player member = _members.get(_itemLastLoot);
			if (member.getInventory().validateCapacityByItemId(itemId) && MathUtil.checkIfInRange(Config.PARTY_RANGE, target, member, true))
				return member;
		}
		return null;
	}
	
	/**
	 * @param player : the potential, initial looter.
	 * @param itemId : the ID of the item for which the member must have inventory space.
	 * @param spoil : a boolean used for spoil process.
	 * @param target : the object of which the member must be within a certain range (must not be null).
	 * @return the next Player looter.
	 */
	private Player getActualLooter(Player player, int itemId, boolean spoil, Creature target)
	{
		Player looter = player;
		
		switch (_lootRule)
		{
			case ITEM_RANDOM:
				if (!spoil)
					looter = getRandomMember(itemId, target);
				break;
			
			case ITEM_RANDOM_SPOIL:
				looter = getRandomMember(itemId, target);
				break;
			
			case ITEM_ORDER:
				if (!spoil)
					looter = getNextLooter(itemId, target);
				break;
			
			case ITEM_ORDER_SPOIL:
				looter = getNextLooter(itemId, target);
				break;
		}
		
		return (looter == null) ? player : looter;
	}
	
	public void broadcastNewLeaderStatus()
	{
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER).addCharName(getLeader());
		for (Player member : _members)
		{
			member.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
			member.sendPacket(new PartySmallWindowAll(member, this));
			member.broadcastUserInfo();
			member.sendPacket(sm);
		}
	}
	
	/**
	 * Send a packet to all other players of the Party, except the player.
	 * @param player : this player won't receive the packet.
	 * @param msg : the packet to send.
	 */
	public void broadcastToPartyMembers(Player player, L2GameServerPacket msg)
	{
		for (Player member : _members)
		{
			if (member != null && !member.equals(player))
				member.sendPacket(msg);
		}
	}
	
	/**
	 * Add a new member to the party.
	 * @param player : the player to add to the party.
	 */
	public void addPartyMember(Player player)
	{
		if (player == null || _members.contains(player))
			return;
		
		// Send new member party window for all members.
		player.sendPacket(new PartySmallWindowAll(player, this));
		broadcastPacket(new PartySmallWindowAdd(player, this));
		
		// Send messages.
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY).addCharName(getLeader()));
		broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_PARTY).addCharName(player));
		
		// Cancel current rift session.
		DimensionalRiftManager.getInstance().onPartyEdit(this);
		
		// Cancel party duel based on leader, as it will affect all players anyway.
		DuelManager.getInstance().onPartyEdit(getLeader());
		
		// Add player to party.
		_members.add(player);
		
		// Add party to player.
		player.setParty(this);
		
		// Adjust party level.
		if (player.getLevel() > getLevel())
			setLevel(player.getLevel());
		
		// Update icons.
		for (Player member : _members)
		{
			member.updateEffectIcons(true);
			member.broadcastUserInfo();
		}
		
		if (_commandChannel != null)
			player.sendPacket(ExOpenMPCC.STATIC_PACKET);
	}
	
	/**
	 * Removes a party member using its name.
	 * @param name : player the player to remove from the party.
	 * @param type : the message type {@link MessageType}.
	 */
	public void removePartyMember(String name, MessageType type)
	{
		removePartyMember(getPlayerByName(name), type);
	}
	
	/**
	 * Removes a party member instance.
	 * @param player : the player to remove from the party.
	 * @param type : the message type {@link MessageType}.
	 */
	public void removePartyMember(Player player, MessageType type)
	{
		if (player == null || !_members.contains(player))
			return;
		
		if (_members.size() == 2 || isLeader(player))
			disband();
		else
		{
			// Cancel current rift session.
			DimensionalRiftManager.getInstance().onPartyEdit(this);
			
			// Cancel party duel based on leader, as it will affect all players anyway.
			DuelManager.getInstance().onPartyEdit(getLeader());
			
			_members.remove(player);
			recalculateLevel();
			
			if (player.isFestivalParticipant())
				FestivalOfDarknessManager.getInstance().updateParticipants(player, this);
			
			if (player.getFusionSkill() != null)
				player.abortCast();
			
			for (Creature character : player.getKnownType(Creature.class))
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == player)
					character.abortCast();
				
			if (type == MessageType.EXPELLED)
			{
				player.sendPacket(SystemMessageId.HAVE_BEEN_EXPELLED_FROM_PARTY);
				broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_EXPELLED_FROM_PARTY).addCharName(player));
			}
			else if (type == MessageType.LEFT || type == MessageType.DISCONNECTED)
			{
				player.sendPacket(SystemMessageId.YOU_LEFT_PARTY);
				broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_LEFT_PARTY).addCharName(player));
			}
			
			player.setParty(null);
			player.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
			
			broadcastPacket(new PartySmallWindowDelete(player));
			
			if (_commandChannel != null)
				player.sendPacket(ExCloseMPCC.STATIC_PACKET);
		}
	}
	
	/**
	 * Change the party leader. If CommandChannel leader was the previous leader, change it too.
	 * @param name : the name of the player newly promoted to leader.
	 */
	public void changePartyLeader(String name)
	{
		final Player player = getPlayerByName(name);
		if (player == null || player.isInDuel())
			return;
		
		// Can't set leader if not part of the party.
		if (!_members.contains(player))
		{
			player.sendPacket(SystemMessageId.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER);
			return;
		}
		
		// If already leader, abort.
		if (isLeader(player))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF);
			return;
		}
		
		// Refresh channel leader, if any.
		if (_commandChannel != null && _commandChannel.isLeader(getLeader()))
		{
			_commandChannel.setLeader(player);
			_commandChannel.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_LEADER_NOW_S1).addCharName(player));
		}
		
		// Update this party leader and broadcast the update.
		setLeader(player);
		broadcastNewLeaderStatus();
		
		if (player.isInPartyMatchRoom())
		{
			final PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
			room.changeLeader(player);
		}
	}
	
	/**
	 * @param name : the name of the player to search.
	 * @return a party member by its name.
	 */
	private Player getPlayerByName(String name)
	{
		for (Player member : _members)
		{
			if (member.getName().equalsIgnoreCase(name))
				return member;
		}
		return null;
	}
	
	/**
	 * Distribute item(s) to one party member, based on party LootRule.
	 * @param player : the initial looter.
	 * @param item : the looted item to distribute.
	 */
	public void distributeItem(Player player, ItemInstance item)
	{
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), player);
			item.destroyMe("Party", player, null);
			return;
		}
		
		final Player target = getActualLooter(player, item.getItemId(), false, player);
		if (target == null)
			return;
		
		// Send messages to other party members about reward.
		if (item.getCount() > 1)
			broadcastToPartyMembers(target, SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S3_S2).addCharName(target).addItemName(item).addItemNumber(item.getCount()));
		else if (item.getEnchantLevel() > 0)
			broadcastToPartyMembers(target, SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S2_S3).addCharName(target).addNumber(item.getEnchantLevel()).addItemName(item));
		else
			broadcastToPartyMembers(target, SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S2).addCharName(target).addItemName(item));
		
		target.addItem("Party", item, player, true);
	}
	
	/**
	 * Distribute item(s) to one party member, based on party LootRule.
	 * @param player : the initial looter.
	 * @param item : the looted item to distribute.
	 * @param spoil : true if the item comes from a spoil process.
	 * @param target : the looted character.
	 */
	public void distributeItem(Player player, IntIntHolder item, boolean spoil, Attackable target)
	{
		if (item == null)
			return;
		
		if (item.getId() == 57)
		{
			distributeAdena(player, item.getValue(), target);
			return;
		}
		
		final Player looter = getActualLooter(player, item.getId(), spoil, target);
		if (looter == null)
			return;
		
		looter.addItem(spoil ? "Sweep" : "Party", item.getId(), item.getValue(), player, true);
		
		// Send messages to other party members about reward.
		SystemMessage msg;
		if (item.getValue() > 1)
		{
			msg = (spoil) ? SystemMessage.getSystemMessage(SystemMessageId.S1_SWEEPED_UP_S3_S2) : SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S3_S2);
			msg.addCharName(looter);
			msg.addItemName(item.getId());
			msg.addItemNumber(item.getValue());
		}
		else
		{
			msg = (spoil) ? SystemMessage.getSystemMessage(SystemMessageId.S1_SWEEPED_UP_S2) : SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S2);
			msg.addCharName(looter);
			msg.addItemName(item.getId());
		}
		broadcastToPartyMembers(looter, msg);
	}
	
	/**
	 * Distribute adena to party members, according distance.
	 * @param player : The player who picked.
	 * @param adena : Amount of adenas.
	 * @param target : Target used for distance checks.
	 */
	public void distributeAdena(Player player, int adena, Creature target)
	{
		List<Player> toReward = new ArrayList<>(_members.size());
		for (Player member : _members)
		{
			if (!MathUtil.checkIfInRange(Config.PARTY_RANGE, target, member, true) || member.getAdena() == Integer.MAX_VALUE)
				continue;
			
			toReward.add(member);
		}
		
		// Avoid divisions by 0.
		if (toReward.isEmpty())
			return;
		
		final int count = adena / toReward.size();
		for (Player member : toReward)
			member.addAdena("Party", count, player, true);
	}
	
	/**
	 * Distribute Experience and SP rewards to party members in the known area of the last attacker.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <ul>
	 * <li>Get the owner of the Summon (if necessary).</li>
	 * <li>Calculate the Experience and SP reward distribution rate.</li>
	 * <li>Add Experience and SP to the player.</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to Pet</B></FONT><BR>
	 * <BR>
	 * Exception are Pets that leech from the owner's XP; they get the exp indirectly, via the owner's exp gain.<BR>
	 * @param xpReward : The Experience reward to distribute.
	 * @param spReward : The SP reward to distribute.
	 * @param rewardedMembers : The list of Player to reward.
	 * @param topLvl : The maximum level.
	 * @param rewards : The list of players and summons.
	 */
	public void distributeXpAndSp(long xpReward, int spReward, List<Player> rewardedMembers, int topLvl, Map<Creature, RewardInfo> rewards)
	{
		final List<Player> validMembers = new ArrayList<>();
		
		if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
		{
			for (Player member : rewardedMembers)
			{
				if (topLvl - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL)
					validMembers.add(member);
			}
		}
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage"))
		{
			int sqLevelSum = 0;
			for (Player member : rewardedMembers)
				sqLevelSum += (member.getLevel() * member.getLevel());
			
			for (Player member : rewardedMembers)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				if (sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT)
					validMembers.add(member);
			}
		}
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto"))
		{
			int sqLevelSum = 0;
			for (Player member : rewardedMembers)
				sqLevelSum += (member.getLevel() * member.getLevel());
			
			// Have to use range 1 to 9, since we -1 it : 0 can't be a good number (would lead to a IOOBE). Since 0 and 1 got same values, it's not a problem.
			final int partySize = MathUtil.limit(rewardedMembers.size(), 1, 9);
			
			for (Player member : rewardedMembers)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				if (sqLevel >= sqLevelSum * (1 - 1 / (1 + BONUS_EXP_SP[partySize] - BONUS_EXP_SP[partySize - 1])))
					validMembers.add(member);
			}
		}
		
		// Since validMembers can also hold CommandChannel members, we have to restrict the value.
		final double partyRate = BONUS_EXP_SP[Math.min(validMembers.size(), 9)];
		
		xpReward *= partyRate * Config.RATE_PARTY_XP;
		spReward *= partyRate * Config.RATE_PARTY_SP;
		
		int sqLevelSum = 0;
		for (Player member : validMembers)
			sqLevelSum += member.getLevel() * member.getLevel();
		
		// Go through the players that must be rewarded.
		for (Player member : rewardedMembers)
		{
			if (member.isDead())
				continue;
			
			// Calculate and add the EXP and SP reward to the member.
			if (validMembers.contains(member))
			{
				// The servitor penalty.
				final float penalty = member.hasServitor() ? ((Servitor) member.getSummon()).getExpPenalty() : 0;
				
				final double sqLevel = member.getLevel() * member.getLevel();
				final double preCalculation = (sqLevel / sqLevelSum) * (1 - penalty);
				
				final long xp = Math.round(xpReward * preCalculation);
				final int sp = (int) (spReward * preCalculation);
				
				// Set new karma.
				member.updateKarmaLoss(xp);
				
				// Add the XP/SP points to the requested party member.
				member.addExpAndSp(xp, sp, rewards);
			}
			else
				member.addExpAndSp(0, 0);
		}
	}
	
	public LootRule getLootRule()
	{
		return _lootRule;
	}
	
	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}
	
	public CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}
	
	public void setCommandChannel(CommandChannel channel)
	{
		_commandChannel = channel;
	}
	
	public boolean isInDimensionalRift()
	{
		return _rift != null;
	}
	
	public DimensionalRift getDimensionalRift()
	{
		return _rift;
	}
	
	public void setDimensionalRift(DimensionalRift rift)
	{
		_rift = rift;
	}
	
	/**
	 * @return true if the entire party is currently dead.
	 */
	public boolean wipedOut()
	{
		for (Player member : _members)
		{
			if (!member.isDead())
				return false;
		}
		return true;
	}
	
	protected class PositionBroadcast implements Runnable
	{
		@Override
		public void run()
		{
			if (_positionPacket == null)
				_positionPacket = new PartyMemberPosition(Party.this);
			else
				_positionPacket.reuse(Party.this);
			
			broadcastPacket(_positionPacket);
		}
	}
}
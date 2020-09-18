package dev.l2j.tesla.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.location.TowerSpawnLocation;
import dev.l2j.tesla.gameserver.model.pledge.Clan;
import dev.l2j.tesla.gameserver.model.pledge.ClanMember;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.PlaySound;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.UserInfo;
import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.util.ArraysUtil;

import dev.l2j.tesla.gameserver.data.manager.CastleManager;
import dev.l2j.tesla.gameserver.data.manager.HeroManager;
import dev.l2j.tesla.gameserver.data.sql.ClanTable;
import dev.l2j.tesla.gameserver.data.xml.NpcData;
import dev.l2j.tesla.gameserver.enums.SiegeSide;
import dev.l2j.tesla.gameserver.enums.SiegeStatus;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.ControlTower;
import dev.l2j.tesla.gameserver.model.actor.instance.FlameTower;
import dev.l2j.tesla.gameserver.model.spawn.L2Spawn;

public class Siege implements Siegable
{
	protected static final CLogger LOGGER = new CLogger(Siege.class.getName());
	
	private static final String LOAD_SIEGE_CLAN = "SELECT clan_id,type FROM siege_clans WHERE castle_id=?";
	
	private static final String CLEAR_SIEGE_CLANS = "DELETE FROM siege_clans WHERE castle_id=?";
	private static final String CLEAR_PENDING_CLANS = "DELETE FROM siege_clans WHERE castle_id=? AND type='PENDING'";
	
	private static final String CLEAR_SIEGE_CLAN = "DELETE FROM siege_clans WHERE castle_id=? AND clan_id=?";
	
	private static final String UPDATE_SIEGE_INFOS = "UPDATE castle SET siegeDate=?, regTimeOver=? WHERE id=?";
	
	private static final String ADD_OR_UPDATE_SIEGE_CLAN = "INSERT INTO siege_clans (clan_id,castle_id,type) VALUES (?,?,?) ON DUPLICATE KEY UPDATE type=VALUES(type)";
	
	private final Map<Clan, SiegeSide> _registeredClans = new ConcurrentHashMap<>();
	
	private final Castle _castle;
	
	private final List<ControlTower> _controlTowers = new ArrayList<>();
	private final List<FlameTower> _flameTowers = new ArrayList<>();
	
	private final List<Npc> _destroyedTowers = new ArrayList<>();
	
	protected Calendar _siegeEndDate;
	protected ScheduledFuture<?> _siegeTask;
	
	private Clan _formerOwner;
	private SiegeStatus _siegeStatus = SiegeStatus.REGISTRATION_OPENED;
	
	private List<Quest> _questEvents = Collections.emptyList();
	
	public Siege(Castle castle)
	{
		_castle = castle;
		
		// Add castle owner as defender (add owner first so that they are on the top of the defender list)
		if (_castle.getOwnerId() > 0)
		{
			final Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
			if (clan != null)
				_registeredClans.put(clan, SiegeSide.OWNER);
		}
		
		// Feed _registeredClans.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement(LOAD_SIEGE_CLAN))
			{
				ps.setInt(1, _castle.getCastleId());
				
				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						final Clan clan = ClanTable.getInstance().getClan(rs.getInt("clan_id"));
						if (clan != null)
							_registeredClans.put(clan, Enum.valueOf(SiegeSide.class, rs.getString("type")));
					}
				}
				
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load siege registered clans.", e);
		}
		
		startAutoTask();
	}
	
	@Override
	public void startSiege()
	{
		if (isInProgress())
			return;
		
		if (getAttackerClans().isEmpty())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage((getCastle().getOwnerId() <= 0) ? SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST : SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
			sm.addString(getCastle().getName());
			
			World.toAllOnlinePlayers(sm);
			saveCastleSiege(true);
			return;
		}
		
		_formerOwner = ClanTable.getInstance().getClan(_castle.getOwnerId());
		
		changeStatus(SiegeStatus.IN_PROGRESS); // Flag so that same siege instance cannot be started again
		
		updatePlayerSiegeStateFlags(false);
		getCastle().getSiegeZone().banishForeigners(getCastle().getOwnerId());
		
		spawnControlTowers(); // Spawn control towers
		spawnFlameTowers(); // Spawn flame towers
		getCastle().closeDoors(); // Close doors
		
		getCastle().spawnSiegeGuardsOrMercenaries();
		
		getCastle().getSiegeZone().setIsActive(true);
		getCastle().getSiegeZone().updateZoneStatusForCharactersInside();
		
		_siegeEndDate = Calendar.getInstance();
		_siegeEndDate.add(Calendar.MINUTE, Config.SIEGE_LENGTH);
		
		// Schedule a task to prepare auto siege end
		ThreadPool.schedule(new EndSiegeTask(getCastle()), 1000);
		
		World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_STARTED).addString(getCastle().getName()));
		World.toAllOnlinePlayers(new PlaySound("systemmsg_e.17"));
	}
	
	@Override
	public void endSiege()
	{
		if (!isInProgress())
			return;
		
		World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED).addString(getCastle().getName()));
		World.toAllOnlinePlayers(new PlaySound("systemmsg_e.18"));
		
		if (getCastle().getOwnerId() > 0)
		{
			Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
			World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE).addString(clan.getName()).addString(getCastle().getName()));
			
			// An initial clan was holding the castle and is different of current owner.
			if (_formerOwner != null && clan != _formerOwner)
			{
				// Delete circlets and crown's leader for initial castle's owner (if one was existing)
				getCastle().checkItemsForClan(_formerOwner);
				
				// Refresh hero diaries.
				for (ClanMember member : clan.getMembers())
				{
					final Player player = member.getPlayerInstance();
					if (player != null && player.isNoble())
						HeroManager.getInstance().setCastleTaken(player.getObjectId(), getCastle().getCastleId());
				}
			}
		}
		else
			World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW).addString(getCastle().getName()));
		
		// Cleanup clans kills/deaths counters, cleanup flag.
		for (Clan clan : _registeredClans.keySet())
		{
			clan.setSiegeKills(0);
			clan.setSiegeDeaths(0);
			clan.setFlag(null);
		}
		
		// Refresh reputation points towards siege end.
		updateClansReputation();
		
		// Teleport all non-owning castle players on second closest town.
		getCastle().getSiegeZone().banishForeigners(getCastle().getOwnerId());
		
		// Clear all flags.
		updatePlayerSiegeStateFlags(true);
		
		// Save castle specific data.
		saveCastleSiege(true);
		
		// Clear registered clans.
		clearAllClans();
		
		// Remove all towers from this castle.
		removeTowers();
		
		// Despawn guards or mercenaries.
		getCastle().despawnSiegeGuardsOrMercenaries();
		
		// Respawn/repair castle doors.
		getCastle().spawnDoors(false);
		
		getCastle().getSiegeZone().setIsActive(false);
		getCastle().getSiegeZone().updateZoneStatusForCharactersInside();
	}
	
	@Override
	public final List<Clan> getAttackerClans()
	{
		return _registeredClans.entrySet().stream().filter(e -> e.getValue() == SiegeSide.ATTACKER).map(Map.Entry::getKey).collect(Collectors.toList());
	}
	
	@Override
	public final List<Clan> getDefenderClans()
	{
		return _registeredClans.entrySet().stream().filter(e -> e.getValue() == SiegeSide.DEFENDER || e.getValue() == SiegeSide.OWNER).map(Map.Entry::getKey).collect(Collectors.toList());
	}
	
	@Override
	public boolean checkSide(Clan clan, SiegeSide type)
	{
		return clan != null && _registeredClans.get(clan) == type;
	}
	
	@Override
	public boolean checkSides(Clan clan, SiegeSide... types)
	{
		return clan != null && ArraysUtil.contains(types, _registeredClans.get(clan));
	}
	
	@Override
	public boolean checkSides(Clan clan)
	{
		return clan != null && _registeredClans.containsKey(clan);
	}
	
	@Override
	public Npc getFlag(Clan clan)
	{
		return (checkSide(clan, SiegeSide.ATTACKER)) ? clan.getFlag() : null;
	}
	
	@Override
	public final Calendar getSiegeDate()
	{
		return getCastle().getSiegeDate();
	}
	
	public Map<Clan, SiegeSide> getRegisteredClans()
	{
		return _registeredClans;
	}
	
	public final List<Clan> getPendingClans()
	{
		return _registeredClans.entrySet().stream().filter(e -> e.getValue() == SiegeSide.PENDING).map(Map.Entry::getKey).collect(Collectors.toList());
	}
	
	/**
	 * Update clan reputation points over siege end, as following :
	 * <ul>
	 * <li>The former clan failed to defend the castle : 1000 points for new owner, -1000 for former clan.</li>
	 * <li>The former clan successfully defended the castle, ending in a draw : 500 points for former clan.</li>
	 * <li>No former clan, which means players successfully attacked over NPCs : 1000 points for new owner.</li>
	 * </ul>
	 */
	public void updateClansReputation()
	{
		final Clan owner = ClanTable.getInstance().getClan(_castle.getOwnerId());
		if (_formerOwner != null)
		{
			// Defenders fail
			if (_formerOwner != owner)
			{
				_formerOwner.takeReputationScore(1000);
				_formerOwner.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAS_DEFEATED_IN_SIEGE_AND_LOST_S1_REPUTATION_POINTS).addNumber(1000));
				
				// Attackers succeed over defenders
				if (owner != null)
				{
					owner.addReputationScore(1000);
					owner.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS).addNumber(1000));
				}
			}
			// Draw
			else
			{
				_formerOwner.addReputationScore(500);
				_formerOwner.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS).addNumber(500));
			}
		}
		// Attackers win over NPCs
		else if (owner != null)
		{
			owner.addReputationScore(1000);
			owner.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS).addNumber(1000));
		}
	}
	
	/**
	 * This method is used to switch all SiegeClanType from one type to another.
	 * @param clan
	 * @param newState
	 */
	private void switchSide(Clan clan, SiegeSide newState)
	{
		_registeredClans.put(clan, newState);
	}
	
	/**
	 * This method is used to switch all SiegeClanType from one type to another.
	 * @param previousStates
	 * @param newState
	 */
	private void switchSides(SiegeSide newState, SiegeSide... previousStates)
	{
		for (Map.Entry<Clan, SiegeSide> entry : _registeredClans.entrySet())
		{
			if (ArraysUtil.contains(previousStates, entry.getValue()))
				entry.setValue(newState);
		}
	}
	
	public SiegeSide getSide(Clan clan)
	{
		return _registeredClans.get(clan);
	}
	
	/**
	 * Check if both clans are registered as opponent.
	 * @param formerClan : The first clan to check.
	 * @param targetClan : The second clan to check.
	 * @return true if one side is attacker/defender and other side is defender/attacker and false if one of clan isn't registered or previous statement didn't match.
	 */
	public boolean isOnOppositeSide(Clan formerClan, Clan targetClan)
	{
		final SiegeSide formerSide = _registeredClans.get(formerClan);
		final SiegeSide targetSide = _registeredClans.get(targetClan);
		
		// Clan isn't even registered ; return false.
		if (formerSide == null || targetSide == null)
			return false;
		
		// One side is owner, pending or defender and the other is attacker ; or vice-versa.
		return (targetSide == SiegeSide.ATTACKER && (formerSide == SiegeSide.OWNER || formerSide == SiegeSide.DEFENDER || formerSide == SiegeSide.PENDING)) || (formerSide == SiegeSide.ATTACKER && (targetSide == SiegeSide.OWNER || targetSide == SiegeSide.DEFENDER || targetSide == SiegeSide.PENDING));
	}
	
	/**
	 * When control of castle changed during siege.
	 */
	public void midVictory()
	{
		if (!isInProgress())
			return;
		
		getCastle().despawnSiegeGuardsOrMercenaries();
		
		if (getCastle().getOwnerId() <= 0)
			return;
		
		final List<Clan> attackers = getAttackerClans();
		final List<Clan> defenders = getDefenderClans();
		
		final Clan castleOwner = ClanTable.getInstance().getClan(getCastle().getOwnerId());
		
		// No defending clans and only one attacker, end siege.
		if (defenders.isEmpty() && attackers.size() == 1)
		{
			switchSide(castleOwner, SiegeSide.OWNER);
			endSiege();
			return;
		}
		
		final int allyId = castleOwner.getAllyId();
		
		// No defending clans and all attackers are part of the newly named castle owner alliance.
		if (defenders.isEmpty() && allyId != 0)
		{
			boolean allInSameAlliance = true;
			for (Clan clan : attackers)
			{
				if (clan.getAllyId() != allyId)
				{
					allInSameAlliance = false;
					break;
				}
			}
			
			if (allInSameAlliance)
			{
				switchSide(castleOwner, SiegeSide.OWNER);
				endSiege();
				return;
			}
		}
		
		// All defenders and owner become attackers.
		switchSides(SiegeSide.ATTACKER, SiegeSide.DEFENDER, SiegeSide.OWNER);
		
		// Newly named castle owner is setted.
		switchSide(castleOwner, SiegeSide.OWNER);
		
		// Define newly named castle owner registered allies as defenders.
		if (allyId != 0)
		{
			for (Clan clan : attackers)
			{
				if (clan.getAllyId() == allyId)
					switchSide(clan, SiegeSide.DEFENDER);
			}
		}
		getCastle().getSiegeZone().banishForeigners(getCastle().getOwnerId());
		
		// Removes defenders' flags.
		for (Clan clan : defenders)
			clan.setFlag(null);
		
		getCastle().removeDoorUpgrade(); // Remove all castle doors upgrades.
		getCastle().removeTrapUpgrade(); // Remove all castle traps upgrades.
		getCastle().spawnDoors(true); // Respawn door to castle but make them weaker (50% hp).
		
		removeTowers(); // Remove all towers from this castle.
		
		spawnControlTowers(); // Each new siege midvictory CT are completely respawned.
		spawnFlameTowers();
		
		updatePlayerSiegeStateFlags(false);
	}
	
	/**
	 * Broadcast a {@link SystemMessage} to defenders (or attackers if parameter is set).
	 * @param message : The SystemMessage of the message to send to player
	 * @param bothSides : If true, broadcast it too to attackers clans.
	 */
	public void announceToPlayers(SystemMessage message, boolean bothSides)
	{
		for (Clan clan : getDefenderClans())
			clan.broadcastToOnlineMembers(message);
		
		if (bothSides)
		{
			for (Clan clan : getAttackerClans())
				clan.broadcastToOnlineMembers(message);
		}
	}
	
	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		for (Clan clan : getAttackerClans())
		{
			for (Player member : clan.getOnlineMembers())
			{
				if (clear)
				{
					member.setSiegeState((byte) 0);
					member.setIsInSiege(false);
				}
				else
				{
					member.setSiegeState((byte) 1);
					if (checkIfInZone(member))
						member.setIsInSiege(true);
				}
				member.sendPacket(new UserInfo(member));
				member.broadcastRelationsChanges();
			}
		}
		
		for (Clan clan : getDefenderClans())
		{
			for (Player member : clan.getOnlineMembers())
			{
				if (clear)
				{
					member.setSiegeState((byte) 0);
					member.setIsInSiege(false);
				}
				else
				{
					member.setSiegeState((byte) 2);
					if (checkIfInZone(member))
						member.setIsInSiege(true);
				}
				member.sendPacket(new UserInfo(member));
				member.broadcastRelationsChanges();
			}
		}
	}
	
	/**
	 * Check if an object is inside an area using his location.
	 * @param object The Object to use positions.
	 * @return true if object is inside the zone
	 */
	public boolean checkIfInZone(WorldObject object)
	{
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return true if object is inside the zone
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return isInProgress() && getCastle().checkIfInZone(x, y, z); // Castle zone during siege
	}
	
	/** Clear all registered siege clans from database for castle */
	public void clearAllClans()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(CLEAR_SIEGE_CLANS))
		{
			ps.setInt(1, getCastle().getCastleId());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't clear siege registered clans.", e);
		}
		
		_registeredClans.clear();
		
		// Add back the owner after cleaning the map.
		if (getCastle().getOwnerId() > 0)
		{
			final Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
			if (clan != null)
				_registeredClans.put(clan, SiegeSide.OWNER);
		}
	}
	
	/** Clear all siege clans waiting for approval from database for castle */
	protected void clearPendingClans()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(CLEAR_PENDING_CLANS))
		{
			ps.setInt(1, getCastle().getCastleId());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't clear siege pending clans.", e);
		}
		
		_registeredClans.entrySet().removeIf(e -> e.getValue() == SiegeSide.PENDING);
	}
	
	/**
	 * Register clan as attacker
	 * @param player : The player trying to register
	 */
	public void registerAttacker(Player player)
	{
		if (player.getClan() == null)
			return;
		
		int allyId = 0;
		if (getCastle().getOwnerId() != 0)
			allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
		
		// If the castle owning clan got an alliance
		if (allyId != 0)
		{
			// Same alliance can't be attacked
			if (player.getClan().getAllyId() == allyId)
			{
				player.sendPacket(SystemMessageId.CANNOT_ATTACK_ALLIANCE_CASTLE);
				return;
			}
		}
		
		// Can't register as attacker if at least one allied clan is registered as defender
		if (allyIsRegisteredOnOppositeSide(player.getClan(), true))
			player.sendPacket(SystemMessageId.CANT_ACCEPT_ALLY_ENEMY_FOR_SIEGE);
		// Save to database
		else if (checkIfCanRegister(player, SiegeSide.ATTACKER))
			registerClan(player.getClan(), SiegeSide.ATTACKER);
	}
	
	/**
	 * Register clan as defender.
	 * @param player : The player trying to register
	 */
	public void registerDefender(Player player)
	{
		if (player.getClan() == null)
			return;
		
		// Castle owned by NPC is considered as full side
		if (getCastle().getOwnerId() <= 0)
			player.sendPacket(SystemMessageId.DEFENDER_SIDE_FULL);
		// Can't register as defender if at least one allied clan is registered as attacker
		else if (allyIsRegisteredOnOppositeSide(player.getClan(), false))
			player.sendPacket(SystemMessageId.CANT_ACCEPT_ALLY_ENEMY_FOR_SIEGE);
		// Save to database
		else if (checkIfCanRegister(player, SiegeSide.PENDING))
			registerClan(player.getClan(), SiegeSide.PENDING);
	}
	
	/**
	 * Verify if allies are registered on different list than the actual player's choice. Let's say clan A and clan B are in same alliance. If clan A wants to attack a castle, clan B mustn't be on defenders' list. The contrary is right too : you can't defend if one ally is on attackers' list.
	 * @param clan : The clan used for alliance existence checks.
	 * @param attacker : A boolean used to know if this check is used for attackers or defenders.
	 * @return true if one clan of the alliance is registered in other side.
	 */
	private boolean allyIsRegisteredOnOppositeSide(Clan clan, boolean attacker)
	{
		// Check if player's clan got an alliance ; if not, skip the check
		final int allyId = clan.getAllyId();
		if (allyId != 0)
		{
			// Verify through the clans list for existing clans
			for (Clan alliedClan : ClanTable.getInstance().getClans())
			{
				// If a clan with same allyId is found (so, same alliance)
				if (alliedClan.getAllyId() == allyId)
				{
					// Skip player's clan from the check
					if (alliedClan.getClanId() == clan.getClanId())
						continue;
					
					// If the check is made for attackers' list
					if (attacker)
					{
						// Check if the allied clan is on defender / defender waiting lists
						if (checkSides(alliedClan, SiegeSide.DEFENDER, SiegeSide.OWNER, SiegeSide.PENDING))
							return true;
					}
					else
					{
						// Check if the allied clan is on attacker list
						if (checkSides(alliedClan, SiegeSide.ATTACKER))
							return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Remove clan from siege. Drop it from _registeredClans and database. Castle owner can't be dropped.
	 * @param clan : The clan to check.
	 */
	public void unregisterClan(Clan clan)
	{
		// Check if clan parameter is ok, avoid to drop castle owner, then remove if possible. If it couldn't be removed, return.
		if (clan == null || clan.getCastleId() == getCastle().getCastleId() || _registeredClans.remove(clan) == null)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(CLEAR_SIEGE_CLAN))
		{
			ps.setInt(1, getCastle().getCastleId());
			ps.setInt(2, clan.getClanId());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't unregister clan on siege.", e);
		}
	}
	
	/**
	 * This method allows to :
	 * <ul>
	 * <li>Check if the siege time is deprecated, and recalculate otherwise.</li>
	 * <li>Schedule start siege (it's in an else because saveCastleSiege() already affect it).</li>
	 * </ul>
	 */
	private void startAutoTask()
	{
		if (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
			saveCastleSiege(false);
		else
		{
			if (_siegeTask != null)
				_siegeTask.cancel(false);
			
			_siegeTask = ThreadPool.schedule(new SiegeTask(getCastle()), 1000);
		}
	}
	
	/**
	 * @param player : The player trying to register.
	 * @param type : The SiegeSide to test.
	 * @return true if the player can register.
	 */
	private boolean checkIfCanRegister(Player player, SiegeSide type)
	{
		SystemMessage sm;
		
		if (isRegistrationOver())
			sm = SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED).addString(getCastle().getName());
		else if (isInProgress())
			sm = SystemMessage.getSystemMessage(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
		else if (player.getClan() == null || player.getClan().getLevel() < Config.MINIMUM_CLAN_LEVEL)
			sm = SystemMessage.getSystemMessage(SystemMessageId.ONLY_CLAN_LEVEL_4_ABOVE_MAY_SIEGE);
		else if (player.getClan().hasCastle())
			sm = (player.getClan().getClanId() == getCastle().getOwnerId()) ? SystemMessage.getSystemMessage(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING) : SystemMessage.getSystemMessage(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
		else if (player.getClan().isRegisteredOnSiege())
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
		else if (checkIfAlreadyRegisteredForSameDay(player.getClan()))
			sm = SystemMessage.getSystemMessage(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE);
		else if (type == SiegeSide.ATTACKER && getAttackerClans().size() >= Config.MAX_ATTACKERS_NUMBER)
			sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACKER_SIDE_FULL);
		else if ((type == SiegeSide.DEFENDER || type == SiegeSide.PENDING || type == SiegeSide.OWNER) && (getDefenderClans().size() + getPendingClans().size() >= Config.MAX_DEFENDERS_NUMBER))
			sm = SystemMessage.getSystemMessage(SystemMessageId.DEFENDER_SIDE_FULL);
		else
			return true;
		
		player.sendPacket(sm);
		return false;
	}
	
	/**
	 * @param clan The L2Clan of the player trying to register
	 * @return true if the clan has already registered to a siege for the same day.
	 */
	public boolean checkIfAlreadyRegisteredForSameDay(Clan clan)
	{
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			final Siege siege = castle.getSiege();
			if (siege == this)
				continue;
			
			if (siege.getSiegeDate().get(Calendar.DAY_OF_WEEK) == getSiegeDate().get(Calendar.DAY_OF_WEEK) && siege.checkSides(clan))
				return true;
		}
		return false;
	}
	
	/** Remove all spawned towers. */
	private void removeTowers()
	{
		for (FlameTower ct : _flameTowers)
			ct.deleteMe();
		
		for (ControlTower ct : _controlTowers)
			ct.deleteMe();
		
		for (Npc ct : _destroyedTowers)
			ct.deleteMe();
		
		_flameTowers.clear();
		_controlTowers.clear();
		_destroyedTowers.clear();
	}
	
	/**
	 * Save castle siege related to database.
	 * @param launchTask : if true, launch the start siege task.
	 */
	private void saveCastleSiege(boolean launchTask)
	{
		// Set the next siege date in 2 weeks from now.
		setNextSiegeDate();
		
		// You can edit time anew.
		getCastle().setTimeRegistrationOver(false);
		
		// Save the new date.
		saveSiegeDate();
		
		// Prepare start siege task.
		if (launchTask)
			startAutoTask();
		
		LOGGER.info("New date for {} siege: {}.", getCastle().getName(), getCastle().getSiegeDate().getTime());
	}
	
	/**
	 * Save siege date to database.
	 */
	private void saveSiegeDate()
	{
		if (_siegeTask != null)
		{
			_siegeTask.cancel(true);
			_siegeTask = ThreadPool.schedule(new SiegeTask(getCastle()), 1000);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_SIEGE_INFOS))
		{
			ps.setLong(1, getSiegeDate().getTimeInMillis());
			ps.setString(2, String.valueOf(isTimeRegistrationOver()));
			ps.setInt(3, getCastle().getCastleId());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't save siege date.", e);
		}
	}
	
	/**
	 * Save registration to database.
	 * @param clan : The L2Clan of player.
	 * @param type
	 */
	public void registerClan(Clan clan, SiegeSide type)
	{
		if (clan.hasCastle())
			return;
		
		switch (type)
		{
			case DEFENDER:
			case PENDING:
			case OWNER:
				if (getDefenderClans().size() + getPendingClans().size() >= Config.MAX_DEFENDERS_NUMBER)
					return;
				break;
			
			default:
				if (getAttackerClans().size() >= Config.MAX_ATTACKERS_NUMBER)
					return;
				break;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(ADD_OR_UPDATE_SIEGE_CLAN))
		{
			ps.setInt(1, clan.getClanId());
			ps.setInt(2, getCastle().getCastleId());
			ps.setString(3, type.toString());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't register clan on siege.", e);
		}
		
		_registeredClans.put(clan, type);
	}
	
	/**
	 * Set the date for the next siege.
	 */
	private void setNextSiegeDate()
	{
		final Calendar siegeDate = getCastle().getSiegeDate();
		if (siegeDate.getTimeInMillis() < System.currentTimeMillis())
			siegeDate.setTimeInMillis(System.currentTimeMillis());
		
		switch (getCastle().getCastleId())
		{
			case 3:
			case 4:
			case 6:
			case 7:
				siegeDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
				break;
			
			default:
				siegeDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
				break;
		}
		
		// Set next siege date if siege has passed ; add 14 days (2 weeks).
		siegeDate.add(Calendar.WEEK_OF_YEAR, 2);
		
		// Set default hour to 18:00. This can be changed - only once - by the castle leader via the chamberlain.
		siegeDate.set(Calendar.HOUR_OF_DAY, 18);
		siegeDate.set(Calendar.MINUTE, 0);
		siegeDate.set(Calendar.SECOND, 0);
		siegeDate.set(Calendar.MILLISECOND, 0);
		
		// Send message and allow registration for next siege.
		World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_ANNOUNCED_SIEGE_TIME).addString(getCastle().getName()));
		changeStatus(SiegeStatus.REGISTRATION_OPENED);
	}
	
	/**
	 * Spawn control towers.
	 */
	private void spawnControlTowers()
	{
		for (TowerSpawnLocation ts : getCastle().getControlTowers())
		{
			try
			{
				final L2Spawn spawn = new L2Spawn(NpcData.getInstance().getTemplate(ts.getId()));
				spawn.setLoc(ts);
				
				final ControlTower tower = (ControlTower) spawn.doSpawn(false);
				tower.setCastle(getCastle());
				
				_controlTowers.add(tower);
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't spawn control tower.", e);
			}
		}
	}
	
	/**
	 * Spawn flame towers.
	 */
	private void spawnFlameTowers()
	{
		for (TowerSpawnLocation ts : getCastle().getFlameTowers())
		{
			try
			{
				final L2Spawn spawn = new L2Spawn(NpcData.getInstance().getTemplate(ts.getId()));
				spawn.setLoc(ts);
				
				final FlameTower tower = (FlameTower) spawn.doSpawn(false);
				tower.setCastle(getCastle());
				tower.setUpgradeLevel(ts.getUpgradeLevel());
				tower.setZoneList(ts.getZoneList());
				
				_flameTowers.add(tower);
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't spawn flame tower.", e);
			}
		}
	}
	
	public final Castle getCastle()
	{
		return _castle;
	}
	
	public final boolean isInProgress()
	{
		return _siegeStatus == SiegeStatus.IN_PROGRESS;
	}
	
	public final boolean isRegistrationOver()
	{
		return _siegeStatus != SiegeStatus.REGISTRATION_OPENED;
	}
	
	public final boolean isTimeRegistrationOver()
	{
		return getCastle().isTimeRegistrationOver();
	}
	
	/**
	 * @return siege registration end date, which always equals siege date minus one day.
	 */
	public final long getSiegeRegistrationEndDate()
	{
		return getCastle().getSiegeDate().getTimeInMillis() - 86400000;
	}
	
	public void endTimeRegistration(boolean automatic)
	{
		getCastle().setTimeRegistrationOver(true);
		if (!automatic)
			saveSiegeDate();
	}
	
	public int getControlTowerCount()
	{
		return (int) _controlTowers.stream().filter(lc -> lc.isActive()).count();
	}
	
	public List<Npc> getDestroyedTowers()
	{
		return _destroyedTowers;
	}
	
	public class EndSiegeTask implements Runnable
	{
		private final Castle _castle;
		
		public EndSiegeTask(Castle castle)
		{
			_castle = castle;
		}
		
		@Override
		public void run()
		{
			if (!isInProgress())
				return;
			
			final long timeRemaining = _siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
			if (timeRemaining > 3600000)
			{
				announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_HOURS_UNTIL_SIEGE_CONCLUSION).addNumber(2), true);
				ThreadPool.schedule(new EndSiegeTask(_castle), timeRemaining - 3600000);
			}
			else if (timeRemaining <= 3600000 && timeRemaining > 600000)
			{
				announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION).addNumber(Math.round(timeRemaining / 60000)), true);
				ThreadPool.schedule(new EndSiegeTask(_castle), timeRemaining - 600000);
			}
			else if (timeRemaining <= 600000 && timeRemaining > 300000)
			{
				announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION).addNumber(Math.round(timeRemaining / 60000)), true);
				ThreadPool.schedule(new EndSiegeTask(_castle), timeRemaining - 300000);
			}
			else if (timeRemaining <= 300000 && timeRemaining > 10000)
			{
				announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION).addNumber(Math.round(timeRemaining / 60000)), true);
				ThreadPool.schedule(new EndSiegeTask(_castle), timeRemaining - 10000);
			}
			else if (timeRemaining <= 10000 && timeRemaining > 0)
			{
				announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.CASTLE_SIEGE_S1_SECONDS_LEFT).addNumber(Math.round(timeRemaining / 1000)), true);
				ThreadPool.schedule(new EndSiegeTask(_castle), timeRemaining);
			}
			else
				_castle.getSiege().endSiege();
		}
	}
	
	private class SiegeTask implements Runnable
	{
		private final Castle _castle;
		
		public SiegeTask(Castle castle)
		{
			_castle = castle;
		}
		
		@Override
		public void run()
		{
			_siegeTask.cancel(false);
			if (isInProgress())
				return;
			
			if (!isTimeRegistrationOver())
			{
				final long regTimeRemaining = getSiegeRegistrationEndDate() - Calendar.getInstance().getTimeInMillis();
				if (regTimeRemaining > 0)
				{
					_siegeTask = ThreadPool.schedule(new SiegeTask(_castle), regTimeRemaining);
					return;
				}
				
				endTimeRegistration(true);
			}
			
			final long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
			
			if (timeRemaining > 86400000)
				_siegeTask = ThreadPool.schedule(new SiegeTask(_castle), timeRemaining - 86400000);
			else if (timeRemaining <= 86400000 && timeRemaining > 13600000)
			{
				World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED).addString(getCastle().getName()));
				changeStatus(SiegeStatus.REGISTRATION_OVER);
				clearPendingClans();
				_siegeTask = ThreadPool.schedule(new SiegeTask(_castle), timeRemaining - 13600000);
			}
			else if (timeRemaining <= 13600000 && timeRemaining > 600000)
				_siegeTask = ThreadPool.schedule(new SiegeTask(_castle), timeRemaining - 600000);
			else if (timeRemaining <= 600000 && timeRemaining > 300000)
				_siegeTask = ThreadPool.schedule(new SiegeTask(_castle), timeRemaining - 300000);
			else if (timeRemaining <= 300000 && timeRemaining > 10000)
				_siegeTask = ThreadPool.schedule(new SiegeTask(_castle), timeRemaining - 10000);
			else if (timeRemaining <= 10000 && timeRemaining > 0)
				_siegeTask = ThreadPool.schedule(new SiegeTask(_castle), timeRemaining);
			else
				_castle.getSiege().startSiege();
		}
	}
	
	public void addQuestEvent(Quest quest)
	{
		if (_questEvents.isEmpty())
			_questEvents = new ArrayList<>(3);
		
		_questEvents.add(quest);
	}
	
	public SiegeStatus getStatus()
	{
		return _siegeStatus;
	}
	
	protected void changeStatus(SiegeStatus status)
	{
		_siegeStatus = status;
		
		for (Quest quest : _questEvents)
			quest.onSiegeEvent();
	}
}
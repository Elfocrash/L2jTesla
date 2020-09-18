package dev.l2j.tesla.gameserver.model.rift;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.network.serverpackets.Earthquake;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.manager.DimensionalRiftManager;

/**
 * The main core of Dimension Rift system, which is part of Seven Signs.<br>
 * <br>
 * In order to participate, a {@link Player} has to pick the quest from Dimensional Gate Keeper. Killing monsters in Seven Signs reward him with Dimensional Fragments, which can be used to enter the Rifts.<br>
 * <br>
 * A {@link Party} of at least 2 members is required. There are 6 different Rifts based on level range, accessible by the given NPC.<br>
 * <br>
 * Once Dimensional Fragments are consumed, the Party is moved into the first {@link DimensionalRiftRoom}. Monsters spawn after 10sec. Party leader got the possibility to use a free-token to change of room, but only once. The Party automatically jumps of room in an interval between 480 and 600sec.
 * An earthquake effect happens 7sec before the teleport.<br>
 * <br>
 * In the Rift, one of the cell is inhabited by Anakazel, a Raid Boss. The cell can only be reached through regular teleport ; this cell can't be the first one, nor a free-token one.<br>
 * <br>
 * The Dimensional Rift collapses after 4 jumps, or if the party wiped out. Players using teleport (SoE, unstuck,...) are moved back to Dimensional Waiting Room. If the amount of Players inside the Rift is reduced to 1, then the Rift collapses. Any Party edit (add/remove party member) also produces
 * the Rift to collapse.
 */
public class DimensionalRift
{
	protected final Set<Byte> _completedRooms = ConcurrentHashMap.newKeySet();
	protected final Set<Player> _revivedInWaitingRoom = ConcurrentHashMap.newKeySet();
	
	protected Party _party;
	protected DimensionalRiftRoom _room;
	
	private Future<?> _teleporterTimerTask;
	private Future<?> _spawnTimerTask;
	private Future<?> _earthQuakeTask;
	
	protected byte _currentJumps = 0;
	private boolean _hasJumped = false;
	
	public DimensionalRift(Party party, DimensionalRiftRoom room)
	{
		_party = party;
		_room = room;
		
		room.setPartyInside(true);
		
		party.setDimensionalRift(this);
		
		for (Player member : party.getMembers())
			member.teleToLocation(room.getTeleportLoc());
		
		prepareNextRoom();
	}
	
	/**
	 * @return the current {@link DimensionalRiftRoom}.
	 */
	public DimensionalRiftRoom getCurrentRoom()
	{
		return _room;
	}
	
	/**
	 * @param object : The {@link WorldObject} to test.
	 * @return true if the WorldObject is inside this {@link DimensionalRift} current {@link DimensionalRiftRoom}.
	 */
	public boolean isInCurrentRoomZone(WorldObject object)
	{
		return _room != null && _room.checkIfInZone(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * @param party : The {@link Party} to test.
	 * @return a {@link List} consisting of {@link Player}s who are still inside a Rift.
	 */
	protected List<Player> getAvailablePlayers(Party party)
	{
		if (party == null)
			return Collections.emptyList();
		
		return party.getMembers().stream().filter(p -> !_revivedInWaitingRoom.contains(p)).collect(Collectors.toList());
	}
	
	/**
	 * Prepare the next room.
	 * <ul>
	 * <li>End all running tasks.</li>
	 * <li>Generate spawn, earthquake and next teleport tasks.</li>
	 * </ul>
	 */
	protected void prepareNextRoom()
	{
		if (_spawnTimerTask != null)
		{
			_spawnTimerTask.cancel(false);
			_spawnTimerTask = null;
		}
		
		if (_teleporterTimerTask != null)
		{
			_teleporterTimerTask.cancel(false);
			_teleporterTimerTask = null;
		}
		
		if (_earthQuakeTask != null)
		{
			_earthQuakeTask.cancel(false);
			_earthQuakeTask = null;
		}
		
		_spawnTimerTask = ThreadPool.schedule(() -> _room.spawn(), Config.RIFT_SPAWN_DELAY);
		
		long jumpTime = Rnd.get(Config.RIFT_AUTO_JUMPS_TIME_MIN, Config.RIFT_AUTO_JUMPS_TIME_MAX) * 1000;
		if (_room.isBossRoom())
			jumpTime *= Config.RIFT_BOSS_ROOM_TIME_MUTIPLY;
		
		_earthQuakeTask = ThreadPool.schedule(() ->
		{
			for (Player member : getAvailablePlayers(_party))
				member.sendPacket(new Earthquake(member.getX(), member.getY(), member.getZ(), 65, 9));
		}, jumpTime - 7000);
		
		_teleporterTimerTask = ThreadPool.schedule(() ->
		{
			_room.unspawn();
			
			if (_currentJumps < Config.RIFT_MAX_JUMPS && !_party.wipedOut())
			{
				_currentJumps++;
				
				chooseRoomAndTeleportPlayers(_room.getType(), getAvailablePlayers(_party), true);
				
				prepareNextRoom();
			}
			else
				killRift();
		}, jumpTime);
	}
	
	/**
	 * The manual teleport, consisting to {@link Player} using the free token on the teleporter {@link Npc} to freely change of room. Only one token is normally allowed.
	 * @param player : The tested Player.
	 * @param npc : The called Npc.
	 */
	public void manualTeleport(Player player, Npc npc)
	{
		final Party party = player.getParty();
		if (party == null || !party.isInDimensionalRift())
			return;
		
		if (!party.isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		if (_currentJumps == Config.RIFT_MAX_JUMPS)
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/UsedAllJumps.htm", npc);
			return;
		}
		
		if (_hasJumped)
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/AlreadyTeleported.htm", npc);
			return;
		}
		_hasJumped = true;
		
		_room.unspawn();
		
		chooseRoomAndTeleportPlayers(_room.getType(), _party.getMembers(), false);
		
		prepareNextRoom();
	}
	
	/**
	 * Allow a {@link Player} to manually exit the {@link DimensionalRift}. He needs to be party leader. If successful, the DimensionalRift is destroyed.
	 * @param player : The tested Player.
	 * @param npc : The called Npc.
	 */
	public void manualExitRift(Player player, Npc npc)
	{
		final Party party = player.getParty();
		if (party == null || !party.isInDimensionalRift())
			return;
		
		if (!party.isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		killRift();
	}
	
	/**
	 * This method allows to jump from one room to another.
	 * @param type : The type of {@link DimensionalRift} to handle.
	 * @param players : The List of {@link Player}s to teleport.
	 * @param canUseBossRoom : if false, Anakazel room can't be choosen (case of manual teleport).
	 */
	protected void chooseRoomAndTeleportPlayers(byte type, List<Player> players, boolean canUseBossRoom)
	{
		// Set the current room id as used.
		_completedRooms.add(_room.getId());
		
		// Compute free rooms for the Party.
		final List<DimensionalRiftRoom> list = DimensionalRiftManager.getInstance().getFreeRooms(type, canUseBossRoom).stream().filter(r -> !_completedRooms.contains(r.getId())).collect(Collectors.toList());
		
		// If no rooms are found, simply break the Party Rift.
		if (list.isEmpty())
		{
			killRift();
			return;
		}
		
		// List is filled ; return a random Room and set it as filled.
		_room = Rnd.get(list);
		_room.setPartyInside(true);
		
		// Teleport all Players in.
		for (Player member : players)
			member.teleToLocation(_room.getTeleportLoc());
	}
	
	/**
	 * Cleanup this instance of {@link DimensionalRift}.
	 * <ul>
	 * <li>Teleport all {@link Player}s to waiting room.</li>
	 * <li>Clean all {@link List}s.</li>
	 * <li>Stop all running tasks.</li>
	 * <li>Unspawn minions and set the room as unused.</li>
	 * </ul>
	 */
	public void killRift()
	{
		if (_party != null)
		{
			for (Player member : getAvailablePlayers(_party))
				DimensionalRiftManager.getInstance().teleportToWaitingRoom(member);
			
			_party.setDimensionalRift(null);
			_party = null;
		}
		
		_completedRooms.clear();
		_revivedInWaitingRoom.clear();
		
		if (_earthQuakeTask != null)
		{
			_earthQuakeTask.cancel(false);
			_earthQuakeTask = null;
		}
		
		if (_teleporterTimerTask != null)
		{
			_teleporterTimerTask.cancel(false);
			_teleporterTimerTask = null;
		}
		
		if (_spawnTimerTask != null)
		{
			_spawnTimerTask.cancel(false);
			_spawnTimerTask = null;
		}
		
		_room.unspawn();
		_room = null;
	}
	
	/**
	 * On {@link Player} teleport (SoE, unstuck,...), put the Player on revived {@link List}. If the minimum required party members amount isn't reached, we kill this {@link DimensionalRift} instance.
	 * @param player : The Player to test.
	 */
	public void usedTeleport(Player player)
	{
		_revivedInWaitingRoom.add(player);
		
		if (_party.getMembersCount() - _revivedInWaitingRoom.size() < Config.RIFT_MIN_PARTY_SIZE)
			killRift();
	}
}
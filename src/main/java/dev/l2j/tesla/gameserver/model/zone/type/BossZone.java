package dev.l2j.tesla.gameserver.model.zone.type;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.zone.ZoneType;
import dev.l2j.tesla.gameserver.data.xml.MapRegionData.TeleportType;
import dev.l2j.tesla.gameserver.enums.ZoneId;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;

/**
 * A Boss zone, extending {@link ZoneType}. It holds a {@link List} and a {@link Map} of allowed {@link Player}s.<br>
 * <br>
 * The Map is used for Players disconnections, while the List is used for Players to re-enter the zone after server downtime/restart.
 */
public class BossZone extends ZoneType
{
	private static final String SELECT_GRAND_BOSS_LIST = "SELECT * FROM grandboss_list WHERE zone = ?";
	
	// Track the times that players got disconnected. Players are allowed to log back into the zone as long as their log-out was within _timeInvade time...
	private final Map<Integer, Long> _allowedPlayersEntryTime = new ConcurrentHashMap<>();
	
	// Track players admitted to the zone who should be allowed back in after reboot/server downtime, within 30min of server restart
	private final Set<Integer> _allowedPlayers = ConcurrentHashMap.newKeySet();
	
	private final int[] _oustLoc = new int[3];
	
	private int _invadeTime;
	
	public BossZone(int id)
	{
		super(id);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_GRAND_BOSS_LIST))
		{
			ps.setInt(1, id);
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
					allowPlayerEntry(rs.getInt("player_id"));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load players for {}.", e, toString());
		}
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("InvadeTime"))
			_invadeTime = Integer.parseInt(value);
		else if (name.equals("oustX"))
			_oustLoc[0] = Integer.parseInt(value);
		else if (name.equals("oustY"))
			_oustLoc[1] = Integer.parseInt(value);
		else if (name.equals("oustZ"))
			_oustLoc[2] = Integer.parseInt(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.BOSS, true);
		
		if (character instanceof Player)
		{
			// Get player and set zone info.
			final Player player = (Player) character;
			player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			
			// Skip other checks for GM or if no invade time is set.
			if (player.isGM() || _invadeTime == 0)
				return;
			
			// Get player object id.
			final int id = player.getObjectId();
			
			if (_allowedPlayers.contains(id))
			{
				// Get and remove the entry expiration time (once entered, can not enter enymore, unless specified).
				final long entryTime = _allowedPlayersEntryTime.remove(id);
				if (entryTime > System.currentTimeMillis())
					return;
				
				// Player trying to join after expiration, remove from allowed list.
				_allowedPlayers.remove(Integer.valueOf(id));
			}
			
			// Teleport out player, who attempt "illegal" (re-)entry.
			if (_oustLoc[0] != 0 && _oustLoc[1] != 0 && _oustLoc[2] != 0)
				player.teleportTo(_oustLoc[0], _oustLoc[1], _oustLoc[2], 0);
			else
				player.teleportTo(TeleportType.TOWN);
		}
		else if (character instanceof Summon)
		{
			final Player player = ((Summon) character).getOwner();
			if (player != null)
			{
				if (_allowedPlayers.contains(player.getObjectId()) || player.isGM() || _invadeTime == 0)
					return;
				
				// Remove summon.
				((Summon) character).unSummon(player);
			}
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.BOSS, false);
		
		if (character instanceof Playable)
		{
			if (character instanceof Player)
			{
				// Get player and set zone info.
				final Player player = (Player) character;
				player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
				
				// Skip other checks for GM or if no invade time is set.
				if (player.isGM() || _invadeTime == 0)
					return;
				
				// Get player object id.
				final int id = player.getObjectId();
				
				if (_allowedPlayers.contains(id))
				{
					if (!player.isOnline())
					{
						// Player disconnected.
						_allowedPlayersEntryTime.put(id, System.currentTimeMillis() + _invadeTime);
					}
					else
					{
						// Player has allowed entry, do not delete from allowed list.
						if (_allowedPlayersEntryTime.containsKey(id))
							return;
						
						// Remove player allowed list.
						_allowedPlayers.remove(Integer.valueOf(id));
					}
				}
			}
			
			// If playables aren't found, force all bosses to return to spawnpoint.
			if (!_characters.isEmpty())
			{
				if (!getKnownTypeInside(Playable.class).isEmpty())
					return;
				
				for (Attackable raid : getKnownTypeInside(Attackable.class))
				{
					if (!raid.isRaidRelated())
						continue;
					
					raid.returnHome();
				}
			}
		}
		else if (character instanceof Attackable && character.isRaidRelated())
			((Attackable) character).returnHome();
	}
	
	/**
	 * Enables the entry of a {@link Player} to this {@link BossZone} for next "duration" seconds. If the Player tries to enter the zone after this period, he will be teleported out.
	 * @param player : The allowed player to entry.
	 * @param duration : The entry permission period (in seconds).
	 */
	public void allowPlayerEntry(Player player, int duration)
	{
		// Get player object id.
		final int playerId = player.getObjectId();
		
		// Allow player entry.
		if (!_allowedPlayers.contains(playerId))
			_allowedPlayers.add(playerId);
		
		// For the given duration.
		_allowedPlayersEntryTime.put(playerId, System.currentTimeMillis() + duration * 1000);
	}
	
	/**
	 * Enables the entry of a {@link Player} to this {@link BossZone} after server shutdown/restart. The time limit is specified by each zone via "InvadeTime" parameter. If the player tries to enter the zone after this period, he will be teleported out.
	 * @param playerId : The objectid of the allowed player to entry.
	 */
	public void allowPlayerEntry(int playerId)
	{
		// Allow player entry.
		if (!_allowedPlayers.contains(playerId))
			_allowedPlayers.add(playerId);
		
		// For the given duration.
		_allowedPlayersEntryTime.put(playerId, System.currentTimeMillis() + _invadeTime);
	}
	
	/**
	 * Removes the {@link Player} from allowed list and cancel the entry permition.
	 * @param player : Player to remove from the zone.
	 */
	public void removePlayer(Player player)
	{
		// Get player object id.
		final int id = player.getObjectId();
		
		// Remove player from allowed list.
		_allowedPlayers.remove(Integer.valueOf(id));
		
		// Remove player permission.
		_allowedPlayersEntryTime.remove(id);
	}
	
	/**
	 * @return the list of all allowed {@link Player}s objectIds.
	 */
	public Set<Integer> getAllowedPlayers()
	{
		return _allowedPlayers;
	}
	
	/**
	 * Teleport all {@link Player}s located in this {@link BossZone} to a specific location, as listed on {@link #_oustLoc}. Clear both containers holding Players informations.
	 * @return the List of all Players who have been forced to teleport.
	 */
	public List<Player> oustAllPlayers()
	{
		final List<Player> players = getKnownTypeInside(Player.class);
		if (players.isEmpty())
			return players;
		
		for (Player player : players)
		{
			if (player.isOnline())
			{
				if (_oustLoc[0] != 0 && _oustLoc[1] != 0 && _oustLoc[2] != 0)
					player.teleportTo(_oustLoc[0], _oustLoc[1], _oustLoc[2], 0);
				else
					player.teleportTo(TeleportType.TOWN);
			}
		}
		_allowedPlayersEntryTime.clear();
		_allowedPlayers.clear();
		
		return players;
	}
}
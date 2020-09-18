package dev.l2j.tesla.gameserver.data.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.commons.logging.CLogger;

/**
 * Loads and stores Raid Points.<br>
 * <br>
 * {@link Player}s obtain Raid Points by killing Raid Bosses within the world of Lineage 2. Each week the Top ranked players clans get rewarded with Clan Reputation Points in relation to their members ranking.
 */
public class RaidPointManager
{
	private static final CLogger LOGGER = new CLogger(RaidPointManager.class.getName());
	
	private static final String LOAD_DATA = "SELECT * FROM character_raid_points";
	private static final String INSERT_DATA = "REPLACE INTO character_raid_points (char_id,boss_id,points) VALUES (?,?,?)";
	private static final String DELETE_DATA = "TRUNCATE TABLE character_raid_points";
	
	private final Map<Integer, Map<Integer, Integer>> _entries = new ConcurrentHashMap<>();
	
	public RaidPointManager()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(LOAD_DATA);
             ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final int objectId = rs.getInt("char_id");
				final int bossId = rs.getInt("boss_id");
				final int points = rs.getInt("points");
				
				Map<Integer, Integer> playerData = _entries.get(objectId);
				if (playerData == null)
					playerData = new HashMap<>();
				
				playerData.put(bossId, points);
				_entries.put(objectId, playerData);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load RaidPoints entries.", e);
		}
		LOGGER.info("Loaded {} RaidPoints entries.", _entries.size());
	}
	
	public final Map<Integer, Integer> getList(Player player)
	{
		return _entries.get(player.getObjectId());
	}
	
	/**
	 * Add points for a given {@link Player} and a given boss npcId.
	 * @param player : The player used for objectId.
	 * @param bossId : The boss npcId to register.
	 * @param points : The points to add.
	 */
	public final void addPoints(Player player, int bossId, int points)
	{
		final int objectId = player.getObjectId();
		
		Map<Integer, Integer> playerData = _entries.get(objectId);
		if (playerData == null)
		{
			playerData = new HashMap<>();
			
			_entries.put(objectId, playerData);
		}
		playerData.merge(bossId, points, Integer::sum);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_DATA))
		{
			ps.setInt(1, objectId);
			ps.setInt(2, bossId);
			ps.setInt(3, points);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't update RaidPoints entries.", e);
		}
	}
	
	/**
	 * @param objectId : The objectId of the {@link Player} to check.
	 * @return the cumulated amount of points for a player, or 0 if no entry is found.
	 */
	public final int getPointsByOwnerId(int objectId)
	{
		Map<Integer, Integer> playerData = _entries.get(objectId);
		if (playerData == null || playerData.isEmpty())
			return 0;
		
		return playerData.values().stream().mapToInt(Number::intValue).sum();
	}
	
	/**
	 * Clean both database and memory from content.
	 */
	public final void cleanUp()
	{
		_entries.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_DATA))
		{
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete RaidPoints entries.", e);
		}
	}
	
	/**
	 * @param objectId : The objectId of the {@link Player} to check.
	 * @return the current rank of a player based on its objectId, or 0 if no entry is found.
	 */
	public final int calculateRanking(int objectId)
	{
		// Iterate the global list, retrieve players and associated cumulated points. Store them if > 0.
		final Map<Integer, Integer> playersData = new HashMap<>();
		for (int ownerId : _entries.keySet())
		{
			final int points = getPointsByOwnerId(ownerId);
			if (points > 0)
				playersData.put(ownerId, points);
		}
		
		final AtomicInteger counter = new AtomicInteger(1);
		
		final Map<Integer, Integer> rankMap = new LinkedHashMap<>();
		
		// Sort the temporary points map, then feed rankMap with objectId as key and ranking as value.
		playersData.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).forEachOrdered(e -> rankMap.put(e.getKey(), counter.getAndIncrement()));
		
		final Integer rank = rankMap.get(objectId);
		return (rank == null) ? 0 : rank;
	}
	
	/**
	 * A method used to generate the ranking map. Limited to the first 100 players.
	 * <ul>
	 * <li>Retrieve player data, compute their points and store the entry on a temporary map if the points are > 0.</li>
	 * <li>Sort the temporary map based on points and feed a second map with objectId<>ranking.</li>
	 * </ul>
	 * @return a {@link Map} consisting of {@link Player} objectId for the key, and ranking for the value.
	 */
	public Map<Integer, Integer> getWinners()
	{
		// Iterate the global list, retrieve players and associated cumulated points. Store them if > 0.
		final Map<Integer, Integer> playersData = new HashMap<>();
		for (int objectId : _entries.keySet())
		{
			final int points = getPointsByOwnerId(objectId);
			if (points > 0)
				playersData.put(objectId, points);
		}
		
		final AtomicInteger counter = new AtomicInteger(1);
		
		final Map<Integer, Integer> rankMap = new LinkedHashMap<>();
		
		// Sort the temporary points map, then feed rankMap with objectId as key and ranking as value.
		playersData.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(100).forEachOrdered(e -> rankMap.put(e.getKey(), counter.getAndIncrement()));
		
		return rankMap;
	}
	
	public static final RaidPointManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RaidPointManager INSTANCE = new RaidPointManager();
	}
}
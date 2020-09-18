package dev.l2j.tesla.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.data.xml.MapRegionData;
import dev.l2j.tesla.gameserver.data.xml.NpcData;
import dev.l2j.tesla.gameserver.idfactory.IdFactory;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.location.SpawnLocation;
import dev.l2j.tesla.gameserver.model.spawn.AutoSpawn;
import dev.l2j.tesla.gameserver.model.spawn.L2Spawn;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.random.Rnd;

/**
 * Allows spawning of a NPC object based on a timer (from the official idea used for the Merchant and Blacksmith of Mammon).
 * <P>
 * General Usage:<BR>
 * Call registerSpawn() with the parameters listed below:
 * <UL>
 * <LI>int npcId int[][] spawnPoints or specify NULL to add points later.</LI>
 * <LI>int initialDelay (If < 0 = default value)</LI>
 * <LI>int respawnDelay (If < 0 = default value)</LI>
 * <LI>int despawnDelay (If < 0 = default value or if = 0, function disabled)</LI>
 * </UL>
 * spawnPoints is a standard two-dimensional int array containing X,Y and Z coordinates. The default respawn/despawn delays are currently every hour (as for Mammon on official servers).</LI><BR>
 * <LI>The resulting AutoSpawnInstance object represents the newly added spawn index. - The interal methods of this object can be used to adjust random spawning, for instance a call to setRandomSpawn(1, true); would set the spawn at index 1 to be randomly rather than sequentially-based. - Also they
 * can be used to specify the number of NPC instances to spawn using setSpawnCount(), and broadcast a message to all users using setBroadcast(). Random Spawning = OFF by default Broadcasting = OFF by default
 * @author Tempy
 */
public class AutoSpawnTable
{
	private static final CLogger LOGGER = new CLogger(AutoSpawnTable.class.getName());
	
	private static final int DEFAULT_INITIAL_SPAWN = 30000; // 30 seconds after registration
	private static final int DEFAULT_RESPAWN = 3600000; // 1 hour in millisecs
	private static final int DEFAULT_DESPAWN = 3600000; // 1 hour in millisecs
	
	private Map<Integer, AutoSpawn> _registeredSpawns = new ConcurrentHashMap<>();
	private Map<Integer, ScheduledFuture<?>> _runningSpawns = new ConcurrentHashMap<>();
	
	protected AutoSpawnTable()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM random_spawn ORDER BY groupId ASC");
             ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				// Register random spawn group, set various options on the created spawn instance.
				AutoSpawn spawnInst = registerSpawn(rs.getInt("npcId"), rs.getInt("initialDelay"), rs.getInt("respawnDelay"), rs.getInt("despawnDelay"));
				spawnInst.setSpawnCount(rs.getInt("count"));
				spawnInst.setBroadcast(rs.getBoolean("broadcastSpawn"));
				spawnInst.setRandomSpawn(rs.getBoolean("randomSpawn"));
				
				// Restore the spawn locations for this spawn group/instance.
				try (PreparedStatement ps2 = con.prepareStatement("SELECT * FROM random_spawn_loc WHERE groupId=?"))
				{
					ps2.setInt(1, rs.getInt("groupId"));
					
					try (ResultSet rs2 = ps2.executeQuery())
					{
						// Add each location to the spawn group/instance.
						while (rs2.next())
							spawnInst.addSpawnLocation(rs2.getInt("x"), rs2.getInt("y"), rs2.getInt("z"), rs2.getInt("heading"));
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't restore auto spawn data.", e);
		}
		LOGGER.info("Loaded {} auto spawns.", _registeredSpawns.size());
	}
	
	/**
	 * Registers a spawn with the given parameters with the spawner, and marks it as active.<br>
	 * Returns a AutoSpawnInstance containing info about the spawn.
	 * @param npcId
	 * @param spawnPoints
	 * @param initialDelay : (If < 0 = default value)
	 * @param respawnDelay : (If < 0 = default value)
	 * @param despawnDelay : despawnDelay (If < 0 = default value or if = 0, function disabled)
	 * @return AutoSpawnInstance spawnInst
	 */
	public AutoSpawn registerSpawn(int npcId, int[][] spawnPoints, int initialDelay, int respawnDelay, int despawnDelay)
	{
		if (initialDelay < 0)
			initialDelay = DEFAULT_INITIAL_SPAWN;
		
		if (respawnDelay < 0)
			respawnDelay = DEFAULT_RESPAWN;
		
		if (despawnDelay < 0)
			despawnDelay = DEFAULT_DESPAWN;
		
		AutoSpawn newSpawn = new AutoSpawn(npcId, initialDelay, respawnDelay, despawnDelay);
		
		if (spawnPoints != null)
			for (int[] spawnPoint : spawnPoints)
				newSpawn.addSpawnLocation(spawnPoint);
			
		final int newId = IdFactory.getInstance().getNextId();
		
		// Refresh the objectId.
		newSpawn.setObjectId(newId);
		
		// Store the spawn.
		_registeredSpawns.put(newId, newSpawn);
		
		setSpawnActive(newSpawn, true);
		
		return newSpawn;
	}
	
	/**
	 * Registers a spawn with the given parameters with the spawner, and marks it as active.<BR>
	 * Returns a AutoSpawnInstance containing info about the spawn.<BR>
	 * <BR>
	 * <B>Warning:</B> Spawn locations must be specified separately using addSpawnLocation().
	 * @param npcId
	 * @param initialDelay (If < 0 = default value)
	 * @param respawnDelay (If < 0 = default value)
	 * @param despawnDelay (If < 0 = default value or if = 0, function disabled)
	 * @return AutoSpawnInstance spawnInst
	 */
	public AutoSpawn registerSpawn(int npcId, int initialDelay, int respawnDelay, int despawnDelay)
	{
		return registerSpawn(npcId, null, initialDelay, respawnDelay, despawnDelay);
	}
	
	/**
	 * Remove a registered spawn from the list, specified by the given spawn instance.
	 * @param spawnInst
	 * @return boolean removedSuccessfully
	 */
	public boolean removeSpawn(AutoSpawn spawnInst)
	{
		if (!isSpawnRegistered(spawnInst))
			return false;
		
		try
		{
			// Try to remove from the list of registered spawns if it exists.
			_registeredSpawns.remove(spawnInst.getObjectId());
			
			// Cancel the currently associated running scheduled task.
			ScheduledFuture<?> respawnTask = _runningSpawns.remove(spawnInst.getObjectId());
			respawnTask.cancel(false);
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't auto spawn NPC {} (Object ID = {}).", e, spawnInst.getNpcId(), spawnInst.getObjectId());
			return false;
		}
		
		return true;
	}
	
	/**
	 * Sets the active state of the specified spawn.
	 * @param spawnInst
	 * @param isActive
	 */
	public void setSpawnActive(AutoSpawn spawnInst, boolean isActive)
	{
		if (spawnInst == null)
			return;
		
		int objectId = spawnInst.getObjectId();
		
		if (isSpawnRegistered(objectId))
		{
			ScheduledFuture<?> spawnTask = null;
			
			if (isActive)
			{
				AutoSpawner rs = new AutoSpawner(objectId);
				
				if (spawnInst.getDespawnDelay() > 0)
					spawnTask = ThreadPool.scheduleAtFixedRate(rs, spawnInst.getInitialDelay(), spawnInst.getRespawnDelay());
				else
					spawnTask = ThreadPool.schedule(rs, spawnInst.getInitialDelay());
				
				_runningSpawns.put(objectId, spawnTask);
			}
			else
			{
				spawnTask = _runningSpawns.remove(objectId);
				if (spawnTask != null)
					spawnTask.cancel(false);
				
				ThreadPool.execute(new AutoDespawner(objectId));
			}
			
			spawnInst.setSpawnActive(isActive);
		}
	}
	
	/**
	 * Returns the number of milliseconds until the next occurrance of the given spawn.
	 * @param spawnInst
	 * @return
	 */
	public final long getTimeToNextSpawn(AutoSpawn spawnInst)
	{
		int objectId = spawnInst.getObjectId();
		
		if (!isSpawnRegistered(objectId))
			return -1;
		
		return _runningSpawns.get(objectId).getDelay(TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Attempts to return the AutoSpawnInstance associated with the given NPC or Object ID type. <BR>
	 * Note: If isObjectId == false, returns first instance for the specified NPC ID.
	 * @param id
	 * @param isObjectId
	 * @return AutoSpawnInstance spawnInst
	 */
	public final AutoSpawn getAutoSpawnInstance(int id, boolean isObjectId)
	{
		if (isObjectId)
		{
			if (isSpawnRegistered(id))
				return _registeredSpawns.get(id);
		}
		else
		{
			for (AutoSpawn spawnInst : _registeredSpawns.values())
				if (spawnInst.getNpcId() == id)
					return spawnInst;
		}
		return null;
	}
	
	public Map<Integer, AutoSpawn> getAutoSpawnInstances(int npcId)
	{
		Map<Integer, AutoSpawn> spawnInstList = new HashMap<>();
		
		for (AutoSpawn spawnInst : _registeredSpawns.values())
			if (spawnInst.getNpcId() == npcId)
				spawnInstList.put(spawnInst.getObjectId(), spawnInst);
			
		return spawnInstList;
	}
	
	/**
	 * Tests if the specified object ID is assigned to an auto spawn.
	 * @param objectId
	 * @return boolean isAssigned
	 */
	public final boolean isSpawnRegistered(int objectId)
	{
		return _registeredSpawns.containsKey(objectId);
	}
	
	/**
	 * Tests if the specified spawn instance is assigned to an auto spawn.
	 * @param spawnInst
	 * @return boolean isAssigned
	 */
	public final boolean isSpawnRegistered(AutoSpawn spawnInst)
	{
		return _registeredSpawns.containsValue(spawnInst);
	}
	
	/**
	 * AutoSpawner Class This handles the main spawn task for an auto spawn instance, and initializes a despawner if required.
	 * @author Tempy
	 */
	private class AutoSpawner implements Runnable
	{
		private final int _objectId;
		
		protected AutoSpawner(int objectId)
		{
			_objectId = objectId;
		}
		
		@Override
		public void run()
		{
			try
			{
				// Retrieve the required spawn instance for this spawn task.
				AutoSpawn spawnInst = _registeredSpawns.get(_objectId);
				
				// If the spawn is not scheduled to be active, cancel the spawn task.
				if (!spawnInst.isSpawnActive())
					return;
				
				SpawnLocation[] locationList = spawnInst.getLocationList();
				
				// If there are no set co-ordinates, cancel the spawn task.
				if (locationList.length == 0)
				{
					LOGGER.warn("No coords specified for spawn instance (Object ID = {}).", _objectId);
					return;
				}
				
				int locationCount = locationList.length;
				int locationIndex = Rnd.get(locationCount);
				
				// If random spawning is disabled, the spawn at the next set of co-ordinates after the last. If the index is greater than the number of possible spawns, reset the counter to zero.
				if (!spawnInst.isRandomSpawn())
				{
					locationIndex = spawnInst.getLastLocIndex();
					locationIndex++;
					
					if (locationIndex == locationCount)
						locationIndex = 0;
					
					spawnInst.setLastLocIndex(locationIndex);
				}
				
				// Set the X, Y and Z co-ordinates, where this spawn will take place.
				final int x = locationList[locationIndex].getX();
				final int y = locationList[locationIndex].getY();
				final int z = locationList[locationIndex].getZ();
				final int heading = locationList[locationIndex].getHeading();
				
				// Fetch the template for this NPC ID and create a new spawn.
				final NpcTemplate template = NpcData.getInstance().getTemplate(spawnInst.getNpcId());
				if (template == null)
				{
					LOGGER.warn("Couldn't find npc template for id: {}.", spawnInst.getNpcId());
					return;
				}
				
				final L2Spawn newSpawn = new L2Spawn(template);
				newSpawn.setLoc(x, y, z, heading);
				
				if (spawnInst.getDespawnDelay() == 0)
					newSpawn.setRespawnDelay(spawnInst.getRespawnDelay());
				
				// Add the new spawn information to the spawn table, but do not store it.
				SpawnTable.getInstance().addSpawn(newSpawn, false);
				
				Npc npcInst = null;
				
				if (spawnInst.getSpawnCount() == 1)
				{
					npcInst = newSpawn.doSpawn(false);
					npcInst.setXYZ(npcInst.getX(), npcInst.getY(), npcInst.getZ());
					spawnInst.addNpcInstance(npcInst);
				}
				else
				{
					for (int i = 0; i < spawnInst.getSpawnCount(); i++)
					{
						npcInst = newSpawn.doSpawn(false);
						
						// To prevent spawning of more than one NPC in the exact same spot, move it slightly by a small random offset.
						npcInst.setXYZ(npcInst.getX() + Rnd.get(50), npcInst.getY() + Rnd.get(50), npcInst.getZ());
						
						// Add the NPC instance to the list of managed instances.
						spawnInst.addNpcInstance(npcInst);
					}
				}
				
				// Announce to all players that the spawn has taken place, with the nearest town location.
				if (npcInst != null && spawnInst.isBroadcasting())
					World.announceToOnlinePlayers("The " + npcInst.getName() + " has spawned near " + MapRegionData.getInstance().getClosestTownName(npcInst.getX(), npcInst.getY()) + "!");
				
				// If there is no despawn time, do not create a despawn task.
				if (spawnInst.getDespawnDelay() > 0)
					ThreadPool.schedule(new AutoDespawner(_objectId), spawnInst.getDespawnDelay() - 1000);
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't spawn (Object ID = {}).", e, _objectId);
			}
		}
	}
	
	/**
	 * Simply used as a secondary class for despawning an auto spawn instance.
	 */
	private class AutoDespawner implements Runnable
	{
		private final int _objectId;
		
		protected AutoDespawner(int objectId)
		{
			_objectId = objectId;
		}
		
		@Override
		public void run()
		{
			try
			{
				AutoSpawn spawnInst = _registeredSpawns.get(_objectId);
				if (spawnInst == null)
				{
					LOGGER.info("No spawn registered for object ID = {}.", _objectId);
					return;
				}
				
				for (Npc npcInst : spawnInst.getNPCInstanceList())
				{
					if (npcInst == null)
						continue;
					
					SpawnTable.getInstance().deleteSpawn(npcInst.getSpawn(), false);
					npcInst.deleteMe();
					spawnInst.removeNpcInstance(npcInst);
				}
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't despawn (Object ID = {}).", e, _objectId);
			}
		}
	}
	
	public static AutoSpawnTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoSpawnTable INSTANCE = new AutoSpawnTable();
	}
}
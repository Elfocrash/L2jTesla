package dev.l2j.tesla.gameserver.model.spawn;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.idfactory.IdFactory;
import dev.l2j.tesla.gameserver.model.location.SpawnLocation;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;

/**
 * This class manages the spawn and respawn of a {@link Npc}.
 */
public final class L2Spawn implements Runnable
{
	private static final Logger _log = Logger.getLogger(L2Spawn.class.getName());
	
	private NpcTemplate _template;
	
	private Constructor<?> _constructor;
	
	private Npc _npc;
	
	private SpawnLocation _loc;
	
	private int _respawnDelay;
	private int _respawnRandom;
	private boolean _respawnEnabled;
	
	private int _respawnMinDelay;
	private int _respawnMaxDelay;
	
	public L2Spawn(NpcTemplate template) throws SecurityException, ClassNotFoundException, NoSuchMethodException
	{
		// Set the template of the L2Spawn.
		_template = template;
		if (_template == null)
			return;
		
		// Create the generic constructor.
		Class<?>[] parameters =
		{
			int.class,
			Class.forName("dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate")
		};
		_constructor = Class.forName("dev.l2j.tesla.gameserver.model.actor.instance." + _template.getType()).getConstructor(parameters);
	}
	
	/**
	 * @return the {@link NpcTemplate} associated to this {@link L2Spawn}.
	 */
	public NpcTemplate getTemplate()
	{
		return _template;
	}
	
	/**
	 * @return the npc id of the {@link NpcTemplate}.
	 */
	public int getNpcId()
	{
		return _template.getNpcId();
	}
	
	/**
	 * @return the {@link Npc} instance of this {@link L2Spawn}.
	 */
	public Npc getNpc()
	{
		return _npc;
	}
	
	/**
	 * Sets the {@link SpawnLocation} of this {@link L2Spawn}.
	 * @param loc : The SpawnLocation to set.
	 */
	public void setLoc(SpawnLocation loc)
	{
		_loc = loc;
	}
	
	/**
	 * Sets the {@link SpawnLocation} of this {@link L2Spawn} using separate coordinates.
	 * @param locX : X coordinate.
	 * @param locY : Y coordinate.
	 * @param locZ : Z coordinate.
	 * @param heading : Heading.
	 */
	public void setLoc(int locX, int locY, int locZ, int heading)
	{
		_loc = new SpawnLocation(locX, locY, locZ, heading);
	}
	
	/**
	 * @return the {@link SpawnLocation} of this {@link L2Spawn}.
	 */
	public SpawnLocation getLoc()
	{
		return _loc;
	}
	
	/**
	 * @return the X coordinate of the {@link SpawnLocation}.
	 */
	public int getLocX()
	{
		return _loc.getX();
	}
	
	/**
	 * @return the Y coordinate of the {@link SpawnLocation}.
	 */
	public int getLocY()
	{
		return _loc.getY();
	}
	
	/**
	 * @return the Z coordinate of the {@link SpawnLocation}.
	 */
	public int getLocZ()
	{
		return _loc.getZ();
	}
	
	/**
	 * @return the heading coordinate of the {@link SpawnLocation}.
	 */
	public int getHeading()
	{
		return _loc.getHeading();
	}
	
	/**
	 * Set the respawn delay, representing the respawn time of this {@link L2Spawn}. It can't be inferior to 0, it is automatically modified to 1 second.
	 * @param delay : Respawn delay in seconds.
	 */
	public void setRespawnDelay(int delay)
	{
		_respawnDelay = Math.max(1, delay);
	}
	
	/**
	 * @return the respawn delay of this {@link L2Spawn} in seconds.
	 */
	public int getRespawnDelay()
	{
		return _respawnDelay;
	}
	
	/**
	 * Set the respawn random delay of this {@link L2Spawn}. It can't be inferior to respawn delay.
	 * @param random : Random respawn delay in seconds.
	 */
	public void setRespawnRandom(int random)
	{
		_respawnRandom = Math.min(_respawnDelay, random);
	}
	
	/**
	 * @return the respawn delay of this {@link L2Spawn} in seconds.
	 */
	public int getRespawnRandom()
	{
		return _respawnRandom;
	}
	
	/**
	 * Calculate the new respawn time, based on respawn delay +- random respawn delay.
	 * @return the respawn time of this {@link L2Spawn}, in seconds.
	 */
	public int calculateRespawnTime()
	{
		int respawnTime = _respawnDelay;
		
		if (_respawnRandom > 0)
			respawnTime += Rnd.get(-_respawnRandom, _respawnRandom);
		
		return respawnTime;
	}
	
	/**
	 * Enables or disable respawn state of this {@link L2Spawn}.
	 * @param state : if true, we allow the respawn.
	 */
	public void setRespawnState(boolean state)
	{
		_respawnEnabled = state;
	}
	
	/**
	 * @return the minimum RaidBoss spawn delay.
	 */
	public int getRespawnMinDelay()
	{
		return _respawnMinDelay;
	}
	
	/**
	 * Set the minimum respawn delay.
	 * @param date
	 */
	public void setRespawnMinDelay(int date)
	{
		_respawnMinDelay = date;
	}
	
	/**
	 * @return the maximum RaidBoss spawn delay.
	 */
	public int getRespawnMaxDelay()
	{
		return _respawnMaxDelay;
	}
	
	/**
	 * Set Maximum respawn delay.
	 * @param date
	 */
	public void setRespawnMaxDelay(int date)
	{
		_respawnMaxDelay = date;
	}
	
	/**
	 * Create the {@link Npc}, add it to the world and launch its onSpawn() action.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Npc can be spawned to already defined {@link SpawnLocation}. If not defined, Npc is not spawned.<BR>
	 * <BR>
	 * <B><U> Actions sequence for each spawn</U> : </B><BR>
	 * <ul>
	 * <li>Get Npc initialize parameters and generate its object ID</li>
	 * <li>Call the constructor of the Npc</li>
	 * <li>Link the Npc to this {@link L2Spawn}</li>
	 * <li>Make SpawnLocation check, when exists spawn process continues</li>
	 * <li>Reset Npc parameters - for re-spawning of existing Npc</li>
	 * <li>Calculate position using SpawnLocation and geodata</li>
	 * <li>Set the HP and MP of the Npc to the max</li>
	 * <li>Set the position and heading of the Npc (random heading is calculated, if not defined : value -1)</li>
	 * <li>Spawn Npc to the world</li>
	 * </ul>
	 * @param isSummonSpawn When true, summon magic circle will appear.
	 * @return the newly created instance.
	 */
	public Npc doSpawn(boolean isSummonSpawn)
	{
		try
		{
			// Check if the template isn't a Pet.
			if (_template.isType("Pet"))
				return null;
			
			// Get parameters and generate an id.
			Object[] parameters =
			{
				IdFactory.getInstance().getNextId(),
				_template
			};
			
			// Call the constructor.
			Object tmp = _constructor.newInstance(parameters);
			
			if (isSummonSpawn && tmp instanceof Creature)
				((Creature) tmp).setShowSummonAnimation(isSummonSpawn);
			
			// Check if the instance is a Npc.
			if (!(tmp instanceof Npc))
				return null;
			
			// Create final instance.
			_npc = (Npc) tmp;
			
			// Assign L2Spawn to Npc instance.
			_npc.setSpawn(this);
			
			// Initialize Npc and spawn it.
			initializeAndSpawn();
			
			return _npc;
		}
		catch (Exception e)
		{
			_log.warning("L2Spawn: Error during spawn, NPC id=" + _template.getNpcId());
			return null;
		}
	}
	
	/**
	 * Create a respawn task to be launched after the fixed + random delay. Respawn is only possible when respawn enabled.
	 */
	public void doRespawn()
	{
		// Check if respawn is possible to prevent multiple respawning caused by lag
		if (_respawnEnabled)
		{
			// Calculate the random time, if any.
			final int respawnTime = calculateRespawnTime() * 1000;
			
			// Schedule respawn of the NPC
			ThreadPool.schedule(this, respawnTime);
		}
	}
	
	/**
	 * Respawns the {@link Npc}.<br>
	 * <br>
	 * Refresh its id, and run {@link #initializeAndSpawn()}.
	 */
	@Override
	public void run()
	{
		if (_respawnEnabled)
		{
			_npc.refreshID();
			
			initializeAndSpawn();
		}
	}
	
	/**
	 * Initializes the {@link Npc} based on data of this {@link L2Spawn} and spawn it into the world.
	 */
	private void initializeAndSpawn()
	{
		// If location does not exist, there's a problem.
		if (_loc == null)
		{
			_log.warning("L2Spawn : the following npcID: " + _template.getNpcId() + " misses location informations.");
			return;
		}
		
		// reset effects and status
		_npc.stopAllEffects();
		_npc.setIsDead(false);
		
		// reset decay info
		_npc.setDecayed(false);
		
		// reset script value
		_npc.setScriptValue(0);
		
		// The Npc is spawned at the exact position (Lox, Locy, Locz)
		int locx = _loc.getX();
		int locy = _loc.getY();
		int locz = GeoEngine.getInstance().getHeight(locx, locy, _loc.getZ());
		
		// FIXME temporarily fix: when the spawn Z and geo Z differs more than 200, use spawn Z coord
		if (Math.abs(locz - _loc.getZ()) > 200)
			locz = _loc.getZ();
		
		// Set the HP and MP of the Npc to the max
		_npc.setCurrentHpMp(_npc.getMaxHp(), _npc.getMaxMp());
		
		// when champion mod is enabled, try to make NPC a champion
		if (Config.CHAMPION_FREQUENCY > 0)
		{
			// It can't be a Raid, a Raid minion nor a minion. Quest mobs and chests are disabled too.
			if (_npc instanceof Monster && !getTemplate().cantBeChampion() && _npc.getLevel() >= Config.CHAMP_MIN_LVL && _npc.getLevel() <= Config.CHAMP_MAX_LVL && !_npc.isRaidRelated() && !_npc.isMinion())
				((Attackable) _npc).setChampion(Rnd.get(100) < Config.CHAMPION_FREQUENCY);
		}
		
		// spawn NPC on new coordinates
		_npc.spawnMe(locx, locy, locz, (_loc.getHeading() < 0) ? Rnd.get(65536) : _loc.getHeading());
	}
	
	@Override
	public String toString()
	{
		return "L2Spawn [id=" + _template.getNpcId() + ", loc=" + _loc.toString() + "]";
	}
}
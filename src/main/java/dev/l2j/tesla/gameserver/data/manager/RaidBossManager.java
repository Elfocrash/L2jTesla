package dev.l2j.tesla.gameserver.data.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.enums.BossStatus;
import dev.l2j.tesla.gameserver.model.actor.instance.RaidBoss;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.spawn.BossSpawn;
import dev.l2j.tesla.gameserver.model.spawn.L2Spawn;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.gameserver.data.sql.SpawnTable;
import dev.l2j.tesla.gameserver.data.xml.NpcData;

/**
 * Loads and store {@link RaidBoss}es informations, using {@link BossSpawn} holder.
 */
public class RaidBossManager
{
	protected static final CLogger LOGGER = new CLogger(RaidBossManager.class.getName());
	
	private static final String LOAD_RAIDBOSSES = "SELECT * from raidboss_spawnlist ORDER BY boss_id";
	private static final String INSERT_RAIDBOSS = "INSERT INTO raidboss_spawnlist (boss_id,loc_x,loc_y,loc_z,heading,respawn_time,currentHp,currentMp) values(?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE respawn_time=VALUES(respawn_time),currentHp=VALUES(currentHp),currentMp=VALUES(currentMp)";
	private static final String SAVE_RAIDBOSS = "UPDATE raidboss_spawnlist SET currentHP = ?, currentMP = ? WHERE boss_id = ?";
	
	protected final Map<Integer, BossSpawn> _spawns = new HashMap<>();
	
	public RaidBossManager()
	{
		load();
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(LOAD_RAIDBOSSES);
             ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final NpcTemplate template = NpcData.getInstance().getTemplate(rs.getInt("boss_id"));
				if (template == null || !template.isType("RaidBoss"))
				{
					LOGGER.warn("Couldn't load raidboss #{}.", rs.getInt("boss_id"));
					continue;
				}
				
				// Generate a L2Spawn.
				final L2Spawn spawn = new L2Spawn(template);
				spawn.setLoc(rs.getInt("loc_x"), rs.getInt("loc_y"), rs.getInt("loc_z"), rs.getInt("heading"));
				spawn.setRespawnMinDelay(rs.getInt("spawn_time"));
				spawn.setRespawnMaxDelay(rs.getInt("random_time"));
				
				addNewSpawn(spawn, rs.getLong("respawn_time"), rs.getDouble("currentHP"), rs.getDouble("currentMP"), false);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Error restoring raid bosses.", e);
		}
		LOGGER.info("Loaded {} raid bosses.", _spawns.size());
	}
	
	public void reload()
	{
		// Cancel running tasks and cleanup _spawns Map.
		cleanUp(false);
		
		// Load data.
		load();
	}
	
	/**
	 * @param id : The id used as reference.
	 * @return an existing {@link BossSpawn} based on a id, or null otherwise.
	 */
	public BossSpawn getBossSpawn(int id)
	{
		return _spawns.get(id);
	}
	
	/**
	 * @param id : The id used as reference.
	 * @return the {@link BossStatus} of an existing {@link BossSpawn}, based on a id.
	 */
	public BossStatus getStatus(int id)
	{
		final BossSpawn bs = _spawns.get(id);
		return (bs == null) ? BossStatus.UNDEFINED : bs.getStatus();
	}
	
	/**
	 * Retrieve an existing {@link BossSpawn} based on a {@link RaidBoss} id, and activate it's {@link BossSpawn#onDeath()} method.
	 * @param boss : The RaidBoss instance used as reference.
	 */
	public void onDeath(RaidBoss boss)
	{
		final BossSpawn bs = _spawns.get(boss.getNpcId());
		if (bs != null)
			bs.onDeath();
	}
	
	/**
	 * Add a new {@link BossSpawn}, based on a {@link L2Spawn}. It is both used on server startup and admincommand.<br>
	 * <br>
	 * Database is either refreshed using forceSave boolean flag, or if a difference of status has been detected.
	 * @param spawn : The spawn used as reference.
	 * @param respawnTime : The respawn time to set.
	 * @param currentHP : The HP of the instance to set.
	 * @param currentMP : The MP of the instance to set.
	 * @param forceSave : If true, we force the insertion of this spawn into the database.
	 */
	public void addNewSpawn(L2Spawn spawn, long respawnTime, double currentHP, double currentMP, boolean forceSave)
	{
		if (spawn == null)
			return;
		
		final int id = spawn.getNpcId();
		if (_spawns.containsKey(id))
			return;
		
		final long time = System.currentTimeMillis();
		
		// Add the spawn.
		SpawnTable.getInstance().addSpawn(spawn, false);
		
		// We generate the StatsSet.
		final BossSpawn bs = new BossSpawn();
		bs.setSpawn(spawn);
		
		// Boss is alive, spawn him.
		if (respawnTime == 0L || time > respawnTime)
		{
			final RaidBoss raidboss = (RaidBoss) spawn.doSpawn(false);
			
			currentHP = (currentHP == 0) ? raidboss.getMaxHp() : currentHP;
			currentMP = (currentMP == 0) ? raidboss.getMaxMp() : currentMP;
			
			// Set HP, MP.
			raidboss.setCurrentHpMp(currentHP, currentMP);
			
			bs.setStatus(BossStatus.ALIVE);
			bs.setCurrentHp(currentHP);
			bs.setCurrentMp(currentMP);
			bs.setRespawnTime(0);
			
			// Time passed by, or we force the database save ; save data on database.
			if (time > respawnTime || forceSave)
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement ps = con.prepareStatement(INSERT_RAIDBOSS))
				{
					ps.setInt(1, spawn.getNpcId());
					ps.setInt(2, spawn.getLocX());
					ps.setInt(3, spawn.getLocY());
					ps.setInt(4, spawn.getLocZ());
					ps.setInt(5, spawn.getHeading());
					ps.setLong(6, respawnTime);
					ps.setDouble(7, currentHP);
					ps.setDouble(8, currentMP);
					ps.executeUpdate();
				}
				catch (Exception e)
				{
					LOGGER.error("Couldn't store raid boss #{}.", e, id);
				}
			}
		}
		// Boss isn't alive, we generate a scheduled task using its respawn time.
		else
		{
			long spawnTime = respawnTime - time;
			bs.setTask(ThreadPool.schedule(() -> bs.onSpawn(), spawnTime));
			
			bs.setStatus(BossStatus.DEAD);
			bs.setCurrentHp(0);
			bs.setCurrentMp(0);
			bs.setRespawnTime(respawnTime);
		}
		
		// Add the BossSpawn.
		_spawns.put(id, bs);
	}
	
	/**
	 * Delete an existing {@link BossSpawn} based on a {@link L2Spawn}. Drop it from {@link SpawnTable}.
	 * @param spawn : The spawn used as reference.
	 */
	public void deleteSpawn(L2Spawn spawn)
	{
		if (spawn == null)
			return;
		
		final int id = spawn.getNpcId();
		
		// Delete the entry. If we couldn't find it, return directly.
		final BossSpawn bs = _spawns.remove(id);
		if (bs == null)
			return;
		
		// Make actions related to despawn.
		bs.onDespawn();
		
		// Remove the spawn.
		SpawnTable.getInstance().deleteSpawn(spawn, false);
	}
	
	/**
	 * Cleanup data. Cancel all running tasks, save hp/mp and location if boolean flag is true, delete existing {@link BossSpawn} entries.
	 * @param saveOnDb : If true, we also save informations on database.
	 */
	public void cleanUp(boolean saveOnDb)
	{
		// Cancel all running tasks.
		for (BossSpawn bs : _spawns.values())
			bs.cancelTask();
		
		// Save HP/MP and locations if boolean flag is set to true.
		if (saveOnDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(SAVE_RAIDBOSS))
			{
				for (Entry<Integer, BossSpawn> entry : _spawns.entrySet())
				{
					final BossSpawn bs = entry.getValue();
					
					// We only bother with living bosses.
					if (bs.getStatus() == BossStatus.ALIVE)
					{
						ps.setDouble(2, bs.getBoss().getCurrentHp());
						ps.setDouble(3, bs.getBoss().getCurrentMp());
						ps.setInt(4, entry.getKey());
						ps.addBatch();
					}
				}
				ps.executeBatch();
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't save raid bosses.", e);
			}
		}
		
		// Delete spawns entries.
		_spawns.clear();
	}
	
	public static RaidBossManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RaidBossManager INSTANCE = new RaidBossManager();
	}
}
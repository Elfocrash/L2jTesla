package dev.l2j.tesla.gameserver.model.actor.npc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import dev.l2j.tesla.gameserver.idfactory.IdFactory;
import dev.l2j.tesla.gameserver.model.MinionData;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.xml.NpcData;

public class MinionList
{
	private final Map<Monster, Boolean> _minions = new ConcurrentHashMap<>();
	private final Monster _master;
	
	public MinionList(Monster master)
	{
		_master = master;
	}
	
	/**
	 * @return a set of the spawned (alive) minions.
	 */
	public List<Monster> getSpawnedMinions()
	{
		return _minions.entrySet().stream().filter(m -> m.getValue() == true).map(Map.Entry::getKey).collect(Collectors.toList());
	}
	
	/**
	 * @return a complete view of minions.
	 */
	public Map<Monster, Boolean> getMinions()
	{
		return _minions;
	}
	
	/**
	 * Manage the spawn of Minions.
	 * <ul>
	 * <li>Get the Minion data of all Minions that must be spawn</li>
	 * <li>For each Minion type, spawn the amount of Minion needed</li>
	 * </ul>
	 */
	public final void spawnMinions()
	{
		// We generate new instances. We can't reuse existing instances, since previous monsters can still exist.
		for (MinionData data : _master.getTemplate().getMinionData())
		{
			// Get the template of the Minion to spawn
			final NpcTemplate template = NpcData.getInstance().getTemplate(data.getMinionId());
			if (template == null)
				continue;
			
			for (int i = 0; i < data.getAmount(); i++)
			{
				final Monster minion = new Monster(IdFactory.getInstance().getNextId(), template);
				minion.setMaster(_master);
				minion.setMinion(_master.isRaidBoss());
				
				initializeNpcInstance(_master, minion);
			}
		}
	}
	
	/**
	 * Called on the master death.
	 * <ul>
	 * <li>In case of regular master monster : minions references are deleted, but monsters instances are kept alive (until they gonna be deleted if their interaction move to IDLE, or killed).</li>
	 * <li>In case of raid bosses : all minions are instantly deleted.</li>
	 * </ul>
	 */
	public void onMasterDie()
	{
		if (_master.isRaidBoss())
		{
			// For all minions, delete them.
			for (Monster minion : getSpawnedMinions())
				minion.deleteMe();
		}
		else
		{
			// For all minions, remove leader reference.
			for (Monster minion : _minions.keySet())
				minion.setMaster(null);
		}
		
		// Cleanup the entire MinionList.
		_minions.clear();
	}
	
	/**
	 * Called on master deletion.
	 */
	public void onMasterDeletion()
	{
		// For all minions, delete them and remove leader reference.
		for (Monster minion : _minions.keySet())
		{
			minion.setMaster(null);
			minion.deleteMe();
		}
		
		// Cleanup the entire MinionList.
		_minions.clear();
	}
	
	/**
	 * Called on minion deletion, or on master death.
	 * @param minion : The minion to make checks on.
	 */
	public void onMinionDeletion(Monster minion)
	{
		// Keep it to avoid OOME.
		minion.setMaster(null);
		
		// Cleanup the map form this reference.
		_minions.remove(minion);
	}
	
	/**
	 * Called on minion death. Flag the {@link Monster} from the list of the spawned minions as unspawned.
	 * @param minion : The minion to make checks on.
	 * @param respawnTime : Respawn the Monster using this timer, but only if master is alive.
	 */
	public void onMinionDie(Monster minion, int respawnTime)
	{
		_minions.put(minion, false);
		
		if (minion.isRaidRelated() && respawnTime > 0 && !_master.isAlikeDead())
		{
			ThreadPool.schedule(() ->
			{
				// Master is visible, but minion isn't spawned back (via teleport, for example).
				if (!_master.isAlikeDead() && _master.isVisible() && !_minions.get(minion))
				{
					minion.refreshID();
					initializeNpcInstance(_master, minion);
				}
			}, respawnTime);
		}
	}
	
	/**
	 * Called if master/minion was attacked. Master and all free minions receive aggro against attacker.
	 * @param caller : That instance will call for help versus attacker.
	 * @param attacker : That instance will receive all aggro.
	 */
	public void onAssist(Creature caller, Creature attacker)
	{
		if (attacker == null)
			return;
		
		// The master is aggroed.
		if (!_master.isAlikeDead() && !_master.isInCombat())
			_master.addDamageHate(attacker, 0, 1);
		
		final boolean callerIsMaster = (caller == _master);
		
		// Define the aggro value of minions.
		int aggro = (callerIsMaster ? 10 : 1);
		if (_master.isRaidBoss())
			aggro *= 10;
		
		for (Monster minion : getSpawnedMinions())
		{
			if (!minion.isDead() && (callerIsMaster || !minion.isInCombat()))
				minion.addDamageHate(attacker, 0, aggro);
		}
	}
	
	/**
	 * Teleport all minions back to master position.
	 */
	public void onMasterTeleported()
	{
		for (Monster minion : getSpawnedMinions())
		{
			if (minion.isDead() || minion.isMovementDisabled())
				continue;
			
			minion.teleToMaster();
		}
	}
	
	protected final Monster initializeNpcInstance(Monster master, Monster minion)
	{
		_minions.put(minion, true);
		
		minion.setIsNoRndWalk(true);
		minion.stopAllEffects();
		minion.setIsDead(false);
		minion.setDecayed(false);
		
		// Set the Minion HP, MP and Heading
		minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());
		
		// Init the position of the Minion and add it in the world as a visible object
		final int offset = (int) (100 + minion.getCollisionRadius() + master.getCollisionRadius());
		final int minRadius = (int) (master.getCollisionRadius() + 30);
		
		int newX = Rnd.get(minRadius * 2, offset * 2); // x
		int newY = Rnd.get(newX, offset * 2); // distance
		newY = (int) Math.sqrt(newY * newY - newX * newX); // y
		if (newX > offset + minRadius)
			newX = master.getX() + newX - offset;
		else
			newX = master.getX() - newX + minRadius;
		if (newY > offset + minRadius)
			newY = master.getY() + newY - offset;
		else
			newY = master.getY() - newY + minRadius;
		
		minion.spawnMe(newX, newY, master.getZ(), master.getHeading());
		
		return minion;
	}
}
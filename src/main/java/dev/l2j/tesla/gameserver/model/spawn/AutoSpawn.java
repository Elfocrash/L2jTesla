package dev.l2j.tesla.gameserver.model.spawn;

import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.model.location.SpawnLocation;
import dev.l2j.tesla.gameserver.model.actor.Npc;

/**
 * Stores information about a registered auto spawn.
 */
public class AutoSpawn
{
	private final List<SpawnLocation> _locList = new ArrayList<>();
	private final List<Npc> _npcList = new ArrayList<>();
	
	protected int _objectId;
	protected int _npcId;
	protected int _initDelay;
	protected int _resDelay;
	protected int _desDelay;
	protected int _spawnCount = 1;
	protected int _lastLocIndex = -1;
	
	private boolean _spawnActive;
	private boolean _randomSpawn;
	private boolean _broadcastAnnouncement;
	
	public AutoSpawn(int npcId, int initDelay, int respawnDelay, int despawnDelay)
	{
		_npcId = npcId;
		_initDelay = initDelay;
		_resDelay = respawnDelay;
		_desDelay = despawnDelay;
	}
	
	public void setSpawnActive(boolean activeValue)
	{
		_spawnActive = activeValue;
	}
	
	public boolean addNpcInstance(Npc npcInst)
	{
		return _npcList.add(npcInst);
	}
	
	public boolean removeNpcInstance(Npc npcInst)
	{
		return _npcList.remove(npcInst);
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}
	
	public int getInitialDelay()
	{
		return _initDelay;
	}
	
	public int getRespawnDelay()
	{
		return _resDelay;
	}
	
	public int getDespawnDelay()
	{
		return _desDelay;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public int getSpawnCount()
	{
		return _spawnCount;
	}
	
	public int getLastLocIndex()
	{
		return _lastLocIndex;
	}
	
	public void setLastLocIndex(int index)
	{
		_lastLocIndex = index;
	}
	
	public SpawnLocation[] getLocationList()
	{
		return _locList.toArray(new SpawnLocation[_locList.size()]);
	}
	
	public Npc[] getNPCInstanceList()
	{
		Npc[] ret;
		synchronized (_npcList)
		{
			ret = new Npc[_npcList.size()];
			_npcList.toArray(ret);
		}
		
		return ret;
	}
	
	public L2Spawn[] getSpawns()
	{
		List<L2Spawn> npcSpawns = new ArrayList<>();
		
		for (Npc npcInst : _npcList)
			npcSpawns.add(npcInst.getSpawn());
		
		return npcSpawns.toArray(new L2Spawn[npcSpawns.size()]);
	}
	
	public void setSpawnCount(int spawnCount)
	{
		_spawnCount = spawnCount;
	}
	
	public void setRandomSpawn(boolean randValue)
	{
		_randomSpawn = randValue;
	}
	
	public void setBroadcast(boolean broadcastValue)
	{
		_broadcastAnnouncement = broadcastValue;
	}
	
	public boolean isSpawnActive()
	{
		return _spawnActive;
	}
	
	public boolean isRandomSpawn()
	{
		return _randomSpawn;
	}
	
	public boolean isBroadcasting()
	{
		return _broadcastAnnouncement;
	}
	
	public boolean addSpawnLocation(int x, int y, int z, int heading)
	{
		return _locList.add(new SpawnLocation(x, y, z, heading));
	}
	
	public boolean addSpawnLocation(int[] spawnLoc)
	{
		if (spawnLoc.length != 3)
			return false;
		
		return addSpawnLocation(spawnLoc[0], spawnLoc[1], spawnLoc[2], -1);
	}
}
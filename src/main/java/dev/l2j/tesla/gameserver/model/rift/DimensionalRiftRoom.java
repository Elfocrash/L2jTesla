package dev.l2j.tesla.gameserver.model.rift;

import java.awt.Polygon;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.model.spawn.L2Spawn;
import dev.l2j.tesla.commons.random.Rnd;
import dev.l2j.tesla.commons.util.StatsSet;

/**
 * One cell of Dimensional Rift system.<br>
 * <br>
 * Each DimensionalRiftRoom holds specific {@link L2Spawn}s, a {@link Shape}, and a teleport {@link Location}.
 */
public class DimensionalRiftRoom
{
	public static final int Z_VALUE = -6752;
	
	private final List<L2Spawn> _spawns = new ArrayList<>();
	
	private final byte _type;
	private final byte _id;
	
	private final int _xMin;
	private final int _xMax;
	private final int _yMin;
	private final int _yMax;
	
	private final Location _teleportLoc;
	
	private final Shape _shape;
	
	private final boolean _isBossRoom;
	
	private boolean _partyInside;
	
	public DimensionalRiftRoom(byte type, StatsSet set)
	{
		final int xMin = set.getInteger("xMin");
		final int xMax = set.getInteger("xMax");
		final int yMin = set.getInteger("yMin");
		final int yMax = set.getInteger("yMax");
		
		_type = type;
		_id = set.getByte("id");
		_xMin = (xMin + 128);
		_xMax = (xMax - 128);
		_yMin = (yMin + 128);
		_yMax = (yMax - 128);
		
		_teleportLoc = new Location(set.getInteger("xT"), set.getInteger("yT"), Z_VALUE);
		
		_isBossRoom = (_id == 9);
		
		_shape = new Polygon(new int[]
		{
			xMin,
			xMax,
			xMax,
			xMin
		}, new int[]
		{
			yMin,
			yMin,
			yMax,
			yMax
		}, 4);
	}
	
	@Override
	public String toString()
	{
		return "RiftRoom #" + _type + "_" + _id + ", full: " + _partyInside + ", tel: " + _teleportLoc.toString() + ", spawns: " + _spawns.size();
		
	}
	
	public byte getType()
	{
		return _type;
	}
	
	public byte getId()
	{
		return _id;
	}
	
	public int getRandomX()
	{
		return Rnd.get(_xMin, _xMax);
	}
	
	public int getRandomY()
	{
		return Rnd.get(_yMin, _yMax);
	}
	
	public Location getTeleportLoc()
	{
		return _teleportLoc;
	}
	
	public boolean checkIfInZone(int x, int y, int z)
	{
		return _shape.contains(x, y) && z >= -6816 && z <= -6240;
	}
	
	public boolean isBossRoom()
	{
		return _isBossRoom;
	}
	
	public List<L2Spawn> getSpawns()
	{
		return _spawns;
	}
	
	public boolean isPartyInside()
	{
		return _partyInside;
	}
	
	public void setPartyInside(boolean partyInside)
	{
		_partyInside = partyInside;
	}
	
	public void spawn()
	{
		for (L2Spawn spawn : _spawns)
		{
			spawn.doSpawn(false);
			spawn.setRespawnState(true);
		}
	}
	
	public void unspawn()
	{
		for (L2Spawn spawn : _spawns)
		{
			spawn.setRespawnState(false);
			if (spawn.getNpc() != null)
				spawn.getNpc().deleteMe();
		}
		_partyInside = false;
	}
}
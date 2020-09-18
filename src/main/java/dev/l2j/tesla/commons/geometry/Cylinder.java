package dev.l2j.tesla.commons.geometry;

import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.commons.random.Rnd;

/**
 * @author Hasha
 */
public class Cylinder extends Circle
{
	// min and max Z coorinates
	private final int _minZ;
	private final int _maxZ;
	
	/**
	 * Cylinder constructor
	 * @param x : Center X coordinate.
	 * @param y : Center X coordinate.
	 * @param r : Cylinder radius.
	 * @param minZ : Minimum Z coordinate.
	 * @param maxZ : Maximum Z coordinate.
	 */
	public Cylinder(int x, int y, int r, int minZ, int maxZ)
	{
		super(x, y, r);
		
		_minZ = minZ;
		_maxZ = maxZ;
	}
	
	@Override
	public final double getArea()
	{
		return 2 * Math.PI * _r * (_r + _maxZ - _minZ);
	}
	
	@Override
	public final double getVolume()
	{
		return Math.PI * _r * _r * (_maxZ - _minZ);
	}
	
	@Override
	public final boolean isInside(int x, int y, int z)
	{
		if (z < _minZ || z > _maxZ)
			return false;
		
		final int dx = x - _x;
		final int dy = y - _y;
		
		return (dx * dx + dy * dy) <= _r * _r;
	}
	
	@Override
	public final Location getRandomLocation()
	{
		// get uniform distance and angle
		final double distance = Math.sqrt(Rnd.nextDouble()) * _r;
		final double angle = Rnd.nextDouble() * Math.PI * 2;
		
		// calculate coordinates and return
		return new Location((int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)), Rnd.get(_minZ, _maxZ));
	}
}
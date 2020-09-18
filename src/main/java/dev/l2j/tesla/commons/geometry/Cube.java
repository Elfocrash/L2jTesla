package dev.l2j.tesla.commons.geometry;

import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.commons.random.Rnd;

/**
 * @author Hasha
 */
public class Cube extends Square
{
	// cube origin coordinates
	private final int _z;
	
	/**
	 * Cube constructor.
	 * @param x : Bottom left lower X coordinate.
	 * @param y : Bottom left lower Y coordinate.
	 * @param z : Bottom left lower Z coordinate.
	 * @param a : Size of cube side.
	 */
	public Cube(int x, int y, int z, int a)
	{
		super(x, y, a);
		
		_z = z;
	}
	
	@Override
	public double getArea()
	{
		return 6 * _a * _a;
	}
	
	@Override
	public double getVolume()
	{
		return _a * _a * _a;
	}
	
	@Override
	public boolean isInside(int x, int y, int z)
	{
		int d = z - _z;
		if (d < 0 || d > _a)
			return false;
		
		d = x - _x;
		if (d < 0 || d > _a)
			return false;
		
		d = y - _y;
		if (d < 0 || d > _a)
			return false;
		
		return true;
	}
	
	@Override
	public Location getRandomLocation()
	{
		// calculate coordinates and return
		return new Location(_x + Rnd.get(_a), _y + Rnd.get(_a), _z + Rnd.get(_a));
	}
}
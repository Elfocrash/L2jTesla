package dev.l2j.tesla.commons.geometry;

import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.commons.random.Rnd;

/**
 * @author Hasha
 */
public class Square extends AShape
{
	// square origin coordinates
	protected final int _x;
	protected final int _y;
	
	// square side
	protected final int _a;
	
	/**
	 * Square constructor.
	 * @param x : Bottom left X coordinate.
	 * @param y : Bottom left Y coordinate.
	 * @param a : Size of square side.
	 */
	public Square(int x, int y, int a)
	{
		_x = x;
		_y = y;
		
		_a = a;
	}
	
	@Override
	public final int getSize()
	{
		return _a * _a;
	}
	
	@Override
	public double getArea()
	{
		return _a * _a;
	}
	
	@Override
	public double getVolume()
	{
		return 0;
	}
	
	@Override
	public boolean isInside(int x, int y)
	{
		int d = x - _x;
		if (d < 0 || d > _a)
			return false;
		
		d = y - _y;
		if (d < 0 || d > _a)
			return false;
		
		return true;
	}
	
	@Override
	public boolean isInside(int x, int y, int z)
	{
		int d = x - _x;
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
		return new Location(_x + Rnd.get(_a), _y + Rnd.get(_a), 0);
	}
}
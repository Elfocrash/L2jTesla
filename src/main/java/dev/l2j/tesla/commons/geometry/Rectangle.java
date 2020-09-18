package dev.l2j.tesla.commons.geometry;

import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.commons.random.Rnd;

/**
 * @author Hasha
 */
public class Rectangle extends AShape
{
	// rectangle origin coordinates
	protected final int _x;
	protected final int _y;
	
	// rectangle width and height
	protected final int _w;
	protected final int _h;
	
	/**
	 * Rectangle constructor.
	 * @param x : Bottom left X coordinate.
	 * @param y : Bottom left Y coordinate.
	 * @param w : Rectangle width.
	 * @param h : Rectangle height.
	 */
	public Rectangle(int x, int y, int w, int h)
	{
		_x = x;
		_y = y;
		
		_w = w;
		_h = h;
	}
	
	@Override
	public final int getSize()
	{
		return _w * _h;
	}
	
	@Override
	public double getArea()
	{
		return _w * _h;
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
		if (d < 0 || d > _w)
			return false;
		
		d = y - _y;
		if (d < 0 || d > _h)
			return false;
		
		return true;
	}
	
	@Override
	public boolean isInside(int x, int y, int z)
	{
		int d = x - _x;
		if (d < 0 || d > _w)
			return false;
		
		d = y - _y;
		if (d < 0 || d > _h)
			return false;
		
		return true;
	}
	
	@Override
	public Location getRandomLocation()
	{
		// calculate coordinates and return
		return new Location(_x + Rnd.get(_w), _y + Rnd.get(_h), 0);
	}
}
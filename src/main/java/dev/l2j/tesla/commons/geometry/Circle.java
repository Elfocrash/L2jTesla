package dev.l2j.tesla.commons.geometry;

import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.commons.random.Rnd;

/**
 * @author Hasha
 */
public class Circle extends AShape
{
	// circle center coordinates
	protected final int _x;
	protected final int _y;
	
	// circle radius
	protected final int _r;
	
	/**
	 * Circle constructor
	 * @param x : Center X coordinate.
	 * @param y : Center Y coordinate.
	 * @param r : Circle radius.
	 */
	public Circle(int x, int y, int r)
	{
		_x = x;
		_y = y;
		
		_r = r;
	}
	
	@Override
	public final int getSize()
	{
		return (int) Math.PI * _r * _r;
	}
	
	@Override
	public double getArea()
	{
		return (int) Math.PI * _r * _r;
	}
	
	@Override
	public double getVolume()
	{
		return 0;
	}
	
	@Override
	public final boolean isInside(int x, int y)
	{
		final int dx = x - _x;
		final int dy = y - _y;
		
		return (dx * dx + dy * dy) <= _r * _r;
	}
	
	@Override
	public boolean isInside(int x, int y, int z)
	{
		final int dx = x - _x;
		final int dy = y - _y;
		
		return (dx * dx + dy * dy) <= _r * _r;
	}
	
	@Override
	public Location getRandomLocation()
	{
		// get uniform distance and angle
		final double distance = Math.sqrt(Rnd.nextDouble()) * _r;
		final double angle = Rnd.nextDouble() * Math.PI * 2;
		
		// calculate coordinates and return
		return new Location((int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)), 0);
	}
}
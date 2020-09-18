package dev.l2j.tesla.gameserver.model.zone.form;

import dev.l2j.tesla.gameserver.model.zone.ZoneForm;

public class ZoneCylinder extends ZoneForm
{
	private final int _x, _y, _z1, _z2, _rad, _radS;
	
	public ZoneCylinder(int x, int y, int z1, int z2, int rad)
	{
		_x = x;
		_y = y;
		_z1 = z1;
		_z2 = z2;
		_rad = rad;
		_radS = rad * rad;
	}
	
	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		if ((Math.pow(_x - x, 2) + Math.pow(_y - y, 2)) > _radS || z < _z1 || z > _z2)
			return false;
		
		return true;
	}
	
	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		// Circles point inside the rectangle?
		if (_x > ax1 && _x < ax2 && _y > ay1 && _y < ay2)
			return true;
		
		// Any point of the rectangle intersecting the Circle?
		if ((Math.pow(ax1 - _x, 2) + Math.pow(ay1 - _y, 2)) < _radS)
			return true;
		
		if ((Math.pow(ax1 - _x, 2) + Math.pow(ay2 - _y, 2)) < _radS)
			return true;
		
		if ((Math.pow(ax2 - _x, 2) + Math.pow(ay1 - _y, 2)) < _radS)
			return true;
		
		if ((Math.pow(ax2 - _x, 2) + Math.pow(ay2 - _y, 2)) < _radS)
			return true;
		
		// Collision on any side of the rectangle?
		if (_x > ax1 && _x < ax2)
		{
			if (Math.abs(_y - ay2) < _rad)
				return true;
			
			if (Math.abs(_y - ay1) < _rad)
				return true;
		}
		
		if (_y > ay1 && _y < ay2)
		{
			if (Math.abs(_x - ax2) < _rad)
				return true;
			
			if (Math.abs(_x - ax1) < _rad)
				return true;
		}
		
		return false;
	}
	
	@Override
	public double getDistanceToZone(int x, int y)
	{
		return (Math.sqrt((Math.pow(_x - x, 2) + Math.pow(_y - y, 2))) - _rad);
	}
	
	@Override
	public int getLowZ()
	{
		return _z1;
	}
	
	@Override
	public int getHighZ()
	{
		return _z2;
	}
	
	@Override
	public void visualizeZone(int id, int z)
	{
		int count = (int) (2 * Math.PI * _rad / STEP);
		double angle = 2 * Math.PI / count;
		
		for (int i = 0; i < count; i++)
		{
			int x = (int) (Math.cos(angle * i) * _rad);
			int y = (int) (Math.sin(angle * i) * _rad);
			
			dropDebugItem(id, _x + x, _y + y, z);
		}
	}
}
package dev.l2j.tesla.gameserver.geoengine.geodata;

import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.model.location.Location;

public class GeoLocation extends Location
{
	private byte _nswe;
	
	public GeoLocation(int x, int y, int z)
	{
		super(x, y, GeoEngine.getInstance().getHeightNearest(x, y, z));
		_nswe = GeoEngine.getInstance().getNsweNearest(x, y, z);
	}
	
	public void set(int x, int y, short z)
	{
		super.set(x, y, GeoEngine.getInstance().getHeightNearest(x, y, z));
		_nswe = GeoEngine.getInstance().getNsweNearest(x, y, z);
	}
	
	public int getGeoX()
	{
		return _x;
	}
	
	public int getGeoY()
	{
		return _y;
	}
	
	@Override
	public int getX()
	{
		return GeoEngine.getWorldX(_x);
	}
	
	@Override
	public int getY()
	{
		return GeoEngine.getWorldY(_y);
	}
	
	public byte getNSWE()
	{
		return _nswe;
	}
}
package dev.l2j.tesla.gameserver.model.location;

/**
 * A datatype extending {@link Location}, wildly used as character position, since it also stores heading of the character.
 */
public class SpawnLocation extends Location
{
	public static final SpawnLocation DUMMY_SPAWNLOC = new SpawnLocation(0, 0, 0, 0);
	
	protected volatile int _heading;
	
	public SpawnLocation(int x, int y, int z, int heading)
	{
		super(x, y, z);
		
		_heading = heading;
	}
	
	public SpawnLocation(SpawnLocation loc)
	{
		super(loc.getX(), loc.getY(), loc.getZ());
		
		_heading = loc.getHeading();
	}
	
	@Override
	public String toString()
	{
		return _x + ", " + _y + ", " + _z + ", " + _heading;
	}
	
	@Override
	public int hashCode()
	{
		return _x ^ _y ^ _z ^ _heading;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof SpawnLocation)
		{
			SpawnLocation loc = (SpawnLocation) o;
			return (loc.getX() == _x && loc.getY() == _y && loc.getZ() == _z && loc.getHeading() == _heading);
		}
		
		return false;
	}
	
	public int getHeading()
	{
		return _heading;
	}
	
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public void set(int x, int y, int z, int heading)
	{
		super.set(x, y, z);
		
		_heading = heading;
	}
	
	public void set(SpawnLocation loc)
	{
		super.set(loc.getX(), loc.getY(), loc.getZ());
		
		_heading = loc.getHeading();
	}
	
	@Override
	public void clean()
	{
		super.set(0, 0, 0);
		
		_heading = 0;
	}
}
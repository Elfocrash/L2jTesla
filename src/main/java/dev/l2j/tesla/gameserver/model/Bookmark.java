package dev.l2j.tesla.gameserver.model;

/**
 * A datatype used as teleportation point reminder. Used by GM admincommand //bk.
 */
public class Bookmark
{
	private final String _name;
	private final int _objId;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public Bookmark(String name, int objId, int x, int y, int z)
	{
		_name = name;
		_objId = objId;
		_x = x;
		_y = y;
		_z = z;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getId()
	{
		return _objId;
	}
	
	public int getX()
	{
		return _x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public int getZ()
	{
		return _z;
	}
}
package dev.l2j.tesla.gameserver.enums;

public enum TeamType
{
	NONE(0),
	BLUE(1),
	RED(2);
	
	private int _id;
	
	private TeamType(int id)
	{
		_id = id;
	}
	
	public int getId()
	{
		return _id;
	}
}
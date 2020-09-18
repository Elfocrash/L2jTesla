package dev.l2j.tesla.gameserver.enums;

public enum PunishmentType
{
	NONE(""),
	CHAT("chat banned"),
	JAIL("jailed"),
	CHAR("banned"),
	ACC("banned");
	
	private final String _name;
	
	PunishmentType(String name)
	{
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public static final PunishmentType[] VALUES = values();
}
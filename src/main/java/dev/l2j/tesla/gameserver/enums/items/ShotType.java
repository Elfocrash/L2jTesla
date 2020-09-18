package dev.l2j.tesla.gameserver.enums.items;

public enum ShotType
{
	SOULSHOT,
	SPIRITSHOT,
	BLESSED_SPIRITSHOT,
	FISH_SOULSHOT;
	
	private final int _mask;
	
	private ShotType()
	{
		_mask = (1 << ordinal());
	}
	
	public int getMask()
	{
		return _mask;
	}
}
package dev.l2j.tesla.gameserver.enums.items;

public enum WeaponType implements ItemType
{
	NONE(40),
	SWORD(40),
	BLUNT(40),
	DAGGER(40),
	BOW(500),
	POLE(66),
	ETC(40),
	FIST(40),
	DUAL(40),
	DUALFIST(40),
	BIGSWORD(40),
	FISHINGROD(40),
	BIGBLUNT(40),
	PET(40);
	
	private final int _mask;
	private final int _range;
	
	private WeaponType(int range)
	{
		_mask = 1 << ordinal();
		_range = range;
	}
	
	/**
	 * Returns the ID of the item after applying the mask.
	 * @return int : ID of the item
	 */
	@Override
	public int mask()
	{
		return _mask;
	}
	
	public int getRange()
	{
		return _range;
	}
}
package dev.l2j.tesla.gameserver.enums.items;

public enum ArmorType implements ItemType
{
	NONE,
	LIGHT,
	HEAVY,
	MAGIC,
	PET,
	SHIELD;
	
	final int _mask;
	
	private ArmorType()
	{
		_mask = 1 << (ordinal() + WeaponType.values().length);
	}
	
	/**
	 * Returns the ID of the ArmorType after applying a mask.
	 * @return int : ID of the ArmorType after mask
	 */
	@Override
	public int mask()
	{
		return _mask;
	}
}
package dev.l2j.tesla.gameserver.model.soulcrystal;

import dev.l2j.tesla.commons.util.StatsSet;

/**
 * This class stores Soul Crystal leveling infos related to items, notably:
 * <ul>
 * <li>The current level on the hierarchy tree of items ;</li>
 * <li>The initial itemId from where we start ;</li>
 * <li>The succeeded itemId rewarded if absorb was successful ;</li>
 * <li>The broken itemId rewarded if absorb failed.</li>
 * </ul>
 */
public final class SoulCrystal
{
	private final int _level;
	private final int _initialItemId;
	private final int _stagedItemId;
	private final int _brokenItemId;
	
	public SoulCrystal(StatsSet set)
	{
		_level = set.getInteger("level");
		_initialItemId = set.getInteger("initial");
		_stagedItemId = set.getInteger("staged");
		_brokenItemId = set.getInteger("broken");
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getInitialItemId()
	{
		return _initialItemId;
	}
	
	public int getStagedItemId()
	{
		return _stagedItemId;
	}
	
	public int getBrokenItemId()
	{
		return _brokenItemId;
	}
}
package dev.l2j.tesla.gameserver.model.soulcrystal;

import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.enums.items.AbsorbCrystalType;

/**
 * This class stores Soul Crystal leveling infos related to NPCs, notably:
 * <ul>
 * <li>AbsorbCrystalType which can be LAST_HIT, FULL_PARTY or PARTY_ONE_RANDOM ;</li>
 * <li>If the item cast on monster is required or not ;</li>
 * <li>The chance of break (base 1000) ;</li>
 * <li>The chance of success (base 1000) ;</li>
 * <li>The list of allowed crystals levels.</li>
 * </ul>
 */
public final class LevelingInfo
{
	private final AbsorbCrystalType _absorbCrystalType;
	private final boolean _skillRequired;
	private final int _chanceStage;
	private final int _chanceBreak;
	private final int[] _levelList;
	
	public LevelingInfo(StatsSet set)
	{
		_absorbCrystalType = set.getEnum("absorbType", AbsorbCrystalType.class);
		_skillRequired = set.getBool("skill");
		_chanceStage = set.getInteger("chanceStage");
		_chanceBreak = set.getInteger("chanceBreak");
		_levelList = set.getIntegerArray("levelList");
	}
	
	public AbsorbCrystalType getAbsorbCrystalType()
	{
		return _absorbCrystalType;
	}
	
	public boolean isSkillRequired()
	{
		return _skillRequired;
	}
	
	public int getChanceStage()
	{
		return _chanceStage;
	}
	
	public int getChanceBreak()
	{
		return _chanceBreak;
	}
	
	public int[] getLevelList()
	{
		return _levelList;
	}
}
package dev.l2j.tesla.gameserver.model;

import dev.l2j.tesla.commons.util.StatsSet;

public class PetDataEntry
{
	private final long _maxExp;
	
	private final int _maxMeal;
	private final int _expType;
	private final int _mealInBattle;
	private final int _mealInNormal;
	
	private final double _pAtk;
	private final double _pDef;
	private final double _mAtk;
	private final double _mDef;
	private final double _maxHp;
	private final double _maxMp;
	
	private final float _hpRegen;
	private final float _mpRegen;
	
	private final int _ssCount;
	private final int _spsCount;
	
	private final int _mountMealInBattle;
	private final int _mountMealInNormal;
	private final int _mountAtkSpd;
	private final double _mountPAtk;
	private final double _mountMAtk;
	private final int _mountBaseSpeed;
	private final int _mountWaterSpeed;
	private final int _mountFlySpeed;
	
	public PetDataEntry(StatsSet stats)
	{
		_maxExp = stats.getLong("exp");
		
		_maxMeal = stats.getInteger("maxMeal");
		_expType = stats.getInteger("expType");
		_mealInBattle = stats.getInteger("mealInBattle");
		_mealInNormal = stats.getInteger("mealInNormal");
		
		_pAtk = stats.getDouble("pAtk");
		_pDef = stats.getDouble("pDef");
		_mAtk = stats.getDouble("mAtk");
		_mDef = stats.getDouble("mDef");
		_maxHp = stats.getDouble("hp");
		_maxMp = stats.getDouble("mp");
		
		_hpRegen = stats.getFloat("hpRegen");
		_mpRegen = stats.getFloat("mpRegen");
		
		_ssCount = stats.getInteger("ssCount");
		_spsCount = stats.getInteger("spsCount");
		
		_mountMealInBattle = stats.getInteger("mealInBattleOnRide", 0);
		_mountMealInNormal = stats.getInteger("mealInNormalOnRide", 0);
		_mountAtkSpd = stats.getInteger("atkSpdOnRide", 0);
		_mountPAtk = stats.getDouble("pAtkOnRide", 0);
		_mountMAtk = stats.getDouble("mAtkOnRide", 0);
		
		String speed = stats.getString("speedOnRide", null);
		if (speed != null)
		{
			String[] speeds = speed.split(";");
			_mountBaseSpeed = Integer.parseInt(speeds[0]);
			_mountWaterSpeed = Integer.parseInt(speeds[2]);
			_mountFlySpeed = Integer.parseInt(speeds[4]);
		}
		else
		{
			_mountBaseSpeed = 0;
			_mountWaterSpeed = 0;
			_mountFlySpeed = 0;
		}
	}
	
	public long getMaxExp()
	{
		return _maxExp;
	}
	
	public int getMaxMeal()
	{
		return _maxMeal;
	}
	
	public int getExpType()
	{
		return _expType;
	}
	
	public int getMealInBattle()
	{
		return _mealInBattle;
	}
	
	public int getMealInNormal()
	{
		return _mealInNormal;
	}
	
	public double getPAtk()
	{
		return _pAtk;
	}
	
	public double getPDef()
	{
		return _pDef;
	}
	
	public double getMAtk()
	{
		return _mAtk;
	}
	
	public double getMDef()
	{
		return _mDef;
	}
	
	public double getMaxHp()
	{
		return _maxHp;
	}
	
	public double getMaxMp()
	{
		return _maxMp;
	}
	
	public float getHpRegen()
	{
		return _hpRegen;
	}
	
	public float getMpRegen()
	{
		return _mpRegen;
	}
	
	public int getSsCount()
	{
		return _ssCount;
	}
	
	public int getSpsCount()
	{
		return _spsCount;
	}
	
	public int getMountMealInBattle()
	{
		return _mountMealInBattle;
	}
	
	public int getMountMealInNormal()
	{
		return _mountMealInNormal;
	}
	
	public int getMountAtkSpd()
	{
		return _mountAtkSpd;
	}
	
	public double getMountPAtk()
	{
		return _mountPAtk;
	}
	
	public double getMountMAtk()
	{
		return _mountMAtk;
	}
	
	public int getMountBaseSpeed()
	{
		return _mountBaseSpeed;
	}
	
	public int getMountSwimSpeed()
	{
		return _mountWaterSpeed;
	}
	
	public int getMountFlySpeed()
	{
		return _mountFlySpeed;
	}
}
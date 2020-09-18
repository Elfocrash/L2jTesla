package dev.l2j.tesla.gameserver.model.actor.template;

import dev.l2j.tesla.commons.util.StatsSet;

/**
 * The generic datatype used by any character template. It holds basic informations, such as base stats (STR, CON, DEX,...) and extended stats (power attack, magic attack, hp/mp regen, collision values).
 */
public class CreatureTemplate
{
	private final int _baseSTR;
	private final int _baseCON;
	private final int _baseDEX;
	private final int _baseINT;
	private final int _baseWIT;
	private final int _baseMEN;
	
	private final double _baseHpMax;
	private final double _baseMpMax;
	
	private final double _baseHpReg;
	private final double _baseMpReg;
	
	private final double _basePAtk;
	private final double _baseMAtk;
	private final double _basePDef;
	private final double _baseMDef;
	
	private final int _basePAtkSpd;
	
	private final int _baseCritRate;
	
	private final int _baseWalkSpd;
	private final int _baseRunSpd;
	
	protected final double _collisionRadius;
	protected final double _collisionHeight;
	
	public CreatureTemplate(StatsSet set)
	{
		_baseSTR = set.getInteger("str", 40);
		_baseCON = set.getInteger("con", 21);
		_baseDEX = set.getInteger("dex", 30);
		_baseINT = set.getInteger("int", 20);
		_baseWIT = set.getInteger("wit", 43);
		_baseMEN = set.getInteger("men", 20);
		
		_baseHpMax = set.getDouble("hp", 0);
		_baseMpMax = set.getDouble("mp", 0);
		
		_baseHpReg = set.getDouble("hpRegen", 1.5d);
		_baseMpReg = set.getDouble("mpRegen", 0.9d);
		
		_basePAtk = set.getDouble("pAtk");
		_baseMAtk = set.getDouble("mAtk");
		_basePDef = set.getDouble("pDef");
		_baseMDef = set.getDouble("mDef");
		
		_basePAtkSpd = set.getInteger("atkSpd", 300);
		
		_baseCritRate = set.getInteger("crit", 4);
		
		_baseWalkSpd = set.getInteger("walkSpd", 0);
		_baseRunSpd = set.getInteger("runSpd", 1);
		
		_collisionRadius = set.getDouble("radius");
		_collisionHeight = set.getDouble("height");
	}
	
	public final int getBaseSTR()
	{
		return _baseSTR;
	}
	
	public final int getBaseCON()
	{
		return _baseCON;
	}
	
	public final int getBaseDEX()
	{
		return _baseDEX;
	}
	
	public final int getBaseINT()
	{
		return _baseINT;
	}
	
	public final int getBaseWIT()
	{
		return _baseWIT;
	}
	
	public final int getBaseMEN()
	{
		return _baseMEN;
	}
	
	public double getBaseHpMax(int level)
	{
		return _baseHpMax;
	}
	
	public double getBaseMpMax(int level)
	{
		return _baseMpMax;
	}
	
	public final double getBaseHpReg()
	{
		return _baseHpReg;
	}
	
	public final double getBaseMpReg()
	{
		return _baseMpReg;
	}
	
	public final double getBasePAtk()
	{
		return _basePAtk;
	}
	
	public final double getBaseMAtk()
	{
		return _baseMAtk;
	}
	
	public final double getBasePDef()
	{
		return _basePDef;
	}
	
	public final double getBaseMDef()
	{
		return _baseMDef;
	}
	
	public final int getBasePAtkSpd()
	{
		return _basePAtkSpd;
	}
	
	public final int getBaseCritRate()
	{
		return _baseCritRate;
	}
	
	public final int getBaseWalkSpeed()
	{
		return _baseWalkSpd;
	}
	
	public final int getBaseRunSpeed()
	{
		return _baseRunSpd;
	}
	
	public final double getCollisionRadius()
	{
		return _collisionRadius;
	}
	
	public final double getCollisionHeight()
	{
		return _collisionHeight;
	}
}
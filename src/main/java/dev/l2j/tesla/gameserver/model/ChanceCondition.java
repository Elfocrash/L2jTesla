package dev.l2j.tesla.gameserver.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import dev.l2j.tesla.commons.random.Rnd;
import dev.l2j.tesla.commons.util.StatsSet;

/**
 * @author kombat
 */
public final class ChanceCondition
{
	protected static final Logger _log = Logger.getLogger(ChanceCondition.class.getName());
	public static final int EVT_HIT = 1;
	public static final int EVT_CRIT = 2;
	public static final int EVT_CAST = 4;
	public static final int EVT_PHYSICAL = 8;
	public static final int EVT_MAGIC = 16;
	public static final int EVT_MAGIC_GOOD = 32;
	public static final int EVT_MAGIC_OFFENSIVE = 64;
	public static final int EVT_ATTACKED = 128;
	public static final int EVT_ATTACKED_HIT = 256;
	public static final int EVT_ATTACKED_CRIT = 512;
	public static final int EVT_HIT_BY_SKILL = 1024;
	public static final int EVT_HIT_BY_OFFENSIVE_SKILL = 2048;
	public static final int EVT_HIT_BY_GOOD_MAGIC = 4096;
	public static final int EVT_EVADED_HIT = 8192;
	public static final int EVT_ON_START = 16384;
	public static final int EVT_ON_ACTION_TIME = 32768;
	public static final int EVT_ON_EXIT = 65536;
	
	public static enum TriggerType
	{
		// You hit an enemy
		ON_HIT(1),
		// You hit an enemy - was crit
		ON_CRIT(2),
		// You cast a skill
		ON_CAST(4),
		// You cast a skill - it was a physical one
		ON_PHYSICAL(8),
		// You cast a skill - it was a magic one
		ON_MAGIC(16),
		// You cast a skill - it was a magic one - good magic
		ON_MAGIC_GOOD(32),
		// You cast a skill - it was a magic one - offensive magic
		ON_MAGIC_OFFENSIVE(64),
		// You are attacked by enemy
		ON_ATTACKED(128),
		// You are attacked by enemy - by hit
		ON_ATTACKED_HIT(256),
		// You are attacked by enemy - by hit - was crit
		ON_ATTACKED_CRIT(512),
		// A skill was casted on you
		ON_HIT_BY_SKILL(1024),
		// An evil skill was casted on you
		ON_HIT_BY_OFFENSIVE_SKILL(2048),
		// A good skill was casted on you
		ON_HIT_BY_GOOD_MAGIC(4096),
		// Evading melee attack
		ON_EVADED_HIT(8192),
		// Effect only - on start
		ON_START(16384),
		// Effect only - each second
		ON_ACTION_TIME(32768),
		// Effect only - on exit
		ON_EXIT(65536);
		
		private final int _mask;
		
		private TriggerType(int mask)
		{
			_mask = mask;
		}
		
		public final boolean check(int event)
		{
			return (_mask & event) != 0; // Trigger (sub-)type contains event (sub-)type
		}
	}
	
	private final TriggerType _triggerType;
	private final int _chance;
	
	private ChanceCondition(TriggerType trigger, int chance)
	{
		_triggerType = trigger;
		_chance = chance;
	}
	
	public static ChanceCondition parse(StatsSet set)
	{
		try
		{
			TriggerType trigger = set.getEnum("chanceType", TriggerType.class, null);
			int chance = set.getInteger("activationChance", -1);
			
			if (trigger != null)
				return new ChanceCondition(trigger, chance);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
		return null;
	}
	
	public static ChanceCondition parse(String chanceType, int chance)
	{
		try
		{
			if (chanceType == null)
				return null;
			
			TriggerType trigger = Enum.valueOf(TriggerType.class, chanceType);
			
			if (trigger != null)
				return new ChanceCondition(trigger, chance);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
		
		return null;
	}
	
	public boolean trigger(int event)
	{
		return _triggerType.check(event) && (_chance < 0 || Rnd.get(100) < _chance);
	}
	
	@Override
	public String toString()
	{
		return "Trigger[" + _chance + ";" + _triggerType.toString() + "]";
	}
}
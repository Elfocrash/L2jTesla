package dev.l2j.tesla.gameserver.model.holder.skillnode;

import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.commons.util.StatsSet;

/**
 * A datatype used by enchant skill types. It extends {@link IntIntHolder}.
 */
public class EnchantSkillNode extends IntIntHolder
{
	private final int _exp;
	private final int _sp;
	
	private final int[] _enchantRates = new int[5];
	
	private IntIntHolder _item;
	
	public EnchantSkillNode(StatsSet set)
	{
		super(set.getInteger("id"), set.getInteger("lvl"));
		
		_exp = set.getInteger("exp");
		_sp = set.getInteger("sp");
		
		_enchantRates[0] = set.getInteger("rate76");
		_enchantRates[1] = set.getInteger("rate77");
		_enchantRates[2] = set.getInteger("rate78");
		_enchantRates[3] = set.getInteger("rate79");
		_enchantRates[4] = set.getInteger("rate80");
		
		if (set.containsKey("itemNeeded"))
			_item = set.getIntIntHolder("itemNeeded");
	}
	
	public int getExp()
	{
		return _exp;
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public int getEnchantRate(int level)
	{
		return _enchantRates[level - 76];
	}
	
	public IntIntHolder getItem()
	{
		return _item;
	}
}
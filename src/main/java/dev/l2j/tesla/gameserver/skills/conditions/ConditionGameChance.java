package dev.l2j.tesla.gameserver.skills.conditions;

import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.skills.Env;

/**
 * @author Advi
 */
public class ConditionGameChance extends Condition
{
	private final int _chance;
	
	public ConditionGameChance(int chance)
	{
		_chance = chance;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return Rnd.get(100) < _chance;
	}
}

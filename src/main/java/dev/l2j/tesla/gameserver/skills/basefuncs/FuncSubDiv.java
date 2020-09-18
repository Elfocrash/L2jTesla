package dev.l2j.tesla.gameserver.skills.basefuncs;

import dev.l2j.tesla.gameserver.enums.skills.Stats;
import dev.l2j.tesla.gameserver.skills.Env;

public class FuncSubDiv extends Func
{
	public FuncSubDiv(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		super(pStat, pOrder, owner, lambda);
	}
	
	@Override
	public void calc(Env env)
	{
		if (cond == null || cond.test(env))
			env.divValue(1 - (_lambda.calc(env) / 100));
	}
}
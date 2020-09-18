package dev.l2j.tesla.gameserver.skills.funcs;

import dev.l2j.tesla.gameserver.skills.Env;
import dev.l2j.tesla.gameserver.skills.Formulas;
import dev.l2j.tesla.gameserver.skills.basefuncs.Func;
import dev.l2j.tesla.gameserver.enums.skills.Stats;
import dev.l2j.tesla.gameserver.model.actor.Summon;

public class FuncAtkCritical extends Func
{
	static final FuncAtkCritical _fac_instance = new FuncAtkCritical();
	
	public static Func getInstance()
	{
		return _fac_instance;
	}
	
	private FuncAtkCritical()
	{
		super(Stats.CRITICAL_RATE, 0x09, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		if (!(env.getCharacter() instanceof Summon))
			env.mulValue(Formulas.DEX_BONUS[env.getCharacter().getDEX()]);
		
		env.mulValue(10);
		
		env.setBaseValue(env.getValue());
	}
}

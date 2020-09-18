package dev.l2j.tesla.gameserver.skills.funcs;

import dev.l2j.tesla.gameserver.skills.Env;
import dev.l2j.tesla.gameserver.skills.Formulas;
import dev.l2j.tesla.gameserver.skills.basefuncs.Func;
import dev.l2j.tesla.gameserver.enums.skills.Stats;

public class FuncMoveSpeed extends Func
{
	static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();
	
	public static Func getInstance()
	{
		return _fms_instance;
	}
	
	private FuncMoveSpeed()
	{
		super(Stats.RUN_SPEED, 0x30, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.mulValue(Formulas.DEX_BONUS[env.getCharacter().getDEX()]);
	}
}
package dev.l2j.tesla.gameserver.skills.effects;

import dev.l2j.tesla.gameserver.skills.Env;
import dev.l2j.tesla.gameserver.enums.skills.L2EffectFlag;
import dev.l2j.tesla.gameserver.enums.skills.L2EffectType;
import dev.l2j.tesla.gameserver.model.L2Effect;

/**
 * @author mkizub
 */
final class EffectSleep extends L2Effect
{
	public EffectSleep(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SLEEP;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startSleeping();
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopSleeping(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public boolean onSameEffect(L2Effect effect)
	{
		return false;
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.SLEEP.getMask();
	}
}
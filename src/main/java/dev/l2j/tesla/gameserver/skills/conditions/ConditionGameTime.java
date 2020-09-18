package dev.l2j.tesla.gameserver.skills.conditions;

import dev.l2j.tesla.gameserver.taskmanager.GameTimeTaskManager;
import dev.l2j.tesla.gameserver.skills.Env;

/**
 * @author mkizub
 */
public class ConditionGameTime extends Condition
{
	private final boolean _night;
	
	public ConditionGameTime(boolean night)
	{
		_night = night;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return GameTimeTaskManager.getInstance().isNight() == _night;
	}
}
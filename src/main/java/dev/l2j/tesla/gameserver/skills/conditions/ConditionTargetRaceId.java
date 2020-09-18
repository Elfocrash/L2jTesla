package dev.l2j.tesla.gameserver.skills.conditions;

import java.util.List;

import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.skills.Env;

/**
 * @author nBd
 */
public class ConditionTargetRaceId extends Condition
{
	private final List<Integer> _raceIds;
	
	public ConditionTargetRaceId(List<Integer> raceId)
	{
		_raceIds = raceId;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.getTarget() instanceof Npc))
			return false;
		
		return _raceIds.contains(((Npc) env.getTarget()).getTemplate().getRace().ordinal());
	}
}
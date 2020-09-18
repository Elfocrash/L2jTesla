package dev.l2j.tesla.gameserver.skills.conditions;

import java.util.List;

import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.instance.Door;
import dev.l2j.tesla.gameserver.skills.Env;

/**
 * The Class ConditionTargetNpcId.
 */
public class ConditionTargetNpcId extends Condition
{
	private final List<Integer> _npcIds;
	
	/**
	 * Instantiates a new condition target npc id.
	 * @param npcIds the npc ids
	 */
	public ConditionTargetNpcId(List<Integer> npcIds)
	{
		_npcIds = npcIds;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getTarget() instanceof Npc)
			return _npcIds.contains(((Npc) env.getTarget()).getNpcId());
		
		if (env.getTarget() instanceof Door)
			return _npcIds.contains(((Door) env.getTarget()).getDoorId());
		
		return false;
	}
}
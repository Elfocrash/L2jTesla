package dev.l2j.tesla.gameserver.skills.conditions;

import dev.l2j.tesla.gameserver.model.L2Effect;
import dev.l2j.tesla.gameserver.skills.Env;

/**
 * The Class ConditionPlayerActiveEffectId.
 */
public class ConditionPlayerActiveEffectId extends Condition
{
	private final int _effectId;
	private final int _effectLvl;
	
	/**
	 * Instantiates a new condition player active effect id.
	 * @param effectId the effect id
	 */
	public ConditionPlayerActiveEffectId(int effectId)
	{
		_effectId = effectId;
		_effectLvl = -1;
	}
	
	/**
	 * Instantiates a new condition player active effect id.
	 * @param effectId the effect id
	 * @param effectLevel the effect level
	 */
	public ConditionPlayerActiveEffectId(int effectId, int effectLevel)
	{
		_effectId = effectId;
		_effectLvl = effectLevel;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		final L2Effect e = env.getCharacter().getFirstEffect(_effectId);
		if (e != null && (_effectLvl == -1 || _effectLvl <= e.getSkill().getLevel()))
			return true;
		
		return false;
	}
}
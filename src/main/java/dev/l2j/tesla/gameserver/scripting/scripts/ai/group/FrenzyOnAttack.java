package dev.l2j.tesla.gameserver.scripting.scripts.ai.group;

import dev.l2j.tesla.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;

/**
 * Frenzy behavior, so far 5 types of orcs.<br>
 * Few others monsters got that skillid, need to investigate later :
 * <ul>
 * <li>Halisha's Officer</li>
 * <li>Executioner of Halisha</li>
 * <li>Alpine Kookaburra</li>
 * <li>Alpine Buffalo</li>
 * <li>Alpine Cougar</li>
 * </ul>
 */
public class FrenzyOnAttack extends L2AttackableAIScript
{
	private static final L2Skill ULTIMATE_BUFF = SkillTable.getInstance().getInfo(4318, 1);
	
	private static final String[] ORCS_WORDS =
	{
		"Dear ultimate power!!!",
		"The battle has just begun!",
		"I never thought I'd use this against a novice!",
		"You won't take me down easily."
	};
	
	public FrenzyOnAttack()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(20270, 20495, 20588, 20778, 21116);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		// The only requirements are HPs < 25% and not already under the buff. It's not 100% aswell.
		if (npc.getCurrentHp() / npc.getMaxHp() < 0.25 && npc.getFirstEffect(ULTIMATE_BUFF) == null && Rnd.get(10) == 0)
		{
			npc.broadcastNpcSay(Rnd.get(ORCS_WORDS));
			npc.setTarget(npc);
			npc.doCast(ULTIMATE_BUFF);
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
}
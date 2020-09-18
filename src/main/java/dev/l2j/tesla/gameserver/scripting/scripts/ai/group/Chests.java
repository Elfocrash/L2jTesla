package dev.l2j.tesla.gameserver.scripting.scripts.ai.group;

import dev.l2j.tesla.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import dev.l2j.tesla.commons.random.Rnd;
import dev.l2j.tesla.commons.util.ArraysUtil;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.enums.ScriptEventType;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.Chest;

public class Chests extends L2AttackableAIScript
{
	private static final int SKILL_DELUXE_KEY = 2229;
	private static final int SKILL_BOX_KEY = 2065;
	
	private static final int[] NPC_IDS =
	{
		18265,
		18266,
		18267,
		18268,
		18269,
		18270,
		18271,
		18272,
		18273,
		18274,
		18275,
		18276,
		18277,
		18278,
		18279,
		18280,
		18281,
		18282,
		18283,
		18284,
		18285,
		18286,
		18287,
		18288,
		18289,
		18290,
		18291,
		18292,
		18293,
		18294,
		18295,
		18296,
		18297,
		18298,
		21671,
		21694,
		21717,
		21740,
		21763,
		21786,
		21801,
		21802,
		21803,
		21804,
		21805,
		21806,
		21807,
		21808,
		21809,
		21810,
		21811,
		21812,
		21813,
		21814,
		21815,
		21816,
		21817,
		21818,
		21819,
		21820,
		21821,
		21822
	};
	
	public Chests()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(NPC_IDS, ScriptEventType.ON_ATTACK, ScriptEventType.ON_SKILL_SEE);
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet)
	{
		if (npc instanceof Chest)
		{
			// This behavior is only run when the target of skill is the passed npc.
			if (!ArraysUtil.contains(targets, npc))
				return super.onSkillSee(npc, caster, skill, targets, isPet);
			
			final Chest chest = ((Chest) npc);
			
			// If this chest has already been interacted, no further AI decisions are needed.
			if (!chest.isInteracted())
			{
				chest.setInteracted();
				
				// If it's the first interaction, check if this is a box or mimic.
				if (Rnd.get(100) < 40)
				{
					switch (skill.getId())
					{
						case SKILL_BOX_KEY:
						case SKILL_DELUXE_KEY:
							// check the chance to open the box.
							int keyLevelNeeded = (chest.getLevel() / 10) - skill.getLevel();
							if (keyLevelNeeded < 0)
								keyLevelNeeded *= -1;
							
							// Regular keys got 60% to succeed.
							final int chance = ((skill.getId() == SKILL_BOX_KEY) ? 60 : 100) - keyLevelNeeded * 40;
							
							// Success, die with rewards.
							if (Rnd.get(100) < chance)
							{
								chest.setSpecialDrop();
								chest.doDie(caster);
							}
							// Used a key but failed to open: disappears with no rewards.
							else
								chest.deleteMe(); // TODO: replace for a better system (as chests attack once before decaying)
							break;
						
						default:
							chest.doCast(SkillTable.getInstance().getInfo(4143, Math.min(10, Math.round(npc.getLevel() / 10))));
							break;
					}
				}
				// Mimic behavior : attack the caster.
				else
					attack(chest, ((isPet) ? caster.getSummon() : caster));
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (npc instanceof Chest)
		{
			final Chest chest = ((Chest) npc);
			
			// If this has already been interacted, no further AI decisions are needed.
			if (!chest.isInteracted())
			{
				chest.setInteracted();
				
				// If it was a box, cast a suicide type skill.
				if (Rnd.get(100) < 40)
					chest.doCast(SkillTable.getInstance().getInfo(4143, Math.min(10, Math.round(npc.getLevel() / 10))));
			}
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
}
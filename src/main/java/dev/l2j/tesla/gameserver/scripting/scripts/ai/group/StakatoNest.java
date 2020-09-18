package dev.l2j.tesla.gameserver.scripting.scripts.ai.group;

import dev.l2j.tesla.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillUse;

/**
 * This AI handles following behaviors :
 * <ul>
 * <li>Cannibalistic Stakato Leader : try to eat a Follower, if any around, at low HPs.</li>
 * <li>Female Spiked Stakato : when Male dies, summons 3 Spiked Stakato Guards.</li>
 * <li>Male Spiked Stakato : when Female dies, transforms in stronger form.</li>
 * <li>Spiked Stakato Baby : when Spiked Stakato Nurse dies, her baby summons 3 Spiked Stakato Captains.</li>
 * <li>Spiked Stakato Nurse : when Spiked Stakato Baby dies, transforms in stronger form.</li>
 * </ul>
 * As NCSoft implemented it on postIL, but skills exist since IL, I decided to implemented that script to "honor" the idea (which is kinda funny).
 */
public class StakatoNest extends L2AttackableAIScript
{
	private static final int SPIKED_STAKATO_GUARD = 22107;
	private static final int FEMALE_SPIKED_STAKATO = 22108;
	private static final int MALE_SPIKED_STAKATO_1 = 22109;
	private static final int MALE_SPIKED_STAKATO_2 = 22110;
	
	private static final int STAKATO_FOLLOWER = 22112;
	private static final int CANNIBALISTIC_STAKATO_LEADER_1 = 22113;
	private static final int CANNIBALISTIC_STAKATO_LEADER_2 = 22114;
	
	private static final int SPIKED_STAKATO_CAPTAIN = 22117;
	private static final int SPIKED_STAKATO_NURSE_1 = 22118;
	private static final int SPIKED_STAKATO_NURSE_2 = 22119;
	private static final int SPIKED_STAKATO_BABY = 22120;
	
	public StakatoNest()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(CANNIBALISTIC_STAKATO_LEADER_1, CANNIBALISTIC_STAKATO_LEADER_2);
		addKillId(MALE_SPIKED_STAKATO_1, FEMALE_SPIKED_STAKATO, SPIKED_STAKATO_NURSE_1, SPIKED_STAKATO_BABY);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (npc.getCurrentHp() / npc.getMaxHp() < 0.3 && Rnd.get(100) < 5)
		{
			for (Monster follower : npc.getKnownTypeInRadius(Monster.class, 400))
			{
				if (follower.getNpcId() == STAKATO_FOLLOWER && !follower.isDead())
				{
					npc.setIsCastingNow(true);
					npc.broadcastPacket(new MagicSkillUse(npc, follower, (npc.getNpcId() == CANNIBALISTIC_STAKATO_LEADER_2) ? 4072 : 4073, 1, 3000, 0));
					ThreadPool.schedule(() ->
					{
						if (npc.isDead())
							return;
						
						if (follower.isDead())
						{
							npc.setIsCastingNow(false);
							return;
						}
						
						npc.setCurrentHp(npc.getCurrentHp() + (follower.getCurrentHp() / 2));
						follower.doDie(follower);
						npc.setIsCastingNow(false);
					}, 3000L);
					break;
				}
			}
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		switch (npc.getNpcId())
		{
			case MALE_SPIKED_STAKATO_1:
				for (Monster angryFemale : npc.getKnownTypeInRadius(Monster.class, 400))
				{
					if (angryFemale.getNpcId() == FEMALE_SPIKED_STAKATO && !angryFemale.isDead())
					{
						for (int i = 0; i < 3; i++)
						{
							final Npc guard = addSpawn(SPIKED_STAKATO_GUARD, angryFemale, true, 0, false);
							attack(((Attackable) guard), killer);
						}
					}
				}
				break;
			
			case FEMALE_SPIKED_STAKATO:
				for (Monster morphingMale : npc.getKnownTypeInRadius(Monster.class, 400))
				{
					if (morphingMale.getNpcId() == MALE_SPIKED_STAKATO_1 && !morphingMale.isDead())
					{
						final Npc newForm = addSpawn(MALE_SPIKED_STAKATO_2, morphingMale, true, 0, false);
						attack(((Attackable) newForm), killer);
						
						morphingMale.deleteMe();
					}
				}
				break;
			
			case SPIKED_STAKATO_NURSE_1:
				for (Monster baby : npc.getKnownTypeInRadius(Monster.class, 400))
				{
					if (baby.getNpcId() == SPIKED_STAKATO_BABY && !baby.isDead())
					{
						for (int i = 0; i < 3; i++)
						{
							final Npc captain = addSpawn(SPIKED_STAKATO_CAPTAIN, baby, true, 0, false);
							attack(((Attackable) captain), killer);
						}
					}
				}
				break;
			
			case SPIKED_STAKATO_BABY:
				for (Monster morphingNurse : npc.getKnownTypeInRadius(Monster.class, 400))
				{
					if (morphingNurse.getNpcId() == SPIKED_STAKATO_NURSE_1 && !morphingNurse.isDead())
					{
						final Npc newForm = addSpawn(SPIKED_STAKATO_NURSE_2, morphingNurse, true, 0, false);
						attack(((Attackable) newForm), killer);
						
						morphingNurse.deleteMe();
					}
				}
				break;
		}
		return super.onKill(npc, killer);
	}
}
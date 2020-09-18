package dev.l2j.tesla.gameserver.handler.skillhandlers;

import dev.l2j.tesla.gameserver.handler.ISkillHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.enums.AiEventType;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.items.ShotType;
import dev.l2j.tesla.gameserver.enums.skills.L2EffectType;
import dev.l2j.tesla.gameserver.enums.skills.L2SkillType;
import dev.l2j.tesla.gameserver.enums.skills.Stats;
import dev.l2j.tesla.gameserver.model.L2Effect;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;
import dev.l2j.tesla.gameserver.model.actor.ai.type.AttackableAI;
import dev.l2j.tesla.gameserver.model.actor.instance.SiegeSummon;
import dev.l2j.tesla.gameserver.skills.Env;
import dev.l2j.tesla.gameserver.skills.Formulas;

public class Disablers implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.STUN,
		L2SkillType.ROOT,
		L2SkillType.SLEEP,
		L2SkillType.CONFUSION,
		L2SkillType.AGGDAMAGE,
		L2SkillType.AGGREDUCE,
		L2SkillType.AGGREDUCE_CHAR,
		L2SkillType.AGGREMOVE,
		L2SkillType.MUTE,
		L2SkillType.FAKE_DEATH,
		L2SkillType.NEGATE,
		L2SkillType.CANCEL_DEBUFF,
		L2SkillType.PARALYZE,
		L2SkillType.ERASE,
		L2SkillType.BETRAY
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		L2SkillType type = skill.getSkillType();
		
		final boolean ss = activeChar.isChargedShot(ShotType.SOULSHOT);
		final boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
		final boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
		
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			Creature target = (Creature) obj;
			if (target.isDead() || (target.isInvul() && !target.isParalyzed())) // bypass if target is dead or invul (excluding invul from Petrification)
				continue;
			
			if (skill.isOffensive() && target.getFirstEffect(L2EffectType.BLOCK_DEBUFF) != null)
				continue;
			
			byte shld = Formulas.calcShldUse(activeChar, target, skill);
			
			switch (type)
			{
				case BETRAY:
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
					else
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
					break;
				
				case FAKE_DEATH:
					// stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
					skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
					break;
				
				case ROOT:
				case STUN:
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
						target = activeChar;
					
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
					else
					{
						if (activeChar instanceof Player)
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill.getId()));
					}
					break;
				
				case SLEEP:
				case PARALYZE: // use same as root for now
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
						target = activeChar;
					
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
					else
					{
						if (activeChar instanceof Player)
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill.getId()));
					}
					break;
				
				case MUTE:
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
						target = activeChar;
					
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
					{
						// stop same type effect if available
						L2Effect[] effects = target.getAllEffects();
						for (L2Effect e : effects)
						{
							if (e.getSkill().getSkillType() == type)
								e.exit();
						}
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
					}
					else
					{
						if (activeChar instanceof Player)
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill.getId()));
					}
					break;
				
				case CONFUSION:
					// do nothing if not on mob
					if (target instanceof Attackable)
					{
						if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
						{
							L2Effect[] effects = target.getAllEffects();
							for (L2Effect e : effects)
							{
								if (e.getSkill().getSkillType() == type)
									e.exit();
							}
							skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
						}
						else
						{
							if (activeChar instanceof Player)
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
						}
					}
					else
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					break;
				
				case AGGDAMAGE:
					if (target instanceof Attackable)
						target.getAI().notifyEvent(AiEventType.AGGRESSION, activeChar, (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
					
					skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
					break;
				
				case AGGREDUCE:
					// these skills needs to be rechecked
					if (target instanceof Attackable)
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
						
						double aggdiff = ((Attackable) target).getHating(activeChar) - target.calcStat(Stats.AGGRESSION, ((Attackable) target).getHating(activeChar), target, skill);
						
						if (skill.getPower() > 0)
							((Attackable) target).reduceHate(null, (int) skill.getPower());
						else if (aggdiff > 0)
							((Attackable) target).reduceHate(null, (int) aggdiff);
					}
					break;
				
				case AGGREDUCE_CHAR:
					// these skills needs to be rechecked
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
					{
						if (target instanceof Attackable)
						{
							Attackable targ = (Attackable) target;
							targ.stopHating(activeChar);
							if (targ.getMostHated() == null && targ.hasAI() && targ.getAI() instanceof AttackableAI)
							{
								((AttackableAI) targ.getAI()).setGlobalAggro(-25);
								targ.getAggroList().clear();
								targ.getAI().setIntention(IntentionType.ACTIVE);
								targ.setWalking();
							}
						}
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
					}
					else
					{
						if (activeChar instanceof Player)
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
						
						target.getAI().notifyEvent(AiEventType.ATTACKED, activeChar);
					}
					break;
				
				case AGGREMOVE:
					// these skills needs to be rechecked
					if (target instanceof Attackable && !target.isRaidRelated())
					{
						if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
						{
							if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_UNDEAD)
							{
								if (target.isUndead())
									((Attackable) target).reduceHate(null, ((Attackable) target).getHating(((Attackable) target).getMostHated()));
							}
							else
								((Attackable) target).reduceHate(null, ((Attackable) target).getHating(((Attackable) target).getMostHated()));
						}
						else
						{
							if (activeChar instanceof Player)
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
							
							target.getAI().notifyEvent(AiEventType.ATTACKED, activeChar);
						}
					}
					else
						target.getAI().notifyEvent(AiEventType.ATTACKED, activeChar);
					break;
				
				case ERASE:
					// doesn't affect siege summons
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps) && !(target instanceof SiegeSummon))
					{
						final Player summonOwner = ((Summon) target).getOwner();
						final Summon summonPet = summonOwner.getSummon();
						if (summonPet != null)
						{
							summonPet.unSummon(summonOwner);
							summonOwner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED);
						}
					}
					else
					{
						if (activeChar instanceof Player)
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
					}
					break;
				
				case CANCEL_DEBUFF:
					L2Effect[] effects = target.getAllEffects();
					
					if (effects == null || effects.length == 0)
						break;
					
					int count = (skill.getMaxNegatedEffects() > 0) ? 0 : -2;
					for (L2Effect e : effects)
					{
						if (e == null || !e.getSkill().isDebuff() || !e.getSkill().canBeDispeled())
							continue;
						
						e.exit();
						
						if (count > -1)
						{
							count++;
							if (count >= skill.getMaxNegatedEffects())
								break;
						}
					}
					break;
				
				case NEGATE:
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
						target = activeChar;
					
					// Skills with negateId (skillId)
					if (skill.getNegateId().length != 0)
					{
						for (int id : skill.getNegateId())
						{
							if (id != 0)
								target.stopSkillEffects(id);
						}
					}
					// All others negate type skills
					else
					{
						final int negateLvl = skill.getNegateLvl();
						
						for (L2Effect e : target.getAllEffects())
						{
							final L2Skill effectSkill = e.getSkill();
							for (L2SkillType skillType : skill.getNegateStats())
							{
								// If power is -1 the effect is always removed without lvl check
								if (negateLvl == -1)
								{
									if (effectSkill.getSkillType() == skillType || (effectSkill.getEffectType() != null && effectSkill.getEffectType() == skillType))
										e.exit();
								}
								// Remove the effect according to its power.
								else
								{
									if (effectSkill.getEffectType() != null && effectSkill.getEffectAbnormalLvl() >= 0)
									{
										if (effectSkill.getEffectType() == skillType && effectSkill.getEffectAbnormalLvl() <= negateLvl)
											e.exit();
									}
									else if (effectSkill.getSkillType() == skillType && effectSkill.getAbnormalLvl() <= negateLvl)
										e.exit();
								}
							}
						}
					}
					skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
					break;
			}
		}
		
		if (skill.hasSelfEffects())
		{
			final L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
				effect.exit();
			
			skill.getEffectsSelf(activeChar);
		}
		activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
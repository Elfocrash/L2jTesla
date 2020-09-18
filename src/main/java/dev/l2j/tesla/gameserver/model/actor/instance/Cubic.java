package dev.l2j.tesla.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import dev.l2j.tesla.gameserver.handler.ISkillHandler;
import dev.l2j.tesla.gameserver.handler.SkillHandler;
import dev.l2j.tesla.gameserver.model.L2Effect;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillUse;
import dev.l2j.tesla.gameserver.skills.Formulas;
import dev.l2j.tesla.gameserver.skills.l2skills.L2SkillDrain;
import dev.l2j.tesla.gameserver.taskmanager.AttackStanceTaskManager;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.data.manager.DuelManager;
import dev.l2j.tesla.gameserver.enums.AiEventType;
import dev.l2j.tesla.gameserver.enums.ZoneId;
import dev.l2j.tesla.gameserver.enums.items.ShotType;
import dev.l2j.tesla.gameserver.enums.skills.L2SkillType;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;

public class Cubic
{
	// Type of cubics
	public static final int STORM_CUBIC = 1;
	public static final int VAMPIRIC_CUBIC = 2;
	public static final int LIFE_CUBIC = 3;
	public static final int VIPER_CUBIC = 4;
	public static final int POLTERGEIST_CUBIC = 5;
	public static final int BINDING_CUBIC = 6;
	public static final int AQUA_CUBIC = 7;
	public static final int SPARK_CUBIC = 8;
	public static final int ATTRACT_CUBIC = 9;
	
	// Max range of cubic skills
	public static final int MAX_MAGIC_RANGE = 900;
	
	// Cubic skills
	public static final int SKILL_CUBIC_HEAL = 4051;
	public static final int SKILL_CUBIC_CURE = 5579;
	
	protected Player _owner;
	protected Creature _target;
	
	protected int _id;
	protected int _matk;
	protected int _activationtime;
	protected int _activationchance;
	protected boolean _active;
	private final boolean _givenByOther;
	
	protected List<L2Skill> _skills = new ArrayList<>();
	
	private Future<?> _disappearTask;
	private Future<?> _actionTask;
	
	public Cubic(Player owner, int id, int level, int mAtk, int activationtime, int activationchance, int totallifetime, boolean givenByOther)
	{
		_owner = owner;
		_id = id;
		_matk = mAtk;
		_activationtime = activationtime * 1000;
		_activationchance = activationchance;
		_active = false;
		_givenByOther = givenByOther;
		
		switch (_id)
		{
			case STORM_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4049, level));
				break;
			
			case VAMPIRIC_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4050, level));
				break;
			
			case LIFE_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4051, level));
				doAction();
				break;
			
			case VIPER_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4052, level));
				break;
			
			case POLTERGEIST_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4053, level));
				_skills.add(SkillTable.getInstance().getInfo(4054, level));
				_skills.add(SkillTable.getInstance().getInfo(4055, level));
				break;
			
			case BINDING_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4164, level));
				break;
			
			case AQUA_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4165, level));
				break;
			
			case SPARK_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4166, level));
				break;
			
			case ATTRACT_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(5115, level));
				_skills.add(SkillTable.getInstance().getInfo(5116, level));
				break;
		}
		_disappearTask = ThreadPool.schedule(new Disappear(), totallifetime); // disappear
	}
	
	public synchronized void doAction()
	{
		if (_active)
			return;
		
		_active = true;
		
		switch (_id)
		{
			case AQUA_CUBIC:
			case BINDING_CUBIC:
			case SPARK_CUBIC:
			case STORM_CUBIC:
			case POLTERGEIST_CUBIC:
			case VAMPIRIC_CUBIC:
			case VIPER_CUBIC:
			case ATTRACT_CUBIC:
				_actionTask = ThreadPool.scheduleAtFixedRate(new Action(_activationchance), 0, _activationtime);
				break;
			
			case LIFE_CUBIC:
				_actionTask = ThreadPool.scheduleAtFixedRate(new Heal(), 0, _activationtime);
				break;
		}
	}
	
	public int getId()
	{
		return _id;
	}
	
	public Player getOwner()
	{
		return _owner;
	}
	
	public final int getMCriticalHit(Creature target, L2Skill skill)
	{
		return _owner.getMCriticalHit(target, skill);
	}
	
	public int getMAtk()
	{
		return _matk;
	}
	
	public void stopAction()
	{
		_target = null;
		if (_actionTask != null)
		{
			_actionTask.cancel(true);
			_actionTask = null;
		}
		_active = false;
	}
	
	public void cancelDisappear()
	{
		if (_disappearTask != null)
		{
			_disappearTask.cancel(true);
			_disappearTask = null;
		}
	}
	
	/** this sets the enemy target for a cubic */
	public void getCubicTarget()
	{
		_target = null;
		WorldObject ownerTarget = _owner.getTarget();
		if (ownerTarget == null)
			return;
		
		// Duel targeting
		if (_owner.isInDuel())
		{
			Player PlayerA = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerA();
			Player PlayerB = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerB();
			
			if (DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
			{
				Party partyA = PlayerA.getParty();
				Party partyB = PlayerB.getParty();
				Party partyEnemy = null;
				
				if (partyA != null)
				{
					if (partyA.containsPlayer(_owner))
						if (partyB != null)
							partyEnemy = partyB;
						else
							_target = PlayerB;
					else
						partyEnemy = partyA;
				}
				else
				{
					if (PlayerA == _owner)
						if (partyB != null)
							partyEnemy = partyB;
						else
							_target = PlayerB;
					else
						_target = PlayerA;
				}
				
				if (_target == PlayerA || _target == PlayerB)
					if (_target == ownerTarget)
						return;
					
				if (partyEnemy != null)
				{
					if (partyEnemy.containsPlayer(ownerTarget))
						_target = (Creature) ownerTarget;
					
					return;
				}
			}
			
			if (PlayerA != _owner && ownerTarget == PlayerA)
			{
				_target = PlayerA;
				return;
			}
			
			if (PlayerB != _owner && ownerTarget == PlayerB)
			{
				_target = PlayerB;
				return;
			}
			
			_target = null;
			return;
		}
		
		// Olympiad targeting
		if (_owner.isInOlympiadMode())
		{
			if (_owner.isOlympiadStart())
			{
				if (ownerTarget instanceof Playable)
				{
					final Player targetPlayer = ownerTarget.getActingPlayer();
					if (targetPlayer != null && targetPlayer.getOlympiadGameId() == _owner.getOlympiadGameId() && targetPlayer.getOlympiadSide() != _owner.getOlympiadSide())
						_target = (Creature) ownerTarget;
				}
			}
			return;
		}
		
		// test owners target if it is valid then use it
		if (ownerTarget instanceof Creature && ownerTarget != _owner.getSummon() && ownerTarget != _owner)
		{
			// target mob which has aggro on you or your summon
			if (ownerTarget instanceof Attackable && !((Attackable) ownerTarget).isDead())
			{
				if (((Attackable) ownerTarget).getAggroList().get(_owner) != null)
				{
					_target = (Creature) ownerTarget;
					return;
				}
				
				if (_owner.getSummon() != null)
				{
					if (((Attackable) ownerTarget).getAggroList().get(_owner.getSummon()) != null)
					{
						_target = (Creature) ownerTarget;
						return;
					}
				}
			}
			
			// get target in pvp or in siege
			Player enemy = null;
			
			if ((_owner.getPvpFlag() > 0 && !_owner.isInsideZone(ZoneId.PEACE)) || _owner.isInsideZone(ZoneId.PVP))
			{
				if (!((Creature) ownerTarget).isDead())
					enemy = ownerTarget.getActingPlayer();
				
				if (enemy != null)
				{
					boolean targetIt = true;
					
					final Party ownerParty = _owner.getParty();
					if (ownerParty != null)
					{
						if (ownerParty.containsPlayer(enemy))
							targetIt = false;
						else if (ownerParty.getCommandChannel() != null)
						{
							if (ownerParty.getCommandChannel().containsPlayer(enemy))
								targetIt = false;
						}
					}
					
					if (_owner.getClan() != null && !_owner.isInsideZone(ZoneId.PVP))
					{
						if (_owner.getClan().isMember(enemy.getObjectId()))
							targetIt = false;
						
						if (_owner.getAllyId() > 0 && enemy.getAllyId() > 0)
						{
							if (_owner.getAllyId() == enemy.getAllyId())
								targetIt = false;
						}
					}
					
					if (enemy.getPvpFlag() == 0 && !enemy.isInsideZone(ZoneId.PVP))
						targetIt = false;
					
					if (enemy.isInsideZone(ZoneId.PEACE))
						targetIt = false;
					
					if (_owner.getSiegeState() > 0 && _owner.getSiegeState() == enemy.getSiegeState())
						targetIt = false;
					
					if (!enemy.isVisible())
						targetIt = false;
					
					if (targetIt)
					{
						_target = enemy;
						return;
					}
				}
			}
		}
	}
	
	private class Action implements Runnable
	{
		private final int _chance;
		
		Action(int chance)
		{
			_chance = chance;
		}
		
		@Override
		public void run()
		{
			if (_owner.isDead() || !_owner.isOnline())
			{
				stopAction();
				_owner.delCubic(_id);
				_owner.broadcastUserInfo();
				cancelDisappear();
				return;
			}
			
			if (!AttackStanceTaskManager.getInstance().isInAttackStance(_owner))
			{
				stopAction();
				return;
			}
			
			if (Rnd.get(1, 100) < _chance)
			{
				final L2Skill skill = Rnd.get(_skills);
				if (skill != null)
				{
					// friendly skill, so we look a target in owner's party
					if (skill.getId() == SKILL_CUBIC_HEAL)
						cubicTargetForHeal();
					// offensive skill, we look for an enemy target
					else
					{
						getCubicTarget();
						if (!isInCubicRange(_owner, _target))
							_target = null;
					}
					
					final Creature target = _target;
					if (target != null && !target.isDead())
					{
						_owner.broadcastPacket(new MagicSkillUse(_owner, target, skill.getId(), skill.getLevel(), 0, 0));
						
						final L2SkillType type = skill.getSkillType();
						final ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
						final Creature[] targets =
						{
							target
						};
						
						if (type == L2SkillType.PARALYZE || type == L2SkillType.STUN || type == L2SkillType.ROOT || type == L2SkillType.AGGDAMAGE)
							useCubicDisabler(type, Cubic.this, skill, targets);
						else if (type == L2SkillType.MDAM)
							useCubicMdam(Cubic.this, skill, targets);
						else if (type == L2SkillType.POISON || type == L2SkillType.DEBUFF || type == L2SkillType.DOT)
							useCubicContinuous(Cubic.this, skill, targets);
						else if (type == L2SkillType.DRAIN)
							((L2SkillDrain) skill).useCubicSkill(Cubic.this, targets);
						else
							handler.useSkill(_owner, skill, targets);
					}
				}
			}
		}
	}
	
	public void useCubicContinuous(Cubic activeCubic, L2Skill skill, WorldObject[] targets)
	{
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			if (target.isDead())
				continue;
			
			if (skill.isOffensive())
			{
				final byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
				final boolean bss = activeCubic.getOwner().isChargedShot(ShotType.BLESSED_SPIRITSHOT);
				final boolean acted = Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld, bss);
				
				if (!acted)
				{
					activeCubic.getOwner().sendPacket(SystemMessageId.ATTACK_FAILED);
					continue;
				}
			}
			
			// If this is a debuff, let the duel manager know about it so the debuff can be removed after the duel (player & target must be in the same duel)
			if (target instanceof Player && ((Player) target).isInDuel() && skill.getSkillType() == L2SkillType.DEBUFF && activeCubic.getOwner().getDuelId() == ((Player) target).getDuelId())
			{
				for (L2Effect debuff : skill.getEffects(activeCubic.getOwner(), target))
				{
					if (debuff != null)
						DuelManager.getInstance().onBuff(((Player) target), debuff);
				}
			}
			else
				skill.getEffects(activeCubic, target, null);
		}
	}
	
	public void useCubicMdam(Cubic activeCubic, L2Skill skill, WorldObject[] targets)
	{
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			if (target.isAlikeDead())
			{
				if (target instanceof Player)
					target.stopFakeDeath(true);
				else
					continue;
			}
			
			final boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, skill));
			final byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
			final boolean bss = activeCubic.getOwner().isChargedShot(ShotType.BLESSED_SPIRITSHOT);
			
			int damage = (int) Formulas.calcMagicDam(activeCubic, target, skill, mcrit, shld);
			
			// If target is reflecting the skill then no damage is done Ignoring vengance-like reflections
			if ((Formulas.calcSkillReflect(target, skill) & Formulas.SKILL_REFLECT_SUCCEED) > 0)
				damage = 0;
			
			if (damage > 0)
			{
				// Manage cast break of the target (calculating rate, sending message...)
				Formulas.calcCastBreak(target, damage);
				
				activeCubic.getOwner().sendDamageMessage(target, damage, mcrit, false, false);
				
				if (skill.hasEffects())
				{
					// activate attacked effects, if any
					target.stopSkillEffects(skill.getId());
					
					if (target.getFirstEffect(skill) != null)
						target.removeEffect(target.getFirstEffect(skill));
					
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld, bss))
						skill.getEffects(activeCubic, target, null);
				}
				
				target.reduceCurrentHp(damage, activeCubic.getOwner(), skill);
			}
		}
	}
	
	public void useCubicDisabler(L2SkillType type, Cubic activeCubic, L2Skill skill, WorldObject[] targets)
	{
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			if (target.isDead())
				continue;
			
			final byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
			final boolean bss = activeCubic.getOwner().isChargedShot(ShotType.BLESSED_SPIRITSHOT);
			
			switch (type)
			{
				case STUN:
				case PARALYZE:
				case ROOT:
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld, bss))
					{
						// If this is a debuff, let the duel manager know about it so the debuff can be removed after the duel (player & target must be in the same duel)
						if (target instanceof Player && ((Player) target).isInDuel() && skill.getSkillType() == L2SkillType.DEBUFF && activeCubic.getOwner().getDuelId() == ((Player) target).getDuelId())
						{
							for (L2Effect debuff : skill.getEffects(activeCubic.getOwner(), target))
							{
								if (debuff != null)
									DuelManager.getInstance().onBuff(((Player) target), debuff);
							}
						}
						else
							skill.getEffects(activeCubic, target, null);
					}
					break;
				
				case CANCEL_DEBUFF:
					final L2Effect[] effects = target.getAllEffects();
					if (effects == null || effects.length == 0)
						break;
					
					int count = (skill.getMaxNegatedEffects() > 0) ? 0 : -2;
					for (L2Effect e : effects)
					{
						if (e.getSkill().isDebuff() && count < skill.getMaxNegatedEffects())
						{
							// Do not remove raid curse skills
							if (e.getSkill().getId() != 4215 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 4082)
							{
								e.exit();
								if (count > -1)
									count++;
							}
						}
					}
					break;
				
				case AGGDAMAGE:
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld, bss))
					{
						if (target instanceof Attackable)
							target.getAI().notifyEvent(AiEventType.AGGRESSION, activeCubic.getOwner(), (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
						
						skill.getEffects(activeCubic, target, null);
					}
					break;
			}
		}
	}
	
	/**
	 * @param owner
	 * @param target
	 * @return true if the target is inside of the owner's max Cubic range
	 */
	public boolean isInCubicRange(Creature owner, Creature target)
	{
		if (owner == null || target == null)
			return false;
		
		int x, y, z;
		int range = MAX_MAGIC_RANGE;
		
		x = (owner.getX() - target.getX());
		y = (owner.getY() - target.getY());
		z = (owner.getZ() - target.getZ());
		
		return ((x * x) + (y * y) + (z * z) <= (range * range));
	}
	
	/** this sets the friendly target for a cubic */
	public void cubicTargetForHeal()
	{
		Creature target = null;
		double percentleft = 100.0;
		Party party = _owner.getParty();
		
		// if owner is in a duel but not in a party duel, then it is the same as he does not have a party
		if (_owner.isInDuel())
			if (!DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
				party = null;
			
		if (party != null && !_owner.isInOlympiadMode())
		{
			// Get all Party Members in a spheric area near the Creature
			for (Creature partyMember : party.getMembers())
			{
				if (!partyMember.isDead())
				{
					// if party member not dead, check if he is in castrange of heal cubic
					if (isInCubicRange(_owner, partyMember))
					{
						// member is in cubic casting range, check if he need heal and if he have the lowest HP
						if (partyMember.getCurrentHp() < partyMember.getMaxHp())
						{
							if (percentleft > (partyMember.getCurrentHp() / partyMember.getMaxHp()))
							{
								percentleft = (partyMember.getCurrentHp() / partyMember.getMaxHp());
								target = partyMember;
							}
						}
					}
				}
				
				final Summon summon = partyMember.getSummon();
				if (summon != null)
				{
					if (summon.isDead())
						continue;
					
					// if party member's pet not dead, check if it is in castrange of heal cubic
					if (!isInCubicRange(_owner, summon))
						continue;
					
					// member's pet is in cubic casting range, check if he need heal and if he have the lowest HP
					if (summon.getCurrentHp() < summon.getMaxHp())
					{
						if (percentleft > (summon.getCurrentHp() / summon.getMaxHp()))
						{
							percentleft = (summon.getCurrentHp() / summon.getMaxHp());
							target = summon;
						}
					}
				}
			}
		}
		else
		{
			if (_owner.getCurrentHp() < _owner.getMaxHp())
			{
				percentleft = (_owner.getCurrentHp() / _owner.getMaxHp());
				target = _owner;
			}
			
			if (_owner.getSummon() != null && !_owner.getSummon().isDead() && _owner.getSummon().getCurrentHp() < _owner.getSummon().getMaxHp() && percentleft > (_owner.getSummon().getCurrentHp() / _owner.getSummon().getMaxHp()) && isInCubicRange(_owner, _owner.getSummon()))
				target = _owner.getSummon();
		}
		
		_target = target;
	}
	
	public boolean givenByOther()
	{
		return _givenByOther;
	}
	
	private class Heal implements Runnable
	{
		Heal()
		{
		}
		
		@Override
		public void run()
		{
			if (_owner.isDead() || !_owner.isOnline())
			{
				stopAction();
				_owner.delCubic(_id);
				_owner.broadcastUserInfo();
				cancelDisappear();
				return;
			}
			
			L2Skill skill = null;
			for (L2Skill sk : _skills)
			{
				if (sk.getId() == SKILL_CUBIC_HEAL)
				{
					skill = sk;
					break;
				}
			}
			
			if (skill != null)
			{
				cubicTargetForHeal();
				Creature target = _target;
				if (target != null && !target.isDead())
				{
					if (target.getMaxHp() - target.getCurrentHp() > skill.getPower())
					{
						final Creature[] targets =
						{
							target
						};
						
						final ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
						if (handler != null)
							handler.useSkill(_owner, skill, targets);
						else
							skill.useSkill(_owner, targets);
						
						MagicSkillUse msu = new MagicSkillUse(_owner, target, skill.getId(), skill.getLevel(), 0, 0);
						_owner.broadcastPacket(msu);
					}
				}
			}
		}
	}
	
	private class Disappear implements Runnable
	{
		Disappear()
		{
		}
		
		@Override
		public void run()
		{
			stopAction();
			_owner.delCubic(_id);
			_owner.broadcastUserInfo();
		}
	}
}
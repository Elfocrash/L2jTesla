package dev.l2j.tesla.gameserver.model.actor.ai.type;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.math.MathUtil;
import dev.l2j.tesla.commons.random.Rnd;
import dev.l2j.tesla.commons.util.ArraysUtil;

import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.ScriptEventType;
import dev.l2j.tesla.gameserver.enums.ZoneId;
import dev.l2j.tesla.gameserver.enums.skills.L2EffectType;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;
import dev.l2j.tesla.gameserver.model.actor.instance.Door;
import dev.l2j.tesla.gameserver.model.actor.instance.FestivalMonster;
import dev.l2j.tesla.gameserver.model.actor.instance.FriendlyMonster;
import dev.l2j.tesla.gameserver.model.actor.instance.Guard;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;
import dev.l2j.tesla.gameserver.model.actor.instance.RiftInvader;

public class AttackableAI extends CreatureAI implements Runnable
{
	protected static final int RANDOM_WALK_RATE = 30;
	protected static final int MAX_ATTACK_TIMEOUT = 90000; // 1m30
	
	private final Set<Creature> _seenCreatures = ConcurrentHashMap.newKeySet();
	
	/** The L2Attackable AI task executed every 1s (call onEvtThink method) */
	protected Future<?> _aiTask;
	
	/** The delay after wich the attacked is stopped */
	protected long _attackTimeout;
	
	/** The L2Attackable aggro counter */
	protected int _globalAggro;
	
	/** The flag used to indicate that a thinking action is in progress ; prevent recursive thinking */
	protected boolean _thinking;
	
	public AttackableAI(Attackable attackable)
	{
		super(attackable);
		
		_attackTimeout = Long.MAX_VALUE;
		_globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
		_seenCreatures.clear();
	}
	
	@Override
	public void run()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * @param target : The targeted Creature.
	 * @return true if the {@link Creature} used as target is autoattackable.
	 */
	protected boolean autoAttackCondition(Creature target)
	{
		// Check if the target isn't null, a Door or dead.
		if (target == null || target instanceof Door || target.isAlikeDead())
			return false;
		
		final Attackable me = getActiveChar();
		
		if (target instanceof Playable)
		{
			// Check if target is in the Aggro range
			if (!me.isInsideRadius(target, me.getTemplate().getAggroRange(), true, false))
				return false;
			
			// Check if the AI isn't a Raid Boss, can See Silent Moving players and the target isn't in silent move mode
			if (!(me.isRaidRelated()) && !(me.canSeeThroughSilentMove()) && ((Playable) target).isSilentMoving())
				return false;
			
			// Check if the target is a Player
			Player targetPlayer = target.getActingPlayer();
			if (targetPlayer != null)
			{
				// GM checks ; check if the target is invisible or got access level
				if (targetPlayer.isGM() && (targetPlayer.getAppearance().getInvisible() || !targetPlayer.getAccessLevel().canTakeAggro()))
					return false;
				
				// Check if player is an allied Varka.
				if (ArraysUtil.contains(me.getTemplate().getClans(), "varka_silenos_clan") && targetPlayer.isAlliedWithVarka())
					return false;
				
				// Check if player is an allied Ketra.
				if (ArraysUtil.contains(me.getTemplate().getClans(), "ketra_orc_clan") && targetPlayer.isAlliedWithKetra())
					return false;
				
				// check if the target is within the grace period for JUST getting up from fake death
				if (targetPlayer.isRecentFakeDeath())
					return false;
				
				if (me instanceof RiftInvader && targetPlayer.isInParty() && targetPlayer.getParty().isInDimensionalRift() && !targetPlayer.getParty().getDimensionalRift().isInCurrentRoomZone(me))
					return false;
			}
		}
		
		// Check if the actor is a Guard
		if (me instanceof Guard)
		{
			// Check if the Player target has karma (=PK)
			if (target instanceof Player && ((Player) target).getKarma() > 0)
				return GeoEngine.getInstance().canSeeTarget(me, target);
			
			// Check if the Monster target is aggressive
			if (target instanceof Monster && Config.GUARD_ATTACK_AGGRO_MOB)
				return (((Monster) target).isAggressive() && GeoEngine.getInstance().canSeeTarget(me, target));
			
			return false;
		}
		// The actor is a FriendlyMonster
		else if (me instanceof FriendlyMonster)
		{
			// Check if the Player target has karma (=PK)
			if (target instanceof Player && ((Player) target).getKarma() > 0)
				return GeoEngine.getInstance().canSeeTarget(me, target); // Los Check
				
			return false;
		}
		// The actor is a Npc
		else
		{
			if (target instanceof Attackable && me.isConfused())
				return GeoEngine.getInstance().canSeeTarget(me, target);
			
			if (target instanceof Npc)
				return false;
				
			// depending on config, do not allow mobs to attack _new_ players in peacezones,
			// unless they are already following those players from outside the peacezone.
			if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(ZoneId.PEACE))
				return false;
			
			// Check if the actor is Aggressive
			return (me.isAggressive() && GeoEngine.getInstance().canSeeTarget(me, target));
		}
	}
	
	@Override
	public void stopAITask()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
		super.stopAITask();
	}
	
	/**
	 * Set the Intention of this CreatureAI and create an AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, IDLE will be change in ACTIVE</B></FONT><BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 */
	@Override
	synchronized void changeIntention(IntentionType intention, Object arg0, Object arg1)
	{
		if (intention == IntentionType.IDLE || intention == IntentionType.ACTIVE)
		{
			// Check if actor is not dead
			Attackable npc = getActiveChar();
			if (!npc.isAlikeDead())
			{
				// If no players are around, set the Intention to ACTIVE
				if (!npc.getKnownType(Player.class).isEmpty())
					intention = IntentionType.ACTIVE;
				else
				{
					if (npc.getSpawn() != null)
					{
						final int range = Config.MAX_DRIFT_RANGE;
						if (!npc.isInsideRadius(npc.getSpawn().getLocX(), npc.getSpawn().getLocY(), npc.getSpawn().getLocZ(), range + range, true, false))
							intention = IntentionType.ACTIVE;
					}
				}
			}
			
			if (intention == IntentionType.IDLE)
			{
				// Set the Intention of this L2AttackableAI to IDLE
				super.changeIntention(IntentionType.IDLE, null, null);
				
				// Stop AI task and detach AI from NPC
				stopAITask();
				
				// Cancel the AI
				_actor.detachAI();
				return;
			}
		}
		
		// Set the Intention of this L2AttackableAI to intention
		super.changeIntention(intention, arg0, arg1);
		
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		if (_aiTask == null)
			_aiTask = ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	/**
	 * Manage the Attack Intention :
	 * <ul>
	 * <li>Stop current Attack (if necessary).</li>
	 * <li>Calculate attack timeout.</li>
	 * <li>Start a new Attack and Launch Think Event.</li>
	 * </ul>
	 * @param target The Creature to attack
	 */
	@Override
	protected void onIntentionAttack(Creature target)
	{
		// Calculate the attack timeout
		_attackTimeout = System.currentTimeMillis() + MAX_ATTACK_TIMEOUT;
		
		// Check buff.
		checkBuffAndSetBackTarget(target);
		
		// Manage the attack intention : stop current attack (if necessary), start a new attack and launch Think event.
		super.onIntentionAttack(target);
	}
	
	private void thinkCast()
	{
		if (checkTargetLost(getTarget()))
		{
			setTarget(null);
			return;
		}
		
		if (maybeMoveToPawn(getTarget(), _skill.getCastRange()))
			return;
		
		clientStopMoving(null);
		setIntention(IntentionType.ACTIVE);
		_actor.doCast(_skill);
	}
	
	/**
	 * Manage AI standard thinks of a L2Attackable (called by onEvtThink).
	 * <ul>
	 * <li>Update every 1s the _globalAggro counter to come close to 0</li>
	 * <li>If the actor is Aggressive and can attack, add all autoAttackable Creature in its Aggro Range to its _aggroList, chose a target and order to attack it</li>
	 * <li>If the actor is a Guard that can't attack, order to it to return to its home location</li>
	 * <li>If the actor is a Monster that can't attack, order to it to random walk (1/100)</li>
	 * </ul>
	 */
	protected void thinkActive()
	{
		final Attackable npc = getActiveChar();
		
		// Update every 1s the _globalAggro counter to come close to 0
		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
				_globalAggro++;
			else
				_globalAggro--;
		}
		
		// Add all autoAttackable Creature in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
		// A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
		if (_globalAggro >= 0)
		{
			final List<Quest> scripts = npc.getTemplate().getEventQuests(ScriptEventType.ON_CREATURE_SEE);
			
			// Get all visible objects inside its Aggro Range
			for (Creature target : npc.getKnownType(Creature.class))
			{
				// Check to see if this is a festival mob spawn. If it is, then check to see if the aggro trigger is a festival participant...if so, move to attack it.
				if (npc instanceof FestivalMonster && target instanceof Player)
				{
					if (!((Player) target).isFestivalParticipant())
						continue;
				}
				
				// ON_CREATURE_SEE implementation.
				if (scripts != null)
				{
					if (_seenCreatures.contains(target))
					{
						if (!npc.isInsideRadius(target, 400, true, false))
							_seenCreatures.remove(target);
					}
					else if (npc.isInsideRadius(target, 400, true, false))
					{
						_seenCreatures.add(target);
						
						for (Quest quest : scripts)
							quest.notifyCreatureSee(npc, target);
					}
				}
				
				// For each Creature check if the target is autoattackable
				if (autoAttackCondition(target)) // check aggression
				{
					// Add the attacker to the L2Attackable _aggroList
					if (npc.getHating(target) == 0)
						npc.addDamageHate(target, 0, 0);
				}
			}
			
			if (!npc.isCoreAIDisabled())
			{
				// Chose a target from its aggroList and order to attack the target
				final Creature hated = (Creature) ((npc.isConfused()) ? getTarget() : npc.getMostHated());
				if (hated != null)
				{
					// Get the hate level of the L2Attackable against this Creature target contained in _aggroList
					if (npc.getHating(hated) + _globalAggro > 0)
					{
						// Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
						npc.setRunning();
						
						// Set the AI Intention to ATTACK
						setIntention(IntentionType.ATTACK, hated);
					}
					return;
				}
			}
		}
		
		// If this is a festival monster, then it remains in the same location.
		if (npc instanceof FestivalMonster)
			return;
		
		// Check buffs.
		if (checkBuffAndSetBackTarget(_actor.getTarget()))
			return;
		
		// Minions following leader
		final Attackable master = npc.getMaster();
		if (master != null && !master.isAlikeDead())
		{
			if (!npc.isCastingNow())
			{
				final int offset = (int) (100 + npc.getCollisionRadius() + master.getCollisionRadius());
				final int minRadius = (int) (master.getCollisionRadius() + 30);
				
				if (master.isRunning())
					npc.setRunning();
				else
					npc.setWalking();
				
				if (npc.getPlanDistanceSq(master.getX(), master.getY()) > offset * offset)
				{
					int x1 = Rnd.get(minRadius * 2, offset * 2); // x
					int y1 = Rnd.get(x1, offset * 2); // distance
					
					y1 = (int) Math.sqrt(y1 * y1 - x1 * x1); // y
					
					if (x1 > offset + minRadius)
						x1 = master.getX() + x1 - offset;
					else
						x1 = master.getX() - x1 + minRadius;
					
					if (y1 > offset + minRadius)
						y1 = master.getY() + y1 - offset;
					else
						y1 = master.getY() - y1 + minRadius;
					
					// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
					moveTo(x1, y1, master.getZ());
					return;
				}
			}
		}
		else
		{
			// Return to home if too far.
			if (npc.returnHome())
				return;
			
			// Random walk otherwise.
			if (npc.getSpawn() != null && !npc.isNoRndWalk() && Rnd.get(RANDOM_WALK_RATE) == 0)
			{
				int x1 = npc.getSpawn().getLocX();
				int y1 = npc.getSpawn().getLocY();
				int z1 = npc.getSpawn().getLocZ();
				
				final int range = Config.MAX_DRIFT_RANGE;
				
				x1 = Rnd.get(range * 2); // x
				y1 = Rnd.get(x1, range * 2); // distance
				y1 = (int) Math.sqrt(y1 * y1 - x1 * x1); // y
				x1 += npc.getSpawn().getLocX() - range;
				y1 += npc.getSpawn().getLocY() - range;
				z1 = npc.getZ();
				
				// Move the actor to Location (x,y,z)
				moveTo(x1, y1, z1);
			}
		}
	}
	
	/**
	 * Manage AI attack thoughts of a L2Attackable (called by onEvtThink).
	 * <ul>
	 * <li>Update the attack timeout if actor is running.</li>
	 * <li>If target is dead or timeout is expired, stop this attack and set the Intention to ACTIVE.</li>
	 * <li>Call all WorldObject of its Faction inside the Faction Range.</li>
	 * <li>Choose a target and order to attack it with magic skill or physical attack.</li>
	 * </ul>
	 */
	protected void thinkAttack()
	{
		final Attackable npc = getActiveChar();
		if (npc.isCastingNow())
			return;
		
		// Pickup most hated character.
		Creature attackTarget = npc.getMostHated();
		
		// If target doesn't exist, is too far or if timeout is expired.
		if (attackTarget == null || _attackTimeout < System.currentTimeMillis() || MathUtil.calculateDistance(npc, attackTarget, true) > 2000)
		{
			// Stop hating this target after the attack timeout or if target is dead
			npc.stopHating(attackTarget);
			setIntention(IntentionType.ACTIVE);
			npc.setWalking();
			return;
		}
		
		// Corpse AIs, as AI scripts, are stopped here.
		if (npc.isCoreAIDisabled())
			return;
		
		setTarget(attackTarget);
		npc.setTarget(attackTarget);
		
		/**
		 * COMMON INFORMATIONS<br>
		 * Used for range and distance check.
		 */
		
		final int actorCollision = (int) npc.getCollisionRadius();
		final int combinedCollision = (int) (actorCollision + attackTarget.getCollisionRadius());
		final double dist = Math.sqrt(npc.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
		
		int range = combinedCollision;
		if (attackTarget.isMoving())
			range += 15;
		
		if (npc.isMoving())
			range += 15;
		
		/**
		 * CAST CHECK<br>
		 * The mob succeeds a skill check ; make all possible checks to define the skill to launch. If nothing is found, go in MELEE CHECK.<br>
		 * It will check skills arrays in that order :
		 * <ul>
		 * <li>suicide skill at 15% max HPs</li>
		 * <li>buff skill if such effect isn't existing</li>
		 * <li>heal skill if self or ally is under 75% HPs (priority to others healers and mages)</li>
		 * <li>debuff skill if such effect isn't existing</li>
		 * <li>damage skill, in that order : short range and long range</li>
		 * </ul>
		 */
		
		if (willCastASpell())
		{
			// This list is used in order to avoid multiple calls on skills lists. Tests are made one after the other, and content is replaced when needed.
			List<L2Skill> defaultList;
			
			// -------------------------------------------------------------------------------
			// Suicide possibility if HPs are < 15%.
			defaultList = npc.getTemplate().getSkills(NpcTemplate.SkillType.SUICIDE);
			if (!defaultList.isEmpty() && (npc.getCurrentHp() / npc.getMaxHp() < 0.15))
			{
				final L2Skill skill = Rnd.get(defaultList);
				if (cast(skill, dist, range + skill.getSkillRadius()))
					return;
			}
			
			// -------------------------------------------------------------------------------
			// Heal
			defaultList = npc.getTemplate().getSkills(NpcTemplate.SkillType.HEAL);
			if (!defaultList.isEmpty())
			{
				// First priority is to heal the master.
				final Attackable master = npc.getMaster();
				if (master != null && !master.isDead() && (master.getCurrentHp() / master.getMaxHp() < 0.75))
				{
					for (L2Skill sk : defaultList)
					{
						if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
							continue;
						
						if (!checkSkillCastConditions(sk))
							continue;
						
						final int overallRange = (int) (sk.getCastRange() + actorCollision + master.getCollisionRadius());
						if (!MathUtil.checkIfInRange(overallRange, npc, master, false) && sk.getTargetType() != L2Skill.SkillTargetType.TARGET_PARTY && !npc.isMovementDisabled())
						{
							moveToPawn(master, overallRange);
							return;
						}
						
						if (GeoEngine.getInstance().canSeeTarget(npc, master))
						{
							clientStopMoving(null);
							npc.setTarget(master);
							npc.doCast(sk);
							return;
						}
					}
				}
				
				// Second priority is to heal himself.
				if (npc.getCurrentHp() / npc.getMaxHp() < 0.75)
				{
					for (L2Skill sk : defaultList)
					{
						if (!checkSkillCastConditions(sk))
							continue;
						
						clientStopMoving(null);
						npc.setTarget(npc);
						npc.doCast(sk);
						return;
					}
				}
				
				for (L2Skill sk : defaultList)
				{
					if (!checkSkillCastConditions(sk))
						continue;
					
					if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
					{
						final String[] actorClans = npc.getTemplate().getClans();
						for (Attackable obj : npc.getKnownTypeInRadius(Attackable.class, sk.getCastRange() + actorCollision))
						{
							if (obj.isDead())
								continue;
							
							if (!ArraysUtil.contains(actorClans, obj.getTemplate().getClans()))
								continue;
							
							if (obj.getCurrentHp() / obj.getMaxHp() < 0.75)
							{
								if (GeoEngine.getInstance().canSeeTarget(npc, obj))
								{
									clientStopMoving(null);
									npc.setTarget(obj);
									npc.doCast(sk);
									return;
								}
							}
						}
						
						if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY)
						{
							clientStopMoving(null);
							npc.doCast(sk);
							return;
						}
					}
				}
			}
			
			// -------------------------------------------------------------------------------
			// Buff
			defaultList = npc.getTemplate().getSkills(NpcTemplate.SkillType.BUFF);
			if (!defaultList.isEmpty())
			{
				for (L2Skill sk : defaultList)
				{
					if (!checkSkillCastConditions(sk))
						continue;
					
					if (npc.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						
						npc.setTarget(npc);
						npc.doCast(sk);
						npc.setTarget(attackTarget);
						return;
					}
				}
			}
			
			// -------------------------------------------------------------------------------
			// Debuff - 10% luck to get debuffed.
			defaultList = npc.getTemplate().getSkills(NpcTemplate.SkillType.DEBUFF);
			if (Rnd.get(100) < 10 && !defaultList.isEmpty())
			{
				for (L2Skill sk : defaultList)
				{
					if (!checkSkillCastConditions(sk) || (sk.getCastRange() + npc.getCollisionRadius() + attackTarget.getCollisionRadius() <= dist && !canAura(sk)))
						continue;
					
					if (!GeoEngine.getInstance().canSeeTarget(npc, attackTarget))
						continue;
					
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						npc.doCast(sk);
						return;
					}
				}
			}
			
			// -------------------------------------------------------------------------------
			// General attack skill - short range is checked, then long range.
			defaultList = npc.getTemplate().getSkills(NpcTemplate.SkillType.SHORT_RANGE);
			if (!defaultList.isEmpty() && dist <= 150)
			{
				final L2Skill skill = Rnd.get(defaultList);
				if (cast(skill, dist, skill.getCastRange()))
					return;
			}
			else
			{
				defaultList = npc.getTemplate().getSkills(NpcTemplate.SkillType.LONG_RANGE);
				if (!defaultList.isEmpty() && dist > 150)
				{
					final L2Skill skill = Rnd.get(defaultList);
					if (cast(skill, dist, skill.getCastRange()))
						return;
				}
			}
		}
		
		/**
		 * MELEE CHECK<br>
		 * The mob failed a skill check ; make him flee if AI authorizes it, else melee attack.
		 */
		
		// The range takes now in consideration physical attack range.
		range += npc.getPhysicalAttackRange();
		
		if (npc.isMovementDisabled())
		{
			// If distance is too big, choose another target.
			if (dist > range)
				attackTarget = targetReconsider(range, true);
			
			// Any AI type, even healer or mage, will try to melee attack if it can't do anything else (desesperate situation).
			if (attackTarget != null)
				_actor.doAttack(attackTarget);
			
			return;
		}
		
		/**
		 * MOVE AROUND CHECK<br>
		 * In case many mobs are trying to hit from same place, move a bit, circling around the target
		 */
		
		if (Rnd.get(100) <= 3)
		{
			for (Attackable nearby : npc.getKnownTypeInRadius(Attackable.class, actorCollision))
			{
				if (nearby != attackTarget)
				{
					int newX = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean())
						newX = attackTarget.getX() + newX;
					else
						newX = attackTarget.getX() - newX;
					
					int newY = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean())
						newY = attackTarget.getY() + newY;
					else
						newY = attackTarget.getY() - newY;
					
					if (!npc.isInsideRadius(newX, newY, actorCollision, false))
					{
						int newZ = npc.getZ() + 30;
						if (GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), newX, newY, newZ))
							moveTo(newX, newY, newZ);
					}
					return;
				}
			}
		}
		
		/**
		 * FLEE CHECK<br>
		 * Test the flee possibility. Archers got 25% chance to flee.
		 */
		
		if (npc.getTemplate().getAiType() == NpcTemplate.AIType.ARCHER && dist <= (60 + combinedCollision) && Rnd.get(4) > 1)
		{
			final int posX = npc.getX() + ((attackTarget.getX() < npc.getX()) ? 300 : -300);
			final int posY = npc.getY() + ((attackTarget.getY() < npc.getY()) ? 300 : -300);
			final int posZ = npc.getZ() + 30;
			
			if (GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), posX, posY, posZ))
			{
				setIntention(IntentionType.MOVE_TO, new Location(posX, posY, posZ));
				return;
			}
		}
		
		/**
		 * BASIC MELEE ATTACK
		 */
		
		if (dist > range || !GeoEngine.getInstance().canSeeTarget(npc, attackTarget))
		{
			if (attackTarget.isMoving())
				range -= 30;
			
			if (range < 5)
				range = 5;
			
			moveToPawn(attackTarget, range);
			return;
		}
		
		_actor.doAttack((Creature) getTarget());
	}
	
	protected boolean cast(L2Skill sk, double distance, int range)
	{
		if (sk == null)
			return false;
		
		final Attackable caster = getActiveChar();
		
		if (caster.isCastingNow() && !sk.isSimultaneousCast())
			return false;
		
		if (!checkSkillCastConditions(sk))
			return false;
		
		Creature attackTarget = (Creature) getTarget();
		if (attackTarget == null)
			return false;
		
		switch (sk.getSkillType())
		{
			case BUFF:
			{
				if (caster.getFirstEffect(sk) == null)
				{
					clientStopMoving(null);
					caster.setTarget(caster);
					caster.doCast(sk);
					return true;
				}
				
				// ----------------------------------------
				// If actor already have buff, start looking at others same faction mob to cast
				if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
					return false;
				
				if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
				{
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						WorldObject targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				
				if (canParty(sk))
				{
					clientStopMoving(null);
					WorldObject targets = attackTarget;
					caster.setTarget(caster);
					caster.doCast(sk);
					caster.setTarget(targets);
					return true;
				}
				break;
			}
			
			case HEAL:
			case HOT:
			case HEAL_PERCENT:
			case HEAL_STATIC:
			case BALANCE_LIFE:
			{
				// Minion case.
				if (sk.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF)
				{
					final Attackable master = caster.getMaster();
					if (master != null && !master.isDead() && Rnd.get(100) > (master.getCurrentHp() / master.getMaxHp() * 100))
					{
						final int overallRange = (int) (sk.getCastRange() + caster.getCollisionRadius() + master.getCollisionRadius());
						if (!MathUtil.checkIfInRange(overallRange, caster, master, false) && sk.getTargetType() != L2Skill.SkillTargetType.TARGET_PARTY && !caster.isMovementDisabled())
							moveToPawn(master, overallRange);
						
						if (GeoEngine.getInstance().canSeeTarget(caster, master))
						{
							clientStopMoving(null);
							caster.setTarget(master);
							caster.doCast(sk);
							return true;
						}
					}
				}
				
				// Personal case.
				double percentage = caster.getCurrentHp() / caster.getMaxHp() * 100;
				if (Rnd.get(100) < (100 - percentage) / 3)
				{
					clientStopMoving(null);
					caster.setTarget(caster);
					caster.doCast(sk);
					return true;
				}
				
				if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
				{
					for (Attackable obj : caster.getKnownTypeInRadius(Attackable.class, (int) (sk.getCastRange() + caster.getCollisionRadius())))
					{
						if (obj.isDead())
							continue;
						
						if (!ArraysUtil.contains(caster.getTemplate().getClans(), obj.getTemplate().getClans()))
							continue;
						
						percentage = obj.getCurrentHp() / obj.getMaxHp() * 100;
						if (Rnd.get(100) < (100 - percentage) / 10)
						{
							if (GeoEngine.getInstance().canSeeTarget(caster, obj))
							{
								clientStopMoving(null);
								caster.setTarget(obj);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				
				if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY)
				{
					for (Attackable obj : caster.getKnownTypeInRadius(Attackable.class, (int) (sk.getSkillRadius() + caster.getCollisionRadius())))
					{
						if (!ArraysUtil.contains(caster.getTemplate().getClans(), obj.getTemplate().getClans()))
							continue;
						
						if (obj.getCurrentHp() < obj.getMaxHp() && Rnd.get(100) <= 20)
						{
							clientStopMoving(null);
							caster.setTarget(caster);
							caster.doCast(sk);
							return true;
						}
					}
				}
				break;
			}
			
			case DEBUFF:
			case POISON:
			case DOT:
			case MDOT:
			case BLEED:
			{
				if (GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && !attackTarget.isDead() && distance <= range)
				{
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					if ((sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
				{
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			
			case SLEEP:
			{
				if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
				{
					if (!attackTarget.isDead() && distance <= range)
					{
						if (distance > range || attackTarget.isMoving())
						{
							if (attackTarget.getFirstEffect(sk) == null)
							{
								clientStopMoving(null);
								caster.doCast(sk);
								return true;
							}
						}
					}
					
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					if ((sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			
			case ROOT:
			case STUN:
			case PARALYZE:
			{
				if (GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && distance <= range)
				{
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					else if ((sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
				{
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			
			case MUTE:
			case FEAR:
			{
				if (GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && distance <= range)
				{
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					if ((sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
				{
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			
			case CANCEL:
			case NEGATE:
			{
				// decrease cancel probability
				if (Rnd.get(50) != 0)
					return true;
				
				if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
				{
					if (attackTarget.getFirstEffect(L2EffectType.BUFF) != null && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						WorldObject targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if ((sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AURA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					else if ((sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			
			default:
			{
				if (!canAura(sk))
				{
					if (GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						WorldObject targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				else
				{
					clientStopMoving(null);
					caster.doCast(sk);
					return true;
				}
			}
				break;
		}
		
		return false;
	}
	
	/**
	 * @param skill the skill to check.
	 * @return {@code true} if the skill is available for casting {@code false} otherwise.
	 */
	protected boolean checkSkillCastConditions(L2Skill skill)
	{
		// Not enough MP.
		if (skill.getMpConsume() >= getActiveChar().getCurrentMp())
			return false;
		
		// Character is in "skill disabled" mode.
		if (getActiveChar().isSkillDisabled(skill))
			return false;
		
		// Is a magic skill and character is magically muted or is a physical skill and character is physically muted.
		if ((skill.isMagic() && getActiveChar().isMuted()) || getActiveChar().isPhysicalMuted())
			return false;
		
		return true;
	}
	
	/**
	 * This method checks if the actor will cast a skill or not.
	 * @return true if the actor will cast a spell, false otherwise.
	 */
	protected boolean willCastASpell()
	{
		switch (getActiveChar().getTemplate().getAiType())
		{
			case HEALER:
			case MAGE:
				return !getActiveChar().isMuted();
			
			default:
				if (getActiveChar().isPhysicalMuted())
					return false;
		}
		return Rnd.get(100) < 10;
	}
	
	/**
	 * Method used when the actor can't attack his current target (immobilize state, for exemple).
	 * <ul>
	 * <li>If the actor got an hate list, pickup a new target from it.</li>
	 * <li>If the actor didn't find a target on his hate list, check if he is aggro type and pickup a new target using his knownlist.</li>
	 * </ul>
	 * @param range The range to check (skill range for skill ; physical range for melee).
	 * @param rangeCheck That boolean is used to see if a check based on the distance must be made (skill check).
	 * @return The new Creature victim.
	 */
	protected Creature targetReconsider(int range, boolean rangeCheck)
	{
		final Attackable actor = getActiveChar();
		
		// Verify first if aggro list is empty, if not search a victim following his aggro position.
		if (!actor.getAggroList().isEmpty())
		{
			// Store aggro value && most hated, in order to add it to the random target we will choose.
			final Creature previousMostHated = actor.getMostHated();
			final int aggroMostHated = actor.getHating(previousMostHated);
			
			for (Creature obj : actor.getHateList())
			{
				if (!autoAttackCondition(obj))
					continue;
				
				if (rangeCheck)
				{
					// Verify the distance, -15 if the victim is moving, -15 if the npc is moving.
					double dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY())) - obj.getCollisionRadius();
					if (actor.isMoving())
						dist -= 15;
					
					if (obj.isMoving())
						dist -= 15;
					
					if (dist > range)
						continue;
				}
				
				// Stop to hate the most hated.
				actor.stopHating(previousMostHated);
				
				// Add previous most hated aggro to that new victim.
				actor.addDamageHate(obj, 0, (aggroMostHated > 0) ? aggroMostHated : 2000);
				return obj;
			}
		}
		
		// If hate list gave nothing, then verify first if the actor is aggressive, and then pickup a victim from his knownlist.
		if (actor.isAggressive())
		{
			for (Creature target : actor.getKnownTypeInRadius(Creature.class, actor.getTemplate().getAggroRange()))
			{
				if (!autoAttackCondition(target))
					continue;
				
				if (rangeCheck)
				{
					// Verify the distance, -15 if the victim is moving, -15 if the npc is moving.
					double dist = Math.sqrt(actor.getPlanDistanceSq(target.getX(), target.getY())) - target.getCollisionRadius();
					if (actor.isMoving())
						dist -= 15;
					
					if (target.isMoving())
						dist -= 15;
					
					if (dist > range)
						continue;
				}
				
				// Only 1 aggro, as the hate list is supposed to be cleaned. Simulate an aggro range entrance.
				actor.addDamageHate(target, 0, 1);
				return target;
			}
		}
		
		// Return null if no new victim has been found.
		return null;
	}
	
	/**
	 * Method used for chaotic mode (RBs / GBs and their minions).
	 */
	public void aggroReconsider()
	{
		final Attackable actor = getActiveChar();
		
		// Don't bother with aggro lists lower or equal to 1.
		if (actor.getHateList().size() <= 1)
			return;
		
		// Choose a new victim, and make checks to see if it fits.
		final Creature mostHated = actor.getMostHated();
		final Creature victim = Rnd.get(actor.getHateList().stream().filter(v -> autoAttackCondition(v)).collect(Collectors.toList()));
		
		if (victim != null && mostHated != victim)
		{
			// Add most hated aggro to the victim aggro.
			actor.addDamageHate(victim, 0, actor.getHating(mostHated));
			setIntention(IntentionType.ATTACK, victim);
		}
	}
	
	/**
	 * Manage AI thinking actions of a L2Attackable.
	 */
	@Override
	protected void onEvtThink()
	{
		// Check if the thinking action is already in progress.
		if (_thinking || _actor.isAllSkillsDisabled())
			return;
		
		// Start thinking action.
		_thinking = true;
		
		try
		{
			// Manage AI thoughts.
			switch (_desire.getIntention())
			{
				case ACTIVE:
					thinkActive();
					break;
				case ATTACK:
					thinkAttack();
					break;
				case CAST:
					thinkCast();
					break;
			}
		}
		finally
		{
			// Stop thinking action.
			_thinking = false;
		}
	}
	
	/**
	 * Launch actions corresponding to the Event Attacked.
	 * <ul>
	 * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList</li>
	 * <li>Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player</li>
	 * <li>Set the Intention to ATTACK</li>
	 * </ul>
	 * @param attacker The Creature that attacks the actor
	 */
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
		final Attackable me = getActiveChar();
		
		// Calculate the attack timeout
		_attackTimeout = System.currentTimeMillis() + MAX_ATTACK_TIMEOUT;
		
		// Set the _globalAggro to 0 to permit attack even just after spawn
		if (_globalAggro < 0)
			_globalAggro = 0;
		
		// Add the attacker to the _aggroList of the actor
		me.addDamageHate(attacker, 0, 1);
		
		// Set the Intention to ATTACK and make the character running, but only if the AI isn't disabled.
		if (!me.isCoreAIDisabled() && (_desire.getIntention() != IntentionType.ATTACK || me.getMostHated() != getTarget()))
		{
			me.setRunning();
			
			setIntention(IntentionType.ATTACK, attacker);
		}
		
		if (me instanceof Monster)
		{
			Monster master = (Monster) me;
			
			if (master.hasMinions())
				master.getMinionList().onAssist(me, attacker);
			else
			{
				master = master.getMaster();
				if (master != null && master.hasMinions())
					master.getMinionList().onAssist(me, attacker);
			}
		}
		
		if (attacker != null)
		{
			// Faction check.
			final String[] actorClans = me.getTemplate().getClans();
			if (actorClans != null && me.getAttackByList().contains(attacker))
			{
				for (Attackable called : me.getKnownTypeInRadius(Attackable.class, me.getTemplate().getClanRange()))
				{
					// Caller hasn't AI or is dead.
					if (!called.hasAI() || called.isDead())
						continue;
					
					// Caller clan doesn't correspond to the called clan.
					if (!ArraysUtil.contains(actorClans, called.getTemplate().getClans()))
						continue;
					
					// Called mob doesnt care about that type of caller id (the bitch !).
					if (ArraysUtil.contains(called.getTemplate().getIgnoredIds(), me.getNpcId()))
						continue;
					
					// Check if the WorldObject is inside the Faction Range of the actor
					final IntentionType calledIntention = called.getAI().getDesire().getIntention();
					if ((calledIntention == IntentionType.IDLE || calledIntention == IntentionType.ACTIVE || (calledIntention == IntentionType.MOVE_TO && !called.isRunning())) && GeoEngine.getInstance().canSeeTarget(me, called))
					{
						if (attacker instanceof Playable)
						{
							final List<Quest> scripts = called.getTemplate().getEventQuests(ScriptEventType.ON_FACTION_CALL);
							if (scripts != null)
							{
								final Player player = attacker.getActingPlayer();
								final boolean isSummon = attacker instanceof Summon;
								
								for (Quest quest : scripts)
									quest.notifyFactionCall(called, me, player, isSummon);
							}
						}
						else
						{
							called.addDamageHate(attacker, 0, me.getHating(attacker));
							called.getAI().setIntention(IntentionType.ATTACK, attacker);
						}
					}
				}
			}
		}
		
		super.onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Aggression.
	 * <ul>
	 * <li>Add the target to the actor _aggroList or update hate if already present</li>
	 * <li>Set the actor Intention to ATTACK (if actor is Guard check if it isn't too far from its home location)</li>
	 * </ul>
	 * @param target The Creature that attacks
	 * @param aggro The value of hate to add to the actor against the target
	 */
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		final Attackable me = getActiveChar();
		
		// Add the target to the actor _aggroList or update hate if already present
		me.addDamageHate(target, 0, aggro);
		
		// Set the Intention to ATTACK and make the character running, but only if the AI isn't disabled.
		if (!me.isCoreAIDisabled() && _desire.getIntention() != IntentionType.ATTACK)
		{
			me.setRunning();
			
			setIntention(IntentionType.ATTACK, target);
		}
		
		if (me instanceof Monster)
		{
			Monster master = (Monster) me;
			
			if (master.hasMinions())
				master.getMinionList().onAssist(me, target);
			else
			{
				master = master.getMaster();
				if (master != null && master.hasMinions())
					master.getMinionList().onAssist(me, target);
			}
		}
		
		if (target == null)
			return;
		
		// Faction check.
		final String[] actorClans = me.getTemplate().getClans();
		if (actorClans != null && me.getAttackByList().contains(target))
		{
			for (Attackable called : me.getKnownTypeInRadius(Attackable.class, me.getTemplate().getClanRange()))
			{
				// Caller hasn't AI or is dead.
				if (!called.hasAI() || called.isDead())
					continue;
				
				// Caller clan doesn't correspond to the called clan.
				if (!ArraysUtil.contains(actorClans, called.getTemplate().getClans()))
					continue;
				
				// Called mob doesnt care about that type of caller id (the bitch !).
				if (ArraysUtil.contains(called.getTemplate().getIgnoredIds(), me.getNpcId()))
					continue;
				
				// Check if the WorldObject is inside the Faction Range of the actor
				final IntentionType calledIntention = called.getAI().getDesire().getIntention();
				if ((calledIntention == IntentionType.IDLE || calledIntention == IntentionType.ACTIVE || (calledIntention == IntentionType.MOVE_TO && !called.isRunning())) && GeoEngine.getInstance().canSeeTarget(me, called))
				{
					if (target instanceof Playable)
					{
						final List<Quest> scripts = called.getTemplate().getEventQuests(ScriptEventType.ON_FACTION_CALL);
						if (scripts != null)
						{
							final Player player = target.getActingPlayer();
							final boolean isSummon = target instanceof Summon;
							
							for (Quest quest : scripts)
								quest.notifyFactionCall(called, me, player, isSummon);
						}
					}
					else
					{
						called.addDamageHate(target, 0, me.getHating(target));
						called.getAI().setIntention(IntentionType.ATTACK, target);
					}
				}
			}
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		// Cancel attack timeout
		_attackTimeout = Long.MAX_VALUE;
		
		super.onIntentionActive();
	}
	
	public void setGlobalAggro(int value)
	{
		_globalAggro = value;
	}
	
	private Attackable getActiveChar()
	{
		return (Attackable) _actor;
	}
	
	private boolean checkBuffAndSetBackTarget(WorldObject target)
	{
		if (Rnd.get(RANDOM_WALK_RATE) != 0)
			return false;
		
		for (L2Skill sk : getActiveChar().getTemplate().getSkills(NpcTemplate.SkillType.BUFF))
		{
			if (getActiveChar().getFirstEffect(sk) != null)
				continue;
			
			clientStopMoving(null);
			
			_actor.setTarget(_actor);
			_actor.doCast(sk);
			_actor.setTarget(target);
			return true;
		}
		return false;
	}
}
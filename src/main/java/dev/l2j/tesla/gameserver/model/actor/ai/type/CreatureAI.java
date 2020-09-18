package dev.l2j.tesla.gameserver.model.actor.ai.type;

import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.model.L2Effect;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.ai.Desire;
import dev.l2j.tesla.gameserver.model.actor.instance.Door;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.model.location.SpawnLocation;
import dev.l2j.tesla.commons.util.ArraysUtil;

import dev.l2j.tesla.gameserver.enums.AiEventType;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.skills.L2SkillType;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance.ItemLocation;

public class CreatureAI extends AbstractAI
{
	public CreatureAI(Creature character)
	{
		super(character);
	}
	
	public Desire getNextIntention()
	{
		return null;
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
	}
	
	@Override
	protected void onIntentionIdle()
	{
		// Set the AI Intention to IDLE
		changeIntention(IntentionType.IDLE, null, null);
		
		// Init cast and attack target
		setTarget(null);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
	}
	
	@Override
	protected void onIntentionActive()
	{
		// Check if the Intention is not already Active
		if (_desire.getIntention() != IntentionType.ACTIVE)
		{
			// Set the AI Intention to ACTIVE
			changeIntention(IntentionType.ACTIVE, null, null);
			
			// Init cast and attack target
			setTarget(null);
			
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);
			
			// Also enable random animations for this Creature if allowed
			// This is only for mobs - town npcs are handled in their constructor
			if (_actor instanceof Attackable)
				((Npc) _actor).startRandomAnimationTimer();
			
			// Launch the Think Event
			onEvtThink();
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
		setIntention(IntentionType.IDLE);
	}
	
	@Override
	protected void onIntentionAttack(Creature target)
	{
		if (target == null)
		{
			clientActionFailed();
			return;
		}
		
		if (_desire.getIntention() == IntentionType.REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAfraid())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		// Check if the Intention is already ATTACK
		if (_desire.getIntention() == IntentionType.ATTACK)
		{
			// Check if the AI already targets the Creature
			if (getTarget() != target)
			{
				// Set the AI attack target (change target)
				setTarget(target);
				
				stopFollow();
				
				// Launch the Think Event
				notifyEvent(AiEventType.THINK, null);
			}
			else
			{
				clientActionFailed(); // else client freezes until cancel target
				
				if (getActor() instanceof Playable && getActor().isAttackingNow() && !target.isAutoAttackable(getActor()))
					changeIntention(IntentionType.IDLE, null, null);
			}
		}
		else
		{
			// Set the Intention of this AbstractAI to ATTACK
			changeIntention(IntentionType.ATTACK, target, null);
			
			// Set the AI attack target
			setTarget(target);
			
			stopFollow();
			
			// Launch the Think Event
			notifyEvent(AiEventType.THINK, null);
		}
	}
	
	@Override
	protected void onIntentionCast(L2Skill skill, WorldObject target)
	{
		if (_desire.getIntention() == IntentionType.REST && skill.isMagic())
		{
			clientActionFailed();
			_actor.setIsCastingNow(false);
			return;
		}
		
		// Set the AI cast target
		setTarget(target);
		
		// Set the AI skill used by INTENTION_CAST
		_skill = skill;
		
		// Change the Intention of this AbstractAI to CAST
		changeIntention(IntentionType.CAST, skill, target);
		
		// Launch the Think Event
		notifyEvent(AiEventType.THINK, null);
	}
	
	@Override
	protected void onIntentionMoveTo(Location loc)
	{
		if (_desire.getIntention() == IntentionType.REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		// Set the Intention of this AbstractAI to MOVE_TO
		changeIntention(IntentionType.MOVE_TO, loc, null);
		
		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)
		moveTo(loc.getX(), loc.getY(), loc.getZ());
	}
	
	@Override
	protected void onIntentionFollow(Creature target)
	{
		if (_desire.getIntention() == IntentionType.REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isMovementDisabled())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}
		
		// Dead actors can`t follow
		if (_actor.isDead())
		{
			clientActionFailed();
			return;
		}
		
		// do not follow yourself
		if (_actor == target)
		{
			clientActionFailed();
			return;
		}
		
		// Set the Intention of this AbstractAI to FOLLOW
		changeIntention(IntentionType.FOLLOW, target, null);
		
		// Create and Launch an AI Follow Task to execute every 1s
		startFollow(target);
	}
	
	@Override
	protected void onIntentionPickUp(WorldObject object)
	{
		// Actor is resting, return.
		if (_desire.getIntention() == IntentionType.REST)
		{
			clientActionFailed();
			return;
		}
		
		// Actor is currently busy casting, return.
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttackingNow())
		{
			clientActionFailed();
			return;
		}
		
		if (object instanceof ItemInstance && ((ItemInstance) object).getLocation() != ItemLocation.VOID)
			return;
		
		// Set the Intention of this AbstractAI to PICK_UP
		changeIntention(IntentionType.PICK_UP, object, null);
		
		// Set the AI pick up target
		setTarget(object);
		
		// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
		moveToPawn(object, 20);
	}
	
	@Override
	protected void onIntentionInteract(WorldObject object)
	{
	}
	
	@Override
	protected void onEvtThink()
	{
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
	}
	
	@Override
	protected void onEvtStunned(Creature attacker)
	{
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtParalyzed(Creature attacker)
	{
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtSleeping(Creature attacker)
	{
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
	}
	
	@Override
	protected void onEvtRooted(Creature attacker)
	{
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtConfused(Creature attacker)
	{
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtMuted(Creature attacker)
	{
		// Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtEvaded(Creature attacker)
	{
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	@Override
	protected void onEvtArrived()
	{
		_actor.revalidateZone(true);
		
		if (_actor.moveToNextRoutePoint())
			return;
		
		if (_actor instanceof Attackable)
			((Attackable) _actor).setIsReturningToSpawnPoint(false);
		
		clientStoppedMoving();
		
		// If the Intention was MOVE_TO, set the Intention to ACTIVE
		if (_desire.getIntention() == IntentionType.MOVE_TO)
			setIntention(IntentionType.ACTIVE);
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	@Override
	protected void onEvtArrivedBlocked(SpawnLocation loc)
	{
		// If the Intention was MOVE_TO, set the Intention to ACTIVE
		if (_desire.getIntention() == IntentionType.MOVE_TO || _desire.getIntention() == IntentionType.CAST)
			setIntention(IntentionType.ACTIVE);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(loc);
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	@Override
	protected void onEvtCancel()
	{
		_actor.abortCast();
		
		// Stop an AI Follow Task
		stopFollow();
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	@Override
	protected void onEvtDead()
	{
		// Stop an AI Tasks
		stopAITask();
		
		// Kill the actor client side.
		clientNotifyDead();
		
		if (!(_actor instanceof Playable))
			_actor.setWalking();
	}
	
	@Override
	protected void onEvtFakeDeath()
	{
		// Stop an AI Follow Task
		stopFollow();
		
		// Stop the actor movement and send Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Init AI
		_desire.update(IntentionType.IDLE, null, null);
		setTarget(null);
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
	}
	
	protected boolean maybeMoveToPosition(Location worldPosition, int offset)
	{
		if (worldPosition == null)
			return false;
		
		if (offset < 0)
			return false; // skill radius -1
			
		if (!_actor.isInsideRadius(worldPosition.getX(), worldPosition.getY(), (int) (offset + _actor.getCollisionRadius()), false))
		{
			if (_actor.isMovementDisabled())
				return true;
			
			if (!(this instanceof PlayerAI) && !(this instanceof SummonAI))
				_actor.setRunning();
			
			stopFollow();
			
			int x = _actor.getX();
			int y = _actor.getY();
			
			double dx = worldPosition.getX() - x;
			double dy = worldPosition.getY() - y;
			
			double dist = Math.sqrt(dx * dx + dy * dy);
			
			double sin = dy / dist;
			double cos = dx / dist;
			
			dist -= offset - 5;
			
			x += (int) (dist * cos);
			y += (int) (dist * sin);
			
			moveTo(x, y, worldPosition.getZ());
			return true;
		}
		
		if (getFollowTarget() != null)
			stopFollow();
		
		return false;
	}
	
	/**
	 * Manage the Move to Pawn action in function of the distance and of the Interact area.
	 * <ul>
	 * <li>Get the distance between the current position of the Creature and the target (x,y)</li>
	 * <li>If the distance > offset+20, move the actor (by running) to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li>
	 * <li>If the distance <= offset+20, Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * </ul>
	 * @param target The targeted WorldObject
	 * @param offset The Interact area radius
	 * @return True if a movement must be done
	 */
	protected boolean maybeMoveToPawn(WorldObject target, int offset)
	{
		if (target == null || offset < 0) // skill radius -1
			return false;
		
		offset += _actor.getCollisionRadius();
		if (target instanceof Creature)
			offset += ((Creature) target).getCollisionRadius();
		
		if (!_actor.isInsideRadius(target, offset, false, false))
		{
			if (getFollowTarget() != null)
			{
				// allow larger hit range when the target is moving (check is run only once per second)
				if (!_actor.isInsideRadius(target, offset + 100, false, false))
					return true;
				
				stopFollow();
				return false;
			}
			
			if (_actor.isMovementDisabled())
			{
				if (_desire.getIntention() == IntentionType.ATTACK)
				{
					setIntention(IntentionType.IDLE);
					clientActionFailed();
				}
				
				return true;
			}
			
			// If not running, set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
			if (!(this instanceof PlayerAI) && !(this instanceof SummonAI))
				_actor.setRunning();
			
			stopFollow();
			
			if (target instanceof Creature && !(target instanceof Door))
			{
				if (((Creature) target).isMoving())
					offset -= 30;
				
				if (offset < 5)
					offset = 5;
				
				startFollow((Creature) target, offset);
			}
			else
			{
				// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
				moveToPawn(target, offset);
			}
			return true;
		}
		
		if (getFollowTarget() != null)
			stopFollow();
		
		return false;
	}
	
	/**
	 * @param target The targeted WorldObject
	 * @return true if the target is lost or dead (fake death isn't considered), and set intention to ACTIVE.
	 */
	protected boolean checkTargetLostOrDead(Creature target)
	{
		if (target == null || target.isAlikeDead())
		{
			if (target instanceof Player && ((Player) target).isFakeDeath())
			{
				target.stopFakeDeath(true);
				return false;
			}
			
			// Set the Intention of this AbstractAI to ACTIVE
			setIntention(IntentionType.ACTIVE);
			return true;
		}
		return false;
	}
	
	/**
	 * @param target : The targeted WorldObject
	 * @return true if the target is lost, and set intention to ACTIVE.
	 */
	protected boolean checkTargetLost(WorldObject target)
	{
		if (target instanceof Player)
		{
			final Player victim = (Player) target;
			if (victim.isFakeDeath())
			{
				victim.stopFakeDeath(true);
				return false;
			}
		}
		
		if (target == null)
		{
			// Set the Intention of this AbstractAI to ACTIVE
			setIntention(IntentionType.ACTIVE);
			return true;
		}
		return false;
	}
	
	public boolean canAura(L2Skill sk)
	{
		if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AURA)
		{
			for (WorldObject target : _actor.getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
			{
				if (target == getTarget())
					return true;
			}
		}
		return false;
	}
	
	public boolean canAOE(L2Skill sk)
	{
		if (sk.getSkillType() != L2SkillType.NEGATE || sk.getSkillType() != L2SkillType.CANCEL)
		{
			if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AURA)
			{
				boolean cancast = true;
				for (Creature target : _actor.getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
				{
					if (!GeoEngine.getInstance().canSeeTarget(_actor, target))
						continue;
					
					if (target instanceof Attackable && !_actor.isConfused())
						continue;
					
					if (target.getFirstEffect(sk) != null)
						cancast = false;
				}
				
				if (cancast)
					return true;
			}
			else if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AREA)
			{
				boolean cancast = true;
				for (Creature target : ((Creature) getTarget()).getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
				{
					if (!GeoEngine.getInstance().canSeeTarget(_actor, target))
						continue;
					
					if (target instanceof Attackable && !_actor.isConfused())
						continue;
					
					L2Effect[] effects = target.getAllEffects();
					if (effects.length > 0)
						cancast = true;
				}
				if (cancast)
					return true;
			}
		}
		else
		{
			if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AURA)
			{
				boolean cancast = false;
				for (Creature target : _actor.getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
				{
					if (!GeoEngine.getInstance().canSeeTarget(_actor, target))
						continue;
					
					if (target instanceof Attackable && !_actor.isConfused())
						continue;
					
					L2Effect[] effects = target.getAllEffects();
					if (effects.length > 0)
						cancast = true;
				}
				if (cancast)
					return true;
			}
			else if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == L2Skill.SkillTargetType.TARGET_FRONT_AREA)
			{
				boolean cancast = true;
				for (Creature target : ((Creature) getTarget()).getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
				{
					if (!GeoEngine.getInstance().canSeeTarget(_actor, target))
						continue;
					
					if (target instanceof Attackable && !_actor.isConfused())
						continue;
					
					if (target.getFirstEffect(sk) != null)
						cancast = false;
				}
				
				if (cancast)
					return true;
			}
		}
		return false;
	}
	
	public boolean canParty(L2Skill sk)
	{
		if (sk.getTargetType() != L2Skill.SkillTargetType.TARGET_PARTY)
			return false;
		
		int count = 0;
		int ccount = 0;
		
		final String[] actorClans = ((Npc) _actor).getTemplate().getClans();
		for (Attackable target : _actor.getKnownTypeInRadius(Attackable.class, sk.getSkillRadius()))
		{
			if (!GeoEngine.getInstance().canSeeTarget(_actor, target))
				continue;
			
			if (!ArraysUtil.contains(actorClans, target.getTemplate().getClans()))
				continue;
			
			count++;
			
			if (target.getFirstEffect(sk) != null)
				ccount++;
		}
		
		if (ccount < count)
			return true;
		
		return false;
	}
}
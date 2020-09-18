package dev.l2j.tesla.gameserver.model.actor.ai.type;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;
import dev.l2j.tesla.gameserver.model.actor.ai.Desire;
import dev.l2j.tesla.gameserver.model.actor.instance.StaticObject;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.AutoAttackStart;
import dev.l2j.tesla.gameserver.taskmanager.AttackStanceTaskManager;
import dev.l2j.tesla.gameserver.enums.IntentionType;

public class PlayerAI extends PlayableAI
{
	private boolean _thinking; // to prevent recursive thinking
	private Desire _nextIntention = new Desire();
	
	public PlayerAI(Player player)
	{
		super(player);
	}
	
	@Override
	protected void clientActionFailed()
	{
		_actor.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public Desire getNextIntention()
	{
		return _nextIntention;
	}
	
	@Override
	synchronized void changeIntention(IntentionType intention, Object arg0, Object arg1)
	{
		// do nothing unless CAST intention
		// however, forget interrupted actions when starting to use an offensive skill
		if (intention != IntentionType.CAST || (arg0 != null && ((L2Skill) arg0).isOffensive()))
		{
			_nextIntention.reset();
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		
		// do nothing if next intention is same as current one.
		if (_desire.equals(intention, arg0, arg1))
			return;
		
		// save current intention so it can be used after cast
		_nextIntention.update(_desire);
		
		super.changeIntention(intention, arg0, arg1);
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
		// Launch actions corresponding to the Event Think
		if (!_nextIntention.isBlank())
		{
			setIntention(_nextIntention.getIntention(), _nextIntention.getFirstParameter(), _nextIntention.getSecondParameter());
			_nextIntention.reset();
		}
		super.onEvtReadyToAct();
	}
	
	@Override
	protected void onEvtCancel()
	{
		_nextIntention.reset();
		super.onEvtCancel();
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		if (_desire.getIntention() == IntentionType.CAST)
		{
			if (!_nextIntention.isBlank() && _nextIntention.getIntention() != IntentionType.CAST) // previous state shouldn't be casting
				setIntention(_nextIntention.getIntention(), _nextIntention.getFirstParameter(), _nextIntention.getSecondParameter());
			else
				setIntention(IntentionType.IDLE);
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
		if (_desire.getIntention() != IntentionType.REST)
		{
			changeIntention(IntentionType.REST, null, null);
			setTarget(null);
			clientStopMoving(null);
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		setIntention(IntentionType.IDLE);
	}
	
	@Override
	protected void onIntentionMoveTo(Location loc)
	{
		// Deny the action if we are currently resting.
		if (_desire.getIntention() == IntentionType.REST)
		{
			clientActionFailed();
			return;
		}
		
		// We delay MOVE_TO intention if character is disabled or is currently casting/attacking.
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttackingNow())
		{
			clientActionFailed();
			_nextIntention.update(IntentionType.MOVE_TO, loc, null);
			return;
		}
		
		// Set the Intention of this AbstractAI to MOVE_TO
		changeIntention(IntentionType.MOVE_TO, loc, null);
		
		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)
		moveTo(loc.getX(), loc.getY(), loc.getZ());
	}
	
	@Override
	protected void onIntentionInteract(WorldObject object)
	{
		// Deny the action if we are currently resting.
		if (_desire.getIntention() == IntentionType.REST)
		{
			clientActionFailed();
			return;
		}
		
		// We delay INTERACT intention if character is disabled or is currently casting.
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			_nextIntention.update(IntentionType.INTERACT, object, null);
			return;
		}
		
		// Set the Intention of this AbstractAI to INTERACT
		changeIntention(IntentionType.INTERACT, object, null);
		
		// Set the AI interact target
		setTarget(object);
		
		// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
		moveToPawn(object, 60);
	}
	
	@Override
	protected void clientNotifyDead()
	{
		_clientMovingToPawnOffset = 0;
		_clientMoving = false;
		
		super.clientNotifyDead();
	}
	
	@Override
	public void startAttackStance()
	{
		// Initial check ; if the actor wasn't yet registered into AttackStanceTaskManager, broadcast AutoAttackStart packet. Check if a summon exists, if so, broadcast AutoAttackStart packet for the summon.
		if (!AttackStanceTaskManager.getInstance().isInAttackStance(_actor))
		{
			final Summon summon = ((Player) _actor).getSummon();
			if (summon != null)
				summon.broadcastPacket(new AutoAttackStart(summon.getObjectId()));
			
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
		}
		
		// Set out of the initial if check to be able to refresh the time.
		AttackStanceTaskManager.getInstance().add(_actor);
	}
	
	private void thinkAttack()
	{
		final Creature target = (Creature) getTarget();
		if (target == null)
		{
			setTarget(null);
			setIntention(IntentionType.ACTIVE);
			return;
		}
		
		if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange()))
			return;
		
		if (target.isAlikeDead())
		{
			if (target instanceof Player && ((Player) target).isFakeDeath())
				target.stopFakeDeath(true);
			else
			{
				setIntention(IntentionType.ACTIVE);
				return;
			}
		}
		
		clientStopMoving(null);
		_actor.doAttack(target);
	}
	
	private void thinkCast()
	{
		Creature target = (Creature) getTarget();
		
		if (_skill.getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND && _actor instanceof Player)
		{
			if (maybeMoveToPosition(((Player) _actor).getCurrentSkillWorldPosition(), _skill.getCastRange()))
			{
				_actor.setIsCastingNow(false);
				return;
			}
		}
		else
		{
			if (checkTargetLost(target))
			{
				// Notify the target
				if (_skill.isOffensive() && getTarget() != null)
					setTarget(null);
				
				_actor.setIsCastingNow(false);
				return;
			}
			
			if (target != null && maybeMoveToPawn(target, _skill.getCastRange()))
			{
				_actor.setIsCastingNow(false);
				return;
			}
		}
		
		if (_skill.getHitTime() > 50 && !_skill.isSimultaneousCast())
			clientStopMoving(null);
		
		_actor.doCast(_skill);
	}
	
	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttackingNow())
			return;
		
		final WorldObject target = getTarget();
		if (checkTargetLost(target))
			return;
		
		if (maybeMoveToPawn(target, 36))
			return;
		
		setIntention(IntentionType.IDLE);
		_actor.getActingPlayer().doPickupItem(target);
	}
	
	private void thinkInteract()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
			return;
		
		WorldObject target = getTarget();
		if (checkTargetLost(target))
			return;
		
		if (maybeMoveToPawn(target, 36))
			return;
		
		if (!(target instanceof StaticObject))
			_actor.getActingPlayer().doInteract((Creature) target);
		
		setIntention(IntentionType.IDLE);
	}
	
	@Override
	protected void onEvtThink()
	{
		// Check if the actor can't use skills and if a thinking action isn't already in progress
		if (_thinking && _desire.getIntention() != IntentionType.CAST) // casting must always continue
			return;
		
		// Start thinking action
		_thinking = true;
		
		try
		{
			// Manage AI thoughts
			switch (_desire.getIntention())
			{
				case ATTACK:
					thinkAttack();
					break;
				case CAST:
					thinkCast();
					break;
				case PICK_UP:
					thinkPickUp();
					break;
				case INTERACT:
					thinkInteract();
					break;
			}
		}
		finally
		{
			// Stop thinking action
			_thinking = false;
		}
	}
}
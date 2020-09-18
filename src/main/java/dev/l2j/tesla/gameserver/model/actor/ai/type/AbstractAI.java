package dev.l2j.tesla.gameserver.model.actor.ai.type;

import java.util.concurrent.Future;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;
import dev.l2j.tesla.gameserver.model.actor.ai.Desire;
import dev.l2j.tesla.gameserver.model.actor.ai.NextAction;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.model.location.SpawnLocation;
import dev.l2j.tesla.gameserver.taskmanager.AttackStanceTaskManager;
import dev.l2j.tesla.commons.concurrent.ThreadPool;

import dev.l2j.tesla.gameserver.enums.AiEventType;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.network.serverpackets.AutoAttackStart;
import dev.l2j.tesla.gameserver.network.serverpackets.AutoAttackStop;
import dev.l2j.tesla.gameserver.network.serverpackets.Die;
import dev.l2j.tesla.gameserver.network.serverpackets.MoveToLocation;
import dev.l2j.tesla.gameserver.network.serverpackets.MoveToPawn;
import dev.l2j.tesla.gameserver.network.serverpackets.StopMove;
import dev.l2j.tesla.gameserver.network.serverpackets.StopRotation;

abstract class AbstractAI
{
	private static final int FOLLOW_INTERVAL = 1000;
	private static final int ATTACK_FOLLOW_INTERVAL = 500;
	
	protected final Creature _actor;
	protected final Desire _desire = new Desire();
	
	private NextAction _nextAction;
	
	/** Flags about client's state, in order to know which messages to send */
	protected volatile boolean _clientMoving;
	
	/** Different targets this AI maintains */
	private WorldObject _target;
	protected Creature _followTarget;
	
	/** The skill we are currently casting by INTENTION_CAST */
	protected L2Skill _skill;
	
	/** Different internal state flags */
	private long _moveToPawnTimeout;
	protected int _clientMovingToPawnOffset;
	
	protected Future<?> _followTask = null;
	
	protected AbstractAI(Creature character)
	{
		_actor = character;
	}
	
	public Creature getActor()
	{
		return _actor;
	}
	
	public Desire getDesire()
	{
		return _desire;
	}
	
	/**
	 * Set the Intention of this AbstractAI.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is USED by AI classes</B></FONT><BR>
	 * <BR>
	 * <B><U> Overridden in </U> : </B><BR>
	 * <B>L2AttackableAI</B> : Create an AI Task executed every 1s (if necessary)<BR>
	 * <B>L2PlayerAI</B> : Stores the current AI intention parameters to later restore it if necessary<BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 */
	synchronized void changeIntention(IntentionType intention, Object arg0, Object arg1)
	{
		_desire.update(intention, arg0, arg1);
	}
	
	/**
	 * Launch the CreatureAI onIntention method corresponding to the new Intention.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 */
	public final void setIntention(IntentionType intention)
	{
		setIntention(intention, null, null);
	}
	
	/**
	 * Launch the CreatureAI onIntention method corresponding to the new Intention.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention (optional target)
	 */
	public final void setIntention(IntentionType intention, Object arg0)
	{
		setIntention(intention, arg0, null);
	}
	
	/**
	 * Launch the CreatureAI onIntention method corresponding to the new Intention.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention (optional target)
	 * @param arg1 The second parameter of the Intention (optional target)
	 */
	public final void setIntention(IntentionType intention, Object arg0, Object arg1)
	{
		// Stop the follow mode if necessary
		if (intention != IntentionType.FOLLOW && intention != IntentionType.ATTACK)
			stopFollow();
		
		// Launch the onIntention method of the CreatureAI corresponding to the new Intention
		switch (intention)
		{
			case IDLE:
				onIntentionIdle();
				break;
			case ACTIVE:
				onIntentionActive();
				break;
			case REST:
				onIntentionRest();
				break;
			case ATTACK:
				onIntentionAttack((Creature) arg0);
				break;
			case CAST:
				onIntentionCast((L2Skill) arg0, (WorldObject) arg1);
				break;
			case MOVE_TO:
				onIntentionMoveTo((Location) arg0);
				break;
			case FOLLOW:
				onIntentionFollow((Creature) arg0);
				break;
			case PICK_UP:
				onIntentionPickUp((WorldObject) arg0);
				break;
			case INTERACT:
				onIntentionInteract((WorldObject) arg0);
				break;
		}
		
		// If do move or follow intention drop next action.
		if (_nextAction != null && _nextAction.getIntention() == intention)
			_nextAction = null;
	}
	
	/**
	 * Launch the CreatureAI onEvt method corresponding to the Event.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned periode)</B></FONT><BR>
	 * <BR>
	 * @param evt The event whose the AI must be notified
	 */
	public final void notifyEvent(AiEventType evt)
	{
		notifyEvent(evt, null, null);
	}
	
	/**
	 * Launch the CreatureAI onEvt method corresponding to the Event.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned periode)</B></FONT><BR>
	 * <BR>
	 * @param evt The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 */
	public final void notifyEvent(AiEventType evt, Object arg0)
	{
		notifyEvent(evt, arg0, null);
	}
	
	/**
	 * Launch the CreatureAI onEvt method corresponding to the Event.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned periode)</B></FONT><BR>
	 * <BR>
	 * @param evt The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 * @param arg1 The second parameter of the Event (optional target)
	 */
	public final void notifyEvent(AiEventType evt, Object arg0, Object arg1)
	{
		if ((!_actor.isVisible() && !_actor.isTeleporting()) || !_actor.hasAI())
			return;
		
		switch (evt)
		{
			case THINK:
				onEvtThink();
				break;
			case ATTACKED:
				onEvtAttacked((Creature) arg0);
				break;
			case AGGRESSION:
				onEvtAggression((Creature) arg0, ((Number) arg1).intValue());
				break;
			case STUNNED:
				onEvtStunned((Creature) arg0);
				break;
			case PARALYZED:
				onEvtParalyzed((Creature) arg0);
				break;
			case SLEEPING:
				onEvtSleeping((Creature) arg0);
				break;
			case ROOTED:
				onEvtRooted((Creature) arg0);
				break;
			case CONFUSED:
				onEvtConfused((Creature) arg0);
				break;
			case MUTED:
				onEvtMuted((Creature) arg0);
				break;
			case EVADED:
				onEvtEvaded((Creature) arg0);
				break;
			case READY_TO_ACT:
				if (!_actor.isCastingNow() && !_actor.isCastingSimultaneouslyNow())
					onEvtReadyToAct();
				break;
			case ARRIVED:
				if (!_actor.isCastingNow() && !_actor.isCastingSimultaneouslyNow())
					onEvtArrived();
				break;
			case ARRIVED_BLOCKED:
				onEvtArrivedBlocked((SpawnLocation) arg0);
				break;
			case CANCEL:
				onEvtCancel();
				break;
			case DEAD:
				onEvtDead();
				break;
			case FAKE_DEATH:
				onEvtFakeDeath();
				break;
			case FINISH_CASTING:
				onEvtFinishCasting();
				break;
		}
		
		// Do next action.
		if (_nextAction != null && _nextAction.getEvent() == evt)
		{
			_nextAction.run();
			_nextAction = null;
		}
	}
	
	/**
	 * Manage the Idle Intention : Stop Attack, Movement and Stand Up the actor.
	 */
	protected abstract void onIntentionIdle();
	
	/**
	 * Manage the Active Intention : Stop Attack, Movement and Launch Think Event.
	 */
	protected abstract void onIntentionActive();
	
	/**
	 * Manage the Rest Intention. Set the AI Intention to IDLE.
	 */
	protected abstract void onIntentionRest();
	
	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event.
	 * @param target : The Creature used as target.
	 */
	protected abstract void onIntentionAttack(Creature target);
	
	/**
	 * Launch a spell.
	 * @param skill : The L2Skill to cast.
	 * @param target : The WorldObject used as target.
	 */
	protected abstract void onIntentionCast(L2Skill skill, WorldObject target);
	
	/**
	 * Launch a movement to a {@link Location} if conditions are met.
	 * @param loc : The Location used as destination.
	 */
	protected abstract void onIntentionMoveTo(Location loc);
	
	/**
	 * Follow the {@link Creature} set as parameter if conditions are met.
	 * @param target : The Creature used as target.
	 */
	protected abstract void onIntentionFollow(Creature target);
	
	/**
	 * Manage the PickUp Intention : Set the pick up target and Launch a Move To Pawn Task (offset=20).
	 * @param item : The WorldObject used as target.
	 */
	protected abstract void onIntentionPickUp(WorldObject item);
	
	protected abstract void onIntentionInteract(WorldObject object);
	
	protected abstract void onEvtThink();
	
	protected abstract void onEvtAttacked(Creature attacker);
	
	/**
	 * Launch actions corresponding to the effect Aggro.
	 * @param target : The Creature used as attacker.
	 * @param aggro : The amount of aggro.
	 */
	protected abstract void onEvtAggression(Creature target, int aggro);
	
	/**
	 * Launch actions corresponding to the effect Stun.
	 * @param attacker : The Creature used as attacker.
	 */
	protected abstract void onEvtStunned(Creature attacker);
	
	/**
	 * Launch actions corresponding to the effect Paralyze.
	 * @param attacker : The Creature used as attacker.
	 */
	protected abstract void onEvtParalyzed(Creature attacker);
	
	/**
	 * Launch actions corresponding to the effect Sleep.
	 * @param attacker : The Creature used as attacker.
	 */
	protected abstract void onEvtSleeping(Creature attacker);
	
	/**
	 * Launch actions corresponding to the effect Rooted.
	 * @param attacker : The Creature used as attacker.
	 */
	protected abstract void onEvtRooted(Creature attacker);
	
	/**
	 * Launch actions corresponding to the effect Confusion.
	 * @param attacker : The Creature used as attacker.
	 */
	protected abstract void onEvtConfused(Creature attacker);
	
	/**
	 * Launch actions corresponding to the effect Mute.
	 * @param attacker : The Creature used as attacker.
	 */
	protected abstract void onEvtMuted(Creature attacker);
	
	/**
	 * Launch actions corresponding to the effect Stun.
	 * @param attacker : The Creature used as attacker.
	 */
	protected abstract void onEvtEvaded(Creature attacker);
	
	/**
	 * Launch actions corresponding to the Event ReadyToAct.
	 */
	protected abstract void onEvtReadyToAct();
	
	/**
	 * Launch actions corresponding to the Event Arrived.
	 */
	protected abstract void onEvtArrived();
	
	/**
	 * Launch actions corresponding to the Event ArrivedBlocked.
	 * @param loc : The Location used as destination.
	 */
	protected abstract void onEvtArrivedBlocked(SpawnLocation loc);
	
	/**
	 * Launch actions corresponding to the Event Cancel.
	 */
	protected abstract void onEvtCancel();
	
	/**
	 * Launch actions corresponding to the death of the actor.
	 */
	protected abstract void onEvtDead();
	
	/**
	 * Launch actions corresponding to the effect Fake Death.
	 */
	protected abstract void onEvtFakeDeath();
	
	/**
	 * Finalize the casting of a skill. Drop latest intention before the actual CAST.
	 */
	protected abstract void onEvtFinishCasting();
	
	/**
	 * Cancel action client side by sending Server->Client packet ActionFailed to the Player actor.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 */
	protected void clientActionFailed()
	{
	}
	
	/**
	 * Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 * @param pawn
	 * @param offset
	 */
	protected void moveToPawn(WorldObject pawn, int offset)
	{
		// Check if actor can move
		if (!_actor.isMovementDisabled())
		{
			if (offset < 10)
				offset = 10;
			
			// prevent possible extra calls to this function (there is none?).
			if (_clientMoving && (_target == pawn))
			{
				if (_clientMovingToPawnOffset == offset)
				{
					if (System.currentTimeMillis() < _moveToPawnTimeout)
					{
						clientActionFailed();
						return;
					}
				}
				else if (_actor.isOnGeodataPath())
				{
					// minimum time to calculate new route is 2 seconds
					if (System.currentTimeMillis() < _moveToPawnTimeout + 1000)
					{
						clientActionFailed();
						return;
					}
				}
			}
			
			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = offset;
			_target = pawn;
			_moveToPawnTimeout = System.currentTimeMillis() + 1000;
			
			if (pawn == null)
			{
				clientActionFailed();
				return;
			}
			
			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			_actor.moveToLocation(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
			
			if (!_actor.isMoving())
			{
				clientActionFailed();
				return;
			}
			
			// Broadcast MoveToPawn/MoveToLocation packet
			if (pawn instanceof Creature)
			{
				if (_actor.isOnGeodataPath())
				{
					_actor.broadcastPacket(new MoveToLocation(_actor));
					_clientMovingToPawnOffset = 0;
				}
				else
					_actor.broadcastPacket(new MoveToPawn(_actor, pawn, offset));
			}
			else
				_actor.broadcastPacket(new MoveToLocation(_actor));
		}
		else
			clientActionFailed();
	}
	
	/**
	 * Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation <I>(broadcast)</I>.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT>
	 * @param x
	 * @param y
	 * @param z
	 */
	protected void moveTo(int x, int y, int z)
	{
		// Chek if actor can move
		if (!_actor.isMovementDisabled())
		{
			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = 0;
			
			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			_actor.moveToLocation(x, y, z, 0);
			
			// Broadcast MoveToLocation packet
			_actor.broadcastPacket(new MoveToLocation(_actor));
			
		}
		else
			clientActionFailed();
	}
	
	/**
	 * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 * @param loc
	 */
	protected void clientStopMoving(SpawnLocation loc)
	{
		// Stop movement of the Creature
		if (_actor.isMoving())
			_actor.stopMove(loc);
		
		_clientMovingToPawnOffset = 0;
		
		if (_clientMoving || loc != null)
		{
			_clientMoving = false;
			
			_actor.broadcastPacket(new StopMove(_actor));
			
			if (loc != null)
				_actor.broadcastPacket(new StopRotation(_actor.getObjectId(), loc.getHeading(), 0));
		}
	}
	
	// Client has already arrived to target, no need to force StopMove packet
	protected void clientStoppedMoving()
	{
		if (_clientMovingToPawnOffset > 0) // movetoPawn needs to be stopped
		{
			_clientMovingToPawnOffset = 0;
			_actor.broadcastPacket(new StopMove(_actor));
		}
		_clientMoving = false;
	}
	
	/**
	 * Activate the attack stance on clients, broadcasting {@link AutoAttackStart} packets. Refresh the timer if already on stance.
	 */
	public void startAttackStance()
	{
		// Initial check ; if the actor wasn't yet registered into AttackStanceTaskManager, broadcast AutoAttackStart packet.
		if (!AttackStanceTaskManager.getInstance().isInAttackStance(_actor))
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
		
		// Set out of the initial if check to be able to refresh the time.
		AttackStanceTaskManager.getInstance().add(_actor);
	}
	
	/**
	 * Deactivate the attack stance on clients, broadcasting {@link AutoAttackStop} packet if the actor was indeed registered on {@link AttackStanceTaskManager}.
	 */
	public void stopAttackStance()
	{
		// If we successfully remove the actor from AttackStanceTaskManager, we also broadcast AutoAttackStop packet.
		if (AttackStanceTaskManager.getInstance().remove(_actor))
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
	}
	
	/**
	 * Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 */
	protected void clientNotifyDead()
	{
		// Broadcast Die packet
		_actor.broadcastPacket(new Die(_actor));
		
		// Init AI
		_desire.update(IntentionType.IDLE, null, null);
		_target = null;
		
		// Cancel the follow task if necessary
		stopFollow();
		
		// Stop the actor auto-attack
		stopAttackStance();
	}
	
	/**
	 * Send the state of this actor to a {@link Player}.
	 * @param player : The Player to notify with the state of this actor.
	 */
	public void describeStateToPlayer(Player player)
	{
		if (_desire.getIntention() == IntentionType.MOVE_TO)
		{
			if (_clientMovingToPawnOffset != 0 && _followTarget != null)
				player.sendPacket(new MoveToPawn(_actor, _followTarget, _clientMovingToPawnOffset));
			else
				player.sendPacket(new MoveToLocation(_actor));
		}
		// else if (getIntention() == CtrlIntention.CAST) TODO
	}
	
	/**
	 * Create and Launch an AI Follow Task to execute every 1s.
	 * @param target The Creature to follow
	 */
	public synchronized void startFollow(Creature target)
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		
		// Create and Launch an AI Follow Task to execute every 1s
		_followTarget = target;
		_followTask = ThreadPool.scheduleAtFixedRate(new FollowTask(), 5, FOLLOW_INTERVAL);
	}
	
	/**
	 * Create and Launch an AI Follow Task to execute every 0.5s, following at specified range.
	 * @param target The Creature to follow
	 * @param range
	 */
	public synchronized void startFollow(Creature target, int range)
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		
		_followTarget = target;
		_followTask = ThreadPool.scheduleAtFixedRate(new FollowTask(range), 5, ATTACK_FOLLOW_INTERVAL);
	}
	
	/**
	 * Stop an AI Follow Task.
	 */
	public synchronized void stopFollow()
	{
		if (_followTask != null)
		{
			// Stop the Follow Task
			_followTask.cancel(false);
			_followTask = null;
		}
		_followTarget = null;
	}
	
	protected Creature getFollowTarget()
	{
		return _followTarget;
	}
	
	public WorldObject getTarget()
	{
		return _target;
	}
	
	protected void setTarget(WorldObject target)
	{
		_target = target;
	}
	
	/**
	 * Stop all Ai tasks and futures.
	 */
	public void stopAITask()
	{
		stopFollow();
	}
	
	/**
	 * @param nextAction the _nextAction to set
	 */
	public void setNextAction(NextAction nextAction)
	{
		_nextAction = nextAction;
	}
	
	@Override
	public String toString()
	{
		return "Actor: " + _actor;
	}
	
	private class FollowTask implements Runnable
	{
		protected int _range = 70;
		
		public FollowTask()
		{
		}
		
		public FollowTask(int range)
		{
			_range = range;
		}
		
		@Override
		public void run()
		{
			if (_followTask == null)
				return;
			
			Creature followTarget = _followTarget;
			if (followTarget == null)
			{
				if (_actor instanceof Summon)
					((Summon) _actor).setFollowStatus(false);
				
				setIntention(IntentionType.IDLE);
				return;
			}
			
			if (!_actor.isInsideRadius(followTarget, _range, true, false))
				moveToPawn(followTarget, _range);
		}
	}
}
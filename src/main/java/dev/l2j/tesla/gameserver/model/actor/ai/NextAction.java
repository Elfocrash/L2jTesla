package dev.l2j.tesla.gameserver.model.actor.ai;

import dev.l2j.tesla.gameserver.enums.AiEventType;
import dev.l2j.tesla.gameserver.enums.IntentionType;

/**
 * Class for AI action after some event.
 * @author Yaroslav
 */
public class NextAction
{
	/** After which CtrlEvent is this action supposed to run. */
	private final AiEventType _event;
	
	/** What is the intention of the action, e.g. if AI gets this IntentionType set, NextAction is canceled. */
	private final IntentionType _intention;
	
	/** Wrapper for NextAction content. */
	private final Runnable _runnable;
	
	/**
	 * Single constructor.
	 * @param event : After which the NextAction is triggered.
	 * @param intention : IntentionType of the action.
	 * @param runnable :
	 */
	public NextAction(AiEventType event, IntentionType intention, Runnable runnable)
	{
		_event = event;
		_intention = intention;
		_runnable = runnable;
	}
	
	/**
	 * @return the _event
	 */
	public AiEventType getEvent()
	{
		return _event;
	}
	
	/**
	 * @return the _intention
	 */
	public IntentionType getIntention()
	{
		return _intention;
	}
	
	/**
	 * Do action.
	 */
	public void run()
	{
		_runnable.run();
	}
}
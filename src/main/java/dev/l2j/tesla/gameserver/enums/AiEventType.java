package dev.l2j.tesla.gameserver.enums;

/**
 * This class contains an enum of each possible event, that can happen on an AI character.
 */
public enum AiEventType
{
	/** Something has changed, usually a previous step has being completed or maybe was completed, the AI must thing on next action. */
	THINK,
	/** The actor was attacked. This event comes each time a physical or magical attack was done on the actor. NPC may start attack in response, or ignore this event if they already attack someone, or change target and so on. */
	ATTACKED,
	/** Increase/decrease aggression towards a target, or reduce global aggression if target is null. */
	AGGRESSION,
	/** Actor is in stun state. */
	STUNNED,
	/** Actor is paralyzed or petrified. */
	PARALYZED,
	/** Actor starts/stops sleeping. */
	SLEEPING,
	/** Actor is in rooted state (cannot move). */
	ROOTED,
	/** Actor evaded hit. */
	EVADED,
	/** An event that previous action was completed. The action may be an attempt to physically or magically hit an enemy, or an action that discarded attack attempt has finished. */
	READY_TO_ACT,
	/** The actor arrived to assigned location, or it's a time to modify movement destination (follow, interact, random move and others intentions). */
	ARRIVED,
	/** The actor cannot move anymore. */
	ARRIVED_BLOCKED,
	/**
	 * Attempt to cancel current step execution, but not change the intention. For example, the actor was put into a stun, so it's current attack or movement has to be canceled. But after the stun state expired, the actor may try to attack again. Another usage for CANCEL is a user's attempt to
	 * cancel a cast/bow attack and so on.
	 */
	CANCEL,
	/** The character is dead. */
	DEAD,
	/** The character looks like dead. */
	FAKE_DEATH,
	/** The character attack anyone randomly. */
	CONFUSED,
	/** The character cannot cast spells anymore. */
	MUTED,
	/** The character flee in random directions */
	AFRAID,
	/** The character finish casting. */
	FINISH_CASTING
}
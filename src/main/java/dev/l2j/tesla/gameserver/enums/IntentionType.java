package dev.l2j.tesla.gameserver.enums;

/** Enumeration of generic intentions of an NPC/PC */
public enum IntentionType
{
	/** Do nothing, disconnect AI of NPC if no players around. */
	IDLE,
	/** Alerted state without goal : scan attackable targets, random walk, etc. */
	ACTIVE,
	/** Rest (sit until attacked). */
	REST,
	/** Attack target (cast combat magic, go to target, combat) - may be ignored (another target, invalid zoning, etc). */
	ATTACK,
	/** Cast a spell, depending on the spell - may start or stop attacking. */
	CAST,
	/** Just move to another location. */
	MOVE_TO,
	/** Like move, but check target's movement and follow it. */
	FOLLOW,
	/** Pick up item (go to item, pick up it, become idle). */
	PICK_UP,
	/** Move to target, then interact. */
	INTERACT
}
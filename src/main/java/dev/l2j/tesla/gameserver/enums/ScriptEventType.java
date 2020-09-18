package dev.l2j.tesla.gameserver.enums;

/**
 * The enum listing all events type.
 */
public enum ScriptEventType
{
	/**
	 * An event controlling the first dialog shown by NPCs when they are clicked.
	 */
	ON_FIRST_TALK(false),
	
	/**
	 * An event controlling onTalk action from start npcs.
	 */
	QUEST_START(true),
	
	/**
	 * An event controlling onTalk action from npcs participating in a quest.
	 */
	ON_TALK(true),
	
	/**
	 * An event triggered when a mob gets attacked by someone.
	 */
	ON_ATTACK(true),
	
	/**
	 * An event triggered when a mob attacks someone.
	 */
	ON_ATTACK_ACT(true),
	
	/**
	 * An event triggered when a mob gets killed.
	 */
	ON_KILL(true),
	
	/**
	 * An event triggered when a NPC is spawned or respawned.
	 */
	ON_SPAWN(true),
	
	/**
	 * An event triggered when a NPC decays.
	 */
	ON_DECAY(true),
	
	/**
	 * An event triggered when a NPC see another creature.
	 */
	ON_CREATURE_SEE(true),
	
	/**
	 * An event triggered when a NPC see a spell being casted.
	 */
	ON_SKILL_SEE(true),
	
	/**
	 * An event triggered when a NPC calls members of its clan.
	 */
	ON_FACTION_CALL(true),
	
	/**
	 * An event triggered when a NPC see another creature, but is aggressive.
	 */
	ON_AGGRO(true),
	
	/**
	 * An event triggered when a spell goes to the end, once casted. Used for exotic skills.
	 */
	ON_SPELL_FINISHED(true),
	
	/**
	 * An event triggered when someone enters a zone.
	 */
	ON_ENTER_ZONE(true),
	
	/**
	 * An event triggered when someone exits a zone.
	 */
	ON_EXIT_ZONE(true);
	
	private boolean _allowMultipleRegistration;
	
	ScriptEventType(boolean allowMultipleRegistration)
	{
		_allowMultipleRegistration = allowMultipleRegistration;
	}
	
	/**
	 * @return true if the {@link ScriptEventType} allows multiple registrations.
	 */
	public boolean isMultipleRegistrationAllowed()
	{
		return _allowMultipleRegistration;
	}
}
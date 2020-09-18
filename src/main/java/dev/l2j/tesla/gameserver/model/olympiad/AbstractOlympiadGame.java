package dev.l2j.tesla.gameserver.model.olympiad;

import java.util.List;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.ExOlympiadMode;
import dev.l2j.tesla.gameserver.network.serverpackets.InventoryUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.data.xml.MapRegionData;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.MessageType;
import dev.l2j.tesla.gameserver.enums.OlympiadType;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;
import dev.l2j.tesla.gameserver.model.actor.instance.Pet;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.model.zone.type.OlympiadStadiumZone;
import dev.l2j.tesla.gameserver.model.zone.type.TownZone;

/**
 * The abstract layer for an Olympiad game (individual, class and non-class based).
 */
public abstract class AbstractOlympiadGame
{
	protected static final CLogger LOGGER = new CLogger(AbstractOlympiadGame.class.getName());
	
	protected static final String POINTS = "olympiad_points";
	protected static final String COMP_DONE = "competitions_done";
	protected static final String COMP_WON = "competitions_won";
	protected static final String COMP_LOST = "competitions_lost";
	protected static final String COMP_DRAWN = "competitions_drawn";
	
	protected final int _stadiumId;
	
	protected long _startTime = 0;
	protected boolean _aborted = false;
	
	protected AbstractOlympiadGame(int id)
	{
		_stadiumId = id;
	}
	
	/**
	 * @return the {@link OlympiadType} of that game.
	 */
	public abstract OlympiadType getType();
	
	/**
	 * @return the array consisting of {@link Player} names.
	 */
	public abstract String[] getPlayerNames();
	
	/**
	 * @param objectId : The objectId to test.
	 * @return true if the current objectId is part of that game.
	 */
	public abstract boolean containsParticipant(int objectId);
	
	/**
	 * Sends olympiad info to the spectator.
	 * @param player : The Creature to send infos.
	 */
	public abstract void sendOlympiadInfo(Creature player);
	
	/**
	 * Broadcasts olympiad info to participants and spectators on battle start.
	 * @param stadium : The related stadium.
	 */
	public abstract void broadcastOlympiadInfo(OlympiadStadiumZone stadium);
	
	/**
	 * Broadcasts packet to participants only.
	 * @param packet : The packet to broadcast.
	 */
	protected abstract void broadcastPacket(L2GameServerPacket packet);
	
	/**
	 * @return true if a defection occured.
	 */
	protected abstract boolean checkDefection();
	
	/**
	 * Delete all effects related to {@link Player}s, and fully heal them. Unsummon their {@link Pet} if existing.
	 */
	protected abstract void removals();
	
	/**
	 * Buff {@link Player}s.
	 */
	protected abstract void buffPlayers();
	
	/**
	 * Heal {@link Player}s.
	 */
	protected abstract void healPlayers();
	
	/**
	 * @param spawns : The Locations used to teleport Players.
	 * @return true if both {@link Participant}s have been successfully teleported on the given {@link Location}s, false otherwise.
	 */
	protected abstract boolean portPlayersToArena(List<Location> spawns);
	
	/**
	 * Cancel all {@link Player}s animations, set their Intention to IDLE. Affects also their {@link Summon}, if existing. Heal Players, start their HP/MP regen.
	 */
	protected abstract void cleanEffects();
	
	/**
	 * Teleport {@link Player}s back to their initial {@link Location}.
	 */
	protected abstract void portPlayersBack();
	
	/**
	 * Reset {@link Player}s status back to regular behaviour : stop all charges and effects, add back clan & hero skills.
	 */
	protected abstract void playersStatusBack();
	
	/**
	 * Clear {@link Player}s references on {@link Participant}s.
	 */
	protected abstract void clearPlayers();
	
	/**
	 * Set the given {@link Player} as disconnected.
	 * @param player : The Player to affect.
	 */
	protected abstract void handleDisconnect(Player player);
	
	/**
	 * Reset done damages.
	 */
	protected abstract void resetDamage();
	
	/**
	 * Add damages for the given {@link Player}.
	 * @param player : The Player who dealt damages.
	 * @param damage : The amount of damage to add.
	 */
	protected abstract void addDamage(Player player, int damage);
	
	/**
	 * @return true if the battle is still continuing due to regular Olympiad rules (no disconnection, correct number of Participants,...)
	 */
	protected abstract boolean checkBattleStatus();
	
	/**
	 * @return true if either the battle ended abruptly, or if one of the side died.
	 */
	protected abstract boolean haveWinner();
	
	/**
	 * Compute winner and loser.
	 * @param stadium : The stadium zone used to broadcast packets.
	 */
	protected abstract void validateWinner(OlympiadStadiumZone stadium);
	
	/**
	 * @return the divider used by that type of game to calculate Olympiad points gain.
	 */
	protected abstract int getDivider();
	
	/**
	 * @return the array of {@link IntIntHolder} rewards.
	 */
	protected abstract IntIntHolder[] getReward();
	
	public final boolean isAborted()
	{
		return _aborted;
	}
	
	public final int getStadiumId()
	{
		return _stadiumId;
	}
	
	protected boolean makeCompetitionStart()
	{
		_startTime = System.currentTimeMillis();
		return !_aborted;
	}
	
	protected final void addPointsToParticipant(Participant par, int points)
	{
		par.updateStat(POINTS, points);
		
		broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_GAINED_S2_OLYMPIAD_POINTS).addString(par.getName()).addNumber(points));
	}
	
	protected final void removePointsFromParticipant(Participant par, int points)
	{
		par.updateStat(POINTS, -points);
		
		broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_LOST_S2_OLYMPIAD_POINTS).addString(par.getName()).addNumber(points));
	}
	
	/**
	 * @param player : The Player to check.
	 * @return null if the tested {@link Player} passed all checks or broadcast the {@link SystemMessage} associated to opponent defection.
	 */
	protected static SystemMessage checkDefection(Player player)
	{
		if (player == null || !player.isOnline())
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME);
		
		if (player.getClient() == null || player.getClient().isDetached())
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME);
		
		// safety precautions
		if (player.isInObserverMode())
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		
		if (player.isDead())
		{
			player.sendPacket(SystemMessageId.CANNOT_PARTICIPATE_OLYMPIAD_WHILE_DEAD);
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}
		
		if (player.isSubClassActive())
		{
			player.sendPacket(SystemMessageId.SINCE_YOU_HAVE_CHANGED_YOUR_CLASS_INTO_A_SUB_JOB_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}
		
		if (player.isCursedWeaponEquipped())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_JOIN_OLYMPIAD_POSSESSING_S1).addItemName(player.getCursedWeaponEquippedId()));
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}
		
		if (player.getInventoryLimit() * 0.8 <= player.getInventory().getSize())
		{
			player.sendPacket(SystemMessageId.SINCE_80_PERCENT_OR_MORE_OF_YOUR_INVENTORY_SLOTS_ARE_FULL_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}
		
		return null;
	}
	
	/**
	 * @param par : The Participant to teleport.
	 * @param loc : The Location to teleport.
	 * @param id : The olympiad game id.
	 * @return true if the {@link Participant} has been successfully teleported on the given {@link Location}, false otherwise.
	 */
	protected static final boolean portPlayerToArena(Participant par, Location loc, int id)
	{
		final Player player = par.getPlayer();
		if (player == null || !player.isOnline())
			return false;
		
		player.getSavedLocation().set(player.getPosition());
		
		player.setTarget(null);
		
		player.setOlympiadGameId(id);
		player.setOlympiadMode(true);
		player.setOlympiadStart(false);
		player.setOlympiadSide(par.getSide());
		player.teleportTo(loc, 0);
		player.sendPacket(new ExOlympiadMode(par.getSide()));
		return true;
	}
	
	/**
	 * Delete all effects related to a {@link Player}, and fully heal him. Unsummon his {@link Pet} if existing.
	 * @param player : The Player to affect.
	 * @param removeParty : If true, expels the Player from its current {@link Party}.
	 */
	protected static final void removals(Player player, boolean removeParty)
	{
		if (player == null)
			return;
		
		// Remove Buffs
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		
		// Remove Clan Skills
		if (player.getClan() != null)
		{
			for (L2Skill skill : player.getClan().getClanSkills().values())
				player.removeSkill(skill.getId(), false);
		}
		
		// Abort casting if player casting
		player.abortAttack();
		player.abortCast();
		
		// Force the character to be visible
		player.getAppearance().setVisible();
		
		// Remove Hero Skills
		if (player.isHero())
		{
			for (L2Skill skill : SkillTable.getHeroSkills())
				player.removeSkill(skill.getId(), false);
		}
		
		// Heal Player fully
		healPlayer(player);
		
		// Dismount player, if mounted.
		if (player.isMounted())
			player.dismount();
		// Test summon existence, if any.
		else
		{
			final Summon summon = player.getSummon();
			
			// Unsummon pets directly.
			if (summon instanceof Pet)
				summon.unSummon(player);
			// Remove servitor buffs and cancel animations.
			else if (summon != null)
			{
				summon.stopAllEffectsExceptThoseThatLastThroughDeath();
				summon.abortAttack();
				summon.abortCast();
			}
		}
		
		// stop any cubic that has been given by other player.
		player.stopCubicsByOthers();
		
		// Remove player from his party
		if (removeParty)
		{
			final Party party = player.getParty();
			if (party != null)
				party.removePartyMember(player, MessageType.EXPELLED);
		}
		
		player.checkItemRestriction();
		
		// Remove shot automation
		player.disableAutoShotsAll();
		
		// Discharge any active shots
		ItemInstance item = player.getActiveWeaponInstance();
		if (item != null)
			item.unChargeAllShots();
		
		player.sendSkillList();
	}
	
	/**
	 * Buff the {@link Player}. WW2 for fighter/mage + haste 1 if fighter.
	 * @param player : the happy benefactor.
	 */
	protected static final void buffPlayer(Player player)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(1204, 2); // Windwalk 2
		if (skill != null)
		{
			skill.getEffects(player, player);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(1204));
		}
		
		if (!player.isMageClass())
		{
			skill = SkillTable.getInstance().getInfo(1086, 1); // Haste 1
			if (skill != null)
			{
				skill.getEffects(player, player);
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(1086));
			}
		}
	}
	
	/**
	 * Heal the {@link Player}.
	 * @param player : the happy benefactor.
	 */
	protected static final void healPlayer(Player player)
	{
		player.setCurrentCp(player.getMaxCp());
		player.setCurrentHp(player.getMaxHp());
		player.setCurrentMp(player.getMaxMp());
	}
	
	/**
	 * Cancel all {@link Player} animations, set the Intention to IDLE. Affects also the {@link Summon}, if existing. Heal the Player, start his HP/MP regen.
	 * @param player : The Player to affect.
	 */
	protected static final void cleanEffects(Player player)
	{
		player.setOlympiadStart(false);
		player.setTarget(null);
		player.abortAttack();
		player.abortCast();
		player.getAI().setIntention(IntentionType.IDLE);
		
		if (player.isDead())
			player.setIsDead(false);
		
		final Summon summon = player.getSummon();
		if (summon != null && !summon.isDead())
		{
			summon.setTarget(null);
			summon.abortAttack();
			summon.abortCast();
			summon.getAI().setIntention(IntentionType.IDLE);
		}
		
		healPlayer(player);
		player.getStatus().startHpMpRegeneration();
	}
	
	/**
	 * Reset {@link Player} status back to regular behaviour : stop all charges and effects, add back clan & hero skills.
	 * @param player : The Player to affect.
	 */
	protected static final void playerStatusBack(Player player)
	{
		player.setOlympiadMode(false);
		player.setOlympiadStart(false);
		player.setOlympiadSide(-1);
		player.setOlympiadGameId(-1);
		player.sendPacket(new ExOlympiadMode(0));
		
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		player.clearCharges();
		
		final Summon summon = player.getSummon();
		if (summon != null && !summon.isDead())
			summon.stopAllEffectsExceptThoseThatLastThroughDeath();
		
		// Add Clan Skills
		if (player.getClan() != null)
		{
			player.getClan().addSkillEffects(player);
			
			// heal again after adding clan skills
			healPlayer(player);
		}
		
		// Add Hero Skills
		if (player.isHero())
		{
			for (L2Skill skill : SkillTable.getHeroSkills())
				player.addSkill(skill, false);
		}
		player.sendSkillList();
	}
	
	/**
	 * Teleport the {@link Player} back to his initial {@link Location}.
	 * @param player : The Player to teleport back.
	 */
	protected static final void portPlayerBack(Player player)
	{
		if (player == null)
			return;
		
		// Retrieve the initial Location.
		Location loc = player.getSavedLocation();
		if (loc.equals(Location.DUMMY_LOC))
			return;
		
		final TownZone town = MapRegionData.getTown(loc.getX(), loc.getY(), loc.getZ());
		if (town != null)
			loc = town.getRandomLoc();
		
		player.teleportTo(loc, 0);
		player.getSavedLocation().clean();
	}
	
	/**
	 * Reward a {@link Player} with items.
	 * @param player : The Player to reward.
	 * @param reward : The IntIntHolder container used as itemId / quantity holder.
	 */
	public static final void rewardParticipant(Player player, IntIntHolder[] reward)
	{
		if (player == null || !player.isOnline() || reward == null)
			return;
		
		final InventoryUpdate iu = new InventoryUpdate();
		for (IntIntHolder it : reward)
		{
			final ItemInstance item = player.getInventory().addItem("Olympiad", it.getId(), it.getValue(), player, null);
			if (item == null)
				continue;
			
			iu.addModifiedItem(item);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(it.getId()).addNumber(it.getValue()));
		}
		player.sendPacket(iu);
	}
}
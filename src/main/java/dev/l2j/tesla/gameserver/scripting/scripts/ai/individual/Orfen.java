package dev.l2j.tesla.gameserver.scripting.scripts.ai.individual;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import dev.l2j.tesla.commons.random.Rnd;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.data.manager.GrandBossManager;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.ZoneId;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.GrandBoss;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.model.location.SpawnLocation;
import dev.l2j.tesla.gameserver.model.spawn.L2Spawn;
import dev.l2j.tesla.gameserver.network.serverpackets.PlaySound;

public class Orfen extends L2AttackableAIScript
{
	private static final SpawnLocation[] ORFEN_LOCATION =
	{
		new SpawnLocation(43728, 17220, -4342, 0),
		new SpawnLocation(55024, 17368, -5412, 0),
		new SpawnLocation(53504, 21248, -5486, 0),
		new SpawnLocation(53248, 24576, -5262, 0)
	};
	
	private static final String[] ORFEN_CHAT =
	{
		"$s1. Stop kidding yourself about your own powerlessness!",
		"$s1. I'll make you feel what true fear is!",
		"You're really stupid to have challenged me. $s1! Get ready!",
		"$s1. Do you think that's going to work?!"
	};
	
	private static final int ORFEN = 29014;
	private static final int RAIKEL_LEOS = 29016;
	private static final int RIBA_IREN = 29018;
	
	private static final byte ALIVE = 0;
	private static final byte DEAD = 1;
	
	private static long _lastAttackTime = 0;
	private static boolean _isTeleported;
	private static int _currentIndex;
	
	public Orfen()
	{
		super("ai/individual");
		
		_isTeleported = false;
		
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(ORFEN);
		final int status = GrandBossManager.getInstance().getBossStatus(ORFEN);
		
		if (status == DEAD)
		{
			// load the unlock date and time for Orfen from DB
			long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			if (temp > 0)
			{
				// The time has not yet expired. Mark Orfen as currently locked (dead).
				startQuestTimer("orfen_unlock", temp, null, null, false);
			}
			else
			{
				// The time has already expired while the server was offline. Spawn Orfen in a random place.
				_currentIndex = Rnd.get(1, 3);
				
				final GrandBoss orfen = (GrandBoss) addSpawn(ORFEN, ORFEN_LOCATION[_currentIndex], false, 0, false);
				GrandBossManager.getInstance().setBossStatus(ORFEN, ALIVE);
				spawnBoss(orfen);
			}
		}
		else
		{
			final int loc_x = info.getInteger("loc_x");
			final int loc_y = info.getInteger("loc_y");
			final int loc_z = info.getInteger("loc_z");
			final int heading = info.getInteger("heading");
			final int hp = info.getInteger("currentHP");
			final int mp = info.getInteger("currentMP");
			
			final GrandBoss orfen = (GrandBoss) addSpawn(ORFEN, loc_x, loc_y, loc_z, heading, false, 0, false);
			orfen.setCurrentHpMp(hp, mp);
			spawnBoss(orfen);
		}
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(ORFEN, RIBA_IREN);
		addFactionCallId(RAIKEL_LEOS, RIBA_IREN);
		addKillId(ORFEN);
		addSkillSeeId(ORFEN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("orfen_unlock"))
		{
			_currentIndex = Rnd.get(1, 3);
			
			final GrandBoss orfen = (GrandBoss) addSpawn(ORFEN, ORFEN_LOCATION[_currentIndex], false, 0, false);
			GrandBossManager.getInstance().setBossStatus(ORFEN, ALIVE);
			spawnBoss(orfen);
		}
		else if (event.equalsIgnoreCase("check_orfen_pos"))
		{
			// 30 minutes are gone without any hit ; Orfen will move to another location.
			if (_lastAttackTime + 1800000 < System.currentTimeMillis())
			{
				// Generates a number until it is different of _currentIndex (avoid to spawn in same place 2 times).
				int index = _currentIndex;
				while (index == _currentIndex)
					index = Rnd.get(1, 3);
				
				// Set the new index as _currentIndex.
				_currentIndex = index;
				
				// Set the teleport flag to false
				_isTeleported = false;
				
				// Reinitialize the timer.
				_lastAttackTime = System.currentTimeMillis();
				
				goTo(npc, ORFEN_LOCATION[_currentIndex]);
			}
			// Orfen already ported once and is lured out of her lair ; teleport her back.
			else if (_isTeleported && !npc.isInsideZone(ZoneId.SWAMP))
				goTo(npc, ORFEN_LOCATION[0]);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet)
	{
		Creature originalCaster = isPet ? caster.getSummon() : caster;
		if (skill.getAggroPoints() > 0 && Rnd.get(5) == 0 && npc.isInsideRadius(originalCaster, 1000, false, false))
		{
			npc.broadcastNpcSay(Rnd.get(ORFEN_CHAT).replace("$s1", caster.getName()));
			originalCaster.teleportTo(npc.getX(), npc.getY(), npc.getZ(), 0);
			npc.setTarget(originalCaster);
			npc.doCast(SkillTable.getInstance().getInfo(4064, 1));
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet)
	{
		if (caller == null || npc == null || npc.isCastingNow())
			return super.onFactionCall(npc, caller, attacker, isPet);
		
		int npcId = npc.getNpcId();
		int callerId = caller.getNpcId();
		if (npcId == RAIKEL_LEOS && Rnd.get(20) == 0)
		{
			npc.setTarget(attacker);
			npc.doCast(SkillTable.getInstance().getInfo(4067, 4));
		}
		else if (npcId == RIBA_IREN)
		{
			int chance = 1;
			if (callerId == ORFEN)
				chance = 9;
			
			if (callerId != RIBA_IREN && (caller.getCurrentHp() / caller.getMaxHp() < 0.5) && Rnd.get(10) < chance)
			{
				npc.getAI().setIntention(IntentionType.IDLE, null, null);
				npc.setTarget(caller);
				npc.doCast(SkillTable.getInstance().getInfo(4516, 1));
			}
		}
		return super.onFactionCall(npc, caller, attacker, isPet);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		final Player player = attacker.getActingPlayer();
		if (player != null)
		{
			if (npc.getNpcId() == ORFEN)
			{
				// update a variable with the last action against Orfen.
				_lastAttackTime = System.currentTimeMillis();
				
				if (!_isTeleported && (npc.getCurrentHp() - damage) < (npc.getMaxHp() / 2))
				{
					_isTeleported = true;
					goTo(npc, ORFEN_LOCATION[0]);
				}
				else if (npc.isInsideRadius(player, 1000, false, false) && !npc.isInsideRadius(player, 300, false, false) && Rnd.get(10) == 0)
				{
					npc.broadcastNpcSay(ORFEN_CHAT[Rnd.get(3)].replace("$s1", player.getName()));
					player.teleportTo(npc.getX(), npc.getY(), npc.getZ(), 0);
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4064, 1));
				}
			}
			// RIBA_IREN case, as it's the only other registered.
			else
			{
				if (!npc.isCastingNow() && (npc.getCurrentHp() - damage) < (npc.getMaxHp() / 2.0))
				{
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4516, 1));
				}
			}
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
		GrandBossManager.getInstance().setBossStatus(ORFEN, DEAD);
		
		long respawnTime = (long) Config.SPAWN_INTERVAL_ORFEN + Rnd.get(-Config.RANDOM_SPAWN_TIME_ORFEN, Config.RANDOM_SPAWN_TIME_ORFEN);
		respawnTime *= 3600000;
		
		startQuestTimer("orfen_unlock", respawnTime, null, null, false);
		
		// also save the respawn time so that the info is maintained past reboots
		StatsSet info = GrandBossManager.getInstance().getStatsSet(ORFEN);
		info.set("respawn_time", System.currentTimeMillis() + respawnTime);
		GrandBossManager.getInstance().setStatsSet(ORFEN, info);
		
		cancelQuestTimer("check_orfen_pos", npc, null);
		return super.onKill(npc, killer);
	}
	
	/**
	 * This method is used by Orfen in order to move from one location to another.<br>
	 * Index 0 means a direct teleport to her lair (case where her HPs <= 50%).
	 * @param npc : Orfen in any case.
	 * @param index : 0 for her lair (teleport) or 1-3 (walking through desert).
	 */
	private static void goTo(Npc npc, SpawnLocation index)
	{
		((Attackable) npc).getAggroList().clear();
		npc.getAI().setIntention(IntentionType.IDLE, null, null);
		
		// Edit the spawn location in case server crashes.
		L2Spawn spawn = npc.getSpawn();
		spawn.setLoc(index);
		
		if (index.getX() == 43728) // Hack !
			npc.teleportTo(index.getX(), index.getY(), index.getZ(), 0);
		else
			npc.getAI().setIntention(IntentionType.MOVE_TO, new Location(index.getX(), index.getY(), index.getZ()));
	}
	
	private void spawnBoss(GrandBoss npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		npc.broadcastPacket(new PlaySound(1, "BS01_A", npc));
		startQuestTimer("check_orfen_pos", 60000, npc, null, true);
		
		// start monitoring Orfen's inactivity
		_lastAttackTime = System.currentTimeMillis();
	}
}
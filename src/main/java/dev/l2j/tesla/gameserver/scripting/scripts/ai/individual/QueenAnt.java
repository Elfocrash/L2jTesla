package dev.l2j.tesla.gameserver.scripting.scripts.ai.individual;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import dev.l2j.tesla.commons.random.Rnd;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.data.SkillTable.FrequentSkill;
import dev.l2j.tesla.gameserver.data.manager.GrandBossManager;
import dev.l2j.tesla.gameserver.data.manager.ZoneManager;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.ai.type.AttackableAI;
import dev.l2j.tesla.gameserver.model.actor.instance.GrandBoss;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.model.zone.ZoneType;
import dev.l2j.tesla.gameserver.model.zone.type.BossZone;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillUse;
import dev.l2j.tesla.gameserver.network.serverpackets.PlaySound;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class QueenAnt extends L2AttackableAIScript
{
	private static final BossZone ZONE = ZoneManager.getInstance().getZoneById(110017, BossZone.class);
	
	private static final int QUEEN = 29001;
	private static final int LARVA = 29002;
	private static final int NURSE = 29003;
	private static final int GUARD = 29004;
	private static final int ROYAL = 29005;
	
	private static final Location[] PLAYER_TELE_OUT =
	{
		new Location(-19480, 187344, -5600),
		new Location(-17928, 180912, -5520),
		new Location(-23808, 182368, -5600)
	};
	
	private static final byte ALIVE = 0;
	private static final byte DEAD = 1;
	
	private Monster _larva = null;
	
	public QueenAnt()
	{
		super("ai/individual");
		
		// Queen Ant is dead, calculate the respawn time. If passed, we spawn it directly, otherwise we set a task to spawn it lately.
		if (GrandBossManager.getInstance().getBossStatus(QUEEN) == DEAD)
		{
			final long temp = GrandBossManager.getInstance().getStatsSet(QUEEN).getLong("respawn_time") - System.currentTimeMillis();
			if (temp > 0)
				startQuestTimer("queen_unlock", temp, null, null, false);
			else
				spawnBoss(true);
		}
		// Queen Ant is alive, spawn it using stored data.
		else
			spawnBoss(false);
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(QUEEN, LARVA, NURSE, GUARD, ROYAL);
		addAggroRangeEnterId(LARVA, NURSE, GUARD, ROYAL);
		addFactionCallId(QUEEN, NURSE);
		addKillId(QUEEN, NURSE, ROYAL);
		addSkillSeeId(QUEEN, LARVA, NURSE, GUARD, ROYAL);
		addSpawnId(LARVA, NURSE);
		addExitZoneId(110017);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("action"))
		{
			// Animation timer.
			if (Rnd.get(10) < 3)
				npc.broadcastPacket(new SocialAction(npc, (Rnd.nextBoolean()) ? 3 : 4));
			
			// Teleport Royal Guards back in zone if out.
			((Monster) npc).getMinionList().getSpawnedMinions().stream().filter(m -> m.getNpcId() == ROYAL && !ZONE.isInsideZone(m)).forEach(m -> m.teleToMaster());
		}
		else if (event.equalsIgnoreCase("chaos"))
		{
			// Randomize the target for Royal Guards.
			((Monster) npc).getMinionList().getSpawnedMinions().stream().filter(m -> m.getNpcId() == ROYAL && m.isInCombat() && Rnd.get(100) < 66).forEach(m -> ((AttackableAI) m.getAI()).aggroReconsider());
			
			// Relaunch a new chaos task.
			startQuestTimer("chaos", 90000 + Rnd.get(240000), npc, null, false);
		}
		else if (event.equalsIgnoreCase("clean"))
		{
			// Delete the larva and the reference.
			_larva.deleteMe();
			_larva = null;
		}
		else if (event.equalsIgnoreCase("queen_unlock"))
		{
			// Choose a teleport location, and teleport players out of Queen Ant zone.
			if (Rnd.get(100) < 33)
				ZONE.movePlayersTo(PLAYER_TELE_OUT[0]);
			else if (Rnd.nextBoolean())
				ZONE.movePlayersTo(PLAYER_TELE_OUT[1]);
			else
				ZONE.movePlayersTo(PLAYER_TELE_OUT[2]);
			
			// Spawn the boss.
			spawnBoss(true);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAggro(Npc npc, Player player, boolean isPet)
	{
		final Playable realBypasser = (isPet && player.getSummon() != null) ? player.getSummon() : player;
		if (testCursesOnAggro(npc, realBypasser))
			return null;
		
		return super.onAggro(npc, player, isPet);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable)
		{
			// Curses
			if (testCursesOnAttack(npc, attacker, QUEEN))
				return null;
			
			// Pick current attacker, and make actions based on it and the actual distance range seperating them.
			if (npc.getNpcId() == QUEEN && !npc.isCastingNow())
			{
				if (skill != null && skill.getElement() == 2 && Rnd.get(100) < 70)
				{
					npc.setTarget(attacker);
					((Monster) npc).useMagic(FrequentSkill.QUEEN_ANT_STRIKE.getSkill());
				}
				else
				{
					final double dist = Math.sqrt(npc.getPlanDistanceSq(attacker.getX(), attacker.getY()));
					if (dist > 500 && Rnd.get(100) < 10)
					{
						npc.setTarget(attacker);
						((Monster) npc).useMagic(FrequentSkill.QUEEN_ANT_STRIKE.getSkill());
					}
					else if (dist > 150 && Rnd.get(100) < 10)
					{
						npc.setTarget(attacker);
						((Monster) npc).useMagic((Rnd.get(10) < 8) ? FrequentSkill.QUEEN_ANT_STRIKE.getSkill() : FrequentSkill.QUEEN_ANT_SPRINKLE.getSkill());
					}
					else if (dist < 250 && Rnd.get(100) < 5)
					{
						npc.setTarget(attacker);
						((Monster) npc).useMagic(FrequentSkill.QUEEN_ANT_BRANDISH.getSkill());
					}
				}
			}
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
	
	@Override
	public String onExitZone(Creature character, ZoneType zone)
	{
		if (character instanceof GrandBoss)
		{
			final GrandBoss queen = (GrandBoss) character;
			if (queen.getNpcId() == QUEEN)
				queen.teleportTo(-21610, 181594, -5734, 0);
		}
		return super.onExitZone(character, zone);
	}
	
	@Override
	public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet)
	{
		if (npc.isCastingNow())
			return null;
		
		switch (npc.getNpcId())
		{
			case QUEEN:
				// Pick current attacker, and make actions based on it and the actual distance range seperating them.
				final Playable realAttacker = (isPet && attacker.getSummon() != null) ? attacker.getSummon() : attacker;
				final double dist = Math.sqrt(npc.getPlanDistanceSq(realAttacker.getX(), realAttacker.getY()));
				if (dist > 500 && Rnd.get(100) < 3)
				{
					npc.setTarget(realAttacker);
					((Monster) npc).useMagic(FrequentSkill.QUEEN_ANT_STRIKE.getSkill());
				}
				else if (dist > 150 && Rnd.get(100) < 3)
				{
					npc.setTarget(realAttacker);
					((Monster) npc).useMagic((Rnd.get(10) < 8) ? FrequentSkill.QUEEN_ANT_STRIKE.getSkill() : FrequentSkill.QUEEN_ANT_SPRINKLE.getSkill());
				}
				else if (dist < 250 && Rnd.get(100) < 2)
				{
					npc.setTarget(realAttacker);
					((Monster) npc).useMagic(FrequentSkill.QUEEN_ANT_BRANDISH.getSkill());
				}
				break;
			
			case NURSE:
				// If the faction caller is the larva, assist it directly, no matter what.
				if (caller.getNpcId() == LARVA)
				{
					npc.setTarget(caller);
					((Monster) npc).useMagic(Rnd.nextBoolean() ? FrequentSkill.NURSE_HEAL_1.getSkill() : FrequentSkill.NURSE_HEAL_2.getSkill());
				}
				// If the faction caller is Queen Ant, then check first Larva.
				else if (caller.getNpcId() == QUEEN)
				{
					if (_larva != null && _larva.getCurrentHp() < _larva.getMaxHp())
					{
						npc.setTarget(_larva);
						((Monster) npc).useMagic(Rnd.nextBoolean() ? FrequentSkill.NURSE_HEAL_1.getSkill() : FrequentSkill.NURSE_HEAL_2.getSkill());
					}
					else
					{
						npc.setTarget(caller);
						((Attackable) npc).useMagic(FrequentSkill.NURSE_HEAL_1.getSkill());
					}
				}
				break;
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		if (npc.getNpcId() == QUEEN)
		{
			// Broadcast death sound.
			npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
			
			// Flag Queen Ant as dead.
			GrandBossManager.getInstance().setBossStatus(QUEEN, DEAD);
			
			// Calculate the next respawn time.
			final long respawnTime = (long) (Config.SPAWN_INTERVAL_AQ + Rnd.get(-Config.RANDOM_SPAWN_TIME_AQ, Config.RANDOM_SPAWN_TIME_AQ)) * 3600000;
			
			// Cancel tasks.
			cancelQuestTimer("action", npc, null);
			cancelQuestTimer("chaos", npc, null);
			
			// Start respawn timer, and clean the monster references.
			startQuestTimer("queen_unlock", respawnTime, null, null, false);
			startQuestTimer("clean", 5000, null, null, false);
			
			// Save the respawn time so that the info is maintained past reboots
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(QUEEN);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(QUEEN, info);
		}
		else
		{
			// Set the respawn time of Royal Guards and Nurses. Pick the npc master.
			final Monster minion = ((Monster) npc);
			final Monster master = minion.getMaster();
			
			if (master != null && master.hasMinions())
				master.getMinionList().onMinionDie(minion, (npc.getNpcId() == NURSE) ? 10000 : (280000 + (Rnd.get(40) * 1000)));
			
			return null;
		}
		return super.onKill(npc, killer);
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet)
	{
		final Playable realAttacker = (isPet && caster.getSummon() != null) ? caster.getSummon() : caster;
		if (!Config.RAID_DISABLE_CURSE && realAttacker.getLevel() - npc.getLevel() > 8)
		{
			final L2Skill curse = FrequentSkill.RAID_CURSE.getSkill();
			
			npc.broadcastPacket(new MagicSkillUse(npc, realAttacker, curse.getId(), curse.getLevel(), 300, 0));
			curse.getEffects(npc, realAttacker);
			
			((Attackable) npc).stopHating(realAttacker);
			return null;
		}
		
		// If Queen Ant see an aggroable skill, try to launch Queen Ant Strike.
		if (npc.getNpcId() == QUEEN && !npc.isCastingNow() && skill.getAggroPoints() > 0 && Rnd.get(100) < 15)
		{
			npc.setTarget(realAttacker);
			((Monster) npc).useMagic(FrequentSkill.QUEEN_ANT_STRIKE.getSkill());
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		switch (npc.getNpcId())
		{
			case LARVA:
				npc.setIsMortal(false);
				npc.setIsImmobilized(true);
			case NURSE:
				npc.disableCoreAI(true);
				break;
		}
		return super.onSpawn(npc);
	}
	
	/**
	 * Make additional actions on boss spawn : register the NPC as boss, activate tasks, spawn the larva.
	 * @param freshStart : If true, it uses static data, otherwise it uses stored data.
	 */
	private void spawnBoss(boolean freshStart)
	{
		final GrandBoss queen;
		if (freshStart)
		{
			GrandBossManager.getInstance().setBossStatus(QUEEN, ALIVE);
			
			queen = (GrandBoss) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0, false);
		}
		else
		{
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(QUEEN);
			
			queen = (GrandBoss) addSpawn(QUEEN, info.getInteger("loc_x"), info.getInteger("loc_y"), info.getInteger("loc_z"), info.getInteger("heading"), false, 0, false);
			queen.setCurrentHpMp(info.getInteger("currentHP"), info.getInteger("currentMP"));
		}
		
		GrandBossManager.getInstance().addBoss(queen);
		
		startQuestTimer("action", 10000, queen, null, true);
		startQuestTimer("chaos", 90000 + Rnd.get(240000), queen, null, false);
		
		queen.broadcastPacket(new PlaySound(1, "BS01_A", queen));
		
		_larva = (Monster) addSpawn(LARVA, -21600, 179482, -5846, Rnd.get(360), false, 0, false);
	}
}
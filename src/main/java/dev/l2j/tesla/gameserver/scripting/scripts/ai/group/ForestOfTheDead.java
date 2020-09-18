package dev.l2j.tesla.gameserver.scripting.scripts.ai.group;

import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import dev.l2j.tesla.gameserver.taskmanager.GameTimeTaskManager;
import dev.l2j.tesla.gameserver.data.manager.RaidBossManager;
import dev.l2j.tesla.gameserver.enums.BossStatus;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.location.SpawnLocation;
import dev.l2j.tesla.gameserver.model.spawn.BossSpawn;

public class ForestOfTheDead extends L2AttackableAIScript
{
	public static final SpawnLocation HELLMANN_DAY_LOC = new SpawnLocation(-104100, -252700, -15542, 0);
	public static final SpawnLocation HELLMANN_NIGHT_LOC = new SpawnLocation(59050, -42200, -3003, 100);
	
	private static final String qn = "Q024_InhabitantsOfTheForestOfTheDead";
	
	private static final int HELLMANN = 25328;
	private static final int NIGHT_DORIAN = 25332;
	private static final int LIDIA_MAID = 31532;
	
	private static final int DAY_VIOLET = 31386;
	private static final int DAY_KURSTIN = 31387;
	private static final int DAY_MINA = 31388;
	private static final int DAY_DORIAN = 31389;
	
	private static final int SILVER_CROSS = 7153;
	private static final int BROKEN_SILVER_CROSS = 7154;
	
	private final List<Npc> _cursedVillageNpcs = new ArrayList<>();
	
	private final Npc _lidiaMaid;
	
	public ForestOfTheDead()
	{
		super("ai/group");
		
		// Initial List feed. NPCs are then unspawned if night is occuring.
		_cursedVillageNpcs.add(addSpawn(DAY_VIOLET, 59618, -42774, -3000, 5636, false, 0, false));
		_cursedVillageNpcs.add(addSpawn(DAY_KURSTIN, 58790, -42646, -3000, 240, false, 0, false));
		_cursedVillageNpcs.add(addSpawn(DAY_MINA, 59626, -41684, -3000, 48457, false, 0, false));
		_cursedVillageNpcs.add(addSpawn(DAY_DORIAN, 60161, -42086, -3000, 30212, false, 0, false));
		
		_lidiaMaid = addSpawn(LIDIA_MAID, 47108, -36189, -1624, -22192, false, 0, false);
		
		if (!GameTimeTaskManager.getInstance().isNight())
		{
			final BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(HELLMANN);
			if (bs != null && bs.getStatus() == BossStatus.ALIVE)
			{
				final Npc raid = bs.getBoss();
				
				raid.getSpawn().setLoc(ForestOfTheDead.HELLMANN_DAY_LOC);
				raid.teleportTo(ForestOfTheDead.HELLMANN_DAY_LOC, 0);
			}
			
			_lidiaMaid.getSpawn().setRespawnState(false);
			_lidiaMaid.deleteMe();
		}
		else
		{
			despawnCursedVillageNpcs();
			
			_lidiaMaid.getSpawn().setRespawnState(true);
			_lidiaMaid.getSpawn().doSpawn(false);
		}
	}
	
	@Override
	protected void registerNpcs()
	{
		addCreatureSeeId(NIGHT_DORIAN);
		
		addGameTimeNotify();
	}
	
	@Override
	public String onCreatureSee(Npc npc, Creature creature)
	{
		if (creature instanceof Player)
		{
			final Player player = creature.getActingPlayer();
			
			final QuestState st = player.getQuestState(qn);
			if (st == null)
				return super.onCreatureSee(npc, creature);
			
			if (st.getInt("cond") == 3)
			{
				st.set("cond", "4");
				st.takeItems(SILVER_CROSS, -1);
				st.giveItems(BROKEN_SILVER_CROSS, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
				npc.broadcastNpcSay("That sign!");
			}
		}
		return super.onCreatureSee(npc, creature);
	}
	
	@Override
	public void onGameTime()
	{
		// Hellmann despawns at day.
		if (GameTimeTaskManager.getInstance().getGameTime() == 360)
		{
			final BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(HELLMANN);
			if (bs != null && bs.getStatus() == BossStatus.ALIVE)
			{
				final Npc raid = bs.getBoss();
				
				raid.getSpawn().setLoc(HELLMANN_DAY_LOC);
				raid.teleportTo(HELLMANN_DAY_LOC, 0);
			}
			spawnCursedVillageNpcs();
		}
		// And spawns at night.
		else if (GameTimeTaskManager.getInstance().getGameTime() == 0)
		{
			final BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(HELLMANN);
			if (bs != null && bs.getStatus() == BossStatus.ALIVE)
			{
				final Npc raid = bs.getBoss();
				
				raid.getSpawn().setLoc(HELLMANN_NIGHT_LOC);
				raid.teleportTo(HELLMANN_NIGHT_LOC, 0);
			}
			despawnCursedVillageNpcs();
		}
	}
	
	private void spawnCursedVillageNpcs()
	{
		for (Npc npc : _cursedVillageNpcs)
		{
			npc.getSpawn().setRespawnState(true);
			npc.getSpawn().doSpawn(false);
		}
	}
	
	private void despawnCursedVillageNpcs()
	{
		for (Npc npc : _cursedVillageNpcs)
		{
			npc.getSpawn().setRespawnState(false);
			npc.deleteMe();
		}
	}
}
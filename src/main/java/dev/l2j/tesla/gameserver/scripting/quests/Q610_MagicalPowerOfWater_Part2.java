package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.manager.RaidBossManager;
import dev.l2j.tesla.gameserver.enums.BossStatus;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.spawn.BossSpawn;

public class Q610_MagicalPowerOfWater_Part2 extends Quest
{
	private static final String qn = "Q610_MagicalPowerOfWater_Part2";
	
	// Monster
	private static final int SOUL_OF_WATER_ASHUTAR = 25316;
	
	// NPCs
	private static final int ASEFA = 31372;
	private static final int VARKAS_HOLY_ALTAR = 31560;
	
	// Items
	private static final int GREEN_TOTEM = 7238;
	private static final int ICE_HEART_OF_ASHUTAR = 7239;
	
	// Other
	private static final int CHECK_INTERVAL = 600000; // 10 minutes
	private static final int IDLE_INTERVAL = 2; // (X * CHECK_INTERVAL) = 20 minutes
	
	private Npc _npc = null;
	private int _status = -1;
	
	public Q610_MagicalPowerOfWater_Part2()
	{
		super(610, "Magical Power of Water - Part 2");
		
		setItemsIds(ICE_HEART_OF_ASHUTAR);
		
		addStartNpc(ASEFA);
		addTalkId(ASEFA, VARKAS_HOLY_ALTAR);
		
		addAttackId(SOUL_OF_WATER_ASHUTAR);
		addKillId(SOUL_OF_WATER_ASHUTAR);
		
		switch (RaidBossManager.getInstance().getStatus(SOUL_OF_WATER_ASHUTAR))
		{
			case ALIVE:
				spawnNpc();
			case DEAD:
				startQuestTimer("check", CHECK_INTERVAL, null, null, true);
				break;
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		// global quest timer has player==null -> cannot get QuestState
		if (event.equals("check"))
		{
			final BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(SOUL_OF_WATER_ASHUTAR);
			if (bs != null && bs.getStatus() == BossStatus.ALIVE)
			{
				final Npc raid = bs.getBoss();
				
				if (_status >= 0 && _status-- == 0)
					despawnRaid(raid);
				
				spawnNpc();
			}
			
			return null;
		}
		
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// Asefa
		if (event.equalsIgnoreCase("31372-04.htm"))
		{
			if (st.hasQuestItems(GREEN_TOTEM))
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
			else
				htmltext = "31372-02.htm";
		}
		else if (event.equalsIgnoreCase("31372-07.htm"))
		{
			if (st.hasQuestItems(ICE_HEART_OF_ASHUTAR))
			{
				st.takeItems(ICE_HEART_OF_ASHUTAR, 1);
				st.rewardExpAndSp(10000, 0);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				htmltext = "31372-08.htm";
		}
		// Varka's Holy Altar
		else if (event.equalsIgnoreCase("31560-02.htm"))
		{
			if (st.hasQuestItems(GREEN_TOTEM))
			{
				if (_status < 0)
				{
					if (spawnRaid())
					{
						st.set("cond", "2");
						st.playSound(QuestState.SOUND_MIDDLE);
						st.takeItems(GREEN_TOTEM, 1);
					}
				}
				else
					htmltext = "31560-04.htm";
			}
			else
				htmltext = "31560-03.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (!st.hasQuestItems(GREEN_TOTEM))
					htmltext = "31372-02.htm";
				else if (player.getLevel() < 75 && player.getAllianceWithVarkaKetra() < 2)
					htmltext = "31372-03.htm";
				else
					htmltext = "31372-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ASEFA:
						htmltext = (cond < 3) ? "31372-05.htm" : "31372-06.htm";
						break;
					
					case VARKAS_HOLY_ALTAR:
						if (cond == 1)
							htmltext = "31560-01.htm";
						else if (cond == 2)
							htmltext = "31560-05.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		final Player player = attacker.getActingPlayer();
		if (player != null)
			_status = IDLE_INTERVAL;
		
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		if (player != null)
		{
			for (QuestState st : getPartyMembers(player, npc, "cond", "2"))
			{
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(ICE_HEART_OF_ASHUTAR, 1);
			}
		}
		
		// despawn raid (reset info)
		despawnRaid(npc);
		
		// despawn npc
		if (_npc != null)
		{
			_npc.deleteMe();
			_npc = null;
		}
		
		return null;
	}
	
	private void spawnNpc()
	{
		// spawn npc, if not spawned
		if (_npc == null)
			_npc = addSpawn(VARKAS_HOLY_ALTAR, 105452, -36775, -1050, 34000, false, 0, false);
	}
	
	private boolean spawnRaid()
	{
		final BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(SOUL_OF_WATER_ASHUTAR);
		if (bs != null && bs.getStatus() == BossStatus.ALIVE)
		{
			final Npc raid = bs.getBoss();
			
			// set temporarily spawn location (to provide correct behavior of checkAndReturnToSpawn())
			raid.getSpawn().setLoc(104771, -36993, -1149, Rnd.get(65536));
			
			// teleport raid from secret place
			raid.teleportTo(104771, -36993, -1149, 100);
			raid.broadcastNpcSay("The water charm then is the storm and the tsunami strength! Opposes with it only has the blind alley!");
			
			// set raid status
			_status = IDLE_INTERVAL;
			
			return true;
		}
		
		return false;
	}
	
	private void despawnRaid(Npc raid)
	{
		// reset spawn location
		raid.getSpawn().setLoc(-105900, -252700, -15542, 0);
		
		// teleport raid back to secret place
		if (!raid.isDead())
			raid.teleportTo(-105900, -252700, -15542, 0);
		
		// reset raid status
		_status = -1;
	}
}
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

public class Q604_DaimonTheWhiteEyed_Part2 extends Quest
{
	private static final String qn = "Q604_DaimonTheWhiteEyed_Part2";
	
	// Monster
	private static final int DAIMON_THE_WHITE_EYED = 25290;
	
	// NPCs
	private static final int EYE_OF_ARGOS = 31683;
	private static final int DAIMON_ALTAR = 31541;
	
	// Items
	private static final int UNFINISHED_SUMMON_CRYSTAL = 7192;
	private static final int SUMMON_CRYSTAL = 7193;
	private static final int ESSENCE_OF_DAIMON = 7194;
	private static final int REWARDS[] =
	{
		4595,
		4596,
		4597,
		4598,
		4599,
		4600
	};
	
	// Other
	private static final int CHECK_INTERVAL = 600000; // 10 minutes
	private static final int IDLE_INTERVAL = 3; // (X * CHECK_INTERVAL) = 30 minutes
	
	private Npc _npc = null;
	private int _status = -1;
	
	public Q604_DaimonTheWhiteEyed_Part2()
	{
		super(604, "Daimon The White-Eyed - Part 2");
		
		setItemsIds(SUMMON_CRYSTAL, ESSENCE_OF_DAIMON);
		
		addStartNpc(EYE_OF_ARGOS);
		addTalkId(EYE_OF_ARGOS, DAIMON_ALTAR);
		
		addAttackId(DAIMON_THE_WHITE_EYED);
		addKillId(DAIMON_THE_WHITE_EYED);
		
		switch (RaidBossManager.getInstance().getStatus(DAIMON_THE_WHITE_EYED))
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
			final BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(DAIMON_THE_WHITE_EYED);
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
		
		// Eye of Argos
		if (event.equalsIgnoreCase("31683-03.htm"))
		{
			if (st.hasQuestItems(UNFINISHED_SUMMON_CRYSTAL))
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
				st.takeItems(UNFINISHED_SUMMON_CRYSTAL, 1);
				st.giveItems(SUMMON_CRYSTAL, 1);
			}
			else
				htmltext = "31683-04.htm";
		}
		else if (event.equalsIgnoreCase("31683-08.htm"))
		{
			if (st.hasQuestItems(ESSENCE_OF_DAIMON))
			{
				st.takeItems(ESSENCE_OF_DAIMON, 1);
				st.rewardItems(Rnd.get(REWARDS), 5);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				htmltext = "31683-09.htm";
		}
		// Diamon's Altar
		else if (event.equalsIgnoreCase("31541-02.htm"))
		{
			if (st.hasQuestItems(SUMMON_CRYSTAL))
			{
				if (_status < 0)
				{
					if (spawnRaid())
					{
						st.set("cond", "2");
						st.playSound(QuestState.SOUND_MIDDLE);
						st.takeItems(SUMMON_CRYSTAL, 1);
					}
				}
				else
					htmltext = "31541-04.htm";
			}
			else
				htmltext = "31541-03.htm";
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
				if (player.getLevel() < 73)
				{
					htmltext = "31683-02.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "31683-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case EYE_OF_ARGOS:
						if (cond == 1)
							htmltext = "31683-05.htm";
						else if (cond == 2)
							htmltext = "31683-06.htm";
						else
							htmltext = "31683-07.htm";
						break;
					
					case DAIMON_ALTAR:
						if (cond == 1)
							htmltext = "31541-01.htm";
						else if (cond == 2)
							htmltext = "31541-05.htm";
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
				st.giveItems(ESSENCE_OF_DAIMON, 1);
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
			_npc = addSpawn(DAIMON_ALTAR, 186304, -43744, -3193, 57000, false, 0, false);
	}
	
	private boolean spawnRaid()
	{
		final BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(DAIMON_THE_WHITE_EYED);
		if (bs != null && bs.getStatus() == BossStatus.ALIVE)
		{
			final Npc raid = bs.getBoss();
			
			// set temporarily spawn location (to provide correct behavior of checkAndReturnToSpawn())
			raid.getSpawn().setLoc(185900, -44000, -3160, Rnd.get(65536));
			
			// teleport raid from secret place
			raid.teleportTo(185900, -44000, -3160, 100);
			raid.broadcastNpcSay("Who called me?");
			
			// set raid status
			_status = IDLE_INTERVAL;
			
			return true;
		}
		
		return false;
	}
	
	private void despawnRaid(Npc raid)
	{
		// reset spawn location
		raid.getSpawn().setLoc(-106500, -252700, -15542, 0);
		
		// teleport raid back to secret place
		if (!raid.isDead())
			raid.teleportTo(-106500, -252700, -15542, 0);
		
		// reset raid status
		_status = -1;
	}
}
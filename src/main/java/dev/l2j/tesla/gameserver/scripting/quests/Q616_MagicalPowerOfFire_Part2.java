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

public class Q616_MagicalPowerOfFire_Part2 extends Quest
{
	private static final String qn = "Q616_MagicalPowerOfFire_Part2";
	
	// Monster
	private static final int SOUL_OF_FIRE_NASTRON = 25306;
	
	// NPCs
	private static final int UDAN_MARDUI = 31379;
	private static final int KETRAS_HOLY_ALTAR = 31558;
	
	// Items
	private static final int RED_TOTEM = 7243;
	private static final int FIRE_HEART_OF_NASTRON = 7244;
	
	// Other
	private static final int CHECK_INTERVAL = 600000; // 10 minutes
	private static final int IDLE_INTERVAL = 2; // (X * CHECK_INTERVAL) = 20 minutes
	
	private Npc _npc = null;
	private int _status = -1;
	
	public Q616_MagicalPowerOfFire_Part2()
	{
		super(616, "Magical Power of Fire - Part 2");
		
		setItemsIds(FIRE_HEART_OF_NASTRON);
		
		addStartNpc(UDAN_MARDUI);
		addTalkId(UDAN_MARDUI, KETRAS_HOLY_ALTAR);
		
		addAttackId(SOUL_OF_FIRE_NASTRON);
		addKillId(SOUL_OF_FIRE_NASTRON);
		
		switch (RaidBossManager.getInstance().getStatus(SOUL_OF_FIRE_NASTRON))
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
			final BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(SOUL_OF_FIRE_NASTRON);
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
		
		// Udan Mardui
		if (event.equalsIgnoreCase("31379-04.htm"))
		{
			if (st.hasQuestItems(RED_TOTEM))
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
			else
				htmltext = "31379-02.htm";
		}
		else if (event.equalsIgnoreCase("31379-08.htm"))
		{
			if (st.hasQuestItems(FIRE_HEART_OF_NASTRON))
			{
				st.takeItems(FIRE_HEART_OF_NASTRON, 1);
				st.rewardExpAndSp(10000, 0);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				htmltext = "31379-09.htm";
		}
		// Ketra's Holy Altar
		else if (event.equalsIgnoreCase("31558-02.htm"))
		{
			if (st.hasQuestItems(RED_TOTEM))
			{
				if (_status < 0)
				{
					if (spawnRaid())
					{
						st.set("cond", "2");
						st.playSound(QuestState.SOUND_MIDDLE);
						st.takeItems(RED_TOTEM, 1);
					}
				}
				else
					htmltext = "31558-04.htm";
			}
			else
				htmltext = "31558-03.htm";
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
				if (!st.hasQuestItems(RED_TOTEM))
					htmltext = "31379-02.htm";
				else if (player.getLevel() < 75 && player.getAllianceWithVarkaKetra() > -2)
					htmltext = "31379-03.htm";
				else
					htmltext = "31379-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case UDAN_MARDUI:
						if (cond == 1)
							htmltext = "31379-05.htm";
						else if (cond == 2)
							htmltext = "31379-06.htm";
						else
							htmltext = "31379-07.htm";
						break;
					
					case KETRAS_HOLY_ALTAR:
						if (cond == 1)
							htmltext = "31558-01.htm";
						else if (cond == 2)
							htmltext = "31558-05.htm";
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
				st.giveItems(FIRE_HEART_OF_NASTRON, 1);
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
			_npc = addSpawn(KETRAS_HOLY_ALTAR, 142368, -82512, -6487, 58000, false, 0, false);
	}
	
	private boolean spawnRaid()
	{
		final BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(SOUL_OF_FIRE_NASTRON);
		if (bs != null && bs.getStatus() == BossStatus.ALIVE)
		{
			final Npc raid = bs.getBoss();
			
			// set temporarily spawn location (to provide correct behavior of checkAndReturnToSpawn())
			raid.getSpawn().setLoc(142624, -82285, -6491, Rnd.get(65536));
			
			// teleport raid from secret place
			raid.teleportTo(142624, -82285, -6491, 100);
			
			// set raid status
			_status = IDLE_INTERVAL;
			
			return true;
		}
		
		return false;
	}
	
	private void despawnRaid(Npc raid)
	{
		// reset spawn location
		raid.getSpawn().setLoc(-105300, -252700, -15542, 0);
		
		// teleport raid back to secret place
		if (!raid.isDead())
			raid.teleportTo(-105300, -252700, -15542, 0);
		
		// reset raid status
		_status = -1;
	}
}
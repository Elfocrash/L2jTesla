package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q354_ConquestOfAlligatorIsland extends Quest
{
	private static final String qn = "Q354_ConquestOfAlligatorIsland";
	
	// Items
	private static final int ALLIGATOR_TOOTH = 5863;
	private static final int TORN_MAP_FRAGMENT = 5864;
	private static final int PIRATE_TREASURE_MAP = 5915;
	
	private static final Map<Integer, int[][]> DROPLIST = new HashMap<>();
	{
		DROPLIST.put(20804, new int[][]
		{
			{
				ALLIGATOR_TOOTH,
				1,
				0,
				490000
			},
			{
				TORN_MAP_FRAGMENT,
				1,
				0,
				100000
			}
		}); // Crokian Lad
		DROPLIST.put(20805, new int[][]
		{
			{
				ALLIGATOR_TOOTH,
				1,
				0,
				560000
			},
			{
				TORN_MAP_FRAGMENT,
				1,
				0,
				100000
			}
		}); // Dailaon Lad
		DROPLIST.put(20806, new int[][]
		{
			{
				ALLIGATOR_TOOTH,
				1,
				0,
				500000
			},
			{
				TORN_MAP_FRAGMENT,
				1,
				0,
				100000
			}
		}); // Crokian Lad Warrior
		DROPLIST.put(20807, new int[][]
		{
			{
				ALLIGATOR_TOOTH,
				1,
				0,
				600000
			},
			{
				TORN_MAP_FRAGMENT,
				1,
				0,
				100000
			}
		}); // Farhite Lad
		DROPLIST.put(20808, new int[][]
		{
			{
				ALLIGATOR_TOOTH,
				1,
				0,
				690000
			},
			{
				TORN_MAP_FRAGMENT,
				1,
				0,
				100000
			}
		}); // Nos Lad
		DROPLIST.put(20991, new int[][]
		{
			{
				ALLIGATOR_TOOTH,
				1,
				0,
				600000
			},
			{
				TORN_MAP_FRAGMENT,
				1,
				0,
				100000
			}
		}); // Swamp Tribe
	}
	
	public Q354_ConquestOfAlligatorIsland()
	{
		super(354, "Conquest of Alligator Island");
		
		setItemsIds(ALLIGATOR_TOOTH, TORN_MAP_FRAGMENT);
		
		addStartNpc(30895); // Kluck
		addTalkId(30895);
		
		addKillId(20804, 20805, 20806, 20807, 20808, 20991);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30895-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30895-03.htm"))
		{
			if (st.hasQuestItems(TORN_MAP_FRAGMENT))
				htmltext = "30895-03a.htm";
		}
		else if (event.equalsIgnoreCase("30895-05.htm"))
		{
			final int amount = st.getQuestItemsCount(ALLIGATOR_TOOTH);
			if (amount > 0)
			{
				int reward = amount * 220 + 3100;
				if (amount >= 100)
				{
					reward += 7600;
					htmltext = "30895-05b.htm";
				}
				else
					htmltext = "30895-05a.htm";
				
				st.takeItems(ALLIGATOR_TOOTH, -1);
				st.rewardItems(57, reward);
			}
		}
		else if (event.equalsIgnoreCase("30895-07.htm"))
		{
			if (st.getQuestItemsCount(TORN_MAP_FRAGMENT) >= 10)
			{
				htmltext = "30895-08.htm";
				st.takeItems(TORN_MAP_FRAGMENT, 10);
				st.giveItems(PIRATE_TREASURE_MAP, 1);
				st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		else if (event.equalsIgnoreCase("30895-09.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				htmltext = (player.getLevel() < 38) ? "30895-00.htm" : "30895-01.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.hasQuestItems(TORN_MAP_FRAGMENT)) ? "30895-03a.htm" : "30895-03.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		st.dropMultipleItems(DROPLIST.get(npc.getNpcId()));
		
		return null;
	}
}
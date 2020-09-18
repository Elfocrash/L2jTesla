package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q631_DeliciousTopChoiceMeat extends Quest
{
	private static final String qn = "Q631_DeliciousTopChoiceMeat";
	
	// NPC
	private static final int TUNATUN = 31537;
	
	// Item
	private static final int TOP_QUALITY_MEAT = 7546;
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	{
		CHANCES.put(21460, 601000);
		CHANCES.put(21461, 480000);
		CHANCES.put(21462, 447000);
		CHANCES.put(21463, 808000);
		CHANCES.put(21464, 447000);
		CHANCES.put(21465, 808000);
		CHANCES.put(21466, 447000);
		CHANCES.put(21467, 808000);
		CHANCES.put(21479, 477000);
		CHANCES.put(21480, 863000);
		CHANCES.put(21481, 477000);
		CHANCES.put(21482, 863000);
		CHANCES.put(21483, 477000);
		CHANCES.put(21484, 863000);
		CHANCES.put(21485, 477000);
		CHANCES.put(21486, 863000);
		CHANCES.put(21498, 509000);
		CHANCES.put(21499, 920000);
		CHANCES.put(21500, 509000);
		CHANCES.put(21501, 920000);
		CHANCES.put(21502, 509000);
		CHANCES.put(21503, 920000);
		CHANCES.put(21504, 509000);
		CHANCES.put(21505, 920000);
	}
	
	// Rewards
	private static final int[][] REWARDS =
	{
		{
			4039,
			15
		},
		{
			4043,
			15
		},
		{
			4044,
			15
		},
		{
			4040,
			10
		},
		{
			4042,
			10
		},
		{
			4041,
			5
		}
	};
	
	public Q631_DeliciousTopChoiceMeat()
	{
		super(631, "Delicious Top Choice Meat");
		
		setItemsIds(TOP_QUALITY_MEAT);
		
		addStartNpc(TUNATUN);
		addTalkId(TUNATUN);
		
		for (int npcId : CHANCES.keySet())
			addKillId(npcId);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31537-03.htm"))
		{
			if (player.getLevel() >= 65)
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
			else
			{
				htmltext = "31537-02.htm";
				st.exitQuest(true);
			}
		}
		else if (StringUtil.isDigit(event))
		{
			if (st.getQuestItemsCount(TOP_QUALITY_MEAT) >= 120)
			{
				htmltext = "31537-06.htm";
				st.takeItems(TOP_QUALITY_MEAT, -1);
				
				int[] reward = REWARDS[Integer.parseInt(event)];
				st.rewardItems(reward[0], reward[1]);
				
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
			{
				st.set("cond", "1");
				htmltext = "31537-07.htm";
			}
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
				htmltext = "31537-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "31537-03a.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(TOP_QUALITY_MEAT) >= 120)
						htmltext = "31537-04.htm";
					else
					{
						st.set("cond", "1");
						htmltext = "31537-03a.htm";
					}
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = getRandomPartyMember(player, npc, "1");
		if (st == null)
			return null;
		
		if (st.dropItems(TOP_QUALITY_MEAT, 1, 120, CHANCES.get(npc.getNpcId())))
			st.set("cond", "2");
		
		return null;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.lang.StringUtil;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q617_GatherTheFlames extends Quest
{
	private static final String qn = "Q617_GatherTheFlames";
	
	// NPCs
	private static final int HILDA = 31271;
	private static final int VULCAN = 31539;
	private static final int ROONEY = 32049;
	
	// Items
	private static final int TORCH = 7264;
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	{
		CHANCES.put(21381, 510000);
		CHANCES.put(21653, 510000);
		CHANCES.put(21387, 530000);
		CHANCES.put(21655, 530000);
		CHANCES.put(21390, 560000);
		CHANCES.put(21656, 690000);
		CHANCES.put(21389, 550000);
		CHANCES.put(21388, 530000);
		CHANCES.put(21383, 510000);
		CHANCES.put(21392, 560000);
		CHANCES.put(21382, 600000);
		CHANCES.put(21654, 520000);
		CHANCES.put(21384, 640000);
		CHANCES.put(21394, 510000);
		CHANCES.put(21395, 560000);
		CHANCES.put(21385, 520000);
		CHANCES.put(21391, 550000);
		CHANCES.put(21393, 580000);
		CHANCES.put(21657, 570000);
		CHANCES.put(21386, 520000);
		CHANCES.put(21652, 490000);
		CHANCES.put(21378, 490000);
		CHANCES.put(21376, 480000);
		CHANCES.put(21377, 480000);
		CHANCES.put(21379, 590000);
		CHANCES.put(21380, 490000);
	}
	
	// Rewards
	private static final int REWARDS[] =
	{
		6881,
		6883,
		6885,
		6887,
		6891,
		6893,
		6895,
		6897,
		6899,
		7580
	};
	
	public Q617_GatherTheFlames()
	{
		super(617, "Gather the Flames");
		
		setItemsIds(TORCH);
		
		addStartNpc(VULCAN, HILDA);
		addTalkId(VULCAN, HILDA, ROONEY);
		
		for (int mobs : CHANCES.keySet())
			addKillId(mobs);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31539-03.htm") || event.equalsIgnoreCase("31271-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31539-05.htm"))
		{
			if (st.getQuestItemsCount(TORCH) >= 1000)
			{
				htmltext = "31539-07.htm";
				st.takeItems(TORCH, 1000);
				st.giveItems(Rnd.get(REWARDS), 1);
			}
		}
		else if (event.equalsIgnoreCase("31539-08.htm"))
		{
			st.takeItems(TORCH, -1);
			st.exitQuest(true);
		}
		else if (StringUtil.isDigit(event))
		{
			if (st.getQuestItemsCount(TORCH) >= 1200)
			{
				htmltext = "32049-03.htm";
				st.takeItems(TORCH, 1200);
				st.giveItems(Integer.valueOf(event), 1);
			}
			else
				htmltext = "32049-02.htm";
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
				htmltext = npc.getNpcId() + ((player.getLevel() >= 74) ? "-01.htm" : "-02.htm");
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case VULCAN:
						htmltext = (st.getQuestItemsCount(TORCH) >= 1000) ? "31539-04.htm" : "31539-05.htm";
						break;
					
					case HILDA:
						htmltext = "31271-04.htm";
						break;
					
					case ROONEY:
						htmltext = (st.getQuestItemsCount(TORCH) >= 1200) ? "32049-01.htm" : "32049-02.htm";
						break;
				}
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
		
		st.dropItems(TORCH, 1, 0, CHANCES.get(npc.getNpcId()));
		
		return null;
	}
}
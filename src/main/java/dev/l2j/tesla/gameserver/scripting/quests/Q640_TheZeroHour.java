package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q640_TheZeroHour extends Quest
{
	private static final String qn = "Q640_TheZeroHour";
	
	// NPC
	private static final int KAHMAN = 31554;
	
	// Item
	private static final int FANG_OF_STAKATO = 8085;
	
	private static final int[][] REWARDS =
	{
		{
			12,
			4042,
			1
		},
		{
			6,
			4043,
			1
		},
		{
			6,
			4044,
			1
		},
		{
			81,
			1887,
			10
		},
		{
			33,
			1888,
			5
		},
		{
			30,
			1889,
			10
		},
		{
			150,
			5550,
			10
		},
		{
			131,
			1890,
			10
		},
		{
			123,
			1893,
			5
		}
	};
	
	public Q640_TheZeroHour()
	{
		super(640, "The Zero Hour");
		
		setItemsIds(FANG_OF_STAKATO);
		
		addStartNpc(KAHMAN);
		addTalkId(KAHMAN);
		
		// All "spiked" stakatos types, except babies and cannibalistic followers.
		addKillId(22105, 22106, 22107, 22108, 22109, 22110, 22111, 22113, 22114, 22115, 22116, 22117, 22118, 22119, 22121);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31554-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31554-05.htm"))
		{
			if (!st.hasQuestItems(FANG_OF_STAKATO))
				htmltext = "31554-06.htm";
		}
		else if (event.equalsIgnoreCase("31554-08.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (StringUtil.isDigit(event))
		{
			int reward[] = REWARDS[Integer.parseInt(event)];
			
			if (st.getQuestItemsCount(FANG_OF_STAKATO) >= reward[0])
			{
				htmltext = "31554-09.htm";
				st.takeItems(FANG_OF_STAKATO, reward[0]);
				st.rewardItems(reward[1], reward[2]);
			}
			else
				htmltext = "31554-06.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (player.getLevel() < 66)
					htmltext = "31554-00.htm";
				else
				{
					QuestState st2 = player.getQuestState("Q109_InSearchOfTheNest");
					htmltext = (st2 != null && st2.isCompleted()) ? "31554-01.htm" : "31554-10.htm";
				}
				break;
			
			case STATE_STARTED:
				htmltext = (st.hasQuestItems(FANG_OF_STAKATO)) ? "31554-04.htm" : "31554-03.htm";
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
		
		st.dropItemsAlways(FANG_OF_STAKATO, 1, 0);
		
		return null;
	}
}
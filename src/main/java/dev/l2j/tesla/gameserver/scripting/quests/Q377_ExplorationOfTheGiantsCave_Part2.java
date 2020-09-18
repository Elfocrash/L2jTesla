package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q377_ExplorationOfTheGiantsCave_Part2 extends Quest
{
	private static final String qn = "Q377_ExplorationOfTheGiantsCave_Part2";
	
	// Items
	private static final int ANCIENT_BOOK = 5955;
	private static final int DICTIONARY_INTERMEDIATE = 5892;
	
	private static final int[][] BOOKS =
	{
		// science & technology -> majestic leather, leather armor of nightmare
		{
			5945,
			5946,
			5947,
			5948,
			5949
		},
		// culture -> armor of nightmare, majestic plate
		{
			5950,
			5951,
			5952,
			5953,
			5954
		}
	};
	
	// Rewards
	private static final int[][] RECIPES =
	{
		// science & technology -> majestic leather, leather armor of nightmare
		{
			5338,
			5336
		},
		// culture -> armor of nightmare, majestic plate
		{
			5420,
			5422
		}
	};
	
	public Q377_ExplorationOfTheGiantsCave_Part2()
	{
		super(377, "Exploration of the Giants' Cave, Part 2");
		
		addStartNpc(31147); // Sobling
		addTalkId(31147);
		
		addKillId(20654, 20656, 20657, 20658);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31147-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31147-04.htm"))
		{
			htmltext = checkItems(st);
		}
		else if (event.equalsIgnoreCase("31147-07.htm"))
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
				htmltext = (player.getLevel() < 57 || !st.hasQuestItems(DICTIONARY_INTERMEDIATE)) ? "31147-01.htm" : "31147-02.htm";
				break;
			
			case STATE_STARTED:
				htmltext = checkItems(st);
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
		
		st.dropItems(ANCIENT_BOOK, 1, 0, 18000);
		
		return null;
	}
	
	private static String checkItems(QuestState st)
	{
		for (int type = 0; type < BOOKS.length; type++)
		{
			boolean complete = true;
			for (int book : BOOKS[type])
			{
				if (!st.hasQuestItems(book))
					complete = false;
			}
			
			if (complete)
			{
				for (int book : BOOKS[type])
					st.takeItems(book, 1);
				
				st.giveItems(Rnd.get(RECIPES[type]), 1);
				return "31147-04.htm";
			}
		}
		return "31147-05.htm";
	}
}
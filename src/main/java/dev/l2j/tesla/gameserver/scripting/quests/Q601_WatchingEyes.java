package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q601_WatchingEyes extends Quest
{
	private static final String qn = "Q601_WatchingEyes";
	
	// Items
	private static final int PROOF_OF_AVENGER = 7188;
	
	// Rewards
	private static final int[][] REWARDS =
	{
		{
			6699,
			90000,
			20
		},
		{
			6698,
			80000,
			40
		},
		{
			6700,
			40000,
			50
		},
		{
			0,
			230000,
			100
		}
	};
	
	public Q601_WatchingEyes()
	{
		super(601, "Watching Eyes");
		
		setItemsIds(PROOF_OF_AVENGER);
		
		addStartNpc(31683); // Eye of Argos
		addTalkId(31683);
		
		addKillId(21306, 21308, 21309, 21310, 21311);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31683-03.htm"))
		{
			if (player.getLevel() < 71)
				htmltext = "31683-02.htm";
			else
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
		}
		else if (event.equalsIgnoreCase("31683-07.htm"))
		{
			st.takeItems(PROOF_OF_AVENGER, -1);
			
			final int random = Rnd.get(100);
			for (int[] element : REWARDS)
			{
				if (random < element[2])
				{
					st.rewardItems(57, element[1]);
					
					if (element[0] != 0)
					{
						st.giveItems(element[0], 5);
						st.rewardExpAndSp(120000, 10000);
					}
					break;
				}
			}
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
				htmltext = "31683-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = (st.hasQuestItems(PROOF_OF_AVENGER)) ? "31683-05.htm" : "31683-04.htm";
				else if (cond == 2)
					htmltext = "31683-06.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = getRandomPartyMember(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (st.dropItems(PROOF_OF_AVENGER, 1, 100, 500000))
			st.set("cond", "2");
		
		return null;
	}
}
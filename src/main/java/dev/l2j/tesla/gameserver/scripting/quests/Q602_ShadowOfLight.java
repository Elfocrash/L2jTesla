package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q602_ShadowOfLight extends Quest
{
	private static final String qn = "Q602_ShadowOfLight";
	
	private static final int EYE_OF_DARKNESS = 7189;
	
	private static final int[][] REWARDS =
	{
		{
			6699,
			40000,
			120000,
			20000,
			20
		},
		{
			6698,
			60000,
			110000,
			15000,
			40
		},
		{
			6700,
			40000,
			150000,
			10000,
			50
		},
		{
			0,
			100000,
			140000,
			11250,
			100
		}
	};
	
	public Q602_ShadowOfLight()
	{
		super(602, "Shadow of Light");
		
		setItemsIds(EYE_OF_DARKNESS);
		
		addStartNpc(31683); // Eye of Argos
		addTalkId(31683);
		
		addKillId(21299, 21304);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31683-02.htm"))
		{
			if (player.getLevel() < 68)
				htmltext = "31683-02a.htm";
			else
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
		}
		else if (event.equalsIgnoreCase("31683-05.htm"))
		{
			st.takeItems(EYE_OF_DARKNESS, -1);
			
			final int random = Rnd.get(100);
			for (int[] element : REWARDS)
			{
				if (random < element[4])
				{
					st.rewardItems(57, element[1]);
					
					if (element[0] != 0)
						st.giveItems(element[0], 3);
					
					st.rewardExpAndSp(element[2], element[3]);
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
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "31683-03.htm";
				else if (cond == 2)
					htmltext = "31683-04.htm";
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
		
		if (st.dropItems(EYE_OF_DARKNESS, 1, 100, (npc.getNpcId() == 21299) ? 450000 : 500000))
			st.set("cond", "2");
		
		return null;
	}
}
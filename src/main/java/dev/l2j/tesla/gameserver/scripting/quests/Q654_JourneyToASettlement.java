package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q654_JourneyToASettlement extends Quest
{
	private static final String qn = "Q654_JourneyToASettlement";
	
	// Item
	private static final int ANTELOPE_SKIN = 8072;
	
	// Reward
	private static final int FORCE_FIELD_REMOVAL_SCROLL = 8073;
	
	public Q654_JourneyToASettlement()
	{
		super(654, "Journey to a Settlement");
		
		setItemsIds(ANTELOPE_SKIN);
		
		addStartNpc(31453); // Nameless Spirit
		addTalkId(31453);
		
		addKillId(21294, 21295); // Canyon Antelope, Canyon Antelope Slave
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31453-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31453-03.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31453-06.htm"))
		{
			st.takeItems(ANTELOPE_SKIN, -1);
			st.giveItems(FORCE_FIELD_REMOVAL_SCROLL, 1);
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
				QuestState prevSt = player.getQuestState("Q119_LastImperialPrince");
				htmltext = (prevSt == null || !prevSt.isCompleted() || player.getLevel() < 74) ? "31453-00.htm" : "31453-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "31453-02.htm";
				else if (cond == 2)
					htmltext = "31453-04.htm";
				else if (cond == 3)
					htmltext = "31453-05.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "2");
		if (st == null)
			return null;
		
		if (st.dropItems(ANTELOPE_SKIN, 1, 1, 50000))
			st.set("cond", "3");
		
		return null;
	}
}
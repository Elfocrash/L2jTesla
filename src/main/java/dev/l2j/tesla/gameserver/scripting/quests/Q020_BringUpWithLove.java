package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q020_BringUpWithLove extends Quest
{
	public static final String qn = "Q020_BringUpWithLove";
	
	// Item
	private static final int JEWEL_OF_INNOCENCE = 7185;
	
	public Q020_BringUpWithLove()
	{
		super(20, "Bring Up With Love");
		
		setItemsIds(JEWEL_OF_INNOCENCE);
		
		addStartNpc(31537); // Tunatun
		addTalkId(31537);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31537-09.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31537-12.htm"))
		{
			st.takeItems(JEWEL_OF_INNOCENCE, -1);
			st.rewardItems(57, 68500);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				htmltext = (player.getLevel() < 65) ? "31537-02.htm" : "31537-01.htm";
				break;
			
			case STATE_STARTED:
				if (st.getInt("cond") == 2)
					htmltext = "31537-11.htm";
				else
					htmltext = "31537-10.htm";
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
}
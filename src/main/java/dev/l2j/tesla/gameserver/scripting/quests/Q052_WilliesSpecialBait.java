package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q052_WilliesSpecialBait extends Quest
{
	private static final String qn = "Q052_WilliesSpecialBait";
	
	// Item
	private static final int TARLK_EYE = 7623;
	
	// Reward
	private static final int EARTH_FISHING_LURE = 7612;
	
	public Q052_WilliesSpecialBait()
	{
		super(52, "Willie's Special Bait");
		
		setItemsIds(TARLK_EYE);
		
		addStartNpc(31574); // Willie
		addTalkId(31574);
		
		addKillId(20573); // Tarlk Basilik
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31574-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31574-07.htm"))
		{
			htmltext = "31574-06.htm";
			st.takeItems(TARLK_EYE, -1);
			st.rewardItems(EARTH_FISHING_LURE, 4);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				htmltext = (player.getLevel() < 48) ? "31574-02.htm" : "31574-01.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.getQuestItemsCount(TARLK_EYE) == 100) ? "31574-04.htm" : "31574-05.htm";
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (st.dropItems(TARLK_EYE, 1, 100, 500000))
			st.set("cond", "2");
		
		return null;
	}
}
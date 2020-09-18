package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q368_TrespassingIntoTheSacredArea extends Quest
{
	private static final String qn = "Q368_TrespassingIntoTheSacredArea";
	
	// NPC
	private static final int RESTINA = 30926;
	
	// Item
	private static final int FANG = 5881;
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	{
		CHANCES.put(20794, 500000);
		CHANCES.put(20795, 770000);
		CHANCES.put(20796, 500000);
		CHANCES.put(20797, 480000);
	}
	
	public Q368_TrespassingIntoTheSacredArea()
	{
		super(368, "Trespassing into the Sacred Area");
		
		setItemsIds(FANG);
		
		addStartNpc(RESTINA);
		addTalkId(RESTINA);
		
		addKillId(20794, 20795, 20796, 20797);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30926-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30926-05.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				htmltext = (player.getLevel() < 36) ? "30926-01a.htm" : "30926-01.htm";
				break;
			
			case STATE_STARTED:
				final int fangs = st.getQuestItemsCount(FANG);
				if (fangs == 0)
					htmltext = "30926-03.htm";
				else
				{
					final int reward = 250 * fangs + (fangs > 10 ? 5730 : 2000);
					
					htmltext = "30926-04.htm";
					st.takeItems(5881, -1);
					st.rewardItems(57, reward);
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
		
		st.dropItems(FANG, 1, 0, CHANCES.get(npc.getNpcId()));
		
		return null;
	}
}
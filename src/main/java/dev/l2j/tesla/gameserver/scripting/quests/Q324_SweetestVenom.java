package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q324_SweetestVenom extends Quest
{
	private static final String qn = "Q324_SweetestVenom";
	
	// Item
	private static final int VENOM_SAC = 1077;
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	{
		CHANCES.put(20034, 220000);
		CHANCES.put(20038, 230000);
		CHANCES.put(20043, 250000);
	}
	
	public Q324_SweetestVenom()
	{
		super(324, "Sweetest Venom");
		
		setItemsIds(VENOM_SAC);
		
		addStartNpc(30351); // Astaron
		addTalkId(30351);
		
		addKillId(20034, 20038, 20043);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30351-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
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
				htmltext = (player.getLevel() < 18) ? "30351-02.htm" : "30351-03.htm";
				break;
			
			case STATE_STARTED:
				if (st.getInt("cond") == 1)
					htmltext = "30351-05.htm";
				else
				{
					htmltext = "30351-06.htm";
					st.takeItems(VENOM_SAC, -1);
					st.rewardItems(57, 5810);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
				}
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
		
		if (st.dropItems(VENOM_SAC, 1, 10, CHANCES.get(npc.getNpcId())))
			st.set("cond", "2");
		
		return null;
	}
}
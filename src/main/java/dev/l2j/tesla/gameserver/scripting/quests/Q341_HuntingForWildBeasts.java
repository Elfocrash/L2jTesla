package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q341_HuntingForWildBeasts extends Quest
{
	private static final String qn = "Q341_HuntingForWildBeasts";
	
	// Item
	private static final int BEAR_SKIN = 4259;
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	{
		CHANCES.put(20021, 500000); // Red Bear
		CHANCES.put(20203, 900000); // Dion Grizzly
		CHANCES.put(20310, 500000); // Brown Bear
		CHANCES.put(20335, 700000); // Grizzly Bear
	}
	
	public Q341_HuntingForWildBeasts()
	{
		super(341, "Hunting for Wild Beasts");
		
		setItemsIds(BEAR_SKIN);
		
		addStartNpc(30078); // Pano
		addTalkId(30078);
		
		addKillId(20021, 20203, 20310, 20335);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30078-02.htm"))
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
				htmltext = (player.getLevel() < 20) ? "30078-00.htm" : "30078-01.htm";
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(BEAR_SKIN) < 20)
					htmltext = "30078-03.htm";
				else
				{
					htmltext = "30078-04.htm";
					st.takeItems(BEAR_SKIN, -1);
					st.rewardItems(57, 3710);
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
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		st.dropItems(BEAR_SKIN, 1, 20, CHANCES.get(npc.getNpcId()));
		
		return null;
	}
}
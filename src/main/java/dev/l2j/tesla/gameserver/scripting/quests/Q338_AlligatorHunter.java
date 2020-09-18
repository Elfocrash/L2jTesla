package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q338_AlligatorHunter extends Quest
{
	private static final String qn = "Q338_AlligatorHunter";
	
	// Item
	private static final int ALLIGATOR_PELT = 4337;
	
	public Q338_AlligatorHunter()
	{
		super(338, "Alligator Hunter");
		
		setItemsIds(ALLIGATOR_PELT);
		
		addStartNpc(30892); // Enverun
		addTalkId(30892);
		
		addKillId(20135); // Alligator
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30892-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30892-05.htm"))
		{
			final int pelts = st.getQuestItemsCount(ALLIGATOR_PELT);
			
			int reward = pelts * 60;
			if (pelts > 10)
				reward += 3430;
			
			st.takeItems(ALLIGATOR_PELT, -1);
			st.rewardItems(57, reward);
		}
		else if (event.equalsIgnoreCase("30892-08.htm"))
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
				htmltext = (player.getLevel() < 40) ? "30892-00.htm" : "30892-01.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.hasQuestItems(ALLIGATOR_PELT)) ? "30892-03.htm" : "30892-04.htm";
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
		
		st.dropItemsAlways(ALLIGATOR_PELT, 1, 0);
		
		return null;
	}
}
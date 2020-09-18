package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q661_MakingTheHarvestGroundsSafe extends Quest
{
	private static final String qn = "Q661_MakingTheHarvestGroundsSafe";
	
	// NPC
	private static final int NORMAN = 30210;
	
	// Items
	private static final int STING_OF_GIANT_POISON_BEE = 8283;
	private static final int CLOUDY_GEM = 8284;
	private static final int TALON_OF_YOUNG_ARANEID = 8285;
	
	// Reward
	private static final int ADENA = 57;
	
	// Monsters
	private static final int GIANT_POISON_BEE = 21095;
	private static final int CLOUDY_BEAST = 21096;
	private static final int YOUNG_ARANEID = 21097;
	
	public Q661_MakingTheHarvestGroundsSafe()
	{
		super(661, "Making the Harvest Grounds Safe");
		
		setItemsIds(STING_OF_GIANT_POISON_BEE, CLOUDY_GEM, TALON_OF_YOUNG_ARANEID);
		
		addStartNpc(NORMAN);
		addTalkId(NORMAN);
		
		addKillId(GIANT_POISON_BEE, CLOUDY_BEAST, YOUNG_ARANEID);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30210-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30210-04.htm"))
		{
			int item1 = st.getQuestItemsCount(STING_OF_GIANT_POISON_BEE);
			int item2 = st.getQuestItemsCount(CLOUDY_GEM);
			int item3 = st.getQuestItemsCount(TALON_OF_YOUNG_ARANEID);
			int sum = 0;
			
			sum = (item1 * 57) + (item2 * 56) + (item3 * 60);
			
			if (item1 + item2 + item3 >= 10)
				sum += 2871;
			
			st.takeItems(STING_OF_GIANT_POISON_BEE, item1);
			st.takeItems(CLOUDY_GEM, item2);
			st.takeItems(TALON_OF_YOUNG_ARANEID, item3);
			st.rewardItems(ADENA, sum);
		}
		else if (event.equalsIgnoreCase("30210-06.htm"))
			st.exitQuest(true);
		
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
				htmltext = (player.getLevel() < 21) ? "30210-01a.htm" : "30210-01.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.hasAtLeastOneQuestItem(STING_OF_GIANT_POISON_BEE, CLOUDY_GEM, TALON_OF_YOUNG_ARANEID)) ? "30210-03.htm" : "30210-05.htm";
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
		
		st.dropItems(npc.getNpcId() - 12812, 1, 0, 500000);
		
		return null;
	}
}
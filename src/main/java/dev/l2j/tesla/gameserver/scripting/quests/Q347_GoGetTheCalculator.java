package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q347_GoGetTheCalculator extends Quest
{
	private static final String qn = "Q347_GoGetTheCalculator";
	
	// NPCs
	private static final int BRUNON = 30526;
	private static final int SILVERA = 30527;
	private static final int SPIRON = 30532;
	private static final int BALANKI = 30533;
	
	// Items
	private static final int GEMSTONE_BEAST_CRYSTAL = 4286;
	private static final int CALCULATOR_QUEST = 4285;
	private static final int CALCULATOR_REAL = 4393;
	
	public Q347_GoGetTheCalculator()
	{
		super(347, "Go Get the Calculator");
		
		setItemsIds(GEMSTONE_BEAST_CRYSTAL, CALCULATOR_QUEST);
		
		addStartNpc(BRUNON);
		addTalkId(BRUNON, SILVERA, SPIRON, BALANKI);
		
		addKillId(20540);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30526-05.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30533-03.htm"))
		{
			if (st.getQuestItemsCount(57) >= 100)
			{
				htmltext = "30533-02.htm";
				st.takeItems(57, 100);
				
				if (st.getInt("cond") == 3)
					st.set("cond", "4");
				else
					st.set("cond", "2");
				
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equalsIgnoreCase("30532-02.htm"))
		{
			if (st.getInt("cond") == 2)
				st.set("cond", "4");
			else
				st.set("cond", "3");
			
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30526-08.htm"))
		{
			st.takeItems(CALCULATOR_QUEST, -1);
			st.giveItems(CALCULATOR_REAL, 1);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30526-09.htm"))
		{
			st.takeItems(CALCULATOR_QUEST, -1);
			st.rewardItems(57, 1000);
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
				htmltext = (player.getLevel() < 12) ? "30526-00.htm" : "30526-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case BRUNON:
						htmltext = (!st.hasQuestItems(CALCULATOR_QUEST)) ? "30526-06.htm" : "30526-07.htm";
						break;
					
					case SPIRON:
						htmltext = (cond < 4) ? "30532-01.htm" : "30532-05.htm";
						break;
					
					case BALANKI:
						htmltext = (cond < 4) ? "30533-01.htm" : "30533-04.htm";
						break;
					
					case SILVERA:
						if (cond < 4)
							htmltext = "30527-00.htm";
						else if (cond == 4)
						{
							htmltext = "30527-01.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 5)
						{
							if (st.getQuestItemsCount(GEMSTONE_BEAST_CRYSTAL) < 10)
								htmltext = "30527-02.htm";
							else
							{
								htmltext = "30527-03.htm";
								st.set("cond", "6");
								st.takeItems(GEMSTONE_BEAST_CRYSTAL, -1);
								st.giveItems(CALCULATOR_QUEST, 1);
								st.playSound(QuestState.SOUND_MIDDLE);
							}
						}
						else if (cond == 6)
							htmltext = "30527-04.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "5");
		if (st == null)
			return null;
		
		st.dropItems(GEMSTONE_BEAST_CRYSTAL, 1, 10, 500000);
		
		return null;
	}
}
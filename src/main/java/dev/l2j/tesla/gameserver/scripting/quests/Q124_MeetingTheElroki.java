package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q124_MeetingTheElroki extends Quest
{
	public static final String qn = "Q124_MeetingTheElroki";
	
	// NPCs
	private static final int MARQUEZ = 32113;
	private static final int MUSHIKA = 32114;
	private static final int ASAMAH = 32115;
	private static final int KARAKAWEI = 32117;
	private static final int MANTARASA = 32118;
	
	public Q124_MeetingTheElroki()
	{
		super(124, "Meeting the Elroki");
		
		addStartNpc(MARQUEZ);
		addTalkId(MARQUEZ, MUSHIKA, ASAMAH, KARAKAWEI, MANTARASA);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32113-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32113-04.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32114-02.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32115-04.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32117-02.htm"))
		{
			if (st.getInt("cond") == 4)
				st.set("progress", "1");
		}
		else if (event.equalsIgnoreCase("32117-03.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32118-02.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(8778, 1); // Egg
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
				htmltext = (player.getLevel() < 75) ? "32113-01a.htm" : "32113-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case MARQUEZ:
						if (cond == 1)
							htmltext = "32113-03.htm";
						else if (cond > 1)
							htmltext = "32113-04a.htm";
						break;
					
					case MUSHIKA:
						if (cond == 2)
							htmltext = "32114-01.htm";
						else if (cond > 2)
							htmltext = "32114-03.htm";
						break;
					
					case ASAMAH:
						if (cond == 3)
							htmltext = "32115-01.htm";
						else if (cond == 6)
						{
							htmltext = "32115-05.htm";
							st.takeItems(8778, -1);
							st.rewardItems(57, 71318);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case KARAKAWEI:
						if (cond == 4)
						{
							htmltext = "32117-01.htm";
							if (st.getInt("progress") == 1)
								htmltext = "32117-02.htm";
						}
						else if (cond > 4)
							htmltext = "32117-04.htm";
						break;
					
					case MANTARASA:
						if (cond == 5)
							htmltext = "32118-01.htm";
						else if (cond > 5)
							htmltext = "32118-03.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				if (npc.getNpcId() == ASAMAH)
					htmltext = "32115-06.htm";
				else
					htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q031_SecretBuriedInTheSwamp extends Quest
{
	private static final String qn = "Q031_SecretBuriedInTheSwamp";
	
	// Item
	private static final int KRORIN_JOURNAL = 7252;
	
	// NPCs
	private static final int ABERCROMBIE = 31555;
	private static final int FORGOTTEN_MONUMENT_1 = 31661;
	private static final int FORGOTTEN_MONUMENT_2 = 31662;
	private static final int FORGOTTEN_MONUMENT_3 = 31663;
	private static final int FORGOTTEN_MONUMENT_4 = 31664;
	private static final int CORPSE_OF_DWARF = 31665;
	
	public Q031_SecretBuriedInTheSwamp()
	{
		super(31, "Secret Buried in the Swamp");
		
		setItemsIds(KRORIN_JOURNAL);
		
		addStartNpc(ABERCROMBIE);
		addTalkId(ABERCROMBIE, CORPSE_OF_DWARF, FORGOTTEN_MONUMENT_1, FORGOTTEN_MONUMENT_2, FORGOTTEN_MONUMENT_3, FORGOTTEN_MONUMENT_4);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31555-01.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31665-01.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(KRORIN_JOURNAL, 1);
		}
		else if (event.equalsIgnoreCase("31555-04.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31661-01.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31662-01.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31663-01.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31664-01.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31555-07.htm"))
		{
			st.takeItems(KRORIN_JOURNAL, 1);
			st.rewardItems(57, 40000);
			st.rewardExpAndSp(130000, 0);
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
				htmltext = (player.getLevel() < 66) ? "31555-00a.htm" : "31555-00.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ABERCROMBIE:
						if (cond == 1)
							htmltext = "31555-02.htm";
						else if (cond == 2)
							htmltext = "31555-03.htm";
						else if (cond > 2 && cond < 7)
							htmltext = "31555-05.htm";
						else if (cond == 7)
							htmltext = "31555-06.htm";
						break;
					
					case CORPSE_OF_DWARF:
						if (cond == 1)
							htmltext = "31665-00.htm";
						else if (cond > 1)
							htmltext = "31665-02.htm";
						break;
					
					case FORGOTTEN_MONUMENT_1:
						if (cond == 3)
							htmltext = "31661-00.htm";
						else if (cond > 3)
							htmltext = "31661-02.htm";
						break;
					
					case FORGOTTEN_MONUMENT_2:
						if (cond == 4)
							htmltext = "31662-00.htm";
						else if (cond > 4)
							htmltext = "31662-02.htm";
						break;
					
					case FORGOTTEN_MONUMENT_3:
						if (cond == 5)
							htmltext = "31663-00.htm";
						else if (cond > 5)
							htmltext = "31663-02.htm";
						break;
					
					case FORGOTTEN_MONUMENT_4:
						if (cond == 6)
							htmltext = "31664-00.htm";
						else if (cond > 6)
							htmltext = "31664-02.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
}
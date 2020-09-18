package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q168_DeliverSupplies extends Quest
{
	private static final String qn = "Q168_DeliverSupplies";
	
	// Items
	private static final int JENNA_LETTER = 1153;
	private static final int SENTRY_BLADE_1 = 1154;
	private static final int SENTRY_BLADE_2 = 1155;
	private static final int SENTRY_BLADE_3 = 1156;
	private static final int OLD_BRONZE_SWORD = 1157;
	
	// NPCs
	private static final int JENNA = 30349;
	private static final int ROSELYN = 30355;
	private static final int KRISTIN = 30357;
	private static final int HARANT = 30360;
	
	public Q168_DeliverSupplies()
	{
		super(168, "Deliver Supplies");
		
		setItemsIds(JENNA_LETTER, SENTRY_BLADE_1, SENTRY_BLADE_2, SENTRY_BLADE_3, OLD_BRONZE_SWORD);
		
		addStartNpc(JENNA);
		addTalkId(JENNA, ROSELYN, KRISTIN, HARANT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30349-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(JENNA_LETTER, 1);
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
				if (player.getRace() != ClassRace.DARK_ELF)
					htmltext = "30349-00.htm";
				else if (player.getLevel() < 3)
					htmltext = "30349-01.htm";
				else
					htmltext = "30349-02.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case JENNA:
						if (cond == 1)
							htmltext = "30349-04.htm";
						else if (cond == 2)
						{
							htmltext = "30349-05.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SENTRY_BLADE_1, 1);
						}
						else if (cond == 3)
							htmltext = "30349-07.htm";
						else if (cond == 4)
						{
							htmltext = "30349-06.htm";
							st.takeItems(OLD_BRONZE_SWORD, 2);
							st.rewardItems(57, 820);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case HARANT:
						if (cond == 1)
						{
							htmltext = "30360-01.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(JENNA_LETTER, 1);
							st.giveItems(SENTRY_BLADE_1, 1);
							st.giveItems(SENTRY_BLADE_2, 1);
							st.giveItems(SENTRY_BLADE_3, 1);
						}
						else if (cond == 2)
							htmltext = "30360-02.htm";
						break;
					
					case ROSELYN:
						if (cond == 3)
						{
							if (st.hasQuestItems(SENTRY_BLADE_2))
							{
								htmltext = "30355-01.htm";
								st.takeItems(SENTRY_BLADE_2, 1);
								st.giveItems(OLD_BRONZE_SWORD, 1);
								if (st.getQuestItemsCount(OLD_BRONZE_SWORD) == 2)
								{
									st.set("cond", "4");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
							}
							else
								htmltext = "30355-02.htm";
						}
						else if (cond == 4)
							htmltext = "30355-02.htm";
						break;
					
					case KRISTIN:
						if (cond == 3)
						{
							if (st.hasQuestItems(SENTRY_BLADE_3))
							{
								htmltext = "30357-01.htm";
								st.takeItems(SENTRY_BLADE_3, 1);
								st.giveItems(OLD_BRONZE_SWORD, 1);
								if (st.getQuestItemsCount(OLD_BRONZE_SWORD) == 2)
								{
									st.set("cond", "4");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
							}
							else
								htmltext = "30357-02.htm";
						}
						else if (cond == 4)
							htmltext = "30357-02.htm";
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
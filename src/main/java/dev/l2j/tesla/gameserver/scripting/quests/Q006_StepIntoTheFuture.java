package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q006_StepIntoTheFuture extends Quest
{
	private static final String qn = "Q006_StepIntoTheFuture";
	
	// NPCs
	private static final int ROXXY = 30006;
	private static final int BAULRO = 30033;
	private static final int SIR_COLLIN = 30311;
	
	// Items
	private static final int BAULRO_LETTER = 7571;
	
	// Rewards
	private static final int MARK_TRAVELER = 7570;
	private static final int SOE_GIRAN = 7559;
	
	public Q006_StepIntoTheFuture()
	{
		super(6, "Step into the Future");
		
		setItemsIds(BAULRO_LETTER);
		
		addStartNpc(ROXXY);
		addTalkId(ROXXY, BAULRO, SIR_COLLIN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30006-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30033-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(BAULRO_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("30311-02.htm"))
		{
			if (st.hasQuestItems(BAULRO_LETTER))
			{
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BAULRO_LETTER, 1);
			}
			else
				htmltext = "30311-03.htm";
		}
		else if (event.equalsIgnoreCase("30006-06.htm"))
		{
			st.giveItems(MARK_TRAVELER, 1);
			st.rewardItems(SOE_GIRAN, 1);
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
				if (player.getRace() != ClassRace.HUMAN || player.getLevel() < 3)
					htmltext = "30006-01.htm";
				else
					htmltext = "30006-02.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ROXXY:
						if (cond == 1 || cond == 2)
							htmltext = "30006-04.htm";
						else if (cond == 3)
							htmltext = "30006-05.htm";
						break;
					
					case BAULRO:
						if (cond == 1)
							htmltext = "30033-01.htm";
						else if (cond == 2)
							htmltext = "30033-03.htm";
						else
							htmltext = "30033-04.htm";
						break;
					
					case SIR_COLLIN:
						if (cond == 2)
							htmltext = "30311-01.htm";
						else if (cond == 3)
							htmltext = "30311-03a.htm";
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
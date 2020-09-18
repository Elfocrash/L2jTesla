package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q160_NerupasRequest extends Quest
{
	private static final String qn = "Q160_NerupasRequest";
	
	// Items
	private static final int SILVERY_SPIDERSILK = 1026;
	private static final int UNOREN_RECEIPT = 1027;
	private static final int CREAMEES_TICKET = 1028;
	private static final int NIGHTSHADE_LEAF = 1029;
	
	// Reward
	private static final int LESSER_HEALING_POTION = 1060;
	
	// NPCs
	private static final int NERUPA = 30370;
	private static final int UNOREN = 30147;
	private static final int CREAMEES = 30149;
	private static final int JULIA = 30152;
	
	public Q160_NerupasRequest()
	{
		super(160, "Nerupa's Request");
		
		setItemsIds(SILVERY_SPIDERSILK, UNOREN_RECEIPT, CREAMEES_TICKET, NIGHTSHADE_LEAF);
		
		addStartNpc(NERUPA);
		addTalkId(NERUPA, UNOREN, CREAMEES, JULIA);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30370-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(SILVERY_SPIDERSILK, 1);
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
				if (player.getRace() != ClassRace.ELF)
					htmltext = "30370-00.htm";
				else if (player.getLevel() < 3)
					htmltext = "30370-02.htm";
				else
					htmltext = "30370-03.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case NERUPA:
						if (cond < 4)
							htmltext = "30370-05.htm";
						else if (cond == 4)
						{
							htmltext = "30370-06.htm";
							st.takeItems(NIGHTSHADE_LEAF, 1);
							st.rewardItems(LESSER_HEALING_POTION, 5);
							st.rewardExpAndSp(1000, 0);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case UNOREN:
						if (cond == 1)
						{
							htmltext = "30147-01.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SILVERY_SPIDERSILK, 1);
							st.giveItems(UNOREN_RECEIPT, 1);
						}
						else if (cond == 2)
							htmltext = "30147-02.htm";
						else if (cond == 4)
							htmltext = "30147-03.htm";
						break;
					
					case CREAMEES:
						if (cond == 2)
						{
							htmltext = "30149-01.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(UNOREN_RECEIPT, 1);
							st.giveItems(CREAMEES_TICKET, 1);
						}
						else if (cond == 3)
							htmltext = "30149-02.htm";
						else if (cond == 4)
							htmltext = "30149-03.htm";
						break;
					
					case JULIA:
						if (cond == 3)
						{
							htmltext = "30152-01.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(CREAMEES_TICKET, 1);
							st.giveItems(NIGHTSHADE_LEAF, 1);
						}
						else if (cond == 4)
							htmltext = "30152-02.htm";
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
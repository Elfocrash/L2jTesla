package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q016_TheComingDarkness extends Quest
{
	private static final String qn = "Q016_TheComingDarkness";
	
	// NPCs
	private static final int HIERARCH = 31517;
	private static final int EVIL_ALTAR_1 = 31512;
	private static final int EVIL_ALTAR_2 = 31513;
	private static final int EVIL_ALTAR_3 = 31514;
	private static final int EVIL_ALTAR_4 = 31515;
	private static final int EVIL_ALTAR_5 = 31516;
	
	// Item
	private static final int CRYSTAL_OF_SEAL = 7167;
	
	public Q016_TheComingDarkness()
	{
		super(16, "The Coming Darkness");
		
		setItemsIds(CRYSTAL_OF_SEAL);
		
		addStartNpc(HIERARCH);
		addTalkId(HIERARCH, EVIL_ALTAR_1, EVIL_ALTAR_2, EVIL_ALTAR_3, EVIL_ALTAR_4, EVIL_ALTAR_5);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31517-2.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(CRYSTAL_OF_SEAL, 5);
		}
		else if (event.equalsIgnoreCase("31512-1.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CRYSTAL_OF_SEAL, 1);
		}
		else if (event.equalsIgnoreCase("31513-1.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CRYSTAL_OF_SEAL, 1);
		}
		else if (event.equalsIgnoreCase("31514-1.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CRYSTAL_OF_SEAL, 1);
		}
		else if (event.equalsIgnoreCase("31515-1.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CRYSTAL_OF_SEAL, 1);
		}
		else if (event.equalsIgnoreCase("31516-1.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CRYSTAL_OF_SEAL, 1);
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
				htmltext = (player.getLevel() < 62) ? "31517-0a.htm" : "31517-0.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				int npcId = npc.getNpcId();
				
				switch (npcId)
				{
					case HIERARCH:
						if (cond == 6)
						{
							htmltext = "31517-4.htm";
							st.rewardExpAndSp(221958, 0);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						else
						{
							if (st.hasQuestItems(CRYSTAL_OF_SEAL))
								htmltext = "31517-3.htm";
							else
							{
								htmltext = "31517-3a.htm";
								st.exitQuest(true);
							}
						}
						break;
					
					case EVIL_ALTAR_1:
					case EVIL_ALTAR_2:
					case EVIL_ALTAR_3:
					case EVIL_ALTAR_4:
					case EVIL_ALTAR_5:
						final int condAltar = npcId - 31511;
						
						if (cond == condAltar)
						{
							if (st.hasQuestItems(CRYSTAL_OF_SEAL))
								htmltext = npcId + "-0.htm";
							else
								htmltext = "altar_nocrystal.htm";
						}
						else if (cond > condAltar)
							htmltext = npcId + "-2.htm";
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
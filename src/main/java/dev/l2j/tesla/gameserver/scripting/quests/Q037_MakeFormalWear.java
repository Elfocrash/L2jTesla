package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q037_MakeFormalWear extends Quest
{
	private static final String qn = "Q037_MakeFormalWear";
	
	// NPCs
	private static final int ALEXIS = 30842;
	private static final int LEIKAR = 31520;
	private static final int JEREMY = 31521;
	private static final int MIST = 31627;
	
	// Items
	private static final int MYSTERIOUS_CLOTH = 7076;
	private static final int JEWEL_BOX = 7077;
	private static final int SEWING_KIT = 7078;
	private static final int DRESS_SHOES_BOX = 7113;
	private static final int SIGNET_RING = 7164;
	private static final int ICE_WINE = 7160;
	private static final int BOX_OF_COOKIES = 7159;
	
	// Reward
	private static final int FORMAL_WEAR = 6408;
	
	public Q037_MakeFormalWear()
	{
		super(37, "Make Formal Wear");
		
		setItemsIds(SIGNET_RING, ICE_WINE, BOX_OF_COOKIES);
		
		addStartNpc(ALEXIS);
		addTalkId(ALEXIS, LEIKAR, JEREMY, MIST);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30842-1.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31520-1.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(SIGNET_RING, 1);
		}
		else if (event.equalsIgnoreCase("31521-1.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(SIGNET_RING, 1);
			st.giveItems(ICE_WINE, 1);
		}
		else if (event.equalsIgnoreCase("31627-1.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ICE_WINE, 1);
		}
		else if (event.equalsIgnoreCase("31521-3.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(BOX_OF_COOKIES, 1);
		}
		else if (event.equalsIgnoreCase("31520-3.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BOX_OF_COOKIES, 1);
		}
		else if (event.equalsIgnoreCase("31520-5.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(JEWEL_BOX, 1);
			st.takeItems(MYSTERIOUS_CLOTH, 1);
			st.takeItems(SEWING_KIT, 1);
		}
		else if (event.equalsIgnoreCase("31520-7.htm"))
		{
			st.takeItems(DRESS_SHOES_BOX, 1);
			st.giveItems(FORMAL_WEAR, 1);
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
				htmltext = (player.getLevel() < 60) ? "30842-0a.htm" : "30842-0.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ALEXIS:
						if (cond == 1)
							htmltext = "30842-2.htm";
						break;
					
					case LEIKAR:
						if (cond == 1)
							htmltext = "31520-0.htm";
						else if (cond == 2)
							htmltext = "31520-1a.htm";
						else if (cond == 5 || cond == 6)
						{
							if (st.hasQuestItems(MYSTERIOUS_CLOTH, JEWEL_BOX, SEWING_KIT))
								htmltext = "31520-4.htm";
							else if (st.hasQuestItems(BOX_OF_COOKIES))
								htmltext = "31520-2.htm";
							else
								htmltext = "31520-3a.htm";
						}
						else if (cond == 7)
							htmltext = (st.hasQuestItems(DRESS_SHOES_BOX)) ? "31520-6.htm" : "31520-5a.htm";
						break;
					
					case JEREMY:
						if (st.hasQuestItems(SIGNET_RING))
							htmltext = "31521-0.htm";
						else if (cond == 3)
							htmltext = "31521-1a.htm";
						else if (cond == 4)
							htmltext = "31521-2.htm";
						else if (cond > 4)
							htmltext = "31521-3a.htm";
						break;
					
					case MIST:
						if (cond == 3)
							htmltext = "31627-0.htm";
						else if (cond > 3)
							htmltext = "31627-2.htm";
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
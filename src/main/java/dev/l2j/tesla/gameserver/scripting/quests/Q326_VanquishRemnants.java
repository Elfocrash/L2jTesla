package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q326_VanquishRemnants extends Quest
{
	private static final String qn = "Q326_VanquishRemnants";
	
	// Items
	private static final int RED_CROSS_BADGE = 1359;
	private static final int BLUE_CROSS_BADGE = 1360;
	private static final int BLACK_CROSS_BADGE = 1361;
	
	// Reward
	private static final int BLACK_LION_MARK = 1369;
	
	public Q326_VanquishRemnants()
	{
		super(326, "Vanquish Remnants");
		
		setItemsIds(RED_CROSS_BADGE, BLUE_CROSS_BADGE, BLACK_CROSS_BADGE);
		
		addStartNpc(30435); // Leopold
		addTalkId(30435);
		
		addKillId(20053, 20437, 20058, 20436, 20061, 20439, 20063, 20066, 20438);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30435-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30435-07.htm"))
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
				htmltext = (player.getLevel() < 21) ? "30435-01.htm" : "30435-02.htm";
				break;
			
			case STATE_STARTED:
				final int redBadges = st.getQuestItemsCount(RED_CROSS_BADGE);
				final int blueBadges = st.getQuestItemsCount(BLUE_CROSS_BADGE);
				final int blackBadges = st.getQuestItemsCount(BLACK_CROSS_BADGE);
				
				final int badgesSum = redBadges + blueBadges + blackBadges;
				
				if (badgesSum > 0)
				{
					st.takeItems(RED_CROSS_BADGE, -1);
					st.takeItems(BLUE_CROSS_BADGE, -1);
					st.takeItems(BLACK_CROSS_BADGE, -1);
					st.rewardItems(57, ((redBadges * 46) + (blueBadges * 52) + (blackBadges * 58) + ((badgesSum >= 10) ? 4320 : 0)));
					
					if (badgesSum >= 100)
					{
						if (!st.hasQuestItems(BLACK_LION_MARK))
						{
							htmltext = "30435-06.htm";
							st.giveItems(BLACK_LION_MARK, 1);
							st.playSound(QuestState.SOUND_ITEMGET);
						}
						else
							htmltext = "30435-09.htm";
					}
					else
						htmltext = "30435-05.htm";
				}
				else
					htmltext = "30435-04.htm";
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
		
		switch (npc.getNpcId())
		{
			case 20053:
			case 20437:
			case 20058:
				st.dropItems(RED_CROSS_BADGE, 1, 0, 330000);
				break;
			
			case 20436:
			case 20061:
			case 20439:
			case 20063:
				st.dropItems(BLUE_CROSS_BADGE, 1, 0, 160000);
				break;
			
			case 20066:
			case 20438:
				st.dropItems(BLACK_CROSS_BADGE, 1, 0, 120000);
				break;
		}
		
		return null;
	}
}
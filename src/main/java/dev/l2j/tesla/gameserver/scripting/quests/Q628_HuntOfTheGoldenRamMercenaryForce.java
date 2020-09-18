package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q628_HuntOfTheGoldenRamMercenaryForce extends Quest
{
	private static final String qn = "Q628_HuntOfTheGoldenRamMercenaryForce";
	
	// NPCs
	private static final int KAHMAN = 31554;
	
	// Items
	private static final int SPLINTER_STAKATO_CHITIN = 7248;
	private static final int NEEDLE_STAKATO_CHITIN = 7249;
	private static final int GOLDEN_RAM_BADGE_RECRUIT = 7246;
	private static final int GOLDEN_RAM_BADGE_SOLDIER = 7247;
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	{
		CHANCES.put(21508, 500000);
		CHANCES.put(21509, 430000);
		CHANCES.put(21510, 521000);
		CHANCES.put(21511, 575000);
		CHANCES.put(21512, 746000);
		CHANCES.put(21513, 500000);
		CHANCES.put(21514, 430000);
		CHANCES.put(21515, 520000);
		CHANCES.put(21516, 531000);
		CHANCES.put(21517, 744000);
	}
	
	public Q628_HuntOfTheGoldenRamMercenaryForce()
	{
		super(628, "Hunt of the Golden Ram Mercenary Force");
		
		setItemsIds(SPLINTER_STAKATO_CHITIN, NEEDLE_STAKATO_CHITIN, GOLDEN_RAM_BADGE_RECRUIT, GOLDEN_RAM_BADGE_SOLDIER);
		
		addStartNpc(KAHMAN);
		addTalkId(KAHMAN);
		
		for (int npcId : CHANCES.keySet())
			addKillId(npcId);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31554-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31554-03a.htm"))
		{
			if (st.getQuestItemsCount(SPLINTER_STAKATO_CHITIN) >= 100 && st.getInt("cond") == 1) // Giving GOLDEN_RAM_BADGE_RECRUIT Medals
			{
				htmltext = "31554-04.htm";
				st.set("cond", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(SPLINTER_STAKATO_CHITIN, -1);
				st.giveItems(GOLDEN_RAM_BADGE_RECRUIT, 1);
			}
		}
		else if (event.equalsIgnoreCase("31554-07.htm")) // Cancel Quest
		{
			st.playSound(QuestState.SOUND_GIVEUP);
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
				htmltext = (player.getLevel() < 66) ? "31554-01a.htm" : "31554-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					if (st.getQuestItemsCount(SPLINTER_STAKATO_CHITIN) >= 100)
						htmltext = "31554-03.htm";
					else
						htmltext = "31554-03a.htm";
				}
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(SPLINTER_STAKATO_CHITIN) >= 100 && st.getQuestItemsCount(NEEDLE_STAKATO_CHITIN) >= 100)
					{
						htmltext = "31554-05.htm";
						st.set("cond", "3");
						st.playSound(QuestState.SOUND_FINISH);
						st.takeItems(SPLINTER_STAKATO_CHITIN, -1);
						st.takeItems(NEEDLE_STAKATO_CHITIN, -1);
						st.takeItems(GOLDEN_RAM_BADGE_RECRUIT, 1);
						st.giveItems(GOLDEN_RAM_BADGE_SOLDIER, 1);
					}
					else if (!st.hasQuestItems(SPLINTER_STAKATO_CHITIN) && !st.hasQuestItems(NEEDLE_STAKATO_CHITIN))
						htmltext = "31554-04b.htm";
					else
						htmltext = "31554-04a.htm";
				}
				else if (cond == 3)
					htmltext = "31554-05a.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		final int cond = st.getInt("cond");
		final int npcId = npc.getNpcId();
		
		switch (npcId)
		{
			case 21508:
			case 21509:
			case 21510:
			case 21511:
			case 21512:
				if (cond == 1 || cond == 2)
					st.dropItems(SPLINTER_STAKATO_CHITIN, 1, 100, CHANCES.get(npcId));
				break;
			
			case 21513:
			case 21514:
			case 21515:
			case 21516:
			case 21517:
				if (cond == 2)
					st.dropItems(NEEDLE_STAKATO_CHITIN, 1, 100, CHANCES.get(npcId));
				break;
		}
		
		return null;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q629_CleanUpTheSwampOfScreams extends Quest
{
	private static final String qn = "Q629_CleanUpTheSwampOfScreams";
	
	// NPC
	private static final int PIERCE = 31553;
	
	// ITEMS
	private static final int TALON_OF_STAKATO = 7250;
	private static final int GOLDEN_RAM_COIN = 7251;
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	{
		CHANCES.put(21508, 500000);
		CHANCES.put(21509, 431000);
		CHANCES.put(21510, 521000);
		CHANCES.put(21511, 576000);
		CHANCES.put(21512, 746000);
		CHANCES.put(21513, 530000);
		CHANCES.put(21514, 538000);
		CHANCES.put(21515, 545000);
		CHANCES.put(21516, 553000);
		CHANCES.put(21517, 560000);
	}
	
	public Q629_CleanUpTheSwampOfScreams()
	{
		super(629, "Clean up the Swamp of Screams");
		
		setItemsIds(TALON_OF_STAKATO, GOLDEN_RAM_COIN);
		
		addStartNpc(PIERCE);
		addTalkId(PIERCE);
		
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
		
		if (event.equalsIgnoreCase("31553-1.htm"))
		{
			if (player.getLevel() >= 66)
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
			else
			{
				htmltext = "31553-0a.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31553-3.htm"))
		{
			if (st.getQuestItemsCount(TALON_OF_STAKATO) >= 100)
			{
				st.takeItems(TALON_OF_STAKATO, 100);
				st.giveItems(GOLDEN_RAM_COIN, 20);
			}
			else
				htmltext = "31553-3a.htm";
		}
		else if (event.equalsIgnoreCase("31553-5.htm"))
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
		
		if (!st.hasAtLeastOneQuestItem(7246, 7247))
			return "31553-6.htm";
		
		switch (st.getState())
		{
			case STATE_CREATED:
				htmltext = (player.getLevel() < 66) ? "31553-0a.htm" : "31553-0.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.getQuestItemsCount(TALON_OF_STAKATO) >= 100) ? "31553-2.htm" : "31553-1a.htm";
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
		
		st.dropItems(TALON_OF_STAKATO, 1, 100, CHANCES.get(npc.getNpcId()));
		
		return null;
	}
}
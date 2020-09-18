package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q380_BringOutTheFlavorOfIngredients extends Quest
{
	private static final String qn = "Q380_BringOutTheFlavorOfIngredients";
	
	// Monsters
	private static final int DIRE_WOLF = 20205;
	private static final int KADIF_WEREWOLF = 20206;
	private static final int GIANT_MIST_LEECH = 20225;
	
	// Items
	private static final int RITRON_FRUIT = 5895;
	private static final int MOON_FACE_FLOWER = 5896;
	private static final int LEECH_FLUIDS = 5897;
	private static final int ANTIDOTE = 1831;
	
	// Rewards
	private static final int RITRON_JELLY = 5960;
	private static final int JELLY_RECIPE = 5959;
	
	public Q380_BringOutTheFlavorOfIngredients()
	{
		super(380, "Bring Out the Flavor of Ingredients!");
		
		setItemsIds(RITRON_FRUIT, MOON_FACE_FLOWER, LEECH_FLUIDS);
		
		addStartNpc(30069); // Rollant
		addTalkId(30069);
		
		addKillId(DIRE_WOLF, KADIF_WEREWOLF, GIANT_MIST_LEECH);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30069-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30069-12.htm"))
		{
			st.giveItems(JELLY_RECIPE, 1);
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
				htmltext = (player.getLevel() < 24) ? "30069-00.htm" : "30069-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "30069-06.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(ANTIDOTE) >= 2)
					{
						htmltext = "30069-07.htm";
						st.set("cond", "3");
						st.playSound(QuestState.SOUND_MIDDLE);
						st.takeItems(RITRON_FRUIT, -1);
						st.takeItems(MOON_FACE_FLOWER, -1);
						st.takeItems(LEECH_FLUIDS, -1);
						st.takeItems(ANTIDOTE, 2);
					}
					else
						htmltext = "30069-06.htm";
				}
				else if (cond == 3)
				{
					htmltext = "30069-08.htm";
					st.set("cond", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else if (cond == 4)
				{
					htmltext = "30069-09.htm";
					st.set("cond", "5");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else if (cond == 5)
				{
					htmltext = "30069-10.htm";
					st.set("cond", "6");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else if (cond == 6)
				{
					st.giveItems(RITRON_JELLY, 1);
					if (Rnd.get(100) < 55)
						htmltext = "30069-11.htm";
					else
					{
						htmltext = "30069-13.htm";
						st.playSound(QuestState.SOUND_FINISH);
						st.exitQuest(true);
					}
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		switch (npc.getNpcId())
		{
			case DIRE_WOLF:
				if (st.dropItems(RITRON_FRUIT, 1, 4, 100000))
					if (st.getQuestItemsCount(MOON_FACE_FLOWER) == 20 && st.getQuestItemsCount(LEECH_FLUIDS) == 10)
						st.set("cond", "2");
				break;
			
			case KADIF_WEREWOLF:
				if (st.dropItems(MOON_FACE_FLOWER, 1, 20, 500000))
					if (st.getQuestItemsCount(RITRON_FRUIT) == 4 && st.getQuestItemsCount(LEECH_FLUIDS) == 10)
						st.set("cond", "2");
				break;
			
			case GIANT_MIST_LEECH:
				if (st.dropItems(LEECH_FLUIDS, 1, 10, 500000))
					if (st.getQuestItemsCount(RITRON_FRUIT) == 4 && st.getQuestItemsCount(MOON_FACE_FLOWER) == 20)
						st.set("cond", "2");
				break;
		}
		
		return null;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q299_GatherIngredientsForPie extends Quest
{
	private static final String qn = "Q299_GatherIngredientsForPie";
	
	// NPCs
	private static final int LARA = 30063;
	private static final int BRIGHT = 30466;
	private static final int EMILY = 30620;
	
	// Items
	private static final int FRUIT_BASKET = 7136;
	private static final int AVELLAN_SPICE = 7137;
	private static final int HONEY_POUCH = 7138;
	
	public Q299_GatherIngredientsForPie()
	{
		super(299, "Gather Ingredients for Pie");
		
		setItemsIds(FRUIT_BASKET, AVELLAN_SPICE, HONEY_POUCH);
		
		addStartNpc(EMILY);
		addTalkId(EMILY, LARA, BRIGHT);
		
		addKillId(20934, 20935); // Wasp Worker, Wasp Leader
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30620-1.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30620-3.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(HONEY_POUCH, -1);
		}
		else if (event.equalsIgnoreCase("30063-1.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(AVELLAN_SPICE, 1);
		}
		else if (event.equalsIgnoreCase("30620-5.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(AVELLAN_SPICE, 1);
		}
		else if (event.equalsIgnoreCase("30466-1.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(FRUIT_BASKET, 1);
		}
		else if (event.equalsIgnoreCase("30620-7a.htm"))
		{
			if (st.hasQuestItems(FRUIT_BASKET))
			{
				htmltext = "30620-7.htm";
				st.takeItems(FRUIT_BASKET, 1);
				st.rewardItems(57, 25000);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				st.set("cond", "5");
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
				htmltext = (player.getLevel() < 34) ? "30620-0a.htm" : "30620-0.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case EMILY:
						if (cond == 1)
							htmltext = "30620-1a.htm";
						else if (cond == 2)
						{
							if (st.getQuestItemsCount(HONEY_POUCH) >= 100)
								htmltext = "30620-2.htm";
							else
							{
								htmltext = "30620-2a.htm";
								st.exitQuest(true);
							}
						}
						else if (cond == 3)
							htmltext = "30620-3a.htm";
						else if (cond == 4)
						{
							if (st.hasQuestItems(AVELLAN_SPICE))
								htmltext = "30620-4.htm";
							else
							{
								htmltext = "30620-4a.htm";
								st.exitQuest(true);
							}
						}
						else if (cond == 5)
							htmltext = "30620-5a.htm";
						else if (cond == 6)
							htmltext = "30620-6.htm";
						break;
					
					case LARA:
						if (cond == 3)
							htmltext = "30063-0.htm";
						else if (cond > 3)
							htmltext = "30063-1a.htm";
						break;
					
					case BRIGHT:
						if (cond == 5)
							htmltext = "30466-0.htm";
						else if (cond > 5)
							htmltext = "30466-1a.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = getRandomPartyMember(player, npc, "1");
		if (st == null)
			return null;
		
		if (st.dropItems(HONEY_POUCH, 1, 100, (npc.getNpcId() == 20934) ? 571000 : 625000))
			st.set("cond", "2");
		
		return null;
	}
}
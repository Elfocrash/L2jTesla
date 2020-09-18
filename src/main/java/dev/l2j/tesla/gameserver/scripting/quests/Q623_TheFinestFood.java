package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q623_TheFinestFood extends Quest
{
	private static final String qn = "Q623_TheFinestFood";
	
	// Items
	private static final int LEAF_OF_FLAVA = 7199;
	private static final int BUFFALO_MEAT = 7200;
	private static final int ANTELOPE_HORN = 7201;
	
	// NPC
	private static final int JEREMY = 31521;
	
	// Monsters
	private static final int FLAVA = 21316;
	private static final int BUFFALO = 21315;
	private static final int ANTELOPE = 21318;
	
	public Q623_TheFinestFood()
	{
		super(623, "The Finest Food");
		
		setItemsIds(LEAF_OF_FLAVA, BUFFALO_MEAT, ANTELOPE_HORN);
		
		addStartNpc(JEREMY);
		addTalkId(JEREMY);
		
		addKillId(FLAVA, BUFFALO, ANTELOPE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31521-02.htm"))
		{
			if (player.getLevel() >= 71)
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
			else
				htmltext = "31521-03.htm";
		}
		else if (event.equalsIgnoreCase("31521-05.htm"))
		{
			st.takeItems(LEAF_OF_FLAVA, -1);
			st.takeItems(BUFFALO_MEAT, -1);
			st.takeItems(ANTELOPE_HORN, -1);
			
			int luck = Rnd.get(100);
			if (luck < 11)
			{
				st.rewardItems(57, 25000);
				st.giveItems(6849, 1);
			}
			else if (luck < 23)
			{
				st.rewardItems(57, 65000);
				st.giveItems(6847, 1);
			}
			else if (luck < 33)
			{
				st.rewardItems(57, 25000);
				st.giveItems(6851, 1);
			}
			else
			{
				st.rewardItems(57, 73000);
				st.rewardExpAndSp(230000, 18250);
			}
			
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
				htmltext = "31521-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "31521-06.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(LEAF_OF_FLAVA) >= 100 && st.getQuestItemsCount(BUFFALO_MEAT) >= 100 && st.getQuestItemsCount(ANTELOPE_HORN) >= 100)
						htmltext = "31521-04.htm";
					else
						htmltext = "31521-07.htm";
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
		
		switch (npc.getNpcId())
		{
			case FLAVA:
				if (st.dropItemsAlways(LEAF_OF_FLAVA, 1, 100) && st.getQuestItemsCount(BUFFALO_MEAT) >= 100 && st.getQuestItemsCount(ANTELOPE_HORN) >= 100)
					st.set("cond", "2");
				break;
			
			case BUFFALO:
				if (st.dropItemsAlways(BUFFALO_MEAT, 1, 100) && st.getQuestItemsCount(LEAF_OF_FLAVA) >= 100 && st.getQuestItemsCount(ANTELOPE_HORN) >= 100)
					st.set("cond", "2");
				break;
			
			case ANTELOPE:
				if (st.dropItemsAlways(ANTELOPE_HORN, 1, 100) && st.getQuestItemsCount(LEAF_OF_FLAVA) >= 100 && st.getQuestItemsCount(BUFFALO_MEAT) >= 100)
					st.set("cond", "2");
				break;
		}
		
		return null;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q356_DigUpTheSeaOfSpores extends Quest
{
	private static final String qn = "Q356_DigUpTheSeaOfSpores";
	
	// Items
	private static final int HERB_SPORE = 5866;
	private static final int CARN_SPORE = 5865;
	
	// Monsters
	private static final int ROTTING_TREE = 20558;
	private static final int SPORE_ZOMBIE = 20562;
	
	public Q356_DigUpTheSeaOfSpores()
	{
		super(356, "Dig Up the Sea of Spores!");
		
		setItemsIds(HERB_SPORE, CARN_SPORE);
		
		addStartNpc(30717); // Gauen
		addTalkId(30717);
		
		addKillId(ROTTING_TREE, SPORE_ZOMBIE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30717-06.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30717-17.htm"))
		{
			st.takeItems(HERB_SPORE, -1);
			st.takeItems(CARN_SPORE, -1);
			st.rewardItems(57, 20950);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30717-14.htm"))
		{
			st.takeItems(HERB_SPORE, -1);
			st.takeItems(CARN_SPORE, -1);
			st.rewardExpAndSp(35000, 2600);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30717-12.htm"))
		{
			st.takeItems(HERB_SPORE, -1);
			st.rewardExpAndSp(24500, 0);
		}
		else if (event.equalsIgnoreCase("30717-13.htm"))
		{
			st.takeItems(CARN_SPORE, -1);
			st.rewardExpAndSp(0, 1820);
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
				htmltext = (player.getLevel() < 43) ? "30717-01.htm" : "30717-02.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "30717-07.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(HERB_SPORE) >= 50)
						htmltext = "30717-08.htm";
					else if (st.getQuestItemsCount(CARN_SPORE) >= 50)
						htmltext = "30717-09.htm";
					else
						htmltext = "30717-07.htm";
				}
				else if (cond == 3)
					htmltext = "30717-10.htm";
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
		
		final int cond = st.getInt("cond");
		if (cond < 3)
		{
			switch (npc.getNpcId())
			{
				case ROTTING_TREE:
					if (st.dropItems(HERB_SPORE, 1, 50, 630000))
						st.set("cond", (cond == 2) ? "3" : "2");
					break;
				
				case SPORE_ZOMBIE:
					if (st.dropItems(CARN_SPORE, 1, 50, 760000))
						st.set("cond", (cond == 2) ? "3" : "2");
					break;
			}
		}
		
		return null;
	}
}
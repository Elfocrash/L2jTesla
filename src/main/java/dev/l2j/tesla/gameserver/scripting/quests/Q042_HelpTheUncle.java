package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q042_HelpTheUncle extends Quest
{
	private static final String qn = "Q042_HelpTheUncle";
	
	// NPCs
	private static final int WATERS = 30828;
	private static final int SOPHYA = 30735;
	
	// Items
	private static final int TRIDENT = 291;
	private static final int MAP_PIECE = 7548;
	private static final int MAP = 7549;
	private static final int PET_TICKET = 7583;
	
	// Monsters
	private static final int MONSTER_EYE_DESTROYER = 20068;
	private static final int MONSTER_EYE_GAZER = 20266;
	
	public Q042_HelpTheUncle()
	{
		super(42, "Help the Uncle!");
		
		setItemsIds(MAP_PIECE, MAP);
		
		addStartNpc(WATERS);
		addTalkId(WATERS, SOPHYA);
		
		addKillId(MONSTER_EYE_DESTROYER, MONSTER_EYE_GAZER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30828-01.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30828-03.htm") && st.hasQuestItems(TRIDENT))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(TRIDENT, 1);
		}
		else if (event.equalsIgnoreCase("30828-05.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(MAP_PIECE, 30);
			st.giveItems(MAP, 1);
		}
		else if (event.equalsIgnoreCase("30735-06.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(MAP, 1);
		}
		else if (event.equalsIgnoreCase("30828-07.htm"))
		{
			st.giveItems(PET_TICKET, 1);
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
				htmltext = (player.getLevel() < 25) ? "30828-00a.htm" : "30828-00.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case WATERS:
						if (cond == 1)
							htmltext = (!st.hasQuestItems(TRIDENT)) ? "30828-01a.htm" : "30828-02.htm";
						else if (cond == 2)
							htmltext = "30828-03a.htm";
						else if (cond == 3)
							htmltext = "30828-04.htm";
						else if (cond == 4)
							htmltext = "30828-05a.htm";
						else if (cond == 5)
							htmltext = "30828-06.htm";
						break;
					
					case SOPHYA:
						if (cond == 4)
							htmltext = "30735-05.htm";
						else if (cond == 5)
							htmltext = "30735-06a.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "2");
		if (st == null)
			return null;
		
		if (st.dropItemsAlways(MAP_PIECE, 1, 30))
			st.set("cond", "3");
		
		return null;
	}
}
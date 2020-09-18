package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q163_LegacyOfThePoet extends Quest
{
	private static final String qn = "Q163_LegacyOfThePoet";
	
	// NPC
	private static final int STARDEN = 30220;
	
	// Items
	private static final int[] RUMIELS_POEMS =
	{
		1038,
		1039,
		1040,
		1041
	};
	
	// Droplist
	private static final int[][] DROPLIST =
	{
		{
			RUMIELS_POEMS[0],
			1,
			1,
			100000
		},
		{
			RUMIELS_POEMS[1],
			1,
			1,
			200000
		},
		{
			RUMIELS_POEMS[2],
			1,
			1,
			200000
		},
		{
			RUMIELS_POEMS[3],
			1,
			1,
			400000
		}
	};
	
	public Q163_LegacyOfThePoet()
	{
		super(163, "Legacy of the Poet");
		
		setItemsIds(RUMIELS_POEMS);
		
		addStartNpc(STARDEN);
		addTalkId(STARDEN);
		
		addKillId(20372, 20373);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30220-07.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
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
				if (player.getRace() == ClassRace.DARK_ELF)
					htmltext = "30220-00.htm";
				else if (player.getLevel() < 11)
					htmltext = "30220-02.htm";
				else
					htmltext = "30220-03.htm";
				break;
			
			case STATE_STARTED:
				if (st.getInt("cond") == 2)
				{
					htmltext = "30220-09.htm";
					
					for (int poem : RUMIELS_POEMS)
						st.takeItems(poem, -1);
					
					st.rewardItems(57, 13890);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				else
					htmltext = "30220-08.htm";
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
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (st.dropMultipleItems(DROPLIST))
			st.set("cond", "2");
		
		return null;
	}
}
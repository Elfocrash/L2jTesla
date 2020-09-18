package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q275_DarkWingedSpies extends Quest
{
	private static final String qn = "Q275_DarkWingedSpies";
	
	// Monsters
	private static final int DARKWING_BAT = 20316;
	private static final int VARANGKA_TRACKER = 27043;
	
	// Items
	private static final int DARKWING_BAT_FANG = 1478;
	private static final int VARANGKA_PARASITE = 1479;
	
	public Q275_DarkWingedSpies()
	{
		super(275, "Dark Winged Spies");
		
		setItemsIds(DARKWING_BAT_FANG, VARANGKA_PARASITE);
		
		addStartNpc(30567); // Tantus
		addTalkId(30567);
		
		addKillId(DARKWING_BAT, VARANGKA_TRACKER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30567-03.htm"))
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
				if (player.getRace() != ClassRace.ORC)
					htmltext = "30567-00.htm";
				else if (player.getLevel() < 11)
					htmltext = "30567-01.htm";
				else
					htmltext = "30567-02.htm";
				break;
			
			case STATE_STARTED:
				if (st.getInt("cond") == 1)
					htmltext = "30567-04.htm";
				else
				{
					htmltext = "30567-05.htm";
					st.takeItems(DARKWING_BAT_FANG, -1);
					st.takeItems(VARANGKA_PARASITE, -1);
					st.rewardItems(57, 4200);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
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
			case DARKWING_BAT:
				if (st.dropItemsAlways(DARKWING_BAT_FANG, 1, 70))
					st.set("cond", "2");
				else if (Rnd.get(100) < 10 && st.getQuestItemsCount(DARKWING_BAT_FANG) > 10 && st.getQuestItemsCount(DARKWING_BAT_FANG) < 66)
				{
					// Spawn of Varangka Tracker on the npc position.
					addSpawn(VARANGKA_TRACKER, npc, true, 0, true);
					
					st.giveItems(VARANGKA_PARASITE, 1);
				}
				break;
			
			case VARANGKA_TRACKER:
				if (st.hasQuestItems(VARANGKA_PARASITE))
				{
					st.takeItems(VARANGKA_PARASITE, -1);
					
					if (st.dropItemsAlways(DARKWING_BAT_FANG, 5, 70))
						st.set("cond", "2");
				}
				break;
		}
		
		return null;
	}
}
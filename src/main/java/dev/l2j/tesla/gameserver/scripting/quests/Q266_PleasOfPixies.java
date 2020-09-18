package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q266_PleasOfPixies extends Quest
{
	private static final String qn = "Q266_PleasOfPixies";
	
	// Items
	private static final int PREDATOR_FANG = 1334;
	
	// Rewards
	private static final int GLASS_SHARD = 1336;
	private static final int EMERALD = 1337;
	private static final int BLUE_ONYX = 1338;
	private static final int ONYX = 1339;
	
	public Q266_PleasOfPixies()
	{
		super(266, "Pleas of Pixies");
		
		setItemsIds(PREDATOR_FANG);
		
		addStartNpc(31852); // Murika
		addTalkId(31852);
		
		addKillId(20525, 20530, 20534, 20537);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31852-03.htm"))
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
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (player.getRace() != ClassRace.ELF)
					htmltext = "31852-00.htm";
				else if (player.getLevel() < 3)
					htmltext = "31852-01.htm";
				else
					htmltext = "31852-02.htm";
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(PREDATOR_FANG) < 100)
					htmltext = "31852-04.htm";
				else
				{
					htmltext = "31852-05.htm";
					st.takeItems(PREDATOR_FANG, -1);
					
					final int n = Rnd.get(100);
					if (n < 10)
					{
						st.playSound(QuestState.SOUND_JACKPOT);
						st.rewardItems(EMERALD, 1);
					}
					else if (n < 30)
						st.rewardItems(BLUE_ONYX, 1);
					else if (n < 60)
						st.rewardItems(ONYX, 1);
					else
						st.rewardItems(GLASS_SHARD, 1);
					
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
			case 20525:
				if (st.dropItemsAlways(PREDATOR_FANG, Rnd.get(2, 3), 100))
					st.set("cond", "2");
				break;
			
			case 20530:
				if (st.dropItems(PREDATOR_FANG, 1, 100, 800000))
					st.set("cond", "2");
				break;
			
			case 20534:
				if (st.dropItems(PREDATOR_FANG, (Rnd.get(3) == 0) ? 1 : 2, 100, 600000))
					st.set("cond", "2");
				break;
			
			case 20537:
				if (st.dropItemsAlways(PREDATOR_FANG, 2, 100))
					st.set("cond", "2");
				break;
		}
		
		return null;
	}
}
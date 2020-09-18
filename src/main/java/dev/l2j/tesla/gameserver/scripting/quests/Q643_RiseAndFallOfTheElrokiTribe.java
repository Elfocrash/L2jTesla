package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q643_RiseAndFallOfTheElrokiTribe extends Quest
{
	private static final String qn = "Q643_RiseAndFallOfTheElrokiTribe";
	
	// NPCs
	private static final int SINGSING = 32106;
	private static final int KARAKAWEI = 32117;
	
	// Items
	private static final int BONES = 8776;
	
	public Q643_RiseAndFallOfTheElrokiTribe()
	{
		super(643, "Rise and Fall of the Elroki Tribe");
		
		setItemsIds(BONES);
		
		addStartNpc(SINGSING);
		addTalkId(SINGSING, KARAKAWEI);
		
		addKillId(22208, 22209, 22210, 22211, 22212, 22213, 22221, 22222, 22226, 22227);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32106-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32106-07.htm"))
		{
			final int count = st.getQuestItemsCount(BONES);
			
			st.takeItems(BONES, count);
			st.rewardItems(57, count * 1374);
		}
		else if (event.equalsIgnoreCase("32106-09.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("32117-03.htm"))
		{
			final int count = st.getQuestItemsCount(BONES);
			if (count >= 300)
			{
				st.takeItems(BONES, 300);
				st.rewardItems(Rnd.get(8712, 8722), 5);
			}
			else
				htmltext = "32117-04.htm";
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
				htmltext = (player.getLevel() < 75) ? "32106-00.htm" : "32106-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case SINGSING:
						htmltext = (st.hasQuestItems(BONES)) ? "32106-06.htm" : "32106-05.htm";
						break;
					
					case KARAKAWEI:
						htmltext = "32117-01.htm";
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
		
		final QuestState st = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		st.dropItems(BONES, 1, 0, 750000);
		
		return null;
	}
}
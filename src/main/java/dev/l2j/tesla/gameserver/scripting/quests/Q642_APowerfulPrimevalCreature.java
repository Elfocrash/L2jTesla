package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q642_APowerfulPrimevalCreature extends Quest
{
	private static final String qn = "Q642_APowerfulPrimevalCreature";
	
	// Items
	private static final int DINOSAUR_TISSUE = 8774;
	private static final int DINOSAUR_EGG = 8775;
	
	private static final int ANCIENT_EGG = 18344;
	
	// Rewards
	private static final int[] REWARDS =
	{
		8690,
		8692,
		8694,
		8696,
		8698,
		8700,
		8702,
		8704,
		8706,
		8708,
		8710
	};
	
	public Q642_APowerfulPrimevalCreature()
	{
		super(642, "A Powerful Primeval Creature");
		
		setItemsIds(DINOSAUR_TISSUE, DINOSAUR_EGG);
		
		addStartNpc(32105); // Dinn
		addTalkId(32105);
		
		// Dinosaurs + egg
		addKillId(22196, 22197, 22198, 22199, 22200, 22201, 22202, 22203, 22204, 22205, 22218, 22219, 22220, 22223, 22224, 22225, ANCIENT_EGG);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32105-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32105-08.htm"))
		{
			if (st.getQuestItemsCount(DINOSAUR_TISSUE) >= 150 && st.hasQuestItems(DINOSAUR_EGG))
				htmltext = "32105-06.htm";
		}
		else if (event.equalsIgnoreCase("32105-07.htm"))
		{
			final int tissues = st.getQuestItemsCount(DINOSAUR_TISSUE);
			if (tissues > 0)
			{
				st.takeItems(DINOSAUR_TISSUE, -1);
				st.rewardItems(57, tissues * 5000);
			}
			else
				htmltext = "32105-08.htm";
		}
		else if (event.contains("event_"))
		{
			if (st.getQuestItemsCount(DINOSAUR_TISSUE) >= 150 && st.hasQuestItems(DINOSAUR_EGG))
			{
				htmltext = "32105-07.htm";
				
				st.takeItems(DINOSAUR_TISSUE, 150);
				st.takeItems(DINOSAUR_EGG, 1);
				st.rewardItems(57, 44000);
				st.giveItems(REWARDS[Integer.parseInt(event.split("_")[1])], 1);
			}
			else
				htmltext = "32105-08.htm";
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
				htmltext = (player.getLevel() < 75) ? "32105-00.htm" : "32105-01.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (!st.hasQuestItems(DINOSAUR_TISSUE)) ? "32105-08.htm" : "32105-05.htm";
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
		
		if (npc.getNpcId() == ANCIENT_EGG)
		{
			if (Rnd.get(100) < 1)
			{
				st.giveItems(DINOSAUR_EGG, 1);
				
				if (st.getQuestItemsCount(DINOSAUR_TISSUE) >= 150)
					st.playSound(QuestState.SOUND_MIDDLE);
				else
					st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		else if (Rnd.get(100) < 33)
		{
			st.rewardItems(DINOSAUR_TISSUE, 1);
			
			if (st.getQuestItemsCount(DINOSAUR_TISSUE) >= 150 && st.hasQuestItems(DINOSAUR_EGG))
				st.playSound(QuestState.SOUND_MIDDLE);
			else
				st.playSound(QuestState.SOUND_ITEMGET);
		}
		
		return null;
	}
}
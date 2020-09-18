package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q159_ProtectTheWaterSource extends Quest
{
	private static final String qn = "Q159_ProtectTheWaterSource";
	
	// Items
	private static final int PLAGUE_DUST = 1035;
	private static final int HYACINTH_CHARM_1 = 1071;
	private static final int HYACINTH_CHARM_2 = 1072;
	
	public Q159_ProtectTheWaterSource()
	{
		super(159, "Protect the Water Source");
		
		setItemsIds(PLAGUE_DUST, HYACINTH_CHARM_1, HYACINTH_CHARM_2);
		
		addStartNpc(30154); // Asterios
		addTalkId(30154);
		
		addKillId(27017); // Plague Zombie
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30154-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(HYACINTH_CHARM_1, 1);
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
				if (player.getRace() != ClassRace.ELF)
					htmltext = "30154-00.htm";
				else if (player.getLevel() < 12)
					htmltext = "30154-02.htm";
				else
					htmltext = "30154-03.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "30154-05.htm";
				else if (cond == 2)
				{
					htmltext = "30154-06.htm";
					st.set("cond", "3");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(PLAGUE_DUST, -1);
					st.takeItems(HYACINTH_CHARM_1, 1);
					st.giveItems(HYACINTH_CHARM_2, 1);
				}
				else if (cond == 3)
					htmltext = "30154-07.htm";
				else if (cond == 4)
				{
					htmltext = "30154-08.htm";
					st.takeItems(HYACINTH_CHARM_2, 1);
					st.takeItems(PLAGUE_DUST, -1);
					st.rewardItems(57, 18250);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
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
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		if (st.getInt("cond") == 1 && st.dropItems(PLAGUE_DUST, 1, 1, 400000))
			st.set("cond", "2");
		else if (st.getInt("cond") == 3 && st.dropItems(PLAGUE_DUST, 1, 5, 400000))
			st.set("cond", "4");
		
		return null;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q117_TheOceanOfDistantStars extends Quest
{
	private static final String qn = "Q117_TheOceanOfDistantStars";
	
	// NPCs
	private static final int ABEY = 32053;
	private static final int GHOST = 32054;
	private static final int ANCIENT_GHOST = 32055;
	private static final int OBI = 32052;
	private static final int BOX = 32076;
	
	// Items
	private static final int GREY_STAR = 8495;
	private static final int ENGRAVED_HAMMER = 8488;
	
	// Monsters
	private static final int BANDIT_WARRIOR = 22023;
	private static final int BANDIT_INSPECTOR = 22024;
	
	public Q117_TheOceanOfDistantStars()
	{
		super(117, "The Ocean of Distant Stars");
		
		setItemsIds(GREY_STAR, ENGRAVED_HAMMER);
		
		addStartNpc(ABEY);
		addTalkId(ABEY, ANCIENT_GHOST, GHOST, OBI, BOX);
		addKillId(BANDIT_WARRIOR, BANDIT_INSPECTOR);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32053-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32055-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32052-02.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32053-04.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32076-02.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(ENGRAVED_HAMMER, 1);
		}
		else if (event.equalsIgnoreCase("32053-06.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32052-04.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32052-06.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(GREY_STAR, 1);
		}
		else if (event.equalsIgnoreCase("32055-04.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ENGRAVED_HAMMER, 1);
		}
		else if (event.equalsIgnoreCase("32054-03.htm"))
		{
			st.rewardExpAndSp(63591, 0);
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
				htmltext = (player.getLevel() < 39) ? "32053-00.htm" : "32053-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ANCIENT_GHOST:
						if (cond == 1)
							htmltext = "32055-01.htm";
						else if (cond > 1 && cond < 9)
							htmltext = "32055-02.htm";
						else if (cond == 9)
							htmltext = "32055-03.htm";
						else if (cond > 9)
							htmltext = "32055-05.htm";
						break;
					
					case OBI:
						if (cond == 2)
							htmltext = "32052-01.htm";
						else if (cond > 2 && cond < 6)
							htmltext = "32052-02.htm";
						else if (cond == 6)
							htmltext = "32052-03.htm";
						else if (cond == 7)
							htmltext = "32052-04.htm";
						else if (cond == 8)
							htmltext = "32052-05.htm";
						else if (cond > 8)
							htmltext = "32052-06.htm";
						break;
					
					case ABEY:
						if (cond == 1 || cond == 2)
							htmltext = "32053-02.htm";
						else if (cond == 3)
							htmltext = "32053-03.htm";
						else if (cond == 4)
							htmltext = "32053-04.htm";
						else if (cond == 5)
							htmltext = "32053-05.htm";
						else if (cond > 5)
							htmltext = "32053-06.htm";
						break;
					
					case BOX:
						if (cond == 4)
							htmltext = "32076-01.htm";
						else if (cond > 4)
							htmltext = "32076-03.htm";
						break;
					
					case GHOST:
						if (cond == 10)
							htmltext = "32054-01.htm";
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
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "7");
		if (st == null)
			return null;
		
		if (st.dropItems(GREY_STAR, 1, 1, 200000))
			st.set("cond", "8");
		
		return null;
	}
}
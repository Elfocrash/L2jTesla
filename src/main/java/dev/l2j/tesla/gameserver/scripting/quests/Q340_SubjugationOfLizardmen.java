package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q340_SubjugationOfLizardmen extends Quest
{
	private static final String qn = "Q340_SubjugationOfLizardmen";
	
	// NPCs
	private static final int WEISZ = 30385;
	private static final int ADONIUS = 30375;
	private static final int LEVIAN = 30037;
	private static final int CHEST = 30989;
	
	// Items
	private static final int CARGO = 4255;
	private static final int HOLY = 4256;
	private static final int ROSARY = 4257;
	private static final int TOTEM = 4258;
	
	public Q340_SubjugationOfLizardmen()
	{
		super(340, "Subjugation of Lizardmen");
		
		setItemsIds(CARGO, HOLY, ROSARY, TOTEM);
		
		addStartNpc(WEISZ);
		addTalkId(WEISZ, ADONIUS, LEVIAN, CHEST);
		
		addKillId(20008, 20010, 20014, 20024, 20027, 20030, 25146);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30385-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30385-07.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CARGO, -1);
		}
		else if (event.equalsIgnoreCase("30385-09.htm"))
		{
			st.takeItems(CARGO, -1);
			st.rewardItems(57, 4090);
		}
		else if (event.equalsIgnoreCase("30385-10.htm"))
		{
			st.takeItems(CARGO, -1);
			st.rewardItems(57, 4090);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30375-02.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30037-02.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30989-02.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(TOTEM, 1);
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
				htmltext = (player.getLevel() < 17) ? "30385-01.htm" : "30385-02.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case WEISZ:
						if (cond == 1)
							htmltext = (st.getQuestItemsCount(CARGO) < 30) ? "30385-05.htm" : "30385-06.htm";
						else if (cond == 2)
							htmltext = "30385-11.htm";
						else if (cond == 7)
						{
							htmltext = "30385-13.htm";
							st.rewardItems(57, 14700);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case ADONIUS:
						if (cond == 2)
							htmltext = "30375-01.htm";
						else if (cond == 3)
						{
							if (st.hasQuestItems(ROSARY, HOLY))
							{
								htmltext = "30375-04.htm";
								st.set("cond", "4");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(HOLY, -1);
								st.takeItems(ROSARY, -1);
							}
							else
								htmltext = "30375-03.htm";
						}
						else if (cond == 4)
							htmltext = "30375-05.htm";
						break;
					
					case LEVIAN:
						if (cond == 4)
							htmltext = "30037-01.htm";
						else if (cond == 5)
							htmltext = "30037-03.htm";
						else if (cond == 6)
						{
							htmltext = "30037-04.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(TOTEM, -1);
						}
						else if (cond == 7)
							htmltext = "30037-05.htm";
						break;
					
					case CHEST:
						if (cond == 5)
							htmltext = "30989-01.htm";
						else
							htmltext = "30989-03.htm";
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
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		switch (npc.getNpcId())
		{
			case 20008:
				if (st.getInt("cond") == 1)
					st.dropItems(CARGO, 1, 30, 500000);
				break;
			
			case 20010:
				if (st.getInt("cond") == 1)
					st.dropItems(CARGO, 1, 30, 520000);
				break;
			
			case 20014:
				if (st.getInt("cond") == 1)
					st.dropItems(CARGO, 1, 30, 550000);
				break;
			
			case 20024:
			case 20027:
			case 20030:
				if (st.getInt("cond") == 3)
				{
					if (st.dropItems(HOLY, 1, 1, 100000))
						st.dropItems(ROSARY, 1, 1, 100000);
				}
				break;
			
			case 25146:
				addSpawn(CHEST, npc, false, 30000, false);
				break;
		}
		return null;
	}
}
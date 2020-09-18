package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q292_BrigandsSweep extends Quest
{
	private static final String qn = "Q292_BrigandsSweep";
	
	// NPCs
	private static final int SPIRON = 30532;
	private static final int BALANKI = 30533;
	
	// Items
	private static final int GOBLIN_NECKLACE = 1483;
	private static final int GOBLIN_PENDANT = 1484;
	private static final int GOBLIN_LORD_PENDANT = 1485;
	private static final int SUSPICIOUS_MEMO = 1486;
	private static final int SUSPICIOUS_CONTRACT = 1487;
	
	// Monsters
	private static final int GOBLIN_BRIGAND = 20322;
	private static final int GOBLIN_BRIGAND_LEADER = 20323;
	private static final int GOBLIN_BRIGAND_LIEUTENANT = 20324;
	private static final int GOBLIN_SNOOPER = 20327;
	private static final int GOBLIN_LORD = 20528;
	
	public Q292_BrigandsSweep()
	{
		super(292, "Brigands Sweep");
		
		setItemsIds(GOBLIN_NECKLACE, GOBLIN_PENDANT, GOBLIN_LORD_PENDANT, SUSPICIOUS_MEMO, SUSPICIOUS_CONTRACT);
		
		addStartNpc(SPIRON);
		addTalkId(SPIRON, BALANKI);
		
		addKillId(GOBLIN_BRIGAND, GOBLIN_BRIGAND_LEADER, GOBLIN_BRIGAND_LIEUTENANT, GOBLIN_SNOOPER, GOBLIN_LORD);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30532-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30532-06.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				if (player.getRace() != ClassRace.DWARF)
					htmltext = "30532-00.htm";
				else if (player.getLevel() < 5)
					htmltext = "30532-01.htm";
				else
					htmltext = "30532-02.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case SPIRON:
						final int goblinNecklaces = st.getQuestItemsCount(GOBLIN_NECKLACE);
						final int goblinPendants = st.getQuestItemsCount(GOBLIN_PENDANT);
						final int goblinLordPendants = st.getQuestItemsCount(GOBLIN_LORD_PENDANT);
						final int suspiciousMemos = st.getQuestItemsCount(SUSPICIOUS_MEMO);
						
						final int countAll = goblinNecklaces + goblinPendants + goblinLordPendants;
						
						final boolean hasContract = st.hasQuestItems(SUSPICIOUS_CONTRACT);
						
						if (countAll == 0)
							htmltext = "30532-04.htm";
						else
						{
							if (hasContract)
								htmltext = "30532-10.htm";
							else if (suspiciousMemos > 0)
							{
								if (suspiciousMemos > 1)
									htmltext = "30532-09.htm";
								else
									htmltext = "30532-08.htm";
							}
							else
								htmltext = "30532-05.htm";
							
							st.takeItems(GOBLIN_NECKLACE, -1);
							st.takeItems(GOBLIN_PENDANT, -1);
							st.takeItems(GOBLIN_LORD_PENDANT, -1);
							
							if (hasContract)
							{
								st.set("cond", "1");
								st.takeItems(SUSPICIOUS_CONTRACT, -1);
							}
							
							st.rewardItems(57, ((12 * goblinNecklaces) + (36 * goblinPendants) + (33 * goblinLordPendants) + (countAll >= 10 ? 1000 : 0) + ((hasContract) ? 1120 : 0)));
						}
						break;
					
					case BALANKI:
						if (!st.hasQuestItems(SUSPICIOUS_CONTRACT))
							htmltext = "30533-01.htm";
						else
						{
							htmltext = "30533-02.htm";
							st.set("cond", "1");
							st.takeItems(SUSPICIOUS_CONTRACT, -1);
							st.rewardItems(57, 1500);
						}
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
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		final int chance = Rnd.get(10);
		
		if (chance > 5)
		{
			switch (npc.getNpcId())
			{
				case GOBLIN_BRIGAND:
				case GOBLIN_SNOOPER:
				case GOBLIN_BRIGAND_LIEUTENANT:
					st.dropItemsAlways(GOBLIN_NECKLACE, 1, 0);
					break;
				
				case GOBLIN_BRIGAND_LEADER:
					st.dropItemsAlways(GOBLIN_PENDANT, 1, 0);
					break;
				
				case GOBLIN_LORD:
					st.dropItemsAlways(GOBLIN_LORD_PENDANT, 1, 0);
					break;
			}
		}
		else if (chance > 4 && st.getInt("cond") == 1 && st.dropItemsAlways(SUSPICIOUS_MEMO, 1, 3))
		{
			st.set("cond", "2");
			st.takeItems(SUSPICIOUS_MEMO, -1);
			st.giveItems(SUSPICIOUS_CONTRACT, 1);
		}
		
		return null;
	}
}
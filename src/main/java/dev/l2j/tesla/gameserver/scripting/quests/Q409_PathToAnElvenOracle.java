package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q409_PathToAnElvenOracle extends Quest
{
	private static final String qn = "Q409_PathToAnElvenOracle";
	
	// Items
	private static final int CRYSTAL_MEDALLION = 1231;
	private static final int SWINDLER_MONEY = 1232;
	private static final int ALLANA_DIARY = 1233;
	private static final int LIZARD_CAPTAIN_ORDER = 1234;
	private static final int LEAF_OF_ORACLE = 1235;
	private static final int HALF_OF_DIARY = 1236;
	private static final int TAMIL_NECKLACE = 1275;
	
	// NPCs
	private static final int MANUEL = 30293;
	private static final int ALLANA = 30424;
	private static final int PERRIN = 30428;
	
	public Q409_PathToAnElvenOracle()
	{
		super(409, "Path to an Elven Oracle");
		
		setItemsIds(CRYSTAL_MEDALLION, SWINDLER_MONEY, ALLANA_DIARY, LIZARD_CAPTAIN_ORDER, HALF_OF_DIARY, TAMIL_NECKLACE);
		
		addStartNpc(MANUEL);
		addTalkId(MANUEL, ALLANA, PERRIN);
		
		addKillId(27032, 27033, 27034, 27035);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30293-05.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(CRYSTAL_MEDALLION, 1);
		}
		else if (event.equalsIgnoreCase("spawn_lizards"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			addSpawn(27032, -92319, 154235, -3284, 2000, false, 0, false);
			addSpawn(27033, -92361, 154190, -3284, 2000, false, 0, false);
			addSpawn(27034, -92375, 154278, -3278, 2000, false, 0, false);
			return null;
		}
		else if (event.equalsIgnoreCase("30428-06.htm"))
			addSpawn(27035, -93194, 147587, -2672, 2000, false, 0, true);
		
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
				if (player.getClassId() != ClassId.ELVEN_MYSTIC)
					htmltext = (player.getClassId() == ClassId.ELVEN_ORACLE) ? "30293-02a.htm" : "30293-02.htm";
				else if (player.getLevel() < 19)
					htmltext = "30293-03.htm";
				else if (st.hasQuestItems(LEAF_OF_ORACLE))
					htmltext = "30293-04.htm";
				else
					htmltext = "30293-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case MANUEL:
						if (cond == 1)
							htmltext = "30293-06.htm";
						else if (cond == 2 || cond == 3)
							htmltext = "30293-09.htm";
						else if (cond > 3 && cond < 7)
							htmltext = "30293-07.htm";
						else if (cond == 7)
						{
							htmltext = "30293-08.htm";
							st.takeItems(ALLANA_DIARY, 1);
							st.takeItems(CRYSTAL_MEDALLION, 1);
							st.takeItems(LIZARD_CAPTAIN_ORDER, 1);
							st.takeItems(SWINDLER_MONEY, 1);
							st.giveItems(LEAF_OF_ORACLE, 1);
							st.rewardExpAndSp(3200, 1130);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case ALLANA:
						if (cond == 1)
							htmltext = "30424-01.htm";
						else if (cond == 3)
						{
							htmltext = "30424-02.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(HALF_OF_DIARY, 1);
						}
						else if (cond == 4)
							htmltext = "30424-03.htm";
						else if (cond == 5)
							htmltext = "30424-06.htm";
						else if (cond == 6)
						{
							htmltext = "30424-04.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(HALF_OF_DIARY, -1);
							st.giveItems(ALLANA_DIARY, 1);
						}
						else if (cond == 7)
							htmltext = "30424-05.htm";
						break;
					
					case PERRIN:
						if (cond == 4)
							htmltext = "30428-01.htm";
						else if (cond == 5)
						{
							htmltext = "30428-04.htm";
							st.set("cond", "6");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(TAMIL_NECKLACE, -1);
							st.giveItems(SWINDLER_MONEY, 1);
						}
						else if (cond > 5)
							htmltext = "30428-05.htm";
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
		
		if (npc.getNpcId() == 27035)
		{
			if (st.getInt("cond") == 4)
			{
				st.set("cond", "5");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(TAMIL_NECKLACE, 1);
			}
		}
		else if (st.getInt("cond") == 2)
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(LIZARD_CAPTAIN_ORDER, 1);
		}
		
		return null;
	}
}
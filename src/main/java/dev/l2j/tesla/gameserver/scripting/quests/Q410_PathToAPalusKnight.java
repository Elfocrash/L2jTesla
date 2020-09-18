package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q410_PathToAPalusKnight extends Quest
{
	private static final String qn = "Q410_PathToAPalusKnight";
	
	// Items
	private static final int PALUS_TALISMAN = 1237;
	private static final int LYCANTHROPE_SKULL = 1238;
	private static final int VIRGIL_LETTER = 1239;
	private static final int MORTE_TALISMAN = 1240;
	private static final int PREDATOR_CARAPACE = 1241;
	private static final int ARACHNID_TRACKER_SILK = 1242;
	private static final int COFFIN_OF_ETERNAL_REST = 1243;
	private static final int GAZE_OF_ABYSS = 1244;
	
	// NPCs
	private static final int KALINTA = 30422;
	private static final int VIRGIL = 30329;
	
	// Monsters
	private static final int POISON_SPIDER = 20038;
	private static final int ARACHNID_TRACKER = 20043;
	private static final int LYCANTHROPE = 20049;
	
	public Q410_PathToAPalusKnight()
	{
		super(410, "Path to a Palus Knight");
		
		setItemsIds(PALUS_TALISMAN, LYCANTHROPE_SKULL, VIRGIL_LETTER, MORTE_TALISMAN, PREDATOR_CARAPACE, ARACHNID_TRACKER_SILK, COFFIN_OF_ETERNAL_REST);
		
		addStartNpc(VIRGIL);
		addTalkId(VIRGIL, KALINTA);
		
		addKillId(POISON_SPIDER, ARACHNID_TRACKER, LYCANTHROPE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30329-05.htm"))
		{
			if (player.getClassId() != ClassId.DARK_FIGHTER)
				htmltext = (player.getClassId() == ClassId.PALUS_KNIGHT) ? "30329-02a.htm" : "30329-03.htm";
			else if (player.getLevel() < 19)
				htmltext = "30329-02.htm";
			else if (st.hasQuestItems(GAZE_OF_ABYSS))
				htmltext = "30329-04.htm";
		}
		else if (event.equalsIgnoreCase("30329-06.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(PALUS_TALISMAN, 1);
		}
		else if (event.equalsIgnoreCase("30329-10.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LYCANTHROPE_SKULL, -1);
			st.takeItems(PALUS_TALISMAN, 1);
			st.giveItems(VIRGIL_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("30422-02.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(VIRGIL_LETTER, 1);
			st.giveItems(MORTE_TALISMAN, 1);
		}
		else if (event.equalsIgnoreCase("30422-06.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ARACHNID_TRACKER_SILK, -1);
			st.takeItems(MORTE_TALISMAN, 1);
			st.takeItems(PREDATOR_CARAPACE, -1);
			st.giveItems(COFFIN_OF_ETERNAL_REST, 1);
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
				htmltext = "30329-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case VIRGIL:
						if (cond == 1)
							htmltext = (!st.hasQuestItems(LYCANTHROPE_SKULL)) ? "30329-07.htm" : "30329-08.htm";
						else if (cond == 2)
							htmltext = "30329-09.htm";
						else if (cond > 2 && cond < 6)
							htmltext = "30329-12.htm";
						else if (cond == 6)
						{
							htmltext = "30329-11.htm";
							st.takeItems(COFFIN_OF_ETERNAL_REST, 1);
							st.giveItems(GAZE_OF_ABYSS, 1);
							st.rewardExpAndSp(3200, 1500);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case KALINTA:
						if (cond == 3)
							htmltext = "30422-01.htm";
						else if (cond == 4)
						{
							if (!st.hasQuestItems(ARACHNID_TRACKER_SILK) || !st.hasQuestItems(PREDATOR_CARAPACE))
								htmltext = "30422-03.htm";
							else
								htmltext = "30422-04.htm";
						}
						else if (cond == 5)
							htmltext = "30422-05.htm";
						else if (cond == 6)
							htmltext = "30422-06.htm";
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
		
		switch (npc.getNpcId())
		{
			case LYCANTHROPE:
				if (st.getInt("cond") == 1 && st.dropItemsAlways(LYCANTHROPE_SKULL, 1, 13))
					st.set("cond", "2");
				break;
			
			case ARACHNID_TRACKER:
				if (st.getInt("cond") == 4 && st.dropItemsAlways(ARACHNID_TRACKER_SILK, 1, 5) && st.hasQuestItems(PREDATOR_CARAPACE))
					st.set("cond", "5");
				break;
			
			case POISON_SPIDER:
				if (st.getInt("cond") == 4 && st.dropItemsAlways(PREDATOR_CARAPACE, 1, 1) && st.getQuestItemsCount(ARACHNID_TRACKER_SILK) == 5)
					st.set("cond", "5");
				break;
		}
		
		return null;
	}
}
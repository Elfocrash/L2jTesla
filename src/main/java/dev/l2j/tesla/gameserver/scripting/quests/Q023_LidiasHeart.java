package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q023_LidiasHeart extends Quest
{
	private static final String qn = "Q023_LidiasHeart";
	
	// NPCs
	private static final int INNOCENTIN = 31328;
	private static final int BROKEN_BOOKSHELF = 31526;
	private static final int GHOST_OF_VON_HELLMANN = 31524;
	private static final int TOMBSTONE = 31523;
	private static final int VIOLET = 31386;
	private static final int BOX = 31530;
	
	// NPC instance
	private Npc _ghost = null;
	
	// Items
	private static final int FOREST_OF_DEADMAN_MAP = 7063;
	private static final int SILVER_KEY = 7149;
	private static final int LIDIA_HAIRPIN = 7148;
	private static final int LIDIA_DIARY = 7064;
	private static final int SILVER_SPEAR = 7150;
	
	public Q023_LidiasHeart()
	{
		super(23, "Lidia's Heart");
		
		setItemsIds(SILVER_KEY, LIDIA_DIARY, SILVER_SPEAR);
		
		addStartNpc(INNOCENTIN);
		addTalkId(INNOCENTIN, BROKEN_BOOKSHELF, GHOST_OF_VON_HELLMANN, VIOLET, BOX, TOMBSTONE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31328-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(FOREST_OF_DEADMAN_MAP, 1);
			st.giveItems(SILVER_KEY, 1);
		}
		else if (event.equalsIgnoreCase("31328-06.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31526-05.htm"))
		{
			if (!st.hasQuestItems(LIDIA_HAIRPIN))
			{
				st.giveItems(LIDIA_HAIRPIN, 1);
				if (st.hasQuestItems(LIDIA_DIARY))
				{
					st.set("cond", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else
					st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		else if (event.equalsIgnoreCase("31526-11.htm"))
		{
			if (!st.hasQuestItems(LIDIA_DIARY))
			{
				st.giveItems(LIDIA_DIARY, 1);
				if (st.hasQuestItems(LIDIA_HAIRPIN))
				{
					st.set("cond", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else
					st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		else if (event.equalsIgnoreCase("31328-11.htm"))
		{
			if (st.getInt("cond") < 5)
			{
				st.set("cond", "5");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equalsIgnoreCase("31328-19.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31524-04.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LIDIA_DIARY, 1);
		}
		else if (event.equalsIgnoreCase("31523-02.htm"))
		{
			if (_ghost == null)
			{
				_ghost = addSpawn(31524, 51432, -54570, -3136, 0, false, 60000, true);
				_ghost.broadcastNpcSay("Who awoke me?");
				startQuestTimer("ghost_cleanup", 58000, null, player, false);
			}
		}
		else if (event.equalsIgnoreCase("31523-05.htm"))
		{
			// Don't launch twice the same task...
			if (getQuestTimer("tomb_digger", null, player) == null)
				startQuestTimer("tomb_digger", 10000, null, player, false);
		}
		else if (event.equalsIgnoreCase("tomb_digger"))
		{
			htmltext = "31523-06.htm";
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(SILVER_KEY, 1);
		}
		else if (event.equalsIgnoreCase("31530-02.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(SILVER_KEY, 1);
			st.giveItems(SILVER_SPEAR, 1);
		}
		else if (event.equalsIgnoreCase("ghost_cleanup"))
		{
			_ghost = null;
			return null;
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
				QuestState st2 = player.getQuestState("Q022_TragedyInVonHellmannForest");
				if (st2 != null && st2.isCompleted())
				{
					if (player.getLevel() >= 64)
						htmltext = "31328-01.htm";
					else
						htmltext = "31328-00a.htm";
				}
				else
					htmltext = "31328-00.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case INNOCENTIN:
						if (cond == 1)
							htmltext = "31328-03.htm";
						else if (cond == 2)
							htmltext = "31328-07.htm";
						else if (cond == 4)
							htmltext = "31328-08.htm";
						else if (cond == 5)
						{
							if (st.getInt("diary") == 1)
								htmltext = "31328-14.htm";
							else
								htmltext = "31328-11.htm";
						}
						else if (cond > 5)
							htmltext = "31328-21.htm";
						break;
					
					case BROKEN_BOOKSHELF:
						if (cond == 2)
						{
							htmltext = "31526-00.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 3)
						{
							if (!st.hasQuestItems(LIDIA_DIARY))
								htmltext = (!st.hasQuestItems(LIDIA_HAIRPIN)) ? "31526-02.htm" : "31526-06.htm";
							else if (!st.hasQuestItems(LIDIA_HAIRPIN))
								htmltext = "31526-12.htm";
						}
						else if (cond > 3)
							htmltext = "31526-13.htm";
						break;
					
					case GHOST_OF_VON_HELLMANN:
						if (cond == 6)
							htmltext = "31524-01.htm";
						else if (cond > 6)
							htmltext = "31524-05.htm";
						break;
					
					case TOMBSTONE:
						if (cond == 6)
							htmltext = (_ghost == null) ? "31523-01.htm" : "31523-03.htm";
						else if (cond == 7)
							htmltext = "31523-04.htm";
						else if (cond > 7)
							htmltext = "31523-06.htm";
						break;
					
					case VIOLET:
						if (cond == 8)
						{
							htmltext = "31386-01.htm";
							st.set("cond", "9");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 9)
							htmltext = "31386-02.htm";
						else if (cond == 10)
						{
							if (st.hasQuestItems(SILVER_SPEAR))
							{
								htmltext = "31386-03.htm";
								st.takeItems(SILVER_SPEAR, 1);
								st.rewardItems(57, 100000);
								st.playSound(QuestState.SOUND_FINISH);
								st.exitQuest(false);
							}
							else
							{
								htmltext = "31386-02.htm";
								st.set("cond", "9");
							}
						}
						break;
					
					case BOX:
						if (cond == 9)
							htmltext = "31530-01.htm";
						else if (cond == 10)
							htmltext = "31530-03.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				if (npc.getNpcId() == VIOLET)
					htmltext = "31386-04.htm";
				else
					htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
}
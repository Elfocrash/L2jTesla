package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q115_TheOtherSideOfTruth extends Quest
{
	private final static String qn = "Q115_TheOtherSideOfTruth";
	
	// Items
	private final static int MISA_LETTER = 8079;
	private final static int RAFFORTY_LETTER = 8080;
	private final static int PIECE_OF_TABLET = 8081;
	private final static int REPORT_PIECE = 8082;
	
	// NPCs
	private final static int RAFFORTY = 32020;
	private final static int MISA = 32018;
	private final static int KIERRE = 32022;
	private final static int SCULPTURE_1 = 32021;
	private final static int SCULPTURE_2 = 32077;
	private final static int SCULPTURE_3 = 32078;
	private final static int SCULPTURE_4 = 32079;
	private final static int SUSPICIOUS_MAN = 32019;
	
	// Used to test progression through sculptures. The array consists of value to add, used modulo, tested modulo value, tested values 1/2/3/4.
	private final static Map<Integer, int[]> NPC_VALUES = new HashMap<>();
	{
		NPC_VALUES.put(32021, new int[]
		{
			1,
			2,
			1,
			6,
			10,
			12,
			14
		});
		NPC_VALUES.put(32077, new int[]
		{
			2,
			4,
			1,
			5,
			9,
			12,
			13
		});
		NPC_VALUES.put(32078, new int[]
		{
			4,
			8,
			3,
			3,
			9,
			10,
			11
		});
		NPC_VALUES.put(32079, new int[]
		{
			8,
			0,
			7,
			3,
			5,
			6,
			7
		});
	}
	
	public Q115_TheOtherSideOfTruth()
	{
		super(115, "The Other Side of Truth");
		
		setItemsIds(MISA_LETTER, RAFFORTY_LETTER, PIECE_OF_TABLET, REPORT_PIECE);
		
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY, MISA, KIERRE, SCULPTURE_1, SCULPTURE_2, SCULPTURE_3, SCULPTURE_4);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32020-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32020-05.htm") || event.equalsIgnoreCase("32020-08.htm") || event.equalsIgnoreCase("32020-13.htm"))
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("32020-07.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(MISA_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("32020-11.htm") || event.equalsIgnoreCase("32020-12.htm"))
		{
			if (st.getInt("cond") == 3)
			{
				st.set("cond", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equalsIgnoreCase("32020-17.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32020-23.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(REPORT_PIECE, 1);
		}
		else if (event.equalsIgnoreCase("32020-27.htm"))
		{
			if (!st.hasQuestItems(PIECE_OF_TABLET))
			{
				st.set("cond", "11");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
			{
				htmltext = "32020-25.htm";
				st.takeItems(PIECE_OF_TABLET, 1);
				st.rewardItems(57, 60040);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
			}
		}
		else if (event.equalsIgnoreCase("32020-28.htm"))
		{
			if (!st.hasQuestItems(PIECE_OF_TABLET))
			{
				st.set("cond", "11");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
			{
				htmltext = "32020-26.htm";
				st.takeItems(PIECE_OF_TABLET, 1);
				st.rewardItems(57, 60040);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
			}
		}
		else if (event.equalsIgnoreCase("32018-05.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(RAFFORTY_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("sculpture-03.htm"))
		{
			final int[] infos = NPC_VALUES.get(npc.getNpcId());
			final int ex = st.getInt("ex");
			final int numberToModulo = (infos[1] == 0) ? ex : ex % infos[1];
			
			if (numberToModulo <= infos[2])
			{
				if (ex == infos[3] || ex == infos[4] || ex == infos[5])
				{
					st.set("ex", String.valueOf(ex + infos[0]));
					st.giveItems(PIECE_OF_TABLET, 1);
					st.playSound(QuestState.SOUND_ITEMGET);
				}
			}
		}
		else if (event.equalsIgnoreCase("sculpture-04.htm"))
		{
			final int[] infos = NPC_VALUES.get(npc.getNpcId());
			final int ex = st.getInt("ex");
			final int numberToModulo = (infos[1] == 0) ? ex : ex % infos[1];
			
			if (numberToModulo <= infos[2])
				if (ex == infos[3] || ex == infos[4] || ex == infos[5])
					st.set("ex", String.valueOf(ex + infos[0]));
		}
		else if (event.equalsIgnoreCase("sculpture-06.htm"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			
			// Spawn a suspicious man broadcasting a message, which dissapear few seconds later broadcasting a second message.
			final Npc stranger = addSpawn(SUSPICIOUS_MAN, player.getX() + 50, player.getY() + 50, player.getZ(), 0, false, 3100, false);
			stranger.broadcastNpcSay("This looks like the right place...");
			
			startQuestTimer("despawn_1", 3000, stranger, player, false);
		}
		else if (event.equalsIgnoreCase("32022-02.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(REPORT_PIECE, 1);
			
			// Spawn a suspicious man broadcasting a message, which dissapear few seconds later broadcasting a second message.
			final Npc stranger = addSpawn(SUSPICIOUS_MAN, player.getX() + 50, player.getY() + 50, player.getZ(), 0, false, 5100, false);
			stranger.broadcastNpcSay("We meet again.");
			
			startQuestTimer("despawn_2", 5000, stranger, player, false);
		}
		else if (event.equalsIgnoreCase("despawn_1"))
		{
			npc.broadcastNpcSay("I see someone. Is this fate?");
			return null;
		}
		else if (event.equalsIgnoreCase("despawn_2"))
		{
			npc.broadcastNpcSay("Don't bother trying to find out more about me. Follow your own destiny.");
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
				htmltext = (player.getLevel() < 53) ? "32020-02.htm" : "32020-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				
				switch (npc.getNpcId())
				{
					case RAFFORTY:
						if (cond == 1)
							htmltext = "32020-04.htm";
						else if (cond == 2)
							htmltext = "32020-06.htm";
						else if (cond == 3)
							htmltext = "32020-09.htm";
						else if (cond == 4)
							htmltext = "32020-16.htm";
						else if (cond == 5)
						{
							htmltext = "32020-18.htm";
							st.set("cond", "6");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(RAFFORTY_LETTER, 1);
						}
						else if (cond == 6)
						{
							if (!st.hasQuestItems(RAFFORTY_LETTER))
							{
								htmltext = "32020-20.htm";
								st.giveItems(RAFFORTY_LETTER, 1);
								st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
								htmltext = "32020-19.htm";
						}
						else if (cond == 7)
							htmltext = "32020-19.htm";
						else if (cond == 8)
							htmltext = "32020-21.htm";
						else if (cond == 9)
							htmltext = "32020-22.htm";
						else if (cond == 10)
							htmltext = "32020-24.htm";
						else if (cond == 11)
							htmltext = "32020-29.htm";
						else if (cond == 12)
						{
							htmltext = "32020-30.htm";
							st.takeItems(PIECE_OF_TABLET, 1);
							st.rewardItems(57, 60040);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case MISA:
						if (cond == 1)
						{
							htmltext = "32018-02.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(MISA_LETTER, 1);
						}
						else if (cond == 2)
							htmltext = "32018-03.htm";
						else if (cond == 6)
							htmltext = "32018-04.htm";
						else if (cond > 6)
							htmltext = "32018-06.htm";
						else
							htmltext = "32018-01.htm";
						break;
					
					case KIERRE:
						if (cond == 8)
							htmltext = "32022-01.htm";
						else if (cond == 9)
						{
							if (!st.hasQuestItems(REPORT_PIECE))
							{
								htmltext = "32022-04.htm";
								st.giveItems(REPORT_PIECE, 1);
								st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
								htmltext = "32022-03.htm";
						}
						else if (cond == 11)
							htmltext = "32022-05.htm";
						break;
					
					case SCULPTURE_1:
					case SCULPTURE_2:
					case SCULPTURE_3:
					case SCULPTURE_4:
						if (cond == 7)
						{
							final int[] infos = NPC_VALUES.get(npc.getNpcId());
							final int ex = st.getInt("ex");
							final int numberToModulo = (infos[1] == 0) ? ex : ex % infos[1];
							
							if (numberToModulo <= infos[2])
							{
								if (ex == infos[3] || ex == infos[4] || ex == infos[5])
									htmltext = "sculpture-02.htm";
								else if (ex == infos[6])
									htmltext = "sculpture-05.htm";
								else
								{
									st.set("ex", String.valueOf(ex + infos[0]));
									htmltext = "sculpture-01.htm";
								}
							}
							else
								htmltext = "sculpture-01a.htm";
						}
						else if (cond > 7 && cond < 11)
							htmltext = "sculpture-07.htm";
						else if (cond == 11)
						{
							if (!st.hasQuestItems(PIECE_OF_TABLET))
							{
								htmltext = "sculpture-08.htm";
								st.set("cond", "12");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.giveItems(PIECE_OF_TABLET, 1);
							}
							else
								htmltext = "sculpture-09.htm";
						}
						else if (cond == 12)
							htmltext = "sculpture-09.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q226_TestOfTheHealer extends Quest
{
	private static final String qn = "Q226_TestOfTheHealer";
	
	// Items
	private static final int REPORT_OF_PERRIN = 2810;
	private static final int KRISTINA_LETTER = 2811;
	private static final int PICTURE_OF_WINDY = 2812;
	private static final int GOLDEN_STATUE = 2813;
	private static final int WINDY_PEBBLES = 2814;
	private static final int ORDER_OF_SORIUS = 2815;
	private static final int SECRET_LETTER_1 = 2816;
	private static final int SECRET_LETTER_2 = 2817;
	private static final int SECRET_LETTER_3 = 2818;
	private static final int SECRET_LETTER_4 = 2819;
	
	// Rewards
	private static final int MARK_OF_HEALER = 2820;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int BANDELLOS = 30473;
	private static final int SORIUS = 30327;
	private static final int ALLANA = 30424;
	private static final int PERRIN = 30428;
	private static final int GUPU = 30658;
	private static final int ORPHAN_GIRL = 30659;
	private static final int WINDY_SHAORING = 30660;
	private static final int MYSTERIOUS_DARKELF = 30661;
	private static final int PIPER_LONGBOW = 30662;
	private static final int SLEIN_SHINING_BLADE = 30663;
	private static final int KAIN_FLYING_KNIFE = 30664;
	private static final int KRISTINA = 30665;
	private static final int DAURIN_HAMMERCRUSH = 30674;
	
	// Monsters
	private static final int LETO_LIZARDMAN_LEADER = 27123;
	private static final int LETO_LIZARDMAN_ASSASSIN = 27124;
	private static final int LETO_LIZARDMAN_SNIPER = 27125;
	private static final int LETO_LIZARDMAN_WIZARD = 27126;
	private static final int LETO_LIZARDMAN_LORD = 27127;
	private static final int TATOMA = 27134;
	
	private Npc _tatoma;
	private Npc _letoLeader;
	
	public Q226_TestOfTheHealer()
	{
		super(226, "Test of the Healer");
		
		setItemsIds(REPORT_OF_PERRIN, KRISTINA_LETTER, PICTURE_OF_WINDY, GOLDEN_STATUE, WINDY_PEBBLES, ORDER_OF_SORIUS, SECRET_LETTER_1, SECRET_LETTER_2, SECRET_LETTER_3, SECRET_LETTER_4);
		
		addStartNpc(BANDELLOS);
		addTalkId(BANDELLOS, SORIUS, ALLANA, PERRIN, GUPU, ORPHAN_GIRL, WINDY_SHAORING, MYSTERIOUS_DARKELF, PIPER_LONGBOW, SLEIN_SHINING_BLADE, KAIN_FLYING_KNIFE, KRISTINA, DAURIN_HAMMERCRUSH);
		
		addKillId(LETO_LIZARDMAN_LEADER, LETO_LIZARDMAN_ASSASSIN, LETO_LIZARDMAN_SNIPER, LETO_LIZARDMAN_WIZARD, LETO_LIZARDMAN_LORD, TATOMA);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// BANDELLOS
		if (event.equalsIgnoreCase("30473-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(REPORT_OF_PERRIN, 1);
			
			if (!player.getMemos().getBool("secondClassChange39", false))
			{
				htmltext = "30473-04a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39.get(player.getClassId().getId()));
				player.getMemos().set("secondClassChange39", true);
			}
		}
		else if (event.equalsIgnoreCase("30473-09.htm"))
		{
			st.takeItems(GOLDEN_STATUE, 1);
			st.giveItems(MARK_OF_HEALER, 1);
			st.rewardExpAndSp(134839, 50000);
			player.broadcastPacket(new SocialAction(player, 3));
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		// PERRIN
		else if (event.equalsIgnoreCase("30428-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			
			if (_tatoma == null)
			{
				_tatoma = addSpawn(TATOMA, -93254, 147559, -2679, 0, false, 0, false);
				startQuestTimer("tatoma_despawn", 200000, null, player, false);
			}
		}
		// GUPU
		else if (event.equalsIgnoreCase("30658-02.htm"))
		{
			if (st.getQuestItemsCount(57) >= 100000)
			{
				st.set("cond", "7");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(57, 100000);
				st.giveItems(PICTURE_OF_WINDY, 1);
			}
			else
				htmltext = "30658-05.htm";
		}
		else if (event.equalsIgnoreCase("30658-03.htm"))
			st.set("gupu", "1");
		else if (event.equalsIgnoreCase("30658-07.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		// WINDY SHAORING
		else if (event.equalsIgnoreCase("30660-03.htm"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(PICTURE_OF_WINDY, 1);
			st.giveItems(WINDY_PEBBLES, 1);
		}
		// DAURIN HAMMERCRUSH
		else if (event.equalsIgnoreCase("30674-02.htm"))
		{
			st.set("cond", "11");
			st.playSound(QuestState.SOUND_BEFORE_BATTLE);
			st.takeItems(ORDER_OF_SORIUS, 1);
			
			if (_letoLeader == null)
			{
				_letoLeader = addSpawn(LETO_LIZARDMAN_LEADER, -97441, 106585, -3405, 0, false, 0, false);
				startQuestTimer("leto_leader_despawn", 200000, null, player, false);
			}
		}
		// KRISTINA
		else if (event.equalsIgnoreCase("30665-02.htm"))
		{
			st.set("cond", "22");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(SECRET_LETTER_1, 1);
			st.takeItems(SECRET_LETTER_2, 1);
			st.takeItems(SECRET_LETTER_3, 1);
			st.takeItems(SECRET_LETTER_4, 1);
			st.giveItems(KRISTINA_LETTER, 1);
		}
		// DESPAWNS
		else if (event.equalsIgnoreCase("tatoma_despawn"))
		{
			_tatoma.deleteMe();
			_tatoma = null;
			return null;
		}
		else if (event.equalsIgnoreCase("leto_leader_despawn"))
		{
			_letoLeader.deleteMe();
			_letoLeader = null;
			return null;
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
				if (player.getClassId() != ClassId.KNIGHT && player.getClassId() != ClassId.ELVEN_KNIGHT && player.getClassId() != ClassId.CLERIC && player.getClassId() != ClassId.ELVEN_ORACLE)
					htmltext = "30473-01.htm";
				else if (player.getLevel() < 39)
					htmltext = "30473-02.htm";
				else
					htmltext = "30473-03.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case BANDELLOS:
						if (cond < 23)
							htmltext = "30473-05.htm";
						else
						{
							if (!st.hasQuestItems(GOLDEN_STATUE))
							{
								htmltext = "30473-06.htm";
								st.giveItems(MARK_OF_HEALER, 1);
								st.rewardExpAndSp(118304, 26250);
								player.broadcastPacket(new SocialAction(player, 3));
								st.playSound(QuestState.SOUND_FINISH);
								st.exitQuest(false);
							}
							else
								htmltext = "30473-07.htm";
						}
						break;
					
					case PERRIN:
						if (cond < 3)
							htmltext = "30428-01.htm";
						else if (cond == 3)
						{
							htmltext = "30428-03.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(REPORT_OF_PERRIN, 1);
						}
						else
							htmltext = "30428-04.htm";
						break;
					
					case ORPHAN_GIRL:
						htmltext = "30659-0" + Rnd.get(1, 5) + ".htm";
						break;
					
					case ALLANA:
						if (cond == 4)
						{
							htmltext = "30424-01.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond > 4)
							htmltext = "30424-02.htm";
						break;
					
					case GUPU:
						if (st.getInt("gupu") == 1 && cond != 9)
						{
							htmltext = "30658-07.htm";
							st.set("cond", "9");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 5)
						{
							htmltext = "30658-01.htm";
							st.set("cond", "6");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 6)
							htmltext = "30658-01.htm";
						else if (cond == 7)
							htmltext = "30658-04.htm";
						else if (cond == 8)
						{
							htmltext = "30658-06.htm";
							st.playSound(QuestState.SOUND_ITEMGET);
							st.takeItems(WINDY_PEBBLES, 1);
							st.giveItems(GOLDEN_STATUE, 1);
						}
						else if (cond > 8)
							htmltext = "30658-07.htm";
						break;
					
					case WINDY_SHAORING:
						if (cond == 7)
							htmltext = "30660-01.htm";
						else if (st.hasQuestItems(WINDY_PEBBLES))
							htmltext = "30660-04.htm";
						break;
					
					case SORIUS:
						if (cond == 9)
						{
							htmltext = "30327-01.htm";
							st.set("cond", "10");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(ORDER_OF_SORIUS, 1);
						}
						else if (cond > 9 && cond < 22)
							htmltext = "30327-02.htm";
						else if (cond == 22)
						{
							htmltext = "30327-03.htm";
							st.set("cond", "23");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(KRISTINA_LETTER, 1);
						}
						else if (cond == 23)
							htmltext = "30327-04.htm";
						break;
					
					case DAURIN_HAMMERCRUSH:
						if (cond == 10)
							htmltext = "30674-01.htm";
						else if (cond == 11)
						{
							htmltext = "30674-02a.htm";
							if (_letoLeader == null)
							{
								_letoLeader = addSpawn(LETO_LIZARDMAN_LEADER, -97441, 106585, -3405, 0, false, 0, false);
								startQuestTimer("leto_leader_despawn", 200000, null, player, false);
							}
						}
						else if (cond == 12)
						{
							htmltext = "30674-03.htm";
							st.set("cond", "13");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond > 12)
							htmltext = "30674-04.htm";
						break;
					
					case PIPER_LONGBOW:
					case SLEIN_SHINING_BLADE:
					case KAIN_FLYING_KNIFE:
						if (cond == 13 || cond == 14)
							htmltext = npc.getNpcId() + "-01.htm";
						else if (cond > 14 && cond < 19)
							htmltext = npc.getNpcId() + "-02.htm";
						else if (cond > 18 && cond < 22)
						{
							htmltext = npc.getNpcId() + "-03.htm";
							st.set("cond", "21");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						break;
					
					case MYSTERIOUS_DARKELF:
						if (cond == 13)
						{
							htmltext = "30661-01.htm";
							st.set("cond", "14");
							st.playSound(QuestState.SOUND_BEFORE_BATTLE);
							addSpawn(LETO_LIZARDMAN_ASSASSIN, player, true, 0, false);
							addSpawn(LETO_LIZARDMAN_ASSASSIN, player, true, 0, false);
							addSpawn(LETO_LIZARDMAN_ASSASSIN, player, true, 0, false);
						}
						else if (cond == 14)
							htmltext = "30661-01.htm";
						else if (cond == 15)
						{
							htmltext = "30661-02.htm";
							st.set("cond", "16");
							st.playSound(QuestState.SOUND_BEFORE_BATTLE);
							addSpawn(LETO_LIZARDMAN_SNIPER, player, true, 0, false);
							addSpawn(LETO_LIZARDMAN_SNIPER, player, true, 0, false);
							addSpawn(LETO_LIZARDMAN_SNIPER, player, true, 0, false);
						}
						else if (cond == 16)
							htmltext = "30661-02.htm";
						else if (cond == 17)
						{
							htmltext = "30661-03.htm";
							st.set("cond", "18");
							st.playSound(QuestState.SOUND_BEFORE_BATTLE);
							addSpawn(LETO_LIZARDMAN_WIZARD, player, true, 0, false);
							addSpawn(LETO_LIZARDMAN_WIZARD, player, true, 0, false);
							addSpawn(LETO_LIZARDMAN_LORD, player, true, 0, false);
						}
						else if (cond == 18)
							htmltext = "30661-03.htm";
						else if (cond == 19)
						{
							htmltext = "30661-04.htm";
							st.set("cond", "20");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 20 || cond == 21)
							htmltext = "30661-04.htm";
						break;
					
					case KRISTINA:
						if (cond > 18 && cond < 22)
							htmltext = "30665-01.htm";
						else if (cond > 21)
							htmltext = "30665-04.htm";
						else
							htmltext = "30665-03.htm";
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
		
		final int cond = st.getInt("cond");
		switch (npc.getNpcId())
		{
			case TATOMA:
				if (cond == 1 || cond == 2)
				{
					st.set("cond", "3");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				_tatoma = null;
				cancelQuestTimer("tatoma_despawn", null, player);
				break;
			
			case LETO_LIZARDMAN_LEADER:
				if (cond == 10 || cond == 11)
				{
					st.set("cond", "12");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(SECRET_LETTER_1, 1);
				}
				_letoLeader = null;
				cancelQuestTimer("leto_leader_despawn", null, player);
				break;
			
			case LETO_LIZARDMAN_ASSASSIN:
				if (cond == 13 || cond == 14)
				{
					st.set("cond", "15");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(SECRET_LETTER_2, 1);
				}
				break;
			
			case LETO_LIZARDMAN_SNIPER:
				if (cond == 15 || cond == 16)
				{
					st.set("cond", "17");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(SECRET_LETTER_3, 1);
				}
				break;
			
			case LETO_LIZARDMAN_LORD:
				if (cond == 17 || cond == 18)
				{
					st.set("cond", "19");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(SECRET_LETTER_4, 1);
				}
				break;
		}
		
		return null;
	}
}
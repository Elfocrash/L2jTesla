package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q232_TestOfTheLord extends Quest
{
	private static final String qn = "Q232_TestOfTheLord";
	
	// NPCs
	private static final int SOMAK = 30510;
	private static final int MANAKIA = 30515;
	private static final int JAKAL = 30558;
	private static final int SUMARI = 30564;
	private static final int KAKAI = 30565;
	private static final int VARKEES = 30566;
	private static final int TANTUS = 30567;
	private static final int HATOS = 30568;
	private static final int TAKUNA = 30641;
	private static final int CHIANTA = 30642;
	private static final int FIRST_ORC = 30643;
	private static final int ANCESTOR_MARTANKUS = 30649;
	
	// Items
	private static final int ORDEAL_NECKLACE = 3391;
	private static final int VARKEES_CHARM = 3392;
	private static final int TANTUS_CHARM = 3393;
	private static final int HATOS_CHARM = 3394;
	private static final int TAKUNA_CHARM = 3395;
	private static final int CHIANTA_CHARM = 3396;
	private static final int MANAKIAS_ORDERS = 3397;
	private static final int BREKA_ORC_FANG = 3398;
	private static final int MANAKIAS_AMULET = 3399;
	private static final int HUGE_ORC_FANG = 3400;
	private static final int SUMARIS_LETTER = 3401;
	private static final int URUTU_BLADE = 3402;
	private static final int TIMAK_ORC_SKULL = 3403;
	private static final int SWORD_INTO_SKULL = 3404;
	private static final int NERUGA_AXE_BLADE = 3405;
	private static final int AXE_OF_CEREMONY = 3406;
	private static final int MARSH_SPIDER_FEELER = 3407;
	private static final int MARSH_SPIDER_FEET = 3408;
	private static final int HANDIWORK_SPIDER_BROOCH = 3409;
	private static final int MONSTEREYE_CORNEA = 3410;
	private static final int MONSTEREYE_WOODCARVING = 3411;
	private static final int BEAR_FANG_NECKLACE = 3412;
	private static final int MARTANKUS_CHARM = 3413;
	private static final int RAGNA_ORC_HEAD = 3414;
	private static final int RAGNA_CHIEF_NOTICE = 3415;
	private static final int BONE_ARROW = 1341;
	private static final int IMMORTAL_FLAME = 3416;
	
	// Rewards
	private static final int MARK_LORD = 3390;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	private static Npc _firstOrc; // Used to avoid to spawn multiple instances.
	
	public Q232_TestOfTheLord()
	{
		super(232, "Test of the Lord");
		
		setItemsIds(VARKEES_CHARM, TANTUS_CHARM, HATOS_CHARM, TAKUNA_CHARM, CHIANTA_CHARM, MANAKIAS_ORDERS, BREKA_ORC_FANG, MANAKIAS_AMULET, HUGE_ORC_FANG, SUMARIS_LETTER, URUTU_BLADE, TIMAK_ORC_SKULL, SWORD_INTO_SKULL, NERUGA_AXE_BLADE, AXE_OF_CEREMONY, MARSH_SPIDER_FEELER, MARSH_SPIDER_FEET, HANDIWORK_SPIDER_BROOCH, MONSTEREYE_CORNEA, MONSTEREYE_WOODCARVING, BEAR_FANG_NECKLACE, MARTANKUS_CHARM, RAGNA_ORC_HEAD, RAGNA_CHIEF_NOTICE, IMMORTAL_FLAME);
		
		addStartNpc(KAKAI);
		addTalkId(KAKAI, CHIANTA, HATOS, SOMAK, SUMARI, TAKUNA, TANTUS, JAKAL, VARKEES, MANAKIA, ANCESTOR_MARTANKUS, FIRST_ORC);
		
		addKillId(20233, 20269, 20270, 20564, 20583, 20584, 20585, 20586, 20587, 20588, 20778, 20779);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30565-05.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(ORDEAL_NECKLACE, 1);
			
			if (!player.getMemos().getBool("secondClassChange39", false))
			{
				htmltext = "30565-05b.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39.get(player.getClassId().getId()));
				player.getMemos().set("secondClassChange39", true);
			}
		}
		else if (event.equalsIgnoreCase("30565-08.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(SWORD_INTO_SKULL, 1);
			st.takeItems(AXE_OF_CEREMONY, 1);
			st.takeItems(MONSTEREYE_WOODCARVING, 1);
			st.takeItems(HANDIWORK_SPIDER_BROOCH, 1);
			st.takeItems(ORDEAL_NECKLACE, 1);
			st.takeItems(HUGE_ORC_FANG, 1);
			st.giveItems(BEAR_FANG_NECKLACE, 1);
		}
		else if (event.equalsIgnoreCase("30566-02.htm"))
		{
			st.giveItems(VARKEES_CHARM, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (event.equalsIgnoreCase("30567-02.htm"))
		{
			st.giveItems(TANTUS_CHARM, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (event.equalsIgnoreCase("30558-02.htm"))
		{
			st.takeItems(57, 1000);
			st.giveItems(NERUGA_AXE_BLADE, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (event.equalsIgnoreCase("30568-02.htm"))
		{
			st.giveItems(HATOS_CHARM, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (event.equalsIgnoreCase("30641-02.htm"))
		{
			st.giveItems(TAKUNA_CHARM, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (event.equalsIgnoreCase("30642-02.htm"))
		{
			st.giveItems(CHIANTA_CHARM, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (event.equalsIgnoreCase("30643-02.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
			startQuestTimer("f_orc_despawn", 10000, null, player, false);
		}
		else if (event.equalsIgnoreCase("30649-04.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BEAR_FANG_NECKLACE, 1);
			st.giveItems(MARTANKUS_CHARM, 1);
		}
		else if (event.equalsIgnoreCase("30649-07.htm"))
		{
			if (_firstOrc == null)
				_firstOrc = addSpawn(FIRST_ORC, 21036, -107690, -3038, 200000, false, 0, true);
		}
		else if (event.equalsIgnoreCase("f_orc_despawn"))
		{
			if (_firstOrc != null)
			{
				_firstOrc.deleteMe();
				_firstOrc = null;
			}
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
				if (player.getRace() != ClassRace.ORC)
					htmltext = "30565-01.htm";
				else if (player.getClassId() != ClassId.ORC_SHAMAN)
					htmltext = "30565-02.htm";
				else if (player.getLevel() < 39)
					htmltext = "30565-03.htm";
				else
					htmltext = "30565-04.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case VARKEES:
						if (st.hasQuestItems(HUGE_ORC_FANG))
							htmltext = "30566-05.htm";
						else if (st.hasQuestItems(VARKEES_CHARM))
						{
							if (st.hasQuestItems(MANAKIAS_AMULET))
							{
								htmltext = "30566-04.htm";
								st.takeItems(VARKEES_CHARM, -1);
								st.takeItems(MANAKIAS_AMULET, -1);
								st.giveItems(HUGE_ORC_FANG, 1);
								
								if (st.hasQuestItems(SWORD_INTO_SKULL, AXE_OF_CEREMONY, MONSTEREYE_WOODCARVING, HANDIWORK_SPIDER_BROOCH, ORDEAL_NECKLACE))
								{
									st.set("cond", "2");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
								htmltext = "30566-03.htm";
						}
						else
							htmltext = "30566-01.htm";
						break;
					
					case MANAKIA:
						if (st.hasQuestItems(HUGE_ORC_FANG))
							htmltext = "30515-05.htm";
						else if (st.hasQuestItems(MANAKIAS_AMULET))
							htmltext = "30515-04.htm";
						else if (st.hasQuestItems(MANAKIAS_ORDERS))
						{
							if (st.getQuestItemsCount(BREKA_ORC_FANG) >= 20)
							{
								htmltext = "30515-03.htm";
								st.takeItems(MANAKIAS_ORDERS, -1);
								st.takeItems(BREKA_ORC_FANG, -1);
								st.giveItems(MANAKIAS_AMULET, 1);
								st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
								htmltext = "30515-02.htm";
						}
						else
						{
							htmltext = "30515-01.htm";
							st.giveItems(MANAKIAS_ORDERS, 1);
							st.playSound(QuestState.SOUND_ITEMGET);
						}
						break;
					
					case TANTUS:
						if (st.hasQuestItems(AXE_OF_CEREMONY))
							htmltext = "30567-05.htm";
						else if (st.hasQuestItems(TANTUS_CHARM))
						{
							if (st.getQuestItemsCount(BONE_ARROW) >= 1000)
							{
								htmltext = "30567-04.htm";
								st.takeItems(BONE_ARROW, 1000);
								st.takeItems(NERUGA_AXE_BLADE, 1);
								st.takeItems(TANTUS_CHARM, 1);
								st.giveItems(AXE_OF_CEREMONY, 1);
								
								if (st.hasQuestItems(SWORD_INTO_SKULL, MONSTEREYE_WOODCARVING, HANDIWORK_SPIDER_BROOCH, ORDEAL_NECKLACE, HUGE_ORC_FANG))
								{
									st.set("cond", "2");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
								htmltext = "30567-03.htm";
						}
						else
							htmltext = "30567-01.htm";
						break;
					
					case JAKAL:
						if (st.hasQuestItems(AXE_OF_CEREMONY))
							htmltext = "30558-05.htm";
						else if (st.hasQuestItems(NERUGA_AXE_BLADE))
							htmltext = "30558-04.htm";
						else if (st.hasQuestItems(TANTUS_CHARM))
						{
							if (st.getQuestItemsCount(57) >= 1000)
								htmltext = "30558-01.htm";
							else
								htmltext = "30558-03.htm";
						}
						break;
					
					case HATOS:
						if (st.hasQuestItems(SWORD_INTO_SKULL))
							htmltext = "30568-05.htm";
						else if (st.hasQuestItems(HATOS_CHARM))
						{
							if (st.hasQuestItems(URUTU_BLADE) && st.getQuestItemsCount(TIMAK_ORC_SKULL) >= 10)
							{
								htmltext = "30568-04.htm";
								st.takeItems(HATOS_CHARM, 1);
								st.takeItems(URUTU_BLADE, 1);
								st.takeItems(TIMAK_ORC_SKULL, -1);
								st.giveItems(SWORD_INTO_SKULL, 1);
								
								if (st.hasQuestItems(AXE_OF_CEREMONY, MONSTEREYE_WOODCARVING, HANDIWORK_SPIDER_BROOCH, ORDEAL_NECKLACE, HUGE_ORC_FANG))
								{
									st.set("cond", "2");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
								htmltext = "30568-03.htm";
						}
						else
							htmltext = "30568-01.htm";
						break;
					
					case SUMARI:
						if (st.hasQuestItems(URUTU_BLADE))
							htmltext = "30564-03.htm";
						else if (st.hasQuestItems(SUMARIS_LETTER))
							htmltext = "30564-02.htm";
						else if (st.hasQuestItems(HATOS_CHARM))
						{
							htmltext = "30564-01.htm";
							st.giveItems(SUMARIS_LETTER, 1);
							st.playSound(QuestState.SOUND_ITEMGET);
						}
						break;
					
					case SOMAK:
						if (st.hasQuestItems(SWORD_INTO_SKULL))
							htmltext = "30510-03.htm";
						else if (st.hasQuestItems(URUTU_BLADE))
							htmltext = "30510-02.htm";
						else if (st.hasQuestItems(SUMARIS_LETTER))
						{
							htmltext = "30510-01.htm";
							st.takeItems(SUMARIS_LETTER, 1);
							st.giveItems(URUTU_BLADE, 1);
							st.playSound(QuestState.SOUND_ITEMGET);
						}
						break;
					
					case TAKUNA:
						if (st.hasQuestItems(HANDIWORK_SPIDER_BROOCH))
							htmltext = "30641-05.htm";
						else if (st.hasQuestItems(TAKUNA_CHARM))
						{
							if (st.getQuestItemsCount(MARSH_SPIDER_FEELER) >= 10 && st.getQuestItemsCount(MARSH_SPIDER_FEET) >= 10)
							{
								htmltext = "30641-04.htm";
								st.takeItems(MARSH_SPIDER_FEELER, -1);
								st.takeItems(MARSH_SPIDER_FEET, -1);
								st.takeItems(TAKUNA_CHARM, 1);
								st.giveItems(HANDIWORK_SPIDER_BROOCH, 1);
								
								if (st.hasQuestItems(SWORD_INTO_SKULL, AXE_OF_CEREMONY, MONSTEREYE_WOODCARVING, ORDEAL_NECKLACE, HUGE_ORC_FANG))
								{
									st.set("cond", "2");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
								htmltext = "30641-03.htm";
						}
						else
							htmltext = "30641-01.htm";
						break;
					
					case CHIANTA:
						if (st.hasQuestItems(MONSTEREYE_WOODCARVING))
							htmltext = "30642-05.htm";
						else if (st.hasQuestItems(CHIANTA_CHARM))
						{
							if (st.getQuestItemsCount(MONSTEREYE_CORNEA) >= 20)
							{
								htmltext = "30642-04.htm";
								st.takeItems(MONSTEREYE_CORNEA, -1);
								st.takeItems(CHIANTA_CHARM, 1);
								st.giveItems(MONSTEREYE_WOODCARVING, 1);
								
								if (st.hasQuestItems(SWORD_INTO_SKULL, AXE_OF_CEREMONY, HANDIWORK_SPIDER_BROOCH, ORDEAL_NECKLACE, HUGE_ORC_FANG))
								{
									st.set("cond", "2");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
								htmltext = "30642-03.htm";
						}
						else
							htmltext = "30642-01.htm";
						break;
					
					case KAKAI:
						if (cond == 1)
							htmltext = "30565-06.htm";
						else if (cond == 2)
							htmltext = "30565-07.htm";
						else if (cond == 3)
							htmltext = "30565-09.htm";
						else if (cond > 3 && cond < 7)
							htmltext = "30565-10.htm";
						else if (cond == 7)
						{
							htmltext = "30565-11.htm";
							
							st.takeItems(IMMORTAL_FLAME, 1);
							st.giveItems(MARK_LORD, 1);
							st.rewardExpAndSp(92955, 16250);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case ANCESTOR_MARTANKUS:
						if (cond == 3)
							htmltext = "30649-01.htm";
						else if (cond == 4)
							htmltext = "30649-05.htm";
						else if (cond == 5)
						{
							htmltext = "30649-06.htm";
							st.set("cond", "6");
							st.playSound(QuestState.SOUND_MIDDLE);
							
							st.takeItems(MARTANKUS_CHARM, 1);
							st.takeItems(RAGNA_ORC_HEAD, 1);
							st.takeItems(RAGNA_CHIEF_NOTICE, 1);
							st.giveItems(IMMORTAL_FLAME, 1);
						}
						else if (cond == 6)
							htmltext = "30649-07.htm";
						else if (cond == 7)
							htmltext = "30649-08.htm";
						break;
					
					case FIRST_ORC:
						if (cond == 6)
							htmltext = "30643-01.htm";
						else if (cond == 7)
							htmltext = "30643-03.htm";
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
			case 20564:
				if (st.hasQuestItems(CHIANTA_CHARM))
					st.dropItemsAlways(MONSTEREYE_CORNEA, 1, 20);
				break;
			
			case 20583:
			case 20584:
			case 20585:
				if (st.hasQuestItems(HATOS_CHARM))
					st.dropItems(TIMAK_ORC_SKULL, 1, 10, 710000);
				break;
			
			case 20586:
				if (st.hasQuestItems(HATOS_CHARM))
					st.dropItems(TIMAK_ORC_SKULL, 1, 10, 810000);
				break;
			
			case 20587:
			case 20588:
				if (st.hasQuestItems(HATOS_CHARM))
					st.dropItemsAlways(TIMAK_ORC_SKULL, 1, 10);
				break;
			
			case 20233:
				if (st.hasQuestItems(TAKUNA_CHARM))
					st.dropItemsAlways((st.getQuestItemsCount(MARSH_SPIDER_FEELER) >= 10) ? MARSH_SPIDER_FEET : MARSH_SPIDER_FEELER, 1, 10);
				break;
			
			case 20269:
				if (st.hasQuestItems(MANAKIAS_ORDERS))
					st.dropItems(BREKA_ORC_FANG, 1, 20, 410000);
				break;
			
			case 20270:
				if (st.hasQuestItems(MANAKIAS_ORDERS))
					st.dropItems(BREKA_ORC_FANG, 1, 20, 510000);
				break;
			
			case 20778:
			case 20779:
				if (st.hasQuestItems(MARTANKUS_CHARM))
				{
					if (!st.hasQuestItems(RAGNA_CHIEF_NOTICE))
					{
						st.playSound(QuestState.SOUND_MIDDLE);
						st.giveItems(RAGNA_CHIEF_NOTICE, 1);
					}
					else if (!st.hasQuestItems(RAGNA_ORC_HEAD))
					{
						st.set("cond", "5");
						st.playSound(QuestState.SOUND_MIDDLE);
						st.giveItems(RAGNA_ORC_HEAD, 1);
					}
				}
				break;
		}
		
		return null;
	}
}
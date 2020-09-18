package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q221_TestimonyOfProsperity extends Quest
{
	private static final String qn = "Q221_TestimonyOfProsperity";
	
	// Items
	private static final int ADENA = 57;
	private static final int ANIMAL_SKIN = 1867;
	private static final int RECIPE_TITAN_KEY = 3023;
	private static final int KEY_OF_TITAN = 3030;
	
	private static final int RING_OF_TESTIMONY_1 = 3239;
	private static final int RING_OF_TESTIMONY_2 = 3240;
	private static final int OLD_ACCOUNT_BOOK = 3241;
	private static final int BLESSED_SEED = 3242;
	private static final int EMILY_RECIPE = 3243;
	private static final int LILITH_ELVEN_WAFER = 3244;
	private static final int MAPHR_TABLET_FRAGMENT = 3245;
	private static final int COLLECTION_LICENSE = 3246;
	private static final int LOCKIRIN_NOTICE_1 = 3247;
	private static final int LOCKIRIN_NOTICE_2 = 3248;
	private static final int LOCKIRIN_NOTICE_3 = 3249;
	private static final int LOCKIRIN_NOTICE_4 = 3250;
	private static final int LOCKIRIN_NOTICE_5 = 3251;
	private static final int CONTRIBUTION_OF_SHARI = 3252;
	private static final int CONTRIBUTION_OF_MION = 3253;
	private static final int CONTRIBUTION_OF_MARYSE = 3254;
	private static final int MARYSE_REQUEST = 3255;
	private static final int CONTRIBUTION_OF_TOMA = 3256;
	private static final int RECEIPT_OF_BOLTER = 3257;
	private static final int RECEIPT_OF_CONTRIBUTION_1 = 3258;
	private static final int RECEIPT_OF_CONTRIBUTION_2 = 3259;
	private static final int RECEIPT_OF_CONTRIBUTION_3 = 3260;
	private static final int RECEIPT_OF_CONTRIBUTION_4 = 3261;
	private static final int RECEIPT_OF_CONTRIBUTION_5 = 3262;
	private static final int PROCURATION_OF_TOROCCO = 3263;
	private static final int BRIGHT_LIST = 3264;
	private static final int MANDRAGORA_PETAL = 3265;
	private static final int CRIMSON_MOSS = 3266;
	private static final int MANDRAGORA_BOUQUET = 3267;
	private static final int PARMAN_INSTRUCTIONS = 3268;
	private static final int PARMAN_LETTER = 3269;
	private static final int CLAY_DOUGH = 3270;
	private static final int PATTERN_OF_KEYHOLE = 3271;
	private static final int NIKOLAS_LIST = 3272;
	private static final int STAKATO_SHELL = 3273;
	private static final int TOAD_LORD_SAC = 3274;
	private static final int SPIDER_THORN = 3275;
	private static final int CRYSTAL_BROOCH = 3428;
	
	// Rewards
	private static final int MARK_OF_PROSPERITY = 3238;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int WILFORD = 30005;
	private static final int PARMAN = 30104;
	private static final int LILITH = 30368;
	private static final int BRIGHT = 30466;
	private static final int SHARI = 30517;
	private static final int MION = 30519;
	private static final int LOCKIRIN = 30531;
	private static final int SPIRON = 30532;
	private static final int BALANKI = 30533;
	private static final int KEEF = 30534;
	private static final int FILAUR = 30535;
	private static final int ARIN = 30536;
	private static final int MARYSE_REDBONNET = 30553;
	private static final int BOLTER = 30554;
	private static final int TOROCCO = 30555;
	private static final int TOMA = 30556;
	private static final int PIOTUR = 30597;
	private static final int EMILY = 30620;
	private static final int NIKOLA = 30621;
	private static final int BOX_OF_TITAN = 30622;
	
	// Monsters
	private static final int MANDRAGORA_SPROUT_1 = 20223;
	private static final int MANDRAGORA_SPROUT_2 = 20154;
	private static final int MANDRAGORA_SAPLING = 20155;
	private static final int MANDRAGORA_BLOSSOM = 20156;
	private static final int MARSH_STAKATO = 20157;
	private static final int GIANT_CRIMSON_ANT = 20228;
	private static final int MARSH_STAKATO_WORKER = 20230;
	private static final int TOAD_LORD = 20231;
	private static final int MARSH_STAKATO_SOLDIER = 20232;
	private static final int MARSH_SPIDER = 20233;
	private static final int MARSH_STAKATO_DRONE = 20234;
	
	public Q221_TestimonyOfProsperity()
	{
		super(221, "Testimony Of Prosperity");
		
		setItemsIds(RING_OF_TESTIMONY_1, RING_OF_TESTIMONY_2, OLD_ACCOUNT_BOOK, BLESSED_SEED, EMILY_RECIPE, LILITH_ELVEN_WAFER, MAPHR_TABLET_FRAGMENT, COLLECTION_LICENSE, LOCKIRIN_NOTICE_1, LOCKIRIN_NOTICE_2, LOCKIRIN_NOTICE_3, LOCKIRIN_NOTICE_4, LOCKIRIN_NOTICE_5, CONTRIBUTION_OF_SHARI, CONTRIBUTION_OF_MION, CONTRIBUTION_OF_MARYSE, MARYSE_REQUEST, CONTRIBUTION_OF_TOMA, RECEIPT_OF_BOLTER, RECEIPT_OF_CONTRIBUTION_1, RECEIPT_OF_CONTRIBUTION_2, RECEIPT_OF_CONTRIBUTION_3, RECEIPT_OF_CONTRIBUTION_4, RECEIPT_OF_CONTRIBUTION_5, PROCURATION_OF_TOROCCO, BRIGHT_LIST, MANDRAGORA_PETAL, CRIMSON_MOSS, MANDRAGORA_BOUQUET, PARMAN_INSTRUCTIONS, PARMAN_LETTER, CLAY_DOUGH, PATTERN_OF_KEYHOLE, NIKOLAS_LIST, STAKATO_SHELL, TOAD_LORD_SAC, SPIDER_THORN, CRYSTAL_BROOCH);
		
		addStartNpc(PARMAN);
		addTalkId(WILFORD, PARMAN, LILITH, BRIGHT, SHARI, MION, LOCKIRIN, SPIRON, BALANKI, KEEF, FILAUR, ARIN, MARYSE_REDBONNET, BOLTER, TOROCCO, TOMA, PIOTUR, EMILY, NIKOLA, BOX_OF_TITAN);
		
		addKillId(MANDRAGORA_SPROUT_1, MANDRAGORA_SAPLING, MANDRAGORA_BLOSSOM, MARSH_STAKATO, MANDRAGORA_SPROUT_2, GIANT_CRIMSON_ANT, MARSH_STAKATO_WORKER, TOAD_LORD, MARSH_STAKATO_SOLDIER, MARSH_SPIDER, MARSH_STAKATO_DRONE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// PARMAN
		if (event.equalsIgnoreCase("30104-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(RING_OF_TESTIMONY_1, 1);
			
			if (!player.getMemos().getBool("secondClassChange37", false))
			{
				htmltext = "30104-04e.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_37.get(player.getRace().ordinal()));
				player.getMemos().set("secondClassChange37", true);
			}
		}
		else if (event.equalsIgnoreCase("30104-07.htm"))
		{
			st.takeItems(BLESSED_SEED, 1);
			st.takeItems(EMILY_RECIPE, 1);
			st.takeItems(LILITH_ELVEN_WAFER, 1);
			st.takeItems(OLD_ACCOUNT_BOOK, 1);
			st.takeItems(RING_OF_TESTIMONY_1, 1);
			st.playSound(QuestState.SOUND_MIDDLE);
			
			if (player.getLevel() < 38)
			{
				st.set("cond", "3");
				st.giveItems(PARMAN_INSTRUCTIONS, 1);
			}
			else
			{
				htmltext = "30104-08.htm";
				st.set("cond", "4");
				st.giveItems(PARMAN_LETTER, 1);
				st.giveItems(RING_OF_TESTIMONY_2, 1);
			}
		}
		// LOCKIRIN
		else if (event.equalsIgnoreCase("30531-02.htm") && st.hasQuestItems(COLLECTION_LICENSE))
			htmltext = "30531-04.htm";
		else if (event.equalsIgnoreCase("30531-03.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(COLLECTION_LICENSE, 1);
			st.giveItems(LOCKIRIN_NOTICE_1, 1);
			st.giveItems(LOCKIRIN_NOTICE_2, 1);
			st.giveItems(LOCKIRIN_NOTICE_3, 1);
			st.giveItems(LOCKIRIN_NOTICE_4, 1);
			st.giveItems(LOCKIRIN_NOTICE_5, 1);
		}
		// KEEF
		else if (event.equalsIgnoreCase("30534-03a.htm") && st.getQuestItemsCount(ADENA) >= 5000)
		{
			htmltext = "30534-03b.htm";
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(ADENA, 5000);
			st.takeItems(PROCURATION_OF_TOROCCO, 1);
			st.giveItems(RECEIPT_OF_CONTRIBUTION_3, 1);
		}
		// WILFORD
		else if (event.equalsIgnoreCase("30005-04.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(CRYSTAL_BROOCH, 1);
		}
		// BRIGHT
		else if (event.equalsIgnoreCase("30466-03.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(BRIGHT_LIST, 1);
		}
		// TOROCCO
		else if (event.equalsIgnoreCase("30555-02.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(PROCURATION_OF_TOROCCO, 1);
		}
		// LILITH
		else if (event.equalsIgnoreCase("30368-03.htm"))
		{
			st.takeItems(CRYSTAL_BROOCH, 1);
			st.giveItems(LILITH_ELVEN_WAFER, 1);
			
			if (st.hasQuestItems(BLESSED_SEED, OLD_ACCOUNT_BOOK, EMILY_RECIPE))
			{
				st.set("cond", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				st.playSound(QuestState.SOUND_ITEMGET);
		}
		// PIOTUR
		else if (event.equalsIgnoreCase("30597-02.htm"))
		{
			st.giveItems(BLESSED_SEED, 1);
			
			if (st.hasQuestItems(OLD_ACCOUNT_BOOK, EMILY_RECIPE, LILITH_ELVEN_WAFER))
			{
				st.set("cond", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				st.playSound(QuestState.SOUND_ITEMGET);
		}
		// EMILY
		else if (event.equalsIgnoreCase("30620-03.htm"))
		{
			st.takeItems(MANDRAGORA_BOUQUET, 1);
			st.giveItems(EMILY_RECIPE, 1);
			
			if (st.hasQuestItems(BLESSED_SEED, OLD_ACCOUNT_BOOK, LILITH_ELVEN_WAFER))
			{
				st.set("cond", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				st.playSound(QuestState.SOUND_ITEMGET);
		}
		// NIKOLA
		else if (event.equalsIgnoreCase("30621-04.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(CLAY_DOUGH, 1);
		}
		// BOX OF TITAN
		else if (event.equalsIgnoreCase("30622-02.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CLAY_DOUGH, 1);
			st.giveItems(PATTERN_OF_KEYHOLE, 1);
		}
		else if (event.equalsIgnoreCase("30622-04.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(KEY_OF_TITAN, 1);
			st.takeItems(NIKOLAS_LIST, 1);
			st.takeItems(RECIPE_TITAN_KEY, 1);
			st.takeItems(STAKATO_SHELL, 20);
			st.takeItems(SPIDER_THORN, 10);
			st.takeItems(TOAD_LORD_SAC, 10);
			st.giveItems(MAPHR_TABLET_FRAGMENT, 1);
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
					htmltext = "30104-01.htm";
				else if (player.getLevel() < 37)
					htmltext = "30104-02.htm";
				else if (player.getClassId().level() != 1)
					htmltext = "30104-01a.htm";
				else
					htmltext = "30104-03.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case PARMAN:
						if (cond == 1)
							htmltext = "30104-05.htm";
						else if (cond == 2)
							htmltext = "30104-06.htm";
						else if (cond == 3)
						{
							if (player.getLevel() < 38)
								htmltext = "30104-09.htm";
							else
							{
								htmltext = "30104-10.htm";
								st.set("cond", "4");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(PARMAN_INSTRUCTIONS, 1);
								st.giveItems(PARMAN_LETTER, 1);
								st.giveItems(RING_OF_TESTIMONY_2, 1);
							}
						}
						else if (cond > 3 && cond < 7)
							htmltext = "30104-11.htm";
						else if (cond == 7 || cond == 8)
							htmltext = "30104-12.htm";
						else if (cond == 9)
						{
							htmltext = "30104-13.htm";
							st.takeItems(MAPHR_TABLET_FRAGMENT, 1);
							st.takeItems(RING_OF_TESTIMONY_2, 1);
							st.giveItems(MARK_OF_PROSPERITY, 1);
							st.rewardExpAndSp(12969, 1000);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case LOCKIRIN:
						if (cond == 1 || cond == 2)
						{
							if (st.hasQuestItems(COLLECTION_LICENSE))
							{
								if (st.hasQuestItems(RECEIPT_OF_CONTRIBUTION_1, RECEIPT_OF_CONTRIBUTION_2, RECEIPT_OF_CONTRIBUTION_3, RECEIPT_OF_CONTRIBUTION_4, RECEIPT_OF_CONTRIBUTION_5))
								{
									htmltext = "30531-05.htm";
									st.takeItems(COLLECTION_LICENSE, 1);
									st.takeItems(RECEIPT_OF_CONTRIBUTION_1, 1);
									st.takeItems(RECEIPT_OF_CONTRIBUTION_2, 1);
									st.takeItems(RECEIPT_OF_CONTRIBUTION_3, 1);
									st.takeItems(RECEIPT_OF_CONTRIBUTION_4, 1);
									st.takeItems(RECEIPT_OF_CONTRIBUTION_5, 1);
									st.giveItems(OLD_ACCOUNT_BOOK, 1);
									
									if (st.hasQuestItems(BLESSED_SEED, EMILY_RECIPE, LILITH_ELVEN_WAFER))
									{
										st.set("cond", "2");
										st.playSound(QuestState.SOUND_MIDDLE);
									}
									else
										st.playSound(QuestState.SOUND_ITEMGET);
								}
								else
									htmltext = "30531-04.htm";
							}
							else
								htmltext = (st.hasQuestItems(OLD_ACCOUNT_BOOK)) ? "30531-06.htm" : "30531-01.htm";
						}
						else if (cond >= 4)
							htmltext = "30531-07.htm";
						break;
					
					case SPIRON:
						if (cond == 1 && st.hasQuestItems(COLLECTION_LICENSE))
						{
							if (st.hasQuestItems(LOCKIRIN_NOTICE_1))
							{
								htmltext = "30532-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(LOCKIRIN_NOTICE_1, 1);
							}
							else if (st.hasQuestItems(CONTRIBUTION_OF_SHARI))
							{
								htmltext = "30532-03.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(CONTRIBUTION_OF_SHARI, 1);
								st.giveItems(RECEIPT_OF_CONTRIBUTION_1, 1);
							}
							else
								htmltext = (st.hasQuestItems(RECEIPT_OF_CONTRIBUTION_1)) ? "30532-04.htm" : "30532-02.htm";
						}
						break;
					
					case BALANKI:
						if (cond == 1 && st.hasQuestItems(COLLECTION_LICENSE))
						{
							if (st.hasQuestItems(LOCKIRIN_NOTICE_2))
							{
								htmltext = "30533-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(LOCKIRIN_NOTICE_2, 1);
							}
							else if (st.hasQuestItems(CONTRIBUTION_OF_MARYSE, CONTRIBUTION_OF_MION))
							{
								htmltext = "30533-03.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(CONTRIBUTION_OF_MARYSE, 1);
								st.takeItems(CONTRIBUTION_OF_MION, 1);
								st.giveItems(RECEIPT_OF_CONTRIBUTION_2, 1);
							}
							else
								htmltext = (st.hasQuestItems(RECEIPT_OF_CONTRIBUTION_2)) ? "30533-04.htm" : "30533-02.htm";
						}
						break;
					
					case KEEF:
						if (cond == 1 && st.hasQuestItems(COLLECTION_LICENSE))
						{
							if (st.hasQuestItems(LOCKIRIN_NOTICE_3))
							{
								htmltext = "30534-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(LOCKIRIN_NOTICE_3, 1);
							}
							else if (st.hasQuestItems(PROCURATION_OF_TOROCCO))
								htmltext = "30534-03.htm";
							else
								htmltext = (st.hasQuestItems(RECEIPT_OF_CONTRIBUTION_3)) ? "30534-04.htm" : "30534-02.htm";
						}
						break;
					
					case FILAUR:
						if (cond == 1 && st.hasQuestItems(COLLECTION_LICENSE))
						{
							if (st.hasQuestItems(LOCKIRIN_NOTICE_4))
							{
								htmltext = "30535-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(LOCKIRIN_NOTICE_4, 1);
							}
							else if (st.hasQuestItems(RECEIPT_OF_BOLTER))
							{
								htmltext = "30535-03.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(RECEIPT_OF_BOLTER, 1);
								st.giveItems(RECEIPT_OF_CONTRIBUTION_4, 1);
							}
							else
								htmltext = (st.hasQuestItems(RECEIPT_OF_CONTRIBUTION_4)) ? "30535-04.htm" : "30535-02.htm";
						}
						break;
					
					case ARIN:
						if (cond == 1 && st.hasQuestItems(COLLECTION_LICENSE))
						{
							if (st.hasQuestItems(LOCKIRIN_NOTICE_5))
							{
								htmltext = "30536-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(LOCKIRIN_NOTICE_5, 1);
							}
							else if (st.hasQuestItems(CONTRIBUTION_OF_TOMA))
							{
								htmltext = "30536-03.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(CONTRIBUTION_OF_TOMA, 1);
								st.giveItems(RECEIPT_OF_CONTRIBUTION_5, 1);
							}
							else
								htmltext = (st.hasQuestItems(RECEIPT_OF_CONTRIBUTION_5)) ? "30536-04.htm" : "30536-02.htm";
						}
						break;
					
					case SHARI:
						if (cond == 1 && st.hasQuestItems(COLLECTION_LICENSE))
						{
							if (st.hasQuestItems(CONTRIBUTION_OF_SHARI))
								htmltext = "30517-02.htm";
							else if (!st.hasAtLeastOneQuestItem(LOCKIRIN_NOTICE_1, RECEIPT_OF_CONTRIBUTION_1))
							{
								htmltext = "30517-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.giveItems(CONTRIBUTION_OF_SHARI, 1);
							}
						}
						break;
					
					case MION:
						if (cond == 1 && st.hasQuestItems(COLLECTION_LICENSE))
						{
							if (st.hasQuestItems(CONTRIBUTION_OF_MION))
								htmltext = "30519-02.htm";
							else if (!st.hasAtLeastOneQuestItem(LOCKIRIN_NOTICE_2, RECEIPT_OF_CONTRIBUTION_2))
							{
								htmltext = "30519-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.giveItems(CONTRIBUTION_OF_MION, 1);
							}
						}
						break;
					
					case MARYSE_REDBONNET:
						if (cond == 1 && st.hasQuestItems(COLLECTION_LICENSE))
						{
							if (st.hasQuestItems(MARYSE_REQUEST))
							{
								if (st.getQuestItemsCount(ANIMAL_SKIN) < 100)
									htmltext = "30553-02.htm";
								else
								{
									htmltext = "30553-03.htm";
									st.playSound(QuestState.SOUND_ITEMGET);
									st.takeItems(ANIMAL_SKIN, 100);
									st.takeItems(MARYSE_REQUEST, 1);
									st.giveItems(CONTRIBUTION_OF_MARYSE, 1);
								}
							}
							else if (st.hasQuestItems(CONTRIBUTION_OF_MARYSE))
								htmltext = "30553-04.htm";
							else if (!st.hasAtLeastOneQuestItem(LOCKIRIN_NOTICE_2, RECEIPT_OF_CONTRIBUTION_2))
							{
								htmltext = "30553-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.giveItems(MARYSE_REQUEST, 1);
							}
						}
						break;
					
					case TOROCCO:
						if (cond == 1 && st.hasQuestItems(COLLECTION_LICENSE))
						{
							if (st.hasQuestItems(PROCURATION_OF_TOROCCO))
								htmltext = "30555-03.htm";
							else if (!st.hasAtLeastOneQuestItem(LOCKIRIN_NOTICE_3, RECEIPT_OF_CONTRIBUTION_3))
								htmltext = "30555-01.htm";
						}
						break;
					
					case BOLTER:
						if (cond == 1 && st.hasQuestItems(COLLECTION_LICENSE))
						{
							if (st.hasQuestItems(RECEIPT_OF_BOLTER))
								htmltext = "30554-02.htm";
							else if (!st.hasAtLeastOneQuestItem(LOCKIRIN_NOTICE_4, RECEIPT_OF_CONTRIBUTION_4))
							{
								htmltext = "30554-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.giveItems(RECEIPT_OF_BOLTER, 1);
							}
						}
						break;
					
					case TOMA:
						if (cond == 1 && st.hasQuestItems(COLLECTION_LICENSE))
						{
							if (st.hasQuestItems(CONTRIBUTION_OF_TOMA))
								htmltext = "30556-02.htm";
							else if (!st.hasAtLeastOneQuestItem(LOCKIRIN_NOTICE_5, RECEIPT_OF_CONTRIBUTION_5))
							{
								htmltext = "30556-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.giveItems(CONTRIBUTION_OF_TOMA, 1);
							}
						}
						break;
					
					case PIOTUR:
						if (cond == 1 || cond == 2)
							htmltext = (st.hasQuestItems(BLESSED_SEED)) ? "30597-03.htm" : "30597-01.htm";
						else if (cond >= 4)
							htmltext = "30597-04.htm";
						break;
					
					case WILFORD:
						if (cond == 1 || cond == 2)
						{
							if (st.hasQuestItems(LILITH_ELVEN_WAFER))
								htmltext = "30005-06.htm";
							else
								htmltext = (st.hasQuestItems(CRYSTAL_BROOCH)) ? "30005-05.htm" : "30005-01.htm";
						}
						else if (cond >= 4)
							htmltext = "30005-07.htm";
						break;
					
					case LILITH:
						if (cond == 1 || cond == 2)
						{
							if (st.hasQuestItems(CRYSTAL_BROOCH))
								htmltext = "30368-01.htm";
							else if (st.hasQuestItems(LILITH_ELVEN_WAFER))
								htmltext = "30368-04.htm";
						}
						else if (cond >= 4)
							htmltext = "30368-05.htm";
						break;
					
					case BRIGHT:
						if (cond == 1 || cond == 2)
						{
							if (st.hasQuestItems(EMILY_RECIPE))
								htmltext = "30466-07.htm";
							else if (st.hasQuestItems(MANDRAGORA_BOUQUET))
								htmltext = "30466-06.htm";
							else if (st.hasQuestItems(BRIGHT_LIST))
							{
								if (st.getQuestItemsCount(CRIMSON_MOSS) + st.getQuestItemsCount(MANDRAGORA_PETAL) < 30)
									htmltext = "30466-04.htm";
								else
								{
									htmltext = "30466-05.htm";
									st.playSound(QuestState.SOUND_ITEMGET);
									st.takeItems(BRIGHT_LIST, 1);
									st.takeItems(CRIMSON_MOSS, 10);
									st.takeItems(MANDRAGORA_PETAL, 20);
									st.giveItems(MANDRAGORA_BOUQUET, 1);
								}
							}
							else
								htmltext = "30466-01.htm";
						}
						else if (cond >= 4)
							htmltext = "30466-08.htm";
						break;
					
					case EMILY:
						if (cond == 1 || cond == 2)
						{
							if (st.hasQuestItems(EMILY_RECIPE))
								htmltext = "30620-04.htm";
							else if (st.hasQuestItems(MANDRAGORA_BOUQUET))
								htmltext = "30620-01.htm";
						}
						else if (cond >= 4)
							htmltext = "30620-05.htm";
						break;
					
					case NIKOLA:
						if (cond == 4)
						{
							htmltext = "30621-01.htm";
							st.playSound(QuestState.SOUND_ITEMGET);
							st.takeItems(PARMAN_LETTER, 1);
						}
						else if (cond == 5)
							htmltext = "30621-05.htm";
						else if (cond == 6)
						{
							htmltext = "30621-06.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(PATTERN_OF_KEYHOLE, 1);
							st.giveItems(NIKOLAS_LIST, 1);
							st.giveItems(RECIPE_TITAN_KEY, 1);
						}
						else if (cond == 7 || cond == 8)
							htmltext = (st.hasQuestItems(KEY_OF_TITAN)) ? "30621-08.htm" : "30621-07.htm";
						else if (cond == 9)
							htmltext = "30621-09.htm";
						break;
					
					case BOX_OF_TITAN:
						if (cond == 5)
							htmltext = "30622-01.htm";
						else if (cond == 8 && st.hasQuestItems(KEY_OF_TITAN))
							htmltext = "30622-03.htm";
						else
							htmltext = "30622-05.htm";
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
			case MANDRAGORA_SPROUT_1:
				if (st.hasQuestItems(BRIGHT_LIST))
					st.dropItems(MANDRAGORA_PETAL, 1, 20, 300000);
				break;
			
			case MANDRAGORA_SPROUT_2:
				if (st.hasQuestItems(BRIGHT_LIST))
					st.dropItems(MANDRAGORA_PETAL, 1, 20, 600000);
				break;
			
			case MANDRAGORA_SAPLING:
				if (st.hasQuestItems(BRIGHT_LIST))
					st.dropItems(MANDRAGORA_PETAL, 1, 20, 800000);
				break;
			
			case MANDRAGORA_BLOSSOM:
				if (st.hasQuestItems(BRIGHT_LIST))
					st.dropItemsAlways(MANDRAGORA_PETAL, 1, 20);
				break;
			
			case GIANT_CRIMSON_ANT:
				if (st.hasQuestItems(BRIGHT_LIST))
					st.dropItemsAlways(CRIMSON_MOSS, 1, 10);
				break;
			
			case MARSH_STAKATO:
				if (cond == 7 && st.dropItems(STAKATO_SHELL, 1, 20, 200000) && st.getQuestItemsCount(TOAD_LORD_SAC) + st.getQuestItemsCount(SPIDER_THORN) == 20)
					st.set("cond", "8");
				break;
			
			case MARSH_STAKATO_WORKER:
				if (cond == 7 && st.dropItems(STAKATO_SHELL, 1, 20, 300000) && st.getQuestItemsCount(TOAD_LORD_SAC) + st.getQuestItemsCount(SPIDER_THORN) == 20)
					st.set("cond", "8");
				break;
			
			case MARSH_STAKATO_SOLDIER:
				if (cond == 7 && st.dropItems(STAKATO_SHELL, 1, 20, 500000) && st.getQuestItemsCount(TOAD_LORD_SAC) + st.getQuestItemsCount(SPIDER_THORN) == 20)
					st.set("cond", "8");
				break;
			
			case MARSH_STAKATO_DRONE:
				if (cond == 7 && st.dropItems(STAKATO_SHELL, 1, 20, 600000) && st.getQuestItemsCount(TOAD_LORD_SAC) + st.getQuestItemsCount(SPIDER_THORN) == 20)
					st.set("cond", "8");
				break;
			
			case TOAD_LORD:
				if (cond == 7 && st.dropItems(TOAD_LORD_SAC, 1, 10, 200000) && st.getQuestItemsCount(STAKATO_SHELL) + st.getQuestItemsCount(SPIDER_THORN) == 30)
					st.set("cond", "8");
				break;
			
			case MARSH_SPIDER:
				if (cond == 7 && st.dropItems(SPIDER_THORN, 1, 10, 200000) && st.getQuestItemsCount(STAKATO_SHELL) + st.getQuestItemsCount(TOAD_LORD_SAC) == 30)
					st.set("cond", "8");
				break;
		}
		
		return null;
	}
}
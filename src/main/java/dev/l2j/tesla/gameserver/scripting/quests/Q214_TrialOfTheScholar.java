package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q214_TrialOfTheScholar extends Quest
{
	private static final String qn = "Q214_TrialOfTheScholar";
	
	// Items
	private static final int MIRIEN_SIGIL_1 = 2675;
	private static final int MIRIEN_SIGIL_2 = 2676;
	private static final int MIRIEN_SIGIL_3 = 2677;
	private static final int MIRIEN_INSTRUCTION = 2678;
	private static final int MARIA_LETTER_1 = 2679;
	private static final int MARIA_LETTER_2 = 2680;
	private static final int LUCAS_LETTER = 2681;
	private static final int LUCILLA_HANDBAG = 2682;
	private static final int CRETA_LETTER_1 = 2683;
	private static final int CRETA_PAINTING_1 = 2684;
	private static final int CRETA_PAINTING_2 = 2685;
	private static final int CRETA_PAINTING_3 = 2686;
	private static final int BROWN_SCROLL_SCRAP = 2687;
	private static final int CRYSTAL_OF_PURITY_1 = 2688;
	private static final int HIGH_PRIEST_SIGIL = 2689;
	private static final int GRAND_MAGISTER_SIGIL = 2690;
	private static final int CRONOS_SIGIL = 2691;
	private static final int SYLVAIN_LETTER = 2692;
	private static final int SYMBOL_OF_SYLVAIN = 2693;
	private static final int JUREK_LIST = 2694;
	private static final int MONSTER_EYE_DESTROYER_SKIN = 2695;
	private static final int SHAMAN_NECKLACE = 2696;
	private static final int SHACKLE_SCALP = 2697;
	private static final int SYMBOL_OF_JUREK = 2698;
	private static final int CRONOS_LETTER = 2699;
	private static final int DIETER_KEY = 2700;
	private static final int CRETA_LETTER_2 = 2701;
	private static final int DIETER_LETTER = 2702;
	private static final int DIETER_DIARY = 2703;
	private static final int RAUT_LETTER_ENVELOPE = 2704;
	private static final int TRIFF_RING = 2705;
	private static final int SCRIPTURE_CHAPTER_1 = 2706;
	private static final int SCRIPTURE_CHAPTER_2 = 2707;
	private static final int SCRIPTURE_CHAPTER_3 = 2708;
	private static final int SCRIPTURE_CHAPTER_4 = 2709;
	private static final int VALKON_REQUEST = 2710;
	private static final int POITAN_NOTES = 2711;
	private static final int STRONG_LIQUOR = 2713;
	private static final int CRYSTAL_OF_PURITY_2 = 2714;
	private static final int CASIAN_LIST = 2715;
	private static final int GHOUL_SKIN = 2716;
	private static final int MEDUSA_BLOOD = 2717;
	private static final int FETTERED_SOUL_ICHOR = 2718;
	private static final int ENCHANTED_GARGOYLE_NAIL = 2719;
	private static final int SYMBOL_OF_CRONOS = 2720;
	
	// Rewards
	private static final int MARK_OF_SCHOLAR = 2674;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int SYLVAIN = 30070;
	private static final int LUCAS = 30071;
	private static final int VALKON = 30103;
	private static final int DIETER = 30111;
	private static final int JUREK = 30115;
	private static final int EDROC = 30230;
	private static final int RAUT = 30316;
	private static final int POITAN = 30458;
	private static final int MIRIEN = 30461;
	private static final int MARIA = 30608;
	private static final int CRETA = 30609;
	private static final int CRONOS = 30610;
	private static final int TRIFF = 30611;
	private static final int CASIAN = 30612;
	
	// Monsters
	private static final int MONSTER_EYE_DESTROYER = 20068;
	private static final int MEDUSA = 20158;
	private static final int GHOUL = 20201;
	private static final int SHACKLE_1 = 20235;
	private static final int SHACKLE_2 = 20279;
	private static final int BREKA_ORC_SHAMAN = 20269;
	private static final int FETTERED_SOUL = 20552;
	private static final int GRANDIS = 20554;
	private static final int ENCHANTED_GARGOYLE = 20567;
	private static final int LETO_LIZARDMAN_WARRIOR = 20580;
	
	public Q214_TrialOfTheScholar()
	{
		super(214, "Trial Of The Scholar");
		
		setItemsIds(MIRIEN_SIGIL_1, MIRIEN_SIGIL_2, MIRIEN_SIGIL_3, MIRIEN_INSTRUCTION, MARIA_LETTER_1, MARIA_LETTER_2, LUCAS_LETTER, LUCILLA_HANDBAG, CRETA_LETTER_1, CRETA_PAINTING_1, CRETA_PAINTING_2, CRETA_PAINTING_3, BROWN_SCROLL_SCRAP, CRYSTAL_OF_PURITY_1, HIGH_PRIEST_SIGIL, GRAND_MAGISTER_SIGIL, CRONOS_SIGIL, SYLVAIN_LETTER, SYMBOL_OF_SYLVAIN, JUREK_LIST, MONSTER_EYE_DESTROYER_SKIN, SHAMAN_NECKLACE, SHACKLE_SCALP, SYMBOL_OF_JUREK, CRONOS_LETTER, DIETER_KEY, CRETA_LETTER_2, DIETER_LETTER, DIETER_DIARY, RAUT_LETTER_ENVELOPE, TRIFF_RING, SCRIPTURE_CHAPTER_1, SCRIPTURE_CHAPTER_2, SCRIPTURE_CHAPTER_3, SCRIPTURE_CHAPTER_4, VALKON_REQUEST, POITAN_NOTES, STRONG_LIQUOR, CRYSTAL_OF_PURITY_2, CASIAN_LIST, GHOUL_SKIN, MEDUSA_BLOOD, FETTERED_SOUL_ICHOR, ENCHANTED_GARGOYLE_NAIL, SYMBOL_OF_CRONOS);
		
		addStartNpc(MIRIEN);
		addTalkId(MIRIEN, SYLVAIN, LUCAS, VALKON, DIETER, JUREK, EDROC, RAUT, POITAN, MARIA, CRETA, CRONOS, TRIFF, CASIAN);
		
		addKillId(MONSTER_EYE_DESTROYER, MEDUSA, GHOUL, SHACKLE_1, SHACKLE_2, BREKA_ORC_SHAMAN, FETTERED_SOUL, GRANDIS, ENCHANTED_GARGOYLE, LETO_LIZARDMAN_WARRIOR);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// MIRIEN
		if (event.equalsIgnoreCase("30461-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(MIRIEN_SIGIL_1, 1);
			
			if (!player.getMemos().getBool("secondClassChange35", false))
			{
				htmltext = "30461-04a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_35.get(player.getClassId().getId()));
				player.getMemos().set("secondClassChange35", true);
			}
		}
		else if (event.equalsIgnoreCase("30461-09.htm"))
		{
			if (player.getLevel() < 36)
			{
				st.playSound(QuestState.SOUND_ITEMGET);
				st.giveItems(MIRIEN_INSTRUCTION, 1);
			}
			else
			{
				htmltext = "30461-10.htm";
				st.set("cond", "19");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(MIRIEN_SIGIL_2, 1);
				st.takeItems(SYMBOL_OF_JUREK, 1);
				st.giveItems(MIRIEN_SIGIL_3, 1);
			}
		}
		// SYLVAIN
		else if (event.equalsIgnoreCase("30070-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(HIGH_PRIEST_SIGIL, 1);
			st.giveItems(SYLVAIN_LETTER, 1);
		}
		// MARIA
		else if (event.equalsIgnoreCase("30608-02.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(SYLVAIN_LETTER, 1);
			st.giveItems(MARIA_LETTER_1, 1);
		}
		else if (event.equalsIgnoreCase("30608-08.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CRETA_LETTER_1, 1);
			st.giveItems(LUCILLA_HANDBAG, 1);
		}
		else if (event.equalsIgnoreCase("30608-14.htm"))
		{
			st.set("cond", "13");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BROWN_SCROLL_SCRAP, -1);
			st.takeItems(CRETA_PAINTING_3, 1);
			st.giveItems(CRYSTAL_OF_PURITY_1, 1);
		}
		// JUREK
		else if (event.equalsIgnoreCase("30115-03.htm"))
		{
			st.set("cond", "16");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(GRAND_MAGISTER_SIGIL, 1);
			st.giveItems(JUREK_LIST, 1);
		}
		// LUCAS
		else if (event.equalsIgnoreCase("30071-04.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CRETA_PAINTING_2, 1);
			st.giveItems(CRETA_PAINTING_3, 1);
		}
		// CRETA
		else if (event.equalsIgnoreCase("30609-05.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(MARIA_LETTER_2, 1);
			st.giveItems(CRETA_LETTER_1, 1);
		}
		else if (event.equalsIgnoreCase("30609-09.htm"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LUCILLA_HANDBAG, 1);
			st.giveItems(CRETA_PAINTING_1, 1);
		}
		else if (event.equalsIgnoreCase("30609-14.htm"))
		{
			st.set("cond", "22");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(DIETER_KEY, 1);
			st.giveItems(CRETA_LETTER_2, 1);
		}
		// CRONOS
		else if (event.equalsIgnoreCase("30610-10.htm"))
		{
			st.set("cond", "20");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(CRONOS_LETTER, 1);
			st.giveItems(CRONOS_SIGIL, 1);
		}
		else if (event.equalsIgnoreCase("30610-14.htm"))
		{
			st.set("cond", "31");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CRONOS_SIGIL, 1);
			st.takeItems(DIETER_DIARY, 1);
			st.takeItems(SCRIPTURE_CHAPTER_1, 1);
			st.takeItems(SCRIPTURE_CHAPTER_2, 1);
			st.takeItems(SCRIPTURE_CHAPTER_3, 1);
			st.takeItems(SCRIPTURE_CHAPTER_4, 1);
			st.takeItems(TRIFF_RING, 1);
			st.giveItems(SYMBOL_OF_CRONOS, 1);
		}
		// DIETER
		else if (event.equalsIgnoreCase("30111-05.htm"))
		{
			st.set("cond", "21");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CRONOS_LETTER, 1);
			st.giveItems(DIETER_KEY, 1);
		}
		else if (event.equalsIgnoreCase("30111-09.htm"))
		{
			st.set("cond", "23");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CRETA_LETTER_2, 1);
			st.giveItems(DIETER_DIARY, 1);
			st.giveItems(DIETER_LETTER, 1);
		}
		// EDROC
		else if (event.equalsIgnoreCase("30230-02.htm"))
		{
			st.set("cond", "24");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(DIETER_LETTER, 1);
			st.giveItems(RAUT_LETTER_ENVELOPE, 1);
		}
		// RAUT
		else if (event.equalsIgnoreCase("30316-02.htm"))
		{
			st.set("cond", "25");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(RAUT_LETTER_ENVELOPE, 1);
			st.giveItems(SCRIPTURE_CHAPTER_1, 1);
			st.giveItems(STRONG_LIQUOR, 1);
		}
		// TRIFF
		else if (event.equalsIgnoreCase("30611-04.htm"))
		{
			st.set("cond", "26");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(STRONG_LIQUOR, 1);
			st.giveItems(TRIFF_RING, 1);
		}
		// VALKON
		else if (event.equalsIgnoreCase("30103-04.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(VALKON_REQUEST, 1);
		}
		// CASIAN
		else if (event.equalsIgnoreCase("30612-04.htm"))
		{
			st.set("cond", "28");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(CASIAN_LIST, 1);
		}
		else if (event.equalsIgnoreCase("30612-07.htm"))
		{
			st.set("cond", "30");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CASIAN_LIST, 1);
			st.takeItems(ENCHANTED_GARGOYLE_NAIL, -1);
			st.takeItems(FETTERED_SOUL_ICHOR, -1);
			st.takeItems(GHOUL_SKIN, -1);
			st.takeItems(MEDUSA_BLOOD, -1);
			st.takeItems(POITAN_NOTES, 1);
			st.giveItems(SCRIPTURE_CHAPTER_4, 1);
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
				if (player.getClassId() != ClassId.HUMAN_WIZARD && player.getClassId() != ClassId.ELVEN_WIZARD && player.getClassId() != ClassId.DARK_WIZARD)
					htmltext = "30461-01.htm";
				else if (player.getLevel() < 35)
					htmltext = "30461-02.htm";
				else
					htmltext = "30461-03.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case MIRIEN:
						if (cond < 14)
							htmltext = "30461-05.htm";
						else if (cond == 14)
						{
							htmltext = "30461-06.htm";
							st.set("cond", "15");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(MIRIEN_SIGIL_1, 1);
							st.takeItems(SYMBOL_OF_SYLVAIN, 1);
							st.giveItems(MIRIEN_SIGIL_2, 1);
						}
						else if (cond > 14 && cond < 18)
							htmltext = "30461-07.htm";
						else if (cond == 18)
						{
							if (!st.hasQuestItems(MIRIEN_INSTRUCTION))
								htmltext = "30461-08.htm";
							else
							{
								if (player.getLevel() < 36)
									htmltext = "30461-11.htm";
								else
								{
									htmltext = "30461-12.htm";
									st.set("cond", "19");
									st.playSound(QuestState.SOUND_MIDDLE);
									st.takeItems(MIRIEN_INSTRUCTION, 1);
									st.takeItems(MIRIEN_SIGIL_2, 1);
									st.takeItems(SYMBOL_OF_JUREK, 1);
									st.giveItems(MIRIEN_SIGIL_3, 1);
								}
							}
						}
						else if (cond > 18 && cond < 31)
							htmltext = "30461-13.htm";
						else if (cond == 31)
						{
							htmltext = "30461-14.htm";
							st.takeItems(MIRIEN_SIGIL_3, 1);
							st.takeItems(SYMBOL_OF_CRONOS, 1);
							st.giveItems(MARK_OF_SCHOLAR, 1);
							st.rewardExpAndSp(80265, 30000);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case SYLVAIN:
						if (cond == 1)
							htmltext = "30070-01.htm";
						else if (cond < 13)
							htmltext = "30070-03.htm";
						else if (cond == 13)
						{
							htmltext = "30070-04.htm";
							st.set("cond", "14");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(CRYSTAL_OF_PURITY_1, 1);
							st.takeItems(HIGH_PRIEST_SIGIL, 1);
							st.giveItems(SYMBOL_OF_SYLVAIN, 1);
						}
						else if (cond == 14)
							htmltext = "30070-05.htm";
						else if (cond > 14)
							htmltext = "30070-06.htm";
						break;
					
					case MARIA:
						if (cond == 2)
							htmltext = "30608-01.htm";
						else if (cond == 3)
							htmltext = "30608-03.htm";
						else if (cond == 4)
						{
							htmltext = "30608-04.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(LUCAS_LETTER, 1);
							st.giveItems(MARIA_LETTER_2, 1);
						}
						else if (cond == 5)
							htmltext = "30608-05.htm";
						else if (cond == 6)
							htmltext = "30608-06.htm";
						else if (cond == 7)
							htmltext = "30608-09.htm";
						else if (cond == 8)
						{
							htmltext = "30608-10.htm";
							st.set("cond", "9");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(CRETA_PAINTING_1, 1);
							st.giveItems(CRETA_PAINTING_2, 1);
						}
						else if (cond == 9)
							htmltext = "30608-11.htm";
						else if (cond == 10)
						{
							htmltext = "30608-12.htm";
							st.set("cond", "11");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 11)
							htmltext = "30608-12.htm";
						else if (cond == 12)
							htmltext = "30608-13.htm";
						else if (cond == 13)
							htmltext = "30608-15.htm";
						else if (st.hasAtLeastOneQuestItem(SYMBOL_OF_SYLVAIN, MIRIEN_SIGIL_2))
							htmltext = "30608-16.htm";
						else if (cond > 18)
						{
							if (!st.hasQuestItems(VALKON_REQUEST))
								htmltext = "30608-17.htm";
							else
							{
								htmltext = "30608-18.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(VALKON_REQUEST, 1);
								st.giveItems(CRYSTAL_OF_PURITY_2, 1);
							}
						}
						break;
					
					case JUREK:
						if (cond == 15)
							htmltext = "30115-01.htm";
						else if (cond == 16)
							htmltext = "30115-04.htm";
						else if (cond == 17)
						{
							htmltext = "30115-05.htm";
							st.set("cond", "18");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(GRAND_MAGISTER_SIGIL, 1);
							st.takeItems(JUREK_LIST, 1);
							st.takeItems(MONSTER_EYE_DESTROYER_SKIN, -1);
							st.takeItems(SHACKLE_SCALP, -1);
							st.takeItems(SHAMAN_NECKLACE, -1);
							st.giveItems(SYMBOL_OF_JUREK, 1);
						}
						else if (cond == 18)
							htmltext = "30115-06.htm";
						else if (cond > 18)
							htmltext = "30115-07.htm";
						break;
					
					case LUCAS:
						if (cond == 3)
						{
							htmltext = "30071-01.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(MARIA_LETTER_1, 1);
							st.giveItems(LUCAS_LETTER, 1);
						}
						else if (cond > 3 && cond < 9)
							htmltext = "30071-02.htm";
						else if (cond == 9)
							htmltext = "30071-03.htm";
						else if (cond == 10 || cond == 11)
							htmltext = "30071-05.htm";
						else if (cond == 12)
							htmltext = "30071-06.htm";
						else if (cond > 12)
							htmltext = "30071-07.htm";
						break;
					
					case CRETA:
						if (cond == 5)
							htmltext = "30609-01.htm";
						else if (cond == 6)
							htmltext = "30609-06.htm";
						else if (cond == 7)
							htmltext = "30609-07.htm";
						else if (cond > 7 && cond < 13)
							htmltext = "30609-10.htm";
						else if (cond >= 13 && cond < 19)
							htmltext = "30609-11.htm";
						else if (cond == 21)
							htmltext = "30609-12.htm";
						else if (cond > 21)
							htmltext = "30609-15.htm";
						break;
					
					case CRONOS:
						if (cond == 19)
							htmltext = "30610-01.htm";
						else if (cond > 19 && cond < 30)
							htmltext = "30610-11.htm";
						else if (cond == 30)
							htmltext = "30610-12.htm";
						else if (cond == 31)
							htmltext = "30610-15.htm";
						break;
					
					case DIETER:
						if (cond == 20)
							htmltext = "30111-01.htm";
						else if (cond == 21)
							htmltext = "30111-06.htm";
						else if (cond == 22)
							htmltext = "30111-07.htm";
						else if (cond == 23)
							htmltext = "30111-10.htm";
						else if (cond == 24)
							htmltext = "30111-11.htm";
						else if (cond > 24 && cond < 31)
							htmltext = (!st.hasQuestItems(SCRIPTURE_CHAPTER_1, SCRIPTURE_CHAPTER_2, SCRIPTURE_CHAPTER_3, SCRIPTURE_CHAPTER_4)) ? "30111-12.htm" : "30111-13.htm";
						else if (cond == 31)
							htmltext = "30111-15.htm";
						break;
					
					case EDROC:
						if (cond == 23)
							htmltext = "30230-01.htm";
						else if (cond == 24)
							htmltext = "30230-03.htm";
						else if (cond > 24)
							htmltext = "30230-04.htm";
						break;
					
					case RAUT:
						if (cond == 24)
							htmltext = "30316-01.htm";
						else if (cond == 25)
							htmltext = "30316-04.htm";
						else if (cond > 25)
							htmltext = "30316-05.htm";
						break;
					
					case TRIFF:
						if (cond == 25)
							htmltext = "30611-01.htm";
						else if (cond > 25)
							htmltext = "30611-05.htm";
						break;
					
					case VALKON:
						if (st.hasQuestItems(TRIFF_RING))
						{
							if (!st.hasQuestItems(SCRIPTURE_CHAPTER_2))
							{
								if (!st.hasQuestItems(VALKON_REQUEST))
								{
									if (!st.hasQuestItems(CRYSTAL_OF_PURITY_2))
										htmltext = "30103-01.htm";
									else
									{
										htmltext = "30103-06.htm";
										st.playSound(QuestState.SOUND_ITEMGET);
										st.takeItems(CRYSTAL_OF_PURITY_2, 1);
										st.giveItems(SCRIPTURE_CHAPTER_2, 1);
									}
								}
								else
									htmltext = "30103-05.htm";
							}
							else
								htmltext = "30103-07.htm";
						}
						break;
					
					case POITAN:
						if (cond == 26 || cond == 27)
						{
							if (!st.hasQuestItems(POITAN_NOTES))
							{
								htmltext = "30458-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.giveItems(POITAN_NOTES, 1);
							}
							else
								htmltext = "30458-02.htm";
						}
						else if (cond == 28 || cond == 29)
							htmltext = "30458-03.htm";
						else if (cond == 30)
							htmltext = "30458-04.htm";
						break;
					
					case CASIAN:
						if ((cond == 26 || cond == 27) && st.hasQuestItems(POITAN_NOTES))
						{
							if (st.hasQuestItems(SCRIPTURE_CHAPTER_1, SCRIPTURE_CHAPTER_2, SCRIPTURE_CHAPTER_3))
								htmltext = "30612-02.htm";
							else
							{
								htmltext = "30612-01.htm";
								if (cond == 26)
								{
									st.set("cond", "27");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
							}
						}
						else if (cond == 28)
							htmltext = "30612-05.htm";
						else if (cond == 29)
							htmltext = "30612-06.htm";
						else if (cond == 30)
							htmltext = "30612-08.htm";
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
			case LETO_LIZARDMAN_WARRIOR:
				if (st.getInt("cond") == 11 && st.dropItems(BROWN_SCROLL_SCRAP, 1, 5, 500000))
					st.set("cond", "12");
				break;
			
			case SHACKLE_1:
			case SHACKLE_2:
				if (st.getInt("cond") == 16 && st.dropItems(SHACKLE_SCALP, 1, 2, 500000))
					if (st.getQuestItemsCount(MONSTER_EYE_DESTROYER_SKIN) == 5 && st.getQuestItemsCount(SHAMAN_NECKLACE) == 5)
						st.set("cond", "17");
				break;
			
			case MONSTER_EYE_DESTROYER:
				if (st.getInt("cond") == 16 && st.dropItems(MONSTER_EYE_DESTROYER_SKIN, 1, 5, 500000))
					if (st.getQuestItemsCount(SHACKLE_SCALP) == 2 && st.getQuestItemsCount(SHAMAN_NECKLACE) == 5)
						st.set("cond", "17");
				break;
			
			case BREKA_ORC_SHAMAN:
				if (st.getInt("cond") == 16 && st.dropItems(SHAMAN_NECKLACE, 1, 5, 500000))
					if (st.getQuestItemsCount(SHACKLE_SCALP) == 2 && st.getQuestItemsCount(MONSTER_EYE_DESTROYER_SKIN) == 5)
						st.set("cond", "17");
				break;
			
			case GRANDIS:
				if (st.hasQuestItems(TRIFF_RING))
					st.dropItems(SCRIPTURE_CHAPTER_3, 1, 1, 300000);
				break;
			
			case MEDUSA:
				if (st.getInt("cond") == 28 && st.dropItemsAlways(MEDUSA_BLOOD, 1, 12))
					if (st.getQuestItemsCount(GHOUL_SKIN) == 10 && st.getQuestItemsCount(FETTERED_SOUL_ICHOR) == 5 && st.getQuestItemsCount(ENCHANTED_GARGOYLE_NAIL) == 5)
						st.set("cond", "29");
				break;
			
			case GHOUL:
				if (st.getInt("cond") == 28 && st.dropItemsAlways(GHOUL_SKIN, 1, 10))
					if (st.getQuestItemsCount(MEDUSA_BLOOD) == 12 && st.getQuestItemsCount(FETTERED_SOUL_ICHOR) == 5 && st.getQuestItemsCount(ENCHANTED_GARGOYLE_NAIL) == 5)
						st.set("cond", "29");
				break;
			
			case FETTERED_SOUL:
				if (st.getInt("cond") == 28 && st.dropItemsAlways(FETTERED_SOUL_ICHOR, 1, 5))
					if (st.getQuestItemsCount(MEDUSA_BLOOD) == 12 && st.getQuestItemsCount(GHOUL_SKIN) == 10 && st.getQuestItemsCount(ENCHANTED_GARGOYLE_NAIL) == 5)
						st.set("cond", "29");
				break;
			
			case ENCHANTED_GARGOYLE:
				if (st.getInt("cond") == 28 && st.dropItemsAlways(ENCHANTED_GARGOYLE_NAIL, 1, 5))
					if (st.getQuestItemsCount(MEDUSA_BLOOD) == 12 && st.getQuestItemsCount(GHOUL_SKIN) == 10 && st.getQuestItemsCount(FETTERED_SOUL_ICHOR) == 5)
						st.set("cond", "29");
				break;
		}
		
		return null;
	}
}
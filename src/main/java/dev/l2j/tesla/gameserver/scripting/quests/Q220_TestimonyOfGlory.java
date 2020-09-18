package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q220_TestimonyOfGlory extends Quest
{
	private static final String qn = "Q220_TestimonyOfGlory";
	
	// Items
	private static final int VOKIAN_ORDER_1 = 3204;
	private static final int MANASHEN_SHARD = 3205;
	private static final int TYRANT_TALON = 3206;
	private static final int GUARDIAN_BASILISK_FANG = 3207;
	private static final int VOKIAN_ORDER_2 = 3208;
	private static final int NECKLACE_OF_AUTHORITY = 3209;
	private static final int CHIANTA_ORDER_1 = 3210;
	private static final int SCEPTER_OF_BREKA = 3211;
	private static final int SCEPTER_OF_ENKU = 3212;
	private static final int SCEPTER_OF_VUKU = 3213;
	private static final int SCEPTER_OF_TUREK = 3214;
	private static final int SCEPTER_OF_TUNATH = 3215;
	private static final int CHIANTA_ORDER_2 = 3216;
	private static final int CHIANTA_ORDER_3 = 3217;
	private static final int TAMLIN_ORC_SKULL = 3218;
	private static final int TIMAK_ORC_HEAD = 3219;
	private static final int SCEPTER_BOX = 3220;
	private static final int PASHIKA_HEAD = 3221;
	private static final int VULTUS_HEAD = 3222;
	private static final int GLOVE_OF_VOLTAR = 3223;
	private static final int ENKU_OVERLORD_HEAD = 3224;
	private static final int GLOVE_OF_KEPRA = 3225;
	private static final int MAKUM_BUGBEAR_HEAD = 3226;
	private static final int GLOVE_OF_BURAI = 3227;
	private static final int MANAKIA_LETTER_1 = 3228;
	private static final int MANAKIA_LETTER_2 = 3229;
	private static final int KASMAN_LETTER_1 = 3230;
	private static final int KASMAN_LETTER_2 = 3231;
	private static final int KASMAN_LETTER_3 = 3232;
	private static final int DRIKO_CONTRACT = 3233;
	private static final int STAKATO_DRONE_HUSK = 3234;
	private static final int TANAPI_ORDER = 3235;
	private static final int SCEPTER_OF_TANTOS = 3236;
	private static final int RITUAL_BOX = 3237;
	
	// Rewards
	private static final int MARK_OF_GLORY = 3203;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int KASMAN = 30501;
	private static final int VOKIAN = 30514;
	private static final int MANAKIA = 30515;
	private static final int KAKAI = 30565;
	private static final int TANAPI = 30571;
	private static final int VOLTAR = 30615;
	private static final int KEPRA = 30616;
	private static final int BURAI = 30617;
	private static final int HARAK = 30618;
	private static final int DRIKO = 30619;
	private static final int CHIANTA = 30642;
	
	// Monsters
	private static final int TYRANT = 20192;
	private static final int MARSH_STAKATO_DRONE = 20234;
	private static final int GUARDIAN_BASILISK = 20550;
	private static final int MANASHEN_GARGOYLE = 20563;
	private static final int TIMAK_ORC = 20583;
	private static final int TIMAK_ORC_ARCHER = 20584;
	private static final int TIMAK_ORC_SOLDIER = 20585;
	private static final int TIMAK_ORC_WARRIOR = 20586;
	private static final int TIMAK_ORC_SHAMAN = 20587;
	private static final int TIMAK_ORC_OVERLORD = 20588;
	private static final int TAMLIN_ORC = 20601;
	private static final int TAMLIN_ORC_ARCHER = 20602;
	private static final int RAGNA_ORC_OVERLORD = 20778;
	private static final int RAGNA_ORC_SEER = 20779;
	private static final int PASHIKA_SON_OF_VOLTAR = 27080;
	private static final int VULTUS_SON_OF_VOLTAR = 27081;
	private static final int ENKU_ORC_OVERLORD = 27082;
	private static final int MAKUM_BUGBEAR_THUG = 27083;
	private static final int REVENANT_OF_TANTOS_CHIEF = 27086;
	
	// Checks & Instances
	private static boolean _sonsOfVoltar = false;
	private static boolean _enkuOrcOverlords = false;
	private static boolean _makumBugbearThugs = false;
	
	public Q220_TestimonyOfGlory()
	{
		super(220, "Testimony Of Glory");
		
		setItemsIds(VOKIAN_ORDER_1, MANASHEN_SHARD, TYRANT_TALON, GUARDIAN_BASILISK_FANG, VOKIAN_ORDER_2, NECKLACE_OF_AUTHORITY, CHIANTA_ORDER_1, SCEPTER_OF_BREKA, SCEPTER_OF_ENKU, SCEPTER_OF_VUKU, SCEPTER_OF_TUREK, SCEPTER_OF_TUNATH, CHIANTA_ORDER_2, CHIANTA_ORDER_3, TAMLIN_ORC_SKULL, TIMAK_ORC_HEAD, SCEPTER_BOX, PASHIKA_HEAD, VULTUS_HEAD, GLOVE_OF_VOLTAR, ENKU_OVERLORD_HEAD, GLOVE_OF_KEPRA, MAKUM_BUGBEAR_HEAD, GLOVE_OF_BURAI, MANAKIA_LETTER_1, MANAKIA_LETTER_2, KASMAN_LETTER_1, KASMAN_LETTER_2, KASMAN_LETTER_3, DRIKO_CONTRACT, STAKATO_DRONE_HUSK, TANAPI_ORDER, SCEPTER_OF_TANTOS, RITUAL_BOX);
		
		addStartNpc(VOKIAN);
		addTalkId(KASMAN, VOKIAN, MANAKIA, KAKAI, TANAPI, VOLTAR, KEPRA, BURAI, HARAK, DRIKO, CHIANTA);
		
		addAttackId(RAGNA_ORC_OVERLORD, RAGNA_ORC_SEER, REVENANT_OF_TANTOS_CHIEF);
		addKillId(TYRANT, MARSH_STAKATO_DRONE, GUARDIAN_BASILISK, MANASHEN_GARGOYLE, TIMAK_ORC, TIMAK_ORC_ARCHER, TIMAK_ORC_SOLDIER, TIMAK_ORC_WARRIOR, TIMAK_ORC_SHAMAN, TIMAK_ORC_OVERLORD, TAMLIN_ORC, TAMLIN_ORC_ARCHER, RAGNA_ORC_OVERLORD, RAGNA_ORC_SEER, PASHIKA_SON_OF_VOLTAR, VULTUS_SON_OF_VOLTAR, ENKU_ORC_OVERLORD, MAKUM_BUGBEAR_THUG, REVENANT_OF_TANTOS_CHIEF);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// VOKIAN
		if (event.equalsIgnoreCase("30514-05.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(VOKIAN_ORDER_1, 1);
			
			if (!player.getMemos().getBool("secondClassChange37", false))
			{
				htmltext = "30514-05a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_37.get(player.getRace().ordinal()));
				player.getMemos().set("secondClassChange37", true);
			}
		}
		// CHIANTA
		else if (event.equalsIgnoreCase("30642-03.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(VOKIAN_ORDER_2, 1);
			st.giveItems(CHIANTA_ORDER_1, 1);
		}
		else if (event.equalsIgnoreCase("30642-07.htm"))
		{
			st.takeItems(CHIANTA_ORDER_1, 1);
			st.takeItems(KASMAN_LETTER_1, 1);
			st.takeItems(MANAKIA_LETTER_1, 1);
			st.takeItems(MANAKIA_LETTER_2, 1);
			st.takeItems(SCEPTER_OF_BREKA, 1);
			st.takeItems(SCEPTER_OF_ENKU, 1);
			st.takeItems(SCEPTER_OF_TUNATH, 1);
			st.takeItems(SCEPTER_OF_TUREK, 1);
			st.takeItems(SCEPTER_OF_VUKU, 1);
			
			if (player.getLevel() >= 37)
			{
				st.set("cond", "6");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.giveItems(CHIANTA_ORDER_3, 1);
			}
			else
			{
				htmltext = "30642-06.htm";
				st.playSound(QuestState.SOUND_ITEMGET);
				st.giveItems(CHIANTA_ORDER_2, 1);
			}
		}
		// KASMAN
		else if (event.equalsIgnoreCase("30501-02.htm") && !st.hasQuestItems(SCEPTER_OF_VUKU))
		{
			if (st.hasQuestItems(KASMAN_LETTER_1))
				htmltext = "30501-04.htm";
			else
			{
				htmltext = "30501-03.htm";
				st.playSound(QuestState.SOUND_ITEMGET);
				st.giveItems(KASMAN_LETTER_1, 1);
			}
			st.addRadar(-2150, 124443, -3724);
		}
		else if (event.equalsIgnoreCase("30501-05.htm") && !st.hasQuestItems(SCEPTER_OF_TUREK))
		{
			if (st.hasQuestItems(KASMAN_LETTER_2))
				htmltext = "30501-07.htm";
			else
			{
				htmltext = "30501-06.htm";
				st.playSound(QuestState.SOUND_ITEMGET);
				st.giveItems(KASMAN_LETTER_2, 1);
			}
			st.addRadar(-94294, 110818, -3563);
		}
		else if (event.equalsIgnoreCase("30501-08.htm") && !st.hasQuestItems(SCEPTER_OF_TUNATH))
		{
			if (st.hasQuestItems(KASMAN_LETTER_3))
				htmltext = "30501-10.htm";
			else
			{
				htmltext = "30501-09.htm";
				st.playSound(QuestState.SOUND_ITEMGET);
				st.giveItems(KASMAN_LETTER_3, 1);
			}
			st.addRadar(-55217, 200628, -3724);
		}
		// MANAKIA
		else if (event.equalsIgnoreCase("30515-02.htm") && !st.hasQuestItems(SCEPTER_OF_BREKA))
		{
			if (st.hasQuestItems(MANAKIA_LETTER_1))
				htmltext = "30515-04.htm";
			else
			{
				htmltext = "30515-03.htm";
				st.playSound(QuestState.SOUND_ITEMGET);
				st.giveItems(MANAKIA_LETTER_1, 1);
			}
			st.addRadar(80100, 119991, -2264);
		}
		else if (event.equalsIgnoreCase("30515-05.htm") && !st.hasQuestItems(SCEPTER_OF_ENKU))
		{
			if (st.hasQuestItems(MANAKIA_LETTER_2))
				htmltext = "30515-07.htm";
			else
			{
				htmltext = "30515-06.htm";
				st.playSound(QuestState.SOUND_ITEMGET);
				st.giveItems(MANAKIA_LETTER_2, 1);
			}
			st.addRadar(19815, 189703, -3032);
		}
		// VOLTAR
		else if (event.equalsIgnoreCase("30615-04.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(MANAKIA_LETTER_1, 1);
			st.giveItems(GLOVE_OF_VOLTAR, 1);
			
			if (!_sonsOfVoltar)
			{
				addSpawn(PASHIKA_SON_OF_VOLTAR, 80117, 120039, -2259, 0, false, 200000, true);
				addSpawn(VULTUS_SON_OF_VOLTAR, 80058, 120038, -2259, 0, false, 200000, true);
				_sonsOfVoltar = true;
				
				// Resets Sons Of Voltar
				startQuestTimer("voltar_sons_cleanup", 201000, null, player, false);
			}
		}
		// KEPRA
		else if (event.equalsIgnoreCase("30616-05.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(MANAKIA_LETTER_2, 1);
			st.giveItems(GLOVE_OF_KEPRA, 1);
			
			if (!_enkuOrcOverlords)
			{
				addSpawn(ENKU_ORC_OVERLORD, 19894, 189743, -3074, 0, false, 200000, true);
				addSpawn(ENKU_ORC_OVERLORD, 19869, 189800, -3059, 0, false, 200000, true);
				addSpawn(ENKU_ORC_OVERLORD, 19818, 189818, -3047, 0, false, 200000, true);
				addSpawn(ENKU_ORC_OVERLORD, 19753, 189837, -3027, 0, false, 200000, true);
				_enkuOrcOverlords = true;
				
				// Resets Enku Orc Overlords
				startQuestTimer("enku_orcs_cleanup", 201000, null, player, false);
			}
		}
		// BURAI
		else if (event.equalsIgnoreCase("30617-04.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(KASMAN_LETTER_2, 1);
			st.giveItems(GLOVE_OF_BURAI, 1);
			
			if (!_makumBugbearThugs)
			{
				addSpawn(MAKUM_BUGBEAR_THUG, -94292, 110781, -3701, 0, false, 200000, true);
				addSpawn(MAKUM_BUGBEAR_THUG, -94293, 110861, -3701, 0, false, 200000, true);
				_makumBugbearThugs = true;
				
				// Resets Makum Bugbear Thugs
				startQuestTimer("makum_bugbears_cleanup", 201000, null, player, false);
			}
		}
		// HARAK
		else if (event.equalsIgnoreCase("30618-03.htm"))
		{
			st.takeItems(KASMAN_LETTER_3, 1);
			st.giveItems(SCEPTER_OF_TUNATH, 1);
			
			if (st.hasQuestItems(SCEPTER_OF_BREKA, SCEPTER_OF_ENKU, SCEPTER_OF_VUKU, SCEPTER_OF_TUREK))
			{
				st.set("cond", "5");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				st.playSound(QuestState.SOUND_ITEMGET);
		}
		// DRIKO
		else if (event.equalsIgnoreCase("30619-03.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(KASMAN_LETTER_1, 1);
			st.giveItems(DRIKO_CONTRACT, 1);
		}
		// TANAPI
		else if (event.equalsIgnoreCase("30571-03.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(SCEPTER_BOX, 1);
			st.giveItems(TANAPI_ORDER, 1);
		}
		// Clean ups
		else if (event.equalsIgnoreCase("voltar_sons_cleanup"))
		{
			_sonsOfVoltar = false;
			return null;
		}
		else if (event.equalsIgnoreCase("enku_orcs_cleanup"))
		{
			_enkuOrcOverlords = false;
			return null;
		}
		else if (event.equalsIgnoreCase("makum_bugbears_cleanup"))
		{
			_makumBugbearThugs = false;
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
				if (player.getRace() != ClassRace.ORC)
					htmltext = "30514-01.htm";
				else if (player.getLevel() < 37)
					htmltext = "30514-02.htm";
				else if (player.getClassId().level() != 1)
					htmltext = "30514-01a.htm";
				else
					htmltext = "30514-03.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case VOKIAN:
						if (cond == 1)
							htmltext = "30514-06.htm";
						else if (cond == 2)
						{
							htmltext = "30514-08.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(GUARDIAN_BASILISK_FANG, 10);
							st.takeItems(MANASHEN_SHARD, 10);
							st.takeItems(TYRANT_TALON, 10);
							st.takeItems(VOKIAN_ORDER_1, 1);
							st.giveItems(NECKLACE_OF_AUTHORITY, 1);
							st.giveItems(VOKIAN_ORDER_2, 1);
						}
						else if (cond == 3)
							htmltext = "30514-09.htm";
						else if (cond == 8)
							htmltext = "30514-10.htm";
						break;
					
					case CHIANTA:
						if (cond == 3)
							htmltext = "30642-01.htm";
						else if (cond == 4)
							htmltext = "30642-04.htm";
						else if (cond == 5)
						{
							if (st.hasQuestItems(CHIANTA_ORDER_2))
							{
								if (player.getLevel() >= 37)
								{
									htmltext = "30642-09.htm";
									st.set("cond", "6");
									st.playSound(QuestState.SOUND_MIDDLE);
									st.takeItems(CHIANTA_ORDER_2, 1);
									st.giveItems(CHIANTA_ORDER_3, 1);
								}
								else
									htmltext = "30642-08.htm";
							}
							else
								htmltext = "30642-05.htm";
						}
						else if (cond == 6)
							htmltext = "30642-10.htm";
						else if (cond == 7)
						{
							htmltext = "30642-11.htm";
							st.set("cond", "8");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(CHIANTA_ORDER_3, 1);
							st.takeItems(NECKLACE_OF_AUTHORITY, 1);
							st.takeItems(TAMLIN_ORC_SKULL, 20);
							st.takeItems(TIMAK_ORC_HEAD, 20);
							st.giveItems(SCEPTER_BOX, 1);
						}
						else if (cond == 8)
							htmltext = "30642-12.htm";
						else if (cond > 8)
							htmltext = "30642-13.htm";
						break;
					
					case KASMAN:
						if (st.hasQuestItems(CHIANTA_ORDER_1))
							htmltext = "30501-01.htm";
						else if (cond > 4)
							htmltext = "30501-11.htm";
						break;
					
					case MANAKIA:
						if (st.hasQuestItems(CHIANTA_ORDER_1))
							htmltext = "30515-01.htm";
						else if (cond > 4)
							htmltext = "30515-08.htm";
						break;
					
					case VOLTAR:
						if (cond > 3)
						{
							if (st.hasQuestItems(MANAKIA_LETTER_1))
							{
								htmltext = "30615-02.htm";
								st.removeRadar(80100, 119991, -2264);
							}
							else if (st.hasQuestItems(GLOVE_OF_VOLTAR))
							{
								htmltext = "30615-05.htm";
								if (!_sonsOfVoltar)
								{
									addSpawn(PASHIKA_SON_OF_VOLTAR, 80117, 120039, -2259, 0, false, 200000, true);
									addSpawn(VULTUS_SON_OF_VOLTAR, 80058, 120038, -2259, 0, false, 200000, true);
									_sonsOfVoltar = true;
									
									// Resets Sons Of Voltar
									startQuestTimer("voltar_sons_cleanup", 201000, null, player, false);
								}
							}
							else if (st.hasQuestItems(PASHIKA_HEAD, VULTUS_HEAD))
							{
								htmltext = "30615-06.htm";
								st.takeItems(PASHIKA_HEAD, 1);
								st.takeItems(VULTUS_HEAD, 1);
								st.giveItems(SCEPTER_OF_BREKA, 1);
								
								if (st.hasQuestItems(SCEPTER_OF_ENKU, SCEPTER_OF_VUKU, SCEPTER_OF_TUREK, SCEPTER_OF_TUNATH))
								{
									st.set("cond", "5");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.playSound(QuestState.SOUND_ITEMGET);
							}
							else if (st.hasQuestItems(SCEPTER_OF_BREKA))
								htmltext = "30615-07.htm";
							else if (st.hasQuestItems(CHIANTA_ORDER_1))
								htmltext = "30615-01.htm";
							else if (cond < 9)
								htmltext = "30615-08.htm";
						}
						break;
					
					case KEPRA:
						if (cond > 3)
						{
							if (st.hasQuestItems(MANAKIA_LETTER_2))
							{
								htmltext = "30616-02.htm";
								st.removeRadar(19815, 189703, -3032);
							}
							else if (st.hasQuestItems(GLOVE_OF_KEPRA))
							{
								htmltext = "30616-05.htm";
								
								if (!_enkuOrcOverlords)
								{
									addSpawn(ENKU_ORC_OVERLORD, 19894, 189743, -3074, 0, false, 200000, true);
									addSpawn(ENKU_ORC_OVERLORD, 19869, 189800, -3059, 0, false, 200000, true);
									addSpawn(ENKU_ORC_OVERLORD, 19818, 189818, -3047, 0, false, 200000, true);
									addSpawn(ENKU_ORC_OVERLORD, 19753, 189837, -3027, 0, false, 200000, true);
									_enkuOrcOverlords = true;
									
									// Resets Enku Orc Overlords
									startQuestTimer("enku_orcs_cleanup", 201000, null, player, false);
								}
							}
							else if (st.getQuestItemsCount(ENKU_OVERLORD_HEAD) == 4)
							{
								htmltext = "30616-06.htm";
								st.takeItems(ENKU_OVERLORD_HEAD, 4);
								st.giveItems(SCEPTER_OF_ENKU, 1);
								
								if (st.hasQuestItems(SCEPTER_OF_BREKA, SCEPTER_OF_VUKU, SCEPTER_OF_TUREK, SCEPTER_OF_TUNATH))
								{
									st.set("cond", "5");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.playSound(QuestState.SOUND_ITEMGET);
							}
							else if (st.hasQuestItems(SCEPTER_OF_ENKU))
								htmltext = "30616-07.htm";
							else if (st.hasQuestItems(CHIANTA_ORDER_1))
								htmltext = "30616-01.htm";
							else if (cond < 9)
								htmltext = "30616-08.htm";
						}
						break;
					
					case BURAI:
						if (cond > 3)
						{
							if (st.hasQuestItems(KASMAN_LETTER_2))
							{
								htmltext = "30617-02.htm";
								st.removeRadar(-94294, 110818, -3563);
							}
							else if (st.hasQuestItems(GLOVE_OF_BURAI))
							{
								htmltext = "30617-04.htm";
								
								if (!_makumBugbearThugs)
								{
									addSpawn(MAKUM_BUGBEAR_THUG, -94292, 110781, -3701, 0, false, 200000, true);
									addSpawn(MAKUM_BUGBEAR_THUG, -94293, 110861, -3701, 0, false, 200000, true);
									_makumBugbearThugs = true;
									
									// Resets Makum Bugbear Thugs
									startQuestTimer("makum_bugbears_cleanup", 201000, null, player, false);
								}
							}
							else if (st.getQuestItemsCount(MAKUM_BUGBEAR_HEAD) == 2)
							{
								htmltext = "30617-05.htm";
								st.takeItems(MAKUM_BUGBEAR_HEAD, 2);
								st.giveItems(SCEPTER_OF_TUREK, 1);
								
								if (st.hasQuestItems(SCEPTER_OF_BREKA, SCEPTER_OF_VUKU, SCEPTER_OF_ENKU, SCEPTER_OF_TUNATH))
								{
									st.set("cond", "5");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.playSound(QuestState.SOUND_ITEMGET);
							}
							else if (st.hasQuestItems(SCEPTER_OF_TUREK))
								htmltext = "30617-06.htm";
							else if (st.hasQuestItems(CHIANTA_ORDER_1))
								htmltext = "30617-01.htm";
							else if (cond < 8)
								htmltext = "30617-07.htm";
						}
						break;
					
					case HARAK:
						if (cond > 3)
						{
							if (st.hasQuestItems(KASMAN_LETTER_3))
							{
								htmltext = "30618-02.htm";
								st.removeRadar(-55217, 200628, -3724);
							}
							else if (st.hasQuestItems(SCEPTER_OF_TUNATH))
								htmltext = "30618-04.htm";
							else if (st.hasQuestItems(CHIANTA_ORDER_1))
								htmltext = "30618-01.htm";
							else if (cond < 9)
								htmltext = "30618-05.htm";
						}
						break;
					
					case DRIKO:
						if (cond > 3)
						{
							if (st.hasQuestItems(KASMAN_LETTER_1))
							{
								htmltext = "30619-02.htm";
								st.removeRadar(-2150, 124443, -3724);
							}
							else if (st.hasQuestItems(DRIKO_CONTRACT))
							{
								if (st.getQuestItemsCount(STAKATO_DRONE_HUSK) == 30)
								{
									htmltext = "30619-05.htm";
									st.takeItems(DRIKO_CONTRACT, 1);
									st.takeItems(STAKATO_DRONE_HUSK, 30);
									st.giveItems(SCEPTER_OF_VUKU, 1);
									
									if (st.hasQuestItems(SCEPTER_OF_BREKA, SCEPTER_OF_TUREK, SCEPTER_OF_ENKU, SCEPTER_OF_TUNATH))
									{
										st.set("cond", "5");
										st.playSound(QuestState.SOUND_MIDDLE);
									}
									else
										st.playSound(QuestState.SOUND_ITEMGET);
								}
								else
									htmltext = "30619-04.htm";
							}
							else if (st.hasQuestItems(SCEPTER_OF_VUKU))
								htmltext = "30619-06.htm";
							else if (st.hasQuestItems(CHIANTA_ORDER_1))
								htmltext = "30619-01.htm";
							else if (cond < 8)
								htmltext = "30619-07.htm";
						}
						break;
					
					case TANAPI:
						if (cond == 8)
							htmltext = "30571-01.htm";
						else if (cond == 9)
							htmltext = "30571-04.htm";
						else if (cond == 10)
						{
							htmltext = "30571-05.htm";
							st.set("cond", "11");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SCEPTER_OF_TANTOS, 1);
							st.takeItems(TANAPI_ORDER, 1);
							st.giveItems(RITUAL_BOX, 1);
						}
						else if (cond == 11)
							htmltext = "30571-06.htm";
						break;
					
					case KAKAI:
						if (cond > 7 && cond < 11)
							htmltext = "30565-01.htm";
						else if (cond == 11)
						{
							htmltext = "30565-02.htm";
							st.takeItems(RITUAL_BOX, 1);
							st.giveItems(MARK_OF_GLORY, 1);
							st.rewardExpAndSp(91457, 2500);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
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
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		final Player player = attacker.getActingPlayer();
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		final int cond = st.getInt("cond");
		
		switch (npc.getNpcId())
		{
			case RAGNA_ORC_OVERLORD:
			case RAGNA_ORC_SEER:
				if (cond == 9 && npc.isScriptValue(0))
				{
					npc.broadcastNpcSay("Is it a lackey of Kakai?!");
					npc.setScriptValue(1);
				}
				break;
			
			case REVENANT_OF_TANTOS_CHIEF:
				if (cond == 9)
				{
					if (npc.isScriptValue(0))
					{
						npc.broadcastNpcSay("How regretful! Unjust dishonor!");
						npc.setScriptValue(1);
					}
					else if (npc.isScriptValue(1) && npc.getCurrentHp() / npc.getMaxHp() < 0.33)
					{
						npc.broadcastNpcSay("Indignant and unfair death!");
						npc.setScriptValue(2);
					}
				}
				break;
		}
		
		return null;
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
			case TYRANT:
				if (cond == 1 && st.dropItems(TYRANT_TALON, 1, 10, 500000) && st.getQuestItemsCount(GUARDIAN_BASILISK_FANG) + st.getQuestItemsCount(MANASHEN_SHARD) == 20)
					st.set("cond", "2");
				break;
			
			case GUARDIAN_BASILISK:
				if (cond == 1 && st.dropItems(GUARDIAN_BASILISK_FANG, 1, 10, 500000) && st.getQuestItemsCount(TYRANT_TALON) + st.getQuestItemsCount(MANASHEN_SHARD) == 20)
					st.set("cond", "2");
				break;
			
			case MANASHEN_GARGOYLE:
				if (cond == 1 && st.dropItems(MANASHEN_SHARD, 1, 10, 750000) && st.getQuestItemsCount(TYRANT_TALON) + st.getQuestItemsCount(GUARDIAN_BASILISK_FANG) == 20)
					st.set("cond", "2");
				break;
			
			case MARSH_STAKATO_DRONE:
				if (st.hasQuestItems(DRIKO_CONTRACT))
					st.dropItems(STAKATO_DRONE_HUSK, 1, 30, 750000);
				break;
			
			case PASHIKA_SON_OF_VOLTAR:
				if (st.hasQuestItems(GLOVE_OF_VOLTAR) && !st.hasQuestItems(PASHIKA_HEAD))
				{
					st.giveItems(PASHIKA_HEAD, 1);
					if (st.hasQuestItems(VULTUS_HEAD))
					{
						st.playSound(QuestState.SOUND_MIDDLE);
						st.takeItems(GLOVE_OF_VOLTAR, 1);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case VULTUS_SON_OF_VOLTAR:
				if (st.hasQuestItems(GLOVE_OF_VOLTAR) && !st.hasQuestItems(VULTUS_HEAD))
				{
					st.giveItems(VULTUS_HEAD, 1);
					if (st.hasQuestItems(PASHIKA_HEAD))
					{
						st.playSound(QuestState.SOUND_MIDDLE);
						st.takeItems(GLOVE_OF_VOLTAR, 1);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case ENKU_ORC_OVERLORD:
				if (st.hasQuestItems(GLOVE_OF_KEPRA) && st.dropItemsAlways(ENKU_OVERLORD_HEAD, 1, 4))
					st.takeItems(GLOVE_OF_KEPRA, 1);
				break;
			
			case MAKUM_BUGBEAR_THUG:
				if (st.hasQuestItems(GLOVE_OF_BURAI) && st.dropItemsAlways(MAKUM_BUGBEAR_HEAD, 1, 2))
					st.takeItems(GLOVE_OF_BURAI, 1);
				break;
			
			case TIMAK_ORC:
			case TIMAK_ORC_ARCHER:
			case TIMAK_ORC_SOLDIER:
			case TIMAK_ORC_WARRIOR:
			case TIMAK_ORC_SHAMAN:
			case TIMAK_ORC_OVERLORD:
				if (cond == 6 && st.dropItems(TIMAK_ORC_HEAD, 1, 20, 500000 + ((npc.getNpcId() - 20583) * 100000)) && st.getQuestItemsCount(TAMLIN_ORC_SKULL) == 20)
					st.set("cond", "7");
				break;
			
			case TAMLIN_ORC:
				if (cond == 6 && st.dropItems(TAMLIN_ORC_SKULL, 1, 20, 500000) && st.getQuestItemsCount(TIMAK_ORC_HEAD) == 20)
					st.set("cond", "7");
				break;
			
			case TAMLIN_ORC_ARCHER:
				if (cond == 6 && st.dropItems(TAMLIN_ORC_SKULL, 1, 20, 600000) && st.getQuestItemsCount(TIMAK_ORC_HEAD) == 20)
					st.set("cond", "7");
				break;
			
			case RAGNA_ORC_OVERLORD:
			case RAGNA_ORC_SEER:
				if (cond == 9)
				{
					npc.broadcastNpcSay("Too late!");
					addSpawn(REVENANT_OF_TANTOS_CHIEF, npc, true, 200000, true);
				}
				break;
			
			case REVENANT_OF_TANTOS_CHIEF:
				if (cond == 9 && st.dropItemsAlways(SCEPTER_OF_TANTOS, 1, 1))
				{
					st.set("cond", "10");
					npc.broadcastNpcSay("I'll get revenge someday!!");
				}
				break;
		}
		
		return null;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q229_TestOfWitchcraft extends Quest
{
	private static final String qn = "Q229_TestOfWitchcraft";
	
	// Items
	private static final int ORIM_DIAGRAM = 3308;
	private static final int ALEXANDRIA_BOOK = 3309;
	private static final int IKER_LIST = 3310;
	private static final int DIRE_WYRM_FANG = 3311;
	private static final int LETO_LIZARDMAN_CHARM = 3312;
	private static final int EN_GOLEM_HEARTSTONE = 3313;
	private static final int LARA_MEMO = 3314;
	private static final int NESTLE_MEMO = 3315;
	private static final int LEOPOLD_JOURNAL = 3316;
	private static final int AKLANTOTH_GEM_1 = 3317;
	private static final int AKLANTOTH_GEM_2 = 3318;
	private static final int AKLANTOTH_GEM_3 = 3319;
	private static final int AKLANTOTH_GEM_4 = 3320;
	private static final int AKLANTOTH_GEM_5 = 3321;
	private static final int AKLANTOTH_GEM_6 = 3322;
	private static final int BRIMSTONE_1 = 3323;
	private static final int ORIM_INSTRUCTIONS = 3324;
	private static final int ORIM_LETTER_1 = 3325;
	private static final int ORIM_LETTER_2 = 3326;
	private static final int SIR_VASPER_LETTER = 3327;
	private static final int VADIN_CRUCIFIX = 3328;
	private static final int TAMLIN_ORC_AMULET = 3329;
	private static final int VADIN_SANCTIONS = 3330;
	private static final int IKER_AMULET = 3331;
	private static final int SOULTRAP_CRYSTAL = 3332;
	private static final int PURGATORY_KEY = 3333;
	private static final int ZERUEL_BIND_CRYSTAL = 3334;
	private static final int BRIMSTONE_2 = 3335;
	private static final int SWORD_OF_BINDING = 3029;
	
	// Rewards
	private static final int MARK_OF_WITCHCRAFT = 3307;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int LARA = 30063;
	private static final int ALEXANDRIA = 30098;
	private static final int IKER = 30110;
	private static final int VADIN = 30188;
	private static final int NESTLE = 30314;
	private static final int SIR_KLAUS_VASPER = 30417;
	private static final int LEOPOLD = 30435;
	private static final int KAIRA = 30476;
	private static final int ORIM = 30630;
	private static final int RODERIK = 30631;
	private static final int ENDRIGO = 30632;
	private static final int EVERT = 30633;
	
	// Monsters
	private static final int DIRE_WYRM = 20557;
	private static final int ENCHANTED_STONE_GOLEM = 20565;
	private static final int LETO_LIZARDMAN = 20577;
	private static final int LETO_LIZARDMAN_ARCHER = 20578;
	private static final int LETO_LIZARDMAN_SOLDIER = 20579;
	private static final int LETO_LIZARDMAN_WARRIOR = 20580;
	private static final int LETO_LIZARDMAN_SHAMAN = 20581;
	private static final int LETO_LIZARDMAN_OVERLORD = 20582;
	private static final int TAMLIN_ORC = 20601;
	private static final int TAMLIN_ORC_ARCHER = 20602;
	private static final int NAMELESS_REVENANT = 27099;
	private static final int SKELETAL_MERCENARY = 27100;
	private static final int DREVANUL_PRINCE_ZERUEL = 27101;
	
	// Checks
	private static boolean _drevanulPrinceZeruel = false;
	private static boolean _swordOfBinding = false;
	
	public Q229_TestOfWitchcraft()
	{
		super(229, "Test Of Witchcraft");
		
		setItemsIds(ORIM_DIAGRAM, ALEXANDRIA_BOOK, IKER_LIST, DIRE_WYRM_FANG, LETO_LIZARDMAN_CHARM, EN_GOLEM_HEARTSTONE, LARA_MEMO, NESTLE_MEMO, LEOPOLD_JOURNAL, AKLANTOTH_GEM_1, AKLANTOTH_GEM_2, AKLANTOTH_GEM_3, AKLANTOTH_GEM_4, AKLANTOTH_GEM_5, AKLANTOTH_GEM_6, BRIMSTONE_1, ORIM_INSTRUCTIONS, ORIM_LETTER_1, ORIM_LETTER_2, SIR_VASPER_LETTER, VADIN_CRUCIFIX, TAMLIN_ORC_AMULET, VADIN_SANCTIONS, IKER_AMULET, SOULTRAP_CRYSTAL, PURGATORY_KEY, ZERUEL_BIND_CRYSTAL, BRIMSTONE_2, SWORD_OF_BINDING);
		
		addStartNpc(ORIM);
		addTalkId(LARA, ALEXANDRIA, IKER, VADIN, NESTLE, SIR_KLAUS_VASPER, LEOPOLD, KAIRA, ORIM, RODERIK, ENDRIGO, EVERT);
		
		addAttackId(NAMELESS_REVENANT, SKELETAL_MERCENARY, DREVANUL_PRINCE_ZERUEL);
		addKillId(DIRE_WYRM, ENCHANTED_STONE_GOLEM, LETO_LIZARDMAN, LETO_LIZARDMAN_ARCHER, LETO_LIZARDMAN_SOLDIER, LETO_LIZARDMAN_WARRIOR, LETO_LIZARDMAN_SHAMAN, LETO_LIZARDMAN_OVERLORD, TAMLIN_ORC, TAMLIN_ORC_ARCHER, NAMELESS_REVENANT, SKELETAL_MERCENARY, DREVANUL_PRINCE_ZERUEL);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// ORIM
		if (event.equalsIgnoreCase("30630-08.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(ORIM_DIAGRAM, 1);
			
			if (!player.getMemos().getBool("secondClassChange39", false))
			{
				htmltext = "30630-08a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39.get(player.getClassId().getId()));
				player.getMemos().set("secondClassChange39", true);
			}
		}
		else if (event.equalsIgnoreCase("30630-14.htm"))
		{
			st.set("cond", "4");
			st.unset("gem456");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(AKLANTOTH_GEM_1, 1);
			st.takeItems(AKLANTOTH_GEM_2, 1);
			st.takeItems(AKLANTOTH_GEM_3, 1);
			st.takeItems(AKLANTOTH_GEM_4, 1);
			st.takeItems(AKLANTOTH_GEM_5, 1);
			st.takeItems(AKLANTOTH_GEM_6, 1);
			st.takeItems(ALEXANDRIA_BOOK, 1);
			st.giveItems(BRIMSTONE_1, 1);
			addSpawn(DREVANUL_PRINCE_ZERUEL, 70381, 109638, -3726, 0, false, 120000, true);
		}
		else if (event.equalsIgnoreCase("30630-16.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BRIMSTONE_1, 1);
			st.giveItems(ORIM_INSTRUCTIONS, 1);
			st.giveItems(ORIM_LETTER_1, 1);
			st.giveItems(ORIM_LETTER_2, 1);
		}
		else if (event.equalsIgnoreCase("30630-22.htm"))
		{
			st.takeItems(IKER_AMULET, 1);
			st.takeItems(ORIM_INSTRUCTIONS, 1);
			st.takeItems(PURGATORY_KEY, 1);
			st.takeItems(SWORD_OF_BINDING, 1);
			st.takeItems(ZERUEL_BIND_CRYSTAL, 1);
			st.giveItems(MARK_OF_WITCHCRAFT, 1);
			st.rewardExpAndSp(139796, 40000);
			player.broadcastPacket(new SocialAction(player, 3));
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		// ALEXANDRIA
		else if (event.equalsIgnoreCase("30098-03.htm"))
		{
			st.set("cond", "2");
			st.set("gem456", "1");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ORIM_DIAGRAM, 1);
			st.giveItems(ALEXANDRIA_BOOK, 1);
		}
		// IKER
		else if (event.equalsIgnoreCase("30110-03.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(IKER_LIST, 1);
		}
		else if (event.equalsIgnoreCase("30110-08.htm"))
		{
			st.takeItems(ORIM_LETTER_2, 1);
			st.giveItems(IKER_AMULET, 1);
			st.giveItems(SOULTRAP_CRYSTAL, 1);
			
			if (st.hasQuestItems(SWORD_OF_BINDING))
			{
				st.set("cond", "7");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				st.playSound(QuestState.SOUND_ITEMGET);
		}
		// KAIRA
		else if (event.equalsIgnoreCase("30476-02.htm"))
		{
			st.giveItems(AKLANTOTH_GEM_2, 1);
			
			if (st.hasQuestItems(AKLANTOTH_GEM_1, AKLANTOTH_GEM_3) && st.getInt("gem456") == 6)
			{
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				st.playSound(QuestState.SOUND_ITEMGET);
		}
		// LARA
		else if (event.equalsIgnoreCase("30063-02.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(LARA_MEMO, 1);
		}
		// NESTLE
		else if (event.equalsIgnoreCase("30314-02.htm"))
		{
			st.set("gem456", "2");
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(NESTLE_MEMO, 1);
		}
		// LEOPOLD
		else if (event.equalsIgnoreCase("30435-02.htm"))
		{
			st.set("gem456", "3");
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(NESTLE_MEMO, 1);
			st.giveItems(LEOPOLD_JOURNAL, 1);
		}
		// SIR KLAUS VASPER
		else if (event.equalsIgnoreCase("30417-03.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(ORIM_LETTER_1, 1);
			st.giveItems(SIR_VASPER_LETTER, 1);
		}
		// EVERT
		else if (event.equalsIgnoreCase("30633-02.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(BRIMSTONE_2, 1);
			
			if (!_drevanulPrinceZeruel)
			{
				addSpawn(DREVANUL_PRINCE_ZERUEL, 13395, 169807, -3708, 0, false, 299000, true);
				_drevanulPrinceZeruel = true;
				
				// Resets Drevanul Prince Zeruel
				startQuestTimer("zeruel_cleanup", 300000, null, player, false);
			}
		}
		// Despawns Drevanul Prince Zeruel
		else if (event.equalsIgnoreCase("zeruel_despawn"))
		{
			npc.abortAttack();
			npc.decayMe();
			return null;
		}
		// Drevanul Prince Zeruel's reset
		else if (event.equalsIgnoreCase("zeruel_cleanup"))
		{
			_drevanulPrinceZeruel = false;
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
				if (player.getClassId() != ClassId.KNIGHT && player.getClassId() != ClassId.HUMAN_WIZARD && player.getClassId() != ClassId.PALUS_KNIGHT)
					htmltext = "30630-01.htm";
				else if (player.getLevel() < 39)
					htmltext = "30630-02.htm";
				else
					htmltext = (player.getClassId() == ClassId.HUMAN_WIZARD) ? "30630-03.htm" : "30630-05.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				int gem456 = st.getInt("gem456");
				
				switch (npc.getNpcId())
				{
					case ORIM:
						if (cond == 1)
							htmltext = "30630-09.htm";
						else if (cond == 2)
							htmltext = "30630-10.htm";
						else if (cond == 3)
							htmltext = "30630-11.htm";
						else if (cond == 4)
							htmltext = "30630-14.htm";
						else if (cond == 5)
							htmltext = "30630-15.htm";
						else if (cond == 6)
							htmltext = "30630-17.htm";
						else if (cond == 7)
						{
							htmltext = "30630-18.htm";
							st.set("cond", "8");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 8 || cond == 9)
							htmltext = "30630-18.htm";
						else if (cond == 10)
							htmltext = "30630-19.htm";
						break;
					
					case ALEXANDRIA:
						if (cond == 1)
							htmltext = "30098-01.htm";
						else if (cond == 2)
							htmltext = "30098-04.htm";
						else
							htmltext = "30098-05.htm";
						break;
					
					case KAIRA:
						if (st.hasQuestItems(AKLANTOTH_GEM_2))
							htmltext = "30476-03.htm";
						else if (cond == 2)
							htmltext = "30476-01.htm";
						else if (cond > 3)
							htmltext = "30476-04.htm";
						break;
					
					case IKER:
						if (st.hasQuestItems(AKLANTOTH_GEM_1))
							htmltext = "30110-06.htm";
						else if (st.hasQuestItems(IKER_LIST))
						{
							if (st.getQuestItemsCount(DIRE_WYRM_FANG) + st.getQuestItemsCount(LETO_LIZARDMAN_CHARM) + st.getQuestItemsCount(EN_GOLEM_HEARTSTONE) < 60)
								htmltext = "30110-04.htm";
							else
							{
								htmltext = "30110-05.htm";
								st.takeItems(IKER_LIST, 1);
								st.takeItems(DIRE_WYRM_FANG, -1);
								st.takeItems(EN_GOLEM_HEARTSTONE, -1);
								st.takeItems(LETO_LIZARDMAN_CHARM, -1);
								st.giveItems(AKLANTOTH_GEM_1, 1);
								
								if (st.hasQuestItems(AKLANTOTH_GEM_2, AKLANTOTH_GEM_3) && gem456 == 6)
								{
									st.set("cond", "3");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.playSound(QuestState.SOUND_ITEMGET);
							}
						}
						else if (cond == 2)
							htmltext = "30110-01.htm";
						else if (cond == 6 && !st.hasQuestItems(SOULTRAP_CRYSTAL))
							htmltext = "30110-07.htm";
						else if (cond >= 6 && cond < 10)
							htmltext = "30110-09.htm";
						else if (cond == 10)
							htmltext = "30110-10.htm";
						break;
					
					case LARA:
						if (st.hasQuestItems(AKLANTOTH_GEM_3))
							htmltext = "30063-04.htm";
						else if (st.hasQuestItems(LARA_MEMO))
							htmltext = "30063-03.htm";
						else if (cond == 2)
							htmltext = "30063-01.htm";
						else if (cond > 2)
							htmltext = "30063-05.htm";
						break;
					
					case RODERIK:
					case ENDRIGO:
						if (st.hasAtLeastOneQuestItem(LARA_MEMO, AKLANTOTH_GEM_3))
							htmltext = npc.getNpcId() + "-01.htm";
						break;
					
					case NESTLE:
						if (gem456 == 1)
							htmltext = "30314-01.htm";
						else if (gem456 == 2)
							htmltext = "30314-03.htm";
						else if (gem456 > 2)
							htmltext = "30314-04.htm";
						break;
					
					case LEOPOLD:
						if (gem456 == 2)
							htmltext = "30435-01.htm";
						else if (gem456 > 2 && gem456 < 6)
							htmltext = "30435-03.htm";
						else if (gem456 == 6)
							htmltext = "30435-04.htm";
						else if (cond > 3)
							htmltext = "30435-05.htm";
						break;
					
					case SIR_KLAUS_VASPER:
						if (st.hasAtLeastOneQuestItem(SIR_VASPER_LETTER, VADIN_CRUCIFIX))
							htmltext = "30417-04.htm";
						else if (st.hasQuestItems(VADIN_SANCTIONS))
						{
							htmltext = "30417-05.htm";
							st.takeItems(VADIN_SANCTIONS, 1);
							st.giveItems(SWORD_OF_BINDING, 1);
							
							if (st.hasQuestItems(SOULTRAP_CRYSTAL))
							{
								st.set("cond", "7");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
								st.playSound(QuestState.SOUND_ITEMGET);
						}
						else if (cond == 6)
							htmltext = "30417-01.htm";
						else if (cond > 6)
							htmltext = "30417-06.htm";
						break;
					
					case VADIN:
						if (st.hasQuestItems(SIR_VASPER_LETTER))
						{
							htmltext = "30188-01.htm";
							st.playSound(QuestState.SOUND_ITEMGET);
							st.takeItems(SIR_VASPER_LETTER, 1);
							st.giveItems(VADIN_CRUCIFIX, 1);
						}
						else if (st.hasQuestItems(VADIN_CRUCIFIX))
						{
							if (st.getQuestItemsCount(TAMLIN_ORC_AMULET) < 20)
								htmltext = "30188-02.htm";
							else
							{
								htmltext = "30188-03.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(TAMLIN_ORC_AMULET, -1);
								st.takeItems(VADIN_CRUCIFIX, -1);
								st.giveItems(VADIN_SANCTIONS, 1);
							}
						}
						else if (st.hasQuestItems(VADIN_SANCTIONS))
							htmltext = "30188-04.htm";
						else if (cond > 6)
							htmltext = "30188-05.htm";
						break;
					
					case EVERT:
						if (cond == 7 || cond == 8)
							htmltext = "30633-01.htm";
						else if (cond == 9)
						{
							htmltext = "30633-02.htm";
							
							if (!_drevanulPrinceZeruel)
							{
								addSpawn(DREVANUL_PRINCE_ZERUEL, 13395, 169807, -3708, 0, false, 299000, true);
								_drevanulPrinceZeruel = true;
								
								// Resets Drevanul Prince Zeruel
								startQuestTimer("zeruel_cleanup", 300000, null, player, false);
							}
						}
						else if (cond == 10)
							htmltext = "30633-03.htm";
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
		
		int cond = st.getInt("cond");
		
		switch (npc.getNpcId())
		{
			case NAMELESS_REVENANT:
				if (st.hasQuestItems(LARA_MEMO) && !npc.isScriptValue(1))
				{
					npc.setScriptValue(1);
					npc.broadcastNpcSay("I absolutely cannot give it to you! It is my precious jewel!");
				}
				break;
			
			case SKELETAL_MERCENARY:
				if (st.getInt("gem456") > 2 && st.getInt("gem456") < 6 && !npc.isScriptValue(1))
				{
					npc.setScriptValue(1);
					npc.broadcastNpcSay("I absolutely cannot give it to you! It is my precious jewel!");
				}
				break;
			
			case DREVANUL_PRINCE_ZERUEL:
				if (cond == 4 && !npc.isScriptValue(1))
				{
					st.set("cond", "5");
					st.playSound(QuestState.SOUND_MIDDLE);
					
					npc.setScriptValue(1);
					npc.broadcastNpcSay("I'll take your lives later!!");
					
					startQuestTimer("zeruel_despawn", 1000, npc, player, false);
				}
				else if (cond == 9 && _drevanulPrinceZeruel)
				{
					if (st.getItemEquipped(7) == SWORD_OF_BINDING)
					{
						_swordOfBinding = true;
						
						if (!npc.isScriptValue(1))
						{
							npc.setScriptValue(1);
							npc.broadcastNpcSay("That sword is really...!");
						}
					}
					else
						_swordOfBinding = false;
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
		
		int cond = st.getInt("cond");
		
		switch (npc.getNpcId())
		{
			case DIRE_WYRM:
				if (st.hasQuestItems(IKER_LIST))
					st.dropItemsAlways(DIRE_WYRM_FANG, 1, 20);
				break;
			
			case ENCHANTED_STONE_GOLEM:
				if (st.hasQuestItems(IKER_LIST))
					st.dropItemsAlways(EN_GOLEM_HEARTSTONE, 1, 20);
				break;
			
			case LETO_LIZARDMAN:
			case LETO_LIZARDMAN_ARCHER:
				if (st.hasQuestItems(IKER_LIST))
					st.dropItems(LETO_LIZARDMAN_CHARM, 1, 20, 500000);
				break;
			case LETO_LIZARDMAN_SOLDIER:
			case LETO_LIZARDMAN_WARRIOR:
				if (st.hasQuestItems(IKER_LIST))
					st.dropItems(LETO_LIZARDMAN_CHARM, 1, 20, 600000);
				break;
			case LETO_LIZARDMAN_SHAMAN:
			case LETO_LIZARDMAN_OVERLORD:
				if (st.hasQuestItems(IKER_LIST))
					st.dropItems(LETO_LIZARDMAN_CHARM, 1, 20, 700000);
				break;
			
			case NAMELESS_REVENANT:
				if (st.hasQuestItems(LARA_MEMO))
				{
					st.takeItems(LARA_MEMO, 1);
					st.giveItems(AKLANTOTH_GEM_3, 1);
					
					if (st.hasQuestItems(AKLANTOTH_GEM_1, AKLANTOTH_GEM_2) && st.getInt("gem456") == 6)
					{
						st.set("cond", "3");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case SKELETAL_MERCENARY:
				int gem456 = st.getInt("gem456");
				if (gem456 == 3)
				{
					st.set("gem456", "4");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.giveItems(AKLANTOTH_GEM_4, 1);
				}
				else if (gem456 == 4)
				{
					st.set("gem456", "5");
					st.playSound(QuestState.SOUND_ITEMGET);
					st.giveItems(AKLANTOTH_GEM_5, 1);
				}
				else if (gem456 == 5)
				{
					st.set("gem456", "6");
					st.takeItems(LEOPOLD_JOURNAL, 1);
					st.giveItems(AKLANTOTH_GEM_6, 1);
					
					if (st.hasQuestItems(AKLANTOTH_GEM_1, AKLANTOTH_GEM_2, AKLANTOTH_GEM_3))
					{
						st.set("cond", "3");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case TAMLIN_ORC:
			case TAMLIN_ORC_ARCHER:
				if (st.hasQuestItems(VADIN_CRUCIFIX))
					st.dropItems(TAMLIN_ORC_AMULET, 1, 20, 500000);
				break;
			
			case DREVANUL_PRINCE_ZERUEL:
				if (cond == 9 && _drevanulPrinceZeruel)
				{
					if (_swordOfBinding)
					{
						st.set("cond", "10");
						st.playSound(QuestState.SOUND_ITEMGET);
						st.takeItems(BRIMSTONE_2, 1);
						st.takeItems(SOULTRAP_CRYSTAL, 1);
						st.giveItems(PURGATORY_KEY, 1);
						st.giveItems(ZERUEL_BIND_CRYSTAL, 1);
						npc.broadcastNpcSay("No! I haven't completely finished the command for destruction and slaughter yet!!!");
					}
					cancelQuestTimer("zeruel_cleanup", null, player);
					_drevanulPrinceZeruel = false;
				}
				break;
		}
		
		return null;
	}
}
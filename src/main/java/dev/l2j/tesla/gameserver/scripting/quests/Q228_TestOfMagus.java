package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q228_TestOfMagus extends Quest
{
	private static final String qn = "Q228_TestOfMagus";
	
	// Items
	private static final int RUKAL_LETTER = 2841;
	private static final int PARINA_LETTER = 2842;
	private static final int LILAC_CHARM = 2843;
	private static final int GOLDEN_SEED_1 = 2844;
	private static final int GOLDEN_SEED_2 = 2845;
	private static final int GOLDEN_SEED_3 = 2846;
	private static final int SCORE_OF_ELEMENTS = 2847;
	private static final int DAZZLING_DROP = 2848;
	private static final int FLAME_CRYSTAL = 2849;
	private static final int HARPY_FEATHER = 2850;
	private static final int WYRM_WINGBONE = 2851;
	private static final int WINDSUS_MANE = 2852;
	private static final int EN_MONSTEREYE_SHELL = 2853;
	private static final int EN_STONEGOLEM_POWDER = 2854;
	private static final int EN_IRONGOLEM_SCRAP = 2855;
	private static final int TONE_OF_WATER = 2856;
	private static final int TONE_OF_FIRE = 2857;
	private static final int TONE_OF_WIND = 2858;
	private static final int TONE_OF_EARTH = 2859;
	private static final int SALAMANDER_CHARM = 2860;
	private static final int SYLPH_CHARM = 2861;
	private static final int UNDINE_CHARM = 2862;
	private static final int SERPENT_CHARM = 2863;
	
	// Rewards
	private static final int MARK_OF_MAGUS = 2840;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int PARINA = 30391;
	private static final int EARTH_SNAKE = 30409;
	private static final int FLAME_SALAMANDER = 30411;
	private static final int WIND_SYLPH = 30412;
	private static final int WATER_UNDINE = 30413;
	private static final int CASIAN = 30612;
	private static final int RUKAL = 30629;
	
	// Monsters
	private static final int HARPY = 20145;
	private static final int MARSH_STAKATO = 20157;
	private static final int WYRM = 20176;
	private static final int MARSH_STAKATO_WORKER = 20230;
	private static final int TOAD_LORD = 20231;
	private static final int MARSH_STAKATO_SOLDIER = 20232;
	private static final int MARSH_STAKATO_DRONE = 20234;
	private static final int WINDSUS = 20553;
	private static final int ENCHANTED_MONSTEREYE = 20564;
	private static final int ENCHANTED_STONE_GOLEM = 20565;
	private static final int ENCHANTED_IRON_GOLEM = 20566;
	private static final int SINGING_FLOWER_PHANTASM = 27095;
	private static final int SINGING_FLOWER_NIGHTMARE = 27096;
	private static final int SINGING_FLOWER_DARKLING = 27097;
	private static final int GHOST_FIRE = 27098;
	
	public Q228_TestOfMagus()
	{
		super(228, "Test Of Magus");
		
		setItemsIds(RUKAL_LETTER, PARINA_LETTER, LILAC_CHARM, GOLDEN_SEED_1, GOLDEN_SEED_2, GOLDEN_SEED_3, SCORE_OF_ELEMENTS, DAZZLING_DROP, FLAME_CRYSTAL, HARPY_FEATHER, WYRM_WINGBONE, WINDSUS_MANE, EN_MONSTEREYE_SHELL, EN_STONEGOLEM_POWDER, EN_IRONGOLEM_SCRAP, TONE_OF_WATER, TONE_OF_FIRE, TONE_OF_WIND, TONE_OF_EARTH, SALAMANDER_CHARM, SYLPH_CHARM, UNDINE_CHARM, SERPENT_CHARM);
		
		addStartNpc(RUKAL);
		addTalkId(PARINA, EARTH_SNAKE, FLAME_SALAMANDER, WIND_SYLPH, WATER_UNDINE, CASIAN, RUKAL);
		
		addKillId(HARPY, MARSH_STAKATO, WYRM, MARSH_STAKATO_WORKER, TOAD_LORD, MARSH_STAKATO_SOLDIER, MARSH_STAKATO_DRONE, WINDSUS, ENCHANTED_MONSTEREYE, ENCHANTED_STONE_GOLEM, ENCHANTED_IRON_GOLEM, SINGING_FLOWER_PHANTASM, SINGING_FLOWER_NIGHTMARE, SINGING_FLOWER_DARKLING, GHOST_FIRE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// RUKAL
		if (event.equalsIgnoreCase("30629-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(RUKAL_LETTER, 1);
			
			if (!player.getMemos().getBool("secondClassChange39", false))
			{
				htmltext = "30629-04a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39.get(player.getClassId().getId()));
				player.getMemos().set("secondClassChange39", true);
			}
		}
		else if (event.equalsIgnoreCase("30629-10.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(GOLDEN_SEED_1, 1);
			st.takeItems(GOLDEN_SEED_2, 1);
			st.takeItems(GOLDEN_SEED_3, 1);
			st.takeItems(LILAC_CHARM, 1);
			st.giveItems(SCORE_OF_ELEMENTS, 1);
		}
		// PARINA
		else if (event.equalsIgnoreCase("30391-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(RUKAL_LETTER, 1);
			st.giveItems(PARINA_LETTER, 1);
		}
		// CASIAN
		else if (event.equalsIgnoreCase("30612-02.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(PARINA_LETTER, 1);
			st.giveItems(LILAC_CHARM, 1);
		}
		// WIND SYLPH
		else if (event.equalsIgnoreCase("30412-02.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(SYLPH_CHARM, 1);
		}
		// EARTH SNAKE
		else if (event.equalsIgnoreCase("30409-03.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(SERPENT_CHARM, 1);
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
				if (player.getClassId() != ClassId.HUMAN_WIZARD && player.getClassId() != ClassId.ELVEN_WIZARD && player.getClassId() != ClassId.DARK_WIZARD)
					htmltext = "30629-01.htm";
				else if (player.getLevel() < 39)
					htmltext = "30629-02.htm";
				else
					htmltext = "30629-03.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case RUKAL:
						if (cond == 1)
							htmltext = "30629-05.htm";
						else if (cond == 2)
							htmltext = "30629-06.htm";
						else if (cond == 3)
							htmltext = "30629-07.htm";
						else if (cond == 4)
							htmltext = "30629-08.htm";
						else if (cond == 5)
							htmltext = "30629-11.htm";
						else if (cond == 6)
						{
							htmltext = "30629-12.htm";
							st.takeItems(SCORE_OF_ELEMENTS, 1);
							st.takeItems(TONE_OF_EARTH, 1);
							st.takeItems(TONE_OF_FIRE, 1);
							st.takeItems(TONE_OF_WATER, 1);
							st.takeItems(TONE_OF_WIND, 1);
							st.giveItems(MARK_OF_MAGUS, 1);
							st.rewardExpAndSp(139039, 40000);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case PARINA:
						if (cond == 1)
							htmltext = "30391-01.htm";
						else if (cond == 2)
							htmltext = "30391-03.htm";
						else if (cond == 3 || cond == 4)
							htmltext = "30391-04.htm";
						else if (cond > 4)
							htmltext = "30391-05.htm";
						break;
					
					case CASIAN:
						if (cond == 2)
							htmltext = "30612-01.htm";
						else if (cond == 3)
							htmltext = "30612-03.htm";
						else if (cond == 4)
							htmltext = "30612-04.htm";
						else if (cond > 4)
							htmltext = "30612-05.htm";
						break;
					
					case WATER_UNDINE:
						if (cond == 5)
						{
							if (st.hasQuestItems(UNDINE_CHARM))
							{
								if (st.getQuestItemsCount(DAZZLING_DROP) < 20)
									htmltext = "30413-02.htm";
								else
								{
									htmltext = "30413-03.htm";
									st.takeItems(DAZZLING_DROP, 20);
									st.takeItems(UNDINE_CHARM, 1);
									st.giveItems(TONE_OF_WATER, 1);
									
									if (st.hasQuestItems(TONE_OF_FIRE, TONE_OF_WIND, TONE_OF_EARTH))
									{
										st.set("cond", "6");
										st.playSound(QuestState.SOUND_MIDDLE);
									}
									else
										st.playSound(QuestState.SOUND_ITEMGET);
								}
							}
							else if (!st.hasQuestItems(TONE_OF_WATER))
							{
								htmltext = "30413-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.giveItems(UNDINE_CHARM, 1);
							}
							else
								htmltext = "30413-04.htm";
						}
						else if (cond == 6)
							htmltext = "30413-04.htm";
						break;
					
					case FLAME_SALAMANDER:
						if (cond == 5)
						{
							if (st.hasQuestItems(SALAMANDER_CHARM))
							{
								if (st.getQuestItemsCount(FLAME_CRYSTAL) < 5)
									htmltext = "30411-02.htm";
								else
								{
									htmltext = "30411-03.htm";
									st.takeItems(FLAME_CRYSTAL, 5);
									st.takeItems(SALAMANDER_CHARM, 1);
									st.giveItems(TONE_OF_FIRE, 1);
									
									if (st.hasQuestItems(TONE_OF_WATER, TONE_OF_WIND, TONE_OF_EARTH))
									{
										st.set("cond", "6");
										st.playSound(QuestState.SOUND_MIDDLE);
									}
									else
										st.playSound(QuestState.SOUND_ITEMGET);
								}
							}
							else if (!st.hasQuestItems(TONE_OF_FIRE))
							{
								htmltext = "30411-01.htm";
								st.giveItems(SALAMANDER_CHARM, 1);
							}
							else
								htmltext = "30411-04.htm";
						}
						else if (cond == 6)
							htmltext = "30411-04.htm";
						break;
					
					case WIND_SYLPH:
						if (cond == 5)
						{
							if (st.hasQuestItems(SYLPH_CHARM))
							{
								if (st.getQuestItemsCount(HARPY_FEATHER) + st.getQuestItemsCount(WYRM_WINGBONE) + st.getQuestItemsCount(WINDSUS_MANE) < 40)
									htmltext = "30412-03.htm";
								else
								{
									htmltext = "30412-04.htm";
									st.takeItems(HARPY_FEATHER, 20);
									st.takeItems(SYLPH_CHARM, 1);
									st.takeItems(WINDSUS_MANE, 10);
									st.takeItems(WYRM_WINGBONE, 10);
									st.giveItems(TONE_OF_WIND, 1);
									
									if (st.hasQuestItems(TONE_OF_WATER, TONE_OF_FIRE, TONE_OF_EARTH))
									{
										st.set("cond", "6");
										st.playSound(QuestState.SOUND_MIDDLE);
									}
									else
										st.playSound(QuestState.SOUND_ITEMGET);
								}
							}
							else if (!st.hasQuestItems(TONE_OF_WIND))
								htmltext = "30412-01.htm";
							else
								htmltext = "30412-05.htm";
						}
						else if (cond == 6)
							htmltext = "30412-05.htm";
						break;
					
					case EARTH_SNAKE:
						if (cond == 5)
						{
							if (st.hasQuestItems(SERPENT_CHARM))
							{
								if (st.getQuestItemsCount(EN_MONSTEREYE_SHELL) + st.getQuestItemsCount(EN_STONEGOLEM_POWDER) + st.getQuestItemsCount(EN_IRONGOLEM_SCRAP) < 30)
									htmltext = "30409-04.htm";
								else
								{
									htmltext = "30409-05.htm";
									st.takeItems(EN_IRONGOLEM_SCRAP, 10);
									st.takeItems(EN_MONSTEREYE_SHELL, 10);
									st.takeItems(EN_STONEGOLEM_POWDER, 10);
									st.takeItems(SERPENT_CHARM, 1);
									st.giveItems(TONE_OF_EARTH, 1);
									
									if (st.hasQuestItems(TONE_OF_WATER, TONE_OF_FIRE, TONE_OF_WIND))
									{
										st.set("cond", "6");
										st.playSound(QuestState.SOUND_MIDDLE);
									}
									else
										st.playSound(QuestState.SOUND_ITEMGET);
								}
							}
							else if (!st.hasQuestItems(TONE_OF_EARTH))
								htmltext = "30409-01.htm";
							else
								htmltext = "30409-06.htm";
						}
						else if (cond == 6)
							htmltext = "30409-06.htm";
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
		
		int cond = st.getInt("cond");
		
		if (cond == 3)
		{
			switch (npc.getNpcId())
			{
				case SINGING_FLOWER_PHANTASM:
					if (!st.hasQuestItems(GOLDEN_SEED_1))
					{
						npc.broadcastNpcSay("I am a tree of nothing... a tree... that knows where to return...");
						st.dropItemsAlways(GOLDEN_SEED_1, 1, 1);
						if (st.hasQuestItems(GOLDEN_SEED_2, GOLDEN_SEED_3))
							st.set("cond", "4");
					}
					break;
				
				case SINGING_FLOWER_NIGHTMARE:
					if (!st.hasQuestItems(GOLDEN_SEED_2))
					{
						npc.broadcastNpcSay("I am a creature that shows the truth of the place deep in my heart...");
						st.dropItemsAlways(GOLDEN_SEED_2, 1, 1);
						if (st.hasQuestItems(GOLDEN_SEED_1, GOLDEN_SEED_3))
							st.set("cond", "4");
					}
					break;
				
				case SINGING_FLOWER_DARKLING:
					if (!st.hasQuestItems(GOLDEN_SEED_3))
					{
						npc.broadcastNpcSay("I am a mirror of darkness... a virtual image of darkness...");
						st.dropItemsAlways(GOLDEN_SEED_3, 1, 1);
						if (st.hasQuestItems(GOLDEN_SEED_1, GOLDEN_SEED_2))
							st.set("cond", "4");
					}
					break;
			}
		}
		else if (cond == 5)
		{
			switch (npc.getNpcId())
			{
				case GHOST_FIRE:
					if (st.hasQuestItems(SALAMANDER_CHARM))
						st.dropItems(FLAME_CRYSTAL, 1, 5, 500000);
					break;
				
				case TOAD_LORD:
				case MARSH_STAKATO:
				case MARSH_STAKATO_WORKER:
					if (st.hasQuestItems(UNDINE_CHARM))
						st.dropItems(DAZZLING_DROP, 1, 20, 300000);
					break;
				
				case MARSH_STAKATO_SOLDIER:
					if (st.hasQuestItems(UNDINE_CHARM))
						st.dropItems(DAZZLING_DROP, 1, 20, 400000);
					break;
				
				case MARSH_STAKATO_DRONE:
					if (st.hasQuestItems(UNDINE_CHARM))
						st.dropItems(DAZZLING_DROP, 1, 20, 500000);
					break;
				
				case HARPY:
					if (st.hasQuestItems(SYLPH_CHARM))
						st.dropItemsAlways(HARPY_FEATHER, 1, 20);
					break;
				
				case WYRM:
					if (st.hasQuestItems(SYLPH_CHARM))
						st.dropItems(WYRM_WINGBONE, 1, 10, 500000);
					break;
				
				case WINDSUS:
					if (st.hasQuestItems(SYLPH_CHARM))
						st.dropItems(WINDSUS_MANE, 1, 10, 500000);
					break;
				
				case ENCHANTED_MONSTEREYE:
					if (st.hasQuestItems(SERPENT_CHARM))
						st.dropItemsAlways(EN_MONSTEREYE_SHELL, 1, 10);
					break;
				
				case ENCHANTED_STONE_GOLEM:
					if (st.hasQuestItems(SERPENT_CHARM))
						st.dropItemsAlways(EN_STONEGOLEM_POWDER, 1, 10);
					break;
				
				case ENCHANTED_IRON_GOLEM:
					if (st.hasQuestItems(SERPENT_CHARM))
						st.dropItemsAlways(EN_IRONGOLEM_SCRAP, 1, 10);
					break;
			}
		}
		
		return null;
	}
}
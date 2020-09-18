package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q330_AdeptOfTaste extends Quest
{
	private static final String qn = "Q330_AdeptOfTaste";
	
	// NPCs
	private static final int SONIA = 30062;
	private static final int GLYVKA = 30067;
	private static final int ROLLANT = 30069;
	private static final int JACOB = 30073;
	private static final int PANO = 30078;
	private static final int MIRIEN = 30461;
	private static final int JONAS = 30469;
	
	// Items
	private static final int INGREDIENT_LIST = 1420;
	private static final int SONIA_BOTANY_BOOK = 1421;
	private static final int RED_MANDRAGORA_ROOT = 1422;
	private static final int WHITE_MANDRAGORA_ROOT = 1423;
	private static final int RED_MANDRAGORA_SAP = 1424;
	private static final int WHITE_MANDRAGORA_SAP = 1425;
	private static final int JACOB_INSECT_BOOK = 1426;
	private static final int NECTAR = 1427;
	private static final int ROYAL_JELLY = 1428;
	private static final int HONEY = 1429;
	private static final int GOLDEN_HONEY = 1430;
	private static final int PANO_CONTRACT = 1431;
	private static final int HOBGOBLIN_AMULET = 1432;
	private static final int DIONIAN_POTATO = 1433;
	private static final int GLYVKA_BOTANY_BOOK = 1434;
	private static final int GREEN_MARSH_MOSS = 1435;
	private static final int BROWN_MARSH_MOSS = 1436;
	private static final int GREEN_MOSS_BUNDLE = 1437;
	private static final int BROWN_MOSS_BUNDLE = 1438;
	private static final int ROLANT_CREATURE_BOOK = 1439;
	private static final int MONSTER_EYE_BODY = 1440;
	private static final int MONSTER_EYE_MEAT = 1441;
	private static final int JONAS_STEAK_DISH_1 = 1442;
	private static final int JONAS_STEAK_DISH_2 = 1443;
	private static final int JONAS_STEAK_DISH_3 = 1444;
	private static final int JONAS_STEAK_DISH_4 = 1445;
	private static final int JONAS_STEAK_DISH_5 = 1446;
	private static final int MIRIEN_REVIEW_1 = 1447;
	private static final int MIRIEN_REVIEW_2 = 1448;
	private static final int MIRIEN_REVIEW_3 = 1449;
	private static final int MIRIEN_REVIEW_4 = 1450;
	private static final int MIRIEN_REVIEW_5 = 1451;
	
	// Rewards
	private static final int JONAS_SALAD_RECIPE = 1455;
	private static final int JONAS_SAUCE_RECIPE = 1456;
	private static final int JONAS_STEAK_RECIPE = 1457;
	
	// Drop chances
	private static final Map<Integer, int[]> CHANCES = new HashMap<>();
	{
		CHANCES.put(20204, new int[]
		{
			92,
			100
		});
		CHANCES.put(20229, new int[]
		{
			80,
			95
		});
		CHANCES.put(20223, new int[]
		{
			70,
			77
		});
		CHANCES.put(20154, new int[]
		{
			70,
			77
		});
		CHANCES.put(20155, new int[]
		{
			87,
			96
		});
		CHANCES.put(20156, new int[]
		{
			77,
			85
		});
	}
	
	public Q330_AdeptOfTaste()
	{
		super(330, "Adept of Taste");
		
		setItemsIds(INGREDIENT_LIST, RED_MANDRAGORA_SAP, WHITE_MANDRAGORA_SAP, HONEY, GOLDEN_HONEY, DIONIAN_POTATO, GREEN_MOSS_BUNDLE, BROWN_MOSS_BUNDLE, MONSTER_EYE_MEAT, MIRIEN_REVIEW_1, MIRIEN_REVIEW_2, MIRIEN_REVIEW_3, MIRIEN_REVIEW_4, MIRIEN_REVIEW_5, JONAS_STEAK_DISH_1, JONAS_STEAK_DISH_2, JONAS_STEAK_DISH_3, JONAS_STEAK_DISH_4, JONAS_STEAK_DISH_5, SONIA_BOTANY_BOOK, RED_MANDRAGORA_ROOT, WHITE_MANDRAGORA_ROOT, JACOB_INSECT_BOOK, NECTAR, ROYAL_JELLY, PANO_CONTRACT, HOBGOBLIN_AMULET, GLYVKA_BOTANY_BOOK, GREEN_MARSH_MOSS, BROWN_MARSH_MOSS, ROLANT_CREATURE_BOOK, MONSTER_EYE_BODY);
		
		addStartNpc(JONAS); // Jonas
		addTalkId(JONAS, SONIA, GLYVKA, ROLLANT, JACOB, PANO, MIRIEN);
		
		addKillId(20147, 20154, 20155, 20156, 20204, 20223, 20226, 20228, 20229, 20265, 20266);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30469-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(INGREDIENT_LIST, 1);
		}
		else if (event.equalsIgnoreCase("30062-05.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(SONIA_BOTANY_BOOK, 1);
			st.takeItems(RED_MANDRAGORA_ROOT, -1);
			st.takeItems(WHITE_MANDRAGORA_ROOT, -1);
			st.giveItems(RED_MANDRAGORA_SAP, 1);
			
		}
		else if (event.equalsIgnoreCase("30073-05.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(JACOB_INSECT_BOOK, 1);
			st.takeItems(NECTAR, -1);
			st.takeItems(ROYAL_JELLY, -1);
			st.giveItems(HONEY, 1);
		}
		else if (event.equalsIgnoreCase("30067-05.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(GLYVKA_BOTANY_BOOK, 1);
			st.takeItems(GREEN_MARSH_MOSS, -1);
			st.takeItems(BROWN_MARSH_MOSS, -1);
			st.giveItems(GREEN_MOSS_BUNDLE, 1);
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
				htmltext = (player.getLevel() < 24) ? "30469-01.htm" : "30469-02.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case JONAS:
						if (st.hasQuestItems(INGREDIENT_LIST))
						{
							if (!hasAllIngredients(st))
								htmltext = "30469-04.htm";
							else
							{
								int dish;
								
								final int specialIngredientsNumber = st.getQuestItemsCount(WHITE_MANDRAGORA_SAP) + st.getQuestItemsCount(GOLDEN_HONEY) + st.getQuestItemsCount(BROWN_MOSS_BUNDLE);
								
								if (Rnd.nextBoolean())
								{
									htmltext = "30469-05t" + Integer.toString(specialIngredientsNumber + 2) + ".htm";
									dish = 1443 + specialIngredientsNumber;
								}
								else
								{
									htmltext = "30469-05t" + Integer.toString(specialIngredientsNumber + 1) + ".htm";
									dish = 1442 + specialIngredientsNumber;
								}
								
								// Sound according dish.
								st.playSound((dish == JONAS_STEAK_DISH_5) ? QuestState.SOUND_JACKPOT : QuestState.SOUND_ITEMGET);
								
								st.takeItems(INGREDIENT_LIST, 1);
								st.takeItems(RED_MANDRAGORA_SAP, 1);
								st.takeItems(WHITE_MANDRAGORA_SAP, 1);
								st.takeItems(HONEY, 1);
								st.takeItems(GOLDEN_HONEY, 1);
								st.takeItems(DIONIAN_POTATO, 1);
								st.takeItems(GREEN_MOSS_BUNDLE, 1);
								st.takeItems(BROWN_MOSS_BUNDLE, 1);
								st.takeItems(MONSTER_EYE_MEAT, 1);
								st.giveItems(dish, 1);
							}
						}
						else if (st.hasAtLeastOneQuestItem(JONAS_STEAK_DISH_1, JONAS_STEAK_DISH_2, JONAS_STEAK_DISH_3, JONAS_STEAK_DISH_4, JONAS_STEAK_DISH_5))
							htmltext = "30469-06.htm";
						else if (st.hasAtLeastOneQuestItem(MIRIEN_REVIEW_1, MIRIEN_REVIEW_2, MIRIEN_REVIEW_3, MIRIEN_REVIEW_4, MIRIEN_REVIEW_5))
						{
							if (st.hasQuestItems(MIRIEN_REVIEW_1))
							{
								htmltext = "30469-06t1.htm";
								st.takeItems(MIRIEN_REVIEW_1, 1);
								st.rewardItems(57, 7500);
								st.rewardExpAndSp(6000, 0);
							}
							else if (st.hasQuestItems(MIRIEN_REVIEW_2))
							{
								htmltext = "30469-06t2.htm";
								st.takeItems(MIRIEN_REVIEW_2, 1);
								st.rewardItems(57, 9000);
								st.rewardExpAndSp(7000, 0);
							}
							else if (st.hasQuestItems(MIRIEN_REVIEW_3))
							{
								htmltext = "30469-06t3.htm";
								st.takeItems(MIRIEN_REVIEW_3, 1);
								st.rewardItems(57, 5800);
								st.giveItems(JONAS_SALAD_RECIPE, 1);
								st.rewardExpAndSp(9000, 0);
							}
							else if (st.hasQuestItems(MIRIEN_REVIEW_4))
							{
								htmltext = "30469-06t4.htm";
								st.takeItems(MIRIEN_REVIEW_4, 1);
								st.rewardItems(57, 6800);
								st.giveItems(JONAS_SAUCE_RECIPE, 1);
								st.rewardExpAndSp(10500, 0);
							}
							else if (st.hasQuestItems(MIRIEN_REVIEW_5))
							{
								htmltext = "30469-06t5.htm";
								st.takeItems(MIRIEN_REVIEW_5, 1);
								st.rewardItems(57, 7800);
								st.giveItems(JONAS_STEAK_RECIPE, 1);
								st.rewardExpAndSp(12000, 0);
							}
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case MIRIEN:
						if (st.hasQuestItems(INGREDIENT_LIST))
							htmltext = "30461-01.htm";
						else if (st.hasAtLeastOneQuestItem(JONAS_STEAK_DISH_1, JONAS_STEAK_DISH_2, JONAS_STEAK_DISH_3, JONAS_STEAK_DISH_4, JONAS_STEAK_DISH_5))
						{
							st.playSound(QuestState.SOUND_ITEMGET);
							if (st.hasQuestItems(JONAS_STEAK_DISH_1))
							{
								htmltext = "30461-02t1.htm";
								st.takeItems(JONAS_STEAK_DISH_1, 1);
								st.giveItems(MIRIEN_REVIEW_1, 1);
							}
							else if (st.hasQuestItems(JONAS_STEAK_DISH_2))
							{
								htmltext = "30461-02t2.htm";
								st.takeItems(JONAS_STEAK_DISH_2, 1);
								st.giveItems(MIRIEN_REVIEW_2, 1);
							}
							else if (st.hasQuestItems(JONAS_STEAK_DISH_3))
							{
								htmltext = "30461-02t3.htm";
								st.takeItems(JONAS_STEAK_DISH_3, 1);
								st.giveItems(MIRIEN_REVIEW_3, 1);
							}
							else if (st.hasQuestItems(JONAS_STEAK_DISH_4))
							{
								htmltext = "30461-02t4.htm";
								st.takeItems(JONAS_STEAK_DISH_4, 1);
								st.giveItems(MIRIEN_REVIEW_4, 1);
							}
							else if (st.hasQuestItems(JONAS_STEAK_DISH_5))
							{
								htmltext = "30461-02t5.htm";
								st.takeItems(JONAS_STEAK_DISH_5, 1);
								st.giveItems(MIRIEN_REVIEW_5, 1);
							}
						}
						else if (st.hasAtLeastOneQuestItem(MIRIEN_REVIEW_1, MIRIEN_REVIEW_2, MIRIEN_REVIEW_3, MIRIEN_REVIEW_4, MIRIEN_REVIEW_5))
							htmltext = "30461-04.htm";
						break;
					
					case SONIA:
						if (!st.hasQuestItems(RED_MANDRAGORA_SAP) && !st.hasQuestItems(WHITE_MANDRAGORA_SAP))
						{
							if (!st.hasQuestItems(SONIA_BOTANY_BOOK))
							{
								htmltext = "30062-01.htm";
								st.giveItems(SONIA_BOTANY_BOOK, 1);
								st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
							{
								if (st.getQuestItemsCount(RED_MANDRAGORA_ROOT) < 40 || st.getQuestItemsCount(WHITE_MANDRAGORA_ROOT) < 40)
									htmltext = "30062-02.htm";
								else if (st.getQuestItemsCount(WHITE_MANDRAGORA_ROOT) >= 40)
								{
									htmltext = "30062-06.htm";
									st.takeItems(SONIA_BOTANY_BOOK, 1);
									st.takeItems(RED_MANDRAGORA_ROOT, -1);
									st.takeItems(WHITE_MANDRAGORA_ROOT, -1);
									st.giveItems(WHITE_MANDRAGORA_SAP, 1);
									st.playSound(QuestState.SOUND_ITEMGET);
								}
								else
									htmltext = "30062-03.htm";
							}
						}
						else
							htmltext = "30062-07.htm";
						break;
					
					case JACOB:
						if (!st.hasQuestItems(HONEY) && !st.hasQuestItems(GOLDEN_HONEY))
						{
							if (!st.hasQuestItems(JACOB_INSECT_BOOK))
							{
								htmltext = "30073-01.htm";
								st.giveItems(JACOB_INSECT_BOOK, 1);
								st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
							{
								if (st.getQuestItemsCount(NECTAR) < 20)
									htmltext = "30073-02.htm";
								else
								{
									if (st.getQuestItemsCount(ROYAL_JELLY) < 10)
										htmltext = "30073-03.htm";
									else
									{
										htmltext = "30073-06.htm";
										st.takeItems(JACOB_INSECT_BOOK, 1);
										st.takeItems(NECTAR, -1);
										st.takeItems(ROYAL_JELLY, -1);
										st.giveItems(GOLDEN_HONEY, 1);
										st.playSound(QuestState.SOUND_ITEMGET);
									}
								}
							}
						}
						else
							htmltext = "30073-07.htm";
						break;
					
					case PANO:
						if (!st.hasQuestItems(DIONIAN_POTATO))
						{
							if (!st.hasQuestItems(PANO_CONTRACT))
							{
								htmltext = "30078-01.htm";
								st.giveItems(PANO_CONTRACT, 1);
								st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
							{
								if (st.getQuestItemsCount(HOBGOBLIN_AMULET) < 30)
									htmltext = "30078-02.htm";
								else
								{
									htmltext = "30078-03.htm";
									st.takeItems(PANO_CONTRACT, 1);
									st.takeItems(HOBGOBLIN_AMULET, -1);
									st.giveItems(DIONIAN_POTATO, 1);
									st.playSound(QuestState.SOUND_ITEMGET);
								}
							}
						}
						else
							htmltext = "30078-04.htm";
						break;
					
					case GLYVKA:
						if (!st.hasQuestItems(GREEN_MOSS_BUNDLE) && !st.hasQuestItems(BROWN_MOSS_BUNDLE))
						{
							if (!st.hasQuestItems(GLYVKA_BOTANY_BOOK))
							{
								st.giveItems(GLYVKA_BOTANY_BOOK, 1);
								htmltext = "30067-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
							{
								if (st.getQuestItemsCount(GREEN_MARSH_MOSS) < 20 || st.getQuestItemsCount(BROWN_MARSH_MOSS) < 20)
									htmltext = "30067-02.htm";
								else if (st.getQuestItemsCount(BROWN_MARSH_MOSS) >= 20)
								{
									htmltext = "30067-06.htm";
									st.takeItems(GLYVKA_BOTANY_BOOK, 1);
									st.takeItems(GREEN_MARSH_MOSS, -1);
									st.takeItems(BROWN_MARSH_MOSS, -1);
									st.giveItems(BROWN_MOSS_BUNDLE, 1);
									st.playSound(QuestState.SOUND_ITEMGET);
								}
								else
									htmltext = "30067-03.htm";
							}
						}
						else
							htmltext = "30067-07.htm";
						break;
					
					case ROLLANT:
						if (!st.hasQuestItems(MONSTER_EYE_MEAT))
						{
							if (!st.hasQuestItems(ROLANT_CREATURE_BOOK))
							{
								htmltext = "30069-01.htm";
								st.giveItems(ROLANT_CREATURE_BOOK, 1);
								st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
							{
								if (st.getQuestItemsCount(MONSTER_EYE_BODY) < 30)
									htmltext = "30069-02.htm";
								else
								{
									htmltext = "30069-03.htm";
									st.takeItems(ROLANT_CREATURE_BOOK, 1);
									st.takeItems(MONSTER_EYE_BODY, -1);
									st.giveItems(MONSTER_EYE_MEAT, 1);
									st.playSound(QuestState.SOUND_ITEMGET);
								}
							}
						}
						else
							htmltext = "30069-04.htm";
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
		
		final int npcId = npc.getNpcId();
		
		switch (npcId)
		{
			case 20265:
				if (st.hasQuestItems(ROLANT_CREATURE_BOOK))
					st.dropItems(MONSTER_EYE_BODY, (Rnd.get(97) < 77) ? 2 : 3, 30, 970000);
				break;
			
			case 20266:
				if (st.hasQuestItems(ROLANT_CREATURE_BOOK))
					st.dropItemsAlways(MONSTER_EYE_BODY, (Rnd.get(10) < 7) ? 1 : 2, 30);
				break;
			
			case 20226:
				if (st.hasQuestItems(GLYVKA_BOTANY_BOOK))
					st.dropItems(((Rnd.get(96) < 87) ? GREEN_MARSH_MOSS : BROWN_MARSH_MOSS), 1, 20, 960000);
				break;
			
			case 20228:
				if (st.hasQuestItems(GLYVKA_BOTANY_BOOK))
					st.dropItemsAlways(((Rnd.get(10) < 9) ? GREEN_MARSH_MOSS : BROWN_MARSH_MOSS), 1, 20);
				break;
			
			case 20147:
				if (st.hasQuestItems(PANO_CONTRACT))
					st.dropItemsAlways(HOBGOBLIN_AMULET, 1, 30);
				break;
			
			case 20204:
			case 20229:
				if (st.hasQuestItems(JACOB_INSECT_BOOK))
				{
					final int random = Rnd.get(100);
					final int[] chances = CHANCES.get(npcId);
					if (random < chances[0])
						st.dropItemsAlways(NECTAR, 1, 20);
					else if (random < chances[1])
						st.dropItemsAlways(ROYAL_JELLY, 1, 10);
				}
				break;
			
			case 20223:
			case 20154:
			case 20155:
			case 20156:
				if (st.hasQuestItems(SONIA_BOTANY_BOOK))
				{
					final int random = Rnd.get(100);
					final int[] chances = CHANCES.get(npcId);
					if (random < chances[1])
						st.dropItemsAlways((random < chances[0]) ? RED_MANDRAGORA_ROOT : WHITE_MANDRAGORA_ROOT, 1, 40);
				}
				break;
		}
		
		return null;
	}
	
	private static boolean hasAllIngredients(QuestState st)
	{
		return st.hasQuestItems(DIONIAN_POTATO, MONSTER_EYE_MEAT) && st.hasAtLeastOneQuestItem(WHITE_MANDRAGORA_SAP, RED_MANDRAGORA_SAP) && st.hasAtLeastOneQuestItem(GOLDEN_HONEY, HONEY) && st.hasAtLeastOneQuestItem(BROWN_MOSS_BUNDLE, GREEN_MOSS_BUNDLE);
	}
}
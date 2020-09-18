package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.itemcontainer.Inventory;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q218_TestimonyOfLife extends Quest
{
	private static final String qn = "Q218_TestimonyOfLife";
	
	private static final int ASTERIOS = 30154;
	private static final int PUSHKIN = 30300;
	private static final int THALIA = 30371;
	private static final int ADONIUS = 30375;
	private static final int ARKENIA = 30419;
	private static final int CARDIEN = 30460;
	private static final int ISAEL = 30655;
	
	// Items
	private static final int TALINS_SPEAR = 3026;
	private static final int CARDIEN_LETTER = 3141;
	private static final int CAMOMILE_CHARM = 3142;
	private static final int HIERARCH_LETTER = 3143;
	private static final int MOONFLOWER_CHARM = 3144;
	private static final int GRAIL_DIAGRAM = 3145;
	private static final int THALIA_LETTER_1 = 3146;
	private static final int THALIA_LETTER_2 = 3147;
	private static final int THALIA_INSTRUCTIONS = 3148;
	private static final int PUSHKIN_LIST = 3149;
	private static final int PURE_MITHRIL_CUP = 3150;
	private static final int ARKENIA_CONTRACT = 3151;
	private static final int ARKENIA_INSTRUCTIONS = 3152;
	private static final int ADONIUS_LIST = 3153;
	private static final int ANDARIEL_SCRIPTURE_COPY = 3154;
	private static final int STARDUST = 3155;
	private static final int ISAEL_INSTRUCTIONS = 3156;
	private static final int ISAEL_LETTER = 3157;
	private static final int GRAIL_OF_PURITY = 3158;
	private static final int TEARS_OF_UNICORN = 3159;
	private static final int WATER_OF_LIFE = 3160;
	private static final int PURE_MITHRIL_ORE = 3161;
	private static final int ANT_SOLDIER_ACID = 3162;
	private static final int WYRM_TALON = 3163;
	private static final int SPIDER_ICHOR = 3164;
	private static final int HARPY_DOWN = 3165;
	
	private static final int[] TALINS_PIECES =
	{
		3166,
		3167,
		3168,
		3169,
		3170,
		3171
	};
	
	// Rewards
	private static final int MARK_OF_LIFE = 3140;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	public Q218_TestimonyOfLife()
	{
		super(218, "Testimony of Life");
		
		setItemsIds(TALINS_SPEAR, CARDIEN_LETTER, CAMOMILE_CHARM, HIERARCH_LETTER, MOONFLOWER_CHARM, GRAIL_DIAGRAM, THALIA_LETTER_1, THALIA_LETTER_2, THALIA_INSTRUCTIONS, PUSHKIN_LIST, PURE_MITHRIL_CUP, ARKENIA_CONTRACT, ARKENIA_INSTRUCTIONS, ADONIUS_LIST, ANDARIEL_SCRIPTURE_COPY, STARDUST, ISAEL_INSTRUCTIONS, ISAEL_LETTER, GRAIL_OF_PURITY, TEARS_OF_UNICORN, WATER_OF_LIFE, PURE_MITHRIL_ORE, ANT_SOLDIER_ACID, WYRM_TALON, SPIDER_ICHOR, HARPY_DOWN, 3166, 3167, 3168, 3169, 3170, 3171);
		
		addStartNpc(CARDIEN);
		addTalkId(ASTERIOS, PUSHKIN, THALIA, ADONIUS, ARKENIA, CARDIEN, ISAEL);
		
		addKillId(20145, 20176, 20233, 27077, 20550, 20581, 20582, 20082, 20084, 20086, 20087, 20088);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30460-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(CARDIEN_LETTER, 1);
			
			if (!player.getMemos().getBool("secondClassChange37", false))
			{
				htmltext = "30460-04a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_37.get(player.getRace().ordinal()));
				player.getMemos().set("secondClassChange37", true);
			}
		}
		else if (event.equalsIgnoreCase("30154-07.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(CARDIEN_LETTER, 1);
			st.giveItems(HIERARCH_LETTER, 1);
			st.giveItems(MOONFLOWER_CHARM, 1);
		}
		else if (event.equalsIgnoreCase("30371-03.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(HIERARCH_LETTER, 1);
			st.giveItems(GRAIL_DIAGRAM, 1);
		}
		else if (event.equalsIgnoreCase("30371-11.htm"))
		{
			st.takeItems(STARDUST, 1);
			st.playSound(QuestState.SOUND_MIDDLE);
			
			if (player.getLevel() < 38)
			{
				htmltext = "30371-10.htm";
				st.set("cond", "13");
				st.giveItems(THALIA_INSTRUCTIONS, 1);
			}
			else
			{
				st.set("cond", "14");
				st.giveItems(THALIA_LETTER_2, 1);
			}
		}
		else if (event.equalsIgnoreCase("30300-06.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(GRAIL_DIAGRAM, 1);
			st.giveItems(PUSHKIN_LIST, 1);
		}
		else if (event.equalsIgnoreCase("30300-10.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(PUSHKIN_LIST, 1);
			st.takeItems(ANT_SOLDIER_ACID, -1);
			st.takeItems(PURE_MITHRIL_ORE, -1);
			st.takeItems(WYRM_TALON, -1);
			st.giveItems(PURE_MITHRIL_CUP, 1);
		}
		else if (event.equalsIgnoreCase("30419-04.htm"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(THALIA_LETTER_1, 1);
			st.giveItems(ARKENIA_CONTRACT, 1);
			st.giveItems(ARKENIA_INSTRUCTIONS, 1);
		}
		else if (event.equalsIgnoreCase("30375-02.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ARKENIA_INSTRUCTIONS, 1);
			st.giveItems(ADONIUS_LIST, 1);
		}
		else if (event.equalsIgnoreCase("30655-02.htm"))
		{
			st.set("cond", "15");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(THALIA_LETTER_2, 1);
			st.giveItems(ISAEL_INSTRUCTIONS, 1);
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
				if (player.getRace() != ClassRace.ELF)
					htmltext = "30460-01.htm";
				else if (player.getLevel() < 37 || player.getClassId().level() != 1)
					htmltext = "30460-02.htm";
				else
					htmltext = "30460-03.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ASTERIOS:
						if (cond == 1)
							htmltext = "30154-01.htm";
						else if (cond == 2)
							htmltext = "30154-08.htm";
						else if (cond == 20)
						{
							htmltext = "30154-09.htm";
							st.set("cond", "21");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(MOONFLOWER_CHARM, 1);
							st.takeItems(WATER_OF_LIFE, 1);
							st.giveItems(CAMOMILE_CHARM, 1);
						}
						else if (cond == 21)
							htmltext = "30154-10.htm";
						break;
					
					case PUSHKIN:
						if (cond == 3)
							htmltext = "30300-01.htm";
						else if (cond == 4)
							htmltext = "30300-07.htm";
						else if (cond == 5)
							htmltext = "30300-08.htm";
						else if (cond == 6)
							htmltext = "30300-11.htm";
						else if (cond > 6)
							htmltext = "30300-12.htm";
						break;
					
					case THALIA:
						if (cond == 2)
							htmltext = "30371-01.htm";
						else if (cond == 3)
							htmltext = "30371-04.htm";
						else if (cond > 3 && cond < 6)
							htmltext = "30371-05.htm";
						else if (cond == 6)
						{
							htmltext = "30371-06.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(PURE_MITHRIL_CUP, 1);
							st.giveItems(THALIA_LETTER_1, 1);
						}
						else if (cond == 7)
							htmltext = "30371-07.htm";
						else if (cond > 7 && cond < 12)
							htmltext = "30371-08.htm";
						else if (cond == 12)
							htmltext = "30371-09.htm";
						else if (cond == 13)
						{
							if (player.getLevel() < 38)
								htmltext = "30371-12.htm";
							else
							{
								htmltext = "30371-13.htm";
								st.set("cond", "14");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(THALIA_INSTRUCTIONS, 1);
								st.giveItems(THALIA_LETTER_2, 1);
							}
						}
						else if (cond == 14)
							htmltext = "30371-14.htm";
						else if (cond > 14 && cond < 17)
							htmltext = "30371-15.htm";
						else if (cond == 17)
						{
							htmltext = "30371-16.htm";
							st.set("cond", "18");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ISAEL_LETTER, 1);
							st.giveItems(GRAIL_OF_PURITY, 1);
						}
						else if (cond == 18)
							htmltext = "30371-17.htm";
						else if (cond == 19)
						{
							htmltext = "30371-18.htm";
							st.set("cond", "20");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(TEARS_OF_UNICORN, 1);
							st.giveItems(WATER_OF_LIFE, 1);
						}
						else if (cond > 19)
							htmltext = "30371-19.htm";
						break;
					
					case ADONIUS:
						if (cond == 8)
							htmltext = "30375-01.htm";
						else if (cond == 9)
							htmltext = "30375-03.htm";
						else if (cond == 10)
						{
							htmltext = "30375-04.htm";
							st.set("cond", "11");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ADONIUS_LIST, 1);
							st.takeItems(HARPY_DOWN, -1);
							st.takeItems(SPIDER_ICHOR, -1);
							st.giveItems(ANDARIEL_SCRIPTURE_COPY, 1);
						}
						else if (cond == 11)
							htmltext = "30375-05.htm";
						else if (cond > 11)
							htmltext = "30375-06.htm";
						break;
					
					case ARKENIA:
						if (cond == 7)
							htmltext = "30419-01.htm";
						else if (cond > 7 && cond < 11)
							htmltext = "30419-05.htm";
						else if (cond == 11)
						{
							htmltext = "30419-06.htm";
							st.set("cond", "12");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ANDARIEL_SCRIPTURE_COPY, 1);
							st.takeItems(ARKENIA_CONTRACT, 1);
							st.giveItems(STARDUST, 1);
						}
						else if (cond == 12)
							htmltext = "30419-07.htm";
						else if (cond > 12)
							htmltext = "30419-08.htm";
						break;
					
					case CARDIEN:
						if (cond == 1)
							htmltext = "30460-05.htm";
						else if (cond > 1 && cond < 21)
							htmltext = "30460-06.htm";
						else if (cond == 21)
						{
							htmltext = "30460-07.htm";
							st.takeItems(CAMOMILE_CHARM, 1);
							st.giveItems(MARK_OF_LIFE, 1);
							st.rewardExpAndSp(104591, 11250);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case ISAEL:
						if (cond == 14)
							htmltext = "30655-01.htm";
						else if (cond == 15)
							htmltext = "30655-03.htm";
						else if (cond == 16)
						{
							if (st.hasQuestItems(TALINS_PIECES))
							{
								htmltext = "30655-04.htm";
								st.set("cond", "17");
								st.playSound(QuestState.SOUND_MIDDLE);
								
								for (int itemId : TALINS_PIECES)
									st.takeItems(itemId, 1);
								
								st.takeItems(ISAEL_INSTRUCTIONS, 1);
								st.giveItems(ISAEL_LETTER, 1);
								st.giveItems(TALINS_SPEAR, 1);
							}
							else
								htmltext = "30655-03.htm";
						}
						else if (cond == 17)
							htmltext = "30655-05.htm";
						else if (cond > 17)
							htmltext = "30655-06.htm";
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
			case 20550:
				if (st.getInt("cond") == 4 && st.dropItems(PURE_MITHRIL_ORE, 1, 10, 500000))
					if (st.getQuestItemsCount(WYRM_TALON) >= 20 && st.getQuestItemsCount(ANT_SOLDIER_ACID) >= 20)
						st.set("cond", "5");
				break;
			
			case 20176:
				if (st.getInt("cond") == 4 && st.dropItems(WYRM_TALON, 1, 20, 500000))
					if (st.getQuestItemsCount(PURE_MITHRIL_ORE) >= 10 && st.getQuestItemsCount(ANT_SOLDIER_ACID) >= 20)
						st.set("cond", "5");
				break;
			
			case 20082:
			case 20084:
			case 20086:
			case 20087:
			case 20088:
				if (st.getInt("cond") == 4 && st.dropItems(ANT_SOLDIER_ACID, 1, 20, 800000))
					if (st.getQuestItemsCount(PURE_MITHRIL_ORE) >= 10 && st.getQuestItemsCount(WYRM_TALON) >= 20)
						st.set("cond", "5");
				break;
			
			case 20233:
				if (st.getInt("cond") == 9 && st.dropItems(SPIDER_ICHOR, 1, 20, 500000) && st.getQuestItemsCount(HARPY_DOWN) >= 20)
					st.set("cond", "10");
				break;
			
			case 20145:
				if (st.getInt("cond") == 9 && st.dropItems(HARPY_DOWN, 1, 20, 500000) && st.getQuestItemsCount(SPIDER_ICHOR) >= 20)
					st.set("cond", "10");
				break;
			
			case 27077:
				if (st.getInt("cond") == 18 && st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == TALINS_SPEAR)
				{
					st.set("cond", "19");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(GRAIL_OF_PURITY, 1);
					st.takeItems(TALINS_SPEAR, 1);
					st.giveItems(TEARS_OF_UNICORN, 1);
				}
				break;
			
			case 20581:
			case 20582:
				if (st.getInt("cond") == 15 && Rnd.nextBoolean())
				{
					for (int itemId : TALINS_PIECES)
					{
						if (!st.hasQuestItems(itemId))
						{
							st.giveItems(itemId, 1);
							
							if (st.hasQuestItems(TALINS_PIECES))
							{
								st.set("cond", "16");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
								st.playSound(QuestState.SOUND_ITEMGET);
							
							return null;
						}
					}
				}
				break;
		}
		
		return null;
	}
}
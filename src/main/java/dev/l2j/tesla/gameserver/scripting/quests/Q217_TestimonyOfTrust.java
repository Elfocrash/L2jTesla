package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q217_TestimonyOfTrust extends Quest
{
	private static final String qn = "Q217_TestimonyOfTrust";
	
	// Items
	private static final int LETTER_TO_ELF = 2735;
	private static final int LETTER_TO_DARK_ELF = 2736;
	private static final int LETTER_TO_DWARF = 2737;
	private static final int LETTER_TO_ORC = 2738;
	private static final int LETTER_TO_SERESIN = 2739;
	private static final int SCROLL_OF_DARK_ELF_TRUST = 2740;
	private static final int SCROLL_OF_ELF_TRUST = 2741;
	private static final int SCROLL_OF_DWARF_TRUST = 2742;
	private static final int SCROLL_OF_ORC_TRUST = 2743;
	private static final int RECOMMENDATION_OF_HOLLINT = 2744;
	private static final int ORDER_OF_ASTERIOS = 2745;
	private static final int BREATH_OF_WINDS = 2746;
	private static final int SEED_OF_VERDURE = 2747;
	private static final int LETTER_FROM_THIFIELL = 2748;
	private static final int BLOOD_GUARDIAN_BASILIK = 2749;
	private static final int GIANT_APHID = 2750;
	private static final int STAKATO_FLUIDS = 2751;
	private static final int BASILIK_PLASMA = 2752;
	private static final int HONEY_DEW = 2753;
	private static final int STAKATO_ICHOR = 2754;
	private static final int ORDER_OF_CLAYTON = 2755;
	private static final int PARASITE_OF_LOTA = 2756;
	private static final int LETTER_TO_MANAKIA = 2757;
	private static final int LETTER_OF_MANAKIA = 2758;
	private static final int LETTER_TO_NIKOLA = 2759;
	private static final int ORDER_OF_NIKOLA = 2760;
	private static final int HEARTSTONE_OF_PORTA = 2761;
	
	// Rewards
	private static final int MARK_OF_TRUST = 2734;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int HOLLINT = 30191;
	private static final int ASTERIOS = 30154;
	private static final int THIFIELL = 30358;
	private static final int CLAYTON = 30464;
	private static final int SERESIN = 30657;
	private static final int KAKAI = 30565;
	private static final int MANAKIA = 30515;
	private static final int LOCKIRIN = 30531;
	private static final int NIKOLA = 30621;
	private static final int BIOTIN = 30031;
	
	// Monsters
	private static final int DRYAD = 20013;
	private static final int DRYAD_ELDER = 20019;
	private static final int LIREIN = 20036;
	private static final int LIREIN_ELDER = 20044;
	private static final int ACTEA_OF_VERDANT_WILDS = 27121;
	private static final int LUELL_OF_ZEPHYR_WINDS = 27120;
	private static final int GUARDIAN_BASILIK = 20550;
	private static final int ANT_RECRUIT = 20082;
	private static final int ANT_PATROL = 20084;
	private static final int ANT_GUARD = 20086;
	private static final int ANT_SOLDIER = 20087;
	private static final int ANT_WARRIOR_CAPTAIN = 20088;
	private static final int MARSH_STAKATO = 20157;
	private static final int MARSH_STAKATO_WORKER = 20230;
	private static final int MARSH_STAKATO_SOLDIER = 20232;
	private static final int MARSH_STAKATO_DRONE = 20234;
	private static final int WINDSUS = 20553;
	private static final int PORTA = 20213;
	
	public Q217_TestimonyOfTrust()
	{
		super(217, "Testimony of Trust");
		
		setItemsIds(LETTER_TO_ELF, LETTER_TO_DARK_ELF, LETTER_TO_DWARF, LETTER_TO_ORC, LETTER_TO_SERESIN, SCROLL_OF_DARK_ELF_TRUST, SCROLL_OF_ELF_TRUST, SCROLL_OF_DWARF_TRUST, SCROLL_OF_ORC_TRUST, RECOMMENDATION_OF_HOLLINT, ORDER_OF_ASTERIOS, BREATH_OF_WINDS, SEED_OF_VERDURE, LETTER_FROM_THIFIELL, BLOOD_GUARDIAN_BASILIK, GIANT_APHID, STAKATO_FLUIDS, BASILIK_PLASMA, HONEY_DEW, STAKATO_ICHOR, ORDER_OF_CLAYTON, PARASITE_OF_LOTA, LETTER_TO_MANAKIA, LETTER_OF_MANAKIA, LETTER_TO_NIKOLA, ORDER_OF_NIKOLA, HEARTSTONE_OF_PORTA);
		
		addStartNpc(HOLLINT);
		addTalkId(HOLLINT, ASTERIOS, THIFIELL, CLAYTON, SERESIN, KAKAI, MANAKIA, LOCKIRIN, NIKOLA, BIOTIN);
		
		addKillId(DRYAD, DRYAD_ELDER, LIREIN, LIREIN_ELDER, ACTEA_OF_VERDANT_WILDS, LUELL_OF_ZEPHYR_WINDS, GUARDIAN_BASILIK, ANT_RECRUIT, ANT_PATROL, ANT_GUARD, ANT_SOLDIER, ANT_WARRIOR_CAPTAIN, MARSH_STAKATO, MARSH_STAKATO_WORKER, MARSH_STAKATO_SOLDIER, MARSH_STAKATO_DRONE, WINDSUS, PORTA);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30191-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LETTER_TO_ELF, 1);
			st.giveItems(LETTER_TO_DARK_ELF, 1);
			
			if (!player.getMemos().getBool("secondClassChange37", false))
			{
				htmltext = "30191-04a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_37.get(player.getRace().ordinal()));
				player.getMemos().set("secondClassChange37", true);
			}
		}
		else if (event.equalsIgnoreCase("30154-03.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LETTER_TO_ELF, 1);
			st.giveItems(ORDER_OF_ASTERIOS, 1);
		}
		else if (event.equalsIgnoreCase("30358-02.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LETTER_TO_DARK_ELF, 1);
			st.giveItems(LETTER_FROM_THIFIELL, 1);
		}
		else if (event.equalsIgnoreCase("30515-02.htm"))
		{
			st.set("cond", "14");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LETTER_TO_MANAKIA, 1);
		}
		else if (event.equalsIgnoreCase("30531-02.htm"))
		{
			st.set("cond", "18");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LETTER_TO_DWARF, 1);
			st.giveItems(LETTER_TO_NIKOLA, 1);
		}
		else if (event.equalsIgnoreCase("30565-02.htm"))
		{
			st.set("cond", "13");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LETTER_TO_ORC, 1);
			st.giveItems(LETTER_TO_MANAKIA, 1);
		}
		else if (event.equalsIgnoreCase("30621-02.htm"))
		{
			st.set("cond", "19");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LETTER_TO_NIKOLA, 1);
			st.giveItems(ORDER_OF_NIKOLA, 1);
		}
		else if (event.equalsIgnoreCase("30657-03.htm"))
		{
			if (player.getLevel() < 38)
			{
				htmltext = "30657-02.htm";
				if (st.getInt("cond") == 10)
				{
					st.set("cond", "11");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
			}
			else
			{
				st.set("cond", "12");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(LETTER_TO_SERESIN, 1);
				st.giveItems(LETTER_TO_DWARF, 1);
				st.giveItems(LETTER_TO_ORC, 1);
			}
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
				if (player.getClassId().level() != 1)
					htmltext = "30191-01a.htm";
				else if (player.getRace() != ClassRace.HUMAN)
					htmltext = "30191-02.htm";
				else if (player.getLevel() < 37)
					htmltext = "30191-01.htm";
				else
					htmltext = "30191-03.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case HOLLINT:
						if (cond < 9)
							htmltext = "30191-08.htm";
						else if (cond == 9)
						{
							htmltext = "30191-05.htm";
							st.set("cond", "10");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SCROLL_OF_DARK_ELF_TRUST, 1);
							st.takeItems(SCROLL_OF_ELF_TRUST, 1);
							st.giveItems(LETTER_TO_SERESIN, 1);
						}
						else if (cond > 9 && cond < 22)
							htmltext = "30191-09.htm";
						else if (cond == 22)
						{
							htmltext = "30191-06.htm";
							st.set("cond", "23");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SCROLL_OF_DWARF_TRUST, 1);
							st.takeItems(SCROLL_OF_ORC_TRUST, 1);
							st.giveItems(RECOMMENDATION_OF_HOLLINT, 1);
						}
						else if (cond == 23)
							htmltext = "30191-07.htm";
						break;
					
					case ASTERIOS:
						if (cond == 1)
							htmltext = "30154-01.htm";
						else if (cond == 2)
							htmltext = "30154-04.htm";
						else if (cond == 3)
						{
							htmltext = "30154-05.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BREATH_OF_WINDS, 1);
							st.takeItems(SEED_OF_VERDURE, 1);
							st.takeItems(ORDER_OF_ASTERIOS, 1);
							st.giveItems(SCROLL_OF_ELF_TRUST, 1);
						}
						else if (cond > 3)
							htmltext = "30154-06.htm";
						break;
					
					case THIFIELL:
						if (cond == 4)
							htmltext = "30358-01.htm";
						else if (cond > 4 && cond < 8)
							htmltext = "30358-05.htm";
						else if (cond == 8)
						{
							htmltext = "30358-03.htm";
							st.set("cond", "9");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BASILIK_PLASMA, 1);
							st.takeItems(HONEY_DEW, 1);
							st.takeItems(STAKATO_ICHOR, 1);
							st.giveItems(SCROLL_OF_DARK_ELF_TRUST, 1);
						}
						else if (cond > 8)
							htmltext = "30358-04.htm";
						break;
					
					case CLAYTON:
						if (cond == 5)
						{
							htmltext = "30464-01.htm";
							st.set("cond", "6");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(LETTER_FROM_THIFIELL, 1);
							st.giveItems(ORDER_OF_CLAYTON, 1);
						}
						else if (cond == 6)
							htmltext = "30464-02.htm";
						else if (cond > 6)
						{
							htmltext = "30464-03.htm";
							if (cond == 7)
							{
								st.set("cond", "8");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(ORDER_OF_CLAYTON, 1);
							}
						}
						break;
					
					case SERESIN:
						if (cond == 10 || cond == 11)
							htmltext = "30657-01.htm";
						else if (cond > 11 && cond < 22)
							htmltext = "30657-04.htm";
						else if (cond == 22)
							htmltext = "30657-05.htm";
						break;
					
					case KAKAI:
						if (cond == 12)
							htmltext = "30565-01.htm";
						else if (cond > 12 && cond < 16)
							htmltext = "30565-03.htm";
						else if (cond == 16)
						{
							htmltext = "30565-04.htm";
							st.set("cond", "17");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(LETTER_OF_MANAKIA, 1);
							st.giveItems(SCROLL_OF_ORC_TRUST, 1);
						}
						else if (cond > 16)
							htmltext = "30565-05.htm";
						break;
					
					case MANAKIA:
						if (cond == 13)
							htmltext = "30515-01.htm";
						else if (cond == 14)
							htmltext = "30515-03.htm";
						else if (cond == 15)
						{
							htmltext = "30515-04.htm";
							st.set("cond", "16");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(PARASITE_OF_LOTA, -1);
							st.giveItems(LETTER_OF_MANAKIA, 1);
						}
						else if (cond > 15)
							htmltext = "30515-05.htm";
						break;
					
					case LOCKIRIN:
						if (cond == 17)
							htmltext = "30531-01.htm";
						else if (cond > 17 && cond < 21)
							htmltext = "30531-03.htm";
						else if (cond == 21)
						{
							htmltext = "30531-04.htm";
							st.set("cond", "22");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(SCROLL_OF_DWARF_TRUST, 1);
						}
						else if (cond == 22)
							htmltext = "30531-05.htm";
						break;
					
					case NIKOLA:
						if (cond == 18)
							htmltext = "30621-01.htm";
						else if (cond == 19)
							htmltext = "30621-03.htm";
						else if (cond == 20)
						{
							htmltext = "30621-04.htm";
							st.set("cond", "21");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(HEARTSTONE_OF_PORTA, -1);
							st.takeItems(ORDER_OF_NIKOLA, 1);
						}
						else if (cond > 20)
							htmltext = "30621-05.htm";
						break;
					
					case BIOTIN:
						if (cond == 23)
						{
							htmltext = "30031-01.htm";
							st.takeItems(RECOMMENDATION_OF_HOLLINT, 1);
							st.giveItems(MARK_OF_TRUST, 1);
							st.rewardExpAndSp(39571, 2500);
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
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		final int npcId = npc.getNpcId();
		switch (npcId)
		{
			case DRYAD:
			case DRYAD_ELDER:
				if (st.getInt("cond") == 2 && !st.hasQuestItems(SEED_OF_VERDURE) && Rnd.get(100) < 33)
				{
					addSpawn(ACTEA_OF_VERDANT_WILDS, npc, true, 200000, true);
					st.playSound(QuestState.SOUND_BEFORE_BATTLE);
				}
				break;
			
			case LIREIN:
			case LIREIN_ELDER:
				if (st.getInt("cond") == 2 && !st.hasQuestItems(BREATH_OF_WINDS) && Rnd.get(100) < 33)
				{
					addSpawn(LUELL_OF_ZEPHYR_WINDS, npc, true, 200000, true);
					st.playSound(QuestState.SOUND_BEFORE_BATTLE);
				}
				break;
			
			case ACTEA_OF_VERDANT_WILDS:
				if (st.getInt("cond") == 2 && !st.hasQuestItems(SEED_OF_VERDURE))
				{
					st.giveItems(SEED_OF_VERDURE, 1);
					if (st.hasQuestItems(BREATH_OF_WINDS))
					{
						st.set("cond", "3");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case LUELL_OF_ZEPHYR_WINDS:
				if (st.getInt("cond") == 2 && !st.hasQuestItems(BREATH_OF_WINDS))
				{
					st.giveItems(BREATH_OF_WINDS, 1);
					if (st.hasQuestItems(SEED_OF_VERDURE))
					{
						st.set("cond", "3");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case MARSH_STAKATO:
			case MARSH_STAKATO_WORKER:
			case MARSH_STAKATO_SOLDIER:
			case MARSH_STAKATO_DRONE:
				if (st.getInt("cond") == 6 && !st.hasQuestItems(STAKATO_ICHOR) && st.dropItemsAlways(STAKATO_FLUIDS, 1, 10))
				{
					st.takeItems(STAKATO_FLUIDS, -1);
					st.giveItems(STAKATO_ICHOR, 1);
					
					if (st.hasQuestItems(BASILIK_PLASMA, HONEY_DEW))
						st.set("cond", "7");
				}
				break;
			
			case ANT_RECRUIT:
			case ANT_PATROL:
			case ANT_GUARD:
			case ANT_SOLDIER:
			case ANT_WARRIOR_CAPTAIN:
				if (st.getInt("cond") == 6 && !st.hasQuestItems(HONEY_DEW) && st.dropItemsAlways(GIANT_APHID, 1, 10))
				{
					st.takeItems(GIANT_APHID, -1);
					st.giveItems(HONEY_DEW, 1);
					
					if (st.hasQuestItems(BASILIK_PLASMA, STAKATO_ICHOR))
						st.set("cond", "7");
				}
				break;
			
			case GUARDIAN_BASILIK:
				if (st.getInt("cond") == 6 && !st.hasQuestItems(BASILIK_PLASMA) && st.dropItemsAlways(BLOOD_GUARDIAN_BASILIK, 1, 10))
				{
					st.takeItems(BLOOD_GUARDIAN_BASILIK, -1);
					st.giveItems(BASILIK_PLASMA, 1);
					
					if (st.hasQuestItems(HONEY_DEW, STAKATO_ICHOR))
						st.set("cond", "7");
				}
				break;
			
			case WINDSUS:
				if (st.getInt("cond") == 14 && st.dropItems(PARASITE_OF_LOTA, 1, 10, 500000))
					st.set("cond", "15");
				break;
			
			case PORTA:
				if (st.getInt("cond") == 19 && st.dropItemsAlways(HEARTSTONE_OF_PORTA, 1, 10))
					st.set("cond", "20");
				break;
		}
		
		return null;
	}
}
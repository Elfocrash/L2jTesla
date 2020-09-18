package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q663_SeductiveWhispers extends Quest
{
	private static final String qn = "Q663_SeductiveWhispers";
	
	// NPC
	private static final int WILBERT = 30846;
	
	// Quest item
	private static final int SPIRIT_BEAD = 8766;
	
	// Rewards
	private static final int ADENA = 57;
	private static final int ENCHANT_WEAPON_A = 729;
	private static final int ENCHANT_ARMOR_A = 730;
	private static final int ENCHANT_WEAPON_B = 947;
	private static final int ENCHANT_ARMOR_B = 948;
	private static final int ENCHANT_WEAPON_C = 951;
	private static final int ENCHANT_WEAPON_D = 955;
	
	private static final int RECIPES[] =
	{
		2353,
		4963,
		4967,
		5000,
		5001,
		5002,
		5004,
		5005,
		5006,
		5007
	};
	
	private static final int BLADES[] =
	{
		2115,
		4104,
		4108,
		4114,
		4115,
		4116,
		4118,
		4119,
		4120,
		4121
	};
	
	// Text of cards
	private static final Map<Integer, String> CARDS = new HashMap<>();
	{
		CARDS.put(0, "No such card");
		CARDS.put(11, "<font color=\"ff453d\"> Sun Card: 1 </font>");
		CARDS.put(12, "<font color=\"ff453d\"> Sun Card: 2 </font>");
		CARDS.put(13, "<font color=\"ff453d\"> Sun Card: 3 </font>");
		CARDS.put(14, "<font color=\"ff453d\"> Sun Card: 4 </font>");
		CARDS.put(15, "<font color=\"ff453d\"> Sun Card: 5 </font>");
		CARDS.put(21, "<font color=\"fff802\"> Moon Card: 1 </font>");
		CARDS.put(22, "<font color=\"fff802\"> Moon Card: 2 </font>");
		CARDS.put(23, "<font color=\"fff802\"> Moon Card: 3 </font>");
		CARDS.put(24, "<font color=\"fff802\"> Moon Card: 4 </font>");
		CARDS.put(25, "<font color=\"fff802\"> Moon Card: 5 </font>");
	}
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	{
		CHANCES.put(20674, 807000); // Doom Knight
		CHANCES.put(20678, 372000); // Tortured Undead
		CHANCES.put(20954, 460000); // Hungered Corpse
		CHANCES.put(20955, 537000); // Ghost War
		CHANCES.put(20956, 540000); // Past Knight
		CHANCES.put(20957, 565000); // Nihil Invader
		CHANCES.put(20958, 425000); // Death Agent
		CHANCES.put(20959, 682000); // Dark Guard
		CHANCES.put(20960, 372000); // Bloody Ghost
		CHANCES.put(20961, 547000); // Bloody Knight
		CHANCES.put(20962, 522000); // Bloody Priest
		CHANCES.put(20963, 498000); // Bloody Lord
		CHANCES.put(20974, 1000000); // Spiteful Soul Leader
		CHANCES.put(20975, 975000); // Spiteful Soul Wizard
		CHANCES.put(20976, 825000); // Spiteful Soul Fighter
		CHANCES.put(20996, 385000); // Spiteful Ghost of Ruins
		CHANCES.put(20997, 342000); // Soldier of Grief
		CHANCES.put(20998, 377000); // Cruel Punisher
		CHANCES.put(20999, 450000); // Roving Soul
		CHANCES.put(21000, 395000); // Soul of Ruins
		CHANCES.put(21001, 535000); // Wretched Archer
		CHANCES.put(21002, 472000); // Doom Scout
		CHANCES.put(21006, 502000); // Doom Servant
		CHANCES.put(21007, 540000); // Doom Guard
		CHANCES.put(21008, 692000); // Doom Archer
		CHANCES.put(21009, 740000); // Doom Trooper
		CHANCES.put(21010, 595000); // Doom Warrior
	}
	
	public Q663_SeductiveWhispers()
	{
		super(663, "Seductive Whispers");
		
		setItemsIds(SPIRIT_BEAD);
		
		addStartNpc(WILBERT);
		addTalkId(WILBERT);
		
		for (int npcId : CHANCES.keySet())
			addKillId(npcId);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		int state = st.getInt("state");
		
		if (event.equalsIgnoreCase("30846-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.set("state", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30846-09.htm") && (state % 10) <= 4)
		{
			if ((state / 10) < 1)
			{
				if (st.getQuestItemsCount(SPIRIT_BEAD) >= 50)
				{
					st.takeItems(SPIRIT_BEAD, 50);
					st.set("state", "5");
				}
				else
					htmltext = "30846-10.htm";
			}
			else
			{
				st.set("state", String.valueOf((state / 10) * 10 + 5));
				st.set("stateEx", "0");
				htmltext = "30846-09a.htm";
			}
		}
		else if (event.equalsIgnoreCase("30846-14.htm") && (state % 10) == 5 && (state / 1000) == 0)
		{
			int i0 = st.getInt("stateEx");
			
			int i1 = i0 % 10;
			int i2 = (i0 - i1) / 10;
			
			int param1 = Rnd.get(2) + 1;
			int param2 = Rnd.get(5) + 1;
			
			int i5 = state / 10;
			
			int param3 = param1 * 10 + param2;
			
			if (param1 == i2)
			{
				int i3 = param2 + i1;
				
				if (i3 % 5 == 0 && i3 != 10)
				{
					if ((state % 100) / 10 >= 7)
					{
						st.set("state", "4");
						st.rewardItems(ADENA, 2384000);
						st.rewardItems(ENCHANT_WEAPON_A, 1);
						st.rewardItems(ENCHANT_ARMOR_A, 1);
						st.rewardItems(ENCHANT_ARMOR_A, 1);
						htmltext = getHTML("30846-14.htm", i0, param3, player.getName());
					}
					else
					{
						st.set("state", String.valueOf((state / 10) * 10 + 7));
						htmltext = getHTML("30846-13.htm", i0, param3, player.getName()).replace("%wincount%", String.valueOf(i5 + 1));
					}
				}
				else
				{
					st.set("state", String.valueOf((state / 10) * 10 + 6));
					st.set("stateEx", String.valueOf(param3));
					htmltext = getHTML("30846-12.htm", i0, param3, player.getName());
				}
			}
			else
			{
				if (param2 == 5 || i1 == 5)
				{
					if ((state % 100) / 10 >= 7)
					{
						st.set("state", "4");
						st.rewardItems(ADENA, 2384000);
						st.rewardItems(ENCHANT_WEAPON_A, 1);
						st.rewardItems(ENCHANT_ARMOR_A, 1);
						st.rewardItems(ENCHANT_ARMOR_A, 1);
						htmltext = getHTML("30846-14.htm", i0, param3, player.getName());
					}
					else
					{
						st.set("state", String.valueOf((state / 10) * 10 + 7));
						htmltext = getHTML("30846-13.htm", i0, param3, player.getName()).replace("%wincount%", String.valueOf(i5 + 1));
					}
				}
				else
				{
					st.set("state", String.valueOf((state / 10) * 10 + 6));
					st.set("stateEx", String.valueOf(param1 * 10 + param2));
					htmltext = getHTML("30846-12.htm", i0, param3, player.getName());
				}
			}
		}
		else if (event.equalsIgnoreCase("30846-19.htm") && (state % 10) == 6 && (state / 1000) == 0)
		{
			int i0 = st.getInt("stateEx");
			
			int i1 = i0 % 10;
			int i2 = (i0 - i1) / 10;
			
			int param1 = Rnd.get(2) + 1;
			int param2 = Rnd.get(5) + 1;
			int param3 = param1 * 10 + param2;
			
			if (param1 == i2)
			{
				int i3 = param1 + i1;
				
				if (i3 % 5 == 0 && i3 != 10)
				{
					st.set("state", "1");
					st.set("stateEx", "0");
					htmltext = getHTML("30846-19.htm", i0, param3, player.getName());
				}
				else
				{
					st.set("state", String.valueOf((state / 10) * 10 + 5));
					st.set("stateEx", String.valueOf(param3));
					htmltext = getHTML("30846-18.htm", i0, param3, player.getName());
				}
			}
			else
			{
				if (param2 == 5 || i1 == 5)
				{
					st.set("state", "1");
					htmltext = getHTML("30846-19.htm", i0, param3, player.getName());
				}
				else
				{
					st.set("state", String.valueOf((state / 10) * 10 + 5));
					st.set("stateEx", String.valueOf(param3));
					htmltext = getHTML("30846-18.htm", i0, param3, player.getName());
				}
			}
		}
		else if (event.equalsIgnoreCase("30846-20.htm") && (state % 10) == 7 && (state / 1000) == 0)
		{
			st.set("state", String.valueOf((state / 10 + 1) * 10 + 4));
			st.set("stateEx", "0");
		}
		else if (event.equalsIgnoreCase("30846-21.htm") && (state % 10) == 7 && (state / 1000) == 0)
		{
			int round = state / 10;
			
			if (round == 0)
				st.rewardItems(ADENA, 40000);
			else if (round == 1)
				st.rewardItems(ADENA, 80000);
			else if (round == 2)
			{
				st.rewardItems(ADENA, 110000);
				st.rewardItems(ENCHANT_WEAPON_D, 1);
			}
			else if (round == 3)
			{
				st.rewardItems(ADENA, 199000);
				st.rewardItems(ENCHANT_WEAPON_C, 1);
			}
			else if (round == 4)
			{
				st.rewardItems(ADENA, 388000);
				st.rewardItems(Rnd.get(RECIPES), 1);
			}
			else if (round == 5)
			{
				st.rewardItems(ADENA, 675000);
				st.rewardItems(Rnd.get(BLADES), 1);
			}
			else if (round == 6)
			{
				st.rewardItems(ADENA, 1284000);
				st.rewardItems(ENCHANT_WEAPON_B, 1);
				st.rewardItems(ENCHANT_ARMOR_B, 1);
				st.rewardItems(ENCHANT_WEAPON_B, 1);
				st.rewardItems(ENCHANT_ARMOR_B, 1);
			}
			
			st.set("state", "1");
			st.set("stateEx", "0");
		}
		else if (event.equalsIgnoreCase("30846-22.htm") && (state % 10) == 1)
		{
			if (st.hasQuestItems(SPIRIT_BEAD))
			{
				st.set("state", "1005");
				st.takeItems(SPIRIT_BEAD, 1);
			}
			else
				htmltext = "30846-22a.htm";
		}
		else if (event.equalsIgnoreCase("30846-25.htm") && state == 1005)
		{
			int i0 = st.getInt("stateEx");
			
			int i1 = i0 % 10;
			int i2 = (i0 - i1) / 10;
			
			int param1 = Rnd.get(2) + 1;
			int param2 = Rnd.get(5) + 1;
			int param3 = param1 * 10 + param2;
			
			if (param1 == i2)
			{
				int i3 = param2 + i1;
				
				if (i3 % 5 == 0 && i3 != 10)
				{
					st.set("state", "1");
					st.set("stateEx", "0");
					st.rewardItems(ADENA, 800);
					htmltext = getHTML("30846-25.htm", i0, param3, player.getName()).replace("%card1%", String.valueOf(i1));
				}
				else
				{
					st.set("state", "1006");
					st.set("stateEx", String.valueOf(param3));
					htmltext = getHTML("30846-24.htm", i0, param3, player.getName());
				}
			}
			else
			{
				if (param2 == 5 || i2 == 5)
				{
					st.set("state", "1");
					st.set("stateEx", "0");
					st.rewardItems(ADENA, 800);
					htmltext = getHTML("30846-25.htm", i0, param3, player.getName()).replace("%card1%", String.valueOf(i1));
				}
				else
				{
					st.set("state", "1006");
					st.set("stateEx", String.valueOf(param3));
					htmltext = getHTML("30846-24.htm", i0, param3, player.getName());
				}
			}
		}
		else if (event.equalsIgnoreCase("30846-29.htm") && state == 1006)
		{
			int i0 = st.getInt("stateEx");
			
			int i1 = i0 % 10;
			int i2 = (i0 - i1) / 10;
			
			int param1 = Rnd.get(2) + 1;
			int param2 = Rnd.get(5) + 1;
			int param3 = param1 * 10 + param2;
			
			if (param1 == i2)
			{
				int i3 = param2 + i1;
				
				if (i3 % 5 == 0 && i3 != 10)
				{
					st.set("state", "1");
					st.set("stateEx", "0");
					st.rewardItems(ADENA, 800);
					htmltext = getHTML("30846-29.htm", i0, param3, player.getName()).replace("%card1%", String.valueOf(i1));
				}
				else
				{
					st.set("state", "1005");
					st.set("stateEx", String.valueOf(param3));
					htmltext = getHTML("30846-28.htm", i0, param3, player.getName());
				}
			}
			else
			{
				if (param2 == 5 || i1 == 5)
				{
					st.set("state", "1");
					st.set("stateEx", "0");
					htmltext = getHTML("30846-29.htm", i0, param3, player.getName());
				}
				else
				{
					st.set("state", "1005");
					st.set("stateEx", String.valueOf(param3));
					htmltext = getHTML("30846-28.htm", i0, param3, player.getName());
				}
			}
		}
		else if (event.equalsIgnoreCase("30846-30.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				htmltext = (player.getLevel() < 50) ? "30846-02.htm" : "30846-01.htm";
				break;
			
			case STATE_STARTED:
				int state = st.getInt("state");
				
				if (state < 4)
				{
					if (st.hasQuestItems(SPIRIT_BEAD))
						htmltext = "30846-05.htm";
					else
						htmltext = "30846-04.htm";
				}
				else if ((state % 10) == 4)
					htmltext = "30846-05a.htm";
				else if ((state % 10) == 5)
					htmltext = "30846-11.htm";
				else if ((state % 10) == 6)
					htmltext = "30846-15.htm";
				else if ((state % 10) == 7)
				{
					int round = (state % 100) / 10;
					
					if (round >= 7)
					{
						st.rewardItems(ADENA, 2384000);
						st.rewardItems(ENCHANT_WEAPON_A, 1);
						st.rewardItems(ENCHANT_ARMOR_A, 1);
						st.rewardItems(ENCHANT_ARMOR_A, 1);
						htmltext = "30846-17.htm";
					}
					else
						htmltext = getHtmlText("30846-16.htm").replace("%wincount%", String.valueOf((state / 10) + 1));
				}
				else if (state == 1005)
					htmltext = "30846-23.htm";
				else if (state == 1006)
					htmltext = "30846-26.htm";
				
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		st.dropItems(SPIRIT_BEAD, 1, 0, CHANCES.get(npc.getNpcId()));
		
		return null;
	}
	
	private String getHTML(String html, int index, int param3, String name)
	{
		return getHtmlText(html).replace("%card1pic%", CARDS.get(index)).replace("%card2pic%", CARDS.get(param3)).replace("%name%", name);
	}
}
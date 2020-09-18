package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.ItemTable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.kind.Item;

public class Q662_AGameOfCards extends Quest
{
	private static final String qn = "Q662_AGameOfCards";
	
	// NPC
	private static final int KLUMP = 30845;
	
	// Quest Item
	private static final int RED_GEM = 8765;
	
	// Reward Items
	private static final int EW_S = 959;
	private static final int EW_A = 729;
	private static final int EW_B = 947;
	private static final int EW_C = 951;
	private static final int EW_D = 955;
	private static final int EA_D = 956;
	private static final int ZIGGO_GEMSTONE = 8868;
	
	// All cards
	private static final Map<Integer, String> CARDS = new HashMap<>();
	{
		CARDS.put(0, "?");
		CARDS.put(1, "!");
		CARDS.put(2, "=");
		CARDS.put(3, "T");
		CARDS.put(4, "V");
		CARDS.put(5, "O");
		CARDS.put(6, "P");
		CARDS.put(7, "S");
		CARDS.put(8, "E");
		CARDS.put(9, "H");
		CARDS.put(10, "A");
		CARDS.put(11, "R");
		CARDS.put(12, "D");
		CARDS.put(13, "I");
		CARDS.put(14, "N");
	}
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	{
		CHANCES.put(18001, 232000); // Blood Queen
		CHANCES.put(20672, 357000); // Trives
		CHANCES.put(20673, 373000); // Falibati
		CHANCES.put(20674, 583000); // Doom Knight
		CHANCES.put(20677, 435000); // Tulben
		CHANCES.put(20955, 358000); // Ghostly Warrior
		CHANCES.put(20958, 283000); // Death Agent
		CHANCES.put(20959, 455000); // Dark Guard
		CHANCES.put(20961, 365000); // Bloody Knight
		CHANCES.put(20962, 348000); // Bloody Priest
		CHANCES.put(20965, 457000); // Chimera Piece
		CHANCES.put(20966, 493000); // Changed Creation
		CHANCES.put(20968, 418000); // Nonexistant Man
		CHANCES.put(20972, 350000); // Shaman of Ancient Times
		CHANCES.put(20973, 453000); // Forgotten Ancient People
		CHANCES.put(21002, 315000); // Doom Scout
		CHANCES.put(21004, 320000); // Dismal Pole
		CHANCES.put(21006, 335000); // Doom Servant
		CHANCES.put(21008, 462000); // Doom Archer
		CHANCES.put(21010, 397000); // Doom Warrior
		CHANCES.put(21109, 507000); // Hames Orc Scout
		CHANCES.put(21112, 552000); // Hames Orc Footman
		CHANCES.put(21114, 587000); // Cursed Guardian
		CHANCES.put(21116, 812000); // Hames Orc Overlord
		CHANCES.put(21278, 483000); // Antelope
		CHANCES.put(21279, 483000); // Antelope
		CHANCES.put(21280, 483000); // Antelope
		CHANCES.put(21281, 483000); // Antelope
		CHANCES.put(21286, 515000); // Buffalo
		CHANCES.put(21287, 515000); // Buffalo
		CHANCES.put(21288, 515000); // Buffalo
		CHANCES.put(21289, 515000); // Buffalo
		CHANCES.put(21508, 493000); // Splinter Stakato
		CHANCES.put(21510, 527000); // Splinter Stakato Soldier
		CHANCES.put(21513, 562000); // Needle Stakato
		CHANCES.put(21515, 598000); // Needle Stakato Soldier
		CHANCES.put(21520, 458000); // Eye of Splendor
		CHANCES.put(21526, 552000); // Wisdom of Splendor
		CHANCES.put(21530, 488000); // Victory of Splendor
		CHANCES.put(21535, 573000); // Signet of Splendor
	}
	
	public Q662_AGameOfCards()
	{
		super(662, "A Game Of Cards");
		
		setItemsIds(RED_GEM);
		
		addStartNpc(KLUMP);
		addTalkId(KLUMP);
		
		for (int monster : CHANCES.keySet())
			addKillId(monster);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30845-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.set("state", "0");
			st.set("stateEx", "0");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30845-04.htm"))
		{
			int state = st.getInt("state");
			int stateEx = st.getInt("stateEx");
			
			if (state == 0 && stateEx == 0 && st.getQuestItemsCount(RED_GEM) >= 50)
				htmltext = "30845-05.htm";
		}
		else if (event.equalsIgnoreCase("30845-07.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30845-11.htm"))
		{
			int state = st.getInt("state");
			int stateEx = st.getInt("stateEx");
			
			if (state == 0 && stateEx == 0 && st.getQuestItemsCount(RED_GEM) >= 50)
			{
				int i1 = Rnd.get(70) + 1;
				int i2 = Rnd.get(70) + 1;
				int i3 = Rnd.get(70) + 1;
				int i4 = Rnd.get(70) + 1;
				int i5 = Rnd.get(70) + 1;
				
				if (i1 >= 57)
					i1 = i1 - 56;
				else if (i1 >= 43)
					i1 = i1 - 42;
				else if (i1 >= 29)
					i1 = i1 - 28;
				else if (i1 >= 15)
					i1 = i1 - 14;
				
				if (i2 >= 57)
					i2 = i2 - 56;
				else if (i2 >= 43)
					i2 = i2 - 42;
				else if (i2 >= 29)
					i2 = i2 - 28;
				else if (i2 >= 15)
					i2 = i2 - 14;
				
				if (i3 >= 57)
					i3 = i3 - 56;
				else if (i3 >= 43)
					i3 = i3 - 42;
				else if (i3 >= 29)
					i3 = i3 - 28;
				else if (i3 >= 15)
					i3 = i3 - 14;
				
				if (i4 >= 57)
					i4 = i4 - 56;
				else if (i4 >= 43)
					i4 = i4 - 42;
				else if (i4 >= 29)
					i4 = i4 - 28;
				else if (i4 >= 15)
					i4 = i4 - 14;
				
				if (i5 >= 57)
					i5 = i5 - 56;
				else if (i5 >= 43)
					i5 = i5 - 42;
				else if (i5 >= 29)
					i5 = i5 - 28;
				else if (i5 >= 15)
					i5 = i5 - 14;
				
				st.set("state", String.valueOf(i4 * 1000000 + i3 * 10000 + i2 * 100 + i1));
				st.set("stateEx", String.valueOf(i5));
				
				st.takeItems(RED_GEM, 50);
			}
		}
		else if (event.equals("First") || event.equals("Second") || event.equals("Third") || event.equals("Fourth") || event.equals("Fifth")) // reply 11 12 13 14 15
		{
			int state = st.getInt("state");
			int stateEx = st.getInt("stateEx");
			
			int i0, i1, i2, i3, i4, i5, i6, i8, i9;
			
			i0 = state;
			i1 = stateEx;
			i5 = i1 % 100;
			i9 = i1 / 100;
			i1 = i0 % 100;
			i2 = i0 % 10000 / 100;
			i3 = i0 % 1000000 / 10000;
			i4 = i0 % 100000000 / 1000000;
			
			if (event.equals("First"))
			{
				if (i9 % 2 < 1)
					i9 = i9 + 1;
			}
			else if (event.equals("Second"))
			{
				if (i9 % 4 < 2)
					i9 = i9 + 2;
			}
			else if (event.equals("Third"))
			{
				if (i9 % 8 < 4)
					i9 = i9 + 4;
			}
			else if (event.equals("Fourth"))
			{
				if (i9 % 16 < 8)
					i9 = i9 + 8;
			}
			else if (event.equals("Fifth"))
			{
				if (i9 % 32 < 16)
					i9 = i9 + 16;
			}
			
			if (i9 % 32 < 31)
			{
				st.set("stateEx", String.valueOf(i9 * 100 + i5));
				htmltext = getHtmlText("30845-12.htm");
			}
			else if (i9 % 32 == 31)
			{
				i6 = 0;
				i8 = 0;
				
				if (i1 >= 1 && i1 <= 14 && i2 >= 1 && i2 <= 14 && i3 >= 1 && i3 <= 14 && i4 >= 1 && i4 <= 14 && i5 >= 1 && i5 <= 14)
				{
					if (i1 == i2)
					{
						i6 = i6 + 10;
						i8 = i8 + 8;
					}
					
					if (i1 == i3)
					{
						i6 = i6 + 10;
						i8 = i8 + 4;
					}
					
					if (i1 == i4)
					{
						i6 = i6 + 10;
						i8 = i8 + 2;
					}
					
					if (i1 == i5)
					{
						i6 = i6 + 10;
						i8 = i8 + 1;
					}
					
					if (i6 % 100 < 10)
					{
						if (i8 % 16 < 8)
						{
							if (i8 % 8 < 4)
							{
								if (i2 == i3)
								{
									i6 = i6 + 10;
									i8 = i8 + 4;
								}
							}
							
							if (i8 % 4 < 2)
							{
								if (i2 == i4)
								{
									i6 = i6 + 10;
									i8 = i8 + 2;
								}
							}
							
							if (i8 % 2 < 1)
							{
								if (i2 == i5)
								{
									i6 = i6 + 10;
									i8 = i8 + 1;
								}
							}
						}
					}
					else if (i6 % 10 == 0)
					{
						if (i8 % 16 < 8)
						{
							if (i8 % 8 < 4)
							{
								if (i2 == i3)
								{
									i6 = i6 + 1;
									i8 = i8 + 4;
								}
							}
							
							if (i8 % 4 < 2)
							{
								if (i2 == i4)
								{
									i6 = i6 + 1;
									i8 = i8 + 2;
								}
							}
							
							if (i8 % 2 < 1)
							{
								if (i2 == i5)
								{
									i6 = i6 + 1;
									i8 = i8 + 1;
								}
							}
						}
					}
					
					if (i6 % 100 < 10)
					{
						if (i8 % 8 < 4)
						{
							if (i8 % 4 < 2)
							{
								if (i3 == i4)
								{
									i6 = i6 + 10;
									i8 = i8 + 2;
								}
							}
							
							if (i8 % 2 < 1)
							{
								if (i3 == i5)
								{
									i6 = i6 + 10;
									i8 = i8 + 1;
								}
							}
						}
					}
					else if (i6 % 10 == 0)
					{
						if (i8 % 8 < 4)
						{
							if (i8 % 4 < 2)
							{
								if (i3 == i4)
								{
									i6 = i6 + 1;
									i8 = i8 + 2;
								}
							}
							
							if (i8 % 2 < 1)
							{
								if (i3 == i5)
								{
									i6 = i6 + 1;
									i8 = i8 + 1;
								}
							}
						}
					}
					
					if (i6 % 100 < 10)
					{
						if (i8 % 4 < 2)
						{
							if (i8 % 2 < 1)
							{
								if (i4 == i5)
								{
									i6 = i6 + 10;
									i8 = i8 + 1;
								}
							}
						}
					}
					else if (i6 % 10 == 0)
					{
						if (i8 % 4 < 2)
						{
							if (i8 % 2 < 1)
							{
								if (i4 == i5)
								{
									i6 = i6 + 1;
									i8 = i8 + 1;
								}
							}
						}
					}
				}
				
				if (i6 == 40)
				{
					giveReward(st, ZIGGO_GEMSTONE, 43);
					giveReward(st, EW_S, 3);
					giveReward(st, EW_A, 1);
					htmltext = getHtmlText("30845-13.htm");
				}
				else if (i6 == 30)
				{
					giveReward(st, EW_S, 2);
					giveReward(st, EW_C, 2);
					htmltext = getHtmlText("30845-14.htm");
				}
				else if (i6 == 21 || i6 == 12)
				{
					giveReward(st, EW_A, 1);
					giveReward(st, EW_B, 2);
					giveReward(st, EW_D, 1);
					htmltext = getHtmlText("30845-15.htm");
				}
				else if (i6 == 20)
				{
					giveReward(st, EW_C, 2);
					htmltext = getHtmlText("30845-16.htm");
				}
				else if (i6 == 11)
				{
					giveReward(st, EW_C, 1);
					htmltext = getHtmlText("30845-17.htm");
				}
				else if (i6 == 10)
				{
					giveReward(st, EA_D, 2);
					htmltext = getHtmlText("30845-18.htm");
				}
				else if (i6 == 0)
					htmltext = getHtmlText("30845-19.htm");
				
				st.set("state", "0");
				st.set("stateEx", "0");
			}
			
			htmltext = htmltext.replace("%FontColor1%", (i9 % 2 < 1) ? "ffff00" : "ff6f6f").replace("%Cell1%", (i9 % 2 < 1) ? CARDS.get(0) : CARDS.get(i1));
			htmltext = htmltext.replace("%FontColor2%", (i9 % 4 < 2) ? "ffff00" : "ff6f6f").replace("%Cell2%", (i9 % 4 < 2) ? CARDS.get(0) : CARDS.get(i2));
			htmltext = htmltext.replace("%FontColor3%", (i9 % 8 < 4) ? "ffff00" : "ff6f6f").replace("%Cell3%", (i9 % 8 < 4) ? CARDS.get(0) : CARDS.get(i3));
			htmltext = htmltext.replace("%FontColor4%", (i9 % 16 < 8) ? "ffff00" : "ff6f6f").replace("%Cell4%", (i9 % 16 < 8) ? CARDS.get(0) : CARDS.get(i4));
			htmltext = htmltext.replace("%FontColor5%", (i9 % 32 < 16) ? "ffff00" : "ff6f6f").replace("%Cell5%", (i9 % 32 < 16) ? CARDS.get(0) : CARDS.get(i5));
		}
		else if (event.equalsIgnoreCase("30845-20.htm")) // reply 20
		{
			if (st.getQuestItemsCount(RED_GEM) < 50)
				htmltext = "30845-21.htm";
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
				htmltext = (player.getLevel() < 61) ? "30845-02.htm" : "30845-01.htm";
				break;
			
			case STATE_STARTED:
				int state = st.getInt("state");
				int stateEx = st.getInt("stateEx");
				
				if (state == 0 && stateEx == 0)
					htmltext = (st.getQuestItemsCount(RED_GEM) < 50) ? "30845-04.htm" : "30845-05.htm";
				else if (state != 0 && stateEx != 0)
				{
					int i0, i1, i2, i3, i4, i5, i9;
					
					i0 = state;
					i1 = stateEx;
					i5 = i1 % 100;
					i9 = i1 / 100;
					i1 = i0 % 100;
					i2 = i0 % 10000 / 100;
					i3 = i0 % 1000000 / 10000;
					i4 = i0 % 100000000 / 1000000;
					
					htmltext = getHtmlText("30845-11a.htm");
					htmltext = htmltext.replace("%FontColor1%", (i9 % 2 < 1) ? "ffff00" : "ff6f6f").replace("%Cell1%", (i9 % 2 < 1) ? CARDS.get(0) : CARDS.get(i1));
					htmltext = htmltext.replace("%FontColor2%", (i9 % 4 < 2) ? "ffff00" : "ff6f6f").replace("%Cell2%", (i9 % 4 < 2) ? CARDS.get(0) : CARDS.get(i2));
					htmltext = htmltext.replace("%FontColor3%", (i9 % 8 < 4) ? "ffff00" : "ff6f6f").replace("%Cell3%", (i9 % 8 < 4) ? CARDS.get(0) : CARDS.get(i3));
					htmltext = htmltext.replace("%FontColor4%", (i9 % 16 < 8) ? "ffff00" : "ff6f6f").replace("%Cell4%", (i9 % 16 < 8) ? CARDS.get(0) : CARDS.get(i4));
					htmltext = htmltext.replace("%FontColor5%", (i9 % 32 < 16) ? "ffff00" : "ff6f6f").replace("%Cell5%", (i9 % 32 < 16) ? CARDS.get(0) : CARDS.get(i5));
				}
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
		
		st.dropItems(RED_GEM, 1, 0, CHANCES.get(npc.getNpcId()));
		return null;
	}
	
	private static void giveReward(QuestState st, int item, int count)
	{
		final Item template = ItemTable.getInstance().getTemplate(item);
		
		if (template.isStackable())
			st.giveItems(item, count);
		else
		{
			for (int i = 0; i < count; i++)
				st.giveItems(item, 1);
		}
	}
}
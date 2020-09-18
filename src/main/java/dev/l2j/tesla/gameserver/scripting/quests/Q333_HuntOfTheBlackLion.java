package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q333_HuntOfTheBlackLion extends Quest
{
	private static final String qn = "Q333_HuntOfTheBlackLion";
	
	// NPCs
	private static final int SOPHYA = 30735;
	private static final int REDFOOT = 30736;
	private static final int RUPIO = 30471;
	private static final int UNDRIAS = 30130;
	private static final int LOCKIRIN = 30531;
	private static final int MORGAN = 30737;
	
	// Needs for start
	private static final int BLACK_LION_MARK = 1369;
	
	// Quest items
	private static final int LION_CLAW = 3675;
	private static final int LION_EYE = 3676;
	private static final int GUILD_COIN = 3677;
	private static final int UNDEAD_ASH = 3848;
	private static final int BLOODY_AXE_INSIGNIA = 3849;
	private static final int DELU_FANG = 3850;
	private static final int STAKATO_TALON = 3851;
	private static final int SOPHYA_LETTER_1 = 3671;
	private static final int SOPHYA_LETTER_2 = 3672;
	private static final int SOPHYA_LETTER_3 = 3673;
	private static final int SOPHYA_LETTER_4 = 3674;
	
	private static final int CARGO_BOX_1 = 3440;
	private static final int CARGO_BOX_2 = 3441;
	private static final int CARGO_BOX_3 = 3442;
	private static final int CARGO_BOX_4 = 3443;
	private static final int GLUDIO_APPLE = 3444;
	private static final int CORN_MEAL = 3445;
	private static final int WOLF_PELTS = 3446;
	private static final int MOONSTONE = 3447;
	private static final int GLUDIO_WHEAT_FLOWER = 3448;
	private static final int SPIDERSILK_ROPE = 3449;
	private static final int ALEXANDRITE = 3450;
	private static final int SILVER_TEA = 3451;
	private static final int GOLEM_PART = 3452;
	private static final int FIRE_EMERALD = 3453;
	private static final int SILK_FROCK = 3454;
	private static final int PORCELAN_URN = 3455;
	private static final int IMPERIAL_DIAMOND = 3456;
	private static final int STATUE_SHILIEN_HEAD = 3457;
	private static final int STATUE_SHILIEN_TORSO = 3458;
	private static final int STATUE_SHILIEN_ARM = 3459;
	private static final int STATUE_SHILIEN_LEG = 3460;
	private static final int COMPLETE_STATUE = 3461;
	private static final int TABLET_FRAGMENT_1 = 3462;
	private static final int TABLET_FRAGMENT_2 = 3463;
	private static final int TABLET_FRAGMENT_3 = 3464;
	private static final int TABLET_FRAGMENT_4 = 3465;
	private static final int COMPLETE_TABLET = 3466;
	
	// Neutral items
	private static final int ADENA = 57;
	private static final int SWIFT_ATTACK_POTION = 735;
	private static final int SCROLL_OF_ESCAPE = 736;
	private static final int HEALING_POTION = 1061;
	private static final int SOULSHOT_D = 1463;
	private static final int SPIRITSHOT_D = 2510;
	
	// Tabs: Part, NpcId, ItemId, Item Chance, Box Id, Box Chance
	private static final int[][] DROPLIST =
	{
		// Part #1 - Execution Ground
		{
			SOPHYA_LETTER_1,
			20160,
			UNDEAD_ASH,
			500000,
			CARGO_BOX_1,
			90000
		}, // Neer Crawler
		{
			SOPHYA_LETTER_1,
			20171,
			UNDEAD_ASH,
			500000,
			CARGO_BOX_1,
			60000
		}, // Specter
		{
			SOPHYA_LETTER_1,
			20197,
			UNDEAD_ASH,
			500000,
			CARGO_BOX_1,
			70000
		}, // Sorrow Maiden
		{
			SOPHYA_LETTER_1,
			20198,
			UNDEAD_ASH,
			500000,
			CARGO_BOX_1,
			80000
		}, // Neer Ghoul Berserker
		{
			SOPHYA_LETTER_1,
			20200,
			UNDEAD_ASH,
			500000,
			CARGO_BOX_1,
			100000
		}, // Strain
		{
			SOPHYA_LETTER_1,
			20201,
			UNDEAD_ASH,
			500000,
			CARGO_BOX_1,
			110000
		}, // Ghoul
		
		// Part #2 - Partisan Hideaway
		{
			SOPHYA_LETTER_2,
			20207,
			BLOODY_AXE_INSIGNIA,
			500000,
			CARGO_BOX_2,
			60000
		}, // Ol Mahum Guerilla
		{
			SOPHYA_LETTER_2,
			20208,
			BLOODY_AXE_INSIGNIA,
			500000,
			CARGO_BOX_2,
			70000
		}, // Ol Mahum Raider
		{
			SOPHYA_LETTER_2,
			20209,
			BLOODY_AXE_INSIGNIA,
			500000,
			CARGO_BOX_2,
			80000
		}, // Ol Mahum Marksman
		{
			SOPHYA_LETTER_2,
			20210,
			BLOODY_AXE_INSIGNIA,
			500000,
			CARGO_BOX_2,
			90000
		}, // Ol Mahum Sergeant
		{
			SOPHYA_LETTER_2,
			20211,
			BLOODY_AXE_INSIGNIA,
			500000,
			CARGO_BOX_2,
			100000
		}, // Ol Mahum Captain
		
		// Part #3 - Near Giran Town
		{
			SOPHYA_LETTER_3,
			20251,
			DELU_FANG,
			500000,
			CARGO_BOX_3,
			100000
		}, // Delu Lizardman
		{
			SOPHYA_LETTER_3,
			20252,
			DELU_FANG,
			500000,
			CARGO_BOX_3,
			110000
		}, // Delu Lizardman Scout
		{
			SOPHYA_LETTER_3,
			20253,
			DELU_FANG,
			500000,
			CARGO_BOX_3,
			120000
		}, // Delu Lizardman Warrior
		
		// Part #4 - Cruma Area
		{
			SOPHYA_LETTER_4,
			20157,
			STAKATO_TALON,
			500000,
			CARGO_BOX_4,
			100000
		}, // Marsh Stakato
		{
			SOPHYA_LETTER_4,
			20230,
			STAKATO_TALON,
			500000,
			CARGO_BOX_4,
			110000
		}, // Marsh Stakato Worker
		{
			SOPHYA_LETTER_4,
			20232,
			STAKATO_TALON,
			500000,
			CARGO_BOX_4,
			120000
		}, // Marsh Stakato Soldier
		{
			SOPHYA_LETTER_4,
			20234,
			STAKATO_TALON,
			500000,
			CARGO_BOX_4,
			130000
		}
		// Marsh Stakato Drone
	};
	
	public Q333_HuntOfTheBlackLion()
	{
		super(333, "Hunt Of The Black Lion");
		
		setItemsIds(LION_CLAW, LION_EYE, GUILD_COIN, UNDEAD_ASH, BLOODY_AXE_INSIGNIA, DELU_FANG, STAKATO_TALON, SOPHYA_LETTER_1, SOPHYA_LETTER_2, SOPHYA_LETTER_3, SOPHYA_LETTER_4);
		
		addStartNpc(SOPHYA);
		addTalkId(SOPHYA, REDFOOT, RUPIO, UNDRIAS, LOCKIRIN, MORGAN);
		
		for (int[] i : DROPLIST)
			addKillId(i[1]);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30735-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30735-10.htm"))
		{
			if (!st.hasQuestItems(SOPHYA_LETTER_1))
			{
				st.giveItems(SOPHYA_LETTER_1, 1);
				st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		else if (event.equalsIgnoreCase("30735-11.htm"))
		{
			if (!st.hasQuestItems(SOPHYA_LETTER_2))
			{
				st.giveItems(SOPHYA_LETTER_2, 1);
				st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		else if (event.equalsIgnoreCase("30735-12.htm"))
		{
			if (!st.hasQuestItems(SOPHYA_LETTER_3))
			{
				st.giveItems(SOPHYA_LETTER_3, 1);
				st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		else if (event.equalsIgnoreCase("30735-13.htm"))
		{
			if (!st.hasQuestItems(SOPHYA_LETTER_4))
			{
				st.giveItems(SOPHYA_LETTER_4, 1);
				st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		else if (event.equalsIgnoreCase("30735-16.htm"))
		{
			if (st.getQuestItemsCount(LION_CLAW) > 9)
			{
				st.takeItems(LION_CLAW, 10);
				
				final int eyes = st.getQuestItemsCount(LION_EYE);
				if (eyes < 5)
				{
					htmltext = "30735-17a.htm";
					
					st.giveItems(LION_EYE, 1);
					
					final int random = Rnd.get(100);
					if (random < 25)
						st.giveItems(HEALING_POTION, 20);
					else if (random < 50)
						st.giveItems(player.isMageClass() ? SPIRITSHOT_D : SOULSHOT_D, player.isMageClass() ? 50 : 100);
					else if (random < 75)
						st.giveItems(SCROLL_OF_ESCAPE, 20);
					else
						st.giveItems(SWIFT_ATTACK_POTION, 3);
				}
				else if (eyes < 9)
				{
					htmltext = "30735-18b.htm";
					
					st.giveItems(LION_EYE, 1);
					
					final int random = Rnd.get(100);
					if (random < 25)
						st.giveItems(HEALING_POTION, 25);
					else if (random < 50)
						st.giveItems(player.isMageClass() ? SPIRITSHOT_D : SOULSHOT_D, player.isMageClass() ? 100 : 200);
					else if (random < 75)
						st.giveItems(SCROLL_OF_ESCAPE, 20);
					else
						st.giveItems(SWIFT_ATTACK_POTION, 3);
				}
				else
				{
					htmltext = "30735-19b.htm";
					
					final int random = Rnd.get(100);
					if (random < 25)
						st.giveItems(HEALING_POTION, 50);
					else if (random < 50)
						st.giveItems(player.isMageClass() ? SPIRITSHOT_D : SOULSHOT_D, player.isMageClass() ? 200 : 400);
					else if (random < 75)
						st.giveItems(SCROLL_OF_ESCAPE, 30);
					else
						st.giveItems(SWIFT_ATTACK_POTION, 4);
				}
			}
		}
		else if (event.equalsIgnoreCase("30735-20.htm"))
		{
			st.takeItems(SOPHYA_LETTER_1, -1);
			st.takeItems(SOPHYA_LETTER_2, -1);
			st.takeItems(SOPHYA_LETTER_3, -1);
			st.takeItems(SOPHYA_LETTER_4, -1);
		}
		else if (event.equalsIgnoreCase("30735-26.htm"))
		{
			st.takeItems(LION_CLAW, -1);
			st.takeItems(LION_EYE, -1);
			st.takeItems(GUILD_COIN, -1);
			st.takeItems(BLACK_LION_MARK, -1);
			st.takeItems(SOPHYA_LETTER_1, -1);
			st.takeItems(SOPHYA_LETTER_2, -1);
			st.takeItems(SOPHYA_LETTER_3, -1);
			st.takeItems(SOPHYA_LETTER_4, -1);
			st.giveItems(ADENA, 12400);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30736-03.htm"))
		{
			final boolean cargo1 = st.hasQuestItems(CARGO_BOX_1);
			final boolean cargo2 = st.hasQuestItems(CARGO_BOX_2);
			final boolean cargo3 = st.hasQuestItems(CARGO_BOX_3);
			final boolean cargo4 = st.hasQuestItems(CARGO_BOX_4);
			
			if ((cargo1 || cargo2 || cargo3 || cargo4) && player.getAdena() > 649)
			{
				st.takeItems(ADENA, 650);
				
				if (cargo1)
					st.takeItems(CARGO_BOX_1, 1);
				else if (cargo2)
					st.takeItems(CARGO_BOX_2, 1);
				else if (cargo3)
					st.takeItems(CARGO_BOX_3, 1);
				else
					st.takeItems(CARGO_BOX_4, 1);
				
				final int i0 = Rnd.get(100);
				final int i1 = Rnd.get(100);
				if (i0 < 40)
				{
					if (i1 < 33)
					{
						htmltext = "30736-04a.htm";
						st.giveItems(GLUDIO_APPLE, 1);
					}
					else if (i1 < 66)
					{
						htmltext = "30736-04b.htm";
						st.giveItems(CORN_MEAL, 1);
					}
					else
					{
						htmltext = "30736-04c.htm";
						st.giveItems(WOLF_PELTS, 1);
					}
				}
				else if (i0 < 60)
				{
					if (i1 < 33)
					{
						htmltext = "30736-04d.htm";
						st.giveItems(MOONSTONE, 1);
					}
					else if (i1 < 66)
					{
						htmltext = "30736-04e.htm";
						st.giveItems(GLUDIO_WHEAT_FLOWER, 1);
					}
					else
					{
						htmltext = "30736-04f.htm";
						st.giveItems(SPIDERSILK_ROPE, 1);
					}
				}
				else if (i0 < 70)
				{
					if (i1 < 33)
					{
						htmltext = "30736-04g.htm";
						st.giveItems(ALEXANDRITE, 1);
					}
					else if (i1 < 66)
					{
						htmltext = "30736-04h.htm";
						st.giveItems(SILVER_TEA, 1);
					}
					else
					{
						htmltext = "30736-04i.htm";
						st.giveItems(GOLEM_PART, 1);
					}
				}
				else if (i0 < 75)
				{
					if (i1 < 33)
					{
						htmltext = "30736-04j.htm";
						st.giveItems(FIRE_EMERALD, 1);
					}
					else if (i1 < 66)
					{
						htmltext = "30736-04k.htm";
						st.giveItems(SILK_FROCK, 1);
					}
					else
					{
						htmltext = "30736-04l.htm";
						st.giveItems(PORCELAN_URN, 1);
					}
				}
				else if (i0 < 76)
				{
					htmltext = "30736-04m.htm";
					st.giveItems(IMPERIAL_DIAMOND, 1);
				}
				else if (Rnd.nextBoolean())
				{
					htmltext = "30736-04n.htm";
					
					if (i1 < 25)
						st.giveItems(STATUE_SHILIEN_HEAD, 1);
					else if (i1 < 50)
						st.giveItems(STATUE_SHILIEN_TORSO, 1);
					else if (i1 < 75)
						st.giveItems(STATUE_SHILIEN_ARM, 1);
					else
						st.giveItems(STATUE_SHILIEN_LEG, 1);
				}
				else
				{
					htmltext = "30736-04o.htm";
					
					if (i1 < 25)
						st.giveItems(TABLET_FRAGMENT_1, 1);
					else if (i1 < 50)
						st.giveItems(TABLET_FRAGMENT_2, 1);
					else if (i1 < 75)
						st.giveItems(TABLET_FRAGMENT_3, 1);
					else
						st.giveItems(TABLET_FRAGMENT_4, 1);
				}
			}
			else
				htmltext = "30736-05.htm";
		}
		else if (event.equalsIgnoreCase("30736-07.htm"))
		{
			final int state = st.getInt("state");
			if (player.getAdena() > (200 + state * 200))
			{
				if (state < 3)
				{
					final int i0 = Rnd.get(100);
					if (i0 < 5)
						htmltext = "30736-08a.htm";
					else if (i0 < 10)
						htmltext = "30736-08b.htm";
					else if (i0 < 15)
						htmltext = "30736-08c.htm";
					else if (i0 < 20)
						htmltext = "30736-08d.htm";
					else if (i0 < 25)
						htmltext = "30736-08e.htm";
					else if (i0 < 30)
						htmltext = "30736-08f.htm";
					else if (i0 < 35)
						htmltext = "30736-08g.htm";
					else if (i0 < 40)
						htmltext = "30736-08h.htm";
					else if (i0 < 45)
						htmltext = "30736-08i.htm";
					else if (i0 < 50)
						htmltext = "30736-08j.htm";
					else if (i0 < 55)
						htmltext = "30736-08k.htm";
					else if (i0 < 60)
						htmltext = "30736-08l.htm";
					else if (i0 < 65)
						htmltext = "30736-08m.htm";
					else if (i0 < 70)
						htmltext = "30736-08n.htm";
					else if (i0 < 75)
						htmltext = "30736-08o.htm";
					else if (i0 < 80)
						htmltext = "30736-08p.htm";
					else if (i0 < 85)
						htmltext = "30736-08q.htm";
					else if (i0 < 90)
						htmltext = "30736-08r.htm";
					else if (i0 < 95)
						htmltext = "30736-08s.htm";
					else
						htmltext = "30736-08t.htm";
					
					st.takeItems(ADENA, 200 + state * 200);
					st.set("state", String.valueOf(state + 1));
				}
				else
					htmltext = "30736-08.htm";
			}
		}
		else if (event.equalsIgnoreCase("30471-03.htm"))
		{
			if (st.hasQuestItems(STATUE_SHILIEN_HEAD, STATUE_SHILIEN_TORSO, STATUE_SHILIEN_ARM, STATUE_SHILIEN_LEG))
			{
				st.takeItems(STATUE_SHILIEN_HEAD, 1);
				st.takeItems(STATUE_SHILIEN_TORSO, 1);
				st.takeItems(STATUE_SHILIEN_ARM, 1);
				st.takeItems(STATUE_SHILIEN_LEG, 1);
				
				if (Rnd.nextBoolean())
				{
					htmltext = "30471-04.htm";
					st.giveItems(COMPLETE_STATUE, 1);
				}
				else
					htmltext = "30471-05.htm";
			}
		}
		else if (event.equalsIgnoreCase("30471-06.htm"))
		{
			if (st.hasQuestItems(TABLET_FRAGMENT_1, TABLET_FRAGMENT_2, TABLET_FRAGMENT_3, TABLET_FRAGMENT_4))
			{
				st.takeItems(TABLET_FRAGMENT_1, 1);
				st.takeItems(TABLET_FRAGMENT_2, 1);
				st.takeItems(TABLET_FRAGMENT_3, 1);
				st.takeItems(TABLET_FRAGMENT_4, 1);
				
				if (Rnd.nextBoolean())
				{
					htmltext = "30471-07.htm";
					st.giveItems(COMPLETE_TABLET, 1);
				}
				else
					htmltext = "30471-08.htm";
			}
		}
		else if (event.equalsIgnoreCase("30130-04.htm") && st.hasQuestItems(COMPLETE_STATUE))
		{
			st.takeItems(COMPLETE_STATUE, 1);
			st.giveItems(ADENA, 30000);
		}
		else if (event.equalsIgnoreCase("30531-04.htm") && st.hasQuestItems(COMPLETE_TABLET))
		{
			st.takeItems(COMPLETE_TABLET, 1);
			st.giveItems(ADENA, 30000);
		}
		else if (event.equalsIgnoreCase("30737-06.htm"))
		{
			final boolean cargo1 = st.hasQuestItems(CARGO_BOX_1);
			final boolean cargo2 = st.hasQuestItems(CARGO_BOX_2);
			final boolean cargo3 = st.hasQuestItems(CARGO_BOX_3);
			final boolean cargo4 = st.hasQuestItems(CARGO_BOX_4);
			
			if (cargo1 || cargo2 || cargo3 || cargo4)
			{
				if (cargo1)
					st.takeItems(CARGO_BOX_1, 1);
				else if (cargo2)
					st.takeItems(CARGO_BOX_2, 1);
				else if (cargo3)
					st.takeItems(CARGO_BOX_3, 1);
				else
					st.takeItems(CARGO_BOX_4, 1);
				
				final int coins = st.getQuestItemsCount(GUILD_COIN);
				if (coins < 40)
				{
					htmltext = "30737-03.htm";
					st.giveItems(ADENA, 100);
				}
				else if (coins < 80)
				{
					htmltext = "30737-04.htm";
					st.giveItems(ADENA, 200);
				}
				else
				{
					htmltext = "30737-05.htm";
					st.giveItems(ADENA, 300);
				}
				
				if (coins < 80)
					st.giveItems(GUILD_COIN, 1);
			}
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
				if (player.getLevel() < 25)
					htmltext = "30735-01.htm";
				else if (!st.hasQuestItems(BLACK_LION_MARK))
					htmltext = "30735-02.htm";
				else
					htmltext = "30735-03.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case SOPHYA:
						if (!st.hasAtLeastOneQuestItem(SOPHYA_LETTER_1, SOPHYA_LETTER_2, SOPHYA_LETTER_3, SOPHYA_LETTER_4))
							htmltext = "30735-14.htm";
						else
						{
							if (!st.hasAtLeastOneQuestItem(UNDEAD_ASH, BLOODY_AXE_INSIGNIA, DELU_FANG, STAKATO_TALON))
								htmltext = st.hasAtLeastOneQuestItem(CARGO_BOX_1, CARGO_BOX_2, CARGO_BOX_3, CARGO_BOX_4) ? "30735-15a.htm" : "30735-15.htm";
							else
							{
								final int count = st.getQuestItemsCount(UNDEAD_ASH) + st.getQuestItemsCount(BLOODY_AXE_INSIGNIA) + st.getQuestItemsCount(DELU_FANG) + st.getQuestItemsCount(STAKATO_TALON);
								
								st.takeItems(UNDEAD_ASH, -1);
								st.takeItems(BLOODY_AXE_INSIGNIA, -1);
								st.takeItems(DELU_FANG, -1);
								st.takeItems(STAKATO_TALON, -1);
								st.giveItems(ADENA, count * 35);
								
								if (count >= 20 && count < 50)
									st.giveItems(LION_CLAW, 1);
								else if (count >= 50 && count < 100)
									st.giveItems(LION_CLAW, 2);
								else if (count >= 100)
									st.giveItems(LION_CLAW, 3);
								
								htmltext = st.hasAtLeastOneQuestItem(CARGO_BOX_1, CARGO_BOX_2, CARGO_BOX_3, CARGO_BOX_4) ? "30735-23.htm" : "30735-22.htm";
							}
						}
						break;
					
					case REDFOOT:
						htmltext = st.hasAtLeastOneQuestItem(CARGO_BOX_1, CARGO_BOX_2, CARGO_BOX_3, CARGO_BOX_4) ? "30736-02.htm" : "30736-01.htm";
						break;
					
					case RUPIO:
						if (st.hasQuestItems(STATUE_SHILIEN_HEAD, STATUE_SHILIEN_TORSO, STATUE_SHILIEN_ARM, STATUE_SHILIEN_LEG) || st.hasQuestItems(TABLET_FRAGMENT_1, TABLET_FRAGMENT_2, TABLET_FRAGMENT_3, TABLET_FRAGMENT_4))
							htmltext = "30471-02.htm";
						else
							htmltext = "30471-01.htm";
						break;
					
					case UNDRIAS:
						if (!st.hasQuestItems(COMPLETE_STATUE))
							htmltext = st.hasQuestItems(STATUE_SHILIEN_HEAD, STATUE_SHILIEN_TORSO, STATUE_SHILIEN_ARM, STATUE_SHILIEN_LEG) ? "30130-02.htm" : "30130-01.htm";
						else
							htmltext = "30130-03.htm";
						break;
					
					case LOCKIRIN:
						if (!st.hasQuestItems(COMPLETE_TABLET))
							htmltext = st.hasQuestItems(TABLET_FRAGMENT_1, TABLET_FRAGMENT_2, TABLET_FRAGMENT_3, TABLET_FRAGMENT_4) ? "30531-02.htm" : "30531-01.htm";
						else
							htmltext = "30531-03.htm";
						break;
					
					case MORGAN:
						htmltext = st.hasAtLeastOneQuestItem(CARGO_BOX_1, CARGO_BOX_2, CARGO_BOX_3, CARGO_BOX_4) ? "30737-02.htm" : "30737-01.htm";
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
		
		for (int[] info : DROPLIST)
		{
			if (st.hasQuestItems(info[0]) && npc.getNpcId() == info[1])
			{
				st.dropItems(info[2], 1, 0, info[3]);
				st.dropItems(info[4], 1, 0, info[5]);
				break;
			}
		}
		
		return null;
	}
}
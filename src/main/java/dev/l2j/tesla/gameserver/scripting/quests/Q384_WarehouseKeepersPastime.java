package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.lang.StringUtil;
import dev.l2j.tesla.commons.random.Rnd;
import dev.l2j.tesla.commons.util.ArraysUtil;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q384_WarehouseKeepersPastime extends Quest
{
	private static final String qn = "Q384_WarehouseKeepersPastime";
	
	// NPCs
	private static final int CLIFF = 30182;
	private static final int BAXT = 30685;
	
	// Items
	private static final int MEDAL = 5964;
	
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	{
		CHANCES.put(20947, 160000); // Connabi
		CHANCES.put(20948, 180000); // Bartal
		CHANCES.put(20945, 120000); // Cadeine
		CHANCES.put(20946, 150000); // Sanhidro
		CHANCES.put(20635, 150000); // Carinkain
		CHANCES.put(20773, 610000); // Conjurer Bat Lord
		CHANCES.put(20774, 600000); // Conjurer Bat
		CHANCES.put(20760, 240000); // Dragon Bearer Archer
		CHANCES.put(20758, 240000); // Dragon Bearer Chief
		CHANCES.put(20759, 230000); // Dragon Bearer Warrior
		CHANCES.put(20242, 220000); // Dustwind Gargoyle
		CHANCES.put(20281, 220000); // Dustwind Gargoyle (2)
		CHANCES.put(20556, 140000); // Giant Monstereye
		CHANCES.put(20668, 210000); // Grave Guard
		CHANCES.put(20241, 220000); // Hunter Gargoyle
		CHANCES.put(20286, 220000); // Hunter Gargoyle (2)
		CHANCES.put(20949, 190000); // Luminun
		CHANCES.put(20950, 200000); // Innersen
		CHANCES.put(20942, 90000); // Nightmare Guide
		CHANCES.put(20943, 120000); // Nightmare Keeper
		CHANCES.put(20944, 110000); // Nightmare Lord
		CHANCES.put(20559, 140000); // Rotting Golem
		CHANCES.put(20243, 210000); // Thunder Wyrm
		CHANCES.put(20282, 210000); // Thunder Wyrm (2)
		CHANCES.put(20677, 340000); // Tulben
		CHANCES.put(20605, 150000); // Weird Drake
	}
	
	private static final int[][] INDEX_MAP =
	{
		{
			1,
			2,
			3
		}, // line 1
		{
			4,
			5,
			6
		}, // line 2
		{
			7,
			8,
			9
		}, // line 3
		{
			1,
			4,
			7
		}, // column 1
		{
			2,
			5,
			8
		}, // column 2
		{
			3,
			6,
			9
		}, // column 3
		{
			1,
			5,
			9
		}, // diagonal 1
		{
			3,
			5,
			7
		}, // diagonal 2
	};
	
	private static final int[][] _rewards_10_win =
	{
		{
			16,
			1888
		}, // Synthetic Cokes
		{
			32,
			1887
		}, // Varnish of Purity
		{
			50,
			1894
		}, // Crafted Leather
		{
			80,
			952
		}, // Scroll: Enchant Armor (C)
		{
			89,
			1890
		}, // Mithril Alloy
		{
			98,
			1893
		}, // Oriharukon
		{
			100,
			951
		}
		// Scroll: Enchant Weapon (C)
	};
	
	private static final int[][] _rewards_10_lose =
	{
		{
			50,
			4041
		}, // Mold Hardener
		{
			80,
			952
		}, // Scroll: Enchant Armor (C)
		{
			98,
			1892
		}, // Blacksmith's Frame
		{
			100,
			917
		}
		// Necklace of Mermaid
	};
	
	private static final int[][] _rewards_100_win =
	{
		{
			50,
			883
		}, // Aquastone Ring
		{
			80,
			951
		}, // Scroll: Enchant Weapon (C)
		{
			98,
			852
		}, // Moonstone Earring
		{
			100,
			401
		}
		// Drake Leather Armor
	};
	
	private static final int[][] _rewards_100_lose =
	{
		{
			50,
			951
		}, // Scroll: Enchant Weapon (C)
		{
			80,
			500
		}, // Great Helmet
		{
			98,
			2437
		}, // Drake Leather Boots
		{
			100,
			135
		}
		// Samurai Longsword
	};
	
	public Q384_WarehouseKeepersPastime()
	{
		super(384, "Warehouse Keeper's Pastime");
		
		setItemsIds(MEDAL);
		
		addStartNpc(CLIFF);
		addTalkId(CLIFF, BAXT);
		
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
		
		final int npcId = npc.getNpcId();
		if (event.equalsIgnoreCase("30182-05.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase(npcId + "-08.htm"))
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase(npcId + "-11.htm"))
		{
			if (st.getQuestItemsCount(MEDAL) < 10)
				htmltext = npcId + "-12.htm";
			else
			{
				st.set("bet", "10");
				st.set("board", StringUtil.scrambleString("123456789"));
				st.takeItems(MEDAL, 10);
			}
		}
		else if (event.equalsIgnoreCase(npcId + "-13.htm"))
		{
			if (st.getQuestItemsCount(MEDAL) < 100)
				htmltext = npcId + "-12.htm";
			else
			{
				st.set("bet", "100");
				st.set("board", StringUtil.scrambleString("123456789"));
				st.takeItems(MEDAL, 100);
			}
		}
		else if (event.startsWith("select_1-")) // first pick
		{
			// Register the first char.
			st.set("playerArray", event.substring(9));
			
			// Send back the finalized HTM with dynamic content.
			htmltext = fillBoard(st, getHtmlText(npcId + "-14.htm"));
		}
		else if (event.startsWith("select_2-")) // pick #2-5
		{
			// Stores the current event for future use.
			String number = event.substring(9);
			
			// Restore the player array.
			String playerArray = st.get("playerArray");
			
			// Verify if the given number is already on the player array, if yes, it's invalid, otherwise register it.
			if (ArraysUtil.contains(playerArray.split(""), number))
				htmltext = fillBoard(st, getHtmlText(npcId + "-" + String.valueOf(14 + 2 * playerArray.length()) + ".htm"));
			else
			{
				// Stores the final String.
				st.set("playerArray", playerArray.concat(number));
				
				htmltext = fillBoard(st, getHtmlText(npcId + "-" + String.valueOf(11 + 2 * (playerArray.length() + 1)) + ".htm"));
			}
		}
		else if (event.startsWith("select_3-")) // pick #6
		{
			// Stores the current event for future use.
			String number = event.substring(9);
			
			// Restore the player array.
			String playerArray = st.get("playerArray");
			
			// Verify if the given number is already on the player array, if yes, it's invalid, otherwise calculate reward.
			if (ArraysUtil.contains(playerArray.split(""), number))
				htmltext = fillBoard(st, getHtmlText(npcId + "-26.htm"));
			else
			{
				// No need to store the String on player db, but still need to update it.
				final String[] playerChoice = playerArray.concat(number).split("");
				
				// Transform the generated board (9 string length) into a 2d matrice (3x3 int).
				final String[] board = st.get("board").split("");
				
				// test for all line combination
				int winningLines = 0;
				
				for (int[] map : INDEX_MAP)
				{
					// test line combination
					boolean won = true;
					for (int index : map)
						won &= ArraysUtil.contains(playerChoice, board[index]);
					
					// cut the loop, when you won
					if (won)
						winningLines++;
				}
				
				if (winningLines == 3)
				{
					htmltext = getHtmlText(npcId + "-23.htm");
					
					final int chance = Rnd.get(100);
					for (int[] reward : ((st.get("bet") == "10") ? _rewards_10_win : _rewards_100_win))
					{
						if (chance < reward[0])
						{
							st.giveItems(reward[1], 1);
							if (reward[1] == 2437)
								st.giveItems(2463, 1);
							
							break;
						}
					}
				}
				else if (winningLines == 0)
				{
					htmltext = getHtmlText(npcId + "-25.htm");
					
					final int chance = Rnd.get(100);
					for (int[] reward : ((st.get("bet") == "10") ? _rewards_10_lose : _rewards_100_lose))
					{
						if (chance < reward[0])
						{
							st.giveItems(reward[1], 1);
							break;
						}
					}
				}
				else
					htmltext = getHtmlText(npcId + "-24.htm");
				
				for (int i = 1; i < 10; i++)
				{
					htmltext = htmltext.replace("<?Cell" + i + "?>", board[i]);
					htmltext = htmltext.replace("<?FontColor" + i + "?>", (ArraysUtil.contains(playerChoice, board[i])) ? "ff0000" : "ffffff");
				}
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
				htmltext = (player.getLevel() < 40) ? "30182-04.htm" : "30182-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case CLIFF:
						htmltext = (st.getQuestItemsCount(MEDAL) < 10) ? "30182-06.htm" : "30182-07.htm";
						break;
					
					case BAXT:
						htmltext = (st.getQuestItemsCount(MEDAL) < 10) ? "30685-01.htm" : "30685-02.htm";
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
		
		final QuestState st = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		st.dropItems(MEDAL, 1, 0, CHANCES.get(npc.getNpcId()));
		
		return null;
	}
	
	private static final String fillBoard(QuestState st, String htmltext)
	{
		final String[] playerArray = st.get("playerArray").split("");
		final String[] board = st.get("board").split("");
		
		for (int i = 1; i < 10; i++)
			htmltext = htmltext.replace("<?Cell" + i + "?>", (ArraysUtil.contains(playerArray, board[i])) ? board[i] : "?");
		
		return htmltext;
	}
}
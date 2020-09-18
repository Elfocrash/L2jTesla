package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q378_MagnificentFeast extends Quest
{
	private static final String qn = "Q378_MagnificentFeast";
	
	// NPC
	private static final int RANSPO = 30594;
	
	// Items
	private static final int WINE_15 = 5956;
	private static final int WINE_30 = 5957;
	private static final int WINE_60 = 5958;
	private static final int MUSICAL_SCORE = 4421;
	private static final int SALAD_RECIPE = 1455;
	private static final int SAUCE_RECIPE = 1456;
	private static final int STEAK_RECIPE = 1457;
	private static final int RITRON_DESSERT = 5959;
	
	// Rewards
	private static final Map<String, int[]> REWARDS = new HashMap<>();
	{
		REWARDS.put("9", new int[]
		{
			847,
			1,
			5700
		});
		REWARDS.put("10", new int[]
		{
			846,
			2,
			0
		});
		REWARDS.put("12", new int[]
		{
			909,
			1,
			25400
		});
		REWARDS.put("17", new int[]
		{
			846,
			2,
			1200
		});
		REWARDS.put("18", new int[]
		{
			879,
			1,
			6900
		});
		REWARDS.put("20", new int[]
		{
			890,
			2,
			8500
		});
		REWARDS.put("33", new int[]
		{
			879,
			1,
			8100
		});
		REWARDS.put("34", new int[]
		{
			910,
			1,
			0
		});
		REWARDS.put("36", new int[]
		{
			848,
			1,
			2200
		});
	}
	
	public Q378_MagnificentFeast()
	{
		super(378, "Magnificent Feast");
		
		addStartNpc(RANSPO);
		addTalkId(RANSPO);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30594-2.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30594-4a.htm"))
		{
			if (st.hasQuestItems(WINE_15))
			{
				st.set("cond", "2");
				st.set("score", "1");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(WINE_15, 1);
			}
			else
				htmltext = "30594-4.htm";
		}
		else if (event.equalsIgnoreCase("30594-4b.htm"))
		{
			if (st.hasQuestItems(WINE_30))
			{
				st.set("cond", "2");
				st.set("score", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(WINE_30, 1);
			}
			else
				htmltext = "30594-4.htm";
		}
		else if (event.equalsIgnoreCase("30594-4c.htm"))
		{
			if (st.hasQuestItems(WINE_60))
			{
				st.set("cond", "2");
				st.set("score", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(WINE_60, 1);
			}
			else
				htmltext = "30594-4.htm";
		}
		else if (event.equalsIgnoreCase("30594-6.htm"))
		{
			if (st.hasQuestItems(MUSICAL_SCORE))
			{
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(MUSICAL_SCORE, 1);
			}
			else
				htmltext = "30594-5.htm";
		}
		else
		{
			int score = st.getInt("score");
			if (event.equalsIgnoreCase("30594-8a.htm"))
			{
				if (st.hasQuestItems(SALAD_RECIPE))
				{
					st.set("cond", "4");
					st.set("score", String.valueOf(score + 8));
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(SALAD_RECIPE, 1);
				}
				else
					htmltext = "30594-8.htm";
			}
			else if (event.equalsIgnoreCase("30594-8b.htm"))
			{
				if (st.hasQuestItems(SAUCE_RECIPE))
				{
					st.set("cond", "4");
					st.set("score", String.valueOf(score + 16));
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(SAUCE_RECIPE, 1);
				}
				else
					htmltext = "30594-8.htm";
			}
			else if (event.equalsIgnoreCase("30594-8c.htm"))
			{
				if (st.hasQuestItems(STEAK_RECIPE))
				{
					st.set("cond", "4");
					st.set("score", String.valueOf(score + 32));
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(STEAK_RECIPE, 1);
				}
				else
					htmltext = "30594-8.htm";
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
				htmltext = (player.getLevel() < 20) ? "30594-0.htm" : "30594-1.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "30594-3.htm";
				else if (cond == 2)
					htmltext = (!st.hasQuestItems(MUSICAL_SCORE)) ? "30594-5.htm" : "30594-5a.htm";
				else if (cond == 3)
					htmltext = "30594-7.htm";
				else if (cond == 4)
				{
					final String score = st.get("score");
					if (REWARDS.containsKey(score) && st.hasQuestItems(RITRON_DESSERT))
					{
						htmltext = "30594-10.htm";
						
						st.takeItems(RITRON_DESSERT, 1);
						st.giveItems(REWARDS.get(score)[0], REWARDS.get(score)[1]);
						
						int adena = REWARDS.get(score)[2];
						if (adena > 0)
							st.rewardItems(57, adena);
						
						st.playSound(QuestState.SOUND_FINISH);
						st.exitQuest(true);
					}
					else
						htmltext = "30594-9.htm";
				}
		}
		
		return htmltext;
	}
}
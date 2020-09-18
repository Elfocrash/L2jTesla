package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q627_HeartInSearchOfPower extends Quest
{
	private static final String qn = "Q627_HeartInSearchOfPower";
	
	// NPCs
	private static final int NECROMANCER = 31518;
	private static final int ENFEUX = 31519;
	
	// Items
	private static final int SEAL_OF_LIGHT = 7170;
	private static final int BEAD_OF_OBEDIENCE = 7171;
	private static final int GEM_OF_SAINTS = 7172;
	
	// Drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	{
		CHANCES.put(21520, 550000);
		CHANCES.put(21523, 584000);
		CHANCES.put(21524, 621000);
		CHANCES.put(21525, 621000);
		CHANCES.put(21526, 606000);
		CHANCES.put(21529, 625000);
		CHANCES.put(21530, 578000);
		CHANCES.put(21531, 690000);
		CHANCES.put(21532, 671000);
		CHANCES.put(21535, 693000);
		CHANCES.put(21536, 615000);
		CHANCES.put(21539, 762000);
		CHANCES.put(21540, 762000);
		CHANCES.put(21658, 690000);
	}
	
	// Rewards
	private static final Map<String, int[]> REWARDS = new HashMap<>();
	{
		REWARDS.put("adena", new int[]
		{
			0,
			0,
			100000
		});
		REWARDS.put("asofe", new int[]
		{
			4043,
			13,
			6400
		});
		REWARDS.put("thon", new int[]
		{
			4044,
			13,
			6400
		});
		REWARDS.put("enria", new int[]
		{
			4042,
			6,
			13600
		});
		REWARDS.put("mold", new int[]
		{
			4041,
			3,
			17200
		});
	}
	
	public Q627_HeartInSearchOfPower()
	{
		super(627, "Heart in Search of Power");
		
		setItemsIds(BEAD_OF_OBEDIENCE);
		
		addStartNpc(NECROMANCER);
		addTalkId(NECROMANCER, ENFEUX);
		
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
		
		if (event.equalsIgnoreCase("31518-01.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31518-03.htm"))
		{
			if (st.getQuestItemsCount(BEAD_OF_OBEDIENCE) == 300)
			{
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BEAD_OF_OBEDIENCE, -1);
				st.giveItems(SEAL_OF_LIGHT, 1);
			}
			else
			{
				htmltext = "31518-03a.htm";
				st.set("cond", "1");
				st.takeItems(BEAD_OF_OBEDIENCE, -1);
			}
		}
		else if (event.equalsIgnoreCase("31519-01.htm"))
		{
			if (st.getQuestItemsCount(SEAL_OF_LIGHT) == 1)
			{
				st.set("cond", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(SEAL_OF_LIGHT, 1);
				st.giveItems(GEM_OF_SAINTS, 1);
			}
		}
		else if (REWARDS.containsKey(event))
		{
			if (st.getQuestItemsCount(GEM_OF_SAINTS) == 1)
			{
				htmltext = "31518-07.htm";
				st.takeItems(GEM_OF_SAINTS, 1);
				
				if (REWARDS.get(event)[0] > 0)
					st.giveItems(REWARDS.get(event)[0], REWARDS.get(event)[1]);
				st.rewardItems(57, REWARDS.get(event)[2]);
				
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				htmltext = "31518-7.htm";
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
				htmltext = (player.getLevel() < 60) ? "31518-00a.htm" : "31518-00.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case NECROMANCER:
						if (cond == 1)
							htmltext = "31518-01a.htm";
						else if (cond == 2)
							htmltext = "31518-02.htm";
						else if (cond == 3)
							htmltext = "31518-04.htm";
						else if (cond == 4)
							htmltext = "31518-05.htm";
						break;
					
					case ENFEUX:
						if (cond == 3)
							htmltext = "31519-00.htm";
						else if (cond == 4)
							htmltext = "31519-02.htm";
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
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (st.dropItems(BEAD_OF_OBEDIENCE, 1, 300, CHANCES.get(npc.getNpcId())))
			st.set("cond", "2");
		
		return null;
	}
}
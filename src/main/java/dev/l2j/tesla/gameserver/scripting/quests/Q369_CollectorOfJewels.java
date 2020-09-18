package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q369_CollectorOfJewels extends Quest
{
	private static final String qn = "Q369_CollectorOfJewels";
	
	// NPC
	private static final int NELL = 30376;
	
	// Items
	private static final int FLARE_SHARD = 5882;
	private static final int FREEZING_SHARD = 5883;
	
	// Reward
	private static final int ADENA = 57;
	
	// Droplist
	private static final Map<Integer, int[]> DROPLIST = new HashMap<>();
	{
		DROPLIST.put(20609, new int[]
		{
			FLARE_SHARD,
			630000
		});
		DROPLIST.put(20612, new int[]
		{
			FLARE_SHARD,
			770000
		});
		DROPLIST.put(20749, new int[]
		{
			FLARE_SHARD,
			850000
		});
		DROPLIST.put(20616, new int[]
		{
			FREEZING_SHARD,
			600000
		});
		DROPLIST.put(20619, new int[]
		{
			FREEZING_SHARD,
			730000
		});
		DROPLIST.put(20747, new int[]
		{
			FREEZING_SHARD,
			850000
		});
	}
	
	public Q369_CollectorOfJewels()
	{
		super(369, "Collector of Jewels");
		
		setItemsIds(FLARE_SHARD, FREEZING_SHARD);
		
		addStartNpc(NELL);
		addTalkId(NELL);
		
		for (int mob : DROPLIST.keySet())
			addKillId(mob);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30376-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30376-07.htm"))
			st.playSound(QuestState.SOUND_ITEMGET);
		else if (event.equalsIgnoreCase("30376-08.htm"))
		{
			st.exitQuest(true);
			st.playSound(QuestState.SOUND_FINISH);
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
				htmltext = (player.getLevel() < 25) ? "30376-01.htm" : "30376-02.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				final int flare = st.getQuestItemsCount(FLARE_SHARD);
				final int freezing = st.getQuestItemsCount(FREEZING_SHARD);
				
				if (cond == 1)
					htmltext = "30376-04.htm";
				else if (cond == 2 && flare >= 50 && freezing >= 50)
				{
					htmltext = "30376-05.htm";
					st.set("cond", "3");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(FLARE_SHARD, -1);
					st.takeItems(FREEZING_SHARD, -1);
					st.rewardItems(ADENA, 12500);
				}
				else if (cond == 3)
					htmltext = "30376-09.htm";
				else if (cond == 4 && flare >= 200 && freezing >= 200)
				{
					htmltext = "30376-10.htm";
					st.takeItems(FLARE_SHARD, -1);
					st.takeItems(FREEZING_SHARD, -1);
					st.rewardItems(ADENA, 63500);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
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
		
		final int cond = st.getInt("cond");
		final int[] drop = DROPLIST.get(npc.getNpcId());
		
		if (cond == 1)
		{
			if (st.dropItems(drop[0], 1, 50, drop[1]) && st.getQuestItemsCount((drop[0] == FLARE_SHARD) ? FREEZING_SHARD : FLARE_SHARD) >= 50)
				st.set("cond", "2");
		}
		else if (cond == 3 && st.dropItems(drop[0], 1, 200, drop[1]) && st.getQuestItemsCount((drop[0] == FLARE_SHARD) ? FREEZING_SHARD : FLARE_SHARD) >= 200)
			st.set("cond", "4");
		
		return null;
	}
}
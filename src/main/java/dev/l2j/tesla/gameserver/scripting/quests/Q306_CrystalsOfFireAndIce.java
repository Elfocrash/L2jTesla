package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q306_CrystalsOfFireAndIce extends Quest
{
	private static final String qn = "Q306_CrystalsOfFireAndIce";
	
	// Items
	private static final int FLAME_SHARD = 1020;
	private static final int ICE_SHARD = 1021;
	
	// Droplist (npcId, itemId, chance)
	private static final int[][] DROPLIST =
	{
		{
			20109,
			FLAME_SHARD,
			300000
		},
		{
			20110,
			ICE_SHARD,
			300000
		},
		{
			20112,
			FLAME_SHARD,
			400000
		},
		{
			20113,
			ICE_SHARD,
			400000
		},
		{
			20114,
			FLAME_SHARD,
			500000
		},
		{
			20115,
			ICE_SHARD,
			500000
		}
	};
	
	public Q306_CrystalsOfFireAndIce()
	{
		super(306, "Crystals of Fire and Ice");
		
		setItemsIds(FLAME_SHARD, ICE_SHARD);
		
		addStartNpc(30004); // Katerina
		addTalkId(30004);
		
		addKillId(20109, 20110, 20112, 20113, 20114, 20115);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30004-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30004-06.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				htmltext = (player.getLevel() < 17) ? "30004-01.htm" : "30004-02.htm";
				break;
			
			case STATE_STARTED:
				final int totalItems = st.getQuestItemsCount(FLAME_SHARD) + st.getQuestItemsCount(ICE_SHARD);
				if (totalItems == 0)
					htmltext = "30004-04.htm";
				else
				{
					htmltext = "30004-05.htm";
					st.takeItems(FLAME_SHARD, -1);
					st.takeItems(ICE_SHARD, -1);
					st.rewardItems(57, 30 * totalItems + ((totalItems > 10) ? 5000 : 0));
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
		
		for (int[] drop : DROPLIST)
		{
			if (npc.getNpcId() == drop[0])
			{
				st.dropItems(drop[1], 1, 0, drop[2]);
				break;
			}
		}
		
		return null;
	}
}
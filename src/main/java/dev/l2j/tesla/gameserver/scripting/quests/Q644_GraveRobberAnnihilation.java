package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q644_GraveRobberAnnihilation extends Quest
{
	private static final String qn = "Q644_GraveRobberAnnihilation";
	
	// Item
	private static final int ORC_GRAVE_GOODS = 8088;
	
	// Rewards
	private static final int[][] REWARDS =
	{
		{
			1865,
			30
		},
		{
			1867,
			40
		},
		{
			1872,
			40
		},
		{
			1871,
			30
		},
		{
			1870,
			30
		},
		{
			1869,
			30
		}
	};
	
	// NPC
	private static final int KARUDA = 32017;
	
	public Q644_GraveRobberAnnihilation()
	{
		super(644, "Grave Robber Annihilation");
		
		setItemsIds(ORC_GRAVE_GOODS);
		
		addStartNpc(KARUDA);
		addTalkId(KARUDA);
		
		addKillId(22003, 22004, 22005, 22006, 22008);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32017-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (StringUtil.isDigit(event))
		{
			htmltext = "32017-04.htm";
			st.takeItems(ORC_GRAVE_GOODS, -1);
			
			final int reward[] = REWARDS[Integer.parseInt(event)];
			st.rewardItems(reward[0], reward[1]);
			
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
				htmltext = (player.getLevel() < 20) ? "32017-06.htm" : "32017-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "32017-05.htm";
				else if (cond == 2)
					htmltext = "32017-07.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = getRandomPartyMember(player, npc, "1");
		if (st == null)
			return null;
		
		if (st.dropItems(ORC_GRAVE_GOODS, 1, 120, 500000))
			st.set("cond", "2");
		
		return null;
	}
}
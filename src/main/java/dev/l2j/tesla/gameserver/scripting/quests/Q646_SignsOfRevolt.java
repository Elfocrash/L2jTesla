package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q646_SignsOfRevolt extends Quest
{
	private static final String qn = "Q646_SignsOfRevolt";
	
	// NPC
	private static final int TORRANT = 32016;
	
	// Item
	private static final int CURSED_DOLL = 8087;
	
	// Rewards
	private static final int[][] REWARDS =
	{
		{
			1880,
			9
		},
		{
			1881,
			12
		},
		{
			1882,
			20
		},
		{
			57,
			21600
		}
	};
	
	public Q646_SignsOfRevolt()
	{
		super(646, "Signs Of Revolt");
		
		setItemsIds(CURSED_DOLL);
		
		addStartNpc(TORRANT);
		addTalkId(TORRANT);
		
		addKillId(22029, 22030, 22031, 22032, 22033, 22034, 22035, 22036, 22037, 22038, 22039, 22040, 22041, 22042, 22043, 22044, 22045, 22047, 22049);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32016-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (StringUtil.isDigit(event))
		{
			htmltext = "32016-07.htm";
			st.takeItems(CURSED_DOLL, -1);
			
			final int reward[] = REWARDS[Integer.parseInt(event)];
			st.giveItems(reward[0], reward[1]);
			
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
				htmltext = (player.getLevel() < 40) ? "32016-02.htm" : "32016-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "32016-04.htm";
				else if (cond == 2)
					htmltext = "32016-05.htm";
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
		
		if (st.dropItems(CURSED_DOLL, 1, 180, 750000))
			st.set("cond", "2");
		
		return null;
	}
}
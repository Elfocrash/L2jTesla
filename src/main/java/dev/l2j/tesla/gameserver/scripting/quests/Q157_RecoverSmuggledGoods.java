package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q157_RecoverSmuggledGoods extends Quest
{
	private static final String qn = "Q157_RecoverSmuggledGoods";
	
	// Item
	private static final int ADAMANTITE_ORE = 1024;
	
	// Reward
	private static final int BUCKLER = 20;
	
	public Q157_RecoverSmuggledGoods()
	{
		super(157, "Recover Smuggled Goods");
		
		setItemsIds(ADAMANTITE_ORE);
		
		addStartNpc(30005); // Wilford
		addTalkId(30005);
		
		addKillId(20121); // Toad
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30005-05.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
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
				htmltext = (player.getLevel() < 5) ? "30005-02.htm" : "30005-03.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "30005-06.htm";
				else if (cond == 2)
				{
					htmltext = "30005-07.htm";
					st.takeItems(ADAMANTITE_ORE, -1);
					st.giveItems(BUCKLER, 1);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
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
		
		if (st.dropItems(ADAMANTITE_ORE, 1, 20, 400000))
			st.set("cond", "2");
		
		return null;
	}
}
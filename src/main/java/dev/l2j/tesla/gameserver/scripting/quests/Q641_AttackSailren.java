package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillUse;

public final class Q641_AttackSailren extends Quest
{
	private static final String qn = "Q641_AttackSailren";
	
	// NPCs
	private static final int STATUE = 32109;
	
	// Quest Item
	private static final int GAZKH_FRAGMENT = 8782;
	private static final int GAZKH = 8784;
	
	public Q641_AttackSailren()
	{
		super(641, "Attack Sailren!");
		
		setItemsIds(GAZKH_FRAGMENT);
		
		addStartNpc(STATUE);
		addTalkId(STATUE);
		
		addKillId(22196, 22197, 22198, 22199, 22218, 22223);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		
		if (event.equalsIgnoreCase("32109-5.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32109-8.htm"))
		{
			if (st.getQuestItemsCount(GAZKH_FRAGMENT) >= 30)
			{
				npc.broadcastPacket(new MagicSkillUse(npc, player, 5089, 1, 3000, 0));
				st.takeItems(GAZKH_FRAGMENT, -1);
				st.giveItems(GAZKH, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
			{
				htmltext = "32109-6.htm";
				st.set("cond", "1");
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
				if (player.getLevel() < 77)
					htmltext = "32109-3.htm";
				else
				{
					QuestState st2 = player.getQuestState(Q126_TheNameOfEvil_2.qn);
					htmltext = (st2 != null && st2.isCompleted()) ? "32109-1.htm" : "32109-2.htm";
				}
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "32109-5.htm";
				else if (cond == 2)
					htmltext = "32109-7.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = getRandomPartyMember(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (st.dropItems(GAZKH_FRAGMENT, 1, 30, 50000))
			st.set("cond", "2");
		
		return null;
	}
}
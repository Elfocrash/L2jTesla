package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q367_ElectrifyingRecharge extends Quest
{
	private static final String qn = "Q367_ElectrifyingRecharge";
	
	// NPCs
	private static final int LORAIN = 30673;
	
	// Item
	private static final int LORAIN_LAMP = 5875;
	private static final int TITAN_LAMP_1 = 5876;
	private static final int TITAN_LAMP_2 = 5877;
	private static final int TITAN_LAMP_3 = 5878;
	private static final int TITAN_LAMP_4 = 5879;
	private static final int TITAN_LAMP_5 = 5880;
	
	// Reward
	private static final int REWARDS[] =
	{
		4553,
		4554,
		4555,
		4556,
		4557,
		4558,
		4559,
		4560,
		4561,
		4562,
		4563,
		4564
	};
	
	// Mobs
	private static final int CATHEROK = 21035;
	
	public Q367_ElectrifyingRecharge()
	{
		super(367, "Electrifying Recharge!");
		
		setItemsIds(LORAIN_LAMP, TITAN_LAMP_1, TITAN_LAMP_2, TITAN_LAMP_3, TITAN_LAMP_4, TITAN_LAMP_5);
		
		addStartNpc(LORAIN);
		addTalkId(LORAIN);
		
		addSpellFinishedId(CATHEROK);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30673-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LORAIN_LAMP, 1);
		}
		else if (event.equalsIgnoreCase("30673-09.htm"))
		{
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LORAIN_LAMP, 1);
		}
		else if (event.equalsIgnoreCase("30673-08.htm"))
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30673-07.htm"))
		{
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LORAIN_LAMP, 1);
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
				htmltext = (player.getLevel() < 37) ? "30673-02.htm" : "30673-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					if (st.hasQuestItems(5880))
					{
						htmltext = "30673-05.htm";
						st.playSound(QuestState.SOUND_ACCEPT);
						st.takeItems(5880, 1);
						st.giveItems(LORAIN_LAMP, 1);
					}
					else if (st.hasQuestItems(5876))
					{
						htmltext = "30673-04.htm";
						st.takeItems(5876, 1);
					}
					else if (st.hasQuestItems(5877))
					{
						htmltext = "30673-04.htm";
						st.takeItems(5877, 1);
					}
					else if (st.hasQuestItems(5878))
					{
						htmltext = "30673-04.htm";
						st.takeItems(5878, 1);
					}
					else
						htmltext = "30673-03.htm";
				}
				else if (cond == 2 && st.hasQuestItems(5879))
				{
					htmltext = "30673-06.htm";
					st.takeItems(5879, 1);
					st.rewardItems(Rnd.get(REWARDS), 1);
					st.playSound(QuestState.SOUND_FINISH);
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, L2Skill skill)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (skill.getId() == 4072)
		{
			if (st.hasQuestItems(LORAIN_LAMP))
			{
				int randomItem = Rnd.get(5876, 5880);
				
				st.takeItems(LORAIN_LAMP, 1);
				st.giveItems(randomItem, 1);
				
				if (randomItem == 5879)
				{
					st.set("cond", "2");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else
					st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		
		return null;
	}
}
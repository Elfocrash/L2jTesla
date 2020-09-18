package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q381_LetsBecomeARoyalMember extends Quest
{
	private static final String qn = "Q381_LetsBecomeARoyalMember";
	
	// NPCs
	private static final int SORINT = 30232;
	private static final int SANDRA = 30090;
	
	// Items
	private static final int KAIL_COIN = 5899;
	private static final int COIN_ALBUM = 5900;
	private static final int GOLDEN_CLOVER_COIN = 7569;
	private static final int COIN_COLLECTOR_MEMBERSHIP = 3813;
	
	// Reward
	private static final int ROYAL_MEMBERSHIP = 5898;
	
	public Q381_LetsBecomeARoyalMember()
	{
		super(381, "Lets Become a Royal Member!");
		
		setItemsIds(KAIL_COIN, GOLDEN_CLOVER_COIN);
		
		addStartNpc(SORINT);
		addTalkId(SORINT, SANDRA);
		
		addKillId(21018, 27316);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30090-02.htm"))
			st.set("aCond", "1"); // Alternative cond used for Sandra.
		else if (event.equalsIgnoreCase("30232-03.htm"))
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
				htmltext = (player.getLevel() < 55 || !st.hasQuestItems(COIN_COLLECTOR_MEMBERSHIP)) ? "30232-02.htm" : "30232-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case SORINT:
						if (!st.hasQuestItems(KAIL_COIN))
							htmltext = "30232-04.htm";
						else if (!st.hasQuestItems(COIN_ALBUM))
							htmltext = "30232-05.htm";
						else
						{
							htmltext = "30232-06.htm";
							st.takeItems(KAIL_COIN, -1);
							st.takeItems(COIN_ALBUM, -1);
							st.giveItems(ROYAL_MEMBERSHIP, 1);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case SANDRA:
						if (!st.hasQuestItems(COIN_ALBUM))
						{
							if (st.getInt("aCond") == 0)
								htmltext = "30090-01.htm";
							else
							{
								if (!st.hasQuestItems(GOLDEN_CLOVER_COIN))
									htmltext = "30090-03.htm";
								else
								{
									htmltext = "30090-04.htm";
									st.takeItems(GOLDEN_CLOVER_COIN, -1);
									st.giveItems(COIN_ALBUM, 1);
								}
							}
						}
						else
							htmltext = "30090-05.htm";
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
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		if (npc.getNpcId() == 21018)
			st.dropItems(KAIL_COIN, 1, 1, 50000);
		else if (st.getInt("aCond") == 1)
			st.dropItemsAlways(GOLDEN_CLOVER_COIN, 1, 1);
		
		return null;
	}
}
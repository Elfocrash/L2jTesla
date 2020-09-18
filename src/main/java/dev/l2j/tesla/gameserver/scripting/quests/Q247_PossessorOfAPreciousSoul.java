package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q247_PossessorOfAPreciousSoul extends Quest
{
	private static final String qn = "Q247_PossessorOfAPreciousSoul";
	
	// NPCs
	private static final int CARADINE = 31740;
	private static final int LADY_OF_THE_LAKE = 31745;
	
	// Items
	private static final int CARADINE_LETTER = 7679;
	private static final int NOBLESS_TIARA = 7694;
	
	public Q247_PossessorOfAPreciousSoul()
	{
		super(247, "Possessor of a Precious Soul - 4");
		
		addStartNpc(CARADINE);
		addTalkId(CARADINE, LADY_OF_THE_LAKE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// Caradine
		if (event.equalsIgnoreCase("31740-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.takeItems(CARADINE_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("31740-05.htm"))
		{
			st.set("cond", "2");
			player.teleportTo(143209, 43968, -3038, 0);
		}
		// Lady of the lake
		else if (event.equalsIgnoreCase("31745-05.htm"))
		{
			player.setNoble(true, true);
			st.giveItems(NOBLESS_TIARA, 1);
			st.rewardExpAndSp(93836, 0);
			player.broadcastPacket(new SocialAction(player, 3));
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				if (st.hasQuestItems(CARADINE_LETTER))
					htmltext = (!player.isSubClassActive() || player.getLevel() < 75) ? "31740-02.htm" : "31740-01.htm";
				break;
			
			case STATE_STARTED:
				if (!player.isSubClassActive())
					break;
				
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case CARADINE:
						if (cond == 1)
							htmltext = "31740-04.htm";
						else if (cond == 2)
							htmltext = "31740-06.htm";
						break;
					
					case LADY_OF_THE_LAKE:
						if (cond == 2)
							htmltext = (player.getLevel() < 75) ? "31745-06.htm" : "31745-01.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
}
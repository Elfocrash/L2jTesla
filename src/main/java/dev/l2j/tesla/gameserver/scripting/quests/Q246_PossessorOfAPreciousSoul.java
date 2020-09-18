package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q246_PossessorOfAPreciousSoul extends Quest
{
	private static final String qn = "Q246_PossessorOfAPreciousSoul";
	
	// NPCs
	private static final int CARADINE = 31740;
	private static final int OSSIAN = 31741;
	private static final int LADD = 30721;
	
	// Items
	private static final int WATERBINDER = 7591;
	private static final int EVERGREEN = 7592;
	private static final int RAIN_SONG = 7593;
	private static final int RELIC_BOX = 7594;
	private static final int CARADINE_LETTER_1 = 7678;
	private static final int CARADINE_LETTER_2 = 7679;
	
	// Mobs
	private static final int PILGRIM_OF_SPLENDOR = 21541;
	private static final int JUDGE_OF_SPLENDOR = 21544;
	private static final int BARAKIEL = 25325;
	
	public Q246_PossessorOfAPreciousSoul()
	{
		super(246, "Possessor of a Precious Soul - 3");
		
		setItemsIds(WATERBINDER, EVERGREEN, RAIN_SONG, RELIC_BOX);
		
		addStartNpc(CARADINE);
		addTalkId(CARADINE, OSSIAN, LADD);
		
		addKillId(PILGRIM_OF_SPLENDOR, JUDGE_OF_SPLENDOR, BARAKIEL);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// Caradine
		if (event.equalsIgnoreCase("31740-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.takeItems(CARADINE_LETTER_1, 1);
		}
		// Ossian
		else if (event.equalsIgnoreCase("31741-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31741-05.htm"))
		{
			if (st.hasQuestItems(WATERBINDER, EVERGREEN))
			{
				st.set("cond", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(WATERBINDER, 1);
				st.takeItems(EVERGREEN, 1);
			}
			else
				htmltext = null;
		}
		else if (event.equalsIgnoreCase("31741-08.htm"))
		{
			if (st.hasQuestItems(RAIN_SONG))
			{
				st.set("cond", "6");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(RAIN_SONG, 1);
				st.giveItems(RELIC_BOX, 1);
			}
			else
				htmltext = null;
		}
		// Ladd
		else if (event.equalsIgnoreCase("30721-02.htm"))
		{
			if (st.hasQuestItems(RELIC_BOX))
			{
				st.takeItems(RELIC_BOX, 1);
				st.giveItems(CARADINE_LETTER_2, 1);
				st.rewardExpAndSp(719843, 0);
				player.broadcastPacket(new SocialAction(player, 3));
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
			}
			else
				htmltext = null;
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
				if (st.hasQuestItems(CARADINE_LETTER_1))
					htmltext = (!player.isSubClassActive() || player.getLevel() < 65) ? "31740-02.htm" : "31740-01.htm";
				break;
			
			case STATE_STARTED:
				if (!player.isSubClassActive())
					break;
				
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case CARADINE:
						if (cond == 1)
							htmltext = "31740-05.htm";
						break;
					
					case OSSIAN:
						if (cond == 1)
							htmltext = "31741-01.htm";
						else if (cond == 2)
							htmltext = "31741-03.htm";
						else if (cond == 3)
						{
							if (st.hasQuestItems(WATERBINDER, EVERGREEN))
								htmltext = "31741-04.htm";
						}
						else if (cond == 4)
							htmltext = "31741-06.htm";
						else if (cond == 5)
						{
							if (st.hasQuestItems(RAIN_SONG))
								htmltext = "31741-07.htm";
						}
						else if (cond == 6)
							htmltext = "31741-09.htm";
						break;
					
					case LADD:
						if (cond == 6 && st.hasQuestItems(RELIC_BOX))
							htmltext = "30721-01.htm";
						break;
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
		final int npcId = npc.getNpcId();
		
		if (npcId == BARAKIEL)
		{
			for (QuestState st : getPartyMembers(player, npc, "cond", "4"))
			{
				if (!st.getPlayer().isSubClassActive())
					continue;
				
				if (!st.hasQuestItems(RAIN_SONG))
				{
					st.set("cond", "5");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(RAIN_SONG, 1);
				}
			}
		}
		else
		{
			if (!player.isSubClassActive())
				return null;
			
			QuestState st = checkPlayerCondition(player, npc, "cond", "2");
			if (st == null)
				return null;
			
			if (Rnd.get(10) < 2)
			{
				final int neklaceOrRing = (npcId == PILGRIM_OF_SPLENDOR) ? WATERBINDER : EVERGREEN;
				
				if (!st.hasQuestItems(neklaceOrRing))
				{
					st.giveItems(neklaceOrRing, 1);
					
					if (!st.hasQuestItems((npcId == PILGRIM_OF_SPLENDOR) ? EVERGREEN : WATERBINDER))
						st.playSound(QuestState.SOUND_ITEMGET);
					else
					{
						st.set("cond", "3");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
				}
			}
		}
		return null;
	}
}
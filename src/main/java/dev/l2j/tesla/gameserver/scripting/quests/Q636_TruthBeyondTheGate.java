package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.zone.ZoneType;

public class Q636_TruthBeyondTheGate extends Quest
{
	private static final String qn = "Q636_TruthBeyondTheGate";
	
	// NPCs
	private static final int ELIYAH = 31329;
	private static final int FLAURON = 32010;
	
	// Reward
	private static final int VISITOR_MARK = 8064;
	private static final int FADED_VISITOR_MARK = 8065;
	
	public Q636_TruthBeyondTheGate()
	{
		super(636, "The Truth Beyond the Gate");
		
		addStartNpc(ELIYAH);
		addTalkId(ELIYAH, FLAURON);
		
		addEnterZoneId(100000);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31329-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32010-02.htm"))
		{
			st.giveItems(VISITOR_MARK, 1);
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
				htmltext = (player.getLevel() < 73) ? "31329-01.htm" : "31329-02.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case ELIYAH:
						htmltext = "31329-05.htm";
						break;
					
					case FLAURON:
						htmltext = (st.hasQuestItems(VISITOR_MARK)) ? "32010-03.htm" : "32010-01.htm";
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
	public final String onEnterZone(Creature character, ZoneType zone)
	{
		// QuestState already null on enter because quest is finished
		if (character instanceof Player)
		{
			if (character.getActingPlayer().destroyItemByItemId("Mark", VISITOR_MARK, 1, character, false))
				character.getActingPlayer().addItem("Mark", FADED_VISITOR_MARK, 1, character, true);
		}
		return null;
	}
}
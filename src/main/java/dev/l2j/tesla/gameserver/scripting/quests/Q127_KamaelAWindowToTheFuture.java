package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.ExPlayScene;

public class Q127_KamaelAWindowToTheFuture extends Quest
{
	private static final String qn = "Q127_KamaelAWindowToTheFuture";
	
	// NPCs
	private static final int DOMINIC = 31350;
	private static final int KLAUS = 30187;
	private static final int ALDER = 32092;
	private static final int AKLAN = 31288;
	private static final int OLTLIN = 30862;
	private static final int JURIS = 30113;
	private static final int RODEMAI = 30756;
	
	// Items
	private static final int MARK_DOMINIC = 8939;
	private static final int MARK_HUMAN = 8940;
	private static final int MARK_DWARF = 8941;
	private static final int MARK_ORC = 8944;
	private static final int MARK_DELF = 8943;
	private static final int MARK_ELF = 8942;
	
	public Q127_KamaelAWindowToTheFuture()
	{
		super(127, "Kamael: A Window to the Future");
		
		setItemsIds(MARK_DOMINIC, MARK_HUMAN, MARK_DWARF, MARK_ORC, MARK_DELF, MARK_ELF);
		
		addStartNpc(DOMINIC);
		addTalkId(DOMINIC, KLAUS, ALDER, AKLAN, OLTLIN, JURIS, RODEMAI);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31350-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(MARK_DOMINIC, 1);
		}
		else if (event.equalsIgnoreCase("31350-06.htm"))
		{
			st.takeItems(MARK_HUMAN, -1);
			st.takeItems(MARK_DWARF, -1);
			st.takeItems(MARK_ELF, -1);
			st.takeItems(MARK_DELF, -1);
			st.takeItems(MARK_ORC, -1);
			st.takeItems(MARK_DOMINIC, -1);
			st.rewardItems(57, 159100);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		else if (event.equalsIgnoreCase("30187-06.htm"))
			st.set("cond", "2");
		else if (event.equalsIgnoreCase("30187-08.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(MARK_HUMAN, 1);
		}
		else if (event.equalsIgnoreCase("32092-05.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(MARK_DWARF, 1);
		}
		else if (event.equalsIgnoreCase("31288-04.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(MARK_ORC, 1);
		}
		else if (event.equalsIgnoreCase("30862-04.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(MARK_DELF, 1);
		}
		else if (event.equalsIgnoreCase("30113-04.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(MARK_ELF, 1);
		}
		else if (event.equalsIgnoreCase("kamaelstory"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			player.sendPacket(ExPlayScene.STATIC_PACKET);
			return null;
		}
		else if (event.equalsIgnoreCase("30756-05.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
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
		
		npc.getNpcId();
		int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case STATE_CREATED:
				htmltext = "31350-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case KLAUS:
						if (cond == 1)
							htmltext = "30187-01.htm";
						else if (cond == 2)
							htmltext = "30187-06.htm";
						break;
					
					case ALDER:
						if (cond == 3)
							htmltext = "32092-01.htm";
						break;
					
					case AKLAN:
						if (cond == 4)
							htmltext = "31288-01.htm";
						break;
					
					case OLTLIN:
						if (cond == 5)
							htmltext = "30862-01.htm";
						break;
					
					case JURIS:
						if (cond == 6)
							htmltext = "30113-01.htm";
						break;
					
					case RODEMAI:
						if (cond == 7)
							htmltext = "30756-01.htm";
						else if (cond == 8)
							htmltext = "30756-04.htm";
						break;
					
					case DOMINIC:
						if (cond == 9)
							htmltext = "31350-05.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				return htmltext;
		}
		
		return htmltext;
	}
}
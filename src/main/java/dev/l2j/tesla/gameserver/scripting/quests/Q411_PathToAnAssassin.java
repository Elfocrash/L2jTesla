package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q411_PathToAnAssassin extends Quest
{
	private static final String qn = "Q411_PathToAnAssassin";
	
	// Items
	private static final int SHILEN_CALL = 1245;
	private static final int ARKENIA_LETTER = 1246;
	private static final int LEIKAN_NOTE = 1247;
	private static final int MOONSTONE_BEAST_MOLAR = 1248;
	private static final int SHILEN_TEARS = 1250;
	private static final int ARKENIA_RECOMMENDATION = 1251;
	private static final int IRON_HEART = 1252;
	
	// NPCs
	private static final int TRISKEL = 30416;
	private static final int ARKENIA = 30419;
	private static final int LEIKAN = 30382;
	
	public Q411_PathToAnAssassin()
	{
		super(411, "Path to an Assassin");
		
		setItemsIds(SHILEN_CALL, ARKENIA_LETTER, LEIKAN_NOTE, MOONSTONE_BEAST_MOLAR, SHILEN_TEARS, ARKENIA_RECOMMENDATION);
		
		addStartNpc(TRISKEL);
		addTalkId(TRISKEL, ARKENIA, LEIKAN);
		
		addKillId(27036, 20369);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30416-05.htm"))
		{
			if (player.getClassId() != ClassId.DARK_FIGHTER)
				htmltext = (player.getClassId() == ClassId.ASSASSIN) ? "30416-02a.htm" : "30416-02.htm";
			else if (player.getLevel() < 19)
				htmltext = "30416-03.htm";
			else if (st.hasQuestItems(IRON_HEART))
				htmltext = "30416-04.htm";
			else
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
				st.giveItems(SHILEN_CALL, 1);
			}
		}
		else if (event.equalsIgnoreCase("30419-05.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(SHILEN_CALL, 1);
			st.giveItems(ARKENIA_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("30382-03.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ARKENIA_LETTER, 1);
			st.giveItems(LEIKAN_NOTE, 1);
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
				htmltext = "30416-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case TRISKEL:
						if (cond == 1)
							htmltext = "30416-11.htm";
						else if (cond == 2)
							htmltext = "30416-07.htm";
						else if (cond == 3 || cond == 4)
							htmltext = "30416-08.htm";
						else if (cond == 5)
							htmltext = "30416-09.htm";
						else if (cond == 6)
							htmltext = "30416-10.htm";
						else if (cond == 7)
						{
							htmltext = "30416-06.htm";
							st.takeItems(ARKENIA_RECOMMENDATION, 1);
							st.giveItems(IRON_HEART, 1);
							st.rewardExpAndSp(3200, 3930);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case ARKENIA:
						if (cond == 1)
							htmltext = "30419-01.htm";
						else if (cond == 2)
							htmltext = "30419-07.htm";
						else if (cond == 3 || cond == 4)
							htmltext = "30419-10.htm";
						else if (cond == 5)
							htmltext = "30419-11.htm";
						else if (cond == 6)
						{
							htmltext = "30419-08.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SHILEN_TEARS, -1);
							st.giveItems(ARKENIA_RECOMMENDATION, 1);
						}
						else if (cond == 7)
							htmltext = "30419-09.htm";
						break;
					
					case LEIKAN:
						if (cond == 2)
							htmltext = "30382-01.htm";
						else if (cond == 3)
							htmltext = (!st.hasQuestItems(MOONSTONE_BEAST_MOLAR)) ? "30382-05.htm" : "30382-06.htm";
						else if (cond == 4)
						{
							htmltext = "30382-07.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(MOONSTONE_BEAST_MOLAR, -1);
							st.takeItems(LEIKAN_NOTE, -1);
						}
						else if (cond == 5)
							htmltext = "30382-09.htm";
						else if (cond > 5)
							htmltext = "30382-08.htm";
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
		
		if (npc.getNpcId() == 20369)
		{
			if (st.getInt("cond") == 3 && st.dropItemsAlways(MOONSTONE_BEAST_MOLAR, 1, 10))
				st.set("cond", "4");
		}
		else if (st.getInt("cond") == 5)
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(SHILEN_TEARS, 1);
		}
		
		return null;
	}
}
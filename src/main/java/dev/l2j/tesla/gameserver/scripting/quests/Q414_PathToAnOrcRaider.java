package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q414_PathToAnOrcRaider extends Quest
{
	private static final String qn = "Q414_PathToAnOrcRaider";
	
	// Items
	private static final int GREEN_BLOOD = 1578;
	private static final int GOBLIN_DWELLING_MAP = 1579;
	private static final int KURUKA_RATMAN_TOOTH = 1580;
	private static final int BETRAYER_REPORT_1 = 1589;
	private static final int BETRAYER_REPORT_2 = 1590;
	private static final int HEAD_OF_BETRAYER = 1591;
	private static final int MARK_OF_RAIDER = 1592;
	private static final int TIMORA_ORC_HEAD = 8544;
	
	// NPCs
	private static final int KARUKIA = 30570;
	private static final int KASMAN = 30501;
	private static final int TAZEER = 31978;
	
	// Monsters
	private static final int GOBLIN_TOMB_RAIDER_LEADER = 20320;
	private static final int KURUKA_RATMAN_LEADER = 27045;
	private static final int UMBAR_ORC = 27054;
	private static final int TIMORA_ORC = 27320;
	
	public Q414_PathToAnOrcRaider()
	{
		super(414, "Path To An Orc Raider");
		
		setItemsIds(GREEN_BLOOD, GOBLIN_DWELLING_MAP, KURUKA_RATMAN_TOOTH, BETRAYER_REPORT_1, BETRAYER_REPORT_2, HEAD_OF_BETRAYER, TIMORA_ORC_HEAD);
		
		addStartNpc(KARUKIA);
		addTalkId(KARUKIA, KASMAN, TAZEER);
		
		addKillId(GOBLIN_TOMB_RAIDER_LEADER, KURUKA_RATMAN_LEADER, UMBAR_ORC, TIMORA_ORC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// KARUKIA
		if (event.equalsIgnoreCase("30570-05.htm"))
		{
			if (player.getClassId() != ClassId.ORC_FIGHTER)
				htmltext = (player.getClassId() == ClassId.ORC_RAIDER) ? "30570-02a.htm" : "30570-03.htm";
			else if (player.getLevel() < 19)
				htmltext = "30570-02.htm";
			else if (st.hasQuestItems(MARK_OF_RAIDER))
				htmltext = "30570-04.htm";
			else
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
				st.giveItems(GOBLIN_DWELLING_MAP, 1);
			}
		}
		else if (event.equalsIgnoreCase("30570-07a.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(GOBLIN_DWELLING_MAP, 1);
			st.takeItems(KURUKA_RATMAN_TOOTH, -1);
			st.giveItems(BETRAYER_REPORT_1, 1);
			st.giveItems(BETRAYER_REPORT_2, 1);
		}
		else if (event.equalsIgnoreCase("30570-07b.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(GOBLIN_DWELLING_MAP, 1);
			st.takeItems(KURUKA_RATMAN_TOOTH, -1);
		}
		// TAZEER
		else if (event.equalsIgnoreCase("31978-03.htm"))
		{
			st.set("cond", "6");
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
		
		switch (st.getState())
		{
			case STATE_CREATED:
				htmltext = "30570-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case KARUKIA:
						if (cond == 1)
							htmltext = "30570-06.htm";
						else if (cond == 2)
							htmltext = "30570-07.htm";
						else if (cond == 3 || cond == 4)
							htmltext = "30570-08.htm";
						else if (cond == 5)
							htmltext = "30570-07b.htm";
						break;
					
					case KASMAN:
						if (cond == 3)
							htmltext = "30501-01.htm";
						else if (cond == 4)
						{
							if (st.getQuestItemsCount(HEAD_OF_BETRAYER) == 1)
								htmltext = "30501-02.htm";
							else
							{
								htmltext = "30501-03.htm";
								st.takeItems(BETRAYER_REPORT_1, 1);
								st.takeItems(BETRAYER_REPORT_2, 1);
								st.takeItems(HEAD_OF_BETRAYER, -1);
								st.giveItems(MARK_OF_RAIDER, 1);
								st.rewardExpAndSp(3200, 2360);
								player.broadcastPacket(new SocialAction(player, 3));
								st.playSound(QuestState.SOUND_FINISH);
								st.exitQuest(true);
							}
						}
						break;
					
					case TAZEER:
						if (cond == 5)
							htmltext = "31978-01.htm";
						else if (cond == 6)
							htmltext = "31978-04.htm";
						else if (cond == 7)
						{
							htmltext = "31978-05.htm";
							st.takeItems(TIMORA_ORC_HEAD, 1);
							st.giveItems(MARK_OF_RAIDER, 1);
							st.rewardExpAndSp(3200, 2360);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
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
		
		final int cond = st.getInt("cond");
		
		switch (npc.getNpcId())
		{
			case GOBLIN_TOMB_RAIDER_LEADER:
				if (cond == 1)
				{
					if (st.getQuestItemsCount(GREEN_BLOOD) <= Rnd.get(20))
					{
						st.playSound(QuestState.SOUND_ITEMGET);
						st.giveItems(GREEN_BLOOD, 1);
					}
					else
					{
						st.takeItems(GREEN_BLOOD, -1);
						addSpawn(KURUKA_RATMAN_LEADER, npc, false, 300000, true);
					}
				}
				break;
			
			case KURUKA_RATMAN_LEADER:
				if (cond == 1 && st.dropItemsAlways(KURUKA_RATMAN_TOOTH, 1, 10))
					st.set("cond", "2");
				break;
			
			case UMBAR_ORC:
				if ((cond == 3 || cond == 4) && st.getQuestItemsCount(HEAD_OF_BETRAYER) < 2 && Rnd.get(10) < 2)
				{
					if (cond == 3)
						st.set("cond", "4");
					
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(HEAD_OF_BETRAYER, 1);
				}
				break;
			
			case TIMORA_ORC:
				if (cond == 6 && st.dropItems(TIMORA_ORC_HEAD, 1, 1, 600000))
					st.set("cond", "7");
				break;
		}
		
		return null;
	}
}
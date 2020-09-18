package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q118_ToLeadAndBeLed extends Quest
{
	private static final String qn = "Q118_ToLeadAndBeLed";
	private static final String qn2 = "Q123_TheLeaderAndTheFollower";
	
	// Npc
	private static final int PINTER = 30298;
	
	// Mobs
	private static final int MAILLE_LIZARDMAN = 20919;
	private static final int MAILLE_LIZARDMAN_SCOUT = 20920;
	private static final int MAILLE_LIZARDMAN_GUARD = 20921;
	private static final int KING_OF_THE_ARANEID = 20927;
	
	// Items
	private static final int BLOOD_OF_MAILLE_LIZARDMAN = 8062;
	private static final int LEG_OF_KING_ARANEID = 8063;
	private static final int CRYSTAL_D = 1458;
	
	// Rewards
	private static final int CLAN_OATH_HELM = 7850;
	private static final int CLAN_OATH_ARMOR = 7851;
	private static final int CLAN_OATH_GAUNTLETS = 7852;
	private static final int CLAN_OATH_SABATON = 7853;
	private static final int CLAN_OATH_BRIGANDINE = 7854;
	private static final int CLAN_OATH_LEATHER_GLOVES = 7855;
	private static final int CLAN_OATH_BOOTS = 7856;
	private static final int CLAN_OATH_AKETON = 7857;
	private static final int CLAN_OATH_PADDED_GLOVES = 7858;
	private static final int CLAN_OATH_SANDALS = 7859;
	
	public Q118_ToLeadAndBeLed()
	{
		super(118, "To Lead and Be Led");
		
		setItemsIds(BLOOD_OF_MAILLE_LIZARDMAN, LEG_OF_KING_ARANEID);
		
		addStartNpc(PINTER);
		addTalkId(PINTER);
		
		addKillId(MAILLE_LIZARDMAN, MAILLE_LIZARDMAN_SCOUT, MAILLE_LIZARDMAN_GUARD, KING_OF_THE_ARANEID);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30298-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.set("state", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30298-05d.htm"))
		{
			if (st.getQuestItemsCount(BLOOD_OF_MAILLE_LIZARDMAN) > 9)
			{
				st.set("cond", "3");
				st.set("state", "2");
				st.set("stateEx", "1");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BLOOD_OF_MAILLE_LIZARDMAN, -1);
			}
		}
		else if (event.equalsIgnoreCase("30298-05e.htm"))
		{
			if (st.getQuestItemsCount(BLOOD_OF_MAILLE_LIZARDMAN) > 9)
			{
				st.set("cond", "4");
				st.set("state", "2");
				st.set("stateEx", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BLOOD_OF_MAILLE_LIZARDMAN, -1);
			}
		}
		else if (event.equalsIgnoreCase("30298-05f.htm"))
		{
			if (st.getQuestItemsCount(BLOOD_OF_MAILLE_LIZARDMAN) > 9)
			{
				st.set("cond", "5");
				st.set("state", "2");
				st.set("stateEx", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(BLOOD_OF_MAILLE_LIZARDMAN, -1);
			}
		}
		else if (event.equalsIgnoreCase("30298-10.htm"))
		{
			final Player academic = getApprentice(player);
			if (academic != null)
			{
				final QuestState st2 = academic.getQuestState(qn);
				if (st2 != null && st2.getInt("state") == 2)
				{
					final int stateEx = st2.getInt("stateEx");
					if (stateEx == 1)
					{
						if (st.getQuestItemsCount(CRYSTAL_D) > 921)
						{
							st.takeItems(CRYSTAL_D, 922);
							st2.set("cond", "6");
							st2.set("state", "3");
							st2.playSound(QuestState.SOUND_MIDDLE);
						}
						else
							htmltext = "30298-11.htm";
					}
					else
					{
						if (st.getQuestItemsCount(CRYSTAL_D) > 770)
						{
							st.takeItems(CRYSTAL_D, 771);
							st2.set("cond", "6");
							st2.set("state", "3");
							st2.playSound(QuestState.SOUND_MIDDLE);
						}
						else
							htmltext = "30298-11a.htm";
					}
				}
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
				if (player.getSponsor() > 0)
				{
					QuestState st2 = player.getQuestState(qn2);
					if (st2 != null)
						htmltext = (st2.isCompleted()) ? "30298-02a.htm" : "30298-02b.htm";
					else
						htmltext = (player.getLevel() > 18) ? "30298-01.htm" : "30298-02.htm";
				}
				else if (player.getApprentice() > 0)
				{
					final Player academic = getApprentice(player);
					if (academic != null)
					{
						final QuestState st3 = academic.getQuestState(qn);
						if (st3 != null)
						{
							final int state = st3.getInt("state");
							if (state == 2)
								htmltext = "30298-08.htm";
							else if (state == 3)
								htmltext = "30298-12.htm";
							else
								htmltext = "30298-14.htm";
						}
					}
					else
						htmltext = "30298-09.htm";
				}
				break;
			
			case STATE_STARTED:
				final int state = st.getInt("state");
				if (state == 1)
					htmltext = (st.getQuestItemsCount(BLOOD_OF_MAILLE_LIZARDMAN) < 10) ? "30298-04.htm" : "30298-05.htm";
				else if (state == 2)
				{
					final int stateEx = st.getInt("stateEx");
					if (player.getSponsor() == 0)
					{
						if (stateEx == 1)
							htmltext = "30298-06a.htm";
						else if (stateEx == 2)
							htmltext = "30298-06b.htm";
						else if (stateEx == 3)
							htmltext = "30298-06c.htm";
					}
					else
					{
						if (getSponsor(player))
						{
							if (stateEx == 1)
								htmltext = "30298-06.htm";
							else if (stateEx == 2)
								htmltext = "30298-06d.htm";
							else if (stateEx == 3)
								htmltext = "30298-06e.htm";
						}
						else
							htmltext = "30298-07.htm";
					}
				}
				else if (state == 3)
				{
					st.set("cond", "7");
					st.set("state", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
					htmltext = "30298-15.htm";
				}
				else if (state == 4)
				{
					if (st.getQuestItemsCount(LEG_OF_KING_ARANEID) > 7)
					{
						htmltext = "30298-17.htm";
						
						st.takeItems(LEG_OF_KING_ARANEID, -1);
						st.giveItems(CLAN_OATH_HELM, 1);
						
						switch (st.getInt("stateEx"))
						{
							case 1:
								st.giveItems(CLAN_OATH_ARMOR, 1);
								st.giveItems(CLAN_OATH_GAUNTLETS, 1);
								st.giveItems(CLAN_OATH_SABATON, 1);
								break;
							
							case 2:
								st.giveItems(CLAN_OATH_BRIGANDINE, 1);
								st.giveItems(CLAN_OATH_LEATHER_GLOVES, 1);
								st.giveItems(CLAN_OATH_BOOTS, 1);
								break;
							
							case 3:
								st.giveItems(CLAN_OATH_AKETON, 1);
								st.giveItems(CLAN_OATH_PADDED_GLOVES, 1);
								st.giveItems(CLAN_OATH_SANDALS, 1);
								break;
						}
						
						st.playSound(QuestState.SOUND_FINISH);
						st.exitQuest(false);
					}
					else
						htmltext = "30298-16.htm";
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
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		if (player.getSponsor() == 0)
		{
			st.exitQuest(true);
			return null;
		}
		
		final int cond = st.getInt("cond");
		switch (npc.getNpcId())
		{
			case MAILLE_LIZARDMAN:
			case MAILLE_LIZARDMAN_SCOUT:
			case MAILLE_LIZARDMAN_GUARD:
				if (cond == 1 && st.dropItems(BLOOD_OF_MAILLE_LIZARDMAN, 1, 10, 700000))
					st.set("cond", "2");
				break;
			
			case KING_OF_THE_ARANEID:
				if (cond == 7 && getSponsor(player) && st.dropItems(LEG_OF_KING_ARANEID, 1, 8, 700000))
					st.set("cond", "8");
				break;
		}
		
		return null;
	}
}
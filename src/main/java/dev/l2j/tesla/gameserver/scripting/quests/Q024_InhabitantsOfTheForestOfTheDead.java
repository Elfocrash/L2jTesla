package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q024_InhabitantsOfTheForestOfTheDead extends Quest
{
	private static final String qn = "Q024_InhabitantsOfTheForestOfTheDead";
	
	// NPCs
	private static final int DORIAN = 31389;
	private static final int MYSTERIOUS_WIZARD = 31522;
	private static final int TOMBSTONE = 31531;
	private static final int LIDIA_MAID = 31532;
	
	// MOBs
	private static final int BONE_SNATCHER = 21557;
	private static final int BONE_SNATCHER_A = 21558;
	private static final int BONE_SHAPER = 21560;
	private static final int BONE_COLLECTOR = 21563;
	private static final int SKULL_COLLECTOR = 21564;
	private static final int BONE_ANIMATOR = 21565;
	private static final int SKULL_ANIMATOR = 21566;
	private static final int BONE_SLAYER = 21567;
	
	// Items
	private static final int LIDIAS_LETTER = 7065;
	private static final int LIDIAS_HAIRPIN = 7148;
	private static final int SUSPICIOUS_TOTEM_DOLL = 7151;
	private static final int FLOWER_BOUQUET = 7152;
	private static final int SILVER_CROSS_OF_EINHASAD = 7153;
	private static final int BROKEN_SILVER_CROSS_OF_EINHASAD = 7154;
	private static final int SUSPICIOUS_TOTEM_DOLL_2 = 7156;
	
	public Q024_InhabitantsOfTheForestOfTheDead()
	{
		super(24, "Inhabitants of the Forest of the Dead");
		
		setItemsIds(LIDIAS_LETTER, LIDIAS_HAIRPIN, SUSPICIOUS_TOTEM_DOLL, FLOWER_BOUQUET, SILVER_CROSS_OF_EINHASAD, BROKEN_SILVER_CROSS_OF_EINHASAD);
		
		addStartNpc(DORIAN);
		addTalkId(DORIAN, MYSTERIOUS_WIZARD, LIDIA_MAID, TOMBSTONE);
		
		addKillId(BONE_SNATCHER, BONE_SNATCHER_A, BONE_SHAPER, BONE_COLLECTOR, SKULL_COLLECTOR, BONE_ANIMATOR, SKULL_ANIMATOR, BONE_SLAYER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31389-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.set("state", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(FLOWER_BOUQUET, 1);
		}
		else if (event.equalsIgnoreCase("31389-08.htm"))
			st.set("state", "3");
		else if (event.equalsIgnoreCase("31389-13.htm"))
		{
			st.set("cond", "3");
			st.set("state", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(SILVER_CROSS_OF_EINHASAD, 1);
		}
		else if (event.equalsIgnoreCase("31389-18.htm"))
			st.playSound("InterfaceSound.charstat_open_01");
		else if (event.equalsIgnoreCase("31389-19.htm"))
		{
			st.set("cond", "5");
			st.set("state", "5");
			st.takeItems(BROKEN_SILVER_CROSS_OF_EINHASAD, -1);
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31522-03.htm"))
		{
			st.set("state", "12");
			st.takeItems(SUSPICIOUS_TOTEM_DOLL, -1);
		}
		else if (event.equalsIgnoreCase("31522-08.htm"))
		{
			st.set("cond", "11");
			st.set("state", "13");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31522-17.htm"))
			st.set("state", "14");
		else if (event.equalsIgnoreCase("31522-21.htm"))
		{
			st.giveItems(SUSPICIOUS_TOTEM_DOLL_2, 1);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		else if (event.equalsIgnoreCase("31532-04.htm"))
		{
			st.set("cond", "6");
			st.set("state", "6");
			st.giveItems(LIDIAS_LETTER, 1);
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31532-06.htm"))
		{
			if (st.hasQuestItems(LIDIAS_HAIRPIN))
			{
				st.set("state", "8");
				st.takeItems(LIDIAS_LETTER, -1);
				st.takeItems(LIDIAS_HAIRPIN, -1);
			}
			else
			{
				st.set("cond", "7");
				st.set("state", "7");
				htmltext = "31532-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("31532-10.htm"))
			st.set("state", "9");
		else if (event.equalsIgnoreCase("31532-14.htm"))
			st.set("state", "10");
		else if (event.equalsIgnoreCase("31532-19.htm"))
		{
			st.set("cond", "9");
			st.set("state", "11");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31531-02.htm"))
		{
			st.set("cond", "2");
			st.set("state", "2");
			st.takeItems(FLOWER_BOUQUET, -1);
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
				QuestState st2 = player.getQuestState("Q023_LidiasHeart");
				if (st2 == null || !st2.isCompleted() || player.getLevel() < 65)
					htmltext = "31389-02.htm";
				else
					htmltext = "31389-01.htm";
				break;
			
			case STATE_STARTED:
				int state = st.getInt("state");
				switch (npc.getNpcId())
				{
					case DORIAN:
						if (state == 1)
							htmltext = "31389-04.htm";
						else if (state == 2)
							htmltext = "31389-05.htm";
						else if (state == 3)
							htmltext = "31389-09.htm";
						else if (state == 4)
						{
							if (st.hasQuestItems(SILVER_CROSS_OF_EINHASAD))
								htmltext = "31389-14.htm";
							else if (st.hasQuestItems(BROKEN_SILVER_CROSS_OF_EINHASAD))
								htmltext = "31389-15.htm";
						}
						else if (state == 5)
							htmltext = "31389-20.htm";
						else if (state == 7 && !st.hasQuestItems(LIDIAS_HAIRPIN))
						{
							htmltext = "31389-21.htm";
							st.set("cond", "8");
							st.giveItems(LIDIAS_HAIRPIN, 1);
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if ((state == 7 && st.hasQuestItems(LIDIAS_HAIRPIN)) || state == 6)
							htmltext = "31389-22.htm";
						break;
					
					case MYSTERIOUS_WIZARD:
						if (state == 11 && st.hasQuestItems(SUSPICIOUS_TOTEM_DOLL))
							htmltext = "31522-01.htm";
						else if (state == 12)
							htmltext = "31522-04.htm";
						else if (state == 13)
							htmltext = "31522-09.htm";
						else if (state == 14)
							htmltext = "31522-18.htm";
						break;
					
					case LIDIA_MAID:
						if (state == 5)
							htmltext = "31532-01.htm";
						else if (state == 6 && st.hasQuestItems(LIDIAS_LETTER))
							htmltext = "31532-05.htm";
						else if (state == 7)
							htmltext = "31532-07a.htm";
						else if (state == 8)
							htmltext = "31532-08.htm";
						else if (state == 9)
							htmltext = "31532-11.htm";
						else if (state == 10)
							htmltext = "31532-15.htm";
						else if (state == 11)
							htmltext = "31532-20.htm";
						break;
					
					case TOMBSTONE:
						if (state == 1 && st.hasQuestItems(FLOWER_BOUQUET))
						{
							htmltext = "31531-01.htm";
							st.playSound("AmdSound.d_wind_loot_02");
						}
						else if (state == 2)
							htmltext = "31531-03.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				if (npc.getNpcId() == MYSTERIOUS_WIZARD)
					htmltext = "31522-22.htm";
				else
					htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "9");
		if (st == null)
			return null;
		
		if (st.dropItems(SUSPICIOUS_TOTEM_DOLL, 1, 1, 100000))
			st.set("cond", "10");
		
		return null;
	}
}
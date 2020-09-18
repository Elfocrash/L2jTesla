package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q402_PathToAHumanKnight extends Quest
{
	private static final String qn = "Q402_PathToAHumanKnight";
	
	// Items
	private static final int SWORD_OF_RITUAL = 1161;
	private static final int COIN_OF_LORDS_1 = 1162;
	private static final int COIN_OF_LORDS_2 = 1163;
	private static final int COIN_OF_LORDS_3 = 1164;
	private static final int COIN_OF_LORDS_4 = 1165;
	private static final int COIN_OF_LORDS_5 = 1166;
	private static final int COIN_OF_LORDS_6 = 1167;
	private static final int GLUDIO_GUARD_MARK_1 = 1168;
	private static final int BUGBEAR_NECKLACE = 1169;
	private static final int EINHASAD_CHURCH_MARK_1 = 1170;
	private static final int EINHASAD_CRUCIFIX = 1171;
	private static final int GLUDIO_GUARD_MARK_2 = 1172;
	private static final int SPIDER_LEG = 1173;
	private static final int EINHASAD_CHURCH_MARK_2 = 1174;
	private static final int LIZARDMAN_TOTEM = 1175;
	private static final int GLUDIO_GUARD_MARK_3 = 1176;
	private static final int GIANT_SPIDER_HUSK = 1177;
	private static final int EINHASAD_CHURCH_MARK_3 = 1178;
	private static final int HORRIBLE_SKULL = 1179;
	private static final int MARK_OF_ESQUIRE = 1271;
	
	// NPCs
	private static final int SIR_KLAUS_VASPER = 30417;
	private static final int BATHIS = 30332;
	private static final int RAYMOND = 30289;
	private static final int BEZIQUE = 30379;
	private static final int LEVIAN = 30037;
	private static final int GILBERT = 30039;
	private static final int BIOTIN = 30031;
	private static final int SIR_AARON_TANFORD = 30653;
	private static final int SIR_COLLIN_WINDAWOOD = 30311;
	
	public Q402_PathToAHumanKnight()
	{
		super(402, "Path to a Human Knight");
		
		setItemsIds(MARK_OF_ESQUIRE, COIN_OF_LORDS_1, COIN_OF_LORDS_2, COIN_OF_LORDS_3, COIN_OF_LORDS_4, COIN_OF_LORDS_5, COIN_OF_LORDS_6, GLUDIO_GUARD_MARK_1, BUGBEAR_NECKLACE, EINHASAD_CHURCH_MARK_1, EINHASAD_CRUCIFIX, GLUDIO_GUARD_MARK_2, SPIDER_LEG, EINHASAD_CHURCH_MARK_2, LIZARDMAN_TOTEM, GLUDIO_GUARD_MARK_3, GIANT_SPIDER_HUSK, EINHASAD_CHURCH_MARK_3, LIZARDMAN_TOTEM, GLUDIO_GUARD_MARK_3, GIANT_SPIDER_HUSK, EINHASAD_CHURCH_MARK_3, HORRIBLE_SKULL);
		
		addStartNpc(SIR_KLAUS_VASPER);
		addTalkId(SIR_KLAUS_VASPER, BATHIS, RAYMOND, BEZIQUE, LEVIAN, GILBERT, BIOTIN, SIR_AARON_TANFORD, SIR_COLLIN_WINDAWOOD);
		
		addKillId(20775, 27024, 20038, 20043, 20050, 20030, 20027, 20024, 20103, 20106, 20108, 20404);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30417-05.htm"))
		{
			if (player.getClassId() != ClassId.HUMAN_FIGHTER)
				htmltext = (player.getClassId() == ClassId.KNIGHT) ? "30417-02a.htm" : "30417-03.htm";
			else if (player.getLevel() < 19)
				htmltext = "30417-02.htm";
			else if (st.hasQuestItems(SWORD_OF_RITUAL))
				htmltext = "30417-04.htm";
		}
		else if (event.equalsIgnoreCase("30417-08.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(MARK_OF_ESQUIRE, 1);
		}
		else if (event.equalsIgnoreCase("30332-02.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(GLUDIO_GUARD_MARK_1, 1);
		}
		else if (event.equalsIgnoreCase("30289-03.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(EINHASAD_CHURCH_MARK_1, 1);
		}
		else if (event.equalsIgnoreCase("30379-02.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(GLUDIO_GUARD_MARK_2, 1);
		}
		else if (event.equalsIgnoreCase("30037-02.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(EINHASAD_CHURCH_MARK_2, 1);
		}
		else if (event.equalsIgnoreCase("30039-02.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(GLUDIO_GUARD_MARK_3, 1);
		}
		else if (event.equalsIgnoreCase("30031-02.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(EINHASAD_CHURCH_MARK_3, 1);
		}
		else if (event.equalsIgnoreCase("30417-13.htm") || event.equalsIgnoreCase("30417-14.htm"))
		{
			final int coinCount = st.getQuestItemsCount(COIN_OF_LORDS_1) + st.getQuestItemsCount(COIN_OF_LORDS_2) + st.getQuestItemsCount(COIN_OF_LORDS_3) + st.getQuestItemsCount(COIN_OF_LORDS_4) + st.getQuestItemsCount(COIN_OF_LORDS_5) + st.getQuestItemsCount(COIN_OF_LORDS_6);
			
			st.takeItems(COIN_OF_LORDS_1, -1);
			st.takeItems(COIN_OF_LORDS_2, -1);
			st.takeItems(COIN_OF_LORDS_3, -1);
			st.takeItems(COIN_OF_LORDS_4, -1);
			st.takeItems(COIN_OF_LORDS_5, -1);
			st.takeItems(COIN_OF_LORDS_6, -1);
			st.takeItems(MARK_OF_ESQUIRE, 1);
			st.giveItems(SWORD_OF_RITUAL, 1);
			st.rewardExpAndSp(3200, 1500 + (1920 * (coinCount - 3)));
			player.broadcastPacket(new SocialAction(player, 3));
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				htmltext = "30417-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case SIR_KLAUS_VASPER:
						final int coins = st.getQuestItemsCount(COIN_OF_LORDS_1) + st.getQuestItemsCount(COIN_OF_LORDS_2) + st.getQuestItemsCount(COIN_OF_LORDS_3) + st.getQuestItemsCount(COIN_OF_LORDS_4) + st.getQuestItemsCount(COIN_OF_LORDS_5) + st.getQuestItemsCount(COIN_OF_LORDS_6);
						if (coins < 3)
							htmltext = "30417-09.htm";
						else if (coins == 3)
							htmltext = "30417-10.htm";
						else if (coins > 3 && coins < 6)
							htmltext = "30417-11.htm";
						else if (coins == 6)
						{
							htmltext = "30417-12.htm";
							st.takeItems(COIN_OF_LORDS_1, -1);
							st.takeItems(COIN_OF_LORDS_2, -1);
							st.takeItems(COIN_OF_LORDS_3, -1);
							st.takeItems(COIN_OF_LORDS_4, -1);
							st.takeItems(COIN_OF_LORDS_5, -1);
							st.takeItems(COIN_OF_LORDS_6, -1);
							st.takeItems(MARK_OF_ESQUIRE, 1);
							st.giveItems(SWORD_OF_RITUAL, 1);
							st.rewardExpAndSp(3200, 7260);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case BATHIS:
						if (st.hasQuestItems(COIN_OF_LORDS_1))
							htmltext = "30332-05.htm";
						else if (st.hasQuestItems(GLUDIO_GUARD_MARK_1))
						{
							if (st.getQuestItemsCount(BUGBEAR_NECKLACE) < 10)
								htmltext = "30332-03.htm";
							else
							{
								htmltext = "30332-04.htm";
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(BUGBEAR_NECKLACE, -1);
								st.takeItems(GLUDIO_GUARD_MARK_1, 1);
								st.giveItems(COIN_OF_LORDS_1, 1);
							}
						}
						else
							htmltext = "30332-01.htm";
						break;
					
					case RAYMOND:
						if (st.hasQuestItems(COIN_OF_LORDS_2))
							htmltext = "30289-06.htm";
						else if (st.hasQuestItems(EINHASAD_CHURCH_MARK_1))
						{
							if (st.getQuestItemsCount(EINHASAD_CRUCIFIX) < 12)
								htmltext = "30289-04.htm";
							else
							{
								htmltext = "30289-05.htm";
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(EINHASAD_CRUCIFIX, -1);
								st.takeItems(EINHASAD_CHURCH_MARK_1, 1);
								st.giveItems(COIN_OF_LORDS_2, 1);
							}
						}
						else
							htmltext = "30289-01.htm";
						break;
					
					case BEZIQUE:
						if (st.hasQuestItems(COIN_OF_LORDS_3))
							htmltext = "30379-05.htm";
						else if (st.hasQuestItems(GLUDIO_GUARD_MARK_2))
						{
							if (st.getQuestItemsCount(SPIDER_LEG) < 20)
								htmltext = "30379-03.htm";
							else
							{
								htmltext = "30379-04.htm";
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(SPIDER_LEG, -1);
								st.takeItems(GLUDIO_GUARD_MARK_2, 1);
								st.giveItems(COIN_OF_LORDS_3, 1);
							}
						}
						else
							htmltext = "30379-01.htm";
						break;
					
					case LEVIAN:
						if (st.hasQuestItems(COIN_OF_LORDS_4))
							htmltext = "30037-05.htm";
						else if (st.hasQuestItems(EINHASAD_CHURCH_MARK_2))
						{
							if (st.getQuestItemsCount(LIZARDMAN_TOTEM) < 20)
								htmltext = "30037-03.htm";
							else
							{
								htmltext = "30037-04.htm";
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(LIZARDMAN_TOTEM, -1);
								st.takeItems(EINHASAD_CHURCH_MARK_2, 1);
								st.giveItems(COIN_OF_LORDS_4, 1);
							}
						}
						else
							htmltext = "30037-01.htm";
						break;
					
					case GILBERT:
						if (st.hasQuestItems(COIN_OF_LORDS_5))
							htmltext = "30039-05.htm";
						else if (st.hasQuestItems(GLUDIO_GUARD_MARK_3))
						{
							if (st.getQuestItemsCount(GIANT_SPIDER_HUSK) < 20)
								htmltext = "30039-03.htm";
							else
							{
								htmltext = "30039-04.htm";
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(GIANT_SPIDER_HUSK, -1);
								st.takeItems(GLUDIO_GUARD_MARK_3, 1);
								st.giveItems(COIN_OF_LORDS_5, 1);
							}
						}
						else
							htmltext = "30039-01.htm";
						break;
					
					case BIOTIN:
						if (st.hasQuestItems(COIN_OF_LORDS_6))
							htmltext = "30031-05.htm";
						else if (st.hasQuestItems(EINHASAD_CHURCH_MARK_3))
						{
							if (st.getQuestItemsCount(HORRIBLE_SKULL) < 10)
								htmltext = "30031-03.htm";
							else
							{
								htmltext = "30031-04.htm";
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(HORRIBLE_SKULL, -1);
								st.takeItems(EINHASAD_CHURCH_MARK_3, 1);
								st.giveItems(COIN_OF_LORDS_6, 1);
							}
						}
						else
							htmltext = "30031-01.htm";
						break;
					
					case SIR_AARON_TANFORD:
						htmltext = "30653-01.htm";
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
		
		switch (npc.getNpcId())
		{
			case 20775: // Bugbear Raider
				if (st.hasQuestItems(GLUDIO_GUARD_MARK_1))
					st.dropItemsAlways(BUGBEAR_NECKLACE, 1, 10);
				break;
			
			case 27024: // Undead Priest
				if (st.hasQuestItems(EINHASAD_CHURCH_MARK_1))
					st.dropItems(EINHASAD_CRUCIFIX, 1, 12, 500000);
				break;
			
			case 20038: // Poison Spider
			case 20043: // Arachnid Tracker
			case 20050: // Arachnid Predator
				if (st.hasQuestItems(GLUDIO_GUARD_MARK_2))
					st.dropItemsAlways(SPIDER_LEG, 1, 20);
				break;
			
			case 20030: // Langk Lizardman
			case 20027: // Langk Lizardman Scout
			case 20024: // Langk Lizardman Warrior
				if (st.hasQuestItems(EINHASAD_CHURCH_MARK_2))
					st.dropItems(LIZARDMAN_TOTEM, 1, 20, 500000);
				break;
			
			case 20103: // Giant Spider
			case 20106: // Talon Spider
			case 20108: // Blade Spider
				if (st.hasQuestItems(GLUDIO_GUARD_MARK_3))
					st.dropItems(GIANT_SPIDER_HUSK, 1, 20, 400000);
				break;
			
			case 20404: // Silent Horror
				if (st.hasQuestItems(EINHASAD_CHURCH_MARK_3))
					st.dropItems(HORRIBLE_SKULL, 1, 10, 400000);
				break;
		}
		
		return null;
	}
}
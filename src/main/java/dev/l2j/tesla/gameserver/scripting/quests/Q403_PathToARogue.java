package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.itemcontainer.Inventory;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q403_PathToARogue extends Quest
{
	private static final String qn = "Q403_PathToARogue";
	
	// Items
	private static final int BEZIQUE_LETTER = 1180;
	private static final int NETI_BOW = 1181;
	private static final int NETI_DAGGER = 1182;
	private static final int SPARTOI_BONES = 1183;
	private static final int HORSESHOE_OF_LIGHT = 1184;
	private static final int MOST_WANTED_LIST = 1185;
	private static final int STOLEN_JEWELRY = 1186;
	private static final int STOLEN_TOMES = 1187;
	private static final int STOLEN_RING = 1188;
	private static final int STOLEN_NECKLACE = 1189;
	private static final int BEZIQUE_RECOMMENDATION = 1190;
	
	// NPCs
	private static final int BEZIQUE = 30379;
	private static final int NETI = 30425;
	
	public Q403_PathToARogue()
	{
		super(403, "Path to a Rogue");
		
		setItemsIds(BEZIQUE_LETTER, NETI_BOW, NETI_DAGGER, SPARTOI_BONES, HORSESHOE_OF_LIGHT, MOST_WANTED_LIST, STOLEN_JEWELRY, STOLEN_TOMES, STOLEN_RING, STOLEN_NECKLACE);
		
		addStartNpc(BEZIQUE);
		addTalkId(BEZIQUE, NETI);
		
		addKillId(20035, 20042, 20045, 20051, 20054, 20060, 27038);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30379-05.htm"))
		{
			if (player.getClassId() != ClassId.HUMAN_FIGHTER)
				htmltext = (player.getClassId() == ClassId.ROGUE) ? "30379-02a.htm" : "30379-02.htm";
			else if (player.getLevel() < 19)
				htmltext = "30379-02.htm";
			else if (st.hasQuestItems(BEZIQUE_RECOMMENDATION))
				htmltext = "30379-04.htm";
		}
		else if (event.equalsIgnoreCase("30379-06.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(BEZIQUE_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("30425-05.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(NETI_BOW, 1);
			st.giveItems(NETI_DAGGER, 1);
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
				htmltext = "30379-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case BEZIQUE:
						if (cond == 1)
							htmltext = "30379-07.htm";
						else if (cond == 2 || cond == 3)
							htmltext = "30379-10.htm";
						else if (cond == 4)
						{
							htmltext = "30379-08.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(HORSESHOE_OF_LIGHT, 1);
							st.giveItems(MOST_WANTED_LIST, 1);
						}
						else if (cond == 5)
							htmltext = "30379-11.htm";
						else if (cond == 6)
						{
							htmltext = "30379-09.htm";
							st.takeItems(NETI_BOW, 1);
							st.takeItems(NETI_DAGGER, 1);
							st.takeItems(STOLEN_JEWELRY, 1);
							st.takeItems(STOLEN_NECKLACE, 1);
							st.takeItems(STOLEN_RING, 1);
							st.takeItems(STOLEN_TOMES, 1);
							st.giveItems(BEZIQUE_RECOMMENDATION, 1);
							st.rewardExpAndSp(3200, 1500);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case NETI:
						if (cond == 1)
							htmltext = "30425-01.htm";
						else if (cond == 2)
							htmltext = "30425-06.htm";
						else if (cond == 3)
						{
							htmltext = "30425-07.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SPARTOI_BONES, 10);
							st.giveItems(HORSESHOE_OF_LIGHT, 1);
						}
						else if (cond > 3)
							htmltext = "30425-08.htm";
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
		
		final int equippedItemId = st.getItemEquipped(Inventory.PAPERDOLL_RHAND);
		if (equippedItemId != NETI_BOW && equippedItemId != NETI_DAGGER)
			return null;
		
		switch (npc.getNpcId())
		{
			case 20035:
			case 20045:
			case 20051:
				if (st.getInt("cond") == 2 && st.dropItems(SPARTOI_BONES, 1, 10, 200000))
					st.set("cond", "3");
				break;
			
			case 20042:
				if (st.getInt("cond") == 2 && st.dropItems(SPARTOI_BONES, 1, 10, 300000))
					st.set("cond", "3");
				break;
			
			case 20054:
			case 20060:
				if (st.getInt("cond") == 2 && st.dropItems(SPARTOI_BONES, 1, 10, 800000))
					st.set("cond", "3");
				break;
			
			case 27038:
				if (st.getInt("cond") == 5)
				{
					final int randomItem = Rnd.get(STOLEN_JEWELRY, STOLEN_NECKLACE);
					
					if (!st.hasQuestItems(randomItem))
					{
						st.giveItems(randomItem, 1);
						
						if (st.hasQuestItems(STOLEN_JEWELRY, STOLEN_TOMES, STOLEN_RING, STOLEN_NECKLACE))
						{
							st.set("cond", "6");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else
							st.playSound(QuestState.SOUND_ITEMGET);
					}
				}
				break;
		}
		
		return null;
	}
}
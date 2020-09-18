package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.itemcontainer.Inventory;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q401_PathToAWarrior extends Quest
{
	private static final String qn = "Q401_PathToAWarrior";
	
	// Items
	private static final int AURON_LETTER = 1138;
	private static final int WARRIOR_GUILD_MARK = 1139;
	private static final int RUSTED_BRONZE_SWORD_1 = 1140;
	private static final int RUSTED_BRONZE_SWORD_2 = 1141;
	private static final int RUSTED_BRONZE_SWORD_3 = 1142;
	private static final int SIMPLON_LETTER = 1143;
	private static final int POISON_SPIDER_LEG = 1144;
	private static final int MEDALLION_OF_WARRIOR = 1145;
	
	// NPCs
	private static final int AURON = 30010;
	private static final int SIMPLON = 30253;
	
	public Q401_PathToAWarrior()
	{
		super(401, "Path to a Warrior");
		
		setItemsIds(AURON_LETTER, WARRIOR_GUILD_MARK, RUSTED_BRONZE_SWORD_1, RUSTED_BRONZE_SWORD_2, RUSTED_BRONZE_SWORD_3, SIMPLON_LETTER, POISON_SPIDER_LEG);
		
		addStartNpc(AURON);
		addTalkId(AURON, SIMPLON);
		
		addKillId(20035, 20038, 20042, 20043);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30010-05.htm"))
		{
			if (player.getClassId() != ClassId.HUMAN_FIGHTER)
				htmltext = (player.getClassId() == ClassId.WARRIOR) ? "30010-03.htm" : "30010-02b.htm";
			else if (player.getLevel() < 19)
				htmltext = "30010-02.htm";
			else if (st.hasQuestItems(MEDALLION_OF_WARRIOR))
				htmltext = "30010-04.htm";
		}
		else if (event.equalsIgnoreCase("30010-06.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(AURON_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("30253-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(AURON_LETTER, 1);
			st.giveItems(WARRIOR_GUILD_MARK, 1);
		}
		else if (event.equalsIgnoreCase("30010-11.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(RUSTED_BRONZE_SWORD_2, 1);
			st.takeItems(SIMPLON_LETTER, 1);
			st.giveItems(RUSTED_BRONZE_SWORD_3, 1);
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
				htmltext = "30010-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case AURON:
						if (cond == 1)
							htmltext = "30010-07.htm";
						else if (cond == 2 || cond == 3)
							htmltext = "30010-08.htm";
						else if (cond == 4)
							htmltext = "30010-09.htm";
						else if (cond == 5)
							htmltext = "30010-12.htm";
						else if (cond == 6)
						{
							htmltext = "30010-13.htm";
							st.takeItems(POISON_SPIDER_LEG, -1);
							st.takeItems(RUSTED_BRONZE_SWORD_3, 1);
							st.giveItems(MEDALLION_OF_WARRIOR, 1);
							st.rewardExpAndSp(3200, 1500);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case SIMPLON:
						if (cond == 1)
							htmltext = "30253-01.htm";
						else if (cond == 2)
						{
							if (!st.hasQuestItems(RUSTED_BRONZE_SWORD_1))
								htmltext = "30253-03.htm";
							else if (st.getQuestItemsCount(RUSTED_BRONZE_SWORD_1) <= 9)
								htmltext = "30253-03b.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30253-04.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(RUSTED_BRONZE_SWORD_1, 10);
							st.takeItems(WARRIOR_GUILD_MARK, 1);
							st.giveItems(RUSTED_BRONZE_SWORD_2, 1);
							st.giveItems(SIMPLON_LETTER, 1);
						}
						else if (cond == 4)
							htmltext = "30253-05.htm";
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
			case 20035:
			case 20042:
				if (st.getInt("cond") == 2 && st.dropItems(RUSTED_BRONZE_SWORD_1, 1, 10, 400000))
					st.set("cond", "3");
				break;
			
			case 20038:
			case 20043:
				if (st.getInt("cond") == 5 && (st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == RUSTED_BRONZE_SWORD_3))
					if (st.dropItemsAlways(POISON_SPIDER_LEG, 1, 20))
						st.set("cond", "6");
				break;
		}
		
		return null;
	}
}
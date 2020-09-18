package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q222_TestOfTheDuelist extends Quest
{
	private static final String qn = "Q222_TestOfTheDuelist";
	
	private static final int KAIEN = 30623;
	
	// Items
	private static final int ORDER_GLUDIO = 2763;
	private static final int ORDER_DION = 2764;
	private static final int ORDER_GIRAN = 2765;
	private static final int ORDER_OREN = 2766;
	private static final int ORDER_ADEN = 2767;
	private static final int PUNCHER_SHARD = 2768;
	private static final int NOBLE_ANT_FEELER = 2769;
	private static final int DRONE_CHITIN = 2770;
	private static final int DEAD_SEEKER_FANG = 2771;
	private static final int OVERLORD_NECKLACE = 2772;
	private static final int FETTERED_SOUL_CHAIN = 2773;
	private static final int CHIEF_AMULET = 2774;
	private static final int ENCHANTED_EYE_MEAT = 2775;
	private static final int TAMRIN_ORC_RING = 2776;
	private static final int TAMRIN_ORC_ARROW = 2777;
	private static final int FINAL_ORDER = 2778;
	private static final int EXCURO_SKIN = 2779;
	private static final int KRATOR_SHARD = 2780;
	private static final int GRANDIS_SKIN = 2781;
	private static final int TIMAK_ORC_BELT = 2782;
	private static final int LAKIN_MACE = 2783;
	
	// Rewards
	private static final int MARK_OF_DUELIST = 2762;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// Monsters
	private static final int PUNCHER = 20085;
	private static final int NOBLE_ANT_LEADER = 20090;
	private static final int MARSH_STAKATO_DRONE = 20234;
	private static final int DEAD_SEEKER = 20202;
	private static final int BREKA_ORC_OVERLORD = 20270;
	private static final int FETTERED_SOUL = 20552;
	private static final int LETO_LIZARDMAN_OVERLORD = 20582;
	private static final int ENCHANTED_MONSTEREYE = 20564;
	private static final int TAMLIN_ORC = 20601;
	private static final int TAMLIN_ORC_ARCHER = 20602;
	private static final int EXCURO = 20214;
	private static final int KRATOR = 20217;
	private static final int GRANDIS = 20554;
	private static final int TIMAK_ORC_OVERLORD = 20588;
	private static final int LAKIN = 20604;
	
	public Q222_TestOfTheDuelist()
	{
		super(222, "Test of the Duelist");
		
		setItemsIds(ORDER_GLUDIO, ORDER_DION, ORDER_GIRAN, ORDER_OREN, ORDER_ADEN, FINAL_ORDER, PUNCHER_SHARD, NOBLE_ANT_FEELER, DRONE_CHITIN, DEAD_SEEKER_FANG, OVERLORD_NECKLACE, FETTERED_SOUL_CHAIN, CHIEF_AMULET, ENCHANTED_EYE_MEAT, TAMRIN_ORC_RING, TAMRIN_ORC_ARROW, EXCURO_SKIN, KRATOR_SHARD, GRANDIS_SKIN, TIMAK_ORC_BELT, LAKIN_MACE);
		
		addStartNpc(KAIEN);
		addTalkId(KAIEN);
		
		addKillId(PUNCHER, NOBLE_ANT_LEADER, MARSH_STAKATO_DRONE, DEAD_SEEKER, BREKA_ORC_OVERLORD, FETTERED_SOUL, LETO_LIZARDMAN_OVERLORD, ENCHANTED_MONSTEREYE, TAMLIN_ORC, TAMLIN_ORC_ARCHER, EXCURO, KRATOR, GRANDIS, TIMAK_ORC_OVERLORD, LAKIN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30623-04.htm"))
		{
			if (player.getRace() == ClassRace.ORC)
				htmltext = "30623-05.htm";
		}
		else if (event.equalsIgnoreCase("30623-07.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(ORDER_GLUDIO, 1);
			st.giveItems(ORDER_DION, 1);
			st.giveItems(ORDER_GIRAN, 1);
			st.giveItems(ORDER_OREN, 1);
			st.giveItems(ORDER_ADEN, 1);
			
			if (!player.getMemos().getBool("secondClassChange39", false))
			{
				htmltext = "30623-07a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39.get(player.getClassId().getId()));
				player.getMemos().set("secondClassChange39", true);
			}
		}
		else if (event.equalsIgnoreCase("30623-16.htm"))
		{
			if (st.getInt("cond") == 3)
			{
				st.set("cond", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
				
				st.takeItems(ORDER_GLUDIO, 1);
				st.takeItems(ORDER_DION, 1);
				st.takeItems(ORDER_GIRAN, 1);
				st.takeItems(ORDER_OREN, 1);
				st.takeItems(ORDER_ADEN, 1);
				
				st.takeItems(PUNCHER_SHARD, -1);
				st.takeItems(NOBLE_ANT_FEELER, -1);
				st.takeItems(DRONE_CHITIN, -1);
				st.takeItems(DEAD_SEEKER_FANG, -1);
				st.takeItems(OVERLORD_NECKLACE, -1);
				st.takeItems(FETTERED_SOUL_CHAIN, -1);
				st.takeItems(CHIEF_AMULET, -1);
				st.takeItems(ENCHANTED_EYE_MEAT, -1);
				st.takeItems(TAMRIN_ORC_RING, -1);
				st.takeItems(TAMRIN_ORC_ARROW, -1);
				
				st.giveItems(FINAL_ORDER, 1);
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
				final int classId = player.getClassId().getId();
				if (classId != 0x01 && classId != 0x2f && classId != 0x13 && classId != 0x20)
					htmltext = "30623-02.htm";
				else if (player.getLevel() < 39)
					htmltext = "30623-01.htm";
				else
					htmltext = "30623-03.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 2)
					htmltext = "30623-07a.htm";
				else if (cond == 3)
					htmltext = "30623-13.htm";
				else if (cond == 4)
					htmltext = "30623-17.htm";
				else if (cond == 5)
				{
					htmltext = "30623-18.htm";
					st.takeItems(FINAL_ORDER, 1);
					st.takeItems(EXCURO_SKIN, -1);
					st.takeItems(KRATOR_SHARD, -1);
					st.takeItems(GRANDIS_SKIN, -1);
					st.takeItems(TIMAK_ORC_BELT, -1);
					st.takeItems(LAKIN_MACE, -1);
					st.giveItems(MARK_OF_DUELIST, 1);
					st.rewardExpAndSp(47015, 20000);
					player.broadcastPacket(new SocialAction(player, 3));
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
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
		
		if (st.getInt("cond") == 2)
		{
			switch (npc.getNpcId())
			{
				case PUNCHER:
					if (st.dropItemsAlways(PUNCHER_SHARD, 1, 10))
						if (st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(DRONE_CHITIN) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_RING) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10)
							st.set("cond", "3");
					break;
				
				case NOBLE_ANT_LEADER:
					if (st.dropItemsAlways(NOBLE_ANT_FEELER, 1, 10))
						if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(DRONE_CHITIN) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_RING) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10)
							st.set("cond", "3");
					break;
				
				case MARSH_STAKATO_DRONE:
					if (st.dropItemsAlways(DRONE_CHITIN, 1, 10))
						if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_RING) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10)
							st.set("cond", "3");
					break;
				
				case DEAD_SEEKER:
					if (st.dropItemsAlways(DEAD_SEEKER_FANG, 1, 10))
						if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(DRONE_CHITIN) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_RING) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10)
							st.set("cond", "3");
					break;
				
				case BREKA_ORC_OVERLORD:
					if (st.dropItemsAlways(OVERLORD_NECKLACE, 1, 10))
						if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(DRONE_CHITIN) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_RING) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10)
							st.set("cond", "3");
					break;
				
				case FETTERED_SOUL:
					if (st.dropItemsAlways(FETTERED_SOUL_CHAIN, 1, 10))
						if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(DRONE_CHITIN) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_RING) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10)
							st.set("cond", "3");
					break;
				
				case LETO_LIZARDMAN_OVERLORD:
					if (st.dropItemsAlways(CHIEF_AMULET, 1, 10))
						if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(DRONE_CHITIN) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_RING) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10)
							st.set("cond", "3");
					break;
				
				case ENCHANTED_MONSTEREYE:
					if (st.dropItemsAlways(ENCHANTED_EYE_MEAT, 1, 10))
						if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(DRONE_CHITIN) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_RING) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10)
							st.set("cond", "3");
					break;
				
				case TAMLIN_ORC:
					if (st.dropItemsAlways(TAMRIN_ORC_RING, 1, 10))
						if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(DRONE_CHITIN) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10)
							st.set("cond", "3");
					break;
				
				case TAMLIN_ORC_ARCHER:
					if (st.dropItemsAlways(TAMRIN_ORC_ARROW, 1, 10))
						if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(DRONE_CHITIN) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_RING) >= 10)
							st.set("cond", "3");
					break;
			}
		}
		else if (st.getInt("cond") == 4)
		{
			switch (npc.getNpcId())
			{
				case EXCURO:
					if (st.dropItemsAlways(EXCURO_SKIN, 1, 3))
						if (st.getQuestItemsCount(KRATOR_SHARD) >= 3 && st.getQuestItemsCount(LAKIN_MACE) >= 3 && st.getQuestItemsCount(GRANDIS_SKIN) >= 3 && st.getQuestItemsCount(TIMAK_ORC_BELT) >= 3)
							st.set("cond", "5");
					break;
				
				case KRATOR:
					if (st.dropItemsAlways(KRATOR_SHARD, 1, 3))
						if (st.getQuestItemsCount(EXCURO_SKIN) >= 3 && st.getQuestItemsCount(LAKIN_MACE) >= 3 && st.getQuestItemsCount(GRANDIS_SKIN) >= 3 && st.getQuestItemsCount(TIMAK_ORC_BELT) >= 3)
							st.set("cond", "5");
					break;
				
				case LAKIN:
					if (st.dropItemsAlways(LAKIN_MACE, 1, 3))
						if (st.getQuestItemsCount(EXCURO_SKIN) >= 3 && st.getQuestItemsCount(KRATOR_SHARD) >= 3 && st.getQuestItemsCount(GRANDIS_SKIN) >= 3 && st.getQuestItemsCount(TIMAK_ORC_BELT) >= 3)
							st.set("cond", "5");
					break;
				
				case GRANDIS:
					if (st.dropItemsAlways(GRANDIS_SKIN, 1, 3))
						if (st.getQuestItemsCount(EXCURO_SKIN) >= 3 && st.getQuestItemsCount(KRATOR_SHARD) >= 3 && st.getQuestItemsCount(LAKIN_MACE) >= 3 && st.getQuestItemsCount(TIMAK_ORC_BELT) >= 3)
							st.set("cond", "5");
					break;
				
				case TIMAK_ORC_OVERLORD:
					if (st.dropItemsAlways(TIMAK_ORC_BELT, 1, 3))
						if (st.getQuestItemsCount(EXCURO_SKIN) >= 3 && st.getQuestItemsCount(KRATOR_SHARD) >= 3 && st.getQuestItemsCount(LAKIN_MACE) >= 3 && st.getQuestItemsCount(GRANDIS_SKIN) >= 3)
							st.set("cond", "5");
					break;
			}
		}
		
		return null;
	}
}
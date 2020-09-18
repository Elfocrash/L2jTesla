package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q223_TestOfTheChampion extends Quest
{
	private static final String qn = "Q223_TestOfTheChampion";
	
	// Items
	private static final int ASCALON_LETTER_1 = 3277;
	private static final int MASON_LETTER = 3278;
	private static final int IRON_ROSE_RING = 3279;
	private static final int ASCALON_LETTER_2 = 3280;
	private static final int WHITE_ROSE_INSIGNIA = 3281;
	private static final int GROOT_LETTER = 3282;
	private static final int ASCALON_LETTER_3 = 3283;
	private static final int MOUEN_ORDER_1 = 3284;
	private static final int MOUEN_ORDER_2 = 3285;
	private static final int MOUEN_LETTER = 3286;
	private static final int HARPY_EGG = 3287;
	private static final int MEDUSA_VENOM = 3288;
	private static final int WINDSUS_BILE = 3289;
	private static final int BLOODY_AXE_HEAD = 3290;
	private static final int ROAD_RATMAN_HEAD = 3291;
	private static final int LETO_LIZARDMAN_FANG = 3292;
	
	// Rewards
	private static final int MARK_OF_CHAMPION = 3276;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int ASCALON = 30624;
	private static final int GROOT = 30093;
	private static final int MOUEN = 30196;
	private static final int MASON = 30625;
	
	// Monsters
	private static final int HARPY = 20145;
	private static final int HARPY_MATRIARCH = 27088;
	private static final int MEDUSA = 20158;
	private static final int WINDSUS = 20553;
	private static final int ROAD_COLLECTOR = 27089;
	private static final int ROAD_SCAVENGER = 20551;
	private static final int LETO_LIZARDMAN = 20577;
	private static final int LETO_LIZARDMAN_ARCHER = 20578;
	private static final int LETO_LIZARDMAN_SOLDIER = 20579;
	private static final int LETO_LIZARDMAN_WARRIOR = 20580;
	private static final int LETO_LIZARDMAN_SHAMAN = 20581;
	private static final int LETO_LIZARDMAN_OVERLORD = 20582;
	private static final int BLOODY_AXE_ELITE = 20780;
	
	public Q223_TestOfTheChampion()
	{
		super(223, "Test of the Champion");
		
		setItemsIds(MASON_LETTER, MEDUSA_VENOM, WINDSUS_BILE, WHITE_ROSE_INSIGNIA, HARPY_EGG, GROOT_LETTER, MOUEN_LETTER, ASCALON_LETTER_1, IRON_ROSE_RING, BLOODY_AXE_HEAD, ASCALON_LETTER_2, ASCALON_LETTER_3, MOUEN_ORDER_1, ROAD_RATMAN_HEAD, MOUEN_ORDER_2, LETO_LIZARDMAN_FANG);
		
		addStartNpc(ASCALON);
		addTalkId(ASCALON, GROOT, MOUEN, MASON);
		
		addAttackId(HARPY, ROAD_SCAVENGER);
		addKillId(HARPY, MEDUSA, HARPY_MATRIARCH, ROAD_COLLECTOR, ROAD_SCAVENGER, WINDSUS, LETO_LIZARDMAN, LETO_LIZARDMAN_ARCHER, LETO_LIZARDMAN_SOLDIER, LETO_LIZARDMAN_WARRIOR, LETO_LIZARDMAN_SHAMAN, LETO_LIZARDMAN_OVERLORD, BLOODY_AXE_ELITE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equals("30624-06.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(ASCALON_LETTER_1, 1);
			
			if (!player.getMemos().getBool("secondClassChange39", false))
			{
				htmltext = "30624-06a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39.get(player.getClassId().getId()));
				player.getMemos().set("secondClassChange39", true);
			}
		}
		else if (event.equals("30624-10.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(MASON_LETTER, 1);
			st.giveItems(ASCALON_LETTER_2, 1);
		}
		else if (event.equals("30624-14.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(GROOT_LETTER, 1);
			st.giveItems(ASCALON_LETTER_3, 1);
		}
		else if (event.equals("30625-03.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ASCALON_LETTER_1, 1);
			st.giveItems(IRON_ROSE_RING, 1);
		}
		else if (event.equals("30093-02.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ASCALON_LETTER_2, 1);
			st.giveItems(WHITE_ROSE_INSIGNIA, 1);
		}
		else if (event.equals("30196-03.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ASCALON_LETTER_3, 1);
			st.giveItems(MOUEN_ORDER_1, 1);
		}
		else if (event.equals("30196-06.htm"))
		{
			st.set("cond", "12");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(MOUEN_ORDER_1, 1);
			st.takeItems(ROAD_RATMAN_HEAD, 1);
			st.giveItems(MOUEN_ORDER_2, 1);
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
				final ClassId classId = player.getClassId();
				if (classId != ClassId.WARRIOR && classId != ClassId.ORC_RAIDER)
					htmltext = "30624-01.htm";
				else if (player.getLevel() < 39)
					htmltext = "30624-02.htm";
				else
					htmltext = (classId == ClassId.WARRIOR) ? "30624-03.htm" : "30624-04.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ASCALON:
						if (cond == 1)
							htmltext = "30624-07.htm";
						else if (cond < 4)
							htmltext = "30624-08.htm";
						else if (cond == 4)
							htmltext = "30624-09.htm";
						else if (cond == 5)
							htmltext = "30624-11.htm";
						else if (cond > 5 && cond < 8)
							htmltext = "30624-12.htm";
						else if (cond == 8)
							htmltext = "30624-13.htm";
						else if (cond == 9)
							htmltext = "30624-15.htm";
						else if (cond > 9 && cond < 14)
							htmltext = "30624-16.htm";
						else if (cond == 14)
						{
							htmltext = "30624-17.htm";
							st.takeItems(MOUEN_LETTER, 1);
							st.giveItems(MARK_OF_CHAMPION, 1);
							st.rewardExpAndSp(117454, 25000);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case MASON:
						if (cond == 1)
							htmltext = "30625-01.htm";
						else if (cond == 2)
							htmltext = "30625-04.htm";
						else if (cond == 3)
						{
							htmltext = "30625-05.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BLOODY_AXE_HEAD, -1);
							st.takeItems(IRON_ROSE_RING, 1);
							st.giveItems(MASON_LETTER, 1);
						}
						else if (cond == 4)
							htmltext = "30625-06.htm";
						else if (cond > 4)
							htmltext = "30625-07.htm";
						break;
					
					case GROOT:
						if (cond == 5)
							htmltext = "30093-01.htm";
						else if (cond == 6)
							htmltext = "30093-03.htm";
						else if (cond == 7)
						{
							htmltext = "30093-04.htm";
							st.set("cond", "8");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(WHITE_ROSE_INSIGNIA, 1);
							st.takeItems(HARPY_EGG, -1);
							st.takeItems(MEDUSA_VENOM, -1);
							st.takeItems(WINDSUS_BILE, -1);
							st.giveItems(GROOT_LETTER, 1);
						}
						else if (cond == 8)
							htmltext = "30093-05.htm";
						else if (cond > 8)
							htmltext = "30093-06.htm";
						break;
					
					case MOUEN:
						if (cond == 9)
							htmltext = "30196-01.htm";
						else if (cond == 10)
							htmltext = "30196-04.htm";
						else if (cond == 11)
							htmltext = "30196-05.htm";
						else if (cond == 12)
							htmltext = "30196-07.htm";
						else if (cond == 13)
						{
							htmltext = "30196-08.htm";
							st.set("cond", "14");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(LETO_LIZARDMAN_FANG, -1);
							st.takeItems(MOUEN_ORDER_2, 1);
							st.giveItems(MOUEN_LETTER, 1);
						}
						else if (cond > 13)
							htmltext = "30196-09.htm";
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
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		final Player player = attacker.getActingPlayer();
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		switch (npc.getNpcId())
		{
			case HARPY: // Possibility to spawn an HARPY _MATRIARCH.
				if (st.getInt("cond") == 6 && Rnd.nextBoolean() && !npc.isScriptValue(1))
				{
					// Spawn one or two matriarchs.
					for (int i = 1; i < ((Rnd.get(10) < 7) ? 2 : 3); i++)
					{
						final Attackable collector = (Attackable) addSpawn(HARPY_MATRIARCH, npc, true, 0, false);
						
						collector.setRunning();
						collector.addDamageHate(attacker, 0, 999);
						collector.getAI().setIntention(IntentionType.ATTACK, attacker);
					}
					npc.setScriptValue(1);
				}
				break;
			
			case ROAD_SCAVENGER: // Possibility to spawn a Road Collector.
				if (st.getInt("cond") == 10 && Rnd.nextBoolean() && !npc.isScriptValue(1))
				{
					// Spawn one or two collectors.
					for (int i = 1; i < ((Rnd.get(10) < 7) ? 2 : 3); i++)
					{
						final Attackable collector = (Attackable) addSpawn(ROAD_COLLECTOR, npc, true, 0, false);
						
						collector.setRunning();
						collector.addDamageHate(attacker, 0, 999);
						collector.getAI().setIntention(IntentionType.ATTACK, attacker);
					}
					npc.setScriptValue(1);
				}
				break;
		}
		
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		final int npcId = npc.getNpcId();
		
		switch (npcId)
		{
			case BLOODY_AXE_ELITE:
				if (st.getInt("cond") == 2 && st.dropItemsAlways(BLOODY_AXE_HEAD, 1, 100))
					st.set("cond", "3");
				break;
			
			case HARPY:
			case HARPY_MATRIARCH:
				if (st.getInt("cond") == 6 && st.dropItems(HARPY_EGG, 1, 30, 500000))
					if (st.getQuestItemsCount(MEDUSA_VENOM) == 30 && st.getQuestItemsCount(WINDSUS_BILE) == 30)
						st.set("cond", "7");
				break;
			
			case MEDUSA:
				if (st.getInt("cond") == 6 && st.dropItems(MEDUSA_VENOM, 1, 30, 500000))
					if (st.getQuestItemsCount(HARPY_EGG) == 30 && st.getQuestItemsCount(WINDSUS_BILE) == 30)
						st.set("cond", "7");
				break;
			
			case WINDSUS:
				if (st.getInt("cond") == 6 && st.dropItems(WINDSUS_BILE, 1, 30, 500000))
					if (st.getQuestItemsCount(HARPY_EGG) == 30 && st.getQuestItemsCount(MEDUSA_VENOM) == 30)
						st.set("cond", "7");
				break;
			
			case ROAD_COLLECTOR:
			case ROAD_SCAVENGER:
				if (st.getInt("cond") == 10 && st.dropItemsAlways(ROAD_RATMAN_HEAD, 1, 100))
					st.set("cond", "11");
				break;
			
			case LETO_LIZARDMAN:
			case LETO_LIZARDMAN_ARCHER:
			case LETO_LIZARDMAN_SOLDIER:
			case LETO_LIZARDMAN_WARRIOR:
			case LETO_LIZARDMAN_SHAMAN:
			case LETO_LIZARDMAN_OVERLORD:
				if (st.getInt("cond") == 12 && st.dropItems(LETO_LIZARDMAN_FANG, 1, 100, 500000 + (100000 * (npcId - 20577))))
					st.set("cond", "13");
				break;
		}
		
		return null;
	}
}
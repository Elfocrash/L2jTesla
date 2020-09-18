package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q417_PathToBecomeAScavenger extends Quest
{
	private static final String qn = "Q417_PathToBecomeAScavenger";
	
	// Items
	private static final int RING_OF_RAVEN = 1642;
	private static final int PIPPI_LETTER = 1643;
	private static final int RAUT_TELEPORT_SCROLL = 1644;
	private static final int SUCCUBUS_UNDIES = 1645;
	private static final int MION_LETTER = 1646;
	private static final int BRONK_INGOT = 1647;
	private static final int SHARI_AXE = 1648;
	private static final int ZIMENF_POTION = 1649;
	private static final int BRONK_PAY = 1650;
	private static final int SHARI_PAY = 1651;
	private static final int ZIMENF_PAY = 1652;
	private static final int BEAR_PICTURE = 1653;
	private static final int TARANTULA_PICTURE = 1654;
	private static final int HONEY_JAR = 1655;
	private static final int BEAD = 1656;
	private static final int BEAD_PARCEL_1 = 1657;
	private static final int BEAD_PARCEL_2 = 8543;
	
	// NPCs
	private static final int RAUT = 30316;
	private static final int SHARI = 30517;
	private static final int MION = 30519;
	private static final int PIPPI = 30524;
	private static final int BRONK = 30525;
	private static final int ZIMENF = 30538;
	private static final int TOMA = 30556;
	private static final int TORAI = 30557;
	private static final int YASHENI = 31958;
	
	// Monsters
	private static final int HUNTER_TARANTULA = 20403;
	private static final int PLUNDER_TARANTULA = 20508;
	private static final int HUNTER_BEAR = 20777;
	private static final int HONEY_BEAR = 27058;
	
	public Q417_PathToBecomeAScavenger()
	{
		super(417, "Path To Become A Scavenger");
		
		setItemsIds(PIPPI_LETTER, RAUT_TELEPORT_SCROLL, SUCCUBUS_UNDIES, MION_LETTER, BRONK_INGOT, SHARI_AXE, ZIMENF_POTION, BRONK_PAY, SHARI_PAY, ZIMENF_PAY, BEAR_PICTURE, TARANTULA_PICTURE, HONEY_JAR, BEAD, BEAD_PARCEL_1, BEAD_PARCEL_2);
		
		addStartNpc(PIPPI);
		addTalkId(RAUT, SHARI, MION, PIPPI, BRONK, ZIMENF, TOMA, TORAI, YASHENI);
		
		addKillId(HUNTER_TARANTULA, PLUNDER_TARANTULA, HUNTER_BEAR, HONEY_BEAR);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// PIPPI
		if (event.equalsIgnoreCase("30524-05.htm"))
		{
			if (player.getClassId() != ClassId.DWARVEN_FIGHTER)
				htmltext = (player.getClassId() == ClassId.SCAVENGER) ? "30524-02a.htm" : "30524-08.htm";
			else if (player.getLevel() < 19)
				htmltext = "30524-02.htm";
			else if (st.hasQuestItems(RING_OF_RAVEN))
				htmltext = "30524-04.htm";
			else
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
				st.giveItems(PIPPI_LETTER, 1);
			}
		}
		// MION
		else if (event.equalsIgnoreCase("30519_1"))
		{
			final int random = Rnd.get(3);
			
			htmltext = "30519-0" + (random + 2) + ".htm";
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(PIPPI_LETTER, -1);
			st.giveItems(ZIMENF_POTION - random, 1);
		}
		else if (event.equalsIgnoreCase("30519_2"))
		{
			final int random = Rnd.get(3);
			
			htmltext = "30519-0" + (random + 2) + ".htm";
			st.takeItems(BRONK_PAY, -1);
			st.takeItems(SHARI_PAY, -1);
			st.takeItems(ZIMENF_PAY, -1);
			st.giveItems(ZIMENF_POTION - random, 1);
		}
		else if (event.equalsIgnoreCase("30519-07.htm"))
			st.set("id", String.valueOf(st.getInt("id") + 1));
		else if (event.equalsIgnoreCase("30519-09.htm"))
		{
			int id = st.getInt("id");
			if (id / 10 < 2)
			{
				htmltext = "30519-07.htm";
				st.set("id", String.valueOf(id + 1));
			}
			else if (id / 10 == 2)
				st.set("id", String.valueOf(id + 1));
			else if (id / 10 >= 3)
			{
				htmltext = "30519-10.htm";
				st.set("cond", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(SHARI_AXE, -1);
				st.takeItems(ZIMENF_POTION, -1);
				st.takeItems(BRONK_INGOT, -1);
				st.giveItems(MION_LETTER, 1);
			}
		}
		else if (event.equalsIgnoreCase("30519-11.htm") && Rnd.nextBoolean())
			htmltext = "30519-06.htm";
		// TOMA
		else if (event.equalsIgnoreCase("30556-05b.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BEAD, -1);
			st.takeItems(TARANTULA_PICTURE, 1);
			st.giveItems(BEAD_PARCEL_1, 1);
		}
		else if (event.equalsIgnoreCase("30556-06b.htm"))
		{
			st.set("cond", "12");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BEAD, -1);
			st.takeItems(TARANTULA_PICTURE, 1);
			st.giveItems(BEAD_PARCEL_2, 1);
		}
		// RAUT
		else if (event.equalsIgnoreCase("30316-02.htm") || event.equalsIgnoreCase("30316-03.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BEAD_PARCEL_1, 1);
			st.giveItems(RAUT_TELEPORT_SCROLL, 1);
		}
		// TORAI
		else if (event.equalsIgnoreCase("30557-03.htm"))
		{
			st.set("cond", "11");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(RAUT_TELEPORT_SCROLL, 1);
			st.giveItems(SUCCUBUS_UNDIES, 1);
		}
		// YASHENI
		else if (event.equalsIgnoreCase("31958-02.htm"))
		{
			st.takeItems(BEAD_PARCEL_2, 1);
			st.giveItems(RING_OF_RAVEN, 1);
			st.rewardExpAndSp(3200, 7080);
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
				htmltext = "30524-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case PIPPI:
						if (cond == 1)
							htmltext = "30524-06.htm";
						else if (cond > 1)
							htmltext = "30524-07.htm";
						break;
					
					case MION:
						if (st.hasQuestItems(PIPPI_LETTER))
							htmltext = "30519-01.htm";
						else if (st.hasAtLeastOneQuestItem(BRONK_INGOT, SHARI_AXE, ZIMENF_POTION))
						{
							int id = st.getInt("id");
							if (id / 10 == 0)
								htmltext = "30519-05.htm";
							else
								htmltext = "30519-08.htm";
						}
						else if (st.hasAtLeastOneQuestItem(BRONK_PAY, SHARI_PAY, ZIMENF_PAY))
						{
							int id = st.getInt("id");
							if (id < 50)
								htmltext = "30519-12.htm";
							else
							{
								htmltext = "30519-15.htm";
								st.set("cond", "4");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(BRONK_PAY, -1);
								st.takeItems(SHARI_PAY, -1);
								st.takeItems(ZIMENF_PAY, -1);
								st.giveItems(MION_LETTER, 1);
							}
						}
						else if (cond == 4)
							htmltext = "30519-13.htm";
						else if (cond > 4)
							htmltext = "30519-14.htm";
						break;
					
					case SHARI:
						if (st.hasQuestItems(SHARI_AXE))
						{
							int id = st.getInt("id");
							if (id < 20)
								htmltext = "30517-01.htm";
							else
							{
								htmltext = "30517-02.htm";
								st.set("cond", "3");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							st.set("id", String.valueOf(id + 10));
							st.takeItems(SHARI_AXE, 1);
							st.giveItems(SHARI_PAY, 1);
						}
						else if (st.hasQuestItems(SHARI_PAY))
							htmltext = "30517-03.htm";
						break;
					
					case BRONK:
						if (st.hasQuestItems(BRONK_INGOT))
						{
							int id = st.getInt("id");
							if (id < 20)
								htmltext = "30525-01.htm";
							else
							{
								htmltext = "30525-02.htm";
								st.set("cond", "3");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							st.set("id", String.valueOf(id + 10));
							st.takeItems(BRONK_INGOT, 1);
							st.giveItems(BRONK_PAY, 1);
						}
						else if (st.hasQuestItems(BRONK_PAY))
							htmltext = "30525-03.htm";
						break;
					
					case ZIMENF:
						if (st.hasQuestItems(ZIMENF_POTION))
						{
							int id = st.getInt("id");
							if (id < 20)
								htmltext = "30538-01.htm";
							else
							{
								htmltext = "30538-02.htm";
								st.set("cond", "3");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							st.set("id", String.valueOf(id + 10));
							st.takeItems(ZIMENF_POTION, 1);
							st.giveItems(ZIMENF_PAY, 1);
						}
						else if (st.hasQuestItems(ZIMENF_PAY))
							htmltext = "30538-03.htm";
						break;
					
					case TOMA:
						if (cond == 4)
						{
							htmltext = "30556-01.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(MION_LETTER, 1);
							st.giveItems(BEAR_PICTURE, 1);
						}
						else if (cond == 5)
							htmltext = "30556-02.htm";
						else if (cond == 6)
						{
							htmltext = "30556-03.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(HONEY_JAR, -1);
							st.takeItems(BEAR_PICTURE, 1);
							st.giveItems(TARANTULA_PICTURE, 1);
						}
						else if (cond == 7)
							htmltext = "30556-04.htm";
						else if (cond == 8)
							htmltext = "30556-05a.htm";
						else if (cond == 9)
							htmltext = "30556-06a.htm";
						else if (cond == 10 || cond == 11)
							htmltext = "30556-07.htm";
						else if (cond == 12)
							htmltext = "30556-06c.htm";
						break;
					
					case RAUT:
						if (cond == 9)
							htmltext = "30316-01.htm";
						else if (cond == 10)
							htmltext = "30316-04.htm";
						else if (cond == 11)
						{
							htmltext = "30316-05.htm";
							st.takeItems(SUCCUBUS_UNDIES, 1);
							st.giveItems(RING_OF_RAVEN, 1);
							st.rewardExpAndSp(3200, 7080);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case TORAI:
						if (cond == 10)
							htmltext = "30557-01.htm";
						break;
					
					case YASHENI:
						if (cond == 12)
							htmltext = "31958-01.htm";
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
			case HUNTER_BEAR:
				if (st.getInt("cond") == 5)
				{
					int step = st.getInt("step");
					if (step > 20)
					{
						if (((step - 20) * 10) >= Rnd.get(100))
						{
							addSpawn(HONEY_BEAR, npc, false, 300000, true);
							st.unset("step");
						}
						else
							st.set("step", String.valueOf(step + 1));
					}
					else
						st.set("step", String.valueOf(step + 1));
				}
				break;
			
			case HONEY_BEAR:
				if (st.getInt("cond") == 5 && ((Monster) npc).getSpoilerId() == player.getObjectId() && st.dropItemsAlways(HONEY_JAR, 1, 5))
					st.set("cond", "6");
				break;
			
			case HUNTER_TARANTULA:
			case PLUNDER_TARANTULA:
				if (st.getInt("cond") == 7 && ((Monster) npc).getSpoilerId() == player.getObjectId() && st.dropItems(BEAD, 1, 20, (npc.getNpcId() == HUNTER_TARANTULA) ? 333333 : 600000))
					st.set("cond", "8");
				break;
		}
		
		return null;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q231_TestOfTheMaestro extends Quest
{
	private static final String qn = "Q231_TestOfTheMaestro";
	
	private static final int RECOMMENDATION_OF_BALANKI = 2864;
	private static final int RECOMMENDATION_OF_FILAUR = 2865;
	private static final int RECOMMENDATION_OF_ARIN = 2866;
	private static final int LETTER_OF_SOLDER_DETACHMENT = 2868;
	private static final int PAINT_OF_KAMURU = 2869;
	private static final int NECKLACE_OF_KAMURU = 2870;
	private static final int PAINT_OF_TELEPORT_DEVICE = 2871;
	private static final int TELEPORT_DEVICE = 2872;
	private static final int ARCHITECTURE_OF_KRUMA = 2873;
	private static final int REPORT_OF_KRUMA = 2874;
	private static final int INGREDIENTS_OF_ANTIDOTE = 2875;
	private static final int STINGER_WASP_NEEDLE = 2876;
	private static final int MARSH_SPIDER_WEB = 2877;
	private static final int BLOOD_OF_LEECH = 2878;
	private static final int BROKEN_TELEPORT_DEVICE = 2916;
	
	// Rewards
	private static final int MARK_OF_MAESTRO = 2867;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int LOCKIRIN = 30531;
	private static final int SPIRON = 30532;
	private static final int BALANKI = 30533;
	private static final int KEEF = 30534;
	private static final int FILAUR = 30535;
	private static final int ARIN = 30536;
	private static final int TOMA = 30556;
	private static final int CROTO = 30671;
	private static final int DUBABAH = 30672;
	private static final int LORAIN = 30673;
	
	// Monsters
	private static final int KING_BUGBEAR = 20150;
	private static final int GIANT_MIST_LEECH = 20225;
	private static final int STINGER_WASP = 20229;
	private static final int MARSH_SPIDER = 20233;
	private static final int EVIL_EYE_LORD = 27133;
	
	public Q231_TestOfTheMaestro()
	{
		super(231, "Test Of The Maestro");
		
		setItemsIds(RECOMMENDATION_OF_BALANKI, RECOMMENDATION_OF_FILAUR, RECOMMENDATION_OF_ARIN, LETTER_OF_SOLDER_DETACHMENT, PAINT_OF_KAMURU, NECKLACE_OF_KAMURU, PAINT_OF_TELEPORT_DEVICE, TELEPORT_DEVICE, ARCHITECTURE_OF_KRUMA, REPORT_OF_KRUMA, INGREDIENTS_OF_ANTIDOTE, STINGER_WASP_NEEDLE, MARSH_SPIDER_WEB, BLOOD_OF_LEECH, BROKEN_TELEPORT_DEVICE);
		
		addStartNpc(LOCKIRIN);
		addTalkId(LOCKIRIN, SPIRON, BALANKI, KEEF, FILAUR, ARIN, TOMA, CROTO, DUBABAH, LORAIN);
		
		addKillId(GIANT_MIST_LEECH, STINGER_WASP, MARSH_SPIDER, EVIL_EYE_LORD);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// LOCKIRIN
		if (event.equalsIgnoreCase("30531-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			
			if (!player.getMemos().getBool("secondClassChange39", false))
			{
				htmltext = "30531-04a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39.get(player.getClassId().getId()));
				player.getMemos().set("secondClassChange39", true);
			}
		}
		// BALANKI
		else if (event.equalsIgnoreCase("30533-02.htm"))
			st.set("bCond", "1");
		// CROTO
		else if (event.equalsIgnoreCase("30671-02.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(PAINT_OF_KAMURU, 1);
		}
		// TOMA
		else if (event.equalsIgnoreCase("30556-05.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(PAINT_OF_TELEPORT_DEVICE, 1);
			st.giveItems(BROKEN_TELEPORT_DEVICE, 1);
			player.teleportTo(140352, -194133, -3146, 0);
			startQuestTimer("spawn_bugbears", 5000, null, player, false);
		}
		// LORAIN
		else if (event.equalsIgnoreCase("30673-04.htm"))
		{
			st.set("fCond", "2");
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(BLOOD_OF_LEECH, -1);
			st.takeItems(INGREDIENTS_OF_ANTIDOTE, 1);
			st.takeItems(MARSH_SPIDER_WEB, -1);
			st.takeItems(STINGER_WASP_NEEDLE, -1);
			st.giveItems(REPORT_OF_KRUMA, 1);
		}
		// Spawns 3 King Bugbears
		else if (event.equalsIgnoreCase("spawn_bugbears"))
		{
			final Attackable bugbear1 = (Attackable) addSpawn(KING_BUGBEAR, 140333, -194153, -3138, 0, false, 200000, true);
			bugbear1.addDamageHate(player, 0, 999);
			bugbear1.getAI().setIntention(IntentionType.ATTACK, player);
			
			final Attackable bugbear2 = (Attackable) addSpawn(KING_BUGBEAR, 140395, -194147, -3146, 0, false, 200000, true);
			bugbear2.addDamageHate(player, 0, 999);
			bugbear2.getAI().setIntention(IntentionType.ATTACK, player);
			
			final Attackable bugbear3 = (Attackable) addSpawn(KING_BUGBEAR, 140304, -194082, -3157, 0, false, 200000, true);
			bugbear3.addDamageHate(player, 0, 999);
			bugbear3.getAI().setIntention(IntentionType.ATTACK, player);
			
			return null;
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
				if (player.getClassId() != ClassId.ARTISAN)
					htmltext = "30531-01.htm";
				else if (player.getLevel() < 39)
					htmltext = "30531-02.htm";
				else
					htmltext = "30531-03.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case LOCKIRIN:
						int cond = st.getInt("cond");
						if (cond == 1)
							htmltext = "30531-05.htm";
						else if (cond == 2)
						{
							htmltext = "30531-06.htm";
							st.takeItems(RECOMMENDATION_OF_ARIN, 1);
							st.takeItems(RECOMMENDATION_OF_BALANKI, 1);
							st.takeItems(RECOMMENDATION_OF_FILAUR, 1);
							st.giveItems(MARK_OF_MAESTRO, 1);
							st.rewardExpAndSp(46000, 5900);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case SPIRON:
						htmltext = "30532-01.htm";
						break;
					
					case KEEF:
						htmltext = "30534-01.htm";
						break;
					
					// Part 1
					case BALANKI:
						int bCond = st.getInt("bCond");
						if (bCond == 0)
							htmltext = "30533-01.htm";
						else if (bCond == 1)
							htmltext = "30533-03.htm";
						else if (bCond == 2)
						{
							htmltext = "30533-04.htm";
							st.set("bCond", "3");
							st.takeItems(LETTER_OF_SOLDER_DETACHMENT, 1);
							st.giveItems(RECOMMENDATION_OF_BALANKI, 1);
							
							if (st.hasQuestItems(RECOMMENDATION_OF_ARIN, RECOMMENDATION_OF_FILAUR))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
								st.playSound(QuestState.SOUND_ITEMGET);
						}
						else if (bCond == 3)
							htmltext = "30533-05.htm";
						break;
					
					case CROTO:
						bCond = st.getInt("bCond");
						if (bCond == 1)
						{
							if (!st.hasQuestItems(PAINT_OF_KAMURU))
								htmltext = "30671-01.htm";
							else if (!st.hasQuestItems(NECKLACE_OF_KAMURU))
								htmltext = "30671-03.htm";
							else
							{
								htmltext = "30671-04.htm";
								st.set("bCond", "2");
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(NECKLACE_OF_KAMURU, 1);
								st.takeItems(PAINT_OF_KAMURU, 1);
								st.giveItems(LETTER_OF_SOLDER_DETACHMENT, 1);
							}
						}
						else if (bCond > 1)
							htmltext = "30671-05.htm";
						break;
					
					case DUBABAH:
						htmltext = "30672-01.htm";
						break;
					
					// Part 2
					case ARIN:
						int aCond = st.getInt("aCond");
						if (aCond == 0)
						{
							htmltext = "30536-01.htm";
							st.set("aCond", "1");
							st.giveItems(PAINT_OF_TELEPORT_DEVICE, 1);
						}
						else if (aCond == 1)
							htmltext = "30536-02.htm";
						else if (aCond == 2)
						{
							htmltext = "30536-03.htm";
							st.set("aCond", "3");
							st.takeItems(TELEPORT_DEVICE, -1);
							st.giveItems(RECOMMENDATION_OF_ARIN, 1);
							
							if (st.hasQuestItems(RECOMMENDATION_OF_BALANKI, RECOMMENDATION_OF_FILAUR))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
								st.playSound(QuestState.SOUND_ITEMGET);
						}
						else if (aCond == 3)
							htmltext = "30536-04.htm";
						break;
					
					case TOMA:
						aCond = st.getInt("aCond");
						if (aCond == 1)
						{
							if (!st.hasQuestItems(BROKEN_TELEPORT_DEVICE))
								htmltext = "30556-01.htm";
							else if (!st.hasQuestItems(TELEPORT_DEVICE))
							{
								htmltext = "30556-06.htm";
								st.set("aCond", "2");
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(BROKEN_TELEPORT_DEVICE, 1);
								st.giveItems(TELEPORT_DEVICE, 5);
							}
						}
						else if (aCond > 1)
							htmltext = "30556-07.htm";
						break;
					
					// Part 3
					case FILAUR:
						int fCond = st.getInt("fCond");
						if (fCond == 0)
						{
							htmltext = "30535-01.htm";
							st.set("fCond", "1");
							st.playSound(QuestState.SOUND_ITEMGET);
							st.giveItems(ARCHITECTURE_OF_KRUMA, 1);
						}
						else if (fCond == 1)
							htmltext = "30535-02.htm";
						else if (fCond == 2)
						{
							htmltext = "30535-03.htm";
							st.set("fCond", "3");
							st.takeItems(REPORT_OF_KRUMA, 1);
							st.giveItems(RECOMMENDATION_OF_FILAUR, 1);
							
							if (st.hasQuestItems(RECOMMENDATION_OF_BALANKI, RECOMMENDATION_OF_ARIN))
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
								st.playSound(QuestState.SOUND_ITEMGET);
						}
						else if (fCond == 3)
							htmltext = "30535-04.htm";
						break;
					
					case LORAIN:
						fCond = st.getInt("fCond");
						if (fCond == 1)
						{
							if (!st.hasQuestItems(REPORT_OF_KRUMA))
							{
								if (!st.hasQuestItems(INGREDIENTS_OF_ANTIDOTE))
								{
									htmltext = "30673-01.htm";
									st.playSound(QuestState.SOUND_ITEMGET);
									st.takeItems(ARCHITECTURE_OF_KRUMA, 1);
									st.giveItems(INGREDIENTS_OF_ANTIDOTE, 1);
								}
								else if (st.getQuestItemsCount(STINGER_WASP_NEEDLE) < 10 || st.getQuestItemsCount(MARSH_SPIDER_WEB) < 10 || st.getQuestItemsCount(BLOOD_OF_LEECH) < 10)
									htmltext = "30673-02.htm";
								else
									htmltext = "30673-03.htm";
							}
						}
						else if (fCond > 1)
							htmltext = "30673-05.htm";
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
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		switch (npc.getNpcId())
		{
			case GIANT_MIST_LEECH:
				if (st.hasQuestItems(INGREDIENTS_OF_ANTIDOTE))
					st.dropItemsAlways(BLOOD_OF_LEECH, 1, 10);
				break;
			
			case STINGER_WASP:
				if (st.hasQuestItems(INGREDIENTS_OF_ANTIDOTE))
					st.dropItemsAlways(STINGER_WASP_NEEDLE, 1, 10);
				break;
			
			case MARSH_SPIDER:
				if (st.hasQuestItems(INGREDIENTS_OF_ANTIDOTE))
					st.dropItemsAlways(MARSH_SPIDER_WEB, 1, 10);
				break;
			
			case EVIL_EYE_LORD:
				if (st.hasQuestItems(PAINT_OF_KAMURU))
					st.dropItemsAlways(NECKLACE_OF_KAMURU, 1, 1);
				break;
		}
		
		return null;
	}
}
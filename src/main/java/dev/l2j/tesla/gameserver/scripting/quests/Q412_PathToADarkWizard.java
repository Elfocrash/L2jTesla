package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q412_PathToADarkWizard extends Quest
{
	private static final String qn = "Q412_PathToADarkWizard";
	
	// Items
	private static final int SEED_OF_ANGER = 1253;
	private static final int SEED_OF_DESPAIR = 1254;
	private static final int SEED_OF_HORROR = 1255;
	private static final int SEED_OF_LUNACY = 1256;
	private static final int FAMILY_REMAINS = 1257;
	private static final int VARIKA_LIQUOR = 1258;
	private static final int KNEE_BONE = 1259;
	private static final int HEART_OF_LUNACY = 1260;
	private static final int JEWEL_OF_DARKNESS = 1261;
	private static final int LUCKY_KEY = 1277;
	private static final int CANDLE = 1278;
	private static final int HUB_SCENT = 1279;
	
	// NPCs
	private static final int VARIKA = 30421;
	private static final int CHARKEREN = 30415;
	private static final int ANNIKA = 30418;
	private static final int ARKENIA = 30419;
	
	public Q412_PathToADarkWizard()
	{
		super(412, "Path to a Dark Wizard");
		
		setItemsIds(SEED_OF_ANGER, SEED_OF_DESPAIR, SEED_OF_HORROR, SEED_OF_LUNACY, FAMILY_REMAINS, VARIKA_LIQUOR, KNEE_BONE, HEART_OF_LUNACY, LUCKY_KEY, CANDLE, HUB_SCENT);
		
		addStartNpc(VARIKA);
		addTalkId(VARIKA, CHARKEREN, ANNIKA, ARKENIA);
		
		addKillId(20015, 20022, 20045, 20517, 20518);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30421-05.htm"))
		{
			if (player.getClassId() != ClassId.DARK_MYSTIC)
				htmltext = (player.getClassId() == ClassId.DARK_WIZARD) ? "30421-02a.htm" : "30421-03.htm";
			else if (player.getLevel() < 19)
				htmltext = "30421-02.htm";
			else if (st.hasQuestItems(JEWEL_OF_DARKNESS))
				htmltext = "30421-04.htm";
			else
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
				st.giveItems(SEED_OF_DESPAIR, 1);
			}
		}
		else if (event.equalsIgnoreCase("30421-07.htm"))
		{
			if (st.hasQuestItems(SEED_OF_ANGER))
				htmltext = "30421-06.htm";
			else if (st.hasQuestItems(LUCKY_KEY))
				htmltext = "30421-08.htm";
			else if (st.getQuestItemsCount(FAMILY_REMAINS) == 3)
				htmltext = "30421-18.htm";
		}
		else if (event.equalsIgnoreCase("30421-10.htm"))
		{
			if (st.hasQuestItems(SEED_OF_HORROR))
				htmltext = "30421-09.htm";
			else if (st.getQuestItemsCount(KNEE_BONE) == 2)
				htmltext = "30421-19.htm";
		}
		else if (event.equalsIgnoreCase("30421-13.htm"))
		{
			if (st.hasQuestItems(SEED_OF_LUNACY))
				htmltext = "30421-12.htm";
		}
		else if (event.equalsIgnoreCase("30415-03.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(LUCKY_KEY, 1);
		}
		else if (event.equalsIgnoreCase("30418-02.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(CANDLE, 1);
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
				htmltext = "30421-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case VARIKA:
						if (st.hasQuestItems(SEED_OF_ANGER, SEED_OF_HORROR, SEED_OF_LUNACY))
						{
							htmltext = "30421-16.htm";
							st.takeItems(SEED_OF_ANGER, 1);
							st.takeItems(SEED_OF_DESPAIR, 1);
							st.takeItems(SEED_OF_HORROR, 1);
							st.takeItems(SEED_OF_LUNACY, 1);
							st.giveItems(JEWEL_OF_DARKNESS, 1);
							st.rewardExpAndSp(3200, 1650);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						else
							htmltext = "30421-17.htm";
						break;
					
					case CHARKEREN:
						if (st.hasQuestItems(SEED_OF_ANGER))
							htmltext = "30415-06.htm";
						else if (!st.hasQuestItems(LUCKY_KEY))
							htmltext = "30415-01.htm";
						else if (st.getQuestItemsCount(FAMILY_REMAINS) == 3)
						{
							htmltext = "30415-05.htm";
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(FAMILY_REMAINS, -1);
							st.takeItems(LUCKY_KEY, 1);
							st.giveItems(SEED_OF_ANGER, 1);
						}
						else
							htmltext = "30415-04.htm";
						break;
					
					case ANNIKA:
						if (st.hasQuestItems(SEED_OF_HORROR))
							htmltext = "30418-04.htm";
						else if (!st.hasQuestItems(CANDLE))
							htmltext = "30418-01.htm";
						else if (st.getQuestItemsCount(KNEE_BONE) == 2)
						{
							htmltext = "30418-04.htm";
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(CANDLE, 1);
							st.takeItems(KNEE_BONE, -1);
							st.giveItems(SEED_OF_HORROR, 1);
						}
						else
							htmltext = "30418-03.htm";
						break;
					
					case ARKENIA:
						if (st.hasQuestItems(SEED_OF_LUNACY))
							htmltext = "30419-03.htm";
						else if (!st.hasQuestItems(HUB_SCENT))
						{
							htmltext = "30419-01.htm";
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(HUB_SCENT, 1);
						}
						else if (st.getQuestItemsCount(HEART_OF_LUNACY) == 3)
						{
							htmltext = "30419-03.htm";
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(HEART_OF_LUNACY, -1);
							st.takeItems(HUB_SCENT, 1);
							st.giveItems(SEED_OF_LUNACY, 1);
						}
						else
							htmltext = "30419-02.htm";
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
			case 20015:
				if (st.hasQuestItems(LUCKY_KEY))
					st.dropItems(FAMILY_REMAINS, 1, 3, 500000);
				break;
			
			case 20022:
			case 20517:
			case 20518:
				if (st.hasQuestItems(CANDLE))
					st.dropItems(KNEE_BONE, 1, 2, 500000);
				break;
			
			case 20045:
				if (st.hasQuestItems(HUB_SCENT))
					st.dropItems(HEART_OF_LUNACY, 1, 3, 500000);
				break;
		}
		
		return null;
	}
}
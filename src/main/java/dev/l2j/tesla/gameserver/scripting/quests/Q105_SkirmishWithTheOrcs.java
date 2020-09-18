package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q105_SkirmishWithTheOrcs extends Quest
{
	private static final String qn = "Q105_SkirmishWithTheOrcs";
	
	// Item
	private static final int KENDELL_ORDER_1 = 1836;
	private static final int KENDELL_ORDER_2 = 1837;
	private static final int KENDELL_ORDER_3 = 1838;
	private static final int KENDELL_ORDER_4 = 1839;
	private static final int KENDELL_ORDER_5 = 1840;
	private static final int KENDELL_ORDER_6 = 1841;
	private static final int KENDELL_ORDER_7 = 1842;
	private static final int KENDELL_ORDER_8 = 1843;
	private static final int KABOO_CHIEF_TORC_1 = 1844;
	private static final int KABOO_CHIEF_TORC_2 = 1845;
	
	// Monster
	private static final int KABOO_CHIEF_UOPH = 27059;
	private static final int KABOO_CHIEF_KRACHA = 27060;
	private static final int KABOO_CHIEF_BATOH = 27061;
	private static final int KABOO_CHIEF_TANUKIA = 27062;
	private static final int KABOO_CHIEF_TUREL = 27064;
	private static final int KABOO_CHIEF_ROKO = 27065;
	private static final int KABOO_CHIEF_KAMUT = 27067;
	private static final int KABOO_CHIEF_MURTIKA = 27068;
	
	// Rewards
	private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	private static final int RED_SUNSET_STAFF = 754;
	private static final int RED_SUNSET_SWORD = 981;
	private static final int ECHO_BATTLE = 4412;
	private static final int ECHO_LOVE = 4413;
	private static final int ECHO_SOLITUDE = 4414;
	private static final int ECHO_FEAST = 4415;
	private static final int ECHO_CELEBRATION = 4416;
	
	public Q105_SkirmishWithTheOrcs()
	{
		super(105, "Skirmish with the Orcs");
		
		setItemsIds(KENDELL_ORDER_1, KENDELL_ORDER_2, KENDELL_ORDER_3, KENDELL_ORDER_4, KENDELL_ORDER_5, KENDELL_ORDER_6, KENDELL_ORDER_7, KENDELL_ORDER_8, KABOO_CHIEF_TORC_1, KABOO_CHIEF_TORC_2);
		
		addStartNpc(30218); // Kendell
		addTalkId(30218);
		
		addKillId(KABOO_CHIEF_UOPH, KABOO_CHIEF_KRACHA, KABOO_CHIEF_BATOH, KABOO_CHIEF_TANUKIA, KABOO_CHIEF_TUREL, KABOO_CHIEF_ROKO, KABOO_CHIEF_KAMUT, KABOO_CHIEF_MURTIKA);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30218-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(Rnd.get(1836, 1839), 1); // Kendell's orders 1 to 4.
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (player.getRace() != ClassRace.ELF)
					htmltext = "30218-00.htm";
				else if (player.getLevel() < 10)
					htmltext = "30218-01.htm";
				else
					htmltext = "30218-02.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "30218-05.htm";
				else if (cond == 2)
				{
					htmltext = "30218-06.htm";
					st.set("cond", "3");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(KABOO_CHIEF_TORC_1, 1);
					st.takeItems(KENDELL_ORDER_1, 1);
					st.takeItems(KENDELL_ORDER_2, 1);
					st.takeItems(KENDELL_ORDER_3, 1);
					st.takeItems(KENDELL_ORDER_4, 1);
					st.giveItems(Rnd.get(1840, 1843), 1); // Kendell's orders 5 to 8.
				}
				else if (cond == 3)
					htmltext = "30218-07.htm";
				else if (cond == 4)
				{
					htmltext = "30218-08.htm";
					st.takeItems(KABOO_CHIEF_TORC_2, 1);
					st.takeItems(KENDELL_ORDER_5, 1);
					st.takeItems(KENDELL_ORDER_6, 1);
					st.takeItems(KENDELL_ORDER_7, 1);
					st.takeItems(KENDELL_ORDER_8, 1);
					
					if (player.isMageClass())
						st.giveItems(RED_SUNSET_STAFF, 1);
					else
						st.giveItems(RED_SUNSET_SWORD, 1);
					
					if (player.isNewbie())
					{
						st.showQuestionMark(26);
						if (player.isMageClass())
						{
							st.playTutorialVoice("tutorial_voice_027");
							st.giveItems(SPIRITSHOT_FOR_BEGINNERS, 3000);
						}
						else
						{
							st.playTutorialVoice("tutorial_voice_026");
							st.giveItems(SOULSHOT_FOR_BEGINNERS, 7000);
						}
					}
					
					st.giveItems(ECHO_BATTLE, 10);
					st.giveItems(ECHO_LOVE, 10);
					st.giveItems(ECHO_SOLITUDE, 10);
					st.giveItems(ECHO_FEAST, 10);
					st.giveItems(ECHO_CELEBRATION, 10);
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
		
		switch (npc.getNpcId())
		{
			case KABOO_CHIEF_UOPH:
			case KABOO_CHIEF_KRACHA:
			case KABOO_CHIEF_BATOH:
			case KABOO_CHIEF_TANUKIA:
				if (st.getInt("cond") == 1 && st.hasQuestItems(npc.getNpcId() - 25223)) // npcId - 25223 = itemId to verify.
				{
					st.set("cond", "2");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(KABOO_CHIEF_TORC_1, 1);
				}
				break;
			
			case KABOO_CHIEF_TUREL:
			case KABOO_CHIEF_ROKO:
				if (st.getInt("cond") == 3 && st.hasQuestItems(npc.getNpcId() - 25224)) // npcId - 25224 = itemId to verify.
				{
					st.set("cond", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(KABOO_CHIEF_TORC_2, 1);
				}
				break;
			
			case KABOO_CHIEF_KAMUT:
			case KABOO_CHIEF_MURTIKA:
				if (st.getInt("cond") == 3 && st.hasQuestItems(npc.getNpcId() - 25225)) // npcId - 25225 = itemId to verify.
				{
					st.set("cond", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(KABOO_CHIEF_TORC_2, 1);
				}
				break;
		}
		
		return null;
	}
}
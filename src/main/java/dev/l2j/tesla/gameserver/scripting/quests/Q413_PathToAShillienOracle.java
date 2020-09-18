package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q413_PathToAShillienOracle extends Quest
{
	private static final String qn = "Q413_PathToAShillienOracle";
	
	// Items
	private static final int SIDRA_LETTER = 1262;
	private static final int BLANK_SHEET = 1263;
	private static final int BLOODY_RUNE = 1264;
	private static final int GARMIEL_BOOK = 1265;
	private static final int PRAYER_OF_ADONIUS = 1266;
	private static final int PENITENT_MARK = 1267;
	private static final int ASHEN_BONES = 1268;
	private static final int ANDARIEL_BOOK = 1269;
	private static final int ORB_OF_ABYSS = 1270;
	
	// NPCs
	private static final int SIDRA = 30330;
	private static final int ADONIUS = 30375;
	private static final int TALBOT = 30377;
	
	public Q413_PathToAShillienOracle()
	{
		super(413, "Path to a Shillien Oracle");
		
		setItemsIds(SIDRA_LETTER, BLANK_SHEET, BLOODY_RUNE, GARMIEL_BOOK, PRAYER_OF_ADONIUS, PENITENT_MARK, ASHEN_BONES, ANDARIEL_BOOK);
		
		addStartNpc(SIDRA);
		addTalkId(SIDRA, ADONIUS, TALBOT);
		
		addKillId(20776, 20457, 20458, 20514, 20515);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30330-05.htm"))
		{
			if (player.getClassId() != ClassId.DARK_MYSTIC)
				htmltext = (player.getClassId() == ClassId.SHILLIEN_ORACLE) ? "30330-02a.htm" : "30330-03.htm";
			else if (player.getLevel() < 19)
				htmltext = "30330-02.htm";
			else if (st.hasQuestItems(ORB_OF_ABYSS))
				htmltext = "30330-04.htm";
		}
		else if (event.equalsIgnoreCase("30330-06.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(SIDRA_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("30377-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(SIDRA_LETTER, 1);
			st.giveItems(BLANK_SHEET, 5);
		}
		else if (event.equalsIgnoreCase("30375-04.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(PRAYER_OF_ADONIUS, 1);
			st.giveItems(PENITENT_MARK, 1);
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
				htmltext = "30330-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case SIDRA:
						if (cond == 1)
							htmltext = "30330-07.htm";
						else if (cond > 1 && cond < 4)
							htmltext = "30330-08.htm";
						else if (cond > 3 && cond < 7)
							htmltext = "30330-09.htm";
						else if (cond == 7)
						{
							htmltext = "30330-10.htm";
							st.takeItems(ANDARIEL_BOOK, 1);
							st.takeItems(GARMIEL_BOOK, 1);
							st.giveItems(ORB_OF_ABYSS, 1);
							st.rewardExpAndSp(3200, 3120);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case TALBOT:
						if (cond == 1)
							htmltext = "30377-01.htm";
						else if (cond == 2)
							htmltext = (st.hasQuestItems(BLOODY_RUNE)) ? "30377-04.htm" : "30377-03.htm";
						else if (cond == 3)
						{
							htmltext = "30377-05.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BLOODY_RUNE, -1);
							st.giveItems(GARMIEL_BOOK, 1);
							st.giveItems(PRAYER_OF_ADONIUS, 1);
						}
						else if (cond > 3 && cond < 7)
							htmltext = "30377-06.htm";
						else if (cond == 7)
							htmltext = "30377-07.htm";
						break;
					
					case ADONIUS:
						if (cond == 4)
							htmltext = "30375-01.htm";
						else if (cond == 5)
							htmltext = (st.hasQuestItems(ASHEN_BONES)) ? "30375-05.htm" : "30375-06.htm";
						else if (cond == 6)
						{
							htmltext = "30375-07.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ASHEN_BONES, -1);
							st.takeItems(PENITENT_MARK, -1);
							st.giveItems(ANDARIEL_BOOK, 1);
						}
						else if (cond == 7)
							htmltext = "30375-08.htm";
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
		
		if (npc.getNpcId() == 20776)
		{
			if (st.getInt("cond") == 2)
			{
				st.takeItems(BLANK_SHEET, 1);
				if (st.dropItemsAlways(BLOODY_RUNE, 1, 5))
					st.set("cond", "3");
			}
		}
		else if (st.getInt("cond") == 5 && st.dropItemsAlways(ASHEN_BONES, 1, 10))
			st.set("cond", "6");
		
		return null;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q418_PathToAnArtisan extends Quest
{
	private static final String qn = "Q418_PathToAnArtisan";
	
	// Items
	private static final int SILVERA_RING = 1632;
	private static final int FIRST_PASS_CERTIFICATE = 1633;
	private static final int SECOND_PASS_CERTIFICATE = 1634;
	private static final int FINAL_PASS_CERTIFICATE = 1635;
	private static final int BOOGLE_RATMAN_TOOTH = 1636;
	private static final int BOOGLE_RATMAN_LEADER_TOOTH = 1637;
	private static final int KLUTO_LETTER = 1638;
	private static final int FOOTPRINT_OF_THIEF = 1639;
	private static final int STOLEN_SECRET_BOX = 1640;
	private static final int SECRET_BOX = 1641;
	
	// NPCs
	private static final int SILVERA = 30527;
	private static final int KLUTO = 30317;
	private static final int PINTER = 30298;
	private static final int OBI = 32052;
	private static final int HITCHI = 31963;
	private static final int LOCKIRIN = 30531;
	private static final int RYDEL = 31956;
	
	public Q418_PathToAnArtisan()
	{
		super(418, "Path to an Artisan");
		
		setItemsIds(SILVERA_RING, FIRST_PASS_CERTIFICATE, SECOND_PASS_CERTIFICATE, BOOGLE_RATMAN_TOOTH, BOOGLE_RATMAN_LEADER_TOOTH, KLUTO_LETTER, FOOTPRINT_OF_THIEF, STOLEN_SECRET_BOX, SECRET_BOX);
		
		addStartNpc(SILVERA);
		addTalkId(SILVERA, KLUTO, PINTER, OBI, HITCHI, LOCKIRIN, RYDEL);
		
		addKillId(20389, 20390, 20017);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30527-05.htm"))
		{
			if (player.getClassId() != ClassId.DWARVEN_FIGHTER)
				htmltext = (player.getClassId() == ClassId.ARTISAN) ? "30527-02a.htm" : "30527-02.htm";
			else if (player.getLevel() < 19)
				htmltext = "30527-03.htm";
			else if (st.hasQuestItems(FINAL_PASS_CERTIFICATE))
				htmltext = "30527-04.htm";
		}
		else if (event.equalsIgnoreCase("30527-06.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(SILVERA_RING, 1);
		}
		else if (event.equalsIgnoreCase("30527-08a.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BOOGLE_RATMAN_LEADER_TOOTH, -1);
			st.takeItems(BOOGLE_RATMAN_TOOTH, -1);
			st.takeItems(SILVERA_RING, 1);
			st.giveItems(FIRST_PASS_CERTIFICATE, 1);
		}
		else if (event.equalsIgnoreCase("30527-08b.htm"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BOOGLE_RATMAN_LEADER_TOOTH, -1);
			st.takeItems(BOOGLE_RATMAN_TOOTH, -1);
			st.takeItems(SILVERA_RING, 1);
		}
		else if (event.equalsIgnoreCase("30317-04.htm") || event.equalsIgnoreCase("30317-07.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(KLUTO_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("30317-10.htm"))
		{
			st.takeItems(FIRST_PASS_CERTIFICATE, 1);
			st.takeItems(SECOND_PASS_CERTIFICATE, 1);
			st.takeItems(SECRET_BOX, 1);
			st.giveItems(FINAL_PASS_CERTIFICATE, 1);
			st.rewardExpAndSp(3200, 6980);
			player.broadcastPacket(new SocialAction(player, 3));
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30317-12.htm") || event.equalsIgnoreCase("30531-05.htm") || event.equalsIgnoreCase("32052-11.htm") || event.equalsIgnoreCase("31963-10.htm") || event.equalsIgnoreCase("31956-04.htm"))
		{
			st.takeItems(FIRST_PASS_CERTIFICATE, 1);
			st.takeItems(SECOND_PASS_CERTIFICATE, 1);
			st.takeItems(SECRET_BOX, 1);
			st.giveItems(FINAL_PASS_CERTIFICATE, 1);
			st.rewardExpAndSp(3200, 3490);
			player.broadcastPacket(new SocialAction(player, 3));
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30298-03.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(KLUTO_LETTER, -1);
			st.giveItems(FOOTPRINT_OF_THIEF, 1);
		}
		else if (event.equalsIgnoreCase("30298-06.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(FOOTPRINT_OF_THIEF, -1);
			st.takeItems(STOLEN_SECRET_BOX, -1);
			st.giveItems(SECOND_PASS_CERTIFICATE, 1);
			st.giveItems(SECRET_BOX, 1);
		}
		else if (event.equalsIgnoreCase("32052-06.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31963-04.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31963-05.htm"))
		{
			st.set("cond", "11");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31963-07.htm"))
		{
			st.set("cond", "12");
			st.playSound(QuestState.SOUND_MIDDLE);
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
				htmltext = "30527-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case SILVERA:
						if (cond == 1)
							htmltext = "30527-07.htm";
						else if (cond == 2)
							htmltext = "30527-08.htm";
						else if (cond == 3)
							htmltext = "30527-09.htm";
						else if (cond == 8)
							htmltext = "30527-09a.htm";
						break;
					
					case KLUTO:
						if (cond == 3)
							htmltext = "30317-01.htm";
						else if (cond == 4)
							htmltext = "30317-08.htm";
						else if (cond == 7)
							htmltext = "30317-09.htm";
						break;
					
					case PINTER:
						if (cond == 4)
							htmltext = "30298-01.htm";
						else if (cond == 5)
							htmltext = "30298-04.htm";
						else if (cond == 6)
							htmltext = "30298-05.htm";
						else if (cond == 7)
							htmltext = "30298-07.htm";
						break;
					
					case OBI:
						if (cond == 8)
							htmltext = "32052-01.htm";
						else if (cond == 9)
							htmltext = "32052-06a.htm";
						else if (cond == 11)
							htmltext = "32052-07.htm";
						break;
					
					case HITCHI:
						if (cond == 9)
							htmltext = "31963-01.htm";
						else if (cond == 10)
							htmltext = "31963-04.htm";
						else if (cond == 11)
							htmltext = "31963-06a.htm";
						else if (cond == 12)
							htmltext = "31963-08.htm";
						break;
					
					case LOCKIRIN:
						if (cond == 10)
							htmltext = "30531-01.htm";
						break;
					
					case RYDEL:
						if (cond == 12)
							htmltext = "31956-01.htm";
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
			case 20389:
				if (st.getInt("cond") == 1 && st.dropItems(BOOGLE_RATMAN_TOOTH, 1, 10, 700000) && st.getQuestItemsCount(BOOGLE_RATMAN_LEADER_TOOTH) == 2)
					st.set("cond", "2");
				break;
			
			case 20390:
				if (st.getInt("cond") == 1 && st.dropItems(BOOGLE_RATMAN_LEADER_TOOTH, 1, 2, 500000) && st.getQuestItemsCount(BOOGLE_RATMAN_TOOTH) == 10)
					st.set("cond", "2");
				break;
			
			case 20017:
				if (st.getInt("cond") == 5 && st.dropItems(STOLEN_SECRET_BOX, 1, 1, 200000))
					st.set("cond", "6");
				break;
		}
		
		return null;
	}
}
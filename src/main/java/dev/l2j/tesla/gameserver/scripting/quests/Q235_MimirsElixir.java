package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillUse;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q235_MimirsElixir extends Quest
{
	private static final String qn = "Q235_MimirsElixir";
	
	// Items
	private static final int STAR_OF_DESTINY = 5011;
	private static final int PURE_SILVER = 6320;
	private static final int TRUE_GOLD = 6321;
	private static final int SAGE_STONE = 6322;
	private static final int BLOOD_FIRE = 6318;
	private static final int MIMIR_ELIXIR = 6319;
	private static final int MAGISTER_MIXING_STONE = 5905;
	
	// Reward
	private static final int SCROLL_ENCHANT_WEAPON_A = 729;
	
	// NPCs
	private static final int JOAN = 30718;
	private static final int LADD = 30721;
	private static final int MIXING_URN = 31149;
	
	public Q235_MimirsElixir()
	{
		super(235, "Mimir's Elixir");
		
		setItemsIds(PURE_SILVER, TRUE_GOLD, SAGE_STONE, BLOOD_FIRE, MAGISTER_MIXING_STONE, MIMIR_ELIXIR);
		
		addStartNpc(LADD);
		addTalkId(LADD, JOAN, MIXING_URN);
		
		addKillId(20965, 21090);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30721-06.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30721-12.htm") && st.hasQuestItems(TRUE_GOLD))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(MAGISTER_MIXING_STONE, 1);
		}
		else if (event.equalsIgnoreCase("30721-16.htm") && st.hasQuestItems(MIMIR_ELIXIR))
		{
			player.broadcastPacket(new MagicSkillUse(player, player, 4339, 1, 1, 1));
			
			st.takeItems(MAGISTER_MIXING_STONE, -1);
			st.takeItems(MIMIR_ELIXIR, -1);
			st.takeItems(STAR_OF_DESTINY, -1);
			st.giveItems(SCROLL_ENCHANT_WEAPON_A, 1);
			player.broadcastPacket(new SocialAction(player, 3));
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		else if (event.equalsIgnoreCase("30718-03.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31149-02.htm"))
		{
			if (!st.hasQuestItems(MAGISTER_MIXING_STONE))
				htmltext = "31149-havent.htm";
		}
		else if (event.equalsIgnoreCase("31149-03.htm"))
		{
			if (!st.hasQuestItems(MAGISTER_MIXING_STONE, PURE_SILVER))
				htmltext = "31149-havent.htm";
		}
		else if (event.equalsIgnoreCase("31149-05.htm"))
		{
			if (!st.hasQuestItems(MAGISTER_MIXING_STONE, PURE_SILVER, TRUE_GOLD))
				htmltext = "31149-havent.htm";
		}
		else if (event.equalsIgnoreCase("31149-07.htm"))
		{
			if (!st.hasQuestItems(MAGISTER_MIXING_STONE, PURE_SILVER, TRUE_GOLD, BLOOD_FIRE))
				htmltext = "31149-havent.htm";
		}
		else if (event.equalsIgnoreCase("31149-success.htm"))
		{
			if (st.hasQuestItems(MAGISTER_MIXING_STONE, PURE_SILVER, TRUE_GOLD, BLOOD_FIRE))
			{
				st.set("cond", "8");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(PURE_SILVER, -1);
				st.takeItems(TRUE_GOLD, -1);
				st.takeItems(BLOOD_FIRE, -1);
				st.giveItems(MIMIR_ELIXIR, 1);
			}
			else
				htmltext = "31149-havent.htm";
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
				if (player.getLevel() < 75)
					htmltext = "30721-01b.htm";
				else if (!st.hasQuestItems(STAR_OF_DESTINY))
					htmltext = "30721-01a.htm";
				else
					htmltext = "30721-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case LADD:
						if (cond == 1)
						{
							if (st.hasQuestItems(PURE_SILVER))
							{
								htmltext = "30721-08.htm";
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
								htmltext = "30721-07.htm";
						}
						else if (cond < 5)
							htmltext = "30721-10.htm";
						else if (cond == 5 && st.hasQuestItems(TRUE_GOLD))
							htmltext = "30721-11.htm";
						else if (cond == 6 || cond == 7)
							htmltext = "30721-13.htm";
						else if (cond == 8 && st.hasQuestItems(MIMIR_ELIXIR))
							htmltext = "30721-14.htm";
						break;
					
					case JOAN:
						if (cond == 2)
							htmltext = "30718-01.htm";
						else if (cond == 3)
							htmltext = "30718-04.htm";
						else if (cond == 4 && st.hasQuestItems(SAGE_STONE))
						{
							htmltext = "30718-05.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SAGE_STONE, -1);
							st.giveItems(TRUE_GOLD, 1);
						}
						else if (cond > 4)
							htmltext = "30718-06.htm";
						break;
					
					// The urn gives the same first htm. Bypasses' events will do all the job.
					case MIXING_URN:
						htmltext = "31149-01.htm";
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
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		switch (npc.getNpcId())
		{
			case 20965:
				if (st.getInt("cond") == 3 && st.dropItems(SAGE_STONE, 1, 1, 200000))
					st.set("cond", "4");
				break;
			
			case 21090:
				if (st.getInt("cond") == 6 && st.dropItems(BLOOD_FIRE, 1, 1, 200000))
					st.set("cond", "7");
				break;
		}
		
		return null;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q171_ActsOfEvil extends Quest
{
	private static final String qn = "Q171_ActsOfEvil";
	
	// Items
	private static final int BLADE_MOLD = 4239;
	private static final int TYRA_BILL = 4240;
	private static final int RANGER_REPORT_1 = 4241;
	private static final int RANGER_REPORT_2 = 4242;
	private static final int RANGER_REPORT_3 = 4243;
	private static final int RANGER_REPORT_4 = 4244;
	private static final int WEAPON_TRADE_CONTRACT = 4245;
	private static final int ATTACK_DIRECTIVES = 4246;
	private static final int CERTIFICATE = 4247;
	private static final int CARGO_BOX = 4248;
	private static final int OL_MAHUM_HEAD = 4249;
	
	// NPCs
	private static final int ALVAH = 30381;
	private static final int ARODIN = 30207;
	private static final int TYRA = 30420;
	private static final int ROLENTO = 30437;
	private static final int NETI = 30425;
	private static final int BURAI = 30617;
	
	// Turek Orcs drop chances
	private static final Map<Integer, Integer> CHANCES = new HashMap<>();
	{
		CHANCES.put(20496, 530000);
		CHANCES.put(20497, 550000);
		CHANCES.put(20498, 510000);
		CHANCES.put(20499, 500000);
	}
	
	public Q171_ActsOfEvil()
	{
		super(171, "Acts of Evil");
		
		setItemsIds(BLADE_MOLD, TYRA_BILL, RANGER_REPORT_1, RANGER_REPORT_2, RANGER_REPORT_3, RANGER_REPORT_4, WEAPON_TRADE_CONTRACT, ATTACK_DIRECTIVES, CERTIFICATE, CARGO_BOX, OL_MAHUM_HEAD);
		
		addStartNpc(ALVAH);
		addTalkId(ALVAH, ARODIN, TYRA, ROLENTO, NETI, BURAI);
		
		addKillId(20496, 20497, 20498, 20499, 20062, 20064, 20066, 20438);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30381-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30207-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30381-04.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30381-07.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(WEAPON_TRADE_CONTRACT, 1);
		}
		else if (event.equalsIgnoreCase("30437-03.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(CARGO_BOX, 1);
			st.giveItems(CERTIFICATE, 1);
		}
		else if (event.equalsIgnoreCase("30617-04.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(ATTACK_DIRECTIVES, 1);
			st.takeItems(CARGO_BOX, 1);
			st.takeItems(CERTIFICATE, 1);
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
				htmltext = (player.getLevel() < 27) ? "30381-01a.htm" : "30381-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ALVAH:
						if (cond < 4)
							htmltext = "30381-02a.htm";
						else if (cond == 4)
							htmltext = "30381-03.htm";
						else if (cond == 5)
						{
							if (st.hasQuestItems(RANGER_REPORT_1, RANGER_REPORT_2, RANGER_REPORT_3, RANGER_REPORT_4))
							{
								htmltext = "30381-05.htm";
								st.set("cond", "6");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(RANGER_REPORT_1, 1);
								st.takeItems(RANGER_REPORT_2, 1);
								st.takeItems(RANGER_REPORT_3, 1);
								st.takeItems(RANGER_REPORT_4, 1);
							}
							else
								htmltext = "30381-04a.htm";
						}
						else if (cond == 6)
						{
							if (st.hasQuestItems(WEAPON_TRADE_CONTRACT, ATTACK_DIRECTIVES))
								htmltext = "30381-06.htm";
							else
								htmltext = "30381-05a.htm";
						}
						else if (cond > 6 && cond < 11)
							htmltext = "30381-07a.htm";
						else if (cond == 11)
						{
							htmltext = "30381-08.htm";
							st.rewardItems(57, 90000);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case ARODIN:
						if (cond == 1)
							htmltext = "30207-01.htm";
						else if (cond == 2)
							htmltext = "30207-01a.htm";
						else if (cond == 3)
						{
							if (st.hasQuestItems(TYRA_BILL))
							{
								htmltext = "30207-03.htm";
								st.set("cond", "4");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(TYRA_BILL, 1);
							}
							else
								htmltext = "30207-01a.htm";
						}
						else if (cond > 3)
							htmltext = "30207-03a.htm";
						break;
					
					case TYRA:
						if (cond == 2)
						{
							if (st.getQuestItemsCount(BLADE_MOLD) >= 20)
							{
								htmltext = "30420-01.htm";
								st.set("cond", "3");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(BLADE_MOLD, -1);
								st.giveItems(TYRA_BILL, 1);
							}
							else
								htmltext = "30420-01b.htm";
						}
						else if (cond == 3)
							htmltext = "30420-01a.htm";
						else if (cond > 3)
							htmltext = "30420-02.htm";
						break;
					
					case NETI:
						if (cond == 7)
						{
							htmltext = "30425-01.htm";
							st.set("cond", "8");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond > 7)
							htmltext = "30425-02.htm";
						break;
					
					case ROLENTO:
						if (cond == 8)
							htmltext = "30437-01.htm";
						else if (cond > 8)
							htmltext = "30437-03a.htm";
						break;
					
					case BURAI:
						if (cond == 9 && st.hasQuestItems(CERTIFICATE, CARGO_BOX, ATTACK_DIRECTIVES))
							htmltext = "30617-01.htm";
						else if (cond == 10)
						{
							if (st.getQuestItemsCount(OL_MAHUM_HEAD) >= 30)
							{
								htmltext = "30617-05.htm";
								st.set("cond", "11");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(OL_MAHUM_HEAD, -1);
								st.rewardItems(57, 8000);
							}
							else
								htmltext = "30617-04a.htm";
						}
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
		
		final int npcId = npc.getNpcId();
		
		switch (npcId)
		{
			case 20496:
			case 20497:
			case 20498:
			case 20499:
				if (st.getInt("cond") == 2 && !st.dropItems(BLADE_MOLD, 1, 20, CHANCES.get(npcId)))
				{
					final int count = st.getQuestItemsCount(BLADE_MOLD);
					if (count == 5 || (count >= 10 && Rnd.get(100) < 25))
						addSpawn(27190, player, false, 0, true);
				}
				break;
			
			case 20062:
			case 20064:
				if (st.getInt("cond") == 5)
				{
					if (!st.hasQuestItems(RANGER_REPORT_1))
					{
						st.giveItems(RANGER_REPORT_1, 1);
						st.playSound(QuestState.SOUND_ITEMGET);
					}
					else if (Rnd.get(100) < 20)
					{
						if (!st.hasQuestItems(RANGER_REPORT_2))
						{
							st.giveItems(RANGER_REPORT_2, 1);
							st.playSound(QuestState.SOUND_ITEMGET);
						}
						else if (!st.hasQuestItems(RANGER_REPORT_3))
						{
							st.giveItems(RANGER_REPORT_3, 1);
							st.playSound(QuestState.SOUND_ITEMGET);
						}
						else if (!st.hasQuestItems(RANGER_REPORT_4))
						{
							st.giveItems(RANGER_REPORT_4, 1);
							st.playSound(QuestState.SOUND_ITEMGET);
						}
					}
				}
				break;
			
			case 20438:
				if (st.getInt("cond") == 6 && Rnd.get(100) < 10 && !st.hasQuestItems(WEAPON_TRADE_CONTRACT, ATTACK_DIRECTIVES))
				{
					st.playSound(QuestState.SOUND_ITEMGET);
					st.giveItems(WEAPON_TRADE_CONTRACT, 1);
					st.giveItems(ATTACK_DIRECTIVES, 1);
				}
				break;
			
			case 20066:
				if (st.getInt("cond") == 10)
					st.dropItems(OL_MAHUM_HEAD, 1, 30, 500000);
				break;
		}
		
		return null;
	}
}
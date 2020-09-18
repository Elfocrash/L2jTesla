package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.ExShowScreenMessage;

public class Q114_ResurrectionOfAnOldManager extends Quest
{
	private static final String qn = "Q114_ResurrectionOfAnOldManager";
	
	// NPCs
	private static final int NEWYEAR = 31961;
	private static final int YUMI = 32041;
	private static final int STONE = 32046;
	private static final int WENDY = 32047;
	private static final int BOX = 32050;
	
	// Items
	private static final int LETTER = 8288;
	private static final int DETECTOR = 8090;
	private static final int DETECTOR_2 = 8091;
	private static final int STARSTONE = 8287;
	private static final int STARSTONE_2 = 8289;
	
	// Mobs
	private static final int GOLEM = 27318;
	
	public Q114_ResurrectionOfAnOldManager()
	{
		super(114, "Resurrection of an Old Manager");
		
		setItemsIds(LETTER, DETECTOR, DETECTOR_2, STARSTONE, STARSTONE_2);
		
		addStartNpc(YUMI);
		addTalkId(YUMI, WENDY, BOX, STONE, NEWYEAR);
		
		addKillId(GOLEM);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32041-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.set("talk", "0");
			st.set("golemSpawned", "0");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32041-06.htm"))
			st.set("talk", "1");
		else if (event.equalsIgnoreCase("32041-07.htm"))
		{
			st.set("cond", "2");
			st.set("talk", "0");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32041-10.htm"))
		{
			final int choice = st.getInt("choice");
			
			if (choice == 1)
				htmltext = "32041-10.htm";
			else if (choice == 2)
				htmltext = "32041-10a.htm";
			else if (choice == 3)
				htmltext = "32041-10b.htm";
		}
		else if (event.equalsIgnoreCase("32041-11.htm"))
			st.set("talk", "1");
		else if (event.equalsIgnoreCase("32041-18.htm"))
			st.set("talk", "2");
		else if (event.equalsIgnoreCase("32041-20.htm"))
		{
			st.set("cond", "6");
			st.set("talk", "0");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32041-25.htm"))
		{
			st.set("cond", "17");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(DETECTOR, 1);
		}
		else if (event.equalsIgnoreCase("32041-28.htm"))
		{
			st.set("talk", "1");
			st.takeItems(DETECTOR_2, 1);
		}
		else if (event.equalsIgnoreCase("32041-31.htm"))
		{
			if (st.getInt("choice") > 1)
				htmltext = "32041-37.htm";
		}
		else if (event.equalsIgnoreCase("32041-32.htm"))
		{
			st.set("cond", "21");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(LETTER, 1);
		}
		else if (event.equalsIgnoreCase("32041-36.htm"))
		{
			st.set("cond", "20");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32046-02.htm"))
		{
			st.set("cond", "19");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32046-06.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		else if (event.equalsIgnoreCase("32047-01.htm"))
		{
			final int talk = st.getInt("talk");
			final int talk1 = st.getInt("talk1");
			
			if (talk == 1 && talk1 == 1)
				htmltext = "32047-04.htm";
			else if (talk == 2 && talk1 == 2 && st.getInt("talk2") == 2)
				htmltext = "32047-08.htm";
		}
		else if (event.equalsIgnoreCase("32047-02.htm"))
		{
			if (st.getInt("talk") == 0)
				st.set("talk", "1");
		}
		else if (event.equalsIgnoreCase("32047-03.htm"))
		{
			if (st.getInt("talk1") == 0)
				st.set("talk1", "1");
		}
		else if (event.equalsIgnoreCase("32047-05.htm"))
		{
			st.set("cond", "3");
			st.set("talk", "0");
			st.set("choice", "1");
			st.unset("talk1");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32047-06.htm"))
		{
			st.set("cond", "4");
			st.set("talk", "0");
			st.set("choice", "2");
			st.unset("talk1");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32047-07.htm"))
		{
			st.set("cond", "5");
			st.set("talk", "0");
			st.set("choice", "3");
			st.unset("talk1");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32047-13.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32047-13a.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32047-15.htm"))
		{
			if (st.getInt("talk") == 0)
				st.set("talk", "1");
		}
		else if (event.equalsIgnoreCase("32047-15a.htm"))
		{
			if (st.getInt("golemSpawned") == 0)
			{
				final Npc golem = addSpawn(GOLEM, 96977, -110625, -3322, 0, true, 0, true);
				golem.broadcastNpcSay("You, " + player.getName() + ", you attacked Wendy. Prepare to die!");
				((Attackable) golem).addDamageHate(player, 0, 999);
				golem.getAI().setIntention(IntentionType.ATTACK, player);
				
				st.set("golemSpawned", "1");
				startQuestTimer("golemDespawn", 900000, golem, player, false);
			}
			else
				htmltext = "32047-19a.htm";
		}
		else if (event.equalsIgnoreCase("32047-17a.htm"))
		{
			st.set("cond", "12");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32047-20.htm"))
			st.set("talk", "2");
		else if (event.equalsIgnoreCase("32047-23.htm"))
		{
			st.set("cond", "13");
			st.set("talk", "0");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32047-25.htm"))
		{
			st.set("cond", "15");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(STARSTONE, 1);
		}
		else if (event.equalsIgnoreCase("32047-30.htm"))
			st.set("talk", "2");
		else if (event.equalsIgnoreCase("32047-33.htm"))
		{
			final int cond = st.getInt("cond");
			
			if (cond == 7)
			{
				st.set("cond", "8");
				st.set("talk", "0");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else if (cond == 8)
			{
				st.set("cond", "9");
				htmltext = "32047-34.htm";
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equalsIgnoreCase("32047-34.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32047-38.htm"))
		{
			if (st.getQuestItemsCount(57) >= 3000)
			{
				st.set("cond", "26");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(57, 3000);
				st.giveItems(STARSTONE_2, 1);
			}
			else
				htmltext = "32047-39.htm";
		}
		else if (event.equalsIgnoreCase("32050-02.htm"))
		{
			st.set("talk", "1");
			st.playSound("ItemSound.armor_wood_3");
		}
		else if (event.equalsIgnoreCase("32050-04.htm"))
		{
			st.set("cond", "14");
			st.set("talk", "0");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(STARSTONE, 1);
		}
		else if (event.equalsIgnoreCase("31961-02.htm"))
		{
			st.set("cond", "22");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LETTER, 1);
			st.giveItems(STARSTONE_2, 1);
		}
		else if (event.equalsIgnoreCase("golemDespawn"))
		{
			st.unset("golemSpawned");
			return null;
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
				QuestState pavelReq = player.getQuestState("Q121_PavelTheGiant");
				htmltext = (pavelReq == null || !pavelReq.isCompleted() || player.getLevel() < 49) ? "32041-00.htm" : "32041-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				int talk = st.getInt("talk");
				
				switch (npc.getNpcId())
				{
					case YUMI:
						if (cond == 1)
						{
							if (talk == 0)
								htmltext = "32041-02.htm";
							else
								htmltext = "32041-06.htm";
						}
						else if (cond == 2)
							htmltext = "32041-08.htm";
						else if (cond > 2 && cond < 6)
						{
							if (talk == 0)
								htmltext = "32041-09.htm";
							else if (talk == 1)
								htmltext = "32041-11.htm";
							else
								htmltext = "32041-18.htm";
						}
						else if (cond == 6)
							htmltext = "32041-21.htm";
						else if (cond == 9 || cond == 12 || cond == 16)
							htmltext = "32041-22.htm";
						else if (cond == 17)
							htmltext = "32041-26.htm";
						else if (cond == 19)
						{
							if (talk == 0)
								htmltext = "32041-27.htm";
							else
								htmltext = "32041-28.htm";
						}
						else if (cond == 20)
							htmltext = "32041-36.htm";
						else if (cond == 21)
							htmltext = "32041-33.htm";
						else if (cond == 22 || cond == 26)
						{
							htmltext = "32041-34.htm";
							st.set("cond", "27");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 27)
							htmltext = "32041-35.htm";
						break;
					
					case WENDY:
						if (cond == 2)
						{
							if (talk == 0 && st.getInt("talk1") == 0)
								htmltext = "32047-01.htm";
							else if (talk == 1 && st.getInt("talk1") == 1)
								htmltext = "32047-04.htm";
						}
						else if (cond == 3)
							htmltext = "32047-09.htm";
						else if (cond == 4 || cond == 5)
							htmltext = "32047-09a.htm";
						else if (cond == 6)
						{
							final int choice = st.getInt("choice");
							
							if (choice == 1)
							{
								if (talk == 0)
									htmltext = "32047-10.htm";
								else if (talk == 1)
									htmltext = "32047-20.htm";
							}
							else if (choice == 2)
								htmltext = "32047-10a.htm";
							else if (choice == 3)
							{
								if (talk == 0)
									htmltext = "32047-14.htm";
								else if (talk == 1)
									htmltext = "32047-15.htm";
								else
									htmltext = "32047-20.htm";
							}
						}
						else if (cond == 7)
						{
							if (talk == 0)
								htmltext = "32047-14.htm";
							else if (talk == 1)
								htmltext = "32047-15.htm";
							else
								htmltext = "32047-20.htm";
						}
						else if (cond == 8)
							htmltext = "32047-30.htm";
						else if (cond == 9)
							htmltext = "32047-27.htm";
						else if (cond == 10)
							htmltext = "32047-14a.htm";
						else if (cond == 11)
							htmltext = "32047-16a.htm";
						else if (cond == 12)
							htmltext = "32047-18a.htm";
						else if (cond == 13)
							htmltext = "32047-23.htm";
						else if (cond == 14)
							htmltext = "32047-24.htm";
						else if (cond == 15)
						{
							htmltext = "32047-26.htm";
							st.set("cond", "16");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 16)
							htmltext = "32047-27.htm";
						else if (cond == 20)
							htmltext = "32047-35.htm";
						else if (cond == 26)
							htmltext = "32047-40.htm";
						break;
					
					case BOX:
						if (cond == 13)
						{
							if (talk == 0)
								htmltext = "32050-01.htm";
							else
								htmltext = "32050-03.htm";
						}
						else if (cond == 14)
							htmltext = "32050-05.htm";
						break;
					
					case STONE:
						if (st.getInt("cond") == 17)
						{
							st.set("cond", "18");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(DETECTOR, 1);
							st.giveItems(DETECTOR_2, 1);
							player.sendPacket(new ExShowScreenMessage("The radio signal detector is responding. # A suspicious pile of stones catches your eye.", 4500));
							return null;
						}
						else if (cond == 18)
							htmltext = "32046-01.htm";
						else if (cond == 19)
							htmltext = "32046-02.htm";
						else if (cond == 27)
							htmltext = "32046-03.htm";
						break;
					
					case NEWYEAR:
						if (cond == 21)
							htmltext = "31961-01.htm";
						else if (cond == 22)
							htmltext = "31961-03.htm";
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
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "10");
		if (st == null)
			return null;
		
		npc.broadcastNpcSay("This enemy is far too powerful for me to fight. I must withdraw!");
		
		st.set("cond", "11");
		st.unset("golemSpawned");
		st.playSound(QuestState.SOUND_MIDDLE);
		
		return null;
	}
}
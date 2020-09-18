package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;

public class Q022_TragedyInVonHellmannForest extends Quest
{
	private static final String qn = "Q022_TragedyInVonHellmannForest";
	
	// NPCs
	private static final int WELL = 31527;
	private static final int TIFAREN = 31334;
	private static final int INNOCENTIN = 31328;
	private static final int GHOST_OF_PRIEST = 31528;
	private static final int GHOST_OF_ADVENTURER = 31529;
	
	// Items
	private static final int CROSS_OF_EINHASAD = 7141;
	private static final int LOST_SKULL_OF_ELF = 7142;
	private static final int LETTER_OF_INNOCENTIN = 7143;
	private static final int GREEN_JEWEL_OF_ADVENTURER = 7144;
	private static final int RED_JEWEL_OF_ADVENTURER = 7145;
	private static final int SEALED_REPORT_BOX = 7146;
	private static final int REPORT_BOX = 7147;
	
	// Monsters
	private static final int SOUL_OF_WELL = 27217;
	
	private Npc _ghostOfPriestInstance = null;
	private Npc _soulOfWellInstance = null;
	
	public Q022_TragedyInVonHellmannForest()
	{
		super(22, "Tragedy in von Hellmann Forest");
		
		setItemsIds(LOST_SKULL_OF_ELF, REPORT_BOX, SEALED_REPORT_BOX, LETTER_OF_INNOCENTIN, RED_JEWEL_OF_ADVENTURER, GREEN_JEWEL_OF_ADVENTURER);
		
		addStartNpc(TIFAREN, INNOCENTIN);
		addTalkId(INNOCENTIN, TIFAREN, GHOST_OF_PRIEST, GHOST_OF_ADVENTURER, WELL);
		
		addAttackId(SOUL_OF_WELL);
		addKillId(SOUL_OF_WELL, 21553, 21554, 21555, 21556, 21561);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31334-03.htm"))
		{
			QuestState st2 = player.getQuestState("Q021_HiddenTruth");
			if (st2 != null && st2.isCompleted() && player.getLevel() >= 63)
				htmltext = "31334-02.htm";
		}
		else if (event.equalsIgnoreCase("31334-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31334-07.htm"))
		{
			if (!st.hasQuestItems(CROSS_OF_EINHASAD))
				st.set("cond", "2");
			else
				htmltext = "31334-06.htm";
		}
		else if (event.equalsIgnoreCase("31334-08.htm"))
		{
			if (st.hasQuestItems(CROSS_OF_EINHASAD))
			{
				st.set("cond", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(CROSS_OF_EINHASAD, 1);
			}
			else
			{
				st.set("cond", "2");
				htmltext = "31334-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("31334-13.htm"))
		{
			if (_ghostOfPriestInstance != null)
			{
				st.set("cond", "6");
				htmltext = "31334-14.htm";
			}
			else
			{
				st.set("cond", "7");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(LOST_SKULL_OF_ELF, 1);
				
				_ghostOfPriestInstance = addSpawn(GHOST_OF_PRIEST, 38418, -49894, -1104, 0, false, 120000, true);
				_ghostOfPriestInstance.broadcastNpcSay("Did you call me, " + player.getName() + "?");
				startQuestTimer("ghost_cleanup", 118000, null, player, false);
			}
		}
		else if (event.equalsIgnoreCase("31528-08.htm"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			
			cancelQuestTimer("ghost_cleanup", null, player);
			
			if (_ghostOfPriestInstance != null)
			{
				_ghostOfPriestInstance.deleteMe();
				_ghostOfPriestInstance = null;
			}
		}
		else if (event.equalsIgnoreCase("31328-10.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(LETTER_OF_INNOCENTIN, 1);
		}
		else if (event.equalsIgnoreCase("31529-12.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LETTER_OF_INNOCENTIN, 1);
			st.giveItems(GREEN_JEWEL_OF_ADVENTURER, 1);
		}
		else if (event.equalsIgnoreCase("31527-02.htm"))
		{
			if (_soulOfWellInstance == null)
			{
				_soulOfWellInstance = addSpawn(SOUL_OF_WELL, 34860, -54542, -2048, 0, false, 0, true);
				
				// Attack player.
				((Attackable) _soulOfWellInstance).addDamageHate(player, 0, 99999);
				_soulOfWellInstance.getAI().setIntention(IntentionType.ATTACK, player, true);
			}
		}
		else if (event.equalsIgnoreCase("attack_timer"))
		{
			st.set("cond", "11");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(GREEN_JEWEL_OF_ADVENTURER, 1);
			st.giveItems(RED_JEWEL_OF_ADVENTURER, 1);
		}
		else if (event.equalsIgnoreCase("31328-13.htm"))
		{
			st.set("cond", "15");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(REPORT_BOX, 1);
		}
		else if (event.equalsIgnoreCase("31328-21.htm"))
		{
			st.set("cond", "16");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("ghost_cleanup"))
		{
			_ghostOfPriestInstance.broadcastNpcSay("I'm confused! Maybe it's time to go back.");
			_ghostOfPriestInstance = null;
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
				switch (npc.getNpcId())
				{
					case INNOCENTIN:
						QuestState st2 = player.getQuestState("Q021_HiddenTruth");
						if (st2 != null && st2.isCompleted())
						{
							if (!st.hasQuestItems(CROSS_OF_EINHASAD))
							{
								htmltext = "31328-01.htm";
								st.giveItems(CROSS_OF_EINHASAD, 1);
								st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
								htmltext = "31328-01b.htm";
						}
						break;
					
					case TIFAREN:
						htmltext = "31334-01.htm";
						break;
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case TIFAREN:
						if (cond == 1 || cond == 2 || cond == 3)
							htmltext = "31334-05.htm";
						else if (cond == 4)
							htmltext = "31334-09.htm";
						else if (cond == 5 || cond == 6)
						{
							if (st.hasQuestItems(LOST_SKULL_OF_ELF))
								htmltext = (_ghostOfPriestInstance == null) ? "31334-10.htm" : "31334-11.htm";
							else
							{
								htmltext = "31334-09.htm";
								st.set("cond", "4");
							}
						}
						else if (cond == 7)
							htmltext = (_ghostOfPriestInstance != null) ? "31334-15.htm" : "31334-17.htm";
						else if (cond > 7)
							htmltext = "31334-18.htm";
						break;
					
					case INNOCENTIN:
						if (cond < 3)
						{
							if (!st.hasQuestItems(CROSS_OF_EINHASAD))
							{
								htmltext = "31328-01.htm";
								st.set("cond", "3");
								st.playSound(QuestState.SOUND_ITEMGET);
								st.giveItems(CROSS_OF_EINHASAD, 1);
							}
							else
								htmltext = "31328-01b.htm";
						}
						else if (cond == 3)
							htmltext = "31328-02.htm";
						else if (cond == 8)
							htmltext = "31328-03.htm";
						else if (cond == 9)
							htmltext = "31328-11.htm";
						else if (cond == 14)
						{
							if (st.hasQuestItems(REPORT_BOX))
								htmltext = "31328-12.htm";
							else
								st.set("cond", "13");
						}
						else if (cond == 15)
							htmltext = "31328-14.htm";
						else if (cond == 16)
						{
							htmltext = (player.getLevel() < 64) ? "31328-23.htm" : "31328-22.htm";
							st.exitQuest(false);
							st.playSound(QuestState.SOUND_FINISH);
						}
						break;
					
					case GHOST_OF_PRIEST:
						if (cond == 7)
							htmltext = "31528-01.htm";
						else if (cond == 8)
							htmltext = "31528-08.htm";
						break;
					
					case GHOST_OF_ADVENTURER:
						if (cond == 9)
						{
							if (st.hasQuestItems(LETTER_OF_INNOCENTIN))
								htmltext = "31529-01.htm";
							else
							{
								htmltext = "31529-10.htm";
								st.set("cond", "8");
							}
						}
						else if (cond == 10)
							htmltext = "31529-16.htm";
						else if (cond == 11)
						{
							if (st.hasQuestItems(RED_JEWEL_OF_ADVENTURER))
							{
								htmltext = "31529-17.htm";
								st.set("cond", "12");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(RED_JEWEL_OF_ADVENTURER, 1);
							}
							else
							{
								htmltext = "31529-09.htm";
								st.set("cond", "10");
							}
						}
						else if (cond == 12)
							htmltext = "31529-17.htm";
						else if (cond == 13)
						{
							if (st.hasQuestItems(SEALED_REPORT_BOX))
							{
								htmltext = "31529-18.htm";
								st.set("cond", "14");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(SEALED_REPORT_BOX, 1);
								st.giveItems(REPORT_BOX, 1);
							}
							else
							{
								htmltext = "31529-10.htm";
								st.set("cond", "12");
							}
						}
						else if (cond > 13)
							htmltext = "31529-19.htm";
						break;
					
					case WELL:
						if (cond == 10)
							htmltext = "31527-01.htm";
						else if (cond == 11)
							htmltext = "31527-03.htm";
						else if (cond == 12)
						{
							htmltext = "31527-04.htm";
							st.set("cond", "13");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(SEALED_REPORT_BOX, 1);
						}
						else if (cond > 12)
							htmltext = "31527-05.htm";
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
		
		final QuestState st = player.getQuestState(qn);
		if (st == null || !st.isStarted())
			return null;
		
		if (attacker instanceof Summon)
			return null;
		
		if (getQuestTimer("attack_timer", null, player) != null)
			return null;
		
		if (st.getInt("cond") == 10)
			startQuestTimer("attack_timer", 20000, null, player, false);
		
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		if (npc.getNpcId() != SOUL_OF_WELL)
		{
			if (st.getInt("cond") == 4 && st.dropItems(LOST_SKULL_OF_ELF, 1, 1, 100000))
				st.set("cond", "5");
		}
		else
		{
			cancelQuestTimer("attack_timer", null, player);
			
			_soulOfWellInstance = null;
		}
		
		return null;
	}
}
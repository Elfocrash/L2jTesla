package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q609_MagicalPowerOfWater_Part1 extends Quest
{
	private static final String qn = "Q609_MagicalPowerOfWater_Part1";
	
	// NPCs
	private static final int WAHKAN = 31371;
	private static final int ASEFA = 31372;
	private static final int UDAN_BOX = 31561;
	private static final int EYE = 31685;
	
	// Items
	private static final int THIEF_KEY = 1661;
	private static final int STOLEN_GREEN_TOTEM = 7237;
	private static final int GREEN_TOTEM = 7238;
	private static final int DIVINE_STONE = 7081;
	
	public Q609_MagicalPowerOfWater_Part1()
	{
		super(609, "Magical Power of Water - Part 1");
		
		setItemsIds(STOLEN_GREEN_TOTEM);
		
		addStartNpc(WAHKAN);
		addTalkId(WAHKAN, ASEFA, UDAN_BOX);
		
		// IDs aggro ranges to avoid, else quest is automatically failed.
		addAggroRangeEnterId(21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, 21362, 21369, 21370, 21364, 21365, 21366, 21368, 21371, 21372, 21373, 21374, 21375);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31371-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.set("spawned", "0");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31561-03.htm"))
		{
			// You have been discovered ; quest is failed.
			if (st.getInt("spawned") == 1)
				htmltext = "31561-04.htm";
			// No Thief's Key in inventory.
			else if (!st.hasQuestItems(THIEF_KEY))
				htmltext = "31561-02.htm";
			else
			{
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(THIEF_KEY, 1);
				st.giveItems(STOLEN_GREEN_TOTEM, 1);
			}
		}
		else if (event.equalsIgnoreCase("AsefaEyeDespawn"))
		{
			npc.broadcastNpcSay("I'll be waiting for your return.");
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
				htmltext = (player.getLevel() >= 74 && player.getAllianceWithVarkaKetra() >= 2) ? "31371-01.htm" : "31371-02.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case WAHKAN:
						htmltext = "31371-04.htm";
						break;
					
					case ASEFA:
						if (cond == 1)
						{
							htmltext = "31372-01.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 2)
						{
							if (st.getInt("spawned") == 0)
								htmltext = "31372-02.htm";
							else
							{
								htmltext = "31372-03.htm";
								st.set("spawned", "0");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
						}
						else if (cond == 3 && st.hasQuestItems(STOLEN_GREEN_TOTEM))
						{
							htmltext = "31372-04.htm";
							
							st.takeItems(STOLEN_GREEN_TOTEM, 1);
							st.giveItems(GREEN_TOTEM, 1);
							st.giveItems(DIVINE_STONE, 1);
							
							st.unset("spawned");
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case UDAN_BOX:
						if (cond == 2)
							htmltext = "31561-01.htm";
						else if (cond == 3)
							htmltext = "31561-05.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onAggro(Npc npc, Player player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		
		if (st.getInt("spawned") == 0 && st.getInt("cond") == 2)
		{
			// Put "spawned" flag to 1 to avoid to spawn another.
			st.set("spawned", "1");
			
			// Spawn Asefa's eye.
			Npc asefaEye = addSpawn(EYE, player, true, 10000, true);
			if (asefaEye != null)
			{
				startQuestTimer("AsefaEyeDespawn", 9000, asefaEye, player, false);
				asefaEye.broadcastNpcSay("You cannot escape Asefa's Eye!");
				st.playSound(QuestState.SOUND_GIVEUP);
			}
		}
		
		return null;
	}
}
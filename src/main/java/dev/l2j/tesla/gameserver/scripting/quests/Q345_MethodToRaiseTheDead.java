package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q345_MethodToRaiseTheDead extends Quest
{
	private static final String qn = "Q345_MethodToRaiseTheDead";
	
	// Items
	private static final int VICTIM_ARM_BONE = 4274;
	private static final int VICTIM_THIGH_BONE = 4275;
	private static final int VICTIM_SKULL = 4276;
	private static final int VICTIM_RIB_BONE = 4277;
	private static final int VICTIM_SPINE = 4278;
	private static final int USELESS_BONE_PIECES = 4280;
	private static final int POWDER_TO_SUMMON_DEAD_SOULS = 4281;
	
	// NPCs
	private static final int XENOVIA = 30912;
	private static final int DOROTHY = 30970;
	private static final int ORPHEUS = 30971;
	private static final int MEDIUM_JAR = 30973;
	
	// Rewards
	private static final int BILL_OF_IASON_HEINE = 4310;
	private static final int IMPERIAL_DIAMOND = 3456;
	
	public Q345_MethodToRaiseTheDead()
	{
		super(345, "Method to Raise the Dead");
		
		setItemsIds(VICTIM_ARM_BONE, VICTIM_THIGH_BONE, VICTIM_SKULL, VICTIM_RIB_BONE, VICTIM_SPINE, POWDER_TO_SUMMON_DEAD_SOULS, USELESS_BONE_PIECES);
		
		addStartNpc(DOROTHY);
		addTalkId(DOROTHY, XENOVIA, MEDIUM_JAR, ORPHEUS);
		
		addKillId(20789, 20791);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30970-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30970-06.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30912-04.htm"))
		{
			if (player.getAdena() >= 1000)
			{
				htmltext = "30912-03.htm";
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(57, 1000);
				st.giveItems(POWDER_TO_SUMMON_DEAD_SOULS, 1);
			}
		}
		else if (event.equalsIgnoreCase("30973-04.htm"))
		{
			if (st.getInt("cond") == 3)
			{
				final int chance = Rnd.get(3);
				if (chance == 0)
				{
					st.set("cond", "6");
					htmltext = "30973-02a.htm";
				}
				else if (chance == 1)
				{
					st.set("cond", "6");
					htmltext = "30973-02b.htm";
				}
				else
				{
					st.set("cond", "7");
					htmltext = "30973-02c.htm";
				}
				
				st.takeItems(POWDER_TO_SUMMON_DEAD_SOULS, -1);
				st.takeItems(VICTIM_ARM_BONE, -1);
				st.takeItems(VICTIM_THIGH_BONE, -1);
				st.takeItems(VICTIM_SKULL, -1);
				st.takeItems(VICTIM_RIB_BONE, -1);
				st.takeItems(VICTIM_SPINE, -1);
				
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equalsIgnoreCase("30971-02a.htm"))
		{
			if (st.hasQuestItems(USELESS_BONE_PIECES))
				htmltext = "30971-02.htm";
		}
		else if (event.equalsIgnoreCase("30971-03.htm"))
		{
			if (st.hasQuestItems(USELESS_BONE_PIECES))
			{
				int amount = st.getQuestItemsCount(USELESS_BONE_PIECES) * 104;
				st.takeItems(USELESS_BONE_PIECES, -1);
				st.rewardItems(57, amount);
			}
			else
				htmltext = "30971-02a.htm";
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
				htmltext = (player.getLevel() < 35) ? "30970-00.htm" : "30970-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case DOROTHY:
						if (cond == 1)
							htmltext = (!st.hasQuestItems(VICTIM_ARM_BONE, VICTIM_THIGH_BONE, VICTIM_SKULL, VICTIM_RIB_BONE, VICTIM_SPINE)) ? "30970-04.htm" : "30970-05.htm";
						else if (cond == 2)
							htmltext = "30970-07.htm";
						else if (cond > 2 && cond < 6)
							htmltext = "30970-08.htm";
						else
						{
							// Shared part between cond 6 and 7.
							int amount = st.getQuestItemsCount(USELESS_BONE_PIECES) * 70;
							st.takeItems(USELESS_BONE_PIECES, -1);
							
							// Scaried little girl
							if (cond == 7)
							{
								htmltext = "30970-10.htm";
								st.rewardItems(57, 3040 + amount);
								
								// Reward can be either an Imperial Diamond or bills.
								if (Rnd.get(100) < 10)
									st.giveItems(IMPERIAL_DIAMOND, 1);
								else
									st.giveItems(BILL_OF_IASON_HEINE, 5);
							}
							// Friends of Dorothy
							else
							{
								htmltext = "30970-09.htm";
								st.rewardItems(57, 5390 + amount);
								st.giveItems(BILL_OF_IASON_HEINE, 3);
							}
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case XENOVIA:
						if (cond == 2)
							htmltext = "30912-01.htm";
						else if (cond > 2)
							htmltext = "30912-06.htm";
						break;
					
					case MEDIUM_JAR:
						htmltext = "30973-01.htm";
						break;
					
					case ORPHEUS:
						htmltext = "30971-01.htm";
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
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (Rnd.get(4) == 0)
		{
			final int randomPart = Rnd.get(VICTIM_ARM_BONE, VICTIM_SPINE);
			if (!st.hasQuestItems(randomPart))
			{
				st.playSound(QuestState.SOUND_ITEMGET);
				st.giveItems(randomPart, 1);
				return null;
			}
		}
		st.dropItemsAlways(USELESS_BONE_PIECES, 1, 0);
		
		return null;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;

public class Q325_GrimCollector extends Quest
{
	private static final String qn = "Q325_GrimCollector";
	
	// Items
	private static final int ANATOMY_DIAGRAM = 1349;
	private static final int ZOMBIE_HEAD = 1350;
	private static final int ZOMBIE_HEART = 1351;
	private static final int ZOMBIE_LIVER = 1352;
	private static final int SKULL = 1353;
	private static final int RIB_BONE = 1354;
	private static final int SPINE = 1355;
	private static final int ARM_BONE = 1356;
	private static final int THIGH_BONE = 1357;
	private static final int COMPLETE_SKELETON = 1358;
	
	// NPCs
	private static final int CURTIS = 30336;
	private static final int VARSAK = 30342;
	private static final int SAMED = 30434;
	
	private static final Map<Integer, List<IntIntHolder>> DROPLIST = new HashMap<>();
	{
		DROPLIST.put(20026, Arrays.asList(new IntIntHolder(ZOMBIE_HEAD, 30), new IntIntHolder(ZOMBIE_HEART, 50), new IntIntHolder(ZOMBIE_LIVER, 75)));
		DROPLIST.put(20029, Arrays.asList(new IntIntHolder(ZOMBIE_HEAD, 30), new IntIntHolder(ZOMBIE_HEART, 52), new IntIntHolder(ZOMBIE_LIVER, 75)));
		DROPLIST.put(20035, Arrays.asList(new IntIntHolder(SKULL, 5), new IntIntHolder(RIB_BONE, 15), new IntIntHolder(SPINE, 29), new IntIntHolder(THIGH_BONE, 79)));
		DROPLIST.put(20042, Arrays.asList(new IntIntHolder(SKULL, 6), new IntIntHolder(RIB_BONE, 19), new IntIntHolder(ARM_BONE, 69), new IntIntHolder(THIGH_BONE, 86)));
		DROPLIST.put(20045, Arrays.asList(new IntIntHolder(SKULL, 9), new IntIntHolder(SPINE, 59), new IntIntHolder(ARM_BONE, 77), new IntIntHolder(THIGH_BONE, 97)));
		DROPLIST.put(20051, Arrays.asList(new IntIntHolder(SKULL, 9), new IntIntHolder(RIB_BONE, 59), new IntIntHolder(SPINE, 79), new IntIntHolder(ARM_BONE, 100)));
		DROPLIST.put(20457, Arrays.asList(new IntIntHolder(ZOMBIE_HEAD, 40), new IntIntHolder(ZOMBIE_HEART, 60), new IntIntHolder(ZOMBIE_LIVER, 80)));
		DROPLIST.put(20458, Arrays.asList(new IntIntHolder(ZOMBIE_HEAD, 40), new IntIntHolder(ZOMBIE_HEART, 70), new IntIntHolder(ZOMBIE_LIVER, 100)));
		DROPLIST.put(20514, Arrays.asList(new IntIntHolder(SKULL, 6), new IntIntHolder(RIB_BONE, 21), new IntIntHolder(SPINE, 30), new IntIntHolder(ARM_BONE, 31), new IntIntHolder(THIGH_BONE, 64)));
		DROPLIST.put(20515, Arrays.asList(new IntIntHolder(SKULL, 5), new IntIntHolder(RIB_BONE, 20), new IntIntHolder(SPINE, 31), new IntIntHolder(ARM_BONE, 33), new IntIntHolder(THIGH_BONE, 69)));
	}
	
	public Q325_GrimCollector()
	{
		super(325, "Grim Collector");
		
		setItemsIds(ZOMBIE_HEAD, ZOMBIE_HEART, ZOMBIE_LIVER, SKULL, RIB_BONE, SPINE, ARM_BONE, THIGH_BONE, COMPLETE_SKELETON, ANATOMY_DIAGRAM);
		
		addStartNpc(CURTIS);
		addTalkId(CURTIS, VARSAK, SAMED);
		
		for (int npcId : DROPLIST.keySet())
			addKillId(npcId);
	}
	
	private static int getNumberOfPieces(QuestState st)
	{
		return st.getQuestItemsCount(ZOMBIE_HEAD) + st.getQuestItemsCount(SPINE) + st.getQuestItemsCount(ARM_BONE) + st.getQuestItemsCount(ZOMBIE_HEART) + st.getQuestItemsCount(ZOMBIE_LIVER) + st.getQuestItemsCount(SKULL) + st.getQuestItemsCount(RIB_BONE) + st.getQuestItemsCount(THIGH_BONE) + st.getQuestItemsCount(COMPLETE_SKELETON);
	}
	
	private static void payback(QuestState st)
	{
		final int count = getNumberOfPieces(st);
		if (count > 0)
		{
			int reward = 30 * st.getQuestItemsCount(ZOMBIE_HEAD) + 20 * st.getQuestItemsCount(ZOMBIE_HEART) + 20 * st.getQuestItemsCount(ZOMBIE_LIVER) + 100 * st.getQuestItemsCount(SKULL) + 40 * st.getQuestItemsCount(RIB_BONE) + 14 * st.getQuestItemsCount(SPINE) + 14 * st.getQuestItemsCount(ARM_BONE) + 14 * st.getQuestItemsCount(THIGH_BONE) + 341 * st.getQuestItemsCount(COMPLETE_SKELETON);
			if (count > 10)
				reward += 1629;
			
			if (st.hasQuestItems(COMPLETE_SKELETON))
				reward += 543;
			
			st.takeItems(ZOMBIE_HEAD, -1);
			st.takeItems(ZOMBIE_HEART, -1);
			st.takeItems(ZOMBIE_LIVER, -1);
			st.takeItems(SKULL, -1);
			st.takeItems(RIB_BONE, -1);
			st.takeItems(SPINE, -1);
			st.takeItems(ARM_BONE, -1);
			st.takeItems(THIGH_BONE, -1);
			st.takeItems(COMPLETE_SKELETON, -1);
			
			st.rewardItems(57, reward);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30336-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30434-03.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(ANATOMY_DIAGRAM, 1);
		}
		else if (event.equalsIgnoreCase("30434-06.htm"))
		{
			st.takeItems(ANATOMY_DIAGRAM, -1);
			payback(st);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30434-07.htm"))
		{
			payback(st);
		}
		else if (event.equalsIgnoreCase("30434-09.htm"))
		{
			final int skeletons = st.getQuestItemsCount(COMPLETE_SKELETON);
			if (skeletons > 0)
			{
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(COMPLETE_SKELETON, -1);
				st.rewardItems(57, 543 + 341 * skeletons);
			}
		}
		else if (event.equalsIgnoreCase("30342-03.htm"))
		{
			if (!st.hasQuestItems(SPINE, ARM_BONE, SKULL, RIB_BONE, THIGH_BONE))
				htmltext = "30342-02.htm";
			else
			{
				st.takeItems(SPINE, 1);
				st.takeItems(SKULL, 1);
				st.takeItems(ARM_BONE, 1);
				st.takeItems(RIB_BONE, 1);
				st.takeItems(THIGH_BONE, 1);
				
				if (Rnd.get(10) < 9)
					st.giveItems(COMPLETE_SKELETON, 1);
				else
					htmltext = "30342-04.htm";
			}
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
				htmltext = (player.getLevel() < 15) ? "30336-01.htm" : "30336-02.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case CURTIS:
						htmltext = (!st.hasQuestItems(ANATOMY_DIAGRAM)) ? "30336-04.htm" : "30336-05.htm";
						break;
					
					case SAMED:
						if (!st.hasQuestItems(ANATOMY_DIAGRAM))
							htmltext = "30434-01.htm";
						else
						{
							if (getNumberOfPieces(st) == 0)
								htmltext = "30434-04.htm";
							else
								htmltext = (!st.hasQuestItems(COMPLETE_SKELETON)) ? "30434-05.htm" : "30434-08.htm";
						}
						break;
					
					case VARSAK:
						htmltext = "30342-01.htm";
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
		
		if (st.hasQuestItems(ANATOMY_DIAGRAM))
		{
			final int chance = Rnd.get(100);
			for (IntIntHolder drop : DROPLIST.get(npc.getNpcId()))
			{
				if (chance < drop.getValue())
				{
					st.dropItemsAlways(drop.getId(), 1, 0);
					break;
				}
			}
		}
		
		return null;
	}
}
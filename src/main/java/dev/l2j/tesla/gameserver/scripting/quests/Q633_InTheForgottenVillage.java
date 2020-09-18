package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q633_InTheForgottenVillage extends Quest
{
	private static final String qn = "Q633_InTheForgottenVillage";
	
	// NPCS
	private static final int MINA = 31388;
	
	// ITEMS
	private static final int RIB_BONE = 7544;
	private static final int ZOMBIE_LIVER = 7545;
	
	// MOBS / DROP chances
	private static final Map<Integer, Integer> MOBS = new HashMap<>();
	{
		MOBS.put(21557, 328000); // Bone Snatcher
		MOBS.put(21558, 328000); // Bone Snatcher
		MOBS.put(21559, 337000); // Bone Maker
		MOBS.put(21560, 337000); // Bone Shaper
		MOBS.put(21563, 342000); // Bone Collector
		MOBS.put(21564, 348000); // Skull Collector
		MOBS.put(21565, 351000); // Bone Animator
		MOBS.put(21566, 359000); // Skull Animator
		MOBS.put(21567, 359000); // Bone Slayer
		MOBS.put(21572, 365000); // Bone Sweeper
		MOBS.put(21574, 383000); // Bone Grinder
		MOBS.put(21575, 383000); // Bone Grinder
		MOBS.put(21580, 385000); // Bone Caster
		MOBS.put(21581, 395000); // Bone Puppeteer
		MOBS.put(21583, 397000); // Bone Scavenger
		MOBS.put(21584, 401000); // Bone Scavenger
	}
	
	private static final Map<Integer, Integer> UNDEADS = new HashMap<>();
	{
		UNDEADS.put(21553, 347000); // Trampled Man
		UNDEADS.put(21554, 347000); // Trampled Man
		UNDEADS.put(21561, 450000); // Sacrificed Man
		UNDEADS.put(21578, 501000); // Behemoth Zombie
		UNDEADS.put(21596, 359000); // Requiem Lord
		UNDEADS.put(21597, 370000); // Requiem Behemoth
		UNDEADS.put(21598, 441000); // Requiem Behemoth
		UNDEADS.put(21599, 395000); // Requiem Priest
		UNDEADS.put(21600, 408000); // Requiem Behemoth
		UNDEADS.put(21601, 411000); // Requiem Behemoth
	}
	
	public Q633_InTheForgottenVillage()
	{
		super(633, "In the Forgotten Village");
		
		setItemsIds(RIB_BONE, ZOMBIE_LIVER);
		
		addStartNpc(MINA);
		addTalkId(MINA);
		
		for (int i : MOBS.keySet())
			addKillId(i);
		
		for (int i : UNDEADS.keySet())
			addKillId(i);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31388-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31388-10.htm"))
		{
			st.takeItems(RIB_BONE, -1);
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("31388-09.htm"))
		{
			if (st.getQuestItemsCount(RIB_BONE) >= 200)
			{
				htmltext = "31388-08.htm";
				st.takeItems(RIB_BONE, 200);
				st.rewardItems(57, 25000);
				st.rewardExpAndSp(305235, 0);
				st.playSound(QuestState.SOUND_FINISH);
			}
			st.set("cond", "1");
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
				htmltext = (player.getLevel() < 65) ? "31388-03.htm" : "31388-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "31388-06.htm";
				else if (cond == 2)
					htmltext = "31388-05.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		final int npcId = npc.getNpcId();
		
		if (UNDEADS.containsKey(npcId))
		{
			final QuestState st = getRandomPartyMemberState(player, npc, STATE_STARTED);
			if (st == null)
				return null;
			
			st.dropItems(ZOMBIE_LIVER, 1, 0, UNDEADS.get(npcId));
		}
		else if (MOBS.containsKey(npcId))
		{
			final QuestState st = getRandomPartyMember(player, npc, "1");
			if (st == null)
				return null;
			
			if (st.dropItems(RIB_BONE, 1, 200, MOBS.get(npcId)))
				st.set("cond", "2");
		}
		
		return null;
	}
}
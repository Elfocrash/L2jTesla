package dev.l2j.tesla.gameserver.scripting.scripts.custom;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.location.SpawnLocation;

public class MissQueen extends Quest
{
	private static final String qn = "MissQueen";
	
	// Rewards
	private static final int COUPON_ONE = 7832;
	private static final int COUPON_TWO = 7833;
	
	// Miss Queen locations
	private static final SpawnLocation[] LOCATIONS =
	{
		new SpawnLocation(116224, -181728, -1378, 0),
		new SpawnLocation(114885, -178092, -832, 0),
		new SpawnLocation(45472, 49312, -3072, 53000),
		new SpawnLocation(47648, 51296, -2994, 38500),
		new SpawnLocation(11340, 15972, -4582, 14000),
		new SpawnLocation(10968, 17540, -4572, 55000),
		new SpawnLocation(-14048, 123184, -3120, 32000),
		new SpawnLocation(-44979, -113508, -199, 32000),
		new SpawnLocation(-84119, 243254, -3730, 8000),
		new SpawnLocation(-84336, 242156, -3730, 24500),
		new SpawnLocation(-82032, 150160, -3127, 16500)
	};
	
	public MissQueen()
	{
		super(-1, "custom");
		
		// Spawn the 11 NPCs.
		for (SpawnLocation loc : LOCATIONS)
			addSpawn(31760, loc, false, 0, false);
		
		addStartNpc(31760);
		addTalkId(31760);
		addFirstTalkId(31760);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (event.equalsIgnoreCase("newbie_coupon"))
		{
			if (player.getClassId().level() == 0 && player.getLevel() >= 6 && player.getLevel() <= 25 && player.getPkKills() <= 0)
			{
				if (st.getInt("reward_1") == 1)
					htmltext = "31760-01.htm";
				else
				{
					st.setState(STATE_STARTED);
					htmltext = "31760-02.htm";
					st.set("reward_1", "1");
					st.giveItems(COUPON_ONE, 1);
				}
			}
			else
				htmltext = "31760-03.htm";
		}
		else if (event.equalsIgnoreCase("traveller_coupon"))
		{
			if (player.getClassId().level() == 1 && player.getLevel() >= 6 && player.getLevel() <= 25 && player.getPkKills() <= 0)
			{
				if (st.getInt("reward_2") == 1)
					htmltext = "31760-04.htm";
				else
				{
					st.setState(STATE_STARTED);
					htmltext = "31760-05.htm";
					st.set("reward_2", "1");
					st.giveItems(COUPON_TWO, 1);
				}
			}
			else
				htmltext = "31760-06.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		
		return "31760.htm";
	}
}
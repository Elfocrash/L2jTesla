package dev.l2j.tesla.gameserver.scripting.scripts.custom;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.data.sql.SpawnTable;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.model.spawn.L2Spawn;

public class RaidbossInfo extends Quest
{
	private static final String qn = "RaidbossInfo";
	private static final String BOSS_CLASS_TYPE = "RaidBoss";
	
	private static final Map<Integer, Location> RADARS = new HashMap<>();
	
	private static final int[] NPCs =
	{
		31729,
		31730,
		31731,
		31732,
		31733,
		31734,
		31735,
		31736,
		31737,
		31738,
		31775,
		31776,
		31777,
		31778,
		31779,
		31780,
		31781,
		31782,
		31783,
		31784,
		31785,
		31786,
		31787,
		31788,
		31789,
		31790,
		31791,
		31792,
		31793,
		31794,
		31795,
		31796,
		31797,
		31798,
		31799,
		31800,
		31801,
		31802,
		31803,
		31804,
		31805,
		31806,
		31807,
		31808,
		31809,
		31810,
		31811,
		31812,
		31813,
		31814,
		31815,
		31816,
		31817,
		31818,
		31819,
		31820,
		31821,
		31822,
		31823,
		31824,
		31825,
		31826,
		31827,
		31828,
		31829,
		31830,
		31831,
		31832,
		31833,
		31834,
		31835,
		31836,
		31837,
		31838,
		31839,
		31840,
		31841
	};
	
	public RaidbossInfo()
	{
		super(-1, "custom");
		
		for (int npcId : NPCs)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
		
		// Add all Raid Bosses locations.
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawns())
		{
			if (spawn.getTemplate().isType(BOSS_CLASS_TYPE))
				RADARS.put(spawn.getNpcId(), spawn.getLoc());
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return event;
		
		if (StringUtil.isDigit(event))
		{
			int rbid = Integer.parseInt(event);
			
			if (RADARS.containsKey(rbid))
			{
				Location loc = RADARS.get(rbid);
				st.addRadar(loc.getX(), loc.getY(), loc.getZ());
			}
			st.exitQuest(true);
			return null;
		}
		return event;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		return "info.htm";
	}
}
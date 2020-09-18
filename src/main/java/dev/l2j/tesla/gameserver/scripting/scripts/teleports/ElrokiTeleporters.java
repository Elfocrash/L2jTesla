package dev.l2j.tesla.gameserver.scripting.scripts.teleports;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class ElrokiTeleporters extends Quest
{
	public ElrokiTeleporters()
	{
		super(-1, "teleports");
		
		addStartNpc(32111, 32112);
		addTalkId(32111, 32112);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		if (npc.getNpcId() == 32111)
		{
			if (player.isInCombat())
				return "32111-no.htm";
			
			player.teleportTo(4990, -1879, -3178, 0);
		}
		else
			player.teleportTo(7557, -5513, -3221, 0);
		
		return null;
	}
}
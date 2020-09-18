package dev.l2j.tesla.gameserver.model.zone.type;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.zone.ZoneType;
import dev.l2j.tesla.gameserver.network.serverpackets.AbstractNpcInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.ServerObjectInfo;
import dev.l2j.tesla.gameserver.enums.ZoneId;

/**
 * A zone extending {@link ZoneType}, used for the water behavior. {@link Player}s can drown if they stay too long below water line.
 */
public class WaterZone extends ZoneType
{
	public WaterZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.WATER, true);
		
		if (character instanceof Player)
			((Player) character).broadcastUserInfo();
		else if (character instanceof Npc)
		{
			for (Player player : character.getKnownType(Player.class))
			{
				if (character.getMoveSpeed() == 0)
					player.sendPacket(new ServerObjectInfo((Npc) character, player));
				else
					player.sendPacket(new AbstractNpcInfo.NpcInfo((Npc) character, player));
			}
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.WATER, false);
		
		if (character instanceof Player)
			((Player) character).broadcastUserInfo();
		else if (character instanceof Npc)
		{
			for (Player player : character.getKnownType(Player.class))
			{
				if (character.getMoveSpeed() == 0)
					player.sendPacket(new ServerObjectInfo((Npc) character, player));
				else
					player.sendPacket(new AbstractNpcInfo.NpcInfo((Npc) character, player));
			}
		}
	}
	
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}
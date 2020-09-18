package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.data.xml.AdminData;
import dev.l2j.tesla.gameserver.model.actor.Player;

public final class RequestGmList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getPlayer();
		if (activeChar == null)
			return;
		
		AdminData.getInstance().sendListToPlayer(activeChar);
	}
}
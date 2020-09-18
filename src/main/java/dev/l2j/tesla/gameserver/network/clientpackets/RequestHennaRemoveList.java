package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.network.serverpackets.HennaRemoveList;
import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * RequestHennaRemoveList
 * @author Tempy
 */
public final class RequestHennaRemoveList extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	protected void readImpl()
	{
		_unknown = readD(); // ??
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getPlayer();
		if (activeChar == null)
			return;
		
		activeChar.sendPacket(new HennaRemoveList(activeChar));
	}
}
package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.model.actor.Player;

public final class RequestDeleteMacro extends L2GameClientPacket
{
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		player.getMacroList().deleteMacro(_id);
	}
}
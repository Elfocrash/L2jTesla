package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.network.serverpackets.PledgeCrest;

public final class RequestPledgeCrest extends L2GameClientPacket
{
	private int _crestId;
	
	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		sendPacket(new PledgeCrest(_crestId));
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
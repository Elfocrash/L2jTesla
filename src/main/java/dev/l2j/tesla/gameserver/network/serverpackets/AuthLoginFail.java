package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.enums.FailReason;

public class AuthLoginFail extends L2GameServerPacket
{
	private final FailReason _reason;
	
	public AuthLoginFail(FailReason reason)
	{
		_reason = reason;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x14);
		writeD(_reason.ordinal());
	}
}
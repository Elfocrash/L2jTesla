package dev.l2j.tesla.loginserver.network.serverpackets;

import dev.l2j.tesla.loginserver.network.SessionKey;

public final class PlayOk extends L2LoginServerPacket
{
	private final int _playOk1, _playOk2;
	
	public PlayOk(SessionKey sessionKey)
	{
		_playOk1 = sessionKey.playOkID1;
		_playOk2 = sessionKey.playOkID2;
	}
	
	@Override
	protected void write()
	{
		writeC(0x07);
		writeD(_playOk1);
		writeD(_playOk2);
	}
}
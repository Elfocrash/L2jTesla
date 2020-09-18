package dev.l2j.tesla.gameserver.network.gameserverpackets;

import dev.l2j.tesla.gameserver.network.SessionKey;

public class PlayerAuthRequest extends GameServerBasePacket
{
	public PlayerAuthRequest(String account, SessionKey key)
	{
		writeC(0x05);
		writeS(account);
		writeD(key.playOkID1);
		writeD(key.playOkID2);
		writeD(key.loginOkID1);
		writeD(key.loginOkID2);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
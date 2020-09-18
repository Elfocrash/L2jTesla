package dev.l2j.tesla.loginserver.network.loginserverpackets;

import dev.l2j.tesla.loginserver.GameServerManager;
import dev.l2j.tesla.loginserver.network.serverpackets.ServerBasePacket;

public class AuthResponse extends ServerBasePacket
{
	public AuthResponse(int serverId)
	{
		writeC(0x02);
		writeC(serverId);
		writeS(GameServerManager.getInstance().getServerNames().get(serverId));
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
package dev.l2j.tesla.loginserver.network.loginserverpackets;

import dev.l2j.tesla.loginserver.network.serverpackets.ServerBasePacket;

public class LoginServerFail extends ServerBasePacket
{
	public static final int REASON_IP_BANNED = 1;
	public static final int REASON_IP_RESERVED = 2;
	public static final int REASON_WRONG_HEXID = 3;
	public static final int REASON_ID_RESERVED = 4;
	public static final int REASON_NO_FREE_ID = 5;
	public static final int NOT_AUTHED = 6;
	public static final int REASON_ALREADY_LOGGED_IN = 7;
	
	public LoginServerFail(int reason)
	{
		writeC(0x01);
		writeC(reason);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
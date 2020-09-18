package dev.l2j.tesla.loginserver.network.serverpackets;

public final class PlayFail extends L2LoginServerPacket
{
	public static final PlayFail REASON_SYSTEM_ERROR = new PlayFail(0x01);
	public static final PlayFail REASON_USER_OR_PASS_WRONG = new PlayFail(0x02);
	public static final PlayFail REASON3 = new PlayFail(0x03);
	public static final PlayFail REASON4 = new PlayFail(0x04);
	public static final PlayFail REASON_TOO_MANY_PLAYERS = new PlayFail(0x0f);
	
	private final int _reason;
	
	private PlayFail(int reason)
	{
		_reason = reason;
	}
	
	@Override
	protected void write()
	{
		writeC(0x06);
		writeC(_reason);
	}
}
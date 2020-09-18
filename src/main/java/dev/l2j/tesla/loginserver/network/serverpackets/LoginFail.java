package dev.l2j.tesla.loginserver.network.serverpackets;

public final class LoginFail extends L2LoginServerPacket
{
	public static final LoginFail REASON_SYSTEM_ERROR = new LoginFail(0x01);
	public static final LoginFail REASON_PASS_WRONG = new LoginFail(0x02);
	public static final LoginFail REASON_USER_OR_PASS_WRONG = new LoginFail(0x03);
	public static final LoginFail REASON_ACCESS_FAILED = new LoginFail(0x04);
	public static final LoginFail REASON_ACCOUNT_IN_USE = new LoginFail(0x07);
	public static final LoginFail REASON_SERVER_OVERLOADED = new LoginFail(0x0f);
	public static final LoginFail REASON_SERVER_MAINTENANCE = new LoginFail(0x10);
	public static final LoginFail REASON_TEMP_PASS_EXPIRED = new LoginFail(0x11);
	public static final LoginFail REASON_DUAL_BOX = new LoginFail(0x23);
	
	private final int _reason;
	
	private LoginFail(int reason)
	{
		_reason = reason;
	}
	
	@Override
	protected void write()
	{
		writeC(0x01);
		writeD(_reason);
	}
}
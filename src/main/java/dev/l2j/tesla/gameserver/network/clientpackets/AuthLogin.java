package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.LoginServerThread;
import dev.l2j.tesla.gameserver.network.SessionKey;

public final class AuthLogin extends L2GameClientPacket
{
	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	
	@Override
	protected void readImpl()
	{
		_loginName = readS().toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (getClient().getAccountName() != null)
			return;
		
		getClient().setAccountName(_loginName);
		getClient().setSessionId(new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2));
		
		// Add the client.
		LoginServerThread.getInstance().addClient(_loginName, getClient());
	}
}
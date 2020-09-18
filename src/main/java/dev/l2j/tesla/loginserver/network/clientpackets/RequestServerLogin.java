package dev.l2j.tesla.loginserver.network.clientpackets;

import dev.l2j.tesla.loginserver.LoginController;
import dev.l2j.tesla.loginserver.network.SessionKey;
import dev.l2j.tesla.loginserver.network.serverpackets.LoginFail;
import dev.l2j.tesla.loginserver.network.serverpackets.PlayFail;
import dev.l2j.tesla.loginserver.network.serverpackets.PlayOk;
import dev.l2j.tesla.Config;

public class RequestServerLogin extends L2LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _serverId;
	
	public int getSessionKey1()
	{
		return _skey1;
	}
	
	public int getSessionKey2()
	{
		return _skey2;
	}
	
	public int getServerID()
	{
		return _serverId;
	}
	
	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 9)
		{
			_skey1 = readD();
			_skey2 = readD();
			_serverId = readC();
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		SessionKey sk = getClient().getSessionKey();
		
		// if we didnt showed the license we cant check these values
		if (!Config.SHOW_LICENCE || sk.checkLoginPair(_skey1, _skey2))
		{
			if (LoginController.getInstance().isLoginPossible(getClient(), _serverId))
			{
				getClient().setJoinedGS(true);
				getClient().sendPacket(new PlayOk(sk));
			}
			else
				getClient().close(PlayFail.REASON_TOO_MANY_PLAYERS);
		}
		else
			getClient().close(LoginFail.REASON_ACCESS_FAILED);
	}
}

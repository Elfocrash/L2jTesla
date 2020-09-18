package dev.l2j.tesla.loginserver.network.clientpackets;

import java.net.InetAddress;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import dev.l2j.tesla.loginserver.LoginController;
import dev.l2j.tesla.loginserver.network.LoginClient;
import dev.l2j.tesla.loginserver.network.SessionKey;
import dev.l2j.tesla.loginserver.network.serverpackets.AccountKicked;
import dev.l2j.tesla.loginserver.network.serverpackets.LoginFail;
import dev.l2j.tesla.loginserver.network.serverpackets.LoginOk;
import dev.l2j.tesla.loginserver.network.serverpackets.ServerList;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.loginserver.model.AccountInfo;
import dev.l2j.tesla.loginserver.model.GameServerInfo;

public class RequestAuthLogin extends L2LoginClientPacket
{
	private final byte[] _raw = new byte[128];
	
	private String _user;
	private String _password;
	private int _ncotp;
	
	public String getPassword()
	{
		return _password;
	}
	
	public String getUser()
	{
		return _user;
	}
	
	public int getOneTimePassword()
	{
		return _ncotp;
	}
	
	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 128)
		{
			readB(_raw);
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		byte[] decrypted = null;
		final LoginClient client = getClient();
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80);
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.error("Failed to generate a cipher.", e);
			return;
		}
		
		try
		{
			_user = new String(decrypted, 0x5E, 14).trim().toLowerCase();
			_password = new String(decrypted, 0x6C, 16).trim();
			_ncotp = decrypted[0x7c];
			_ncotp |= decrypted[0x7d] << 8;
			_ncotp |= decrypted[0x7e] << 16;
			_ncotp |= decrypted[0x7f] << 24;
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to decrypt user/password.", e);
			return;
		}
		
		final InetAddress clientAddr = client.getConnection().getInetAddress();
		
		final AccountInfo info = LoginController.getInstance().retrieveAccountInfo(clientAddr, _user, _password);
		if (info == null)
		{
			client.close(LoginFail.REASON_USER_OR_PASS_WRONG);
			return;
		}
		
		final LoginController.AuthLoginResult result = LoginController.getInstance().tryCheckinAccount(client, clientAddr, info);
		switch (result)
		{
			case AUTH_SUCCESS:
				client.setAccount(info.getLogin());
				client.setState(LoginClient.LoginClientState.AUTHED_LOGIN);
				client.setSessionKey(new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt()));
				client.sendPacket((Config.SHOW_LICENCE) ? new LoginOk(client.getSessionKey()) : new ServerList(client));
				break;
			
			case INVALID_PASSWORD:
				client.close(LoginFail.REASON_USER_OR_PASS_WRONG);
				break;
			
			case ACCOUNT_BANNED:
				client.close(new AccountKicked(AccountKicked.AccountKickedReason.REASON_PERMANENTLY_BANNED));
				break;
			
			case ALREADY_ON_LS:
				final LoginClient oldClient = LoginController.getInstance().getAuthedClient(info.getLogin());
				if (oldClient != null)
				{
					oldClient.close(LoginFail.REASON_ACCOUNT_IN_USE);
					LoginController.getInstance().removeAuthedLoginClient(info.getLogin());
				}
				client.close(LoginFail.REASON_ACCOUNT_IN_USE);
				break;
			
			case ALREADY_ON_GS:
				final GameServerInfo gsi = LoginController.getInstance().getAccountOnGameServer(info.getLogin());
				if (gsi != null)
				{
					client.close(LoginFail.REASON_ACCOUNT_IN_USE);
					
					if (gsi.isAuthed())
						gsi.getGameServerThread().kickPlayer(info.getLogin());
				}
				break;
		}
	}
}
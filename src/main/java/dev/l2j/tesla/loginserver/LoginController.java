package dev.l2j.tesla.loginserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.loginserver.crypt.ScrambledKeyPair;
import dev.l2j.tesla.loginserver.model.AccountInfo;
import dev.l2j.tesla.loginserver.model.GameServerInfo;
import dev.l2j.tesla.loginserver.network.LoginClient;
import dev.l2j.tesla.loginserver.network.SessionKey;
import dev.l2j.tesla.loginserver.network.serverpackets.LoginFail;
import org.mindrot.jbcrypt.BCrypt;

public class LoginController
{
	public static enum AuthLoginResult
	{
		INVALID_PASSWORD,
		ACCOUNT_BANNED,
		ALREADY_ON_LS,
		ALREADY_ON_GS,
		AUTH_SUCCESS
	}
	
	protected static final CLogger LOGGER = new CLogger(LoginController.class.getName());
	
	private static final String USER_INFO_SELECT = "SELECT login, password, access_level, lastServer FROM accounts WHERE login=?";
	private static final String AUTOCREATE_ACCOUNTS_INSERT = "INSERT INTO accounts (login, password, lastactive, access_level) values (?, ?, ?, ?)";
	private static final String ACCOUNT_INFO_UPDATE = "UPDATE accounts SET lastactive = ? WHERE login = ?";
	private static final String ACCOUNT_LAST_SERVER_UPDATE = "UPDATE accounts SET lastServer = ? WHERE login = ?";
	private static final String ACCOUNT_ACCESS_LEVEL_UPDATE = "UPDATE accounts SET access_level = ? WHERE login = ?";
	
	/** Time before kicking the client if he didnt logged yet */
	public static final int LOGIN_TIMEOUT = 60 * 1000;
	
	protected Map<String, LoginClient> _clients = new ConcurrentHashMap<>();
	private final Map<InetAddress, Long> _bannedIps = new ConcurrentHashMap<>();
	private final Map<InetAddress, Integer> _failedAttempts = new ConcurrentHashMap<>();
	
	protected ScrambledKeyPair[] _keyPairs;
	
	protected byte[][] _blowfishKeys;
	private static final int BLOWFISH_KEYS = 20;
	
	protected LoginController()
	{
		_keyPairs = new ScrambledKeyPair[10];
		
		try
		{
			KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
			RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
			keygen.initialize(spec);
			
			// generate the initial set of keys
			for (int i = 0; i < 10; i++)
				_keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
			
			LOGGER.info("Cached 10 KeyPairs for RSA communication.");
			
			// Test the cipher.
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, _keyPairs[0].getKeyPair().getPrivate());
			
			// Store keys for blowfish communication
			_blowfishKeys = new byte[BLOWFISH_KEYS][16];
			
			for (int i = 0; i < BLOWFISH_KEYS; i++)
			{
				for (int j = 0; j < _blowfishKeys[i].length; j++)
					_blowfishKeys[i][j] = (byte) (Rnd.get(255) + 1);
			}
			LOGGER.info("Stored {} keys for Blowfish communication.", _blowfishKeys.length);
		}
		catch (GeneralSecurityException gse)
		{
			LOGGER.error("Failed generating keys.", gse);
		}
		
		// "Dropping AFK connections on login" task.
		final Thread purge = new PurgeThread();
		purge.setDaemon(true);
		purge.start();
	}
	
	/**
	 * @return Returns a random key
	 */
	public byte[] getBlowfishKey()
	{
		return _blowfishKeys[(int) (Math.random() * BLOWFISH_KEYS)];
	}
	
	public void removeAuthedLoginClient(String account)
	{
		if (account == null)
			return;
		
		_clients.remove(account);
	}
	
	public LoginClient getAuthedClient(String account)
	{
		return _clients.get(account);
	}
	
	/**
	 * Update attempts counter. If the maximum amount is reached, it will end with a client ban.
	 * @param addr : The InetAddress to test.
	 */
	private void recordFailedAttempt(InetAddress addr)
	{
		final int attempts = _failedAttempts.merge(addr, 1, (k, v) -> k + v);
		if (attempts >= Config.LOGIN_TRY_BEFORE_BAN)
		{
			addBanForAddress(addr, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
			
			// we need to clear the failed login attempts here
			_failedAttempts.remove(addr);
			
			LOGGER.info("IP address: {} has been banned due to too many login attempts.", addr.getHostAddress());
		}
	}
	
	public AccountInfo retrieveAccountInfo(InetAddress addr, String login, String password)
	{
		try
		{	
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(USER_INFO_SELECT))
			{
				ps.setString(1, login);
				try (ResultSet rset = ps.executeQuery())
				{
					if (rset.next())
					{
						AccountInfo info = new AccountInfo(rset.getString("login"), rset.getString("password"), rset.getInt("access_level"), rset.getInt("lastServer"));
						if ((Config.PASSWORD_HASH_METHOD.equalsIgnoreCase("SHA1") && !SHA1.passwordMatches(password, info.getPassHash())) ||
								(Config.PASSWORD_HASH_METHOD.equalsIgnoreCase("bcrypt") && !BCrypt.checkpw(password, info.getPassHash())))
						{
							// wrong password
							recordFailedAttempt(addr);
							return null;
						}
						
						_failedAttempts.remove(addr);
						return info;
					}
				}
			}
			
			if (!Config.AUTO_CREATE_ACCOUNTS)
			{
				// account does not exist and auto create account is not desired
				recordFailedAttempt(addr);
				return null;
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(AUTOCREATE_ACCOUNTS_INSERT))
			{
				ps.setString(1, login);
				ps.setString(2, Config.PASSWORD_HASH_METHOD.equalsIgnoreCase("SHA1") ? SHA1.hash(password) : BCrypt.hashpw(password, BCrypt.gensalt()));
				ps.setLong(3, System.currentTimeMillis());
				ps.setInt(4, 0);
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.error("Exception auto creating account for {}.", e, login);
				return null;
			}
			
			LOGGER.info("Auto created account '{}'.", login);
			return retrieveAccountInfo(addr, login, password);
		}
		catch (Exception e)
		{
			LOGGER.error("Exception retrieving account info for '{}'.", e, login);
			return null;
		}
	}
	
	public AuthLoginResult tryCheckinAccount(LoginClient client, InetAddress address, AccountInfo info)
	{
		if (info.getAccessLevel() < 0)
			return AuthLoginResult.ACCOUNT_BANNED;
		
		AuthLoginResult ret = AuthLoginResult.INVALID_PASSWORD;
		if (canCheckin(client, address, info))
		{
			// login was successful, verify presence on Gameservers
			ret = AuthLoginResult.ALREADY_ON_GS;
			if (!isAccountInAnyGameServer(info.getLogin()))
			{
				// account isnt on any GS verify LS itself
				ret = AuthLoginResult.ALREADY_ON_LS;
				
				if (_clients.putIfAbsent(info.getLogin(), client) == null)
					ret = AuthLoginResult.AUTH_SUCCESS;
			}
		}
		return ret;
	}
	
	/**
	 * @param client the client
	 * @param address client host address
	 * @param info the account info to checkin
	 * @return true when ok to checkin, false otherwise
	 */
	private static boolean canCheckin(LoginClient client, InetAddress address, AccountInfo info)
	{
		client.setAccessLevel(info.getAccessLevel());
		client.setLastServer(info.getLastServer());
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(ACCOUNT_INFO_UPDATE))
		{
			ps.setLong(1, System.currentTimeMillis());
			ps.setString(2, info.getLogin());
			ps.execute();
			
			return true;
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't finish login process.", e);
			return false;
		}
	}
	
	/**
	 * Adds the address to the ban list of the login server, with the given duration.
	 * @param address The Address to be banned.
	 * @param expiration Timestamp in miliseconds when this ban expires
	 * @throws UnknownHostException if the address is invalid.
	 */
	public void addBanForAddress(String address, long expiration) throws UnknownHostException
	{
		_bannedIps.putIfAbsent(InetAddress.getByName(address), expiration);
	}
	
	/**
	 * Adds the address to the ban list of the login server, with the given duration.
	 * @param address The Address to be banned.
	 * @param duration is miliseconds
	 */
	public void addBanForAddress(InetAddress address, long duration)
	{
		_bannedIps.putIfAbsent(address, System.currentTimeMillis() + duration);
	}
	
	public boolean isBannedAddress(InetAddress address)
	{
		final Long time = _bannedIps.get(address);
		if (time != null)
		{
			if (time > 0 && time < System.currentTimeMillis())
			{
				_bannedIps.remove(address);
				LOGGER.info("Removed expired ip address ban {}.", address.getHostAddress());
				return false;
			}
			return true;
		}
		return false;
	}
	
	public Map<InetAddress, Long> getBannedIps()
	{
		return _bannedIps;
	}
	
	/**
	 * Remove the specified address from the ban list
	 * @param address The address to be removed from the ban list
	 * @return true if the ban was removed, false if there was no ban for this ip
	 */
	public boolean removeBanForAddress(InetAddress address)
	{
		return _bannedIps.remove(address) != null;
	}
	
	/**
	 * Remove the specified address from the ban list
	 * @param address The address to be removed from the ban list
	 * @return true if the ban was removed, false if there was no ban for this ip or the address was invalid.
	 */
	public boolean removeBanForAddress(String address)
	{
		try
		{
			return this.removeBanForAddress(InetAddress.getByName(address));
		}
		catch (UnknownHostException e)
		{
			return false;
		}
	}
	
	public SessionKey getKeyForAccount(String account)
	{
		final LoginClient client = _clients.get(account);
		return (client == null) ? null : client.getSessionKey();
	}
	
	public boolean isAccountInAnyGameServer(String account)
	{
		for (GameServerInfo gsi : GameServerManager.getInstance().getRegisteredGameServers().values())
		{
			final GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
				return true;
		}
		return false;
	}
	
	public GameServerInfo getAccountOnGameServer(String account)
	{
		for (GameServerInfo gsi : GameServerManager.getInstance().getRegisteredGameServers().values())
		{
			final GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
				return gsi;
		}
		return null;
	}
	
	public boolean isLoginPossible(LoginClient client, int serverId)
	{
		final GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServers().get(serverId);
		if (gsi == null || !gsi.isAuthed())
			return false;
		
		final boolean canLogin = gsi.canLogin(client);
		if (canLogin && client.getLastServer() != serverId)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(ACCOUNT_LAST_SERVER_UPDATE))
			{
				ps.setInt(1, serverId);
				ps.setString(2, client.getAccount());
				ps.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't set lastServer.", e);
			}
		}
		return canLogin;
	}
	
	public void setAccountAccessLevel(String account, int banLevel)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(ACCOUNT_ACCESS_LEVEL_UPDATE))
		{
			ps.setInt(1, banLevel);
			ps.setString(2, account);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't set access level {} for {}.", e, banLevel, account);
		}
	}
	
	/**
	 * This method returns one of the cached {@link ScrambledKeyPair ScrambledKeyPairs} for communication with Login Clients.
	 * @return a scrambled keypair
	 */
	public ScrambledKeyPair getScrambledRSAKeyPair()
	{
		return Rnd.get(_keyPairs);
	}
	
	private class PurgeThread extends Thread
	{
		public PurgeThread()
		{
			setName("PurgeThread");
		}
		
		@Override
		public void run()
		{
			while (!isInterrupted())
			{
				for (LoginClient client : _clients.values())
				{
					if ((client.getConnectionStartTime() + LOGIN_TIMEOUT) < System.currentTimeMillis())
						client.close(LoginFail.REASON_ACCESS_FAILED);
				}
				
				try
				{
					Thread.sleep(LOGIN_TIMEOUT / 2);
				}
				catch (InterruptedException e)
				{
					return;
				}
			}
		}
	}
	
	private static class SHA1 {
		public static boolean passwordMatches(String password, String hash) throws NoSuchAlgorithmException {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] raw = password.getBytes(StandardCharsets.UTF_8);
			String hashBase64 = Base64.getEncoder().encodeToString(md.digest(raw));
			return hash.equals(hashBase64);
		}

		public static String hash(String password) throws NoSuchAlgorithmException {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] raw = password.getBytes(StandardCharsets.UTF_8);
			return Base64.getEncoder().encodeToString(md.digest(raw));
		}
	}
	
	public static LoginController getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final LoginController INSTANCE = new LoginController();
	}
}
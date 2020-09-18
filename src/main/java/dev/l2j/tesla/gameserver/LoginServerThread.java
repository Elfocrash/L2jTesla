package dev.l2j.tesla.gameserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.loginserver.crypt.NewCrypt;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.network.AttributeType;
import dev.l2j.tesla.commons.network.StatusType;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.FailReason;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.GameClient;
import dev.l2j.tesla.gameserver.network.GameClient.GameClientState;
import dev.l2j.tesla.gameserver.network.gameserverpackets.AuthRequest;
import dev.l2j.tesla.gameserver.network.gameserverpackets.BlowFishKey;
import dev.l2j.tesla.gameserver.network.gameserverpackets.ChangeAccessLevel;
import dev.l2j.tesla.gameserver.network.gameserverpackets.GameServerBasePacket;
import dev.l2j.tesla.gameserver.network.gameserverpackets.PlayerAuthRequest;
import dev.l2j.tesla.gameserver.network.gameserverpackets.PlayerInGame;
import dev.l2j.tesla.gameserver.network.gameserverpackets.PlayerLogout;
import dev.l2j.tesla.gameserver.network.gameserverpackets.ServerStatus;
import dev.l2j.tesla.gameserver.network.loginserverpackets.AuthResponse;
import dev.l2j.tesla.gameserver.network.loginserverpackets.InitLS;
import dev.l2j.tesla.gameserver.network.loginserverpackets.KickPlayer;
import dev.l2j.tesla.gameserver.network.loginserverpackets.LoginServerFail;
import dev.l2j.tesla.gameserver.network.loginserverpackets.PlayerAuthResponse;
import dev.l2j.tesla.gameserver.network.serverpackets.AuthLoginFail;
import dev.l2j.tesla.gameserver.network.serverpackets.CharSelectInfo;

public class LoginServerThread extends Thread
{
	protected static final CLogger LOGGER = new CLogger(LoginServerThread.class.getName());
	
	private static final int REVISION = 0x0102;
	
	private final Map<String, GameClient> _clients = new ConcurrentHashMap<>();
	
	private int _serverId;
	private String _serverName;
	
	private Socket _loginSocket;
	private InputStream _in;
	private OutputStream _out;
	
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;
	private RSAPublicKey _publicKey;
	
	private byte[] _hexId;
	
	private int _requestId;
	private int _maxPlayers;
	private StatusType _status = StatusType.AUTO;
	
	protected LoginServerThread()
	{
		super("LoginServerThread");
		
		_hexId = Config.HEX_ID;
		if (_hexId == null)
		{
			_requestId = Config.REQUEST_ID;
			_hexId = generateHex(16);
		}
		else
			_requestId = Config.SERVER_ID;
		
		_maxPlayers = Config.MAXIMUM_ONLINE_USERS;
	}
	
	@Override
	public void run()
	{
		while (!isInterrupted())
		{
			try
			{
				// Connection
				LOGGER.info("Connecting to login on {}:{}.", Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
				
				_loginSocket = new Socket(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
				_in = _loginSocket.getInputStream();
				_out = new BufferedOutputStream(_loginSocket.getOutputStream());
				
				// init Blowfish
				_blowfishKey = generateHex(40);
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				
				while (!isInterrupted())
				{
					int lengthLo = _in.read();
					int lengthHi = _in.read();
					int length = lengthHi * 256 + lengthLo;
					
					if (lengthHi < 0)
						break;
					
					byte[] incoming = new byte[length - 2];
					
					int receivedBytes = 0;
					int newBytes = 0;
					int left = length - 2;
					
					while (newBytes != -1 && receivedBytes < length - 2)
					{
						newBytes = _in.read(incoming, receivedBytes, left);
						receivedBytes = receivedBytes + newBytes;
						left -= newBytes;
					}
					
					if (receivedBytes != length - 2)
					{
						LOGGER.warn("Incomplete packet is sent to the server, closing connection.");
						break;
					}
					
					// Decrypt if we have a key.
					final byte[] decrypt = _blowfish.decrypt(incoming);
					
					// Verify the checksum.
					if (!NewCrypt.verifyChecksum(decrypt))
					{
						LOGGER.warn("Incorrect packet checksum, ignoring packet.");
						break;
					}
					
					int packetType = decrypt[0] & 0xff;
					switch (packetType)
					{
						case 0x00:
							final InitLS init = new InitLS(decrypt);
							
							if (init.getRevision() != REVISION)
							{
								LOGGER.warn("Revision mismatch between LS and GS.");
								break;
							}
							
							try
							{
								final KeyFactory kfac = KeyFactory.getInstance("RSA");
								final BigInteger modulus = new BigInteger(init.getRSAKey());
								final RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								
								_publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
							}
							catch (GeneralSecurityException e)
							{
								LOGGER.error("Troubles while init the public key sent by login.");
								break;
							}
							
							// send the blowfish key through the rsa encryption
							sendPacket(new BlowFishKey(_blowfishKey, _publicKey));
							
							// now, only accept paket with the new encryption
							_blowfish = new NewCrypt(_blowfishKey);
							
							sendPacket(new AuthRequest(_requestId, Config.ACCEPT_ALTERNATE_ID, _hexId, Config.HOSTNAME, Config.PORT_GAME, Config.RESERVE_HOST_ON_LOGIN, _maxPlayers));
							break;
						
						case 0x01:
							// login will close the connection here
							final LoginServerFail lsf = new LoginServerFail(decrypt);
							LOGGER.info("LoginServer registration failed: {}.", lsf.getReasonString());
							break;
						
						case 0x02:
							final AuthResponse aresp = new AuthResponse(decrypt);
							
							_serverId = aresp.getServerId();
							_serverName = aresp.getServerName();
							
							Config.saveHexid(_serverId, new BigInteger(_hexId).toString(16));
							LOGGER.info("Registered as server: [{}] {}.", _serverId, _serverName);
							
							final ServerStatus ss = new ServerStatus();
							ss.addAttribute(AttributeType.STATUS, (Config.SERVER_GMONLY) ? StatusType.GM_ONLY.getId() : StatusType.AUTO.getId());
							ss.addAttribute(AttributeType.CLOCK, Config.SERVER_LIST_CLOCK);
							ss.addAttribute(AttributeType.BRACKETS, Config.SERVER_LIST_BRACKET);
							ss.addAttribute(AttributeType.AGE_LIMIT, Config.SERVER_LIST_AGE);
							ss.addAttribute(AttributeType.TEST_SERVER, Config.SERVER_LIST_TESTSERVER);
							ss.addAttribute(AttributeType.PVP_SERVER, Config.SERVER_LIST_PVPSERVER);
							sendPacket(ss);
							
							final Collection<Player> players = World.getInstance().getPlayers();
							if (!players.isEmpty())
							{
								final List<String> playerList = new ArrayList<>();
								for (Player player : players)
									playerList.add(player.getAccountName());
								
								sendPacket(new PlayerInGame(playerList));
							}
							break;
						
						case 0x03:
							final PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
							
							final GameClient client = _clients.get(par.getAccount());
							if (client != null)
							{
								if (par.isAuthed())
								{
									sendPacket(new PlayerInGame(par.getAccount()));
									
									client.setState(GameClientState.AUTHED);
									client.sendPacket(new CharSelectInfo(par.getAccount(), client.getSessionId().playOkID1));
								}
								else
								{
									client.sendPacket(new AuthLoginFail(FailReason.SYSTEM_ERROR_LOGIN_LATER));
									client.closeNow();
								}
							}
							break;
						
						case 0x04:
							final KickPlayer kp = new KickPlayer(decrypt);
							kickPlayer(kp.getAccount());
							break;
					}
				}
			}
			catch (UnknownHostException e)
			{
			}
			catch (IOException e)
			{
				LOGGER.error("No connection found with loginserver, next try in 10 seconds.");
			}
			finally
			{
				try
				{
					_loginSocket.close();
					if (isInterrupted())
						return;
				}
				catch (Exception e)
				{
				}
			}
			
			// 10 seconds tempo before another try
			try
			{
				Thread.sleep(10000);
			}
			catch (InterruptedException e)
			{
				return;
			}
		}
	}
	
	public void sendLogout(String account)
	{
		if (account == null)
			return;
		
		try
		{
			sendPacket(new PlayerLogout(account));
		}
		catch (IOException e)
		{
			LOGGER.error("Error while sending logout packet to login.");
		}
		finally
		{
			_clients.remove(account);
		}
	}
	
	public void addClient(String account, GameClient client)
	{
		final GameClient existingClient = _clients.putIfAbsent(account, client);
		if (existingClient == null)
		{
			try
			{
				sendPacket(new PlayerAuthRequest(client.getAccountName(), client.getSessionId()));
			}
			catch (IOException e)
			{
				LOGGER.error("Error while sending player auth request.");
			}
		}
		else
		{
			client.closeNow();
			existingClient.closeNow();
		}
	}
	
	public void sendAccessLevel(String account, int level)
	{
		try
		{
			sendPacket(new ChangeAccessLevel(account, level));
		}
		catch (IOException e)
		{
		}
	}
	
	public void kickPlayer(String account)
	{
		final GameClient client = _clients.get(account);
		if (client != null)
			client.closeNow();
	}
	
	public static byte[] generateHex(int size)
	{
		byte[] array = new byte[size];
		Rnd.nextBytes(array);
		return array;
	}
	
	private void sendPacket(GameServerBasePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		
		data = _blowfish.crypt(data);
		
		int len = data.length + 2;
		synchronized (_out) // avoids tow threads writing in the mean time
		{
			_out.write(len & 0xff);
			_out.write(len >> 8 & 0xff);
			_out.write(data);
			_out.flush();
		}
	}
	
	public void setMaxPlayer(int maxPlayers)
	{
		sendServerStatus(AttributeType.MAX_PLAYERS, maxPlayers);
		
		_maxPlayers = maxPlayers;
	}
	
	public int getMaxPlayers()
	{
		return _maxPlayers;
	}
	
	public void sendServerStatus(AttributeType type, int value)
	{
		try
		{
			final ServerStatus ss = new ServerStatus();
			ss.addAttribute(type, value);
			
			sendPacket(ss);
		}
		catch (IOException e)
		{
		}
	}
	
	public String getServerName()
	{
		return _serverName;
	}
	
	public StatusType getServerStatus()
	{
		return _status;
	}
	
	public void setServerStatus(StatusType status)
	{
		sendServerStatus(AttributeType.STATUS, status.getId());
		
		_status = status;
	}
	
	public static LoginServerThread getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final LoginServerThread INSTANCE = new LoginServerThread();
	}
}
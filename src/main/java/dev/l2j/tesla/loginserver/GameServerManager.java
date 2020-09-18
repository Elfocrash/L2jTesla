package dev.l2j.tesla.loginserver;

import java.math.BigInteger;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.commons.data.xml.IXmlReader;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.random.Rnd;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.loginserver.model.GameServerInfo;

import org.w3c.dom.Document;

public class GameServerManager implements IXmlReader
{
	private static final CLogger LOGGER = new CLogger(GameServerManager.class.getName());
	
	private static final int KEYS_SIZE = 10;
	
	private static final String LOAD_SERVERS = "SELECT * FROM gameservers";
	private static final String ADD_SERVER = "INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)";
	
	private final Map<Integer, String> _serverNames = new HashMap<>();
	private final Map<Integer, GameServerInfo> _registeredServers = new ConcurrentHashMap<>();
	
	private KeyPair[] _keyPairs;
	
	protected GameServerManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("serverNames.xml");
		LOGGER.info("Loaded {} server names.", _serverNames.size());
		
		loadRegisteredGameServers();
		LOGGER.info("Loaded {} registered gameserver(s).", _registeredServers.size());
		
		initRSAKeys();
		LOGGER.info("Cached {} RSA keys for gameserver communication.", _keyPairs.length);
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode ->
		{
			forEach(listNode, "server", serverNode ->
			{
				final StatsSet set = parseAttributes(serverNode);
				_serverNames.put(set.getInteger("id"), set.getString("name"));
			});
		});
	}
	
	private void initRSAKeys()
	{
		try
		{
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4));
			
			_keyPairs = new KeyPair[KEYS_SIZE];
			for (int i = 0; i < KEYS_SIZE; i++)
				_keyPairs[i] = keyGen.genKeyPair();
		}
		catch (Exception e)
		{
			LOGGER.error("Error loading RSA keys for Game Server communication.", e);
		}
	}
	
	private void loadRegisteredGameServers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(LOAD_SERVERS);
             ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final int id = rs.getInt("server_id");
				_registeredServers.put(id, new GameServerInfo(id, stringToHex(rs.getString("hexid"))));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Error loading registered gameservers.", e);
		}
	}
	
	public Map<Integer, GameServerInfo> getRegisteredGameServers()
	{
		return _registeredServers;
	}
	
	public boolean registerWithFirstAvailableId(GameServerInfo gsi)
	{
		for (int id : _serverNames.keySet())
		{
			if (!_registeredServers.containsKey(id))
			{
				_registeredServers.put(id, gsi);
				gsi.setId(id);
				return true;
			}
		}
		return false;
	}
	
	public boolean register(int id, GameServerInfo gsi)
	{
		if (!_registeredServers.containsKey(id))
		{
			_registeredServers.put(id, gsi);
			gsi.setId(id);
			return true;
		}
		return false;
	}
	
	public void registerServerOnDB(GameServerInfo gsi)
	{
		registerServerOnDB(gsi.getHexId(), gsi.getId(), gsi.getHostName());
	}
	
	public void registerServerOnDB(byte[] hexId, int id, String hostName)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(ADD_SERVER))
		{
			ps.setString(1, hexToString(hexId));
			ps.setInt(2, id);
			ps.setString(3, hostName);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Error while saving gameserver data.", e);
		}
	}
	
	public Map<Integer, String> getServerNames()
	{
		return _serverNames;
	}
	
	public KeyPair getKeyPair()
	{
		return Rnd.get(_keyPairs);
	}
	
	private static byte[] stringToHex(String string)
	{
		return new BigInteger(string, 16).toByteArray();
	}
	
	private static String hexToString(byte[] hex)
	{
		return (hex == null) ? "null" : new BigInteger(hex).toString(16);
	}
	
	public static GameServerManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final GameServerManager INSTANCE = new GameServerManager();
	}
}
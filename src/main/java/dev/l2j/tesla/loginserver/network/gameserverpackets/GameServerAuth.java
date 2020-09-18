package dev.l2j.tesla.loginserver.network.gameserverpackets;

import dev.l2j.tesla.loginserver.network.clientpackets.ClientBasePacket;

public class GameServerAuth extends ClientBasePacket
{
	private final byte[] _hexId;
	private final int _desiredId;
	private final boolean _hostReserved;
	private final boolean _acceptAlternativeId;
	private final int _maxPlayers;
	private final int _port;
	private final String _hostName;
	
	public GameServerAuth(byte[] decrypt)
	{
		super(decrypt);
		
		_desiredId = readC();
		_acceptAlternativeId = readC() != 0;
		_hostReserved = readC() != 0;
		_hostName = readS();
		_port = readH();
		_maxPlayers = readD();
		int size = readD();
		_hexId = readB(size);
	}
	
	public byte[] getHexID()
	{
		return _hexId;
	}
	
	public boolean getHostReserved()
	{
		return _hostReserved;
	}
	
	public int getDesiredID()
	{
		return _desiredId;
	}
	
	public boolean acceptAlternateID()
	{
		return _acceptAlternativeId;
	}
	
	public int getMaxPlayers()
	{
		return _maxPlayers;
	}
	
	public String getHostName()
	{
		return _hostName;
	}
	
	public int getPort()
	{
		return _port;
	}
}
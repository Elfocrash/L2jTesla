package dev.l2j.tesla.loginserver.model;

import dev.l2j.tesla.commons.network.StatusType;

public class ServerData
{
	private final StatusType _status;
	private final String _hostName;
	
	private final int _serverId;
	private final int _port;
	private final int _currentPlayers;
	private final int _maxPlayers;
	private final int _ageLimit;
	private final boolean _isPvp;
	private final boolean _isTestServer;
	private final boolean _isShowingBrackets;
	private final boolean _isShowingClock;
	
	public ServerData(StatusType status, String hostName, GameServerInfo gsi)
	{
		_status = status;
		_hostName = hostName;
		
		_serverId = gsi.getId();
		_port = gsi.getPort();
		_currentPlayers = gsi.getCurrentPlayerCount();
		_maxPlayers = gsi.getMaxPlayers();
		_ageLimit = gsi.getAgeLimit();
		_isPvp = gsi.isPvp();
		_isTestServer = gsi.isTestServer();
		_isShowingBrackets = gsi.isShowingBrackets();
		_isShowingClock = gsi.isShowingClock();
	}
	
	public StatusType getStatus()
	{
		return _status;
	}
	
	public String getHostName()
	{
		return _hostName;
	}
	
	public int getServerId()
	{
		return _serverId;
	}
	
	public int getPort()
	{
		return _port;
	}
	
	public int getCurrentPlayers()
	{
		return _currentPlayers;
	}
	
	public int getMaxPlayers()
	{
		return _maxPlayers;
	}
	
	public int getAgeLimit()
	{
		return _ageLimit;
	}
	
	public boolean isPvp()
	{
		return _isPvp;
	}
	
	public boolean isTestServer()
	{
		return _isTestServer;
	}
	
	public boolean isShowingBrackets()
	{
		return _isShowingBrackets;
	}
	
	public boolean isShowingClock()
	{
		return _isShowingClock;
	}
}
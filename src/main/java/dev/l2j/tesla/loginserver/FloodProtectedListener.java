package dev.l2j.tesla.loginserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.Config;

public abstract class FloodProtectedListener extends Thread
{
	private static final CLogger LOGGER = new CLogger(FloodProtectedListener.class.getName());
	
	private final Map<String, ForeignConnection> _flooders = new ConcurrentHashMap<>();
	
	private final ServerSocket _serverSocket;
	
	public FloodProtectedListener(String listenIp, int port) throws IOException
	{
		if (listenIp.equals("*"))
			_serverSocket = new ServerSocket(port);
		else
			_serverSocket = new ServerSocket(port, 50, InetAddress.getByName(listenIp));
	}
	
	public abstract void addClient(Socket s);
	
	@SuppressWarnings("resource")
	@Override
	public void run()
	{
		Socket connection = null;
		while (true)
		{
			try
			{
				connection = _serverSocket.accept();
				if (Config.FLOOD_PROTECTION)
				{
					final String address = connection.getInetAddress().getHostAddress();
					final long currentTime = System.currentTimeMillis();
					
					final ForeignConnection fc = _flooders.get(address);
					if (fc != null)
					{
						fc.attempts += 1;
						if ((fc.attempts > Config.FAST_CONNECTION_LIMIT && (currentTime - fc.lastConnection) < Config.NORMAL_CONNECTION_TIME) || (currentTime - fc.lastConnection) < Config.FAST_CONNECTION_TIME || fc.attempts > Config.MAX_CONNECTION_PER_IP)
						{
							fc.lastConnection = currentTime;
							fc.attempts -= 1;
							
							connection.close();
							
							if (!fc.isFlooding)
								LOGGER.info("Flood detected from {}.", address);
							
							fc.isFlooding = true;
							continue;
						}
						
						// If connection was flooding server but now passed the check.
						if (fc.isFlooding)
						{
							fc.isFlooding = false;
							LOGGER.info("{} isn't considered as flooding anymore.", address);
						}
						
						fc.lastConnection = currentTime;
					}
					else
						_flooders.put(address, new ForeignConnection(currentTime));
				}
				addClient(connection);
			}
			catch (Exception e)
			{
				try
				{
					if (connection != null)
						connection.close();
				}
				catch (Exception e2)
				{
				}
				if (isInterrupted())
				{
					try
					{
						_serverSocket.close();
					}
					catch (IOException io)
					{
						LOGGER.error(io);
					}
					break;
				}
			}
		}
	}
	
	public void removeFloodProtection(String ip)
	{
		if (!Config.FLOOD_PROTECTION)
			return;
		
		final ForeignConnection fc = _flooders.get(ip);
		if (fc != null)
		{
			fc.attempts -= 1;
			
			if (fc.attempts == 0)
				_flooders.remove(ip);
		}
	}
	
	protected static class ForeignConnection
	{
		public int attempts;
		public long lastConnection;
		public boolean isFlooding = false;
		
		public ForeignConnection(long time)
		{
			lastConnection = time;
			attempts = 1;
		}
	}
}
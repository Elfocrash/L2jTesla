package dev.l2j.tesla.gsregistering;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Scanner;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.LoginServerThread;
import dev.l2j.tesla.loginserver.GameServerManager;
import dev.l2j.tesla.loginserver.model.GameServerInfo;

public class GameServerRegister
{
	private static final String DELETE_SERVER = "DELETE FROM gameservers WHERE server_id=?";
	private static final String DELETE_SERVERS = "DELETE FROM gameservers";
	
	private static String _choice;
	
	public static void main(String[] args)
	{
		Config.loadGameServerRegistration();
		
		try (Scanner _scn = new Scanner(System.in))
		{
			System.out.println();
			System.out.println();
			System.out.println("                        L2jTesla gameserver registering");
			System.out.println("                        ____________________________");
			System.out.println();
			System.out.println("OPTIONS : a number : register a server ID, if available and existing on list.");
			System.out.println("          list : get a list of IDs. A '*' means the id is already used.");
			System.out.println("          clean : unregister a specified gameserver.");
			System.out.println("          cleanall : unregister all gameservers.");
			System.out.println("          exit : exit the program.");
			
			while (true)
			{
				System.out.println();
				System.out.print("Your choice? ");
				_choice = _scn.next();
				
				if (_choice.equalsIgnoreCase("list"))
				{
					System.out.println();
					for (Map.Entry<Integer, String> entry : GameServerManager.getInstance().getServerNames().entrySet())
						System.out.println(entry.getKey() + ": " + entry.getValue() + " " + (GameServerManager.getInstance().getRegisteredGameServers().containsKey(entry.getKey()) ? "*" : ""));
				}
				else if (_choice.equalsIgnoreCase("clean"))
				{
					System.out.println();
					
					if (GameServerManager.getInstance().getServerNames().isEmpty())
						System.out.println("No server names available, be sure 'serverNames.xml' is in the LoginServer directory.");
					else
					{
						System.out.println("UNREGISTER a specific server. Here's the current list :");
						for (GameServerInfo entry : GameServerManager.getInstance().getRegisteredGameServers().values())
							System.out.println(entry.getId() + ": " + GameServerManager.getInstance().getServerNames().get(entry.getId()));
						
						System.out.println();
						System.out.print("Your choice? ");
						
						_choice = _scn.next();
						try
						{
							final int id = Integer.parseInt(_choice);
							
							if (!GameServerManager.getInstance().getRegisteredGameServers().containsKey(id))
								System.out.println("This server id isn't used.");
							else
							{
								try (Connection con = L2DatabaseFactory.getInstance().getConnection();
									 PreparedStatement ps = con.prepareStatement(DELETE_SERVER))
								{
									ps.setInt(1, id);
									ps.executeUpdate();
								}
								catch (Exception e)
								{
									System.out.println("SQL error while cleaning registered server: " + e);
								}
								GameServerManager.getInstance().getRegisteredGameServers().remove(id);
								
								System.out.println("You successfully dropped gameserver #" + id + ".");
							}
						}
						catch (NumberFormatException nfe)
						{
							System.out.println("Type a valid server id.");
						}
					}
				}
				else if (_choice.equalsIgnoreCase("cleanall"))
				{
					System.out.println();
					System.out.print("UNREGISTER ALL servers. Are you sure? (y/n) ");
					
					_choice = _scn.next();
					
					if (_choice.equals("y"))
					{
						try (Connection con = L2DatabaseFactory.getInstance().getConnection();
							PreparedStatement ps = con.prepareStatement(DELETE_SERVERS))
						{
							ps.executeUpdate();
						}
						catch (Exception e)
						{
							System.out.println("SQL error while cleaning registered servers: " + e);
						}
						GameServerManager.getInstance().getRegisteredGameServers().clear();
						
						System.out.println("You successfully dropped all registered gameservers.");
					}
					else
						System.out.println("'cleanall' processus has been aborted.");
				}
				else if (_choice.equalsIgnoreCase("exit"))
					System.exit(0);
				else
				{
					try
					{
						System.out.println();
						
						if (GameServerManager.getInstance().getServerNames().isEmpty())
							System.out.println("No server names available, be sure 'serverNames.xml' is in the LoginServer directory.");
						else
						{
							final int id = Integer.parseInt(_choice);
							
							if (GameServerManager.getInstance().getServerNames().get(id) == null)
								System.out.println("No name for server id: " + id + ".");
							else if (GameServerManager.getInstance().getRegisteredGameServers().containsKey(id))
								System.out.println("This server id is already used.");
							else
							{
								byte[] hexId = LoginServerThread.generateHex(16);
								
								GameServerManager.getInstance().getRegisteredGameServers().put(id, new GameServerInfo(id, hexId));
								GameServerManager.getInstance().registerServerOnDB(hexId, id, "");
								Config.saveHexid(id, new BigInteger(hexId).toString(16), "hexid(server " + id + ").txt");
								
								System.out.println("Server registered under 'hexid(server " + id + ").txt'.");
								System.out.println("Put this file in /config gameserver folder and rename it 'hexid.txt'.");
							}
						}
					}
					catch (NumberFormatException nfe)
					{
						System.out.println("Type a number or list|clean|cleanall commands.");
					}
				}
			}
		}
	}
}
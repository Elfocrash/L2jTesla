package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.LoginServerThread;
import dev.l2j.tesla.gameserver.Shutdown;
import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.taskmanager.GameTimeTaskManager;
import dev.l2j.tesla.commons.network.StatusType;
import dev.l2j.tesla.commons.util.SysUtil;

import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class AdminMaintenance implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_server",
		
		"admin_server_shutdown",
		"admin_server_restart",
		"admin_server_abort",
		
		"admin_server_gm_only",
		"admin_server_all",
		"admin_server_max_player",
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_server"))
			sendHtmlForm(activeChar);
		else if (command.startsWith("admin_server_shutdown"))
		{
			try
			{
				Shutdown.getInstance().startShutdown(activeChar, null, Integer.parseInt(command.substring(22)), false);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				sendHtmlForm(activeChar);
			}
		}
		else if (command.startsWith("admin_server_restart"))
		{
			try
			{
				Shutdown.getInstance().startShutdown(activeChar, null, Integer.parseInt(command.substring(21)), true);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				sendHtmlForm(activeChar);
			}
		}
		else if (command.startsWith("admin_server_abort"))
		{
			Shutdown.getInstance().abort(activeChar);
		}
		else if (command.equals("admin_server_gm_only"))
		{
			LoginServerThread.getInstance().setServerStatus(StatusType.GM_ONLY);
			Config.SERVER_GMONLY = true;
			
			activeChar.sendMessage("Server is now setted as GMonly.");
			sendHtmlForm(activeChar);
		}
		else if (command.equals("admin_server_all"))
		{
			LoginServerThread.getInstance().setServerStatus(StatusType.AUTO);
			Config.SERVER_GMONLY = false;
			
			activeChar.sendMessage("Server isn't setted as GMonly anymore.");
			sendHtmlForm(activeChar);
		}
		else if (command.startsWith("admin_server_max_player"))
		{
			try
			{
				final int number = Integer.parseInt(command.substring(24));
				
				LoginServerThread.getInstance().setMaxPlayer(number);
				activeChar.sendMessage("Server maximum player amount is setted to " + number + ".");
				sendHtmlForm(activeChar);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("The parameter must be a valid number.");
			}
		}
		return true;
	}
	
	private static void sendHtmlForm(Player activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/maintenance.htm");
		html.replace("%count%", World.getInstance().getPlayers().size());
		html.replace("%used%", SysUtil.getUsedMemory());
		html.replace("%server_name%", LoginServerThread.getInstance().getServerName());
		html.replace("%status%", LoginServerThread.getInstance().getServerStatus().getName());
		html.replace("%max_players%", LoginServerThread.getInstance().getMaxPlayers());
		html.replace("%time%", GameTimeTaskManager.getInstance().getGameTimeFormated());
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
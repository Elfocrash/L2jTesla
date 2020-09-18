package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.commons.concurrent.ThreadPool;

import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>gm = turns gm mode off for a short period of time (by default 1 minute).</li>
 * </ul>
 */
public class AdminGm implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_gm"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_gm"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			int numberOfMinutes = 1;
			if (st.hasMoreTokens())
			{
				try
				{
					numberOfMinutes = Integer.parseInt(st.nextToken());
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Invalid timer setted for //gm ; default time is used.");
				}
			}
			
			// We keep the previous level to rehabilitate it later.
			final int previousAccessLevel = activeChar.getAccessLevel().getLevel();
			
			activeChar.setAccessLevel(0);
			activeChar.sendMessage("You no longer have GM status, but will be rehabilitated after " + numberOfMinutes + " minutes.");
			
			ThreadPool.schedule(() ->
			{
				if (!activeChar.isOnline())
					return;
				
				activeChar.setAccessLevel(previousAccessLevel);
				activeChar.sendMessage("Your previous access level has been rehabilitated.");
			}, numberOfMinutes * 60000);
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
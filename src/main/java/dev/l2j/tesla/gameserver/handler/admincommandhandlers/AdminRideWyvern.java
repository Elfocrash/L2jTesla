package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class AdminRideWyvern implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_ride",
		"admin_unride",
	};
	
	private int _petRideId;
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_ride"))
		{
			// command disabled if CW is worn. Warn user.
			if (activeChar.isCursedWeaponEquipped())
			{
				activeChar.sendMessage("You can't use //ride owning a Cursed Weapon.");
				return false;
			}
			
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command
			
			if (st.hasMoreTokens())
			{
				String mount = st.nextToken();
				
				if (mount.equals("wyvern") || mount.equals("2"))
					_petRideId = 12621;
				else if (mount.equals("strider") || mount.equals("1"))
					_petRideId = 12526;
				else
				{
					activeChar.sendMessage("Parameter '" + mount + "' isn't recognized for that command.");
					return false;
				}
			}
			else
			{
				activeChar.sendMessage("You must enter a parameter for that command.");
				return false;
			}
			
			// If code reached that place, it means _petRideId has been filled.
			if (activeChar.isMounted())
				activeChar.dismount();
			else if (activeChar.getSummon() != null)
				activeChar.getSummon().unSummon(activeChar);
			
			activeChar.mount(_petRideId, 0);
		}
		else if (command.equals("admin_unride"))
			activeChar.dismount();
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
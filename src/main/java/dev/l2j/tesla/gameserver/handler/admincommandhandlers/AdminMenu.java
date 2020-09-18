package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class AdminMenu implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_char_manage",
		"admin_teleport_character_to_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_char_manage"))
			showMainPage(activeChar);
		else if (command.startsWith("admin_teleport_character_to_menu"))
		{
			String[] data = command.split(" ");
			if (data.length == 5)
			{
				String playerName = data[1];
				Player player = World.getInstance().getPlayer(playerName);
				if (player != null)
					teleportCharacter(player, Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4]), activeChar);
			}
			showMainPage(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void teleportCharacter(Player player, int x, int y, int z, Player activeChar)
	{
		if (player != null)
		{
			player.sendMessage("A GM is teleporting you.");
			player.teleportTo(x, y, z, 0);
		}
		showMainPage(activeChar);
	}
	
	private static void showMainPage(Player activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
	}
}
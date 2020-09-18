package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.serverpackets.BuyList;
import dev.l2j.tesla.gameserver.data.manager.BuyListManager;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.buylist.NpcBuyList;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>gmshop = shows menu</li>
 * <li>buy id = shows shop with respective id</li>
 * </ul>
 */
public class AdminShop implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_buy",
		"admin_gmshop"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_buy"))
		{
			try
			{
				final int val = Integer.parseInt(command.substring(10));
				
				final NpcBuyList list = BuyListManager.getInstance().getBuyList(val);
				if (list == null)
					activeChar.sendMessage("Invalid buylist id.");
				else
					activeChar.sendPacket(new BuyList(list, activeChar.getAdena(), 0));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Invalid buylist id.");
			}
		}
		else if (command.equals("admin_gmshop"))
			AdminHelpPage.showHelpPage(activeChar, "gmshops.htm");
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
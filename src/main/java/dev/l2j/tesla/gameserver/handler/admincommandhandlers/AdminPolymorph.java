package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.enums.PolyType;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * This class handles polymorph commands.
 */
public class AdminPolymorph implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_polymorph",
		"admin_unpolymorph",
		"admin_polymorph_menu",
		"admin_unpolymorph_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (activeChar.isMounted())
			return false;
		
		WorldObject target = activeChar.getTarget();
		if (target == null)
			target = activeChar;
		
		if (command.startsWith("admin_polymorph"))
		{
			try
			{
				final StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				
				PolyType info = PolyType.NPC;
				if (st.countTokens() > 1)
					info = Enum.valueOf(PolyType.class, st.nextToken().toUpperCase());
				
				final int npcId = Integer.parseInt(st.nextToken());
				
				if (!target.polymorph(info, npcId))
				{
					activeChar.sendPacket(SystemMessageId.APPLICANT_INFORMATION_INCORRECT);
					return true;
				}
				
				activeChar.sendMessage("You polymorphed " + target.getName() + " into a " + info + " using id: " + npcId + ".");
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //polymorph <type> <id>");
			}
		}
		else if (command.startsWith("admin_unpolymorph"))
		{
			if (target.getPolyType() == PolyType.DEFAULT)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return true;
			}
			
			target.unpolymorph();
			
			activeChar.sendMessage("You successfully unpolymorphed " + target.getName() + ".");
		}
		
		if (command.contains("menu"))
			AdminHelpPage.showHelpPage(activeChar, "effects_menu.htm");
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
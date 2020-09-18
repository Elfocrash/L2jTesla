package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.util.List;
import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.commons.lang.StringUtil;
import dev.l2j.tesla.commons.math.MathUtil;

import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * Handles visibility over target's knownlist, offering details about current target's vicinity.
 */
public class AdminKnownlist implements IAdminCommandHandler
{
	private static final int PAGE_LIMIT = 15;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_knownlist",
		"admin_knownlist_page",
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_knownlist"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			WorldObject target = null;
			
			// Try to parse the parameter as an int, then try to retrieve an objectId ; if it's a string, search for any player name.
			if (st.hasMoreTokens())
			{
				final String parameter = st.nextToken();
				
				try
				{
					final int objectId = Integer.parseInt(parameter);
					target = World.getInstance().getObject(objectId);
				}
				catch (NumberFormatException nfe)
				{
					target = World.getInstance().getPlayer(parameter);
				}
			}
			
			// If no one is found, pick potential activeChar's target or the activeChar himself.
			if (target == null)
			{
				target = activeChar.getTarget();
				if (target == null)
					target = activeChar;
			}
			
			int page = 1;
			
			if (command.startsWith("admin_knownlist_page") && st.hasMoreTokens())
			{
				try
				{
					page = Integer.parseInt(st.nextToken());
				}
				catch (NumberFormatException nfe)
				{
				}
			}
			
			showKnownlist(activeChar, target, page);
		}
		return true;
	}
	
	private static void showKnownlist(Player activeChar, WorldObject target, int page)
	{
		List<WorldObject> knownlist = target.getKnownType(WorldObject.class);
		
		// Load static Htm.
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/knownlist.htm");
		html.replace("%target%", target.getName());
		html.replace("%size%", knownlist.size());
		
		if (knownlist.isEmpty())
		{
			html.replace("%knownlist%", "<tr><td>No objects in vicinity.</td></tr>");
			html.replace("%pages%", 0);
			activeChar.sendPacket(html);
			return;
		}
		
		final int max = MathUtil.countPagesNumber(knownlist.size(), PAGE_LIMIT);
		if (page > max)
			page = max;
		
		knownlist = knownlist.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, knownlist.size()));
		
		// Generate data.
		final StringBuilder sb = new StringBuilder(knownlist.size() * 150);
		for (WorldObject object : knownlist)
			StringUtil.append(sb, "<tr><td>", object.getName(), "</td><td>", object.getClass().getSimpleName(), "</td></tr>");
		
		html.replace("%knownlist%", sb.toString());
		
		sb.setLength(0);
		
		// End of table, open a new table for pages system.
		for (int i = 0; i < max; i++)
		{
			final int pagenr = i + 1;
			if (page == pagenr)
				StringUtil.append(sb, pagenr, "&nbsp;");
			else
				StringUtil.append(sb, "<a action=\"bypass -h admin_knownlist_page ", target.getObjectId(), " ", pagenr, "\">", pagenr, "</a>&nbsp;");
		}
		
		html.replace("%pages%", sb.toString());
		
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
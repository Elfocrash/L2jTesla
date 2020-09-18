package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.util.List;
import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.commons.lang.StringUtil;
import dev.l2j.tesla.commons.math.MathUtil;

import dev.l2j.tesla.gameserver.data.sql.BookmarkTable;
import dev.l2j.tesla.gameserver.model.Bookmark;
import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * This class handles bookmarks (stored locations for GMs use).<br>
 * A bookmark is registered using //bk name. The book itself is called with //bk without parameter.
 * @author Tryskell
 */
public class AdminBookmark implements IAdminCommandHandler
{
	private static final int PAGE_LIMIT = 15;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_bkpage",
		"admin_bk",
		"admin_delbk"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_bkpage"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command
			
			int page = 1;
			if (st.hasMoreTokens())
				page = Integer.parseInt(st.nextToken());
			
			showBookmarks(activeChar, page);
		}
		else if (command.startsWith("admin_bk"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command
			
			// Save the bookmark on SQL, and call the HTM.
			if (st.hasMoreTokens())
			{
				final String name = st.nextToken();
				
				if (name.length() > 15)
				{
					activeChar.sendMessage("The location name is too long.");
					return true;
				}
				
				if (BookmarkTable.getInstance().isExisting(name, activeChar.getObjectId()))
				{
					activeChar.sendMessage("That location is already existing.");
					return true;
				}
				
				BookmarkTable.getInstance().saveBookmark(name, activeChar);
			}
			
			// Show the HTM.
			showBookmarks(activeChar, 1);
		}
		else if (command.startsWith("admin_delbk"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command
			
			if (st.hasMoreTokens())
			{
				final String name = st.nextToken();
				final int objId = activeChar.getObjectId();
				
				if (!BookmarkTable.getInstance().isExisting(name, objId))
				{
					activeChar.sendMessage("That location doesn't exist.");
					return true;
				}
				BookmarkTable.getInstance().deleteBookmark(name, objId);
			}
			else
				activeChar.sendMessage("The command delbk must be followed by a valid name.");
			
			showBookmarks(activeChar, 1);
		}
		return true;
	}
	
	/**
	 * Show the basic HTM fed with generated data.
	 * @param activeChar The player to make checks on.
	 * @param page The page id to show.
	 */
	private static void showBookmarks(Player activeChar, int page)
	{
		final int objId = activeChar.getObjectId();
		List<Bookmark> bookmarks = BookmarkTable.getInstance().getBookmarks(objId);
		
		// Load static Htm.
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/bk.htm");
		
		if (bookmarks.isEmpty())
		{
			html.replace("%locs%", "<tr><td>No bookmarks are currently registered.</td></tr>");
			activeChar.sendPacket(html);
			return;
		}
		
		final int max = MathUtil.countPagesNumber(bookmarks.size(), PAGE_LIMIT);
		
		bookmarks = bookmarks.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, bookmarks.size()));
		
		// Generate data.
		final StringBuilder sb = new StringBuilder(2000);
		
		for (Bookmark bk : bookmarks)
		{
			final String name = bk.getName();
			final int x = bk.getX();
			final int y = bk.getY();
			final int z = bk.getZ();
			
			StringUtil.append(sb, "<tr><td><a action=\"bypass -h admin_move_to ", x, " ", y, " ", z, "\">", name, " (", x, " ", y, " ", z, ")", "</a></td><td><a action=\"bypass -h admin_delbk ", name, "\">Remove</a></td></tr>");
		}
		html.replace("%locs%", sb.toString());
		
		// Cleanup the sb.
		sb.setLength(0);
		
		// End of table, open a new table for pages system.
		for (int i = 0; i < max; i++)
		{
			final int pagenr = i + 1;
			if (page == pagenr)
				StringUtil.append(sb, pagenr, "&nbsp;");
			else
				StringUtil.append(sb, "<a action=\"bypass -h admin_bkpage ", pagenr, "\">", pagenr, "</a>&nbsp;");
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
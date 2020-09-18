package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.data.xml.MapRegionData.TeleportType;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.model.pledge.Clan;

/**
 * This class handles teleport admin commands
 */
public class AdminTeleport implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_runmod",
		"admin_instant_move",
		"admin_tele",
		"admin_tele_areas",
		"admin_goto",
		"admin_teleportto", // deprecated
		"admin_recall",
		"admin_recall_party",
		"admin_recall_clan",
		"admin_move_to",
		"admin_sendhome"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		// runmod
		if (command.equals("admin_runmod") || command.equals("admin_instant_move"))
			activeChar.setTeleMode(1);
		if (command.equals("admin_runmod tele"))
			activeChar.setTeleMode(2);
		if (command.equals("admin_runmod norm"))
			activeChar.setTeleMode(0);
		
		// teleport via panels
		if (command.equals("admin_tele"))
			AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
		if (command.equals("admin_tele_areas"))
			AdminHelpPage.showHelpPage(activeChar, "tele/other.htm");
		
		// recalls / goto types
		if (command.startsWith("admin_goto") || command.startsWith("admin_teleportto"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
			{
				st.nextToken();
				String plyr = st.nextToken();
				Player player = World.getInstance().getPlayer(plyr);
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
				
				teleportToCharacter(activeChar, player);
			}
		}
		else if (command.startsWith("admin_recall "))
		{
			try
			{
				String targetName = command.substring(13);
				Player player = World.getInstance().getPlayer(targetName);
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
				
				teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ());
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_recall_party"))
		{
			try
			{
				String targetName = command.substring(19);
				Player player = World.getInstance().getPlayer(targetName);
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
				
				final Party party = player.getParty();
				if (party != null)
				{
					for (Player member : party.getMembers())
						teleportCharacter(member, activeChar.getX(), activeChar.getY(), activeChar.getZ());
					
					activeChar.sendMessage("You recall " + player.getName() + "'s party.");
				}
				else
				{
					activeChar.sendMessage("You recall " + player.getName() + ", but he isn't in a party.");
					teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ());
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_recall_clan"))
		{
			try
			{
				String targetName = command.substring(18);
				Player player = World.getInstance().getPlayer(targetName);
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
				
				Clan clan = player.getClan();
				if (clan != null)
				{
					for (Player member : clan.getOnlineMembers())
						teleportCharacter(member, activeChar.getX(), activeChar.getY(), activeChar.getZ());
					
					activeChar.sendMessage("You recall " + player.getName() + "'s clan.");
				}
				else
				{
					activeChar.sendMessage("You recall " + player.getName() + ", but he isn't a clan member.");
					teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ());
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_move_to"))
		{
			try
			{
				String val = command.substring(14);
				teleportTo(activeChar, val);
			}
			catch (Exception e)
			{
				// Case of empty or missing coordinates
				AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
			}
		}
		else if (command.startsWith("admin_sendhome"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
			{
				st.nextToken();
				String plyr = st.nextToken();
				Player player = World.getInstance().getPlayer(plyr);
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
				
				sendHome(player);
			}
			else
			{
				WorldObject target = activeChar.getTarget();
				Player player = null;
				
				// if target isn't a player, select yourself as target
				if (target instanceof Player)
					player = (Player) target;
				else
					player = activeChar;
				
				sendHome(player);
			}
		}
		return true;
	}
	
	private static void sendHome(Player player)
	{
		player.teleportTo(TeleportType.TOWN);
		player.setIsIn7sDungeon(false);
		player.sendMessage("A GM sent you at nearest town.");
	}
	
	private static void teleportTo(Player activeChar, String Cords)
	{
		try
		{
			StringTokenizer st = new StringTokenizer(Cords);
			String x1 = st.nextToken();
			int x = Integer.parseInt(x1);
			String y1 = st.nextToken();
			int y = Integer.parseInt(y1);
			String z1 = st.nextToken();
			int z = Integer.parseInt(z1);
			
			activeChar.getAI().setIntention(IntentionType.IDLE);
			activeChar.teleportTo(x, y, z, 0);
			
			activeChar.sendMessage("You have been teleported to " + Cords + ".");
		}
		catch (NoSuchElementException nsee)
		{
			activeChar.sendMessage("Coordinates you entered as parameter [" + Cords + "] are wrong.");
		}
	}
	
	private static void teleportCharacter(Player player, int x, int y, int z)
	{
		player.getAI().setIntention(IntentionType.IDLE);
		player.teleportTo(x, y, z, 0);
		player.sendMessage("A GM is teleporting you.");
	}
	
	private static void teleportToCharacter(Player activeChar, Player target)
	{
		if (target.getObjectId() == activeChar.getObjectId())
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
		else
		{
			int x = target.getX();
			int y = target.getY();
			int z = target.getZ();
			
			activeChar.getAI().setIntention(IntentionType.IDLE);
			activeChar.teleportTo(x, y, z, 0);
			activeChar.sendMessage("You have teleported to " + target.getName() + ".");
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
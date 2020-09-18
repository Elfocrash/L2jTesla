package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.LoginServerThread;
import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.enums.PunishmentType;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>ban_acc [account_name] = changes account access level to -100 and logs him off. If no account is specified target's account is used.</li>
 * <li>ban_char [char_name] = changes a characters access level to -100 and logs him off. If no character is specified target is used.</li>
 * <li>ban_chat [char_name] [duration] = chat bans a character for the specified duration. If no name is specified the target is chat banned indefinitely.</li>
 * <li>unban_acc [account_name] = changes account access level to 0.</li>
 * <li>unban_char [char_name] = changes specified characters access level to 0.</li>
 * <li>unban_chat [char_name] = lifts chat ban from specified player. If no player name is specified current target is used.</li>
 * <li>jail [char_name] [penalty_time] = jails character. Time specified in minutes. For ever if no time is specified.</li>
 * <li>unjail [char_name] = Unjails player, teleport him to Floran.</li>
 * </ul>
 */
public class AdminBan implements IAdminCommandHandler
{
	private static final Logger LOG = Logger.getLogger(AdminBan.class.getName());
	
	private static final String UPDATE_BAN = "UPDATE characters SET punish_level=?, punish_timer=? WHERE char_name=?";
	private static final String UPDATE_JAIL = "UPDATE characters SET x=-114356, y=-249645, z=-2984, punish_level=?, punish_timer=? WHERE char_name=?";
	private static final String UPDATE_UNJAIL = "UPDATE characters SET x=17836, y=170178, z=-3507, punish_level=0, punish_timer=0 WHERE char_name=?";
	private static final String UPDATE_ACCESS = "UPDATE characters SET accesslevel=? WHERE char_name=?";
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_ban", // returns ban commands
		"admin_ban_acc",
		"admin_ban_char",
		"admin_ban_chat",
		
		"admin_unban", // returns unban commands
		"admin_unban_acc",
		"admin_unban_char",
		"admin_unban_chat",
		
		"admin_jail",
		"admin_unjail"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String player = "";
		int duration = -1;
		Player targetPlayer = null;
		
		// One parameter, player name
		if (st.hasMoreTokens())
		{
			player = st.nextToken();
			targetPlayer = World.getInstance().getPlayer(player);
			
			// Second parameter, duration
			if (st.hasMoreTokens())
			{
				try
				{
					duration = Integer.parseInt(st.nextToken());
				}
				catch (NumberFormatException nfe)
				{
					activeChar.sendMessage("Invalid number format used: " + nfe);
					return false;
				}
			}
		}
		else
		{
			// If there is no name, select target
			if (activeChar.getTarget() != null && activeChar.getTarget() instanceof Player)
				targetPlayer = (Player) activeChar.getTarget();
		}
		
		// Can't ban yourself
		if (targetPlayer != null && targetPlayer.equals(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
			return false;
		}
		
		if (command.startsWith("admin_ban ") || command.equalsIgnoreCase("admin_ban"))
		{
			activeChar.sendMessage("Available ban commands: //ban_acc, //ban_char, //ban_chat");
			return false;
		}
		else if (command.startsWith("admin_ban_acc"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //ban_acc <account_name> (if none, target char's account gets banned).");
				return false;
			}
			
			if (targetPlayer == null)
			{
				LoginServerThread.getInstance().sendAccessLevel(player, -100);
				activeChar.sendMessage("Ban request sent for account " + player + ".");
			}
			else
			{
				targetPlayer.getPunishment().setType(PunishmentType.ACC, 0);
				activeChar.sendMessage(targetPlayer.getAccountName() + " account is now banned.");
			}
		}
		else if (command.startsWith("admin_ban_char"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //ban_char <char_name> (if none, target char is banned)");
				return false;
			}
			
			return changeCharAccessLevel(targetPlayer, player, activeChar, -1);
		}
		else if (command.startsWith("admin_ban_chat"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //ban_chat <char_name> [penalty_minutes]");
				return false;
			}
			
			if (targetPlayer != null)
			{
				if (targetPlayer.getPunishment().getType() != PunishmentType.NONE)
				{
					activeChar.sendMessage(targetPlayer.getName() + " is already jailed or banned.");
					return false;
				}
				
				String banLengthStr = "";
				targetPlayer.getPunishment().setType(PunishmentType.CHAT, duration);
				
				if (duration > 0)
					banLengthStr = " for " + duration + " minutes";
				
				activeChar.sendMessage(targetPlayer.getName() + " is now chat banned" + banLengthStr + ".");
			}
			else
				banChatOfflinePlayer(activeChar, player, duration, true);
		}
		else if (command.startsWith("admin_unban ") || command.equalsIgnoreCase("admin_unban"))
		{
			activeChar.sendMessage("Available unban commands: //unban_acc, //unban_char, //unban_chat");
			return false;
		}
		else if (command.startsWith("admin_unban_acc"))
		{
			if (targetPlayer != null)
			{
				activeChar.sendMessage(targetPlayer.getName() + " is currently online so mustn't be banned.");
				return false;
			}
			
			if (!player.equals(""))
			{
				LoginServerThread.getInstance().sendAccessLevel(player, 0);
				activeChar.sendMessage("Unban request sent for account " + player + ".");
			}
			else
			{
				activeChar.sendMessage("Usage: //unban_acc <account_name>");
				return false;
			}
		}
		else if (command.startsWith("admin_unban_char"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //unban_char <char_name>");
				return false;
			}
			
			if (targetPlayer != null)
			{
				activeChar.sendMessage(targetPlayer.getName() + " is currently online so mustn't be banned.");
				return false;
			}
			
			return changeCharAccessLevel(null, player, activeChar, 0);
		}
		else if (command.startsWith("admin_unban_chat"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //unban_chat <char_name>");
				return false;
			}
			
			if (targetPlayer != null)
			{
				if (targetPlayer.isChatBanned())
				{
					targetPlayer.getPunishment().setType(PunishmentType.NONE, 0);
					activeChar.sendMessage(targetPlayer.getName() + "'s chat ban has been lifted.");
				}
				else
					activeChar.sendMessage(targetPlayer.getName() + " isn't currently chat banned.");
			}
			else
				banChatOfflinePlayer(activeChar, player, 0, false);
		}
		else if (command.startsWith("admin_jail"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes] (if no name is given, selected target is jailed forever).");
				return false;
			}
			
			if (targetPlayer != null)
			{
				targetPlayer.getPunishment().setType(PunishmentType.JAIL, duration);
				activeChar.sendMessage(targetPlayer.getName() + " has been jailed for " + ((duration > 0) ? duration + " minutes." : "ever !"));
			}
			else
				jailOfflinePlayer(activeChar, player, duration);
		}
		else if (command.startsWith("admin_unjail"))
		{
			if (targetPlayer == null && player.equals(""))
			{
				activeChar.sendMessage("Usage: //unjail <charname> (If no name is given target is used).");
				return false;
			}
			
			if (targetPlayer != null)
			{
				targetPlayer.getPunishment().setType(PunishmentType.NONE, 0);
				activeChar.sendMessage(targetPlayer.getName() + " has been unjailed.");
			}
			else
				unjailOfflinePlayer(activeChar, player);
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void banChatOfflinePlayer(Player activeChar, String name, int delay, boolean ban)
	{
		PunishmentType punishement;
		long value = 0;
		
		if (ban)
		{
			punishement = PunishmentType.CHAT;
			value = ((delay > 0) ? delay * 60000L : 60000);
		}
		else
			punishement = PunishmentType.NONE;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_BAN))
		{
			ps.setInt(1, punishement.ordinal());
			ps.setLong(2, value);
			ps.setString(3, name);
			ps.execute();
			
			final int count = ps.getUpdateCount();
			if (count == 0)
				activeChar.sendMessage("Character isn't found.");
			else if (ban)
				activeChar.sendMessage(name + " is chat banned for " + ((delay > 0) ? delay + " minutes." : "ever !"));
			else
				activeChar.sendMessage(name + "'s chat ban has been lifted.");
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "AdminBan.banChatOfflinePlayer :" + e.getMessage(), e);
		}
	}
	
	private static void jailOfflinePlayer(Player activeChar, String name, int delay)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_JAIL))
		{
			ps.setInt(1, PunishmentType.JAIL.ordinal());
			ps.setLong(2, ((delay > 0) ? delay * 60000L : 0));
			ps.setString(3, name);
			ps.execute();
			
			final int count = ps.getUpdateCount();
			if (count == 0)
				activeChar.sendMessage("Character not found!");
			else
				activeChar.sendMessage(name + " has been jailed for " + ((delay > 0) ? delay + " minutes." : "ever!"));
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "AdminBan.jailOfflinePlayer :" + e.getMessage(), e);
		}
	}
	
	private static void unjailOfflinePlayer(Player activeChar, String name)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_UNJAIL))
		{
			ps.setString(1, name);
			ps.execute();
			
			final int count = ps.getUpdateCount();
			if (count == 0)
				activeChar.sendMessage("Character isn't found.");
			else
				activeChar.sendMessage(name + " has been unjailed.");
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "AdminBan.unjailOfflinePlayer :" + e.getMessage(), e);
		}
	}
	
	private static boolean changeCharAccessLevel(Player targetPlayer, String player, Player activeChar, int lvl)
	{
		if (targetPlayer != null)
		{
			targetPlayer.setAccessLevel(lvl);
			targetPlayer.logout(false);
			activeChar.sendMessage(targetPlayer.getName() + " has been banned.");
		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(UPDATE_ACCESS))
			{
				ps.setInt(1, lvl);
				ps.setString(2, player);
				ps.execute();
				
				final int count = ps.getUpdateCount();
				if (count == 0)
				{
					activeChar.sendMessage("Character not found or access level unaltered.");
					return false;
				}
				
				activeChar.sendMessage(player + " now has an access level of " + lvl + ".");
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, "AdminBan.changeCharAccessLevel :" + e.getMessage(), e);
				return false;
			}
		}
		return true;
	}
}
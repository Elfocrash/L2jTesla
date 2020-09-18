package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.GMViewPledgeInfo;
import dev.l2j.tesla.gameserver.data.sql.ClanTable;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.pledge.Clan;

/**
 * This handler handles pledge commands.<br>
 * <br>
 * With any player target:
 * <ul>
 * <li>//pledge create <b>String</b></li>
 * </ul>
 * With clan member target:
 * <ul>
 * <li>//pledge info</li>
 * <li>//pledge dismiss</li>
 * <li>//pledge setlevel <b>int</b></li>
 * <li>//pledge rep <b>int</b></li>
 * </ul>
 */
public class AdminPledge implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_pledge"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final WorldObject target = activeChar.getTarget();
		if (!(target instanceof Player))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			showMainPage(activeChar);
			return false;
		}
		
		if (command.startsWith("admin_pledge"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				final String action = st.nextToken();
				final Player player = (Player) target;
				
				if (action.equals("create"))
				{
					try
					{
						final String parameter = st.nextToken();
						
						long cet = player.getClanCreateExpiryTime();
						player.setClanCreateExpiryTime(0);
						Clan clan = ClanTable.getInstance().createClan(player, parameter);
						if (clan != null)
							activeChar.sendMessage("Clan " + parameter + " have been created. Clan leader is " + player.getName() + ".");
						else
						{
							player.setClanCreateExpiryTime(cet);
							activeChar.sendMessage("There was a problem while creating the clan.");
						}
					}
					catch (Exception e)
					{
						activeChar.sendMessage("Invalid string parameter for //pledge create.");
					}
				}
				else
				{
					if (player.getClan() == null)
					{
						activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
						showMainPage(activeChar);
						return false;
					}
					
					if (action.equals("dismiss"))
					{
						ClanTable.getInstance().destroyClan(player.getClan());
						
						if (player.getClan() == null)
							activeChar.sendMessage("The clan is now disbanded.");
						else
							activeChar.sendMessage("There was a problem while destroying the clan.");
					}
					else if (action.equals("info"))
						activeChar.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
					else if (action.equals("setlevel"))
					{
						try
						{
							final int level = Integer.parseInt(st.nextToken());
							
							if (level >= 0 && level < 9)
							{
								player.getClan().changeLevel(level);
								activeChar.sendMessage("You have set clan " + player.getClan().getName() + " to level " + level);
							}
							else
								activeChar.sendMessage("This clan level is incorrect. Put a number between 0 and 8.");
						}
						catch (Exception e)
						{
							activeChar.sendMessage("Invalid number parameter for //pledge setlevel.");
						}
					}
					else if (action.startsWith("rep"))
					{
						try
						{
							final int points = Integer.parseInt(st.nextToken());
							final Clan clan = player.getClan();
							
							if (clan.getLevel() < 5)
							{
								activeChar.sendMessage("Only clans of level 5 or above may receive reputation points.");
								showMainPage(activeChar);
								return false;
							}
							
							clan.addReputationScore(points);
							activeChar.sendMessage("You " + (points > 0 ? "added " : "removed ") + Math.abs(points) + " points " + (points > 0 ? "to " : "from ") + clan.getName() + "'s reputation. Their current score is: " + clan.getReputationScore());
						}
						catch (Exception e)
						{
							activeChar.sendMessage("Invalid number parameter for //pledge rep.");
						}
					}
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Invalid action or parameter.");
			}
		}
		showMainPage(activeChar);
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void showMainPage(Player activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "game_menu.htm");
	}
}
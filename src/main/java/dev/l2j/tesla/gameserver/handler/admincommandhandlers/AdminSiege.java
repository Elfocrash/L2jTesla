package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.SiegeInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.data.manager.CastleManager;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.entity.Castle;
import dev.l2j.tesla.gameserver.model.location.TowerSpawnLocation;

/**
 * This class handles all siege commands
 */
public class AdminSiege implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_siege",
		"admin_add_attacker",
		"admin_add_defender",
		"admin_list_siege_clans",
		"admin_clear_siege_list",
		"admin_move_defenders",
		"admin_spawn_doors",
		"admin_endsiege",
		"admin_startsiege",
		"admin_setcastle",
		"admin_removecastle",
		"admin_reset_certificates"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		try
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			command = st.nextToken(); // Get actual command
			
			// Get castle
			Castle castle = null;
			if (st.hasMoreTokens())
				castle = CastleManager.getInstance().getCastleByName(st.nextToken());
			
			if (castle == null)
			{
				showCastleSelectPage(activeChar);
				return true;
			}
			
			WorldObject target = activeChar.getTarget();
			Player targetPlayer = null;
			if (target instanceof Player)
				targetPlayer = (Player) target;
			
			if (command.equalsIgnoreCase("admin_add_attacker"))
			{
				if (targetPlayer == null)
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				else
					castle.getSiege().registerAttacker(targetPlayer);
			}
			else if (command.equalsIgnoreCase("admin_add_defender"))
			{
				if (targetPlayer == null)
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				else
					castle.getSiege().registerDefender(targetPlayer);
			}
			else if (command.equalsIgnoreCase("admin_clear_siege_list"))
			{
				castle.getSiege().clearAllClans();
			}
			else if (command.equalsIgnoreCase("admin_endsiege"))
			{
				castle.getSiege().endSiege();
			}
			else if (command.equalsIgnoreCase("admin_list_siege_clans"))
			{
				activeChar.sendPacket(new SiegeInfo(castle));
				return true;
			}
			else if (command.equalsIgnoreCase("admin_move_defenders"))
			{
				activeChar.sendPacket(SystemMessage.sendString("Not implemented yet."));
			}
			else if (command.equalsIgnoreCase("admin_setcastle"))
			{
				if (targetPlayer == null || targetPlayer.getClan() == null)
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				else if (targetPlayer.getClan().hasCastle())
					activeChar.sendMessage(targetPlayer.getName() + "'s clan already owns a castle.");
				else
					castle.setOwner(targetPlayer.getClan());
			}
			else if (command.equalsIgnoreCase("admin_removecastle"))
			{
				if (castle.getOwnerId() > 0)
					castle.removeOwner();
				else
					activeChar.sendMessage("This castle does not have an owner.");
			}
			else if (command.equalsIgnoreCase("admin_spawn_doors"))
			{
				castle.spawnDoors(false);
			}
			else if (command.equalsIgnoreCase("admin_startsiege"))
			{
				castle.getSiege().startSiege();
			}
			else if (command.equalsIgnoreCase("admin_reset_certificates"))
			{
				castle.setLeftCertificates(300, true);
			}
			
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/admin/castle.htm");
			html.replace("%castleName%", castle.getName());
			html.replace("%circletId%", castle.getCircletId());
			html.replace("%artifactId%", castle.getArtifacts().toString());
			html.replace("%ticketsNumber%", castle.getTickets().size());
			html.replace("%droppedTicketsNumber%", castle.getDroppedTickets().size());
			html.replace("%npcsNumber%", castle.getRelatedNpcIds().size());
			html.replace("%certificates%", castle.getLeftCertificates());
			
			final StringBuilder sb = new StringBuilder();
			
			// Feed Control Tower infos.
			for (TowerSpawnLocation spawn : castle.getControlTowers())
			{
				final String teleLoc = spawn.toString().replaceAll(",", "");
				StringUtil.append(sb, "<a action=\"bypass -h admin_move_to ", teleLoc, "\">", teleLoc, "</a><br1>");
			}
			
			html.replace("%ct%", sb.toString());
			
			// Cleanup the sb to reuse it.
			sb.setLength(0);
			
			// Feed Flame Tower infos.
			for (TowerSpawnLocation spawn : castle.getFlameTowers())
			{
				final String teleLoc = spawn.toString().replaceAll(",", "");
				StringUtil.append(sb, "<a action=\"bypass -h admin_move_to ", teleLoc, "\">", teleLoc, "</a><br1>");
			}
			
			html.replace("%ft%", sb.toString());
			
			activeChar.sendPacket(html);
		}
		catch (Exception e)
		{
			
		}
		return true;
	}
	
	private static void showCastleSelectPage(Player player)
	{
		int i = 0;
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/castles.htm");
		
		final StringBuilder sb = new StringBuilder();
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle != null)
			{
				StringUtil.append(sb, "<td fixwidth=90><a action=\"bypass -h admin_siege ", castle.getName(), "\">", castle.getName(), "</a></td>");
				i++;
			}
			
			if (i > 2)
			{
				sb.append("</tr><tr>");
				i = 0;
			}
		}
		html.replace("%castles%", sb.toString());
		player.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
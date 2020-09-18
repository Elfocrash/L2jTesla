package dev.l2j.tesla.gameserver.handler.usercommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.handler.IUserCommandHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.pledge.Clan;

public class ClanWarsList implements IUserCommandHandler
{
	private static final String SELECT_ATTACK_LIST = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 NOT IN (SELECT clan1 FROM clan_wars WHERE clan2=?)";
	private static final String SELECT_UNDER_ATTACK_LIST = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan2=? AND clan_id=clan1 AND clan1 NOT IN (SELECT clan2 FROM clan_wars WHERE clan1=?)";
	private static final String SELECT_WAR_LIST = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 IN (SELECT clan1 FROM clan_wars WHERE clan2=?)";
	
	private static final int[] COMMAND_IDS =
	{
		88,
		89,
		90
	};
	
	@Override
	public boolean useUserCommand(int id, Player player)
	{
		final Clan clan = player.getClan();
		if (clan == null)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement((id == 88) ? SELECT_ATTACK_LIST : ((id == 89) ? SELECT_UNDER_ATTACK_LIST : SELECT_WAR_LIST)))
		{
			ps.setInt(1, clan.getClanId());
			ps.setInt(2, clan.getClanId());
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.first())
				{
					if (id == 88)
						player.sendPacket(SystemMessageId.CLANS_YOU_DECLARED_WAR_ON);
					else if (id == 89)
						player.sendPacket(SystemMessageId.CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU);
					else
						player.sendPacket(SystemMessageId.WAR_LIST);
					
					SystemMessage sm;
					while (rs.next())
					{
						final String clanName = rs.getString("clan_name");
						if (rs.getInt("ally_id") > 0)
							sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ALLIANCE).addString(clanName).addString(rs.getString("ally_name"));
						else
							sm = SystemMessage.getSystemMessage(SystemMessageId.S1_NO_ALLI_EXISTS).addString(clanName);
						
						player.sendPacket(sm);
					}
					
					player.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
				}
				else
				{
					if (id == 88)
						player.sendPacket(SystemMessageId.YOU_ARENT_IN_CLAN_WARS);
					else if (id == 89)
						player.sendPacket(SystemMessageId.NO_CLAN_WARS_VS_YOU);
					else if (id == 90)
						player.sendPacket(SystemMessageId.NOT_INVOLVED_IN_WAR);
				}
			}
		}
		catch (Exception e)
		{
		}
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
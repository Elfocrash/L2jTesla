package dev.l2j.tesla.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.FriendList;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.model.actor.Player;

public final class RequestAnswerFriendInvite extends L2GameClientPacket
{
	private static final String ADD_FRIEND = "INSERT INTO character_friends (char_id, friend_id) VALUES (?,?), (?,?)";
	
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		final Player requestor = player.getActiveRequester();
		if (requestor == null)
			return;
		
		if (_response == 1)
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);
			
			// Player added to your friendlist
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED_TO_FRIENDS).addCharName(player));
			requestor.getFriendList().add(player.getObjectId());
			
			// has joined as friend.
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_AS_FRIEND).addCharName(requestor));
			player.getFriendList().add(requestor.getObjectId());
			
			// update friendLists *heavy method*
			requestor.sendPacket(new FriendList(requestor));
			player.sendPacket(new FriendList(player));
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(ADD_FRIEND))
			{
				ps.setInt(1, requestor.getObjectId());
				ps.setInt(2, player.getObjectId());
				ps.setInt(3, player.getObjectId());
				ps.setInt(4, requestor.getObjectId());
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't add friendId {} for {}.", e, player.getObjectId(), requestor.toString());
			}
		}
		else
			requestor.sendPacket(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
		
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
}
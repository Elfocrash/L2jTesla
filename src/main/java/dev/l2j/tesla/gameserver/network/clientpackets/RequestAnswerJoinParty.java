package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.network.serverpackets.ExManagePartyRoomMember;
import dev.l2j.tesla.gameserver.network.serverpackets.JoinParty;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.model.partymatching.PartyMatchRoom;
import dev.l2j.tesla.gameserver.model.partymatching.PartyMatchRoomList;

public final class RequestAnswerJoinParty extends L2GameClientPacket
{
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
		
		requestor.sendPacket(new JoinParty(_response));
		
		Party party = requestor.getParty();
		if (_response == 1)
		{
			if (party == null)
				party = new Party(requestor, player, requestor.getLootRule());
			else
				party.addPartyMember(player);
			
			if (requestor.isInPartyMatchRoom())
			{
				final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
				if (list != null)
				{
					final PartyMatchRoom room = list.getPlayerRoom(requestor);
					if (room != null)
					{
						if (player.isInPartyMatchRoom())
						{
							if (list.getPlayerRoomId(requestor) == list.getPlayerRoomId(player))
							{
								final ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
								for (Player member : room.getPartyMembers())
									member.sendPacket(packet);
							}
						}
						else
						{
							room.addMember(player);
							
							final ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
							for (Player member : room.getPartyMembers())
								member.sendPacket(packet);
							
							player.setPartyRoom(room.getId());
							player.broadcastUserInfo();
						}
					}
				}
			}
		}
		
		// Must be kept out of "ok" answer, can't be merged with higher content.
		if (party != null)
			party.setPendingInvitation(false);
		
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
}
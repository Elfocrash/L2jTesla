package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.SendTradeDone;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;

public final class AnswerTradeRequest extends L2GameClientPacket
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
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final Player partner = player.getActiveRequester();
		if (partner == null || World.getInstance().getPlayer(partner.getObjectId()) == null)
		{
			// Trade partner not found, cancel trade
			player.sendPacket(new SendTradeDone(0));
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.setActiveRequester(null);
			return;
		}
		
		if (_response == 1 && !partner.isRequestExpired())
			player.startTrade(partner);
		else
			partner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DENIED_TRADE_REQUEST).addCharName(player));
		
		// Clears requesting status
		player.setActiveRequester(null);
		partner.onTransactionResponse();
	}
}
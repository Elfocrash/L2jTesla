package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.EnchantResult;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.tradelist.TradeList;

public final class TradeDone extends L2GameClientPacket
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
		
		final TradeList trade = player.getActiveTradeList();
		if (trade == null)
			return;
		
		if (trade.isLocked())
			return;
		
		if (_response != 1)
		{
			player.cancelActiveTrade();
			return;
		}
		
		// Trade owner not found, or owner is different of packet sender.
		final Player owner = trade.getOwner();
		if (owner == null || !owner.equals(player))
			return;
		
		// Trade partner not found, cancel trade
		final Player partner = trade.getPartner();
		if (partner == null || World.getInstance().getPlayer(partner.getObjectId()) == null)
		{
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.cancelActiveTrade();
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			player.cancelActiveTrade();
			return;
		}
		
		// Sender under enchant process, close it.
		if (owner.getActiveEnchantItem() != null)
		{
			owner.setActiveEnchantItem(null);
			owner.sendPacket(EnchantResult.CANCELLED);
			owner.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
		}
		
		// Partner under enchant process, close it.
		if (partner.getActiveEnchantItem() != null)
		{
			partner.setActiveEnchantItem(null);
			partner.sendPacket(EnchantResult.CANCELLED);
			partner.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
		}
		
		trade.confirm();
	}
}
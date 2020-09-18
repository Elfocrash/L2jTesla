package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.HennaInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.UserInfo;
import dev.l2j.tesla.gameserver.data.xml.HennaData;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.Henna;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

public final class RequestHennaEquip extends L2GameClientPacket
{
	private int _symbolId;
	
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		final Henna henna = HennaData.getInstance().getHenna(_symbolId);
		if (henna == null)
			return;
		
		if (!henna.canBeUsedBy(player))
		{
			player.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
			return;
		}
		
		if (player.getHennaList().isFull())
		{
			player.sendPacket(SystemMessageId.SYMBOLS_FULL);
			return;
		}
		
		final ItemInstance ownedDyes = player.getInventory().getItemByItemId(henna.getDyeId());
		final int count = (ownedDyes == null) ? 0 : ownedDyes.getCount();
		
		if (count < Henna.DRAW_AMOUNT)
		{
			player.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
			return;
		}
		
		// reduceAdena sends a message.
		if (!player.reduceAdena("Henna", henna.getDrawPrice(), player.getCurrentFolk(), true))
			return;
		
		// destroyItemByItemId sends a message.
		if (!player.destroyItemByItemId("Henna", henna.getDyeId(), Henna.DRAW_AMOUNT, player, true))
			return;
		
		final boolean success = player.getHennaList().add(henna);
		if (success)
		{
			player.sendPacket(new HennaInfo(player));
			player.sendPacket(new UserInfo(player));
			player.sendPacket(SystemMessageId.SYMBOL_ADDED);
		}
	}
}
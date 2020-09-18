package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.kind.Item;

/**
 * d h (h dddhh dhhh)
 */
public class TradeStart extends L2GameServerPacket
{
	private final Player _activeChar;
	private final ItemInstance[] _itemList;
	
	public TradeStart(Player player)
	{
		_activeChar = player;
		_itemList = player.getInventory().getAvailableItems(true, false);
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_activeChar.getActiveTradeList() == null || _activeChar.getActiveTradeList().getPartner() == null)
			return;
		
		writeC(0x1E);
		writeD(_activeChar.getActiveTradeList().getPartner().getObjectId());
		writeH(_itemList.length);
		
		for (ItemInstance temp : _itemList)
		{
			if (temp == null || temp.getItem() == null)
				continue;
			
			Item item = temp.getItem();
			
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(item.getType2());
			writeH(temp.getCustomType1());
			writeD(item.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeH(0x00);
		}
	}
}
package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.List;
import java.util.Set;

import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.tradelist.TradeItem;
import dev.l2j.tesla.gameserver.model.tradelist.TradeList;

public class TradeItemUpdate extends L2GameServerPacket
{
	private final Set<ItemInstance> _items;
	private final List<TradeItem> _currentTrade;
	
	public TradeItemUpdate(TradeList trade, Player activeChar)
	{
		_items = activeChar.getInventory().getItems();
		_currentTrade = trade.getItems();
	}
	
	private int getItemCount(int objectId)
	{
		for (ItemInstance item : _items)
			if (item.getObjectId() == objectId)
				return item.getCount();
			
		return 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x74);
		writeH(_currentTrade.size());
		
		for (TradeItem item : _currentTrade)
		{
			int availableCount = getItemCount(item.getObjectId()) - item.getCount();
			boolean stackable = item.getItem().isStackable();
			
			if (availableCount == 0)
			{
				availableCount = 1;
				stackable = false;
			}
			
			writeH(stackable ? 3 : 2);
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(availableCount);
			writeH(item.getItem().getType2());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchant());
			writeH(0x00);
			writeH(0x00);
		}
	}
}
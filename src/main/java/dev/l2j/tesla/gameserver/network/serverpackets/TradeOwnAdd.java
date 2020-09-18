package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.tradelist.TradeItem;

public class TradeOwnAdd extends L2GameServerPacket
{
	private final TradeItem _item;
	
	public TradeOwnAdd(TradeItem item)
	{
		_item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x20);
		
		writeH(1); // item count
		
		writeH(_item.getItem().getType1());
		writeD(_item.getObjectId());
		writeD(_item.getItem().getItemId());
		writeD(_item.getCount());
		writeH(_item.getItem().getType2());
		writeH(0x00); // ?
		
		writeD(_item.getItem().getBodyPart());
		writeH(_item.getEnchant());
		writeH(0x00); // ?
		writeH(0x00);
	}
}
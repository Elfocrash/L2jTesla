package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.List;

import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.craft.ManufactureItem;
import dev.l2j.tesla.gameserver.model.craft.ManufactureList;

public class RecipeShopSellList extends L2GameServerPacket
{
	private final Player _buyer;
	private final Player _manufacturer;
	
	public RecipeShopSellList(Player buyer, Player manufacturer)
	{
		_buyer = buyer;
		_manufacturer = manufacturer;
	}
	
	@Override
	protected final void writeImpl()
	{
		final ManufactureList createList = _manufacturer.getCreateList();
		if (createList != null)
		{
			writeC(0xd9);
			writeD(_manufacturer.getObjectId());
			writeD((int) _manufacturer.getCurrentMp());
			writeD(_manufacturer.getMaxMp());
			writeD(_buyer.getAdena());
			
			final List<ManufactureItem> list = createList.getList();
			writeD(list.size());
			
			for (ManufactureItem item : list)
			{
				writeD(item.getId());
				writeD(0x00); // unknown
				writeD(item.getValue());
			}
		}
	}
}
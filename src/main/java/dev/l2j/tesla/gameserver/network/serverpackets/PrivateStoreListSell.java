package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.List;

import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.tradelist.TradeItem;

public class PrivateStoreListSell extends L2GameServerPacket
{
	private final int _playerAdena;
	private final Player _storePlayer;
	private final List<TradeItem> _items;
	private final boolean _packageSale;
	
	public PrivateStoreListSell(Player player, Player storePlayer)
	{
		_playerAdena = player.getAdena();
		_storePlayer = storePlayer;
		_items = _storePlayer.getSellList().getItems();
		_packageSale = _storePlayer.getSellList().isPackaged();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9b);
		writeD(_storePlayer.getObjectId());
		writeD(_packageSale ? 1 : 0);
		writeD(_playerAdena);
		writeD(_items.size());
		
		for (TradeItem item : _items)
		{
			writeD(item.getItem().getType2());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getCount());
			writeH(0x00);
			writeH(item.getEnchant());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeD(item.getPrice()); // your price
			writeD(item.getItem().getReferencePrice()); // store price
		}
	}
}
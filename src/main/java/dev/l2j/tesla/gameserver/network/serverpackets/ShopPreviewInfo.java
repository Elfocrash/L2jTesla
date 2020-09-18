package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.Map;

import dev.l2j.tesla.gameserver.model.itemcontainer.Inventory;

/**
 * <font color="red">This packet still need more work. Main items have all been identified.</font><br>
 * <br>
 * Calls the wearlist ("try on" option), and sends items in good paperdoll slot.
 * @author Gnacik, Tk
 */
public class ShopPreviewInfo extends L2GameServerPacket
{
	private final Map<Integer, Integer> _itemlist;
	
	public ShopPreviewInfo(Map<Integer, Integer> itemlist)
	{
		_itemlist = itemlist;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xf0);
		writeD(Inventory.PAPERDOLL_TOTALSLOTS);
		// Slots
		writeD(getFromList(Inventory.PAPERDOLL_REAR)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_LEAR)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_NECK)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_RFINGER)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_LFINGER)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_HEAD)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_RHAND)); // good
		writeD(getFromList(Inventory.PAPERDOLL_LHAND)); // good
		writeD(getFromList(Inventory.PAPERDOLL_GLOVES)); // good
		writeD(getFromList(Inventory.PAPERDOLL_CHEST)); // good
		writeD(getFromList(Inventory.PAPERDOLL_LEGS)); // good
		writeD(getFromList(Inventory.PAPERDOLL_FEET)); // good
		writeD(getFromList(Inventory.PAPERDOLL_BACK)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_FACE)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_HAIR)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_HAIRALL)); // unverified
		writeD(getFromList(Inventory.PAPERDOLL_UNDER)); // unverified
	}
	
	private int getFromList(int key)
	{
		return ((_itemlist.get(key) != null) ? _itemlist.get(key) : 0);
	}
}
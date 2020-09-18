package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

public class SpawnItem extends L2GameServerPacket
{
	private final int _objectId;
	private final int _itemId;
	private final int _x, _y, _z;
	private final int _stackable, _count;
	
	public SpawnItem(ItemInstance item)
	{
		_objectId = item.getObjectId();
		_itemId = item.getItemId();
		_x = item.getX();
		_y = item.getY();
		_z = item.getZ();
		_stackable = item.isStackable() ? 0x01 : 0x00;
		_count = item.getCount();
	}
	
	public SpawnItem(WorldObject object)
	{
		_objectId = object.getObjectId();
		_itemId = object.getPolyId();
		_x = object.getX();
		_y = object.getY();
		_z = object.getZ();
		_stackable = 0x00;
		_count = 1;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0b);
		writeD(_objectId);
		writeD(_itemId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_stackable);
		writeD(_count);
		writeD(0x00); // c2
	}
}
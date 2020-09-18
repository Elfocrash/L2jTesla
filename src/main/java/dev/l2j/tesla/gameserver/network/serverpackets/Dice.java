package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.actor.Player;

public class Dice extends L2GameServerPacket
{
	private final int _objectId;
	private final int _itemId;
	private final int _number;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public Dice(Player player, int itemId, int number)
	{
		_objectId = player.getObjectId();
		_itemId = itemId;
		_number = number;
		_x = player.getX() - 30;
		_y = player.getY() - 30;
		_z = player.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xD4);
		writeD(_objectId);
		writeD(_itemId);
		writeD(_number);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.actor.instance.Door;

public class DoorStatusUpdate extends L2GameServerPacket
{
	private final Door _door;
	private final boolean _showHp;
	
	public DoorStatusUpdate(Door door)
	{
		_door = door;
		_showHp = door.getCastle() != null && door.getCastle().getSiege().isInProgress();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4d);
		writeD(_door.getObjectId());
		writeD(_door.isOpened() ? 0 : 1);
		writeD(_door.getDamage());
		writeD((_showHp) ? 1 : 0);
		writeD(_door.getDoorId());
		writeD(_door.getMaxHp());
		writeD((int) _door.getCurrentHp());
	}
}
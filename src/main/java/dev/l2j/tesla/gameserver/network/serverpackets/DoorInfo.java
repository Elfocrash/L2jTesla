package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.actor.instance.Door;

public class DoorInfo extends L2GameServerPacket
{
	private final Door _door;
	private final boolean _showHp;
	
	public DoorInfo(Door door)
	{
		_door = door;
		_showHp = door.getCastle() != null && door.getCastle().getSiege().isInProgress();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4c);
		writeD(_door.getObjectId());
		writeD(_door.getDoorId());
		writeD((_showHp) ? 1 : 0);
		writeD(1); // ??? (can target)
		writeD(_door.isOpened() ? 0 : 1);
		writeD(_door.getMaxHp());
		writeD((int) _door.getCurrentHp());
		writeD(0); // ??? (show HP)
		writeD(0); // ??? (Damage)
	}
}
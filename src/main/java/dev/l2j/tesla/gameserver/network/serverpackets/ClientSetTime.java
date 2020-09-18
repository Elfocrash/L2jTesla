package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.taskmanager.GameTimeTaskManager;

public class ClientSetTime extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(0xEC);
		writeD(GameTimeTaskManager.getInstance().getGameTime());
		writeD(6);
	}
}
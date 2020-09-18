package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.location.Location;

public class ObservationReturn extends L2GameServerPacket
{
	private final Location _location;
	
	public ObservationReturn(Location loc)
	{
		_location = loc;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe0);
		writeD(_location.getX());
		writeD(_location.getY());
		writeD(_location.getZ());
	}
}
package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.location.SpawnLocation;

public class GetOnVehicle extends L2GameServerPacket
{
	private final int _objectId;
	private final int _boatId;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public GetOnVehicle(int objectId, int boatId, int x, int y, int z)
	{
		_objectId = objectId;
		_boatId = boatId;
		_x = x;
		_y = y;
		_z = z;
	}
	
	public GetOnVehicle(int objectId, int boatId, SpawnLocation loc)
	{
		_objectId = objectId;
		_boatId = boatId;
		_x = loc.getX();
		_y = loc.getY();
		_z = loc.getZ();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x5C);
		writeD(_objectId);
		writeD(_boatId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
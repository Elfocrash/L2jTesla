package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * @author Maktakien
 */
public class GetOffVehicle extends L2GameServerPacket
{
	private final int _charObjId, _boatObjId, _x, _y, _z;
	
	public GetOffVehicle(int charObjId, int boatObjId, int x, int y, int z)
	{
		_charObjId = charObjId;
		_boatObjId = boatObjId;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x5D);
		writeD(_charObjId);
		writeD(_boatObjId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
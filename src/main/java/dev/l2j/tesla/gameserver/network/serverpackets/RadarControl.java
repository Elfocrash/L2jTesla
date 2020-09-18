package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * 0xEB RadarControl ddddd
 */
public class RadarControl extends L2GameServerPacket
{
	private final int _showRadar;
	private final int _type;
	private final int _x, _y, _z;
	
	public RadarControl(int showRadar, int type, int x, int y, int z)
	{
		_showRadar = showRadar; // 0 = showradar; 1 = delete radar;
		_type = type;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xEB);
		writeD(_showRadar);
		writeD(_type);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
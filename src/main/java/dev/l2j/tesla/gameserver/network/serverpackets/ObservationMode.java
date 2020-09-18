package dev.l2j.tesla.gameserver.network.serverpackets;

public class ObservationMode extends L2GameServerPacket
{
	private final int _x, _y, _z;
	
	public ObservationMode(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xdf);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeC(0x00);
		writeC(0xc0);
		writeC(0x00);
	}
}
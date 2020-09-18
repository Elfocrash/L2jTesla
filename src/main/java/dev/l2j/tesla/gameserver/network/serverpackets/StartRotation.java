package dev.l2j.tesla.gameserver.network.serverpackets;

public class StartRotation extends L2GameServerPacket
{
	private final int _charObjId, _degree, _side, _speed;
	
	public StartRotation(int objId, int degree, int side, int speed)
	{
		_charObjId = objId;
		_degree = degree;
		_side = side;
		_speed = speed;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x62);
		writeD(_charObjId);
		writeD(_degree);
		writeD(_side);
		writeD(_speed);
	}
}
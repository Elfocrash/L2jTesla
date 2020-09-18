package dev.l2j.tesla.gameserver.network.serverpackets;

public class StopRotation extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _degree;
	private final int _speed;
	
	public StopRotation(int objid, int degree, int speed)
	{
		_charObjId = objid;
		_degree = degree;
		_speed = speed;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x63);
		writeD(_charObjId);
		writeD(_degree);
		writeD(_speed);
		writeC(_degree);
	}
}
package dev.l2j.tesla.gameserver.network.serverpackets;

public class AllyCrest extends L2GameServerPacket
{
	private final int _crestId;
	private final int _crestSize;
	private final byte[] _data;
	
	public AllyCrest(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
		_crestSize = _data.length;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xae);
		writeD(_crestId);
		writeD(_crestSize);
		writeB(_data);
	}
}
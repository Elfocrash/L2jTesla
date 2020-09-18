package dev.l2j.tesla.gameserver.network.serverpackets;

public class ShowTownMap extends L2GameServerPacket
{
	private final String _texture;
	private final int _x;
	private final int _y;
	
	public ShowTownMap(String texture, int x, int y)
	{
		_texture = texture;
		_x = x;
		_y = y;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xde);
		writeS(_texture);
		writeD(_x);
		writeD(_y);
	}
}
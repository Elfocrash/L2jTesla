package dev.l2j.tesla.gameserver.network.serverpackets;

public class SunSet extends L2GameServerPacket
{
	public static final SunSet STATIC_PACKET = new SunSet();
	
	private SunSet()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x1d);
	}
}
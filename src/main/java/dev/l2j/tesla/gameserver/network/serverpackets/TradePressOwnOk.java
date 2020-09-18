package dev.l2j.tesla.gameserver.network.serverpackets;

public final class TradePressOwnOk extends L2GameServerPacket
{
	public static final TradePressOwnOk STATIC_PACKET = new TradePressOwnOk();
	
	private TradePressOwnOk()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x75);
	}
}
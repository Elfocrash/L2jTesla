package dev.l2j.tesla.gameserver.network.serverpackets;

public class CharDeleteOk extends L2GameServerPacket
{
	public static final CharDeleteOk STATIC_PACKET = new CharDeleteOk();
	
	private CharDeleteOk()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x23);
	}
}
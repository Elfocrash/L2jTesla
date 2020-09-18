package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * @author GodKratos
 */
public class ExOlympiadMatchEnd extends L2GameServerPacket
{
	public static final ExOlympiadMatchEnd STATIC_PACKET = new ExOlympiadMatchEnd();
	
	private ExOlympiadMatchEnd()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x2c);
	}
}
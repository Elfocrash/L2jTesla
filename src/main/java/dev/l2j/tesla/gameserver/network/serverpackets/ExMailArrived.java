package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * Format: (ch) (just a trigger)
 * @author -Wooden-
 */
public class ExMailArrived extends L2GameServerPacket
{
	public static final ExMailArrived STATIC_PACKET = new ExMailArrived();
	
	private ExMailArrived()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x2d);
	}
}
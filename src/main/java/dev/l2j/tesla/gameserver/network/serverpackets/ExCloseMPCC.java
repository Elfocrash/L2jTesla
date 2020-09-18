package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * @author chris_00 close the CommandChannel Information window
 */
public class ExCloseMPCC extends L2GameServerPacket
{
	public static final ExCloseMPCC STATIC_PACKET = new ExCloseMPCC();
	
	private ExCloseMPCC()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x26);
	}
}
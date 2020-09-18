package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * @author Gnacik
 */
public class ExClosePartyRoom extends L2GameServerPacket
{
	public static final ExClosePartyRoom STATIC_PACKET = new ExClosePartyRoom();
	
	private ExClosePartyRoom()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x0f);
	}
}
package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * @author Luca Baldi
 */
public class ExQuestInfo extends L2GameServerPacket
{
	public static final ExQuestInfo STATIC_PACKET = new ExQuestInfo();
	
	private ExQuestInfo()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x19);
	}
}
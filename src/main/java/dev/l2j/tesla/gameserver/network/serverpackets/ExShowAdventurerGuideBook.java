package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * Format: ch (trigger)
 * @author KenM
 */
public class ExShowAdventurerGuideBook extends L2GameServerPacket
{
	public static final ExShowAdventurerGuideBook STATIC_PACKET = new ExShowAdventurerGuideBook();
	
	private ExShowAdventurerGuideBook()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x37);
	}
}
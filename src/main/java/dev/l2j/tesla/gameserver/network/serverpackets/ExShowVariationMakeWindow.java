package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * Format: ch - Trigger packet
 * @author KenM
 */
public class ExShowVariationMakeWindow extends L2GameServerPacket
{
	public static final ExShowVariationMakeWindow STATIC_PACKET = new ExShowVariationMakeWindow();
	
	private ExShowVariationMakeWindow()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x50);
	}
}
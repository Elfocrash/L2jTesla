package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * Format: (ch)ddd
 */
public class ExVariationResult extends L2GameServerPacket
{
	private final int _stat12;
	private final int _stat34;
	private final int _unk3;
	
	public ExVariationResult(int unk1, int unk2, int unk3)
	{
		_stat12 = unk1;
		_stat34 = unk2;
		_unk3 = unk3;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x55);
		writeD(_stat12);
		writeD(_stat34);
		writeD(_unk3);
	}
}
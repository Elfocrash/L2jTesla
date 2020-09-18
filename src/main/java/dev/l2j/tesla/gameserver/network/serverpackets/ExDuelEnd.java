package dev.l2j.tesla.gameserver.network.serverpackets;

public class ExDuelEnd extends L2GameServerPacket
{
	private final int _isPartyDuel;
	
	public ExDuelEnd(boolean isPartyDuel)
	{
		_isPartyDuel = isPartyDuel ? 1 : 0;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4e);
		
		writeD(_isPartyDuel);
	}
}
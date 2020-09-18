package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * Format: ch S
 * @author KenM
 */
public class ExAskJoinPartyRoom extends L2GameServerPacket
{
	private final String _charName;
	
	public ExAskJoinPartyRoom(String charName)
	{
		_charName = charName;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x34);
		writeS(_charName);
	}
}
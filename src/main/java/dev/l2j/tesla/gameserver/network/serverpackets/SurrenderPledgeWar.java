package dev.l2j.tesla.gameserver.network.serverpackets;

public class SurrenderPledgeWar extends L2GameServerPacket
{
	private final String _pledgeName;
	private final String _playerName;
	
	public SurrenderPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_playerName = charName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x69);
		writeS(_pledgeName);
		writeS(_playerName);
	}
}
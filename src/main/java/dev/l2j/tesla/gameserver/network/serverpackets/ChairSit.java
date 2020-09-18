package dev.l2j.tesla.gameserver.network.serverpackets;

public class ChairSit extends L2GameServerPacket
{
	private final int _playerId;
	private final int _staticId;
	
	public ChairSit(int playerId, int staticId)
	{
		_playerId = playerId;
		_staticId = staticId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe1);
		writeD(_playerId);
		writeD(_staticId);
	}
}
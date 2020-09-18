package dev.l2j.tesla.gameserver.network.serverpackets;

public class MagicSkillCanceled extends L2GameServerPacket
{
	private final int _objectId;
	
	public MagicSkillCanceled(int objectId)
	{
		_objectId = objectId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x49);
		writeD(_objectId);
	}
}
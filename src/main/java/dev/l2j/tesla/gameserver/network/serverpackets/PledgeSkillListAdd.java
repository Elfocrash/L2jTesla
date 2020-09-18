package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * Format: (ch) dd
 * @author -Wooden-
 */
public class PledgeSkillListAdd extends L2GameServerPacket
{
	private final int _id;
	private final int _lvl;
	
	public PledgeSkillListAdd(int id, int lvl)
	{
		_id = id;
		_lvl = lvl;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3a);
		writeD(_id);
		writeD(_lvl);
	}
}
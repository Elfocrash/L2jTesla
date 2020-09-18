package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * @author godson
 */
public class ExOlympiadUserInfo extends L2GameServerPacket
{
	private int _side;
	private int _objectId;
	private String _name;
	private int _classId;
	private int _curHp;
	private int _maxHp;
	private int _curCp;
	private int _maxCp;
	
	public ExOlympiadUserInfo(Player player)
	{
		_side = player.getOlympiadSide();
		_objectId = player.getObjectId();
		_name = player.getName();
		_classId = player.getClassId().getId();
		_curHp = (int) player.getCurrentHp();
		_maxHp = player.getMaxHp();
		_curCp = (int) player.getCurrentCp();
		_maxCp = player.getMaxCp();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x29);
		writeC(_side);
		writeD(_objectId);
		writeS(_name);
		writeD(_classId);
		writeD(_curHp);
		writeD(_maxHp);
		writeD(_curCp);
		writeD(_maxCp);
	}
}
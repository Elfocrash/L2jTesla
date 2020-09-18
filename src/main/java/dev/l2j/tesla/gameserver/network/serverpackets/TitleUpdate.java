package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.actor.Creature;

/**
 * @author devScarlet
 */
public class TitleUpdate extends L2GameServerPacket
{
	private final String _title;
	private final int _objectId;
	
	public TitleUpdate(Creature cha)
	{
		_objectId = cha.getObjectId();
		_title = cha.getTitle();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xcc);
		writeD(_objectId);
		writeS(_title);
	}
}
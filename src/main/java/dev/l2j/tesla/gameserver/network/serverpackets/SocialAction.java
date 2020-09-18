package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.actor.Creature;

public class SocialAction extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _actionId;
	
	public SocialAction(Creature cha, int actionId)
	{
		_charObjId = cha.getObjectId();
		_actionId = actionId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2d);
		writeD(_charObjId);
		writeD(_actionId);
	}
}
package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;

/**
 * This packet is used to move characters to a target.<br>
 * It is aswell used to rotate characters in front of the target.
 */
public class MoveToPawn extends L2GameServerPacket
{
	private final int _objectId;
	private final int _targetId;
	private final int _distance;
	private final int _x, _y, _z;
	
	public MoveToPawn(Creature cha, WorldObject target, int distance)
	{
		_objectId = cha.getObjectId();
		_targetId = target.getObjectId();
		_distance = distance;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x60);
		
		writeD(_objectId);
		writeD(_targetId);
		writeD(_distance);
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
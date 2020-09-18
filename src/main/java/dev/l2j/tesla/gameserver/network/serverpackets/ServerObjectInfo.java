package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;

public final class ServerObjectInfo extends L2GameServerPacket
{
	private final Npc _npc;
	
	private final int _idTemplate;
	private final String _name;
	
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	
	private final double _collisionHeight;
	private final double _collisionRadius;
	
	private final boolean _isAttackable;
	
	public ServerObjectInfo(Npc npc, Creature actor)
	{
		_npc = npc;
		
		_idTemplate = _npc.getTemplate().getIdTemplate();
		_name = _npc.getName();
		
		_x = _npc.getX();
		_y = _npc.getY();
		_z = _npc.getZ();
		_heading = _npc.getHeading();
		
		_collisionHeight = _npc.getCollisionHeight();
		_collisionRadius = _npc.getCollisionRadius();
		
		_isAttackable = _npc.isAutoAttackable(actor);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x8C);
		writeD(_npc.getObjectId());
		writeD(_idTemplate + 1000000);
		writeS(_name);
		writeD(_isAttackable ? 1 : 0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeF(1.0); // movement multiplier
		writeF(1.0); // attack speed multiplier
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD((int) (_isAttackable ? _npc.getCurrentHp() : 0));
		writeD(_isAttackable ? _npc.getMaxHp() : 0);
		writeD(0x01); // object type
		writeD(0x00); // special effects
	}
}
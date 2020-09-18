package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * format dddc dddh (ddc)
 */
public class Attack extends L2GameServerPacket
{
	public static final int HITFLAG_USESS = 0x10;
	public static final int HITFLAG_CRIT = 0x20;
	public static final int HITFLAG_SHLD = 0x40;
	public static final int HITFLAG_MISS = 0x80;
	
	public class Hit
	{
		protected final int _targetId;
		protected final int _damage;
		protected int _flags;
		
		Hit(WorldObject target, int damage, boolean miss, boolean crit, byte shld)
		{
			_targetId = target.getObjectId();
			_damage = damage;
			
			if (miss)
			{
				_flags = HITFLAG_MISS;
				return;
			}
			
			if (soulshot)
				_flags = HITFLAG_USESS | _ssGrade;
			
			if (crit)
				_flags |= HITFLAG_CRIT;
			
			if (shld > 0 && !(target instanceof Player && ((Player) target).isInOlympiadMode()))
				_flags |= HITFLAG_SHLD;
		}
	}
	
	private final int _attackerObjId;
	public final boolean soulshot;
	public final int _ssGrade;
	private final int _x, _y, _z;
	private Hit[] _hits;
	
	/**
	 * @param attacker The attacking Creature.
	 * @param useShots True if soulshots are used.
	 * @param ssGrade The grade of the soulshots.
	 */
	public Attack(Creature attacker, boolean useShots, int ssGrade)
	{
		_attackerObjId = attacker.getObjectId();
		soulshot = useShots;
		_ssGrade = ssGrade;
		_x = attacker.getX();
		_y = attacker.getY();
		_z = attacker.getZ();
	}
	
	public Hit createHit(WorldObject target, int damage, boolean miss, boolean crit, byte shld)
	{
		return new Hit(target, damage, miss, crit, shld);
	}
	
	public void hit(Hit... hits)
	{
		if (_hits == null)
		{
			_hits = hits;
			return;
		}
		
		// this will only happen with pole attacks
		Hit[] tmp = new Hit[hits.length + _hits.length];
		System.arraycopy(_hits, 0, tmp, 0, _hits.length);
		System.arraycopy(hits, 0, tmp, _hits.length, hits.length);
		_hits = tmp;
	}
	
	/**
	 * @return True if the Server-Client packet Attack contains at least 1 hit.
	 */
	public boolean hasHits()
	{
		return _hits != null;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x05);
		
		writeD(_attackerObjId);
		writeD(_hits[0]._targetId);
		writeD(_hits[0]._damage);
		writeC(_hits[0]._flags);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeH(_hits.length - 1);
		// prevent sending useless packet while there is only one target.
		if (_hits.length > 1)
		{
			for (int i = 1; i < _hits.length; i++)
			{
				writeD(_hits[i]._targetId);
				writeD(_hits[i]._damage);
				writeC(_hits[i]._flags);
			}
		}
	}
}
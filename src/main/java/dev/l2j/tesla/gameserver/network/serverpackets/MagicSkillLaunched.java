package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;

/**
 * format ddddd d
 */
public class MagicSkillLaunched extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _skillId;
	private final int _skillLevel;
	private final int _numberOfTargets;
	private WorldObject[] _targets;
	private final int _singleTargetId;
	
	public MagicSkillLaunched(Creature cha, int skillId, int skillLevel, WorldObject[] targets)
	{
		_charObjId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_numberOfTargets = targets.length;
		_targets = targets;
		_singleTargetId = 0;
	}
	
	public MagicSkillLaunched(Creature cha, int skillId, int skillLevel)
	{
		_charObjId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_numberOfTargets = 1;
		_singleTargetId = cha.getTargetId();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x76);
		writeD(_charObjId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_numberOfTargets); // also failed or not?
		if (_singleTargetId != 0 || _numberOfTargets == 0)
			writeD(_singleTargetId);
		else
			for (WorldObject target : _targets)
			{
				try
				{
					writeD(target.getObjectId());
				}
				catch (NullPointerException e)
				{
					writeD(0); // untested
				}
			}
	}
}
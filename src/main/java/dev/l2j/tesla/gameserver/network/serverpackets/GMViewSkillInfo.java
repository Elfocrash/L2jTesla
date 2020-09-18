package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.Collection;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class GMViewSkillInfo extends L2GameServerPacket
{
	private final Player _player;
	private final Collection<L2Skill> _skills;
	private final boolean _isWearingFormalWear;
	private final boolean _isClanDisabled;
	
	public GMViewSkillInfo(Player player)
	{
		_player = player;
		_skills = player.getSkills().values();
		_isWearingFormalWear = player.isWearingFormalWear();
		_isClanDisabled = player.getClan() != null && player.getClan().getReputationScore() < 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x91);
		writeS(_player.getName());
		writeD(_skills.size());
		
		for (L2Skill skill : _skills)
		{
			writeD(skill.isPassive() ? 1 : 0);
			writeD(skill.getLevel());
			writeD(skill.getId());
			writeC(_isWearingFormalWear || (skill.isClanSkill() && _isClanDisabled) ? 1 : 0);
		}
	}
}
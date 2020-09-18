package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.Collection;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.pledge.Clan;

public class PledgeSkillList extends L2GameServerPacket
{
	private final Clan _clan;
	
	public PledgeSkillList(Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected void writeImpl()
	{
		Collection<L2Skill> skills = _clan.getClanSkills().values();
		
		writeC(0xfe);
		writeH(0x39);
		
		writeD(skills.size());
		
		for (L2Skill sk : skills)
		{
			writeD(sk.getId());
			writeD(sk.getLevel());
		}
	}
}
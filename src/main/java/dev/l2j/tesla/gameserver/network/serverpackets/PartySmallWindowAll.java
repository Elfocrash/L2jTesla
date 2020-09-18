package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.group.Party;

public final class PartySmallWindowAll extends L2GameServerPacket
{
	private final Party _party;
	private final Player _exclude;
	private final int _dist;
	private final int _leaderObjectId;
	
	public PartySmallWindowAll(Player exclude, Party party)
	{
		_exclude = exclude;
		_party = party;
		_leaderObjectId = _party.getLeaderObjectId();
		_dist = _party.getLootRule().ordinal();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4e);
		writeD(_leaderObjectId);
		writeD(_dist);
		writeD(_party.getMembersCount() - 1);
		
		for (Player member : _party.getMembers())
		{
			if (member == _exclude)
				continue;
			
			writeD(member.getObjectId());
			writeS(member.getName());
			writeD((int) member.getCurrentCp());
			writeD(member.getMaxCp());
			writeD((int) member.getCurrentHp());
			writeD(member.getMaxHp());
			writeD((int) member.getCurrentMp());
			writeD(member.getMaxMp());
			writeD(member.getLevel());
			writeD(member.getClassId().getId());
			writeD(0);// writeD(0x01); ??
			writeD(member.getRace().ordinal());
		}
	}
}
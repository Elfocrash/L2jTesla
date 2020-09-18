package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.pledge.Clan;

/**
 * format ddddd
 */
public class PledgeStatusChanged extends L2GameServerPacket
{
	private final Clan _clan;
	
	public PledgeStatusChanged(Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xcd);
		writeD(_clan.getLeaderId());
		writeD(_clan.getClanId());
		writeD(_clan.getCrestId());
		writeD(_clan.getAllyId());
		writeD(_clan.getAllyCrestId());
		writeD(0);
		writeD(0);
	}
}
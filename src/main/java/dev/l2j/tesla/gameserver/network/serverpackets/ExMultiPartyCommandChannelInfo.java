package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.group.CommandChannel;
import dev.l2j.tesla.gameserver.model.group.Party;

public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket
{
	private final CommandChannel _channel;
	
	public ExMultiPartyCommandChannelInfo(CommandChannel channel)
	{
		_channel = channel;
	}
	
	@Override
	protected void writeImpl()
	{
		if (_channel == null)
			return;
		
		writeC(0xfe);
		writeH(0x30);
		
		writeS(_channel.getLeader().getName());
		writeD(0); // Channel loot
		writeD(_channel.getMembersCount());
		
		writeD(_channel.getParties().size());
		for (Party party : _channel.getParties())
		{
			writeS(party.getLeader().getName());
			writeD(party.getLeaderObjectId());
			writeD(party.getMembersCount());
		}
	}
}
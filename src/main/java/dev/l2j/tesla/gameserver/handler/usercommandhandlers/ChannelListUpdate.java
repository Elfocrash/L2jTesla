package dev.l2j.tesla.gameserver.handler.usercommandhandlers;

import dev.l2j.tesla.gameserver.handler.IUserCommandHandler;
import dev.l2j.tesla.gameserver.network.serverpackets.ExMultiPartyCommandChannelInfo;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.group.CommandChannel;
import dev.l2j.tesla.gameserver.model.group.Party;

public class ChannelListUpdate implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		97
	};
	
	@Override
	public boolean useUserCommand(int id, Player player)
	{
		final Party party = player.getParty();
		if (party == null)
			return false;
		
		final CommandChannel channel = party.getCommandChannel();
		if (channel == null)
			return false;
		
		player.sendPacket(new ExMultiPartyCommandChannelInfo(channel));
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
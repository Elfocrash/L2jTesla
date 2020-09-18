package dev.l2j.tesla.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.handler.usercommandhandlers.ChannelDelete;
import dev.l2j.tesla.gameserver.handler.usercommandhandlers.ChannelLeave;
import dev.l2j.tesla.gameserver.handler.usercommandhandlers.ChannelListUpdate;
import dev.l2j.tesla.gameserver.handler.usercommandhandlers.ClanPenalty;
import dev.l2j.tesla.gameserver.handler.usercommandhandlers.ClanWarsList;
import dev.l2j.tesla.gameserver.handler.usercommandhandlers.DisMount;
import dev.l2j.tesla.gameserver.handler.usercommandhandlers.Escape;
import dev.l2j.tesla.gameserver.handler.usercommandhandlers.Loc;
import dev.l2j.tesla.gameserver.handler.usercommandhandlers.Mount;
import dev.l2j.tesla.gameserver.handler.usercommandhandlers.OlympiadStat;
import dev.l2j.tesla.gameserver.handler.usercommandhandlers.PartyInfo;
import dev.l2j.tesla.gameserver.handler.usercommandhandlers.SiegeStatus;
import dev.l2j.tesla.gameserver.handler.usercommandhandlers.Time;

public class UserCommandHandler
{
	private final Map<Integer, IUserCommandHandler> _entries = new HashMap<>();
	
	protected UserCommandHandler()
	{
		registerHandler(new ChannelDelete());
		registerHandler(new ChannelLeave());
		registerHandler(new ChannelListUpdate());
		registerHandler(new ClanPenalty());
		registerHandler(new ClanWarsList());
		registerHandler(new DisMount());
		registerHandler(new Escape());
		registerHandler(new Loc());
		registerHandler(new Mount());
		registerHandler(new OlympiadStat());
		registerHandler(new PartyInfo());
		registerHandler(new SiegeStatus());
		registerHandler(new Time());
	}
	
	private void registerHandler(IUserCommandHandler handler)
	{
		for (int id : handler.getUserCommandList())
			_entries.put(id, handler);
	}
	
	public IUserCommandHandler getHandler(int userCommand)
	{
		return _entries.get(userCommand);
	}
	
	public int size()
	{
		return _entries.size();
	}
	
	public static UserCommandHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final UserCommandHandler INSTANCE = new UserCommandHandler();
	}
}
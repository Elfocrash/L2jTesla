package dev.l2j.tesla.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.handler.chathandlers.ChatAll;
import dev.l2j.tesla.gameserver.handler.chathandlers.ChatAlliance;
import dev.l2j.tesla.gameserver.handler.chathandlers.ChatClan;
import dev.l2j.tesla.gameserver.handler.chathandlers.ChatHeroVoice;
import dev.l2j.tesla.gameserver.handler.chathandlers.ChatParty;
import dev.l2j.tesla.gameserver.handler.chathandlers.ChatPartyMatchRoom;
import dev.l2j.tesla.gameserver.handler.chathandlers.ChatPartyRoomAll;
import dev.l2j.tesla.gameserver.handler.chathandlers.ChatPartyRoomCommander;
import dev.l2j.tesla.gameserver.handler.chathandlers.ChatPetition;
import dev.l2j.tesla.gameserver.handler.chathandlers.ChatShout;
import dev.l2j.tesla.gameserver.handler.chathandlers.ChatTell;
import dev.l2j.tesla.gameserver.handler.chathandlers.ChatTrade;

public class ChatHandler
{
	private final Map<Integer, IChatHandler> _entries = new HashMap<>();
	
	protected ChatHandler()
	{
		registerHandler(new ChatAll());
		registerHandler(new ChatAlliance());
		registerHandler(new ChatClan());
		registerHandler(new ChatHeroVoice());
		registerHandler(new ChatParty());
		registerHandler(new ChatPartyMatchRoom());
		registerHandler(new ChatPartyRoomAll());
		registerHandler(new ChatPartyRoomCommander());
		registerHandler(new ChatPetition());
		registerHandler(new ChatShout());
		registerHandler(new ChatTell());
		registerHandler(new ChatTrade());
	}
	
	private void registerHandler(IChatHandler handler)
	{
		for (int id : handler.getChatTypeList())
			_entries.put(id, handler);
	}
	
	public IChatHandler getHandler(int chatType)
	{
		return _entries.get(chatType);
	}
	
	public int size()
	{
		return _entries.size();
	}
	
	public static ChatHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ChatHandler INSTANCE = new ChatHandler();
	}
}
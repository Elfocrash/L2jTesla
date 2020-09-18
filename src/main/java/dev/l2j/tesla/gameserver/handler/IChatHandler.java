package dev.l2j.tesla.gameserver.handler;

import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * Interface for chat handlers
 * @author durgus
 */
public interface IChatHandler
{
	/**
	 * Handles a specific type of chat messages
	 * @param type
	 * @param activeChar
	 * @param target
	 * @param text
	 */
	public void handleChat(int type, Player activeChar, String target, String text);
	
	/**
	 * Returns a list of all chat types registered to this handler
	 * @return
	 */
	public int[] getChatTypeList();
}

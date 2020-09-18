package dev.l2j.tesla.gameserver.handler;

import dev.l2j.tesla.gameserver.model.actor.Player;

public interface IUserCommandHandler
{
	/**
	 * this is the worker method that is called when someone uses an admin command.
	 * @param id
	 * @param activeChar
	 * @return command success
	 */
	public boolean useUserCommand(int id, Player activeChar);
	
	/**
	 * this method is called at initialization to register all the item ids automatically
	 * @return all known itemIds
	 */
	public int[] getUserCommandList();
}

package dev.l2j.tesla.gameserver.handler;

import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

/**
 * Mother class of all itemHandlers.
 */
public interface IItemHandler
{
	public static final CLogger LOGGER = new CLogger(IItemHandler.class.getName());
	
	/**
	 * Launch task associated to the item.
	 * @param playable L2Playable designating the player
	 * @param item ItemInstance designating the item to use
	 * @param forceUse ctrl hold on item use
	 */
	public void useItem(Playable playable, ItemInstance item, boolean forceUse);
}
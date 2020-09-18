package dev.l2j.tesla.gameserver.taskmanager;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.commons.concurrent.ThreadPool;

import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.itemcontainer.listeners.OnEquipListener;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.InventoryUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;

/**
 * Updates the timer and removes the {@link ItemInstance} as a shadow item.
 */
public class ShadowItemTaskManager implements Runnable, OnEquipListener
{
	private static final int DELAY = 1; // 1 second
	
	private final Map<ItemInstance, Player> _shadowItems = new ConcurrentHashMap<>();
	
	protected ShadowItemTaskManager()
	{
		// Run task each second.
		ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	@Override
	public final void run()
	{
		// List is empty, skip.
		if (_shadowItems.isEmpty())
			return;
		
		// For all items.
		for (Entry<ItemInstance, Player> entry : _shadowItems.entrySet())
		{
			// Get item and player.
			final ItemInstance item = entry.getKey();
			final Player player = entry.getValue();
			
			// Decrease item mana.
			int mana = item.decreaseMana(DELAY);
			
			// If not enough mana, destroy the item and inform the player.
			if (mana == -1)
			{
				// Remove item first.
				player.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(item);
				player.sendPacket(iu);
				
				// Destroy shadow item, remove from list.
				player.destroyItem("ShadowItem", item, player, false);
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0).addItemName(item.getItemId()));
				_shadowItems.remove(item);
				
				continue;
			}
			
			// Enough mana, show messages.
			if (mana == 60 - DELAY)
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1).addItemName(item.getItemId()));
			else if (mana == 300 - DELAY)
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5).addItemName(item.getItemId()));
			else if (mana == 600 - DELAY)
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10).addItemName(item.getItemId()));
			
			// Update inventory every minute.
			if (mana % 60 == 60 - DELAY)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(item);
				player.sendPacket(iu);
			}
		}
	}
	
	@Override
	public final void onEquip(int slot, ItemInstance item, Playable playable)
	{
		// Must be a shadow item.
		if (!item.isShadowItem())
			return;
		
		// Must be a player.
		if (!(playable instanceof Player))
			return;
		
		_shadowItems.put(item, (Player) playable);
	}
	
	@Override
	public final void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		// Must be a shadow item.
		if (!item.isShadowItem())
			return;
		
		_shadowItems.remove(item);
	}
	
	public final void remove(Player player)
	{
		// List is empty, skip.
		if (_shadowItems.isEmpty())
			return;
		
		// Remove ALL associated items.
		_shadowItems.values().removeAll(Collections.singleton(player));
	}
	
	public static final ShadowItemTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ShadowItemTaskManager INSTANCE = new ShadowItemTaskManager();
	}
}
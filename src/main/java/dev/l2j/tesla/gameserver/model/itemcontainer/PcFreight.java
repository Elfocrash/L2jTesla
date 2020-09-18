package dev.l2j.tesla.gameserver.model.itemcontainer;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance.ItemLocation;

public class PcFreight extends ItemContainer
{
	private final Player _owner;
	
	private int _activeLocationId;
	private int _tempOwnerId = 0;
	
	public PcFreight(Player owner)
	{
		_owner = owner;
	}
	
	@Override
	public String getName()
	{
		return "Freight";
	}
	
	@Override
	public Player getOwner()
	{
		return _owner;
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.FREIGHT;
	}
	
	public void setActiveLocation(int locationId)
	{
		_activeLocationId = locationId;
	}
	
	@Override
	public int getSize()
	{
		int size = 0;
		for (ItemInstance item : _items)
		{
			if (item.getLocationSlot() == 0 || _activeLocationId == 0 || item.getLocationSlot() == _activeLocationId)
				size++;
		}
		return size;
	}
	
	@Override
	public Set<ItemInstance> getItems()
	{
		if (_items.isEmpty())
			return Collections.emptySet();
		
		return _items.stream().filter(i -> i.getLocationSlot() == 0 || i.getLocationSlot() == _activeLocationId).collect(Collectors.toSet());
	}
	
	@Override
	public ItemInstance getItemByItemId(int itemId)
	{
		for (ItemInstance item : _items)
		{
			if (item.getItemId() == itemId && (item.getLocationSlot() == 0 || _activeLocationId == 0 || item.getLocationSlot() == _activeLocationId))
				return item;
		}
		return null;
	}
	
	@Override
	protected void addItem(ItemInstance item)
	{
		super.addItem(item);
		
		if (_activeLocationId > 0)
			item.setLocation(item.getLocation(), _activeLocationId);
	}
	
	@Override
	public void restore()
	{
		int locationId = _activeLocationId;
		_activeLocationId = 0;
		
		super.restore();
		
		_activeLocationId = locationId;
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return getSize() + slots <= ((_owner == null) ? Config.FREIGHT_SLOTS : _owner.getFreightLimit());
	}
	
	@Override
	public int getOwnerId()
	{
		return (_owner == null) ? _tempOwnerId : super.getOwnerId();
	}
	
	/**
	 * This provides support to load a new PcFreight without owner so that transactions can be done
	 * @param val The id of the owner.
	 */
	public void doQuickRestore(int val)
	{
		_tempOwnerId = val;
		
		restore();
	}
}
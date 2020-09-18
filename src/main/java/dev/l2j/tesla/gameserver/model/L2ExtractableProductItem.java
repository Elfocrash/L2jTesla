package dev.l2j.tesla.gameserver.model;

import java.util.List;

import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;

/**
 * @author -Nemesiss-, Zoey76
 */
public class L2ExtractableProductItem
{
	private final List<IntIntHolder> _items;
	private final double _chance;
	
	public L2ExtractableProductItem(List<IntIntHolder> items, double chance)
	{
		_items = items;
		_chance = chance;
	}
	
	public List<IntIntHolder> getItems()
	{
		return _items;
	}
	
	public double getChance()
	{
		return _chance;
	}
}
package dev.l2j.tesla.gameserver.model.holder.skillnode;

import dev.l2j.tesla.commons.util.StatsSet;

/**
 * A datatype used by fishing (or common) skill types. It extends {@link SkillNode}.
 */
public final class FishingSkillNode extends SkillNode
{
	private final int _itemId;
	private final int _itemCount;
	
	private final boolean _isDwarven;
	
	public FishingSkillNode(StatsSet set)
	{
		super(set);
		
		_itemId = set.getInteger("itemId");
		_itemCount = set.getInteger("itemCount");
		
		_isDwarven = set.getBool("isDwarven", false);
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getItemCount()
	{
		return _itemCount;
	}
	
	public boolean isDwarven()
	{
		return _isDwarven;
	}
}
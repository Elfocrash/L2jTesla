package dev.l2j.tesla.gameserver.model.item.kind;

import dev.l2j.tesla.gameserver.model.itemcontainer.PcInventory;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.enums.items.EtcItemType;

/**
 * This class is dedicated to the management of EtcItem.
 */
public final class EtcItem extends Item
{
	private final String _handler;
	private final int _sharedReuseGroup;
	private EtcItemType _type;
	private final int _reuseDelay;
	
	/**
	 * Constructor for EtcItem.
	 * @see Item constructor
	 * @param set : StatsSet designating the set of couples (key,value) for description of the Etc
	 */
	public EtcItem(StatsSet set)
	{
		super(set);
		_type = EtcItemType.valueOf(set.getString("etcitem_type", "none").toUpperCase());
		
		// l2j custom - L2EtcItemType.SHOT
		switch (getDefaultAction())
		{
			case soulshot:
			case summon_soulshot:
			case summon_spiritshot:
			case spiritshot:
			{
				_type = EtcItemType.SHOT;
				break;
			}
		}
		
		_type1 = TYPE1_ITEM_QUESTITEM_ADENA;
		_type2 = TYPE2_OTHER; // default is other
		
		if (isQuestItem())
			_type2 = TYPE2_QUEST;
		else if (getItemId() == PcInventory.ADENA_ID || getItemId() == PcInventory.ANCIENT_ADENA_ID)
			_type2 = TYPE2_MONEY;
		
		_handler = set.getString("handler", null); // ! null !
		_sharedReuseGroup = set.getInteger("shared_reuse_group", -1);
		_reuseDelay = set.getInteger("reuse_delay", 0);
	}
	
	/**
	 * Returns the type of Etc Item
	 * @return L2EtcItemType
	 */
	@Override
	public EtcItemType getItemType()
	{
		return _type;
	}
	
	/**
	 * Returns if the item is consumable
	 * @return boolean
	 */
	@Override
	public final boolean isConsumable()
	{
		return ((getItemType() == EtcItemType.SHOT) || (getItemType() == EtcItemType.POTION));
	}
	
	/**
	 * Returns the ID of the Etc item after applying the mask.
	 * @return int : ID of the EtcItem
	 */
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * Return handler name. null if no handler for item
	 * @return String
	 */
	public String getHandlerName()
	{
		return _handler;
	}
	
	public int getSharedReuseGroup()
	{
		return _sharedReuseGroup;
	}
	
	public int getReuseDelay()
	{
		return _reuseDelay;
	}
}
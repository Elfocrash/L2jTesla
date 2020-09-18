package dev.l2j.tesla.gameserver.model.tradelist;

import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.kind.Item;

public class TradeItem
{
	private int _objectId;
	private final Item _item;
	private int _enchant;
	private int _count;
	private int _price;
	
	public TradeItem(ItemInstance item, int count, int price)
	{
		_objectId = item.getObjectId();
		_item = item.getItem();
		_enchant = item.getEnchantLevel();
		_count = count;
		_price = price;
	}
	
	public TradeItem(Item item, int count, int price)
	{
		_objectId = 0;
		_item = item;
		_enchant = 0;
		_count = count;
		_price = price;
	}
	
	public TradeItem(TradeItem item, int count, int price)
	{
		_objectId = item.getObjectId();
		_item = item.getItem();
		_enchant = item.getEnchant();
		_count = count;
		_price = price;
	}
	
	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public Item getItem()
	{
		return _item;
	}
	
	public void setEnchant(int enchant)
	{
		_enchant = enchant;
	}
	
	public int getEnchant()
	{
		return _enchant;
	}
	
	public void setCount(int count)
	{
		_count = count;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public void setPrice(int price)
	{
		_price = price;
	}
	
	public int getPrice()
	{
		return _price;
	}
}
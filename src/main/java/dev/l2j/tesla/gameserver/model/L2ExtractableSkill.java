package dev.l2j.tesla.gameserver.model;

import java.util.List;

public class L2ExtractableSkill
{
	private final int _hash;
	private final List<L2ExtractableProductItem> _product;
	
	public L2ExtractableSkill(int hash, List<L2ExtractableProductItem> products)
	{
		_hash = hash;
		_product = products;
	}
	
	public int getSkillHash()
	{
		return _hash;
	}
	
	public List<L2ExtractableProductItem> getProductItemsArray()
	{
		return _product;
	}
}
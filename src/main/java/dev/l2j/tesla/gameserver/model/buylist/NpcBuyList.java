package dev.l2j.tesla.gameserver.model.buylist;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A datatype used to hold buylists. Each buylist got a Map of {@link Product}.<br>
 * For security reasons and to avoid crafted packets, we added npcId aswell.
 */
public class NpcBuyList
{
	private final Map<Integer, Product> _products = new LinkedHashMap<>();
	private final int _listId;
	
	private int _npcId;
	
	public NpcBuyList(int listId)
	{
		_listId = listId;
	}
	
	public int getListId()
	{
		return _listId;
	}
	
	public Collection<Product> getProducts()
	{
		return _products.values();
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public void setNpcId(int id)
	{
		_npcId = id;
	}
	
	public Product getProductByItemId(int itemId)
	{
		return _products.get(itemId);
	}
	
	public void addProduct(Product product)
	{
		_products.put(product.getItemId(), product);
	}
	
	public boolean isNpcAllowed(int npcId)
	{
		return _npcId == npcId;
	}
}
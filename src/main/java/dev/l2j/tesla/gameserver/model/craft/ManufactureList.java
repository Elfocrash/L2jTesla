package dev.l2j.tesla.gameserver.model.craft;

import java.util.ArrayList;
import java.util.List;

/**
 * A datacontainer used by private workshop system. It retains a List of {@link ManufactureItem}s, the store name and the shop state.
 */
public class ManufactureList
{
	private final List<ManufactureItem> _list = new ArrayList<>();
	
	private boolean _confirmed;
	private String _storeName;
	
	public ManufactureList()
	{
	}
	
	public void setConfirmedTrade(boolean confirmed)
	{
		_confirmed = confirmed;
	}
	
	public boolean hasConfirmed()
	{
		return _confirmed;
	}
	
	public String getStoreName()
	{
		return _storeName;
	}
	
	public void setStoreName(String storeName)
	{
		_storeName = storeName;
	}
	
	public void add(ManufactureItem item)
	{
		_list.add(item);
	}
	
	public List<ManufactureItem> getList()
	{
		return _list;
	}
}
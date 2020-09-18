package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.craft.ManufactureItem;
import dev.l2j.tesla.gameserver.model.item.Recipe;

public class RecipeShopManageList extends L2GameServerPacket
{
	private final Player _seller;
	private final boolean _isDwarven;
	
	private Collection<Recipe> _recipes;
	
	public RecipeShopManageList(Player seller, boolean isDwarven)
	{
		_seller = seller;
		_isDwarven = isDwarven;
		
		if (_isDwarven && seller.hasDwarvenCraft())
			_recipes = seller.getDwarvenRecipeBook();
		else
			_recipes = seller.getCommonRecipeBook();
		
		// clean previous recipes
		if (seller.getCreateList() != null)
		{
			final Iterator<ManufactureItem> it = seller.getCreateList().getList().iterator();
			while (it.hasNext())
			{
				ManufactureItem item = it.next();
				if (item.isDwarven() != _isDwarven || !seller.hasRecipeList(item.getId()))
					it.remove();
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xd8);
		writeD(_seller.getObjectId());
		writeD(_seller.getAdena());
		writeD(_isDwarven ? 0x00 : 0x01);
		
		if (_recipes == null)
			writeD(0);
		else
		{
			writeD(_recipes.size());// number of items in recipe book
			
			int i = 0;
			for (Recipe recipe : _recipes)
			{
				writeD(recipe.getId());
				writeD(++i);
			}
		}
		
		if (_seller.getCreateList() == null)
			writeD(0);
		else
		{
			final List<ManufactureItem> list = _seller.getCreateList().getList();
			writeD(list.size());
			
			for (ManufactureItem item : list)
			{
				writeD(item.getId());
				writeD(0x00);
				writeD(item.getValue());
			}
		}
	}
}
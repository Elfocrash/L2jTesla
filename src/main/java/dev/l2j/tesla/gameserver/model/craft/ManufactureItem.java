package dev.l2j.tesla.gameserver.model.craft;

import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.data.xml.RecipeData;

/**
 * A datatype extending {@link IntIntHolder}. It is part of private workshop system, and is used to hold individual entries.
 */
public class ManufactureItem extends IntIntHolder
{
	private final boolean _isDwarven;
	
	public ManufactureItem(int recipeId, int cost)
	{
		super(recipeId, cost);
		
		_isDwarven = RecipeData.getInstance().getRecipeList(recipeId).isDwarven();
	}
	
	public boolean isDwarven()
	{
		return _isDwarven;
	}
}
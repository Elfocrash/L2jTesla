package dev.l2j.tesla.gameserver.model.item;

import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.commons.util.ArraysUtil;
import dev.l2j.tesla.commons.util.StatsSet;

/**
 * A datatype used to retain Henna infos. Hennas are called "dye" ingame, and enhance {@link Player} stats for a fee.<br>
 * <br>
 * You can draw up to 3 hennas (depending about your current class rank), but accumulated boni for a stat can't be higher than +5. There is no limit in reduction.
 */
public final class Henna
{
	public static final int DRAW_AMOUNT = 10;
	public static final int REMOVE_AMOUNT = 5;
	
	private final int _symbolId;
	private final int _dyeId;
	private final int _drawPrice;
	private final int _INT;
	private final int _STR;
	private final int _CON;
	private final int _MEN;
	private final int _DEX;
	private final int _WIT;
	private final int[] _classes;
	
	public Henna(StatsSet set)
	{
		_symbolId = set.getInteger("symbolId");
		_dyeId = set.getInteger("dyeId");
		_drawPrice = set.getInteger("price", 0);
		_INT = set.getInteger("INT", 0);
		_STR = set.getInteger("STR", 0);
		_CON = set.getInteger("CON", 0);
		_MEN = set.getInteger("MEN", 0);
		_DEX = set.getInteger("DEX", 0);
		_WIT = set.getInteger("WIT", 0);
		_classes = set.getIntegerArray("classes");
	}
	
	public int getSymbolId()
	{
		return _symbolId;
	}
	
	public int getDyeId()
	{
		return _dyeId;
	}
	
	public int getDrawPrice()
	{
		return _drawPrice;
	}
	
	public int getRemovePrice()
	{
		return _drawPrice / 5;
	}
	
	public int getINT()
	{
		return _INT;
	}
	
	public int getSTR()
	{
		return _STR;
	}
	
	public int getCON()
	{
		return _CON;
	}
	
	public int getMEN()
	{
		return _MEN;
	}
	
	public int getDEX()
	{
		return _DEX;
	}
	
	public int getWIT()
	{
		return _WIT;
	}
	
	/**
	 * Seek if this {@link Henna} can be used by a {@link Player}, based on his classId.
	 * @param player : The Player to check.
	 * @return true if this Henna owns the Player classId.
	 */
	public boolean canBeUsedBy(Player player)
	{
		return ArraysUtil.contains(_classes, player.getClassId().getId());
	}
}
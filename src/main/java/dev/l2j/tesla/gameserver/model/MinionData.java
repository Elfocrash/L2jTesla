package dev.l2j.tesla.gameserver.model;

import dev.l2j.tesla.commons.random.Rnd;

/**
 * This class defines the spawn data of a Minion type.<BR>
 * In a group mob, there are one master called RaidBoss and several slaves called Minions.
 */
public class MinionData
{
	/** The Identifier of the L2Minion */
	private int _minionId;
	
	/** The number of this Minion Type to spawn */
	private int _minionAmount;
	private int _minionAmountMin;
	private int _minionAmountMax;
	
	/**
	 * Set the Identifier of the Minion to spawn.
	 * @param id The Creature Identifier to spawn
	 */
	public void setMinionId(int id)
	{
		_minionId = id;
	}
	
	/**
	 * @return the Identifier of the Minion to spawn.
	 */
	public int getMinionId()
	{
		return _minionId;
	}
	
	/**
	 * Set the minimum of minions to amount.
	 * @param amountMin The minimum quantity of this Minion type to spawn
	 */
	public void setAmountMin(int amountMin)
	{
		_minionAmountMin = amountMin;
	}
	
	/**
	 * Set the maximum of minions to amount.
	 * @param amountMax The maximum quantity of this Minion type to spawn
	 */
	public void setAmountMax(int amountMax)
	{
		_minionAmountMax = amountMax;
	}
	
	/**
	 * Set the amount of this Minion type to spawn.
	 * @param amount The quantity of this Minion type to spawn
	 */
	public void setAmount(int amount)
	{
		_minionAmount = amount;
	}
	
	/**
	 * @return the amount of this Minion type to spawn.
	 */
	public int getAmount()
	{
		if (_minionAmountMax > _minionAmountMin)
		{
			_minionAmount = Rnd.get(_minionAmountMin, _minionAmountMax);
			return _minionAmount;
		}
		
		return _minionAmountMin;
	}
}
package dev.l2j.tesla.gameserver.model.actor.stat;

import dev.l2j.tesla.gameserver.model.actor.Boat;

public class BoatStat extends CreatureStat
{
	private int _moveSpeed = 0;
	private int _rotationSpeed = 0;
	
	public BoatStat(Boat activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public float getMoveSpeed()
	{
		return _moveSpeed;
	}
	
	public final void setMoveSpeed(int speed)
	{
		_moveSpeed = speed;
	}
	
	public final int getRotationSpeed()
	{
		return _rotationSpeed;
	}
	
	public final void setRotationSpeed(int speed)
	{
		_rotationSpeed = speed;
	}
}
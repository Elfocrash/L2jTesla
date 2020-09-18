package dev.l2j.tesla.gameserver.model.olympiad;

import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * A model containing all informations related to a single {@link Olympiad} Participant.
 */
public final class Participant
{
	private final int _objectId;
	private final String _name;
	private final int _side;
	private final int _baseClass;
	private final StatsSet _stats;
	
	private boolean _isDisconnected = false;
	private boolean _isDefecting = false;
	private Player _player;
	
	public Participant(Player player, int side)
	{
		_objectId = player.getObjectId();
		_player = player;
		_name = player.getName();
		_side = side;
		_baseClass = player.getBaseClass();
		_stats = Olympiad.getInstance().getNobleStats(_objectId);
	}
	
	public Participant(int objectId, int side)
	{
		_objectId = objectId;
		_player = null;
		_name = "-";
		_side = side;
		_baseClass = 0;
		_stats = null;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getSide()
	{
		return _side;
	}
	
	public int getBaseClass()
	{
		return _baseClass;
	}
	
	public StatsSet getStats()
	{
		return _stats;
	}
	
	public boolean isDisconnected()
	{
		return _isDisconnected;
	}
	
	public void setDisconnection(boolean isDisconnected)
	{
		_isDisconnected = isDisconnected;
	}
	
	public boolean isDefecting()
	{
		return _isDefecting;
	}
	
	public void setDefection(boolean isDefecting)
	{
		_isDefecting = isDefecting;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public void setPlayer(Player player)
	{
		_player = player;
	}
	
	public final void updatePlayer()
	{
		if (_player == null || !_player.isOnline())
			_player = World.getInstance().getPlayer(_objectId);
	}
	
	public final void updateStat(String statName, int increment)
	{
		_stats.set(statName, Math.max(_stats.getInteger(statName) + increment, 0));
	}
}
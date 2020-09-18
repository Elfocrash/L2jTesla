package dev.l2j.tesla.gameserver.model.partymatching;

import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * @author Gnacik
 */
public class PartyMatchWaitingList
{
	private final List<Player> _members;
	
	protected PartyMatchWaitingList()
	{
		_members = new ArrayList<>();
	}
	
	public void addPlayer(Player player)
	{
		if (!_members.contains(player))
			_members.add(player);
	}
	
	public void removePlayer(Player player)
	{
		if (_members.contains(player))
			_members.remove(player);
	}
	
	public List<Player> getPlayers()
	{
		return _members;
	}
	
	public static PartyMatchWaitingList getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PartyMatchWaitingList _instance = new PartyMatchWaitingList();
	}
}
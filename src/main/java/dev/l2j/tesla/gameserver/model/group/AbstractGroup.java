package dev.l2j.tesla.gameserver.model.group;

import java.util.List;

import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.CreatureSay;
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.Player;

public abstract class AbstractGroup
{
	private Player _leader;
	private int _level;
	
	public AbstractGroup(Player leader)
	{
		_leader = leader;
	}
	
	/**
	 * @return a list of all members of this group.
	 */
	public abstract List<Player> getMembers();
	
	/**
	 * @return the count of all players in this group.
	 */
	public abstract int getMembersCount();
	
	/**
	 * Check if this group contains a given player.
	 * @param player : the player to check.
	 * @return {@code true} if this group contains the specified player, {@code false} otherwise.
	 */
	public abstract boolean containsPlayer(final WorldObject player);
	
	/**
	 * Broadcast a packet to every member of this group.
	 * @param packet : the packet to broadcast.
	 */
	public abstract void broadcastPacket(final L2GameServerPacket packet);
	
	/**
	 * Broadcast a CreatureSay packet to every member of this group. Similar to broadcastPacket, but with an embbed BlockList check.
	 * @param msg : the msg to broadcast.
	 * @param broadcaster : the player who broadcasts the message.
	 */
	public abstract void broadcastCreatureSay(final CreatureSay msg, final Player broadcaster);
	
	/**
	 * Recalculate the group level.
	 */
	public abstract void recalculateLevel();
	
	/**
	 * Destroy that group, resetting all possible values, leading to that group object destruction.
	 */
	public abstract void disband();
	
	/**
	 * @return the level of this group.
	 */
	public int getLevel()
	{
		return _level;
	}
	
	/**
	 * Change the level of this group. <b>Used only when the group is created.</b>
	 * @param level : the level to set.
	 */
	public void setLevel(int level)
	{
		_level = level;
	}
	
	/**
	 * @return the leader of this group.
	 */
	public Player getLeader()
	{
		return _leader;
	}
	
	/**
	 * Change the leader of this group to the specified player.
	 * @param leader : the player to set as the new leader of this group.
	 */
	public void setLeader(Player leader)
	{
		_leader = leader;
	}
	
	/**
	 * @return the leader objectId.
	 */
	public int getLeaderObjectId()
	{
		return _leader.getObjectId();
	}
	
	/**
	 * Check if a given player is the leader of this group.
	 * @param player : the player to check.
	 * @return {@code true} if the specified player is the leader of this group, {@code false} otherwise.
	 */
	public boolean isLeader(Player player)
	{
		return _leader.getObjectId() == player.getObjectId();
	}
	
	/**
	 * Broadcast a system message to this group.
	 * @param message : the system message to broadcast.
	 */
	public void broadcastMessage(SystemMessageId message)
	{
		broadcastPacket(SystemMessage.getSystemMessage(message));
	}
	
	/**
	 * Broadcast a custom text message to this group.
	 * @param text : the custom string to broadcast.
	 */
	public void broadcastString(String text)
	{
		broadcastPacket(SystemMessage.sendString(text));
	}
	
	/**
	 * @return a random member of this group.
	 */
	public Player getRandomPlayer()
	{
		return Rnd.get(getMembers());
	}
}
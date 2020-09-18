package dev.l2j.tesla.gameserver.model.group;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.CreatureSay;
import dev.l2j.tesla.gameserver.network.serverpackets.ExCloseMPCC;
import dev.l2j.tesla.gameserver.network.serverpackets.ExMPCCPartyInfoUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.ExOpenMPCC;
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * Mass events, like sieges or raids require joining several {@link Party}. That's what {@link CommandChannel}s are for in the world of Lineage.<br>
 * <br>
 * Using a command channel:
 * <ul>
 * <li>A command channel can only be created by a character belonging to a clan level 4 and above. The clan needs to learn the Clan Imperium skill.</li>
 * <li>It takes at least 2 parties to create a command channel. One of the leaders need to use the /channelinvite command on any member of the other party.</li>
 * <li>Only the leader of a command channel can accept new parties to the CC.</li>
 * <li>If the main party is disbanded, the CC is disbanded as well.</li>
 * <li>If the CC leader is disconnected from the channel, a random player from the main party will become the new leader.</li>
 * <li>You can change the leader of the CC and the leader of the main party at the same time using the /changepartyleader [Character Name] command or from the Action Window.</li>
 * </ul>
 */
public class CommandChannel extends AbstractGroup
{
	private final List<Party> _parties = new CopyOnWriteArrayList<>();
	
	public CommandChannel(Party requestor, Party target)
	{
		super(requestor.getLeader());
		
		_parties.add(requestor);
		_parties.add(target);
		
		requestor.setCommandChannel(this);
		target.setCommandChannel(this);
		
		recalculateLevel();
		
		for (Player member : requestor.getMembers())
		{
			member.sendPacket(SystemMessageId.COMMAND_CHANNEL_FORMED);
			member.sendPacket(ExOpenMPCC.STATIC_PACKET);
		}
		
		for (Player member : target.getMembers())
		{
			member.sendPacket(SystemMessageId.JOINED_COMMAND_CHANNEL);
			member.sendPacket(ExOpenMPCC.STATIC_PACKET);
		}
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof CommandChannel))
			return false;
		
		if (obj == this)
			return true;
		
		return isLeader(((CommandChannel) obj).getLeader());
	}
	
	/**
	 * <b>BEWARE : create a temporary List. Uses containsPlayer whenever possible.</b>
	 */
	@Override
	public List<Player> getMembers()
	{
		final List<Player> members = new ArrayList<>();
		for (Party party : _parties)
			members.addAll(party.getMembers());
		
		return members;
	}
	
	@Override
	public int getMembersCount()
	{
		int count = 0;
		for (Party party : _parties)
			count += party.getMembersCount();
		
		return count;
	}
	
	@Override
	public boolean containsPlayer(WorldObject player)
	{
		for (Party party : _parties)
		{
			if (party.containsPlayer(player))
				return true;
		}
		return false;
	}
	
	@Override
	public void broadcastPacket(final L2GameServerPacket packet)
	{
		for (Party party : _parties)
			party.broadcastPacket(packet);
	}
	
	@Override
	public void broadcastCreatureSay(final CreatureSay msg, final Player broadcaster)
	{
		for (Party party : _parties)
			party.broadcastCreatureSay(msg, broadcaster);
	}
	
	@Override
	public void recalculateLevel()
	{
		int newLevel = 0;
		for (Party party : _parties)
		{
			if (party.getLevel() > newLevel)
				newLevel = party.getLevel();
		}
		setLevel(newLevel);
	}
	
	@Override
	public void disband()
	{
		for (Party party : _parties)
		{
			party.setCommandChannel(null);
			party.broadcastPacket(ExCloseMPCC.STATIC_PACKET);
			party.broadcastMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED);
		}
		_parties.clear();
	}
	
	/**
	 * Adds a {@link Party} to this {@link CommandChannel}.
	 * @param party : the Party to add.
	 */
	public void addParty(Party party)
	{
		// Null party or party is already registered in this command channel.
		if (party == null || _parties.contains(party))
			return;
		
		// Update the CCinfo for existing players.
		broadcastPacket(new ExMPCCPartyInfoUpdate(party, 1));
		
		_parties.add(party);
		
		if (party.getLevel() > getLevel())
			setLevel(party.getLevel());
		
		party.setCommandChannel(this);
		
		for (Player member : party.getMembers())
		{
			member.sendPacket(SystemMessageId.JOINED_COMMAND_CHANNEL);
			member.sendPacket(ExOpenMPCC.STATIC_PACKET);
		}
	}
	
	/**
	 * Removes a {@link Party} from this {@link CommandChannel}.
	 * @param party : the Party to remove. Disband the CommandChannel if there was only 2 parties left.
	 * @return true if the Party has been successfully removed from CommandChannel.
	 */
	public boolean removeParty(Party party)
	{
		// Null party or party isn't registered in this command channel.
		if (party == null || !_parties.contains(party))
			return false;
		
		// Don't bother individually drop parties, disband entirely if there is only 2 parties in command channel.
		if (_parties.size() == 2)
			disband();
		else
		{
			_parties.remove(party);
			
			party.setCommandChannel(null);
			party.broadcastPacket(ExCloseMPCC.STATIC_PACKET);
			
			recalculateLevel();
			
			// Update the CCinfo for existing players.
			broadcastPacket(new ExMPCCPartyInfoUpdate(party, 0));
		}
		return true;
	}
	
	/**
	 * @return the {@link List} of {@link Party} registered in this {@link CommandChannel}.
	 */
	public List<Party> getParties()
	{
		return _parties;
	}
	
	/**
	 * @param attackable : the {@link Attackable} to check.
	 * @return true if the members count is reached.
	 */
	public boolean meetRaidWarCondition(Attackable attackable)
	{
		switch (attackable.getNpcId())
		{
			case 29001: // Queen Ant
			case 29006: // Core
			case 29014: // Orfen
			case 29022: // Zaken
				return getMembersCount() > 36;
			
			case 29020: // Baium
				return getMembersCount() > 56;
			
			case 29019: // Antharas
				return getMembersCount() > 225;
			
			case 29028: // Valakas
				return getMembersCount() > 99;
			
			default: // normal Raidboss
				return getMembersCount() > 18;
		}
	}
}
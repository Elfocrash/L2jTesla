package dev.l2j.tesla.gameserver.model.partymatching;

import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.ExManagePartyRoomMember;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.data.xml.MapRegionData;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class PartyMatchRoom
{
	private final int _id;
	private String _title;
	private int _loot;
	private int _location;
	private int _minlvl;
	private int _maxlvl;
	private int _maxmem;
	private final List<Player> _members = new ArrayList<>();
	
	public PartyMatchRoom(int id, String title, int loot, int minlvl, int maxlvl, int maxmem, Player owner)
	{
		_id = id;
		_title = title;
		_loot = loot;
		_location = MapRegionData.getInstance().getClosestLocation(owner.getX(), owner.getY());
		_minlvl = minlvl;
		_maxlvl = maxlvl;
		_maxmem = maxmem;
		_members.add(owner);
	}
	
	public List<Player> getPartyMembers()
	{
		return _members;
	}
	
	public void addMember(Player player)
	{
		_members.add(player);
	}
	
	public void deleteMember(Player player)
	{
		if (player != getOwner())
		{
			_members.remove(player);
			notifyMembersAboutExit(player);
		}
		else if (_members.size() == 1)
		{
			PartyMatchRoomList.getInstance().deleteRoom(_id);
		}
		else
		{
			changeLeader(_members.get(1));
			deleteMember(player);
		}
	}
	
	public void notifyMembersAboutExit(Player player)
	{
		for (Player _member : getPartyMembers())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_LEFT_PARTY_ROOM);
			sm.addCharName(player);
			_member.sendPacket(sm);
			_member.sendPacket(new ExManagePartyRoomMember(player, this, 2));
		}
	}
	
	public void changeLeader(Player newLeader)
	{
		// Get current leader
		Player oldLeader = _members.get(0);
		// Remove new leader
		_members.remove(newLeader);
		// Move him to first position
		_members.set(0, newLeader);
		// Add old leader as normal member
		_members.add(oldLeader);
		// Broadcast change
		for (Player member : getPartyMembers())
		{
			member.sendPacket(new ExManagePartyRoomMember(newLeader, this, 1));
			member.sendPacket(new ExManagePartyRoomMember(oldLeader, this, 1));
			member.sendPacket(SystemMessageId.PARTY_ROOM_LEADER_CHANGED);
		}
	}
	
	public int getId()
	{
		return _id;
	}
	
	public Player getOwner()
	{
		return _members.get(0);
	}
	
	public int getMembers()
	{
		return _members.size();
	}
	
	public int getLootType()
	{
		return _loot;
	}
	
	public void setLootType(int loot)
	{
		_loot = loot;
	}
	
	public int getMinLvl()
	{
		return _minlvl;
	}
	
	public void setMinLvl(int minlvl)
	{
		_minlvl = minlvl;
	}
	
	public int getMaxLvl()
	{
		return _maxlvl;
	}
	
	public void setMaxLvl(int maxlvl)
	{
		_maxlvl = maxlvl;
	}
	
	public int getLocation()
	{
		return _location;
	}
	
	public void setLocation(int loc)
	{
		_location = loc;
	}
	
	public int getMaxMembers()
	{
		return _maxmem;
	}
	
	public void setMaxMembers(int maxmem)
	{
		_maxmem = maxmem;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public void setTitle(String title)
	{
		_title = title;
	}
}
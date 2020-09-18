package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.partymatching.PartyMatchRoom;

/**
 * @author Gnacik
 */
public class PartyMatchDetail extends L2GameServerPacket
{
	private final PartyMatchRoom _room;
	
	public PartyMatchDetail(PartyMatchRoom room)
	{
		_room = room;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x97);
		writeD(_room.getId()); // Room ID
		writeD(_room.getMaxMembers()); // Max Members
		writeD(_room.getMinLvl()); // Level Min
		writeD(_room.getMaxLvl()); // Level Max
		writeD(_room.getLootType()); // Loot Type
		writeD(_room.getLocation()); // Room Location
		writeS(_room.getTitle()); // Room title
	}
}
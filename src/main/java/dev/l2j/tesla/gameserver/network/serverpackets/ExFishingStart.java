package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.location.Location;

/**
 * Format (ch)ddddd
 * @author -Wooden-
 */
public class ExFishingStart extends L2GameServerPacket
{
	private final Creature _activeChar;
	private final Location _loc;
	private final int _fishType;
	private final boolean _isNightLure;
	
	public ExFishingStart(Creature character, int fishType, Location loc, boolean isNightLure)
	{
		_activeChar = character;
		_fishType = fishType;
		_loc = loc;
		_isNightLure = isNightLure;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x13);
		writeD(_activeChar.getObjectId());
		writeD(_fishType); // fish type
		writeD(_loc.getX()); // x position
		writeD(_loc.getY()); // y position
		writeD(_loc.getZ()); // z position
		writeC(_isNightLure ? 0x01 : 0x00); // night lure
		writeC(Config.ALT_FISH_CHAMPIONSHIP_ENABLED ? 0x01 : 0x00); // show fish rank result button
	}
}
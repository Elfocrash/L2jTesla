package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.data.manager.SevenSignsManager;

public class ShowMiniMap extends L2GameServerPacket
{
	public static final ShowMiniMap REGULAR_MAP = new ShowMiniMap(1665);
	
	private final int _mapId;
	
	public ShowMiniMap(int mapId)
	{
		_mapId = mapId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9d);
		writeD(_mapId);
		writeD(SevenSignsManager.getInstance().getCurrentPeriod().ordinal());
	}
}
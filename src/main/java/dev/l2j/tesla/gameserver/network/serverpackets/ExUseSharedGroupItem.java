package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * Format: ch dddd
 * @author KenM
 */
public class ExUseSharedGroupItem extends L2GameServerPacket
{
	private final int _itemId, _grpId, _remainedTime, _totalTime;
	
	public ExUseSharedGroupItem(int itemId, int grpId, int remainedTime, int totalTime)
	{
		_itemId = itemId;
		_grpId = grpId;
		_remainedTime = remainedTime / 1000;
		_totalTime = totalTime / 1000;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x49);
		
		writeD(_itemId);
		writeD(_grpId);
		writeD(_remainedTime);
		writeD(_totalTime);
	}
}
package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * Format: ch ddcdc
 * @author KenM
 */
public class ExPCCafePointInfo extends L2GameServerPacket
{
	private final int _score, _modify, _periodType, _remainingTime;
	private int _pointType = 0;
	
	public ExPCCafePointInfo(int score, int modify, boolean addPoint, boolean pointType, int remainingTime)
	{
		_score = score;
		_modify = addPoint ? modify : modify * -1;
		_remainingTime = remainingTime;
		_pointType = addPoint ? (pointType ? 0 : 1) : 2;
		_periodType = 1; // get point time
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x31);
		writeD(_score);
		writeD(_modify);
		writeC(_periodType);
		writeD(_remainingTime);
		writeC(_pointType);
	}
}
package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.ArrayList;

public class ExEnchantSkillInfo extends L2GameServerPacket
{
	private final ArrayList<Req> _reqs;
	private final int _id;
	private final int _level;
	private final int _spCost;
	private final int _xpCost;
	private final int _rate;
	
	class Req
	{
		public int id;
		public int count;
		public int type;
		public int unk;
		
		Req(int pType, int pId, int pCount, int pUnk)
		{
			id = pId;
			type = pType;
			count = pCount;
			unk = pUnk;
		}
	}
	
	public ExEnchantSkillInfo(int id, int level, int spCost, int xpCost, int rate)
	{
		_reqs = new ArrayList<>();
		_id = id;
		_level = level;
		_spCost = spCost;
		_xpCost = xpCost;
		_rate = rate;
	}
	
	public void addRequirement(int type, int id, int count, int unk)
	{
		_reqs.add(new Req(type, id, count, unk));
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x18);
		
		writeD(_id);
		writeD(_level);
		writeD(_spCost);
		writeQ(_xpCost);
		writeD(_rate);
		
		writeD(_reqs.size());
		
		for (Req temp : _reqs)
		{
			writeD(temp.type);
			writeD(temp.id);
			writeD(temp.count);
			writeD(temp.unk);
		}
	}
}
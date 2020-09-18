package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.data.manager.CastleManorManager;
import dev.l2j.tesla.gameserver.model.manor.SeedProduction;

public final class BuyListSeed extends L2GameServerPacket
{
	private final int _manorId;
	private final List<SeedProduction> _list;
	private final int _money;
	
	public BuyListSeed(int currentMoney, int castleId)
	{
		_money = currentMoney;
		_manorId = castleId;
		
		_list = new ArrayList<>();
		for (SeedProduction s : CastleManorManager.getInstance().getSeedProduction(castleId, false))
		{
			if (s.getAmount() > 0 && s.getPrice() > 0)
				_list.add(s);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE8);
		
		writeD(_money);
		writeD(_manorId);
		
		if (!_list.isEmpty())
		{
			writeH(_list.size());
			for (SeedProduction s : _list)
			{
				writeH(0x04);
				writeD(s.getId());
				writeD(s.getId());
				writeD(s.getAmount());
				writeH(0x04);
				writeH(0x00);
				writeD(s.getPrice());
			}
		}
	}
}
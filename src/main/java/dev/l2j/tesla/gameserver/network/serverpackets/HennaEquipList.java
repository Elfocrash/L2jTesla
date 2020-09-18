package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.List;
import java.util.stream.Collectors;

import dev.l2j.tesla.gameserver.data.xml.HennaData;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.Henna;

public class HennaEquipList extends L2GameServerPacket
{
	private final int _adena;
	private final int _maxHennas;
	private final List<Henna> _availableHennas;
	
	public HennaEquipList(Player player)
	{
		_adena = player.getAdena();
		_maxHennas = player.getHennaList().getMaxSize();
		_availableHennas = HennaData.getInstance().getHennas().stream().filter(h -> h.canBeUsedBy(player) && player.getInventory().getItemByItemId(h.getDyeId()) != null).collect(Collectors.toList());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe2);
		writeD(_adena);
		writeD(_maxHennas);
		writeD(_availableHennas.size());
		
		for (Henna temp : _availableHennas)
		{
			writeD(temp.getSymbolId());
			writeD(temp.getDyeId());
			writeD(Henna.DRAW_AMOUNT);
			writeD(temp.getDrawPrice());
			writeD(1);
		}
	}
}
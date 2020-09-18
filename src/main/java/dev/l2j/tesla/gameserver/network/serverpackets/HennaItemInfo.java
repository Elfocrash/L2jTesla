package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.Henna;

public class HennaItemInfo extends L2GameServerPacket
{
	private final Henna _henna;
	private final int _adena;
	private final int _int, _str, _con, _men, _dex, _wit;
	
	public HennaItemInfo(Henna henna, Player player)
	{
		_henna = henna;
		_adena = player.getAdena();
		_int = player.getINT();
		_str = player.getSTR();
		_con = player.getCON();
		_men = player.getMEN();
		_dex = player.getDEX();
		_wit = player.getWIT();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe3);
		writeD(_henna.getSymbolId());
		writeD(_henna.getDyeId());
		writeD(Henna.DRAW_AMOUNT);
		writeD(_henna.getDrawPrice());
		writeD(1);
		writeD(_adena);
		writeD(_int);
		writeC(_int + _henna.getINT());
		writeD(_str);
		writeC(_str + _henna.getSTR());
		writeD(_con);
		writeC(_con + _henna.getCON());
		writeD(_men);
		writeC(_men + _henna.getMEN());
		writeD(_dex);
		writeC(_dex + _henna.getDEX());
		writeD(_wit);
		writeC(_wit + _henna.getWIT());
	}
}
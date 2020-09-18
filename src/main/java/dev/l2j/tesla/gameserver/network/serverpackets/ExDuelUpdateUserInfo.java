package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.actor.Player;

public class ExDuelUpdateUserInfo extends L2GameServerPacket
{
	private final Player _player;
	
	public ExDuelUpdateUserInfo(Player player)
	{
		_player = player;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4f);
		writeS(_player.getName());
		writeD(_player.getObjectId());
		writeD(_player.getClassId().getId());
		writeD(_player.getLevel());
		writeD((int) _player.getCurrentHp());
		writeD(_player.getMaxHp());
		writeD((int) _player.getCurrentMp());
		writeD(_player.getMaxMp());
		writeD((int) _player.getCurrentCp());
		writeD(_player.getMaxCp());
	}
}
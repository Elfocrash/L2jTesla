package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.taskmanager.GameTimeTaskManager;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class CharSelected extends L2GameServerPacket
{
	private final Player _activeChar;
	private final int _sessionId;
	
	public CharSelected(Player cha, int sessionId)
	{
		_activeChar = cha;
		_sessionId = sessionId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x15);
		
		writeS(_activeChar.getName());
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getTitle());
		writeD(_sessionId);
		writeD(_activeChar.getClanId());
		
		writeD(0x00); // unknown
		
		writeD(_activeChar.getAppearance().getSex().ordinal());
		writeD(_activeChar.getRace().ordinal());
		writeD(_activeChar.getClassId().getId());
		
		writeD(0x01);
		
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
		writeF(_activeChar.getCurrentHp());
		writeF(_activeChar.getCurrentMp());
		writeD(_activeChar.getSp());
		writeQ(_activeChar.getExp());
		writeD(_activeChar.getLevel());
		writeD(_activeChar.getKarma());
		writeD(_activeChar.getPkKills());
		writeD(_activeChar.getINT());
		writeD(_activeChar.getSTR());
		writeD(_activeChar.getCON());
		writeD(_activeChar.getMEN());
		writeD(_activeChar.getDEX());
		writeD(_activeChar.getWIT());
		
		for (int i = 0; i < 30; i++)
		{
			writeD(0x00);
		}
		
		writeD(0x00); // c3 work
		writeD(0x00); // c3 work
		
		writeD(GameTimeTaskManager.getInstance().getGameTime());
		
		writeD(0x00); // c3
		
		writeD(_activeChar.getClassId().getId());
		
		writeD(0x00); // c3 InspectorBin
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
	}
}
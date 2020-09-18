package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.network.serverpackets.CharTemplates;
import dev.l2j.tesla.gameserver.data.xml.PlayerData;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;

public final class NewCharacter extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		CharTemplates ct = new CharTemplates();
		
		ct.addChar(PlayerData.getInstance().getTemplate(0));
		ct.addChar(PlayerData.getInstance().getTemplate(ClassId.HUMAN_FIGHTER));
		ct.addChar(PlayerData.getInstance().getTemplate(ClassId.HUMAN_MYSTIC));
		ct.addChar(PlayerData.getInstance().getTemplate(ClassId.ELVEN_FIGHTER));
		ct.addChar(PlayerData.getInstance().getTemplate(ClassId.ELVEN_MYSTIC));
		ct.addChar(PlayerData.getInstance().getTemplate(ClassId.DARK_FIGHTER));
		ct.addChar(PlayerData.getInstance().getTemplate(ClassId.DARK_MYSTIC));
		ct.addChar(PlayerData.getInstance().getTemplate(ClassId.ORC_FIGHTER));
		ct.addChar(PlayerData.getInstance().getTemplate(ClassId.ORC_MYSTIC));
		ct.addChar(PlayerData.getInstance().getTemplate(ClassId.DWARVEN_FIGHTER));
		
		sendPacket(ct);
	}
}
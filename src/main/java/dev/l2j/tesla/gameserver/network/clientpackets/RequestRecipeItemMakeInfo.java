package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.network.serverpackets.RecipeItemMakeInfo;
import dev.l2j.tesla.gameserver.model.actor.Player;

public final class RequestRecipeItemMakeInfo extends L2GameClientPacket
{
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getPlayer();
		if (activeChar == null)
			return;
		
		activeChar.sendPacket(new RecipeItemMakeInfo(_id, activeChar));
	}
}
package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.network.serverpackets.PrivateStoreMsgSell;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class SetPrivateStoreMsgSell extends L2GameClientPacket
{
	private static final int MAX_MSG_LENGTH = 29;
	
	private String _storeMsg;
	
	@Override
	protected void readImpl()
	{
		_storeMsg = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null || player.getSellList() == null)
			return;
		
		if (_storeMsg != null && _storeMsg.length() > MAX_MSG_LENGTH)
			return;
		
		player.getSellList().setTitle(_storeMsg);
		sendPacket(new PrivateStoreMsgSell(player));
	}
}
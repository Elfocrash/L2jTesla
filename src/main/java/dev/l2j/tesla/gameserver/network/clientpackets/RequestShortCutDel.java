package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.model.actor.Player;

public final class RequestShortCutDel extends L2GameClientPacket
{
	private int _slot;
	private int _page;
	
	@Override
	protected void readImpl()
	{
		int id = readD();
		_slot = id % 12;
		_page = id / 12;
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (_page < 0 || _page > 9)
			return;
		
		player.getShortcutList().deleteShortcut(_slot, _page);
	}
}
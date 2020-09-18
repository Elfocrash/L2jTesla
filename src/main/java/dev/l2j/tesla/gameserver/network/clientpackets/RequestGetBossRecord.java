package dev.l2j.tesla.gameserver.network.clientpackets;

import java.util.Map;

import dev.l2j.tesla.gameserver.network.serverpackets.ExGetBossRecord;
import dev.l2j.tesla.gameserver.data.manager.RaidPointManager;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class RequestGetBossRecord extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _bossId;
	
	@Override
	protected void readImpl()
	{
		_bossId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		final int points = RaidPointManager.getInstance().getPointsByOwnerId(player.getObjectId());
		final int ranking = RaidPointManager.getInstance().calculateRanking(player.getObjectId());
		final Map<Integer, Integer> list = RaidPointManager.getInstance().getList(player);
		
		player.sendPacket(new ExGetBossRecord(ranking, points, list));
	}
}
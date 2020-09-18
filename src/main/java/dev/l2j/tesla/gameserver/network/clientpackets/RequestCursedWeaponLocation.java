package dev.l2j.tesla.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.network.serverpackets.ExCursedWeaponLocation;
import dev.l2j.tesla.gameserver.data.manager.CursedWeaponManager;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.entity.CursedWeapon;
import dev.l2j.tesla.gameserver.model.location.Location;

public final class RequestCursedWeaponLocation extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		final List<ExCursedWeaponLocation.CursedWeaponInfo> list = new ArrayList<>();
		for (CursedWeapon cw : CursedWeaponManager.getInstance().getCursedWeapons())
		{
			if (!cw.isActive())
				continue;
			
			final Location loc = cw.getWorldPosition();
			if (loc != null)
				list.add(new ExCursedWeaponLocation.CursedWeaponInfo(loc, cw.getItemId(), (cw.isActivated()) ? 1 : 0));
		}
		
		if (!list.isEmpty())
			player.sendPacket(new ExCursedWeaponLocation(list));
	}
}
package dev.l2j.tesla.gameserver.handler.itemhandlers;

import dev.l2j.tesla.gameserver.handler.IItemHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillUse;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.enums.items.ShotType;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

public class BeastSoulShot implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (playable == null)
			return;
		
		final Player player = playable.getActingPlayer();
		if (player == null)
			return;
		
		if (playable instanceof Summon)
		{
			player.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
			return;
		}
		
		final Summon summon = player.getSummon();
		if (summon == null)
		{
			player.sendPacket(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return;
		}
		
		if (summon.isDead())
		{
			player.sendPacket(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET);
			return;
		}
		
		// SoulShots are already active.
		if (summon.isChargedShot(ShotType.SOULSHOT))
			return;
		
		// If the player doesn't have enough beast soulshot remaining, remove any auto soulshot task.
		if (!player.destroyItemWithoutTrace("Consume", item.getObjectId(), summon.getSoulShotsPerHit(), null, false))
		{
			if (!player.disableAutoShot(item.getItemId()))
				player.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET);
			
			return;
		}
		
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1).addItemName(item.getItemId()));
		summon.setChargedShot(ShotType.SOULSHOT, true);
		player.broadcastPacketInRadius(new MagicSkillUse(summon, summon, 2033, 1, 0, 0), 600);
	}
}
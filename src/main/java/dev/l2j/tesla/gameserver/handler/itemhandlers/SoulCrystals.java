package dev.l2j.tesla.gameserver.handler.itemhandlers;

import dev.l2j.tesla.gameserver.handler.IItemHandler;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.kind.EtcItem;

/**
 * Template for item skills handler.
 * @author Hasha
 */
public class SoulCrystals implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final EtcItem etcItem = item.getEtcItem();
		
		final IntIntHolder[] skills = etcItem.getSkills();
		if (skills == null)
			return;
		
		final L2Skill itemSkill = skills[0].getSkill();
		if (itemSkill == null || itemSkill.getId() != 2096)
			return;
		
		final Player player = (Player) playable;
		
		if (player.isCastingNow())
			return;
		
		if (!itemSkill.checkCondition(player, player.getTarget(), false))
			return;
		
		// No message on retail, the use is just forgotten.
		if (player.isSkillDisabled(itemSkill))
			return;
		
		player.getAI().setIntention(IntentionType.IDLE);
		if (!player.useMagic(itemSkill, forceUse, false))
			return;
		
		int reuseDelay = itemSkill.getReuseDelay();
		if (etcItem.getReuseDelay() > reuseDelay)
			reuseDelay = etcItem.getReuseDelay();
		
		player.addTimeStamp(itemSkill, reuseDelay);
		if (reuseDelay != 0)
			player.disableSkill(itemSkill, reuseDelay);
	}
}
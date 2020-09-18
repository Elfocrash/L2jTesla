package dev.l2j.tesla.gameserver.handler.itemhandlers;

import dev.l2j.tesla.gameserver.handler.IItemHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.ExUseSharedGroupItem;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.items.EtcItemType;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.Pet;
import dev.l2j.tesla.gameserver.model.actor.instance.Servitor;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

/**
 * Template for item skills handler.
 */
public class ItemSkills implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (playable instanceof Servitor)
			return;
		
		final boolean isPet = playable instanceof Pet;
		final Player activeChar = playable.getActingPlayer();
		
		// Pets can only use tradable items.
		if (isPet && !item.isTradable())
		{
			activeChar.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return;
		}
		
		final IntIntHolder[] skills = item.getEtcItem().getSkills();
		if (skills == null)
		{
			LOGGER.warn("{} doesn't have any registered skill for handler.", item.getName());
			return;
		}
		
		for (IntIntHolder skillInfo : skills)
		{
			if (skillInfo == null)
				continue;
			
			final L2Skill itemSkill = skillInfo.getSkill();
			if (itemSkill == null)
				continue;
			
			if (!itemSkill.checkCondition(playable, playable.getTarget(), false))
				return;
			
			// No message on retail, the use is just forgotten.
			if (playable.isSkillDisabled(itemSkill))
				return;
			
			if (!itemSkill.isPotion() && playable.isCastingNow())
				return;
			
			// Item consumption is setup here.
			if (itemSkill.isPotion() || itemSkill.isSimultaneousCast())
			{
				if (!item.isHerb())
				{
					// Normal item consumption is 1, if more, it must be given in DP with getItemConsume().
					if (!playable.destroyItem("Consume", item.getObjectId(), (itemSkill.getItemConsumeId() == 0 && itemSkill.getItemConsume() > 0) ? itemSkill.getItemConsume() : 1, null, false))
					{
						activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
						return;
					}
				}
				
				playable.doSimultaneousCast(itemSkill);
				// Summons should be affected by herbs too, self time effect is handled at L2Effect constructor.
				if (!isPet && item.getItemType() == EtcItemType.HERB && activeChar.hasServitor())
					activeChar.getSummon().doSimultaneousCast(itemSkill);
			}
			else
			{
				// Normal item consumption is 1, if more, it must be given in DP with getItemConsume().
				if (!playable.destroyItem("Consume", item.getObjectId(), (itemSkill.getItemConsumeId() == 0 && itemSkill.getItemConsume() > 0) ? itemSkill.getItemConsume() : 1, null, false))
				{
					activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					return;
				}
				
				playable.getAI().setIntention(IntentionType.IDLE);
				if (!playable.useMagic(itemSkill, forceUse, false))
					return;
			}
			
			// Send message to owner.
			if (isPet)
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1).addSkillName(itemSkill));
			else
			{
				final int skillId = skillInfo.getId();
				
				// Buff icon for healing potions.
				switch (skillId)
				{
					case 2031:
					case 2032:
					case 2037:
						final int buffId = activeChar.getShortBuffTaskSkillId();
						
						// Greater healing potions.
						if (skillId == 2037)
							activeChar.shortBuffStatusUpdate(skillId, skillInfo.getValue(), itemSkill.getBuffDuration() / 1000);
						// Healing potions.
						else if (skillId == 2032 && buffId != 2037)
							activeChar.shortBuffStatusUpdate(skillId, skillInfo.getValue(), itemSkill.getBuffDuration() / 1000);
						// Lesser healing potions.
						else
						{
							if (buffId != 2037 && buffId != 2032)
								activeChar.shortBuffStatusUpdate(skillId, skillInfo.getValue(), itemSkill.getBuffDuration() / 1000);
						}
						break;
				}
			}
			
			// Reuse.
			int reuseDelay = itemSkill.getReuseDelay();
			if (item.isEtcItem())
			{
				if (item.getEtcItem().getReuseDelay() > reuseDelay)
					reuseDelay = item.getEtcItem().getReuseDelay();
				
				playable.addTimeStamp(itemSkill, reuseDelay);
				if (reuseDelay != 0)
					playable.disableSkill(itemSkill, reuseDelay);
				
				if (!isPet)
				{
					final int group = item.getEtcItem().getSharedReuseGroup();
					if (group >= 0)
						activeChar.sendPacket(new ExUseSharedGroupItem(item.getItemId(), group, reuseDelay, reuseDelay));
				}
			}
			else if (reuseDelay > 0)
			{
				playable.addTimeStamp(itemSkill, reuseDelay);
				playable.disableSkill(itemSkill, reuseDelay);
			}
		}
	}
}
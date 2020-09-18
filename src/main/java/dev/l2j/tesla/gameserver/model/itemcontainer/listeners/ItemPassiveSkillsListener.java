package dev.l2j.tesla.gameserver.model.itemcontainer.listeners;

import dev.l2j.tesla.gameserver.network.serverpackets.SkillCoolTime;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.kind.Item;
import dev.l2j.tesla.gameserver.model.item.kind.Weapon;

public class ItemPassiveSkillsListener implements OnEquipListener
{
	private static ItemPassiveSkillsListener instance = new ItemPassiveSkillsListener();
	
	public static ItemPassiveSkillsListener getInstance()
	{
		return instance;
	}
	
	@Override
	public void onEquip(int slot, ItemInstance item, Playable actor)
	{
		final Player player = (Player) actor;
		final Item it = item.getItem();
		
		boolean update = false;
		boolean updateTimeStamp = false;
		
		if (it instanceof Weapon)
		{
			// Apply augmentation bonuses on equip
			if (item.isAugmented())
				item.getAugmentation().applyBonus(player);
			
			// Verify if the grade penalty is occuring. If yes, then forget +4 dual skills and SA attached to weapon.
			if (player.getSkillLevel(L2Skill.SKILL_EXPERTISE) < it.getCrystalType().getId())
				return;
			
			// Add skills bestowed from +4 Duals
			if (item.getEnchantLevel() >= 4)
			{
				final L2Skill enchant4Skill = ((Weapon) it).getEnchant4Skill();
				if (enchant4Skill != null)
				{
					player.addSkill(enchant4Skill, false);
					update = true;
				}
			}
		}
		
		final IntIntHolder[] skills = it.getSkills();
		if (skills != null)
		{
			for (IntIntHolder skillInfo : skills)
			{
				if (skillInfo == null)
					continue;
				
				final L2Skill itemSkill = skillInfo.getSkill();
				if (itemSkill != null)
				{
					player.addSkill(itemSkill, false);
					
					if (itemSkill.isActive())
					{
						if (!player.getReuseTimeStamp().containsKey(itemSkill.getReuseHashCode()))
						{
							final int equipDelay = itemSkill.getEquipDelay();
							if (equipDelay > 0)
							{
								player.addTimeStamp(itemSkill, equipDelay);
								player.disableSkill(itemSkill, equipDelay);
							}
						}
						updateTimeStamp = true;
					}
					update = true;
				}
			}
		}
		
		if (update)
		{
			player.sendSkillList();
			
			if (updateTimeStamp)
				player.sendPacket(new SkillCoolTime(player));
		}
	}
	
	@Override
	public void onUnequip(int slot, ItemInstance item, Playable actor)
	{
		final Player player = (Player) actor;
		final Item it = item.getItem();
		
		boolean update = false;
		
		if (it instanceof Weapon)
		{
			// Remove augmentation bonuses on unequip
			if (item.isAugmented())
				item.getAugmentation().removeBonus(player);
			
			// Remove skills bestowed from +4 Duals
			if (item.getEnchantLevel() >= 4)
			{
				final L2Skill enchant4Skill = ((Weapon) it).getEnchant4Skill();
				if (enchant4Skill != null)
				{
					player.removeSkill(enchant4Skill.getId(), false, enchant4Skill.isPassive() || enchant4Skill.isToggle());
					update = true;
				}
			}
		}
		
		final IntIntHolder[] skills = it.getSkills();
		if (skills != null)
		{
			for (IntIntHolder skillInfo : skills)
			{
				if (skillInfo == null)
					continue;
				
				final L2Skill itemSkill = skillInfo.getSkill();
				if (itemSkill != null)
				{
					boolean found = false;
					
					for (ItemInstance pItem : player.getInventory().getPaperdollItems())
					{
						if (pItem != null && it.getItemId() == pItem.getItemId())
						{
							found = true;
							break;
						}
					}
					
					if (!found)
					{
						player.removeSkill(itemSkill.getId(), false, itemSkill.isPassive() || itemSkill.isToggle());
						update = true;
					}
				}
			}
		}
		
		if (update)
			player.sendSkillList();
	}
}
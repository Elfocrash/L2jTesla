package dev.l2j.tesla.gameserver.model.multisell;

import java.util.ArrayList;
import java.util.LinkedList;

import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.kind.Armor;
import dev.l2j.tesla.gameserver.model.item.kind.Weapon;

/**
 * A dynamic layer of {@link ListContainer}, which holds the current {@link Npc} objectId for security reasons.<br>
 * <br>
 * It can also allow to check inventory content.
 */
public class PreparedListContainer extends ListContainer
{
	private int _npcObjectId = 0;
	
	public PreparedListContainer(ListContainer template, boolean inventoryOnly, Player player, Npc npc)
	{
		super(template.getId());
		
		setMaintainEnchantment(template.getMaintainEnchantment());
		setApplyTaxes(false);
		
		_npcsAllowed = template._npcsAllowed;
		
		double taxRate = 0;
		if (npc != null)
		{
			_npcObjectId = npc.getObjectId();
			if (template.getApplyTaxes() && npc.getCastle() != null && npc.getCastle().getOwnerId() > 0)
			{
				setApplyTaxes(true);
				taxRate = npc.getCastle().getTaxRate();
			}
		}
		
		if (inventoryOnly)
		{
			if (player == null)
				return;
			
			final ItemInstance[] items;
			if (getMaintainEnchantment())
				items = player.getInventory().getUniqueItemsByEnchantLevel(false, false, false);
			else
				items = player.getInventory().getUniqueItems(false, false, false);
			
			_entries = new LinkedList<>();
			for (ItemInstance item : items)
			{
				// only do the match up on equippable items that are not currently equipped
				// so for each appropriate item, produce a set of entries for the multisell list.
				if (!item.isEquipped() && (item.getItem() instanceof Armor || item.getItem() instanceof Weapon))
				{
					// loop through the entries to see which ones we wish to include
					for (Entry ent : template.getEntries())
					{
						// check ingredients of this entry to see if it's an entry we'd like to include.
						for (Ingredient ing : ent.getIngredients())
						{
							if (item.getItemId() == ing.getItemId())
							{
								_entries.add(new PreparedEntry(ent, item, getApplyTaxes(), getMaintainEnchantment(), taxRate));
								break; // next entry
							}
						}
					}
				}
			}
		}
		else
		{
			_entries = new ArrayList<>(template.getEntries().size());
			
			for (Entry ent : template.getEntries())
				_entries.add(new PreparedEntry(ent, null, getApplyTaxes(), false, taxRate));
		}
	}
	
	public final boolean checkNpcObjectId(int npcObjectId)
	{
		return (_npcObjectId != 0) ? _npcObjectId == npcObjectId : true;
	}
}
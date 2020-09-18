package dev.l2j.tesla.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.network.FloodProtectors;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.ItemList;
import dev.l2j.tesla.gameserver.network.serverpackets.StatusUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.model.L2Augmentation;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.Folk;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.itemcontainer.PcInventory;
import dev.l2j.tesla.gameserver.model.multisell.Entry;
import dev.l2j.tesla.gameserver.model.multisell.Ingredient;
import dev.l2j.tesla.gameserver.model.multisell.PreparedListContainer;

public class MultiSellChoose extends L2GameClientPacket
{
	// Special IDs.
	private static final int CLAN_REPUTATION = 65336;
	// private static final int PC_BANG_POINTS = 65436;
	
	private int _listId;
	private int _entryId;
	private int _amount;
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_entryId = readD();
		_amount = readD();
	}
	
	@Override
	public void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (!FloodProtectors.performAction(getClient(), FloodProtectors.Action.MULTISELL))
		{
			player.setMultiSell(null);
			return;
		}
		
		if (_amount < 1 || _amount > 9999)
		{
			player.setMultiSell(null);
			return;
		}
		
		final PreparedListContainer list = player.getMultiSell();
		if (list == null || list.getId() != _listId)
		{
			player.setMultiSell(null);
			return;
		}
		
		if (_entryId < 1 || _entryId > list.getEntries().size())
		{
			player.setMultiSell(null);
			return;
		}
		
		final Folk folk = player.getCurrentFolk();
		if ((folk != null && !list.isNpcAllowed(folk.getNpcId())) || (folk == null && list.isNpcOnly()))
		{
			player.setMultiSell(null);
			return;
		}
		
		if (folk != null && !folk.canInteract(player))
		{
			player.setMultiSell(null);
			return;
		}
		
		final PcInventory inv = player.getInventory();
		final Entry entry = list.getEntries().get(_entryId - 1); // Entry Id begins from 1. We currently use entry IDs as index pointer.
		if (entry == null)
		{
			player.setMultiSell(null);
			return;
		}
		
		if (!entry.isStackable() && _amount > 1)
		{
			player.setMultiSell(null);
			return;
		}
		
		int slots = 0;
		int weight = 0;
		for (Ingredient e : entry.getProducts())
		{
			if (e.getItemId() < 0)
				continue;
			
			if (!e.isStackable())
				slots += e.getItemCount() * _amount;
			else if (player.getInventory().getItemByItemId(e.getItemId()) == null)
				slots++;
			
			weight += e.getItemCount() * _amount * e.getWeight();
		}
		
		if (!inv.validateWeight(weight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}
		
		if (!inv.validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}
		
		// Generate a list of distinct ingredients and counts in order to check if the correct item-counts are possessed by the player
		List<Ingredient> ingredientsList = new ArrayList<>(entry.getIngredients().size());
		boolean newIng;
		
		for (Ingredient e : entry.getIngredients())
		{
			newIng = true;
			
			// at this point, the template has already been modified so that enchantments are properly included
			// whenever they need to be applied. Uniqueness of items is thus judged by item id AND enchantment level
			for (int i = ingredientsList.size(); --i >= 0;)
			{
				Ingredient ex = ingredientsList.get(i);
				
				// if the item was already added in the list, merely increment the count
				// this happens if 1 list entry has the same ingredient twice (example 2 swords = 1 dual)
				if (ex.getItemId() == e.getItemId() && ex.getEnchantLevel() == e.getEnchantLevel())
				{
					if (ex.getItemCount() + e.getItemCount() > Integer.MAX_VALUE)
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
						return;
					}
					
					// two same ingredients, merge into one and replace old
					final Ingredient ing = ex.getCopy();
					ing.setItemCount(ex.getItemCount() + e.getItemCount());
					ingredientsList.set(i, ing);
					
					newIng = false;
					break;
				}
			}
			
			// if it's a new ingredient, just store its info directly (item id, count, enchantment)
			if (newIng)
				ingredientsList.add(e);
		}
		
		// now check if the player has sufficient items in the inventory to cover the ingredients' expences
		for (Ingredient e : ingredientsList)
		{
			if (e.getItemCount() * _amount > Integer.MAX_VALUE)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			
			if (e.getItemId() == CLAN_REPUTATION)
			{
				if (player.getClan() == null)
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
					return;
				}
				
				if (!player.isClanLeader())
				{
					player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
					return;
				}
				
				if (player.getClan().getReputationScore() < e.getItemCount() * _amount)
				{
					player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
					return;
				}
			}
			else
			{
				// if this is not a list that maintains enchantment, check the count of all items that have the given id.
				// otherwise, check only the count of items with exactly the needed enchantment level
				if (inv.getInventoryItemCount(e.getItemId(), list.getMaintainEnchantment() ? e.getEnchantLevel() : -1, false) < ((Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient()) ? (e.getItemCount() * _amount) : e.getItemCount()))
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					return;
				}
			}
		}
		
		List<L2Augmentation> augmentation = new ArrayList<>();
		
		for (Ingredient e : entry.getIngredients())
		{
			if (e.getItemId() == CLAN_REPUTATION)
			{
				final int amount = e.getItemCount() * _amount;
				
				player.getClan().takeReputationScore(amount);
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(amount));
			}
			else
			{
				ItemInstance itemToTake = inv.getItemByItemId(e.getItemId());
				if (itemToTake == null)
				{
					player.setMultiSell(null);
					return;
				}
				
				if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient())
				{
					// if it's a stackable item, just reduce the amount from the first (only) instance that is found in the inventory
					if (itemToTake.isStackable())
					{
						if (!player.destroyItem("Multisell", itemToTake.getObjectId(), (e.getItemCount() * _amount), player.getTarget(), true))
						{
							player.setMultiSell(null);
							return;
						}
					}
					else
					{
						// for non-stackable items, one of two scenaria are possible:
						// a) list maintains enchantment: get the instances that exactly match the requested enchantment level
						// b) list does not maintain enchantment: get the instances with the LOWEST enchantment level
						
						// a) if enchantment is maintained, then get a list of items that exactly match this enchantment
						if (list.getMaintainEnchantment())
						{
							// loop through this list and remove (one by one) each item until the required amount is taken.
							ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), e.getEnchantLevel(), false);
							for (int i = 0; i < (e.getItemCount() * _amount); i++)
							{
								if (inventoryContents[i].isAugmented())
									augmentation.add(inventoryContents[i].getAugmentation());
								
								if (!player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1, player.getTarget(), true))
								{
									player.setMultiSell(null);
									return;
								}
							}
						}
						else
						// b) enchantment is not maintained. Get the instances with the LOWEST enchantment level
						{
							for (int i = 1; i <= (e.getItemCount() * _amount); i++)
							{
								ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), false);
								
								itemToTake = inventoryContents[0];
								// get item with the LOWEST enchantment level from the inventory (0 is the lowest)
								if (itemToTake.getEnchantLevel() > 0)
								{
									for (ItemInstance item : inventoryContents)
									{
										if (item.getEnchantLevel() < itemToTake.getEnchantLevel())
										{
											itemToTake = item;
											
											// nothing will have enchantment less than 0. If a zero-enchanted item is found, just take it
											if (itemToTake.getEnchantLevel() == 0)
												break;
										}
									}
								}
								
								if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1, player.getTarget(), true))
								{
									player.setMultiSell(null);
									return;
								}
							}
						}
					}
				}
			}
		}
		
		// Generate the appropriate items
		for (Ingredient e : entry.getProducts())
		{
			if (e.getItemId() == CLAN_REPUTATION)
				player.getClan().addReputationScore(e.getItemCount() * _amount);
			else
			{
				if (e.isStackable())
					inv.addItem("Multisell", e.getItemId(), e.getItemCount() * _amount, player, player.getTarget());
				else
				{
					for (int i = 0; i < (e.getItemCount() * _amount); i++)
					{
						ItemInstance product = inv.addItem("Multisell", e.getItemId(), 1, player, player.getTarget());
						if (product != null && list.getMaintainEnchantment())
						{
							if (i < augmentation.size())
								product.setAugmentation(new L2Augmentation(augmentation.get(i).getAugmentationId(), augmentation.get(i).getSkill()));
							
							product.setEnchantLevel(e.getEnchantLevel());
							product.updateDatabase();
						}
					}
				}
				
				// msg part
				SystemMessage sm;
				
				if (e.getItemCount() * _amount > 1)
					sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(e.getItemId()).addNumber(e.getItemCount() * _amount);
				else
				{
					if (list.getMaintainEnchantment() && e.getEnchantLevel() > 0)
						sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2).addNumber(e.getEnchantLevel()).addItemName(e.getItemId());
					else
						sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(e.getItemId());
				}
				player.sendPacket(sm);
			}
		}
		player.sendPacket(new ItemList(player, false));
		
		// All ok, send success message, remove items and add final product
		player.sendPacket(SystemMessageId.SUCCESSFULLY_TRADED_WITH_NPC);
		
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		// finally, give the tax to the castle...
		if (folk != null && entry.getTaxAmount() > 0)
			folk.getCastle().addToTreasury(entry.getTaxAmount() * _amount);
	}
}
package dev.l2j.tesla.gameserver.network.clientpackets;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.enums.items.CrystalType;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.kind.Weapon;

public abstract class AbstractRefinePacket extends L2GameClientPacket
{
	public static final int GRADE_NONE = 0;
	public static final int GRADE_MID = 1;
	public static final int GRADE_HIGH = 2;
	public static final int GRADE_TOP = 3;
	
	protected static final int GEMSTONE_D = 2130;
	protected static final int GEMSTONE_C = 2131;
	protected static final int GEMSTONE_B = 2132;
	
	private static final Map<Integer, LifeStone> _lifeStones = new HashMap<>();
	
	protected static final class LifeStone
	{
		// lifestone level to player level table
		private static final int[] LEVELS =
		{
			46,
			49,
			52,
			55,
			58,
			61,
			64,
			67,
			70,
			76
		};
		private final int _grade;
		private final int _level;
		
		public LifeStone(int grade, int level)
		{
			_grade = grade;
			_level = level;
		}
		
		public final int getLevel()
		{
			return _level;
		}
		
		public final int getGrade()
		{
			return _grade;
		}
		
		public final int getPlayerLevel()
		{
			return LEVELS[_level];
		}
	}
	
	static
	{
		// itemId, (LS grade, LS level)
		_lifeStones.put(8723, new LifeStone(GRADE_NONE, 0));
		_lifeStones.put(8724, new LifeStone(GRADE_NONE, 1));
		_lifeStones.put(8725, new LifeStone(GRADE_NONE, 2));
		_lifeStones.put(8726, new LifeStone(GRADE_NONE, 3));
		_lifeStones.put(8727, new LifeStone(GRADE_NONE, 4));
		_lifeStones.put(8728, new LifeStone(GRADE_NONE, 5));
		_lifeStones.put(8729, new LifeStone(GRADE_NONE, 6));
		_lifeStones.put(8730, new LifeStone(GRADE_NONE, 7));
		_lifeStones.put(8731, new LifeStone(GRADE_NONE, 8));
		_lifeStones.put(8732, new LifeStone(GRADE_NONE, 9));
		
		_lifeStones.put(8733, new LifeStone(GRADE_MID, 0));
		_lifeStones.put(8734, new LifeStone(GRADE_MID, 1));
		_lifeStones.put(8735, new LifeStone(GRADE_MID, 2));
		_lifeStones.put(8736, new LifeStone(GRADE_MID, 3));
		_lifeStones.put(8737, new LifeStone(GRADE_MID, 4));
		_lifeStones.put(8738, new LifeStone(GRADE_MID, 5));
		_lifeStones.put(8739, new LifeStone(GRADE_MID, 6));
		_lifeStones.put(8740, new LifeStone(GRADE_MID, 7));
		_lifeStones.put(8741, new LifeStone(GRADE_MID, 8));
		_lifeStones.put(8742, new LifeStone(GRADE_MID, 9));
		
		_lifeStones.put(8743, new LifeStone(GRADE_HIGH, 0));
		_lifeStones.put(8744, new LifeStone(GRADE_HIGH, 1));
		_lifeStones.put(8745, new LifeStone(GRADE_HIGH, 2));
		_lifeStones.put(8746, new LifeStone(GRADE_HIGH, 3));
		_lifeStones.put(8747, new LifeStone(GRADE_HIGH, 4));
		_lifeStones.put(8748, new LifeStone(GRADE_HIGH, 5));
		_lifeStones.put(8749, new LifeStone(GRADE_HIGH, 6));
		_lifeStones.put(8750, new LifeStone(GRADE_HIGH, 7));
		_lifeStones.put(8751, new LifeStone(GRADE_HIGH, 8));
		_lifeStones.put(8752, new LifeStone(GRADE_HIGH, 9));
		
		_lifeStones.put(8753, new LifeStone(GRADE_TOP, 0));
		_lifeStones.put(8754, new LifeStone(GRADE_TOP, 1));
		_lifeStones.put(8755, new LifeStone(GRADE_TOP, 2));
		_lifeStones.put(8756, new LifeStone(GRADE_TOP, 3));
		_lifeStones.put(8757, new LifeStone(GRADE_TOP, 4));
		_lifeStones.put(8758, new LifeStone(GRADE_TOP, 5));
		_lifeStones.put(8759, new LifeStone(GRADE_TOP, 6));
		_lifeStones.put(8760, new LifeStone(GRADE_TOP, 7));
		_lifeStones.put(8761, new LifeStone(GRADE_TOP, 8));
		_lifeStones.put(8762, new LifeStone(GRADE_TOP, 9));
	}
	
	protected static final LifeStone getLifeStone(int itemId)
	{
		return _lifeStones.get(itemId);
	}
	
	/*
	 * Checks player, source item, lifestone and gemstone validity for augmentation process
	 */
	protected static final boolean isValid(Player player, ItemInstance item, ItemInstance refinerItem, ItemInstance gemStones)
	{
		if (!isValid(player, item, refinerItem))
			return false;
		
		// GemStones must belong to owner
		if (gemStones.getOwnerId() != player.getObjectId())
			return false;
		// .. and located in inventory
		if (gemStones.getLocation() != ItemInstance.ItemLocation.INVENTORY)
			return false;
		
		final CrystalType grade = item.getItem().getCrystalType();
		
		// Check for item id
		if (getGemStoneId(grade) != gemStones.getItemId())
			return false;
		// Count must be greater or equal of required number
		if (getGemStoneCount(grade) > gemStones.getCount())
			return false;
		
		return true;
	}
	
	/**
	 * Checks augmentation process.
	 * @param player The target of the check.
	 * @param item The item to check.
	 * @param refinerItem The augmentation stone.
	 * @return true if all checks are successfully passed, false otherwise.
	 */
	protected static final boolean isValid(Player player, ItemInstance item, ItemInstance refinerItem)
	{
		if (!isValid(player, item))
			return false;
		
		// Item must belong to owner
		if (refinerItem.getOwnerId() != player.getObjectId())
			return false;
		
		// Lifestone must be located in inventory
		if (refinerItem.getLocation() != ItemInstance.ItemLocation.INVENTORY)
			return false;
		
		final LifeStone ls = _lifeStones.get(refinerItem.getItemId());
		if (ls == null)
			return false;
		
		// check for level of the lifestone
		if (player.getLevel() < ls.getPlayerLevel())
			return false;
		
		return true;
	}
	
	/*
	 * Check both player and source item conditions for augmentation process
	 */
	protected static final boolean isValid(Player player, ItemInstance item)
	{
		if (!isValid(player))
			return false;
		
		// Item must belong to owner
		if (item.getOwnerId() != player.getObjectId())
			return false;
		if (item.isAugmented())
			return false;
		if (item.isHeroItem())
			return false;
		if (item.isShadowItem())
			return false;
		if (item.getItem().getCrystalType().isLesser(CrystalType.C))
			return false;
		
		// Source item can be equipped or in inventory
		switch (item.getLocation())
		{
			case INVENTORY:
			case PAPERDOLL:
				break;
			default:
				return false;
		}
		
		if (item.getItem() instanceof Weapon)
		{
			// Rods and fists aren't augmentable
			switch (((Weapon) item.getItem()).getItemType())
			{
				case NONE:
				case FISHINGROD:
					return false;
				
				default:
					break;
			}
		}
		else
			return false;
		
		return true;
	}
	
	/*
	 * Check if player's conditions valid for augmentation process
	 */
	protected static final boolean isValid(Player player)
	{
		if (player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
			return false;
		}
		if (player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessageId.AUGMENTED_ITEM_CANNOT_BE_DISCARDED);
			return false;
		}
		if (player.isDead())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
			return false;
		}
		if (player.isParalyzed())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
			return false;
		}
		if (player.isFishing())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
			return false;
		}
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
			return false;
		}
		if (player.isCursedWeaponEquipped())
			return false;
		if (player.isProcessingTransaction())
			return false;
		
		return true;
	}
	
	/*
	 * Returns GemStone itemId based on item grade
	 */
	protected static final int getGemStoneId(CrystalType itemGrade)
	{
		switch (itemGrade)
		{
			case C:
			case B:
				return GEMSTONE_D;
			
			case A:
			case S:
				return GEMSTONE_C;
			
			default:
				return 0;
		}
	}
	
	/*
	 * Returns GemStone count based on item grade and lifestone grade (different for weapon and accessory augmentation)
	 */
	protected static final int getGemStoneCount(CrystalType itemGrade)
	{
		switch (itemGrade)
		{
			case C:
				return 20;
			
			case B:
				return 30;
			
			case A:
				return 20;
			
			case S:
				return 25;
			
			default:
				return 0;
		}
	}
}
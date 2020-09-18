package dev.l2j.tesla.gameserver.model.item.kind;

import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.enums.items.ArmorType;

/**
 * This class is dedicated to the management of armors.
 */
public final class Armor extends Item
{
	private ArmorType _type;
	
	/**
	 * Constructor for Armor.<BR>
	 * <BR>
	 * <U><I>Variables filled :</I></U><BR>
	 * <LI>_avoidModifier</LI>
	 * <LI>_pDef & _mDef</LI>
	 * <LI>_mpBonus & _hpBonus</LI>
	 * @param set : StatsSet designating the set of couples (key,value) caracterizing the armor
	 * @see Item constructor
	 */
	public Armor(StatsSet set)
	{
		super(set);
		_type = ArmorType.valueOf(set.getString("armor_type", "none").toUpperCase());
		
		int _bodyPart = getBodyPart();
		if (_bodyPart == Item.SLOT_NECK || _bodyPart == Item.SLOT_FACE || _bodyPart == Item.SLOT_HAIR || _bodyPart == Item.SLOT_HAIRALL || (_bodyPart & Item.SLOT_L_EAR) != 0 || (_bodyPart & Item.SLOT_L_FINGER) != 0 || (_bodyPart & Item.SLOT_BACK) != 0)
		{
			_type1 = Item.TYPE1_WEAPON_RING_EARRING_NECKLACE;
			_type2 = Item.TYPE2_ACCESSORY;
		}
		else
		{
			if (_type == ArmorType.NONE && getBodyPart() == Item.SLOT_L_HAND) // retail define shield as NONE
				_type = ArmorType.SHIELD;
			
			_type1 = Item.TYPE1_SHIELD_ARMOR;
			_type2 = Item.TYPE2_SHIELD_ARMOR;
		}
	}
	
	/**
	 * Returns the type of the armor.
	 * @return ArmorType
	 */
	@Override
	public ArmorType getItemType()
	{
		return _type;
	}
	
	/**
	 * Returns the ID of the item after applying the mask.
	 * @return int : ID of the item
	 */
	@Override
	public final int getItemMask()
	{
		return getItemType().mask();
	}
}
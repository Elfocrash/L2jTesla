package dev.l2j.tesla.gameserver.model.actor.stat;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.instance.Pet;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.InventoryUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.enums.skills.Stats;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

public class PetStat extends SummonStat
{
	public PetStat(Pet activeChar)
	{
		super(activeChar);
	}
	
	public boolean addExp(int value)
	{
		if (!super.addExp(value))
			return false;
		
		getActiveChar().updateAndBroadcastStatus(1);
		return true;
	}
	
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		if (!super.addExpAndSp(addToExp, addToSp))
			return false;
		
		getActiveChar().getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_EARNED_S1_EXP).addNumber((int) addToExp));
		return true;
	}
	
	@Override
	public final boolean addLevel(byte value)
	{
		if (getLevel() + value > (getMaxLevel() - 1))
			return false;
		
		boolean levelIncreased = super.addLevel(value);
		if (levelIncreased)
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar(), 15));
		
		return levelIncreased;
	}
	
	@Override
	public final long getExpForLevel(int level)
	{
		return getActiveChar().getTemplate().getPetDataEntry(level).getMaxExp();
	}
	
	@Override
	public Pet getActiveChar()
	{
		return (Pet) super.getActiveChar();
	}
	
	@Override
	public void setLevel(byte value)
	{
		getActiveChar().setPetData(value);
		
		super.setLevel(value); // Set level.
		
		// If a control item exists and its level is different of the new level.
		final ItemInstance controlItem = getActiveChar().getControlItem();
		if (controlItem != null && controlItem.getEnchantLevel() != getLevel())
		{
			getActiveChar().sendPetInfosToOwner();
			
			controlItem.setEnchantLevel(getLevel());
			
			// Update item
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(controlItem);
			getActiveChar().getOwner().sendPacket(iu);
		}
	}
	
	@Override
	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, getActiveChar().getPetData().getMaxHp(), null, null);
	}
	
	@Override
	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, getActiveChar().getPetData().getMaxMp(), null, null);
	}
	
	@Override
	public int getMAtk(Creature target, L2Skill skill)
	{
		double attack = getActiveChar().getPetData().getMAtk();
		
		if (skill != null)
			attack += skill.getPower();
		
		return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}
	
	@Override
	public int getMAtkSpd()
	{
		double base = 333;
		
		if (getActiveChar().checkHungryState())
			base /= 2;
		
		return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, base, null, null);
	}
	
	@Override
	public int getMDef(Creature target, L2Skill skill)
	{
		return (int) calcStat(Stats.MAGIC_DEFENCE, getActiveChar().getPetData().getMDef(), target, skill);
	}
	
	@Override
	public int getPAtk(Creature target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, getActiveChar().getPetData().getPAtk(), target, null);
	}
	
	@Override
	public int getPAtkSpd()
	{
		double base = getActiveChar().getTemplate().getBasePAtkSpd();
		
		if (getActiveChar().checkHungryState())
			base /= 2;
		
		return (int) calcStat(Stats.POWER_ATTACK_SPEED, base, null, null);
	}
	
	@Override
	public int getPDef(Creature target)
	{
		return (int) calcStat(Stats.POWER_DEFENCE, getActiveChar().getPetData().getPDef(), target, null);
	}
}
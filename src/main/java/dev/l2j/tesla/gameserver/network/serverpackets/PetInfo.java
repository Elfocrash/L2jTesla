package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.enums.ZoneId;
import dev.l2j.tesla.gameserver.model.actor.Summon;
import dev.l2j.tesla.gameserver.model.actor.instance.Pet;
import dev.l2j.tesla.gameserver.model.actor.instance.Servitor;

public class PetInfo extends L2GameServerPacket
{
	private final Summon _summon;
	private final int _val;
	
	private int _maxFed;
	private int _curFed;
	
	public PetInfo(Summon summon, int val)
	{
		_summon = summon;
		_val = val;
		
		if (_summon instanceof Pet)
		{
			final Pet pet = (Pet) _summon;
			
			_curFed = pet.getCurrentFed();
			_maxFed = pet.getPetData().getMaxMeal();
		}
		else if (_summon instanceof Servitor)
		{
			final Servitor sum = (Servitor) _summon;
			
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb1);
		writeD(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().getIdTemplate() + 1000000);
		writeD(0); // 1=attackable
		
		writeD(_summon.getX());
		writeD(_summon.getY());
		writeD(_summon.getZ());
		writeD(_summon.getHeading());
		writeD(0);
		writeD(_summon.getMAtkSpd());
		writeD(_summon.getPAtkSpd());
		
		final int runSpd = _summon.getStat().getBaseRunSpeed();
		final int walkSpd = _summon.getStat().getBaseWalkSpeed();
		
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		
		writeF(_summon.getStat().getMovementSpeedMultiplier());
		writeF(1);
		writeF(_summon.getCollisionRadius());
		writeF(_summon.getCollisionHeight());
		writeD(_summon.getWeapon());
		writeD(_summon.getArmor());
		writeD(0);
		writeC((_summon.getOwner() != null) ? 1 : 0); // when pet is dead and player exit game, pet doesn't show master name
		writeC(1);
		writeC((_summon.isInCombat()) ? 1 : 0);
		writeC((_summon.isAlikeDead()) ? 1 : 0);
		writeC((_summon.isShowSummonAnimation()) ? 2 : _val);
		writeS(_summon.getName());
		writeS(_summon.getTitle());
		writeD(1);
		writeD(_summon.getPvpFlag());
		writeD(_summon.getKarma());
		writeD(_curFed);
		writeD(_maxFed);
		writeD((int) _summon.getCurrentHp());
		writeD(_summon.getMaxHp());
		writeD((int) _summon.getCurrentMp());
		writeD(_summon.getMaxMp());
		writeD(_summon.getStat().getSp());
		writeD(_summon.getLevel());
		writeQ(_summon.getStat().getExp());
		writeQ(_summon.getExpForThisLevel());
		writeQ(_summon.getExpForNextLevel());
		writeD((_summon instanceof Pet) ? _summon.getInventory().getTotalWeight() : 0);
		writeD(_summon.getMaxLoad());
		writeD(_summon.getPAtk(null));
		writeD(_summon.getPDef(null));
		writeD(_summon.getMAtk(null, null));
		writeD(_summon.getMDef(null, null));
		writeD(_summon.getAccuracy());
		writeD(_summon.getEvasionRate(null));
		writeD(_summon.getCriticalHit(null, null));
		writeD(_summon.getMoveSpeed());
		writeD(_summon.getPAtkSpd());
		writeD(_summon.getMAtkSpd());
		
		writeD(_summon.getAbnormalEffect());
		writeH((_summon.isMountable()) ? 1 : 0);
		writeC((_summon.isInsideZone(ZoneId.WATER)) ? 1 : (_summon.isFlying()) ? 2 : 0);
		
		writeH(0); // ??
		writeC(_summon.getTeam().getId());
		writeD(_summon.getSoulShotsPerHit());
		writeD(_summon.getSpiritShotsPerHit());
	}
}
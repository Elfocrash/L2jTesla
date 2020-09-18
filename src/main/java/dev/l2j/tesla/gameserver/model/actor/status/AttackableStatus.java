package dev.l2j.tesla.gameserver.model.actor.status;

import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;

public class AttackableStatus extends NpcStatus
{
	public AttackableStatus(Attackable activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public final void reduceHp(double value, Creature attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}
	
	@Override
	public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption)
	{
		if (getActiveChar().isDead())
			return;
		
		Monster monster = null;
		if (getActiveChar() instanceof Monster)
		{
			monster = (Monster) getActiveChar();
			if (value > 0)
			{
				if (monster.isOverhit())
					monster.setOverhitValues(attacker, value);
				else
					monster.overhitEnabled(false);
			}
			else
				monster.overhitEnabled(false);
		}
		
		// Add attackers to npc's attacker list
		if (attacker != null)
			getActiveChar().addAttacker(attacker);
		
		super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
		
		// And the attacker's hit didn't kill the mob, clear the over-hit flag
		if (monster != null && !monster.isDead())
			monster.overhitEnabled(false);
	}
	
	@Override
	public Attackable getActiveChar()
	{
		return (Attackable) super.getActiveChar();
	}
}
package dev.l2j.tesla.gameserver.model.actor.instance;

import java.util.concurrent.Future;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.skills.L2SkillType;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

/**
 * A BabyPet can heal his owner. It got 2 heal power, weak or strong.
 * <ul>
 * <li>If the owner's HP is more than 80%, do nothing.</li>
 * <li>If the owner's HP is under 15%, have 75% chances of using a strong heal.</li>
 * <li>Otherwise, have 25% chances for weak heal.</li>
 * </ul>
 */
public final class BabyPet extends Pet
{
	protected IntIntHolder _majorHeal = null;
	protected IntIntHolder _minorHeal = null;
	
	private Future<?> _castTask;
	
	public BabyPet(int objectId, NpcTemplate template, Player owner, ItemInstance control)
	{
		super(objectId, template, owner, control);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		double healPower = 0;
		int skillLevel;
		for (L2Skill skill : getTemplate().getSkills(NpcTemplate.SkillType.HEAL))
		{
			if (skill.getTargetType() != L2Skill.SkillTargetType.TARGET_OWNER_PET || skill.getSkillType() != L2SkillType.HEAL)
				continue;
			
			// The skill level is calculated on the fly. Template got an skill level of 1.
			skillLevel = getSkillLevel(skill.getId());
			if (skillLevel <= 0)
				continue;
			
			if (healPower == 0)
			{
				// set both heal types to the same skill
				_majorHeal = new IntIntHolder(skill.getId(), skillLevel);
				_minorHeal = _majorHeal;
				healPower = skill.getPower();
			}
			else
			{
				// another heal skill found - search for most powerful
				if (skill.getPower() > healPower)
					_majorHeal = new IntIntHolder(skill.getId(), skillLevel);
				else
					_minorHeal = new IntIntHolder(skill.getId(), skillLevel);
			}
		}
		startCastTask();
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		stopCastTask();
		abortCast();
		return true;
	}
	
	@Override
	public synchronized void unSummon(Player owner)
	{
		stopCastTask();
		abortCast();
		
		super.unSummon(owner);
	}
	
	@Override
	public void doRevive()
	{
		super.doRevive();
		startCastTask();
	}
	
	private final void startCastTask()
	{
		if (_majorHeal != null && _castTask == null && !isDead()) // cast task is not yet started and not dead (will start on revive)
			_castTask = ThreadPool.scheduleAtFixedRate(new CastTask(this), 3000, 1000);
	}
	
	private final void stopCastTask()
	{
		if (_castTask != null)
		{
			_castTask.cancel(false);
			_castTask = null;
		}
	}
	
	protected void castSkill(L2Skill skill)
	{
		// casting automatically stops any other action (such as autofollow or a move-to).
		// We need to gather the necessary info to restore the previous state.
		final boolean previousFollowStatus = getFollowStatus();
		
		// pet not following and owner outside cast range
		if (!previousFollowStatus && !isInsideRadius(getOwner(), skill.getCastRange(), true, true))
			return;
		
		setTarget(getOwner());
		useMagic(skill, false, false);
		
		getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1).addSkillName(skill));
		
		// calling useMagic changes the follow status, if the babypet actually casts
		// (as opposed to failing due some factors, such as too low MP, etc).
		// if the status has actually been changed, revert it. Else, allow the pet to
		// continue whatever it was trying to do.
		// NOTE: This is important since the pet may have been told to attack a target.
		// reverting the follow status will abort this attack! While aborting the attack
		// in order to heal is natural, it is not acceptable to abort the attack on its own,
		// merely because the timer stroke and without taking any other action...
		if (previousFollowStatus != getFollowStatus())
			setFollowStatus(previousFollowStatus);
	}
	
	private class CastTask implements Runnable
	{
		private final BabyPet _baby;
		
		public CastTask(BabyPet baby)
		{
			_baby = baby;
		}
		
		@Override
		public void run()
		{
			Player owner = _baby.getOwner();
			
			// if the owner is dead, merely wait for the owner to be resurrected
			// if the pet is still casting from the previous iteration, allow the cast to complete...
			if (owner != null && !owner.isDead() && !owner.isInvul() && !_baby.isCastingNow() && !_baby.isBetrayed() && !_baby.isMuted() && !_baby.isOutOfControl() && _baby.getAI().getDesire().getIntention() != IntentionType.CAST)
			{
				L2Skill skill = null;
				
				if (_majorHeal != null)
				{
					final double hpPercent = owner.getCurrentHp() / owner.getMaxHp();
					if (hpPercent < 0.15)
					{
						skill = _majorHeal.getSkill();
						if (!_baby.isSkillDisabled(skill) && Rnd.get(100) <= 75)
						{
							if (_baby.getCurrentMp() >= skill.getMpConsume())
							{
								castSkill(skill);
								return;
							}
						}
					}
					else if ((_majorHeal.getSkill() != _minorHeal.getSkill()) && hpPercent < 0.8)
					{
						// Cast _minorHeal only if it's different than _majorHeal, then pet has two heals available.
						skill = _minorHeal.getSkill();
						if (!_baby.isSkillDisabled(skill) && Rnd.get(100) <= 25)
						{
							if (_baby.getCurrentMp() >= skill.getMpConsume())
							{
								castSkill(skill);
								return;
							}
						}
					}
				}
			}
		}
	}
}
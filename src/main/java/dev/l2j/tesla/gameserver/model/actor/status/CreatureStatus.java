package dev.l2j.tesla.gameserver.model.actor.status;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.skills.Formulas;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.model.actor.stat.CreatureStat;

public class CreatureStatus
{
	private final Creature _activeChar;
	
	private final Set<Creature> _statusListener = ConcurrentHashMap.newKeySet();
	
	protected static final byte REGEN_FLAG_CP = 4;
	private static final byte REGEN_FLAG_HP = 1;
	private static final byte REGEN_FLAG_MP = 2;
	
	private double _currentHp = 0;
	private double _currentMp = 0;
	
	private Future<?> _regTask;
	protected byte _flagsRegenActive = 0;
	
	public CreatureStatus(Creature activeChar)
	{
		_activeChar = activeChar;
	}
	
	/**
	 * Add the object to the list of Creature that must be informed of HP/MP updates of this Creature.
	 * @param object : Creature to add to the listener.
	 */
	public final void addStatusListener(Creature object)
	{
		if (object == getActiveChar())
			return;
		
		_statusListener.add(object);
	}
	
	/**
	 * Remove the object from the list of Creature that must be informed of HP/MP updates of this Creature.
	 * @param object : Creature to remove to the listener.
	 */
	public final void removeStatusListener(Creature object)
	{
		_statusListener.remove(object);
	}
	
	/**
	 * @return The list of Creature to inform, or null if empty.
	 */
	public final Set<Creature> getStatusListener()
	{
		return _statusListener;
	}
	
	public void reduceCp(int value)
	{
	}
	
	/**
	 * Reduce the current HP of the Creature and launch the doDie Task if necessary.
	 * @param value : The amount of removed HPs.
	 * @param attacker : The Creature who attacks.
	 */
	public void reduceHp(double value, Creature attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}
	
	public void reduceHp(double value, Creature attacker, boolean isHpConsumption)
	{
		reduceHp(value, attacker, true, false, isHpConsumption);
	}
	
	public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		if (getActiveChar().isDead())
			return;
		
		// invul handling
		if (getActiveChar().isInvul())
		{
			// other chars can't damage
			if (attacker != getActiveChar())
				return;
			
			// only DOT and HP consumption allowed for damage self
			if (!isDOT && !isHPConsumption)
				return;
		}
		
		if (attacker != null)
		{
			final Player attackerPlayer = attacker.getActingPlayer();
			if (attackerPlayer != null && attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage())
				return;
		}
		
		if (!isDOT && !isHPConsumption)
		{
			getActiveChar().stopEffectsOnDamage(awake);
			
			if (getActiveChar().isStunned() && Rnd.get(10) == 0)
				getActiveChar().stopStunning(true);
			
			if (getActiveChar().isImmobileUntilAttacked())
				getActiveChar().stopImmobileUntilAttacked(null);
		}
		
		if (value > 0) // Reduce Hp if any
			setCurrentHp(Math.max(getCurrentHp() - value, 0));
		
		// Die if character is mortal
		if (getActiveChar().getCurrentHp() < 0.5 && getActiveChar().isMortal())
		{
			getActiveChar().abortAttack();
			getActiveChar().abortCast();
			
			getActiveChar().doDie(attacker);
		}
	}
	
	public void reduceMp(double value)
	{
		setCurrentMp(Math.max(getCurrentMp() - value, 0));
	}
	
	/**
	 * Start the HP/MP/CP Regeneration task.
	 */
	public final synchronized void startHpMpRegeneration()
	{
		if (_regTask == null && !getActiveChar().isDead())
		{
			// Get the regeneration period.
			final int period = Formulas.getRegeneratePeriod(getActiveChar());
			
			// Create the HP/MP/CP regeneration task.
			_regTask = ThreadPool.scheduleAtFixedRate(() -> doRegeneration(), period, period);
		}
	}
	
	/**
	 * Stop the HP/MP/CP Regeneration task.
	 */
	public final synchronized void stopHpMpRegeneration()
	{
		if (_regTask != null)
		{
			// Stop the HP/MP/CP regeneration task.
			_regTask.cancel(false);
			_regTask = null;
			
			// Set the RegenActive flag to false.
			_flagsRegenActive = 0;
		}
	}
	
	public double getCurrentCp()
	{
		return 0;
	}
	
	public void setCurrentCp(double newCp)
	{
	}
	
	public final double getCurrentHp()
	{
		return _currentHp;
	}
	
	public final void setCurrentHp(double newHp)
	{
		setCurrentHp(newHp, true);
	}
	
	public void setCurrentHp(double newHp, boolean broadcastPacket)
	{
		final double maxHp = getActiveChar().getMaxHp();
		
		synchronized (this)
		{
			if (getActiveChar().isDead())
				return;
			
			if (newHp >= maxHp)
			{
				// Set the RegenActive flag to false
				_currentHp = maxHp;
				_flagsRegenActive &= ~REGEN_FLAG_HP;
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
					stopHpMpRegeneration();
			}
			else
			{
				// Set the RegenActive flag to true
				_currentHp = newHp;
				_flagsRegenActive |= REGEN_FLAG_HP;
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		if (broadcastPacket)
			getActiveChar().broadcastStatusUpdate();
	}
	
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHp(newHp, false);
		setCurrentMp(newMp, true);
	}
	
	public final double getCurrentMp()
	{
		return _currentMp;
	}
	
	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}
	
	public final void setCurrentMp(double newMp, boolean broadcastPacket)
	{
		final int maxMp = getActiveChar().getStat().getMaxMp();
		
		synchronized (this)
		{
			if (getActiveChar().isDead())
				return;
			
			if (newMp >= maxMp)
			{
				// Set the RegenActive flag to false
				_currentMp = maxMp;
				_flagsRegenActive &= ~REGEN_FLAG_MP;
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
					stopHpMpRegeneration();
			}
			else
			{
				// Set the RegenActive flag to true
				_currentMp = newMp;
				_flagsRegenActive |= REGEN_FLAG_MP;
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		if (broadcastPacket)
			getActiveChar().broadcastStatusUpdate();
	}
	
	protected void doRegeneration()
	{
		final CreatureStat charstat = getActiveChar().getStat();
		
		// Modify the current HP of the Creature.
		if (getCurrentHp() < charstat.getMaxHp())
			setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
		
		// Modify the current MP of the Creature.
		if (getCurrentMp() < charstat.getMaxMp())
			setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
		
		// Send the StatusUpdate packet.
		getActiveChar().broadcastStatusUpdate();
	}
	
	public Creature getActiveChar()
	{
		return _activeChar;
	}
}
package dev.l2j.tesla.gameserver.model.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.ScriptEventType;
import dev.l2j.tesla.gameserver.model.actor.ai.type.AttackableAI;
import dev.l2j.tesla.gameserver.model.actor.ai.type.CreatureAI;
import dev.l2j.tesla.gameserver.model.actor.npc.AggroInfo;
import dev.l2j.tesla.gameserver.model.actor.status.AttackableStatus;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

/**
 * This class manages all NPCs which can hold an aggro list. It inherits from {@link Npc}.
 */
public class Attackable extends Npc
{
	private final Map<Creature, AggroInfo> _aggroList = new ConcurrentHashMap<>();
	
	private final Set<Creature> _attackedBy = ConcurrentHashMap.newKeySet();
	
	private boolean _isReturningToSpawnPoint;
	private boolean _seeThroughSilentMove;
	
	public Attackable(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new AttackableStatus(this));
	}
	
	@Override
	public AttackableStatus getStatus()
	{
		return (AttackableStatus) super.getStatus();
	}
	
	@Override
	public CreatureAI getAI()
	{
		CreatureAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
					_ai = new AttackableAI(this);
				
				return _ai;
			}
		}
		return ai;
	}
	
	@Override
	public void addKnownObject(WorldObject object)
	{
		// If the new object is a Player and our AI was IDLE, we set it to ACTIVE.
		if (object instanceof Player && getAI().getDesire().getIntention() == IntentionType.IDLE)
			getAI().setIntention(IntentionType.ACTIVE, null);
	}
	
	@Override
	public void removeKnownObject(WorldObject object)
	{
		super.removeKnownObject(object);
		
		// Delete the object from aggro list.
		if (object instanceof Creature)
			getAggroList().remove(object);
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, L2Skill skill)
	{
		reduceCurrentHp(damage, attacker, true, false, skill);
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		// Test the ON_ATTACK ScriptEventType.
		if (attacker != null && !isDead())
		{
			final List<Quest> scripts = getTemplate().getEventQuests(ScriptEventType.ON_ATTACK);
			if (scripts != null)
				for (Quest quest : scripts)
					quest.notifyAttack(this, attacker, (int) damage, skill);
		}
		
		// Reduce the current HP of the Attackable and launch the doDie Task if necessary
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		// Test the ON_KILL ScriptEventType.
		final List<Quest> scripts = getTemplate().getEventQuests(ScriptEventType.ON_KILL);
		if (scripts != null)
			for (Quest quest : scripts)
				ThreadPool.schedule(() -> quest.notifyKill(this, killer), 3000);
			
		_attackedBy.clear();
		
		return true;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		// Clear the aggro list.
		_aggroList.clear();
		
		setWalking();
		
		// Stop the AI if region is inactive.
		if (!isInActiveRegion() && hasAI())
			getAI().stopAITask();
	}
	
	@Override
	public int calculateRandomAnimationTimer()
	{
		return Rnd.get(Config.MIN_MONSTER_ANIMATION, Config.MAX_MONSTER_ANIMATION);
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return Config.MAX_MONSTER_ANIMATION > 0 && !isRaidRelated();
	}
	
	/**
	 * Add a {@link Creature} attacker on _attackedBy {@link List}.
	 * @param attacker : The Creature to add.
	 */
	public void addAttacker(Creature attacker)
	{
		if (attacker == null || attacker == this)
			return;
		
		_attackedBy.add(attacker);
	}
	
	/**
	 * Add damage and hate to the {@link Creature} attacker {@link AggroInfo} of this {@link Attackable}.
	 * @param attacker : The Creature which dealt damages.
	 * @param damage : The done amount of damages.
	 * @param aggro : The generated hate.
	 */
	public void addDamageHate(Creature attacker, int damage, int aggro)
	{
		if (attacker == null)
			return;
		
		// Get or create the AggroInfo of the attacker.
		final AggroInfo ai = _aggroList.computeIfAbsent(attacker, AggroInfo::new);
		ai.addDamage(damage);
		ai.addHate(aggro);
		
		if (aggro == 0)
		{
			final Player targetPlayer = attacker.getActingPlayer();
			if (targetPlayer != null)
			{
				final List<Quest> scripts = getTemplate().getEventQuests(ScriptEventType.ON_AGGRO);
				if (scripts != null)
					for (Quest quest : scripts)
						quest.notifyAggro(this, targetPlayer, (attacker instanceof Summon));
			}
			else
			{
				aggro = 1;
				ai.addHate(1);
			}
		}
		else
		{
			// Set the intention to the Attackable to ACTIVE
			if (aggro > 0 && getAI().getDesire().getIntention() == IntentionType.IDLE)
				getAI().setIntention(IntentionType.ACTIVE);
		}
	}
	
	/**
	 * Reduce hate for the {@link Creature} target. If the target is null, decrease the hate for the whole aggro list.
	 * @param target : The Creature to check.
	 * @param amount : The amount of hate to remove.
	 */
	public void reduceHate(Creature target, int amount)
	{
		// No target ; we first check if most hated exists, if no we process the whole aggro list. If the amount of hate is <= 0, we stop the aggro behavior.
		if (target == null)
		{
			Creature mostHated = getMostHated();
			if (mostHated == null)
			{
				((AttackableAI) getAI()).setGlobalAggro(-25);
				return;
			}
			
			for (AggroInfo ai : _aggroList.values())
				ai.addHate(-amount);
			
			amount = getHating(mostHated);
			
			if (amount <= 0)
			{
				((AttackableAI) getAI()).setGlobalAggro(-25);
				_aggroList.clear();
				getAI().setIntention(IntentionType.ACTIVE);
				setWalking();
			}
			return;
		}
		
		// Retrieve the AggroInfo related to the target.
		AggroInfo ai = _aggroList.get(target);
		if (ai == null)
			return;
		
		// Reduce hate.
		ai.addHate(-amount);
		
		// If hate is <= 0 and no most hated target is found, we stop the aggro behavior.
		if (ai.getHate() <= 0 && getMostHated() == null)
		{
			((AttackableAI) getAI()).setGlobalAggro(-25);
			_aggroList.clear();
			getAI().setIntention(IntentionType.ACTIVE);
			setWalking();
		}
	}
	
	/**
	 * Clears the hate of a {@link Creature} target without removing it from the list.
	 * @param target : The Creature to clean hate.
	 */
	public void stopHating(Creature target)
	{
		if (target == null)
			return;
		
		AggroInfo ai = _aggroList.get(target);
		if (ai != null)
			ai.stopHate();
	}
	
	/**
	 * Clean the hate values of all registered aggroed {@link Creature}s, without dropping them.
	 */
	public void cleanAllHate()
	{
		for (AggroInfo ai : _aggroList.values())
			ai.stopHate();
	}
	
	/**
	 * @return the most hated {@link Creature} of this {@link Attackable}.
	 */
	public Creature getMostHated()
	{
		if (_aggroList.isEmpty() || isAlikeDead())
			return null;
		
		Creature mostHated = null;
		int maxHate = 0;
		
		for (AggroInfo ai : _aggroList.values())
		{
			if (ai.checkHate(this) > maxHate)
			{
				mostHated = ai.getAttacker();
				maxHate = ai.getHate();
			}
		}
		return mostHated;
	}
	
	/**
	 * @return the {@link List} of hated {@link Creature}s. It also make checks, setting hate to 0 following conditions.
	 */
	public List<Creature> getHateList()
	{
		if (_aggroList.isEmpty() || isAlikeDead())
			return Collections.emptyList();
		
		final List<Creature> result = new ArrayList<>();
		for (AggroInfo ai : _aggroList.values())
		{
			ai.checkHate(this);
			result.add(ai.getAttacker());
		}
		return result;
	}
	
	/**
	 * @param target : The Creature whose hate level must be returned.
	 * @return the hate level of this {@link Attackable} against the {@link Creature} set as target.
	 */
	public int getHating(final Creature target)
	{
		if (_aggroList.isEmpty() || target == null)
			return 0;
		
		// Retrieve the AggroInfo related to the target.
		final AggroInfo ai = _aggroList.get(target);
		if (ai == null)
			return 0;
		
		// Delete hate of invisible Players.
		if (ai.getAttacker() instanceof Player && ((Player) ai.getAttacker()).getAppearance().getInvisible())
		{
			_aggroList.remove(target);
			return 0;
		}
		
		// Delete hate of invisible-region Creatures.
		if (!ai.getAttacker().isVisible())
		{
			_aggroList.remove(target);
			return 0;
		}
		
		// Stop the hate process if the attacker is dead-alike.
		if (ai.getAttacker().isAlikeDead())
		{
			ai.stopHate();
			return 0;
		}
		
		return ai.getHate();
	}
	
	/**
	 * Cast a {@link L2Skill}, with regular skills checks (dead, passive, already casting, disabled, mana/hp consumption, muted).
	 * @param skill : The L2Skill to cast.
	 */
	public void useMagic(L2Skill skill)
	{
		if (skill == null || isAlikeDead())
			return;
		
		if (skill.isPassive())
			return;
		
		if (isCastingNow())
			return;
		
		if (isSkillDisabled(skill))
			return;
		
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
			return;
		
		if (getCurrentHp() <= skill.getHpConsume())
			return;
		
		if (skill.isMagic())
		{
			if (isMuted())
				return;
		}
		else
		{
			if (isPhysicalMuted())
				return;
		}
		
		final WorldObject target = skill.getFirstOfTargetList(this);
		if (target == null)
			return;
		
		getAI().setIntention(IntentionType.CAST, skill, target);
	}
	
	/**
	 * @return true if the {@link Attackable} successfully returned to spawn point. In case of minions, they are simply deleted.
	 */
	public boolean returnHome()
	{
		// Do nothing if the Attackable is already dead.
		if (isDead())
			return false;
		
		// Minions are simply squeezed if they lose activity.
		if (isMinion() && !isRaidRelated())
		{
			deleteMe();
			return true;
		}
		
		// For regular Attackable, we check if a spawn exists, and if we're far from it (using drift range).
		if (getSpawn() != null && !isInsideRadius(getSpawn().getLocX(), getSpawn().getLocY(), getDriftRange(), false))
		{
			cleanAllHate();
			
			setIsReturningToSpawnPoint(true);
			setWalking();
			getAI().setIntention(IntentionType.MOVE_TO, getSpawn().getLoc());
			return true;
		}
		return false;
	}
	
	public int getDriftRange()
	{
		return Config.MAX_DRIFT_RANGE;
	}
	
	public final Set<Creature> getAttackByList()
	{
		return _attackedBy;
	}
	
	public final Map<Creature, AggroInfo> getAggroList()
	{
		return _aggroList;
	}
	
	public final boolean isReturningToSpawnPoint()
	{
		return _isReturningToSpawnPoint;
	}
	
	public final void setIsReturningToSpawnPoint(boolean value)
	{
		_isReturningToSpawnPoint = value;
	}
	
	public boolean canSeeThroughSilentMove()
	{
		return _seeThroughSilentMove;
	}
	
	public void seeThroughSilentMove(boolean val)
	{
		_seeThroughSilentMove = val;
	}
	
	/**
	 * @return the {@link ItemInstance} used as weapon of this {@link Attackable} (null by default).
	 */
	public ItemInstance getActiveWeapon()
	{
		return null;
	}
	
	/**
	 * @return the {@link Attackable} leader of this Attackable, or null if this Attackable isn't linked to any master.
	 */
	public Attackable getMaster()
	{
		return null;
	}
	
	public boolean isGuard()
	{
		return false;
	}
}
package dev.l2j.tesla.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Npc;

/**
 * Handles {@link Npc} random social animation after specified time.
 */
public final class RandomAnimationTaskManager implements Runnable
{
	private final Map<Npc, Long> _characters = new ConcurrentHashMap<>();
	
	protected RandomAnimationTaskManager()
	{
		// Run task each second.
		ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	@Override
	public final void run()
	{
		// List is empty, skip.
		if (_characters.isEmpty())
			return;
		
		// Get current time.
		final long time = System.currentTimeMillis();
		
		// Loop all characters.
		for (Map.Entry<Npc, Long> entry : _characters.entrySet())
		{
			final Npc npc = entry.getKey();
			
			// Dead NPCs or set in inactive region don't need to run this task. We also deny the task for Attackable with IntentionType different of ACTIVE.
			if (!npc.isInActiveRegion() || npc.isDead() || (npc instanceof Attackable && npc.getAI().getDesire().getIntention() != IntentionType.ACTIVE))
			{
				_characters.remove(npc);
				continue;
			}
			
			// Time hasn't passed yet, skip.
			if (time < entry.getValue())
				continue;
			
			if (!(npc.isStunned() || npc.isSleeping() || npc.isParalyzed()))
				npc.onRandomAnimation(Rnd.get(2, 3));
			
			// Renew the timer.
			add(npc, npc.calculateRandomAnimationTimer());
		}
	}
	
	/**
	 * Adds {@link Npc} to the RandomAnimationTask with additional interval.
	 * @param character : {@link Npc} to be added.
	 * @param interval : Interval in seconds, after which the decay task is triggered.
	 */
	public final void add(Npc character, int interval)
	{
		_characters.put(character, System.currentTimeMillis() + interval * 1000);
	}
	
	public static final RandomAnimationTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		protected static final RandomAnimationTaskManager INSTANCE = new RandomAnimationTaskManager();
	}
}
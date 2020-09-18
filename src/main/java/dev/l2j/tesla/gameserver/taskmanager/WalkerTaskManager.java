package dev.l2j.tesla.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.commons.concurrent.ThreadPool;

import dev.l2j.tesla.gameserver.model.actor.instance.Walker;

/**
 * Handles {@link Walker} waiting state case, when they got a delay option on their WalkNode.
 */
public final class WalkerTaskManager implements Runnable
{
	private final Map<Walker, Long> _walkers = new ConcurrentHashMap<>();
	
	protected WalkerTaskManager()
	{
		// Run task each second.
		ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	@Override
	public final void run()
	{
		// List is empty, skip.
		if (_walkers.isEmpty())
			return;
		
		// Get current time.
		final long time = System.currentTimeMillis();
		
		// Loop all Walkers.
		for (Map.Entry<Walker, Long> entry : _walkers.entrySet())
		{
			// Time hasn't passed yet, skip.
			if (time < entry.getValue())
				continue;
			
			// Get character.
			final Walker walker = entry.getKey();
			
			// Order the Walker to move to next point.
			walker.getAI().moveToNextPoint();
			
			// Release it from the map.
			_walkers.remove(walker);
		}
	}
	
	/**
	 * Adds {@link Walker} to the WalkerTaskManager.
	 * @param walker : Walker to be added.
	 * @param delay : The delay to add.
	 */
	public final void add(Walker walker, int delay)
	{
		_walkers.put(walker, System.currentTimeMillis() + delay);
	}
	
	public static final WalkerTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final WalkerTaskManager INSTANCE = new WalkerTaskManager();
	}
}
package dev.l2j.tesla.gameserver.model.memo;

import java.util.concurrent.atomic.AtomicBoolean;

import dev.l2j.tesla.commons.util.StatsSet;

/**
 * A {@link StatsSet} which overrides methods to prevent doing useless database operations if there is no changes since last edit (it uses an AtomicBoolean to keep edition tracking).<br>
 * <br>
 * It also has 2 abstract methods, named restoreMe() and storeMe().
 */
@SuppressWarnings("serial")
public abstract class AbstractMemo extends StatsSet
{
	private final AtomicBoolean _hasChanges = new AtomicBoolean(false);
	
	@Override
	public final void set(String name, boolean value)
	{
		_hasChanges.compareAndSet(false, true);
		super.set(name, value);
	}
	
	@Override
	public final void set(String name, double value)
	{
		_hasChanges.compareAndSet(false, true);
		super.set(name, value);
	}
	
	@Override
	public final void set(String name, Enum<?> value)
	{
		_hasChanges.compareAndSet(false, true);
		super.set(name, value);
	}
	
	@Override
	public final void set(String name, int value)
	{
		_hasChanges.compareAndSet(false, true);
		super.set(name, value);
	}
	
	@Override
	public final void set(String name, long value)
	{
		_hasChanges.compareAndSet(false, true);
		super.set(name, value);
	}
	
	@Override
	public final void set(String name, String value)
	{
		_hasChanges.compareAndSet(false, true);
		super.set(name, value);
	}
	
	/**
	 * @return {@code true} if changes are made since last load/save.
	 */
	public final boolean hasChanges()
	{
		return _hasChanges.get();
	}
	
	/**
	 * Atomically sets the value to the given updated value if the current value {@code ==} the expected value.
	 * @param expect
	 * @param update
	 * @return {@code true} if successful. {@code false} return indicates that the actual value was not equal to the expected value.
	 */
	public final boolean compareAndSetChanges(boolean expect, boolean update)
	{
		return _hasChanges.compareAndSet(expect, update);
	}
	
	/**
	 * Removes variable
	 * @param name
	 */
	public final void remove(String name)
	{
		_hasChanges.compareAndSet(false, true);
		unset(name);
	}
	
	protected abstract boolean restoreMe();
	
	protected abstract boolean storeMe();
}
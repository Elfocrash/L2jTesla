package dev.l2j.tesla.gameserver.model.actor.ai;

import dev.l2j.tesla.gameserver.enums.IntentionType;

/**
 * A datatype used as a simple "wish" of an actor, consisting of an {@link IntentionType} and to up to 2 {@link Object}s of any type.
 */
public class Desire
{
	private IntentionType _intention;
	
	private Object _firstParameter;
	private Object _secondParameter;
	
	public Desire()
	{
		_intention = IntentionType.IDLE;
	}
	
	@Override
	public String toString()
	{
		return "Desire " + _intention.toString() + ", with following parameters: " + _firstParameter + " and " + _secondParameter;
	}
	
	public IntentionType getIntention()
	{
		return _intention;
	}
	
	public Object getFirstParameter()
	{
		return _firstParameter;
	}
	
	public Object getSecondParameter()
	{
		return _secondParameter;
	}
	
	/**
	 * Update the current {@link Desire} with parameters.
	 * @param intention : The new {@link IntentionType} to set.
	 * @param firstParameter : The first argument to set.
	 * @param secondParameter : The second argument to set.
	 */
	public synchronized void update(IntentionType intention, Object firstParameter, Object secondParameter)
	{
		_intention = intention;
		
		_firstParameter = firstParameter;
		_secondParameter = secondParameter;
	}
	
	/**
	 * Update the current {@link Desire} with parameters taken from another Desire.
	 * @param desire : The Desire to use as parameters.
	 */
	public synchronized void update(Desire desire)
	{
		_intention = desire.getIntention();
		
		_firstParameter = desire.getFirstParameter();
		_secondParameter = desire.getSecondParameter();
	}
	
	/**
	 * Reset the current {@link Desire} parameters.
	 */
	public synchronized void reset()
	{
		_intention = IntentionType.IDLE;
		
		_firstParameter = null;
		_secondParameter = null;
	}
	
	/**
	 * @return true if the current {@link Desire} got blank parameters.
	 */
	public boolean isBlank()
	{
		return _intention == IntentionType.IDLE && _firstParameter == null && _secondParameter == null;
	}
	
	/**
	 * @param intention : The intention to test.
	 * @param param1 : The first Object to test.
	 * @param param2 : The second Object to test.
	 * @return true if all tested parameters are equal (intention and both parameters).
	 */
	public boolean equals(IntentionType intention, Object param1, Object param2)
	{
		return _intention == intention && _firstParameter == param1 && _secondParameter == param2;
	}
}
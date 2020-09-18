package dev.l2j.tesla.gameserver.model.location;

import dev.l2j.tesla.commons.util.StatsSet;

/**
 * A datatype extending {@link Location}, used as a unique node of a pre-programmed route for Walker NPCs.<br>
 * <br>
 * Added to the x/y/z informations, you can also find delay (the time the Walker NPC will stand on the point without moving), the String to broadcast (null if none) and the running behavior.
 */
public class WalkerLocation extends Location
{
	private String _chat;
	private final int _delay;
	private final boolean _run;
	
	public WalkerLocation(StatsSet set, boolean run)
	{
		super(set.getInteger("X"), set.getInteger("Y"), set.getInteger("Z"));
		
		_run = run;
		_delay = set.getInteger("delay", 0) * 1000;
		_chat = set.getString("chat", null);
	}
	
	public boolean doesNpcMustRun()
	{
		return _run;
	}
	
	public int getDelay()
	{
		return _delay;
	}
	
	public String getChat()
	{
		return _chat;
	}
}
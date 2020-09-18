package dev.l2j.tesla.commons.mmocore;

import java.nio.ByteBuffer;

public abstract class AbstractPacket<T extends MMOClient<?>>
{
	protected ByteBuffer _buf;
	
	T _client;
	
	public final T getClient()
	{
		return _client;
	}
}
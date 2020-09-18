package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.network.GameClient;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.mmocore.SendablePacket;

public abstract class L2GameServerPacket extends SendablePacket<GameClient>
{
	protected static final CLogger LOGGER = new CLogger(L2GameServerPacket.class.getName());
	
	protected abstract void writeImpl();
	
	@Override
	protected void write()
	{
		if (Config.PACKET_HANDLER_DEBUG)
			LOGGER.info(getType());
		
		try
		{
			writeImpl();
		}
		catch (Throwable t)
		{
			LOGGER.error("Failed writing {} for {}. ", t, getType(), getClient().toString());
		}
	}
	
	public void runImpl()
	{
	}
	
	public String getType()
	{
		return "[S] " + getClass().getSimpleName();
	}
}
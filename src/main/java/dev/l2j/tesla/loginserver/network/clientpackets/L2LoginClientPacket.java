package dev.l2j.tesla.loginserver.network.clientpackets;

import dev.l2j.tesla.loginserver.network.LoginClient;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.mmocore.ReceivablePacket;

public abstract class L2LoginClientPacket extends ReceivablePacket<LoginClient>
{
	protected static final CLogger LOGGER = new CLogger(L2LoginClientPacket.class.getName());
	
	@Override
	protected final boolean read()
	{
		try
		{
			return readImpl();
		}
		catch (Exception e)
		{
			LOGGER.error("Failed reading {}. ", e, getClass().getSimpleName());
			return false;
		}
	}
	
	protected abstract boolean readImpl();
}

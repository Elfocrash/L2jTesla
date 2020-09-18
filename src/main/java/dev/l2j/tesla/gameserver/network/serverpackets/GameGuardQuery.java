package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * @author zabbix Lets drink to code!
 */
public class GameGuardQuery extends L2GameServerPacket
{
	public GameGuardQuery()
	{
	}
	
	@Override
	public void runImpl()
	{
		// Lets make user as gg-unauthorized, we will set him as ggOK after reply from client or kick
		getClient().setGameGuardOk(false);
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xf9);
	}
}
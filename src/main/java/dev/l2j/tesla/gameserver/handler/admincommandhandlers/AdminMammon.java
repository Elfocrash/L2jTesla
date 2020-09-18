package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.data.manager.SevenSignsManager;
import dev.l2j.tesla.gameserver.data.sql.AutoSpawnTable;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.spawn.AutoSpawn;

public class AdminMammon implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_mammon_find",
		"admin_mammon_respawn",
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_mammon_find"))
		{
			int teleportIndex = -1;
			
			try
			{
				teleportIndex = Integer.parseInt(command.substring(18));
			}
			catch (Exception NumberFormatException)
			{
				activeChar.sendMessage("Usage: //mammon_find [teleportIndex] (1 / 2)");
				return false;
			}
			
			if (!SevenSignsManager.getInstance().isSealValidationPeriod())
			{
				activeChar.sendMessage("The competition period is currently in effect.");
				return true;
			}
			
			if (teleportIndex == 1)
			{
				final AutoSpawn blackSpawnInst = AutoSpawnTable.getInstance().getAutoSpawnInstance(SevenSignsManager.MAMMON_BLACKSMITH_ID, false);
				if (blackSpawnInst != null)
				{
					final Npc[] blackInst = blackSpawnInst.getNPCInstanceList();
					if (blackInst.length > 0)
					{
						final int x1 = blackInst[0].getX(), y1 = blackInst[0].getY(), z1 = blackInst[0].getZ();
						activeChar.sendMessage("Blacksmith of Mammon: " + x1 + " " + y1 + " " + z1);
						activeChar.teleportTo(x1, y1, z1, 0);
					}
				}
				else
					activeChar.sendMessage("Blacksmith of Mammon isn't registered.");
			}
			else if (teleportIndex == 2)
			{
				final AutoSpawn merchSpawnInst = AutoSpawnTable.getInstance().getAutoSpawnInstance(SevenSignsManager.MAMMON_MERCHANT_ID, false);
				if (merchSpawnInst != null)
				{
					final Npc[] merchInst = merchSpawnInst.getNPCInstanceList();
					if (merchInst.length > 0)
					{
						final int x2 = merchInst[0].getX(), y2 = merchInst[0].getY(), z2 = merchInst[0].getZ();
						activeChar.sendMessage("Merchant of Mammon: " + x2 + " " + y2 + " " + z2);
						activeChar.teleportTo(x2, y2, z2, 0);
					}
				}
				else
					activeChar.sendMessage("Merchant of Mammon isn't registered.");
			}
			else
				activeChar.sendMessage("Invalid parameter '" + teleportIndex + "' for //mammon_find.");
		}
		else if (command.startsWith("admin_mammon_respawn"))
		{
			if (!SevenSignsManager.getInstance().isSealValidationPeriod())
			{
				activeChar.sendMessage("The competition period is currently in effect.");
				return true;
			}
			
			final AutoSpawn merchSpawnInst = AutoSpawnTable.getInstance().getAutoSpawnInstance(SevenSignsManager.MAMMON_MERCHANT_ID, false);
			if (merchSpawnInst != null)
			{
				long merchRespawn = AutoSpawnTable.getInstance().getTimeToNextSpawn(merchSpawnInst);
				activeChar.sendMessage("The Merchant of Mammon will respawn in " + (merchRespawn / 60000) + " minute(s).");
			}
			else
				activeChar.sendMessage("Merchant of Mammon isn't registered.");
			
			final AutoSpawn blackSpawnInst = AutoSpawnTable.getInstance().getAutoSpawnInstance(SevenSignsManager.MAMMON_BLACKSMITH_ID, false);
			if (blackSpawnInst != null)
			{
				long blackRespawn = AutoSpawnTable.getInstance().getTimeToNextSpawn(blackSpawnInst);
				activeChar.sendMessage("The Blacksmith of Mammon will respawn in " + (blackRespawn / 60000) + " minute(s).");
			}
			else
				activeChar.sendMessage("Blacksmith of Mammon isn't registered.");
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
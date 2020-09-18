package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.serverpackets.CameraMode;
import dev.l2j.tesla.gameserver.network.serverpackets.ExShowScreenMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.NormalCamera;
import dev.l2j.tesla.gameserver.network.serverpackets.SpecialCamera;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class AdminCamera implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_camera",
		"admin_cameramode"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_camera "))
		{
			try
			{
				final Creature target = (Creature) activeChar.getTarget();
				final String[] com = command.split(" ");
				
				target.broadcastPacket(new SpecialCamera(target.getObjectId(), Integer.parseInt(com[1]), Integer.parseInt(com[2]), Integer.parseInt(com[3]), Integer.parseInt(com[4]), Integer.parseInt(com[5]), Integer.parseInt(com[6]), Integer.parseInt(com[7]), Integer.parseInt(com[8]), Integer.parseInt(com[9])));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //camera dist yaw pitch time duration turn rise widescreen unknown");
				return false;
			}
		}
		else if (command.equals("admin_cameramode"))
		{
			// lolcheck. But basically, chance to be invisible AND rooted is kinda null, except with this command
			if (!(activeChar.getAppearance().getInvisible() && activeChar.isImmobilized()))
			{
				activeChar.setTarget(null);
				activeChar.setIsImmobilized(true);
				activeChar.sendPacket(new CameraMode(1));
				
				// Make the character disappears (from world too)
				activeChar.getAppearance().setInvisible();
				activeChar.broadcastUserInfo();
				activeChar.decayMe();
				activeChar.spawnMe();
				
				activeChar.sendPacket(new ExShowScreenMessage(1, 0, 2, false, 1, 0, 0, false, 5000, true, "To remove this text, press ALT+H. To exit, press ALT+H and type //cameramode"));
			}
			else
			{
				activeChar.setIsImmobilized(false);
				activeChar.sendPacket(new CameraMode(0));
				activeChar.sendPacket(NormalCamera.STATIC_PACKET);
				
				// Make the character appears (to world too)
				activeChar.getAppearance().setVisible();
				activeChar.broadcastUserInfo();
				
				// Teleport back the player to beginning point
				activeChar.teleportTo(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0);
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
package dev.l2j.tesla.gameserver.model.actor.instance;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.data.manager.SevenSignsManager;
import dev.l2j.tesla.gameserver.enums.CabalType;
import dev.l2j.tesla.gameserver.enums.SealType;

/**
 * This instance leads the behavior of Wyvern Managers.<br>
 * Those NPCs allow Castle Lords to mount a wyvern in return for B Crystals.<br>
 * Three configs exist so far :<br>
 * <ul>
 * <li>WYVERN_ALLOW_UPGRADER : spawn instances of Wyvern Manager through the world, or no;</li>
 * <li>WYVERN_REQUIRED_LEVEL : the strider's required level;</li>
 * <li>WYVERN_REQUIRED_CRYSTALS : the B-crystals' required amount;</li>
 * </ul>
 */
public class WyvernManagerNpc extends CastleChamberlain
{
	public WyvernManagerNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (player.getCurrentFolk() == null || player.getCurrentFolk().getObjectId() != getObjectId())
			return;
		
		if (command.startsWith("RideWyvern"))
		{
			String val = "2";
			if (player.isClanLeader())
			{
				// Verify if Dusk own Seal of Strife (if true, CLs can't mount wyvern).
				if (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE) == CabalType.DUSK)
					val = "3";
				// If player is mounted on a strider
				else if (player.isMounted() && (player.getMountNpcId() == 12526 || player.getMountNpcId() == 12527 || player.getMountNpcId() == 12528))
				{
					// Check for strider level
					if (player.getMountLevel() < Config.WYVERN_REQUIRED_LEVEL)
						val = "6";
					// Check for items consumption
					else if (player.destroyItemByItemId("Wyvern", 1460, Config.WYVERN_REQUIRED_CRYSTALS, player, true))
					{
						player.dismount();
						if (player.mount(12621, 0))
							val = "4";
					}
					else
						val = "5";
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER);
					val = "1";
				}
			}
			
			sendHtm(player, val);
		}
		else if (command.startsWith("Chat"))
		{
			String val = "1"; // Default send you to error HTM.
			try
			{
				val = command.substring(5);
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			
			sendHtm(player, val);
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		String val = "0a"; // Default value : player's clan doesn't own castle.
		
		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_OWNER) // Clan owns castle && player is CL ; send the good HTM.
			{
				if (player.isFlying()) // Already mounted on Wyvern
					val = "4";
				else
					val = "0"; // Initial screen
			}
			else if (condition == COND_CLAN_MEMBER) // Good clan, but player isn't a CL.
				val = "2";
		}
		sendHtm(player, val);
	}
	
	private void sendHtm(Player player, String val)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/wyvernmanager/wyvernmanager-" + val + ".htm");
		html.replace("%objectId%", getObjectId());
		html.replace("%npcname%", getName());
		html.replace("%wyvern_level%", Config.WYVERN_REQUIRED_LEVEL);
		html.replace("%needed_crystals%", Config.WYVERN_REQUIRED_CRYSTALS);
		player.sendPacket(html);
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
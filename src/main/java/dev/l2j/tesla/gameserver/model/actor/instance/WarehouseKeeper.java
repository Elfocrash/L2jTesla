package dev.l2j.tesla.gameserver.model.actor.instance;

import java.util.Map;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.itemcontainer.PcFreight;
import dev.l2j.tesla.gameserver.model.pledge.Clan;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.EnchantResult;
import dev.l2j.tesla.gameserver.network.serverpackets.PackageToList;
import dev.l2j.tesla.gameserver.network.serverpackets.WarehouseDepositList;
import dev.l2j.tesla.gameserver.network.serverpackets.WarehouseWithdrawList;

/**
 * An instance type extending {@link Folk}, used by warehouse keepers.<br>
 * <br>
 * A warehouse keeper stores {@link Player} items in a personal container.
 */
public class WarehouseKeeper extends Folk
{
	public WarehouseKeeper(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isWarehouse()
	{
		return true;
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/warehouse/" + filename + ".htm";
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		// Generic PK check. Send back the HTM if found and cancel current action.
		if (!Config.KARMA_PLAYER_CAN_USE_WH && player.getKarma() > 0 && showPkDenyChatWindow(player, "warehouse"))
			return;
		
		if (player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}
		
		if (player.getActiveEnchantItem() != null)
		{
			player.setActiveEnchantItem(null);
			player.sendPacket(EnchantResult.CANCELLED);
			player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
		}
		
		if (command.startsWith("WithdrawP"))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setActiveWarehouse(player.getWarehouse());
			
			if (player.getActiveWarehouse().getSize() == 0)
			{
				player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
				return;
			}
			
			player.sendPacket(new WarehouseWithdrawList(player, WarehouseWithdrawList.PRIVATE));
		}
		else if (command.equals("DepositP"))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setActiveWarehouse(player.getWarehouse());
			player.tempInventoryDisable();
			
			player.sendPacket(new WarehouseDepositList(player, WarehouseDepositList.PRIVATE));
		}
		else if (command.equals("WithdrawC"))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			if ((player.getClanPrivileges() & Clan.CP_CL_VIEW_WAREHOUSE) != Clan.CP_CL_VIEW_WAREHOUSE)
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
				return;
			}
			
			if (player.getClan().getLevel() == 0)
				player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
			else
			{
				player.setActiveWarehouse(player.getClan().getWarehouse());
				player.sendPacket(new WarehouseWithdrawList(player, WarehouseWithdrawList.CLAN));
			}
		}
		else if (command.equals("DepositC"))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			if (player.getClan() != null)
			{
				if (player.getClan().getLevel() == 0)
					player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
				else
				{
					player.setActiveWarehouse(player.getClan().getWarehouse());
					player.tempInventoryDisable();
					player.sendPacket(new WarehouseDepositList(player, WarehouseDepositList.CLAN));
				}
			}
		}
		else if (command.startsWith("WithdrawF"))
		{
			if (Config.ALLOW_FREIGHT)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				PcFreight freight = player.getFreight();
				
				if (freight != null)
				{
					if (freight.getSize() > 0)
					{
						if (Config.ALT_GAME_FREIGHTS)
							freight.setActiveLocation(0);
						else
							freight.setActiveLocation(getRegion().hashCode());
						
						player.setActiveWarehouse(freight);
						player.sendPacket(new WarehouseWithdrawList(player, WarehouseWithdrawList.FREIGHT));
					}
					else
						player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
				}
			}
		}
		else if (command.startsWith("DepositF"))
		{
			if (Config.ALLOW_FREIGHT)
			{
				// No other chars in the account of this player
				if (player.getAccountChars().isEmpty())
					player.sendPacket(SystemMessageId.CHARACTER_DOES_NOT_EXIST);
				// One or more chars other than this player for this account
				else
				{
					Map<Integer, String> chars = player.getAccountChars();
					
					if (chars.size() < 1)
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					
					player.sendPacket(new PackageToList(chars));
				}
			}
		}
		else if (command.startsWith("FreightChar"))
		{
			if (Config.ALLOW_FREIGHT)
			{
				String id = command.substring(command.lastIndexOf("_") + 1);
				
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				PcFreight freight = player.getDepositedFreight(Integer.parseInt(id));
				
				if (Config.ALT_GAME_FREIGHTS)
					freight.setActiveLocation(0);
				else
					freight.setActiveLocation(getRegion().hashCode());
				
				player.setActiveWarehouse(freight);
				player.tempInventoryDisable();
				player.sendPacket(new WarehouseDepositList(player, WarehouseDepositList.FREIGHT));
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		// Generic PK check. Send back the HTM if found and cancel current action.
		if (!Config.KARMA_PLAYER_CAN_USE_WH && player.getKarma() > 0 && showPkDenyChatWindow(player, "warehouse"))
			return;
		
		showChatWindow(player, getHtmlPath(getNpcId(), val));
	}
}
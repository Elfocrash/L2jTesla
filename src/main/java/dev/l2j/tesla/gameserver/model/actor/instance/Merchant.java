package dev.l2j.tesla.gameserver.model.actor.instance;

import java.util.List;
import java.util.StringTokenizer;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.buylist.NpcBuyList;
import dev.l2j.tesla.gameserver.network.serverpackets.BuyList;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.SellList;
import dev.l2j.tesla.gameserver.network.serverpackets.ShopPreviewList;
import dev.l2j.tesla.gameserver.data.cache.HtmCache;
import dev.l2j.tesla.gameserver.data.manager.BuyListManager;
import dev.l2j.tesla.gameserver.data.xml.MultisellData;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

/**
 * An instance type extending {@link Folk}, used for merchant (regular and multisell). It got buy/sell methods.<br>
 * <br>
 * It is used as mother class for few children, such as {@link Fisherman}.
 */
public class Merchant extends Folk
{
	public Merchant(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/merchant/" + filename + ".htm";
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		// Generic PK check. Send back the HTM if found and cancel current action.
		if (!Config.KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0 && showPkDenyChatWindow(player, "merchant"))
			return;
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		if (actualCommand.equalsIgnoreCase("Buy"))
		{
			if (st.countTokens() < 1)
				return;
			
			showBuyWindow(player, Integer.parseInt(st.nextToken()));
		}
		else if (actualCommand.equalsIgnoreCase("Sell"))
		{
			// Retrieve sellable items.
			final List<ItemInstance> items = player.getInventory().getSellableItems();
			if (items.isEmpty())
			{
				final String content = HtmCache.getInstance().getHtm("data/html/" + ((this instanceof Fisherman) ? "fisherman" : "merchant") + "/" + getNpcId() + "-empty.htm");
				if (content != null)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setHtml(content);
					html.replace("%objectId%", getObjectId());
					player.sendPacket(html);
					return;
				}
			}
			
			player.sendPacket(new SellList(player.getAdena(), items));
		}
		else if (actualCommand.equalsIgnoreCase("Wear") && Config.ALLOW_WEAR)
		{
			if (st.countTokens() < 1)
				return;
			
			showWearWindow(player, Integer.parseInt(st.nextToken()));
		}
		else if (actualCommand.equalsIgnoreCase("Multisell"))
		{
			if (st.countTokens() < 1)
				return;
			
			MultisellData.getInstance().separateAndSend(st.nextToken(), player, this, false);
		}
		else if (actualCommand.equalsIgnoreCase("Multisell_Shadow"))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			
			if (player.getLevel() < 40)
				html.setFile("data/html/common/shadow_item-lowlevel.htm");
			else if (player.getLevel() < 46)
				html.setFile("data/html/common/shadow_item_mi_c.htm");
			else if (player.getLevel() < 52)
				html.setFile("data/html/common/shadow_item_hi_c.htm");
			else
				html.setFile("data/html/common/shadow_item_b.htm");
			
			html.replace("%objectId%", getObjectId());
			player.sendPacket(html);
		}
		else if (actualCommand.equalsIgnoreCase("Exc_Multisell"))
		{
			if (st.countTokens() < 1)
				return;
			
			MultisellData.getInstance().separateAndSend(st.nextToken(), player, this, true);
		}
		else if (actualCommand.equalsIgnoreCase("Newbie_Exc_Multisell"))
		{
			if (st.countTokens() < 1)
				return;
			
			if (player.isNewbie())
				MultisellData.getInstance().separateAndSend(st.nextToken(), player, this, true);
			else
				showChatWindow(player, "data/html/exchangelvlimit.htm");
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		// Generic PK check. Send back the HTM if found and cancel current action.
		if (!Config.KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0 && showPkDenyChatWindow(player, "merchant"))
			return;
		
		showChatWindow(player, getHtmlPath(getNpcId(), val));
	}
	
	private final void showWearWindow(Player player, int val)
	{
		final NpcBuyList buyList = BuyListManager.getInstance().getBuyList(val);
		if (buyList == null || !buyList.isNpcAllowed(getNpcId()))
			return;
		
		player.tempInventoryDisable();
		player.sendPacket(new ShopPreviewList(buyList, player.getAdena(), player.getSkillLevel(L2Skill.SKILL_EXPERTISE)));
	}
	
	protected final void showBuyWindow(Player player, int val)
	{
		final NpcBuyList buyList = BuyListManager.getInstance().getBuyList(val);
		if (buyList == null || !buyList.isNpcAllowed(getNpcId()))
			return;
		
		player.tempInventoryDisable();
		player.sendPacket(new BuyList(buyList, player.getAdena(), (getCastle() != null) ? getCastle().getTaxRate() : 0));
	}
}
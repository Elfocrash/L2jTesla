package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.data.ItemTable;
import dev.l2j.tesla.gameserver.data.manager.BuyListManager;
import dev.l2j.tesla.gameserver.data.xml.NpcData;
import dev.l2j.tesla.gameserver.enums.ScriptEventType;
import dev.l2j.tesla.gameserver.enums.skills.L2SkillType;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.Merchant;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate.SkillType;
import dev.l2j.tesla.gameserver.model.buylist.NpcBuyList;
import dev.l2j.tesla.gameserver.model.buylist.Product;
import dev.l2j.tesla.gameserver.model.item.DropCategory;
import dev.l2j.tesla.gameserver.model.item.DropData;

public class AdminEditNpc implements IAdminCommandHandler
{
	private static final int PAGE_LIMIT = 20;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_show_droplist",
		"admin_show_minion",
		"admin_show_scripts",
		"admin_show_shop",
		"admin_show_shoplist",
		"admin_show_skilllist"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		st.nextToken();
		
		if (command.startsWith("admin_show_minion"))
		{
			// You need to target a Monster.
			final WorldObject target = activeChar.getTarget();
			if (!(target instanceof Monster))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final Monster monster = (Monster) target;
			
			// Load static Htm.
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/admin/minion.htm");
			html.replace("%target%", target.getName());
			
			final StringBuilder sb = new StringBuilder();
			
			// Monster is a minion, deliver boss state.
			final Monster master = monster.getMaster();
			if (master != null)
			{
				html.replace("%type%", "minion");
				StringUtil.append(sb, "<tr><td>", master.getNpcId(), "</td><td>", master.getName(), " (", ((master.isDead()) ? "Dead" : "Alive"), ")</td></tr>");
			}
			// Monster is a master, find back minions informations.
			else if (monster.hasMinions())
			{
				html.replace("%type%", "master");
				
				for (Entry<Monster, Boolean> data : monster.getMinionList().getMinions().entrySet())
					StringUtil.append(sb, "<tr><td>", data.getKey().getNpcId(), "</td><td>", data.getKey().toString(), " (", ((data.getValue()) ? "Alive" : "Dead"), ")</td></tr>");
			}
			// Monster isn't anything.
			else
				html.replace("%type%", "regular monster");
			
			html.replace("%minion%", sb.toString());
			activeChar.sendPacket(html);
		}
		else if (command.startsWith("admin_show_shoplist"))
		{
			try
			{
				showShopList(activeChar, Integer.parseInt(st.nextToken()));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //show_shoplist <list_id>");
			}
		}
		else if (command.startsWith("admin_show_shop"))
		{
			try
			{
				showShop(activeChar, Integer.parseInt(st.nextToken()));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //show_shop <npc_id>");
			}
		}
		else if (command.startsWith("admin_show_droplist"))
		{
			try
			{
				int npcId = Integer.parseInt(st.nextToken());
				int page = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 1;
				
				showNpcDropList(activeChar, npcId, page);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //show_droplist <npc_id> [<page>]");
			}
		}
		else if (command.startsWith("admin_show_skilllist"))
		{
			try
			{
				showNpcSkillList(activeChar, Integer.parseInt(st.nextToken()));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //show_skilllist <npc_id>");
			}
		}
		else if (command.startsWith("admin_show_scripts"))
		{
			try
			{
				showScriptsList(activeChar, Integer.parseInt(st.nextToken()));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //show_scripts <npc_id>");
			}
		}
		
		return true;
	}
	
	private static void showShopList(Player activeChar, int listId)
	{
		final NpcBuyList buyList = BuyListManager.getInstance().getBuyList(listId);
		if (buyList == null)
		{
			activeChar.sendMessage("BuyList template is unknown for id: " + listId + ".");
			return;
		}
		
		final StringBuilder sb = new StringBuilder(500);
		StringUtil.append(sb, "<html><body><center><font color=\"LEVEL\">", NpcData.getInstance().getTemplate(buyList.getNpcId()).getName(), " (", buyList.getNpcId(), ") buylist id: ", buyList.getListId(), "</font></center><br><table width=\"100%\"><tr><td width=200>Item</td><td width=80>Price</td></tr>");
		
		for (Product product : buyList.getProducts())
			StringUtil.append(sb, "<tr><td>", product.getItem().getName(), "</td><td>", product.getPrice(), "</td></tr>");
		
		sb.append("</table></body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private static void showShop(Player activeChar, int npcId)
	{
		final List<NpcBuyList> buyLists = BuyListManager.getInstance().getBuyListsByNpcId(npcId);
		if (buyLists.isEmpty())
		{
			activeChar.sendMessage("No buyLists found for id: " + npcId + ".");
			return;
		}
		
		final StringBuilder sb = new StringBuilder(500);
		StringUtil.append(sb, "<html><title>Merchant Shop Lists</title><body>");
		
		if (activeChar.getTarget() instanceof Merchant)
		{
			Npc merchant = (Npc) activeChar.getTarget();
			int taxRate = merchant.getCastle().getTaxPercent();
			
			StringUtil.append(sb, "<center><font color=\"LEVEL\">", merchant.getName(), " (", npcId, ")</font></center><br>Tax rate: ", taxRate, "%");
		}
		
		StringUtil.append(sb, "<table width=\"100%\">");
		
		for (NpcBuyList buyList : buyLists)
			StringUtil.append(sb, "<tr><td><a action=\"bypass -h admin_show_shoplist ", buyList.getListId(), " 1\">Buylist id: ", buyList.getListId(), "</a></td></tr>");
		
		StringUtil.append(sb, "</table></body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private static void showNpcDropList(Player activeChar, int npcId, int page)
	{
		final NpcTemplate npcData = NpcData.getInstance().getTemplate(npcId);
		if (npcData == null)
		{
			activeChar.sendMessage("Npc template is unknown for id: " + npcId + ".");
			return;
		}
		
		final StringBuilder sb = new StringBuilder(2000);
		StringUtil.append(sb, "<html><title>Show droplist page ", page, "</title><body><center><font color=\"LEVEL\">", npcData.getName(), " (", npcId, ")</font></center><br>");
		
		if (!npcData.getDropData().isEmpty())
		{
			sb.append("Drop type legend: <font color=\"3BB9FF\">Drop</font> | <font color=\"00ff00\">Sweep</font><br><table><tr><td width=25>cat.</td><td width=255>item</td></tr>");
			
			int myPage = 1;
			int i = 0;
			int shown = 0;
			boolean hasMore = false;
			
			for (DropCategory cat : npcData.getDropData())
			{
				if (shown == PAGE_LIMIT)
				{
					hasMore = true;
					break;
				}
				
				for (DropData drop : cat.getAllDrops())
				{
					if (myPage != page)
					{
						i++;
						if (i == PAGE_LIMIT)
						{
							myPage++;
							i = 0;
						}
						continue;
					}
					
					if (shown == PAGE_LIMIT)
					{
						hasMore = true;
						break;
					}
					
					StringUtil.append(sb, "<tr><td><font color=\"", ((cat.isSweep()) ? "00FF00" : "3BB9FF"), "\">", cat.getCategoryType(), "</td><td>", ItemTable.getInstance().getTemplate(drop.getItemId()).getName(), " (", drop.getItemId(), ")</td></tr>");
					shown++;
				}
			}
			
			sb.append("</table><table width=\"100%\" bgcolor=666666><tr>");
			
			if (page > 1)
			{
				StringUtil.append(sb, "<td width=120><a action=\"bypass -h admin_show_droplist ", npcId, " ", page - 1, "\">Prev Page</a></td>");
				if (!hasMore)
					StringUtil.append(sb, "<td width=100>Page ", page, "</td><td width=70></td></tr>");
			}
			
			if (hasMore)
			{
				if (page <= 1)
					sb.append("<td width=120></td>");
				
				StringUtil.append(sb, "<td width=100>Page ", page, "</td><td width=70><a action=\"bypass -h admin_show_droplist ", npcId, " ", page + 1, "\">Next Page</a></td></tr>");
			}
			sb.append("</table>");
		}
		else
			sb.append("This NPC has no drops.");
		
		sb.append("</body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private static void showNpcSkillList(Player activeChar, int npcId)
	{
		final NpcTemplate npcData = NpcData.getInstance().getTemplate(npcId);
		if (npcData == null)
		{
			activeChar.sendMessage("Npc template is unknown for id: " + npcId + ".");
			return;
		}
		
		final StringBuilder sb = new StringBuilder(500);
		StringUtil.append(sb, "<html><body><center><font color=\"LEVEL\">", npcData.getName(), " (", npcId, ") skills</font></center><br>");
		
		if (!npcData.getSkills().isEmpty())
		{
			SkillType type = null; // Used to see if we moved of type.
			
			// For any type of SkillType
			for (Entry<SkillType, List<L2Skill>> entry : npcData.getSkills().entrySet())
			{
				if (type != entry.getKey())
				{
					type = entry.getKey();
					StringUtil.append(sb, "<br><font color=\"LEVEL\">", type.name(), "</font><br1>");
				}
				
				for (L2Skill skill : entry.getValue())
					StringUtil.append(sb, ((skill.getSkillType() == L2SkillType.NOTDONE) ? ("<font color=\"777777\">" + skill.getName() + "</font>") : skill.getName()), " [", skill.getId(), "-", skill.getLevel(), "]<br1>");
			}
		}
		else
			sb.append("This NPC doesn't hold any skill.");
		
		sb.append("</body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private static void showScriptsList(Player activeChar, int npcId)
	{
		final NpcTemplate npcData = NpcData.getInstance().getTemplate(npcId);
		if (npcData == null)
		{
			activeChar.sendMessage("Npc template is unknown for id: " + npcId + ".");
			return;
		}
		
		final StringBuilder sb = new StringBuilder(500);
		StringUtil.append(sb, "<html><body><center><font color=\"LEVEL\">", npcData.getName(), " (", npcId, ")</font></center><br>");
		
		if (!npcData.getEventQuests().isEmpty())
		{
			ScriptEventType type = null; // Used to see if we moved of type.
			
			// For any type of EventType
			for (Entry<ScriptEventType, List<Quest>> entry : npcData.getEventQuests().entrySet())
			{
				if (type != entry.getKey())
				{
					type = entry.getKey();
					StringUtil.append(sb, "<br><font color=\"LEVEL\">", type.name(), "</font><br1>");
				}
				
				for (Quest quest : entry.getValue())
					StringUtil.append(sb, quest.getName(), "<br1>");
			}
		}
		else
			sb.append("This NPC isn't affected by scripts.");
		
		sb.append("</body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
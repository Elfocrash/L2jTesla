package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.SkillCoolTime;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.model.L2Effect;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class AdminBuffs implements IAdminCommandHandler
{
	private static final int PAGE_LIMIT = 20;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_buff",
		"admin_getbuffs",
		"admin_stopbuff",
		"admin_stopallbuffs",
		"admin_areacancel",
		"admin_removereuse"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_buff"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				
				Creature target = null;
				if (activeChar.getTarget() instanceof Creature)
					target = (Creature) activeChar.getTarget();
				
				if (target == null)
					target = activeChar;
				
				int skillId = Integer.parseInt(st.nextToken());
				int skillLvl = Integer.parseInt(st.nextToken());
				
				final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
				if (skill == null)
				{
					activeChar.sendMessage("Usage: //buff <skillId> <skillLvl>");
					return false;
				}
				
				skill.getEffects(activeChar, target);
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
				return true;
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //buff <skillId> <skillLvl>");
				return false;
			}
		}
		else if (command.startsWith("admin_getbuffs"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			if (st.hasMoreTokens())
			{
				String playername = st.nextToken();
				Player player = World.getInstance().getPlayer(playername);
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
					return false;
				}
				
				int page = 1;
				if (st.hasMoreTokens())
					page = Integer.parseInt(st.nextToken());
				
				showBuffs(activeChar, player, page);
				return true;
			}
			else if ((activeChar.getTarget() != null) && (activeChar.getTarget() instanceof Creature))
			{
				showBuffs(activeChar, (Creature) activeChar.getTarget(), 1);
				return true;
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
		}
		else if (command.startsWith("admin_stopbuff"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				
				st.nextToken();
				int objectId = Integer.parseInt(st.nextToken());
				int skillId = Integer.parseInt(st.nextToken());
				
				removeBuff(activeChar, objectId, skillId);
				return true;
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //stopbuff <objectId> <skillId>");
				return false;
			}
		}
		else if (command.startsWith("admin_stopallbuffs"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int objectId = Integer.parseInt(st.nextToken());
				removeAllBuffs(activeChar, objectId);
				return true;
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //stopallbuffs <objectId>");
				return false;
			}
		}
		else if (command.startsWith("admin_areacancel"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				String val = st.nextToken();
				int radius = Integer.parseInt(val);
				
				for (Player knownChar : activeChar.getKnownTypeInRadius(Player.class, radius))
					knownChar.stopAllEffects();
				
				activeChar.sendMessage("All effects canceled within radius " + radius + ".");
				return true;
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //areacancel <radius>");
				return false;
			}
		}
		else if (command.startsWith("admin_removereuse"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			Player player = null;
			if (st.hasMoreTokens())
			{
				final String name = st.nextToken();
				
				player = World.getInstance().getPlayer(name);
				if (player == null)
				{
					activeChar.sendMessage("The player " + name + " is not online.");
					return false;
				}
			}
			else if (activeChar.getTarget() instanceof Player)
				player = (Player) activeChar.getTarget();
			
			if (player == null)
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
			
			player.getReuseTimeStamp().clear();
			player.getDisabledSkills().clear();
			player.sendPacket(new SkillCoolTime(player));
			activeChar.sendMessage(player.getName() + "'s skills reuse time is now cleaned.");
			return true;
		}
		else
			return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	public static void showBuffs(Player activeChar, Creature target, int page)
	{
		final L2Effect[] effects = target.getAllEffects();
		
		if (page > effects.length / PAGE_LIMIT + 1 || page < 1)
			return;
		
		int max = effects.length / PAGE_LIMIT;
		if (effects.length > PAGE_LIMIT * max)
			max++;
		
		final StringBuilder sb = new StringBuilder("<html><table width=\"100%\"><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=180><center><font color=\"LEVEL\">Effects of " + target.getName() + "</font></td><td width=45><button value=\"Back\" action=\"bypass -h admin_current_player\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br><table width=\"100%\"><tr><td width=160>Skill</td><td width=60>Time Left</td><td width=60>Action</td></tr>");
		
		int start = ((page - 1) * PAGE_LIMIT);
		int end = Math.min(((page - 1) * PAGE_LIMIT) + PAGE_LIMIT, effects.length);
		
		for (int i = start; i < end; i++)
		{
			L2Effect e = effects[i];
			if (e != null)
				StringUtil.append(sb, "<tr><td>", e.getSkill().getName(), "</td><td>", (e.getSkill().isToggle()) ? "toggle" : e.getPeriod() - e.getTime() + "s", "</td><td><a action=\"bypass -h admin_stopbuff ", target.getObjectId(), " ", e.getSkill().getId(), "\">Remove</a></td></tr>");
		}
		
		sb.append("</table><br><table width=\"100%\" bgcolor=444444><tr>");
		for (int x = 0; x < max; x++)
		{
			int pagenr = x + 1;
			if (page == pagenr)
				StringUtil.append(sb, "<td>Page ", pagenr, "</td>");
			else
				StringUtil.append(sb, "<td><a action=\"bypass -h admin_getbuffs ", target.getName(), " ", x + 1, "\"> Page ", pagenr, "</a></td>");
		}
		
		StringUtil.append(sb, "</tr></table><br><center><button value=\"Remove All\" action=\"bypass -h admin_stopallbuffs ", target.getObjectId(), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private static void removeBuff(Player activeChar, int objId, int skillId)
	{
		if (skillId < 1)
			return;
		
		final WorldObject obj = World.getInstance().getObject(objId);
		if (obj instanceof Creature)
		{
			final Creature target = (Creature) obj;
			
			for (L2Effect e : target.getAllEffects())
			{
				if (e != null && e.getSkill().getId() == skillId)
				{
					e.exit();
					activeChar.sendMessage("Removed " + e.getSkill().getName() + " level " + e.getSkill().getLevel() + " from " + target.getName() + " (" + objId + ")");
				}
			}
			showBuffs(activeChar, target, 1);
		}
	}
	
	private static void removeAllBuffs(Player activeChar, int objId)
	{
		final WorldObject target = World.getInstance().getObject(objId);
		if (target instanceof Creature)
		{
			((Creature) target).stopAllEffects();
			activeChar.sendMessage("Removed all effects from " + target.getName() + " (" + objId + ")");
			showBuffs(activeChar, ((Creature) target), 1);
		}
	}
}
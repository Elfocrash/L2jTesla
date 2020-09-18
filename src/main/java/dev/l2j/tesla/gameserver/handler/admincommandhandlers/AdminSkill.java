package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillUse;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.lang.StringUtil;
import dev.l2j.tesla.commons.math.MathUtil;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class AdminSkill implements IAdminCommandHandler
{
	private static final int PAGE_LIMIT = 10;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_show_skills",
		"admin_remove_skills",
		"admin_skill_list",
		"admin_skill_index",
		"admin_add_skill",
		"admin_remove_skill",
		"admin_get_skills",
		"admin_reset_skills",
		"admin_give_all_skills",
		"admin_remove_all_skills",
		"admin_add_clan_skill",
		"admin_st"
	};
	
	private static final List<L2Skill> ADMIN_SKILLS = new ArrayList<>();
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_show_skills"))
			showMainPage(activeChar);
		else if (command.startsWith("admin_remove_skills"))
		{
			try
			{
				removeSkillsPage(activeChar, Integer.parseInt(command.substring(20)));
			}
			catch (Exception e)
			{
				removeSkillsPage(activeChar, 1);
			}
		}
		else if (command.startsWith("admin_skill_list"))
		{
			AdminHelpPage.showHelpPage(activeChar, "skills.htm");
		}
		else if (command.startsWith("admin_skill_index"))
		{
			try
			{
				String val = command.substring(18);
				AdminHelpPage.showHelpPage(activeChar, "skills/" + val + ".htm");
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_add_skill"))
		{
			try
			{
				String val = command.substring(15);
				adminAddSkill(activeChar, val);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //add_skill <skill_id> <level>");
			}
		}
		else if (command.startsWith("admin_remove_skill"))
		{
			try
			{
				String id = command.substring(19);
				int idval = Integer.parseInt(id);
				adminRemoveSkill(activeChar, idval);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //remove_skill <skill_id>");
			}
		}
		else if (command.equals("admin_get_skills"))
		{
			adminGetSkills(activeChar);
		}
		else if (command.equals("admin_reset_skills"))
			adminResetSkills(activeChar);
		else if (command.equals("admin_give_all_skills"))
			adminGiveAllSkills(activeChar);
		else if (command.equals("admin_remove_all_skills"))
		{
			if (activeChar.getTarget() instanceof Player)
			{
				Player player = (Player) activeChar.getTarget();
				
				for (L2Skill skill : player.getSkills().values())
					player.removeSkill(skill.getId(), true);
				
				activeChar.sendMessage("You removed all skills from " + player.getName() + ".");
				if (player != activeChar)
					player.sendMessage("Admin removed all skills from you.");
				
				player.sendSkillList();
			}
		}
		else if (command.startsWith("admin_add_clan_skill"))
		{
			try
			{
				String[] val = command.split(" ");
				adminAddClanSkill(activeChar, Integer.parseInt(val[1]), Integer.parseInt(val[2]));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			}
		}
		else if (command.startsWith("admin_st"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				
				int id = Integer.parseInt(st.nextToken());
				adminTestSkill(activeChar, id);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Used to test skills' visual effect, format : //st <ID>");
			}
		}
		
		return true;
	}
	
	private static void adminTestSkill(Player activeChar, int id)
	{
		Creature player;
		WorldObject target = activeChar.getTarget();
		
		if (target == null || !(target instanceof Creature))
			player = activeChar;
		else
			player = (Creature) target;
		
		player.broadcastPacket(new MagicSkillUse(activeChar, player, id, 1, 1, 1));
	}
	
	/**
	 * This function will give all the skills that the target can learn at his/her level
	 * @param activeChar The GM char.
	 */
	private static void adminGiveAllSkills(Player activeChar)
	{
		WorldObject target = activeChar.getTarget();
		Player player = null;
		
		if (target instanceof Player)
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		player.rewardSkills();
		activeChar.sendMessage("You gave all available skills to " + player.getName() + ".");
	}
	
	private static void removeSkillsPage(Player activeChar, int page)
	{
		WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player)
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		
		List<L2Skill> skills = new ArrayList<>(player.getSkills().values());
		
		final int max = MathUtil.countPagesNumber(skills.size(), PAGE_LIMIT);
		
		skills = skills.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, skills.size()));
		
		final StringBuilder sb = new StringBuilder(3000);
		StringUtil.append(sb, "<html><body><table width=270><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=180><center>Delete Skills Menu</center></td><td width=45><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br><br><center>Editing <font color=\"LEVEL\">", player.getName(), "</font>, ", player.getTemplate().getClassName(), " lvl ", player.getLevel(), ".<br><center><table width=270><tr>");
		
		for (int i = 0; i < max; i++)
		{
			final int pagenr = i + 1;
			if (page == pagenr)
				StringUtil.append(sb, "<td>", pagenr, "</td>");
			else
				StringUtil.append(sb, "<td><a action=\"bypass -h admin_remove_skills ", pagenr, "\">", pagenr, "</a></td>");
		}
		
		sb.append("</tr></table></center><br><table width=270><tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>");
		
		for (L2Skill skill : skills)
			StringUtil.append(sb, "<tr><td width=80><a action=\"bypass -h admin_remove_skill ", skill.getId(), "\">", skill.getName(), "</a></td><td width=60>", skill.getLevel(), "</td><td width=40>", skill.getId(), "</td></tr>");
		
		sb.append("</table><br><center><table width=200><tr><td width=50 align=right>Id: </td><td><edit var=\"id_to_remove\" width=55></td><td width=100><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=95 height=21 back=\"bigbutton_over\" fore=\"bigbutton\"></td></tr><tr><td></td><td></td><td><button value=\"Back to stats\" action=\"bypass -h admin_current_player\" width=95 height=21 back=\"bigbutton_over\" fore=\"bigbutton\"></td></tr></table></center></body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private static void showMainPage(Player activeChar)
	{
		WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player)
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/charskills.htm");
		html.replace("%name%", player.getName());
		html.replace("%level%", player.getLevel());
		html.replace("%class%", player.getTemplate().getClassName());
		activeChar.sendPacket(html);
	}
	
	private static void adminGetSkills(Player activeChar)
	{
		WorldObject target = activeChar.getTarget();
		Player player = null;
		
		if (target instanceof Player)
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		if (player == activeChar)
			player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
		else
		{
			ADMIN_SKILLS.addAll(activeChar.getSkills().values());
			
			for (L2Skill skill : ADMIN_SKILLS)
				activeChar.removeSkill(skill.getId(), false);
			
			for (L2Skill skill : player.getSkills().values())
				activeChar.addSkill(skill, false, true);
			
			activeChar.sendMessage("You ninjaed " + player.getName() + "'s skills list.");
			activeChar.sendSkillList();
		}
	}
	
	private static void adminResetSkills(Player activeChar)
	{
		if (ADMIN_SKILLS.isEmpty())
			activeChar.sendMessage("Ninja first skills of someone to use that command.");
		else
		{
			for (L2Skill skill : activeChar.getSkills().values())
				activeChar.removeSkill(skill.getId(), false);
			
			for (L2Skill skill : ADMIN_SKILLS)
				activeChar.addSkill(skill, false, true);
			
			activeChar.sendMessage("All your skills have been returned back.");
			activeChar.sendSkillList();
			
			ADMIN_SKILLS.clear();
		}
	}
	
	private static void adminAddSkill(Player activeChar, String val)
	{
		WorldObject target = activeChar.getTarget();
		Player player = null;
		
		if (target instanceof Player)
			player = (Player) target;
		else
		{
			showMainPage(activeChar);
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		StringTokenizer st = new StringTokenizer(val);
		if (st.countTokens() != 2)
			showMainPage(activeChar);
		else
		{
			L2Skill skill = null;
			try
			{
				String id = st.nextToken();
				String level = st.nextToken();
				int idval = Integer.parseInt(id);
				int levelval = Integer.parseInt(level);
				skill = SkillTable.getInstance().getInfo(idval, levelval);
			}
			catch (Exception e)
			{
			}
			
			if (skill != null)
			{
				String name = skill.getName();
				
				player.addSkill(skill, true, true);
				player.sendMessage("Admin gave you the skill " + name + ".");
				if (player != activeChar)
					activeChar.sendMessage("You gave the skill " + name + " to " + player.getName() + ".");
				
				player.sendSkillList();
			}
			else
				activeChar.sendMessage("Error: there is no such skill.");
			
			showMainPage(activeChar); // Back to start
		}
	}
	
	private static void adminRemoveSkill(Player activeChar, int idval)
	{
		WorldObject target = activeChar.getTarget();
		Player player = null;
		
		if (target instanceof Player)
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final L2Skill skill = player.removeSkill(idval, true);
		if (skill == null)
			activeChar.sendMessage("Error: there is no such skill.");
		else
		{
			activeChar.sendMessage("You removed the skill " + skill.getName() + " from " + player.getName() + ".");
			if (player != activeChar)
				player.sendMessage("Admin removed the skill " + skill.getName() + " from your skills list.");
			
			player.sendSkillList();
		}
		
		removeSkillsPage(activeChar, 1);
	}
	
	private static void adminAddClanSkill(Player activeChar, int id, int level)
	{
		WorldObject target = activeChar.getTarget();
		Player player = null;
		
		if (target instanceof Player)
			player = (Player) target;
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			showMainPage(activeChar);
			return;
		}
		
		if (!player.isClanLeader())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addCharName(player));
			showMainPage(activeChar);
			return;
		}
		
		if (id < 370 || id > 391 || level < 1 || level > 3)
		{
			activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			showMainPage(activeChar);
			return;
		}
		
		L2Skill skill = SkillTable.getInstance().getInfo(id, level);
		if (skill == null)
		{
			activeChar.sendMessage("Error: there is no such skill.");
			return;
		}
		
		player.getClan().addNewSkill(skill);
		
		activeChar.sendMessage("You gave " + skill.getName() + " Clan Skill to " + player.getClan().getName() + " clan.");
		
		showMainPage(activeChar);
		return;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
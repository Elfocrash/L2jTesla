package dev.l2j.tesla.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.handler.IAdminCommandHandler;
import dev.l2j.tesla.gameserver.network.GameClient;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.AbstractNpcInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.GMViewItemList;
import dev.l2j.tesla.gameserver.network.serverpackets.HennaInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.commons.lang.StringUtil;
import dev.l2j.tesla.commons.math.MathUtil;

import dev.l2j.tesla.gameserver.data.manager.CastleManager;
import dev.l2j.tesla.gameserver.data.manager.ClanHallManager;
import dev.l2j.tesla.gameserver.data.sql.PlayerInfoTable;
import dev.l2j.tesla.gameserver.data.xml.NpcData;
import dev.l2j.tesla.gameserver.data.xml.PlayerData;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.enums.actors.Sex;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;
import dev.l2j.tesla.gameserver.model.actor.instance.Pet;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.model.pledge.Clan;

public class AdminEditChar implements IAdminCommandHandler
{
	private static final int PAGE_LIMIT = 20;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_changelvl", // edit player access level
		"admin_edit_character",
		"admin_current_player",
		"admin_setkarma", // sets karma of target char to any amount. //setkarma <karma>
		"admin_character_info", // given a player name, displays an information window
		"admin_debug", // same than previous
		"admin_show_characters", // list of characters
		"admin_find_character", // find a player by his name or a part of it (case-insensitive)
		"admin_find_ip", // find all the player connections from a given IPv4 number
		"admin_find_account", // list all the characters from an account (useful for GMs w/o DB access)
		"admin_find_dualbox", // list all IPs with more than 1 char logged in (dualbox)
		"admin_rec", // gives recommendation points
		"admin_settitle", // changes char's title
		"admin_setname", // changes char's name
		"admin_setsex", // changes char's sex
		"admin_setcolor", // change char name's color
		"admin_settcolor", // change char title's color
		"admin_setclass", // changes char's classId
		
		"admin_summon_info", // displays an information window about target summon
		"admin_unsummon", // unsummon target's pet/summon
		"admin_summon_setlvl", // set the pet's level
		"admin_show_pet_inv", // show pet's inventory
		"admin_fullfood", // fulfills a pet's food bar
		
		"admin_party_info", // find party infos of targeted character, if any
		"admin_clan_info", // find clan infos of the character, if any
		"admin_remove_clan_penalty" // removes clan penalties
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_changelvl"))
		{
			try
			{
				final StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				
				final int paramCount = st.countTokens();
				if (paramCount == 1)
				{
					final int lvl = Integer.parseInt(st.nextToken());
					if (activeChar.getTarget() instanceof Player)
						onLineChange(activeChar, (Player) activeChar.getTarget(), lvl);
					else
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				else if (paramCount == 2)
				{
					final String name = st.nextToken();
					final int lvl = Integer.parseInt(st.nextToken());
					
					final Player player = World.getInstance().getPlayer(name);
					if (player != null)
						onLineChange(activeChar, player, lvl);
					else
					{
						try (Connection con = L2DatabaseFactory.getInstance().getConnection();
							 PreparedStatement ps = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?"))
						{
							ps.setInt(1, lvl);
							ps.setString(2, name);
							ps.execute();
							
							final int count = ps.getUpdateCount();
							if (count == 0)
								activeChar.sendMessage("Player can't be found or access level unaltered.");
							else
								activeChar.sendMessage("Player's access level is now set to " + lvl);
						}
						catch (Exception e)
						{
						}
					}
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //changelvl <target_new_level> | <player_name> <new_level>");
			}
		}
		else if (command.equals("admin_current_player"))
			showCharacterInfo(activeChar, null);
		else if (command.startsWith("admin_character_info") || command.startsWith("admin_debug"))
		{
			try
			{
				final String name = command.substring((command.startsWith("admin_character_info")) ? 21 : 12);
				final Player target = World.getInstance().getPlayer(name);
				if (target == null)
				{
					activeChar.sendPacket(SystemMessageId.CHARACTER_DOES_NOT_EXIST);
					return true;
				}
				showCharacterInfo(activeChar, target);
			}
			catch (Exception e)
			{
				showCharacterInfo(activeChar, null);
			}
		}
		else if (command.startsWith("admin_show_characters"))
		{
			try
			{
				listCharacters(activeChar, Integer.parseInt(command.substring(22)));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //show_characters <page_number>");
			}
		}
		else if (command.startsWith("admin_find_character"))
		{
			try
			{
				findCharacter(activeChar, command.substring(21));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //find_character <character_name>");
				listCharacters(activeChar, 1);
			}
		}
		else if (command.startsWith("admin_find_ip"))
		{
			try
			{
				findCharactersPerIp(activeChar, command.substring(14));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //find_ip <www.xxx.yyy.zzz>");
				listCharacters(activeChar, 1);
			}
		}
		else if (command.startsWith("admin_find_account"))
		{
			try
			{
				findCharactersPerAccount(activeChar, command.substring(19));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //find_account <player_name>");
				listCharacters(activeChar, 1);
			}
		}
		else if (command.startsWith("admin_find_dualbox"))
		{
			int multibox = 2;
			try
			{
				multibox = Integer.parseInt(command.substring(19));
				if (multibox < 1)
				{
					activeChar.sendMessage("Usage: //find_dualbox [number > 0]");
					return false;
				}
			}
			catch (Exception e)
			{
			}
			findDualbox(activeChar, multibox);
		}
		else if (command.equals("admin_edit_character"))
			editCharacter(activeChar);
		else if (command.startsWith("admin_setkarma"))
		{
			try
			{
				setTargetKarma(activeChar, Integer.parseInt(command.substring(15)));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setkarma <new_karma_value>");
			}
		}
		else if (command.startsWith("admin_rec"))
		{
			try
			{
				WorldObject target = activeChar.getTarget();
				Player player = null;
				
				if (target instanceof Player)
					player = (Player) target;
				else
					return false;
				
				player.setRecomHave(Integer.parseInt(command.substring(10)));
				player.sendMessage("You have been recommended by a GM.");
				player.broadcastUserInfo();
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //rec number");
			}
		}
		else if (command.startsWith("admin_setclass"))
		{
			try
			{
				WorldObject target = activeChar.getTarget();
				Player player = null;
				
				if (target instanceof Player)
					player = (Player) target;
				else
					return false;
				
				boolean valid = false;
				
				final int classidval = Integer.parseInt(command.substring(15));
				for (ClassId classid : ClassId.VALUES)
					if (classidval == classid.getId())
						valid = true;
					
				if (valid && (player.getClassId().getId() != classidval))
				{
					player.setClassId(classidval);
					if (!player.isSubClassActive())
						player.setBaseClass(classidval);
					
					String newclass = player.getTemplate().getClassName();
					
					player.refreshOverloaded();
					player.store();
					player.sendPacket(new HennaInfo(player));
					player.broadcastUserInfo();
					
					// Messages
					if (player != activeChar)
						player.sendMessage("A GM changed your class to " + newclass + ".");
					activeChar.sendMessage(player.getName() + " is now a " + newclass + ".");
				}
				else
					activeChar.sendMessage("Usage: //setclass <valid classid>");
			}
			catch (Exception e)
			{
				AdminHelpPage.showHelpPage(activeChar, "charclasses.htm");
			}
		}
		else if (command.startsWith("admin_settitle"))
		{
			try
			{
				final WorldObject target = activeChar.getTarget();
				final String newTitle = command.substring(15);
				
				if (target instanceof Player)
				{
					final Player player = (Player) target;
					
					player.setTitle(newTitle);
					player.sendMessage("Your title has been changed by a GM.");
					player.broadcastTitleInfo();
				}
				else if (target instanceof Npc)
				{
					final Npc npc = (Npc) target;
					
					npc.setTitle(newTitle);
					npc.broadcastPacket(new AbstractNpcInfo.NpcInfo(npc, null));
				}
				else
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //settitle title");
			}
		}
		else if (command.startsWith("admin_setname"))
		{
			try
			{
				final WorldObject target = activeChar.getTarget();
				final String newName = command.substring(14);
				
				if (target instanceof Player)
				{
					// Invalid pattern.
					if (!StringUtil.isValidString(newName, "^[A-Za-z0-9]{3,16}$"))
					{
						activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
						return false;
					}
					
					// Name is a npc name.
					if (NpcData.getInstance().getTemplateByName(newName) != null)
					{
						activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
						return false;
					}
					
					// Name already exists.
					if (PlayerInfoTable.getInstance().getPlayerObjectId(newName) > 0)
					{
						activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
						return false;
					}
					
					final Player player = (Player) target;
					
					player.setName(newName);
					PlayerInfoTable.getInstance().updatePlayerData(player, false);
					player.sendMessage("Your name has been changed by a GM.");
					player.broadcastUserInfo();
					player.store();
				}
				else if (target instanceof Npc)
				{
					final Npc npc = (Npc) target;
					
					npc.setName(newName);
					npc.broadcastPacket(new AbstractNpcInfo.NpcInfo(npc, null));
				}
				else
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setname name");
			}
		}
		else if (command.startsWith("admin_setsex"))
		{
			WorldObject target = activeChar.getTarget();
			Player player = null;
			
			if (target instanceof Player)
				player = (Player) target;
			else
				return false;
			
			Sex sex = Sex.MALE;
			try
			{
				final StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				
				sex = Enum.valueOf(Sex.class, st.nextToken().toUpperCase());
			}
			catch (Exception e)
			{
			}
			
			if (sex != player.getAppearance().getSex())
			{
				player.getAppearance().setSex(sex);
				player.sendMessage("Your gender has been changed to " + sex.toString() + " by a GM.");
				player.broadcastUserInfo();
				
				player.decayMe();
				player.spawnMe();
			}
			else
				activeChar.sendMessage("The character sex is already defined as " + sex.toString() + ".");
		}
		else if (command.startsWith("admin_setcolor"))
		{
			try
			{
				WorldObject target = activeChar.getTarget();
				Player player = null;
				
				if (target instanceof Player)
					player = (Player) target;
				else
					return false;
				
				player.getAppearance().setNameColor(Integer.decode("0x" + command.substring(15)));
				player.sendMessage("Your name color has been changed by a GM.");
				player.broadcastUserInfo();
			}
			catch (Exception e)
			{
				activeChar.sendMessage("You need to specify a valid new color.");
			}
		}
		else if (command.startsWith("admin_settcolor"))
		{
			try
			{
				WorldObject target = activeChar.getTarget();
				Player player = null;
				
				if (target instanceof Player)
					player = (Player) target;
				else
					return false;
				
				player.getAppearance().setTitleColor(Integer.decode("0x" + command.substring(16)));
				player.sendMessage("Your title color has been changed by a GM.");
				player.broadcastUserInfo();
			}
			catch (Exception e)
			{
				activeChar.sendMessage("You need to specify a valid new color.");
			}
		}
		else if (command.startsWith("admin_summon_info"))
		{
			final WorldObject target = activeChar.getTarget();
			if (!(target instanceof Playable))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final Player targetPlayer = target.getActingPlayer();
			if (targetPlayer != null)
			{
				final Summon summon = targetPlayer.getSummon();
				if (summon != null)
					gatherSummonInfo(summon, activeChar);
				else
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
		}
		else if (command.startsWith("admin_unsummon"))
		{
			final WorldObject target = activeChar.getTarget();
			if (!(target instanceof Playable))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final Player targetPlayer = target.getActingPlayer();
			if (targetPlayer != null)
			{
				final Summon summon = targetPlayer.getSummon();
				if (summon != null)
					summon.unSummon(targetPlayer);
				else
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
		}
		else if (command.startsWith("admin_summon_setlvl"))
		{
			WorldObject target = activeChar.getTarget();
			if (target instanceof Pet)
			{
				Pet pet = (Pet) target;
				try
				{
					final int level = Integer.parseInt(command.substring(20));
					
					final long oldExp = pet.getStat().getExp();
					final long newExp = pet.getStat().getExpForLevel(level);
					
					if (oldExp > newExp)
						pet.getStat().removeExp(oldExp - newExp);
					else if (oldExp < newExp)
						pet.getStat().addExp(newExp - oldExp);
				}
				catch (Exception e)
				{
				}
			}
			else
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		else if (command.startsWith("admin_show_pet_inv"))
		{
			WorldObject target;
			try
			{
				target = World.getInstance().getPet(Integer.parseInt(command.substring(19)));
			}
			catch (Exception e)
			{
				target = activeChar.getTarget();
			}
			
			if (target instanceof Pet)
				activeChar.sendPacket(new GMViewItemList((Pet) target));
			else
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			
		}
		else if (command.startsWith("admin_fullfood"))
		{
			WorldObject target = activeChar.getTarget();
			if (target instanceof Pet)
			{
				Pet targetPet = (Pet) target;
				targetPet.setCurrentFed(targetPet.getPetData().getMaxMeal());
			}
			else
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		else if (command.startsWith("admin_party_info"))
		{
			WorldObject target;
			try
			{
				target = World.getInstance().getPlayer(command.substring(17));
				if (target == null)
					target = activeChar.getTarget();
			}
			catch (Exception e)
			{
				target = activeChar.getTarget();
			}
			
			if (!(target instanceof Player))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final Player player = ((Player) target);
			
			final Party party = player.getParty();
			if (party == null)
			{
				activeChar.sendMessage(player.getName() + " isn't in a party.");
				return false;
			}
			
			final StringBuilder sb = new StringBuilder(400);
			for (Player member : party.getMembers())
			{
				if (!party.isLeader(member))
					StringUtil.append(sb, "<tr><td width=150><a action=\"bypass -h admin_character_info ", member.getName(), "\">", member.getName(), " (", member.getLevel(), ")</a></td><td width=120 align=right>", member.getClassId().toString(), "</td></tr>");
				else
					StringUtil.append(sb, "<tr><td width=150><a action=\"bypass -h admin_character_info ", member.getName(), "\"><font color=\"LEVEL\">", member.getName(), " (", member.getLevel(), ")</font></a></td><td width=120 align=right>", member.getClassId().toString(), "</td></tr>");
			}
			
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/admin/partyinfo.htm");
			html.replace("%party%", sb.toString());
			activeChar.sendPacket(html);
		}
		else if (command.startsWith("admin_clan_info"))
		{
			try
			{
				final Player player = World.getInstance().getPlayer(command.substring(16));
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
					return false;
				}
				
				final Clan clan = player.getClan();
				if (clan == null)
				{
					activeChar.sendMessage("This player isn't in a clan.");
					return false;
				}
				
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/admin/claninfo.htm");
				html.replace("%clan_name%", clan.getName());
				html.replace("%clan_leader%", clan.getLeaderName());
				html.replace("%clan_level%", clan.getLevel());
				html.replace("%clan_has_castle%", (clan.hasCastle()) ? CastleManager.getInstance().getCastleById(clan.getCastleId()).getName() : "No");
				html.replace("%clan_has_clanhall%", (clan.hasClanHall()) ? ClanHallManager.getInstance().getClanHall(clan.getClanHallId()).getName() : "No");
				html.replace("%clan_points%", clan.getReputationScore());
				html.replace("%clan_players_count%", clan.getMembersCount());
				html.replace("%clan_ally%", (clan.getAllyId() > 0) ? clan.getAllyName() : "Not in ally");
				activeChar.sendPacket(html);
			}
			catch (Exception e)
			{
				activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			}
		}
		else if (command.startsWith("admin_remove_clan_penalty"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				if (st.countTokens() != 3)
				{
					activeChar.sendMessage("Usage: //remove_clan_penalty join|create charname");
					return false;
				}
				
				st.nextToken();
				
				boolean changeCreateExpiryTime = st.nextToken().equalsIgnoreCase("create");
				String playerName = st.nextToken();
				
				Player player = World.getInstance().getPlayer(playerName);
				if (player == null)
				{
					try (Connection con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement ps = con.prepareStatement("UPDATE characters SET " + (changeCreateExpiryTime ? "clan_create_expiry_time" : "clan_join_expiry_time") + " WHERE char_name=? LIMIT 1"))
					{
						ps.setString(1, playerName);
						ps.execute();
					}
					catch (Exception e)
					{
					}
				}
				else
				{
					// removing penalty
					if (changeCreateExpiryTime)
						player.setClanCreateExpiryTime(0);
					else
						player.setClanJoinExpiryTime(0);
				}
				activeChar.sendMessage("Clan penalty is successfully removed for " + playerName + ".");
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Couldn't remove clan penalty.");
			}
		}
		return true;
	}
	
	private static void onLineChange(Player activeChar, Player player, int lvl)
	{
		player.setAccessLevel(lvl);
		
		if (lvl >= 0)
			player.sendMessage("Your access level has been changed to " + lvl + ".");
		else
			player.logout(false);
		
		activeChar.sendMessage(player.getName() + "'s access level is now set to " + lvl + ".");
	}
	
	private static void listCharacters(Player activeChar, int page)
	{
		List<Player> players = new ArrayList<>(World.getInstance().getPlayers());
		
		final int max = MathUtil.countPagesNumber(players.size(), PAGE_LIMIT);
		
		players = players.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, players.size()));
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/charlist.htm");
		
		final StringBuilder sb = new StringBuilder(players.size() * 200);
		
		// First use of sb.
		for (int x = 0; x < max; x++)
		{
			final int pagenr = x + 1;
			if (page == pagenr)
				StringUtil.append(sb, pagenr, "&nbsp;");
			else
				StringUtil.append(sb, "<a action=\"bypass -h admin_show_characters ", pagenr, "\">", pagenr, "</a>&nbsp;");
		}
		html.replace("%pages%", sb.toString());
		
		// Cleanup current sb.
		sb.setLength(0);
		
		// Second use of sb, add player info into new table row.
		for (Player player : players)
			StringUtil.append(sb, "<tr><td width=80><a action=\"bypass -h admin_character_info ", player.getName(), "\">", player.getName(), "</a></td><td width=110>", player.getTemplate().getClassName(), "</td><td width=40>", player.getLevel(), "</td></tr>");
		
		html.replace("%players%", sb.toString());
		activeChar.sendPacket(html);
	}
	
	public static void showCharacterInfo(Player activeChar, Player player)
	{
		if (player == null)
		{
			WorldObject target = activeChar.getTarget();
			if (!(target instanceof Player))
				return;
			
			player = (Player) target;
		}
		else
			activeChar.setTarget(player);
		
		gatherCharacterInfo(activeChar, player, "charinfo.htm");
	}
	
	/**
	 * Gather character informations.
	 * @param activeChar The player who requested that action.
	 * @param player The target to gather informations from.
	 * @param filename The name of the HTM to send.
	 */
	private static void gatherCharacterInfo(Player activeChar, Player player, String filename)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/" + filename);
		html.replace("%name%", player.getName());
		html.replace("%level%", player.getLevel());
		html.replace("%clan%", player.getClan() != null ? "<a action=\"bypass -h admin_clan_info " + player.getName() + "\">" + player.getClan().getName() + "</a>" : "none");
		html.replace("%xp%", player.getExp());
		html.replace("%sp%", player.getSp());
		html.replace("%class%", player.getTemplate().getClassName());
		html.replace("%ordinal%", player.getClassId().ordinal());
		html.replace("%classid%", player.getClassId().toString());
		html.replace("%baseclass%", PlayerData.getInstance().getClassNameById(player.getBaseClass()));
		html.replace("%x%", player.getX());
		html.replace("%y%", player.getY());
		html.replace("%z%", player.getZ());
		html.replace("%currenthp%", (int) player.getCurrentHp());
		html.replace("%maxhp%", player.getMaxHp());
		html.replace("%karma%", player.getKarma());
		html.replace("%currentmp%", (int) player.getCurrentMp());
		html.replace("%maxmp%", player.getMaxMp());
		html.replace("%pvpflag%", player.getPvpFlag());
		html.replace("%currentcp%", (int) player.getCurrentCp());
		html.replace("%maxcp%", player.getMaxCp());
		html.replace("%pvpkills%", player.getPvpKills());
		html.replace("%pkkills%", player.getPkKills());
		html.replace("%currentload%", player.getCurrentLoad());
		html.replace("%maxload%", player.getMaxLoad());
		html.replace("%percent%", MathUtil.roundTo(((float) player.getCurrentLoad() / (float) player.getMaxLoad()) * 100, 2));
		html.replace("%patk%", player.getPAtk(null));
		html.replace("%matk%", player.getMAtk(null, null));
		html.replace("%pdef%", player.getPDef(null));
		html.replace("%mdef%", player.getMDef(null, null));
		html.replace("%accuracy%", player.getAccuracy());
		html.replace("%evasion%", player.getEvasionRate(null));
		html.replace("%critical%", player.getCriticalHit(null, null));
		html.replace("%runspeed%", player.getMoveSpeed());
		html.replace("%patkspd%", player.getPAtkSpd());
		html.replace("%matkspd%", player.getMAtkSpd());
		html.replace("%account%", player.getAccountName());
		html.replace("%ip%", (player.getClient().isDetached()) ? "Disconnected" : player.getClient().getConnection().getInetAddress().getHostAddress());
		html.replace("%ai%", player.getAI().getDesire().getIntention().name());
		activeChar.sendPacket(html);
	}
	
	private static void setTargetKarma(Player activeChar, int newKarma)
	{
		WorldObject target = activeChar.getTarget();
		if (!(target instanceof Player))
			return;
		
		Player player = (Player) target;
		
		if (newKarma >= 0)
		{
			int oldKarma = player.getKarma();
			
			player.setKarma(newKarma);
			activeChar.sendMessage("You changed " + player.getName() + "'s karma from " + oldKarma + " to " + newKarma + ".");
		}
		else
			activeChar.sendMessage("The karma value must be greater or equal to 0.");
	}
	
	private static void editCharacter(Player activeChar)
	{
		WorldObject target = activeChar.getTarget();
		if (!(target instanceof Player))
			return;
		
		gatherCharacterInfo(activeChar, (Player) target, "charedit.htm");
	}
	
	/**
	 * Find the character based on his name, and send back the result to activeChar.
	 * @param activeChar The player to send back results.
	 * @param characterToFind The name to search.
	 */
	private static void findCharacter(Player activeChar, String characterToFind)
	{
		int charactersFound = 0;
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/charfind.htm");
		
		final StringBuilder sb = new StringBuilder();
		
		// First use of sb, add player info into new Table row
		for (Player player : World.getInstance().getPlayers())
		{
			String name = player.getName();
			if (name.toLowerCase().contains(characterToFind.toLowerCase()))
			{
				charactersFound++;
				StringUtil.append(sb, "<tr><td width=80><a action=\"bypass -h admin_character_info ", name, "\">", name, "</a></td><td width=110>", player.getTemplate().getClassName(), "</td><td width=40>", player.getLevel(), "</td></tr>");
			}
			
			if (charactersFound > 20)
				break;
		}
		html.replace("%results%", sb.toString());
		
		// Cleanup sb.
		sb.setLength(0);
		
		// Second use of sb.
		if (charactersFound == 0)
			sb.append("s. Please try again.");
		else if (charactersFound > 20)
		{
			html.replace("%number%", " more than 20.");
			sb.append("s.<br>Please refine your search to see all of the results.");
		}
		else if (charactersFound == 1)
			sb.append(".");
		else
			sb.append("s.");
		
		html.replace("%number%", charactersFound);
		html.replace("%end%", sb.toString());
		activeChar.sendPacket(html);
	}
	
	/**
	 * @param activeChar
	 * @param IpAdress
	 * @throws IllegalArgumentException
	 */
	private static void findCharactersPerIp(Player activeChar, String IpAdress) throws IllegalArgumentException
	{
		boolean findDisconnected = false;
		
		if (IpAdress.equals("disconnected"))
			findDisconnected = true;
		else
		{
			if (!IpAdress.matches("^(?:(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))$"))
				throw new IllegalArgumentException("Malformed IPv4 number");
		}
		
		int charactersFound = 0;
		String ip = "0.0.0.0";
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/ipfind.htm");
		
		final StringBuilder sb = new StringBuilder(1000);
		for (Player player : World.getInstance().getPlayers())
		{
			GameClient client = player.getClient();
			if (client.isDetached())
			{
				if (!findDisconnected)
					continue;
			}
			else
			{
				if (findDisconnected)
					continue;
				
				ip = client.getConnection().getInetAddress().getHostAddress();
				if (!ip.equals(IpAdress))
					continue;
			}
			
			String name = player.getName();
			charactersFound++;
			StringUtil.append(sb, "<tr><td width=80><a action=\"bypass -h admin_character_info ", name, "\">", name, "</a></td><td width=110>", player.getTemplate().getClassName(), "</td><td width=40>", player.getLevel(), "</td></tr>");
			
			if (charactersFound > 20)
				break;
		}
		html.replace("%results%", sb.toString());
		
		final String replyMSG2;
		
		if (charactersFound == 0)
			replyMSG2 = ".";
		else if (charactersFound > 20)
		{
			html.replace("%number%", " more than 20.");
			replyMSG2 = "s.";
		}
		else if (charactersFound == 1)
			replyMSG2 = ".";
		else
			replyMSG2 = "s.";
		
		html.replace("%ip%", IpAdress);
		html.replace("%number%", charactersFound);
		html.replace("%end%", replyMSG2);
		activeChar.sendPacket(html);
	}
	
	/**
	 * Returns accountinfo.htm with
	 * @param activeChar
	 * @param characterName
	 */
	private static void findCharactersPerAccount(Player activeChar, String characterName)
	{
		final Player player = World.getInstance().getPlayer(characterName);
		if (player == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			return;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/accountinfo.htm");
		html.replace("%characters%", String.join("<br1>", player.getAccountChars().values()));
		html.replace("%account%", player.getAccountName());
		html.replace("%player%", characterName);
		activeChar.sendPacket(html);
	}
	
	/**
	 * @param activeChar
	 * @param multibox
	 */
	private static void findDualbox(Player activeChar, int multibox)
	{
		Map<String, List<Player>> ipMap = new HashMap<>();
		
		String ip = "0.0.0.0";
		
		final Map<String, Integer> dualboxIPs = new HashMap<>();
		
		for (Player player : World.getInstance().getPlayers())
		{
			GameClient client = player.getClient();
			if (client == null || client.isDetached())
				continue;
			
			ip = client.getConnection().getInetAddress().getHostAddress();
			if (ipMap.get(ip) == null)
				ipMap.put(ip, new ArrayList<Player>());
			
			ipMap.get(ip).add(player);
			
			if (ipMap.get(ip).size() >= multibox)
			{
				Integer count = dualboxIPs.get(ip);
				if (count == null)
					dualboxIPs.put(ip, multibox);
				else
					dualboxIPs.put(ip, count++);
			}
		}
		
		List<String> keys = new ArrayList<>(dualboxIPs.keySet());
		Collections.sort(keys, new Comparator<String>()
		{
			@Override
			public int compare(String left, String right)
			{
				return dualboxIPs.get(left).compareTo(dualboxIPs.get(right));
			}
		});
		Collections.reverse(keys);
		
		final StringBuilder sb = new StringBuilder();
		for (String dualboxIP : keys)
			StringUtil.append(sb, "<a action=\"bypass -h admin_find_ip ", dualboxIP, "\">", dualboxIP, " (", dualboxIPs.get(dualboxIP), ")</a><br1>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/dualbox.htm");
		html.replace("%multibox%", multibox);
		html.replace("%results%", sb.toString());
		html.replace("%strict%", "");
		activeChar.sendPacket(html);
	}
	
	private static void gatherSummonInfo(Summon target, Player activeChar)
	{
		final String name = target.getName();
		final String owner = target.getActingPlayer().getName();
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/petinfo.htm");
		html.replace("%name%", (name == null) ? "N/A" : name);
		html.replace("%level%", target.getLevel());
		html.replace("%exp%", target.getStat().getExp());
		html.replace("%owner%", " <a action=\"bypass -h admin_character_info " + owner + "\">" + owner + "</a>");
		html.replace("%class%", target.getClass().getSimpleName());
		html.replace("%ai%", (target.hasAI()) ? target.getAI().getDesire().getIntention().name() : "NULL");
		html.replace("%hp%", (int) target.getStatus().getCurrentHp() + "/" + target.getStat().getMaxHp());
		html.replace("%mp%", (int) target.getStatus().getCurrentMp() + "/" + target.getStat().getMaxMp());
		html.replace("%karma%", target.getKarma());
		html.replace("%undead%", (target.isUndead()) ? "yes" : "no");
		
		if (target instanceof Pet)
		{
			final Pet pet = ((Pet) target);
			
			html.replace("%inv%", " <a action=\"bypass admin_show_pet_inv " + target.getActingPlayer().getObjectId() + "\">view</a>");
			html.replace("%food%", pet.getCurrentFed() + "/" + pet.getPetData().getMaxMeal());
			html.replace("%load%", pet.getInventory().getTotalWeight() + "/" + pet.getMaxLoad());
		}
		else
		{
			html.replace("%inv%", "none");
			html.replace("%food%", "N/A");
			html.replace("%load%", "N/A");
		}
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
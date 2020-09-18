package dev.l2j.tesla.gameserver.data.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.entity.Castle;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.itemcontainer.Inventory;
import dev.l2j.tesla.gameserver.model.olympiad.Olympiad;
import dev.l2j.tesla.gameserver.model.pledge.Clan;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.lang.StringUtil;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.data.sql.ClanTable;
import dev.l2j.tesla.gameserver.data.sql.PlayerInfoTable;
import dev.l2j.tesla.gameserver.data.xml.NpcData;
import dev.l2j.tesla.gameserver.data.xml.PlayerData;

public class HeroManager
{
	private static final CLogger LOGGER = new CLogger(HeroManager.class.getName());
	
	private static final String LOAD_HEROES = "SELECT heroes.char_id, characters.char_name, heroes.class_id, heroes.count, heroes.played, heroes.active FROM heroes, characters WHERE characters.obj_Id = heroes.char_id AND heroes.played = 1";
	private static final String LOAD_ALL_HEROES = "SELECT heroes.char_id, characters.char_name, heroes.class_id, heroes.count, heroes.played, heroes.active FROM heroes, characters WHERE characters.obj_Id = heroes.char_id";
	private static final String RESET_PLAYED = "UPDATE heroes SET played = 0";
	private static final String INSERT_HERO = "INSERT INTO heroes (char_id, class_id, count, played, active) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE count=VALUES(count),played=VALUES(played),active=VALUES(active)";
	private static final String LOAD_CLAN_DATA = "SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid WHERE characters.obj_Id = ?";
	
	private static final String LOAD_MESSAGE = "SELECT message FROM heroes WHERE char_id=?";
	private static final String LOAD_DIARY = "SELECT * FROM  heroes_diary WHERE char_id=? ORDER BY time ASC";
	private static final String LOAD_FIGHTS = "SELECT * FROM olympiad_fights WHERE (charOneId=? OR charTwoId=?) AND start<? ORDER BY start ASC";
	
	private static final String UPDATE_DIARY = "INSERT INTO heroes_diary (char_id, time, action, param) values(?,?,?,?)";
	private static final String UPDATE_MESSAGE = "UPDATE heroes SET message=? WHERE char_id=?";
	private static final String DELETE_ITEMS = "DELETE FROM items WHERE item_id IN (6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621) AND owner_id NOT IN (SELECT obj_Id FROM characters WHERE accesslevel > 0)";
	
	public static final String COUNT = "count";
	public static final String PLAYED = "played";
	public static final String CLAN_NAME = "clan_name";
	public static final String CLAN_CREST = "clan_crest";
	public static final String ALLY_NAME = "ally_name";
	public static final String ALLY_CREST = "ally_crest";
	public static final String ACTIVE = "active";
	
	public static final int ACTION_RAID_KILLED = 1;
	public static final int ACTION_HERO_GAINED = 2;
	public static final int ACTION_CASTLE_TAKEN = 3;
	
	private final Map<Integer, StatsSet> _heroes = new HashMap<>();
	private final Map<Integer, StatsSet> _completeHeroes = new HashMap<>();
	
	private final Map<Integer, StatsSet> _heroCounts = new HashMap<>();
	private final Map<Integer, List<StatsSet>> _heroFights = new HashMap<>();
	private final List<StatsSet> _fights = new ArrayList<>();
	
	private final Map<Integer, List<StatsSet>> _heroDiaries = new HashMap<>();
	private final Map<Integer, String> _heroMessages = new HashMap<>();
	private final List<StatsSet> _diary = new ArrayList<>();
	
	protected HeroManager()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps2 = con.prepareStatement(LOAD_CLAN_DATA))
		{
			try (PreparedStatement ps = con.prepareStatement(LOAD_HEROES);
				ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final int objectId = rs.getInt(Olympiad.CHAR_ID);
					
					final StatsSet hero = new StatsSet();
					hero.set(Olympiad.CHAR_NAME, rs.getString(Olympiad.CHAR_NAME));
					hero.set(Olympiad.CLASS_ID, rs.getInt(Olympiad.CLASS_ID));
					hero.set(COUNT, rs.getInt(COUNT));
					hero.set(PLAYED, rs.getInt(PLAYED));
					hero.set(ACTIVE, rs.getInt(ACTIVE));
					
					loadFights(objectId);
					loadDiary(objectId);
					loadMessage(objectId);
					
					ps2.setInt(1, objectId);
					
					try (ResultSet rs2 = ps2.executeQuery())
					{
						if (rs2.next())
						{
							String clanName = "";
							String allyName = "";
							int clanCrest = 0;
							int allyCrest = 0;
							
							final int clanId = rs2.getInt("clanid");
							if (clanId > 0)
							{
								final Clan clan = ClanTable.getInstance().getClan(clanId);
								if (clan != null)
								{
									clanName = clan.getName();
									clanCrest = clan.getCrestId();
									
									final int allyId = rs2.getInt("allyId");
									if (allyId > 0)
									{
										allyName = clan.getAllyName();
										allyCrest = clan.getAllyCrestId();
									}
								}
							}
							
							hero.set(CLAN_CREST, clanCrest);
							hero.set(CLAN_NAME, clanName);
							hero.set(ALLY_CREST, allyCrest);
							hero.set(ALLY_NAME, allyName);
						}
					}
					ps2.clearParameters();
					
					_heroes.put(objectId, hero);
				}
			}
			
			try (PreparedStatement ps = con.prepareStatement(LOAD_ALL_HEROES);
				ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final int objectId = rs.getInt(Olympiad.CHAR_ID);
					
					final StatsSet hero = new StatsSet();
					hero.set(Olympiad.CHAR_NAME, rs.getString(Olympiad.CHAR_NAME));
					hero.set(Olympiad.CLASS_ID, rs.getInt(Olympiad.CLASS_ID));
					hero.set(COUNT, rs.getInt(COUNT));
					hero.set(PLAYED, rs.getInt(PLAYED));
					hero.set(ACTIVE, rs.getInt(ACTIVE));
					
					ps2.setInt(1, objectId);
					
					try (ResultSet rs2 = ps2.executeQuery())
					{
						if (rs2.next())
						{
							String clanName = "";
							String allyName = "";
							int clanCrest = 0;
							int allyCrest = 0;
							
							final int clanId = rs2.getInt("clanid");
							if (clanId > 0)
							{
								final Clan clan = ClanTable.getInstance().getClan(clanId);
								if (clan != null)
								{
									clanName = clan.getName();
									clanCrest = clan.getCrestId();
									
									final int allyId = rs2.getInt("allyId");
									if (allyId > 0)
									{
										allyName = clan.getAllyName();
										allyCrest = clan.getAllyCrestId();
									}
								}
							}
							
							hero.set(CLAN_CREST, clanCrest);
							hero.set(CLAN_NAME, clanName);
							hero.set(ALLY_CREST, allyCrest);
							hero.set(ALLY_NAME, allyName);
						}
					}
					ps2.clearParameters();
					
					_completeHeroes.put(objectId, hero);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load heroes.", e);
		}
		LOGGER.info("Loaded {} heroes and {} all time heroes.", _heroes.size(), _completeHeroes.size());
	}
	
	private static String calcFightTime(long fightTime)
	{
		String format = String.format("%%0%dd", 2);
		fightTime = fightTime / 1000;
		String seconds = String.format(format, fightTime % 60);
		String minutes = String.format(format, (fightTime % 3600) / 60);
		String time = minutes + ":" + seconds;
		return time;
	}
	
	/**
	 * Restore hero message.
	 * @param objectId : The objectId of the hero.
	 */
	private void loadMessage(int objectId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_MESSAGE))
		{
			ps.setInt(1, objectId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
					_heroMessages.put(objectId, rs.getString("message"));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load hero message for: {}.", e, objectId);
		}
	}
	
	private void loadDiary(int objectId)
	{
		int entries = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_DIARY))
		{
			ps.setInt(1, objectId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final long time = rs.getLong("time");
					final int action = rs.getInt("action");
					final int param = rs.getInt("param");
					
					final StatsSet entry = new StatsSet();
					entry.set("date", new SimpleDateFormat("yyyy-MM-dd HH").format(time));
					
					if (action == ACTION_RAID_KILLED)
					{
						final NpcTemplate template = NpcData.getInstance().getTemplate(param);
						if (template != null)
							entry.set("action", template.getName() + " was defeated");
					}
					else if (action == ACTION_HERO_GAINED)
						entry.set("action", "Gained Hero status");
					else if (action == ACTION_CASTLE_TAKEN)
					{
						final Castle castle = CastleManager.getInstance().getCastleById(param);
						if (castle != null)
							entry.set("action", castle.getName() + " Castle was successfuly taken");
					}
					_diary.add(entry);
					
					entries++;
				}
			}
			
			_heroDiaries.put(objectId, _diary);
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load hero diary for: {}.", e, objectId);
		}
		LOGGER.info("Loaded {} diary entries for hero: {}.", entries, PlayerInfoTable.getInstance().getPlayerName(objectId));
	}
	
	private void loadFights(int charId)
	{
		StatsSet heroCountData = new StatsSet();
		
		Calendar data = Calendar.getInstance();
		data.set(Calendar.DAY_OF_MONTH, 1);
		data.set(Calendar.HOUR_OF_DAY, 0);
		data.set(Calendar.MINUTE, 0);
		data.set(Calendar.MILLISECOND, 0);
		
		long from = data.getTimeInMillis();
		int numberOfFights = 0;
		int victories = 0;
		int losses = 0;
		int draws = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_FIGHTS))
		{
			ps.setInt(1, charId);
			ps.setInt(2, charId);
			ps.setLong(3, from);
			
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					int charOneId = rset.getInt("charOneId");
					int charOneClass = rset.getInt("charOneClass");
					int charTwoId = rset.getInt("charTwoId");
					int charTwoClass = rset.getInt("charTwoClass");
					int winner = rset.getInt("winner");
					long start = rset.getLong("start");
					int time = rset.getInt("time");
					int classed = rset.getInt("classed");
					
					if (charId == charOneId)
					{
						String name = PlayerInfoTable.getInstance().getPlayerName(charTwoId);
						String cls = PlayerData.getInstance().getClassNameById(charTwoClass);
						if (name != null && cls != null)
						{
							StatsSet fight = new StatsSet();
							fight.set("oponent", name);
							fight.set("oponentclass", cls);
							
							fight.set("time", calcFightTime(time));
							fight.set("start", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(start));
							
							fight.set("classed", classed);
							if (winner == 1)
							{
								fight.set("result", "<font color=\"00ff00\">victory</font>");
								victories++;
							}
							else if (winner == 2)
							{
								fight.set("result", "<font color=\"ff0000\">loss</font>");
								losses++;
							}
							else if (winner == 0)
							{
								fight.set("result", "<font color=\"ffff00\">draw</font>");
								draws++;
							}
							
							_fights.add(fight);
							
							numberOfFights++;
						}
					}
					else if (charId == charTwoId)
					{
						String name = PlayerInfoTable.getInstance().getPlayerName(charOneId);
						String cls = PlayerData.getInstance().getClassNameById(charOneClass);
						if (name != null && cls != null)
						{
							StatsSet fight = new StatsSet();
							fight.set("oponent", name);
							fight.set("oponentclass", cls);
							
							fight.set("time", calcFightTime(time));
							fight.set("start", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(start));
							
							fight.set("classed", classed);
							if (winner == 1)
							{
								fight.set("result", "<font color=\"ff0000\">loss</font>");
								losses++;
							}
							else if (winner == 2)
							{
								fight.set("result", "<font color=\"00ff00\">victory</font>");
								victories++;
							}
							else if (winner == 0)
							{
								fight.set("result", "<font color=\"ffff00\">draw</font>");
								draws++;
							}
							
							_fights.add(fight);
							
							numberOfFights++;
						}
					}
				}
			}
			
			heroCountData.set("victory", victories);
			heroCountData.set("draw", draws);
			heroCountData.set("loss", losses);
			
			_heroCounts.put(charId, heroCountData);
			_heroFights.put(charId, _fights);
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load hero fights history for: {}.", e, charId);
		}
		LOGGER.info("Loaded {} fights for: {}.", numberOfFights, PlayerInfoTable.getInstance().getPlayerName(charId));
	}
	
	public Map<Integer, StatsSet> getHeroes()
	{
		return _heroes;
	}
	
	public Map<Integer, StatsSet> getAllHeroes()
	{
		return _completeHeroes;
	}
	
	public int getHeroByClass(int classId)
	{
		if (_heroes.isEmpty())
			return 0;
		
		for (Map.Entry<Integer, StatsSet> hero : _heroes.entrySet())
		{
			if (hero.getValue().getInteger(Olympiad.CLASS_ID) == classId)
				return hero.getKey();
		}
		return 0;
	}
	
	public void resetData()
	{
		_heroDiaries.clear();
		_heroFights.clear();
		_heroCounts.clear();
		_heroMessages.clear();
	}
	
	public void showHeroDiary(Player player, int heroclass, int objectId, int page)
	{
		final List<StatsSet> mainList = _heroDiaries.get(objectId);
		if (mainList == null)
			return;
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/olympiad/herodiary.htm");
		html.replace("%heroname%", PlayerInfoTable.getInstance().getPlayerName(objectId));
		html.replace("%message%", _heroMessages.get(objectId));
		html.disableValidation();
		
		if (!mainList.isEmpty())
		{
			List<StatsSet> list = new ArrayList<>();
			list.addAll(mainList);
			Collections.reverse(list);
			
			boolean color = true;
			int counter = 0;
			int breakat = 0;
			final int perpage = 10;
			
			final StringBuilder sb = new StringBuilder(500);
			for (int i = ((page - 1) * perpage); i < list.size(); i++)
			{
				breakat = i;
				StatsSet _diaryentry = list.get(i);
				StringUtil.append(sb, "<tr><td>", ((color) ? "<table width=270 bgcolor=\"131210\">" : "<table width=270>"), "<tr><td width=270><font color=\"LEVEL\">", _diaryentry.getString("date"), ":xx</font></td></tr><tr><td width=270>", _diaryentry.getString("action"), "</td></tr><tr><td>&nbsp;</td></tr></table></td></tr>");
				color = !color;
				
				counter++;
				if (counter >= perpage)
					break;
			}
			
			if (breakat < (list.size() - 1))
				html.replace("%buttprev%", "<button value=\"Prev\" action=\"bypass _diary?class=" + heroclass + "&page=" + (page + 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			else
				html.replace("%buttprev%", "");
			
			if (page > 1)
				html.replace("%buttnext%", "<button value=\"Next\" action=\"bypass _diary?class=" + heroclass + "&page=" + (page - 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			else
				html.replace("%buttnext%", "");
			
			html.replace("%list%", sb.toString());
		}
		else
		{
			html.replace("%list%", "");
			html.replace("%buttprev%", "");
			html.replace("%buttnext%", "");
		}
		player.sendPacket(html);
	}
	
	public void showHeroFights(Player player, int heroclass, int objectId, int page)
	{
		final List<StatsSet> list = _heroFights.get(objectId);
		if (list == null)
			return;
		
		int win = 0;
		int loss = 0;
		int draw = 0;
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/olympiad/herohistory.htm");
		html.replace("%heroname%", PlayerInfoTable.getInstance().getPlayerName(objectId));
		html.disableValidation();
		
		if (!list.isEmpty())
		{
			if (_heroCounts.containsKey(objectId))
			{
				StatsSet _herocount = _heroCounts.get(objectId);
				win = _herocount.getInteger("victory");
				loss = _herocount.getInteger("loss");
				draw = _herocount.getInteger("draw");
			}
			
			boolean color = true;
			int counter = 0;
			int breakat = 0;
			final int perpage = 20;
			
			final StringBuilder sb = new StringBuilder(500);
			for (int i = ((page - 1) * perpage); i < list.size(); i++)
			{
				breakat = i;
				StatsSet fight = list.get(i);
				StringUtil.append(sb, "<tr><td>", ((color) ? "<table width=270 bgcolor=\"131210\">" : "<table width=270><tr><td width=220><font color=\"LEVEL\">"), fight.getString("start"), "</font>&nbsp;&nbsp;", fight.getString("result"), "</td><td width=50 align=right>", ((fight.getInteger("classed") > 0) ? "<font color=\"FFFF99\">cls</font>" : "<font color=\"999999\">non-cls<font>"), "</td></tr><tr><td width=220>vs ", fight.getString("oponent"), " (", fight.getString("oponentclass"), ")</td><td width=50 align=right>(", fight.getString("time"), ")</td></tr><tr><td colspan=2>&nbsp;</td></tr></table></td></tr>");
				color = !color;
				
				counter++;
				if (counter >= perpage)
					break;
			}
			
			if (breakat < (list.size() - 1))
				html.replace("%buttprev%", "<button value=\"Prev\" action=\"bypass _match?class=" + heroclass + "&page=" + (page + 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			else
				html.replace("%buttprev%", "");
			
			if (page > 1)
				html.replace("%buttnext%", "<button value=\"Next\" action=\"bypass _match?class=" + heroclass + "&page=" + (page - 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			else
				html.replace("%buttnext%", "");
			
			html.replace("%list%", sb.toString());
		}
		else
		{
			html.replace("%list%", "");
			html.replace("%buttprev%", "");
			html.replace("%buttnext%", "");
		}
		
		html.replace("%win%", win);
		html.replace("%draw%", draw);
		html.replace("%loos%", loss);
		
		player.sendPacket(html);
	}
	
	public synchronized void computeNewHeroes(List<StatsSet> newHeroes)
	{
		// Reset heroes played variable.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(RESET_PLAYED))
		{
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't reset heroes.", e);
		}
		
		if (!_heroes.isEmpty())
		{
			for (StatsSet hero : _heroes.values())
			{
				String name = hero.getString(Olympiad.CHAR_NAME);
				
				Player player = World.getInstance().getPlayer(name);
				if (player == null)
					continue;
				
				player.setHero(false);
				
				// Unequip hero items, if found.
				for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
				{
					ItemInstance equippedItem = player.getInventory().getPaperdollItem(i);
					if (equippedItem != null && equippedItem.isHeroItem())
						player.getInventory().unEquipItemInSlot(i);
				}
				
				// Check inventory items.
				for (ItemInstance item : player.getInventory().getAvailableItems(false, true))
				{
					if (!item.isHeroItem())
						continue;
					
					player.destroyItem("Hero", item, null, true);
				}
				
				player.broadcastUserInfo();
			}
		}
		
		if (newHeroes.isEmpty())
		{
			_heroes.clear();
			return;
		}
		
		final Map<Integer, StatsSet> heroes = new HashMap<>();
		
		for (StatsSet hero : newHeroes)
		{
			final int objectId = hero.getInteger(Olympiad.CHAR_ID);
			
			StatsSet set = _completeHeroes.get(objectId);
			if (set != null)
			{
				set.set(COUNT, set.getInteger(COUNT) + 1);
				set.set(PLAYED, 1);
				set.set(ACTIVE, 0);
			}
			else
			{
				set = new StatsSet();
				set.set(Olympiad.CHAR_NAME, hero.getString(Olympiad.CHAR_NAME));
				set.set(Olympiad.CLASS_ID, hero.getInteger(Olympiad.CLASS_ID));
				set.set(COUNT, 1);
				set.set(PLAYED, 1);
				set.set(ACTIVE, 0);
			}
			heroes.put(objectId, set);
		}
		
		// Delete hero items.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_ITEMS))
		{
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete hero items.", e);
		}
		
		_heroes.clear();
		_heroes.putAll(heroes);
		
		heroes.clear();
		
		updateHeroes();
	}
	
	private void updateHeroes()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_HERO))
		{
			for (Map.Entry<Integer, StatsSet> heroEntry : _heroes.entrySet())
			{
				final int heroId = heroEntry.getKey();
				final StatsSet hero = heroEntry.getValue();
				
				ps.setInt(1, heroId);
				ps.setInt(2, hero.getInteger(Olympiad.CLASS_ID));
				ps.setInt(3, hero.getInteger(COUNT));
				ps.setInt(4, hero.getInteger(PLAYED));
				ps.setInt(5, hero.getInteger(ACTIVE));
				ps.addBatch();
				
				if (!_completeHeroes.containsKey(heroId))
				{
					try (PreparedStatement ps2 = con.prepareStatement(LOAD_CLAN_DATA))
					{
						ps2.setInt(1, heroId);
						
						try (ResultSet rs2 = ps2.executeQuery())
						{
							if (rs2.next())
							{
								String clanName = "";
								String allyName = "";
								int clanCrest = 0;
								int allyCrest = 0;
								
								final int clanId = rs2.getInt("clanid");
								if (clanId > 0)
								{
									final Clan clan = ClanTable.getInstance().getClan(clanId);
									if (clan != null)
									{
										clanName = clan.getName();
										clanCrest = clan.getCrestId();
										
										final int allyId = rs2.getInt("allyId");
										if (allyId > 0)
										{
											allyName = clan.getAllyName();
											allyCrest = clan.getAllyCrestId();
										}
									}
								}
								
								hero.set(CLAN_CREST, clanCrest);
								hero.set(CLAN_NAME, clanName);
								hero.set(ALLY_CREST, allyCrest);
								hero.set(ALLY_NAME, allyName);
							}
						}
					}
					
					_heroes.put(heroId, hero);
					_completeHeroes.put(heroId, hero);
				}
			}
			ps.executeBatch();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't update heroes.", e);
		}
	}
	
	public void setHeroGained(int objectId)
	{
		setDiaryData(objectId, ACTION_HERO_GAINED, 0);
	}
	
	public void setRBkilled(int objectId, int npcId)
	{
		setDiaryData(objectId, ACTION_RAID_KILLED, npcId);
		
		final NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
		if (template == null)
			return;
		
		// Get Data
		final List<StatsSet> list = _heroDiaries.get(objectId);
		if (list == null)
			return;
		
		// Clear old data
		_heroDiaries.remove(objectId);
		
		// Prepare new data
		StatsSet entry = new StatsSet();
		entry.set("date", new SimpleDateFormat("yyyy-MM-dd HH").format(System.currentTimeMillis()));
		entry.set("action", template.getName() + " was defeated");
		
		// Add to old list
		list.add(entry);
		
		// Put new list into diary
		_heroDiaries.put(objectId, list);
	}
	
	public void setCastleTaken(int objectId, int castleId)
	{
		setDiaryData(objectId, ACTION_CASTLE_TAKEN, castleId);
		
		final Castle castle = CastleManager.getInstance().getCastleById(castleId);
		if (castle == null)
			return;
		
		// Get Data
		final List<StatsSet> list = _heroDiaries.get(objectId);
		if (list == null)
			return;
		
		// Clear old data
		_heroDiaries.remove(objectId);
		
		// Prepare new data
		final StatsSet entry = new StatsSet();
		entry.set("date", new SimpleDateFormat("yyyy-MM-dd HH").format(System.currentTimeMillis()));
		entry.set("action", castle.getName() + " Castle was successfuly taken");
		
		// Add to old list
		list.add(entry);
		
		// Put new list into diary
		_heroDiaries.put(objectId, list);
	}
	
	public void setDiaryData(int objectId, int action, int param)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_DIARY))
		{
			ps.setInt(1, objectId);
			ps.setLong(2, System.currentTimeMillis());
			ps.setInt(3, action);
			ps.setInt(4, param);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't save diary data for {}.", e, objectId);
		}
	}
	
	/**
	 * Set new hero message for hero
	 * @param player the player instance
	 * @param message String to set
	 */
	public void setHeroMessage(Player player, String message)
	{
		_heroMessages.put(player.getObjectId(), message);
	}
	
	/**
	 * Update hero message in database
	 * @param objectId : The Player objectId.
	 */
	public void saveHeroMessage(int objectId)
	{
		if (!_heroMessages.containsKey(objectId))
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_MESSAGE))
		{
			ps.setString(1, _heroMessages.get(objectId));
			ps.setInt(2, objectId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't save hero message for {}.", e, objectId);
		}
	}
	
	/**
	 * Save all hero messages to DB.
	 */
	public void shutdown()
	{
		for (int charId : _heroMessages.keySet())
			saveHeroMessage(charId);
	}
	
	public boolean isActiveHero(int id)
	{
		final StatsSet entry = _heroes.get(id);
		
		return entry != null && entry.getInteger(ACTIVE) == 1;
	}
	
	public boolean isInactiveHero(int id)
	{
		final StatsSet entry = _heroes.get(id);
		
		return entry != null && entry.getInteger(ACTIVE) == 0;
	}
	
	public void activateHero(Player player)
	{
		final StatsSet hero = _heroes.get(player.getObjectId());
		if (hero == null)
			return;
		
		hero.set(ACTIVE, 1);
		
		player.setHero(true);
		player.broadcastPacket(new SocialAction(player, 16));
		player.broadcastUserInfo();
		
		final Clan clan = player.getClan();
		if (clan != null && clan.getLevel() >= 5)
		{
			clan.addReputationScore(1000);
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan), SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS).addString(hero.getString("char_name")).addNumber(1000));
		}
		
		// Set Gained hero and reload data
		setHeroGained(player.getObjectId());
		loadFights(player.getObjectId());
		loadDiary(player.getObjectId());
		
		_heroMessages.put(player.getObjectId(), "");
		
		updateHeroes();
	}
	
	public static HeroManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HeroManager INSTANCE = new HeroManager();
	}
}
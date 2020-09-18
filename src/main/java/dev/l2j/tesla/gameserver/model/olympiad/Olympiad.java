package dev.l2j.tesla.gameserver.model.olympiad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.clientpackets.Say2;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcSay;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.data.manager.HeroManager;
import dev.l2j.tesla.gameserver.data.manager.ZoneManager;
import dev.l2j.tesla.gameserver.enums.OlympiadState;
import dev.l2j.tesla.gameserver.enums.OlympiadType;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.OlympiadManagerNpc;
import dev.l2j.tesla.gameserver.model.zone.type.OlympiadStadiumZone;

public class Olympiad
{
	protected static final CLogger LOGGER = new CLogger(Olympiad.class.getName());
	
	private final Map<Integer, StatsSet> _nobles = new HashMap<>();
	private final Map<Integer, Integer> _noblesRank = new HashMap<>();
	private final List<StatsSet> _heroesToBe = new ArrayList<>();
	
	public static final String OLYMPIAD_HTML_PATH = "data/html/olympiad/";
	
	private static final String OLYMPIAD_LOAD_DATA = "SELECT current_cycle, period, olympiad_end, validation_end, next_weekly_change FROM olympiad_data WHERE id = 0";
	private static final String OLYMPIAD_SAVE_DATA = "INSERT INTO olympiad_data (id, current_cycle, period, olympiad_end, validation_end, next_weekly_change) VALUES (0,?,?,?,?,?) ON DUPLICATE KEY UPDATE current_cycle=?, period=?, olympiad_end=?, validation_end=?, next_weekly_change=?";
	
	private static final String OLYMPIAD_LOAD_NOBLES = "SELECT olympiad_nobles.char_id, olympiad_nobles.class_id, characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_done, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.char_id";
	private static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles (`char_id`,`class_id`,`olympiad_points`,`competitions_done`,`competitions_won`,`competitions_lost`, `competitions_drawn`) VALUES (?,?,?,?,?,?,?)";
	private static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles SET olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ? WHERE char_id = ?";
	private static final String OLYMPIAD_GET_HEROS = "SELECT olympiad_nobles.char_id, characters.char_name FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.char_id AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " AND olympiad_nobles.competitions_won > 0 ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC";
	private static final String GET_ALL_CLASSIFIED_NOBLESS = "SELECT char_id from olympiad_nobles_eom WHERE competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_points DESC, competitions_done DESC, competitions_won DESC";
	private static final String GET_EACH_CLASS_LEADER = "SELECT characters.char_name from olympiad_nobles_eom, characters WHERE characters.obj_Id = olympiad_nobles_eom.char_id AND olympiad_nobles_eom.class_id = ? AND olympiad_nobles_eom.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC, olympiad_nobles_eom.competitions_won DESC LIMIT 10";
	
	private static final String OLYMPIAD_LOAD_POINTS = "SELECT olympiad_points FROM olympiad_nobles_eom WHERE char_id = ?";
	private static final String OLYMPIAD_DELETE_ALL = "TRUNCATE olympiad_nobles";
	private static final String OLYMPIAD_MONTH_CLEAR = "TRUNCATE olympiad_nobles_eom";
	private static final String OLYMPIAD_MONTH_CREATE = "INSERT INTO olympiad_nobles_eom SELECT char_id, class_id, olympiad_points, competitions_done, competitions_won, competitions_lost, competitions_drawn FROM olympiad_nobles";
	
	public static final String CHAR_ID = "char_id";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String POINTS = "olympiad_points";
	public static final String COMP_DONE = "competitions_done";
	public static final String COMP_WON = "competitions_won";
	public static final String COMP_LOST = "competitions_lost";
	public static final String COMP_DRAWN = "competitions_drawn";
	
	protected long _olympiadEnd;
	protected long _validationEnd;
	
	protected OlympiadState _period;
	protected long _nextWeeklyChange;
	protected int _currentCycle;
	private long _compEnd;
	private Calendar _compStart;
	protected boolean _isInCompPeriod;
	protected boolean _compStarted = false;
	
	protected ScheduledFuture<?> _scheduledCompStart;
	protected ScheduledFuture<?> _scheduledCompEnd;
	protected ScheduledFuture<?> _scheduledOlympiadEnd;
	protected ScheduledFuture<?> _scheduledWeeklyTask;
	protected ScheduledFuture<?> _scheduledValdationTask;
	protected ScheduledFuture<?> _gameManager;
	protected ScheduledFuture<?> _gameAnnouncer;
	
	protected Olympiad()
	{
		load();
		
		if (_period == OlympiadState.COMPETITION)
			init();
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(OLYMPIAD_LOAD_DATA);
             ResultSet rs = ps.executeQuery())
		{
			if (rs.next())
			{
				_currentCycle = rs.getInt("current_cycle");
				_period = Enum.valueOf(OlympiadState.class, rs.getString("period"));
				_olympiadEnd = rs.getLong("olympiad_end");
				_validationEnd = rs.getLong("validation_end");
				_nextWeeklyChange = rs.getLong("next_weekly_change");
			}
			else
			{
				_currentCycle = 1;
				_period = OlympiadState.COMPETITION;
				_olympiadEnd = 0;
				_validationEnd = 0;
				_nextWeeklyChange = 0;
				
				LOGGER.info("Couldn't load Olympiad data, default values are used.");
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load Olympiad data.", e);
		}
		
		switch (_period)
		{
			case COMPETITION:
				if (_olympiadEnd == 0 || _olympiadEnd < Calendar.getInstance().getTimeInMillis())
					setNewOlympiadEnd();
				else
					scheduleWeeklyChange();
				break;
			
			case VALIDATION:
				if (_validationEnd > Calendar.getInstance().getTimeInMillis())
				{
					loadNoblesRank();
					_scheduledValdationTask = ThreadPool.schedule(new ValidationEndTask(), getMillisToValidationEnd());
				}
				else
				{
					_currentCycle++;
					_period = OlympiadState.COMPETITION;
					deleteNobles();
					setNewOlympiadEnd();
				}
				break;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(OLYMPIAD_LOAD_NOBLES);
			ResultSet rset = ps.executeQuery())
		{
			while (rset.next())
			{
				StatsSet statData = new StatsSet();
				statData.set(CLASS_ID, rset.getInt(CLASS_ID));
				statData.set(CHAR_NAME, rset.getString(CHAR_NAME));
				statData.set(POINTS, rset.getInt(POINTS));
				statData.set(COMP_DONE, rset.getInt(COMP_DONE));
				statData.set(COMP_WON, rset.getInt(COMP_WON));
				statData.set(COMP_LOST, rset.getInt(COMP_LOST));
				statData.set(COMP_DRAWN, rset.getInt(COMP_DRAWN));
				statData.set("to_save", false);
				
				addNobleStats(rset.getInt(CHAR_ID), statData);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load noblesse data.", e);
		}
		
		synchronized (this)
		{
			long milliToEnd;
			if (_period == OlympiadState.COMPETITION)
				milliToEnd = getMillisToOlympiadEnd();
			else
				milliToEnd = getMillisToValidationEnd();
			
			LOGGER.info("{} minutes until Olympiad period ends.", Math.round(milliToEnd / 60000));
			
			if (_period == OlympiadState.COMPETITION)
			{
				milliToEnd = getMillisToWeekChange();
				LOGGER.info("Next weekly Olympiad change is in {} minutes.", Math.round(milliToEnd / 60000));
			}
		}
		
		LOGGER.info("Loaded {} nobles.", _nobles.size());
	}
	
	public void loadNoblesRank()
	{
		_noblesRank.clear();
		
		final Map<Integer, Integer> tmpPlace = new HashMap<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(GET_ALL_CLASSIFIED_NOBLESS);
			ResultSet rs = ps.executeQuery())
		{
			int place = 1;
			
			while (rs.next())
				tmpPlace.put(rs.getInt(CHAR_ID), place++);
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load Olympiad ranks.", e);
		}
		
		int rank1 = (int) Math.round(tmpPlace.size() * 0.01);
		int rank2 = (int) Math.round(tmpPlace.size() * 0.10);
		int rank3 = (int) Math.round(tmpPlace.size() * 0.25);
		int rank4 = (int) Math.round(tmpPlace.size() * 0.50);
		
		if (rank1 == 0)
		{
			rank1 = 1;
			rank2++;
			rank3++;
			rank4++;
		}
		
		for (int charId : tmpPlace.keySet())
		{
			if (tmpPlace.get(charId) <= rank1)
				_noblesRank.put(charId, 1);
			else if (tmpPlace.get(charId) <= rank2)
				_noblesRank.put(charId, 2);
			else if (tmpPlace.get(charId) <= rank3)
				_noblesRank.put(charId, 3);
			else if (tmpPlace.get(charId) <= rank4)
				_noblesRank.put(charId, 4);
			else
				_noblesRank.put(charId, 5);
		}
	}
	
	protected void init()
	{
		if (_period == OlympiadState.VALIDATION)
			return;
		
		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, Config.ALT_OLY_START_TIME);
		_compStart.set(Calendar.MINUTE, Config.ALT_OLY_MIN);
		_compStart.set(Calendar.SECOND, 0);
		
		_compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;
		
		if (_scheduledOlympiadEnd != null)
			_scheduledOlympiadEnd.cancel(true);
		
		_scheduledOlympiadEnd = ThreadPool.schedule(new OlympiadEndTask(), getMillisToOlympiadEnd());
		
		updateCompStatus();
	}
	
	protected class OlympiadEndTask implements Runnable
	{
		@Override
		public void run()
		{
			World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_ENDED).addNumber(_currentCycle));
			
			if (_scheduledWeeklyTask != null)
				_scheduledWeeklyTask.cancel(true);
			
			saveNobleData();
			
			_period = OlympiadState.VALIDATION;
			
			sortHeroesToBe();
			HeroManager.getInstance().resetData();
			HeroManager.getInstance().computeNewHeroes(_heroesToBe);
			
			saveOlympiadStatus();
			updateMonthlyData();
			
			Calendar validationEnd = Calendar.getInstance();
			_validationEnd = validationEnd.getTimeInMillis() + Config.ALT_OLY_VPERIOD;
			
			loadNoblesRank();
			_scheduledValdationTask = ThreadPool.schedule(new ValidationEndTask(), getMillisToValidationEnd());
		}
	}
	
	protected class ValidationEndTask implements Runnable
	{
		@Override
		public void run()
		{
			_period = OlympiadState.COMPETITION;
			_currentCycle++;
			
			deleteNobles();
			setNewOlympiadEnd();
			init();
		}
	}
	
	protected int getNobleCount()
	{
		return _nobles.size();
	}
	
	protected StatsSet getNobleStats(int playerId)
	{
		return _nobles.get(playerId);
	}
	
	private void updateCompStatus()
	{
		synchronized (this)
		{
			long milliToStart = getMillisToCompBegin();
			
			double numSecs = (milliToStart / 1000) % 60;
			double countDown = ((milliToStart / 1000) - numSecs) / 60;
			int numMins = (int) Math.floor(countDown % 60);
			countDown = (countDown - numMins) / 60;
			int numHours = (int) Math.floor(countDown % 24);
			int numDays = (int) Math.floor((countDown - numHours) / 24);
			
			LOGGER.info("Olympiad competition period starts in {} days, {} hours and {} mins.", numDays, numHours, numMins);
			LOGGER.info("Olympiad event starts/started @ {}.", _compStart.getTime());
		}
		
		_scheduledCompStart = ThreadPool.schedule(() ->
		{
			if (isOlympiadEnd())
				return;
			
			_isInCompPeriod = true;
			
			World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_STARTED));
			LOGGER.info("Olympiad game started.");
			
			_gameManager = ThreadPool.scheduleAtFixedRate(OlympiadGameManager.getInstance(), 30000, 30000);
			if (Config.ALT_OLY_ANNOUNCE_GAMES)
				_gameAnnouncer = ThreadPool.scheduleAtFixedRate(new OlympiadAnnouncer(), 30000, 500);
			
			long regEnd = getMillisToCompEnd() - 600000;
			if (regEnd > 0)
				ThreadPool.schedule(() -> World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_REGISTRATION_PERIOD_ENDED)), regEnd);
			
			_scheduledCompEnd = ThreadPool.schedule(() ->
			{
				if (isOlympiadEnd())
					return;
				
				_isInCompPeriod = false;
				World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_ENDED));
				LOGGER.info("Olympiad game ended.");
				
				while (OlympiadGameManager.getInstance().isBattleStarted()) // cleared in game manager
				{
					// wait 1 minutes for end of pendings games
					try
					{
						Thread.sleep(60000);
					}
					catch (InterruptedException e)
					{
					}
				}
				
				if (_gameManager != null)
				{
					_gameManager.cancel(false);
					_gameManager = null;
				}
				
				if (_gameAnnouncer != null)
				{
					_gameAnnouncer.cancel(false);
					_gameAnnouncer = null;
				}
				
				saveOlympiadStatus();
				
				init();
			}, getMillisToCompEnd());
		}, getMillisToCompBegin());
	}
	
	private long getMillisToOlympiadEnd()
	{
		return (_olympiadEnd - Calendar.getInstance().getTimeInMillis());
	}
	
	public void manualSelectHeroes()
	{
		if (_scheduledOlympiadEnd != null)
			_scheduledOlympiadEnd.cancel(true);
		
		_scheduledOlympiadEnd = ThreadPool.schedule(new OlympiadEndTask(), 0);
	}
	
	protected long getMillisToValidationEnd()
	{
		if (_validationEnd > Calendar.getInstance().getTimeInMillis())
			return (_validationEnd - Calendar.getInstance().getTimeInMillis());
		
		return 10L;
	}
	
	public boolean isOlympiadEnd()
	{
		return _period == OlympiadState.VALIDATION;
	}
	
	protected void setNewOlympiadEnd()
	{
		World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_STARTED).addNumber(_currentCycle));
		
		Calendar currentTime = Calendar.getInstance();
		currentTime.add(Calendar.MONTH, 1);
		currentTime.set(Calendar.DAY_OF_MONTH, 1);
		currentTime.set(Calendar.AM_PM, Calendar.AM);
		currentTime.set(Calendar.HOUR, 12);
		currentTime.set(Calendar.MINUTE, 0);
		currentTime.set(Calendar.SECOND, 0);
		_olympiadEnd = currentTime.getTimeInMillis();
		
		Calendar nextChange = Calendar.getInstance();
		_nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;
		scheduleWeeklyChange();
	}
	
	public boolean isInCompPeriod()
	{
		return _isInCompPeriod;
	}
	
	private long getMillisToCompBegin()
	{
		if (_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && _compEnd > Calendar.getInstance().getTimeInMillis())
			return 10L;
		
		if (_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
			return (_compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
		
		return setNewCompBegin();
	}
	
	private long setNewCompBegin()
	{
		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, Config.ALT_OLY_START_TIME);
		_compStart.set(Calendar.MINUTE, Config.ALT_OLY_MIN);
		_compStart.set(Calendar.SECOND, 0);
		_compStart.add(Calendar.HOUR_OF_DAY, 24);
		
		_compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;
		
		LOGGER.info("New Olympiad schedule @ {}.", _compStart.getTime());
		
		return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
	}
	
	protected long getMillisToCompEnd()
	{
		return _compEnd - Calendar.getInstance().getTimeInMillis();
	}
	
	private long getMillisToWeekChange()
	{
		if (_nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
			return (_nextWeeklyChange - Calendar.getInstance().getTimeInMillis());
		
		return 10L;
	}
	
	private void scheduleWeeklyChange()
	{
		_scheduledWeeklyTask = ThreadPool.scheduleAtFixedRate(() ->
		{
			addWeeklyPoints();
			LOGGER.info("Added weekly Olympiad points to nobles.");
			
			Calendar nextChange = Calendar.getInstance();
			_nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;
		}, getMillisToWeekChange(), Config.ALT_OLY_WPERIOD);
	}
	
	protected synchronized void addWeeklyPoints()
	{
		if (_period == OlympiadState.VALIDATION)
			return;
		
		int currentPoints;
		for (StatsSet nobleInfo : _nobles.values())
		{
			currentPoints = nobleInfo.getInteger(POINTS);
			currentPoints += Config.ALT_OLY_WEEKLY_POINTS;
			nobleInfo.set(POINTS, currentPoints);
		}
	}
	
	public int getCurrentCycle()
	{
		return _currentCycle;
	}
	
	public boolean playerInStadia(Player player)
	{
		return ZoneManager.getInstance().getZone(player, OlympiadStadiumZone.class) != null;
	}
	
	/**
	 * Save noblesse data to database
	 */
	protected synchronized void saveNobleData()
	{
		if (_nobles == null || _nobles.isEmpty())
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement ps;
			for (Map.Entry<Integer, StatsSet> noble : _nobles.entrySet())
			{
				final StatsSet set = noble.getValue();
				if (set == null)
					continue;
				
				int charId = noble.getKey();
				int classId = set.getInteger(CLASS_ID);
				int points = set.getInteger(POINTS);
				int compDone = set.getInteger(COMP_DONE);
				int compWon = set.getInteger(COMP_WON);
				int compLost = set.getInteger(COMP_LOST);
				int compDrawn = set.getInteger(COMP_DRAWN);
				boolean toSave = set.getBool("to_save");
				
				if (toSave)
				{
					ps = con.prepareStatement(OLYMPIAD_SAVE_NOBLES);
					ps.setInt(1, charId);
					ps.setInt(2, classId);
					ps.setInt(3, points);
					ps.setInt(4, compDone);
					ps.setInt(5, compWon);
					ps.setInt(6, compLost);
					ps.setInt(7, compDrawn);
					
					set.set("to_save", false);
				}
				else
				{
					ps = con.prepareStatement(OLYMPIAD_UPDATE_NOBLES);
					ps.setInt(1, points);
					ps.setInt(2, compDone);
					ps.setInt(3, compWon);
					ps.setInt(4, compLost);
					ps.setInt(5, compDrawn);
					ps.setInt(6, charId);
				}
				ps.execute();
				ps.close();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't save Olympiad nobles data.", e);
		}
	}
	
	/**
	 * Save current olympiad status and update noblesse table in database
	 */
	public void saveOlympiadStatus()
	{
		saveNobleData();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(OLYMPIAD_SAVE_DATA))
		{
			ps.setInt(1, _currentCycle);
			ps.setString(2, _period.toString());
			ps.setLong(3, _olympiadEnd);
			ps.setLong(4, _validationEnd);
			ps.setLong(5, _nextWeeklyChange);
			ps.setInt(6, _currentCycle);
			ps.setString(7, _period.toString());
			ps.setLong(8, _olympiadEnd);
			ps.setLong(9, _validationEnd);
			ps.setLong(10, _nextWeeklyChange);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't save Olympiad status.", e);
		}
	}
	
	protected void updateMonthlyData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(OLYMPIAD_MONTH_CLEAR);
			PreparedStatement ps2 = con.prepareStatement(OLYMPIAD_MONTH_CREATE))
		{
			ps.execute();
			ps2.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't update monthly Olympiad nobles.", e);
		}
	}
	
	protected void sortHeroesToBe()
	{
		_heroesToBe.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(OLYMPIAD_GET_HEROS))
		{
			for (ClassId id : ClassId.VALUES)
			{
				if (id.level() != 3)
					continue;
				
				ps.setInt(1, id.getId());
				
				try (ResultSet rs = ps.executeQuery())
				{
					if (rs.next())
					{
						final StatsSet hero = new StatsSet();
						hero.set(CLASS_ID, id.getId());
						hero.set(CHAR_ID, rs.getInt(CHAR_ID));
						hero.set(CHAR_NAME, rs.getString(CHAR_NAME));
						
						_heroesToBe.add(hero);
					}
					ps.clearParameters();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load future Olympiad heroes.", e);
		}
	}
	
	public List<String> getClassLeaderBoard(int classId)
	{
		final List<String> names = new ArrayList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(GET_EACH_CLASS_LEADER))
		{
			ps.setInt(1, classId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
					names.add(rs.getString(CHAR_NAME));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load Olympiad leaders.", e);
		}
		return names;
	}
	
	public int getNoblessePasses(Player player, boolean clear)
	{
		if (player == null || _period != OlympiadState.VALIDATION || _noblesRank.isEmpty())
			return 0;
		
		final int objId = player.getObjectId();
		if (!_noblesRank.containsKey(objId))
			return 0;
		
		final StatsSet noble = _nobles.get(objId);
		if ((noble == null) || (noble.getInteger(POINTS) == 0))
			return 0;
		
		final int rank = _noblesRank.get(objId);
		int points = (player.isHero() || HeroManager.getInstance().isInactiveHero(player.getObjectId())) ? Config.ALT_OLY_HERO_POINTS : 0;
		switch (rank)
		{
			case 1:
				points += Config.ALT_OLY_RANK1_POINTS;
				break;
			case 2:
				points += Config.ALT_OLY_RANK2_POINTS;
				break;
			case 3:
				points += Config.ALT_OLY_RANK3_POINTS;
				break;
			case 4:
				points += Config.ALT_OLY_RANK4_POINTS;
				break;
			default:
				points += Config.ALT_OLY_RANK5_POINTS;
		}
		
		if (clear)
			noble.set(POINTS, 0);
		
		points *= Config.ALT_OLY_GP_PER_POINT;
		return points;
	}
	
	public int getNoblePoints(int objId)
	{
		if (_nobles == null || !_nobles.containsKey(objId))
			return 0;
		
		return _nobles.get(objId).getInteger(POINTS);
	}
	
	public int getLastNobleOlympiadPoints(int objId)
	{
		int result = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(OLYMPIAD_LOAD_POINTS))
		{
			ps.setInt(1, objId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.first())
					result = rs.getInt(1);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load last Olympiad points.", e);
		}
		return result;
	}
	
	public int getCompetitionDone(int objId)
	{
		if (_nobles == null || !_nobles.containsKey(objId))
			return 0;
		
		return _nobles.get(objId).getInteger(COMP_DONE);
	}
	
	public int getCompetitionWon(int objId)
	{
		if (_nobles == null || !_nobles.containsKey(objId))
			return 0;
		
		return _nobles.get(objId).getInteger(COMP_WON);
	}
	
	public int getCompetitionLost(int objId)
	{
		if (_nobles == null || !_nobles.containsKey(objId))
			return 0;
		
		return _nobles.get(objId).getInteger(COMP_LOST);
	}
	
	protected void deleteNobles()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(OLYMPIAD_DELETE_ALL))
		{
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete Olympiad nobles.", e);
		}
		_nobles.clear();
	}
	
	/**
	 * @param charId the noble object Id.
	 * @param data the stats set data to add.
	 * @return the old stats set if the noble is already present, null otherwise.
	 */
	protected StatsSet addNobleStats(int charId, StatsSet data)
	{
		return _nobles.put(charId, data);
	}
	
	private final class OlympiadAnnouncer implements Runnable
	{
		private final OlympiadGameTask[] _tasks;
		
		public OlympiadAnnouncer()
		{
			_tasks = OlympiadGameManager.getInstance().getOlympiadTasks();
		}
		
		@Override
		public void run()
		{
			for (OlympiadGameTask task : _tasks)
			{
				if (!task.needAnnounce())
					continue;
				
				final AbstractOlympiadGame game = task.getGame();
				if (game == null)
					continue;
				
				String announcement;
				if (game.getType() == OlympiadType.NON_CLASSED)
					announcement = "Olympiad class-free individual match is going to begin in Arena " + (game.getStadiumId() + 1) + " in a moment.";
				else
					announcement = "Olympiad class individual match is going to begin in Arena " + (game.getStadiumId() + 1) + " in a moment.";
				
				for (OlympiadManagerNpc manager : OlympiadManagerNpc.getInstances())
					manager.broadcastPacket(new NpcSay(manager.getObjectId(), Say2.SHOUT, manager.getNpcId(), announcement));
			}
		}
	}
	
	public static Olympiad getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Olympiad INSTANCE = new Olympiad();
	}
}
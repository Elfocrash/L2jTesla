package dev.l2j.tesla.gameserver.scripting;

import java.util.Calendar;

import dev.l2j.tesla.gameserver.enums.ScheduleType;

public abstract class ScheduledQuest extends Quest
{
	private ScheduleType _type;
	private Calendar _start;
	private Calendar _end;
	private boolean _started;
	
	public ScheduledQuest(int questId, String descr)
	{
		super(questId, descr);
	}
	
	/**
	 * Return true, when a {@link ScheduledQuest} is started.
	 * @return boolean : True, when started.
	 */
	public final boolean isStarted()
	{
		return _started;
	}
	
	/**
	 * Set up schedule system for the script. Returns true, when successfully done.
	 * @param type : Type of the schedule.
	 * @param start : Start information.
	 * @param end : End information.
	 * @return boolean : True, when successfully loaded schedule system.
	 */
	public final boolean setSchedule(String type, String start, String end)
	{
		try
		{
			_type = Enum.valueOf(ScheduleType.class, type);
			_start = parseTimeStamp(start);
			_end = parseTimeStamp(end);
			_started = false;
			
			final long st = _start.getTimeInMillis();
			final long now = System.currentTimeMillis();
			if (_end == null || _end.getTimeInMillis() == st)
			{
				// start and end events are at same time, consider as one-event script
				_end = null;
				
				// schedule next start
				if (st < now)
					_start.add(_type.getPeriod(), 1);
			}
			else
			{
				// normal schedule, both events are in same period
				final long en = _end.getTimeInMillis();
				if (st < en)
				{
					// last schedule had passed, schedule next start
					if (en < now)
						_start.add(_type.getPeriod(), 1);
					// last schedule is running, start script
					else if (st < now)
						_started = true;
					// last schedule has not started yet, shift end by 1 period backwards (is updated in notifyAndSchedule() when starting schedule)
					else
						_end.add(_type.getPeriod(), -1);
				}
				// reverse schedule, each event is in different period (e.g. different day for DAILY - start = 23:00, end = 01:00)
				else
				{
					// last schedule is running, schedule next end and start script
					if (st < now)
					{
						_end.add(_type.getPeriod(), 1);
						_started = true;
					}
					// last schedule is running, shift start by 1 period backwards (is updated in notifyAndSchedule() when starting schedule) and start script
					else if (now < en)
					{
						_start.add(_type.getPeriod(), -1);
						_started = true;
					}
					// last schedule has not started yet, do nothing
				}
			}
			
			// initialize script and return
			return init();
		}
		catch (Exception e)
		{
			LOGGER.error("Error loading schedule data for {}.", e, toString());
			
			_type = null;
			_start = null;
			_end = null;
			_started = false;
			return false;
		}
	}
	
	private final Calendar parseTimeStamp(String value) throws Exception
	{
		if (value == null)
			return null;
		
		final Calendar calendar = Calendar.getInstance();
		String[] timeStamp;
		
		switch (_type)
		{
			case HOURLY:
				// HOURLY, "20:10", "50:00"
				timeStamp = value.split(":");
				calendar.set(Calendar.MINUTE, Integer.valueOf(timeStamp[0]));
				calendar.set(Calendar.SECOND, Integer.valueOf(timeStamp[1]));
				calendar.set(Calendar.MILLISECOND, 0);
				return calendar;
			
			case DAILY:
				// DAILY, "16:20:10", "17:20:00"
				timeStamp = value.split(":");
				break;
			
			case WEEKLY:
				// WEEKLY, "MON 6:20:10", "FRI 17:20:00"
				String[] params = value.split(" ");
				timeStamp = params[1].split(":");
				calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek(params[0]));
				break;
			
			case MONTHLY_DAY:
				// MONTHLY_DAY, "1 6:20:10", "2 17:20:00"
				params = value.split(" ");
				timeStamp = params[1].split(":");
				calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(params[0]));
				break;
			
			case MONTHLY_WEEK:
				// MONTHLY_WEEK, "MON-1 6:20:10", "FRI-2 17:20:00"
				params = value.split(" ");
				String[] date = params[0].split("-");
				timeStamp = params[1].split(":");
				calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek(date[0]));
				calendar.set(Calendar.WEEK_OF_MONTH, Integer.valueOf(date[1]));
				break;
			
			case YEARLY_DAY:
				// YEARLY_DAY, "23-02 6:20:10", "25-03 17:20:00"
				params = value.split(" ");
				date = params[0].split("-");
				timeStamp = params[1].split(":");
				calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(date[0]));
				calendar.set(Calendar.MONTH, Integer.valueOf(date[1]) - 1);
				break;
			
			case YEARLY_WEEK:
				// YEARLY_WEEK, "MON-1 6:20:10", "FRI-2 17:20:00"
				params = value.split(" ");
				date = params[0].split("-");
				timeStamp = params[1].split(":");
				calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek(date[0]));
				calendar.set(Calendar.WEEK_OF_YEAR, Integer.valueOf(date[1]));
				break;
			
			default:
				return null;
		}
		
		// set hour, minute and second
		calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeStamp[0]));
		calendar.set(Calendar.MINUTE, Integer.valueOf(timeStamp[1]));
		calendar.set(Calendar.SECOND, Integer.valueOf(timeStamp[2]));
		calendar.set(Calendar.MILLISECOND, 0);
		
		return calendar;
	}
	
	/**
	 * Returns time of next action of the script.
	 * @return long : Time in milliseconds.
	 */
	public final long getTimeNext()
	{
		if (_type == null)
			return 0;
		
		return _started ? _end.getTimeInMillis() : _start.getTimeInMillis();
	}
	
	/**
	 * Notify and schedule next action of the script.
	 */
	public final void notifyAndSchedule()
	{
		if (_type == null)
			return;
		
		// notify one-action script
		if (_end == null)
		{
			// notify start
			try
			{
				onStart();
			}
			catch (Exception e)
			{
				LOGGER.error("Error starting {}.", e, toString());
			}
			
			// schedule next start
			_start.add(_type.getPeriod(), 1);
			print(_start);
			return;
		}
		
		// notify two-action script
		if (_started)
		{
			// notify end
			try
			{
				onEnd();
				_started = false;
			}
			catch (Exception e)
			{
				LOGGER.error("Error ending {}.", e, toString());
			}
			
			// schedule start
			_start.add(_type.getPeriod(), 1);
			print(_start);
		}
		else
		{
			// notify start
			try
			{
				onStart();
				_started = true;
			}
			catch (Exception e)
			{
				LOGGER.error("Error starting {}.", e, toString());
			}
			
			// schedule end
			_end.add(_type.getPeriod(), 1);
			print(_end);
		}
	}
	
	/**
	 * Initializes a script and returns information about script to be scheduled or not. Set internal values, parameters, etc...
	 * @return boolean : True, when script was initialized and can be scheduled.
	 */
	protected boolean init()
	{
		// the script was initialized as started, run start event
		if (_started)
			onStart();
		
		return true;
	}
	
	/**
	 * Starts a script. Handles spawns, announcements, loads variables, etc...
	 */
	protected abstract void onStart();
	
	/**
	 * Ends a script. Handles spawns, announcements, saves variables, etc...
	 */
	protected abstract void onEnd();
	
	/**
	 * Convert text representation of day {@link Calendar} day.
	 * @param day : String representation of day.
	 * @return int : {@link Calendar} representation of day.
	 * @throws Exception : Throws {@link Exception}, when can't convert day.
	 */
	private static final int getDayOfWeek(String day) throws Exception
	{
		if (day.equals("MON"))
			return Calendar.MONDAY;
		else if (day.equals("TUE"))
			return Calendar.TUESDAY;
		else if (day.equals("WED"))
			return Calendar.WEDNESDAY;
		else if (day.equals("THU"))
			return Calendar.THURSDAY;
		else if (day.equals("FRI"))
			return Calendar.FRIDAY;
		else if (day.equals("SAT"))
			return Calendar.SATURDAY;
		else if (day.equals("SUN"))
			return Calendar.SUNDAY;
		else
			throw new Exception();
	}
	
	private final void print(Calendar c)
	{
		LOGGER.debug("{}: {} = {}.", toString(), ((c == _start) ? "Next start" : "Next end"), String.format("%d.%d.%d %d:%02d:%02d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND)));
	}
}
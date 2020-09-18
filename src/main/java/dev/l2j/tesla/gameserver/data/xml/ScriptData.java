package dev.l2j.tesla.gameserver.data.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.ScheduledQuest;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.data.xml.IXmlReader;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

/**
 * This class loads and stores {@link Quest}s - being regular quests, AI scripts or scheduled scripts.
 */
public final class ScriptData implements IXmlReader, Runnable
{
	public static final int PERIOD = 5 * 60 * 1000; // 5 minutes
	
	private final List<Quest> _quests = new ArrayList<>();
	private final List<ScheduledQuest> _scheduled = new LinkedList<>();
	
	public ScriptData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/scripts.xml");
		LOGGER.info("Loaded {} regular scripts and {} scheduled scripts.", _quests.size(), _scheduled.size());
		
		ThreadPool.scheduleAtFixedRate(this, 0, PERIOD);
	}
	
	@Override
	public void parseDocument(Document doc, Path p)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "script", scriptNode ->
		{
			final NamedNodeMap params = scriptNode.getAttributes();
			final String path = parseString(params, "path");
			if (path == null)
			{
				LOGGER.warn("One of the script path isn't defined.");
				return;
			}
			
			try
			{
				// Create the script.
				Quest instance = (Quest) Class.forName("dev.l2j.tesla.gameserver.scripting." + path).getDeclaredConstructor().newInstance();
				
				// Add quest, script, AI or any other custom type of script.
				_quests.add(instance);
				
				// The script has been identified as a scheduled script, make proper checks and schedule the launch.
				if (instance instanceof ScheduledQuest)
				{
					// Get schedule parameter, when not exist, script is not scheduled.
					final String type = parseString(params, "schedule");
					if (type == null)
						return;
					
					// Get mandatory start parameter, when not exist, script is not scheduled.
					final String start = parseString(params, "start");
					if (start == null)
					{
						LOGGER.warn("Missing 'start' parameter for scheduled script '{}'.", path);
						return;
					}
					
					// Get optional end parameter, when not exist, script is one-event type.
					final String end = parseString(params, "end");
					
					// Schedule script, when successful, register it.
					if (((ScheduledQuest) instance).setSchedule(type, start, end))
						_scheduled.add(((ScheduledQuest) instance));
				}
			}
			catch (Exception e)
			{
				LOGGER.error("Script '{}' is missing.", e, path);
			}
		}));
	}
	
	@Override
	public void run()
	{
		// For each PERIOD.
		final long next = System.currentTimeMillis() + PERIOD;
		
		// Check all scheduled scripts.
		for (ScheduledQuest script : _scheduled)
		{
			// When next action triggers in closest period, schedule the script action.
			final long eta = next - script.getTimeNext();
			if (eta > 0)
				ThreadPool.schedule(new Scheduler(script), PERIOD - eta);
		}
	}
	
	/**
	 * Returns the {@link Quest} by given quest name.
	 * @param questName : The name of the quest.
	 * @return Quest : Quest to be returned, null if quest does not exist.
	 */
	public final Quest getQuest(String questName)
	{
		return _quests.stream().filter(q -> q.getName().equalsIgnoreCase(questName)).findFirst().orElse(null);
	}
	
	/**
	 * Returns the {@link Quest} by given quest id.
	 * @param questId : The id of the quest.
	 * @return Quest : Quest to be returned, null if quest does not exist.
	 */
	public final Quest getQuest(int questId)
	{
		return _quests.stream().filter(q -> q.getQuestId() == questId).findFirst().orElse(null);
	}
	
	/**
	 * @return the {@link List} of {@link Quest}s.
	 */
	public final List<Quest> getQuests()
	{
		return _quests;
	}
	
	private final class Scheduler implements Runnable
	{
		private final ScheduledQuest _script;
		
		protected Scheduler(ScheduledQuest script)
		{
			_script = script;
		}
		
		@Override
		public void run()
		{
			// Notify script.
			_script.notifyAndSchedule();
			
			// In case the next action is triggered before the resolution, schedule the the action again.
			final long eta = System.currentTimeMillis() + PERIOD - _script.getTimeNext();
			if (eta > 0)
				ThreadPool.schedule(this, PERIOD - eta);
		}
	}
	
	public static ScriptData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ScriptData INSTANCE = new ScriptData();
	}
}
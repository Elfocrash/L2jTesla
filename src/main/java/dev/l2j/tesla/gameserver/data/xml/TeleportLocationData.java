package dev.l2j.tesla.gameserver.data.xml;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.model.location.TeleportLocation;
import dev.l2j.tesla.commons.data.xml.IXmlReader;
import dev.l2j.tesla.commons.util.StatsSet;

import org.w3c.dom.Document;

/**
 * This class loads and stores {@link TeleportLocation}s.
 */
public class TeleportLocationData implements IXmlReader
{
	private final Map<Integer, TeleportLocation> _teleports = new HashMap<>();
	
	protected TeleportLocationData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/teleportLocations.xml");
		LOGGER.info("Loaded {} teleport locations.", _teleports.size());
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "teleport", teleportNode ->
		{
			final StatsSet set = parseAttributes(teleportNode);
			_teleports.put(set.getInteger("id"), new TeleportLocation(set));
		}));
	}
	
	public void reload()
	{
		_teleports.clear();
		
		load();
	}
	
	public TeleportLocation getTeleportLocation(int id)
	{
		return _teleports.get(id);
	}

	public Collection<TeleportLocation> getAllTeleportLocations() {
		return _teleports.values(); 
	}
	
	public static TeleportLocationData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TeleportLocationData INSTANCE = new TeleportLocationData();
	}
}
package dev.l2j.tesla.gameserver.data.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.l2j.tesla.gameserver.model.location.WalkerLocation;
import dev.l2j.tesla.commons.data.xml.IXmlReader;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

/**
 * This class loads and stores routes for Walker NPCs, under a List of {@link WalkerLocation} ; the key being the npcId.
 */
public class WalkerRouteData implements IXmlReader
{
	private final Map<Integer, List<WalkerLocation>> _routes = new HashMap<>();
	
	protected WalkerRouteData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/walkerRoutes.xml");
		LOGGER.info("Loaded {} Walker routes.", _routes.size());
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "route", routeNode ->
		{
			final NamedNodeMap attrs = routeNode.getAttributes();
			final List<WalkerLocation> list = new ArrayList<>();
			final int npcId = parseInteger(attrs, "npcId");
			final boolean run = parseBoolean(attrs, "run");
			forEach(routeNode, "node", nodeNode -> list.add(new WalkerLocation(parseAttributes(nodeNode), run)));
			_routes.put(npcId, list);
		}));
	}
	
	public void reload()
	{
		_routes.clear();
		
		load();
	}
	
	public List<WalkerLocation> getWalkerRoute(int npcId)
	{
		return _routes.get(npcId);
	}
	
	public static WalkerRouteData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final WalkerRouteData INSTANCE = new WalkerRouteData();
	}
}
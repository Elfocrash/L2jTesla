package dev.l2j.tesla.gameserver.data.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.l2j.tesla.gameserver.model.item.DropCategory;
import dev.l2j.tesla.gameserver.model.item.DropData;
import dev.l2j.tesla.commons.data.xml.IXmlReader;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

/**
 * This class loads herbs drop rules.<br>
 * TODO parse L2OFF GF (since IL doesn't exist) and introduce the additional droplist concept directly on npc data XMLs.
 */
public class HerbDropData implements IXmlReader
{
	private final Map<Integer, List<DropCategory>> _herbGroups = new HashMap<>();
	
	protected HerbDropData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/herbDrops.xml");
		LOGGER.info("Loaded {} herbs groups.", _herbGroups.size());
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "group", groupNode ->
		{
			final int groupId = parseInteger(groupNode.getAttributes(), "id");
			final List<DropCategory> category = _herbGroups.computeIfAbsent(groupId, k -> new ArrayList<>());
			forEach(groupNode, "item", itemNode ->
			{
				final NamedNodeMap attrs = itemNode.getAttributes();
				final int id = parseInteger(attrs, "id");
				final int categoryType = parseInteger(attrs, "category");
				final int chance = parseInteger(attrs, "chance");
				final DropData dropDat = new DropData();
				dropDat.setItemId(id);
				dropDat.setMinDrop(1);
				dropDat.setMaxDrop(1);
				dropDat.setChance(chance);
				
				boolean catExists = false;
				for (final DropCategory cat : category)
				{
					if (cat.getCategoryType() == categoryType)
					{
						cat.addDropData(dropDat, false);
						catExists = true;
						break;
					}
				}
				if (!catExists)
				{
					final DropCategory cat = new DropCategory(categoryType);
					cat.addDropData(dropDat, false);
					category.add(cat);
				}
			});
		}));
	}
	
	public List<DropCategory> getHerbDroplist(int groupId)
	{
		return _herbGroups.get(groupId);
	}
	
	public static HerbDropData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HerbDropData INSTANCE = new HerbDropData();
	}
}
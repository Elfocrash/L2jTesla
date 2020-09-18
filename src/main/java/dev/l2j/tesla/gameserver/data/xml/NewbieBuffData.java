package dev.l2j.tesla.gameserver.data.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.model.NewbieBuff;
import dev.l2j.tesla.commons.data.xml.IXmlReader;
import dev.l2j.tesla.commons.util.StatsSet;

import org.w3c.dom.Document;

/**
 * This class loads and store {@link NewbieBuff} into a List.
 */
public class NewbieBuffData implements IXmlReader
{
	private final List<NewbieBuff> _buffs = new ArrayList<>();
	
	private int _magicLowestLevel = 100;
	private int _physicLowestLevel = 100;
	
	private int _magicHighestLevel = 1;
	private int _physicHighestLevel = 1;
	
	protected NewbieBuffData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/newbieBuffs.xml");
		LOGGER.info("Loaded {} newbie buffs.", _buffs.size());
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "buff", buffNode ->
		{
			final StatsSet set = parseAttributes(buffNode);
			final int lowerLevel = set.getInteger("lowerLevel");
			final int upperLevel = set.getInteger("upperLevel");
			if (set.getBool("isMagicClass"))
			{
				if (lowerLevel < _magicLowestLevel)
					_magicLowestLevel = lowerLevel;
				if (upperLevel > _magicHighestLevel)
					_magicHighestLevel = upperLevel;
			}
			else
			{
				if (lowerLevel < _physicLowestLevel)
					_physicLowestLevel = lowerLevel;
				if (upperLevel > _physicHighestLevel)
					_physicHighestLevel = upperLevel;
			}
			_buffs.add(new NewbieBuff(set));
		}));
	}
	
	/**
	 * @return the Helper Buff List
	 */
	public List<NewbieBuff> getBuffs()
	{
		return _buffs;
	}
	
	/**
	 * @return Returns the magicHighestLevel.
	 */
	public int getMagicHighestLevel()
	{
		return _magicHighestLevel;
	}
	
	/**
	 * @return Returns the magicLowestLevel.
	 */
	public int getMagicLowestLevel()
	{
		return _magicLowestLevel;
	}
	
	/**
	 * @return Returns the physicHighestLevel.
	 */
	public int getPhysicHighestLevel()
	{
		return _physicHighestLevel;
	}
	
	/**
	 * @return Returns the physicLowestLevel.
	 */
	public int getPhysicLowestLevel()
	{
		return _physicLowestLevel;
	}
	
	public static NewbieBuffData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final NewbieBuffData INSTANCE = new NewbieBuffData();
	}
}
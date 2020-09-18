package dev.l2j.tesla.gameserver.data.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import dev.l2j.tesla.gameserver.data.ItemTable;
import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.MinionData;
import dev.l2j.tesla.gameserver.model.PetDataEntry;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.actor.template.PetTemplate;
import dev.l2j.tesla.gameserver.model.item.DropCategory;
import dev.l2j.tesla.gameserver.model.item.DropData;
import dev.l2j.tesla.commons.data.xml.IXmlReader;
import dev.l2j.tesla.commons.util.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

/**
 * Loads and stores {@link NpcTemplate}s.
 */
public class NpcData implements IXmlReader
{
	private final Map<Integer, NpcTemplate> _npcs = new HashMap<>();
	
	protected NpcData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/npcs");
		LOGGER.info("Loaded {} NPC templates.", _npcs.size());
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "npc", npcNode ->
		{
			final NamedNodeMap attrs = npcNode.getAttributes();
			final int npcId = parseInteger(attrs, "id");
			final int templateId = attrs.getNamedItem("idTemplate") == null ? npcId : parseInteger(attrs, "idTemplate");
			final StatsSet set = new StatsSet();
			set.set("id", npcId);
			set.set("idTemplate", templateId);
			set.set("name", parseString(attrs, "name"));
			set.set("title", parseString(attrs, "title"));
			
			forEach(npcNode, "set", setNode ->
			{
				final NamedNodeMap setAttrs = setNode.getAttributes();
				set.set(parseString(setAttrs, "name"), parseString(setAttrs, "val"));
			});
			forEach(npcNode, "ai", aiNode ->
			{
				final NamedNodeMap aiAttrs = aiNode.getAttributes();
				set.set("aiType", parseString(aiAttrs, "type"));
				set.set("ssCount", parseInteger(aiAttrs, "ssCount"));
				set.set("ssRate", parseInteger(aiAttrs, "ssRate"));
				set.set("spsCount", parseInteger(aiAttrs, "spsCount"));
				set.set("spsRate", parseInteger(aiAttrs, "spsRate"));
				set.set("aggro", parseInteger(aiAttrs, "aggro"));
				if (aiAttrs.getNamedItem("clan") != null)
				{
					set.set("clan", parseString(aiAttrs, "clan").split(";"));
					set.set("clanRange", parseInteger(aiAttrs, "clanRange"));
					if (aiAttrs.getNamedItem("ignoredIds") != null)
						set.set("ignoredIds", parseString(aiAttrs, "ignoredIds"));
				}
				set.set("canMove", parseBoolean(aiAttrs, "canMove"));
				set.set("seedable", parseBoolean(aiAttrs, "seedable"));
			});
			forEach(npcNode, "drops", dropsNode ->
			{
				final String type = set.getString("type");
				final boolean isRaid = type.equalsIgnoreCase("RaidBoss") || type.equalsIgnoreCase("GrandBoss");
				final List<DropCategory> drops = new ArrayList<>();
				forEach(dropsNode, "category", categoryNode ->
				{
					final NamedNodeMap categoryAttrs = categoryNode.getAttributes();
					final DropCategory category = new DropCategory(parseInteger(categoryAttrs, "id"));
					forEach(categoryNode, "drop", dropNode ->
					{
						final NamedNodeMap dropAttrs = dropNode.getAttributes();
						final DropData data = new DropData();
						data.setItemId(parseInteger(dropAttrs, "itemid"));
						data.setMinDrop(parseInteger(dropAttrs, "min"));
						data.setMaxDrop(parseInteger(dropAttrs, "max"));
						data.setChance(parseInteger(dropAttrs, "chance"));
						if (ItemTable.getInstance().getTemplate(data.getItemId()) == null)
						{
							LOGGER.warn("Droplist data for undefined itemId: {}.", data.getItemId());
							return;
						}
						category.addDropData(data, isRaid);
					});
					drops.add(category);
				});
				set.set("drops", drops);
			});
			forEach(npcNode, "minions", minionsNode ->
			{
				final List<MinionData> minions = new ArrayList<>();
				forEach(minionsNode, "minion", minionNode ->
				{
					final NamedNodeMap minionAttrs = minionNode.getAttributes();
					final MinionData data = new MinionData();
					data.setMinionId(parseInteger(minionAttrs, "id"));
					data.setAmountMin(parseInteger(minionAttrs, "min"));
					data.setAmountMax(parseInteger(minionAttrs, "max"));
					minions.add(data);
				});
				set.set("minions", minions);
			});
			forEach(npcNode, "petdata", petdataNode ->
			{
				final NamedNodeMap petdataAttrs = petdataNode.getAttributes();
				set.set("mustUsePetTemplate", true);
				set.set("food1", parseInteger(petdataAttrs, "food1"));
				set.set("food2", parseInteger(petdataAttrs, "food2"));
				set.set("autoFeedLimit", parseDouble(petdataAttrs, "autoFeedLimit"));
				set.set("hungryLimit", parseDouble(petdataAttrs, "hungryLimit"));
				set.set("unsummonLimit", parseDouble(petdataAttrs, "unsummonLimit"));
				
				final Map<Integer, PetDataEntry> entries = new HashMap<>();
				forEach(petdataNode, "stat", statNode ->
				{
					final StatsSet petSet = parseAttributes(statNode);
					entries.put(petSet.getInteger("level"), new PetDataEntry(petSet));
				});
				set.set("petData", entries);
			});
			forEach(npcNode, "skills", skillsNode ->
			{
				final List<L2Skill> skills = new ArrayList<>();
				forEach(skillsNode, "skill", skillNode ->
				{
					final NamedNodeMap skillAttrs = skillNode.getAttributes();
					final int skillId = parseInteger(skillAttrs, "id");
					final int level = parseInteger(skillAttrs, "level");
					if (skillId == L2Skill.SKILL_NPC_RACE)
					{
						set.set("raceId", level);
						return;
					}
					
					final L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
					if (skill == null)
						return;
					
					skills.add(skill);
				});
				set.set("skills", skills);
			});
			forEach(npcNode, "teachTo", teachToNode -> set.set("teachTo", parseString(teachToNode.getAttributes(), "classes")));
			
			_npcs.put(npcId, set.getBool("mustUsePetTemplate", false) ? new PetTemplate(set) : new NpcTemplate(set));
		}));
	}
	
	public void reload()
	{
		_npcs.clear();
		
		load();
	}
	
	public NpcTemplate getTemplate(int id)
	{
		return _npcs.get(id);
	}
	
	/**
	 * @param name : The name of the NPC to search.
	 * @return the {@link NpcTemplate} for a given name.
	 */
	public NpcTemplate getTemplateByName(String name)
	{
		return _npcs.values().stream().filter(t -> t.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
	
	/**
	 * Gets all {@link NpcTemplate}s matching the filter.
	 * @param filter : The Predicate filter used as a filter.
	 * @return a NpcTemplate list matching the given filter.
	 */
	public List<NpcTemplate> getTemplates(Predicate<NpcTemplate> filter)
	{
		return _npcs.values().stream().filter(filter).collect(Collectors.toList());
	}
	
	public Collection<NpcTemplate> getAllNpcs()
	{
		return _npcs.values();
	}
	
	public static NpcData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final NpcData INSTANCE = new NpcData();
	}
}
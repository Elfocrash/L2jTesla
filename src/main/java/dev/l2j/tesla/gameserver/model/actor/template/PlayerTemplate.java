package dev.l2j.tesla.gameserver.model.actor.template;

import java.util.List;

import dev.l2j.tesla.gameserver.model.holder.ItemTemplateHolder;
import dev.l2j.tesla.gameserver.model.holder.skillnode.GeneralSkillNode;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.commons.random.Rnd;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.data.ItemTable;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.enums.actors.Sex;
import dev.l2j.tesla.gameserver.model.item.kind.Weapon;

/**
 * A datatype extending {@link CreatureTemplate}, used to retain Player template informations such as classId, specific collision values for female, hp/mp/cp tables, etc.<br>
 * <br>
 * Since each PlayerTemplate is associated to a {@link ClassId}, it is also used as a container for {@link GeneralSkillNode}s this class can use.<br>
 * <br>
 * Finally, it holds starter equipment (under an int array of itemId) and initial spawn {@link Location} for newbie templates.
 */
public class PlayerTemplate extends CreatureTemplate
{
	private final ClassId _classId;
	
	private final int _fallingHeight;
	
	private final int _baseSwimSpd;
	
	private final double _collisionRadiusFemale;
	private final double _collisionHeightFemale;
	
	private final List<Location> _spawnLocations;
	
	private final int _classBaseLevel;
	
	private final double[] _hpTable;
	private final double[] _mpTable;
	private final double[] _cpTable;
	
	private final List<ItemTemplateHolder> _items;
	private final List<GeneralSkillNode> _skills;
	
	private final Weapon _fists;
	
	public PlayerTemplate(StatsSet set)
	{
		super(set);
		
		_classId = ClassId.VALUES[set.getInteger("id")];
		
		_fallingHeight = set.getInteger("falling_height", 333);
		
		_baseSwimSpd = set.getInteger("swimSpd", 1);
		
		_collisionRadiusFemale = set.getDouble("radiusFemale");
		_collisionHeightFemale = set.getDouble("heightFemale");
		
		_spawnLocations = set.getList("spawnLocations");
		
		_classBaseLevel = set.getInteger("baseLvl");
		
		_hpTable = set.getDoubleArray("hpTable");
		_mpTable = set.getDoubleArray("mpTable");
		_cpTable = set.getDoubleArray("cpTable");
		
		_items = set.getList("items");
		_skills = set.getList("skills");
		
		_fists = (Weapon) ItemTable.getInstance().getTemplate(set.getInteger("fists"));
	}
	
	public final ClassId getClassId()
	{
		return _classId;
	}
	
	public final ClassRace getRace()
	{
		return _classId.getRace();
	}
	
	public final String getClassName()
	{
		return _classId.toString();
	}
	
	public final int getFallHeight()
	{
		return _fallingHeight;
	}
	
	public final int getBaseSwimSpeed()
	{
		return _baseSwimSpd;
	}
	
	/**
	 * @param sex
	 * @return : height depends on sex.
	 */
	public double getCollisionRadiusBySex(Sex sex)
	{
		return (sex == Sex.MALE) ? _collisionRadius : _collisionRadiusFemale;
	}
	
	/**
	 * @param sex
	 * @return : height depends on sex.
	 */
	public double getCollisionHeightBySex(Sex sex)
	{
		return (sex == Sex.MALE) ? _collisionHeight : _collisionHeightFemale;
	}
	
	public final Location getRandomSpawn()
	{
		final Location loc = Rnd.get(_spawnLocations);
		return (loc == null) ? Location.DUMMY_LOC : loc;
	}
	
	public final int getClassBaseLevel()
	{
		return _classBaseLevel;
	}
	
	@Override
	public final double getBaseHpMax(int level)
	{
		return _hpTable[level - 1];
	}
	
	@Override
	public final double getBaseMpMax(int level)
	{
		return _mpTable[level - 1];
	}
	
	public final double getBaseCpMax(int level)
	{
		return _cpTable[level - 1];
	}
	
	/**
	 * @return the {@link List} of {@link ItemTemplateHolder}s holding the starter equipment informations for this {@link PlayerTemplate}.
	 */
	public final List<ItemTemplateHolder> getItems()
	{
		return _items;
	}
	
	/**
	 * @return the {@link List} of all available {@link GeneralSkillNode} for this {@link PlayerTemplate}.
	 */
	public final List<GeneralSkillNode> getSkills()
	{
		return _skills;
	}
	
	/**
	 * Find if the skill exists on skill tree.
	 * @param id : The skill id to check.
	 * @param level : The skill level to check.
	 * @return the associated {@link GeneralSkillNode} if a matching id/level is found on this {@link PlayerTemplate}, or null.
	 */
	public GeneralSkillNode findSkill(int id, int level)
	{
		return _skills.stream().filter(s -> s.getId() == id && s.getValue() == level).findFirst().orElse(null);
	}
	
	/**
	 * @return the {@link Weapon} used as fists for this {@link PlayerTemplate}.
	 */
	public final Weapon getFists()
	{
		return _fists;
	}
}
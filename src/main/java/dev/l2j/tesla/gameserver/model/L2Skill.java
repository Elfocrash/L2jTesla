package dev.l2j.tesla.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.skills.Env;
import dev.l2j.tesla.gameserver.skills.Formulas;
import dev.l2j.tesla.gameserver.skills.basefuncs.Func;
import dev.l2j.tesla.gameserver.skills.basefuncs.FuncTemplate;
import dev.l2j.tesla.gameserver.skills.conditions.Condition;
import dev.l2j.tesla.gameserver.skills.effects.EffectTemplate;
import dev.l2j.tesla.gameserver.taskmanager.DecayTaskManager;
import dev.l2j.tesla.commons.math.MathUtil;
import dev.l2j.tesla.commons.util.ArraysUtil;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.enums.ZoneId;
import dev.l2j.tesla.gameserver.enums.items.ArmorType;
import dev.l2j.tesla.gameserver.enums.items.WeaponType;
import dev.l2j.tesla.gameserver.enums.skills.L2SkillType;
import dev.l2j.tesla.gameserver.enums.skills.Stats;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Playable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.Summon;
import dev.l2j.tesla.gameserver.model.actor.instance.Chest;
import dev.l2j.tesla.gameserver.model.actor.instance.Cubic;
import dev.l2j.tesla.gameserver.model.actor.instance.Door;
import dev.l2j.tesla.gameserver.model.actor.instance.HolyThing;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;
import dev.l2j.tesla.gameserver.model.actor.instance.Pet;
import dev.l2j.tesla.gameserver.model.actor.instance.Servitor;
import dev.l2j.tesla.gameserver.model.actor.instance.SiegeFlag;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.model.item.kind.Armor;
import dev.l2j.tesla.gameserver.model.item.kind.Item;
import dev.l2j.tesla.gameserver.model.item.kind.Weapon;
import dev.l2j.tesla.gameserver.model.pledge.Clan;
import dev.l2j.tesla.gameserver.model.pledge.ClanMember;

public abstract class L2Skill implements IChanceSkillTrigger
{
	protected static final Logger _log = Logger.getLogger(L2Skill.class.getName());
	
	private static final WorldObject[] _emptyTargetList = new WorldObject[0];
	
	public static final int SKILL_LUCKY = 194;
	public static final int SKILL_EXPERTISE = 239;
	public static final int SKILL_SHADOW_SENSE = 294;
	public static final int SKILL_CREATE_COMMON = 1320;
	public static final int SKILL_CREATE_DWARVEN = 172;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_DIVINE_INSPIRATION = 1405;
	public static final int SKILL_NPC_RACE = 4416;
	
	public static enum SkillOpType
	{
		OP_PASSIVE,
		OP_ACTIVE,
		OP_TOGGLE
	}
	
	/** Target types of skills : SELF, PARTY, CLAN, PET... */
	public static enum SkillTargetType
	{
		TARGET_NONE,
		TARGET_SELF,
		TARGET_ONE,
		TARGET_PARTY,
		TARGET_ALLY,
		TARGET_CLAN,
		TARGET_PET,
		TARGET_AREA,
		TARGET_FRONT_AREA,
		TARGET_BEHIND_AREA,
		TARGET_AURA,
		TARGET_FRONT_AURA,
		TARGET_BEHIND_AURA,
		TARGET_CORPSE,
		TARGET_UNDEAD,
		TARGET_AURA_UNDEAD,
		TARGET_CORPSE_ALLY,
		TARGET_CORPSE_PLAYER,
		TARGET_CORPSE_PET,
		TARGET_AREA_CORPSE_MOB,
		TARGET_CORPSE_MOB,
		TARGET_UNLOCKABLE,
		TARGET_HOLY,
		TARGET_PARTY_MEMBER,
		TARGET_PARTY_OTHER,
		TARGET_SUMMON,
		TARGET_AREA_SUMMON,
		TARGET_ENEMY_SUMMON,
		TARGET_OWNER_PET,
		TARGET_GROUND
	}
	
	// conditional values
	public static final int COND_BEHIND = 0x0008;
	public static final int COND_CRIT = 0x0010;
	
	private final int _id;
	private final int _level;
	
	private final String _name;
	private final SkillOpType _operateType;
	
	private final boolean _magic;
	
	private final int _mpConsume;
	private final int _mpInitialConsume;
	private final int _hpConsume;
	
	private final int _targetConsume;
	private final int _targetConsumeId;
	
	private final int _itemConsume; // items consumption
	private final int _itemConsumeId;
	
	private final int _castRange;
	private final int _effectRange;
	
	private final int _abnormalLvl; // Abnormal levels for skills and their canceling
	private final int _effectAbnormalLvl;
	
	private final int _hitTime; // all times in milliseconds
	private final int _coolTime;
	
	private final int _reuseDelay;
	private final int _equipDelay;
	
	private final int _buffDuration;
	
	/** Target type of the skill : SELF, PARTY, CLAN, PET... */
	private final SkillTargetType _targetType;
	
	private final double _power;
	
	private final int _magicLevel;
	
	private final int _negateLvl; // abnormalLvl is negated with negateLvl
	private final int[] _negateId; // cancels the effect of skill ID
	private final L2SkillType[] _negateStats; // lists the effect types that are canceled
	private final int _maxNegatedEffects; // maximum number of effects to negate
	
	private final int _levelDepend;
	
	private final int _skillRadius; // Effecting area of the skill, in radius.
	
	private final L2SkillType _skillType;
	private final L2SkillType _effectType;
	
	private final int _effectId;
	private final int _effectPower;
	private final int _effectLvl;
	
	private final boolean _ispotion;
	private final byte _element;
	
	private final boolean _ignoreResists;
	
	private final boolean _staticReuse;
	private final boolean _staticHitTime;
	
	private final int _reuseHashCode;
	
	private final Stats _stat;
	
	private final int _condition;
	private final int _conditionValue;
	
	private final boolean _overhit;
	private final boolean _killByDOT;
	private final boolean _isSuicideAttack;
	
	private final boolean _isSiegeSummonSkill;
	
	private final int _weaponsAllowed;
	
	private final boolean _nextActionIsAttack;
	
	private final int _minPledgeClass;
	
	private final boolean _isOffensive;
	private final int _maxCharges;
	private final int _numCharges;
	
	private final int _triggeredId;
	private final int _triggeredLevel;
	protected ChanceCondition _chanceCondition = null;
	private final String _chanceType;
	
	private final String _flyType;
	private final int _flyRadius;
	private final float _flyCourse;
	
	private final int _feed;
	
	private final boolean _isHeroSkill; // If true the skill is a Hero Skill
	
	private final int _baseCritRate; // percent of success for skill critical hit (especially for PDAM & BLOW - they're not affected by rCrit values or buffs). Default loads -1 for all other skills but 0 to PDAM & BLOW
	private final int _lethalEffect1; // percent of success for lethal 1st effect (hit cp to 1 or if mob hp to 50%) (only for PDAM skills)
	private final int _lethalEffect2; // percent of success for lethal 2nd effect (hit cp,hp to 1 or if mob hp to 1) (only for PDAM skills)
	private final boolean _directHpDmg; // If true then dmg is being make directly
	private final boolean _isDance; // If true then casting more dances will cost more MP
	private final int _nextDanceCost;
	private final float _sSBoost; // If true skill will have SoulShot boost (power*2)
	private final int _aggroPoints;
	
	protected List<Condition> _preCondition;
	protected List<Condition> _itemPreCondition;
	protected List<FuncTemplate> _funcTemplates;
	protected List<EffectTemplate> _effectTemplates;
	protected List<EffectTemplate> _effectTemplatesSelf;
	
	private final String _attribute;
	
	private final boolean _isDebuff;
	private final boolean _stayAfterDeath; // skill should stay after death
	
	private final boolean _removedOnAnyActionExceptMove;
	private final boolean _removedOnDamage;
	
	private final boolean _canBeReflected;
	private final boolean _canBeDispeled;
	
	private final boolean _isClanSkill;
	
	private final boolean _ignoreShield;
	
	private final boolean _simultaneousCast;
	
	private L2ExtractableSkill _extractableItems = null;
	
	protected L2Skill(StatsSet set)
	{
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");
		
		_name = set.getString("name");
		_operateType = set.getEnum("operateType", SkillOpType.class);
		
		_magic = set.getBool("isMagic", false);
		_ispotion = set.getBool("isPotion", false);
		
		_mpConsume = set.getInteger("mpConsume", 0);
		_mpInitialConsume = set.getInteger("mpInitialConsume", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		
		_targetConsume = set.getInteger("targetConsumeCount", 0);
		_targetConsumeId = set.getInteger("targetConsumeId", 0);
		
		_itemConsume = set.getInteger("itemConsumeCount", 0);
		_itemConsumeId = set.getInteger("itemConsumeId", 0);
		
		_castRange = set.getInteger("castRange", 0);
		_effectRange = set.getInteger("effectRange", -1);
		
		_abnormalLvl = set.getInteger("abnormalLvl", -1);
		_effectAbnormalLvl = set.getInteger("effectAbnormalLvl", -1); // support for a separate effect abnormal lvl, e.g. poison inside a different skill
		_negateLvl = set.getInteger("negateLvl", -1);
		
		_hitTime = set.getInteger("hitTime", 0);
		_coolTime = set.getInteger("coolTime", 0);
		
		_reuseDelay = set.getInteger("reuseDelay", 0);
		_equipDelay = set.getInteger("equipDelay", 0);
		
		_buffDuration = set.getInteger("buffDuration", 0);
		
		_skillRadius = set.getInteger("skillRadius", 80);
		
		_targetType = set.getEnum("target", SkillTargetType.class);
		
		_power = set.getFloat("power", 0.f);
		
		_attribute = set.getString("attribute", "");
		String str = set.getString("negateStats", "");
		
		if (str.isEmpty())
			_negateStats = new L2SkillType[0];
		else
		{
			String[] stats = str.split(" ");
			L2SkillType[] array = new L2SkillType[stats.length];
			
			for (int i = 0; i < stats.length; i++)
			{
				L2SkillType type = null;
				try
				{
					type = Enum.valueOf(L2SkillType.class, stats[i]);
				}
				catch (Exception e)
				{
					throw new IllegalArgumentException("SkillId: " + _id + "Enum value of type " + L2SkillType.class.getName() + " required, but found: " + stats[i]);
				}
				
				array[i] = type;
			}
			_negateStats = array;
		}
		
		String negateId = set.getString("negateId", null);
		if (negateId != null)
		{
			String[] valuesSplit = negateId.split(",");
			_negateId = new int[valuesSplit.length];
			for (int i = 0; i < valuesSplit.length; i++)
			{
				_negateId[i] = Integer.parseInt(valuesSplit[i]);
			}
		}
		else
			_negateId = new int[0];
		
		_maxNegatedEffects = set.getInteger("maxNegated", 0);
		
		_magicLevel = set.getInteger("magicLvl", 0);
		_levelDepend = set.getInteger("lvlDepend", 0);
		_ignoreResists = set.getBool("ignoreResists", false);
		
		_staticReuse = set.getBool("staticReuse", false);
		_staticHitTime = set.getBool("staticHitTime", false);
		
		String reuseHash = set.getString("sharedReuse", null);
		if (reuseHash != null)
		{
			try
			{
				String[] valuesSplit = reuseHash.split("-");
				_reuseHashCode = SkillTable.getSkillHashCode(Integer.parseInt(valuesSplit[0]), Integer.parseInt(valuesSplit[1]));
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("SkillId: " + _id + " invalid sharedReuse value: " + reuseHash + ", \"skillId-skillLvl\" required");
			}
		}
		else
			_reuseHashCode = SkillTable.getSkillHashCode(_id, _level);
		
		_stat = set.getEnum("stat", Stats.class, null);
		_ignoreShield = set.getBool("ignoreShld", false);
		
		_skillType = set.getEnum("skillType", L2SkillType.class);
		_effectType = set.getEnum("effectType", L2SkillType.class, null);
		
		_effectId = set.getInteger("effectId", 0);
		_effectPower = set.getInteger("effectPower", 0);
		_effectLvl = set.getInteger("effectLevel", 0);
		
		_element = set.getByte("element", (byte) -1);
		
		_condition = set.getInteger("condition", 0);
		_conditionValue = set.getInteger("conditionValue", 0);
		
		_overhit = set.getBool("overHit", false);
		_killByDOT = set.getBool("killByDOT", false);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		
		_isSiegeSummonSkill = set.getBool("isSiegeSummonSkill", false);
		
		String weaponsAllowedString = set.getString("weaponsAllowed", null);
		if (weaponsAllowedString != null)
		{
			int mask = 0;
			StringTokenizer st = new StringTokenizer(weaponsAllowedString, ",");
			while (st.hasMoreTokens())
			{
				int old = mask;
				String item = st.nextToken();
				for (WeaponType wt : WeaponType.values())
				{
					if (wt.name().equals(item))
					{
						mask |= wt.mask();
						break;
					}
				}
				
				for (ArmorType at : ArmorType.values())
				{
					if (at.name().equals(item))
					{
						mask |= at.mask();
						break;
					}
				}
				
				if (old == mask)
					_log.info("[weaponsAllowed] Unknown item type name: " + item);
			}
			_weaponsAllowed = mask;
		}
		else
			_weaponsAllowed = 0;
		
		_nextActionIsAttack = set.getBool("nextActionAttack", false);
		
		_minPledgeClass = set.getInteger("minPledgeClass", 0);
		
		_triggeredId = set.getInteger("triggeredId", 0);
		_triggeredLevel = set.getInteger("triggeredLevel", 0);
		_chanceType = set.getString("chanceType", "");
		if (!_chanceType.isEmpty())
			_chanceCondition = ChanceCondition.parse(set);
		
		_isDebuff = set.getBool("isDebuff", false);
		_isOffensive = set.getBool("offensive", isSkillTypeOffensive());
		_maxCharges = set.getInteger("maxCharges", 0);
		_numCharges = set.getInteger("numCharges", 0);
		
		_isHeroSkill = SkillTable.isHeroSkill(_id);
		
		_baseCritRate = set.getInteger("baseCritRate", (_skillType == L2SkillType.PDAM || _skillType == L2SkillType.BLOW) ? 0 : -1);
		_lethalEffect1 = set.getInteger("lethal1", 0);
		_lethalEffect2 = set.getInteger("lethal2", 0);
		
		_directHpDmg = set.getBool("dmgDirectlyToHp", false);
		_isDance = set.getBool("isDance", false);
		_nextDanceCost = set.getInteger("nextDanceCost", 0);
		_sSBoost = set.getFloat("SSBoost", 0.f);
		_aggroPoints = set.getInteger("aggroPoints", 0);
		
		_stayAfterDeath = set.getBool("stayAfterDeath", false);
		
		_removedOnAnyActionExceptMove = set.getBool("removedOnAnyActionExceptMove", false);
		_removedOnDamage = set.getBool("removedOnDamage", _skillType == L2SkillType.SLEEP);
		
		_flyType = set.getString("flyType", null);
		_flyRadius = set.getInteger("flyRadius", 0);
		_flyCourse = set.getFloat("flyCourse", 0);
		
		_feed = set.getInteger("feed", 0);
		
		_canBeReflected = set.getBool("canBeReflected", true);
		_canBeDispeled = set.getBool("canBeDispeled", true);
		
		_isClanSkill = set.getBool("isClanSkill", false);
		
		_simultaneousCast = set.getBool("simultaneousCast", false);
		
		String capsuled_items = set.getString("capsuled_items_skill", null);
		if (capsuled_items != null)
		{
			if (capsuled_items.isEmpty())
				_log.warning("Empty extractable data for skill: " + _id);
			
			_extractableItems = parseExtractableSkill(_id, _level, capsuled_items);
		}
	}
	
	public abstract void useSkill(Creature caster, WorldObject[] targets);
	
	public final boolean isPotion()
	{
		return _ispotion;
	}
	
	public final int getConditionValue()
	{
		return _conditionValue;
	}
	
	public final L2SkillType getSkillType()
	{
		return _skillType;
	}
	
	public final byte getElement()
	{
		return _element;
	}
	
	/**
	 * @return the target type of the skill : SELF, PARTY, CLAN, PET...
	 */
	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}
	
	public final int getCondition()
	{
		return _condition;
	}
	
	public final boolean isOverhit()
	{
		return _overhit;
	}
	
	public final boolean killByDOT()
	{
		return _killByDOT;
	}
	
	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}
	
	public final boolean isSiegeSummonSkill()
	{
		return _isSiegeSummonSkill;
	}
	
	/**
	 * @param activeChar
	 * @return the power of the skill.
	 */
	public final double getPower(Creature activeChar)
	{
		if (activeChar == null)
			return _power;
		
		switch (_skillType)
		{
			case DEATHLINK:
				return _power * Math.pow(1.7165 - activeChar.getCurrentHp() / activeChar.getMaxHp(), 2) * 0.577;
			case FATAL:
				return _power + (_power * Math.pow(1.7165 - activeChar.getCurrentHp() / activeChar.getMaxHp(), 3.5) * 0.577);
			default:
				return _power;
		}
	}
	
	public final double getPower()
	{
		return _power;
	}
	
	public final L2SkillType[] getNegateStats()
	{
		return _negateStats;
	}
	
	public final int getAbnormalLvl()
	{
		return _abnormalLvl;
	}
	
	public final int getNegateLvl()
	{
		return _negateLvl;
	}
	
	public final int[] getNegateId()
	{
		return _negateId;
	}
	
	public final int getMagicLevel()
	{
		return _magicLevel;
	}
	
	public final int getMaxNegatedEffects()
	{
		return _maxNegatedEffects;
	}
	
	public final int getLevelDepend()
	{
		return _levelDepend;
	}
	
	/**
	 * @return true if skill should ignore all resistances.
	 */
	public final boolean ignoreResists()
	{
		return _ignoreResists;
	}
	
	public int getTriggeredId()
	{
		return _triggeredId;
	}
	
	public int getTriggeredLevel()
	{
		return _triggeredLevel;
	}
	
	public boolean triggerAnotherSkill()
	{
		return _triggeredId > 1;
	}
	
	/**
	 * @return true if skill effects should be removed on any action except movement
	 */
	public final boolean isRemovedOnAnyActionExceptMove()
	{
		return _removedOnAnyActionExceptMove;
	}
	
	/**
	 * @return true if skill effects should be removed on damage
	 */
	public final boolean isRemovedOnDamage()
	{
		return _removedOnDamage;
	}
	
	/**
	 * @return the additional effect power or base probability.
	 */
	public final double getEffectPower()
	{
		if (_effectTemplates != null)
		{
			for (EffectTemplate et : _effectTemplates)
			{
				if (et.effectPower > 0)
					return et.effectPower;
			}
		}
		
		if (_effectPower > 0)
			return _effectPower;
		
		// Allow damage dealing skills having proper resist even without specified effectPower.
		switch (_skillType)
		{
			case PDAM:
			case MDAM:
				return 20;
			
			default:
				// to let debuffs succeed even without specified power
				return (_power <= 0 || 100 < _power) ? 20 : _power;
		}
	}
	
	/**
	 * @return the additional effect Id.
	 */
	public final int getEffectId()
	{
		return _effectId;
	}
	
	/**
	 * @return the additional effect level.
	 */
	public final int getEffectLvl()
	{
		return _effectLvl;
	}
	
	public final int getEffectAbnormalLvl()
	{
		return _effectAbnormalLvl;
	}
	
	/**
	 * @return the additional effect skill type (ex : STUN, PARALYZE,...).
	 */
	public final L2SkillType getEffectType()
	{
		if (_effectTemplates != null)
		{
			for (EffectTemplate et : _effectTemplates)
			{
				if (et.effectType != null)
					return et.effectType;
			}
		}
		
		if (_effectType != null)
			return _effectType;
		
		// to let damage dealing skills having proper resist even without specified effectType
		switch (_skillType)
		{
			case PDAM:
				return L2SkillType.STUN;
			case MDAM:
				return L2SkillType.PARALYZE;
			default:
				return _skillType;
		}
	}
	
	/**
	 * @return true if character should attack target after skill
	 */
	public final boolean nextActionIsAttack()
	{
		return _nextActionIsAttack;
	}
	
	/**
	 * @return Returns the buffDuration.
	 */
	public final int getBuffDuration()
	{
		return _buffDuration;
	}
	
	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange()
	{
		return _castRange;
	}
	
	/**
	 * @return Returns the effectRange.
	 */
	public final int getEffectRange()
	{
		return _effectRange;
	}
	
	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return _hpConsume;
	}
	
	/**
	 * @return Returns the boolean _isDebuff.
	 */
	public final boolean isDebuff()
	{
		return _isDebuff;
	}
	
	/**
	 * @return the skill id.
	 */
	public final int getId()
	{
		return _id;
	}
	
	public final Stats getStat()
	{
		return _stat;
	}
	
	/**
	 * @return the _targetConsumeId.
	 */
	public final int getTargetConsumeId()
	{
		return _targetConsumeId;
	}
	
	/**
	 * @return the targetConsume.
	 */
	public final int getTargetConsume()
	{
		return _targetConsume;
	}
	
	/**
	 * @return the itemConsume.
	 */
	public final int getItemConsume()
	{
		return _itemConsume;
	}
	
	/**
	 * @return the itemConsumeId.
	 */
	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}
	
	/**
	 * @return the level.
	 */
	public final int getLevel()
	{
		return _level;
	}
	
	/**
	 * @return the magic.
	 */
	public final boolean isMagic()
	{
		return _magic;
	}
	
	/**
	 * @return true to set static reuse.
	 */
	public final boolean isStaticReuse()
	{
		return _staticReuse;
	}
	
	/**
	 * @return true to set static hittime.
	 */
	public final boolean isStaticHitTime()
	{
		return _staticHitTime;
	}
	
	/**
	 * @return Returns the mpConsume.
	 */
	public final int getMpConsume()
	{
		return _mpConsume;
	}
	
	/**
	 * @return Returns the mpInitialConsume.
	 */
	public final int getMpInitialConsume()
	{
		return _mpInitialConsume;
	}
	
	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return _name;
	}
	
	/**
	 * @return Returns the reuseDelay.
	 */
	public final int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	public final int getEquipDelay()
	{
		return _equipDelay;
	}
	
	public final int getReuseHashCode()
	{
		return _reuseHashCode;
	}
	
	public final int getHitTime()
	{
		return _hitTime;
	}
	
	/**
	 * @return Returns the coolTime.
	 */
	public final int getCoolTime()
	{
		return _coolTime;
	}
	
	public final int getSkillRadius()
	{
		return _skillRadius;
	}
	
	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}
	
	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}
	
	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}
	
	public boolean isChance()
	{
		return _chanceCondition != null && isPassive();
	}
	
	public final boolean isDance()
	{
		return _isDance;
	}
	
	public final int getNextDanceMpCost()
	{
		return _nextDanceCost;
	}
	
	public final float getSSBoost()
	{
		return _sSBoost;
	}
	
	public final int getAggroPoints()
	{
		return _aggroPoints;
	}
	
	public final boolean useSoulShot()
	{
		switch (_skillType)
		{
			case BLOW:
			case PDAM:
			case STUN:
			case CHARGEDAM:
				return true;
		}
		return false;
	}
	
	public final boolean useSpiritShot()
	{
		return isMagic();
	}
	
	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}
	
	public boolean isSimultaneousCast()
	{
		return _simultaneousCast;
	}
	
	public int getMinPledgeClass()
	{
		return _minPledgeClass;
	}
	
	public String getAttributeName()
	{
		return _attribute;
	}
	
	public boolean ignoreShield()
	{
		return _ignoreShield;
	}
	
	public boolean canBeReflected()
	{
		return _canBeReflected;
	}
	
	public boolean canBeDispeled()
	{
		return _canBeDispeled;
	}
	
	public boolean isClanSkill()
	{
		return _isClanSkill;
	}
	
	public final String getFlyType()
	{
		return _flyType;
	}
	
	public final int getFlyRadius()
	{
		return _flyRadius;
	}
	
	public int getFeed()
	{
		return _feed;
	}
	
	public final float getFlyCourse()
	{
		return _flyCourse;
	}
	
	public final int getMaxCharges()
	{
		return _maxCharges;
	}
	
	@Override
	public boolean triggersChanceSkill()
	{
		return _triggeredId > 0 && isChance();
	}
	
	@Override
	public int getTriggeredChanceId()
	{
		return _triggeredId;
	}
	
	@Override
	public int getTriggeredChanceLevel()
	{
		return _triggeredLevel;
	}
	
	@Override
	public ChanceCondition getTriggeredChanceCondition()
	{
		return _chanceCondition;
	}
	
	public final boolean isPvpSkill()
	{
		switch (_skillType)
		{
			case DOT:
			case BLEED:
			case POISON:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case FEAR:
			case SLEEP:
			case MDOT:
			case MUTE:
			case WEAKNESS:
			case PARALYZE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case BETRAY:
			case AGGDAMAGE:
			case AGGREDUCE_CHAR:
			case MANADAM:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean is7Signs()
	{
		if (_id > 4360 && _id < 4367)
			return true;
		return false;
	}
	
	public final boolean isStayAfterDeath()
	{
		return _stayAfterDeath;
	}
	
	public final boolean isOffensive()
	{
		return _isOffensive;
	}
	
	public final boolean isHeroSkill()
	{
		return _isHeroSkill;
	}
	
	public final int getNumCharges()
	{
		return _numCharges;
	}
	
	public final int getBaseCritRate()
	{
		return _baseCritRate;
	}
	
	public final int getLethalChance1()
	{
		return _lethalEffect1;
	}
	
	public final int getLethalChance2()
	{
		return _lethalEffect2;
	}
	
	public final boolean getDmgDirectlyToHP()
	{
		return _directHpDmg;
	}
	
	public final boolean isSkillTypeOffensive()
	{
		switch (_skillType)
		{
			case PDAM:
			case MDAM:
			case CPDAMPERCENT:
			case DOT:
			case BLEED:
			case POISON:
			case AGGDAMAGE:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case CONFUSION:
			case ERASE:
			case BLOW:
			case FATAL:
			case FEAR:
			case DRAIN:
			case SLEEP:
			case CHARGEDAM:
			case DEATHLINK:
			case DETECT_WEAKNESS:
			case MANADAM:
			case MDOT:
			case MUTE:
			case SOULSHOT:
			case SPIRITSHOT:
			case SPOIL:
			case WEAKNESS:
			case SWEEP:
			case PARALYZE:
			case DRAIN_SOUL:
			case AGGREDUCE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case AGGREMOVE:
			case AGGREDUCE_CHAR:
			case BETRAY:
			case DELUXE_KEY_UNLOCK:
			case SOW:
			case HARVEST:
			case INSTANT_JUMP:
				return true;
			default:
				return isDebuff();
		}
	}
	
	public final boolean getWeaponDependancy(Creature activeChar)
	{
		// check to see if skill has a weapon dependency.
		final int weaponsAllowed = getWeaponsAllowed();
		if (weaponsAllowed == 0)
			return true;
		
		int mask = 0;
		
		final Weapon weapon = activeChar.getActiveWeaponItem();
		if (weapon != null)
			mask |= weapon.getItemType().mask();
		
		final Item shield = activeChar.getSecondaryWeaponItem();
		if (shield != null && shield instanceof Armor)
			mask |= ((ArmorType) shield.getItemType()).mask();
		
		if ((mask & weaponsAllowed) != 0)
			return true;
		
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(this));
		return false;
	}
	
	public boolean checkCondition(Creature activeChar, WorldObject target, boolean itemOrWeapon)
	{
		final List<Condition> preCondition = (itemOrWeapon) ? _itemPreCondition : _preCondition;
		if (preCondition == null || preCondition.isEmpty())
			return true;
		
		final Env env = new Env();
		env.setCharacter(activeChar);
		if (target instanceof Creature)
			env.setTarget((Creature) target);
		
		env.setSkill(this);
		
		for (Condition cond : preCondition)
		{
			if (!cond.test(env))
			{
				final int msgId = cond.getMessageId();
				if (msgId != 0)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(msgId);
					if (cond.isAddName())
						sm.addSkillName(_id);
					activeChar.sendPacket(sm);
				}
				else
				{
					final String msg = cond.getMessage();
					if (msg != null)
						activeChar.sendMessage(msg);
				}
				return false;
			}
		}
		return true;
	}
	
	public final WorldObject[] getTargetList(Creature activeChar, boolean onlyFirst)
	{
		// Init to null the target of the skill
		Creature target = null;
		
		// Get the WorldObject targeted by the user of the skill at this moment
		WorldObject objTarget = activeChar.getTarget();
		if (objTarget instanceof Creature)
			target = (Creature) objTarget;
		
		return getTargetList(activeChar, onlyFirst, target);
	}
	
	/**
	 * @param activeChar : The skill caster.
	 * @param onlyFirst : Returns the first target only, dropping others results.
	 * @param target : The skill target, which can be used as a radius center or as main target.
	 * @return an WorldObject[] consisting of all targets, depending of the skill type.
	 */
	public final WorldObject[] getTargetList(Creature activeChar, boolean onlyFirst, Creature target)
	{
		switch (_targetType)
		{
			case TARGET_ONE:
			{
				boolean canTargetSelf = false;
				switch (_skillType)
				{
					case BUFF:
					case HEAL:
					case HOT:
					case HEAL_PERCENT:
					case MANARECHARGE:
					case MANAHEAL:
					case NEGATE:
					case CANCEL_DEBUFF:
					case REFLECT:
					case COMBATPOINTHEAL:
					case SEED:
					case BALANCE_LIFE:
						canTargetSelf = true;
						break;
				}
				
				if (target == null || target.isDead() || (target == activeChar && !canTargetSelf))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				return new Creature[]
				{
					target
				};
			}
			case TARGET_SELF:
			case TARGET_GROUND:
			{
				return new Creature[]
				{
					activeChar
				};
			}
			case TARGET_HOLY:
			{
				if (!(target instanceof HolyThing))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				return new Creature[]
				{
					target
				};
			}
			case TARGET_PET:
			{
				target = activeChar.getSummon();
				if (target != null && !target.isDead())
					return new Creature[]
					{
						target
					};
				
				return _emptyTargetList;
			}
			case TARGET_SUMMON:
			{
				target = activeChar.getSummon();
				if (target != null && !target.isDead() && target instanceof Servitor)
					return new Creature[]
					{
						target
					};
				
				return _emptyTargetList;
			}
			case TARGET_OWNER_PET:
			{
				if (activeChar instanceof Summon)
				{
					target = activeChar.getActingPlayer();
					if (target != null && !target.isDead())
						return new Creature[]
						{
							target
						};
				}
				
				return _emptyTargetList;
			}
			case TARGET_CORPSE_PET:
			{
				if (activeChar instanceof Player)
				{
					target = activeChar.getSummon();
					if (target != null && target.isDead())
						return new Creature[]
						{
							target
						};
				}
				
				return _emptyTargetList;
			}
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			{
				List<Creature> targetList = new ArrayList<>();
				
				// Go through the Creature knownList
				if (_skillType == L2SkillType.DUMMY)
				{
					if (onlyFirst)
						return new Creature[]
						{
							activeChar
						};
					
					final Player sourcePlayer = activeChar.getActingPlayer();
					
					targetList.add(activeChar);
					for (Creature obj : activeChar.getKnownTypeInRadius(Creature.class, _skillRadius))
					{
						if (!(obj == activeChar || obj == sourcePlayer || obj instanceof Npc || obj instanceof Attackable))
							continue;
						
						targetList.add(obj);
					}
				}
				else
				{
					final boolean srcInArena = activeChar.isInArena();
					
					for (Creature obj : activeChar.getKnownTypeInRadius(Creature.class, _skillRadius))
					{
						if (obj instanceof Attackable || obj instanceof Playable)
						{
							switch (_targetType)
							{
								case TARGET_FRONT_AURA:
									if (!obj.isInFrontOf(activeChar))
										continue;
									break;
								case TARGET_BEHIND_AURA:
									if (!obj.isBehind(activeChar))
										continue;
									break;
							}
							
							if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
								continue;
							
							if (onlyFirst)
								return new Creature[]
								{
									obj
								};
							
							targetList.add(obj);
						}
					}
				}
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_AREA_SUMMON:
			{
				target = activeChar.getSummon();
				if (target == null || !(target instanceof Servitor) || target.isDead())
					return _emptyTargetList;
				
				if (onlyFirst)
					return new Creature[]
					{
						target
					};
				
				final boolean srcInArena = activeChar.isInArena();
				List<Creature> targetList = new ArrayList<>();
				
				for (Creature obj : target.getKnownType(Creature.class))
				{
					if (obj == null || obj == target || obj == activeChar)
						continue;
					
					if (!MathUtil.checkIfInRange(_skillRadius, target, obj, true))
						continue;
					
					if (!(obj instanceof Attackable || obj instanceof Playable))
						continue;
					
					if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
						continue;
					
					targetList.add(obj);
				}
				
				if (targetList.isEmpty())
					return _emptyTargetList;
				
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_AREA:
			case TARGET_FRONT_AREA:
			case TARGET_BEHIND_AREA:
			{
				if (((target == null || target == activeChar || target.isAlikeDead()) && _castRange >= 0) || (!(target instanceof Attackable || target instanceof Playable)))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				final Creature origin;
				final boolean srcInArena = activeChar.isInArena();
				List<Creature> targetList = new ArrayList<>();
				
				if (_castRange >= 0)
				{
					if (!checkForAreaOffensiveSkills(activeChar, target, this, srcInArena))
						return _emptyTargetList;
					
					if (onlyFirst)
						return new Creature[]
						{
							target
						};
					
					origin = target;
					targetList.add(origin); // Add target to target list
				}
				else
					origin = activeChar;
				
				for (Creature obj : activeChar.getKnownType(Creature.class))
				{
					if (!(obj instanceof Attackable || obj instanceof Playable))
						continue;
					
					if (obj == origin)
						continue;
					
					if (MathUtil.checkIfInRange(_skillRadius, origin, obj, true))
					{
						switch (_targetType)
						{
							case TARGET_FRONT_AREA:
								if (!obj.isInFrontOf(activeChar))
									continue;
								break;
							case TARGET_BEHIND_AREA:
								if (!obj.isBehind(activeChar))
									continue;
								break;
						}
						
						if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
							continue;
						
						targetList.add(obj);
					}
				}
				
				if (targetList.isEmpty())
					return _emptyTargetList;
				
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_PARTY:
			{
				if (onlyFirst)
					return new Creature[]
					{
						activeChar
					};
				
				List<Creature> targetList = new ArrayList<>();
				targetList.add(activeChar);
				
				final int radius = _skillRadius;
				final Player player = activeChar.getActingPlayer();
				
				if (activeChar instanceof Summon)
				{
					if (addCharacter(activeChar, player, radius, false))
						targetList.add(player);
				}
				else if (activeChar instanceof Player)
				{
					if (addSummon(activeChar, player, radius, false))
						targetList.add(player.getSummon());
				}
				
				final Party party = activeChar.getParty();
				if (party != null)
				{
					// Get a list of Party Members
					for (Player partyMember : party.getMembers())
					{
						if (partyMember == player)
							continue;
						
						if (addCharacter(activeChar, partyMember, radius, false))
							targetList.add(partyMember);
						
						if (addSummon(activeChar, partyMember, radius, false))
							targetList.add(partyMember.getSummon());
					}
				}
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_PARTY_MEMBER:
			{
				if (target != null && (target == activeChar || (activeChar.isInParty() && target.isInParty() && activeChar.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId()) || (activeChar instanceof Player && target instanceof Summon && activeChar.getSummon() == target) || (activeChar instanceof Summon && target instanceof Player && activeChar == target.getSummon())))
				{
					if (!target.isDead())
					{
						// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
						return new Creature[]
						{
							target
						};
					}
					return _emptyTargetList;
				}
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return _emptyTargetList;
			}
			case TARGET_PARTY_OTHER:
			{
				if (target != null && target != activeChar && activeChar.isInParty() && target.isInParty() && activeChar.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId())
				{
					if (!target.isDead())
					{
						if (target instanceof Player)
						{
							switch (getId())
							{
								// FORCE BUFFS may cancel here but there should be a proper condition
								case 426:
									if (!((Player) target).isMageClass())
										return new Creature[]
										{
											target
										};
									return _emptyTargetList;
								
								case 427:
									if (((Player) target).isMageClass())
										return new Creature[]
										{
											target
										};
									
									return _emptyTargetList;
							}
						}
						return new Creature[]
						{
							target
						};
					}
					return _emptyTargetList;
				}
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return _emptyTargetList;
			}
			case TARGET_ALLY:
			{
				final Player player = activeChar.getActingPlayer();
				if (player == null)
					return _emptyTargetList;
				
				if (onlyFirst || player.isInOlympiadMode())
					return new Creature[]
					{
						activeChar
					};
				
				List<Creature> targetList = new ArrayList<>();
				targetList.add(player);
				
				final int radius = _skillRadius;
				
				if (addSummon(activeChar, player, radius, false))
					targetList.add(player.getSummon());
				
				if (player.getClan() != null)
				{
					for (Player obj : activeChar.getKnownTypeInRadius(Player.class, radius))
					{
						if ((obj.getAllyId() == 0 || obj.getAllyId() != player.getAllyId()) && (obj.getClan() == null || obj.getClanId() != player.getClanId()))
							continue;
						
						if (player.isInDuel())
						{
							if (player.getDuelId() != obj.getDuelId())
								continue;
							
							if (player.isInParty() && obj.isInParty() && player.getParty().getLeaderObjectId() != obj.getParty().getLeaderObjectId())
								continue;
						}
						
						if (!player.checkPvpSkill(obj, this))
							continue;
						
						final Summon summon = obj.getSummon();
						if (summon != null && !summon.isDead())
							targetList.add(summon);
						
						if (!obj.isDead())
							targetList.add(obj);
					}
				}
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_CORPSE_ALLY:
			{
				final Player player = activeChar.getActingPlayer();
				if (player == null)
					return _emptyTargetList;
				
				if (onlyFirst || player.isInOlympiadMode())
					return new Creature[]
					{
						activeChar
					};
				
				final int radius = _skillRadius;
				List<Creature> targetList = new ArrayList<>();
				
				targetList.add(activeChar);
				
				if (player.getClan() != null)
				{
					final boolean isInBossZone = player.isInsideZone(ZoneId.BOSS);
					
					for (Player obj : activeChar.getKnownTypeInRadius(Player.class, radius))
					{
						if (!obj.isDead())
							continue;
						
						if ((obj.getAllyId() == 0 || obj.getAllyId() != player.getAllyId()) && (obj.getClan() == null || obj.getClanId() != player.getClanId()))
							continue;
						
						if (player.isInDuel())
						{
							if (player.getDuelId() != obj.getDuelId())
								continue;
							
							if (player.isInParty() && obj.isInParty() && player.getParty().getLeaderObjectId() != obj.getParty().getLeaderObjectId())
								continue;
						}
						
						// Siege battlefield resurrect has been made possible for participants
						if (obj.isInsideZone(ZoneId.SIEGE) && !obj.isInSiege())
							continue;
						
						// Check if both caster and target are in a boss zone.
						if (isInBossZone != obj.isInsideZone(ZoneId.BOSS))
							continue;
						
						targetList.add(obj);
					}
				}
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_CLAN:
			{
				List<Creature> targetList = new ArrayList<>();
				
				if (activeChar instanceof Playable)
				{
					final Player player = activeChar.getActingPlayer();
					if (player == null)
						return _emptyTargetList;
					
					if (onlyFirst || player.isInOlympiadMode())
						return new Creature[]
						{
							activeChar
						};
					
					targetList.add(player);
					
					final int radius = _skillRadius;
					
					if (addSummon(activeChar, player, radius, false))
						targetList.add(player.getSummon());
					
					final Clan clan = player.getClan();
					if (clan != null)
					{
						for (ClanMember member : clan.getMembers())
						{
							final Player obj = member.getPlayerInstance();
							if (obj == null || obj == player)
								continue;
							
							if (player.isInDuel())
							{
								if (player.getDuelId() != obj.getDuelId())
									continue;
								
								if (player.isInParty() && obj.isInParty() && player.getParty().getLeaderObjectId() != obj.getParty().getLeaderObjectId())
									continue;
							}
							
							if (!player.checkPvpSkill(obj, this))
								continue;
							
							if (addSummon(activeChar, obj, radius, false))
								targetList.add(obj.getSummon());
							
							if (!addCharacter(activeChar, obj, radius, false))
								continue;
							
							targetList.add(obj);
						}
					}
				}
				else if (activeChar instanceof Npc)
				{
					targetList.add(activeChar);
					for (Npc newTarget : activeChar.getKnownTypeInRadius(Npc.class, _castRange))
					{
						if (newTarget.isDead() || !ArraysUtil.contains(((Npc) activeChar).getTemplate().getClans(), newTarget.getTemplate().getClans()))
							continue;
						
						targetList.add(newTarget);
					}
				}
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_CORPSE_PLAYER:
			{
				if (!(activeChar instanceof Player))
					return _emptyTargetList;
				
				if (target != null && target.isDead())
				{
					final Player targetPlayer;
					if (target instanceof Player)
						targetPlayer = (Player) target;
					else
						targetPlayer = null;
					
					final Pet targetPet;
					if (target instanceof Pet)
						targetPet = (Pet) target;
					else
						targetPet = null;
					
					if (targetPlayer != null || targetPet != null)
					{
						boolean condGood = true;
						
						if (_skillType == L2SkillType.RESURRECT)
						{
							final Player player = (Player) activeChar;
							
							if (targetPlayer != null)
							{
								// check target is not in a active siege zone
								if (targetPlayer.isInsideZone(ZoneId.SIEGE) && !targetPlayer.isInSiege())
								{
									condGood = false;
									activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE));
								}
								
								if (targetPlayer.isFestivalParticipant()) // Check to see if the current player target is in a festival.
								{
									condGood = false;
									activeChar.sendMessage("You may not resurrect participants in a festival.");
								}
								
								if (targetPlayer.isReviveRequested())
								{
									if (targetPlayer.isRevivingPet())
										player.sendPacket(SystemMessageId.MASTER_CANNOT_RES); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
									else
										player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
									condGood = false;
								}
							}
							else if (targetPet != null)
							{
								if (targetPet.getOwner() != player)
								{
									if (targetPet.getOwner().isReviveRequested())
									{
										if (targetPet.getOwner().isRevivingPet())
											player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
										else
											player.sendPacket(SystemMessageId.CANNOT_RES_PET2); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
										condGood = false;
									}
								}
							}
						}
						
						if (condGood)
							return new Creature[]
							{
								target
							};
					}
				}
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return _emptyTargetList;
			}
			case TARGET_CORPSE_MOB:
			{
				if (!(target instanceof Monster) || !target.isDead())
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				// Corpse mob only available for half time
				if (_skillType == L2SkillType.DRAIN && !DecayTaskManager.getInstance().isCorpseActionAllowed((Monster) target))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED));
					return _emptyTargetList;
				}
				
				return new Creature[]
				{
					target
				};
			}
			case TARGET_AREA_CORPSE_MOB:
			{
				if ((!(target instanceof Attackable)) || !target.isDead())
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				if (onlyFirst)
					return new Creature[]
					{
						target
					};
				
				List<Creature> targetList = new ArrayList<>();
				targetList.add(target);
				
				final boolean srcInArena = activeChar.isInArena();
				
				for (Creature obj : activeChar.getKnownTypeInRadius(Creature.class, _skillRadius))
				{
					if (!(obj instanceof Attackable || obj instanceof Playable))
						continue;
					
					if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
						continue;
					
					targetList.add(obj);
				}
				
				if (targetList.isEmpty())
					return _emptyTargetList;
				
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_UNLOCKABLE:
			{
				if (!(target instanceof Door) && !(target instanceof Chest))
					return _emptyTargetList;
				
				return new Creature[]
				{
					target
				};
				
			}
			case TARGET_UNDEAD:
			{
				if (target instanceof Npc || target instanceof Servitor)
				{
					if (!target.isUndead() || target.isDead())
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
						return _emptyTargetList;
					}
					
					return new Creature[]
					{
						target
					};
				}
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return _emptyTargetList;
			}
			case TARGET_AURA_UNDEAD:
			{
				List<Creature> targetList = new ArrayList<>();
				
				for (Creature obj : activeChar.getKnownTypeInRadius(Creature.class, _skillRadius))
				{
					if (obj instanceof Npc || obj instanceof Servitor)
						target = obj;
					else
						continue;
					
					if (target.isAlikeDead() || !target.isUndead())
						continue;
					
					if (!GeoEngine.getInstance().canSeeTarget(activeChar, target))
						continue;
					
					if (onlyFirst)
						return new Creature[]
						{
							obj
						};
					
					targetList.add(obj);
				}
				
				if (targetList.isEmpty())
					return _emptyTargetList;
				
				return targetList.toArray(new Creature[targetList.size()]);
			}
			case TARGET_ENEMY_SUMMON:
			{
				if (target instanceof Summon)
				{
					final Summon targetSummon = (Summon) target;
					final Player summonOwner = targetSummon.getActingPlayer();
					
					if (activeChar instanceof Player && activeChar.getSummon() != targetSummon && !targetSummon.isDead() && (summonOwner.getPvpFlag() != 0 || summonOwner.getKarma() > 0) || (summonOwner.isInsideZone(ZoneId.PVP) && activeChar.isInsideZone(ZoneId.PVP)) || (summonOwner.isInDuel() && ((Player) activeChar).isInDuel() && summonOwner.getDuelId() == ((Player) activeChar).getDuelId()))
						return new Creature[]
						{
							targetSummon
						};
				}
				return _emptyTargetList;
			}
			default:
			{
				activeChar.sendMessage("Target type of skill is not currently handled");
				return _emptyTargetList;
			}
		}
	}
	
	public final WorldObject[] getTargetList(Creature activeChar)
	{
		return getTargetList(activeChar, false);
	}
	
	public final WorldObject getFirstOfTargetList(Creature activeChar)
	{
		WorldObject[] targets = getTargetList(activeChar, true);
		if (targets.length == 0)
			return null;
		
		return targets[0];
	}
	
	/**
	 * Check if target should be added to the target list.
	 * <ul>
	 * <li>Target is dead.</li>
	 * <li>Target same as caster.</li>
	 * <li>Target inside peace zone.</li>
	 * <li>Target in the same party than caster.</li>
	 * <li>Caster can see target.</li>
	 * </ul>
	 * Additional checks.
	 * <ul>
	 * <li>Mustn't be in Observer mode.</li>
	 * <li>If not in PvP zones (arena, siege): target in not the same clan and alliance with caster, and usual PvP skill check.</li>
	 * <li>In case caster and target are L2Attackable : verify if caster isn't confused.</li>
	 * </ul>
	 * Caution: distance is not checked.
	 * @param caster The skill caster.
	 * @param target The victim
	 * @param skill The skill to use.
	 * @param sourceInArena True means the caster is in a pvp or siege zone, and so the additional check will be skipped.
	 * @return
	 */
	public static final boolean checkForAreaOffensiveSkills(Creature caster, Creature target, L2Skill skill, boolean sourceInArena)
	{
		if (target == null || target.isDead() || target == caster)
			return false;
		
		final Player player = caster.getActingPlayer();
		final Player targetPlayer = target.getActingPlayer();
		if (player != null && targetPlayer != null)
		{
			if (targetPlayer == caster || targetPlayer == player)
				return false;
			
			if (targetPlayer.isInObserverMode())
				return false;
			
			if (skill.isOffensive() && player.getSiegeState() > 0 && player.isInsideZone(ZoneId.SIEGE) && player.getSiegeState() == targetPlayer.getSiegeState())
				return false;
			
			if (target.isInsideZone(ZoneId.PEACE))
				return false;
			
			if (player.isInParty() && targetPlayer.isInParty())
			{
				// Same party
				if (player.getParty().getLeaderObjectId() == targetPlayer.getParty().getLeaderObjectId())
					return false;
				
				// Same commandchannel
				if (player.getParty().getCommandChannel() != null && player.getParty().getCommandChannel() == targetPlayer.getParty().getCommandChannel())
					return false;
			}
			
			if (!sourceInArena && !(targetPlayer.isInsideZone(ZoneId.PVP) && !targetPlayer.isInsideZone(ZoneId.SIEGE)))
			{
				if (player.getAllyId() != 0 && player.getAllyId() == targetPlayer.getAllyId())
					return false;
				
				if (player.getClanId() != 0 && player.getClanId() == targetPlayer.getClanId())
					return false;
				
				if (!player.checkPvpSkill(targetPlayer, skill))
					return false;
			}
		}
		else if (target instanceof Attackable)
		{
			if (caster instanceof Attackable && !caster.isConfused())
				return false;
			
			if (skill.isOffensive() && !target.isAutoAttackable(caster))
				return false;
		}
		return GeoEngine.getInstance().canSeeTarget(caster, target);
	}
	
	public static final boolean addSummon(Creature caster, Player owner, int radius, boolean isDead)
	{
		final Summon summon = owner.getSummon();
		
		if (summon == null)
			return false;
		
		return addCharacter(caster, summon, radius, isDead);
	}
	
	public static final boolean addCharacter(Creature caster, Creature target, int radius, boolean isDead)
	{
		if (isDead != target.isDead())
			return false;
		
		if (radius > 0 && !MathUtil.checkIfInRange(radius, caster, target, true))
			return false;
		
		return true;
	}
	
	public final List<Func> getStatFuncs(Creature player)
	{
		if (_funcTemplates == null)
			return Collections.emptyList();
		
		if (!(player instanceof Playable) && !(player instanceof Attackable))
			return Collections.emptyList();
		
		final List<Func> funcs = new ArrayList<>(_funcTemplates.size());
		
		final Env env = new Env();
		env.setCharacter(player);
		env.setSkill(this);
		
		for (FuncTemplate t : _funcTemplates)
		{
			final Func f = t.getFunc(env, this); // skill is owner
			if (f != null)
				funcs.add(f);
		}
		return funcs;
	}
	
	public boolean hasEffects()
	{
		return (_effectTemplates != null && !_effectTemplates.isEmpty());
	}
	
	public List<EffectTemplate> getEffectTemplates()
	{
		return _effectTemplates;
	}
	
	public boolean hasSelfEffects()
	{
		return (_effectTemplatesSelf != null && !_effectTemplatesSelf.isEmpty());
	}
	
	/**
	 * @param effector
	 * @param effected
	 * @param env parameters for secondary effects (shield and ss/bss/bsss)
	 * @return an array with the effects that have been added to effector
	 */
	public final List<L2Effect> getEffects(Creature effector, Creature effected, Env env)
	{
		if (!hasEffects() || isPassive())
			return Collections.emptyList();
		
		// doors and siege flags cannot receive any effects
		if (effected instanceof Door || effected instanceof SiegeFlag)
			return Collections.emptyList();
		
		if (effector != effected)
		{
			if (isOffensive() || isDebuff())
			{
				if (effected.isInvul())
					return Collections.emptyList();
				
				if (effector instanceof Player && ((Player) effector).isGM())
				{
					if (!((Player) effector).getAccessLevel().canGiveDamage())
						return Collections.emptyList();
				}
			}
		}
		
		final List<L2Effect> effects = new ArrayList<>(_effectTemplates.size());
		
		if (env == null)
			env = new Env();
		
		env.setSkillMastery(Formulas.calcSkillMastery(effector, this));
		env.setCharacter(effector);
		env.setTarget(effected);
		env.setSkill(this);
		
		for (EffectTemplate et : _effectTemplates)
		{
			boolean success = true;
			
			if (et.effectPower > -1)
				success = Formulas.calcEffectSuccess(effector, effected, et, this, env.getShield(), env.isBlessedSpiritShot());
			
			if (success)
			{
				final L2Effect e = et.getEffect(env);
				if (e != null)
				{
					e.scheduleEffect();
					effects.add(e);
				}
			}
			// display fail message only for effects with icons
			else if (et.icon && effector instanceof Player)
				((Player) effector).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(effected).addSkillName(this));
		}
		return effects;
	}
	
	/**
	 * Warning: this method doesn't consider modifier (shield, ss, sps, bss) for secondary effects
	 * @param effector
	 * @param effected
	 * @return An array of L2Effect.
	 */
	public final List<L2Effect> getEffects(Creature effector, Creature effected)
	{
		return getEffects(effector, effected, null);
	}
	
	/**
	 * This method has suffered some changes in CT2.2 ->CT2.3<br>
	 * Effect engine is now supporting secondary effects with independent success/fail calculus from effect skill. Env parameter has been added to pass parameters like soulshot, spiritshots, blessed spiritshots or shield deffence. Some other optimizations have been done <br>
	 * <br>
	 * This new feature works following next rules:
	 * <li>To enable feature, effectPower must be over -1 (check DocumentSkill#attachEffect for further information)</li>
	 * <li>If main skill fails, secondary effect always fail</li>
	 * @param effector
	 * @param effected
	 * @param env parameters for secondary effects (shield and ss/bss/bsss)
	 * @return An array of L2Effect.
	 */
	public final List<L2Effect> getEffects(Cubic effector, Creature effected, Env env)
	{
		if (!hasEffects() || isPassive())
			return Collections.emptyList();
		
		if (effector.getOwner() != effected)
		{
			if (isDebuff() || isOffensive())
			{
				if (effected.isInvul())
					return Collections.emptyList();
				
				if (effector.getOwner().isGM() && !effector.getOwner().getAccessLevel().canGiveDamage())
					return Collections.emptyList();
			}
		}
		
		final List<L2Effect> effects = new ArrayList<>(_effectTemplates.size());
		
		if (env == null)
			env = new Env();
		
		env.setCharacter(effector.getOwner());
		env.setCubic(effector);
		env.setTarget(effected);
		env.setSkill(this);
		
		for (EffectTemplate et : _effectTemplates)
		{
			boolean success = true;
			if (et.effectPower > -1)
				success = Formulas.calcEffectSuccess(effector.getOwner(), effected, et, this, env.getShield(), env.isBlessedSpiritShot());
			
			if (success)
			{
				final L2Effect e = et.getEffect(env);
				if (e != null)
				{
					e.scheduleEffect();
					effects.add(e);
				}
			}
		}
		return effects;
	}
	
	public final List<L2Effect> getEffectsSelf(Creature effector)
	{
		if (!hasSelfEffects() || isPassive())
			return Collections.emptyList();
		
		final List<L2Effect> effects = new ArrayList<>(_effectTemplatesSelf.size());
		
		final Env env = new Env();
		env.setCharacter(effector);
		env.setTarget(effector);
		env.setSkill(this);
		
		for (EffectTemplate et : _effectTemplatesSelf)
		{
			final L2Effect e = et.getEffect(env);
			if (e != null)
			{
				e.setSelfEffect();
				e.scheduleEffect();
				effects.add(e);
			}
		}
		return effects;
	}
	
	public final void attach(FuncTemplate f)
	{
		if (_funcTemplates == null)
			_funcTemplates = new ArrayList<>(1);
		
		_funcTemplates.add(f);
	}
	
	public final void attach(EffectTemplate effect)
	{
		if (_effectTemplates == null)
			_effectTemplates = new ArrayList<>(1);
		
		_effectTemplates.add(effect);
	}
	
	public final void attachSelf(EffectTemplate effect)
	{
		if (_effectTemplatesSelf == null)
			_effectTemplatesSelf = new ArrayList<>(1);
		
		_effectTemplatesSelf.add(effect);
	}
	
	public final void attach(Condition c, boolean itemOrWeapon)
	{
		if (itemOrWeapon)
		{
			if (_itemPreCondition == null)
				_itemPreCondition = new ArrayList<>();
			
			_itemPreCondition.add(c);
		}
		else
		{
			if (_preCondition == null)
				_preCondition = new ArrayList<>();
			
			_preCondition.add(c);
		}
	}
	
	/**
	 * @param skillId
	 * @param skillLvl
	 * @param values
	 * @return L2ExtractableSkill
	 * @author Zoey76
	 */
	private L2ExtractableSkill parseExtractableSkill(int skillId, int skillLvl, String values)
	{
		final String[] prodLists = values.split(";");
		final List<L2ExtractableProductItem> products = new ArrayList<>();
		
		for (String prodList : prodLists)
		{
			final String[] prodData = prodList.split(",");
			
			if (prodData.length < 3)
				_log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> wrong seperator!");
			
			final int lenght = prodData.length - 1;
			
			List<IntIntHolder> items = null;
			double chance = 0;
			int prodId = 0;
			int quantity = 0;
			
			try
			{
				items = new ArrayList<>(lenght / 2);
				for (int j = 0; j < lenght; j++)
				{
					prodId = Integer.parseInt(prodData[j]);
					quantity = Integer.parseInt(prodData[j += 1]);
					
					if (prodId <= 0 || quantity <= 0)
						_log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " wrong production Id: " + prodId + " or wrond quantity: " + quantity + "!");
					
					items.add(new IntIntHolder(prodId, quantity));
				}
				chance = Double.parseDouble(prodData[lenght]);
			}
			catch (Exception e)
			{
				_log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> incomplete/invalid production data or wrong seperator!");
			}
			products.add(new L2ExtractableProductItem(items, chance));
		}
		
		if (products.isEmpty())
			_log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> There are no production items!");
		
		return new L2ExtractableSkill(SkillTable.getSkillHashCode(this), products);
	}
	
	public L2ExtractableSkill getExtractableSkill()
	{
		return _extractableItems;
	}
	
	public boolean isDamage()
	{
		switch (_skillType)
		{
			case PDAM:
			case MDAM:
			case DRAIN:
			case BLOW:
			case CPDAMPERCENT:
			case FATAL:
				return true;
		}
		return false;
	}
	
	public boolean isAOE()
	{
		switch (_targetType)
		{
			case TARGET_AREA:
			case TARGET_AURA:
			case TARGET_BEHIND_AREA:
			case TARGET_BEHIND_AURA:
			case TARGET_FRONT_AREA:
			case TARGET_FRONT_AURA:
				return true;
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		return "" + _name + "[id=" + _id + ",lvl=" + _level + "]";
	}
}
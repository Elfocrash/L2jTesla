package dev.l2j.tesla.gameserver.model.item.kind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.l2j.tesla.gameserver.handler.ISkillHandler;
import dev.l2j.tesla.gameserver.handler.SkillHandler;
import dev.l2j.tesla.gameserver.model.L2Effect;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.skills.Env;
import dev.l2j.tesla.gameserver.skills.Formulas;
import dev.l2j.tesla.gameserver.skills.conditions.Condition;
import dev.l2j.tesla.gameserver.skills.conditions.ConditionGameChance;
import dev.l2j.tesla.commons.random.Rnd;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.enums.ScriptEventType;
import dev.l2j.tesla.gameserver.enums.items.WeaponType;
import dev.l2j.tesla.gameserver.enums.skills.L2SkillType;

/**
 * This class is dedicated to the management of weapons.
 */
public final class Weapon extends Item
{
	private final WeaponType _type;
	private final int _rndDam;
	private final int _soulShotCount;
	private final int _spiritShotCount;
	private final int _mpConsume;
	private final int _mpConsumeReduceRate;
	private final int _mpConsumeReduceValue;
	private final boolean _isMagical;
	
	private IntIntHolder _enchant4Skill = null; // skill that activates when item is enchanted +4 (for duals)
	
	// Attached skills for Special Abilities
	private IntIntHolder _skillsOnCast;
	private Condition _skillsOnCastCondition = null;
	private IntIntHolder _skillsOnCrit;
	private Condition _skillsOnCritCondition = null;
	
	private final int _reuseDelay;
	
	private final int _reducedSoulshot;
	private final int _reducedSoulshotChance;
	
	/**
	 * Constructor for Weapon.<BR>
	 * <BR>
	 * <U><I>Variables filled :</I></U>
	 * <UL>
	 * <LI>_soulShotCount & _spiritShotCount</LI>
	 * <LI>_pDam & _mDam & _rndDam</LI>
	 * <LI>_critical</LI>
	 * <LI>_hitModifier</LI>
	 * <LI>_avoidModifier</LI>
	 * <LI>_shieldDes & _shieldDefRate</LI>
	 * <LI>_atkSpeed & _AtkReuse</LI>
	 * <LI>_mpConsume</LI>
	 * <LI>_isMagical</LI>
	 * </UL>
	 * @param set : StatsSet designating the set of couples (key,value) caracterizing the armor
	 * @see Item constructor
	 */
	public Weapon(StatsSet set)
	{
		super(set);
		_type = WeaponType.valueOf(set.getString("weapon_type", "none").toUpperCase());
		_type1 = Item.TYPE1_WEAPON_RING_EARRING_NECKLACE;
		_type2 = Item.TYPE2_WEAPON;
		_soulShotCount = set.getInteger("soulshots", 0);
		_spiritShotCount = set.getInteger("spiritshots", 0);
		_rndDam = set.getInteger("random_damage", 0);
		_mpConsume = set.getInteger("mp_consume", 0);
		String[] reduce = set.getString("mp_consume_reduce", "0,0").split(",");
		_mpConsumeReduceRate = Integer.parseInt(reduce[0]);
		_mpConsumeReduceValue = Integer.parseInt(reduce[1]);
		_reuseDelay = set.getInteger("reuse_delay", 0);
		_isMagical = set.getBool("is_magical", false);
		
		String[] reduced_soulshots = set.getString("reduced_soulshot", "").split(",");
		_reducedSoulshotChance = (reduced_soulshots.length == 2) ? Integer.parseInt(reduced_soulshots[0]) : 0;
		_reducedSoulshot = (reduced_soulshots.length == 2) ? Integer.parseInt(reduced_soulshots[1]) : 0;
		
		String skill = set.getString("enchant4_skill", null);
		if (skill != null)
		{
			String[] info = skill.split("-");
			
			if (info != null && info.length == 2)
			{
				int id = 0;
				int level = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
				}
				catch (Exception nfe)
				{
					// Incorrect syntax, dont add new skill
					_log.info("> Couldnt parse " + skill + " in weapon enchant skills! item " + toString());
				}
				if (id > 0 && level > 0)
					_enchant4Skill = new IntIntHolder(id, level);
			}
		}
		
		skill = set.getString("oncast_skill", null);
		if (skill != null)
		{
			String[] info = skill.split("-");
			String infochance = set.getString("oncast_chance", null);
			if (info != null && info.length == 2)
			{
				int id = 0;
				int level = 0;
				int chance = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
					if (infochance != null)
						chance = Integer.parseInt(infochance);
				}
				catch (Exception nfe)
				{
					// Incorrect syntax, dont add new skill
					_log.info("> Couldnt parse " + skill + " in weapon oncast skills! item " + toString());
				}
				if (id > 0 && level > 0 && chance > 0)
				{
					_skillsOnCast = new IntIntHolder(id, level);
					if (infochance != null)
						_skillsOnCastCondition = new ConditionGameChance(chance);
				}
			}
		}
		
		skill = set.getString("oncrit_skill", null);
		if (skill != null)
		{
			String[] info = skill.split("-");
			String infochance = set.getString("oncrit_chance", null);
			if (info != null && info.length == 2)
			{
				int id = 0;
				int level = 0;
				int chance = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
					if (infochance != null)
						chance = Integer.parseInt(infochance);
				}
				catch (Exception nfe)
				{
					// Incorrect syntax, dont add new skill
					_log.info("> Couldnt parse " + skill + " in weapon oncrit skills! item " + toString());
				}
				if (id > 0 && level > 0 && chance > 0)
				{
					_skillsOnCrit = new IntIntHolder(id, level);
					if (infochance != null)
						_skillsOnCritCondition = new ConditionGameChance(chance);
				}
			}
		}
	}
	
	/**
	 * @return the type of weapon.
	 */
	@Override
	public WeaponType getItemType()
	{
		return _type;
	}
	
	/**
	 * @return the ID of the Etc item after applying the mask.
	 */
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * @return the quantity of SoulShot used.
	 */
	public int getSoulShotCount()
	{
		return _soulShotCount;
	}
	
	/**
	 * @return the quatity of SpiritShot used.
	 */
	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}
	
	/**
	 * @return the reduced quantity of SoultShot used.
	 */
	public int getReducedSoulShot()
	{
		return _reducedSoulshot;
	}
	
	/**
	 * @return the chance to use Reduced SoultShot.
	 */
	public int getReducedSoulShotChance()
	{
		return _reducedSoulshotChance;
	}
	
	/**
	 * @return the random damage inflicted by the weapon
	 */
	public int getRandomDamage()
	{
		return _rndDam;
	}
	
	/**
	 * @return the Reuse Delay of the Weapon.
	 */
	public int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	/**
	 * @return true or false if weapon is considered as a mage weapon.
	 */
	public final boolean isMagical()
	{
		return _isMagical;
	}
	
	/**
	 * @return the MP consumption of the weapon.
	 */
	public int getMpConsume()
	{
		if (_mpConsumeReduceRate > 0 && Rnd.get(100) < _mpConsumeReduceRate)
			return _mpConsumeReduceValue;
		
		return _mpConsume;
	}
	
	/**
	 * @return The skill player obtains when he equiped weapon +4 or more (for duals SA)
	 */
	public L2Skill getEnchant4Skill()
	{
		if (_enchant4Skill == null)
			return null;
		
		return _enchant4Skill.getSkill();
	}
	
	/**
	 * @param caster : Creature pointing out the caster
	 * @param target : Creature pointing out the target
	 * @param crit : boolean tells whether the hit was critical
	 * @return An array of L2Effect of skills associated with the item to be triggered onHit.
	 */
	public List<L2Effect> getSkillEffects(Creature caster, Creature target, boolean crit)
	{
		if (_skillsOnCrit == null || !crit)
			return Collections.emptyList();
		
		final List<L2Effect> effects = new ArrayList<>();
		
		if (_skillsOnCritCondition != null)
		{
			final Env env = new Env();
			env.setCharacter(caster);
			env.setTarget(target);
			env.setSkill(_skillsOnCrit.getSkill());
			
			if (!_skillsOnCritCondition.test(env))
				return Collections.emptyList();
		}
		
		final byte shld = Formulas.calcShldUse(caster, target, _skillsOnCrit.getSkill());
		if (!Formulas.calcSkillSuccess(caster, target, _skillsOnCrit.getSkill(), shld, false))
			return Collections.emptyList();
		
		if (target.getFirstEffect(_skillsOnCrit.getSkill().getId()) != null)
			target.getFirstEffect(_skillsOnCrit.getSkill().getId()).exit();
		
		for (L2Effect e : _skillsOnCrit.getSkill().getEffects(caster, target, new Env(shld, false, false, false)))
			effects.add(e);
		
		return effects;
	}
	
	/**
	 * @param caster : Creature pointing out the caster
	 * @param target : Creature pointing out the target
	 * @param trigger : L2Skill pointing out the skill triggering this action
	 * @return An array of L2Effect associated with the item to be triggered onCast.
	 */
	public List<L2Effect> getSkillEffects(Creature caster, Creature target, L2Skill trigger)
	{
		if (_skillsOnCast == null)
			return Collections.emptyList();
		
		// Trigger only same type of skill.
		if (trigger.isOffensive() != _skillsOnCast.getSkill().isOffensive())
			return Collections.emptyList();
		
		// No buffing with toggle or not magic skills.
		if ((trigger.isToggle() || !trigger.isMagic()) && _skillsOnCast.getSkill().getSkillType() == L2SkillType.BUFF)
			return Collections.emptyList();
		
		if (_skillsOnCastCondition != null)
		{
			final Env env = new Env();
			env.setCharacter(caster);
			env.setTarget(target);
			env.setSkill(_skillsOnCast.getSkill());
			
			if (!_skillsOnCastCondition.test(env))
				return Collections.emptyList();
		}
		
		final byte shld = Formulas.calcShldUse(caster, target, _skillsOnCast.getSkill());
		if (_skillsOnCast.getSkill().isOffensive() && !Formulas.calcSkillSuccess(caster, target, _skillsOnCast.getSkill(), shld, false))
			return Collections.emptyList();
		
		// Get the skill handler corresponding to the skill type
		ISkillHandler handler = SkillHandler.getInstance().getHandler(_skillsOnCast.getSkill().getSkillType());
		
		Creature[] targets = new Creature[1];
		targets[0] = target;
		
		// Launch the magic skill and calculate its effects
		if (handler != null)
			handler.useSkill(caster, _skillsOnCast.getSkill(), targets);
		else
			_skillsOnCast.getSkill().useSkill(caster, targets);
		
		// notify quests of a skill use
		if (caster instanceof Player)
		{
			// Mobs in range 1000 see spell
			for (Npc npcMob : caster.getKnownTypeInRadius(Npc.class, 1000))
			{
				final List<Quest> scripts = npcMob.getTemplate().getEventQuests(ScriptEventType.ON_SKILL_SEE);
				if (scripts != null)
					for (Quest quest : scripts)
						quest.notifySkillSee(npcMob, (Player) caster, _skillsOnCast.getSkill(), targets, false);
			}
		}
		return Collections.emptyList();
	}
}
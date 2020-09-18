package dev.l2j.tesla.gameserver.model.actor.instance;

import java.util.List;
import java.util.concurrent.Future;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcSay;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.IntentionType;

/**
 * A tamed beast behaves a lot like a pet and has an owner. Some points :
 * <ul>
 * <li>feeding another beast to level 4 will vanish your actual tamed beast.</li>
 * <li>running out of spices will vanish your actual tamed beast. There's a 1min food check timer.</li>
 * <li>running out of the Beast Farm perimeter will vanish your tamed beast.</li>
 * <li>no need to force attack it, it's a normal monster.</li>
 * </ul>
 * This class handles the running tasks (such as skills use and feed) of the mob.
 */
public final class TamedBeast extends FeedableBeast
{
	private static final int MAX_DISTANCE_FROM_HOME = 13000;
	private static final int TASK_INTERVAL = 5000;
	
	// Messages used every minute by the tamed beast when he automatically eats food.
	protected static final String[] FOOD_CHAT =
	{
		"Refills! Yeah!",
		"I am such a gluttonous beast, it is embarrassing! Ha ha.",
		"Your cooperative feeling has been getting better and better.",
		"I will help you!",
		"The weather is really good. Wanna go for a picnic?",
		"I really like you! This is tasty...",
		"If you do not have to leave this place, then I can help you.",
		"What can I help you with?",
		"I am not here only for food!",
		"Yam, yam, yam, yam, yam!"
	};
	
	protected int _foodId;
	protected Player _owner;
	
	private Future<?> _aiTask = null;
	
	public TamedBeast(int objectId, NpcTemplate template, Player owner, int foodId, Location loc)
	{
		super(objectId, template);
		
		disableCoreAI(true);
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
		setTitle(owner.getName());
		
		_owner = owner;
		owner.setTrainedBeast(this);
		
		_foodId = foodId;
		
		// Generate AI task.
		_aiTask = ThreadPool.scheduleAtFixedRate(new AiTask(), TASK_INTERVAL, TASK_INTERVAL);
		
		spawnMe(loc);
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		// Stop AI task.
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
			_aiTask = null;
		}
		
		// Clean up actual trained beast.
		if (_owner != null)
			_owner.setTrainedBeast(null);
		
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		// Stop AI task.
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
			_aiTask = null;
		}
		
		stopHpMpRegeneration();
		getAI().stopFollow();
		
		// Clean up actual trained beast.
		if (_owner != null)
			_owner.setTrainedBeast(null);
		
		super.deleteMe();
	}
	
	/**
	 * Notification triggered by the owner when the owner is attacked.<br>
	 * Tamed mobs will heal/recharge or debuff the enemy according to their skills.
	 * @param attacker
	 */
	public void onOwnerGotAttacked(Creature attacker)
	{
		// Check if the owner is no longer around. If so, despawn.
		if (_owner == null || !_owner.isOnline())
		{
			deleteMe();
			return;
		}
		
		// If the owner is dead or if the tamed beast is currently casting a spell,do nothing.
		if (_owner.isDead() || isCastingNow())
			return;
		
		final int proba = Rnd.get(3);
		
		// Heal, 33% luck.
		if (proba == 0)
		{
			// Happen only when owner's HPs < 50%
			float HPRatio = ((float) _owner.getCurrentHp()) / _owner.getMaxHp();
			if (HPRatio < 0.5)
			{
				for (L2Skill skill : getTemplate().getSkills(NpcTemplate.SkillType.HEAL))
				{
					switch (skill.getSkillType())
					{
						case HEAL:
						case HOT:
						case BALANCE_LIFE:
						case HEAL_PERCENT:
						case HEAL_STATIC:
							sitCastAndFollow(skill, _owner);
							return;
					}
				}
			}
		}
		// Debuff, 33% luck.
		else if (proba == 1)
		{
			for (L2Skill skill : getTemplate().getSkills(NpcTemplate.SkillType.DEBUFF))
			{
				// if the skill is a debuff, check if the attacker has it already
				if (attacker.getFirstEffect(skill) == null)
				{
					sitCastAndFollow(skill, attacker);
					return;
				}
			}
		}
		// Recharge, 33% luck.
		else if (proba == 2)
		{
			// Happen only when owner's MPs < 50%
			float MPRatio = ((float) _owner.getCurrentMp()) / _owner.getMaxMp();
			if (MPRatio < 0.5)
			{
				for (L2Skill skill : getTemplate().getSkills(NpcTemplate.SkillType.HEAL))
				{
					switch (skill.getSkillType())
					{
						case MANARECHARGE:
						case MANAHEAL_PERCENT:
							sitCastAndFollow(skill, _owner);
							return;
					}
				}
			}
		}
	}
	
	/**
	 * Prepare and cast a skill:
	 * <ul>
	 * <li>First, prepare the beast for casting, by abandoning other actions.</li>
	 * <li>Next, call doCast in order to cast the spell.</li>
	 * <li>Finally, return to auto-following the owner.</li>
	 * </ul>
	 * @param skill The skill to cast.
	 * @param target The benefactor of the skill.
	 */
	protected void sitCastAndFollow(L2Skill skill, Creature target)
	{
		stopMove(null);
		getAI().setIntention(IntentionType.IDLE);
		
		setTarget(target);
		doCast(skill);
		getAI().setIntention(IntentionType.FOLLOW, _owner);
	}
	
	private class AiTask implements Runnable
	{
		private int _step;
		
		public AiTask()
		{
		}
		
		@Override
		public void run()
		{
			final Player owner = _owner;
			
			// Check if the owner is no longer around. If so, despawn.
			if (owner == null || !owner.isOnline())
			{
				deleteMe();
				return;
			}
			
			// Happens every 60s.
			if (++_step > 12)
			{
				// Verify first if the tamed beast is still in the good range. If not, delete it.
				if (!isInsideRadius(52335, -83086, MAX_DISTANCE_FROM_HOME, true))
				{
					deleteMe();
					return;
				}
				
				// Destroy the food from owner's inventory ; if none is found, delete the pet.
				if (!owner.destroyItemByItemId("BeastMob", _foodId, 1, TamedBeast.this, true))
				{
					deleteMe();
					return;
				}
				
				broadcastPacket(new SocialAction(TamedBeast.this, 2));
				broadcastPacket(new NpcSay(getObjectId(), 0, getNpcId(), Rnd.get(FOOD_CHAT)));
				
				_step = 0;
			}
			
			// If the owner is dead or if the tamed beast is currently casting a spell,do nothing.
			if (owner.isDead() || isCastingNow())
				return;
			
			int totalBuffsOnOwner = 0;
			int i = 0;
			L2Skill buffToGive = null;
			
			final List<L2Skill> skills = getTemplate().getSkills(NpcTemplate.SkillType.BUFF);
			final int rand = Rnd.get(skills.size());
			
			// Retrieve the random buff, and check how much tamed beast buffs the player has.
			for (L2Skill skill : skills)
			{
				if (i == rand)
					buffToGive = skill;
				
				i++;
				
				if (owner.getFirstEffect(skill) != null)
					totalBuffsOnOwner++;
			}
			
			// If the owner has less than 2 buffs, cast the chosen buff.
			if (totalBuffsOnOwner < 2 && owner.getFirstEffect(buffToGive) == null)
				sitCastAndFollow(buffToGive, owner);
			else
				getAI().setIntention(IntentionType.FOLLOW, owner);
		}
	}
}
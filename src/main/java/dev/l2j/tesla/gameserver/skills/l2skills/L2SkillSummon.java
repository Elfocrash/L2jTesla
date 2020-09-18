package dev.l2j.tesla.gameserver.skills.l2skills;

import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.idfactory.IdFactory;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.data.xml.NpcData;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.Cubic;
import dev.l2j.tesla.gameserver.model.actor.instance.Servitor;
import dev.l2j.tesla.gameserver.model.actor.instance.SiegeSummon;
import dev.l2j.tesla.gameserver.model.actor.player.Experience;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;

public class L2SkillSummon extends L2Skill
{
	public static final int SKILL_CUBIC_MASTERY = 143;
	
	private final int _npcId;
	private final float _expPenalty;
	private final boolean _isCubic;
	
	// Activation time for a cubic
	private final int _activationtime;
	// Activation chance for a cubic.
	private final int _activationchance;
	
	// What is the total lifetime of summons (in millisecs)
	private final int _summonTotalLifeTime;
	// How much lifetime is lost per second of idleness (non-fighting)
	private final int _summonTimeLostIdle;
	// How much time is lost per second of activity (fighting)
	private final int _summonTimeLostActive;
	
	// item consume time in milliseconds
	private final int _itemConsumeTime;
	// item consume count over time
	private final int _itemConsumeOT;
	// item consume id over time
	private final int _itemConsumeIdOT;
	// how many times to consume an item
	private final int _itemConsumeSteps;
	
	public L2SkillSummon(StatsSet set)
	{
		super(set);
		
		_npcId = set.getInteger("npcId", 0); // default for undescribed skills
		_expPenalty = set.getFloat("expPenalty", 0.f);
		_isCubic = set.getBool("isCubic", false);
		
		_activationtime = set.getInteger("activationtime", 8);
		_activationchance = set.getInteger("activationchance", 30);
		
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000); // 20 minutes default
		_summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
		_summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
		
		_itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
		_itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
		_itemConsumeTime = set.getInteger("itemConsumeTime", 0);
		_itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
	}
	
	public boolean checkCondition(Creature activeChar)
	{
		if (activeChar instanceof Player)
		{
			Player player = (Player) activeChar;
			
			if (isCubic())
			{
				// Player is always able to cast mass cubic skill
				if (getTargetType() != SkillTargetType.TARGET_SELF)
					return true;
				
				if (player.getCubics().size() > player.getSkillLevel(SKILL_CUBIC_MASTERY))
				{
					player.sendPacket(SystemMessageId.CUBIC_SUMMONING_FAILED);
					return false;
				}
			}
			else
			{
				if (player.isInObserverMode())
					return false;
				
				if (player.getSummon() != null)
				{
					player.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
					return false;
				}
			}
		}
		return super.checkCondition(activeChar, null, false);
	}
	
	@Override
	public void useSkill(Creature caster, WorldObject[] targets)
	{
		if (caster.isAlikeDead() || !(caster instanceof Player))
			return;
		
		Player activeChar = (Player) caster;
		
		if (_npcId == 0)
		{
			activeChar.sendMessage("Summon skill " + getId() + " not described yet");
			return;
		}
		
		if (_isCubic)
		{
			int _cubicSkillLevel = getLevel();
			if (_cubicSkillLevel > 100)
				_cubicSkillLevel = Math.round(((getLevel() - 100) / 7) + 8);
			
			if (targets.length > 1) // Mass cubic skill
			{
				for (WorldObject obj : targets)
				{
					if (!(obj instanceof Player))
						continue;
					
					Player player = ((Player) obj);
					
					final int mastery = player.getSkillLevel(SKILL_CUBIC_MASTERY);
					
					// Player can have only 1 cubic if they don't own cubic mastery - we should replace old cubic with new one.
					if (mastery == 0 && !player.getCubics().isEmpty())
					{
						for (Cubic c : player.getCubics().values())
						{
							c.stopAction();
							c = null;
						}
						player.getCubics().clear();
					}
					
					if (player.getCubics().containsKey(_npcId))
					{
						Cubic cubic = player.getCubic(_npcId);
						cubic.stopAction();
						cubic.cancelDisappear();
						player.delCubic(_npcId);
					}
					
					if (player.getCubics().size() > mastery)
						continue;
					
					if (player == activeChar)
						player.addCubic(_npcId, _cubicSkillLevel, getPower(), _activationtime, _activationchance, _summonTotalLifeTime, false);
					else
						// given by other player
						player.addCubic(_npcId, _cubicSkillLevel, getPower(), _activationtime, _activationchance, _summonTotalLifeTime, true);
					
					player.broadcastUserInfo();
				}
				return;
			}
			
			if (activeChar.getCubics().containsKey(_npcId))
			{
				Cubic cubic = activeChar.getCubic(_npcId);
				cubic.stopAction();
				cubic.cancelDisappear();
				activeChar.delCubic(_npcId);
			}
			
			if (activeChar.getCubics().size() > activeChar.getSkillLevel(SKILL_CUBIC_MASTERY))
			{
				activeChar.sendPacket(SystemMessageId.CUBIC_SUMMONING_FAILED);
				return;
			}
			
			activeChar.addCubic(_npcId, _cubicSkillLevel, getPower(), _activationtime, _activationchance, _summonTotalLifeTime, false);
			activeChar.broadcastUserInfo();
			return;
		}
		
		if (activeChar.getSummon() != null || activeChar.isMounted())
			return;
		
		Servitor summon;
		NpcTemplate summonTemplate = NpcData.getInstance().getTemplate(_npcId);
		if (summonTemplate == null)
		{
			_log.warning("Summon attempt for nonexisting NPC ID: " + _npcId + ", skill ID: " + getId());
			return;
		}
		
		if (summonTemplate.isType("SiegeSummon"))
			summon = new SiegeSummon(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
		else
			summon = new Servitor(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
		
		summon.setName(summonTemplate.getName());
		summon.setTitle(activeChar.getName());
		summon.setExpPenalty(_expPenalty);
		
		if (summon.getLevel() >= Experience.LEVEL.length)
		{
			summon.getStat().setExp(Experience.LEVEL[Experience.LEVEL.length - 1]);
			_log.warning("Summon (" + summon.getName() + ") NpcID: " + summon.getNpcId() + " has a level above 75. Please rectify.");
		}
		else
			summon.getStat().setExp(Experience.LEVEL[(summon.getLevel() % Experience.LEVEL.length)]);
		
		summon.setCurrentHp(summon.getMaxHp());
		summon.setCurrentMp(summon.getMaxMp());
		summon.setRunning();
		activeChar.setSummon(summon);
		
		final int x = activeChar.getX();
		final int y = activeChar.getY();
		final int z = activeChar.getZ();
		
		summon.spawnMe(GeoEngine.getInstance().canMoveToTargetLoc(x, y, z, x + 20, y + 20, z), activeChar.getHeading());
		summon.setFollowStatus(true);
	}
	
	public final boolean isCubic()
	{
		return _isCubic;
	}
	
	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}
	
	public final int getTimeLostIdle()
	{
		return _summonTimeLostIdle;
	}
	
	public final int getTimeLostActive()
	{
		return _summonTimeLostActive;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getItemConsumeOT()
	{
		return _itemConsumeOT;
	}
	
	/**
	 * @return Returns the itemConsumeId over time.
	 */
	public final int getItemConsumeIdOT()
	{
		return _itemConsumeIdOT;
	}
	
	public final int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}
	
	/**
	 * @return Returns the itemConsume time in milliseconds.
	 */
	public final int getItemConsumeTime()
	{
		return _itemConsumeTime;
	}
}
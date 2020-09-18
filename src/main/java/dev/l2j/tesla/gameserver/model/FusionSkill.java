package dev.l2j.tesla.gameserver.model;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.skills.effects.EffectFusion;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.math.MathUtil;

import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.model.actor.Creature;

/**
 * @author kombat, Forsaiken
 */
public final class FusionSkill
{
	protected static final Logger _log = Logger.getLogger(FusionSkill.class.getName());
	
	protected int _skillCastRange;
	protected int _fusionId;
	protected int _fusionLevel;
	protected Creature _caster;
	protected Creature _target;
	protected Future<?> _geoCheckTask;
	
	public Creature getCaster()
	{
		return _caster;
	}
	
	public Creature getTarget()
	{
		return _target;
	}
	
	public FusionSkill(Creature caster, Creature target, L2Skill skill)
	{
		_skillCastRange = skill.getCastRange();
		_caster = caster;
		_target = target;
		_fusionId = skill.getTriggeredId();
		_fusionLevel = skill.getTriggeredLevel();
		
		L2Effect effect = _target.getFirstEffect(_fusionId);
		if (effect != null)
			((EffectFusion) effect).increaseEffect();
		else
		{
			L2Skill force = SkillTable.getInstance().getInfo(_fusionId, _fusionLevel);
			if (force != null)
				force.getEffects(_caster, _target, null);
			else
				_log.warning("Triggered skill [" + _fusionId + ";" + _fusionLevel + "] not found!");
		}
		_geoCheckTask = ThreadPool.scheduleAtFixedRate(new GeoCheckTask(), 1000, 1000);
	}
	
	public void onCastAbort()
	{
		_caster.setFusionSkill(null);
		L2Effect effect = _target.getFirstEffect(_fusionId);
		if (effect != null)
			((EffectFusion) effect).decreaseForce();
		
		_geoCheckTask.cancel(true);
	}
	
	public class GeoCheckTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (!MathUtil.checkIfInRange(_skillCastRange, _caster, _target, true))
					_caster.abortCast();
				
				if (!GeoEngine.getInstance().canSeeTarget(_caster, _target))
					_caster.abortCast();
			}
			catch (Exception e)
			{
				// ignore
			}
		}
	}
}
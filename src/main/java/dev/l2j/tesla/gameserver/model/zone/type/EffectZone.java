package dev.l2j.tesla.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.model.zone.ZoneType;
import dev.l2j.tesla.gameserver.network.serverpackets.EtcStatusUpdate;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.ZoneId;

/**
 * A zone extending {@link ZoneType}, which fires a task on the first character entrance.<br>
 * <br>
 * This task launches skill effects on all characters within this zone, and can affect specific class types. It can also be activated or desactivated. The zone is considered a danger zone.
 */
public class EffectZone extends ZoneType
{
	private final List<IntIntHolder> _skills = new ArrayList<>(5);
	
	private int _chance = 100;
	private int _initialDelay = 0;
	private int _reuseDelay = 30000;
	
	private boolean _isEnabled = true;
	
	private Future<?> _task;
	private String _target = "Playable";
	
	public EffectZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("chance"))
			_chance = Integer.parseInt(value);
		else if (name.equals("initialDelay"))
			_initialDelay = Integer.parseInt(value);
		else if (name.equals("reuseDelay"))
			_reuseDelay = Integer.parseInt(value);
		else if (name.equals("defaultStatus"))
			_isEnabled = Boolean.parseBoolean(value);
		else if (name.equals("skill"))
		{
			final String[] skills = value.split(";");
			for (String skill : skills)
			{
				final String[] skillSplit = skill.split("-");
				if (skillSplit.length != 2)
					LOGGER.warn("Invalid skill format {} for {}.", skill, toString());
				else
				{
					try
					{
						_skills.add(new IntIntHolder(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1])));
					}
					catch (NumberFormatException nfe)
					{
						LOGGER.warn("Invalid skill format {} for {}.", skill, toString());
					}
				}
			}
		}
		else if (name.equals("targetType"))
			_target = value;
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected boolean isAffected(Creature character)
	{
		try
		{
			if (!(Class.forName("dev.l2j.tesla.gameserver.model.actor." + _target).isInstance(character)))
				return false;
		}
		catch (ClassNotFoundException e)
		{
			LOGGER.error("Error for {} on invalid target type {}.", e, toString(), _target);
		}
		return true;
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (_task == null)
		{
			synchronized (this)
			{
				if (_task == null)
					_task = ThreadPool.scheduleAtFixedRate(() ->
					{
						if (!_isEnabled)
							return;
						
						if (_characters.isEmpty())
						{
							_task.cancel(true);
							_task = null;
							
							return;
						}
						
						for (Creature temp : _characters.values())
						{
							if (temp.isDead() || Rnd.get(100) >= _chance)
								continue;
							
							for (IntIntHolder entry : _skills)
							{
								final L2Skill skill = entry.getSkill();
								if (skill != null && skill.checkCondition(temp, temp, false) && temp.getFirstEffect(entry.getId()) == null)
									skill.getEffects(temp, temp);
							}
						}
					}, _initialDelay, _reuseDelay);
			}
		}
		
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.DANGER_AREA, true);
			character.sendPacket(new EtcStatusUpdate((Player) character));
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.DANGER_AREA, false);
			
			if (!character.isInsideZone(ZoneId.DANGER_AREA))
				character.sendPacket(new EtcStatusUpdate((Player) character));
		}
	}
	
	/**
	 * Edit this zone activation state.
	 * @param state : The new state to set.
	 */
	public void editStatus(boolean state)
	{
		_isEnabled = state;
	}
}
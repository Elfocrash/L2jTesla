package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

/**
 * The old "MagicEffectIcons" packet format h (dhd)
 */
public class AbnormalStatusUpdate extends L2GameServerPacket
{
	private final List<Effect> _effects;
	
	private static class Effect
	{
		protected int _skillId;
		protected int _level;
		protected int _duration;
		
		public Effect(int pSkillId, int pLevel, int pDuration)
		{
			_skillId = pSkillId;
			_level = pLevel;
			_duration = pDuration;
		}
	}
	
	public AbnormalStatusUpdate()
	{
		_effects = new ArrayList<>();
	}
	
	public void addEffect(int skillId, int level, int duration)
	{
		_effects.add(new Effect(skillId, level, duration));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7f);
		
		writeH(_effects.size());
		
		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level);
			
			if (temp._duration == -1)
				writeD(-1);
			else
				writeD(temp._duration / 1000);
		}
	}
}
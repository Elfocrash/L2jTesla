package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.instance.Pet;
import dev.l2j.tesla.gameserver.model.actor.instance.Servitor;

public class PartySpelled extends L2GameServerPacket
{
	private final List<Effect> _effects;
	private final Creature _activeChar;
	
	private class Effect
	{
		protected int _skillId;
		protected int _dat;
		protected int _duration;
		
		public Effect(int pSkillId, int pDat, int pDuration)
		{
			_skillId = pSkillId;
			_dat = pDat;
			_duration = pDuration;
		}
	}
	
	public PartySpelled(Creature cha)
	{
		_effects = new ArrayList<>();
		_activeChar = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_activeChar == null)
			return;
		
		writeC(0xee);
		writeD(_activeChar instanceof Servitor ? 2 : _activeChar instanceof Pet ? 1 : 0);
		writeD(_activeChar.getObjectId());
		writeD(_effects.size());
		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._dat);
			writeD(temp._duration / 1000);
		}
	}
	
	public void addPartySpelledEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}
}
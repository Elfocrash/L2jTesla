package dev.l2j.tesla.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.enums.skills.L2SkillType;
import dev.l2j.tesla.gameserver.handler.skillhandlers.BalanceLife;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Blow;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Cancel;
import dev.l2j.tesla.gameserver.handler.skillhandlers.CombatPointHeal;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Continuous;
import dev.l2j.tesla.gameserver.handler.skillhandlers.CpDamPercent;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Craft;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Disablers;
import dev.l2j.tesla.gameserver.handler.skillhandlers.DrainSoul;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Dummy;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Extractable;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Fishing;
import dev.l2j.tesla.gameserver.handler.skillhandlers.FishingSkill;
import dev.l2j.tesla.gameserver.handler.skillhandlers.GetPlayer;
import dev.l2j.tesla.gameserver.handler.skillhandlers.GiveSp;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Harvest;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Heal;
import dev.l2j.tesla.gameserver.handler.skillhandlers.HealPercent;
import dev.l2j.tesla.gameserver.handler.skillhandlers.InstantJump;
import dev.l2j.tesla.gameserver.handler.skillhandlers.ManaHeal;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Manadam;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Mdam;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Pdam;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Resurrect;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Sow;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Spoil;
import dev.l2j.tesla.gameserver.handler.skillhandlers.StrSiegeAssault;
import dev.l2j.tesla.gameserver.handler.skillhandlers.SummonFriend;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Sweep;
import dev.l2j.tesla.gameserver.handler.skillhandlers.TakeCastle;
import dev.l2j.tesla.gameserver.handler.skillhandlers.Unlock;

public class SkillHandler
{
	private final Map<Integer, ISkillHandler> _entries = new HashMap<>();
	
	protected SkillHandler()
	{
		registerHandler(new BalanceLife());
		registerHandler(new Blow());
		registerHandler(new Cancel());
		registerHandler(new CombatPointHeal());
		registerHandler(new Continuous());
		registerHandler(new CpDamPercent());
		registerHandler(new Craft());
		registerHandler(new Disablers());
		registerHandler(new DrainSoul());
		registerHandler(new Dummy());
		registerHandler(new Extractable());
		registerHandler(new Fishing());
		registerHandler(new FishingSkill());
		registerHandler(new GetPlayer());
		registerHandler(new GiveSp());
		registerHandler(new Harvest());
		registerHandler(new Heal());
		registerHandler(new HealPercent());
		registerHandler(new InstantJump());
		registerHandler(new Manadam());
		registerHandler(new ManaHeal());
		registerHandler(new Mdam());
		registerHandler(new Pdam());
		registerHandler(new Resurrect());
		registerHandler(new Sow());
		registerHandler(new Spoil());
		registerHandler(new StrSiegeAssault());
		registerHandler(new SummonFriend());
		registerHandler(new Sweep());
		registerHandler(new TakeCastle());
		registerHandler(new Unlock());
	}
	
	private void registerHandler(ISkillHandler handler)
	{
		for (L2SkillType t : handler.getSkillIds())
			_entries.put(t.ordinal(), handler);
	}
	
	public ISkillHandler getHandler(L2SkillType skillType)
	{
		return _entries.get(skillType.ordinal());
	}
	
	public int size()
	{
		return _entries.size();
	}
	
	public static SkillHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillHandler INSTANCE = new SkillHandler();
	}
}
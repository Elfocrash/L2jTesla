package dev.l2j.tesla.gameserver.scripting.scripts.ai.group;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import dev.l2j.tesla.gameserver.enums.ScriptEventType;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;

/**
 * Angel spawns... When one of the angels in the keys dies, the other angel will spawn.
 */
public class PolymorphingAngel extends L2AttackableAIScript
{
	private static final Map<Integer, Integer> ANGELSPAWNS = new HashMap<>();
	
	static
	{
		ANGELSPAWNS.put(20830, 20859);
		ANGELSPAWNS.put(21067, 21068);
		ANGELSPAWNS.put(21062, 21063);
		ANGELSPAWNS.put(20831, 20860);
		ANGELSPAWNS.put(21070, 21071);
	}
	
	public PolymorphingAngel()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(ANGELSPAWNS.keySet(), ScriptEventType.ON_KILL);
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Attackable newNpc = (Attackable) addSpawn(ANGELSPAWNS.get(npc.getNpcId()), npc, false, 0, false);
		attack(newNpc, killer);
		
		return super.onKill(npc, killer);
	}
}
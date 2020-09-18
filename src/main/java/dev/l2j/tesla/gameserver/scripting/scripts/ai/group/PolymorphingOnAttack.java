package dev.l2j.tesla.gameserver.scripting.scripts.ai.group;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.ScriptEventType;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.network.clientpackets.Say2;
import dev.l2j.tesla.gameserver.network.serverpackets.CreatureSay;

public class PolymorphingOnAttack extends L2AttackableAIScript
{
	private static final Map<Integer, Integer[]> MOBSPAWNS = new HashMap<>();
	
	static
	{
		MOBSPAWNS.put(21258, new Integer[]
		{
			21259,
			100,
			100,
			-1
		}); // Fallen Orc Shaman -> Sharp Talon Tiger (always polymorphs)
		MOBSPAWNS.put(21261, new Integer[]
		{
			21262,
			100,
			20,
			0
		}); // Ol Mahum Transcender 1st stage
		MOBSPAWNS.put(21262, new Integer[]
		{
			21263,
			100,
			10,
			1
		}); // Ol Mahum Transcender 2nd stage
		MOBSPAWNS.put(21263, new Integer[]
		{
			21264,
			100,
			5,
			2
		}); // Ol Mahum Transcender 3rd stage
		MOBSPAWNS.put(21265, new Integer[]
		{
			21271,
			100,
			33,
			0
		}); // Cave Ant Larva -> Cave Ant
		MOBSPAWNS.put(21266, new Integer[]
		{
			21269,
			100,
			100,
			-1
		}); // Cave Ant Larva -> Cave Ant (always polymorphs)
		MOBSPAWNS.put(21267, new Integer[]
		{
			21270,
			100,
			100,
			-1
		}); // Cave Ant Larva -> Cave Ant Soldier (always polymorphs)
		MOBSPAWNS.put(21271, new Integer[]
		{
			21272,
			66,
			10,
			1
		}); // Cave Ant -> Cave Ant Soldier
		MOBSPAWNS.put(21272, new Integer[]
		{
			21273,
			33,
			5,
			2
		}); // Cave Ant Soldier -> Cave Noble Ant
		MOBSPAWNS.put(21521, new Integer[]
		{
			21522,
			100,
			30,
			-1
		}); // Claws of Splendor
		MOBSPAWNS.put(21527, new Integer[]
		{
			21528,
			100,
			30,
			-1
		}); // Anger of Splendor
		MOBSPAWNS.put(21533, new Integer[]
		{
			21534,
			100,
			30,
			-1
		}); // Alliance of Splendor
		MOBSPAWNS.put(21537, new Integer[]
		{
			21538,
			100,
			30,
			-1
		}); // Fang of Splendor
	}
	
	private static final String[][] MOBTEXTS =
	{
		new String[]
		{
			"Enough fooling around. Get ready to die!",
			"You idiot! I've just been toying with you!",
			"Now the fun starts!"
		},
		new String[]
		{
			"I must admit, no one makes my blood boil quite like you do!",
			"Now the battle begins!",
			"Witness my true power!"
		},
		new String[]
		{
			"Prepare to die!",
			"I'll double my strength!",
			"You have more skill than I thought"
		}
	};
	
	public PolymorphingOnAttack()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(MOBSPAWNS.keySet(), ScriptEventType.ON_ATTACK);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (npc.isVisible() && !npc.isDead())
		{
			final Integer[] tmp = MOBSPAWNS.get(npc.getNpcId());
			if (tmp != null)
			{
				if (npc.getCurrentHp() <= (npc.getMaxHp() * tmp[1] / 100.0) && Rnd.get(100) < tmp[2])
				{
					if (tmp[3] >= 0)
					{
						String text = Rnd.get(MOBTEXTS[tmp[3]]);
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), text));
					}
					npc.deleteMe();
					
					Attackable newNpc = (Attackable) addSpawn(tmp[0], npc, false, 0, true);
					attack(newNpc, attacker);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
}
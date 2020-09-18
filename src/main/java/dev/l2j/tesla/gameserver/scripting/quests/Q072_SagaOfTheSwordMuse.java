package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q072_SagaOfTheSwordMuse extends SagasSuperClass
{
	public Q072_SagaOfTheSwordMuse()
	{
		super(72, "Saga of the Sword Muse");
		
		NPC = new int[]
		{
			30853,
			31624,
			31583,
			31537,
			31618,
			31646,
			31649,
			31652,
			31654,
			31655,
			31659,
			31281
		};
		
		Items = new int[]
		{
			7080,
			7536,
			7081,
			7487,
			7270,
			7301,
			7332,
			7363,
			7394,
			7425,
			7095,
			6482
		};
		
		Mob = new int[]
		{
			27288,
			27221,
			27280
		};
		
		classid = 100;
		prevclass = 0x15;
		
		X = new int[]
		{
			161719,
			124355,
			124376
		};
		
		Y = new int[]
		{
			-92823,
			82155,
			82127
		};
		
		Z = new int[]
		{
			-1893,
			-2803,
			-2796
		};
		
		registerNPCs();
	}
}
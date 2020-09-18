package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q076_SagaOfTheGrandKhavatari extends SagasSuperClass
{
	public Q076_SagaOfTheGrandKhavatari()
	{
		super(76, "Saga of the Grand Khavatari");
		
		NPC = new int[]
		{
			31339,
			31624,
			31589,
			31290,
			31637,
			31646,
			31647,
			31652,
			31654,
			31655,
			31659,
			31290
		};
		
		Items = new int[]
		{
			7080,
			7539,
			7081,
			7491,
			7274,
			7305,
			7336,
			7367,
			7398,
			7429,
			7099,
			0
		};
		
		Mob = new int[]
		{
			27293,
			27226,
			27284
		};
		
		classid = 114;
		prevclass = 0x30;
		
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
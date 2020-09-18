package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q078_SagaOfTheDoomcryer extends SagasSuperClass
{
	public Q078_SagaOfTheDoomcryer()
	{
		super(78, "Saga of the Doomcryer");
		
		NPC = new int[]
		{
			31336,
			31624,
			31589,
			31290,
			31642,
			31646,
			31649,
			31650,
			31654,
			31655,
			31657,
			31290
		};
		
		Items = new int[]
		{
			7080,
			7539,
			7081,
			7493,
			7276,
			7307,
			7338,
			7369,
			7400,
			7431,
			7101,
			0
		};
		
		Mob = new int[]
		{
			27295,
			27227,
			27285
		};
		
		classid = 116;
		prevclass = 0x34;
		
		X = new int[]
		{
			191046,
			46087,
			46066
		};
		
		Y = new int[]
		{
			-40640,
			-36372,
			-36396
		};
		
		Z = new int[]
		{
			-3042,
			-1685,
			-1685
		};
		
		registerNPCs();
	}
}
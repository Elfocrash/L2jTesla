package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q077_SagaOfTheDominator extends SagasSuperClass
{
	public Q077_SagaOfTheDominator()
	{
		super(77, "Saga of the Dominator");
		
		NPC = new int[]
		{
			31336,
			31624,
			31371,
			31290,
			31636,
			31646,
			31648,
			31653,
			31654,
			31655,
			31656,
			31290
		};
		
		Items = new int[]
		{
			7080,
			7539,
			7081,
			7492,
			7275,
			7306,
			7337,
			7368,
			7399,
			7430,
			7100,
			0
		};
		
		Mob = new int[]
		{
			27294,
			27226,
			27262
		};
		
		classid = 115;
		prevclass = 0x33;
		
		X = new int[]
		{
			164650,
			47429,
			47391
		};
		
		Y = new int[]
		{
			-74121,
			-56923,
			-56929
		};
		
		Z = new int[]
		{
			-2871,
			-2383,
			-2370
		};
		
		registerNPCs();
	}
}
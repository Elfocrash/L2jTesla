package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q095_SagaOfTheHellKnight extends SagasSuperClass
{
	public Q095_SagaOfTheHellKnight()
	{
		super(95, "Saga of the Hell Knight");
		
		NPC = new int[]
		{
			31582,
			31623,
			31297,
			31297,
			31599,
			31646,
			31647,
			31653,
			31654,
			31655,
			31656,
			31297
		};
		
		Items = new int[]
		{
			7080,
			7532,
			7081,
			7510,
			7293,
			7324,
			7355,
			7386,
			7417,
			7448,
			7086,
			0
		};
		
		Mob = new int[]
		{
			27258,
			27244,
			27263
		};
		
		classid = 91;
		prevclass = 0x06;
		
		X = new int[]
		{
			164650,
			47391,
			47429
		};
		
		Y = new int[]
		{
			-74121,
			-56929,
			-56923
		};
		
		Z = new int[]
		{
			-2871,
			-2370,
			-2383
		};
		
		registerNPCs();
	}
}
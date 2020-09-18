package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q100_SagaOfTheMaestro extends SagasSuperClass
{
	public Q100_SagaOfTheMaestro()
	{
		super(100, "Saga of the Maestro");
		
		NPC = new int[]
		{
			31592,
			31273,
			31597,
			31597,
			31596,
			31646,
			31648,
			31653,
			31654,
			31655,
			31656,
			31597
		};
		
		Items = new int[]
		{
			7080,
			7607,
			7081,
			7515,
			7298,
			7329,
			7360,
			7391,
			7422,
			7453,
			7108,
			0
		};
		
		Mob = new int[]
		{
			27260,
			27249,
			27308
		};
		
		classid = 118;
		prevclass = 0x39;
		
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
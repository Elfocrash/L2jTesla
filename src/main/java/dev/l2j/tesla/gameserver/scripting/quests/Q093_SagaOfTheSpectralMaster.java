package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q093_SagaOfTheSpectralMaster extends SagasSuperClass
{
	public Q093_SagaOfTheSpectralMaster()
	{
		super(93, "Saga of the Spectral Master");
		
		NPC = new int[]
		{
			30175,
			31287,
			31613,
			30175,
			31632,
			31646,
			31649,
			31653,
			31654,
			31655,
			31656,
			31613
		};
		
		Items = new int[]
		{
			7080,
			7606,
			7081,
			7508,
			7291,
			7322,
			7353,
			7384,
			7415,
			7446,
			7112,
			0
		};
		
		Mob = new int[]
		{
			27315,
			27242,
			27312
		};
		
		classid = 111;
		prevclass = 0x29;
		
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
package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q097_SagaOfTheShillienTemplar extends SagasSuperClass
{
	public Q097_SagaOfTheShillienTemplar()
	{
		super(97, "Saga of the Shillien Templar");
		
		NPC = new int[]
		{
			31580,
			31623,
			31285,
			31285,
			31610,
			31646,
			31648,
			31652,
			31654,
			31655,
			31659,
			31285
		};
		
		Items = new int[]
		{
			7080,
			7526,
			7081,
			7512,
			7295,
			7326,
			7357,
			7388,
			7419,
			7450,
			7091,
			0
		};
		
		Mob = new int[]
		{
			27271,
			27246,
			27273
		};
		
		classid = 106;
		prevclass = 0x21;
		
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
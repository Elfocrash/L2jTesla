package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q074_SagaOfTheDreadnought extends SagasSuperClass
{
	public Q074_SagaOfTheDreadnought()
	{
		super(74, "Saga of the Dreadnought");
		
		NPC = new int[]
		{
			30850,
			31624,
			31298,
			31276,
			31595,
			31646,
			31648,
			31650,
			31654,
			31655,
			31657,
			31522
		};
		
		Items = new int[]
		{
			7080,
			7538,
			7081,
			7489,
			7272,
			7303,
			7334,
			7365,
			7396,
			7427,
			7097,
			6480
		};
		
		Mob = new int[]
		{
			27290,
			27223,
			27282
		};
		
		classid = 89;
		prevclass = 0x03;
		
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
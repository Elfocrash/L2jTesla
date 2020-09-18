package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q088_SagaOfTheArchmage extends SagasSuperClass
{
	public Q088_SagaOfTheArchmage()
	{
		super(88, "Saga of the Archmage");
		
		NPC = new int[]
		{
			30176,
			31627,
			31282,
			31282,
			31590,
			31646,
			31647,
			31650,
			31654,
			31655,
			31657,
			31282
		};
		
		Items = new int[]
		{
			7080,
			7529,
			7081,
			7503,
			7286,
			7317,
			7348,
			7379,
			7410,
			7441,
			7082,
			0
		};
		
		Mob = new int[]
		{
			27250,
			27237,
			27254
		};
		
		classid = 94;
		prevclass = 0x0c;
		
		X = new int[]
		{
			191046,
			46066,
			46087
		};
		
		Y = new int[]
		{
			-40640,
			-36396,
			-36372
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
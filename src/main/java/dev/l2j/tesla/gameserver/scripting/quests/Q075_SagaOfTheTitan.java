package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q075_SagaOfTheTitan extends SagasSuperClass
{
	public Q075_SagaOfTheTitan()
	{
		super(75, "Saga of the Titan");
		
		NPC = new int[]
		{
			31327,
			31624,
			31289,
			31290,
			31607,
			31646,
			31649,
			31651,
			31654,
			31655,
			31658,
			31290
		};
		
		Items = new int[]
		{
			7080,
			7539,
			7081,
			7490,
			7273,
			7304,
			7335,
			7366,
			7397,
			7428,
			7098,
			0
		};
		
		Mob = new int[]
		{
			27292,
			27224,
			27283
		};
		
		classid = 113;
		prevclass = 0x2e;
		
		X = new int[]
		{
			119518,
			181215,
			181227
		};
		
		Y = new int[]
		{
			-28658,
			36676,
			36703
		};
		
		Z = new int[]
		{
			-3811,
			-4812,
			-4816
		};
		
		registerNPCs();
	}
}
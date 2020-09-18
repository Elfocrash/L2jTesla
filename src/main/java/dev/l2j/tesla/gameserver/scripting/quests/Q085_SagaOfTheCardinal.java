package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q085_SagaOfTheCardinal extends SagasSuperClass
{
	public Q085_SagaOfTheCardinal()
	{
		super(85, "Saga of the Cardinal");
		
		NPC = new int[]
		{
			30191,
			31626,
			31588,
			31280,
			31644,
			31646,
			31647,
			31651,
			31654,
			31655,
			31658,
			31280
		};
		
		Items = new int[]
		{
			7080,
			7522,
			7081,
			7500,
			7283,
			7314,
			7345,
			7376,
			7407,
			7438,
			7087,
			0
		};
		
		Mob = new int[]
		{
			27267,
			27234,
			27274
		};
		
		classid = 97;
		prevclass = 0x10;
		
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
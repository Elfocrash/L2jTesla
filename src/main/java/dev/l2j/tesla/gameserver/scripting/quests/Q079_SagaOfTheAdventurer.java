package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q079_SagaOfTheAdventurer extends SagasSuperClass
{
	public Q079_SagaOfTheAdventurer()
	{
		super(79, "Saga of the Adventurer");
		
		NPC = new int[]
		{
			31603,
			31584,
			31579,
			31615,
			31619,
			31646,
			31647,
			31651,
			31654,
			31655,
			31658,
			31616
		};
		
		Items = new int[]
		{
			7080,
			7516,
			7081,
			7494,
			7277,
			7308,
			7339,
			7370,
			7401,
			7432,
			7102,
			0
		};
		
		Mob = new int[]
		{
			27299,
			27228,
			27302
		};
		
		classid = 93;
		prevclass = 0x08;
		
		X = new int[]
		{
			119518,
			181205,
			181215
		};
		
		Y = new int[]
		{
			-28658,
			36676,
			36676
		};
		
		Z = new int[]
		{
			-3811,
			-4816,
			-4812
		};
		
		registerNPCs();
	}
}
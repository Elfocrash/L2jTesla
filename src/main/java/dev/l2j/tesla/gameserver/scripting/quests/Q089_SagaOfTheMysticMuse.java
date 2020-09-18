package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q089_SagaOfTheMysticMuse extends SagasSuperClass
{
	public Q089_SagaOfTheMysticMuse()
	{
		super(89, "Saga of the Mystic Muse");
		
		NPC = new int[]
		{
			30174,
			31627,
			31283,
			31283,
			31643,
			31646,
			31648,
			31651,
			31654,
			31655,
			31658,
			31283
		};
		
		Items = new int[]
		{
			7080,
			7530,
			7081,
			7504,
			7287,
			7318,
			7349,
			7380,
			7411,
			7442,
			7083,
			0
		};
		
		Mob = new int[]
		{
			27251,
			27238,
			27255
		};
		
		classid = 103;
		prevclass = 0x1b;
		
		X = new int[]
		{
			119518,
			181227,
			181215
		};
		
		Y = new int[]
		{
			-28658,
			36703,
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
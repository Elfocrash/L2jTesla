package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q083_SagaOfTheMoonlightSentinel extends SagasSuperClass
{
	public Q083_SagaOfTheMoonlightSentinel()
	{
		super(83, "Saga of the Moonlight Sentinel");
		
		NPC = new int[]
		{
			30702,
			31627,
			31604,
			31640,
			31634,
			31646,
			31648,
			31652,
			31654,
			31655,
			31658,
			31641
		};
		
		Items = new int[]
		{
			7080,
			7520,
			7081,
			7498,
			7281,
			7312,
			7343,
			7374,
			7405,
			7436,
			7106,
			0
		};
		
		Mob = new int[]
		{
			27297,
			27232,
			27306
		};
		
		classid = 102;
		prevclass = 0x18;
		
		X = new int[]
		{
			161719,
			181227,
			181215
		};
		
		Y = new int[]
		{
			-92823,
			36703,
			36676
		};
		
		Z = new int[]
		{
			-1893,
			-4816,
			-4812
		};
		
		registerNPCs();
	}
}
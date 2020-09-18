package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q080_SagaOfTheWindRider extends SagasSuperClass
{
	public Q080_SagaOfTheWindRider()
	{
		super(80, "Saga of the Wind Rider");
		
		NPC = new int[]
		{
			31603,
			31624,
			31284,
			31615,
			31612,
			31646,
			31648,
			31652,
			31654,
			31655,
			31659,
			31616
		};
		
		Items = new int[]
		{
			7080,
			7517,
			7081,
			7495,
			7278,
			7309,
			7340,
			7371,
			7402,
			7433,
			7103,
			0
		};
		
		Mob = new int[]
		{
			27300,
			27229,
			27303
		};
		
		classid = 101;
		prevclass = 0x17;
		
		X = new int[]
		{
			161719,
			124314,
			124355
		};
		
		Y = new int[]
		{
			-92823,
			82155,
			82155
		};
		
		Z = new int[]
		{
			-1893,
			-2803,
			-2803
		};
		
		registerNPCs();
	}
}
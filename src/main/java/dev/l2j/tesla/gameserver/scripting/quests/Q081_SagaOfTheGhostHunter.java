package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q081_SagaOfTheGhostHunter extends SagasSuperClass
{
	public Q081_SagaOfTheGhostHunter()
	{
		super(81, "Saga of the Ghost Hunter");
		
		NPC = new int[]
		{
			31603,
			31624,
			31286,
			31615,
			31617,
			31646,
			31649,
			31653,
			31654,
			31655,
			31656,
			31616
		};
		
		Items = new int[]
		{
			7080,
			7518,
			7081,
			7496,
			7279,
			7310,
			7341,
			7372,
			7403,
			7434,
			7104,
			0
		};
		
		Mob = new int[]
		{
			27301,
			27230,
			27304
		};
		
		classid = 108;
		prevclass = 0x24;
		
		X = new int[]
		{
			164650,
			47391,
			47429
		};
		
		Y = new int[]
		{
			-74121,
			-56929,
			-56923
		};
		
		Z = new int[]
		{
			-2871,
			-2370,
			-2383
		};
		
		registerNPCs();
	}
}
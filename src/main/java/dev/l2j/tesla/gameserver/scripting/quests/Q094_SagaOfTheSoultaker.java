package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q094_SagaOfTheSoultaker extends SagasSuperClass
{
	public Q094_SagaOfTheSoultaker()
	{
		super(94, "Saga of the Soultaker");
		
		NPC = new int[]
		{
			30832,
			31623,
			31279,
			31279,
			31645,
			31646,
			31648,
			31650,
			31654,
			31655,
			31657,
			31279
		};
		
		Items = new int[]
		{
			7080,
			7533,
			7081,
			7509,
			7292,
			7323,
			7354,
			7385,
			7416,
			7447,
			7085,
			0
		};
		
		Mob = new int[]
		{
			27257,
			27243,
			27265
		};
		
		classid = 95;
		prevclass = 0x0d;
		
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
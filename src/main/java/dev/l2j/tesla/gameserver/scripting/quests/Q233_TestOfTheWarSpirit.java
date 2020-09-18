package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q233_TestOfTheWarSpirit extends Quest
{
	private static final String qn = "Q233_TestOfTheWarSpirit";
	
	// Items
	private static final int VENDETTA_TOTEM = 2880;
	private static final int TAMLIN_ORC_HEAD = 2881;
	private static final int WARSPIRIT_TOTEM = 2882;
	private static final int ORIM_CONTRACT = 2883;
	private static final int PORTA_EYE = 2884;
	private static final int EXCURO_SCALE = 2885;
	private static final int MORDEO_TALON = 2886;
	private static final int BRAKI_REMAINS_1 = 2887;
	private static final int PEKIRON_TOTEM = 2888;
	private static final int TONAR_SKULL = 2889;
	private static final int TONAR_RIBBONE = 2890;
	private static final int TONAR_SPINE = 2891;
	private static final int TONAR_ARMBONE = 2892;
	private static final int TONAR_THIGHBONE = 2893;
	private static final int TONAR_REMAINS_1 = 2894;
	private static final int MANAKIA_TOTEM = 2895;
	private static final int HERMODT_SKULL = 2896;
	private static final int HERMODT_RIBBONE = 2897;
	private static final int HERMODT_SPINE = 2898;
	private static final int HERMODT_ARMBONE = 2899;
	private static final int HERMODT_THIGHBONE = 2900;
	private static final int HERMODT_REMAINS_1 = 2901;
	private static final int RACOY_TOTEM = 2902;
	private static final int VIVYAN_LETTER = 2903;
	private static final int INSECT_DIAGRAM_BOOK = 2904;
	private static final int KIRUNA_SKULL = 2905;
	private static final int KIRUNA_RIBBONE = 2906;
	private static final int KIRUNA_SPINE = 2907;
	private static final int KIRUNA_ARMBONE = 2908;
	private static final int KIRUNA_THIGHBONE = 2909;
	private static final int KIRUNA_REMAINS_1 = 2910;
	private static final int BRAKI_REMAINS_2 = 2911;
	private static final int TONAR_REMAINS_2 = 2912;
	private static final int HERMODT_REMAINS_2 = 2913;
	private static final int KIRUNA_REMAINS_2 = 2914;
	
	// Rewards
	private static final int MARK_OF_WARSPIRIT = 2879;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int VIVYAN = 30030;
	private static final int SARIEN = 30436;
	private static final int RACOY = 30507;
	private static final int SOMAK = 30510;
	private static final int MANAKIA = 30515;
	private static final int ORIM = 30630;
	private static final int ANCESTOR_MARTANKUS = 30649;
	private static final int PEKIRON = 30682;
	
	// Monsters
	private static final int NOBLE_ANT = 20089;
	private static final int NOBLE_ANT_LEADER = 20090;
	private static final int MEDUSA = 20158;
	private static final int PORTA = 20213;
	private static final int EXCURO = 20214;
	private static final int MORDEO = 20215;
	private static final int LETO_LIZARDMAN_SHAMAN = 20581;
	private static final int LETO_LIZARDMAN_OVERLORD = 20582;
	private static final int TAMLIN_ORC = 20601;
	private static final int TAMLIN_ORC_ARCHER = 20602;
	private static final int STENOA_GORGON_QUEEN = 27108;
	
	public Q233_TestOfTheWarSpirit()
	{
		super(233, "Test of the War Spirit");
		
		setItemsIds(VENDETTA_TOTEM, TAMLIN_ORC_HEAD, WARSPIRIT_TOTEM, ORIM_CONTRACT, PORTA_EYE, EXCURO_SCALE, MORDEO_TALON, BRAKI_REMAINS_1, PEKIRON_TOTEM, TONAR_SKULL, TONAR_RIBBONE, TONAR_SPINE, TONAR_ARMBONE, TONAR_THIGHBONE, TONAR_REMAINS_1, MANAKIA_TOTEM, HERMODT_SKULL, HERMODT_RIBBONE, HERMODT_SPINE, HERMODT_ARMBONE, HERMODT_THIGHBONE, HERMODT_REMAINS_1, RACOY_TOTEM, VIVYAN_LETTER, INSECT_DIAGRAM_BOOK, KIRUNA_SKULL, KIRUNA_RIBBONE, KIRUNA_SPINE, KIRUNA_ARMBONE, KIRUNA_THIGHBONE, KIRUNA_REMAINS_1, BRAKI_REMAINS_2, TONAR_REMAINS_2, HERMODT_REMAINS_2, KIRUNA_REMAINS_2);
		
		addStartNpc(SOMAK);
		addTalkId(SOMAK, VIVYAN, SARIEN, RACOY, MANAKIA, ORIM, ANCESTOR_MARTANKUS, PEKIRON);
		addKillId(NOBLE_ANT, NOBLE_ANT_LEADER, MEDUSA, PORTA, EXCURO, MORDEO, LETO_LIZARDMAN_SHAMAN, LETO_LIZARDMAN_OVERLORD, TAMLIN_ORC, TAMLIN_ORC_ARCHER, STENOA_GORGON_QUEEN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// SOMAK
		if (event.equalsIgnoreCase("30510-05.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			
			if (!player.getMemos().getBool("secondClassChange39", false))
			{
				htmltext = "30510-05e.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39.get(player.getClassId().getId()));
				player.getMemos().set("secondClassChange39", true);
			}
		}
		// ORIM
		else if (event.equalsIgnoreCase("30630-04.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(ORIM_CONTRACT, 1);
		}
		// RACOY
		else if (event.equalsIgnoreCase("30507-02.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(RACOY_TOTEM, 1);
		}
		// VIVYAN
		else if (event.equalsIgnoreCase("30030-04.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(VIVYAN_LETTER, 1);
		}
		// PEKIRON
		else if (event.equalsIgnoreCase("30682-02.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(PEKIRON_TOTEM, 1);
		}
		// MANAKIA
		else if (event.equalsIgnoreCase("30515-02.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(MANAKIA_TOTEM, 1);
		}
		// ANCESTOR MARTANKUS
		else if (event.equalsIgnoreCase("30649-03.htm"))
		{
			st.takeItems(TAMLIN_ORC_HEAD, -1);
			st.takeItems(WARSPIRIT_TOTEM, -1);
			st.takeItems(BRAKI_REMAINS_2, -1);
			st.takeItems(HERMODT_REMAINS_2, -1);
			st.takeItems(KIRUNA_REMAINS_2, -1);
			st.takeItems(TONAR_REMAINS_2, -1);
			st.giveItems(MARK_OF_WARSPIRIT, 1);
			st.rewardExpAndSp(63483, 17500);
			player.broadcastPacket(new SocialAction(player, 3));
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (player.getClassId() == ClassId.ORC_SHAMAN)
					htmltext = (player.getLevel() < 39) ? "30510-03.htm" : "30510-04.htm";
				else
					htmltext = (player.getRace() == ClassRace.ORC) ? "30510-02.htm" : "30510-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case SOMAK:
						if (cond == 1)
							htmltext = "30510-06.htm";
						else if (cond == 2)
						{
							htmltext = "30510-07.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BRAKI_REMAINS_1, 1);
							st.takeItems(HERMODT_REMAINS_1, 1);
							st.takeItems(KIRUNA_REMAINS_1, 1);
							st.takeItems(TONAR_REMAINS_1, 1);
							st.giveItems(VENDETTA_TOTEM, 1);
						}
						else if (cond == 3)
							htmltext = "30510-08.htm";
						else if (cond == 4)
						{
							htmltext = "30510-09.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(VENDETTA_TOTEM, 1);
							st.giveItems(BRAKI_REMAINS_2, 1);
							st.giveItems(HERMODT_REMAINS_2, 1);
							st.giveItems(KIRUNA_REMAINS_2, 1);
							st.giveItems(TONAR_REMAINS_2, 1);
							st.giveItems(WARSPIRIT_TOTEM, 1);
						}
						else if (cond == 5)
							htmltext = "30510-10.htm";
						break;
					
					case ORIM:
						if (cond == 1 && !st.hasQuestItems(BRAKI_REMAINS_1))
						{
							if (!st.hasQuestItems(ORIM_CONTRACT))
								htmltext = "30630-01.htm";
							else if (st.getQuestItemsCount(PORTA_EYE) + st.getQuestItemsCount(EXCURO_SCALE) + st.getQuestItemsCount(MORDEO_TALON) == 30)
							{
								htmltext = "30630-06.htm";
								st.takeItems(EXCURO_SCALE, 10);
								st.takeItems(MORDEO_TALON, 10);
								st.takeItems(PORTA_EYE, 10);
								st.takeItems(ORIM_CONTRACT, 1);
								st.giveItems(BRAKI_REMAINS_1, 1);
								
								if (st.hasQuestItems(HERMODT_REMAINS_1, KIRUNA_REMAINS_1, TONAR_REMAINS_1))
								{
									st.set("cond", "2");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
								htmltext = "30630-05.htm";
						}
						else
							htmltext = "30630-07.htm";
						break;
					
					case RACOY:
						if (cond == 1 && !st.hasQuestItems(KIRUNA_REMAINS_1))
						{
							if (!st.hasQuestItems(RACOY_TOTEM))
								htmltext = "30507-01.htm";
							else if (st.hasQuestItems(VIVYAN_LETTER))
								htmltext = "30507-04.htm";
							else if (st.hasQuestItems(INSECT_DIAGRAM_BOOK))
							{
								if (st.hasQuestItems(KIRUNA_ARMBONE, KIRUNA_RIBBONE, KIRUNA_SKULL, KIRUNA_SPINE, KIRUNA_THIGHBONE))
								{
									htmltext = "30507-06.htm";
									st.takeItems(INSECT_DIAGRAM_BOOK, 1);
									st.takeItems(RACOY_TOTEM, 1);
									st.takeItems(KIRUNA_ARMBONE, 1);
									st.takeItems(KIRUNA_RIBBONE, 1);
									st.takeItems(KIRUNA_SKULL, 1);
									st.takeItems(KIRUNA_SPINE, 1);
									st.takeItems(KIRUNA_THIGHBONE, 1);
									st.giveItems(KIRUNA_REMAINS_1, 1);
									
									if (st.hasQuestItems(BRAKI_REMAINS_1, HERMODT_REMAINS_1, TONAR_REMAINS_1))
									{
										st.set("cond", "2");
										st.playSound(QuestState.SOUND_MIDDLE);
									}
									else
										st.playSound(QuestState.SOUND_ITEMGET);
								}
								else
									htmltext = "30507-05.htm";
							}
							else
								htmltext = "30507-03.htm";
						}
						else
							htmltext = "30507-07.htm";
						break;
					
					case VIVYAN:
						if (cond == 1 && st.hasQuestItems(RACOY_TOTEM))
						{
							if (st.hasQuestItems(VIVYAN_LETTER))
								htmltext = "30030-05.htm";
							else if (st.hasQuestItems(INSECT_DIAGRAM_BOOK))
								htmltext = "30030-06.htm";
							else
								htmltext = "30030-01.htm";
						}
						else
							htmltext = "30030-07.htm";
						break;
					
					case SARIEN:
						if (cond == 1 && st.hasQuestItems(RACOY_TOTEM))
						{
							if (st.hasQuestItems(VIVYAN_LETTER))
							{
								htmltext = "30436-01.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(VIVYAN_LETTER, 1);
								st.giveItems(INSECT_DIAGRAM_BOOK, 1);
							}
							else if (st.hasQuestItems(INSECT_DIAGRAM_BOOK))
								htmltext = "30436-02.htm";
						}
						else
							htmltext = "30436-03.htm";
						break;
					
					case PEKIRON:
						if (cond == 1 && !st.hasQuestItems(TONAR_REMAINS_1))
						{
							if (!st.hasQuestItems(PEKIRON_TOTEM))
								htmltext = "30682-01.htm";
							else if (st.hasQuestItems(TONAR_ARMBONE, TONAR_RIBBONE, TONAR_SKULL, TONAR_SPINE, TONAR_THIGHBONE))
							{
								htmltext = "30682-04.htm";
								st.takeItems(PEKIRON_TOTEM, 1);
								st.takeItems(TONAR_ARMBONE, 1);
								st.takeItems(TONAR_RIBBONE, 1);
								st.takeItems(TONAR_SKULL, 1);
								st.takeItems(TONAR_SPINE, 1);
								st.takeItems(TONAR_THIGHBONE, 1);
								st.giveItems(TONAR_REMAINS_1, 1);
								
								if (st.hasQuestItems(BRAKI_REMAINS_1, HERMODT_REMAINS_1, KIRUNA_REMAINS_1))
								{
									st.set("cond", "2");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
								htmltext = "30682-03.htm";
						}
						else
							htmltext = "30682-05.htm";
						break;
					
					case MANAKIA:
						if (cond == 1 && !st.hasQuestItems(HERMODT_REMAINS_1))
						{
							if (!st.hasQuestItems(MANAKIA_TOTEM))
								htmltext = "30515-01.htm";
							else if (st.hasQuestItems(HERMODT_ARMBONE, HERMODT_RIBBONE, HERMODT_SKULL, HERMODT_SPINE, HERMODT_THIGHBONE))
							{
								htmltext = "30515-04.htm";
								st.takeItems(MANAKIA_TOTEM, 1);
								st.takeItems(HERMODT_ARMBONE, 1);
								st.takeItems(HERMODT_RIBBONE, 1);
								st.takeItems(HERMODT_SKULL, 1);
								st.takeItems(HERMODT_SPINE, 1);
								st.takeItems(HERMODT_THIGHBONE, 1);
								st.giveItems(HERMODT_REMAINS_1, 1);
								
								if (st.hasQuestItems(BRAKI_REMAINS_1, KIRUNA_REMAINS_1, TONAR_REMAINS_1))
								{
									st.set("cond", "2");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.playSound(QuestState.SOUND_ITEMGET);
							}
							else
								htmltext = "30515-03.htm";
						}
						else
							htmltext = "30515-05.htm";
						break;
					
					case ANCESTOR_MARTANKUS:
						if (cond == 5)
							htmltext = "30649-01.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		switch (npc.getNpcId())
		{
			case PORTA:
				if (st.hasQuestItems(ORIM_CONTRACT))
					st.dropItemsAlways(PORTA_EYE, 1, 10);
				break;
			
			case EXCURO:
				if (st.hasQuestItems(ORIM_CONTRACT))
					st.dropItemsAlways(EXCURO_SCALE, 1, 10);
				break;
			
			case MORDEO:
				if (st.hasQuestItems(ORIM_CONTRACT))
					st.dropItemsAlways(MORDEO_TALON, 1, 10);
				break;
			
			case NOBLE_ANT:
			case NOBLE_ANT_LEADER:
				if (st.hasQuestItems(INSECT_DIAGRAM_BOOK))
				{
					int rndAnt = Rnd.get(100);
					if (rndAnt > 70)
					{
						if (st.hasQuestItems(KIRUNA_THIGHBONE))
							st.dropItemsAlways(KIRUNA_ARMBONE, 1, 1);
						else
							st.dropItemsAlways(KIRUNA_THIGHBONE, 1, 1);
					}
					else if (rndAnt > 40)
					{
						if (st.hasQuestItems(KIRUNA_SPINE))
							st.dropItemsAlways(KIRUNA_RIBBONE, 1, 1);
						else
							st.dropItemsAlways(KIRUNA_SPINE, 1, 1);
					}
					else if (rndAnt > 10)
						st.dropItemsAlways(KIRUNA_SKULL, 1, 1);
				}
				break;
			
			case LETO_LIZARDMAN_SHAMAN:
			case LETO_LIZARDMAN_OVERLORD:
				if (st.hasQuestItems(PEKIRON_TOTEM) && Rnd.nextBoolean())
				{
					if (!st.hasQuestItems(TONAR_SKULL))
						st.dropItemsAlways(TONAR_SKULL, 1, 1);
					else if (!st.hasQuestItems(TONAR_RIBBONE))
						st.dropItemsAlways(TONAR_RIBBONE, 1, 1);
					else if (!st.hasQuestItems(TONAR_SPINE))
						st.dropItemsAlways(TONAR_SPINE, 1, 1);
					else if (!st.hasQuestItems(TONAR_ARMBONE))
						st.dropItemsAlways(TONAR_ARMBONE, 1, 1);
					else
						st.dropItemsAlways(TONAR_THIGHBONE, 1, 1);
				}
				break;
			
			case MEDUSA:
				if (st.hasQuestItems(MANAKIA_TOTEM) && Rnd.nextBoolean())
				{
					if (!st.hasQuestItems(HERMODT_RIBBONE))
						st.dropItemsAlways(HERMODT_RIBBONE, 1, 1);
					else if (!st.hasQuestItems(HERMODT_SPINE))
						st.dropItemsAlways(HERMODT_SPINE, 1, 1);
					else if (!st.hasQuestItems(HERMODT_ARMBONE))
						st.dropItemsAlways(HERMODT_ARMBONE, 1, 1);
					else
						st.dropItemsAlways(HERMODT_THIGHBONE, 1, 1);
				}
				break;
			
			case STENOA_GORGON_QUEEN:
				if (st.hasQuestItems(MANAKIA_TOTEM))
					st.dropItemsAlways(HERMODT_SKULL, 1, 1);
				break;
			
			case TAMLIN_ORC:
			case TAMLIN_ORC_ARCHER:
				if (st.hasQuestItems(VENDETTA_TOTEM) && st.dropItems(TAMLIN_ORC_HEAD, 1, 13, 500000))
					st.set("cond", "4");
				break;
		}
		
		return null;
	}
}
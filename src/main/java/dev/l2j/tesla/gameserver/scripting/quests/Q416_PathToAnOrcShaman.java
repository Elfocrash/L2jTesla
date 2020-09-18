package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q416_PathToAnOrcShaman extends Quest
{
	private static final String qn = "Q416_PathToAnOrcShaman";
	
	// Items
	private static final int FIRE_CHARM = 1616;
	private static final int KASHA_BEAR_PELT = 1617;
	private static final int KASHA_BLADE_SPIDER_HUSK = 1618;
	private static final int FIERY_EGG_1 = 1619;
	private static final int HESTUI_MASK = 1620;
	private static final int FIERY_EGG_2 = 1621;
	private static final int TOTEM_SPIRIT_CLAW = 1622;
	private static final int TATARU_LETTER = 1623;
	private static final int FLAME_CHARM = 1624;
	private static final int GRIZZLY_BLOOD = 1625;
	private static final int BLOOD_CAULDRON = 1626;
	private static final int SPIRIT_NET = 1627;
	private static final int BOUND_DURKA_SPIRIT = 1628;
	private static final int DURKA_PARASITE = 1629;
	private static final int TOTEM_SPIRIT_BLOOD = 1630;
	private static final int MASK_OF_MEDIUM = 1631;
	
	// NPCs
	private static final int TATARU_ZU_HESTUI = 30585;
	private static final int UMOS = 30502;
	private static final int HESTUI_TOTEM_SPIRIT = 30592;
	private static final int DUDA_MARA_TOTEM_SPIRIT = 30593;
	private static final int MOIRA = 31979;
	private static final int TOTEM_SPIRIT_OF_GANDI = 32057;
	private static final int DEAD_LEOPARD_CARCASS = 32090;
	
	// Monsters
	private static final int VENOMOUS_SPIDER = 20038;
	private static final int ARACHNID_TRACKER = 20043;
	private static final int GRIZZLY_BEAR = 20335;
	private static final int SCARLET_SALAMANDER = 20415;
	private static final int KASHA_BLADE_SPIDER = 20478;
	private static final int KASHA_BEAR = 20479;
	private static final int DURKA_SPIRIT = 27056;
	private static final int BLACK_LEOPARD = 27319;
	
	public Q416_PathToAnOrcShaman()
	{
		super(416, "Path To An Orc Shaman");
		
		setItemsIds(FIRE_CHARM, KASHA_BEAR_PELT, KASHA_BLADE_SPIDER_HUSK, FIERY_EGG_1, HESTUI_MASK, FIERY_EGG_2, TOTEM_SPIRIT_CLAW, TATARU_LETTER, FLAME_CHARM, GRIZZLY_BLOOD, BLOOD_CAULDRON, SPIRIT_NET, BOUND_DURKA_SPIRIT, DURKA_PARASITE, TOTEM_SPIRIT_BLOOD);
		
		addStartNpc(TATARU_ZU_HESTUI);
		addTalkId(TATARU_ZU_HESTUI, UMOS, HESTUI_TOTEM_SPIRIT, DUDA_MARA_TOTEM_SPIRIT, MOIRA, TOTEM_SPIRIT_OF_GANDI, DEAD_LEOPARD_CARCASS);
		
		addKillId(VENOMOUS_SPIDER, ARACHNID_TRACKER, GRIZZLY_BEAR, SCARLET_SALAMANDER, KASHA_BLADE_SPIDER, KASHA_BEAR, DURKA_SPIRIT, BLACK_LEOPARD);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// TATARU ZU HESTUI
		if (event.equalsIgnoreCase("30585-05.htm"))
		{
			if (player.getClassId() != ClassId.ORC_MYSTIC)
				htmltext = (player.getClassId() == ClassId.ORC_SHAMAN) ? "30585-02a.htm" : "30585-02.htm";
			else if (player.getLevel() < 19)
				htmltext = "30585-03.htm";
			else if (st.hasQuestItems(MASK_OF_MEDIUM))
				htmltext = "30585-04.htm";
		}
		else if (event.equalsIgnoreCase("30585-06.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(FIRE_CHARM, 1);
		}
		else if (event.equalsIgnoreCase("30585-11b.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(TOTEM_SPIRIT_CLAW, 1);
			st.giveItems(TATARU_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("30585-11c.htm"))
		{
			st.set("cond", "12");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(TOTEM_SPIRIT_CLAW, 1);
		}
		// HESTUI TOTEM SPIRIT
		else if (event.equalsIgnoreCase("30592-03.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(HESTUI_MASK, 1);
			st.takeItems(FIERY_EGG_2, 1);
			st.giveItems(TOTEM_SPIRIT_CLAW, 1);
		}
		// DUDA MARA TOTEM SPIRIT
		else if (event.equalsIgnoreCase("30593-03.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BLOOD_CAULDRON, 1);
			st.giveItems(SPIRIT_NET, 1);
		}
		// TOTEM SPIRIT OF GANDI
		else if (event.equalsIgnoreCase("32057-02.htm"))
		{
			st.set("cond", "14");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32057-05.htm"))
		{
			st.set("cond", "21");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		// DEAD LEOPARD CARCASS
		else if (event.equalsIgnoreCase("32090-04.htm"))
		{
			st.set("cond", "18");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		// UMOS
		else if (event.equalsIgnoreCase("30502-07.htm"))
		{
			st.takeItems(TOTEM_SPIRIT_BLOOD, -1);
			st.giveItems(MASK_OF_MEDIUM, 1);
			st.rewardExpAndSp(3200, 2600);
			player.broadcastPacket(new SocialAction(player, 3));
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				htmltext = "30585-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case TATARU_ZU_HESTUI:
						if (cond == 1)
							htmltext = "30585-07.htm";
						else if (cond == 2)
						{
							htmltext = "30585-08.htm";
							st.set("cond", "3");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(FIERY_EGG_1, 1);
							st.takeItems(FIRE_CHARM, 1);
							st.takeItems(KASHA_BEAR_PELT, 1);
							st.takeItems(KASHA_BLADE_SPIDER_HUSK, 1);
							st.giveItems(FIERY_EGG_2, 1);
							st.giveItems(HESTUI_MASK, 1);
						}
						else if (cond == 3)
							htmltext = "30585-09.htm";
						else if (cond == 4)
							htmltext = "30585-10.htm";
						else if (cond == 5)
							htmltext = "30585-12.htm";
						else if (cond > 5 && cond < 12)
							htmltext = "30585-13.htm";
						else if (cond == 12)
							htmltext = "30585-11c.htm";
						break;
					
					case HESTUI_TOTEM_SPIRIT:
						if (cond == 3)
							htmltext = "30592-01.htm";
						else if (cond == 4)
							htmltext = "30592-04.htm";
						else if (cond > 4 && cond < 12)
							htmltext = "30592-05.htm";
						break;
					
					case UMOS:
						if (cond == 5)
						{
							htmltext = "30502-01.htm";
							st.set("cond", "6");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(TATARU_LETTER, 1);
							st.giveItems(FLAME_CHARM, 1);
						}
						else if (cond == 6)
							htmltext = "30502-02.htm";
						else if (cond == 7)
						{
							htmltext = "30502-03.htm";
							st.set("cond", "8");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(FLAME_CHARM, 1);
							st.takeItems(GRIZZLY_BLOOD, 3);
							st.giveItems(BLOOD_CAULDRON, 1);
						}
						else if (cond == 8)
							htmltext = "30502-04.htm";
						else if (cond == 9 || cond == 10)
							htmltext = "30502-05.htm";
						else if (cond == 11)
							htmltext = "30502-06.htm";
						break;
					
					case MOIRA:
						if (cond == 12)
						{
							htmltext = "31979-01.htm";
							st.set("cond", "13");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond > 12 && cond < 21)
							htmltext = "31979-02.htm";
						else if (cond == 21)
						{
							htmltext = "31979-03.htm";
							st.giveItems(MASK_OF_MEDIUM, 1);
							st.rewardExpAndSp(3200, 3250);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case TOTEM_SPIRIT_OF_GANDI:
						if (cond == 13)
							htmltext = "32057-01.htm";
						else if (cond > 13 && cond < 20)
							htmltext = "32057-03.htm";
						else if (cond == 20)
							htmltext = "32057-04.htm";
						break;
					
					case DUDA_MARA_TOTEM_SPIRIT:
						if (cond == 8)
							htmltext = "30593-01.htm";
						else if (cond == 9)
							htmltext = "30593-04.htm";
						else if (cond == 10)
						{
							htmltext = "30593-05.htm";
							st.set("cond", "11");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BOUND_DURKA_SPIRIT, 1);
							st.giveItems(TOTEM_SPIRIT_BLOOD, 1);
						}
						else if (cond == 11)
							htmltext = "30593-06.htm";
						break;
					
					case DEAD_LEOPARD_CARCASS:
						if (cond == 14)
							htmltext = "32090-01a.htm";
						else if (cond == 15)
						{
							htmltext = "32090-01.htm";
							st.set("cond", "16");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 16)
							htmltext = "32090-01b.htm";
						else if (cond == 17)
							htmltext = "32090-02.htm";
						else if (cond == 18)
							htmltext = "32090-05.htm";
						else if (cond == 19)
						{
							htmltext = "32090-06.htm";
							st.set("cond", "20");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						break;
				}
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
		
		final int cond = st.getInt("cond");
		
		switch (npc.getNpcId())
		{
			case KASHA_BEAR:
				if (cond == 1 && !st.hasQuestItems(KASHA_BEAR_PELT))
				{
					st.giveItems(KASHA_BEAR_PELT, 1);
					if (st.hasQuestItems(FIERY_EGG_1, KASHA_BLADE_SPIDER_HUSK))
					{
						st.set("cond", "2");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case KASHA_BLADE_SPIDER:
				if (cond == 1 && !st.hasQuestItems(KASHA_BLADE_SPIDER_HUSK))
				{
					st.giveItems(KASHA_BLADE_SPIDER_HUSK, 1);
					if (st.hasQuestItems(KASHA_BEAR_PELT, FIERY_EGG_1))
					{
						st.set("cond", "2");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case SCARLET_SALAMANDER:
				if (cond == 1 && !st.hasQuestItems(FIERY_EGG_1))
				{
					st.giveItems(FIERY_EGG_1, 1);
					if (st.hasQuestItems(KASHA_BEAR_PELT, KASHA_BLADE_SPIDER_HUSK))
					{
						st.set("cond", "2");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case GRIZZLY_BEAR:
				if (cond == 6 && st.dropItemsAlways(GRIZZLY_BLOOD, 1, 3))
					st.set("cond", "7");
				break;
			
			case VENOMOUS_SPIDER:
			case ARACHNID_TRACKER:
				if (cond == 9)
				{
					final int count = st.getQuestItemsCount(DURKA_PARASITE);
					final int rnd = Rnd.get(10);
					if ((count == 5 && rnd < 1) || ((count == 6 || count == 7) && rnd < 2) || count >= 8)
					{
						st.playSound(QuestState.SOUND_BEFORE_BATTLE);
						st.takeItems(DURKA_PARASITE, -1);
						addSpawn(DURKA_SPIRIT, npc, false, 120000, true);
					}
					else
						st.dropItemsAlways(DURKA_PARASITE, 1, 0);
				}
				break;
			
			case DURKA_SPIRIT:
				if (cond == 9)
				{
					st.set("cond", "10");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(DURKA_PARASITE, -1);
					st.takeItems(SPIRIT_NET, 1);
					st.giveItems(BOUND_DURKA_SPIRIT, 1);
				}
				break;
			
			case BLACK_LEOPARD:
				if (cond == 14)
				{
					if (st.getInt("leopard") > 0)
					{
						st.set("cond", "15");
						st.playSound(QuestState.SOUND_MIDDLE);
						
						if (Rnd.get(3) < 2)
							npc.broadcastNpcSay("My dear friend of " + player.getName() + ", who has gone on ahead of me!");
					}
					else
						st.set("leopard", "1");
				}
				else if (cond == 16)
				{
					st.set("cond", "17");
					st.playSound(QuestState.SOUND_MIDDLE);
					
					if (Rnd.get(3) < 2)
						npc.broadcastNpcSay("Listen to Tejakar Gandi, young Oroka! The spirit of the slain leopard is calling you, " + player.getName() + "!");
				}
				else if (cond == 18)
				{
					st.set("cond", "19");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				break;
		}
		
		return null;
	}
}
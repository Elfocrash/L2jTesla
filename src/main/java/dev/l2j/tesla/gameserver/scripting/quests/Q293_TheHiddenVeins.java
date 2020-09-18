package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q293_TheHiddenVeins extends Quest
{
	private static final String qn = "Q293_TheHiddenVeins";
	
	// Items
	private static final int CHRYSOLITE_ORE = 1488;
	private static final int TORN_MAP_FRAGMENT = 1489;
	private static final int HIDDEN_VEIN_MAP = 1490;
	
	// Reward
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	
	// NPCs
	private static final int FILAUR = 30535;
	private static final int CHINCHIRIN = 30539;
	
	// Mobs
	private static final int UTUKU_ORC = 20446;
	private static final int UTUKU_ARCHER = 20447;
	private static final int UTUKU_GRUNT = 20448;
	
	public Q293_TheHiddenVeins()
	{
		super(293, "The Hidden Veins");
		
		setItemsIds(CHRYSOLITE_ORE, TORN_MAP_FRAGMENT, HIDDEN_VEIN_MAP);
		
		addStartNpc(FILAUR);
		addTalkId(FILAUR, CHINCHIRIN);
		
		addKillId(UTUKU_ORC, UTUKU_ARCHER, UTUKU_GRUNT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30535-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30535-06.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30539-02.htm"))
		{
			if (st.getQuestItemsCount(TORN_MAP_FRAGMENT) >= 4)
			{
				htmltext = "30539-03.htm";
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(TORN_MAP_FRAGMENT, 4);
				st.giveItems(HIDDEN_VEIN_MAP, 1);
			}
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
				if (player.getRace() != ClassRace.DWARF)
					htmltext = "30535-00.htm";
				else if (player.getLevel() < 6)
					htmltext = "30535-01.htm";
				else
					htmltext = "30535-02.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case FILAUR:
						final int chrysoliteOres = st.getQuestItemsCount(CHRYSOLITE_ORE);
						final int hiddenVeinMaps = st.getQuestItemsCount(HIDDEN_VEIN_MAP);
						
						if (chrysoliteOres + hiddenVeinMaps == 0)
							htmltext = "30535-04.htm";
						else
						{
							if (hiddenVeinMaps > 0)
							{
								if (chrysoliteOres > 0)
									htmltext = "30535-09.htm";
								else
									htmltext = "30535-08.htm";
							}
							else
								htmltext = "30535-05.htm";
							
							int reward = (chrysoliteOres * 5) + (hiddenVeinMaps * 500) + ((chrysoliteOres >= 10) ? 2000 : 0);
							
							st.takeItems(CHRYSOLITE_ORE, -1);
							st.takeItems(HIDDEN_VEIN_MAP, -1);
							st.rewardItems(57, reward);
							
							if (player.isNewbie() && st.getInt("Reward") == 0)
							{
								st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000);
								st.playTutorialVoice("tutorial_voice_026");
								st.set("Reward", "1");
							}
						}
						break;
					
					case CHINCHIRIN:
						htmltext = "30539-01.htm";
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
		
		final int chance = Rnd.get(100);
		
		if (chance > 50)
			st.dropItemsAlways(CHRYSOLITE_ORE, 1, 0);
		else if (chance < 5)
			st.dropItemsAlways(TORN_MAP_FRAGMENT, 1, 0);
		
		return null;
	}
}
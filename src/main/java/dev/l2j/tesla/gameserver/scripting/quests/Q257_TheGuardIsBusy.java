package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q257_TheGuardIsBusy extends Quest
{
	private static final String qn = "Q257_TheGuardIsBusy";
	
	// Items
	private static final int GLUDIO_LORD_MARK = 1084;
	private static final int ORC_AMULET = 752;
	private static final int ORC_NECKLACE = 1085;
	private static final int WEREWOLF_FANG = 1086;
	
	// Newbie Items
	private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	
	public Q257_TheGuardIsBusy()
	{
		super(257, "The Guard Is Busy");
		
		setItemsIds(ORC_AMULET, ORC_NECKLACE, WEREWOLF_FANG, GLUDIO_LORD_MARK);
		
		addStartNpc(30039); // Gilbert
		addTalkId(30039);
		
		addKillId(20006, 20093, 20096, 20098, 20130, 20131, 20132, 20342, 20343);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30039-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(GLUDIO_LORD_MARK, 1);
		}
		else if (event.equalsIgnoreCase("30039-05.htm"))
		{
			st.takeItems(GLUDIO_LORD_MARK, 1);
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
				htmltext = (player.getLevel() < 6) ? "30039-01.htm" : "30039-02.htm";
				break;
			
			case STATE_STARTED:
				final int amulets = st.getQuestItemsCount(ORC_AMULET);
				final int necklaces = st.getQuestItemsCount(ORC_NECKLACE);
				final int fangs = st.getQuestItemsCount(WEREWOLF_FANG);
				
				if (amulets + necklaces + fangs == 0)
					htmltext = "30039-04.htm";
				else
				{
					htmltext = "30039-07.htm";
					
					st.takeItems(ORC_AMULET, -1);
					st.takeItems(ORC_NECKLACE, -1);
					st.takeItems(WEREWOLF_FANG, -1);
					
					int reward = (10 * amulets) + 20 * (necklaces + fangs);
					if (amulets + necklaces + fangs >= 10)
						reward += 1000;
					
					st.rewardItems(57, reward);
					
					if (player.isNewbie() && st.getInt("Reward") == 0)
					{
						st.showQuestionMark(26);
						st.set("Reward", "1");
						
						if (player.isMageClass())
						{
							st.playTutorialVoice("tutorial_voice_027");
							st.giveItems(SPIRITSHOT_FOR_BEGINNERS, 3000);
						}
						else
						{
							st.playTutorialVoice("tutorial_voice_026");
							st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000);
						}
					}
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
		
		switch (npc.getNpcId())
		{
			case 20006:
			case 20130:
			case 20131:
				st.dropItems(ORC_AMULET, 1, 0, 500000);
				break;
			
			case 20093:
			case 20096:
			case 20098:
				st.dropItems(ORC_NECKLACE, 1, 0, 500000);
				break;
			
			case 20342:
				st.dropItems(WEREWOLF_FANG, 1, 0, 200000);
				break;
			
			case 20343:
				st.dropItems(WEREWOLF_FANG, 1, 0, 400000);
				break;
			
			case 20132:
				st.dropItems(WEREWOLF_FANG, 1, 0, 500000);
				break;
		}
		
		return null;
	}
}
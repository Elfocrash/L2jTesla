package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class Q126_TheNameOfEvil_2 extends Quest
{
	public static final String qn = "Q126_TheNameOfEvil_2";
	
	private static final int MUSHIKA = 32114;
	private static final int ASAMANAH = 32115;
	private static final int ULU_KAIMU = 32119;
	private static final int BALU_KAIMU = 32120;
	private static final int CHUTA_KAIMU = 32121;
	private static final int WARRIOR_GRAVE = 32122;
	private static final int SHILEN_STONE_STATUE = 32109;
	
	private static final int BONEPOWDER = 8783;
	private static final int EWA = 729;
	
	public Q126_TheNameOfEvil_2()
	{
		super(126, "The Name of Evil - 2");
		
		addStartNpc(ASAMANAH);
		addTalkId(ASAMANAH, MUSHIKA, ULU_KAIMU, BALU_KAIMU, CHUTA_KAIMU, WARRIOR_GRAVE, SHILEN_STONE_STATUE);
	}
	
	@Override
	public final String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32115-05.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32115-10.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32119-02.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32119-09.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32119-11.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32120-07.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32120-09.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32120-11.htm"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32121-07.htm"))
		{
			st.set("cond", "9");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32121-10.htm"))
		{
			st.set("cond", "10");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32121-15.htm"))
		{
			st.set("cond", "11");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32122-03.htm"))
		{
			st.set("cond", "12");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32122-15.htm"))
		{
			st.set("cond", "13");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32122-18.htm"))
		{
			st.set("cond", "14");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32122-87.htm"))
			st.giveItems(BONEPOWDER, 1);
		else if (event.equalsIgnoreCase("32122-90.htm"))
		{
			st.set("cond", "18");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32109-02.htm"))
		{
			st.set("cond", "19");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32109-19.htm"))
		{
			st.set("cond", "20");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BONEPOWDER, 1);
		}
		else if (event.equalsIgnoreCase("32115-21.htm"))
		{
			st.set("cond", "21");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32115-28.htm"))
		{
			st.set("cond", "22");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32114-08.htm"))
		{
			st.set("cond", "23");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32114-09.htm"))
		{
			st.giveItems(EWA, 1);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		else if (event.equalsIgnoreCase("DOOne"))
		{
			htmltext = "32122-26.htm";
			if (st.getInt("DO") < 1)
				st.set("DO", "1");
		}
		else if (event.equalsIgnoreCase("MIOne"))
		{
			htmltext = "32122-30.htm";
			if (st.getInt("MI") < 1)
				st.set("MI", "1");
		}
		else if (event.equalsIgnoreCase("FAOne"))
		{
			htmltext = "32122-34.htm";
			if (st.getInt("FA") < 1)
				st.set("FA", "1");
		}
		else if (event.equalsIgnoreCase("SOLOne"))
		{
			htmltext = "32122-38.htm";
			if (st.getInt("SOL") < 1)
				st.set("SOL", "1");
		}
		else if (event.equalsIgnoreCase("FA_2One"))
		{
			if (st.getInt("FA_2") < 1)
				st.set("FA_2", "1");
			htmltext = getSongOne(st);
		}
		else if (event.equalsIgnoreCase("FATwo"))
		{
			htmltext = "32122-47.htm";
			if (st.getInt("FA") < 1)
				st.set("FA", "1");
		}
		else if (event.equalsIgnoreCase("SOLTwo"))
		{
			htmltext = "32122-51.htm";
			if (st.getInt("SOL") < 1)
				st.set("SOL", "1");
		}
		else if (event.equalsIgnoreCase("TITwo"))
		{
			htmltext = "32122-55.htm";
			if (st.getInt("TI") < 1)
				st.set("TI", "1");
		}
		else if (event.equalsIgnoreCase("SOL_2Two"))
		{
			htmltext = "32122-59.htm";
			if (st.getInt("SOL_2") < 1)
				st.set("SOL_2", "1");
		}
		else if (event.equalsIgnoreCase("FA_2Two"))
		{
			if (st.getInt("FA_2") < 1)
				st.set("FA_2", "1");
			htmltext = getSongTwo(st);
		}
		else if (event.equalsIgnoreCase("SOLTri"))
		{
			htmltext = "32122-68.htm";
			if (st.getInt("SOL") < 1)
				st.set("SOL", "1");
		}
		else if (event.equalsIgnoreCase("FATri"))
		{
			htmltext = "32122-72.htm";
			if (st.getInt("FA") < 1)
				st.set("FA", "1");
		}
		else if (event.equalsIgnoreCase("MITri"))
		{
			htmltext = "32122-76.htm";
			if (st.getInt("MI") < 1)
				st.set("MI", "1");
		}
		else if (event.equalsIgnoreCase("FA_2Tri"))
		{
			htmltext = "32122-80.htm";
			if (st.getInt("FA_2") < 1)
				st.set("FA_2", "1");
		}
		else if (event.equalsIgnoreCase("MI_2Tri"))
		{
			if (st.getInt("MI_2") < 1)
				st.set("MI_2", "1");
			htmltext = getSongTri(st);
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
				if (player.getLevel() < 77)
					htmltext = "32115-02.htm";
				else
				{
					QuestState st2 = player.getQuestState(Q125_TheNameOfEvil_1.qn);
					if (st2 != null && st2.isCompleted())
						htmltext = "32115-01.htm";
					else
						htmltext = "32115-04.htm";
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ASAMANAH:
						if (cond == 1)
						{
							htmltext = "32115-11.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond > 1 && cond < 20)
							htmltext = "32115-12.htm";
						else if (cond == 20)
							htmltext = "32115-13.htm";
						else if (cond == 21)
							htmltext = "32115-22.htm";
						else if (cond == 22)
							htmltext = "32115-29.htm";
						break;
					
					case ULU_KAIMU:
						if (cond == 1)
							htmltext = "32119-01a.htm";
						else if (cond == 2)
							htmltext = "32119-02.htm";
						else if (cond == 3)
							htmltext = "32119-08.htm";
						else if (cond == 4)
							htmltext = "32119-09.htm";
						else if (cond > 4)
							htmltext = "32119-12.htm";
						break;
					
					case BALU_KAIMU:
						if (cond < 5)
							htmltext = "32120-02.htm";
						else if (cond == 5)
							htmltext = "32120-01.htm";
						else if (cond == 6)
							htmltext = "32120-03.htm";
						else if (cond == 7)
							htmltext = "32120-08.htm";
						else if (cond > 7)
							htmltext = "32120-12.htm";
						break;
					
					case CHUTA_KAIMU:
						if (cond < 8)
							htmltext = "32121-02.htm";
						else if (cond == 8)
							htmltext = "32121-01.htm";
						else if (cond == 9)
							htmltext = "32121-03.htm";
						else if (cond == 10)
							htmltext = "32121-10.htm";
						else if (cond > 10)
							htmltext = "32121-16.htm";
						break;
					
					case WARRIOR_GRAVE:
						if (cond < 11)
							htmltext = "32122-02.htm";
						else if (cond == 11)
							htmltext = "32122-01.htm";
						else if (cond == 12)
							htmltext = "32122-15.htm";
						else if (cond == 13)
							htmltext = "32122-18.htm";
						else if (cond == 14)
							htmltext = "32122-24.htm";
						else if (cond == 15)
							htmltext = "32122-45.htm";
						else if (cond == 16)
							htmltext = "32122-66.htm";
						else if (cond == 17)
							htmltext = "32122-84.htm";
						else if (cond == 18)
							htmltext = "32122-91.htm";
						break;
					
					case SHILEN_STONE_STATUE:
						if (cond < 18)
							htmltext = "32109-03.htm";
						else if (cond == 18)
							htmltext = "32109-02.htm";
						else if (cond == 19)
							htmltext = "32109-05.htm";
						else if (cond > 19)
							htmltext = "32109-04.htm";
						break;
					
					case MUSHIKA:
						if (cond < 22)
							htmltext = "32114-02.htm";
						else if (cond == 22)
							htmltext = "32114-01.htm";
						else if (cond == 23)
							htmltext = "32114-04.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	private static String getSongOne(QuestState st)
	{
		String htmltext = "32122-24.htm";
		if (st.getInt("cond") == 14 && st.getInt("DO") > 0 && st.getInt("MI") > 0 && st.getInt("FA") > 0 && st.getInt("SOL") > 0 && st.getInt("FA_2") > 0)
		{
			htmltext = "32122-42.htm";
			st.set("cond", "15");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.unset("DO");
			st.unset("MI");
			st.unset("FA");
			st.unset("SOL");
			st.unset("FA_2");
		}
		return htmltext;
	}
	
	private static String getSongTwo(QuestState st)
	{
		String htmltext = "32122-45.htm";
		if (st.getInt("cond") == 15 && st.getInt("FA") > 0 && st.getInt("SOL") > 0 && st.getInt("TI") > 0 && st.getInt("SOL_2") > 0 && st.getInt("FA_2") > 0)
		{
			htmltext = "32122-63.htm";
			st.set("cond", "16");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.unset("FA");
			st.unset("SOL");
			st.unset("TI");
			st.unset("SOL_2");
			st.unset("FA3_2");
		}
		return htmltext;
	}
	
	private static String getSongTri(QuestState st)
	{
		String htmltext = "32122-66.htm";
		if (st.getInt("cond") == 16 && st.getInt("SOL") > 0 && st.getInt("FA") > 0 && st.getInt("MI") > 0 && st.getInt("FA_2") > 0 && st.getInt("MI_2") > 0)
		{
			htmltext = "32122-84.htm";
			st.set("cond", "17");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.unset("SOL");
			st.unset("FA");
			st.unset("MI");
			st.unset("FA_2");
			st.unset("MI_2");
		}
		return htmltext;
	}
}
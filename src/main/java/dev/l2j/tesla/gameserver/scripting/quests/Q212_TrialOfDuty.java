package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.itemcontainer.Inventory;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q212_TrialOfDuty extends Quest
{
	private static final String qn = "Q212_TrialOfDuty";
	
	// Items
	private static final int LETTER_OF_DUSTIN = 2634;
	private static final int KNIGHTS_TEAR = 2635;
	private static final int MIRROR_OF_ORPIC = 2636;
	private static final int TEAR_OF_CONFESSION = 2637;
	private static final int REPORT_PIECE_1 = 2638;
	private static final int REPORT_PIECE_2 = 2639;
	private static final int TEAR_OF_LOYALTY = 2640;
	private static final int MILITAS_ARTICLE = 2641;
	private static final int SAINTS_ASHES_URN = 2642;
	private static final int ATHEBALDT_SKULL = 2643;
	private static final int ATHEBALDT_RIBS = 2644;
	private static final int ATHEBALDT_SHIN = 2645;
	private static final int LETTER_OF_WINDAWOOD = 2646;
	private static final int OLD_KNIGHT_SWORD = 3027;
	
	// Rewards
	private static final int MARK_OF_DUTY = 2633;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int HANNAVALT = 30109;
	private static final int DUSTIN = 30116;
	private static final int SIR_COLLIN = 30311;
	private static final int SIR_ARON = 30653;
	private static final int SIR_KIEL = 30654;
	private static final int SILVERSHADOW = 30655;
	private static final int SPIRIT_TALIANUS = 30656;
	
	public Q212_TrialOfDuty()
	{
		super(212, "Trial of Duty");
		
		setItemsIds(LETTER_OF_DUSTIN, KNIGHTS_TEAR, MIRROR_OF_ORPIC, TEAR_OF_CONFESSION, REPORT_PIECE_1, REPORT_PIECE_2, TEAR_OF_LOYALTY, MILITAS_ARTICLE, SAINTS_ASHES_URN, ATHEBALDT_SKULL, ATHEBALDT_RIBS, ATHEBALDT_SHIN, LETTER_OF_WINDAWOOD, OLD_KNIGHT_SWORD);
		
		addStartNpc(HANNAVALT);
		addTalkId(HANNAVALT, DUSTIN, SIR_COLLIN, SIR_ARON, SIR_KIEL, SILVERSHADOW, SPIRIT_TALIANUS);
		
		addKillId(20144, 20190, 20191, 20200, 20201, 20270, 27119, 20577, 20578, 20579, 20580, 20581, 20582);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30109-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			
			if (!player.getMemos().getBool("secondClassChange35", false))
			{
				htmltext = "30109-04a.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_35.get(player.getClassId().getId()));
				player.getMemos().set("secondClassChange35", true);
			}
		}
		else if (event.equalsIgnoreCase("30116-05.htm"))
		{
			st.set("cond", "14");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(TEAR_OF_LOYALTY, 1);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = Quest.getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (player.getClassId() != ClassId.KNIGHT && player.getClassId() != ClassId.ELVEN_KNIGHT && player.getClassId() != ClassId.PALUS_KNIGHT)
					htmltext = "30109-02.htm";
				else if (player.getLevel() < 35)
					htmltext = "30109-01.htm";
				else
					htmltext = "30109-03.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case HANNAVALT:
						if (cond == 18)
						{
							htmltext = "30109-05.htm";
							st.takeItems(LETTER_OF_DUSTIN, 1);
							st.giveItems(MARK_OF_DUTY, 1);
							st.rewardExpAndSp(79832, 3750);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						else
							htmltext = "30109-04a.htm";
						break;
					
					case SIR_ARON:
						if (cond == 1)
						{
							htmltext = "30653-01.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(OLD_KNIGHT_SWORD, 1);
						}
						else if (cond == 2)
							htmltext = "30653-02.htm";
						else if (cond == 3)
						{
							htmltext = "30653-03.htm";
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(KNIGHTS_TEAR, 1);
							st.takeItems(OLD_KNIGHT_SWORD, 1);
						}
						else if (cond > 3)
							htmltext = "30653-04.htm";
						break;
					
					case SIR_KIEL:
						if (cond == 4)
						{
							htmltext = "30654-01.htm";
							st.set("cond", "5");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 5)
							htmltext = "30654-02.htm";
						else if (cond == 6)
						{
							htmltext = "30654-03.htm";
							st.set("cond", "7");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(MIRROR_OF_ORPIC, 1);
						}
						else if (cond == 7)
							htmltext = "30654-04.htm";
						else if (cond == 9)
						{
							htmltext = "30654-05.htm";
							st.set("cond", "10");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(TEAR_OF_CONFESSION, 1);
						}
						else if (cond > 9)
							htmltext = "30654-06.htm";
						break;
					
					case SPIRIT_TALIANUS:
						if (cond == 8)
						{
							htmltext = "30656-01.htm";
							st.set("cond", "9");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(MIRROR_OF_ORPIC, 1);
							st.takeItems(REPORT_PIECE_2, 1);
							st.giveItems(TEAR_OF_CONFESSION, 1);
							
							// Despawn the spirit.
							npc.deleteMe();
						}
						break;
					
					case SILVERSHADOW:
						if (cond == 10)
						{
							if (player.getLevel() < 35)
								htmltext = "30655-01.htm";
							else
							{
								htmltext = "30655-02.htm";
								st.set("cond", "11");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
						}
						else if (cond == 11)
							htmltext = "30655-03.htm";
						else if (cond == 12)
						{
							htmltext = "30655-04.htm";
							st.set("cond", "13");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(MILITAS_ARTICLE, -1);
							st.giveItems(TEAR_OF_LOYALTY, 1);
						}
						else if (cond == 13)
							htmltext = "30655-05.htm";
						break;
					
					case DUSTIN:
						if (cond == 13)
							htmltext = "30116-01.htm";
						else if (cond == 14)
							htmltext = "30116-06.htm";
						else if (cond == 15)
						{
							htmltext = "30116-07.htm";
							st.set("cond", "16");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(ATHEBALDT_SKULL, 1);
							st.takeItems(ATHEBALDT_RIBS, 1);
							st.takeItems(ATHEBALDT_SHIN, 1);
							st.giveItems(SAINTS_ASHES_URN, 1);
						}
						else if (cond == 16)
							htmltext = "30116-09.htm";
						else if (cond == 17)
						{
							htmltext = "30116-08.htm";
							st.set("cond", "18");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(LETTER_OF_WINDAWOOD, 1);
							st.giveItems(LETTER_OF_DUSTIN, 1);
						}
						else if (cond == 18)
							htmltext = "30116-10.htm";
						break;
					
					case SIR_COLLIN:
						if (cond == 16)
						{
							htmltext = "30311-01.htm";
							st.set("cond", "17");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(SAINTS_ASHES_URN, 1);
							st.giveItems(LETTER_OF_WINDAWOOD, 1);
						}
						else if (cond > 16)
							htmltext = "30311-02.htm";
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
		
		int cond = st.getInt("cond");
		switch (npc.getNpcId())
		{
			case 20190:
			case 20191:
				if (cond == 2 && Rnd.get(10) < 1)
				{
					st.playSound(QuestState.SOUND_BEFORE_BATTLE);
					addSpawn(27119, npc, false, 120000, true);
				}
				break;
			
			case 27119:
				if (cond == 2 && st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == OLD_KNIGHT_SWORD)
				{
					st.set("cond", "3");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(KNIGHTS_TEAR, 1);
				}
				break;
			
			case 20201:
			case 20200:
				if (cond == 5 && st.dropItemsAlways(REPORT_PIECE_1, 1, 10))
				{
					st.set("cond", "6");
					st.takeItems(REPORT_PIECE_1, -1);
					st.giveItems(REPORT_PIECE_2, 1);
				}
				break;
			
			case 20144:
				if ((cond == 7 || cond == 8) && Rnd.get(100) < 33)
				{
					if (cond == 7)
					{
						st.set("cond", "8");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					addSpawn(30656, npc, false, 300000, true);
				}
				break;
			
			case 20577:
			case 20578:
			case 20579:
			case 20580:
			case 20581:
			case 20582:
				if (cond == 11 && st.dropItemsAlways(MILITAS_ARTICLE, 1, 20))
					st.set("cond", "12");
				break;
			
			case 20270:
				if (cond == 14 && Rnd.nextBoolean())
				{
					if (!st.hasQuestItems(ATHEBALDT_SKULL))
					{
						st.playSound(QuestState.SOUND_ITEMGET);
						st.giveItems(ATHEBALDT_SKULL, 1);
					}
					else if (!st.hasQuestItems(ATHEBALDT_RIBS))
					{
						st.playSound(QuestState.SOUND_ITEMGET);
						st.giveItems(ATHEBALDT_RIBS, 1);
					}
					else if (!st.hasQuestItems(ATHEBALDT_SHIN))
					{
						st.set("cond", "15");
						st.playSound(QuestState.SOUND_MIDDLE);
						st.giveItems(ATHEBALDT_SHIN, 1);
					}
				}
				break;
		}
		
		return null;
	}
}
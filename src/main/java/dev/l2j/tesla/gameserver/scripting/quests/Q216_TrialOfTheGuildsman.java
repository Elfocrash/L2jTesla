package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q216_TrialOfTheGuildsman extends Quest
{
	private static final String qn = "Q216_TrialOfTheGuildsman";
	
	// Items
	private static final int RECIPE_JOURNEYMAN_RING = 3024;
	private static final int RECIPE_AMBER_BEAD = 3025;
	private static final int VALKON_RECOMMENDATION = 3120;
	private static final int MANDRAGORA_BERRY = 3121;
	private static final int ALTRAN_INSTRUCTIONS = 3122;
	private static final int ALTRAN_RECOMMENDATION_1 = 3123;
	private static final int ALTRAN_RECOMMENDATION_2 = 3124;
	private static final int NORMAN_INSTRUCTIONS = 3125;
	private static final int NORMAN_RECEIPT = 3126;
	private static final int DUNING_INSTRUCTIONS = 3127;
	private static final int DUNING_KEY = 3128;
	private static final int NORMAN_LIST = 3129;
	private static final int GRAY_BONE_POWDER = 3130;
	private static final int GRANITE_WHETSTONE = 3131;
	private static final int RED_PIGMENT = 3132;
	private static final int BRAIDED_YARN = 3133;
	private static final int JOURNEYMAN_GEM = 3134;
	private static final int PINTER_INSTRUCTIONS = 3135;
	private static final int AMBER_BEAD = 3136;
	private static final int AMBER_LUMP = 3137;
	private static final int JOURNEYMAN_DECO_BEADS = 3138;
	private static final int JOURNEYMAN_RING = 3139;
	
	// Rewards
	private static final int MARK_OF_GUILDSMAN = 3119;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int VALKON = 30103;
	private static final int NORMAN = 30210;
	private static final int ALTRAN = 30283;
	private static final int PINTER = 30298;
	private static final int DUNING = 30688;
	
	// Monsters
	private static final int ANT = 20079;
	private static final int ANT_CAPTAIN = 20080;
	private static final int GRANITE_GOLEM = 20083;
	private static final int MANDRAGORA_SPROUT = 20154;
	private static final int MANDRAGORA_SAPLING = 20155;
	private static final int MANDRAGORA_BLOSSOM = 20156;
	private static final int SILENOS = 20168;
	private static final int STRAIN = 20200;
	private static final int GHOUL = 20201;
	private static final int DEAD_SEEKER = 20202;
	private static final int BREKA_ORC_SHAMAN = 20269;
	private static final int BREKA_ORC_OVERLORD = 20270;
	private static final int BREKA_ORC_WARRIOR = 20271;
	
	public Q216_TrialOfTheGuildsman()
	{
		super(216, "Trial of the Guildsman");
		
		setItemsIds(RECIPE_JOURNEYMAN_RING, RECIPE_AMBER_BEAD, VALKON_RECOMMENDATION, MANDRAGORA_BERRY, ALTRAN_INSTRUCTIONS, ALTRAN_RECOMMENDATION_1, ALTRAN_RECOMMENDATION_2, NORMAN_INSTRUCTIONS, NORMAN_RECEIPT, DUNING_INSTRUCTIONS, DUNING_KEY, NORMAN_LIST, GRAY_BONE_POWDER, GRANITE_WHETSTONE, RED_PIGMENT, BRAIDED_YARN, JOURNEYMAN_GEM, PINTER_INSTRUCTIONS, AMBER_BEAD, AMBER_LUMP, JOURNEYMAN_DECO_BEADS, JOURNEYMAN_RING);
		
		addStartNpc(VALKON);
		addTalkId(VALKON, NORMAN, ALTRAN, PINTER, DUNING);
		
		addKillId(ANT, ANT_CAPTAIN, GRANITE_GOLEM, MANDRAGORA_SPROUT, MANDRAGORA_SAPLING, MANDRAGORA_BLOSSOM, SILENOS, STRAIN, GHOUL, DEAD_SEEKER, BREKA_ORC_SHAMAN, BREKA_ORC_OVERLORD, BREKA_ORC_WARRIOR);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30103-06.htm"))
		{
			if (st.getQuestItemsCount(57) >= 2000)
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
				st.takeItems(57, 2000);
				st.giveItems(VALKON_RECOMMENDATION, 1);
				
				if (!player.getMemos().getBool("secondClassChange35", false))
				{
					htmltext = "30103-06d.htm";
					st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_35.get(player.getClassId().getId()));
					player.getMemos().set("secondClassChange35", true);
				}
			}
			else
				htmltext = "30103-05a.htm";
		}
		else if (event.equalsIgnoreCase("30103-06c.htm") || event.equalsIgnoreCase("30103-07c.htm"))
		{
			if (st.getInt("cond") < 3)
			{
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equalsIgnoreCase("30103-09a.htm") || event.equalsIgnoreCase("30103-09b.htm"))
		{
			st.takeItems(ALTRAN_INSTRUCTIONS, 1);
			st.takeItems(JOURNEYMAN_RING, -1);
			st.giveItems(MARK_OF_GUILDSMAN, 1);
			st.rewardExpAndSp(80993, 12250);
			player.broadcastPacket(new SocialAction(player, 3));
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}
		else if (event.equalsIgnoreCase("30210-04.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(ALTRAN_RECOMMENDATION_1, 1);
			st.giveItems(NORMAN_INSTRUCTIONS, 1);
			st.giveItems(NORMAN_RECEIPT, 1);
		}
		else if (event.equalsIgnoreCase("30210-10.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(NORMAN_LIST, 1);
		}
		else if (event.equalsIgnoreCase("30283-03.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(MANDRAGORA_BERRY, 1);
			st.takeItems(VALKON_RECOMMENDATION, 1);
			st.giveItems(ALTRAN_INSTRUCTIONS, 1);
			st.giveItems(ALTRAN_RECOMMENDATION_1, 1);
			st.giveItems(ALTRAN_RECOMMENDATION_2, 1);
			st.giveItems(RECIPE_JOURNEYMAN_RING, 1);
		}
		else if (event.equalsIgnoreCase("30298-04.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(ALTRAN_RECOMMENDATION_2, 1);
			st.giveItems(PINTER_INSTRUCTIONS, 1);
			
			// Artisan receives a recipe to craft Amber Beads, while spoiler case is handled in onKill section.
			if (player.getClassId() == ClassId.ARTISAN)
			{
				htmltext = "30298-05.htm";
				st.giveItems(RECIPE_AMBER_BEAD, 1);
			}
		}
		else if (event.equalsIgnoreCase("30688-02.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(NORMAN_RECEIPT, 1);
			st.giveItems(DUNING_INSTRUCTIONS, 1);
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
				if (player.getClassId() != ClassId.SCAVENGER && player.getClassId() != ClassId.ARTISAN)
					htmltext = "30103-01.htm";
				else if (player.getLevel() < 35)
					htmltext = "30103-02.htm";
				else
					htmltext = "30103-03.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case VALKON:
						if (cond == 1)
							htmltext = "30103-06c.htm";
						else if (cond < 5)
							htmltext = "30103-07.htm";
						else if (cond == 5)
							htmltext = "30103-08.htm";
						else if (cond == 6)
							htmltext = (st.getQuestItemsCount(JOURNEYMAN_RING) == 7) ? "30103-09.htm" : "30103-08.htm";
						break;
					
					case ALTRAN:
						if (cond < 4)
						{
							htmltext = "30283-01.htm";
							if (cond == 1)
							{
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
						}
						else if (cond == 4)
							htmltext = "30283-02.htm";
						else if (cond > 4)
							htmltext = "30283-04.htm";
						break;
					
					case NORMAN:
						if (cond == 5)
						{
							if (st.hasQuestItems(ALTRAN_RECOMMENDATION_1))
								htmltext = "30210-01.htm";
							else if (st.hasQuestItems(NORMAN_RECEIPT))
								htmltext = "30210-05.htm";
							else if (st.hasQuestItems(DUNING_INSTRUCTIONS))
								htmltext = "30210-06.htm";
							else if (st.getQuestItemsCount(DUNING_KEY) == 30)
							{
								htmltext = "30210-07.htm";
								st.playSound(QuestState.SOUND_ITEMGET);
								st.takeItems(DUNING_KEY, -1);
							}
							else if (st.hasQuestItems(NORMAN_LIST))
							{
								if (st.getQuestItemsCount(GRAY_BONE_POWDER) == 70 && st.getQuestItemsCount(GRANITE_WHETSTONE) == 70 && st.getQuestItemsCount(RED_PIGMENT) == 70 && st.getQuestItemsCount(BRAIDED_YARN) == 70)
								{
									htmltext = "30210-12.htm";
									st.takeItems(NORMAN_INSTRUCTIONS, 1);
									st.takeItems(NORMAN_LIST, 1);
									st.takeItems(BRAIDED_YARN, -1);
									st.takeItems(GRANITE_WHETSTONE, -1);
									st.takeItems(GRAY_BONE_POWDER, -1);
									st.takeItems(RED_PIGMENT, -1);
									st.giveItems(JOURNEYMAN_GEM, 7);
									
									if (st.getQuestItemsCount(JOURNEYMAN_DECO_BEADS) == 7)
									{
										st.set("cond", "6");
										st.playSound(QuestState.SOUND_MIDDLE);
									}
									else
										st.playSound(QuestState.SOUND_ITEMGET);
								}
								else
									htmltext = "30210-11.htm";
							}
						}
						break;
					
					case DUNING:
						if (cond == 5)
						{
							if (st.hasQuestItems(NORMAN_RECEIPT))
								htmltext = "30688-01.htm";
							else if (st.hasQuestItems(DUNING_INSTRUCTIONS))
							{
								if (st.getQuestItemsCount(DUNING_KEY) < 30)
									htmltext = "30688-03.htm";
								else
								{
									htmltext = "30688-04.htm";
									st.playSound(QuestState.SOUND_ITEMGET);
									st.takeItems(DUNING_INSTRUCTIONS, 1);
								}
							}
							else
								htmltext = "30688-05.htm";
						}
						break;
					
					case PINTER:
						if (cond == 5)
						{
							if (st.hasQuestItems(ALTRAN_RECOMMENDATION_2))
								htmltext = (player.getLevel() < 36) ? "30298-01.htm" : "30298-02.htm";
							else if (st.hasQuestItems(PINTER_INSTRUCTIONS))
							{
								if (st.getQuestItemsCount(AMBER_BEAD) < 70)
									htmltext = "30298-06.htm";
								else
								{
									htmltext = "30298-07.htm";
									st.takeItems(AMBER_BEAD, -1);
									st.takeItems(PINTER_INSTRUCTIONS, 1);
									st.giveItems(JOURNEYMAN_DECO_BEADS, 7);
									
									if (st.getQuestItemsCount(JOURNEYMAN_GEM) == 7)
									{
										st.set("cond", "6");
										st.playSound(QuestState.SOUND_MIDDLE);
									}
									else
										st.playSound(QuestState.SOUND_ITEMGET);
								}
							}
						}
						else if (st.hasQuestItems(JOURNEYMAN_DECO_BEADS))
							htmltext = "30298-08.htm";
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
			case MANDRAGORA_SPROUT:
			case MANDRAGORA_SAPLING:
			case MANDRAGORA_BLOSSOM:
				if (st.getInt("cond") == 3 && st.dropItemsAlways(MANDRAGORA_BERRY, 1, 1))
					st.set("cond", "4");
				break;
			
			case BREKA_ORC_WARRIOR:
			case BREKA_ORC_OVERLORD:
			case BREKA_ORC_SHAMAN:
				if (st.hasQuestItems(DUNING_INSTRUCTIONS))
					st.dropItemsAlways(DUNING_KEY, 1, 30);
				break;
			
			case GHOUL:
			case STRAIN:
				if (st.hasQuestItems(NORMAN_LIST))
					st.dropItemsAlways(GRAY_BONE_POWDER, 5, 70);
				break;
			
			case GRANITE_GOLEM:
				if (st.hasQuestItems(NORMAN_LIST))
					st.dropItemsAlways(GRANITE_WHETSTONE, 7, 70);
				break;
			
			case DEAD_SEEKER:
				if (st.hasQuestItems(NORMAN_LIST))
					st.dropItemsAlways(RED_PIGMENT, 7, 70);
				break;
			
			case SILENOS:
				if (st.hasQuestItems(NORMAN_LIST))
					st.dropItemsAlways(BRAIDED_YARN, 10, 70);
				break;
			
			case ANT:
			case ANT_CAPTAIN:
				if (st.hasQuestItems(PINTER_INSTRUCTIONS))
				{
					// Different cases if player is a wannabe BH or WS.
					if (st.dropItemsAlways(AMBER_BEAD, (player.getClassId() == ClassId.SCAVENGER && ((Monster) npc).getSpoilerId() == player.getObjectId()) ? 10 : 5, 70))
						if (player.getClassId() == ClassId.ARTISAN && Rnd.nextBoolean())
							st.giveItems(AMBER_LUMP, 1);
				}
				break;
		}
		
		return null;
	}
}
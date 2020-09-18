package dev.l2j.tesla.gameserver.scripting.quests;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.util.ArraysUtil;

import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q227_TestOfTheReformer extends Quest
{
	private static final String qn = "Q227_TestOfTheReformer";
	
	// Items
	private static final int BOOK_OF_REFORM = 2822;
	private static final int LETTER_OF_INTRODUCTION = 2823;
	private static final int SLA_LETTER = 2824;
	private static final int GREETINGS = 2825;
	private static final int OL_MAHUM_MONEY = 2826;
	private static final int KATARI_LETTER = 2827;
	private static final int NYAKURI_LETTER = 2828;
	private static final int UNDEAD_LIST = 2829;
	private static final int RAMUS_LETTER = 2830;
	private static final int RIPPED_DIARY = 2831;
	private static final int HUGE_NAIL = 2832;
	private static final int LETTER_OF_BETRAYER = 2833;
	private static final int BONE_FRAGMENT_4 = 2834;
	private static final int BONE_FRAGMENT_5 = 2835;
	private static final int BONE_FRAGMENT_6 = 2836;
	private static final int BONE_FRAGMENT_7 = 2837;
	private static final int BONE_FRAGMENT_8 = 2838;
	private static final int BONE_FRAGMENT_9 = 2839;
	private static final int KAKAN_LETTER = 3037;
	
	// Rewards
	private static final int MARK_OF_REFORMER = 2821;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	
	// NPCs
	private static final int PUPINA = 30118;
	private static final int SLA = 30666;
	private static final int RAMUS = 30667;
	private static final int KATARI = 30668;
	private static final int KAKAN = 30669;
	private static final int NYAKURI = 30670;
	private static final int OL_MAHUM_PILGRIM = 30732;
	
	// Monsters
	private static final int MISERY_SKELETON = 20022;
	private static final int SKELETON_ARCHER = 20100;
	private static final int SKELETON_MARKSMAN = 20102;
	private static final int SKELETON_LORD = 20104;
	private static final int SILENT_HORROR = 20404;
	private static final int NAMELESS_REVENANT = 27099;
	private static final int ARURAUNE = 27128;
	private static final int OL_MAHUM_INSPECTOR = 27129;
	private static final int OL_MAHUM_BETRAYER = 27130;
	private static final int CRIMSON_WEREWOLF = 27131;
	private static final int KRUDEL_LIZARDMAN = 27132;
	
	// Checks & Instances
	private static long _timer;
	
	private static Npc _olMahumInspector;
	private static Npc _olMahumPilgrim;
	private static Npc _olMahumBetrayer;
	
	private static boolean _crimsonWerewolf = false;
	private static boolean _krudelLizardman = false;
	
	// Allowed skills when attacking Crimson Werewolf
	private static final int[] ALLOWED_SKILLS =
	{
		1031,
		1069,
		1164,
		1168,
		1147,
		1177,
		1184,
		1201,
		1206
	};
	
	public Q227_TestOfTheReformer()
	{
		super(227, "Test Of The Reformer");
		
		setItemsIds(BOOK_OF_REFORM, LETTER_OF_INTRODUCTION, SLA_LETTER, GREETINGS, OL_MAHUM_MONEY, KATARI_LETTER, NYAKURI_LETTER, UNDEAD_LIST, RAMUS_LETTER, RIPPED_DIARY, HUGE_NAIL, LETTER_OF_BETRAYER, BONE_FRAGMENT_4, BONE_FRAGMENT_5, BONE_FRAGMENT_6, BONE_FRAGMENT_7, BONE_FRAGMENT_8, BONE_FRAGMENT_9, KAKAN_LETTER);
		
		addStartNpc(PUPINA);
		addTalkId(PUPINA, SLA, RAMUS, KATARI, KAKAN, NYAKURI, OL_MAHUM_PILGRIM);
		
		addAttackId(NAMELESS_REVENANT, CRIMSON_WEREWOLF);
		addKillId(MISERY_SKELETON, SKELETON_ARCHER, SKELETON_MARKSMAN, SKELETON_LORD, SILENT_HORROR, NAMELESS_REVENANT, ARURAUNE, OL_MAHUM_INSPECTOR, OL_MAHUM_BETRAYER, CRIMSON_WEREWOLF, KRUDEL_LIZARDMAN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// PUPINA
		if (event.equalsIgnoreCase("30118-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(BOOK_OF_REFORM, 1);
			
			if (!player.getMemos().getBool("secondClassChange39", false))
			{
				htmltext = "30118-04b.htm";
				st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39.get(player.getClassId().getId()));
				player.getMemos().set("secondClassChange39", true);
			}
		}
		else if (event.equalsIgnoreCase("30118-06.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BOOK_OF_REFORM, 1);
			st.takeItems(HUGE_NAIL, 1);
			st.giveItems(LETTER_OF_INTRODUCTION, 1);
		}
		// SLA
		else if (event.equalsIgnoreCase("30666-04.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LETTER_OF_INTRODUCTION, 1);
			st.giveItems(SLA_LETTER, 1);
		}
		// KAKAN
		else if (event.equalsIgnoreCase("30669-03.htm"))
		{
			if (st.getInt("cond") != 12)
			{
				st.set("cond", "12");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			
			if (!_crimsonWerewolf)
			{
				addSpawn(CRIMSON_WEREWOLF, -9382, -89852, -2333, 0, false, 299000, true);
				_crimsonWerewolf = true;
				
				// Resets Crimson Werewolf
				startQuestTimer("werewolf_cleanup", 300000, null, player, false);
			}
		}
		// NYAKURI
		else if (event.equalsIgnoreCase("30670-03.htm"))
		{
			st.set("cond", "15");
			st.playSound(QuestState.SOUND_MIDDLE);
			if (!_krudelLizardman)
			{
				addSpawn(KRUDEL_LIZARDMAN, 126019, -179983, -1781, 0, false, 299000, true);
				_krudelLizardman = true;
				
				// Resets Krudel Lizardman
				startQuestTimer("lizardman_cleanup", 300000, null, player, false);
			}
		}
		// Despawns Crimson Werewolf
		else if (event.equalsIgnoreCase("werewolf_despawn"))
		{
			npc.abortAttack();
			npc.broadcastNpcSay("Cowardly guy!");
			npc.decayMe();
			_crimsonWerewolf = false;
			cancelQuestTimer("werewolf_cleanup", null, player);
			return null;
		}
		// Despawns
		else if (event.equalsIgnoreCase("ol_mahums_despawn"))
		{
			_timer++;
			
			if (st.getInt("cond") == 8 || _timer >= 60)
			{
				if (_olMahumPilgrim != null)
				{
					_olMahumPilgrim.deleteMe();
					_olMahumPilgrim = null;
				}
				
				if (_olMahumInspector != null)
				{
					_olMahumInspector.deleteMe();
					_olMahumInspector = null;
				}
				cancelQuestTimer("ol_mahums_despawn", null, player);
				_timer = 0;
			}
			
			return null;
		}
		else if (event.equalsIgnoreCase("betrayer_despawn"))
		{
			if (_olMahumBetrayer != null)
			{
				_olMahumBetrayer.deleteMe();
				_olMahumBetrayer = null;
			}
			
			return null;
		}
		// Clean ups
		else if (event.equalsIgnoreCase("werewolf_cleanup"))
		{
			_crimsonWerewolf = false;
			return null;
		}
		else if (event.equalsIgnoreCase("lizardman_cleanup"))
		{
			_krudelLizardman = false;
			return null;
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
				if (player.getClassId() == ClassId.CLERIC || player.getClassId() == ClassId.SHILLIEN_ORACLE)
					htmltext = (player.getLevel() < 39) ? "30118-01.htm" : "30118-03.htm";
				else
					htmltext = "30118-02.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case PUPINA:
						if (cond < 3)
							htmltext = "30118-04a.htm";
						else if (cond == 3)
							htmltext = "30118-05.htm";
						else if (cond > 3)
							htmltext = "30118-07.htm";
						break;
					
					case SLA:
						if (cond == 4)
							htmltext = "30666-01.htm";
						else if (cond > 4 && cond < 10)
							htmltext = "30666-05.htm";
						else if (cond == 10)
						{
							htmltext = "30666-06.htm";
							st.set("cond", "11");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(OL_MAHUM_MONEY, 1);
							st.giveItems(GREETINGS, 3);
						}
						else if (cond > 10 && cond < 20)
							htmltext = "30666-06.htm";
						else if (cond == 20)
						{
							htmltext = "30666-07.htm";
							st.takeItems(KATARI_LETTER, 1);
							st.takeItems(KAKAN_LETTER, 1);
							st.takeItems(NYAKURI_LETTER, 1);
							st.takeItems(RAMUS_LETTER, 1);
							st.giveItems(MARK_OF_REFORMER, 1);
							st.rewardExpAndSp(164032, 17500);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case KATARI:
						if (cond == 5 || cond == 6)
						{
							htmltext = "30668-01.htm";
							
							if (cond == 5)
							{
								st.set("cond", "6");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(SLA_LETTER, 1);
							}
							
							if (_olMahumPilgrim == null && _olMahumInspector == null)
							{
								_olMahumPilgrim = addSpawn(OL_MAHUM_PILGRIM, -4015, 40141, -3664, 0, false, 0, true);
								_olMahumInspector = addSpawn(OL_MAHUM_INSPECTOR, -4034, 40201, -3665, 0, false, 0, true);
								
								// Resets Ol Mahums' instances
								startQuestTimer("ol_mahums_despawn", 5000, null, player, true);
								
								((Attackable) _olMahumInspector).addDamageHate(_olMahumPilgrim, 0, 99999);
								_olMahumInspector.getAI().setIntention(IntentionType.ATTACK, _olMahumPilgrim);
								
								// TODO : make Npc be able to attack Attackable.
								// ((Attackable) _olMahumPilgrim).addDamageHate(_olMahumInspector, 0, 99999);
								// _olMahumPilgrim.getAI().setIntention(IntentionType.ATTACK, _olMahumInspector);
							}
						}
						else if (cond == 7)
						{
							htmltext = "30668-01.htm";
							
							if (_olMahumPilgrim == null)
							{
								_olMahumPilgrim = addSpawn(OL_MAHUM_PILGRIM, -4015, 40141, -3664, 0, false, 0, true);
								
								// Resets Ol Mahums' instances
								startQuestTimer("ol_mahums_despawn", 5000, null, player, true);
							}
						}
						else if (cond == 8)
						{
							htmltext = "30668-02.htm";
							
							if (_olMahumBetrayer == null)
							{
								_olMahumBetrayer = addSpawn(OL_MAHUM_BETRAYER, -4106, 40174, -3660, 0, false, 0, true);
								_olMahumBetrayer.setRunning();
								_olMahumBetrayer.getAI().setIntention(IntentionType.MOVE_TO, new Location(-7732, 36787, -3709));
								
								// Resets Ol Mahum Betrayer's instance
								startQuestTimer("betrayer_despawn", 40000, null, player, false);
							}
						}
						else if (cond == 9)
						{
							htmltext = "30668-03.htm";
							st.set("cond", "10");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(LETTER_OF_BETRAYER, 1);
							st.giveItems(KATARI_LETTER, 1);
						}
						else if (cond > 9)
							htmltext = "30668-04.htm";
						break;
					
					case OL_MAHUM_PILGRIM:
						if (cond == 7)
						{
							htmltext = "30732-01.htm";
							st.set("cond", "8");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(OL_MAHUM_MONEY, 1);
						}
						break;
					
					case KAKAN:
						if (cond == 11 || cond == 12)
							htmltext = "30669-01.htm";
						else if (cond == 13)
						{
							htmltext = "30669-04.htm";
							st.set("cond", "14");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(GREETINGS, 1);
							st.giveItems(KAKAN_LETTER, 1);
						}
						else if (cond > 13)
							htmltext = "30669-04.htm";
						break;
					
					case NYAKURI:
						if (cond == 14 || cond == 15)
							htmltext = "30670-01.htm";
						else if (cond == 16)
						{
							htmltext = "30670-04.htm";
							st.set("cond", "17");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(GREETINGS, 1);
							st.giveItems(NYAKURI_LETTER, 1);
						}
						else if (cond > 16)
							htmltext = "30670-04.htm";
						break;
					
					case RAMUS:
						if (cond == 17)
						{
							htmltext = "30667-01.htm";
							st.set("cond", "18");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(GREETINGS, 1);
							st.giveItems(UNDEAD_LIST, 1);
						}
						else if (cond == 18)
							htmltext = "30667-02.htm";
						else if (cond == 19)
						{
							htmltext = "30667-03.htm";
							st.set("cond", "20");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BONE_FRAGMENT_4, 1);
							st.takeItems(BONE_FRAGMENT_5, 1);
							st.takeItems(BONE_FRAGMENT_6, 1);
							st.takeItems(BONE_FRAGMENT_7, 1);
							st.takeItems(BONE_FRAGMENT_8, 1);
							st.takeItems(UNDEAD_LIST, 1);
							st.giveItems(RAMUS_LETTER, 1);
						}
						else if (cond > 19)
							htmltext = "30667-03.htm";
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
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		final Player player = attacker.getActingPlayer();
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		final int cond = st.getInt("cond");
		switch (npc.getNpcId())
		{
			case NAMELESS_REVENANT:
				if ((cond == 1 || cond == 2) && skill != null && skill.getId() == 1031)
					npc.setScriptValue(1);
				break;
			
			case CRIMSON_WEREWOLF:
				if (cond == 12 && !npc.isScriptValue(1) && (skill == null || !ArraysUtil.contains(ALLOWED_SKILLS, skill.getId())))
				{
					npc.setScriptValue(1);
					startQuestTimer("werewolf_despawn", 1000, npc, player, false);
				}
				break;
		}
		
		return null;
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
			case NAMELESS_REVENANT:
				if ((cond == 1 || cond == 2) && npc.isScriptValue(1) && st.dropItemsAlways(RIPPED_DIARY, 1, 7))
				{
					st.set("cond", "2");
					st.takeItems(RIPPED_DIARY, -1);
					addSpawn(ARURAUNE, npc, false, 300000, true);
				}
				break;
			
			case ARURAUNE:
				if (cond == 2)
				{
					st.set("cond", "3");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(HUGE_NAIL, 1);
					npc.broadcastNpcSay("The concealed truth will always be revealed...!");
				}
				break;
			
			case OL_MAHUM_INSPECTOR:
				if (cond == 6)
				{
					st.set("cond", "7");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				break;
			
			case OL_MAHUM_BETRAYER:
				if (cond == 8)
				{
					st.set("cond", "9");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(LETTER_OF_BETRAYER, 1);
					cancelQuestTimer("betrayer_despawn", null, player);
					_olMahumBetrayer = null;
				}
				break;
			
			case CRIMSON_WEREWOLF:
				if (cond == 12)
				{
					st.set("cond", "13");
					st.playSound(QuestState.SOUND_MIDDLE);
					cancelQuestTimer("werewolf_cleanup", null, player);
					_crimsonWerewolf = false;
				}
				break;
			
			case KRUDEL_LIZARDMAN:
				if (cond == 15)
				{
					st.set("cond", "16");
					st.playSound(QuestState.SOUND_MIDDLE);
					cancelQuestTimer("lizardman_cleanup", null, player);
					_krudelLizardman = false;
				}
				break;
			
			case SILENT_HORROR:
				if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_4))
				{
					st.giveItems(BONE_FRAGMENT_4, 1);
					if (st.hasQuestItems(BONE_FRAGMENT_5, BONE_FRAGMENT_6, BONE_FRAGMENT_7, BONE_FRAGMENT_8))
					{
						st.set("cond", "19");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case SKELETON_LORD:
				if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_5))
				{
					st.giveItems(BONE_FRAGMENT_5, 1);
					if (st.hasQuestItems(BONE_FRAGMENT_4, BONE_FRAGMENT_6, BONE_FRAGMENT_7, BONE_FRAGMENT_8))
					{
						st.set("cond", "19");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case SKELETON_MARKSMAN:
				if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_6))
				{
					st.giveItems(BONE_FRAGMENT_6, 1);
					if (st.hasQuestItems(BONE_FRAGMENT_4, BONE_FRAGMENT_5, BONE_FRAGMENT_7, BONE_FRAGMENT_8))
					{
						st.set("cond", "19");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case MISERY_SKELETON:
				if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_7))
				{
					st.giveItems(BONE_FRAGMENT_7, 1);
					if (st.hasQuestItems(BONE_FRAGMENT_4, BONE_FRAGMENT_5, BONE_FRAGMENT_6, BONE_FRAGMENT_8))
					{
						st.set("cond", "19");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case SKELETON_ARCHER:
				if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_8))
				{
					st.giveItems(BONE_FRAGMENT_8, 1);
					if (st.hasQuestItems(BONE_FRAGMENT_4, BONE_FRAGMENT_5, BONE_FRAGMENT_6, BONE_FRAGMENT_7))
					{
						st.set("cond", "19");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
		}
		
		return null;
	}
}
package dev.l2j.tesla.gameserver.scripting.quests.SagasScripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillUse;

public class SagasSuperClass extends Quest
{
	public int qnu;
	
	public int[] NPC = {};
	public int[] Items = {};
	public int[] Mob = {};
	
	public int classid;
	public int prevclass;
	
	public int[] X = {};
	public int[] Y = {};
	public int[] Z = {};
	
	private final Map<Npc, Integer> _SpawnList = new HashMap<>();
	
	private static final String[] Text = new String[]
	{
		"PLAYERNAME! Pursued to here! However, I jumped out of the Banshouren boundaries! You look at the giant as the sign of power!",
		"... Oh ... good! So it was ... let's begin!",
		"I do not have the patience ..! I have been a giant force ...! Cough chatter ah ah ah!",
		"Paying homage to those who disrupt the orderly will be PLAYERNAME's death!",
		"Now, my soul freed from the shackles of the millennium, Halixia, to the back side I come ...",
		"Why do you interfere others' battles?",
		"This is a waste of time.. Say goodbye...!",
		"...That is the enemy",
		"...Goodness! PLAYERNAME you are still looking?",
		"PLAYERNAME ... Not just to whom the victory. Only personnel involved in the fighting are eligible to share in the victory.",
		"Your sword is not an ornament. Don't you think, PLAYERNAME?",
		"Goodness! I no longer sense a battle there now.",
		"let...",
		"Only engaged in the battle to bar their choice. Perhaps you should regret.",
		"The human nation was foolish to try and fight a giant's strength.",
		"Must...Retreat... Too...Strong.",
		"PLAYERNAME. Defeat...by...retaining...and...Mo...Hacker",
		"....! Fight...Defeat...It...Fight...Defeat...It..."
	};
	
	private static final int[] Archon_Hellisha_Norm =
	{
		18212,
		18214,
		18215,
		18216,
		18218
	};
	
	private static final int[] QuestClass =
	{
		0x05,
		0x14,
		0x15,
		0x02,
		0x03,
		0x2e,
		0x30,
		0x33,
		0x34,
		0x08,
		0x17,
		0x24,
		0x09,
		0x18,
		0x25,
		0x10,
		0x11,
		0x1e,
		0x0c,
		0x1b,
		0x28,
		0x0e,
		0x1c,
		0x29,
		0x0d,
		0x06,
		0x22,
		0x21,
		0x2b,
		0x37,
		0x39
	};
	
	public SagasSuperClass()
	{
		// initialize superclass
		super(-1, "Saga's SuperClass");
	}
	
	public SagasSuperClass(int id, String descr)
	{
		super(id, descr);
		
		qnu = id;
	}
	
	public void registerNPCs()
	{
		addStartNpc(NPC[0]);
		addAttackId(Mob[2], Mob[1]);
		addSkillSeeId(Mob[1]);
		addFirstTalkId(NPC[4]);
		
		for (int npc : NPC)
			addTalkId(npc);
		
		for (int mobid : Mob)
			addKillId(mobid);
		
		final int[] questItemIds = Items.clone();
		questItemIds[0] = 0;
		questItemIds[2] = 0; // remove Ice Crystal and Divine Stone of Wisdom
		setItemsIds(questItemIds);
		
		for (int Archon_Minion = 21646; Archon_Minion < 21652; Archon_Minion++)
			addKillId(Archon_Minion);
		
		for (int element : Archon_Hellisha_Norm)
			addKillId(element);
		
		for (int Guardian_Angel = 27214; Guardian_Angel < 27217; Guardian_Angel++)
			addKillId(Guardian_Angel);
	}
	
	private static void cast(Npc npc, Creature target, int skillId, int level)
	{
		target.broadcastPacket(new MagicSkillUse(target, target, skillId, level, 6000, 1));
		target.broadcastPacket(new MagicSkillUse(npc, npc, skillId, level, 6000, 1));
	}
	
	public void AddSpawn(QuestState st, Npc mob)
	{
		_SpawnList.put(mob, st.getPlayer().getObjectId());
	}
	
	public void DeleteSpawn(QuestState st, Npc npc)
	{
		if (_SpawnList.containsKey(npc))
		{
			_SpawnList.remove(npc);
			npc.deleteMe();
		}
	}
	
	public QuestState findRightState(Npc npc)
	{
		if (_SpawnList.containsKey(npc))
		{
			final Player player = World.getInstance().getPlayer(_SpawnList.get(npc));
			if (player != null)
				return player.getQuestState(getName());
		}
		return null;
	}
	
	public void giveHallishaMark(QuestState st2)
	{
		if (st2.getInt("spawned") == 0)
		{
			if (st2.getQuestItemsCount(Items[3]) >= 700)
			{
				st2.takeItems(Items[3], 20);
				Npc Archon = addSpawn(Mob[1], st2.getPlayer(), false, 0, true);
				AddSpawn(st2, Archon);
				st2.set("spawned", "1");
				startQuestTimer("Archon Hellisha has despawned", 600000, Archon, st2.getPlayer(), false);
				
				// Attack player
				((Attackable) Archon).addDamageHate(st2.getPlayer(), 0, 99999);
				Archon.getAI().setIntention(IntentionType.ATTACK, st2.getPlayer(), null);
			}
			else
			{
				st2.giveItems(Items[3], 1);
				st2.playSound(QuestState.SOUND_ITEMGET);
			}
		}
	}
	
	public QuestState findQuest(Player player)
	{
		QuestState st = player.getQuestState(getName());
		if (st != null && player.getClassId().getId() == QuestClass[qnu - 70])
			return st;
		
		return null;
	}
	
	public int getClassId(Player player)
	{
		return classid;
	}
	
	public int getPrevClass(Player player)
	{
		return prevclass;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		QuestState st = player.getQuestState(getName());
		String htmltext = "";
		if (st != null)
		{
			if (event.equalsIgnoreCase("0-011.htm") || event.equalsIgnoreCase("0-012.htm") || event.equalsIgnoreCase("0-013.htm") || event.equalsIgnoreCase("0-014.htm") || event.equalsIgnoreCase("0-015.htm"))
				htmltext = event;
			else if (event.equalsIgnoreCase("accept"))
			{
				st.set("cond", "1");
				st.setState(STATE_STARTED);
				st.playSound(QuestState.SOUND_ACCEPT);
				st.giveItems(Items[10], 1);
				htmltext = "0-03.htm";
			}
			else if (event.equalsIgnoreCase("0-1"))
			{
				if (player.getLevel() < 76)
				{
					htmltext = "0-02.htm";
					if (st.isCreated())
						st.exitQuest(true);
				}
				else
					htmltext = "0-05.htm";
			}
			else if (event.equalsIgnoreCase("0-2"))
			{
				if (player.getLevel() >= 76)
				{
					st.exitQuest(false);
					st.set("cond", "0");
					htmltext = "0-07.htm";
					st.takeItems(Items[10], -1);
					st.rewardExpAndSp(2299404, 0);
					st.giveItems(57, 5000000);
					st.giveItems(6622, 1);
					
					int Class = getClassId(player);
					player.setClassId(Class);
					if (!player.isSubClassActive() && player.getBaseClass() == getPrevClass(player))
						player.setBaseClass(Class);
					
					player.broadcastUserInfo();
					cast(npc, player, 4339, 1);
				}
				else
				{
					st.takeItems(Items[10], -1);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.set("cond", "20");
					htmltext = "0-08.htm";
				}
			}
			else if (event.equalsIgnoreCase("1-3"))
			{
				st.set("cond", "3");
				htmltext = "1-05.htm";
			}
			else if (event.equalsIgnoreCase("1-4"))
			{
				st.set("cond", "4");
				st.takeItems(Items[0], 1);
				if (Items[11] != 0)
					st.takeItems(Items[11], 1);
				st.giveItems(Items[1], 1);
				htmltext = "1-06.htm";
			}
			else if (event.equalsIgnoreCase("2-1"))
			{
				st.set("cond", "2");
				htmltext = "2-05.htm";
			}
			else if (event.equalsIgnoreCase("2-2"))
			{
				st.set("cond", "5");
				st.takeItems(Items[1], 1);
				st.giveItems(Items[4], 1);
				htmltext = "2-06.htm";
			}
			else if (event.equalsIgnoreCase("3-5"))
			{
				htmltext = "3-07.htm";
			}
			else if (event.equalsIgnoreCase("3-6"))
			{
				st.set("cond", "11");
				htmltext = "3-02.htm";
			}
			else if (event.equalsIgnoreCase("3-7"))
			{
				st.set("cond", "12");
				htmltext = "3-03.htm";
			}
			else if (event.equalsIgnoreCase("3-8"))
			{
				st.set("cond", "13");
				st.takeItems(Items[2], 1);
				st.giveItems(Items[7], 1);
				htmltext = "3-08.htm";
			}
			else if (event.equalsIgnoreCase("4-1"))
			{
				htmltext = "4-010.htm";
			}
			else if (event.equalsIgnoreCase("4-2"))
			{
				st.giveItems(Items[9], 1);
				st.set("cond", "18");
				st.playSound(QuestState.SOUND_MIDDLE);
				htmltext = "4-011.htm";
			}
			else if (event.equalsIgnoreCase("4-3"))
			{
				st.giveItems(Items[9], 1);
				st.set("cond", "18");
				npc.broadcastNpcSay(Text[13]);
				st.set("Quest0", "0");
				cancelQuestTimer("Mob_2 has despawned", npc, player);
				st.playSound(QuestState.SOUND_MIDDLE);
				DeleteSpawn(st, npc);
				return null;
			}
			else if (event.equalsIgnoreCase("5-1"))
			{
				st.set("cond", "6");
				st.takeItems(Items[4], 1);
				cast(npc, player, 4546, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
				htmltext = "5-02.htm";
			}
			else if (event.equalsIgnoreCase("6-1"))
			{
				st.set("cond", "8");
				st.takeItems(Items[5], 1);
				cast(npc, player, 4546, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
				htmltext = "6-03.htm";
			}
			else if (event.equalsIgnoreCase("7-1"))
			{
				if (st.getInt("spawned") == 1)
					htmltext = "7-03.htm";
				else if (st.getInt("spawned") == 0)
				{
					Npc Mob_1 = addSpawn(Mob[0], X[0], Y[0], Z[0], 0, false, 0, true);
					st.set("spawned", "1");
					startQuestTimer("Mob_1 Timer 1", 500, Mob_1, player, false);
					startQuestTimer("Mob_1 has despawned", 300000, Mob_1, player, false);
					AddSpawn(st, Mob_1);
					htmltext = "7-02.htm";
				}
				else
					htmltext = "7-04.htm";
			}
			else if (event.equalsIgnoreCase("7-2"))
			{
				st.set("cond", "10");
				st.takeItems(Items[6], 1);
				cast(npc, player, 4546, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
				htmltext = "7-06.htm";
			}
			else if (event.equalsIgnoreCase("8-1"))
			{
				st.set("cond", "14");
				st.takeItems(Items[7], 1);
				cast(npc, player, 4546, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
				htmltext = "8-02.htm";
			}
			else if (event.equalsIgnoreCase("9-1"))
			{
				st.set("cond", "17");
				st.takeItems(Items[8], 1);
				cast(npc, player, 4546, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
				htmltext = "9-03.htm";
			}
			else if (event.equalsIgnoreCase("10-1"))
			{
				if (st.getInt("Quest0") == 0)
				{
					// Spawn NPC and mob fighting each other, and register them in _Spawnlist.
					Npc Mob_3 = addSpawn(Mob[2], X[1], Y[1], Z[1], 0, false, 0, true);
					Npc Mob_2 = addSpawn(NPC[4], X[2], Y[2], Z[2], 0, false, 0, true);
					AddSpawn(st, Mob_3);
					AddSpawn(st, Mob_2);
					
					st.set("Mob_2", String.valueOf(Mob_2.getObjectId()));
					
					st.set("Quest0", "1");
					st.set("Quest1", "45");
					
					startQuestTimer("Mob_3 Timer 1", 500, Mob_3, player, false);
					startQuestTimer("Mob_2 Timer 1", 500, Mob_2, player, false);
					
					startQuestTimer("Mob_3 has despawned", 59000, Mob_3, player, false);
					startQuestTimer("Mob_2 has despawned", 60000, Mob_2, player, false);
					
					htmltext = "10-02.htm";
				}
				else if (st.getInt("Quest1") == 45)
					htmltext = "10-03.htm";
				else
					htmltext = "10-04.htm";
			}
			else if (event.equalsIgnoreCase("10-2"))
			{
				st.set("cond", "19");
				st.takeItems(Items[9], 1);
				cast(npc, player, 4546, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
				htmltext = "10-06.htm";
			}
			else if (event.equalsIgnoreCase("11-9"))
			{
				st.set("cond", "15");
				htmltext = "11-03.htm";
			}
			else if (event.equalsIgnoreCase("Mob_1 Timer 1"))
			{
				// Attack player
				((Attackable) npc).addDamageHate(st.getPlayer(), 0, 99999);
				npc.getAI().setIntention(IntentionType.ATTACK, st.getPlayer(), null);
				
				npc.broadcastNpcSay(Text[0].replace("PLAYERNAME", player.getName()));
				return null;
			}
			else if (event.equalsIgnoreCase("Mob_1 has despawned"))
			{
				npc.broadcastNpcSay(Text[1]);
				st.set("spawned", "0");
				DeleteSpawn(st, npc);
				return null;
			}
			else if (event.equalsIgnoreCase("Archon Hellisha has despawned"))
			{
				st.set("spawned", "0");
				DeleteSpawn(st, npc);
				return null;
			}
			else if (event.equalsIgnoreCase("Mob_3 Timer 1"))
			{
				// Search the NPC.
				Npc Mob_2 = (Npc) World.getInstance().getObject(st.getInt("Mob_2"));
				if (Mob_2 == null)
					return null;
				
				if (_SpawnList.containsKey(Mob_2) && _SpawnList.get(Mob_2) == player.getObjectId())
				{
					((Attackable) npc).addDamageHate(Mob_2, 0, 99999);
					npc.getAI().setIntention(IntentionType.ATTACK, Mob_2, null);
					
					npc.broadcastNpcSay(Text[14]);
				}
				return null;
			}
			else if (event.equalsIgnoreCase("Mob_3 has despawned"))
			{
				npc.broadcastNpcSay(Text[15]);
				st.set("Quest0", "2");
				DeleteSpawn(st, npc);
				return null;
			}
			else if (event.equalsIgnoreCase("Mob_2 Timer 1"))
			{
				npc.broadcastNpcSay(Text[7]);
				startQuestTimer("Mob_2 Timer 2", 1500, npc, player, false);
				if (st.getInt("Quest1") == 45)
					st.set("Quest1", "0");
				return null;
			}
			else if (event.equalsIgnoreCase("Mob_2 Timer 2"))
			{
				npc.broadcastNpcSay(Text[8].replace("PLAYERNAME", player.getName()));
				startQuestTimer("Mob_2 Timer 3", 10000, npc, player, false);
				return null;
			}
			else if (event.equalsIgnoreCase("Mob_2 Timer 3"))
			{
				if (st.getInt("Quest0") == 0)
				{
					startQuestTimer("Mob_2 Timer 3", 13000, npc, player, false);
					if (Rnd.get(2) == 0)
						npc.broadcastNpcSay(Text[9].replace("PLAYERNAME", player.getName()));
					else
						npc.broadcastNpcSay(Text[10].replace("PLAYERNAME", player.getName()));
				}
				return null;
			}
			else if (event.equalsIgnoreCase("Mob_2 has despawned"))
			{
				st.set("Quest1", String.valueOf(st.getInt("Quest1") + 1));
				if (st.getInt("Quest0") == 1 || st.getInt("Quest0") == 2 || st.getInt("Quest1") > 3)
				{
					st.set("Quest0", "0");
					if (st.getInt("Quest0") == 1)
						npc.broadcastNpcSay(Text[11]);
					else
						npc.broadcastNpcSay(Text[12]);
					DeleteSpawn(st, npc);
				}
				else
					startQuestTimer("Mob_2 has despawned", 1000, npc, player, false);
				return null;
			}
		}
		else
			return null;
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(getName());
		if (st != null)
		{
			int npcId = npc.getNpcId();
			int cond = st.getInt("cond");
			if (st.isCompleted() && npcId == NPC[0])
				htmltext = getAlreadyCompletedMsg();
			else if (player.getClassId().getId() == getPrevClass(player))
			{
				if (cond == 0)
				{
					if (npcId == NPC[0])
						htmltext = "0-01.htm";
				}
				else if (cond == 1)
				{
					if (npcId == NPC[0])
						htmltext = "0-04.htm";
					else if (npcId == NPC[2])
						htmltext = "2-01.htm";
				}
				else if (cond == 2)
				{
					if (npcId == NPC[2])
						htmltext = "2-02.htm";
					else if (npcId == NPC[1])
						htmltext = "1-01.htm";
				}
				else if (cond == 3)
				{
					if (npcId == NPC[1] && st.hasQuestItems(Items[0]))
					{
						htmltext = "1-02.htm";
						if (Items[11] == 0 || st.hasQuestItems(Items[11]))
							htmltext = "1-03.htm";
					}
				}
				else if (cond == 4)
				{
					if (npcId == NPC[1])
						htmltext = "1-04.htm";
					else if (npcId == NPC[2])
						htmltext = "2-03.htm";
				}
				else if (cond == 5)
				{
					if (npcId == NPC[2])
						htmltext = "2-04.htm";
					else if (npcId == NPC[5])
						htmltext = "5-01.htm";
				}
				else if (cond == 6)
				{
					if (npcId == NPC[5])
						htmltext = "5-03.htm";
					else if (npcId == NPC[6])
						htmltext = "6-01.htm";
				}
				else if (cond == 7)
				{
					if (npcId == NPC[6])
						htmltext = "6-02.htm";
				}
				else if (cond == 8)
				{
					if (npcId == NPC[6])
						htmltext = "6-04.htm";
					else if (npcId == NPC[7])
						htmltext = "7-01.htm";
				}
				else if (cond == 9)
				{
					if (npcId == NPC[7])
						htmltext = "7-05.htm";
				}
				else if (cond == 10)
				{
					if (npcId == NPC[7])
						htmltext = "7-07.htm";
					else if (npcId == NPC[3])
						htmltext = "3-01.htm";
				}
				else if (cond == 11 || cond == 12)
				{
					if (npcId == NPC[3])
					{
						if (st.hasQuestItems(Items[2]))
							htmltext = "3-05.htm";
						else
							htmltext = "3-04.htm";
					}
				}
				else if (cond == 13)
				{
					if (npcId == NPC[3])
						htmltext = "3-06.htm";
					else if (npcId == NPC[8])
						htmltext = "8-01.htm";
				}
				else if (cond == 14)
				{
					if (npcId == NPC[8])
						htmltext = "8-03.htm";
					else if (npcId == NPC[11])
						htmltext = "11-01.htm";
				}
				else if (cond == 15)
				{
					if (npcId == NPC[11])
						htmltext = "11-02.htm";
					else if (npcId == NPC[9])
						htmltext = "9-01.htm";
				}
				else if (cond == 16)
				{
					if (npcId == NPC[9])
						htmltext = "9-02.htm";
				}
				else if (cond == 17)
				{
					if (npcId == NPC[9])
						htmltext = "9-04.htm";
					else if (npcId == NPC[10])
						htmltext = "10-01.htm";
				}
				else if (cond == 18)
				{
					if (npcId == NPC[10])
						htmltext = "10-05.htm";
				}
				else if (cond == 19)
				{
					if (npcId == NPC[10])
						htmltext = "10-07.htm";
					else if (npcId == NPC[0])
						htmltext = "0-06.htm";
				}
				else if (cond == 20)
				{
					if (npcId == NPC[0])
					{
						if (player.getLevel() >= 76)
						{
							htmltext = "0-09.htm";
							st.exitQuest(false);
							st.set("cond", "0");
							st.rewardExpAndSp(2299404, 0);
							st.giveItems(57, 5000000);
							st.giveItems(6622, 1);
							int Class = getClassId(player);
							int prevClass = getPrevClass(player);
							player.setClassId(Class);
							if (!player.isSubClassActive() && player.getBaseClass() == prevClass)
								player.setBaseClass(Class);
							player.broadcastUserInfo();
							cast(npc, player, 4339, 1);
						}
						else
							htmltext = "0-010.htm";
					}
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		int npcId = npc.getNpcId();
		if (st != null)
		{
			int cond = st.getInt("cond");
			if (npcId == NPC[4])
			{
				if (cond == 17)
				{
					QuestState st2 = findRightState(npc);
					if (st2 != null)
					{
						player.setLastQuestNpcObject(npc.getObjectId());
						if (st == st2)
						{
							if (st.getInt("Tab") == 1)
							{
								if (st.getInt("Quest0") == 0)
									htmltext = "4-04.htm";
								else if (st.getInt("Quest0") == 1)
									htmltext = "4-06.htm";
							}
							else
							{
								if (st.getInt("Quest0") == 0)
									htmltext = "4-01.htm";
								else if (st.getInt("Quest0") == 1)
									htmltext = "4-03.htm";
							}
						}
						else
						{
							if (st.getInt("Tab") == 1)
							{
								if (st.getInt("Quest0") == 0)
									htmltext = "4-05.htm";
								else if (st.getInt("Quest0") == 1)
									htmltext = "4-07.htm";
							}
							else
							{
								if (st.getInt("Quest0") == 0)
									htmltext = "4-02.htm";
							}
						}
					}
				}
				else if (cond == 18)
					htmltext = "4-08.htm";
			}
		}
		if (htmltext == "")
			npc.showChatWindow(player);
		return htmltext;
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		final Player player = attacker.getActingPlayer();
		if (player != null)
		{
			QuestState st2 = findRightState(npc);
			if (st2 == null)
				return super.onAttack(npc, attacker, damage, skill);
			
			int cond = st2.getInt("cond");
			QuestState st = player.getQuestState(getName());
			int npcId = npc.getNpcId();
			if (npcId == Mob[2] && st == st2 && cond == 17)
			{
				st.set("Quest0", String.valueOf(st.getInt("Quest0") + 1));
				if (st.getInt("Quest0") == 1)
					npc.broadcastNpcSay(Text[16].replace("PLAYERNAME", player.getName()));
				if (st.getInt("Quest0") > 15)
				{
					st.set("Quest0", "1");
					npc.broadcastNpcSay(Text[17]);
					cancelQuestTimer("Mob_3 has despawned", npc, st2.getPlayer());
					st.set("Tab", "1");
					DeleteSpawn(st, npc);
				}
			}
			else if (npcId == Mob[1] && cond == 15)
			{
				if (st != st2 || (st == st2 && player.isInParty()))
				{
					npc.broadcastNpcSay(Text[5]);
					cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
					st2.set("spawned", "0");
					DeleteSpawn(st2, npc);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
	
	@Override
	public String onSkillSee(Npc npc, Player player, L2Skill skill, WorldObject[] targets, boolean isPet)
	{
		if (_SpawnList.containsKey(npc) && _SpawnList.get(npc) != player.getObjectId())
		{
			Player quest_player = World.getInstance().getPlayer(_SpawnList.get(npc));
			if (quest_player == null)
				return null;
			
			for (WorldObject obj : targets)
			{
				if (obj == quest_player || obj == npc)
				{
					QuestState st2 = findRightState(npc);
					if (st2 == null)
						return null;
					
					npc.broadcastNpcSay(Text[5]);
					cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
					st2.set("spawned", "0");
					DeleteSpawn(st2, npc);
				}
			}
		}
		return super.onSkillSee(npc, player, skill, targets, isPet);
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		if (player == null)
			return super.onKill(npc, player);
		
		int npcId = npc.getNpcId();
		
		for (int Archon_Minion = 21646; Archon_Minion < 21652; Archon_Minion++)
		{
			if (npcId == Archon_Minion)
			{
				Party party = player.getParty();
				if (party != null)
				{
					List<QuestState> PartyQuestMembers = new ArrayList<>();
					for (Player player1 : party.getMembers())
					{
						QuestState st1 = findQuest(player1);
						if (st1 != null && player1.isInsideRadius(player, Config.PARTY_RANGE, false, false))
						{
							if (st1.getInt("cond") == 15)
								PartyQuestMembers.add(st1);
						}
					}
					if (PartyQuestMembers.size() > 0)
					{
						QuestState st2 = Rnd.get(PartyQuestMembers);
						giveHallishaMark(st2);
					}
				}
				else
				{
					QuestState st1 = findQuest(player);
					if (st1 != null)
					{
						if (st1.getInt("cond") == 15)
							giveHallishaMark(st1);
					}
				}
				return super.onKill(npc, player);
			}
		}
		
		for (int element : Archon_Hellisha_Norm)
		{
			if (npcId == element)
			{
				QuestState st1 = findQuest(player);
				if (st1 != null)
				{
					if (st1.getInt("cond") == 15)
					{
						npc.broadcastNpcSay(Text[4]);
						st1.giveItems(Items[8], 1);
						st1.takeItems(Items[3], -1);
						st1.set("cond", "16");
						st1.playSound(QuestState.SOUND_MIDDLE);
					}
				}
				return super.onKill(npc, player);
			}
		}
		
		for (int Guardian_Angel = 27214; Guardian_Angel < 27217; Guardian_Angel++)
		{
			if (npcId == Guardian_Angel)
			{
				QuestState st1 = findQuest(player);
				if (st1 != null)
				{
					if (st1.getInt("cond") == 6)
					{
						if (st1.getInt("kills") < 9)
							st1.set("kills", String.valueOf(st1.getInt("kills") + 1));
						else
						{
							st1.playSound(QuestState.SOUND_MIDDLE);
							st1.giveItems(Items[5], 1);
							st1.set("cond", "7");
						}
					}
				}
				return super.onKill(npc, player);
			}
		}
		
		QuestState st = player.getQuestState(getName());
		if (st != null && npcId != Mob[2])
		{
			QuestState st2 = findRightState(npc);
			if (st2 == null)
				return super.onKill(npc, player);
			
			int cond = st.getInt("cond");
			if (npcId == Mob[0] && cond == 8)
			{
				if (!player.isInParty())
				{
					if (st == st2)
					{
						npc.broadcastNpcSay(Text[12]);
						st.giveItems(Items[6], 1);
						st.set("cond", "9");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
				}
				cancelQuestTimer("Mob_1 has despawned", npc, st2.getPlayer());
				st2.set("spawned", "0");
				DeleteSpawn(st2, npc);
			}
			else if (npcId == Mob[1] && cond == 15)
			{
				if (!player.isInParty())
				{
					if (st == st2)
					{
						npc.broadcastNpcSay(Text[4]);
						st.giveItems(Items[8], 1);
						st.takeItems(Items[3], -1);
						st.set("cond", "16");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						npc.broadcastNpcSay(Text[5]);
				}
				cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
				st2.set("spawned", "0");
				DeleteSpawn(st2, npc);
			}
		}
		else
		{
			if (npcId == Mob[0])
			{
				st = findRightState(npc);
				if (st != null)
				{
					cancelQuestTimer("Mob_1 has despawned", npc, st.getPlayer());
					st.set("spawned", "0");
					DeleteSpawn(st, npc);
				}
			}
			else if (npcId == Mob[1])
			{
				st = findRightState(npc);
				if (st != null)
				{
					cancelQuestTimer("Archon Hellisha has despawned", npc, st.getPlayer());
					st.set("spawned", "0");
					DeleteSpawn(st, npc);
				}
			}
		}
		return super.onKill(npc, player);
	}
}
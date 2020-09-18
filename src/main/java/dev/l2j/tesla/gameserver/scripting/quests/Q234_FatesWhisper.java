package dev.l2j.tesla.gameserver.scripting.quests;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.data.ItemTable;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;

public class Q234_FatesWhisper extends Quest
{
	private static final String qn = "Q234_FatesWhisper";
	
	// Items
	private static final int REIRIA_SOUL_ORB = 4666;
	private static final int KERMON_INFERNIUM_SCEPTER = 4667;
	private static final int GOLKONDA_INFERNIUM_SCEPTER = 4668;
	private static final int HALLATE_INFERNIUM_SCEPTER = 4669;
	
	private static final int INFERNIUM_VARNISH = 4672;
	private static final int REORIN_HAMMER = 4670;
	private static final int REORIN_MOLD = 4671;
	
	private static final int PIPETTE_KNIFE = 4665;
	private static final int RED_PIPETTE_KNIFE = 4673;
	
	private static final int CRYSTAL_B = 1460;
	
	// Reward
	private static final int STAR_OF_DESTINY = 5011;
	
	// Chest Spawn
	private static final Map<Integer, Integer> CHEST_SPAWN = new HashMap<>();
	{
		CHEST_SPAWN.put(25035, 31027);
		CHEST_SPAWN.put(25054, 31028);
		CHEST_SPAWN.put(25126, 31029);
		CHEST_SPAWN.put(25220, 31030);
	}
	
	// Weapons
	private static final Map<Integer, String> WEAPONS = new HashMap<>();
	{
		WEAPONS.put(79, "Sword of Damascus");
		WEAPONS.put(97, "Lance");
		WEAPONS.put(171, "Deadman's Glory");
		WEAPONS.put(175, "Art of Battle Axe");
		WEAPONS.put(210, "Staff of Evil Spirits");
		WEAPONS.put(234, "Demon Dagger");
		WEAPONS.put(268, "Bellion Cestus");
		WEAPONS.put(287, "Bow of Peril");
		WEAPONS.put(2626, "Samurai Dual-sword");
		WEAPONS.put(7883, "Guardian Sword");
		WEAPONS.put(7889, "Wizard's Tear");
		WEAPONS.put(7893, "Kaim Vanul's Bones");
		WEAPONS.put(7901, "Star Buster");
	}
	
	public Q234_FatesWhisper()
	{
		super(234, "Fate's Whispers");
		
		setItemsIds(PIPETTE_KNIFE, RED_PIPETTE_KNIFE);
		
		addStartNpc(31002);
		addTalkId(31002, 30182, 30847, 30178, 30833, 31028, 31029, 31030, 31027);
		
		// The 4 bosses which spawn chests
		addKillId(25035, 25054, 25126, 25220);
		
		// Baium
		addAttackId(29020);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31002-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30182-01c.htm"))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(INFERNIUM_VARNISH, 1);
		}
		else if (event.equalsIgnoreCase("30178-01a.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30833-01b.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(PIPETTE_KNIFE, 1);
		}
		else if (event.startsWith("selectBGrade_"))
		{
			if (st.getInt("bypass") == 1)
				return null;
			
			String bGradeId = event.replace("selectBGrade_", "");
			st.set("weaponId", bGradeId);
			htmltext = getHtmlText("31002-13.htm").replace("%weaponname%", WEAPONS.get(st.getInt("weaponId")));
		}
		else if (event.startsWith("confirmWeapon"))
		{
			st.set("bypass", "1");
			htmltext = getHtmlText("31002-14.htm").replace("%weaponname%", WEAPONS.get(st.getInt("weaponId")));
		}
		else if (event.startsWith("selectAGrade_"))
		{
			if (st.getInt("bypass") == 1)
			{
				final int itemId = st.getInt("weaponId");
				if (st.hasQuestItems(itemId))
				{
					int aGradeItemId = Integer.parseInt(event.replace("selectAGrade_", ""));
					
					htmltext = getHtmlText("31002-12.htm").replace("%weaponname%", ItemTable.getInstance().getTemplate(aGradeItemId).getName());
					st.takeItems(itemId, 1);
					st.giveItems(aGradeItemId, 1);
					st.giveItems(STAR_OF_DESTINY, 1);
					player.broadcastPacket(new SocialAction(player, 3));
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				else
					htmltext = getHtmlText("31002-15.htm").replace("%weaponname%", WEAPONS.get(itemId));
			}
			else
				htmltext = "31002-16.htm";
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
				htmltext = (player.getLevel() < 75) ? "31002-01.htm" : "31002-02.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case 31002:
						if (cond == 1)
						{
							if (!st.hasQuestItems(REIRIA_SOUL_ORB))
								htmltext = "31002-04b.htm";
							else
							{
								htmltext = "31002-05.htm";
								st.set("cond", "2");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(REIRIA_SOUL_ORB, 1);
							}
						}
						else if (cond == 2)
						{
							if (!st.hasQuestItems(KERMON_INFERNIUM_SCEPTER) || !st.hasQuestItems(GOLKONDA_INFERNIUM_SCEPTER) || !st.hasQuestItems(HALLATE_INFERNIUM_SCEPTER))
								htmltext = "31002-05c.htm";
							else
							{
								htmltext = "31002-06.htm";
								st.set("cond", "3");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(GOLKONDA_INFERNIUM_SCEPTER, 1);
								st.takeItems(HALLATE_INFERNIUM_SCEPTER, 1);
								st.takeItems(KERMON_INFERNIUM_SCEPTER, 1);
							}
						}
						else if (cond == 3)
						{
							if (!st.hasQuestItems(INFERNIUM_VARNISH))
								htmltext = "31002-06b.htm";
							else
							{
								htmltext = "31002-07.htm";
								st.set("cond", "4");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(INFERNIUM_VARNISH, 1);
							}
						}
						else if (cond == 4)
						{
							if (!st.hasQuestItems(REORIN_HAMMER))
								htmltext = "31002-07b.htm";
							else
							{
								htmltext = "31002-08.htm";
								st.set("cond", "5");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(REORIN_HAMMER, 1);
							}
						}
						else if (cond > 4 && cond < 8)
							htmltext = "31002-08b.htm";
						else if (cond == 8)
						{
							htmltext = "31002-09.htm";
							st.set("cond", "9");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(REORIN_MOLD, 1);
						}
						else if (cond == 9)
						{
							if (st.getQuestItemsCount(CRYSTAL_B) < 984)
								htmltext = "31002-09b.htm";
							else
							{
								htmltext = "31002-BGradeList.htm";
								st.set("cond", "10");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(CRYSTAL_B, 984);
							}
						}
						else if (cond == 10)
						{
							// If a weapon is selected
							if (st.getInt("bypass") == 1)
							{
								// If you got it in the inventory
								final int itemId = st.getInt("weaponId");
								htmltext = getHtmlText((st.hasQuestItems(itemId)) ? "31002-AGradeList.htm" : "31002-15.htm").replace("%weaponname%", WEAPONS.get(itemId));
							}
							// B weapon is still not selected
							else
								htmltext = "31002-BGradeList.htm";
						}
						break;
					
					case 30182:
						if (cond == 3)
							htmltext = (!st.hasQuestItems(INFERNIUM_VARNISH)) ? "30182-01.htm" : "30182-02.htm";
						break;
					
					case 30847:
						if (cond == 4 && !st.hasQuestItems(REORIN_HAMMER))
						{
							htmltext = "30847-01.htm";
							st.playSound(QuestState.SOUND_ITEMGET);
							st.giveItems(REORIN_HAMMER, 1);
						}
						else if (cond >= 4 && st.hasQuestItems(REORIN_HAMMER))
							htmltext = "30847-02.htm";
						break;
					
					case 30178:
						if (cond == 5)
							htmltext = "30178-01.htm";
						else if (cond > 5)
							htmltext = "30178-02.htm";
						break;
					
					case 30833:
						if (cond == 6)
							htmltext = "30833-01.htm";
						else if (cond == 7)
						{
							if (st.hasQuestItems(PIPETTE_KNIFE) && !st.hasQuestItems(RED_PIPETTE_KNIFE))
								htmltext = "30833-02.htm";
							else
							{
								htmltext = "30833-03.htm";
								st.set("cond", "8");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(RED_PIPETTE_KNIFE, 1);
								st.giveItems(REORIN_MOLD, 1);
							}
						}
						else if (cond > 7)
							htmltext = "30833-04.htm";
						break;
					
					case 31027:
						if (cond == 1 && !st.hasQuestItems(REIRIA_SOUL_ORB))
						{
							htmltext = "31027-01.htm";
							st.playSound(QuestState.SOUND_ITEMGET);
							st.giveItems(REIRIA_SOUL_ORB, 1);
						}
						else
							htmltext = "31027-02.htm";
						break;
					
					case 31028:
					case 31029:
					case 31030:
						final int itemId = npc.getNpcId() - 26361;
						if (cond == 2 && !st.hasQuestItems(itemId))
						{
							htmltext = npc.getNpcId() + "-01.htm";
							st.playSound(QuestState.SOUND_ITEMGET);
							st.giveItems(itemId, 1);
						}
						else
							htmltext = npc.getNpcId() + "-02.htm";
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
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "7");
		if (st == null)
			return null;
		
		if (player.getActiveWeaponItem() != null && player.getActiveWeaponItem().getItemId() == PIPETTE_KNIFE && !st.hasQuestItems(RED_PIPETTE_KNIFE))
		{
			st.playSound(QuestState.SOUND_ITEMGET);
			st.takeItems(PIPETTE_KNIFE, 1);
			st.giveItems(RED_PIPETTE_KNIFE, 1);
		}
		
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		addSpawn(CHEST_SPAWN.get(npc.getNpcId()), npc, true, 120000, false);
		
		return null;
	}
}
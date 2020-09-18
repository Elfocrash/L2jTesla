package dev.l2j.tesla.gameserver.scripting.scripts.custom;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.commons.util.ArraysUtil;

import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

public class HeroWeapon extends Quest
{
	private static final int[] WEAPON_IDS =
	{
		6611,
		6612,
		6613,
		6614,
		6615,
		6616,
		6617,
		6618,
		6619,
		6620,
		6621
	};
	
	public HeroWeapon()
	{
		super(-1, "custom");
		
		addStartNpc(31690, 31769, 31770, 31771, 31772, 31773);
		addTalkId(31690, 31769, 31770, 31771, 31772, 31773);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		QuestState st = player.getQuestState(getName());
		
		int weaponId = Integer.valueOf(event);
		if (ArraysUtil.contains(WEAPON_IDS, weaponId))
			st.giveItems(weaponId, 1);
		
		st.exitQuest(true);
		return null;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			newQuestState(player);
		
		if (st != null)
		{
			if (player.isHero())
			{
				if (hasHeroWeapon(player))
				{
					htmltext = "already_have_weapon.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "weapon_list.htm";
			}
			else
			{
				htmltext = "no_hero.htm";
				st.exitQuest(true);
			}
		}
		
		return htmltext;
	}
	
	private static boolean hasHeroWeapon(Player player)
	{
		for (int i : WEAPON_IDS)
		{
			if (player.getInventory().getItemByItemId(i) != null)
				return true;
		}
		
		return false;
	}
}
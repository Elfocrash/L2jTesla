package dev.l2j.tesla.gameserver.scripting.scripts.custom;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.scripting.scripts.village_master.FirstClassChange;
import dev.l2j.tesla.gameserver.scripting.scripts.village_master.SecondClassChange;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;

/**
 * @authors: DrLecter (python), Nyaran (java)
 */
public class ShadowWeapon extends Quest
{
	private static final String qn = "ShadowWeapon";
	
	// itemId for shadow weapon coupons, it's not used more than once but increases readability
	private static final int D_COUPON = 8869;
	private static final int C_COUPON = 8870;
	
	public ShadowWeapon()
	{
		super(-1, "custom");
		
		addStartNpc(FirstClassChange.FIRSTCLASSNPCS);
		addTalkId(FirstClassChange.FIRSTCLASSNPCS);
		
		addStartNpc(SecondClassChange.SECONDCLASSNPCS);
		addTalkId(SecondClassChange.SECONDCLASSNPCS);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		boolean hasD = st.hasQuestItems(D_COUPON);
		boolean hasC = st.hasQuestItems(C_COUPON);
		
		if (hasD || hasC)
		{
			// let's assume character had both c & d-grade coupons, we'll confirm later
			String multisell = "306893003";
			if (!hasD) // if s/he had c-grade only...
				multisell = "306893002";
			else if (!hasC) // or d-grade only.
				multisell = "306893001";
			
			// finally, return htm with proper multisell value in it.
			htmltext = getHtmlText("exchange.htm").replace("%msid%", multisell);
		}
		else
			htmltext = "exchange-no.htm";
		
		st.exitQuest(true);
		return htmltext;
	}
}
package dev.l2j.tesla.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.data.xml.MultisellData;

/**
 * This instance leads behaviors of Golden Ram mofos, where shown htm is different according to your quest condition. Abercrombie shows you multisells, Selina shows you Buffs list, when Pierce shows you "Quest" link.<br>
 * <br>
 * Kahman shows you only different htm. He's enthusiastic lazy-ass.
 */
public class GoldenRamMercenary extends Folk
{
	private static final String qn = "Q628_HuntOfTheGoldenRamMercenaryForce";
	
	private static final int[][] data =
	{
		{
			4404,
			2,
			2
		},
		{
			4405,
			2,
			2
		},
		{
			4393,
			3,
			3
		},
		{
			4400,
			2,
			3
		},
		{
			4397,
			1,
			3
		},
		{
			4399,
			2,
			3
		},
		{
			4401,
			1,
			6
		},
		{
			4402,
			2,
			6
		}
	};
	
	private static final int GOLDEN_RAM = 7251;
	
	public GoldenRamMercenary(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		int npcId = getNpcId();
		String filename = "data/html/default/" + npcId + ".htm";
		
		final QuestState st = player.getQuestState(qn);
		if (st != null)
		{
			int cond = st.getInt("cond");
			
			switch (npcId)
			{
				case 31553:
				case 31554:
					// Captain Pierce && Kahman ; different behavior if you got at least one badge.
					if (cond >= 2)
						filename = "data/html/default/" + npcId + "-1.htm";
					break;
				
				case 31555:
				case 31556:
					// Abercrombie and Selina
					if (cond == 2)
						filename = "data/html/default/" + npcId + "-1.htm";
					else if (cond == 3)
						filename = "data/html/default/" + npcId + "-2.htm";
					break;
			}
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final QuestState qs = player.getQuestState(qn);
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		if (actualCommand.contains("buff"))
		{
			if (qs != null && qs.getInt("cond") == 3)
			{
				// Search the next token, which is a number between 0 and 7.
				int[] buffData = data[Integer.valueOf(st.nextToken())];
				
				int coins = buffData[2];
				int val = 3;
				
				if (qs.getQuestItemsCount(GOLDEN_RAM) >= coins)
				{
					qs.takeItems(GOLDEN_RAM, coins);
					setTarget(player);
					doCast(SkillTable.getInstance().getInfo(buffData[0], buffData[1]));
					val = 4;
				}
				
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/default/31556-" + val + ".htm");
				player.sendPacket(html);
				return;
			}
		}
		else if (command.startsWith("gmultisell"))
		{
			if (qs != null && qs.getInt("cond") == 3)
				MultisellData.getInstance().separateAndSend(command.substring(10).trim(), player, this, false);
		}
		else
			super.onBypassFeedback(player, command);
	}
}
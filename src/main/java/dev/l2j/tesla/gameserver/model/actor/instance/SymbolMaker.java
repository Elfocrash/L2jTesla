package dev.l2j.tesla.gameserver.model.actor.instance;

import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.HennaEquipList;
import dev.l2j.tesla.gameserver.network.serverpackets.HennaRemoveList;

public class SymbolMaker extends Folk
{
	public SymbolMaker(int objectID, NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.equals("Draw"))
			player.sendPacket(new HennaEquipList(player));
		else if (command.equals("RemoveList"))
		{
			if (player.getHennaList().isEmpty())
			{
				player.sendPacket(SystemMessageId.SYMBOL_NOT_FOUND);
				return;
			}
			
			player.sendPacket(new HennaRemoveList(player));
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/symbolmaker/SymbolMaker.htm";
	}
}
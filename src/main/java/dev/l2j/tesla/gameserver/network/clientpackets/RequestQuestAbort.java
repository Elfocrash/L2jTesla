package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.data.xml.ScriptData;
import dev.l2j.tesla.gameserver.model.actor.Player;

public final class RequestQuestAbort extends L2GameClientPacket
{
	private int _questId;
	
	@Override
	protected void readImpl()
	{
		_questId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getPlayer();
		if (activeChar == null)
			return;
		
		final Quest qe = ScriptData.getInstance().getQuest(_questId);
		if (qe == null)
			return;
		
		final QuestState qs = activeChar.getQuestState(qe.getName());
		if (qs != null)
			qs.exitQuest(true);
	}
}
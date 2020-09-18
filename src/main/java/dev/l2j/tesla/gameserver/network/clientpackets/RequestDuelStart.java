package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.ExDuelAskStart;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.group.CommandChannel;
import dev.l2j.tesla.gameserver.model.group.Party;

public final class RequestDuelStart extends L2GameClientPacket
{
	private String _player;
	private boolean _isPartyDuel;
	
	@Override
	protected void readImpl()
	{
		_player = readS();
		_isPartyDuel = readD() == 1;
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getPlayer();
		if (activeChar == null)
			return;
		
		final Player targetChar = World.getInstance().getPlayer(_player);
		if (targetChar == null || activeChar == targetChar)
		{
			activeChar.sendPacket(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
			return;
		}
		
		// Check if duel is possible.
		if (!activeChar.canDuel())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return;
		}
		
		if (!targetChar.canDuel())
		{
			activeChar.sendPacket(targetChar.getNoDuelReason());
			return;
		}
		
		// Players musn't be too far.
		if (!activeChar.isInsideRadius(targetChar, 2000, false, false))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY).addCharName(targetChar));
			return;
		}
		
		// Duel is a party duel.
		if (_isPartyDuel)
		{
			// Player must be a party leader, the target can't be of the same party.
			final Party activeCharParty = activeChar.getParty();
			if (activeCharParty == null || !activeCharParty.isLeader(activeChar) || activeCharParty.containsPlayer(targetChar))
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
				return;
			}
			
			// Target must be in a party.
			final Party targetCharParty = targetChar.getParty();
			if (targetCharParty == null)
			{
				activeChar.sendPacket(SystemMessageId.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY);
				return;
			}
			
			// Check if every player is ready for a duel.
			for (Player member : activeCharParty.getMembers())
			{
				if (member != activeChar && !member.canDuel())
				{
					activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
					return;
				}
			}
			
			for (Player member : targetCharParty.getMembers())
			{
				if (member != targetChar && !member.canDuel())
				{
					activeChar.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL);
					return;
				}
			}
			
			final Player partyLeader = targetCharParty.getLeader();
			
			// Send request to targetChar's party leader.
			if (!partyLeader.isProcessingRequest())
			{
				// Drop command channels, for both requestor && player parties.
				final CommandChannel activeCharChannel = activeCharParty.getCommandChannel();
				if (activeCharChannel != null)
					activeCharChannel.removeParty(activeCharParty);
				
				final CommandChannel targetCharChannel = targetCharParty.getCommandChannel();
				if (targetCharChannel != null)
					targetCharChannel.removeParty(targetCharParty);
				
				// Partymatching
				for (Player member : activeCharParty.getMembers())
					member.removeMeFromPartyMatch();
				
				for (Player member : targetCharParty.getMembers())
					member.removeMeFromPartyMatch();
				
				activeChar.onTransactionRequest(partyLeader);
				partyLeader.sendPacket(new ExDuelAskStart(activeChar.getName(), _isPartyDuel));
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL).addCharName(partyLeader));
				targetChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL).addCharName(activeChar));
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addCharName(partyLeader));
		}
		// 1vs1 duel.
		else
		{
			if (!targetChar.isProcessingRequest())
			{
				// Partymatching
				activeChar.removeMeFromPartyMatch();
				targetChar.removeMeFromPartyMatch();
				
				activeChar.onTransactionRequest(targetChar);
				targetChar.sendPacket(new ExDuelAskStart(activeChar.getName(), _isPartyDuel));
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_CHALLENGED_TO_A_DUEL).addCharName(targetChar));
				targetChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_CHALLENGED_YOU_TO_A_DUEL).addCharName(activeChar));
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addCharName(targetChar));
		}
	}
}
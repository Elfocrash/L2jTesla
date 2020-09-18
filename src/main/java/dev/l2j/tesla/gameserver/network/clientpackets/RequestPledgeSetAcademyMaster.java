package dev.l2j.tesla.gameserver.network.clientpackets;

import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.pledge.Clan;
import dev.l2j.tesla.gameserver.model.pledge.ClanMember;

/**
 * Format: (ch) dSS
 * @author -Wooden-
 */
public final class RequestPledgeSetAcademyMaster extends L2GameClientPacket
{
	private String _currPlayerName;
	private int _set; // 1 set, 0 delete
	private String _targetPlayerName;
	
	@Override
	protected void readImpl()
	{
		_set = readD();
		_currPlayerName = readS();
		_targetPlayerName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getPlayer();
		if (activeChar == null)
			return;
		
		final Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		
		if ((activeChar.getClanPrivileges() & Clan.CP_CL_MASTER_RIGHTS) != Clan.CP_CL_MASTER_RIGHTS)
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_DISMISS_AN_APPRENTICE);
			return;
		}
		
		final ClanMember currentMember = clan.getClanMember(_currPlayerName);
		final ClanMember targetMember = clan.getClanMember(_targetPlayerName);
		if (currentMember == null || targetMember == null)
			return;
		
		ClanMember apprenticeMember, sponsorMember;
		if (currentMember.getPledgeType() == Clan.SUBUNIT_ACADEMY)
		{
			apprenticeMember = currentMember;
			sponsorMember = targetMember;
		}
		else
		{
			apprenticeMember = targetMember;
			sponsorMember = currentMember;
		}
		
		final Player apprentice = apprenticeMember.getPlayerInstance();
		final Player sponsor = sponsorMember.getPlayerInstance();
		
		SystemMessage sm = null;
		if (_set == 0)
		{
			// test: do we get the current sponsor & apprentice from this packet or no?
			if (apprentice != null)
				apprentice.setSponsor(0);
			else
				// offline
				apprenticeMember.setApprenticeAndSponsor(0, 0);
			
			if (sponsor != null)
				sponsor.setApprentice(0);
			else
				// offline
				sponsorMember.setApprenticeAndSponsor(0, 0);
			
			apprenticeMember.saveApprenticeAndSponsor(0, 0);
			sponsorMember.saveApprenticeAndSponsor(0, 0);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_CLAN_MEMBER_S1_APPRENTICE_HAS_BEEN_REMOVED);
		}
		else
		{
			if (apprenticeMember.getSponsor() != 0 || sponsorMember.getApprentice() != 0 || apprenticeMember.getApprentice() != 0 || sponsorMember.getSponsor() != 0)
			{
				activeChar.sendMessage("Remove previous connections first.");
				return;
			}
			
			if (apprentice != null)
				apprentice.setSponsor(sponsorMember.getObjectId());
			else
				// offline
				apprenticeMember.setApprenticeAndSponsor(0, sponsorMember.getObjectId());
			
			if (sponsor != null)
				sponsor.setApprentice(apprenticeMember.getObjectId());
			else
				// offline
				sponsorMember.setApprenticeAndSponsor(apprenticeMember.getObjectId(), 0);
			
			// saving to database even if online, since both must match
			apprenticeMember.saveApprenticeAndSponsor(0, sponsorMember.getObjectId());
			sponsorMember.saveApprenticeAndSponsor(apprenticeMember.getObjectId(), 0);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HAS_BEEN_DESIGNATED_AS_APPRENTICE_OF_CLAN_MEMBER_S1);
		}
		sm.addString(sponsorMember.getName());
		sm.addString(apprenticeMember.getName());
		
		if (sponsor != activeChar && sponsor != apprentice)
			activeChar.sendPacket(sm);
		
		if (sponsor != null)
			sponsor.sendPacket(sm);
		
		if (apprentice != null)
			apprentice.sendPacket(sm);
		
		clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(sponsorMember), new PledgeShowMemberListUpdate(apprenticeMember));
	}
}
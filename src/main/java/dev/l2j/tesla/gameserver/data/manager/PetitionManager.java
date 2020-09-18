package dev.l2j.tesla.gameserver.data.manager;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.gameserver.enums.PetitionState;
import dev.l2j.tesla.gameserver.model.Petition;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.clientpackets.Say2;
import dev.l2j.tesla.gameserver.network.serverpackets.CreatureSay;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.data.xml.AdminData;

/**
 * Store all existing {@link Petition}s, being pending or completed.
 */
public final class PetitionManager
{
	private final Map<Integer, Petition> _pendingPetitions = new ConcurrentHashMap<>();
	private final Map<Integer, Petition> _completedPetitions = new ConcurrentHashMap<>();
	
	protected PetitionManager()
	{
	}
	
	public Map<Integer, Petition> getCompletedPetitions()
	{
		return _completedPetitions;
	}
	
	public Map<Integer, Petition> getPendingPetitions()
	{
		return _pendingPetitions;
	}
	
	public int getPlayerTotalPetitionCount(Player player)
	{
		if (player == null)
			return 0;
		
		int petitionCount = 0;
		
		for (Petition petition : _pendingPetitions.values())
		{
			if (petition.getPetitioner() != null && petition.getPetitioner().getObjectId() == player.getObjectId())
				petitionCount++;
		}
		
		for (Petition petition : _completedPetitions.values())
		{
			if (petition.getPetitioner() != null && petition.getPetitioner().getObjectId() == player.getObjectId())
				petitionCount++;
		}
		
		return petitionCount;
	}
	
	public boolean isPetitionInProcess()
	{
		for (Petition petition : _pendingPetitions.values())
		{
			if (petition.getState() == PetitionState.IN_PROCESS)
				return true;
		}
		return false;
	}
	
	public boolean isPetitionInProcess(int id)
	{
		final Petition petition = _pendingPetitions.get(id);
		return petition != null && petition.getState() == PetitionState.IN_PROCESS;
	}
	
	public boolean isPlayerInConsultation(Player player)
	{
		if (player == null)
			return false;
		
		for (Petition petition : _pendingPetitions.values())
		{
			if (petition.getState() != PetitionState.IN_PROCESS)
				continue;
			
			if ((petition.getPetitioner() != null && petition.getPetitioner().getObjectId() == player.getObjectId()) || (petition.getResponder() != null && petition.getResponder().getObjectId() == player.getObjectId()))
				return true;
		}
		return false;
	}
	
	public boolean isPlayerPetitionPending(Player player)
	{
		if (player == null)
			return false;
		
		for (Petition petition : _pendingPetitions.values())
		{
			if (petition.getPetitioner() != null && petition.getPetitioner().getObjectId() == player.getObjectId())
				return true;
		}
		return false;
	}
	
	public boolean rejectPetition(Player player, int id)
	{
		final Petition petition = _pendingPetitions.get(id);
		if (petition == null || petition.getResponder() != null)
			return false;
		
		petition.setResponder(player);
		return petition.endPetitionConsultation(PetitionState.RESPONDER_REJECT);
	}
	
	public boolean sendActivePetitionMessage(Player player, String messageText)
	{
		CreatureSay cs;
		
		for (Petition petition : _pendingPetitions.values())
		{
			if (petition.getPetitioner() != null && petition.getPetitioner().getObjectId() == player.getObjectId())
			{
				cs = new CreatureSay(player.getObjectId(), Say2.PETITION_PLAYER, player.getName(), messageText);
				petition.addLogMessage(cs);
				
				petition.sendResponderPacket(cs);
				petition.sendPetitionerPacket(cs);
				return true;
			}
			
			if (petition.getResponder() != null && petition.getResponder().getObjectId() == player.getObjectId())
			{
				cs = new CreatureSay(player.getObjectId(), Say2.PETITION_GM, player.getName(), messageText);
				petition.addLogMessage(cs);
				
				petition.sendResponderPacket(cs);
				petition.sendPetitionerPacket(cs);
				return true;
			}
		}
		
		return false;
	}
	
	public void sendPendingPetitionList(Player player)
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		final StringBuilder sb = new StringBuilder("<html><body><center><font color=\"LEVEL\">Current Petitions</font><br><table width=\"300\">");
		
		if (_pendingPetitions.size() == 0)
			sb.append("<tr><td colspan=\"4\">There are no currently pending petitions.</td></tr>");
		else
			sb.append("<tr><td></td><td><font color=\"999999\">Petitioner</font></td><td><font color=\"999999\">Petition Type</font></td><td><font color=\"999999\">Submitted</font></td></tr>");
		
		for (Petition petition : _pendingPetitions.values())
		{
			sb.append("<tr><td>");
			
			if (petition.getState() != PetitionState.IN_PROCESS)
				StringUtil.append(sb, "<button value=\"View\" action=\"bypass -h admin_view_petition ", petition.getId(), "\" width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			else
				sb.append("<font color=\"999999\">In Process</font>");
			
			StringUtil.append(sb, "</td><td>", petition.getPetitioner().getName(), "</td><td>", petition.getTypeAsString(), "</td><td>", sdf.format(petition.getSubmitTime()), "</td></tr>");
		}
		
		sb.append("</table><br><button value=\"Refresh\" action=\"bypass -h admin_view_petitions\" width=\"50\" " + "height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br><button value=\"Back\" action=\"bypass -h admin_admin\" " + "width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	public int submitPetition(Player player, String content, int type)
	{
		// Create a new petition instance and add it to the list of pending petitions.
		final Petition petition = new Petition(player, content, type);
		
		_pendingPetitions.put(petition.getId(), petition);
		
		// Notify all GMs that a new petition has been submitted.
		AdminData.getInstance().broadcastToGMs(new CreatureSay(player.getObjectId(), 17, "Petition System", (player.getName() + " has submitted a new petition.")));
		
		return petition.getId();
	}
	
	public void viewPetition(Player player, int id)
	{
		if (!player.isGM())
			return;
		
		final Petition petition = _pendingPetitions.get(id);
		if (petition == null)
			return;
		
		final StringBuilder sb = new StringBuilder("<html><body>");
		sb.append("<center><br><font color=\"LEVEL\">Petition #" + petition.getId() + "</font><br1>");
		sb.append("<img src=\"L2UI.SquareGray\" width=\"200\" height=\"1\"></center><br>");
		sb.append("Submit Time: " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(petition.getSubmitTime()) + "<br1>");
		sb.append("Petitioner: " + petition.getPetitioner().getName() + "<br1>");
		sb.append("Petition Type: " + petition.getTypeAsString() + "<br>" + petition.getContent() + "<br>");
		sb.append("<center><button value=\"Accept\" action=\"bypass -h admin_accept_petition " + petition.getId() + "\"" + "width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1>");
		sb.append("<button value=\"Reject\" action=\"bypass -h admin_reject_petition " + petition.getId() + "\" " + "width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
		sb.append("<button value=\"Back\" action=\"bypass -h admin_view_petitions\" width=\"40\" height=\"15\" back=\"sek.cbui94\" " + "fore=\"sek.cbui92\"></center>");
		sb.append("</body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	public boolean acceptPetition(Player player, int id)
	{
		final Petition petition = _pendingPetitions.get(id);
		if (petition == null || petition.getResponder() != null)
			return false;
		
		petition.setResponder(player);
		petition.setState(PetitionState.IN_PROCESS);
		
		// Petition application accepted. (Send to Petitioner)
		petition.sendPetitionerPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_APP_ACCEPTED));
		
		// Petition application accepted. Reciept No. is <ID>
		petition.sendResponderPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1).addNumber(petition.getId()));
		
		// Petition consultation with <Player> underway.
		petition.sendResponderPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_WITH_S1_UNDER_WAY).addCharName(petition.getPetitioner()));
		return true;
	}
	
	public boolean cancelActivePetition(Player player)
	{
		for (Petition currPetition : _pendingPetitions.values())
		{
			if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
				return (currPetition.endPetitionConsultation(PetitionState.PETITIONER_CANCEL));
			
			if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())
				return (currPetition.endPetitionConsultation(PetitionState.RESPONDER_CANCEL));
		}
		
		return false;
	}
	
	public void checkPetitionMessages(Player player)
	{
		if (player == null)
			return;
		
		for (Petition currPetition : _pendingPetitions.values())
		{
			if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
			{
				for (CreatureSay logMessage : currPetition.getLogMessages())
					player.sendPacket(logMessage);
				
				return;
			}
		}
	}
	
	public boolean endActivePetition(Player player)
	{
		if (!player.isGM())
			return false;
		
		for (Petition currPetition : _pendingPetitions.values())
		{
			if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())
				return (currPetition.endPetitionConsultation(PetitionState.COMPLETED));
		}
		
		return false;
	}
	
	public static PetitionManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PetitionManager INSTANCE = new PetitionManager();
	}
}
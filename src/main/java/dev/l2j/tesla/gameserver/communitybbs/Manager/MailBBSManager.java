package dev.l2j.tesla.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.data.cache.HtmCache;
import dev.l2j.tesla.gameserver.data.sql.PlayerInfoTable;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.player.BlockList;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.ExMailArrived;
import dev.l2j.tesla.gameserver.network.serverpackets.PlaySound;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;

public class MailBBSManager extends BaseBBSManager
{
	private static enum MailType
	{
		INBOX("Inbox", "<a action=\"bypass _bbsmail\">Inbox</a>"),
		SENTBOX("Sent Box", "<a action=\"bypass _bbsmail;sentbox\">Sent Box</a>"),
		ARCHIVE("Mail Archive", "<a action=\"bypass _bbsmail;archive\">Mail Archive</a>"),
		TEMPARCHIVE("Temporary Mail Archive", "<a action=\"bypass _bbsmail;temp_archive\">Temporary Mail Archive</a>");
		
		private final String _description;
		private final String _bypass;
		
		private MailType(String description, String bypass)
		{
			_description = description;
			_bypass = bypass;
		}
		
		public String getDescription()
		{
			return _description;
		}
		
		public String getBypass()
		{
			return _bypass;
		}
		
		public static final MailType[] VALUES = values();
	}
	
	private static final String SELECT_CHAR_MAILS = "SELECT * FROM character_mail WHERE charId = ? ORDER BY letterId ASC";
	private static final String INSERT_NEW_MAIL = "INSERT INTO character_mail (charId, letterId, senderId, location, recipientNames, subject, message, sentDate, unread) VALUES (?,?,?,?,?,?,?,?,?)";
	private static final String DELETE_MAIL = "DELETE FROM character_mail WHERE letterId = ?";
	private static final String MARK_MAIL_READ = "UPDATE character_mail SET unread = 0 WHERE letterId = ?";
	private static final String SET_MAIL_LOC = "UPDATE character_mail SET location = ? WHERE letterId = ?";
	private static final String SELECT_LAST_ID = "SELECT letterId FROM character_mail ORDER BY letterId DESC LIMIT 1";
	private static final String GET_GM_STATUS = "SELECT accesslevel FROM characters WHERE obj_Id = ?";
	
	private final Map<Integer, Set<Mail>> _mails = new ConcurrentHashMap<>();
	
	private int _lastid = 0;
	
	public class Mail
	{
		int charId;
		int mailId;
		int senderId;
		MailType location;
		String recipientNames;
		String subject;
		String message;
		Timestamp sentDate;
		String sentDateString;
		boolean unread;
	}
	
	protected MailBBSManager()
	{
		initId();
	}
	
	@Override
	public void parseCmd(String command, Player player)
	{
		if (command.equals("_bbsmail") || command.equals("_maillist_0_1_0_"))
			showMailList(player, 1, MailType.INBOX);
		else if (command.startsWith("_bbsmail"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			
			final String action = st.nextToken();
			
			if (action.equals("inbox") || action.equals("sentbox") || action.equals("archive") || action.equals("temparchive"))
			{
				final int page = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 1;
				final String sType = (st.hasMoreTokens()) ? st.nextToken() : "";
				final String search = (st.hasMoreTokens()) ? st.nextToken() : "";
				
				showMailList(player, page, Enum.valueOf(MailType.class, action.toUpperCase()), sType, search);
			}
			else if (action.equals("crea"))
				showWriteView(player);
			else
			{
				// Retrieve the mail based on its id (written as part of the command). If invalid, return to last known forum.
				final Mail mail = getMail(player, (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : -1);
				if (mail == null)
				{
					showLastForum(player);
					return;
				}
				
				if (action.equals("view"))
				{
					showMailView(player, mail);
					if (mail.unread)
						setMailToRead(player, mail.mailId);
				}
				else if (action.equals("reply"))
					showWriteView(player, mail);
				else if (action.equals("del"))
				{
					deleteMail(player, mail.mailId);
					showLastForum(player);
				}
				else if (action.equals("store"))
				{
					setMailLocation(player, mail.mailId, MailType.ARCHIVE);
					showMailList(player, 1, MailType.ARCHIVE);
				}
			}
		}
		else
			super.parseCmd(command, player);
	}
	
	@Override
	public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, Player player)
	{
		if (ar1.equals("Send"))
		{
			sendMail(ar3, ar4, ar5, player);
			showMailList(player, 1, MailType.SENTBOX);
		}
		else if (ar1.startsWith("Search"))
		{
			final StringTokenizer st = new StringTokenizer(ar1, ";");
			st.nextToken();
			
			showMailList(player, 1, Enum.valueOf(MailType.class, st.nextToken().toUpperCase()), ar4, ar5);
		}
		else
			super.parseWrite(ar1, ar2, ar3, ar4, ar5, player);
	}
	
	private void initId()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_LAST_ID))
		{
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					if (rs.getInt(1) > _lastid)
						_lastid = rs.getInt(1);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't find the last mail id.", e);
		}
	}
	
	private synchronized int getNewMailId()
	{
		return ++_lastid;
	}
	
	private Set<Mail> getPlayerMails(int objectId)
	{
		Set<Mail> mails = _mails.get(objectId);
		if (mails == null)
		{
			mails = ConcurrentHashMap.newKeySet();
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(SELECT_CHAR_MAILS))
			{
				ps.setInt(1, objectId);
				
				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						final Mail mail = new Mail();
						mail.charId = rs.getInt("charId");
						mail.mailId = rs.getInt("letterId");
						mail.senderId = rs.getInt("senderId");
						mail.location = Enum.valueOf(MailType.class, rs.getString("location").toUpperCase());
						mail.recipientNames = rs.getString("recipientNames");
						mail.subject = rs.getString("subject");
						mail.message = rs.getString("message");
						mail.sentDate = rs.getTimestamp("sentDate");
						mail.sentDateString = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(mail.sentDate);
						mail.unread = rs.getInt("unread") != 0;
						mails.add(mail);
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't load mail for player id : {}.", e, objectId);
			}
			_mails.put(objectId, mails);
		}
		return mails;
	}
	
	private Mail getMail(Player player, int mailId)
	{
		return getPlayerMails(player.getObjectId()).stream().filter(l -> l.mailId == mailId).findFirst().orElse(null);
	}
	
	private static String abbreviate(String s, int maxWidth)
	{
		return (s.length() > maxWidth) ? s.substring(0, maxWidth) : s;
	}
	
	public int checkUnreadMail(Player player)
	{
		return (int) getPlayerMails(player.getObjectId()).stream().filter(l -> l.unread).count();
	}
	
	private void showMailList(Player player, int page, MailType type)
	{
		showMailList(player, page, type, "", "");
	}
	
	private void showMailList(Player player, int page, MailType type, String sType, String search)
	{
		Set<Mail> mails;
		if (!sType.equals("") && !search.equals(""))
		{
			mails = ConcurrentHashMap.newKeySet();
			
			boolean byTitle = sType.equalsIgnoreCase("title");
			
			for (Mail mail : getPlayerMails(player.getObjectId()))
			{
				if (byTitle && mail.subject.toLowerCase().contains(search.toLowerCase()))
					mails.add(mail);
				else if (!byTitle)
				{
					String writer = getPlayerName(mail.senderId);
					if (writer.toLowerCase().contains(search.toLowerCase()))
						mails.add(mail);
				}
			}
		}
		else
			mails = getPlayerMails(player.getObjectId());
		
		final int countMails = getMailCount(player.getObjectId(), type, sType, search);
		final int maxpage = getPagesCount(countMails);
		
		if (page > maxpage)
			page = maxpage;
		if (page < 1)
			page = 1;
		
		player.setMailPosition(page);
		int index = 0, minIndex = 0, maxIndex = 0;
		maxIndex = (page == 1 ? page * 9 : (page * 10) - 1);
		minIndex = maxIndex - 9;
		
		String content = HtmCache.getInstance().getHtm(CB_PATH + "mail/mail.htm");
		content = content.replace("%inbox%", Integer.toString(getMailCount(player.getObjectId(), MailType.INBOX, "", "")));
		content = content.replace("%sentbox%", Integer.toString(getMailCount(player.getObjectId(), MailType.SENTBOX, "", "")));
		content = content.replace("%archive%", Integer.toString(getMailCount(player.getObjectId(), MailType.ARCHIVE, "", "")));
		content = content.replace("%temparchive%", Integer.toString(getMailCount(player.getObjectId(), MailType.TEMPARCHIVE, "", "")));
		content = content.replace("%type%", type.getDescription());
		content = content.replace("%htype%", type.toString().toLowerCase());
		
		final StringBuilder sb = new StringBuilder();
		for (Mail mail : mails)
		{
			if (mail.location.equals(type))
			{
				if (index < minIndex)
				{
					index++;
					continue;
				}
				
				if (index > maxIndex)
					break;
				
				StringUtil.append(sb, "<table width=610><tr><td width=5></td><td width=150>", getPlayerName(mail.senderId), "</td><td width=300><a action=\"bypass _bbsmail;view;", mail.mailId, "\">");
				
				if (mail.unread)
					sb.append("<font color=\"LEVEL\">");
				
				sb.append(abbreviate(mail.subject, 51));
				
				if (mail.unread)
					sb.append("</font>");
				
				StringUtil.append(sb, "</a></td><td width=150>", mail.sentDateString, "</td><td width=5></td></tr></table><img src=\"L2UI.Squaregray\" width=610 height=1>");
				index++;
			}
		}
		content = content.replace("%maillist%", sb.toString());
		
		// CLeanup sb.
		sb.setLength(0);
		
		final String fullSearch = (!sType.equals("") && !search.equals("")) ? ";" + sType + ";" + search : "";
		
		StringUtil.append(sb, "<td><table><tr><td></td></tr><tr><td><button action=\"bypass _bbsmail;", type, ";", (page == 1 ? page : page - 1), fullSearch, "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td></tr></table></td>");
		
		int i = 0;
		if (maxpage > 21)
		{
			if (page <= 11)
			{
				for (i = 1; i <= (10 + page); i++)
				{
					if (i == page)
						StringUtil.append(sb, "<td> ", i, " </td>");
					else
						StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
				}
			}
			else if (page > 11 && (maxpage - page) > 10)
			{
				for (i = (page - 10); i <= (page - 1); i++)
				{
					if (i == page)
						continue;
					
					StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
				}
				for (i = page; i <= (page + 10); i++)
				{
					if (i == page)
						StringUtil.append(sb, "<td> ", i, " </td>");
					else
						StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
				}
			}
			else if ((maxpage - page) <= 10)
			{
				for (i = (page - 10); i <= maxpage; i++)
				{
					if (i == page)
						StringUtil.append(sb, "<td> ", i, " </td>");
					else
						StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
				}
			}
		}
		else
		{
			for (i = 1; i <= maxpage; i++)
			{
				if (i == page)
					StringUtil.append(sb, "<td> ", i, " </td>");
				else
					StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
			}
		}
		StringUtil.append(sb, "<td><table><tr><td></td></tr><tr><td><button action=\"bypass _bbsmail;", type, ";", (page == maxpage ? page : page + 1), fullSearch, "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td></tr></table></td>");
		
		content = content.replace("%maillistlength%", sb.toString());
		
		separateAndSend(content, player);
	}
	
	private void showMailView(Player player, Mail mail)
	{
		if (mail == null)
		{
			showMailList(player, 1, MailType.INBOX);
			return;
		}
		
		String content = HtmCache.getInstance().getHtm(CB_PATH + "mail/mail-show.htm");
		
		String link = mail.location.getBypass() + "&nbsp;&gt;&nbsp;" + mail.subject;
		content = content.replace("%maillink%", link);
		
		content = content.replace("%writer%", getPlayerName(mail.senderId));
		content = content.replace("%sentDate%", mail.sentDateString);
		content = content.replace("%receiver%", mail.recipientNames);
		content = content.replace("%delDate%", "Unknown");
		content = content.replace("%title%", mail.subject.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;"));
		content = content.replace("%mes%", mail.message.replaceAll("\r\n", "<br>").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;"));
		content = content.replace("%letterId%", mail.mailId + "");
		
		separateAndSend(content, player);
	}
	
	private static void showWriteView(Player player)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "mail/mail-write.htm");
		separateAndSend(content, player);
	}
	
	private static void showWriteView(Player player, Mail mail)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "mail/mail-reply.htm");
		
		String link = mail.location.getBypass() + "&nbsp;&gt;&nbsp;<a action=\"bypass _bbsmail;view;" + mail.mailId + "\">" + mail.subject + "</a>&nbsp;&gt;&nbsp;";
		content = content.replace("%maillink%", link);
		
		content = content.replace("%recipients%", mail.senderId == player.getObjectId() ? mail.recipientNames : getPlayerName(mail.senderId));
		content = content.replace("%letterId%", mail.mailId + "");
		send1001(content, player);
		send1002(player, " ", "Re: " + mail.subject, "0");
	}
	
	public void sendMail(String recipients, String subject, String message, Player player)
	{
		// Current time.
		final long currentDate = Calendar.getInstance().getTimeInMillis();
		
		// Get the current time - 1 day under timestamp format.
		final Timestamp ts = new Timestamp(currentDate - 86400000L);
		
		// Check sender mails based on previous timestamp. If more than 10 mails have been found for today, then cancel the use.
		if (getPlayerMails(player.getObjectId()).stream().filter(l -> l.sentDate.after(ts) && l.location == MailType.SENTBOX).count() >= 10)
		{
			player.sendPacket(SystemMessageId.NO_MORE_MESSAGES_TODAY);
			return;
		}
		
		// Format recipient names. If more than 5 are found, cancel the mail.
		final String[] recipientNames = recipients.trim().split(";");
		if (recipientNames.length > 5 && !player.isGM())
		{
			player.sendPacket(SystemMessageId.ONLY_FIVE_RECIPIENTS);
			return;
		}
		
		// Edit subject, if none.
		if (subject == null || subject.isEmpty())
			subject = "(no subject)";
		
		// Edit message.
		message = message.replaceAll("\n", "<br1>");
		
		// Get the current time under timestamp format.
		final Timestamp time = new Timestamp(currentDate);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_NEW_MAIL))
		{
			ps.setInt(3, player.getObjectId());
			ps.setString(4, "inbox");
			ps.setString(5, recipients);
			ps.setString(6, abbreviate(subject, 128));
			ps.setString(7, message);
			ps.setTimestamp(8, time);
			ps.setInt(9, 1);
			
			for (String recipientName : recipientNames)
			{
				// Recipient is an invalid player, or is the sender.
				final int recipientId = PlayerInfoTable.getInstance().getPlayerObjectId(recipientName);
				if (recipientId <= 0 || recipientId == player.getObjectId())
				{
					player.sendPacket(SystemMessageId.INCORRECT_TARGET);
					continue;
				}
				
				final Player recipientPlayer = World.getInstance().getPlayer(recipientId);
				
				if (!player.isGM())
				{
					// Sender is a regular player, while recipient is a GM.
					if (isGM(recipientId))
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_MAIL_GM_S1).addString(recipientName));
						continue;
					}
					
					// The recipient is on block mode.
					if (isBlocked(player, recipientId))
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_BLOCKED_YOU_CANNOT_MAIL).addString(recipientName));
						continue;
					}
					
					// The recipient box is already full.
					if (isInboxFull(recipientId))
					{
						player.sendPacket(SystemMessageId.MESSAGE_NOT_SENT);
						if (recipientPlayer != null)
							recipientPlayer.sendPacket(SystemMessageId.MAILBOX_FULL);
						
						continue;
					}
				}
				
				final int id = getNewMailId();
				
				ps.setInt(1, recipientId);
				ps.setInt(2, id);
				ps.addBatch();
				
				final Mail mail = new Mail();
				mail.charId = recipientId;
				mail.mailId = id;
				mail.senderId = player.getObjectId();
				mail.location = MailType.INBOX;
				mail.recipientNames = recipients;
				mail.subject = abbreviate(subject, 128);
				mail.message = message;
				mail.sentDate = time;
				mail.sentDateString = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(mail.sentDate);
				mail.unread = true;
				
				getPlayerMails(recipientId).add(mail);
				
				if (recipientPlayer != null)
				{
					recipientPlayer.sendPacket(SystemMessageId.NEW_MAIL);
					recipientPlayer.sendPacket(new PlaySound("systemmsg_e.1233"));
					recipientPlayer.sendPacket(ExMailArrived.STATIC_PACKET);
				}
			}
			
			// Create a copy into player's sent box, if at least one recipient has been reached.
			final int[] result = ps.executeBatch();
			if (result.length > 0)
			{
				final int id = getNewMailId();
				
				ps.setInt(1, player.getObjectId());
				ps.setInt(2, id);
				ps.setString(4, "sentbox");
				ps.setInt(9, 0);
				ps.execute();
				
				final Mail mail = new Mail();
				mail.charId = player.getObjectId();
				mail.mailId = id;
				mail.senderId = player.getObjectId();
				mail.location = MailType.SENTBOX;
				mail.recipientNames = recipients;
				mail.subject = abbreviate(subject, 128);
				mail.message = message;
				mail.sentDate = time;
				mail.sentDateString = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(mail.sentDate);
				mail.unread = false;
				
				getPlayerMails(player.getObjectId()).add(mail);
				
				player.sendPacket(SystemMessageId.SENT_MAIL);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't send mail for {}.", e, player.getName());
		}
	}
	
	private int getMailCount(int objectId, MailType location, String type, String search)
	{
		int count = 0;
		if (!type.equals("") && !search.equals(""))
		{
			boolean byTitle = type.equalsIgnoreCase("title");
			for (Mail mail : getPlayerMails(objectId))
			{
				if (!mail.location.equals(location))
					continue;
				
				if (byTitle && mail.subject.toLowerCase().contains(search.toLowerCase()))
					count++;
				else if (!byTitle)
				{
					String writer = getPlayerName(mail.senderId);
					if (writer.toLowerCase().contains(search.toLowerCase()))
						count++;
				}
			}
		}
		else
		{
			for (Mail mail : getPlayerMails(objectId))
			{
				if (mail.location.equals(location))
					count++;
			}
		}
		return count;
	}
	
	private static boolean isBlocked(Player player, int objectId)
	{
		for (Player playerToTest : World.getInstance().getPlayers())
		{
			if (playerToTest.getObjectId() == objectId)
			{
				if (BlockList.isInBlockList(playerToTest, player))
					return true;
				
				return false;
			}
		}
		return false;
	}
	
	private void deleteMail(Player player, int mailId)
	{
		for (Mail mail : getPlayerMails(player.getObjectId()))
		{
			if (mail.mailId == mailId)
			{
				getPlayerMails(player.getObjectId()).remove(mail);
				break;
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_MAIL))
		{
			ps.setInt(1, mailId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete mail #{}.", e, mailId);
		}
	}
	
	private void setMailToRead(Player player, int mailId)
	{
		getMail(player, mailId).unread = false;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(MARK_MAIL_READ))
		{
			ps.setInt(1, mailId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't set read status for mail #{}.", e, mailId);
		}
	}
	
	private void setMailLocation(Player player, int mailId, MailType location)
	{
		getMail(player, mailId).location = location;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SET_MAIL_LOC))
		{
			ps.setString(1, location.toString().toLowerCase());
			ps.setInt(2, mailId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't set mail #{} location.", e, mailId);
		}
	}
	
	private static String getPlayerName(int objectId)
	{
		final String name = PlayerInfoTable.getInstance().getPlayerName(objectId);
		return (name == null) ? "Unknown" : name;
	}
	
	private static boolean isGM(int objectId)
	{
		boolean isGM = false;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(GET_GM_STATUS))
		{
			ps.setInt(1, objectId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
					isGM = rs.getInt(1) > 0;
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't verify GM access for {}.", e, objectId);
		}
		return isGM;
	}
	
	private boolean isInboxFull(int objectId)
	{
		return getMailCount(objectId, MailType.INBOX, "", "") >= 100;
	}
	
	private void showLastForum(Player player)
	{
		final int page = player.getMailPosition() % 1000;
		final int type = player.getMailPosition() / 1000;
		
		showMailList(player, page, MailType.VALUES[type]);
	}
	
	private static int getPagesCount(int mailCount)
	{
		if (mailCount < 1)
			return 1;
		
		if (mailCount % 10 == 0)
			return mailCount / 10;
		
		return (mailCount / 10) + 1;
	}
	
	public static MailBBSManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MailBBSManager INSTANCE = new MailBBSManager();
	}
}
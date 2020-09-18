package dev.l2j.tesla.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.data.cache.HtmCache;
import dev.l2j.tesla.gameserver.data.sql.PlayerInfoTable;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.player.BlockList;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.FriendList;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;

public class FriendsBBSManager extends BaseBBSManager
{
	private static final String FRIENDLIST_DELETE_BUTTON = "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all friends from your Friends List?</td><td width=20></td><td><button value=\"OK\" action=\"bypass _friend;delall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table>";
	private static final String BLOCKLIST_DELETE_BUTTON = "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all players from your Block List?</td><td width=20></td><td><button value=\"OK\" action=\"bypass _block;delall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table>";
	
	private static final String DELETE_ALL_FRIENDS = "DELETE FROM character_friends WHERE char_id = ? OR friend_id = ?";
	private static final String DELETE_FRIEND = "DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)";
	
	protected FriendsBBSManager()
	{
	}
	
	@Override
	public void parseCmd(String command, Player player)
	{
		if (command.startsWith("_friendlist"))
			showFriendsList(player, false);
		else if (command.startsWith("_blocklist"))
			showBlockList(player, false);
		else if (command.startsWith("_friend"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			String action = st.nextToken();
			
			if (action.equals("select"))
			{
				player.selectFriend((st.hasMoreTokens()) ? Integer.valueOf(st.nextToken()) : 0);
				showFriendsList(player, false);
			}
			else if (action.equals("deselect"))
			{
				player.deselectFriend((st.hasMoreTokens()) ? Integer.valueOf(st.nextToken()) : 0);
				showFriendsList(player, false);
			}
			else if (action.equals("delall"))
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
                     PreparedStatement ps = con.prepareStatement(DELETE_ALL_FRIENDS))
				{
					ps.setInt(1, player.getObjectId());
					ps.setInt(2, player.getObjectId());
					ps.execute();
				}
				catch (Exception e)
				{
					LOGGER.error("Couldn't delete friends.", e);
				}
				
				for (int friendId : player.getFriendList())
				{
					// Update friend's friendlist.
					final Player friend = World.getInstance().getPlayer(friendId);
					if (friend != null)
					{
						friend.getFriendList().remove(Integer.valueOf(player.getObjectId()));
						friend.getSelectedFriendList().remove(Integer.valueOf(player.getObjectId()));
						
						friend.sendPacket(new FriendList(friend));
					}
				}
				
				player.getFriendList().clear();
				player.getSelectedFriendList().clear();
				showFriendsList(player, false);
				
				player.sendMessage("You have cleared your friends list.");
				player.sendPacket(new FriendList(player));
			}
			else if (action.equals("delconfirm"))
				showFriendsList(player, true);
			else if (action.equals("del"))
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement ps = con.prepareStatement(DELETE_FRIEND))
				{
					ps.setInt(1, player.getObjectId());
					ps.setInt(4, player.getObjectId());
					
					for (int friendId : player.getSelectedFriendList())
					{
						ps.setInt(2, friendId);
						ps.setInt(3, friendId);
						ps.addBatch();
						
						// Update friend's friendlist.
						final Player friend = World.getInstance().getPlayer(friendId);
						if (friend != null)
						{
							friend.getFriendList().remove(Integer.valueOf(player.getObjectId()));
							friend.sendPacket(new FriendList(friend));
						}
						
						// The friend is deleted from your friendlist.
						player.getFriendList().remove(Integer.valueOf(friendId));
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(PlayerInfoTable.getInstance().getPlayerName(friendId)));
					}
					ps.executeBatch();
				}
				catch (Exception e)
				{
					LOGGER.error("Couldn't delete friend.", e);
				}
				
				player.getSelectedFriendList().clear();
				showFriendsList(player, false);
				
				player.sendPacket(new FriendList(player)); // update friendList *heavy method*
			}
			else if (action.equals("mail"))
			{
				if (!player.getSelectedFriendList().isEmpty())
					showMailWrite(player);
			}
		}
		else if (command.startsWith("_block"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			String action = st.nextToken();
			
			if (action.equals("select"))
			{
				player.selectBlock((st.hasMoreTokens()) ? Integer.valueOf(st.nextToken()) : 0);
				showBlockList(player, false);
			}
			else if (action.equals("deselect"))
			{
				player.deselectBlock((st.hasMoreTokens()) ? Integer.valueOf(st.nextToken()) : 0);
				showBlockList(player, false);
			}
			else if (action.equals("delall"))
			{
				List<Integer> list = new ArrayList<>();
				list.addAll(player.getBlockList().getBlockList());
				
				for (Integer blockId : list)
					BlockList.removeFromBlockList(player, blockId);
				
				player.getSelectedBlocksList().clear();
				showBlockList(player, false);
			}
			else if (action.equals("delconfirm"))
				showBlockList(player, true);
			else if (action.equals("del"))
			{
				for (Integer blockId : player.getSelectedBlocksList())
					BlockList.removeFromBlockList(player, blockId);
				
				player.getSelectedBlocksList().clear();
				showBlockList(player, false);
			}
		}
		else
			super.parseCmd(command, player);
	}
	
	@Override
	public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, Player player)
	{
		if (ar1.equalsIgnoreCase("mail"))
		{
			MailBBSManager.getInstance().sendMail(ar2, ar4, ar5, player);
			showFriendsList(player, false);
		}
		else
			super.parseWrite(ar1, ar2, ar3, ar4, ar5, player);
	}
	
	@Override
	protected String getFolder()
	{
		return "friend/";
	}
	
	private static void showFriendsList(Player player, boolean delMsg)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "friend/friend-list.htm");
		if (content == null)
			return;
		
		// Retrieve player's friendlist and selected
		final List<Integer> list = player.getFriendList();
		final List<Integer> selectedList = player.getSelectedFriendList();
		
		final StringBuilder sb = new StringBuilder();
		
		// Friendlist
		for (Integer id : list)
		{
			if (selectedList.contains(id))
				continue;
			
			final String friendName = PlayerInfoTable.getInstance().getPlayerName(id);
			if (friendName == null)
				continue;
			
			final Player friend = World.getInstance().getPlayer(id);
			StringUtil.append(sb, "<a action=\"bypass _friend;select;", id, "\">[Select]</a>&nbsp;", friendName, " ", ((friend != null && friend.isOnline()) ? "(on)" : "(off)"), "<br1>");
		}
		content = content.replaceAll("%friendslist%", sb.toString());
		
		// Cleanup sb.
		sb.setLength(0);
		
		// Selected friendlist
		for (Integer id : selectedList)
		{
			final String friendName = PlayerInfoTable.getInstance().getPlayerName(id);
			if (friendName == null)
				continue;
			
			final Player friend = World.getInstance().getPlayer(id);
			StringUtil.append(sb, "<a action=\"bypass _friend;deselect;", id, "\">[Deselect]</a>&nbsp;", friendName, " ", ((friend != null && friend.isOnline()) ? "(on)" : "(off)"), "<br1>");
		}
		content = content.replaceAll("%selectedFriendsList%", sb.toString());
		
		// Delete button.
		content = content.replaceAll("%deleteMSG%", (delMsg) ? FRIENDLIST_DELETE_BUTTON : "");
		
		separateAndSend(content, player);
	}
	
	private static void showBlockList(Player player, boolean delMsg)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "friend/friend-blocklist.htm");
		if (content == null)
			return;
		
		// Retrieve player's blocklist and selected
		final List<Integer> list = player.getBlockList().getBlockList();
		final List<Integer> selectedList = player.getSelectedBlocksList();
		
		final StringBuilder sb = new StringBuilder();
		
		// Blocklist
		for (Integer id : list)
		{
			if (selectedList.contains(id))
				continue;
			
			final String blockName = PlayerInfoTable.getInstance().getPlayerName(id);
			if (blockName == null)
				continue;
			
			final Player block = World.getInstance().getPlayer(id);
			StringUtil.append(sb, "<a action=\"bypass _block;select;", id, "\">[Select]</a>&nbsp;", blockName, " ", ((block != null && block.isOnline()) ? "(on)" : "(off)"), "<br1>");
		}
		content = content.replaceAll("%blocklist%", sb.toString());
		
		// Cleanup sb.
		sb.setLength(0);
		
		// Selected Blocklist
		for (Integer id : selectedList)
		{
			final String blockName = PlayerInfoTable.getInstance().getPlayerName(id);
			if (blockName == null)
				continue;
			
			final Player block = World.getInstance().getPlayer(id);
			StringUtil.append(sb, "<a action=\"bypass _block;deselect;", id, "\">[Deselect]</a>&nbsp;", blockName, " ", ((block != null && block.isOnline()) ? "(on)" : "(off)"), "<br1>");
		}
		content = content.replaceAll("%selectedBlocksList%", sb.toString());
		
		// Delete button.
		content = content.replaceAll("%deleteMSG%", (delMsg) ? BLOCKLIST_DELETE_BUTTON : "");
		
		separateAndSend(content, player);
	}
	
	public static final void showMailWrite(Player player)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "friend/friend-mail.htm");
		if (content == null)
			return;
		
		final StringBuilder sb = new StringBuilder();
		for (int id : player.getSelectedFriendList())
		{
			String friendName = PlayerInfoTable.getInstance().getPlayerName(id);
			if (friendName == null)
				continue;
			
			if (sb.length() > 0)
				sb.append(";");
			
			sb.append(friendName);
		}
		
		content = content.replaceAll("%list%", sb.toString());
		
		separateAndSend(content, player);
	}
	
	public static FriendsBBSManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final FriendsBBSManager INSTANCE = new FriendsBBSManager();
	}
}
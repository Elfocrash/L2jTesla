package dev.l2j.tesla.gameserver.model.actor.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.gameserver.data.sql.PlayerInfoTable;

public class BlockList
{
	private static final CLogger LOGGER = new CLogger(BlockList.class.getName());
	
	private static final Map<Integer, List<Integer>> OFFLINE_LIST = new HashMap<>();
	
	private static final String LOAD_BLOCKLIST = "SELECT friend_id FROM character_friends WHERE char_id = ? AND relation = 1";
	private static final String INSERT_BLOCKED_USER = "INSERT INTO character_friends (char_id, friend_id, relation) VALUES (?, ?, 1)";
	private static final String DELETE_BLOCKED_USER = "DELETE FROM character_friends WHERE char_id = ? AND friend_id = ? AND relation = 1";
	
	private final Player _owner;
	private List<Integer> _blockList;
	
	public BlockList(Player owner)
	{
		_owner = owner;
		
		_blockList = OFFLINE_LIST.get(owner.getObjectId());
		if (_blockList == null)
			_blockList = loadList(_owner.getObjectId());
	}
	
	private synchronized void addToBlockList(int target)
	{
		_blockList.add(target);
		
		updateInDB(target, true);
	}
	
	private synchronized void removeFromBlockList(int target)
	{
		_blockList.remove(Integer.valueOf(target));
		
		updateInDB(target, false);
	}
	
	public void playerLogout()
	{
		OFFLINE_LIST.put(_owner.getObjectId(), _blockList);
	}
	
	private static List<Integer> loadList(int objectId)
	{
		final List<Integer> list = new ArrayList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement(LOAD_BLOCKLIST))
			{
				ps.setInt(1, objectId);
				
				try (ResultSet rset = ps.executeQuery())
				{
					while (rset.next())
					{
						final int friendId = rset.getInt("friend_id");
						if (friendId == objectId)
							continue;
						
						list.add(friendId);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load blocklist for {}.", e, objectId);
		}
		return list;
	}
	
	private void updateInDB(int targetId, boolean state)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement((state) ? INSERT_BLOCKED_USER : DELETE_BLOCKED_USER))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, targetId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't add/remove block player.", e);
		}
	}
	
	public boolean isInBlockList(Player target)
	{
		return _blockList.contains(target.getObjectId());
	}
	
	public boolean isInBlockList(int targetId)
	{
		return _blockList.contains(targetId);
	}
	
	private boolean isBlockAll()
	{
		return _owner.isInRefusalMode();
	}
	
	public static boolean isBlocked(Player listOwner, Player target)
	{
		final BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(target);
	}
	
	public static boolean isBlocked(Player listOwner, int targetId)
	{
		final BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(targetId);
	}
	
	private void setBlockAll(boolean state)
	{
		_owner.setInRefusalMode(state);
	}
	
	public List<Integer> getBlockList()
	{
		return _blockList;
	}
	
	public static void addToBlockList(Player listOwner, int targetId)
	{
		if (listOwner == null)
			return;
		
		final String targetName = PlayerInfoTable.getInstance().getPlayerName(targetId);
		
		if (listOwner.getFriendList().contains(targetId))
		{
			listOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST).addString(targetName));
			return;
		}
		
		if (listOwner.getBlockList().getBlockList().contains(targetId))
		{
			listOwner.sendMessage(targetName + " is already registered in your ignore list.");
			return;
		}
		
		listOwner.getBlockList().addToBlockList(targetId);
		listOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST).addString(targetName));
		
		final Player targetPlayer = World.getInstance().getPlayer(targetId);
		if (targetPlayer != null)
			targetPlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addString(listOwner.getName()));
	}
	
	public static void removeFromBlockList(Player listOwner, int targetId)
	{
		if (listOwner == null)
			return;
		
		if (!listOwner.getBlockList().getBlockList().contains(targetId))
		{
			listOwner.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		
		listOwner.getBlockList().removeFromBlockList(targetId);
		listOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST).addString(PlayerInfoTable.getInstance().getPlayerName(targetId)));
	}
	
	public static boolean isInBlockList(Player listOwner, Player target)
	{
		return listOwner.getBlockList().isInBlockList(target);
	}
	
	public boolean isBlockAll(Player listOwner)
	{
		return listOwner.getBlockList().isBlockAll();
	}
	
	public static void setBlockAll(Player listOwner, boolean newValue)
	{
		listOwner.getBlockList().setBlockAll(newValue);
	}
	
	public static void sendListToOwner(Player listOwner)
	{
		int i = 1;
		listOwner.sendPacket(SystemMessageId.BLOCK_LIST_HEADER);
		
		for (int playerId : listOwner.getBlockList().getBlockList())
			listOwner.sendMessage((i++) + ". " + PlayerInfoTable.getInstance().getPlayerName(playerId));
		
		listOwner.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
	}
	
	/**
	 * @param ownerId object id of owner block list
	 * @param targetId object id of potential blocked player
	 * @return true if blocked
	 */
	public static boolean isInBlockList(int ownerId, int targetId)
	{
		final Player player = World.getInstance().getPlayer(ownerId);
		if (player != null)
			return BlockList.isBlocked(player, targetId);
		
		if (!OFFLINE_LIST.containsKey(ownerId))
			OFFLINE_LIST.put(ownerId, loadList(ownerId));
		
		return OFFLINE_LIST.get(ownerId).contains(targetId);
	}
}
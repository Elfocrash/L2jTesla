package dev.l2j.tesla.gameserver.network;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.LoginServerThread;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.mmocore.MMOClient;
import dev.l2j.tesla.commons.mmocore.MMOConnection;
import dev.l2j.tesla.commons.mmocore.ReceivablePacket;

import dev.l2j.tesla.gameserver.data.sql.ClanTable;
import dev.l2j.tesla.gameserver.data.sql.PlayerInfoTable;
import dev.l2j.tesla.gameserver.model.CharSelectSlot;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.pledge.Clan;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket;
import dev.l2j.tesla.gameserver.network.serverpackets.ServerClose;

/**
 * Represents a client connected on Game Server.<br>
 * <br>
 * It is linked to a {@link Player} and hold account informations (flood protectors, connection time, etc).
 */
public class GameClient extends MMOClient<MMOConnection<GameClient>> implements Runnable
{
	private static final CLogger LOGGER = new CLogger(GameClient.class.getName());
	
	private static final String SELECT_CLAN = "SELECT clanId FROM characters WHERE obj_id=?";
	private static final String UPDATE_DELETE_TIME = "UPDATE characters SET deletetime=? WHERE obj_id=?";
	
	private static final String DELETE_CHAR_FRIENDS = "DELETE FROM character_friends WHERE char_id=? OR friend_id=?";
	private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=?";
	private static final String DELETE_CHAR_MACROS = "DELETE FROM character_macroses WHERE char_obj_id=?";
	private static final String DELETE_CHAR_MEMOS = "DELETE FROM character_memo WHERE charId=?";
	private static final String DELETE_CHAR_QUESTS = "DELETE FROM character_quests WHERE charId=?";
	private static final String DELETE_CHAR_RECIPES = "DELETE FROM character_recipebook WHERE charId=?";
	private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=?";
	private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=?";
	private static final String DELETE_CHAR_SKILLS_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=?";
	private static final String DELETE_CHAR_SUBCLASSES = "DELETE FROM character_subclasses WHERE char_obj_id=?";
	private static final String DELETE_CHAR_HERO = "DELETE FROM heroes WHERE char_id=?";
	private static final String DELETE_CHAR_NOBLE = "DELETE FROM olympiad_nobles WHERE char_id=?";
	private static final String DELETE_CHAR_SEVEN_SIGNS = "DELETE FROM seven_signs WHERE char_obj_id=?";
	private static final String DELETE_CHAR_PETS = "DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)";
	private static final String DELETE_CHAR_AUGMENTS = "DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)";
	private static final String DELETE_CHAR_ITEMS = "DELETE FROM items WHERE owner_id=?";
	private static final String DELETE_CHAR_RBP = "DELETE FROM character_raid_points WHERE char_id=?";
	private static final String DELETE_CHAR = "DELETE FROM characters WHERE obj_Id=?";
	
	public static enum GameClientState
	{
		CONNECTED, // client has just connected
		AUTHED, // client has authed but doesnt has character attached to it yet
		ENTERING, // client is currently loading his Player instance, but didn't end
		IN_GAME // client has selected a char and is in game
	}
	
	private final long[] _floodProtectors = new long[FloodProtectors.Action.VALUES_LENGTH];
	private final ArrayBlockingQueue<ReceivablePacket<GameClient>> _packetQueue;
	private final ReentrantLock _queueLock = new ReentrantLock();
	private final ReentrantLock _activeCharLock = new ReentrantLock();
	
	private final GameCrypt _crypt;
	private final ClientStats _stats;
	private final long _connectionStartTime;
	
	public GameClientState _state;
	private String _accountName;
	private SessionKey _sessionId;
	private Player _player;
	private boolean _isDetached;
	@SuppressWarnings("unused")
	private boolean _isAuthedGG;
	
	private CharSelectSlot[] _slots;
	
	protected final ScheduledFuture<?> _autoSaveInDB;
	protected ScheduledFuture<?> _cleanupTask;
	
	public GameClient(MMOConnection<GameClient> con)
	{
		super(con);
		
		_state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();
		_crypt = new GameCrypt();
		_stats = new ClientStats();
		_packetQueue = new ArrayBlockingQueue<>(Config.CLIENT_PACKET_QUEUE_SIZE);
		
		_autoSaveInDB = ThreadPool.scheduleAtFixedRate(() ->
		{
			if (getPlayer() != null && getPlayer().isOnline())
			{
				getPlayer().store();
				
				if (getPlayer().getSummon() != null)
					getPlayer().getSummon().store();
			}
		}, 300000L, 900000L);
	}
	
	@Override
	public void run()
	{
		if (!_queueLock.tryLock())
			return;
		
		try
		{
			int count = 0;
			ReceivablePacket<GameClient> packet;
			while (true)
			{
				packet = _packetQueue.poll();
				if (packet == null) // queue is empty
					return;
				
				if (_isDetached) // clear queue immediately after detach
				{
					_packetQueue.clear();
					return;
				}
				
				try
				{
					packet.run();
				}
				catch (Exception e)
				{
					LOGGER.error("Execution failed on {} for {}.", e, packet.getClass().getSimpleName(), toString());
				}
				
				count++;
				if (getStats().countBurst(count))
					return;
			}
		}
		finally
		{
			_queueLock.unlock();
		}
	}
	
	@Override
	public String toString()
	{
		try
		{
			final InetAddress address = getConnection().getInetAddress();
			switch (getState())
			{
				case CONNECTED:
					return "[IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				
				case AUTHED:
					return "[Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				
				case ENTERING:
				case IN_GAME:
					return "[Character: " + (getPlayer() == null ? "disconnected" : getPlayer().getName()) + " - Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				
				default:
					throw new IllegalStateException("Missing state on switch");
			}
		}
		catch (NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}
	
	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		_crypt.decrypt(buf.array(), buf.position(), size);
		return true;
	}
	
	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}
	
	@Override
	protected void onDisconnection()
	{
		try
		{
			ThreadPool.execute(() ->
			{
				boolean fast = true;
				if (getPlayer() != null && !isDetached())
				{
					setDetached(true);
					fast = !getPlayer().isInCombat() && !getPlayer().isLocked();
				}
				cleanMe(fast);
			});
		}
		catch (RejectedExecutionException e)
		{
		}
	}
	
	@Override
	protected void onForcedDisconnection()
	{
		LOGGER.debug("{} disconnected abnormally.", toString());
	}
	
	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}
	
	public GameClientState getState()
	{
		return _state;
	}
	
	public void setState(GameClientState pState)
	{
		if (_state != pState)
		{
			_state = pState;
			_packetQueue.clear();
		}
	}
	
	public ClientStats getStats()
	{
		return _stats;
	}
	
	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public void setPlayer(Player player)
	{
		_player = player;
	}
	
	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
	}
	
	public long[] getFloodProtectors()
	{
		return _floodProtectors;
	}
	
	public void setGameGuardOk(boolean val)
	{
		_isAuthedGG = val;
	}
	
	public void setAccountName(String pAccountName)
	{
		_accountName = pAccountName;
	}
	
	public String getAccountName()
	{
		return _accountName;
	}
	
	public void setSessionId(SessionKey sk)
	{
		_sessionId = sk;
	}
	
	public SessionKey getSessionId()
	{
		return _sessionId;
	}
	
	public void sendPacket(L2GameServerPacket gsp)
	{
		if (_isDetached)
			return;
		
		getConnection().sendPacket(gsp);
		gsp.runImpl();
	}
	
	public boolean isDetached()
	{
		return _isDetached;
	}
	
	public void setDetached(boolean b)
	{
		_isDetached = b;
	}
	
	/**
	 * Method to handle character deletion
	 * @param slot The slot to check.
	 * @return a byte:
	 *         <li>-1: Error: No char was found for such charslot, caught exception, etc...
	 *         <li>0: character is not member of any clan, proceed with deletion
	 *         <li>1: character is member of a clan, but not clan leader
	 *         <li>2: character is clan leader
	 */
	public byte markToDeleteChar(int slot)
	{
		final int objectId = getObjectIdForSlot(slot);
		if (objectId < 0)
			return -1;
		
		byte answer = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement(SELECT_CLAN))
			{
				ps.setInt(1, objectId);
				
				try (ResultSet rs = ps.executeQuery())
				{
					rs.next();
					
					final int clanId = rs.getInt(1);
					if (clanId != 0)
					{
						final Clan clan = ClanTable.getInstance().getClan(clanId);
						if (clan == null)
							answer = 0;
						else if (clan.getLeaderId() == objectId)
							answer = 2;
						else
							answer = 1;
					}
				}
			}
			
			// Setting delete time
			if (answer == 0)
			{
				if (Config.DELETE_DAYS == 0)
					deleteCharByObjId(objectId);
				else
				{
					try (PreparedStatement ps = con.prepareStatement(UPDATE_DELETE_TIME))
					{
						ps.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L);
						ps.setInt(2, objectId);
						ps.execute();
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't mark as delete a player.", e);
			return -1;
		}
		return answer;
	}
	
	public void markRestoredChar(int slot)
	{
		final int objectId = getObjectIdForSlot(slot);
		if (objectId < 0)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_DELETE_TIME))
		{
			ps.setLong(1, 0);
			ps.setInt(2, objectId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't restore player.", e);
		}
	}
	
	public static void deleteCharByObjId(int objectId)
	{
		if (objectId < 0)
			return;
		
		PlayerInfoTable.getInstance().removePlayer(objectId);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_FRIENDS))
			{
				ps.setInt(1, objectId);
				ps.setInt(2, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_HENNAS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_MACROS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_MEMOS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_QUESTS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_RECIPES))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SHORTCUTS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SKILLS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SKILLS_SAVE))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SUBCLASSES))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_HERO))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_NOBLE))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SEVEN_SIGNS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_PETS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_AUGMENTS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_ITEMS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_RBP))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete player.", e);
		}
	}
	
	public Player loadCharFromDisk(int slot)
	{
		final int objectId = getObjectIdForSlot(slot);
		if (objectId < 0)
			return null;
		
		Player player = World.getInstance().getPlayer(objectId);
		if (player != null)
		{
			if (player.getClient() != null)
				player.getClient().closeNow();
			else
				player.deleteMe();
			
			return null;
		}
		
		player = Player.restore(objectId);
		if (player != null)
		{
			player.setRunning();
			player.standUp();
			
			player.setOnlineStatus(true, false);
			World.getInstance().addPlayer(player);
		}
		return player;
	}
	
	/**
	 * Get a {@link CharSelectSlot} based on its id. Integrity checks are included.
	 * @param id : The slot id to call.
	 * @return the associated slot informations based on slot id.
	 */
	public CharSelectSlot getCharSelectSlot(int id)
	{
		if (_slots == null || id < 0 || id >= _slots.length)
			return null;
		
		return _slots[id];
	}
	
	/**
	 * Set the character selection slots.
	 * @param list : Use the List as character slots.
	 */
	public void setCharSelectSlot(CharSelectSlot[] list)
	{
		_slots = list;
	}
	
	public void close(L2GameServerPacket gsp)
	{
		getConnection().close(gsp);
	}
	
	private int getObjectIdForSlot(int charslot)
	{
		final CharSelectSlot info = getCharSelectSlot(charslot);
		if (info == null)
			return -1;
		
		return info.getObjectId();
	}
	
	/**
	 * Close client connection with {@link ServerClose} packet
	 */
	public synchronized void closeNow()
	{
		// Prevent packets execution.
		_isDetached = true;
		
		close(ServerClose.STATIC_PACKET);
		
		// Cancel cleanup task, if running.
		if (_cleanupTask != null)
		{
			_cleanupTask.cancel(true);
			_cleanupTask = null;
		}
		
		// Instant cleaning.
		ThreadPool.schedule(new CleanupTask(), 0);
	}
	
	public synchronized void cleanMe(boolean fast)
	{
		if (_cleanupTask == null)
			_cleanupTask = ThreadPool.schedule(new CleanupTask(), fast ? 100 : 15000);
	}
	
	protected class CleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			// Cancel the auto save task.
			if (_autoSaveInDB != null)
				_autoSaveInDB.cancel(true);
			
			// This should only happen on connection loss.
			if (getPlayer() != null)
			{
				// Prevent closing again.
				getPlayer().setClient(null);
				
				if (getPlayer().isOnline())
					getPlayer().deleteMe();
			}
			setPlayer(null);
			
			// Send logout packet, remove the client from connected clients.
			LoginServerThread.getInstance().sendLogout(getAccountName());
		}
	}
	
	/**
	 * @return false if client can receive packets. True if detached, or flood detected, or queue overflow detected and queue still not empty.
	 */
	public boolean dropPacket()
	{
		// Drop packet automatically if client is detached.
		if (_isDetached)
			return true;
		
		// A flood has been detected.
		if (getStats().countPacket(_packetQueue.size()))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		
		return getStats().dropPacket();
	}
	
	/**
	 * Counts buffer underflow exceptions.
	 */
	public void onBufferUnderflow()
	{
		if (_state == GameClientState.CONNECTED)
		{
			if (Config.PACKET_HANDLER_DEBUG)
				LOGGER.warn("{} has been disconnected: too many buffer underflows in non-authed state.", toString());
			
			closeNow();
		}
		else if (getStats().countUnderflowException())
		{
			LOGGER.warn("{} has been disconnected: too many buffer underflows.", toString());
			closeNow();
		}
	}
	
	/**
	 * Counts unknown packets
	 */
	public void onUnknownPacket()
	{
		if (_state == GameClientState.CONNECTED)
		{
			if (Config.PACKET_HANDLER_DEBUG)
				LOGGER.warn("{} has been disconnected: too many unknown packets in non-authed state.", toString());
			
			closeNow();
		}
		else if (getStats().countUnknownPacket())
		{
			LOGGER.warn("{} has been disconnected: too many unknown packets.", toString());
			closeNow();
		}
	}
	
	/**
	 * Add packet to the queue and start worker thread if needed
	 * @param packet The packet to execute.
	 */
	public void execute(ReceivablePacket<GameClient> packet)
	{
		if (getStats().countFloods())
		{
			LOGGER.warn("{} has been disconnected: too many floods ({} long and {} short).", toString(), getStats().longFloods, getStats().shortFloods);
			closeNow();
			return;
		}
		
		if (!_packetQueue.offer(packet))
		{
			if (getStats().countQueueOverflow())
			{
				LOGGER.warn("{} has been disconnected: too many queue overflows.", toString());
				closeNow();
			}
			else
				sendPacket(ActionFailed.STATIC_PACKET);
			
			return;
		}
		
		if (_queueLock.isLocked()) // already processing
			return;
		
		try
		{
			if (_state == GameClientState.CONNECTED && getStats().processedPackets > 3)
			{
				if (Config.PACKET_HANDLER_DEBUG)
					LOGGER.warn("{} has been disconnected: too many packets in non-authed state.", toString());
				
				closeNow();
				return;
			}
			
			ThreadPool.execute(this);
		}
		catch (RejectedExecutionException e)
		{
		}
	}
}
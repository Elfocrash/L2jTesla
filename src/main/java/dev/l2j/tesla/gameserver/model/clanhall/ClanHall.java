package dev.l2j.tesla.gameserver.model.clanhall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.data.sql.ClanTable;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.Door;
import dev.l2j.tesla.gameserver.model.pledge.Clan;
import dev.l2j.tesla.gameserver.model.zone.type.ClanHallZone;

/**
 * In Lineage 2, there are special building for clans: clan halls.<br>
 * <br>
 * Clan halls give the owning clan some useful benefits. There are 2 types of clan halls: auctionable and contestable. A clan can own only 1 hall at the same time.
 * <ul>
 * <li>Auctionable clan halls can be found in any big township, excluding starting villages, Oren and Heine. Any clan can purchase a hall via auction if they can afford it.</li>
 * <li>Some clan halls come into players possession only once they're conquered. Just like clan halls available via purchase, they are used for making items, teleportation, casting auras etc.</li>
 * </ul>
 */
public class ClanHall
{
	private static final CLogger LOGGER = new CLogger(ClanHall.class.getName());
	
	private static final String DELETE_FUNCTIONS = "DELETE FROM clanhall_functions WHERE hall_id=?";
	private static final String UPDATE_CH = "UPDATE clanhall SET ownerId=?, paidUntil=?, paid=?, sellerBid=?, sellerName=?, sellerClanName=?, endDate=? WHERE id=?";
	
	private static final int ONE_WEEK = 604800000; // One week
	
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_ITEM_CREATE = 2;
	public static final int FUNC_RESTORE_HP = 3;
	public static final int FUNC_RESTORE_MP = 4;
	public static final int FUNC_RESTORE_EXP = 5;
	public static final int FUNC_SUPPORT = 6;
	public static final int FUNC_DECO_FRONTPLATEFORM = 7;
	public static final int FUNC_DECO_CURTAINS = 8;
	
	private final Map<Integer, ClanHallFunction> _functions = new ConcurrentHashMap<>();
	private final List<Door> _doors = new ArrayList<>();
	
	private final int _id;
	private final String _name;
	private final String _desc;
	private final String _location;
	private final int _grade;
	private final int _lease;
	private final int _defaultBid;
	
	private ScheduledFuture<?> _feeTask;
	private Auction _auction;
	private int _ownerId;
	private ClanHallZone _zone;
	private long _paidUntil;
	private boolean _isPaid;
	
	public ClanHall(StatsSet set)
	{
		_id = set.getInteger("id");
		_name = set.getString("name");
		_desc = set.getString("desc");
		_location = set.getString("loc");
		_grade = set.getInteger("grade");
		_lease = set.getInteger("lease");
		_defaultBid = set.getInteger("defaultBid");
	}
	
	public final int getId()
	{
		return _id;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public final String getDesc()
	{
		return _desc;
	}
	
	public final String getLocation()
	{
		return _location;
	}
	
	public final int getGrade()
	{
		return _grade;
	}
	
	public final int getLease()
	{
		return _lease;
	}
	
	public final int getDefaultBid()
	{
		return _defaultBid;
	}
	
	public final Auction getAuction()
	{
		return _auction;
	}
	
	public final void setAuction(Auction auction)
	{
		_auction = auction;
	}
	
	public final int getOwnerId()
	{
		return _ownerId;
	}
	
	public void setOwnerId(int ownerId)
	{
		_ownerId = ownerId;
	}
	
	public final long getPaidUntil()
	{
		return _paidUntil;
	}
	
	public void setPaidUntil(long paidUntil)
	{
		_paidUntil = paidUntil;
	}
	
	public final boolean getPaid()
	{
		return _isPaid;
	}
	
	public void setPaid(boolean isPaid)
	{
		_isPaid = isPaid;
	}
	
	public ClanHallZone getZone()
	{
		return _zone;
	}
	
	public void setZone(ClanHallZone zone)
	{
		_zone = zone;
	}
	
	/**
	 * @return true if this {@link ClanHall} is free.
	 */
	public boolean isFree()
	{
		return _ownerId == 0;
	}
	
	/**
	 * @return the {@link List} of all {@link ClanHallFunction}s this {@link ClanHall} owns.
	 */
	public final Map<Integer, ClanHallFunction> getFunctions()
	{
		return _functions;
	}
	
	/**
	 * @return the {@link List} of all {@link Door}s this {@link ClanHall} owns.
	 */
	public final List<Door> getDoors()
	{
		return _doors;
	}
	
	/**
	 * @param doorId : The id to make checks on.
	 * @return the {@link Door} based on a doorId.
	 */
	public final Door getDoor(int doorId)
	{
		return _doors.stream().filter(d -> d.getDoorId() == doorId).findFirst().orElse(null);
	}
	
	/**
	 * @param type : The type of ClanHallFunction we search.
	 * @return the {@link ClanHallFunction} associated to the type.
	 */
	public ClanHallFunction getFunction(int type)
	{
		return _functions.get(type);
	}
	
	/**
	 * Free this {@link ClanHall}.
	 * <ul>
	 * <li>Remove the ClanHall from the Clan.</li>
	 * <li>Reset all variables to default.</li>
	 * <li>Delete functions, and update the database.</li>
	 * </ul>
	 */
	public void free()
	{
		// Cancel fee task, if existing.
		if (_feeTask != null)
		{
			_feeTask.cancel(false);
			_feeTask = null;
		}
		
		// Do some actions on previous owner, if any.
		final Clan clan = ClanTable.getInstance().getClan(_ownerId);
		if (clan != null)
		{
			// Set the clan hall id back to 0.
			clan.setClanHall(0);
			
			// Refresh Clan Action panel.
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		}
		
		_ownerId = 0;
		_paidUntil = 0;
		_isPaid = false;
		
		// Remove all related functions.
		removeAllFunctions();
		
		// Close all doors.
		openCloseDoors(false);
		
		if (_auction != null)
		{
			// Remove existing bids.
			_auction.removeBids(null);
			
			// Reset auction to initial values if existing.
			_auction.reset(true);
			
			// Launch the auction task.
			_auction.startAutoTask();
		}
		
		// Update dabase.
		updateDb();
	}
	
	/**
	 * Set {@link ClanHall} {@link Clan} owner. If previous owner was existing, do some actions on it.
	 * @param clan : The new ClanHall owner.
	 */
	public void setOwner(Clan clan)
	{
		if (_auction != null)
		{
			// Send back all losers bids, clear the Bidders Map.
			_auction.removeBids(clan);
			
			// Reset variables.
			_auction.reset(false);
		}
		
		// Verify that Clan isn't null.
		if (clan == null)
		{
			if (_auction != null)
				_auction.startAutoTask();
			
			return;
		}
		
		// Do some actions on previous owner, if any.
		final Clan owner = ClanTable.getInstance().getClan(_ownerId);
		if (owner != null)
		{
			// Set the clan hall id back to 0.
			owner.setClanHall(0);
			
			// Refresh Clan Action panel.
			owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
		}
		
		// Remove all related functions.
		removeAllFunctions();
		
		// Close all doors.
		openCloseDoors(false);
		
		clan.setClanHall(_id);
		
		_ownerId = clan.getClanId();
		_paidUntil = System.currentTimeMillis() + ONE_WEEK;
		_isPaid = true;
		
		// Initialize the Fee task for this Clan. The previous Fee task is dropped.
		initializeFeeTask();
		
		// Refresh Clan Action panel.
		clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		
		// Teleport out all outsiders (potential previous owners).
		banishForeigners();
		
		// Save all informations into database.
		updateDb();
	}
	
	/**
	 * Open or close a {@link ClanHall} {@link Door} by a {@link Player}. The Player must be owner of this ClanHall.
	 * @param player : The player who requested to open/close the door.
	 * @param doorId : The affected doorId.
	 * @param open : If set to true the door will open, false will close it.
	 */
	public void openCloseDoor(Player player, int doorId, boolean open)
	{
		if (player != null && player.getClanId() == getOwnerId())
			openCloseDoor(doorId, open);
	}
	
	/**
	 * Open or close a {@link ClanHall} {@link Door} based on its doorId.
	 * @param doorId : The affected doorId.
	 * @param open : If set to true the door will open, false will close it.
	 */
	public void openCloseDoor(int doorId, boolean open)
	{
		openCloseDoor(getDoor(doorId), open);
	}
	
	/**
	 * Open or close a {@link ClanHall} {@link Door}.
	 * @param door : The affected Door.
	 * @param open : If set to true the door will open, false will close it.
	 */
	public static void openCloseDoor(Door door, boolean open)
	{
		if (door != null)
		{
			if (open)
				door.openMe();
			else
				door.closeMe();
		}
	}
	
	/**
	 * Open or close all {@link ClanHall} related {@link Door}s by a {@link Player}. The Player must be owner of this ClanHall.
	 * @param player : The player who requested to open/close doors.
	 * @param open : If set to true the door will open, false will close it.
	 */
	public void openCloseDoors(Player player, boolean open)
	{
		if (player != null && player.getClanId() == getOwnerId())
			openCloseDoors(open);
	}
	
	/**
	 * Open or close all {@link ClanHall} related {@link Door}s.
	 * @param open : If set to true the door will open, false will close it.
	 */
	public void openCloseDoors(boolean open)
	{
		for (Door door : _doors)
		{
			if (open)
				door.openMe();
			else
				door.closeMe();
		}
	}
	
	/**
	 * Banish all {@link Player}s stranger to that {@link ClanHall} zone.
	 */
	public void banishForeigners()
	{
		if (_zone != null)
			_zone.banishForeigners(getOwnerId());
	}
	
	/**
	 * Remove all {@link ClanHallFunction}s linked to this {@link ClanHall}.
	 */
	public void removeAllFunctions()
	{
		_functions.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_FUNCTIONS))
		{
			ps.setInt(1, getId());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete all clan hall functions.", e);
		}
	}
	
	/**
	 * Update a {@link ClanHallFunction}s linked to this {@link ClanHall}.
	 * @param player : The player who requested the change.
	 * @param type : The type of ClanHallFunction to update.
	 * @param lvl : The new level to set.
	 * @param lease : The associated lease taken from Player inventory.
	 * @param rate : The new rate to set.
	 * @return true if the ClanHallFunction has been successfully updated.
	 */
	public boolean updateFunctions(Player player, int type, int lvl, int lease, long rate)
	{
		// Player doesn't exist.
		if (player == null)
			return false;
		
		// A lease exists, but the Player can't pay it using its inventory adena.
		if (lease > 0 && !player.destroyItemByItemId("Consume", 57, lease, null, true))
			return false;
		
		// The function doesn't exist, we create it.
		final ClanHallFunction chf = _functions.get(type);
		if (chf == null)
		{
			_functions.put(type, new ClanHallFunction(this, type, lvl, lease, rate, System.currentTimeMillis() + rate));
			return true;
		}
		
		// Both lease and level are set to 0, we remove the function.
		if (lvl == 0 && lease == 0)
			chf.removeFunction();
		// Refresh the function.
		else
			chf.refreshFunction(lease, lvl);
		
		return true;
	}
	
	/**
	 * Save all related informations of this {@link ClanHall} into database.
	 */
	public void updateDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_CH))
		{
			ps.setInt(1, _ownerId);
			ps.setLong(2, _paidUntil);
			ps.setInt(3, (_isPaid) ? 1 : 0);
			
			if (_auction != null)
			{
				if (_auction.getSeller() != null)
				{
					ps.setInt(4, _auction.getSeller().getBid());
					ps.setString(5, _auction.getSeller().getName());
					ps.setString(6, _auction.getSeller().getClanName());
				}
				else
				{
					ps.setInt(4, 0);
					ps.setString(5, "");
					ps.setString(6, "");
				}
				ps.setLong(7, _auction.getEndDate());
			}
			else
			{
				ps.setInt(4, 0);
				ps.setString(5, "");
				ps.setString(6, "");
				ps.setLong(7, 0);
			}
			ps.setInt(8, _id);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't update clan hall.", e);
		}
	}
	
	/**
	 * Initialize Fee Task.
	 */
	public void initializeFeeTask()
	{
		// Cancel fee task, if existing. We don't care setting it to null, since it is fed just after.
		if (_feeTask != null)
			_feeTask.cancel(false);
		
		// Take current time.
		long time = System.currentTimeMillis();
		
		// If time didn't past yet, calculate the difference and apply it on the Fee task. Otherwise, run it instantly.
		time = (_paidUntil > time) ? _paidUntil - time : 0;
		
		// Run the Fee task with the given, calculated, time.
		_feeTask = ThreadPool.schedule(new FeeTask(), time);
	}
	
	private class FeeTask implements Runnable
	{
		public FeeTask()
		{
		}
		
		@Override
		public void run()
		{
			// Don't bother if ClanHall is already free.
			if (isFree())
				return;
			
			// Clan can't be retrieved, we free the ClanHall.
			final Clan clan = ClanTable.getInstance().getClan(getOwnerId());
			if (clan == null)
			{
				free();
				return;
			}
			
			// We got enough adena, mark the ClanHall as being paid, send back the task one week later.
			if (clan.getWarehouse().getAdena() >= getLease())
			{
				// Delete the adena.
				clan.getWarehouse().destroyItemByItemId("CH_rental_fee", 57, getLease(), null, null);
				
				// Run the task one week later.
				_feeTask = ThreadPool.schedule(new FeeTask(), ONE_WEEK);
				
				// Refresh variables. Force _isPaid to be set on true, in case we return from grace period.
				_paidUntil += ONE_WEEK;
				_isPaid = true;
				
				// Save all informations into database.
				updateDb();
			}
			// Not enough adena.
			else
			{
				// The ClanHall was already under failed payment ; we free the ClanHall immediately.
				if (!_isPaid)
				{
					// Free the ClanHall.
					free();
					
					// Send message to all Clan members.
					clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
				}
				// Grace period, we will retest it one day later.
				else
				{
					// Run the task one day later.
					_feeTask = ThreadPool.schedule(new FeeTask(), ONE_WEEK);
					
					// Refresh variables.
					_paidUntil += ONE_WEEK;
					_isPaid = false;
					
					// Save all informations into database.
					updateDb();
					
					// Send message to all Clan members.
					clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addNumber(getLease()));
				}
			}
		}
	}
}
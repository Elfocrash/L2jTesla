package dev.l2j.tesla.gameserver.model.clanhall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.lang.StringUtil;
import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.pledge.Clan;

/**
 * An Auction container, used in conjonction with {@link ClanHall} system.<br>
 * <br>
 * <b>Purchasing Clan Halls</b><br>
 * <ul>
 * <li>The clan leader who placed the highest bid at the end of the auction wins the clan hall. Players cannot see how much others have bid.</li>
 * <li>Any player can view the clan halls in the auction, even if they are not qualified to make a bid.</li>
 * <li>When a clan leader places a bid on a clan hall, the bid amount of adena is taken from the clan's warehouse. Bids must be higher than the previous bid. Clan leaders cannot bid more than the amount of adena in the clan's warehouse.</li>
 * <li>Bids can be changed once they have been made by pressing the Rebid button, as long as the new bid is higher than the original.</li>
 * <li>If a bid is cancelled, the adena is returned, minus a ten percent tax fee.</li>
 * <li>If a clan disbands after placing a bid, the amount of the bid disappears.</li>
 * <li>If a clan acquires another clan hall while bidding, the bid is canceled automatically and the adena is returned to the clan's warehouse, minus taxes.</li>
 * <li>If two different clans placed the highest bid at the end of an auction, the clan hall is sold to the clan that bid first.</li>
 * <li>If a clan is successful in purchasing a clan hall, the clan leader receives a message that they won. Any previous residents that are still in the clan hall are kicked out.</li>
 * <li>The bid amounts of the clan leaders who bid unsuccessfully are returned to their clan's warehouses.</li>
 * </ul>
 * <b>Selling Clan Halls</b><br>
 * <ul>
 * <li>Clan leaders may put their clan hall up for auction through the clan hall manager.</li>
 * <li>Auction periods can be set for seven days, three days or one day. For example, if a three-day auction were set on Nov. 13 at 7:27pm, the end would be November 16 at 8:00pm.</li>
 * <li>Clan leaders can write a simple description of the clan hall they wish to auction.</li>
 * <li>Clan leaders must pay a deposit to put their clan hall up for auction.</li>
 * <li>The clan leader can cancel the auction during the set time, but the deposit is not returned and they cannot set up another auction for seven days.</li>
 * <li>When a clan hall does not have an owner, or has not been maintained sufficiently, it will be set up automatically for a seven-day auction period.</li>
 * <li>If the clan breaks up before the end of an auction period for their clan hall, the auction will continue, but the proceeds and deposit from selling the clan hall cannot be received.</li>
 * <li>If no one participates in an auction, the clan hall is returned to the owners, but their deposit is not returned.</li>
 * <li>If a clan successfully sells a clan hall, the clan leader will receive a message. The highest bid, minus taxes, is placed in the selling clan's warehouse, along with the deposit.</li>
 * </ul>
 */
public class Auction
{
	private static final CLogger LOGGER = new CLogger(Auction.class.getName());
	
	private static final String LOAD_BIDDERS = "SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM auction_bid WHERE auctionId = ? ORDER BY maxBid DESC";
	private static final String UPDATE_DATE = "UPDATE clanhall SET endDate=? WHERE id=?";
	private static final String INSERT_OR_UPDATE_BIDDER = "INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE bidderId=VALUES(bidderId), bidderName=VALUES(bidderName), maxBid=VALUES(maxBid), time_bid=VALUES(time_bid)";
	private static final String DELETE_BIDDERS = "DELETE FROM auction_bid WHERE auctionId=?";
	private static final String DELETE_BIDDER = "DELETE FROM auction_bid WHERE auctionId=? AND bidderId=?";
	private static final String UPDATE_SELLER = "UPDATE clanhall SET sellerBid=?, sellerName=?, sellerClanName=?, endDate=? WHERE id=?";
	
	private final Map<Integer, Bidder> _bidders = new HashMap<>();
	private final ClanHall _ch;
	
	private long _endDate;
	
	private Bidder _highestBidder;
	private Seller _seller;
	
	private Future<?> _task;
	
	public Auction(ClanHall ch, int sellerBid, String sellerName, String sellerClanName, long endDate)
	{
		_ch = ch;
		_endDate = endDate;
		
		// Generate a Seller, but only if leader name and clan name has been registered.
		if (!StringUtil.isEmpty(sellerName, sellerClanName))
			_seller = new Seller(sellerName, sellerClanName, sellerBid);
		
		// Load Bidders.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(LOAD_BIDDERS))
		{
			ps.setInt(1, ch.getId());
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final Bidder bidder = new Bidder(rs.getString("bidderName"), rs.getString("clan_name"), rs.getInt("maxBid"), rs.getLong("time_bid"));
					
					if (rs.isFirst())
						_highestBidder = bidder;
					
					_bidders.put(rs.getInt("bidderId"), bidder);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load Auction bid.", e);
		}
		
		startAutoTask();
	}
	
	public final long getEndDate()
	{
		return _endDate;
	}
	
	public final void setEndDate(long endDate)
	{
		_endDate = System.currentTimeMillis() + endDate;
	}
	
	public final Bidder getHighestBidder()
	{
		return _highestBidder;
	}
	
	public final Seller getSeller()
	{
		return _seller;
	}
	
	public final void setSeller(Clan clan, int bid)
	{
		if (clan == null)
			return;
		
		_seller = new Seller(clan.getLeaderName(), clan.getName(), bid);
	}
	
	public final Map<Integer, Bidder> getBidders()
	{
		return _bidders;
	}
	
	/**
	 * Test the auction process.<br>
	 * <br>
	 * If the end date already exceeded, add 1 week to the time and save it on database, otherwise, schedule a task to fire the auction ending process.
	 */
	public void startAutoTask()
	{
		long currentTime = System.currentTimeMillis();
		long taskDelay = 0;
		
		if (_endDate <= currentTime)
		{
			_endDate = currentTime + 604800000; // 1 week
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(UPDATE_DATE))
			{
				ps.setLong(1, _endDate);
				ps.setInt(2, _ch.getId());
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't save Auction date.", e);
			}
		}
		else
			taskDelay = _endDate - currentTime;
		
		_task = ThreadPool.schedule(() -> endAuction(), taskDelay);
	}
	
	/**
	 * Set a bid for the given {@link Auction}.
	 * @param player : The {@link Player} who requested the bid.
	 * @param bid : The bid amount.
	 */
	public synchronized void setBid(Player player, int bid)
	{
		final Clan clan = player.getClan();
		if (clan == null)
			return;
		
		// Bid price can't be lower than default ClanHall price, or seller price.
		if (bid <= getMinimumBid())
		{
			player.sendPacket(SystemMessageId.BID_PRICE_MUST_BE_HIGHER);
			return;
		}
		
		// Required adena to be spend. Default is the bid, but it can be influenced by the Bidder object, if existing.
		int requiredAdena = bid;
		
		// Retrieve the Bidder object.
		Bidder bidder = _bidders.get(player.getClanId());
		
		// Bidder object exists, we retrieve the bid.
		if (bidder != null)
		{
			// We test if bid we try to set is higher than stored bid.
			if (bid <= bidder.getBid())
			{
				player.sendPacket(SystemMessageId.BID_PRICE_MUST_BE_HIGHER);
				return;
			}
			
			// We calculate the difference, which will be taken from clan warehouse.
			requiredAdena -= bidder.getBid();
		}
		
		// We couldn't retrieve adena from clan warehouse, abort.
		if (!takeItem(player, requiredAdena))
			return;
		
		final long time = System.currentTimeMillis();
		
		// If the Bidder doesn't exist, we create it.
		if (bidder == null)
		{
			// If not existing, create it.
			bidder = new Bidder(clan.getLeaderName(), clan.getName(), bid, time);
			
			// Add it on the Bidder Map.
			_bidders.put(player.getClanId(), bidder);
		}
		// Refresh Bidder object if found.
		else
		{
			bidder.setBid(bid);
			bidder.setTime(time);
		}
		
		// Recalculate the new highest Bidder.
		recalculateHighestBidder();
		
		player.sendPacket(SystemMessageId.BID_IN_CLANHALL_AUCTION);
		
		// Set the auction bid on clan.
		clan.setAuctionBiddedAt(_ch.getId());
		
		// Save the bidder on database.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_OR_UPDATE_BIDDER))
		{
			ps.setInt(1, player.getClanId());
			ps.setInt(2, _ch.getId());
			ps.setInt(3, player.getClanId());
			ps.setString(4, player.getName());
			ps.setInt(5, bid);
			ps.setString(6, clan.getName());
			ps.setLong(7, time);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't update Auction.", e);
		}
	}
	
	/**
	 * Return Item in WHC
	 * @param clan : The {@link Clan} to make warehouse checks on.
	 * @param quantity : The amount of returned adenas.
	 * @param penalty : If true, 10% of quantity is lost.
	 */
	private static void returnItem(Clan clan, int quantity, boolean penalty)
	{
		if (clan == null)
			return;
		
		// Take 10% tax fee if penalty occurs.
		if (penalty)
			quantity *= 0.9;
		
		// avoid overflow on return
		final int limit = Integer.MAX_VALUE - clan.getWarehouse().getAdena();
		quantity = Math.min(quantity, limit);
		
		clan.getWarehouse().addItem("Outbidded", 57, quantity, null, null);
	}
	
	/**
	 * Take adenas from Clan warehouse. Thise method is used for the Auction confimation, holding lease.
	 * @param bidder : The bidder to make checks on.
	 * @param quantity : The amount of money.
	 * @return true if successful.
	 */
	public boolean takeItem(Player bidder, int quantity)
	{
		final Clan clan = bidder.getClan();
		if (clan == null)
			return false;
		
		// Clan warehouse check.
		if (clan.getWarehouse().getAdena() < quantity)
		{
			bidder.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_IN_CWH);
			return false;
		}
		
		clan.getWarehouse().destroyItemByItemId("Buy", 57, quantity, bidder, bidder);
		return true;
	}
	
	/**
	 * Remove bids.
	 * @param newOwner The Clan object who won the bid.
	 */
	public void removeBids(Clan newOwner)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_BIDDERS))
		{
			ps.setInt(1, _ch.getId());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't remove Auction bids.", e);
		}
		
		for (Bidder bidder : _bidders.values())
		{
			final Clan clan = bidder.getClan();
			if (clan == null)
				continue;
			
			clan.setAuctionBiddedAt(0);
			
			if (clan != newOwner)
				returnItem(clan, bidder.getBid(), true); // 10 % tax
				
			if (newOwner != null)
				clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLANHALL_AWARDED_TO_CLAN_S1).addString(newOwner.getName()));
		}
		_bidders.clear();
	}
	
	/** End of auction */
	public void endAuction()
	{
		// Reset task, but don't cancel current execution.
		if (_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
		
		// None bought this clanhall. We return in any case.
		if (_highestBidder == null)
		{
			// No owner, we simply refresh the Auction timer.
			if (_seller == null)
				startAutoTask();
			// If seller hasn't sell clanHall, the auction is dropped. Money of seller is lost.
			else
			{
				final Clan owner = _seller.getClan();
				if (owner == null)
					return;
				
				owner.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLANHALL_NOT_SOLD));
			}
			return;
		}
		
		// Return intial lease of seller + highest bid amount.
		if (_seller != null)
		{
			final Clan clan = _seller.getClan();
			
			returnItem(clan, _highestBidder.getBid(), true);
			returnItem(clan, _ch.getLease(), false);
		}
		
		// Set the new ClanHall owner.
		_ch.setOwner(_highestBidder.getClan());
	}
	
	/**
	 * Cancel bid
	 * @param objectId : The objectId of the bidder.
	 */
	public synchronized void cancelBid(int objectId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_BIDDER))
		{
			ps.setInt(1, _ch.getId());
			ps.setInt(2, objectId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't cancel Auction bid.", e);
		}
		
		final Bidder bidder = _bidders.remove(objectId);
		if (bidder != null)
		{
			final Clan clan = bidder.getClan();
			if (clan != null)
			{
				// Return the money to CWH.
				returnItem(clan, bidder.getBid(), true);
				
				// Drop the Auction id from Clan.
				clan.setAuctionBiddedAt(0);
			}
		}
		
		// The bidder was the highest bidder ; we recalculate it.
		if (bidder == _highestBidder)
			recalculateHighestBidder();
	}
	
	/** Cancel auction */
	public void cancelAuction()
	{
		if (_seller == null)
			return;
		
		// Remove all bids.
		removeBids(_seller.getClan());
		
		// Reset variables.
		reset(false);
		
		// Save all informations into database.
		_ch.updateDb();
	}
	
	/** Confirm an auction */
	public void confirmAuction()
	{
		if (_seller == null)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_SELLER))
		{
			ps.setInt(1, _seller.getBid());
			ps.setString(2, _seller.getName());
			ps.setString(3, _seller.getClanName());
			ps.setLong(4, _endDate);
			ps.setInt(5, _ch.getId());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't confirm Auction.", e);
		}
	}
	
	public void recalculateHighestBidder()
	{
		Bidder highestBidder = null;
		int highestBid = 0;
		
		for (Bidder bidder : _bidders.values())
		{
			// The current bidder outperformed current highest bid, we register him and use his bid as new value.
			if (bidder.getBid() > highestBid)
			{
				// Store the Bidder.
				highestBidder = bidder;
				
				// The highest bid becomes the bidder bid.
				highestBid = bidder.getBid();
			}
		}
		
		// Set the new higher Bidder.
		_highestBidder = highestBidder;
	}
	
	/**
	 * Reset all variables of this {@link Auction}. It has to be used with {@link ClanHall#updateDb()}.
	 * @param runTask : If true, we also care about stopping and renew the auto task.
	 */
	public void reset(boolean runTask)
	{
		// Clean variables.
		_highestBidder = null;
		_seller = null;
		
		_endDate = 0;
		
		// Reset task.
		if (_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
		
		if (runTask)
			startAutoTask();
	}
	
	public int getMinimumBid()
	{
		return (_seller == null) ? _ch.getDefaultBid() : Math.max(_ch.getDefaultBid(), _seller.getBid());
	}
}
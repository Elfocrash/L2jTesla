package dev.l2j.tesla.gameserver.data.manager;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.clanhall.Auction;
import dev.l2j.tesla.gameserver.model.clanhall.ClanHall;
import dev.l2j.tesla.gameserver.model.clanhall.ClanHallFunction;
import dev.l2j.tesla.gameserver.model.pledge.Clan;
import dev.l2j.tesla.gameserver.model.zone.type.ClanHallZone;
import dev.l2j.tesla.commons.data.xml.IXmlReader;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.data.sql.ClanTable;

import org.w3c.dom.Document;

/**
 * Loads and store {@link ClanHall}s informations, along their associated {@link Auction}s (if existing), using database and XML informations.
 */
public class ClanHallManager implements IXmlReader
{
	private static final String LOAD_CLANHALLS = "SELECT * FROM clanhall";
	private static final String LOAD_FUNCTIONS = "SELECT * FROM clanhall_functions WHERE hall_id = ?";
	
	private final Map<Integer, ClanHall> _clanHalls = new HashMap<>();
	
	protected ClanHallManager()
	{
		// Build ClanHalls objects with static data.
		load();
		
		// Generate the List of ClanHallZones.
		final Collection<ClanHallZone> zones = ZoneManager.getInstance().getAllZones(ClanHallZone.class);
		
		// Add dynamic data.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(LOAD_CLANHALLS);
             PreparedStatement ps2 = con.prepareStatement(LOAD_FUNCTIONS);
             ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final int id = rs.getInt("id");
				
				final ClanHall ch = _clanHalls.get(id);
				if (ch == null)
					continue;
				
				// Find the related zone, and associate it with the Clan Hall.
				final ClanHallZone zone = zones.stream().filter(z -> z.getClanHallId() == id).findFirst().orElse(null);
				if (zone == null)
					LOGGER.warn("No existing ClanHallZone for ClanHall {}.", id);
				
				ch.setZone(zone);
				
				// Generate an Auction if default bid exists for this Clan Hall.
				if (ch.getDefaultBid() > 0)
					ch.setAuction(new Auction(ch, rs.getInt("sellerBid"), rs.getString("sellerName"), rs.getString("sellerClanName"), rs.getLong("endDate")));
				
				final int ownerId = rs.getInt("ownerId");
				if (ownerId > 0)
				{
					final Clan clan = ClanTable.getInstance().getClan(ownerId);
					if (clan == null)
					{
						ch.free();
						continue;
					}
					
					// Set Clan variable.
					clan.setClanHall(id);
					
					// Set ClanHall variables.
					ch.setOwnerId(ownerId);
					ch.setPaidUntil(rs.getLong("paidUntil"));
					ch.setPaid(rs.getBoolean("paid"));
					
					// Initialize the fee task.
					ch.initializeFeeTask();
					
					// Load related ClanHallFunctions.
					ps2.setInt(1, id);
					
					try (ResultSet rs2 = ps2.executeQuery())
					{
						while (rs2.next())
							ch.getFunctions().put(rs2.getInt("type"), new ClanHallFunction(ch, rs2.getInt("type"), rs2.getInt("lvl"), rs2.getInt("lease"), rs2.getLong("rate"), rs2.getLong("endTime")));
					}
					ps2.clearParameters();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load clan hall data.", e);
		}
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/clanHalls.xml");
		LOGGER.info("Loaded {} clan halls.", _clanHalls.size());
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "clanhall", armorsetNode ->
		{
			final StatsSet set = parseAttributes(armorsetNode);
			_clanHalls.put(set.getInteger("id"), new ClanHall(set));
		}));
	}
	
	/**
	 * @param id : The ChanHall id to retrieve.
	 * @return a {@link ClanHall} by its id.
	 */
	public final ClanHall getClanHall(int id)
	{
		return _clanHalls.get(id);
	}
	
	/**
	 * @return a {@link Map} with all {@link ClanHall}s.
	 */
	public final Map<Integer, ClanHall> getClanHalls()
	{
		return _clanHalls;
	}
	
	/**
	 * @return a {@link List} with all auctionable {@link ClanHall}s.
	 */
	public final List<ClanHall> getAuctionableClanHalls()
	{
		final List<ClanHall> list = new ArrayList<>();
		for (ClanHall ch : _clanHalls.values())
		{
			// No Auction has been registered for this ClanHall - continue.
			final Auction auction = ch.getAuction();
			if (auction == null)
				continue;
			
			// ClanHall is owned, but no Auction has been registered by the owner - continue.
			if (ch.getOwnerId() > 0 && auction.getSeller() == null)
				continue;
			
			list.add(ch);
		}
		return list;
	}
	
	/**
	 * @param location : The location name used as parameter.
	 * @return a {@link List} with all {@link ClanHall}s which are in a given location.
	 */
	public final List<ClanHall> getClanHallsByLocation(String location)
	{
		return _clanHalls.values().stream().filter(ch -> ch.getLocation().equalsIgnoreCase(location)).collect(Collectors.toList());
	}
	
	/**
	 * @param clan : The {@link Clan} to check.
	 * @return the {@link ClanHall} owned by the Clan, or null otherwise.
	 */
	public final ClanHall getClanHallByOwner(Clan clan)
	{
		return _clanHalls.values().stream().filter(ch -> ch.getOwnerId() == clan.getClanId()).findFirst().orElse(null);
	}
	
	/**
	 * @param x : The X coordinate to check.
	 * @param y : The Y coordinate to check.
	 * @param maxDist : The radius we must search.
	 * @return the {@link ClanHall} associated to the X/Y coordinates.
	 */
	public final ClanHall getNearestClanHall(int x, int y, int maxDist)
	{
		for (ClanHall ch : _clanHalls.values())
		{
			if (ch.getZone() != null && ch.getZone().getDistanceToZone(x, y) < maxDist)
				return ch;
		}
		return null;
	}
	
	/**
	 * @param id : The ClanHall id used as reference.
	 * @return the {@link Auction} associated to a {@link ClanHall}, or null if not existing.
	 */
	public final Auction getAuction(int id)
	{
		final ClanHall ch = _clanHalls.get(id);
		return (ch == null) ? null : ch.getAuction();
	}
	
	public static ClanHallManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanHallManager INSTANCE = new ClanHallManager();
	}
}
package dev.l2j.tesla.gameserver.data.xml;

import java.nio.file.Path;

import dev.l2j.tesla.gameserver.data.manager.CastleManager;
import dev.l2j.tesla.gameserver.data.manager.ClanHallManager;
import dev.l2j.tesla.gameserver.data.manager.ZoneManager;
import dev.l2j.tesla.gameserver.enums.SiegeSide;
import dev.l2j.tesla.gameserver.enums.ZoneId;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.clanhall.ClanHall;
import dev.l2j.tesla.gameserver.model.entity.Castle;
import dev.l2j.tesla.gameserver.model.entity.Siege;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.model.zone.type.ArenaZone;
import dev.l2j.tesla.gameserver.model.zone.type.ClanHallZone;
import dev.l2j.tesla.gameserver.model.zone.type.TownZone;
import dev.l2j.tesla.commons.data.xml.IXmlReader;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

/**
 * This class loads and stores map regions values under a 2D int array.<br>
 * <br>
 * It is notably used to find closest {@link Location} to teleport, closest {@link TownZone} zone or name, etc.
 */
public class MapRegionData implements IXmlReader
{
	public static enum TeleportType
	{
		CASTLE,
		CLAN_HALL,
		SIEGE_FLAG,
		TOWN
	}
	
	private static final int REGIONS_X = 11;
	private static final int REGIONS_Y = 16;
	
	private static final Location MDT_LOCATION = new Location(12661, 181687, -3560);
	
	private final int[][] _regions = new int[REGIONS_X][REGIONS_Y];
	
	protected MapRegionData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/mapRegions.xml");
		LOGGER.info("Loaded regions.");
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "map", mapNode ->
		{
			final NamedNodeMap attrs = mapNode.getAttributes();
			final int rY = parseInteger(attrs, "geoY") - 10;
			for (int rX = 0; rX < REGIONS_X; rX++)
				_regions[rX][rY] = parseInteger(attrs, "geoX_" + (rX + 16));
		}));
	}
	
	public final int getMapRegion(int posX, int posY)
	{
		return _regions[getMapRegionX(posX)][getMapRegionY(posY)];
	}
	
	public static final int getMapRegionX(int posX)
	{
		// +4 to shift coords center
		return (posX >> 15) + 4;
	}
	
	public static final int getMapRegionY(int posY)
	{
		// +8 to shift coords center
		return (posY >> 15) + 8;
	}
	
	/**
	 * @param x : The X value (part of 2D point) to check.
	 * @param y : The Y value (part of 2D point) to check.
	 * @return the castleId associated to the townId, based on X/Y points.
	 */
	public final int getAreaCastle(int x, int y)
	{
		switch (getMapRegion(x, y))
		{
			case 0: // Talking Island Village
			case 5: // Town of Gludio
			case 6: // Gludin Village
				return 1;
			
			case 7: // Town of Dion
				return 2;
			
			case 8: // Town of Giran
			case 12: // Giran Harbor
				return 3;
			
			case 1: // Elven Village
			case 2: // Dark Elven Village
			case 9: // Town of Oren
			case 17: // Floran Village
				return 4;
			
			case 10: // Town of Aden
			case 11: // Hunters Village
			default: // Town of Aden
				return 5;
			
			case 13: // Heine
				return 6;
			
			case 15: // Town of Goddard
				return 7;
			
			case 14: // Rune Township
			case 18: // Primeval Isle Wharf
				return 8;
			
			case 3: // Orc Village
			case 4: // Dwarven Village
			case 16: // Town of Schuttgart
				return 9;
		}
	}
	
	/**
	 * @param x : The X value (part of 2D point) to check.
	 * @param y : The Y value (part of 2D point) to check.
	 * @return a String consisting of town name, based on X/Y points.
	 */
	public String getClosestTownName(int x, int y)
	{
		switch (getMapRegion(x, y))
		{
			case 0:
				return "Talking Island Village";
			
			case 1:
				return "Elven Village";
			
			case 2:
				return "Dark Elven Village";
			
			case 3:
				return "Orc Village";
			
			case 4:
				return "Dwarven Village";
			
			case 5:
				return "Town of Gludio";
			
			case 6:
				return "Gludin Village";
			
			case 7:
				return "Town of Dion";
			
			case 8:
				return "Town of Giran";
			
			case 9:
				return "Town of Oren";
			
			case 10:
				return "Town of Aden";
			
			case 11:
				return "Hunters Village";
			
			case 12:
				return "Giran Harbor";
			
			case 13:
				return "Heine";
			
			case 14:
				return "Rune Township";
			
			case 15:
				return "Town of Goddard";
			
			case 16:
				return "Town of Schuttgart";
			
			case 17:
				return "Floran Village";
			
			case 18:
				return "Primeval Isle";
			
			default:
				return "Town of Aden";
		}
	}
	
	/**
	 * @param creature : The Creature to check.
	 * @param teleportType : The TeleportType to check.
	 * @return a {@link Location} based on {@link Creature} and {@link TeleportType} parameters.
	 */
	public Location getLocationToTeleport(Creature creature, TeleportType teleportType)
	{
		// The character isn't a player, bypass all checks and retrieve a random spawn location on closest town.
		if (!(creature instanceof Player))
			return getClosestTown(creature).getRandomLoc();
		
		final Player player = ((Player) creature);
		
		// The player is in MDT, move him out.
		if (player.isInsideZone(ZoneId.MONSTER_TRACK))
			return MDT_LOCATION;
		
		if (teleportType != TeleportType.TOWN && player.getClan() != null)
		{
			if (teleportType == TeleportType.CLAN_HALL)
			{
				final ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
				if (ch != null)
				{
					final ClanHallZone zone = ch.getZone();
					if (zone != null)
						return zone.getRandomLoc();
				}
			}
			else if (teleportType == TeleportType.CASTLE)
			{
				// Check if the player is part of a castle owning clan.
				Castle castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
				if (castle == null)
				{
					// If not, check if he is in defending side.
					castle = CastleManager.getInstance().getCastle(player);
					if (!(castle != null && castle.getSiege().isInProgress() && castle.getSiege().checkSides(player.getClan(), SiegeSide.DEFENDER, SiegeSide.OWNER)))
						castle = null;
				}
				
				if (castle != null && castle.getCastleId() > 0)
					return castle.getCastleZone().getRandomLoc();
			}
			else if (teleportType == TeleportType.SIEGE_FLAG)
			{
				final Siege siege = CastleManager.getInstance().getActiveSiege(player);
				if (siege != null)
				{
					final Npc flag = siege.getFlag(player.getClan());
					if (flag != null)
						return flag.getPosition();
				}
			}
		}
		
		// Check if the player needs to be teleported in second closest town, during an active siege.
		final Castle castle = CastleManager.getInstance().getCastle(player);
		if (castle != null && castle.getSiege().isInProgress())
			return (player.getKarma() > 0) ? castle.getSiegeZone().getRandomChaoticLoc() : castle.getSiegeZone().getRandomLoc();
		
		// Karma player lands out of city.
		if (player.getKarma() > 0)
			return getClosestTown(player).getRandomChaoticLoc();
		
		// Check if player is in arena.
		final ArenaZone arena = ZoneManager.getInstance().getZone(player, ArenaZone.class);
		if (arena != null)
			return arena.getRandomLoc();
		
		// Retrieve a random spawn location of the nearest town.
		return getClosestTown(player).getRandomLoc();
	}
	
	/**
	 * @param creature : The Creature used to find race, x and y.
	 * @return the closest {@link TownZone} based on a {@link Creature}. There's a Race condition for elf / dark elf races.
	 */
	private final TownZone getClosestTown(Creature creature)
	{
		switch (getMapRegion(creature.getX(), creature.getY()))
		{
			case 0: // TI
				return getTown(2);
			
			case 1:// Elven
				return getTown((creature instanceof Player && ((Player) creature).getTemplate().getRace() == ClassRace.DARK_ELF) ? 1 : 3);
			
			case 2:// DE
				return getTown((creature instanceof Player && ((Player) creature).getTemplate().getRace() == ClassRace.ELF) ? 3 : 1);
			
			case 3: // Orc
				return getTown(4);
			
			case 4:// Dwarven
				return getTown(6);
			
			case 5:// Gludio
				return getTown(7);
			
			case 6:// Gludin
				return getTown(5);
			
			case 7: // Dion
				return getTown(8);
			
			case 8: // Giran
			case 12: // Giran Harbor
				return getTown(9);
			
			case 9: // Oren
				return getTown(10);
			
			case 10: // Aden
				return getTown(12);
			
			case 11: // HV
				return getTown(11);
			
			case 13: // Heine
				return getTown(15);
			
			case 14: // Rune
				return getTown(14);
			
			case 15: // Goddard
				return getTown(13);
			
			case 16: // Schuttgart
				return getTown(17);
			
			case 17:// Floran
				return getTown(16);
			
			case 18:// Primeval Isle
				return getTown(19);
		}
		return getTown(16); // Default to floran
	}
	
	/**
	 * @param x : The X value (part of 2D point) to check.
	 * @param y : The Y value (part of 2D point) to check.
	 * @return the closest regionId based on X/Y points.
	 */
	public final int getClosestLocation(int x, int y)
	{
		switch (getMapRegion(x, y))
		{
			case 0: // TI
				return 1;
			
			case 1: // Elven
				return 4;
			
			case 2: // DE
				return 3;
			
			case 3: // Orc
			case 4: // Dwarven
			case 16:// Schuttgart
				return 9;
			
			case 5: // Gludio
			case 6: // Gludin
				return 2;
			
			case 7: // Dion
				return 5;
			
			case 8: // Giran
			case 12: // Giran Harbor
				return 6;
			
			case 9: // Oren
				return 10;
			
			case 10: // Aden
				return 13;
			
			case 11: // HV
				return 11;
			
			case 13: // Heine
				return 12;
			
			case 14: // Rune
				return 14;
			
			case 15: // Goddard
				return 15;
		}
		return 0;
	}
	
	/**
	 * @param townId : the townId to match.
	 * @return a {@link TownZone} based on the overall list of existing towns, matching the townId.
	 */
	public static final TownZone getTown(int townId)
	{
		return ZoneManager.getInstance().getAllZones(TownZone.class).stream().filter(t -> t.getTownId() == townId).findFirst().orElse(null);
	}
	
	/**
	 * @param x : The X value (part of 3D point) to check.
	 * @param y : The Y value (part of 3D point) to check.
	 * @param z : The Z value (part of 3D point) to check.
	 * @return a {@link TownZone} based on the overall list of existing towns, matching X/Y/Z points.
	 */
	public static final TownZone getTown(int x, int y, int z)
	{
		return ZoneManager.getInstance().getZone(x, y, z, TownZone.class);
	}
	
	/**
	 * @param x : The X coordinate.
	 * @param y : The Y coordinate.
	 * @return the String used for Auctioneer picture name.
	 */
	public final String getPictureName(int x, int y)
	{
		switch (getMapRegion(x, y))
		{
			case 5:
				return "GLUDIO";
			
			case 6:
				return "GLUDIN";
			
			case 7:
				return "DION";
			
			case 8:
				return "GIRAN";
			
			case 14:
				return "RUNE";
			
			case 15:
				return "GODARD";
			
			case 16:
				return "SCHUTTGART";
			
			default:
				return "ADEN";
		}
	}
	
	public static MapRegionData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MapRegionData INSTANCE = new MapRegionData();
	}
}
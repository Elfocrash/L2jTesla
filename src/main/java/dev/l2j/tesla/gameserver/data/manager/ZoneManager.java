package dev.l2j.tesla.gameserver.data.manager;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.WorldRegion;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.zone.SpawnZoneType;
import dev.l2j.tesla.gameserver.model.zone.ZoneType;
import dev.l2j.tesla.gameserver.model.zone.form.ZoneCuboid;
import dev.l2j.tesla.gameserver.model.zone.form.ZoneCylinder;
import dev.l2j.tesla.gameserver.model.zone.form.ZoneNPoly;
import dev.l2j.tesla.gameserver.model.zone.type.BossZone;
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket;
import dev.l2j.tesla.commons.data.xml.IXmlReader;
import dev.l2j.tesla.commons.lang.StringUtil;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Loads and stores zones, based on their {@link ZoneType}.
 */
public class ZoneManager implements IXmlReader
{
	private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";
	private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";
	
	private final Map<Class<? extends ZoneType>, Map<Integer, ? extends ZoneType>> _zones = new HashMap<>();
	private final Map<Integer, ItemInstance> _debugItems = new ConcurrentHashMap<>();
	
	private int _lastDynamicId = 0;
	
	protected ZoneManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/zones");
		LOGGER.info("Loaded {} zones classes and total {} zones.", _zones.size(), _zones.values().stream().mapToInt(Map::size).sum());
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		_lastDynamicId = (_lastDynamicId / 1000) * 1000 + 1000;
		
		final String zoneType = StringUtil.getNameWithoutExtension(path.toFile().getName());
		
		// Create the Constructor, based on file name. It is reused by every zone.
		Constructor<?> zoneConstructor;
		try
		{
			zoneConstructor = Class.forName("dev.l2j.tesla.gameserver.model.zone.type." + zoneType).getConstructor(int.class);
		}
		catch (Exception e)
		{
			LOGGER.error("The zone type {} doesn't exist. Abort zones loading for {}.", e, zoneType, path.toFile().getName());
			return;
		}
		
		forEach(doc, "list", listNode -> forEach(listNode, "zone", zoneNode ->
		{
			final NamedNodeMap attrs = zoneNode.getAttributes();
			final Node attribute = attrs.getNamedItem("id");
			final int zoneId = attribute == null ? _lastDynamicId++ : Integer.parseInt(attribute.getNodeValue());
			final ZoneType temp;
			try
			{
				temp = (ZoneType) zoneConstructor.newInstance(zoneId);
			}
			catch (Exception e)
			{
				LOGGER.error("The zone id {} couldn't be instantiated.", e, zoneId);
				return;
			}
			
			final String zoneShape = parseString(attrs, "shape");
			final int minZ = parseInteger(attrs, "minZ");
			final int maxZ = parseInteger(attrs, "maxZ");
			
			final List<IntIntHolder> nodes = new ArrayList<>();
			forEach(zoneNode, "node", nodeNode ->
			{
				final NamedNodeMap nodeAttrs = nodeNode.getAttributes();
				nodes.add(new IntIntHolder(parseInteger(nodeAttrs, "x"), parseInteger(nodeAttrs, "y")));
			});
			if (nodes.isEmpty())
			{
				LOGGER.warn("Missing nodes for zone {} in file {}.", zoneId, zoneType);
				return;
			}
			
			forEach(zoneNode, "stat", statNode ->
			{
				final NamedNodeMap statAttrs = statNode.getAttributes();
				temp.setParameter(parseString(statAttrs, "name"), parseString(statAttrs, "val"));
			});
			
			if (temp instanceof SpawnZoneType)
			{
				forEach(zoneNode, "spawn", spawnNode ->
				{
					final NamedNodeMap spawnAttrs = spawnNode.getAttributes();
					((SpawnZoneType) temp).addLoc(parseLocation(spawnNode), parseBoolean(spawnAttrs, "isChaotic", false));
				});
			}
			
			final IntIntHolder[] coords = nodes.toArray(new IntIntHolder[nodes.size()]);
			switch (zoneShape)
			{
				case "Cuboid":
					if (coords.length == 2)
						temp.setZone(new ZoneCuboid(coords[0].getId(), coords[1].getId(), coords[0].getValue(), coords[1].getValue(), minZ, maxZ));
					else
					{
						LOGGER.warn("Missing cuboid nodes for zone {} in file {}.", zoneId, zoneType);
						return;
					}
					break;
				case "NPoly":
					if (coords.length > 2)
					{
						final int[] aX = new int[coords.length];
						final int[] aY = new int[coords.length];
						for (int i = 0; i < coords.length; i++)
						{
							aX[i] = coords[i].getId();
							aY[i] = coords[i].getValue();
						}
						temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ));
					}
					else
					{
						LOGGER.warn("Missing NPoly nodes for zone {} in file {}.", zoneId, zoneType);
						return;
					}
					break;
				case "Cylinder":
					final int zoneRad = parseInteger(attrs, "rad");
					if (coords.length == 1 && zoneRad > 0)
						temp.setZone(new ZoneCylinder(coords[0].getId(), coords[0].getValue(), minZ, maxZ, zoneRad));
					else
					{
						LOGGER.warn("Missing Cylinder nodes for zone {} in file {}.", zoneId, zoneType);
						return;
					}
					break;
				default:
					LOGGER.warn("Unknown {} shape in file {}.", zoneShape, zoneType);
					return;
			}
			
			addZone(zoneId, temp);
			
			final WorldRegion[][] regions = World.getInstance().getWorldRegions();
			for (int x = 0; x < regions.length; x++)
			{
				final int xLoc = World.getRegionX(x);
				final int xLoc2 = World.getRegionX(x + 1);
				for (int y = 0; y < regions[x].length; y++)
					if (temp.getZone().intersectsRectangle(xLoc, xLoc2, World.getRegionY(y), World.getRegionY(y + 1)))
						regions[x][y].addZone(temp);
			}
		}));
	}
	
	/**
	 * Reload zones using following steps :
	 * <ul>
	 * <li>Save boss zones data.</li>
	 * <li>Clean zones from all regions.</li>
	 * <li>Clear containers.</li>
	 * <li>Use the regular load process.</li>
	 * <li>Revalidate zones for all existing creatures.</li>
	 * </ul>
	 */
	public void reload()
	{
		// Save boss zones data.
		save();
		
		// Remove zones from world.
		for (WorldRegion[] regions : World.getInstance().getWorldRegions())
		{
			for (WorldRegion region : regions)
				region.getZones().clear();
		}
		
		// Clear _zones and _debugItems Maps.
		_zones.clear();
		clearDebugItems();
		
		// Reset dynamic id.
		_lastDynamicId = 0;
		
		// Load all zones.
		load();
		
		// Revalidate creatures in zones.
		for (WorldObject object : World.getInstance().getObjects())
		{
			if (object instanceof Creature)
				((Creature) object).revalidateZone(true);
		}
	}
	
	/**
	 * Save boss zone data.<br>
	 * <br>
	 * We first clear existing entries, than we save each zone data on database.
	 */
	public final void save()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// clear table first
			try (PreparedStatement ps = con.prepareStatement(DELETE_GRAND_BOSS_LIST))
			{
				ps.executeUpdate();
			}
			
			// store actual data
			try (PreparedStatement ps = con.prepareStatement(INSERT_GRAND_BOSS_LIST))
			{
				for (ZoneType zone : _zones.get(BossZone.class).values())
				{
					for (int player : ((BossZone) zone).getAllowedPlayers())
					{
						ps.setInt(1, player);
						ps.setInt(2, zone.getId());
						ps.addBatch();
					}
				}
				ps.executeBatch();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Error storing boss zones.", e);
		}
		LOGGER.info("Saved boss zones data.");
	}
	
	/**
	 * Add a new zone into _zones {@link Map}. If the zone type doesn't exist, generate the entry first.
	 * @param id : The zone id to add.
	 * @param <T> : The {@link ZoneType} children class.
	 * @param zone : The zone to add.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> void addZone(Integer id, T zone)
	{
		Map<Integer, T> map = (Map<Integer, T>) _zones.get(zone.getClass());
		if (map == null)
		{
			map = new HashMap<>();
			map.put(id, zone);
			_zones.put(zone.getClass(), map);
		}
		else
			map.put(id, zone);
	}
	
	/**
	 * @param <T> : The {@link ZoneType} children class.
	 * @param type : The Class type to refer.
	 * @return all zones by {@link Class} type.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> Collection<T> getAllZones(Class<T> type)
	{
		return (Collection<T>) _zones.get(type).values();
	}
	
	/**
	 * @param id : The zone id to retrieve.
	 * @return the first zone matching id.
	 */
	public ZoneType getZoneById(int id)
	{
		for (Map<Integer, ? extends ZoneType> map : _zones.values())
		{
			if (map.containsKey(id))
				return map.get(id);
		}
		return null;
	}
	
	/**
	 * @param <T> : The {@link ZoneType} children class.
	 * @param id : The zone id to retrieve.
	 * @param type : The Class type to refer.
	 * @return a zone by id and {@link Class}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZoneById(int id, Class<T> type)
	{
		return (T) _zones.get(type).get(id);
	}
	
	/**
	 * @param object : The object position to refer.
	 * @return all zones based on object position.
	 */
	public List<ZoneType> getZones(WorldObject object)
	{
		return getZones(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * @param <T> : The {@link ZoneType} children class.
	 * @param object : The object position to refer.
	 * @param type : The Class type to refer.
	 * @return a zone based on object position and zone {@link Class}.
	 */
	public <T extends ZoneType> T getZone(WorldObject object, Class<T> type)
	{
		if (object == null)
			return null;
		
		return getZone(object.getX(), object.getY(), object.getZ(), type);
	}
	
	/**
	 * @param x : The X location to check.
	 * @param y : The Y location to check.
	 * @return all zones on a 2D plane from given coordinates (no matter their {@link Class}).
	 */
	public List<ZoneType> getZones(int x, int y)
	{
		final List<ZoneType> temp = new ArrayList<>();
		for (ZoneType zone : World.getInstance().getRegion(x, y).getZones())
		{
			if (zone.isInsideZone(x, y))
				temp.add(zone);
		}
		return temp;
	}
	
	/**
	 * @param x : The X location to check.
	 * @param y : The Y location to check.
	 * @param z : The Z location to check.
	 * @return all zones on a 3D plane from given coordinates (no matter their {@link Class}).
	 */
	public List<ZoneType> getZones(int x, int y, int z)
	{
		final List<ZoneType> temp = new ArrayList<>();
		for (ZoneType zone : World.getInstance().getRegion(x, y).getZones())
		{
			if (zone.isInsideZone(x, y, z))
				temp.add(zone);
		}
		return temp;
	}
	
	/**
	 * @param <T> : The {@link ZoneType} children class.
	 * @param x : The X location to check.
	 * @param y : The Y location to check.
	 * @param type : The Class type to refer.
	 * @return a zone based on given coordinates and its {@link Class}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZone(int x, int y, Class<T> type)
	{
		for (ZoneType zone : World.getInstance().getRegion(x, y).getZones())
		{
			if (zone.isInsideZone(x, y) && type.isInstance(zone))
				return (T) zone;
		}
		return null;
	}
	
	/**
	 * @param <T> : The {@link ZoneType} children class.
	 * @param x : The X location to check.
	 * @param y : The Y location to check.
	 * @param z : The Z location to check.
	 * @param type : The Class type to refer.
	 * @return a zone based on given coordinates and its {@link Class}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZone(int x, int y, int z, Class<T> type)
	{
		for (ZoneType zone : World.getInstance().getRegion(x, y).getZones())
		{
			if (zone.isInsideZone(x, y, z) && type.isInstance(zone))
				return (T) zone;
		}
		return null;
	}
	
	/**
	 * Add an {@link ItemInstance} on debug list. Used to visualize zones.
	 * @param item : The item to add.
	 */
	public void addDebugItem(ItemInstance item)
	{
		_debugItems.put(item.getObjectId(), item);
	}
	
	/**
	 * Remove all {@link ItemInstance} debug items from the world and clear _debugItems {@link Map}.
	 */
	public void clearDebugItems()
	{
		for (ItemInstance item : _debugItems.values())
			item.decayMe();
		
		_debugItems.clear();
	}
	
	/**
	 * Send {@link L2GameServerPacket}s to all {@link Player}s in a specific {@link ZoneType}.
	 * @param <T> L2ZoneType.
	 * @param zoneType : The zone type to send packets.
	 * @param packets : The packets to send.
	 */
	public static <T extends ZoneType> void toAllPlayersInZoneType(Class<T> zoneType, L2GameServerPacket... packets)
	{
		for (ZoneType zone : ZoneManager.getInstance().getAllZones(zoneType))
		{
			for (Player player : zone.getKnownTypeInside(Player.class))
			{
				for (L2GameServerPacket packet : packets)
					player.sendPacket(packet);
			}
		}
	}
	
	public static final ZoneManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ZoneManager INSTANCE = new ZoneManager();
	}
}
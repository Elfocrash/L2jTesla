package dev.l2j.tesla.gameserver.data.manager;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.rift.DimensionalRift;
import dev.l2j.tesla.gameserver.model.rift.DimensionalRiftRoom;
import dev.l2j.tesla.gameserver.model.spawn.L2Spawn;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.commons.data.xml.IXmlReader;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.sql.SpawnTable;
import dev.l2j.tesla.gameserver.data.xml.NpcData;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

/**
 * Loads and stores available {@link DimensionalRiftRoom}s for the {@link DimensionalRift} system.
 */
public class DimensionalRiftManager implements IXmlReader
{
	private static final int DIMENSIONAL_FRAGMENT = 7079;
	
	private final Map<Byte, HashMap<Byte, DimensionalRiftRoom>> _rooms = new HashMap<>(7);
	
	protected DimensionalRiftManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/dimensionalRift.xml");
		LOGGER.info("Loaded Dimensional Rift rooms.");
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "area", areaNode ->
		{
			final NamedNodeMap areaAttrs = areaNode.getAttributes();
			
			final byte type = Byte.parseByte(areaAttrs.getNamedItem("type").getNodeValue());
			
			// Generate new layer of rooms if that type doesn't exist yet.
			if (!_rooms.containsKey(type))
				_rooms.put(type, new HashMap<Byte, DimensionalRiftRoom>(9));
			
			forEach(areaNode, "room", roomNode ->
			{
				// Generate the room using StatsSet content.
				final DimensionalRiftRoom riftRoom = new DimensionalRiftRoom(type, parseAttributes(roomNode));
				
				// Store it.
				_rooms.get(type).put(riftRoom.getId(), riftRoom);
				
				forEach(roomNode, "spawn", spawnNode ->
				{
					final NamedNodeMap spawnAttrs = spawnNode.getAttributes();
					
					int mobId = Integer.parseInt(spawnAttrs.getNamedItem("mobId").getNodeValue());
					int delay = Integer.parseInt(spawnAttrs.getNamedItem("delay").getNodeValue());
					int count = Integer.parseInt(spawnAttrs.getNamedItem("count").getNodeValue());
					
					final NpcTemplate template = NpcData.getInstance().getTemplate(mobId);
					if (template == null)
					{
						LOGGER.warn("Template " + mobId + " not found!");
						return;
					}
					
					try
					{
						for (int i = 0; i < count; i++)
						{
							final L2Spawn spawnDat = new L2Spawn(template);
							spawnDat.setLoc(riftRoom.getRandomX(), riftRoom.getRandomY(), DimensionalRiftRoom.Z_VALUE, -1);
							spawnDat.setRespawnDelay(delay);
							SpawnTable.getInstance().addSpawn(spawnDat, false);
							
							riftRoom.getSpawns().add(spawnDat);
						}
					}
					catch (Exception e)
					{
						LOGGER.error("Failed to initialize a spawn.", e);
					}
				});
			});
		}));
	}
	
	public void reload()
	{
		// For every room of every area, clean the spawns, area data and then _rooms data.
		for (Map<Byte, DimensionalRiftRoom> area : _rooms.values())
		{
			for (DimensionalRiftRoom room : area.values())
				room.getSpawns().clear();
			
			area.clear();
		}
		_rooms.clear();
		
		// Reload the static data.
		load();
	}
	
	public DimensionalRiftRoom getRoom(byte type, byte room)
	{
		final Map<Byte, DimensionalRiftRoom> area = _rooms.get(type);
		return (area == null) ? null : area.get(room);
	}
	
	public boolean checkIfInRiftZone(int x, int y, int z, boolean ignorePeaceZone)
	{
		final Map<Byte, DimensionalRiftRoom> area = _rooms.get((byte) 0);
		if (area == null)
			return false;
		
		if (ignorePeaceZone)
			return area.get((byte) 1).checkIfInZone(x, y, z);
		
		return area.get((byte) 1).checkIfInZone(x, y, z) && !area.get((byte) 0).checkIfInZone(x, y, z);
	}
	
	public boolean checkIfInPeaceZone(int x, int y, int z)
	{
		final DimensionalRiftRoom room = getRoom((byte) 0, (byte) 0);
		if (room == null)
			return false;
		
		return room.checkIfInZone(x, y, z);
	}
	
	/**
	 * Teleport the {@link Player} into the waiting room.
	 * @param player : The Player to teleport.
	 */
	public void teleportToWaitingRoom(Player player)
	{
		final DimensionalRiftRoom room = getRoom((byte) 0, (byte) 0);
		if (room == null)
			return;
		
		player.teleToLocation(room.getTeleportLoc());
	}
	
	/**
	 * Start the {@link DimensionalRift} process for a {@link Player} {@link Party}. Numerous checks are processed (party leader, already rifting, not enough mats, no free cells,...).
	 * @param player : The Player to test.
	 * @param type : The type of rift to start.
	 * @param npc : The given {@link Npc}.
	 */
	public synchronized void start(Player player, byte type, Npc npc)
	{
		final Party party = player.getParty();
		
		// No party.
		if (party == null)
		{
			showHtmlFile(player, "data/html/seven_signs/rift/NoParty.htm", npc);
			return;
		}
		
		// Player isn't the party leader.
		if (!party.isLeader(player))
		{
			showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		// Party is already in rift.
		if (party.isInDimensionalRift())
			return;
		
		// Party members' count is lower than config.
		if (party.getMembersCount() < Config.RIFT_MIN_PARTY_SIZE)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/html/seven_signs/rift/SmallParty.htm");
			html.replace("%npc_name%", npc.getName());
			html.replace("%count%", Integer.toString(Config.RIFT_MIN_PARTY_SIZE));
			player.sendPacket(html);
			return;
		}
		
		// Rift is full.
		final List<DimensionalRiftRoom> availableRooms = getFreeRooms(type, false);
		if (availableRooms.isEmpty())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/html/seven_signs/rift/Full.htm");
			html.replace("%npc_name%", npc.getName());
			player.sendPacket(html);
			return;
		}
		
		// One of teammates isn't on peace zone or hasn't required amount of items.
		for (Player member : party.getMembers())
		{
			if (!checkIfInPeaceZone(member.getX(), member.getY(), member.getZ()))
			{
				showHtmlFile(player, "data/html/seven_signs/rift/NotInWaitingRoom.htm", npc);
				return;
			}
		}
		
		final int count = getNeededItems(type);
		
		// Check if every party member got a Dimensional Fragment.
		for (Player member : party.getMembers())
		{
			final ItemInstance item = member.getInventory().getItemByItemId(DIMENSIONAL_FRAGMENT);
			if (item == null || item.getCount() < getNeededItems(type))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile("data/html/seven_signs/rift/NoFragments.htm");
				html.replace("%npc_name%", npc.getName());
				html.replace("%count%", Integer.toString(count));
				player.sendPacket(html);
				return;
			}
		}
		
		// Delete the Dimensional Fragment for every member.
		for (Player member : party.getMembers())
			member.destroyItemByItemId("RiftEntrance", DIMENSIONAL_FRAGMENT, count, null, true);
		
		// Creates an instance of the rift.
		new DimensionalRift(party, Rnd.get(availableRooms));
	}
	
	/**
	 * @param type : The type of rift to check.
	 * @return the amount of needed Dimensional Fragments for the given type of rift.
	 */
	private static int getNeededItems(byte type)
	{
		switch (type)
		{
			case 1:
				return Config.RIFT_ENTER_COST_RECRUIT;
			case 2:
				return Config.RIFT_ENTER_COST_SOLDIER;
			case 3:
				return Config.RIFT_ENTER_COST_OFFICER;
			case 4:
				return Config.RIFT_ENTER_COST_CAPTAIN;
			case 5:
				return Config.RIFT_ENTER_COST_COMMANDER;
			case 6:
				return Config.RIFT_ENTER_COST_HERO;
			default:
				throw new IndexOutOfBoundsException();
		}
	}
	
	public void showHtmlFile(Player player, String file, Npc npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(file);
		html.replace("%npc_name%", npc.getName());
		player.sendPacket(html);
	}
	
	/**
	 * @param type : The type of area to test.
	 * @param canUseBossRoom : If true, the boss zone is considered as valid zone.
	 * @return a {@link List} of all available {@link DimensionalRiftRoom}s. A free room is considered without party inside. Boss room is a valid room according the boolean flag.
	 */
	public List<DimensionalRiftRoom> getFreeRooms(byte type, boolean canUseBossRoom)
	{
		return _rooms.get(type).values().stream().filter(r -> !r.isPartyInside() && (canUseBossRoom || !r.isBossRoom())).collect(Collectors.toList());
	}
	
	public void onPartyEdit(Party party)
	{
		if (party == null)
			return;
		
		final DimensionalRift rift = party.getDimensionalRift();
		if (rift != null)
		{
			for (Player member : party.getMembers())
				teleportToWaitingRoom(member);
			
			rift.killRift();
		}
	}
	
	public static DimensionalRiftManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DimensionalRiftManager INSTANCE = new DimensionalRiftManager();
	}
}
package dev.l2j.tesla.gameserver.model.zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket;
import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.gameserver.enums.ScriptEventType;

/**
 * An abstract base class for any zone type, which holds {@link Creature}s affected by this zone, linked {@link Quest}s and the associated {@link ZoneForm}.<br>
 * <br>
 * Zones can be retrieved by id, but since most use dynamic IDs, you must set individual zone id yourself if you want the system works correctly (otherwise id can be different if you add or remove zone types or zones).
 */
public abstract class ZoneType
{
	protected static final CLogger LOGGER = new CLogger(ZoneType.class.getName());
	
	private final int _id;
	protected final Map<Integer, Creature> _characters = new ConcurrentHashMap<>();
	
	private Map<ScriptEventType, List<Quest>> _questEvents;
	private ZoneForm _zone;
	
	protected ZoneType(int id)
	{
		_id = id;
	}
	
	protected abstract void onEnter(Creature character);
	
	protected abstract void onExit(Creature character);
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + _id + "]";
	}
	
	public int getId()
	{
		return _id;
	}
	
	public ZoneForm getZone()
	{
		return _zone;
	}
	
	public void setZone(ZoneForm zone)
	{
		if (_zone != null)
			throw new IllegalStateException("Zone already set");
		
		_zone = zone;
	}
	
	/**
	 * @param x : The X position to test.
	 * @param y : The Y position to test.
	 * @return true if the given coordinates are within zone's plane. We use getHighZ() as Z reference.
	 */
	public boolean isInsideZone(int x, int y)
	{
		return _zone.isInsideZone(x, y, _zone.getHighZ());
	}
	
	/**
	 * @param x : The X position to test.
	 * @param y : The Y position to test.
	 * @param z : The Z position to test.
	 * @return true if the given coordinates are within the zone.
	 */
	public boolean isInsideZone(int x, int y, int z)
	{
		return _zone.isInsideZone(x, y, z);
	}
	
	/**
	 * @param object : Use object's X/Y positions.
	 * @return true if the {@link WorldObject} is inside the zone.
	 */
	public boolean isInsideZone(WorldObject object)
	{
		return isInsideZone(object.getX(), object.getY(), object.getZ());
	}
	
	public double getDistanceToZone(int x, int y)
	{
		return _zone.getDistanceToZone(x, y);
	}
	
	public double getDistanceToZone(WorldObject object)
	{
		return _zone.getDistanceToZone(object.getX(), object.getY());
	}
	
	public void visualizeZone(int z)
	{
		_zone.visualizeZone(_id, z);
	}
	
	/**
	 * Update a {@link Creature} zone state.<br>
	 * <br>
	 * If the Creature is inside the zone, but not yet part of _characters {@link Map} :
	 * <ul>
	 * <li>Fire {@link Quest#notifyEnterZone}.</li>
	 * <li>Add the Creature to the Map.</li>
	 * <li>Fire zone onEnter() event.</li>
	 * </ul>
	 * If the Creature isn't inside the zone, and was part of _characters Map, we run {@link #removeCharacter(Creature)}.
	 * @param character : The affected Creature.
	 */
	public void revalidateInZone(Creature character)
	{
		// If the character can't be affected by this zone, return.
		if (!isAffected(character))
			return;
		
		// If the character is inside the zone.
		if (isInsideZone(character))
		{
			// We test if the character was part of the zone.
			if (!_characters.containsKey(character.getObjectId()))
			{
				// Notify to scripts.
				final List<Quest> quests = getQuestByEvent(ScriptEventType.ON_ENTER_ZONE);
				if (quests != null)
				{
					for (Quest quest : quests)
						quest.notifyEnterZone(character, this);
				}
				
				// Register player.
				_characters.put(character.getObjectId(), character);
				
				// Notify Zone implementation.
				onEnter(character);
			}
		}
		else
			removeCharacter(character);
	}
	
	/**
	 * Remove a {@link Creature} from this zone.
	 * <ul>
	 * <li>Fire {@link Quest#notifyExitZone}.</li>
	 * <li>Remove the Creature from the {@link Map}.</li>
	 * <li>Fire zone onExit() event.</li>
	 * </ul>
	 * @param character : The Creature to remove.
	 */
	public void removeCharacter(Creature character)
	{
		// We test and remove the character if he was part of the zone.
		if (_characters.remove(character.getObjectId()) != null)
		{
			// Notify to scripts.
			final List<Quest> quests = getQuestByEvent(ScriptEventType.ON_EXIT_ZONE);
			if (quests != null)
			{
				for (Quest quest : quests)
					quest.notifyExitZone(character, this);
			}
			
			// Notify Zone implementation.
			onExit(character);
		}
	}
	
	/**
	 * @param character : The Creature to test.
	 * @return true if the {@link Creature} is in the zone _characters {@link Map}.
	 */
	public boolean isCharacterInZone(Creature character)
	{
		return _characters.containsKey(character.getObjectId());
	}
	
	public Collection<Creature> getCharacters()
	{
		return _characters.values();
	}
	
	/**
	 * @param <A> : The generic type.
	 * @param type : The instance type to filter.
	 * @return a {@link List} of filtered type {@link Creature}s within this zone. Generate a temporary List.
	 */
	@SuppressWarnings("unchecked")
	public final <A> List<A> getKnownTypeInside(Class<A> type)
	{
		if (_characters.isEmpty())
			return Collections.emptyList();
		
		List<A> result = new ArrayList<>();
		
		for (WorldObject obj : _characters.values())
		{
			if (type.isAssignableFrom(obj.getClass()))
				result.add((A) obj);
		}
		return result;
	}
	
	/**
	 * Add a {@link Quest} on _questEvents {@link Map}. Generate both Map and {@link List} if not existing (lazy initialization).<br>
	 * <br>
	 * If already existing, we remove and add it back.
	 * @param type : The EventType to test.
	 * @param quest : The Quest to add.
	 */
	public void addQuestEvent(ScriptEventType type, Quest quest)
	{
		if (_questEvents == null)
			_questEvents = new HashMap<>();
		
		List<Quest> eventList = _questEvents.get(type);
		if (eventList == null)
		{
			eventList = new ArrayList<>();
			eventList.add(quest);
			
			_questEvents.put(type, eventList);
		}
		else
		{
			eventList.remove(quest);
			eventList.add(quest);
		}
	}
	
	/**
	 * @param type : The EventType to test.
	 * @return the {@link List} of available {@link Quest}s associated to this zone for a given {@link ScriptEventType}.
	 */
	public List<Quest> getQuestByEvent(ScriptEventType type)
	{
		return (_questEvents == null) ? null : _questEvents.get(type);
	}
	
	/**
	 * Broadcast a {@link L2GameServerPacket} to all {@link Player}s inside the zone.
	 * @param packet : The packet to use.
	 */
	public void broadcastPacket(L2GameServerPacket packet)
	{
		for (Creature character : _characters.values())
		{
			if (character instanceof Player)
				character.sendPacket(packet);
		}
	}
	
	/**
	 * Setup new parameters for this zone. By default, we return a warning (which mean this parameter isn't used on child zone).
	 * @param name : The parameter name.
	 * @param value : The parameter value.
	 */
	public void setParameter(String name, String value)
	{
		LOGGER.warn("Unknown name/values couple {}, {} for {}.", name, value, toString());
	}
	
	/**
	 * @param character : The Creature to test.
	 * @return true if the given {@link Creature} is affected by this zone. Overriden in children classes.
	 */
	protected boolean isAffected(Creature character)
	{
		return true;
	}
	
	/**
	 * Teleport all {@link Player}s located in this {@link ZoneType} to specific coords x/y/z.
	 * @param x : The X parameter used as teleport location.
	 * @param y : The Y parameter used as teleport location.
	 * @param z : The Z parameter used as teleport location.
	 */
	public void movePlayersTo(int x, int y, int z)
	{
		for (Player player : getKnownTypeInside(Player.class))
		{
			if (player.isOnline())
				player.teleportTo(x, y, z, 0);
		}
	}
	
	/**
	 * Teleport all {@link Player}s located in this {@link ZoneType} to a specific {@link Location}.
	 * @see #movePlayersTo(int, int, int)
	 * @param loc : The Location used as coords.
	 */
	public void movePlayersTo(Location loc)
	{
		movePlayersTo(loc.getX(), loc.getY(), loc.getZ());
	}
	
	/**
	 * Add a {@link WorldObject} to knownlist.
	 * @param object : An object to be added.
	 */
	public void addKnownObject(WorldObject object)
	{
	}
	
	/**
	 * Remove a {@link WorldObject} from knownlist.
	 * @param object : An object to be removed.
	 */
	public void removeKnownObject(WorldObject object)
	{
	}
}
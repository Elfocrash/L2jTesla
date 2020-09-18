package dev.l2j.tesla.gameserver.model.actor.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.gameserver.data.xml.HennaData;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.enums.actors.HennaType;
import dev.l2j.tesla.gameserver.model.item.Henna;

/**
 * This class handles dyes (or {@link Henna}s) of a {@link Player}.
 */
public class HennaList
{
	private static final CLogger LOGGER = new CLogger(HennaList.class.getName());
	
	private static final int MAX_HENNA_STAT_VALUE = 5;
	private static final int HENNA_FIRST_SLOT_ID = 1;
	
	public static final int MAX_HENNAS_AMOUNT = 3;
	
	private final Player _owner;
	private final Henna[] _hennas = new Henna[MAX_HENNAS_AMOUNT];
	private final int[] _stats = new int[HennaType.values().length];
	
	public HennaList(Player owner)
	{
		_owner = owner;
	}
	
	/**
	 * Recalculate stats of existing {@link Henna}s.<br>
	 * <br>
	 * A stat can't be superior to 5. Previous stats are dumped.
	 */
	private void recalculateStats()
	{
		for (int i = 0; i < _stats.length; i++)
			_stats[i] = 0;
		
		for (Henna henna : _hennas)
		{
			if (henna != null)
			{
				_stats[0] += henna.getINT();
				_stats[1] += henna.getSTR();
				_stats[2] += henna.getCON();
				_stats[3] += henna.getMEN();
				_stats[4] += henna.getDEX();
				_stats[5] += henna.getWIT();
			}
		}
		
		for (int i = 0; i < _stats.length; i++)
			_stats[i] = Math.min(_stats[i], MAX_HENNA_STAT_VALUE);
	}
	
	/**
	 * @param henna : The Henna to test.
	 * @return the index of an existing {@link Henna}, or -1 if not found.
	 */
	private int indexOf(Henna henna)
	{
		if (henna == null)
			return -1;
		
		for (int i = 0; i < _hennas.length; i++)
		{
			final Henna h = _hennas[i];
			if (h != null && h.getSymbolId() == henna.getSymbolId())
				return i;
		}
		return -1;
	}
	
	/**
	 * @return the first empty slot on this {@link HennaList}.
	 */
	private int getEmptySlotIndex()
	{
		for (int i = 0; i < getMaxSize(); i++)
		{
			if (_hennas[i] == null)
				return i;
		}
		return -1;
	}

	public boolean addDontStore(Henna henna) {
		final int slot = getEmptySlotIndex();
		if (slot < 0)
			return false;

		_hennas[slot] = henna;
		recalculateStats();
		return true;
	}
	
	/**
	 * @return the {@link List} of available {@link Henna}s.
	 */
	public List<Henna> getHennas()
	{
		return Arrays.stream(_hennas).filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	/**
	 * @param HennaType : The HennaType we use as reference.
	 * @return the stat value based on an {@link HennaType}, which is between +5 and -15.
	 */
	public int getStat(HennaType HennaType)
	{
		return _stats[HennaType.ordinal()];
	}
	
	/**
	 * Restore the {@link Henna}s associated to this {@link HennaList}, based on {@link Player} class index.
	 */
	public void restore()
	{
		final Henna[] hennas = new Henna[HennaList.MAX_HENNAS_AMOUNT];
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT slot, symbol_id FROM character_hennas WHERE char_obj_id = ? AND class_index = ?"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, _owner.getClassIndex());
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final int slot = rs.getInt("slot");
					final int symbolId = rs.getInt("symbol_id");
					
					if (slot < HENNA_FIRST_SLOT_ID || slot > HENNA_FIRST_SLOT_ID + HennaList.MAX_HENNAS_AMOUNT)
					{
						LOGGER.warn("{} has Henna on invalid slot {}.", _owner.toString(), slot);
						continue;
					}
					
					final Henna henna = HennaData.getInstance().getHenna(symbolId);
					if (henna == null)
					{
						LOGGER.warn("{} has unknown Henna Symbol Id: {} in slot {}.", _owner.toString(), symbolId, slot);
						continue;
					}
					
					hennas[slot - HENNA_FIRST_SLOT_ID] = henna;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't restore hennas.", e);
		}
		
		for (int i = 0; i < hennas.length; i++)
			_hennas[i] = hennas[i];
		
		recalculateStats();
	}
	
	/**
	 * @return true if all slots of this {@link HennaList} are empty, false otherwise.
	 */
	public boolean isEmpty()
	{
		for (Henna h : _hennas)
		{
			if (h != null)
				return false;
		}
		return true;
	}
	
	/**
	 * @return the current amount of equipped {@link Henna}s on this {@link HennaList}.
	 */
	public int size()
	{
		int result = 0;
		for (int i = 0; i < _hennas.length; i++)
		{
			if (_hennas[i] != null)
				result++;
		}
		return result;
	}
	
	/**
	 * @param symbolId : The symbol id used as reference.
	 * @return the {@link Henna} associated to a symbol id, or null if not found.
	 */
	public Henna getBySymbolId(int symbolId)
	{
		for (Henna h : _hennas)
		{
			if (h != null && h.getSymbolId() == symbolId)
				return h;
		}
		return null;
	}
	
	/**
	 * @return true if the {@link HennaList} is entirely filled, based on maximum amount of {@link Henna}s this {@link Player} can equip.
	 */
	public boolean isFull()
	{
		return getEmptySlotsAmount() <= 0;
	}
	
	/**
	 * @return the number of empty slots of this {@link HennaList}, based on maximum amount of {@link Henna}s this {@link Player} can equip.
	 */
	public int getEmptySlotsAmount()
	{
		int usedSlots = size();
		int maxSlots = getMaxSize();
		
		return Math.max(maxSlots - usedSlots, 0);
	}
	
	/**
	 * @return the maximum amount of {@link Henna}s this {@link Player} can equip.
	 */
	public int getMaxSize()
	{
		final ClassId classId = _owner.getClassId();
		if (classId.level() < 1)
			return 0;
		
		if (classId.level() == 1)
			return 2;
		
		return MAX_HENNAS_AMOUNT;
	}
	
	/**
	 * @param henna : The Henna to add.
	 * @return true if the {@link Henna} could be added, false otherwise.
	 */
	public boolean add(Henna henna)
	{
		final int slot = getEmptySlotIndex();
		if (slot < 0)
			return false;
		
		_hennas[slot] = henna;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, henna.getSymbolId());
			ps.setInt(3, slot + HENNA_FIRST_SLOT_ID);
			ps.setInt(4, _owner.getClassIndex());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't add Henna.", e);
		}
		
		recalculateStats();
		return true;
	}
	
	/**
	 * @param henna : The Henna to remove.
	 * @return true if the {@link Henna} could be removed, false otherwise.
	 */
	public boolean remove(Henna henna)
	{
		final int slot = indexOf(henna);
		if (slot < 0)
			return false;
		
		_hennas[slot] = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, slot + HENNA_FIRST_SLOT_ID);
			ps.setInt(3, _owner.getClassIndex());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't remove Henna.", e);
		}
		
		recalculateStats();
		
		return true;
	}
	
	/**
	 * @param henna : The Henna used as reference.
	 * @return true if the tested {@link Henna} can be used by the {@link Player}, false otherwise.
	 */
	public boolean canBeUsedBy(Henna henna)
	{
		return henna.canBeUsedBy(_owner);
	}
	
	@Override
	public String toString()
	{
		return "HennaList{" + "_owner=" + _owner.toString() + ", _hennas=" + Arrays.toString(_hennas) + ", _stats=" + Arrays.toString(_stats) + '}';
	}
}
package dev.l2j.tesla.gameserver.model.actor.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.Macro;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.SendMacroList;
import dev.l2j.tesla.commons.lang.StringUtil;
import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.gameserver.enums.ShortcutType;

/**
 * An ordered container holding {@link Macro}s of a {@link Player}.
 */
public class MacroList
{
	private static final CLogger LOGGER = new CLogger(MacroList.class.getName());
	
	private static final String INSERT_MACRO = "REPLACE INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)";
	private static final String DELETE_MACRO = "DELETE FROM character_macroses WHERE char_obj_id=? AND id=?";
	private static final String LOAD_MACROS = "SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?";
	
	private final Map<Integer, Macro> _macros = new LinkedHashMap<>();
	private final Player _owner;
	
	private int _revision;
	private int _macroId;
	
	public MacroList(Player owner)
	{
		_owner = owner;
		_revision = 1;
		_macroId = 1000;
	}
	
	public int getRevision()
	{
		return _revision;
	}
	
	public Macro[] getMacros()
	{
		return _macros.values().toArray(new Macro[_macros.size()]);
	}
	
	public Macro getMacro(int id)
	{
		return _macros.get(id);
	}
	
	/**
	 * Add a {@link Macro} to this {@link MacroList}.
	 * @param macro : The Macro object to add.
	 */
	public void registerMacro(Macro macro)
	{
		if (macro.id == 0)
		{
			macro.id = _macroId++;
			
			while (_macros.get(macro.id) != null)
				macro.id = _macroId++;
			
			_macros.put(macro.id, macro);
		}
		else
		{
			final Macro old = _macros.put(macro.id, macro);
			if (old != null)
				deleteMacroFromDb(old);
		}
		registerMacroInDb(macro);
		sendUpdate();
	}
	
	/**
	 * Delete the {@link Macro} corresponding to the id from this {@link MacroList}.
	 * @param id : The id of the Macro to delete.
	 */
	public void deleteMacro(int id)
	{
		final Macro toRemove = _macros.get(id);
		if (toRemove != null)
			deleteMacroFromDb(toRemove);
		
		_macros.remove(id);
		
		// Delete all existing shortcuts refering to this macro id.
		_owner.getShortcutList().deleteShortcuts(id, ShortcutType.MACRO);
		
		sendUpdate();
	}
	
	/**
	 * Refresh {@link Macro}s list. Used on onEnterWorld.
	 */
	public void sendUpdate()
	{
		_revision++;
		
		final Macro[] macros = getMacros();
		if (macros.length == 0)
			_owner.sendPacket(new SendMacroList(_revision, macros.length, null));
		else
		{
			for (Macro macro : macros)
				_owner.sendPacket(new SendMacroList(_revision, macros.length, macro));
		}
	}
	
	/**
	 * Save the given {@link Macro} to the database.
	 * @param macro : The Macro to save.
	 */
	private void registerMacroInDb(Macro macro)
	{
		final StringBuilder sb = new StringBuilder(300);
		for (Macro.MacroCmd cmd : macro.commands)
		{
			StringUtil.append(sb, cmd.type, ",", cmd.d1, ",", cmd.d2);
			if (cmd.cmd != null && cmd.cmd.length() > 0)
				StringUtil.append(sb, ",", cmd.cmd);
			
			sb.append(';');
		}
		
		if (sb.length() > 255)
			sb.setLength(255);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_MACRO))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, macro.id);
			ps.setInt(3, macro.icon);
			ps.setString(4, macro.name);
			ps.setString(5, macro.descr);
			ps.setString(6, macro.acronym);
			ps.setString(7, sb.toString());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't store macro.", e);
		}
	}
	
	/**
	 * Delete the given {@link Macro} to the database.
	 * @param macro : The Macro to delete.
	 */
	private void deleteMacroFromDb(Macro macro)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_MACRO))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, macro.id);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't delete macro.", e);
		}
	}
	
	/**
	 * Restore {@link Macro}s associated to the {@link Player} owner.
	 */
	public void restore()
	{
		_macros.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_MACROS))
		{
			ps.setInt(1, _owner.getObjectId());
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final int id = rs.getInt("id");
					final int icon = rs.getInt("icon");
					final String name = rs.getString("name");
					final String descr = rs.getString("descr");
					final String acronym = rs.getString("acronym");
					
					final List<Macro.MacroCmd> commands = new ArrayList<>();
					final StringTokenizer st1 = new StringTokenizer(rs.getString("commands"), ";");
					
					while (st1.hasMoreTokens())
					{
						final StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
						if (st.countTokens() < 3)
							continue;
						
						final int type = Integer.parseInt(st.nextToken());
						final int d1 = Integer.parseInt(st.nextToken());
						final int d2 = Integer.parseInt(st.nextToken());
						
						String cmd = "";
						if (st.hasMoreTokens())
							cmd = st.nextToken();
						
						final Macro.MacroCmd mcmd = new Macro.MacroCmd(commands.size(), type, d1, d2, cmd);
						commands.add(mcmd);
					}
					
					final Macro macro = new Macro(id, icon, name, descr, acronym, commands.toArray(new Macro.MacroCmd[commands.size()]));
					_macros.put(macro.id, macro);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load macros.", e);
		}
	}
}
package dev.l2j.tesla.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.model.memo.AbstractMemo;
import dev.l2j.tesla.commons.logging.CLogger;

/**
 * A global, server-size, container for variables of any type, which can be then saved/restored upon server restart. It extends {@link AbstractMemo}.
 */
@SuppressWarnings("serial")
public class ServerMemoTable extends AbstractMemo
{
	private static final CLogger LOGGER = new CLogger(ServerMemoTable.class.getName());
	
	private static final String SELECT_QUERY = "SELECT * FROM server_memo";
	private static final String DELETE_QUERY = "DELETE FROM server_memo";
	private static final String INSERT_QUERY = "INSERT INTO server_memo (var, value) VALUES (?, ?)";
	
	protected ServerMemoTable()
	{
		restoreMe();
	}
	
	@Override
	public boolean restoreMe()
	{
		// Restore previous variables.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_QUERY);
             ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
				set(rs.getString("var"), rs.getString("value"));
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't restore server variables.", e);
			return false;
		}
		finally
		{
			compareAndSetChanges(true, false);
		}
		LOGGER.info("Loaded {} server variables.", size());
		return true;
	}
	
	@Override
	public boolean storeMe()
	{
		// No changes, nothing to store.
		if (!hasChanges())
			return false;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Clear previous entries.
			try (PreparedStatement ps = con.prepareStatement(DELETE_QUERY))
			{
				ps.executeUpdate();
			}
			
			// Insert all variables.
			try (PreparedStatement ps = con.prepareStatement(INSERT_QUERY))
			{
				for (Entry<String, Object> entry : entrySet())
				{
					ps.setString(1, entry.getKey());
					ps.setString(2, String.valueOf(entry.getValue()));
					ps.addBatch();
				}
				ps.executeBatch();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't save server variables to database.", e);
			return false;
		}
		finally
		{
			compareAndSetChanges(true, false);
		}
		LOGGER.info("Stored {} server variables.", size());
		return true;
	}
	
	public static final ServerMemoTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ServerMemoTable INSTANCE = new ServerMemoTable();
	}
}
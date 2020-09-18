package dev.l2j.tesla;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class L2DatabaseFactory
{
	protected static Logger _log = Logger.getLogger(L2DatabaseFactory.class.getName());

	private HikariDataSource _source;

	public static L2DatabaseFactory getInstance()
	{
		return SingletonHolder._instance;
	}

	public L2DatabaseFactory() throws SQLException
	{
		try
		{
			var hikariConfig = new HikariConfig();
			hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
			hikariConfig.setJdbcUrl(Config.DATABASE_URL);
			hikariConfig.setUsername(Config.DATABASE_LOGIN);
			hikariConfig.setPassword(Config.DATABASE_PASSWORD);

			hikariConfig.setMaximumPoolSize(Math.max(10, Config.DATABASE_MAX_CONNECTIONS));
			hikariConfig.setConnectionTestQuery("SELECT 1");
			hikariConfig.setPoolName("L2HikariCP");

			hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
			hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
			hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
			hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
			hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
			hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
			hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
			hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
			hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
			hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

			_source = new HikariDataSource(hikariConfig);

			/* Test the connection */
			_source.getConnection().close();
		}
		catch (SQLException x)
		{
			throw x;
		}
		catch (Exception e)
		{
			throw new SQLException("could not init DB connection:" + e);
		}
	}

	public void shutdown()
	{
		try
		{
			_source.close();
		}
		catch (Exception e)
		{
			_log.log(Level.INFO, "", e);
		}

		try
		{
			_source = null;
		}
		catch (Exception e)
		{
			_log.log(Level.INFO, "", e);
		}
	}

	/**
	 * Use brace as a safty precaution in case name is a reserved word.
	 * @param whatToCheck the list of arguments.
	 * @return the list of arguments between brackets.
	 */
	public static final String safetyString(String... whatToCheck)
	{
		final var sb = new StringBuilder();
		for (var word : whatToCheck)
		{
			if (sb.length() > 0)
				sb.append(", ");

			sb.append('`');
			sb.append(word);
			sb.append('`');
		}
		return sb.toString();
	}

	public Connection getConnection()
	{
		Connection con = null;

		while (con == null)
		{
			try
			{
				con = _source.getConnection();
			}
			catch (SQLException e)
			{
				_log.warning("L2DatabaseFactory: getConnection() failed, trying again " + e);
			}
		}
		return con;
	}

	private static class SingletonHolder
	{
		protected static final L2DatabaseFactory _instance;

		static
		{
			try
			{
				_instance = new L2DatabaseFactory();
			}
			catch (Exception e)
			{
				throw new ExceptionInInitializerError(e);
			}
		}
	}
}
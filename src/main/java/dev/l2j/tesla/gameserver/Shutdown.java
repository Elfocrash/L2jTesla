package dev.l2j.tesla.gameserver;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.lang.StringUtil;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.network.StatusType;

import dev.l2j.tesla.gameserver.data.manager.BufferManager;
import dev.l2j.tesla.gameserver.data.manager.CastleManorManager;
import dev.l2j.tesla.gameserver.data.manager.CoupleManager;
import dev.l2j.tesla.gameserver.data.manager.FestivalOfDarknessManager;
import dev.l2j.tesla.gameserver.data.manager.FishingChampionshipManager;
import dev.l2j.tesla.gameserver.data.manager.GrandBossManager;
import dev.l2j.tesla.gameserver.data.manager.HeroManager;
import dev.l2j.tesla.gameserver.data.manager.RaidBossManager;
import dev.l2j.tesla.gameserver.data.manager.SevenSignsManager;
import dev.l2j.tesla.gameserver.data.manager.ZoneManager;
import dev.l2j.tesla.gameserver.data.sql.ServerMemoTable;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.olympiad.Olympiad;
import dev.l2j.tesla.gameserver.network.GameClient;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.gameserverpackets.ServerStatus;
import dev.l2j.tesla.gameserver.network.serverpackets.ServerClose;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.taskmanager.ItemsOnGroundTaskManager;

/**
 * This class provides functions for shutting down and restarting the server. It closes all client connections and saves data.
 */
public class Shutdown extends Thread
{
	private static final CLogger LOGGER = new CLogger(Shutdown.class.getName());
	
	private static Shutdown _counterInstance = null;
	
	private int _secondsShut;
	private int _shutdownMode;
	
	public static final int SIGTERM = 0;
	public static final int GM_SHUTDOWN = 1;
	public static final int GM_RESTART = 2;
	public static final int ABORT = 3;
	private static final String[] MODE_TEXT =
	{
		"SIGTERM",
		"shutting down",
		"restarting",
		"aborting"
	};
	
	protected Shutdown()
	{
		_secondsShut = -1;
		_shutdownMode = SIGTERM;
	}
	
	public Shutdown(int seconds, boolean restart)
	{
		_secondsShut = Math.max(0, seconds);
		_shutdownMode = (restart) ? GM_RESTART : GM_SHUTDOWN;
	}
	
	/**
	 * This function is called, when a new thread starts if this thread is the thread of getInstance, then this is the shutdown hook and we save all data and disconnect all clients.<br>
	 * <br>
	 * After this thread ends, the server will completely exit if this is not the thread of getInstance, then this is a countdown thread.<br>
	 * <br>
	 * We start the countdown, and when we finished it, and it was not aborted, we tell the shutdown-hook why we call exit, and then call exit when the exit status of the server is 1, startServer.sh / startServer.bat will restart the server.
	 */
	@Override
	public void run()
	{
		if (this == SingletonHolder.INSTANCE)
		{
			StringUtil.printSection("Under " + MODE_TEXT[_shutdownMode] + " process");
			
			// disconnect players
			try
			{
				disconnectAllPlayers();
				LOGGER.info("All players have been disconnected.");
			}
			catch (Throwable t)
			{
			}
			
			// stop all threadpolls
			ThreadPool.shutdown();
			
			try
			{
				LoginServerThread.getInstance().interrupt();
			}
			catch (Throwable t)
			{
			}
			
			// Seven Signs data is now saved along with Festival data.
			if (!SevenSignsManager.getInstance().isSealValidationPeriod())
				FestivalOfDarknessManager.getInstance().saveFestivalData(false);
			
			// Save Seven Signs data && status.
			SevenSignsManager.getInstance().saveSevenSignsData();
			SevenSignsManager.getInstance().saveSevenSignsStatus();
			LOGGER.info("Seven Signs Festival, general data && status have been saved.");
			
			// Save zones (grandbosses status)
			ZoneManager.getInstance().save();
			
			// Save raidbosses status
			RaidBossManager.getInstance().cleanUp(true);
			LOGGER.info("Raid Bosses data has been saved.");
			
			// Save grandbosses status
			GrandBossManager.getInstance().cleanUp();
			LOGGER.info("World Bosses data has been saved.");
			
			// Save olympiads
			Olympiad.getInstance().saveOlympiadStatus();
			LOGGER.info("Olympiad data has been saved.");
			
			// Save Hero data
			HeroManager.getInstance().shutdown();
			LOGGER.info("Hero data has been saved.");
			
			// Save all manor data
			CastleManorManager.getInstance().storeMe();
			LOGGER.info("Manors data has been saved.");
			
			// Save Fishing tournament data
			FishingChampionshipManager.getInstance().shutdown();
			LOGGER.info("Fishing Championship data has been saved.");
			
			// Schemes save.
			BufferManager.getInstance().saveSchemes();
			LOGGER.info("BufferTable data has been saved.");
			
			// Couples save.
			if (Config.ALLOW_WEDDING)
			{
				CoupleManager.getInstance().save();
				LOGGER.info("CoupleManager data has been saved.");
			}
			
			// Save server memos.
			ServerMemoTable.getInstance().storeMe();
			LOGGER.info("ServerMemo data has been saved.");
			
			// Save items on ground before closing
			ItemsOnGroundTaskManager.getInstance().save();
			
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
			}
			
			try
			{
				GameServer.getInstance().getSelectorThread().shutdown();
			}
			catch (Throwable t)
			{
			}
			
			try
			{
				L2DatabaseFactory.getInstance().shutdown();
			}
			catch (Throwable t)
			{
			}
			
			Runtime.getRuntime().halt((SingletonHolder.INSTANCE._shutdownMode == GM_RESTART) ? 2 : 0);
		}
		else
		{
			countdown();
			
			switch (_shutdownMode)
			{
				case GM_SHUTDOWN:
					SingletonHolder.INSTANCE.setMode(GM_SHUTDOWN);
					SingletonHolder.INSTANCE.run();
					System.exit(0);
					break;
				
				case GM_RESTART:
					SingletonHolder.INSTANCE.setMode(GM_RESTART);
					SingletonHolder.INSTANCE.run();
					System.exit(2);
					break;
			}
		}
	}
	
	/**
	 * This functions starts a shutdown countdown.
	 * @param player : The {@link Player} who issued the shutdown command.
	 * @param ghostEntity : The entity who issued the shutdown command.
	 * @param seconds : The number of seconds until shutdown.
	 * @param restart : If true, the server will restart after shutdown.
	 */
	public void startShutdown(Player player, String ghostEntity, int seconds, boolean restart)
	{
		_shutdownMode = (restart) ? GM_RESTART : GM_SHUTDOWN;
		
		if (player != null)
			LOGGER.info("GM: {} issued {} process in {} seconds.", player.toString(), MODE_TEXT[_shutdownMode], seconds);
		else if (!ghostEntity.isEmpty())
			LOGGER.info("Entity: {} issued {} process in {} seconds.", ghostEntity, MODE_TEXT[_shutdownMode], seconds);
		
		if (_shutdownMode > 0)
		{
			switch (seconds)
			{
				case 540:
				case 480:
				case 420:
				case 360:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
				case 30:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					break;
				default:
					sendServerQuit(seconds);
			}
		}
		
		if (_counterInstance != null)
			_counterInstance.setMode(ABORT);
		
		// the main instance should only run for shutdown hook, so we start a new instance
		_counterInstance = new Shutdown(seconds, restart);
		_counterInstance.start();
	}
	
	/**
	 * This function aborts a running countdown.
	 * @param player : The {@link Player} who issued the abort process.
	 */
	public void abort(Player player)
	{
		if (_counterInstance != null)
		{
			LOGGER.info("GM: {} aborted {} process.", player.toString(), MODE_TEXT[_shutdownMode]);
			_counterInstance.setMode(ABORT);
			
			World.announceToOnlinePlayers("Server aborted " + MODE_TEXT[_shutdownMode] + " process and continues normal operation.");
		}
	}
	
	/**
	 * Set the shutdown mode.
	 * @param mode : what mode shall be set.
	 */
	private void setMode(int mode)
	{
		_shutdownMode = mode;
	}
	
	/**
	 * Report the current countdown to all players. Flag the server as "down" when reaching 60sec. Rehabilitate the server status if ABORT {@link ServerStatus} is seen.
	 */
	private void countdown()
	{
		try
		{
			while (_secondsShut > 0)
			{
				// Rehabilitate previous server status if shutdown is aborted.
				if (_shutdownMode == ABORT)
				{
					if (LoginServerThread.getInstance().getServerStatus() == StatusType.DOWN)
						LoginServerThread.getInstance().setServerStatus((Config.SERVER_GMONLY) ? StatusType.GM_ONLY : StatusType.AUTO);
					
					break;
				}
				
				switch (_secondsShut)
				{
					case 540:
					case 480:
					case 420:
					case 360:
					case 300:
					case 240:
					case 180:
					case 120:
					case 60:
					case 30:
					case 10:
					case 5:
					case 4:
					case 3:
					case 2:
					case 1:
						sendServerQuit(_secondsShut);
						break;
				}
				
				// avoids new players from logging in
				if (_secondsShut <= 60 && LoginServerThread.getInstance().getServerStatus() != StatusType.DOWN)
					LoginServerThread.getInstance().setServerStatus(StatusType.DOWN);
				
				_secondsShut--;
				
				Thread.sleep(1000);
			}
		}
		catch (InterruptedException e)
		{
		}
	}
	
	private static void sendServerQuit(int seconds)
	{
		World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS).addNumber(seconds));
	}
	
	/**
	 * Disconnect all {@link Player}s from the server.
	 */
	private static void disconnectAllPlayers()
	{
		for (Player player : World.getInstance().getPlayers())
		{
			final GameClient client = player.getClient();
			if (client != null && !client.isDetached())
			{
				client.close(ServerClose.STATIC_PACKET);
				client.setPlayer(null);
				
				player.setClient(null);
			}
			player.deleteMe();
		}
	}
	
	public static Shutdown getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Shutdown INSTANCE = new Shutdown();
	}
}
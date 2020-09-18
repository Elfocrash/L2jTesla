package dev.l2j.tesla.util;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.gameserver.Shutdown;
import dev.l2j.tesla.gameserver.model.World;

/**
 * Thread to check for deadlocked threads.
 */
public class DeadLockDetector extends Thread
{
	private static final CLogger LOGGER = new CLogger(DeadLockDetector.class.getName());
	
	/** Interval to check for deadlocked threads */
	private static final int SLEEP_TIME = Config.DEADLOCK_CHECK_INTERVAL * 1000;
	
	private final ThreadMXBean tmx;
	
	public DeadLockDetector()
	{
		super("DeadLockDetector");
		tmx = ManagementFactory.getThreadMXBean();
	}
	
	@Override
	public final void run()
	{
		boolean deadlock = false;
		while (!deadlock)
		{
			try
			{
				long[] ids = tmx.findDeadlockedThreads();
				
				if (ids != null)
				{
					deadlock = true;
					ThreadInfo[] tis = tmx.getThreadInfo(ids, true, true);
					StringBuilder info = new StringBuilder();
					info.append("DeadLock Found!\n");
					
					for (ThreadInfo ti : tis)
						info.append(ti.toString());
					
					for (ThreadInfo ti : tis)
					{
						LockInfo[] locks = ti.getLockedSynchronizers();
						MonitorInfo[] monitors = ti.getLockedMonitors();
						
						if (locks.length == 0 && monitors.length == 0)
							continue;
						
						ThreadInfo dl = ti;
						info.append("Java-level deadlock:\n");
						info.append("\t");
						info.append(dl.getThreadName());
						info.append(" is waiting to lock ");
						info.append(dl.getLockInfo().toString());
						info.append(" which is held by ");
						info.append(dl.getLockOwnerName());
						info.append("\n");
						
						while ((dl = tmx.getThreadInfo(new long[]
						{
							dl.getLockOwnerId()
						}, true, true)[0]).getThreadId() != ti.getThreadId())
						{
							info.append("\t");
							info.append(dl.getThreadName());
							info.append(" is waiting to lock ");
							info.append(dl.getLockInfo().toString());
							info.append(" which is held by ");
							info.append(dl.getLockOwnerName());
							info.append("\n");
						}
					}
					LOGGER.warn(info.toString());
					
					if (Config.RESTART_ON_DEADLOCK)
					{
						World.announceToOnlinePlayers("Server has stability issues - restarting now.");
						Shutdown.getInstance().startShutdown(null, "DeadLockDetector - Auto Restart", 60, true);
					}
				}
				Thread.sleep(SLEEP_TIME);
			}
			catch (Exception e)
			{
				LOGGER.warn("The DeadLockDetector encountered a problem.", e);
			}
		}
	}
}
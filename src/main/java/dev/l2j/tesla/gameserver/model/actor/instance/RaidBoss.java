package dev.l2j.tesla.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.model.spawn.L2Spawn;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.PlaySound;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.manager.HeroManager;
import dev.l2j.tesla.gameserver.data.manager.RaidBossManager;
import dev.l2j.tesla.gameserver.data.manager.RaidPointManager;
import dev.l2j.tesla.gameserver.model.actor.ai.type.AttackableAI;

/**
 * This class manages all classic raid bosses.<br>
 * <br>
 * Raid Bosses (RB) are mobs which are supposed to be defeated by a party of several players. It extends most of {@link Monster} aspects.<br>
 * They automatically teleport if out of their initial spawn area, and can randomly attack a Player from their Hate List once attacked.
 */
public class RaidBoss extends Monster
{
	private ScheduledFuture<?> _maintenanceTask;
	
	public RaidBoss(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setRaid(true);
	}
	
	@Override
	public void onSpawn()
	{
		// No random walk allowed.
		setIsNoRndWalk(true);
		
		// Basic behavior.
		super.onSpawn();
		
		// "AI task" for regular bosses.
		_maintenanceTask = ThreadPool.scheduleAtFixedRate(() ->
		{
			// Don't bother with dead bosses.
			if (!isDead())
			{
				// The boss isn't in combat, check the teleport possibility.
				if (!isInCombat())
				{
					// Gordon is excluded too.
					if (getNpcId() != 29095 && Rnd.nextBoolean())
					{
						// Spawn must exist.
						final L2Spawn spawn = getSpawn();
						if (spawn == null)
							return;
						
						// If the boss is above drift range (or 200 minimum), teleport him on his spawn.
						if (!isInsideRadius(spawn.getLocX(), spawn.getLocY(), spawn.getLocZ(), Math.max(Config.MAX_DRIFT_RANGE, 200), true, false))
							teleportTo(spawn.getLoc(), 0);
					}
				}
				// Randomized attack if the boss is already attacking.
				else if (Rnd.get(5) == 0)
					((AttackableAI) getAI()).aggroReconsider();
			}
			
			// For each minion (if any), randomize the attack.
			if (hasMinions())
			{
				for (Monster minion : getMinionList().getSpawnedMinions())
				{
					// Don't bother with dead minions.
					if (minion.isDead() || !minion.isInCombat())
						return;
					
					// Randomized attack if the boss is already attacking.
					if (Rnd.get(3) == 0)
						((AttackableAI) minion.getAI()).aggroReconsider();
				}
			}
		}, 1000, 60000);
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		if (killer != null)
		{
			final Player player = killer.getActingPlayer();
			if (player != null)
			{
				broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
				broadcastPacket(new PlaySound("systemmsg_e.1209"));
				
				final Party party = player.getParty();
				if (party != null)
				{
					for (Player member : party.getMembers())
					{
						RaidPointManager.getInstance().addPoints(member, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
						if (member.isNoble())
							HeroManager.getInstance().setRBkilled(member.getObjectId(), getNpcId());
					}
				}
				else
				{
					RaidPointManager.getInstance().addPoints(player, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
					if (player.isNoble())
						HeroManager.getInstance().setRBkilled(player.getObjectId(), getNpcId());
				}
			}
		}
		
		RaidBossManager.getInstance().onDeath(this);
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		super.deleteMe();
	}
}
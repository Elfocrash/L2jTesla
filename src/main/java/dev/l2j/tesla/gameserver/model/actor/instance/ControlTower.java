package dev.l2j.tesla.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Npc;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.model.entity.Siege;
import dev.l2j.tesla.gameserver.model.spawn.L2Spawn;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.MoveToPawn;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.data.xml.NpcData;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.SiegeSide;

public class ControlTower extends Npc
{
	private final List<L2Spawn> _guards = new ArrayList<>();
	
	private boolean _isActive = true;
	
	public ControlTower(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isAttackable()
	{
		// Attackable during siege by attacker only
		return getCastle() != null && getCastle().getSiege().isInProgress();
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		// Attackable during siege by attacker only
		return attacker instanceof Player && getCastle() != null && getCastle().getSiege().isInProgress() && getCastle().getSiege().checkSide(((Player) attacker).getClan(), SiegeSide.ATTACKER);
	}
	
	@Override
	public void onForcedAttack(Player player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100 && GeoEngine.getInstance().canSeeTarget(player, this))
			{
				// Notify the Player AI with INTERACT
				player.getAI().setIntention(IntentionType.ATTACK, this);
			}
			else
			{
				// Stop moving if we're already in interact range.
				if (player.isMoving() || player.isInCombat())
					player.getAI().setIntention(IntentionType.IDLE);
				
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (getCastle() != null)
		{
			final Siege siege = getCastle().getSiege();
			if (siege.isInProgress())
			{
				_isActive = false;
				
				for (L2Spawn spawn : _guards)
					spawn.setRespawnState(false);
				
				_guards.clear();
				
				// If siege life controls reach 0, broadcast a message to defenders.
				if (siege.getControlTowerCount() == 0)
					siege.announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.TOWER_DESTROYED_NO_RESURRECTION), false);
				
				// Spawn a little version of it. This version is a simple NPC, cleaned on siege end.
				try
				{
					final L2Spawn spawn = new L2Spawn(NpcData.getInstance().getTemplate(13003));
					spawn.setLoc(getPosition());
					
					final Npc tower = spawn.doSpawn(false);
					tower.setCastle(getCastle());
					
					siege.getDestroyedTowers().add(tower);
				}
				catch (Exception e)
				{
					WorldObject.LOGGER.error("Couldn't spawn the control tower.", e);
				}
			}
		}
		return super.doDie(killer);
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	public void registerGuard(L2Spawn guard)
	{
		_guards.add(guard);
	}
	
	public final List<L2Spawn> getGuards()
	{
		return _guards;
	}
	
	public final boolean isActive()
	{
		return _isActive;
	}
}
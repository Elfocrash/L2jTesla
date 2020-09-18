package dev.l2j.tesla.gameserver.data.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.gameserver.idfactory.IdFactory;
import dev.l2j.tesla.gameserver.model.L2Effect;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.entity.Duel;
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * Loads and stores {@link Duel}s for easier management.
 */
public final class DuelManager
{
	private final Map<Integer, Duel> _duels = new ConcurrentHashMap<>();
	
	protected DuelManager()
	{
	}
	
	public Duel getDuel(int duelId)
	{
		return _duels.get(duelId);
	}
	
	/**
	 * Add a Duel on the _duels Map. Both players must exist.
	 * @param playerA : The first player to use.
	 * @param playerB : The second player to use.
	 * @param isPartyDuel : True if the duel is a party duel.
	 */
	public void addDuel(Player playerA, Player playerB, boolean isPartyDuel)
	{
		if (playerA == null || playerB == null)
			return;
		
		// Compute a new id.
		final int duelId = IdFactory.getInstance().getNextId();
		
		// Feed the Map.
		_duels.put(duelId, new Duel(playerA, playerB, isPartyDuel, duelId));
	}
	
	/**
	 * Remove the duel from the Map, and release the id.
	 * @param duelId : The id to remove.
	 */
	public void removeDuel(int duelId)
	{
		// Release the id.
		IdFactory.getInstance().releaseId(duelId);
		
		// Delete from the Map.
		_duels.remove(duelId);
	}
	
	/**
	 * Ends the duel by a surrender action.
	 * @param player : The player used to retrieve the duelId. The player is then used as surrendered opponent.
	 */
	public void doSurrender(Player player)
	{
		if (player == null || !player.isInDuel())
			return;
		
		final Duel duel = getDuel(player.getDuelId());
		if (duel != null)
			duel.doSurrender(player);
	}
	
	/**
	 * Ends the duel by a defeat action.
	 * @param player : The player used to retrieve the duelId. The player is then used as defeated opponent.
	 */
	public void onPlayerDefeat(Player player)
	{
		if (player == null || !player.isInDuel())
			return;
		
		final Duel duel = getDuel(player.getDuelId());
		if (duel != null)
			duel.onPlayerDefeat(player);
	}
	
	/**
	 * Registers a buff which will be removed if the duel ends.
	 * @param player : The player to buff.
	 * @param buff : The effect to cast.
	 */
	public void onBuff(Player player, L2Effect buff)
	{
		if (player == null || !player.isInDuel() || buff == null)
			return;
		
		final Duel duel = getDuel(player.getDuelId());
		if (duel != null)
			duel.onBuff(player, buff);
	}
	
	/**
	 * Removes player from duel, enforcing duel cancellation.
	 * @param player : The player to check.
	 */
	public void onPartyEdit(Player player)
	{
		if (player == null || !player.isInDuel())
			return;
		
		final Duel duel = getDuel(player.getDuelId());
		if (duel != null)
			duel.onPartyEdit();
	}
	
	/**
	 * Broadcasts a packet to the team (or the player) opposing the given player.
	 * @param player : The player used to find the opponent.
	 * @param packet : The packet to send.
	 */
	public void broadcastToOppositeTeam(Player player, L2GameServerPacket packet)
	{
		if (player == null || !player.isInDuel())
			return;
		
		final Duel duel = getDuel(player.getDuelId());
		if (duel == null)
			return;
		
		if (duel.getPlayerA() == player)
			duel.broadcastToTeam2(packet);
		else if (duel.getPlayerB() == player)
			duel.broadcastToTeam1(packet);
		else if (duel.isPartyDuel())
		{
			if (duel.getPlayerA().getParty() != null && duel.getPlayerA().getParty().containsPlayer(player))
				duel.broadcastToTeam2(packet);
			else if (duel.getPlayerB().getParty() != null && duel.getPlayerB().getParty().containsPlayer(player))
				duel.broadcastToTeam1(packet);
		}
	}
	
	public static final DuelManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DuelManager INSTANCE = new DuelManager();
	}
}
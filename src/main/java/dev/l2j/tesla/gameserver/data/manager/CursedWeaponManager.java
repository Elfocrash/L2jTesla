package dev.l2j.tesla.gameserver.data.manager;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.model.actor.Attackable;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.entity.CursedWeapon;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.commons.data.xml.IXmlReader;
import dev.l2j.tesla.commons.util.StatsSet;

import dev.l2j.tesla.gameserver.model.actor.instance.FeedableBeast;
import dev.l2j.tesla.gameserver.model.actor.instance.FestivalMonster;
import dev.l2j.tesla.gameserver.model.actor.instance.GrandBoss;
import dev.l2j.tesla.gameserver.model.actor.instance.RiftInvader;
import dev.l2j.tesla.gameserver.model.actor.instance.SiegeGuard;

import org.w3c.dom.Document;

/**
 * Loads and store {@link CursedWeapon}s. A cursed weapon is a feature involving the drop of a powerful weapon, which stages on player kills and give powerful stats.
 * <ul>
 * <li><u>dropRate :</u> the drop rate used by the monster to drop the item. Default : 1/1000000</li>
 * <li><u>duration :</u> the overall lifetime duration in hours. Default : 72 hours (3 days)</li>
 * <li><u>durationLost :</u> the task time duration, launched when someone pickups a cursed weapon. Renewed when the owner kills a player. Default : 24 hours.</li>
 * <li><u>disapearChance :</u> chance to dissapear when the owner dies. Default : 50%</li>
 * <li><u>stageKills :</u> the number used to calculate random number of needed kills to rank up the cursed weapon. That number is used as a base, it takes a random number between 50% and 150% of that value. Default : 10</li>
 * </ul>
 */
public class CursedWeaponManager implements IXmlReader
{
	private final Map<Integer, CursedWeapon> _cursedWeapons = new HashMap<>();
	
	public CursedWeaponManager()
	{
		if (!Config.ALLOW_CURSED_WEAPONS)
		{
			LOGGER.info("Cursed weapons loading is skipped.");
			return;
		}
		
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/cursedWeapons.xml");
		LOGGER.info("Loaded {} cursed weapons.", _cursedWeapons.size());
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "item", itemNode ->
		{
			final StatsSet set = parseAttributes(itemNode);
			_cursedWeapons.put(set.getInteger("id"), new CursedWeapon(set));
		}));
	}
	
	/**
	 * Ends the life of existing {@link CursedWeapon}s, clear the map, and reload content.
	 */
	public void reload()
	{
		for (CursedWeapon cw : _cursedWeapons.values())
			cw.endOfLife();
		
		_cursedWeapons.clear();
		
		load();
	}
	
	public boolean isCursed(int itemId)
	{
		return _cursedWeapons.containsKey(itemId);
	}
	
	public Collection<CursedWeapon> getCursedWeapons()
	{
		return _cursedWeapons.values();
	}
	
	public Set<Integer> getCursedWeaponsIds()
	{
		return _cursedWeapons.keySet();
	}
	
	public CursedWeapon getCursedWeapon(int itemId)
	{
		return _cursedWeapons.get(itemId);
	}
	
	/**
	 * Checks if a {@link CursedWeapon} can drop. Verify if it is already active and if the {@link Attackable} you killed was a valid candidate.
	 * @param attackable : The target to test.
	 * @param player : The player who killed the Attackable.
	 */
	public synchronized void checkDrop(Attackable attackable, Player player)
	{
		if (attackable instanceof SiegeGuard || attackable instanceof RiftInvader || attackable instanceof FestivalMonster || attackable instanceof GrandBoss || attackable instanceof FeedableBeast)
			return;
		
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActive())
				continue;
			
			if (cw.checkDrop(attackable, player))
				break;
		}
	}
	
	/**
	 * Assimilate a {@link CursedWeapon} if you already possess one (which ranks up possessed weapon), or activate it otherwise.
	 * @param player : The player to test.
	 * @param item : The item player picked up.
	 */
	public void activate(Player player, ItemInstance item)
	{
		final CursedWeapon cw = _cursedWeapons.get(item.getItemId());
		if (cw == null)
			return;
		
		// Can't own 2 cursed swords ; ranks the existing one, and ends the life of the newly obtained cursed weapon.
		if (player.isCursedWeaponEquipped())
		{
			// Ranks up the existing cursed weapon.
			_cursedWeapons.get(player.getCursedWeaponEquippedId()).rankUp();
			
			// Setup the player in order to drop the weapon from inventory.
			cw.setPlayer(player);
			
			// Erase the newly obtained cursed weapon.
			cw.endOfLife();
		}
		else
			cw.activate(player, item);
	}
	
	/**
	 * Retrieve the {@link CursedWeapon} based on its itemId and handle the drop process.
	 * @param itemId : The cursed weapon itemId.
	 * @param killer : The creature who killed the monster.
	 */
	public void drop(int itemId, Creature killer)
	{
		final CursedWeapon cw = _cursedWeapons.get(itemId);
		if (cw == null)
			return;
		
		cw.dropIt(killer);
	}
	
	/**
	 * Retrieve the {@link CursedWeapon} based on its itemId and increase its kills.
	 * @param itemId : The cursed weapon itemId.
	 */
	public void increaseKills(int itemId)
	{
		final CursedWeapon cw = _cursedWeapons.get(itemId);
		if (cw == null)
			return;
		
		cw.increaseKills();
	}
	
	public int getCurrentStage(int itemId)
	{
		final CursedWeapon cw = _cursedWeapons.get(itemId);
		return (cw == null) ? 0 : cw.getCurrentStage();
	}
	
	/**
	 * This method is used on EnterWorld to check if the {@link Player} is equipped with a {@link CursedWeapon}.<br>
	 * If so, we set the player and item references on the cursed weapon, then we reward cursed skills to that player.
	 * @param player : The player to check.
	 */
	public void checkPlayer(Player player)
	{
		if (player == null)
			return;
		
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActivated() && player.getObjectId() == cw.getPlayerId())
			{
				cw.setPlayer(player);
				cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
				cw.giveDemonicSkills();
				
				player.setCursedWeaponEquippedId(cw.getItemId());
				break;
			}
		}
	}
	
	public static final CursedWeaponManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CursedWeaponManager INSTANCE = new CursedWeaponManager();
	}
}
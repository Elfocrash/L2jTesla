package dev.l2j.tesla.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.handler.itemhandlers.BeastSoulShot;
import dev.l2j.tesla.gameserver.handler.itemhandlers.BeastSpice;
import dev.l2j.tesla.gameserver.handler.itemhandlers.BeastSpiritShot;
import dev.l2j.tesla.gameserver.handler.itemhandlers.BlessedSpiritShot;
import dev.l2j.tesla.gameserver.handler.itemhandlers.Book;
import dev.l2j.tesla.gameserver.handler.itemhandlers.Calculator;
import dev.l2j.tesla.gameserver.handler.itemhandlers.Elixir;
import dev.l2j.tesla.gameserver.handler.itemhandlers.EnchantScrolls;
import dev.l2j.tesla.gameserver.handler.itemhandlers.FishShots;
import dev.l2j.tesla.gameserver.handler.itemhandlers.Harvester;
import dev.l2j.tesla.gameserver.handler.itemhandlers.ItemSkills;
import dev.l2j.tesla.gameserver.handler.itemhandlers.Keys;
import dev.l2j.tesla.gameserver.handler.itemhandlers.Maps;
import dev.l2j.tesla.gameserver.handler.itemhandlers.MercTicket;
import dev.l2j.tesla.gameserver.handler.itemhandlers.PaganKeys;
import dev.l2j.tesla.gameserver.handler.itemhandlers.PetFood;
import dev.l2j.tesla.gameserver.handler.itemhandlers.Recipes;
import dev.l2j.tesla.gameserver.handler.itemhandlers.RollingDice;
import dev.l2j.tesla.gameserver.handler.itemhandlers.ScrollOfResurrection;
import dev.l2j.tesla.gameserver.handler.itemhandlers.SeedHandler;
import dev.l2j.tesla.gameserver.handler.itemhandlers.SevenSignsRecord;
import dev.l2j.tesla.gameserver.handler.itemhandlers.SoulCrystals;
import dev.l2j.tesla.gameserver.handler.itemhandlers.SoulShots;
import dev.l2j.tesla.gameserver.handler.itemhandlers.SpecialXMas;
import dev.l2j.tesla.gameserver.handler.itemhandlers.SpiritShot;
import dev.l2j.tesla.gameserver.handler.itemhandlers.SummonItems;
import dev.l2j.tesla.gameserver.model.item.kind.EtcItem;

public class ItemHandler
{
	private final Map<Integer, IItemHandler> _entries = new HashMap<>();
	
	protected ItemHandler()
	{
		registerHandler(new BeastSoulShot());
		registerHandler(new BeastSpice());
		registerHandler(new BeastSpiritShot());
		registerHandler(new BlessedSpiritShot());
		registerHandler(new Book());
		registerHandler(new Calculator());
		registerHandler(new Elixir());
		registerHandler(new EnchantScrolls());
		registerHandler(new FishShots());
		registerHandler(new Harvester());
		registerHandler(new ItemSkills());
		registerHandler(new Keys());
		registerHandler(new Maps());
		registerHandler(new MercTicket());
		registerHandler(new PaganKeys());
		registerHandler(new PetFood());
		registerHandler(new Recipes());
		registerHandler(new RollingDice());
		registerHandler(new ScrollOfResurrection());
		registerHandler(new SeedHandler());
		registerHandler(new SevenSignsRecord());
		registerHandler(new SoulShots());
		registerHandler(new SpecialXMas());
		registerHandler(new SoulCrystals());
		registerHandler(new SpiritShot());
		registerHandler(new SummonItems());
	}
	
	private void registerHandler(IItemHandler handler)
	{
		_entries.put(handler.getClass().getSimpleName().intern().hashCode(), handler);
	}
	
	public IItemHandler getHandler(EtcItem item)
	{
		if (item == null || item.getHandlerName() == null)
			return null;
		
		return _entries.get(item.getHandlerName().hashCode());
	}
	
	public int size()
	{
		return _entries.size();
	}
	
	public static ItemHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemHandler INSTANCE = new ItemHandler();
	}
}
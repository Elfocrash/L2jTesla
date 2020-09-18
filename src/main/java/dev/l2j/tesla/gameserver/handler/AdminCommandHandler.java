package dev.l2j.tesla.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.autobots.admincommands.AdminAutobots;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminAdmin;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminBan;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminBookmark;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminBuffs;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminCamera;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminClanHall;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminCreateItem;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminCursedWeapons;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminDelete;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminDoorControl;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminEditChar;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminEditNpc;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminEffects;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminEnchant;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminExpSp;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminGeoEngine;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminGm;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminGmChat;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminHeal;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminHelpPage;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminKick;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminKnownlist;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminLevel;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminMaintenance;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminMammon;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminManor;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminMenu;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminMovieMaker;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminOlympiad;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminPForge;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminPetition;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminPledge;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminPolymorph;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminRes;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminRideWyvern;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminShop;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminSiege;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminSkill;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminSpawn;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminTarget;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminTeleport;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminZone;

public class AdminCommandHandler
{
	private final Map<Integer, IAdminCommandHandler> _entries = new HashMap<>();
	
	protected AdminCommandHandler()
	{
		registerHandler(new AdminAdmin());
		registerHandler(new AdminAnnouncements());
		registerHandler(new AdminBan());
		registerHandler(new AdminBookmark());
		registerHandler(new AdminBuffs());
		registerHandler(new AdminCamera());
		registerHandler(new AdminClanHall());
		registerHandler(new AdminCreateItem());
		registerHandler(new AdminCursedWeapons());
		registerHandler(new AdminDelete());
		registerHandler(new AdminDoorControl());
		registerHandler(new AdminEditChar());
		registerHandler(new AdminEditNpc());
		registerHandler(new AdminEffects());
		registerHandler(new AdminEnchant());
		registerHandler(new AdminExpSp());
		registerHandler(new AdminGeoEngine());
		registerHandler(new AdminGm());
		registerHandler(new AdminGmChat());
		registerHandler(new AdminHeal());
		registerHandler(new AdminHelpPage());
		registerHandler(new AdminKick());
		registerHandler(new AdminKnownlist());
		registerHandler(new AdminLevel());
		registerHandler(new AdminMaintenance());
		registerHandler(new AdminMammon());
		registerHandler(new AdminManor());
		registerHandler(new AdminMenu());
		registerHandler(new AdminMovieMaker());
		registerHandler(new AdminOlympiad());
		registerHandler(new AdminPetition());
		registerHandler(new AdminPForge());
		registerHandler(new AdminPledge());
		registerHandler(new AdminPolymorph());
		registerHandler(new AdminRes());
		registerHandler(new AdminRideWyvern());
		registerHandler(new AdminShop());
		registerHandler(new AdminSiege());
		registerHandler(new AdminSkill());
		registerHandler(new AdminSpawn());
		registerHandler(new AdminTarget());
		registerHandler(new AdminTeleport());
		registerHandler(new AdminZone());

		registerHandler(new AdminAutobots());
	}
	
	private void registerHandler(IAdminCommandHandler handler)
	{
		for (String id : handler.getAdminCommandList())
			_entries.put(id.hashCode(), handler);
	}
	
	public IAdminCommandHandler getHandler(String adminCommand)
	{
		String command = adminCommand;
		
		if (adminCommand.indexOf(" ") != -1)
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		
		return _entries.get(command.hashCode());
	}
	
	public int size()
	{
		return _entries.size();
	}
	
	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AdminCommandHandler INSTANCE = new AdminCommandHandler();
	}
}
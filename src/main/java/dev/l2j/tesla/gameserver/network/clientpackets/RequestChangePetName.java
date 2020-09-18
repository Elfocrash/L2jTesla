package dev.l2j.tesla.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.commons.lang.StringUtil;

import dev.l2j.tesla.gameserver.data.xml.NpcData;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.instance.Pet;

public final class RequestChangePetName extends L2GameClientPacket
{
	private static final String SEARCH_NAME = "SELECT name FROM pets WHERE name=?";
	
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		// No active pet.
		if (!player.hasPet())
			return;
		
		// Name length integrity check.
		if (_name.length() < 2 || _name.length() > 8)
		{
			player.sendPacket(SystemMessageId.NAMING_PETNAME_UP_TO_8CHARS);
			return;
		}
		
		// Pet is already named.
		final Pet pet = (Pet) player.getSummon();
		if (pet.getName() != null)
		{
			player.sendPacket(SystemMessageId.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET);
			return;
		}
		
		// Invalid name pattern.
		if (!StringUtil.isValidString(_name, "^[A-Za-z0-9]{2,8}$"))
		{
			player.sendPacket(SystemMessageId.NAMING_PETNAME_CONTAINS_INVALID_CHARS);
			return;
		}
		
		// Name is a npc name.
		if (NpcData.getInstance().getTemplateByName(_name) != null)
			return;
		
		// Name already exists on another pet.
		if (doesPetNameExist(_name))
		{
			player.sendPacket(SystemMessageId.NAMING_ALREADY_IN_USE_BY_ANOTHER_PET);
			return;
		}
		
		pet.setName(_name);
		pet.sendPetInfosToOwner();
	}
	
	/**
	 * @param name : The name to search.
	 * @return true if such name already exists on database, false otherwise.
	 */
	private static boolean doesPetNameExist(String name)
	{
		boolean result = true;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SEARCH_NAME))
		{
			ps.setString(1, name);
			
			try (ResultSet rs = ps.executeQuery())
			{
				result = rs.next();
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("Couldn't check existing petname.", e);
		}
		return result;
	}
}
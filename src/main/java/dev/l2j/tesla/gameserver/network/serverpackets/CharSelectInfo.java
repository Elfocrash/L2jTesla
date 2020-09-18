package dev.l2j.tesla.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.network.GameClient;
import dev.l2j.tesla.gameserver.data.sql.ClanTable;
import dev.l2j.tesla.gameserver.model.CharSelectSlot;
import dev.l2j.tesla.gameserver.model.itemcontainer.Inventory;
import dev.l2j.tesla.gameserver.model.pledge.Clan;

public class CharSelectInfo extends L2GameServerPacket
{
	private static final String SELECT_INFOS = "SELECT obj_Id, char_name, level, maxHp, curHp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, sp, karma, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, accesslevel, online, lastAccess, base_class FROM characters WHERE account_name=?";
	private static final String SELECT_CURRENT_SUBCLASS = "SELECT exp, sp, level FROM character_subclasses WHERE char_obj_id=? && class_id=? ORDER BY char_obj_id";
	private static final String SELECT_AUGMENTS = "SELECT attributes FROM augmentations WHERE item_id=?";
	
	private final CharSelectSlot[] _slots;
	private final String _loginName;
	private final int _sessionId;
	
	private int _activeId;
	
	public CharSelectInfo(String loginName, int sessionId)
	{
		_slots = loadCharSelectSlots(loginName);
		_sessionId = sessionId;
		_loginName = loginName;
		
		_activeId = -1;
	}
	
	public CharSelectInfo(String loginName, int sessionId, int activeId)
	{
		_slots = loadCharSelectSlots(loginName);
		_sessionId = sessionId;
		_loginName = loginName;
		
		_activeId = activeId;
	}
	
	@Override
	protected final void writeImpl()
	{
		final int size = _slots.length;
		
		writeC(0x13);
		writeD(size);
		
		long lastAccess = 0L;
		
		if (_activeId == -1)
		{
			for (int i = 0; i < size; i++)
				if (lastAccess < _slots[i].getLastAccess())
				{
					lastAccess = _slots[i].getLastAccess();
					_activeId = i;
				}
		}
		
		for (int i = 0; i < size; i++)
		{
			CharSelectSlot slot = _slots[i];
			
			writeS(slot.getName());
			writeD(slot.getCharId());
			writeS(_loginName);
			writeD(_sessionId);
			writeD(slot.getClanId());
			writeD(0x00); // Builder level
			
			writeD(slot.getSex());
			writeD(slot.getRace());
			writeD(slot.getBaseClassId());
			
			writeD(0x01); // active ??
			
			writeD(slot.getX());
			writeD(slot.getY());
			writeD(slot.getZ());
			
			writeF(slot.getCurrentHp());
			writeF(slot.getCurrentMp());
			
			writeD(slot.getSp());
			writeQ(slot.getExp());
			writeD(slot.getLevel());
			
			writeD(slot.getKarma());
			writeD(slot.getPkKills());
			writeD(slot.getPvPKills());
			
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
			writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
			
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
			writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_FACE));
			
			writeD(slot.getHairStyle());
			writeD(slot.getHairColor());
			writeD(slot.getFace());
			
			writeF(slot.getMaxHp());
			writeF(slot.getMaxMp());
			
			writeD((slot.getAccessLevel() > -1) ? ((slot.getDeleteTimer() > 0) ? (int) ((slot.getDeleteTimer() - System.currentTimeMillis()) / 1000) : 0) : -1);
			writeD(slot.getClassId());
			writeD((i == _activeId) ? 0x01 : 0x00);
			writeC(Math.min(127, slot.getEnchantEffect()));
			writeD(slot.getAugmentationId());
		}
		getClient().setCharSelectSlot(_slots);
	}
	
	public CharSelectSlot[] getCharacterSlots()
	{
		return _slots;
	}
	
	private static CharSelectSlot[] loadCharSelectSlots(String loginName)
	{
		final List<CharSelectSlot> list = new ArrayList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_INFOS))
		{
			ps.setString(1, loginName);
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final int objectId = rs.getInt("obj_id");
					final String name = rs.getString("char_name");
					
					// See if the char must be deleted
					final long deleteTime = rs.getLong("deletetime");
					if (deleteTime > 0)
					{
						if (System.currentTimeMillis() > deleteTime)
						{
							final Clan clan = ClanTable.getInstance().getClan(rs.getInt("clanid"));
							if (clan != null)
								clan.removeClanMember(objectId, 0);
							
							GameClient.deleteCharByObjId(objectId);
							continue;
						}
					}
					
					final CharSelectSlot slot = new CharSelectSlot(objectId, name);
					slot.setAccessLevel(rs.getInt("accesslevel"));
					slot.setLevel(rs.getInt("level"));
					slot.setMaxHp(rs.getInt("maxhp"));
					slot.setCurrentHp(rs.getDouble("curhp"));
					slot.setMaxMp(rs.getInt("maxmp"));
					slot.setCurrentMp(rs.getDouble("curmp"));
					slot.setKarma(rs.getInt("karma"));
					slot.setPkKills(rs.getInt("pkkills"));
					slot.setPvPKills(rs.getInt("pvpkills"));
					slot.setFace(rs.getInt("face"));
					slot.setHairStyle(rs.getInt("hairstyle"));
					slot.setHairColor(rs.getInt("haircolor"));
					slot.setSex(rs.getInt("sex"));
					slot.setExp(rs.getLong("exp"));
					slot.setSp(rs.getInt("sp"));
					slot.setClanId(rs.getInt("clanid"));
					slot.setRace(rs.getInt("race"));
					slot.setX(rs.getInt("x"));
					slot.setY(rs.getInt("y"));
					slot.setZ(rs.getInt("z"));
					
					final int baseClassId = rs.getInt("base_class");
					final int activeClassId = rs.getInt("classid");
					
					// If the player is currently on a subclass, loads subclass content.
					if (baseClassId != activeClassId)
					{
						try (PreparedStatement ps2 = con.prepareStatement(SELECT_CURRENT_SUBCLASS))
						{
							ps2.setInt(1, objectId);
							ps2.setInt(2, activeClassId);
							
							try (ResultSet rs2 = ps2.executeQuery())
							{
								if (rs2.next())
								{
									slot.setExp(rs2.getLong("exp"));
									slot.setSp(rs2.getInt("sp"));
									slot.setLevel(rs2.getInt("level"));
								}
							}
						}
					}
					
					slot.setClassId(activeClassId);
					
					// Get the augmentation for equipped weapon.
					final int weaponObjId = slot.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
					if (weaponObjId > 0)
					{
						try (PreparedStatement ps3 = con.prepareStatement(SELECT_AUGMENTS))
						{
							ps3.setInt(1, weaponObjId);
							
							try (ResultSet rs3 = ps3.executeQuery())
							{
								if (rs3.next())
								{
									final int augment = rs3.getInt("attributes");
									slot.setAugmentationId((augment == -1) ? 0 : augment);
								}
							}
						}
					}
					
					slot.setBaseClassId((baseClassId == 0 && activeClassId > 0) ? activeClassId : baseClassId);
					slot.setDeleteTimer(deleteTime);
					slot.setLastAccess(rs.getLong("lastAccess"));
					
					// Finally add the slot to the list.
					list.add(slot);
				}
			}
			
			return list.toArray(new CharSelectSlot[list.size()]);
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't restore player slots for account {}.", e, loginName);
		}
		
		return new CharSelectSlot[0];
	}
}
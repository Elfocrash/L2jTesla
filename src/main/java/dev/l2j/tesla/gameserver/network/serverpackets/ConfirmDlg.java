package dev.l2j.tesla.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.model.L2Effect;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.kind.Item;
import dev.l2j.tesla.gameserver.model.location.Location;

public class ConfirmDlg extends L2GameServerPacket
{
	private static final int TYPE_ZONE_NAME = 7;
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;
	
	private final int _messageId;
	private final List<CnfDlgData> _info = new ArrayList<>();
	
	private int _time = 0;
	private int _requesterId = 0;
	
	public ConfirmDlg(int messageId)
	{
		_messageId = messageId;
	}
	
	public ConfirmDlg(SystemMessageId messageId)
	{
		_messageId = messageId.getId();
	}
	
	public ConfirmDlg addString(String text)
	{
		_info.add(new CnfDlgData(TYPE_TEXT, text));
		return this;
	}
	
	public ConfirmDlg addNumber(int number)
	{
		_info.add(new CnfDlgData(TYPE_NUMBER, number));
		return this;
	}
	
	public ConfirmDlg addCharName(Creature cha)
	{
		return addString(cha.getName());
	}
	
	public ConfirmDlg addItemName(ItemInstance item)
	{
		return addItemName(item.getItem().getItemId());
	}
	
	public ConfirmDlg addItemName(Item item)
	{
		return addItemName(item.getItemId());
	}
	
	public ConfirmDlg addItemName(int id)
	{
		_info.add(new CnfDlgData(TYPE_ITEM_NAME, id));
		return this;
	}
	
	public ConfirmDlg addZoneName(Location loc)
	{
		_info.add(new CnfDlgData(TYPE_ZONE_NAME, loc));
		return this;
	}
	
	public ConfirmDlg addSkillName(L2Effect effect)
	{
		return addSkillName(effect.getSkill());
	}
	
	public ConfirmDlg addSkillName(L2Skill skill)
	{
		return addSkillName(skill.getId(), skill.getLevel());
	}
	
	public ConfirmDlg addSkillName(int id)
	{
		return addSkillName(id, 1);
	}
	
	public ConfirmDlg addSkillName(int id, int lvl)
	{
		_info.add(new CnfDlgData(TYPE_SKILL_NAME, new IntIntHolder(id, lvl)));
		return this;
	}
	
	public ConfirmDlg addTime(int time)
	{
		_time = time;
		return this;
	}
	
	public ConfirmDlg addRequesterId(int id)
	{
		_requesterId = id;
		return this;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xed);
		writeD(_messageId);
		
		if (_info.isEmpty())
		{
			writeD(0x00);
			writeD(_time);
			writeD(_requesterId);
		}
		else
		{
			writeD(_info.size());
			
			for (CnfDlgData data : _info)
			{
				writeD(data.getType());
				
				switch (data.getType())
				{
					case TYPE_TEXT:
						writeS((String) data.getObject());
						break;
					
					case TYPE_NUMBER:
					case TYPE_NPC_NAME:
					case TYPE_ITEM_NAME:
						writeD((Integer) data.getObject());
						break;
					
					case TYPE_SKILL_NAME:
						final IntIntHolder info = (IntIntHolder) data.getObject();
						writeD(info.getId());
						writeD(info.getValue());
						break;
					
					case TYPE_ZONE_NAME:
						writeLoc((Location) data.getObject());
						break;
				}
			}
			if (_time != 0)
				writeD(_time);
			if (_requesterId != 0)
				writeD(_requesterId);
		}
	}
	
	private static final class CnfDlgData
	{
		private final int _type;
		private final Object _value;
		
		protected CnfDlgData(int type, Object val)
		{
			_type = type;
			_value = val;
		}
		
		public int getType()
		{
			return _type;
		}
		
		public Object getObject()
		{
			return _value;
		}
	}
}
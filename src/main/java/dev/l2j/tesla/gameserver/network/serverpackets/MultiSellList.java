package dev.l2j.tesla.gameserver.network.serverpackets;

import static dev.l2j.tesla.gameserver.data.xml.MultisellData.PAGE_SIZE;

import dev.l2j.tesla.gameserver.model.multisell.Entry;
import dev.l2j.tesla.gameserver.model.multisell.Ingredient;
import dev.l2j.tesla.gameserver.model.multisell.ListContainer;

public class MultiSellList extends L2GameServerPacket
{
	private final ListContainer _list;
	
	private int _index;
	private int _size;
	
	private boolean _finished;
	
	public MultiSellList(ListContainer list, int index)
	{
		_list = list;
		_index = index;
		
		_size = list.getEntries().size() - index;
		if (_size > PAGE_SIZE)
		{
			_finished = false;
			_size = PAGE_SIZE;
		}
		else
			_finished = true;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xd0);
		writeD(_list.getId()); // list id
		writeD(1 + (_index / PAGE_SIZE)); // page
		writeD(_finished ? 1 : 0); // finished
		writeD(PAGE_SIZE); // size of pages
		writeD(_size); // list lenght
		
		while (_size-- > 0)
		{
			Entry ent = _list.getEntries().get(_index++);
			
			writeD(_index);
			writeD(0x00); // C6
			writeD(0x00); // C6
			writeC(ent.isStackable() ? 1 : 0);
			writeH(ent.getProducts().size());
			writeH(ent.getIngredients().size());
			
			for (Ingredient ing : ent.getProducts())
			{
				writeH(ing.getItemId());
				if (ing.getTemplate() != null)
				{
					writeD(ing.getTemplate().getBodyPart());
					writeH(ing.getTemplate().getType2());
				}
				else
				{
					writeD(0);
					writeH(65535);
				}
				writeD(ing.getItemCount());
				writeH(ing.getEnchantLevel());
				writeD(0x00); // TODO: i.getAugmentId()
				writeD(0x00); // TODO: i.getManaLeft()
			}
			
			for (Ingredient ing : ent.getIngredients())
			{
				writeH(ing.getItemId());
				writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : 65535);
				writeD(ing.getItemCount());
				writeH(ing.getEnchantLevel());
				writeD(0x00); // TODO: i.getAugmentId()
				writeD(0x00); // TODO: i.getManaLeft()
			}
		}
	}
}
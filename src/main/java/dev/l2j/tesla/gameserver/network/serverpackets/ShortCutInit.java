package dev.l2j.tesla.gameserver.network.serverpackets;

import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.Shortcut;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

public class ShortCutInit extends L2GameServerPacket
{
	private final Player _player;
	private final Shortcut[] _shortcuts;
	
	public ShortCutInit(Player player)
	{
		_player = player;
		_shortcuts = player.getShortcutList().getShortcuts();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x45);
		writeD(_shortcuts.length);
		
		for (Shortcut sc : _shortcuts)
		{
			writeD(sc.getType().ordinal());
			writeD(sc.getSlot() + sc.getPage() * 12);
			
			switch (sc.getType())
			{
				case ITEM:
					writeD(sc.getId());
					writeD(sc.getCharacterType());
					writeD(sc.getSharedReuseGroup());
					
					if (sc.getSharedReuseGroup() < 0)
					{
						writeD(0x00); // Remaining time
						writeD(0x00); // Cooldown time
					}
					else
					{
						final ItemInstance item = _player.getInventory().getItemByObjectId(sc.getId());
						if (item == null || !item.isEtcItem())
						{
							writeD(0x00); // Remaining time
							writeD(0x00); // Cooldown time
						}
						else
						{
							final IntIntHolder[] skills = item.getEtcItem().getSkills();
							if (skills == null)
							{
								writeD(0x00); // Remaining time
								writeD(0x00); // Cooldown time
							}
							else
							{
								for (IntIntHolder skillInfo : skills)
								{
									final L2Skill itemSkill = skillInfo.getSkill();
									if (_player.getReuseTimeStamp().containsKey(itemSkill.getReuseHashCode()))
									{
										writeD((int) (_player.getReuseTimeStamp().get(itemSkill.getReuseHashCode()).getRemaining() / 1000L));
										writeD((int) (itemSkill.getReuseDelay() / 1000L));
									}
									else
									{
										writeD(0x00); // Remaining time
										writeD(0x00); // Cooldown time
									}
								}
							}
						}
					}
					
					writeD(0x00); // Augmentation
					break;
				
				case SKILL:
					writeD(sc.getId());
					writeD(sc.getLevel());
					writeC(0x00); // C5
					writeD(0x01); // C6
					break;
				
				default:
					writeD(sc.getId());
					writeD(0x01); // C6
			}
		}
	}
}
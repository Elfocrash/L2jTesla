package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * @author glory to Setekh for IL opcode ;p
 */
public class ExShowScreenMessage extends L2GameServerPacket
{
	public static enum SMPOS
	{
		DUMMY,
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		MIDDLE_LEFT,
		MIDDLE_CENTER,
		MIDDLE_RIGHT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT,
	}
	
	private final int _type;
	private final int _sysMessageId;
	private final boolean _hide;
	private final int _unk2;
	private final int _unk3;
	private final boolean _fade;
	private final int _size;
	private final int _position;
	private final boolean _effect;
	private final String _text;
	private final int _time;
	
	public ExShowScreenMessage(String text, int time)
	{
		_type = 1;
		_sysMessageId = -1;
		_hide = false;
		_unk2 = 0;
		_unk3 = 0;
		_fade = false;
		_position = 0x02;
		_text = text;
		_time = time;
		_size = 0;
		_effect = false;
	}
	
	public ExShowScreenMessage(String text, int time, SMPOS pos, boolean effect)
	{
		this(text, time, pos.ordinal(), effect);
	}
	
	public ExShowScreenMessage(String text, int time, int pos, boolean effect)
	{
		_type = 1;
		_sysMessageId = -1;
		_hide = false;
		_unk2 = 0;
		_unk3 = 0;
		_fade = false;
		_position = pos;
		_text = text;
		_time = time;
		_size = 0;
		_effect = effect;
	}
	
	public ExShowScreenMessage(int type, int messageId, int position, boolean hide, int size, int unk2, int unk3, boolean showEffect, int time, boolean fade, String text)
	{
		_type = type;
		_sysMessageId = messageId;
		_hide = hide;
		_unk2 = unk2;
		_unk3 = unk3;
		_fade = fade;
		_position = position;
		_text = text;
		_time = time;
		_size = size;
		_effect = showEffect;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x38);
		writeD(_type); // 0 - system messages, 1 - your defined text
		writeD(_sysMessageId); // system message id (_type must be 0 otherwise no effect)
		writeD(_position); // message position
		writeD(_hide ? 1 : 0); // hide
		writeD(_size); // font size 0 - normal, 1 - small
		writeD(_unk2); // ?
		writeD(_unk3); // ?
		writeD(_effect ? 1 : 0); // upper effect (0 - disabled, 1 enabled) - _position must be 2 (center) otherwise no effect
		writeD(_time); // time
		writeD(_fade ? 1 : 0); // fade effect (0 - disabled, 1 enabled)
		writeS(_text); // your text (_type must be 1, otherwise no effect)
	}
}
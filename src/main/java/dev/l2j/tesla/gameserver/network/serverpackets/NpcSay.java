package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * @author Kerberos
 */
public final class NpcSay extends L2GameServerPacket
{
	private final int _objectId;
	private final int _textType;
	private final int _npcId;
	private final String _text;
	
	public NpcSay(int objectId, int messageType, int npcId, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		_text = text;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x02);
		writeD(_objectId);
		writeD(_textType);
		writeD(_npcId);
		writeS(_text);
	}
}
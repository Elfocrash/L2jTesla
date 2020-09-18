package dev.l2j.tesla.gameserver.network.serverpackets;

/**
 * Send Private (Friend) Message Format: c dSSS d: Unknown S: Sending Player S: Receiving Player S: Message
 * @author Tempy
 */
public class L2FriendSay extends L2GameServerPacket
{
	private final String _sender, _receiver, _message;
	
	public L2FriendSay(String sender, String reciever, String message)
	{
		_sender = sender;
		_receiver = reciever;
		_message = message;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfd);
		writeD(0); // ??
		writeS(_receiver);
		writeS(_sender);
		writeS(_message);
	}
}
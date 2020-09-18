package dev.l2j.tesla.gameserver.network.serverpackets;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is made to create packets with any format
 * @author Maktakien
 */
public class AdminForgePacket extends L2GameServerPacket
{
	private final List<Part> _parts = new ArrayList<>();
	
	private class Part
	{
		public byte b;
		public String str;
		
		public Part(byte bb, String string)
		{
			b = bb;
			str = string;
		}
	}
	
	public AdminForgePacket()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		for (Part p : _parts)
			generate(p.b, p.str);
	}
	
	public boolean generate(byte b, String string)
	{
		if ((b == 'C') || (b == 'c'))
		{
			writeC(Integer.decode(string));
			return true;
		}
		else if ((b == 'D') || (b == 'd'))
		{
			writeD(Integer.decode(string));
			return true;
		}
		else if ((b == 'H') || (b == 'h'))
		{
			writeH(Integer.decode(string));
			return true;
		}
		else if ((b == 'F') || (b == 'f'))
		{
			writeF(Double.parseDouble(string));
			return true;
		}
		else if ((b == 'S') || (b == 's'))
		{
			writeS(string);
			return true;
		}
		else if ((b == 'B') || (b == 'b') || (b == 'X') || (b == 'x'))
		{
			writeB(new BigInteger(string).toByteArray());
			return true;
		}
		return false;
	}
	
	public void addPart(byte b, String string)
	{
		_parts.add(new Part(b, string));
	}
}
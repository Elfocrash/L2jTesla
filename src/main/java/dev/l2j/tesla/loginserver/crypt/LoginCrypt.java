package dev.l2j.tesla.loginserver.crypt;

import java.io.IOException;

import dev.l2j.tesla.commons.random.Rnd;

public class LoginCrypt
{
	private static final byte[] STATIC_BLOWFISH_KEY =
	{
		(byte) 0x6b,
		(byte) 0x60,
		(byte) 0xcb,
		(byte) 0x5b,
		(byte) 0x82,
		(byte) 0xce,
		(byte) 0x90,
		(byte) 0xb1,
		(byte) 0xcc,
		(byte) 0x2b,
		(byte) 0x6c,
		(byte) 0x55,
		(byte) 0x6c,
		(byte) 0x6c,
		(byte) 0x6c,
		(byte) 0x6c
	};
	
	private NewCrypt _staticCrypt;
	private NewCrypt _crypt;
	private boolean _static = true;
	
	public void setKey(byte[] key)
	{
		_staticCrypt = new NewCrypt(STATIC_BLOWFISH_KEY);
		_crypt = new NewCrypt(key);
	}
	
	public boolean decrypt(byte[] raw, final int offset, final int size) throws IOException
	{
		_crypt.decrypt(raw, offset, size);
		return NewCrypt.verifyChecksum(raw, offset, size);
	}
	
	public int encrypt(byte[] raw, final int offset, int size) throws IOException
	{
		// reserve checksum
		size += 4;
		
		if (_static)
		{
			// reserve for XOR "key"
			size += 4;
			
			// padding
			size += 8 - size % 8;
			NewCrypt.encXORPass(raw, offset, size, Rnd.nextInt());
			_staticCrypt.crypt(raw, offset, size);
			
			_static = false;
		}
		else
		{
			// padding
			size += 8 - size % 8;
			NewCrypt.appendChecksum(raw, offset, size);
			_crypt.crypt(raw, offset, size);
		}
		return size;
	}
}

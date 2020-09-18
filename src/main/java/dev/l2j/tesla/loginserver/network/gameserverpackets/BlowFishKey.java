package dev.l2j.tesla.loginserver.network.gameserverpackets;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;

import javax.crypto.Cipher;

import dev.l2j.tesla.commons.logging.CLogger;

import dev.l2j.tesla.loginserver.network.clientpackets.ClientBasePacket;

public class BlowFishKey extends ClientBasePacket
{
	private static final CLogger LOGGER = new CLogger(BlowFishKey.class.getName());
	
	byte[] _key;
	
	public BlowFishKey(byte[] decrypt, RSAPrivateKey privateKey)
	{
		super(decrypt);
		
		int size = readD();
		byte[] tempKey = readB(size);
		
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
			
			final byte[] tempDecryptKey = rsaCipher.doFinal(tempKey);
			
			// there are nulls before the key we must remove them
			int i = 0;
			int len = tempDecryptKey.length;
			for (; i < len; i++)
			{
				if (tempDecryptKey[i] != 0)
					break;
			}
			_key = new byte[len - i];
			System.arraycopy(tempDecryptKey, i, _key, 0, len - i);
		}
		catch (GeneralSecurityException e)
		{
			LOGGER.error("Couldn't decrypt blowfish key (RSA)", e);
		}
	}
	
	public byte[] getKey()
	{
		return _key;
	}
}
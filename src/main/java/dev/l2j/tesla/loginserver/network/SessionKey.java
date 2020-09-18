package dev.l2j.tesla.loginserver.network;

import dev.l2j.tesla.loginserver.network.serverpackets.LoginOk;
import dev.l2j.tesla.loginserver.network.serverpackets.PlayOk;
import dev.l2j.tesla.Config;

/**
 * <p>
 * This class is used to represent session keys used by the client to authenticate in the gameserver
 * </p>
 * <p>
 * A SessionKey is made up of two 8 bytes keys. One is send in the {@link LoginOk LoginOk} packet and the other is sent in {@link PlayOk PlayOk}
 * </p>
 */
public class SessionKey
{
	public int playOkID1;
	public int playOkID2;
	public int loginOkID1;
	public int loginOkID2;
	
	public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
	{
		playOkID1 = playOK1;
		playOkID2 = playOK2;
		loginOkID1 = loginOK1;
		loginOkID2 = loginOK2;
	}
	
	@Override
	public String toString()
	{
		return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
	}
	
	public boolean checkLoginPair(int loginOk1, int loginOk2)
	{
		return loginOkID1 == loginOk1 && loginOkID2 == loginOk2;
	}
	
	/**
	 * Only checks the PlayOk part of the session key if server doesnt show the licence when player logs in.
	 * @param key
	 * @return true if keys are equal.
	 */
	public boolean equals(SessionKey key)
	{
		// when server doesnt show licence it deosnt send the LoginOk packet, client doesnt have this part of the key then.
		if (Config.SHOW_LICENCE)
			return (playOkID1 == key.playOkID1 && loginOkID1 == key.loginOkID1 && playOkID2 == key.playOkID2 && loginOkID2 == key.loginOkID2);
		
		return (playOkID1 == key.playOkID1 && playOkID2 == key.playOkID2);
	}
}
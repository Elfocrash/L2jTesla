package dev.l2j.tesla.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.model.actor.template.NpcTemplate;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcSay;
import dev.l2j.tesla.commons.concurrent.ThreadPool;

import dev.l2j.tesla.gameserver.data.xml.MapRegionData;

public class CastleGatekeeper extends Folk
{
	protected boolean _currentTask;
	private int _delay;
	
	public CastleGatekeeper(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		if (actualCommand.equalsIgnoreCase("tele"))
		{
			if (!_currentTask)
			{
				if (getCastle().getSiege().isInProgress())
				{
					if (getCastle().getSiege().getControlTowerCount() == 0)
						_delay = 480000;
					else
						_delay = 30000;
				}
				else
					_delay = 0;
				
				_currentTask = true;
				ThreadPool.schedule(new oustAllPlayers(), _delay);
			}
			
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/castleteleporter/MassGK-1.htm");
			html.replace("%delay%", getDelayInSeconds());
			player.sendPacket(html);
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		String filename;
		if (!_currentTask)
		{
			if (getCastle().getSiege().isInProgress() && getCastle().getSiege().getControlTowerCount() == 0)
				filename = "data/html/castleteleporter/MassGK-2.htm";
			else
				filename = "data/html/castleteleporter/MassGK.htm";
		}
		else
			filename = "data/html/castleteleporter/MassGK-1.htm";
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", getObjectId());
		html.replace("%delay%", getDelayInSeconds());
		player.sendPacket(html);
	}
	
	protected class oustAllPlayers implements Runnable
	{
		@Override
		public void run()
		{
			// Make the region talk only during a siege
			if (getCastle().getSiege().isInProgress())
			{
				final NpcSay cs = new NpcSay(getObjectId(), 1, getNpcId(), "The defenders of " + getCastle().getName() + " castle have been teleported to the inner castle.");
				final int region = MapRegionData.getInstance().getMapRegion(getX(), getY());
				
				for (Player player : World.getInstance().getPlayers())
				{
					if (region == MapRegionData.getInstance().getMapRegion(player.getX(), player.getY()))
						player.sendPacket(cs);
				}
			}
			getCastle().oustAllPlayers();
			_currentTask = false;
		}
	}
	
	private final int getDelayInSeconds()
	{
		return (_delay > 0) ? _delay / 1000 : 0;
	}
}
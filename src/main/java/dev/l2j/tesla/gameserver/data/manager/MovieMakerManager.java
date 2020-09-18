package dev.l2j.tesla.gameserver.data.manager;

import java.util.HashMap;
import java.util.Map;

import dev.l2j.tesla.gameserver.model.actor.Player;
import dev.l2j.tesla.gameserver.network.serverpackets.NpcHtmlMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.SpecialCamera;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.lang.StringUtil;

/**
 * Store a serie of {@link Sequence}s, which set in a particular order form a cinematic using {@link SpecialCamera} packet.<br>
 * <br>
 * This system is used to ease the craft of animations by admins.
 */
public class MovieMakerManager
{
	protected final Map<Integer, Sequence> _sequences = new HashMap<>();
	
	public void mainHtm(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		
		if (_sequences.isEmpty())
			html.setFile("data/html/admin/movie/main_empty.htm");
		else
		{
			final StringBuilder sb = new StringBuilder();
			for (Sequence sequence : _sequences.values())
				StringUtil.append(sb, "<tr><td>", sequence._sequenceId, ": (", sequence._dist, ", ", sequence._yaw, ", ", sequence._pitch, ", ", sequence._time, ", ", sequence._duration, ", ", sequence._turn, ", ", sequence._rise, ", ", sequence._widescreen, ")</td></tr>");
			
			html.setFile("data/html/admin/movie/main_notempty.htm");
			html.replace("%sequences%", sb.toString());
		}
		player.sendPacket(html);
	}
	
	public void playSequence(int id, Player player)
	{
		final Sequence sequence = _sequences.get(id);
		if (sequence == null)
		{
			player.sendMessage("Wrong sequence id.");
			mainHtm(player);
			return;
		}
		
		player.sendPacket(new SpecialCamera(sequence._objid, sequence._dist, sequence._yaw, sequence._pitch, sequence._time, sequence._duration, sequence._turn, sequence._rise, sequence._widescreen, 0));
	}
	
	public void broadcastSequence(int id, Player player)
	{
		final Sequence sequence = _sequences.get(id);
		if (sequence == null)
		{
			player.sendMessage("Wrong sequence id.");
			mainHtm(player);
			return;
		}
		
		player.broadcastPacket(new SpecialCamera(sequence._objid, sequence._dist, sequence._yaw, sequence._pitch, sequence._time, sequence._duration, sequence._turn, sequence._rise, sequence._widescreen, 0));
	}
	
	public void playSequence(Player player, int objid, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int screen)
	{
		player.sendPacket(new SpecialCamera(objid, dist, yaw, pitch, time, duration, turn, rise, screen, 0));
	}
	
	public void addSequence(Player player, int seqId, int objid, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int screen)
	{
		if (_sequences.containsKey(seqId))
			player.sendMessage("This sequence already exists.");
		else
		{
			final Sequence sequence = new Sequence();
			sequence._sequenceId = seqId;
			sequence._objid = objid;
			sequence._dist = dist;
			sequence._yaw = yaw;
			sequence._pitch = pitch;
			sequence._time = time;
			sequence._duration = duration;
			sequence._turn = turn;
			sequence._rise = rise;
			sequence._widescreen = screen;
			
			_sequences.put(seqId, sequence);
			
		}
		mainHtm(player);
	}
	
	public void addSequence(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/movie/add_sequence.htm");
		player.sendPacket(html);
	}
	
	public void editSequence(int id, Player player)
	{
		final Sequence sequence = _sequences.get(id);
		if (sequence == null)
		{
			player.sendMessage("The sequence couldn't be updated.");
			mainHtm(player);
			return;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/movie/edit_sequence.htm");
		html.replace("%sId%", sequence._sequenceId);
		html.replace("%sDist%", sequence._dist);
		html.replace("%sYaw%", sequence._yaw);
		html.replace("%sPitch%", sequence._pitch);
		html.replace("%sTime%", sequence._time);
		html.replace("%sDuration%", sequence._duration);
		html.replace("%sTurn%", sequence._turn);
		html.replace("%sRise%", sequence._rise);
		html.replace("%sWidescreen%", sequence._widescreen);
		player.sendPacket(html);
	}
	
	public void updateSequence(Player player, int seqId, int objid, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int screen)
	{
		final Sequence sequence = _sequences.get(seqId);
		if (sequence == null)
			player.sendMessage("This sequence doesn't exist.");
		else
		{
			sequence._objid = objid;
			sequence._dist = dist;
			sequence._yaw = yaw;
			sequence._pitch = pitch;
			sequence._time = time;
			sequence._duration = duration;
			sequence._turn = turn;
			sequence._rise = rise;
			sequence._widescreen = screen;
		}
		
		mainHtm(player);
	}
	
	public void deleteSequence(int id, Player player)
	{
		if (_sequences.remove(id) == null)
			player.sendMessage("This sequence id doesn't exist.");
		
		mainHtm(player);
	}
	
	public void playMovie(int broadcast, Player player)
	{
		if (_sequences.isEmpty())
		{
			player.sendMessage("There is nothing to play.");
			mainHtm(player);
			return;
		}
		
		ThreadPool.schedule(new Play(1, broadcast, player), 500);
	}
	
	private class Play implements Runnable
	{
		private final int _id;
		private final int _broad;
		private final Player _player;
		
		public Play(int id, int broadcast, Player player)
		{
			_id = id;
			_broad = broadcast;
			_player = player;
		}
		
		@Override
		public void run()
		{
			final Sequence sequence = _sequences.get(_id);
			if (sequence == null)
			{
				_player.sendMessage("Movie ended on sequence: " + (_id - 1) + ".");
				mainHtm(_player);
				return;
			}
			
			if (_broad == 1)
				_player.broadcastPacket(new SpecialCamera(sequence._objid, sequence._dist, sequence._yaw, sequence._pitch, sequence._time, sequence._duration, sequence._turn, sequence._rise, sequence._widescreen, 0));
			else
				_player.sendPacket(new SpecialCamera(sequence._objid, sequence._dist, sequence._yaw, sequence._pitch, sequence._time, sequence._duration, sequence._turn, sequence._rise, sequence._widescreen, 0));
			
			ThreadPool.schedule(new Play(_id + 1, _broad, _player), (sequence._duration - 100));
		}
	}
	
	protected class Sequence
	{
		protected int _sequenceId;
		protected int _objid;
		protected int _dist;
		protected int _yaw;
		protected int _pitch;
		protected int _time;
		protected int _duration;
		protected int _turn;
		protected int _rise;
		protected int _widescreen;
	}
	
	public static final MovieMakerManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MovieMakerManager INSTANCE = new MovieMakerManager();
	}
}
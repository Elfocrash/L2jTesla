package dev.l2j.tesla.gameserver.network.serverpackets;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import dev.l2j.tesla.gameserver.model.location.Location;

/**
 * A packet used to draw points and lines on client.<br/>
 * <b>Note:</b> Names in points and lines are bugged they will appear even when not looking at them.
 */
public class ExServerPrimitive extends L2GameServerPacket
{
	private final String _name;
	private final List<Point> _points = new ArrayList<>();
	private final List<Line> _lines = new ArrayList<>();
	private int _x;
	private int _y;
	private int _z;
	
	/**
	 * @param name A unique name this will be used to replace lines if second packet is sent
	 * @param x the x coordinate usually middle of drawing area
	 * @param y the y coordinate usually middle of drawing area
	 * @param z the z coordinate usually middle of drawing area
	 */
	public ExServerPrimitive(String name, int x, int y, int z)
	{
		_name = name;
		_x = x;
		_y = y;
		_z = z;
	}
	
	/**
	 * @param name A unique name this will be used to replace lines if second packet is sent
	 * @param location the Location to take coordinates usually middle of drawing area
	 */
	public ExServerPrimitive(String name, Location location)
	{
		this(name, location.getX(), location.getY(), location.getZ());
	}
	
	/**
	 * @param name A unique name this will be used to replace lines if second packet is sent
	 */
	public ExServerPrimitive(String name)
	{
		this(name, 0, 0, 0);
	}
	
	/**
	 * Set XYZ before changing packet each time. Useful for broadcasting purposes instead of building the object anew each time.
	 * @param x the x coordinate usually middle of drawing area
	 * @param y the y coordinate usually middle of drawing area
	 * @param z the z coordinate usually middle of drawing area
	 */
	public void setXYZ(final int x, final int y, final int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	/**
	 * Set XYZ before sending packet each time. Useful for broadcasting purposes instead of building the object anew each time.
	 * @param location the Location to take coordinates usually middle of drawing area
	 */
	public void setXYZ(Location location)
	{
		setXYZ(location.getX(), location.getY(), location.getZ());
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param name the name that will be displayed over the point
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this point
	 * @param y the y coordinate for this point
	 * @param z the z coordinate for this point
	 */
	public void addPoint(String name, int color, boolean isNameColored, int x, int y, int z)
	{
		_points.add(new Point(name, color, isNameColored, x, y, z));
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param name the name that will be displayed over the point
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param location the Location to take coordinates for this point
	 */
	public void addPoint(String name, int color, boolean isNameColored, Location location)
	{
		addPoint(name, color, isNameColored, location.getX(), location.getY(), location.getZ());
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param color the color
	 * @param x the x coordinate for this point
	 * @param y the y coordinate for this point
	 * @param z the z coordinate for this point
	 */
	public void addPoint(int color, int x, int y, int z)
	{
		addPoint("", color, false, x, y, z);
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param color the color
	 * @param location the Location to take coordinates for this point
	 */
	public void addPoint(int color, Location location)
	{
		addPoint("", color, false, location);
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param name the name that will be displayed over the point
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this point
	 * @param y the y coordinate for this point
	 * @param z the z coordinate for this point
	 */
	public void addPoint(String name, Color color, boolean isNameColored, int x, int y, int z)
	{
		addPoint(name, color.getRGB(), isNameColored, x, y, z);
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param name the name that will be displayed over the point
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param location the Location to take coordinates for this point
	 */
	public void addPoint(String name, Color color, boolean isNameColored, Location location)
	{
		addPoint(name, color.getRGB(), isNameColored, location);
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param color the color
	 * @param x the x coordinate for this point
	 * @param y the y coordinate for this point
	 * @param z the z coordinate for this point
	 */
	public void addPoint(Color color, int x, int y, int z)
	{
		addPoint("", color, false, x, y, z);
	}
	
	/**
	 * Adds a point to be displayed on client.
	 * @param color the color
	 * @param location the Location to take coordinates for this point
	 */
	public void addPoint(Color color, Location location)
	{
		addPoint("", color, false, location);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(String name, int color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
	{
		_lines.add(new Line(name, color, isNameColored, x, y, z, x2, y2, z2));
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param location the Location to take coordinates for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(String name, int color, boolean isNameColored, Location location, int x2, int y2, int z2)
	{
		addLine(name, color, isNameColored, location.getX(), location.getY(), location.getZ(), x2, y2, z2);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param location the Location to take coordinates for this line end point
	 */
	public void addLine(String name, int color, boolean isNameColored, int x, int y, int z, Location location)
	{
		addLine(name, color, isNameColored, x, y, z, location.getX(), location.getY(), location.getZ());
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param location the Location to take coordinates for this line start point
	 * @param location2 the Location to take coordinates for this line end point
	 */
	public void addLine(String name, int color, boolean isNameColored, Location location, Location location2)
	{
		addLine(name, color, isNameColored, location, location2.getX(), location2.getY(), location2.getZ());
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(int color, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine("", color, false, x, y, z, x2, y2, z2);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param location the Location to take coordinates for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(int color, Location location, int x2, int y2, int z2)
	{
		addLine("", color, false, location, x2, y2, z2);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param location the Location to take coordinates for this line end point
	 */
	public void addLine(int color, int x, int y, int z, Location location)
	{
		addLine("", color, false, x, y, z, location);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param location the Location to take coordinates for this line start point
	 * @param location2 the Location to take coordinates for this line end point
	 */
	public void addLine(int color, Location location, Location location2)
	{
		addLine("", color, false, location, location2);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(String name, Color color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine(name, color.getRGB(), isNameColored, x, y, z, x2, y2, z2);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param location the Location to take coordinates for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(String name, Color color, boolean isNameColored, Location location, int x2, int y2, int z2)
	{
		addLine(name, color.getRGB(), isNameColored, location, x2, y2, z2);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param location the Location to take coordinates for this line end point
	 */
	public void addLine(String name, Color color, boolean isNameColored, int x, int y, int z, Location location)
	{
		addLine(name, color.getRGB(), isNameColored, x, y, z, location);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param name the name that will be displayed over the middle of line
	 * @param color the color
	 * @param isNameColored if {@code true} name will be colored as well.
	 * @param location the Location to take coordinates for this line start point
	 * @param location2 the Location to take coordinates for this line end point
	 */
	public void addLine(String name, Color color, boolean isNameColored, Location location, Location location2)
	{
		addLine(name, color.getRGB(), isNameColored, location, location2);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(Color color, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine("", color, false, x, y, z, x2, y2, z2);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param location the Location to take coordinates for this line start point
	 * @param x2 the x coordinate for this line end point
	 * @param y2 the y coordinate for this line end point
	 * @param z2 the z coordinate for this line end point
	 */
	public void addLine(Color color, Location location, int x2, int y2, int z2)
	{
		addLine("", color, false, location, x2, y2, z2);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param x the x coordinate for this line start point
	 * @param y the y coordinate for this line start point
	 * @param z the z coordinate for this line start point
	 * @param location the Location to take coordinates for this line end point
	 */
	public void addLine(Color color, int x, int y, int z, Location location)
	{
		addLine("", color, false, x, y, z, location);
	}
	
	/**
	 * Adds a line to be displayed on client
	 * @param color the color
	 * @param location the ILocational to take coordinates for this line start point
	 * @param location2 the ILocational to take coordinates for this line end point
	 */
	public void addLine(Color color, Location location, Location location2)
	{
		addLine("", color, false, location, location2);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x24);
		writeS(_name);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(Integer.MAX_VALUE); // has to do something with display range and angle
		writeD(Integer.MAX_VALUE); // has to do something with display range and angle
		
		writeD(_points.size() + _lines.size());
		
		for (Point point : _points)
		{
			writeC(1); // Its the type in this case Point
			writeS(point.getName());
			int color = point.getColor();
			writeD((color >> 16) & 0xFF); // R
			writeD((color >> 8) & 0xFF); // G
			writeD(color & 0xFF); // B
			writeD(point.isNameColored() ? 1 : 0);
			writeD(point.getX());
			writeD(point.getY());
			writeD(point.getZ());
		}
		
		for (Line line : _lines)
		{
			writeC(2); // Its the type in this case Line
			writeS(line.getName());
			int color = line.getColor();
			writeD((color >> 16) & 0xFF); // R
			writeD((color >> 8) & 0xFF); // G
			writeD(color & 0xFF); // B
			writeD(line.isNameColored() ? 1 : 0);
			writeD(line.getX());
			writeD(line.getY());
			writeD(line.getZ());
			writeD(line.getX2());
			writeD(line.getY2());
			writeD(line.getZ2());
		}
	}
	
	private static class Point
	{
		private final String _name;
		private final int _color;
		private final boolean _isNameColored;
		private final int _x;
		private final int _y;
		private final int _z;
		
		public Point(String name, int color, boolean isNameColored, int x, int y, int z)
		{
			_name = name;
			_color = color;
			_isNameColored = isNameColored;
			_x = x;
			_y = y;
			_z = z;
		}
		
		/**
		 * @return the name
		 */
		public String getName()
		{
			return _name;
		}
		
		/**
		 * @return the color
		 */
		public int getColor()
		{
			return _color;
		}
		
		/**
		 * @return the isNameColored
		 */
		public boolean isNameColored()
		{
			return _isNameColored;
		}
		
		/**
		 * @return the x
		 */
		public int getX()
		{
			return _x;
		}
		
		/**
		 * @return the y
		 */
		public int getY()
		{
			return _y;
		}
		
		/**
		 * @return the z
		 */
		public int getZ()
		{
			return _z;
		}
	}
	
	private static class Line extends Point
	{
		private final int _x2;
		private final int _y2;
		private final int _z2;
		
		public Line(String name, int color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
		{
			super(name, color, isNameColored, x, y, z);
			_x2 = x2;
			_y2 = y2;
			_z2 = z2;
		}
		
		/**
		 * @return the x2
		 */
		public int getX2()
		{
			return _x2;
		}
		
		/**
		 * @return the y2
		 */
		public int getY2()
		{
			return _y2;
		}
		
		/**
		 * @return the z2
		 */
		public int getZ2()
		{
			return _z2;
		}
	}
}
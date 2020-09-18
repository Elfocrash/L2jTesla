package dev.l2j.tesla.gameserver.geoengine.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.geoengine.geodata.GeoStructure;

public class NodeBuffer
{
	private final ReentrantLock _lock = new ReentrantLock();
	private final int _size;
	private final Node[][] _buffer;
	
	// center coordinates
	private int _cx = 0;
	private int _cy = 0;
	
	// target coordinates
	private int _gtx = 0;
	private int _gty = 0;
	private short _gtz = 0;
	
	// pathfinding statistics
	private long _timeStamp = 0;
	private long _lastElapsedTime = 0;
	
	private Node _current = null;
	
	/**
	 * Constructor of NodeBuffer.
	 * @param size : one dimension size of buffer
	 */
	public NodeBuffer(int size)
	{
		// set size
		_size = size;
		
		// initialize buffer
		_buffer = new Node[size][size];
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++)
				_buffer[x][y] = new Node();
	}
	
	/**
	 * Find path consisting of Nodes. Starts at origin coordinates, ends in target coordinates.
	 * @param gox : origin point x
	 * @param goy : origin point y
	 * @param goz : origin point z
	 * @param gtx : target point x
	 * @param gty : target point y
	 * @param gtz : target point z
	 * @return Node : first node of path
	 */
	public final Node findPath(int gox, int goy, short goz, int gtx, int gty, short gtz)
	{
		// load timestamp
		_timeStamp = System.currentTimeMillis();
		
		// set coordinates (middle of the line (gox,goy) - (gtx,gty), will be in the center of the buffer)
		_cx = gox + (gtx - gox - _size) / 2;
		_cy = goy + (gty - goy - _size) / 2;
		
		_gtx = gtx;
		_gty = gty;
		_gtz = gtz;
		
		_current = getNode(gox, goy, goz);
		_current.setCost(getCostH(gox, goy, goz));
		
		int count = 0;
		do
		{
			// reached target?
			if (_current.getLoc().getGeoX() == _gtx && _current.getLoc().getGeoY() == _gty && Math.abs(_current.getLoc().getZ() - _gtz) < 8)
				return _current;
			
			// expand current node
			expand();
			
			// move pointer
			_current = _current.getChild();
		}
		while (_current != null && ++count < Config.MAX_ITERATIONS);
		
		return null;
	}
	
	/**
	 * Creates list of Nodes to show debug path.
	 * @return List<Node> : nodes
	 */
	public final List<Node> debugPath()
	{
		List<Node> result = new ArrayList<>();
		
		for (Node n = _current; n.getParent() != null; n = n.getParent())
		{
			result.add(n);
			n.setCost(-n.getCost());
		}
		
		for (Node[] nodes : _buffer)
		{
			for (Node node : nodes)
			{
				if (node.getLoc() == null || node.getCost() <= 0)
					continue;
				
				result.add(node);
			}
		}
		
		return result;
	}
	
	public final boolean isLocked()
	{
		return _lock.tryLock();
	}
	
	public final void free()
	{
		_current = null;
		
		for (Node[] nodes : _buffer)
			for (Node node : nodes)
				if (node.getLoc() != null)
					node.free();
				
		_lock.unlock();
		_lastElapsedTime = System.currentTimeMillis() - _timeStamp;
	}
	
	public final long getElapsedTime()
	{
		return _lastElapsedTime;
	}
	
	/**
	 * Check _current Node and add its neighbors to the buffer.
	 */
	private final void expand()
	{
		// can't move anywhere, don't expand
		byte nswe = _current.getLoc().getNSWE();
		if (nswe == 0)
			return;
		
		// get geo coords of the node to be expanded
		final int x = _current.getLoc().getGeoX();
		final int y = _current.getLoc().getGeoY();
		final short z = (short) _current.getLoc().getZ();
		
		// can move north, expand
		if ((nswe & GeoStructure.CELL_FLAG_N) != 0)
			addNode(x, y - 1, z, Config.BASE_WEIGHT);
		
		// can move south, expand
		if ((nswe & GeoStructure.CELL_FLAG_S) != 0)
			addNode(x, y + 1, z, Config.BASE_WEIGHT);
		
		// can move west, expand
		if ((nswe & GeoStructure.CELL_FLAG_W) != 0)
			addNode(x - 1, y, z, Config.BASE_WEIGHT);
		
		// can move east, expand
		if ((nswe & GeoStructure.CELL_FLAG_E) != 0)
			addNode(x + 1, y, z, Config.BASE_WEIGHT);
		
		// can move north-west, expand
		if ((nswe & GeoStructure.CELL_FLAG_NW) != 0)
			addNode(x - 1, y - 1, z, Config.DIAGONAL_WEIGHT);
		
		// can move north-east, expand
		if ((nswe & GeoStructure.CELL_FLAG_NE) != 0)
			addNode(x + 1, y - 1, z, Config.DIAGONAL_WEIGHT);
		
		// can move south-west, expand
		if ((nswe & GeoStructure.CELL_FLAG_SW) != 0)
			addNode(x - 1, y + 1, z, Config.DIAGONAL_WEIGHT);
		
		// can move south-east, expand
		if ((nswe & GeoStructure.CELL_FLAG_SE) != 0)
			addNode(x + 1, y + 1, z, Config.DIAGONAL_WEIGHT);
	}
	
	/**
	 * Returns node, if it exists in buffer.
	 * @param x : node X coord
	 * @param y : node Y coord
	 * @param z : node Z coord
	 * @return Node : node, if exits in buffer
	 */
	private final Node getNode(int x, int y, short z)
	{
		// check node X out of coordinates
		final int ix = x - _cx;
		if (ix < 0 || ix >= _size)
			return null;
		
		// check node Y out of coordinates
		final int iy = y - _cy;
		if (iy < 0 || iy >= _size)
			return null;
		
		// get node
		Node result = _buffer[ix][iy];
		
		// check and update
		if (result.getLoc() == null)
			result.setLoc(x, y, z);
		
		// return node
		return result;
	}
	
	/**
	 * Add node given by coordinates to the buffer.
	 * @param x : geo X coord
	 * @param y : geo Y coord
	 * @param z : geo Z coord
	 * @param weight : weight of movement to new node
	 */
	private final void addNode(int x, int y, short z, int weight)
	{
		// get node to be expanded
		Node node = getNode(x, y, z);
		if (node == null)
			return;
		
		// Z distance between nearby cells is higher than cell size, record as geodata bug
		if (node.getLoc().getZ() > (z + 2 * GeoStructure.CELL_HEIGHT))
		{
			if (Config.DEBUG_GEO_NODE)
				GeoEngine.getInstance().addGeoBug(node.getLoc(), "NodeBufferDiag: Check Z coords.");
			
			return;
		}
		
		// node was already expanded, return
		if (node.getCost() >= 0)
			return;
		
		node.setParent(_current);
		if (node.getLoc().getNSWE() != (byte) 0xFF)
			node.setCost(getCostH(x, y, node.getLoc().getZ()) + weight * Config.OBSTACLE_MULTIPLIER);
		else
			node.setCost(getCostH(x, y, node.getLoc().getZ()) + weight);
		
		Node current = _current;
		int count = 0;
		while (current.getChild() != null && count < Config.MAX_ITERATIONS * 4)
		{
			count++;
			if (current.getChild().getCost() > node.getCost())
			{
				node.setChild(current.getChild());
				break;
			}
			current = current.getChild();
		}
		
		if (count >= Config.MAX_ITERATIONS * 4)
			System.err.println("Pathfinding: too long loop detected, cost:" + node.getCost());
		
		current.setChild(node);
	}
	
	/**
	 * @param x : node X coord
	 * @param y : node Y coord
	 * @param i : node Z coord
	 * @return double : node cost
	 */
	private final double getCostH(int x, int y, int i)
	{
		final int dX = x - _gtx;
		final int dY = y - _gty;
		final int dZ = (i - _gtz) / GeoStructure.CELL_HEIGHT;
		
		// return (Math.abs(dX) + Math.abs(dY) + Math.abs(dZ)) * Config.HEURISTIC_WEIGHT; // Manhattan distance
		return Math.sqrt(dX * dX + dY * dY + dZ * dZ) * Config.HEURISTIC_WEIGHT; // Direct distance
	}
}
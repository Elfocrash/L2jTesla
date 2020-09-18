package dev.l2j.tesla.gameserver.geoengine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dev.l2j.tesla.Config;
import dev.l2j.tesla.gameserver.geoengine.pathfinding.Node;
import dev.l2j.tesla.gameserver.geoengine.pathfinding.NodeBuffer;
import dev.l2j.tesla.gameserver.idfactory.IdFactory;
import dev.l2j.tesla.commons.config.ExProperties;
import dev.l2j.tesla.commons.lang.StringUtil;
import dev.l2j.tesla.commons.logging.CLogger;
import dev.l2j.tesla.commons.math.MathUtil;

import dev.l2j.tesla.gameserver.enums.GeoType;
import dev.l2j.tesla.gameserver.geoengine.geodata.ABlock;
import dev.l2j.tesla.gameserver.geoengine.geodata.BlockComplex;
import dev.l2j.tesla.gameserver.geoengine.geodata.BlockComplexDynamic;
import dev.l2j.tesla.gameserver.geoengine.geodata.BlockFlat;
import dev.l2j.tesla.gameserver.geoengine.geodata.BlockMultilayer;
import dev.l2j.tesla.gameserver.geoengine.geodata.BlockMultilayerDynamic;
import dev.l2j.tesla.gameserver.geoengine.geodata.BlockNull;
import dev.l2j.tesla.gameserver.geoengine.geodata.GeoLocation;
import dev.l2j.tesla.gameserver.geoengine.geodata.GeoStructure;
import dev.l2j.tesla.gameserver.geoengine.geodata.IBlockDynamic;
import dev.l2j.tesla.gameserver.geoengine.geodata.IGeoObject;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.actor.Creature;
import dev.l2j.tesla.gameserver.model.actor.instance.Door;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.location.Location;

public class GeoEngine
{
	protected static final CLogger LOGGER = new CLogger(GeoEngine.class.getName());
	
	private static final String GEO_BUG = "%d;%d;%d;%d;%d;%d;%d;%s\r\n";
	
	private final ABlock[][] _blocks;
	private final BlockNull _nullBlock;
	
	private final PrintWriter _geoBugReports;
	private final Set<ItemInstance> _debugItems = ConcurrentHashMap.newKeySet();
	
	// pre-allocated buffers
	private BufferHolder[] _buffers;
	
	// pathfinding statistics
	private int _findSuccess = 0;
	private int _findFails = 0;
	private int _postFilterPlayableUses = 0;
	private int _postFilterUses = 0;
	private long _postFilterElapsed = 0;
	
	/**
	 * GeoEngine contructor. Loads all geodata files of chosen geodata format.
	 */
	public GeoEngine()
	{
		// initialize block container
		_blocks = new ABlock[GeoStructure.GEO_BLOCKS_X][GeoStructure.GEO_BLOCKS_Y];
		
		// load null block
		_nullBlock = new BlockNull();
		
		// initialize multilayer temporarily buffer
		BlockMultilayer.initialize();
		
		// load geo files according to geoengine config setup
		final ExProperties props = Config.initProperties(Config.GEOENGINE_FILE);
		int loaded = 0;
		int failed = 0;
		for (int rx = World.TILE_X_MIN; rx <= World.TILE_X_MAX; rx++)
		{
			for (int ry = World.TILE_Y_MIN; ry <= World.TILE_Y_MAX; ry++)
			{
				if (props.containsKey(String.valueOf(rx) + "_" + String.valueOf(ry)))
				{
					// region file is load-able, try to load it
					if (loadGeoBlocks(rx, ry))
						loaded++;
					else
						failed++;
				}
				else
				{
					// region file is not load-able, load null blocks
					loadNullBlocks(rx, ry);
				}
			}
		}
		LOGGER.info("Loaded {} L2D region files.", loaded);
		
		// release multilayer block temporarily buffer
		BlockMultilayer.release();
		
		if (failed > 0)
		{
			LOGGER.warn("Failed to load {} L2D region files. Please consider to check your \"geodata.properties\" settings and location of your geodata files.", failed);
			System.exit(1);
		}
		
		// initialize bug reports
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(new FileOutputStream(new File(Config.GEODATA_PATH + "geo_bugs.txt"), true), true);
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load \"geo_bugs.txt\" file.", e);
		}
		_geoBugReports = writer;
		
		String[] array = Config.PATHFIND_BUFFERS.split(";");
		_buffers = new BufferHolder[array.length];
		
		int count = 0;
		for (int i = 0; i < array.length; i++)
		{
			String buf = array[i];
			String[] args = buf.split("x");
			
			try
			{
				int size = Integer.parseInt(args[1]);
				count += size;
				_buffers[i] = new BufferHolder(Integer.parseInt(args[0]), size);
			}
			catch (Exception e)
			{
				LOGGER.error("Couldn't load buffer setting: {}.", e, buf);
			}
		}
		
		LOGGER.info("Loaded {} node buffers.", count);
	}
	
	/**
	 * Create list of node locations as result of calculated buffer node tree.
	 * @param target : the entry point
	 * @return List<NodeLoc> : list of node location
	 */
	private static final List<Location> constructPath(Node target)
	{
		// create empty list
		LinkedList<Location> list = new LinkedList<>();
		
		// set direction X/Y
		int dx = 0;
		int dy = 0;
		
		// get target parent
		Node parent = target.getParent();
		
		// while parent exists
		while (parent != null)
		{
			// get parent <> target direction X/Y
			final int nx = parent.getLoc().getGeoX() - target.getLoc().getGeoX();
			final int ny = parent.getLoc().getGeoY() - target.getLoc().getGeoY();
			
			// direction has changed?
			if (dx != nx || dy != ny)
			{
				// add node to the beginning of the list
				list.addFirst(target.getLoc());
				
				// update direction X/Y
				dx = nx;
				dy = ny;
			}
			
			// move to next node, set target and get its parent
			target = parent;
			parent = target.getParent();
		}
		
		// return list
		return list;
	}
	
	/**
	 * Provides optimize selection of the buffer. When all pre-initialized buffer are locked, creates new buffer and log this situation.
	 * @param size : pre-calculated minimal required size
	 * @param playable : moving object is playable?
	 * @return NodeBuffer : buffer
	 */
	private final NodeBuffer getBuffer(int size, boolean playable)
	{
		NodeBuffer current = null;
		for (BufferHolder holder : _buffers)
		{
			// Find proper size of buffer
			if (holder._size < size)
				continue;
			
			// Find unlocked NodeBuffer
			for (NodeBuffer buffer : holder._buffer)
			{
				if (!buffer.isLocked())
					continue;
				
				holder._uses++;
				if (playable)
					holder._playableUses++;
				
				holder._elapsed += buffer.getElapsedTime();
				return buffer;
			}
			
			// NodeBuffer not found, allocate temporary buffer
			current = new NodeBuffer(holder._size);
			current.isLocked();
			
			holder._overflows++;
			if (playable)
				holder._playableOverflows++;
		}
		
		return current;
	}
	
	/**
	 * Loads geodata from a file. When file does not exist, is corrupted or not consistent, loads none geodata.
	 * @param regionX : Geodata file region X coordinate.
	 * @param regionY : Geodata file region Y coordinate.
	 * @return boolean : True, when geodata file was loaded without problem.
	 */
	private final boolean loadGeoBlocks(int regionX, int regionY)
	{
		final String filename = String.format(GeoType.L2D.getFilename(), regionX, regionY);
		final String filepath = Config.GEODATA_PATH + filename;
		
		// standard load
		try (RandomAccessFile raf = new RandomAccessFile(filepath, "r");
			FileChannel fc = raf.getChannel())
		{
			// initialize file buffer
			MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).load();
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			
			// get block indexes
			final int blockX = (regionX - World.TILE_X_MIN) * GeoStructure.REGION_BLOCKS_X;
			final int blockY = (regionY - World.TILE_Y_MIN) * GeoStructure.REGION_BLOCKS_Y;
			
			// loop over region blocks
			for (int ix = 0; ix < GeoStructure.REGION_BLOCKS_X; ix++)
			{
				for (int iy = 0; iy < GeoStructure.REGION_BLOCKS_Y; iy++)
				{
					// get block type
					final byte type = buffer.get();
					
					// load block according to block type
					switch (type)
					{
						case GeoStructure.TYPE_FLAT_L2D:
							_blocks[blockX + ix][blockY + iy] = new BlockFlat(buffer, GeoType.L2D);
							break;
						
						case GeoStructure.TYPE_COMPLEX_L2D:
							_blocks[blockX + ix][blockY + iy] = new BlockComplex(buffer, GeoType.L2D);
							break;
						
						case GeoStructure.TYPE_MULTILAYER_L2D:
							_blocks[blockX + ix][blockY + iy] = new BlockMultilayer(buffer, GeoType.L2D);
							break;
						
						default:
							throw new IllegalArgumentException("Unknown block type: " + type);
					}
				}
			}
			
			// check data consistency
			if (buffer.remaining() > 0)
				LOGGER.warn("Region file {} can be corrupted, remaining {} bytes to read.", filename, buffer.remaining());
			
			// loading was successful
			return true;
		}
		catch (Exception e)
		{
			// an error occured while loading, load null blocks
			LOGGER.error("Error loading {} region file.", e, filename);
			
			// replace whole region file with null blocks
			loadNullBlocks(regionX, regionY);
			
			// loading was not successful
			return false;
		}
	}
	
	/**
	 * Loads null blocks. Used when no region file is detected or an error occurs during loading.
	 * @param regionX : Geodata file region X coordinate.
	 * @param regionY : Geodata file region Y coordinate.
	 */
	private final void loadNullBlocks(int regionX, int regionY)
	{
		// get block indexes
		final int blockX = (regionX - World.TILE_X_MIN) * GeoStructure.REGION_BLOCKS_X;
		final int blockY = (regionY - World.TILE_Y_MIN) * GeoStructure.REGION_BLOCKS_Y;
		
		// load all null blocks
		for (int ix = 0; ix < GeoStructure.REGION_BLOCKS_X; ix++)
			for (int iy = 0; iy < GeoStructure.REGION_BLOCKS_Y; iy++)
				_blocks[blockX + ix][blockY + iy] = _nullBlock;
	}
	
	/**
	 * Returns the height of cell, which is closest to given coordinates.<br>
	 * Geodata without {@link IGeoObject} are taken in consideration.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return short : Cell geodata Z coordinate, closest to given coordinates.
	 */
	private final short getHeightNearestOriginal(int geoX, int geoY, int worldZ)
	{
		return getBlock(geoX, geoY).getHeightNearestOriginal(geoX, geoY, worldZ);
	}
	
	/**
	 * Returns the NSWE flag byte of cell, which is closes to given coordinates.<br>
	 * Geodata without {@link IGeoObject} are taken in consideration.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return short : Cell NSWE flag byte coordinate, closest to given coordinates.
	 */
	private final byte getNsweNearestOriginal(int geoX, int geoY, int worldZ)
	{
		return getBlock(geoX, geoY).getNsweNearestOriginal(geoX, geoY, worldZ);
	}
	
	// GEODATA - GENERAL
	
	/**
	 * Converts world X to geodata X.
	 * @param worldX
	 * @return int : Geo X
	 */
	public static final int getGeoX(int worldX)
	{
		return (MathUtil.limit(worldX, World.WORLD_X_MIN, World.WORLD_X_MAX) - World.WORLD_X_MIN) >> 4;
	}
	
	/**
	 * Converts world Y to geodata Y.
	 * @param worldY
	 * @return int : Geo Y
	 */
	public static final int getGeoY(int worldY)
	{
		return (MathUtil.limit(worldY, World.WORLD_Y_MIN, World.WORLD_Y_MAX) - World.WORLD_Y_MIN) >> 4;
	}
	
	/**
	 * Converts geodata X to world X.
	 * @param geoX
	 * @return int : World X
	 */
	public static final int getWorldX(int geoX)
	{
		return (MathUtil.limit(geoX, 0, GeoStructure.GEO_CELLS_X) << 4) + World.WORLD_X_MIN + 8;
	}
	
	/**
	 * Converts geodata Y to world Y.
	 * @param geoY
	 * @return int : World Y
	 */
	public static final int getWorldY(int geoY)
	{
		return (MathUtil.limit(geoY, 0, GeoStructure.GEO_CELLS_Y) << 4) + World.WORLD_Y_MIN + 8;
	}
	
	/**
	 * Returns block of geodata on given coordinates.
	 * @param geoX : Geodata X
	 * @param geoY : Geodata Y
	 * @return {@link ABlock} : Bloack of geodata.
	 */
	public final ABlock getBlock(int geoX, int geoY)
	{
		return _blocks[geoX / GeoStructure.BLOCK_CELLS_X][geoY / GeoStructure.BLOCK_CELLS_Y];
	}
	
	/**
	 * Check if geo coordinates has geo.
	 * @param geoX : Geodata X
	 * @param geoY : Geodata Y
	 * @return boolean : True, if given geo coordinates have geodata
	 */
	public final boolean hasGeoPos(int geoX, int geoY)
	{
		return getBlock(geoX, geoY).hasGeoPos();
	}
	
	/**
	 * Returns the height of cell, which is closest to given coordinates.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return short : Cell geodata Z coordinate, closest to given coordinates.
	 */
	public final short getHeightNearest(int geoX, int geoY, int worldZ)
	{
		return getBlock(geoX, geoY).getHeightNearest(geoX, geoY, worldZ);
	}
	
	/**
	 * Returns the NSWE flag byte of cell, which is closes to given coordinates.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return short : Cell NSWE flag byte coordinate, closest to given coordinates.
	 */
	public final byte getNsweNearest(int geoX, int geoY, int worldZ)
	{
		return getBlock(geoX, geoY).getNsweNearest(geoX, geoY, worldZ);
	}
	
	/**
	 * Check if world coordinates has geo.
	 * @param worldX : World X
	 * @param worldY : World Y
	 * @return boolean : True, if given world coordinates have geodata
	 */
	public final boolean hasGeo(int worldX, int worldY)
	{
		return hasGeoPos(getGeoX(worldX), getGeoY(worldY));
	}
	
	/**
	 * Returns closest Z coordinate according to geodata.
	 * @param worldX : world x
	 * @param worldY : world y
	 * @param worldZ : world z
	 * @return short : nearest Z coordinates according to geodata
	 */
	public final short getHeight(int worldX, int worldY, int worldZ)
	{
		return getHeightNearest(getGeoX(worldX), getGeoY(worldY), worldZ);
	}
	
	// GEODATA - DYNAMIC
	
	/**
	 * Returns calculated NSWE flag byte as a description of {@link IGeoObject}.<br>
	 * The {@link IGeoObject} is defined by boolean 2D array, saying if the object is present on given cell or not.
	 * @param inside : 2D description of {@link IGeoObject}
	 * @return byte[][] : Returns NSWE flags of {@link IGeoObject}.
	 */
	public static final byte[][] calculateGeoObject(boolean inside[][])
	{
		// get dimensions
		final int width = inside.length;
		final int height = inside[0].length;
		
		// create object flags for geodata, according to the geo object 2D description
		final byte[][] result = new byte[width][height];
		
		// loop over each cell of the geo object
		for (int ix = 0; ix < width; ix++)
			for (int iy = 0; iy < height; iy++)
				if (inside[ix][iy])
				{
					// cell is inside geo object, block whole movement (nswe = 0)
					result[ix][iy] = 0;
				}
				else
				{
					// cell is outside of geo object, block only movement leading inside geo object
					
					// set initial value -> no geodata change
					byte nswe = (byte) 0xFF;
					
					// perform axial and diagonal checks
					if (iy < height - 1)
						if (inside[ix][iy + 1])
							nswe &= ~GeoStructure.CELL_FLAG_S;
					if (iy > 0)
						if (inside[ix][iy - 1])
							nswe &= ~GeoStructure.CELL_FLAG_N;
					if (ix < width - 1)
						if (inside[ix + 1][iy])
							nswe &= ~GeoStructure.CELL_FLAG_E;
					if (ix > 0)
						if (inside[ix - 1][iy])
							nswe &= ~GeoStructure.CELL_FLAG_W;
					if (ix < (width - 1) && iy < (height - 1))
						if (inside[ix + 1][iy + 1] || inside[ix][iy + 1] || inside[ix + 1][iy])
							nswe &= ~GeoStructure.CELL_FLAG_SE;
					if (ix < (width - 1) && iy > 0)
						if (inside[ix + 1][iy - 1] || inside[ix][iy - 1] || inside[ix + 1][iy])
							nswe &= ~GeoStructure.CELL_FLAG_NE;
					if (ix > 0 && iy < (height - 1))
						if (inside[ix - 1][iy + 1] || inside[ix][iy + 1] || inside[ix - 1][iy])
							nswe &= ~GeoStructure.CELL_FLAG_SW;
					if (ix > 0 && iy > 0)
						if (inside[ix - 1][iy - 1] || inside[ix][iy - 1] || inside[ix - 1][iy])
							nswe &= ~GeoStructure.CELL_FLAG_NW;
						
					result[ix][iy] = nswe;
				}
			
		return result;
	}
	
	/**
	 * Add {@link IGeoObject} to the geodata.
	 * @param object : An object using {@link IGeoObject} interface.
	 */
	public final void addGeoObject(IGeoObject object)
	{
		toggleGeoObject(object, true);
	}
	
	/**
	 * Remove {@link IGeoObject} from the geodata.
	 * @param object : An object using {@link IGeoObject} interface.
	 */
	public final void removeGeoObject(IGeoObject object)
	{
		toggleGeoObject(object, false);
	}
	
	/**
	 * Toggles an {@link IGeoObject} in the geodata.
	 * @param object : An object using {@link IGeoObject} interface.
	 * @param add : Add/remove object.
	 */
	private final void toggleGeoObject(IGeoObject object, boolean add)
	{
		// get object geo coordinates and data
		final int minGX = object.getGeoX();
		final int minGY = object.getGeoY();
		final byte[][] geoData = object.getObjectGeoData();
		
		// get min/max block coordinates
		int minBX = minGX / GeoStructure.BLOCK_CELLS_X;
		int maxBX = (minGX + geoData.length - 1) / GeoStructure.BLOCK_CELLS_X;
		int minBY = minGY / GeoStructure.BLOCK_CELLS_Y;
		int maxBY = (minGY + geoData[0].length - 1) / GeoStructure.BLOCK_CELLS_Y;
		
		// loop over affected blocks in X direction
		for (int bx = minBX; bx <= maxBX; bx++)
		{
			// loop over affected blocks in Y direction
			for (int by = minBY; by <= maxBY; by++)
			{
				ABlock block;
				
				// conversion to dynamic block must be synchronized to prevent 2 independent threads converting same block
				synchronized (_blocks)
				{
					// get related block
					block = _blocks[bx][by];
					
					// check for dynamic block
					if (!(block instanceof IBlockDynamic))
					{
						// null block means no geodata (particular region file is not loaded), no geodata means no geobjects
						if (block instanceof BlockNull)
							continue;
						
						// not a dynamic block, convert it
						if (block instanceof BlockFlat)
						{
							// convert flat block to the dynamic complex block
							block = new BlockComplexDynamic(bx, by, (BlockFlat) block);
							_blocks[bx][by] = block;
						}
						else if (block instanceof BlockComplex)
						{
							// convert complex block to the dynamic complex block
							block = new BlockComplexDynamic(bx, by, (BlockComplex) block);
							_blocks[bx][by] = block;
						}
						else if (block instanceof BlockMultilayer)
						{
							// convert multilayer block to the dynamic multilayer block
							block = new BlockMultilayerDynamic(bx, by, (BlockMultilayer) block);
							_blocks[bx][by] = block;
						}
					}
				}
				
				// add/remove geo object to/from dynamic block
				if (add)
					((IBlockDynamic) block).addGeoObject(object);
				else
					((IBlockDynamic) block).removeGeoObject(object);
			}
		}
	}
	
	// PATHFINDING
	
	/**
	 * Check line of sight from {@link WorldObject} to {@link WorldObject}.
	 * @param origin : The origin object.
	 * @param target : The target object.
	 * @return {@code boolean} : True if origin can see target
	 */
	public final boolean canSeeTarget(WorldObject origin, WorldObject target)
	{
		// get origin and target world coordinates
		final int ox = origin.getX();
		final int oy = origin.getY();
		final int oz = origin.getZ();
		final int tx = target.getX();
		final int ty = target.getY();
		final int tz = target.getZ();
		
		// get origin and check existing geo coordinates
		final int gox = getGeoX(ox);
		final int goy = getGeoY(oy);
		if (!hasGeoPos(gox, goy))
			return true;
		
		final short goz = getHeightNearest(gox, goy, oz);
		
		// get target and check existing geo coordinates
		final int gtx = getGeoX(tx);
		final int gty = getGeoY(ty);
		if (!hasGeoPos(gtx, gty))
			return true;
		
		final boolean door = target instanceof Door;
		final short gtz = door ? getHeightNearestOriginal(gtx, gty, tz) : getHeightNearest(gtx, gty, tz);
		
		// origin and target coordinates are same
		if (gox == gtx && goy == gty)
			return goz == gtz;
		
		// get origin and target height, real height = collision height * 2
		double oheight = 0;
		if (origin instanceof Creature)
			oheight = ((Creature) origin).getCollisionHeight() * 2;
		
		double theight = 0;
		if (target instanceof Creature)
			theight = ((Creature) target).getCollisionHeight() * 2;
		
		// perform geodata check
		return door ? checkSeeOriginal(gox, goy, goz, oheight, gtx, gty, gtz, theight) : checkSee(gox, goy, goz, oheight, gtx, gty, gtz, theight);
	}
	
	/**
	 * Check line of sight from {@link WorldObject} to {@link Location}.
	 * @param origin : The origin object.
	 * @param position : The target position.
	 * @return {@code boolean} : True if object can see position
	 */
	public final boolean canSeeTarget(WorldObject origin, Location position)
	{
		// get origin and target world coordinates
		final int ox = origin.getX();
		final int oy = origin.getY();
		final int oz = origin.getZ();
		final int tx = position.getX();
		final int ty = position.getY();
		final int tz = position.getZ();
		
		// get origin and check existing geo coordinates
		final int gox = getGeoX(ox);
		final int goy = getGeoY(oy);
		if (!hasGeoPos(gox, goy))
			return true;
		
		final short goz = getHeightNearest(gox, goy, oz);
		
		// get target and check existing geo coordinates
		final int gtx = getGeoX(tx);
		final int gty = getGeoY(ty);
		if (!hasGeoPos(gtx, gty))
			return true;
		
		final short gtz = getHeightNearest(gtx, gty, tz);
		
		// origin and target coordinates are same
		if (gox == gtx && goy == gty)
			return goz == gtz;
		
		// get origin and target height, real height = collision height * 2
		double oheight = 0;
		if (origin instanceof Creature)
			oheight = ((Creature) origin).getTemplate().getCollisionHeight();
		
		// perform geodata check
		return checkSee(gox, goy, goz, oheight, gtx, gty, gtz, 0);
	}
	
	/**
	 * Simple check for origin to target visibility.
	 * @param gox : origin X geodata coordinate
	 * @param goy : origin Y geodata coordinate
	 * @param goz : origin Z geodata coordinate
	 * @param oheight : origin height (if instance of {@link Creature})
	 * @param gtx : target X geodata coordinate
	 * @param gty : target Y geodata coordinate
	 * @param gtz : target Z geodata coordinate
	 * @param theight : target height (if instance of {@link Creature})
	 * @return {@code boolean} : True, when target can be seen.
	 */
	protected final boolean checkSee(int gox, int goy, int goz, double oheight, int gtx, int gty, int gtz, double theight)
	{
		// get line of sight Z coordinates
		double losoz = goz + oheight * Config.PART_OF_CHARACTER_HEIGHT / 100;
		double lostz = gtz + theight * Config.PART_OF_CHARACTER_HEIGHT / 100;
		
		// get X delta and signum
		final int dx = Math.abs(gtx - gox);
		final int sx = gox < gtx ? 1 : -1;
		final byte dirox = sx > 0 ? GeoStructure.CELL_FLAG_E : GeoStructure.CELL_FLAG_W;
		final byte dirtx = sx > 0 ? GeoStructure.CELL_FLAG_W : GeoStructure.CELL_FLAG_E;
		
		// get Y delta and signum
		final int dy = Math.abs(gty - goy);
		final int sy = goy < gty ? 1 : -1;
		final byte diroy = sy > 0 ? GeoStructure.CELL_FLAG_S : GeoStructure.CELL_FLAG_N;
		final byte dirty = sy > 0 ? GeoStructure.CELL_FLAG_N : GeoStructure.CELL_FLAG_S;
		
		// get Z delta
		final int dm = Math.max(dx, dy);
		final double dz = (lostz - losoz) / dm;
		
		// get direction flag for diagonal movement
		final byte diroxy = getDirXY(dirox, diroy);
		final byte dirtxy = getDirXY(dirtx, dirty);
		
		// delta, determines axis to move on (+..X axis, -..Y axis)
		int d = dx - dy;
		
		// NSWE direction of movement
		byte diro;
		byte dirt;
		
		// clearDebugItems();
		// dropDebugItem(728, 0, new GeoLocation(gox, goy, goz)); // blue potion
		// dropDebugItem(728, 0, new GeoLocation(gtx, gty, gtz)); // blue potion
		
		// initialize node values
		int nox = gox;
		int noy = goy;
		int ntx = gtx;
		int nty = gty;
		byte nsweo = getNsweNearest(gox, goy, goz);
		byte nswet = getNsweNearest(gtx, gty, gtz);
		
		// loop
		ABlock block;
		int index;
		for (int i = 0; i < (dm + 1) / 2; i++)
		{
			// dropDebugItem(57, 0, new GeoLocation(gox, goy, goz)); // antidote
			// dropDebugItem(1831, 0, new GeoLocation(gtx, gty, gtz)); // adena
			
			// reset direction flag
			diro = 0;
			dirt = 0;
			
			// calculate next point coordinates
			int e2 = 2 * d;
			if (e2 > -dy && e2 < dx)
			{
				// calculate next point XY coordinates
				d -= dy;
				d += dx;
				nox += sx;
				ntx -= sx;
				noy += sy;
				nty -= sy;
				diro |= diroxy;
				dirt |= dirtxy;
			}
			else if (e2 > -dy)
			{
				// calculate next point X coordinate
				d -= dy;
				nox += sx;
				ntx -= sx;
				diro |= dirox;
				dirt |= dirtx;
			}
			else if (e2 < dx)
			{
				// calculate next point Y coordinate
				d += dx;
				noy += sy;
				nty -= sy;
				diro |= diroy;
				dirt |= dirty;
			}
			
			{
				// get block of the next cell
				block = getBlock(nox, noy);
				
				// get index of particular layer, based on movement conditions
				if ((nsweo & diro) == 0)
					index = block.getIndexAbove(nox, noy, goz - GeoStructure.CELL_IGNORE_HEIGHT);
				else
					index = block.getIndexBelow(nox, noy, goz + GeoStructure.CELL_IGNORE_HEIGHT);
				
				// layer does not exist, return
				if (index == -1)
					return false;
				
				// get layer and next line of sight Z coordinate
				goz = block.getHeight(index);
				losoz += dz;
				
				// perform line of sight check, return when fails
				if ((goz - losoz) > Config.MAX_OBSTACLE_HEIGHT)
					return false;
				
				// get layer nswe
				nsweo = block.getNswe(index);
			}
			{
				// get block of the next cell
				block = getBlock(ntx, nty);
				
				// get index of particular layer, based on movement conditions
				if ((nswet & dirt) == 0)
					index = block.getIndexAbove(ntx, nty, gtz - GeoStructure.CELL_IGNORE_HEIGHT);
				else
					index = block.getIndexBelow(ntx, nty, gtz + GeoStructure.CELL_IGNORE_HEIGHT);
				
				// layer does not exist, return
				if (index == -1)
					return false;
				
				// get layer and next line of sight Z coordinate
				gtz = block.getHeight(index);
				lostz -= dz;
				
				// perform line of sight check, return when fails
				if ((gtz - lostz) > Config.MAX_OBSTACLE_HEIGHT)
					return false;
				
				// get layer nswe
				nswet = block.getNswe(index);
			}
			
			// update coords
			gox = nox;
			goy = noy;
			gtx = ntx;
			gty = nty;
		}
		
		// when iteration is completed, compare final Z coordinates
		return Math.abs(goz - gtz) < GeoStructure.CELL_HEIGHT * 4;
	}
	
	/**
	 * Simple check for origin to target visibility.<br>
	 * Geodata without {@link IGeoObject} are taken in consideration.<br>
	 * NOTE: When two doors close between each other and the LoS check of one doors is performed through another door, result will not be accurate (the other door are skipped).
	 * @param gox : origin X geodata coordinate
	 * @param goy : origin Y geodata coordinate
	 * @param goz : origin Z geodata coordinate
	 * @param oheight : origin height (if instance of {@link Creature})
	 * @param gtx : target X geodata coordinate
	 * @param gty : target Y geodata coordinate
	 * @param gtz : target Z geodata coordinate
	 * @param theight : target height (if instance of {@link Creature} or {@link Door})
	 * @return {@code boolean} : True, when target can be seen.
	 */
	protected final boolean checkSeeOriginal(int gox, int goy, int goz, double oheight, int gtx, int gty, int gtz, double theight)
	{
		// get line of sight Z coordinates
		double losoz = goz + oheight * Config.PART_OF_CHARACTER_HEIGHT / 100;
		double lostz = gtz + theight * Config.PART_OF_CHARACTER_HEIGHT / 100;
		
		// get X delta and signum
		final int dx = Math.abs(gtx - gox);
		final int sx = gox < gtx ? 1 : -1;
		final byte dirox = sx > 0 ? GeoStructure.CELL_FLAG_E : GeoStructure.CELL_FLAG_W;
		final byte dirtx = sx > 0 ? GeoStructure.CELL_FLAG_W : GeoStructure.CELL_FLAG_E;
		
		// get Y delta and signum
		final int dy = Math.abs(gty - goy);
		final int sy = goy < gty ? 1 : -1;
		final byte diroy = sy > 0 ? GeoStructure.CELL_FLAG_S : GeoStructure.CELL_FLAG_N;
		final byte dirty = sy > 0 ? GeoStructure.CELL_FLAG_N : GeoStructure.CELL_FLAG_S;
		
		// get Z delta
		final int dm = Math.max(dx, dy);
		final double dz = (lostz - losoz) / dm;
		
		// get direction flag for diagonal movement
		final byte diroxy = getDirXY(dirox, diroy);
		final byte dirtxy = getDirXY(dirtx, dirty);
		
		// delta, determines axis to move on (+..X axis, -..Y axis)
		int d = dx - dy;
		
		// NSWE direction of movement
		byte diro;
		byte dirt;
		
		// clearDebugItems();
		// dropDebugItem(728, 0, new GeoLocation(gox, goy, goz)); // blue potion
		// dropDebugItem(728, 0, new GeoLocation(gtx, gty, gtz)); // blue potion
		
		// initialize node values
		int nox = gox;
		int noy = goy;
		int ntx = gtx;
		int nty = gty;
		byte nsweo = getNsweNearestOriginal(gox, goy, goz);
		byte nswet = getNsweNearestOriginal(gtx, gty, gtz);
		
		// loop
		ABlock block;
		int index;
		for (int i = 0; i < (dm + 1) / 2; i++)
		{
			// dropDebugItem(57, 0, new GeoLocation(gox, goy, goz)); // antidote
			// dropDebugItem(1831, 0, new GeoLocation(gtx, gty, gtz)); // adena
			
			// reset direction flag
			diro = 0;
			dirt = 0;
			
			// calculate next point coordinates
			int e2 = 2 * d;
			if (e2 > -dy && e2 < dx)
			{
				// calculate next point XY coordinates
				d -= dy;
				d += dx;
				nox += sx;
				ntx -= sx;
				noy += sy;
				nty -= sy;
				diro |= diroxy;
				dirt |= dirtxy;
			}
			else if (e2 > -dy)
			{
				// calculate next point X coordinate
				d -= dy;
				nox += sx;
				ntx -= sx;
				diro |= dirox;
				dirt |= dirtx;
			}
			else if (e2 < dx)
			{
				// calculate next point Y coordinate
				d += dx;
				noy += sy;
				nty -= sy;
				diro |= diroy;
				dirt |= dirty;
			}
			
			{
				// get block of the next cell
				block = getBlock(nox, noy);
				
				// get index of particular layer, based on movement conditions
				if ((nsweo & diro) == 0)
					index = block.getIndexAboveOriginal(nox, noy, goz - GeoStructure.CELL_IGNORE_HEIGHT);
				else
					index = block.getIndexBelowOriginal(nox, noy, goz + GeoStructure.CELL_IGNORE_HEIGHT);
				
				// layer does not exist, return
				if (index == -1)
					return false;
				
				// get layer and next line of sight Z coordinate
				goz = block.getHeightOriginal(index);
				losoz += dz;
				
				// perform line of sight check, return when fails
				if ((goz - losoz) > Config.MAX_OBSTACLE_HEIGHT)
					return false;
				
				// get layer nswe
				nsweo = block.getNsweOriginal(index);
			}
			{
				// get block of the next cell
				block = getBlock(ntx, nty);
				
				// get index of particular layer, based on movement conditions
				if ((nswet & dirt) == 0)
					index = block.getIndexAboveOriginal(ntx, nty, gtz - GeoStructure.CELL_IGNORE_HEIGHT);
				else
					index = block.getIndexBelowOriginal(ntx, nty, gtz + GeoStructure.CELL_IGNORE_HEIGHT);
				
				// layer does not exist, return
				if (index == -1)
					return false;
				
				// get layer and next line of sight Z coordinate
				gtz = block.getHeightOriginal(index);
				lostz -= dz;
				
				// perform line of sight check, return when fails
				if ((gtz - lostz) > Config.MAX_OBSTACLE_HEIGHT)
					return false;
				
				// get layer nswe
				nswet = block.getNsweOriginal(index);
			}
			
			// update coords
			gox = nox;
			goy = noy;
			gtx = ntx;
			gty = nty;
		}
		
		// when iteration is completed, compare final Z coordinates
		return Math.abs(goz - gtz) < GeoStructure.CELL_HEIGHT * 4;
	}
	
	/**
	 * Check movement from coordinates to coordinates.
	 * @param ox : origin X coordinate
	 * @param oy : origin Y coordinate
	 * @param oz : origin Z coordinate
	 * @param tx : target X coordinate
	 * @param ty : target Y coordinate
	 * @param tz : target Z coordinate
	 * @return {code boolean} : True if target coordinates are reachable from origin coordinates
	 */
	public final boolean canMoveToTarget(int ox, int oy, int oz, int tx, int ty, int tz)
	{
		// get origin and check existing geo coordinates
		final int gox = getGeoX(ox);
		final int goy = getGeoY(oy);
		if (!hasGeoPos(gox, goy))
			return true;
		
		final short goz = getHeightNearest(gox, goy, oz);
		
		// get target and check existing geo coordinates
		final int gtx = getGeoX(tx);
		final int gty = getGeoY(ty);
		if (!hasGeoPos(gtx, gty))
			return true;
		
		final short gtz = getHeightNearest(gtx, gty, tz);
		
		// target coordinates reached
		if (gox == gtx && goy == gty && goz == gtz)
			return true;
		
		// perform geodata check
		GeoLocation loc = checkMove(gox, goy, goz, gtx, gty, gtz);
		return loc.getGeoX() == gtx && loc.getGeoY() == gty;
	}
	
	/**
	 * Check movement from origin to target. Returns last available point in the checked path.
	 * @param ox : origin X coordinate
	 * @param oy : origin Y coordinate
	 * @param oz : origin Z coordinate
	 * @param tx : target X coordinate
	 * @param ty : target Y coordinate
	 * @param tz : target Z coordinate
	 * @return {@link Location} : Last point where object can walk (just before wall)
	 */
	public final Location canMoveToTargetLoc(int ox, int oy, int oz, int tx, int ty, int tz)
	{
		// get origin and check existing geo coordinates
		final int gox = getGeoX(ox);
		final int goy = getGeoY(oy);
		if (!hasGeoPos(gox, goy))
			return new Location(tx, ty, tz);
		
		final short goz = getHeightNearest(gox, goy, oz);
		
		// get target and check existing geo coordinates
		final int gtx = getGeoX(tx);
		final int gty = getGeoY(ty);
		if (!hasGeoPos(gtx, gty))
			return new Location(tx, ty, tz);
		
		final short gtz = getHeightNearest(gtx, gty, tz);
		
		// target coordinates reached
		if (gox == gtx && goy == gty && goz == gtz)
			return new Location(tx, ty, tz);
		
		// perform geodata check
		return checkMove(gox, goy, goz, gtx, gty, gtz);
	}
	
	/**
	 * With this method you can check if a position is visible or can be reached by beeline movement.<br>
	 * Target X and Y reachable and Z is on same floor:
	 * <ul>
	 * <li>Location of the target with corrected Z value from geodata.</li>
	 * </ul>
	 * Target X and Y reachable but Z is on another floor:
	 * <ul>
	 * <li>Location of the origin with corrected Z value from geodata.</li>
	 * </ul>
	 * Target X and Y not reachable:
	 * <ul>
	 * <li>Last accessible location in destination to target.</li>
	 * </ul>
	 * @param gox : origin X geodata coordinate
	 * @param goy : origin Y geodata coordinate
	 * @param goz : origin Z geodata coordinate
	 * @param gtx : target X geodata coordinate
	 * @param gty : target Y geodata coordinate
	 * @param gtz : target Z geodata coordinate
	 * @return {@link GeoLocation} : The last allowed point of movement.
	 */
	protected final GeoLocation checkMove(int gox, int goy, int goz, int gtx, int gty, int gtz)
	{
		// get X delta, signum and direction flag
		final int dx = Math.abs(gtx - gox);
		final int sx = gox < gtx ? 1 : -1;
		final byte dirX = sx > 0 ? GeoStructure.CELL_FLAG_E : GeoStructure.CELL_FLAG_W;
		
		// get Y delta, signum and direction flag
		final int dy = Math.abs(gty - goy);
		final int sy = goy < gty ? 1 : -1;
		final byte dirY = sy > 0 ? GeoStructure.CELL_FLAG_S : GeoStructure.CELL_FLAG_N;
		
		// get direction flag for diagonal movement
		final byte dirXY = getDirXY(dirX, dirY);
		
		// delta, determines axis to move on (+..X axis, -..Y axis)
		int d = dx - dy;
		
		// NSWE direction of movement
		byte direction;
		
		// load pointer coordinates
		int gpx = gox;
		int gpy = goy;
		int gpz = goz;
		
		// load next pointer
		int nx = gpx;
		int ny = gpy;
		
		// loop
		do
		{
			direction = 0;
			
			// calculate next point coordinates
			int e2 = 2 * d;
			if (e2 > -dy && e2 < dx)
			{
				d -= dy;
				d += dx;
				nx += sx;
				ny += sy;
				direction |= dirXY;
			}
			else if (e2 > -dy)
			{
				d -= dy;
				nx += sx;
				direction |= dirX;
			}
			else if (e2 < dx)
			{
				d += dx;
				ny += sy;
				direction |= dirY;
			}
			
			// obstacle found, return
			if ((getNsweNearest(gpx, gpy, gpz) & direction) == 0)
				return new GeoLocation(gpx, gpy, gpz);
			
			// update pointer coordinates
			gpx = nx;
			gpy = ny;
			gpz = getHeightNearest(nx, ny, gpz);
			
			// target coordinates reached
			if (gpx == gtx && gpy == gty)
			{
				if (gpz == gtz)
				{
					// path found, Z coordinates are okay, return target point
					return new GeoLocation(gtx, gty, gtz);
				}
				
				// path found, Z coordinates are not okay, return origin point
				return new GeoLocation(gox, goy, goz);
			}
		}
		while (true);
	}
	
	/**
	 * Returns diagonal NSWE flag format of combined two NSWE flags.
	 * @param dirX : X direction NSWE flag
	 * @param dirY : Y direction NSWE flag
	 * @return byte : NSWE flag of combined direction
	 */
	private static final byte getDirXY(byte dirX, byte dirY)
	{
		// check axis directions
		if (dirY == GeoStructure.CELL_FLAG_N)
		{
			if (dirX == GeoStructure.CELL_FLAG_W)
				return GeoStructure.CELL_FLAG_NW;
			
			return GeoStructure.CELL_FLAG_NE;
		}
		
		if (dirX == GeoStructure.CELL_FLAG_W)
			return GeoStructure.CELL_FLAG_SW;
		
		return GeoStructure.CELL_FLAG_SE;
	}
	
	/**
	 * Returns the list of location objects as a result of complete path calculation.
	 * @param ox : origin x
	 * @param oy : origin y
	 * @param oz : origin z
	 * @param tx : target x
	 * @param ty : target y
	 * @param tz : target z
	 * @param playable : moving object is playable?
	 * @return {@code List<Location>} : complete path from nodes
	 */
	public List<Location> findPath(int ox, int oy, int oz, int tx, int ty, int tz, boolean playable)
	{
		// get origin and check existing geo coords
		int gox = getGeoX(ox);
		int goy = getGeoY(oy);
		if (!hasGeoPos(gox, goy))
			return null;
		
		short goz = getHeightNearest(gox, goy, oz);
		
		// get target and check existing geo coords
		int gtx = getGeoX(tx);
		int gty = getGeoY(ty);
		if (!hasGeoPos(gtx, gty))
			return null;
		
		short gtz = getHeightNearest(gtx, gty, tz);
		
		// Prepare buffer for pathfinding calculations
		NodeBuffer buffer = getBuffer(64 + (2 * Math.max(Math.abs(gox - gtx), Math.abs(goy - gty))), playable);
		if (buffer == null)
			return null;
		
		// clean debug path
		boolean debug = playable && Config.DEBUG_PATH;
		if (debug)
			clearDebugItems();
		
		// find path
		List<Location> path = null;
		try
		{
			Node result = buffer.findPath(gox, goy, goz, gtx, gty, gtz);
			
			if (result == null)
			{
				_findFails++;
				return null;
			}
			
			if (debug)
			{
				// path origin
				dropDebugItem(728, 0, new GeoLocation(gox, goy, goz)); // blue potion
				
				// path
				for (Node n : buffer.debugPath())
				{
					if (n.getCost() < 0)
						dropDebugItem(1831, (int) (-n.getCost() * 10), n.getLoc()); // antidote
					else
						dropDebugItem(57, (int) (n.getCost() * 10), n.getLoc()); // adena
				}
			}
			
			path = constructPath(result);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to generate a path.", e);
			
			_findFails++;
			return null;
		}
		finally
		{
			buffer.free();
			_findSuccess++;
		}
		
		// check path
		if (path.size() < 3)
			return path;
		
		// log data
		long timeStamp = System.currentTimeMillis();
		_postFilterUses++;
		if (playable)
			_postFilterPlayableUses++;
		
		// get path list iterator
		ListIterator<Location> point = path.listIterator();
		
		// get node A (origin)
		int nodeAx = gox;
		int nodeAy = goy;
		short nodeAz = goz;
		
		// get node B
		GeoLocation nodeB = (GeoLocation) point.next();
		
		// iterate thought the path to optimize it
		while (point.hasNext())
		{
			// get node C
			GeoLocation nodeC = (GeoLocation) path.get(point.nextIndex());
			
			// check movement from node A to node C
			GeoLocation loc = checkMove(nodeAx, nodeAy, nodeAz, nodeC.getGeoX(), nodeC.getGeoY(), nodeC.getZ());
			if (loc.getGeoX() == nodeC.getGeoX() && loc.getGeoY() == nodeC.getGeoY())
			{
				// can move from node A to node C
				
				// remove node B
				point.remove();
				
				// show skipped nodes
				if (debug)
					dropDebugItem(735, 0, nodeB); // green potion
			}
			else
			{
				// can not move from node A to node C
				
				// set node A (node B is part of path, update A coordinates)
				nodeAx = nodeB.getGeoX();
				nodeAy = nodeB.getGeoY();
				nodeAz = (short) nodeB.getZ();
			}
			
			// set node B
			nodeB = (GeoLocation) point.next();
		}
		
		// show final path
		if (debug)
		{
			for (Location node : path)
				dropDebugItem(65, 0, node); // red potion
		}
		
		// log data
		_postFilterElapsed += System.currentTimeMillis() - timeStamp;
		
		return path;
	}
	
	/**
	 * Return pathfinding statistics, useful for getting information about pathfinding status.
	 * @return {@code List<String>} : stats
	 */
	public List<String> getStat()
	{
		List<String> list = new ArrayList<>();
		
		for (BufferHolder buffer : _buffers)
			list.add(buffer.toString());
		
		list.add("Use: playable=" + String.valueOf(_postFilterPlayableUses) + " non-playable=" + String.valueOf(_postFilterUses - _postFilterPlayableUses));
		
		if (_postFilterUses > 0)
			list.add("Time (ms): total=" + String.valueOf(_postFilterElapsed) + " avg=" + String.format("%1.2f", (double) _postFilterElapsed / _postFilterUses));
		
		list.add("Pathfind: success=" + String.valueOf(_findSuccess) + ", fail=" + String.valueOf(_findFails));
		
		return list;
	}
	
	// MISC
	
	/**
	 * Record a geodata bug.
	 * @param loc : Location of the geodata bug.
	 * @param comment : Short commentary.
	 * @return boolean : True, when bug was successfully recorded.
	 */
	public final boolean addGeoBug(Location loc, String comment)
	{
		int gox = getGeoX(loc.getX());
		int goy = getGeoY(loc.getY());
		int goz = loc.getZ();
		int rx = gox / GeoStructure.REGION_CELLS_X + World.TILE_X_MIN;
		int ry = goy / GeoStructure.REGION_CELLS_Y + World.TILE_Y_MIN;
		int bx = (gox / GeoStructure.BLOCK_CELLS_X) % GeoStructure.REGION_BLOCKS_X;
		int by = (goy / GeoStructure.BLOCK_CELLS_Y) % GeoStructure.REGION_BLOCKS_Y;
		int cx = gox % GeoStructure.BLOCK_CELLS_X;
		int cy = goy % GeoStructure.BLOCK_CELLS_Y;
		
		try
		{
			_geoBugReports.printf(GEO_BUG, rx, ry, bx, by, cx, cy, goz, comment.replace(";", ":"));
			return true;
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't save new entry to \"geo_bugs.txt\" file.", e);
			return false;
		}
	}
	
	/**
	 * Add new item to drop list for debug purpose.
	 * @param id : Item id
	 * @param count : Item count
	 * @param loc : Item location
	 */
	public final void dropDebugItem(int id, int count, Location loc)
	{
		final ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), id);
		item.setCount(count);
		item.spawnMe(loc);
		
		_debugItems.add(item);
	}
	
	/**
	 * Clear item drop list for debugging paths.
	 */
	public final void clearDebugItems()
	{
		for (ItemInstance item : _debugItems)
			item.decayMe();
		
		_debugItems.clear();
	}
	
	/**
	 * NodeBuffer container with specified size and count of separate buffers.
	 */
	private static final class BufferHolder
	{
		final int _size;
		final int _count;
		ArrayList<NodeBuffer> _buffer;
		
		// statistics
		int _playableUses = 0;
		int _uses = 0;
		int _playableOverflows = 0;
		int _overflows = 0;
		long _elapsed = 0;
		
		public BufferHolder(int size, int count)
		{
			_size = size;
			_count = count;
			_buffer = new ArrayList<>(count);
			
			for (int i = 0; i < count; i++)
				_buffer.add(new NodeBuffer(size));
		}
		
		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder(100);
			
			StringUtil.append(sb, "Buffer ", String.valueOf(_size), "x", String.valueOf(_size), ": count=", String.valueOf(_count), " uses=", String.valueOf(_playableUses), "/", String.valueOf(_uses));
			
			if (_uses > 0)
				StringUtil.append(sb, " total/avg(ms)=", String.valueOf(_elapsed), "/", String.format("%1.2f", (double) _elapsed / _uses));
			
			StringUtil.append(sb, " ovf=", String.valueOf(_playableOverflows), "/", String.valueOf(_overflows));
			
			return sb.toString();
		}
	}
	
	/**
	 * Returns the instance of the {@link GeoEngine}.
	 * @return {@link GeoEngine} : The instance.
	 */
	public static final GeoEngine getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final GeoEngine INSTANCE = new GeoEngine();
	}
}
package dev.l2j.tesla.gameserver.geoengine.geodata;

import java.util.LinkedList;
import java.util.List;

public final class BlockMultilayerDynamic extends BlockMultilayer implements IBlockDynamic
{
	private final int _bx;
	private final int _by;
	private final byte[] _original;
	private final List<IGeoObject> _objects;
	
	/**
	 * Creates {@link BlockMultilayerDynamic}.
	 * @param bx : Block X coordinate.
	 * @param by : Block Y coordinate.
	 * @param block : The original MultilayerBlock to create a dynamic version from.
	 */
	public BlockMultilayerDynamic(int bx, int by, BlockMultilayer block)
	{
		// move buffer from ComplexBlock object to this object
		_buffer = block._buffer;
		block._buffer = null;
		
		// get block coordinates
		_bx = bx;
		_by = by;
		
		// create copy for dynamic implementation
		_original = new byte[_buffer.length];
		System.arraycopy(_buffer, 0, _original, 0, _buffer.length);
		
		// create list for geo objects
		_objects = new LinkedList<>();
	}
	
	@Override
	public short getHeightNearestOriginal(int geoX, int geoY, int worldZ)
	{
		// get cell index
		final int index = getIndexNearestOriginal(geoX, geoY, worldZ);
		
		// get height
		return (short) (_original[index + 1] & 0x00FF | _original[index + 2] << 8);
	}
	
	@Override
	public byte getNsweNearestOriginal(int geoX, int geoY, int worldZ)
	{
		// get cell index
		final int index = getIndexNearestOriginal(geoX, geoY, worldZ);
		
		// get nswe
		return _original[index];
	}
	
	private final int getIndexNearestOriginal(int geoX, int geoY, int worldZ)
	{
		// move index to the cell given by coordinates
		int index = 0;
		for (int i = 0; i < (geoX % GeoStructure.BLOCK_CELLS_X) * GeoStructure.BLOCK_CELLS_Y + (geoY % GeoStructure.BLOCK_CELLS_Y); i++)
		{
			// move index by amount of layers for this cell
			index += _original[index] * 3 + 1;
		}
		
		// get layers count and shift to first layer data (first from bottom)
		byte layers = _original[index++];
		
		// loop though all cell layers, find closest layer
		int limit = Integer.MAX_VALUE;
		while (layers-- > 0)
		{
			// get layer height
			final int height = _original[index + 1] & 0x00FF | _original[index + 2] << 8;
			
			// get Z distance and compare with limit
			// note: When 2 layers have same distance to worldZ (worldZ is in the middle of them):
			// > returns bottom layer
			// >= returns upper layer
			final int distance = Math.abs(height - worldZ);
			if (distance > limit)
				break;
			
			// update limit and move to next layer
			limit = distance;
			index += 3;
		}
		
		// return layer index
		return index - 3;
	}
	
	@Override
	public final int getIndexAboveOriginal(int geoX, int geoY, int worldZ)
	{
		// move index to the cell given by coordinates
		int index = 0;
		for (int i = 0; i < (geoX % GeoStructure.BLOCK_CELLS_X) * GeoStructure.BLOCK_CELLS_Y + (geoY % GeoStructure.BLOCK_CELLS_Y); i++)
		{
			// move index by amount of layers for this cell
			index += _original[index] * 3 + 1;
		}
		
		// get layers count and shift to last layer data (first from bottom)
		byte layers = _original[index++];
		index += (layers - 1) * 3;
		
		// loop though all layers, find first layer above worldZ
		while (layers-- > 0)
		{
			// get layer height
			final int height = _original[index + 1] & 0x00FF | _original[index + 2] << 8;
			
			// layer height is higher than worldZ, return layer index
			if (height > worldZ)
				return index;
			
			// move index to next layer
			index -= 3;
		}
		
		// none layer found
		return -1;
	}
	
	@Override
	public final int getIndexBelowOriginal(int geoX, int geoY, int worldZ)
	{
		// move index to the cell given by coordinates
		int index = 0;
		for (int i = 0; i < (geoX % GeoStructure.BLOCK_CELLS_X) * GeoStructure.BLOCK_CELLS_Y + (geoY % GeoStructure.BLOCK_CELLS_Y); i++)
		{
			// move index by amount of layers for this cell
			index += _original[index] * 3 + 1;
		}
		
		// get layers count and shift to first layer data (first from top)
		byte layers = _original[index++];
		
		// loop though all layers, find first layer below worldZ
		while (layers-- > 0)
		{
			// get layer height
			final int height = _original[index + 1] & 0x00FF | _original[index + 2] << 8;
			
			// layer height is lower than worldZ, return layer index
			if (height < worldZ)
				return index;
			
			// move index to next layer
			index += 3;
		}
		
		// none layer found
		return -1;
	}
	
	@Override
	public short getHeightOriginal(int index)
	{
		// get height
		return (short) (_original[index + 1] & 0x00FF | _original[index + 2] << 8);
	}
	
	@Override
	public byte getNsweOriginal(int index)
	{
		// get nswe
		return _original[index];
	}
	
	@Override
	public synchronized final void addGeoObject(IGeoObject object)
	{
		// add geo object, update block geodata when added
		if (_objects.add(object))
			update();
	}
	
	@Override
	public synchronized final void removeGeoObject(IGeoObject object)
	{
		// remove geo object, update block geodata when removed
		if (_objects.remove(object))
			update();
	}
	
	private final void update()
	{
		// copy original geodata, than apply changes
		System.arraycopy(_original, 0, _buffer, 0, _original.length);
		
		// get block geo coordinates
		final int minBX = _bx * GeoStructure.BLOCK_CELLS_X;
		final int minBY = _by * GeoStructure.BLOCK_CELLS_Y;
		final int maxBX = minBX + GeoStructure.BLOCK_CELLS_X;
		final int maxBY = minBY + GeoStructure.BLOCK_CELLS_Y;
		
		// for all objects
		for (IGeoObject object : _objects)
		{
			// get object geo coordinates and other object variables
			final int minOX = object.getGeoX();
			final int minOY = object.getGeoY();
			final int minOZ = object.getGeoZ();
			final int maxOZ = minOZ + object.getHeight();
			final byte[][] geoData = object.getObjectGeoData();
			
			// calculate min/max geo coordinates for iteration (intersection of block and object)
			final int minGX = Math.max(minBX, minOX);
			final int minGY = Math.max(minBY, minOY);
			final int maxGX = Math.min(maxBX, minOX + geoData.length);
			final int maxGY = Math.min(maxBY, minOY + geoData[0].length);
			
			// iterate over intersection of block and object
			for (int gx = minGX; gx < maxGX; gx++)
			{
				for (int gy = minGY; gy < maxGY; gy++)
				{
					// get object nswe
					final byte objNswe = geoData[gx - minOX][gy - minOY];
					
					// object contains no change of data in this cell, continue to next cell
					if (objNswe == 0xFF)
						continue;
					
					// get block index of this cell
					int ib = getIndexNearest(gx, gy, minOZ);
					
					// compare block data and original data, when height differs -> height was affected by other geo object
					// -> cell is inside an object -> no need to check/change it anymore (Z is lifted, nswe is 0)
					// compare is done in raw format (2 bytes) instead of conversion to short
					if (_buffer[ib + 1] != _original[ib + 1] || _buffer[ib + 2] != _original[ib + 2])
						continue;
					
					// so far cell is not inside of any object
					if (objNswe == 0)
					{
						// cell is inside of this object -> set nswe to 0 and lift Z up
						
						// set block nswe
						_buffer[ib] = 0;
						
						// calculate object height, limit to next layer
						int z = maxOZ;
						int i = getIndexAbove(gx, gy, minOZ);
						if (i != -1)
						{
							int az = getHeight(i);
							if (az <= maxOZ)
								z = az - GeoStructure.CELL_IGNORE_HEIGHT;
						}
						
						// set block Z to object height
						_buffer[ib + 1] = (byte) (z & 0x00FF);
						_buffer[ib + 2] = (byte) (z >> 8);
					}
					else
					{
						// cell is outside of this object -> update nswe
						
						// height different is too high (trying to update another layer), skip
						short z = getHeight(ib);
						if (Math.abs(z - minOZ) > GeoStructure.CELL_IGNORE_HEIGHT)
							continue;
						
						// adjust block nswe according to the object nswe
						_buffer[ib] &= objNswe;
					}
				}
			}
		}
	}
}
package dev.l2j.tesla.gameserver.geoengine.geodata;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import dev.l2j.tesla.gameserver.enums.GeoType;

public class BlockFlat extends ABlock
{
	protected final short _height;
	protected byte _nswe;
	
	/**
	 * Creates FlatBlock.
	 * @param bb : Input byte buffer.
	 * @param format : GeoFormat specifying format of loaded data.
	 */
	public BlockFlat(ByteBuffer bb, GeoType format)
	{
		_height = bb.getShort();
		_nswe = format != GeoType.L2D ? 0x0F : (byte) (0xFF);
		
		if (format == GeoType.L2OFF)
			bb.getShort();
	}
	
	@Override
	public final boolean hasGeoPos()
	{
		return true;
	}
	
	@Override
	public final short getHeightNearest(int geoX, int geoY, int worldZ)
	{
		return _height;
	}
	
	@Override
	public final short getHeightNearestOriginal(int geoX, int geoY, int worldZ)
	{
		return _height;
	}
	
	@Override
	public final short getHeightAbove(int geoX, int geoY, int worldZ)
	{
		// check and return height
		return _height > worldZ ? _height : Short.MIN_VALUE;
	}
	
	@Override
	public final short getHeightBelow(int geoX, int geoY, int worldZ)
	{
		// check and return height
		return _height < worldZ ? _height : Short.MAX_VALUE;
	}
	
	@Override
	public final byte getNsweNearest(int geoX, int geoY, int worldZ)
	{
		return _nswe;
	}
	
	@Override
	public final byte getNsweNearestOriginal(int geoX, int geoY, int worldZ)
	{
		return _nswe;
	}
	
	@Override
	public final byte getNsweAbove(int geoX, int geoY, int worldZ)
	{
		// check height and return nswe
		return _height > worldZ ? _nswe : 0;
	}
	
	@Override
	public final byte getNsweBelow(int geoX, int geoY, int worldZ)
	{
		// check height and return nswe
		return _height < worldZ ? _nswe : 0;
	}
	
	@Override
	public final int getIndexNearest(int geoX, int geoY, int worldZ)
	{
		return 0;
	}
	
	@Override
	public final int getIndexAbove(int geoX, int geoY, int worldZ)
	{
		// check height and return index
		return _height > worldZ ? 0 : -1;
	}
	
	@Override
	public final int getIndexAboveOriginal(int geoX, int geoY, int worldZ)
	{
		return getIndexAbove(geoX, geoY, worldZ);
	}
	
	@Override
	public final int getIndexBelow(int geoX, int geoY, int worldZ)
	{
		// check height and return index
		return _height < worldZ ? 0 : -1;
	}
	
	@Override
	public final int getIndexBelowOriginal(int geoX, int geoY, int worldZ)
	{
		return getIndexBelow(geoX, geoY, worldZ);
	}
	
	@Override
	public final short getHeight(int index)
	{
		return _height;
	}
	
	@Override
	public final short getHeightOriginal(int index)
	{
		return _height;
	}
	
	@Override
	public final byte getNswe(int index)
	{
		return _nswe;
	}
	
	@Override
	public final byte getNsweOriginal(int index)
	{
		return _nswe;
	}
	
	@Override
	public final void setNswe(int index, byte nswe)
	{
		_nswe = nswe;
	}
	
	@Override
	public final void saveBlock(BufferedOutputStream stream) throws IOException
	{
		// write block type
		stream.write(GeoStructure.TYPE_FLAT_L2D);
		
		// write height
		stream.write((byte) (_height & 0x00FF));
		stream.write((byte) (_height >> 8));
	}
}
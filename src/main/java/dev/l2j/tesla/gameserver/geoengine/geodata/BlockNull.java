package dev.l2j.tesla.gameserver.geoengine.geodata;

import java.io.BufferedOutputStream;

public class BlockNull extends ABlock
{
	private byte _nswe;
	
	public BlockNull()
	{
		_nswe = (byte) 0xFF;
	}
	
	@Override
	public final boolean hasGeoPos()
	{
		return false;
	}
	
	@Override
	public final short getHeightNearest(int geoX, int geoY, int worldZ)
	{
		return (short) worldZ;
	}
	
	@Override
	public final short getHeightNearestOriginal(int geoX, int geoY, int worldZ)
	{
		return (short) worldZ;
	}
	
	@Override
	public final short getHeightAbove(int geoX, int geoY, int worldZ)
	{
		return (short) worldZ;
	}
	
	@Override
	public final short getHeightBelow(int geoX, int geoY, int worldZ)
	{
		return (short) worldZ;
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
		return _nswe;
	}
	
	@Override
	public final byte getNsweBelow(int geoX, int geoY, int worldZ)
	{
		return _nswe;
	}
	
	@Override
	public final int getIndexNearest(int geoX, int geoY, int worldZ)
	{
		return 0;
	}
	
	@Override
	public final int getIndexAbove(int geoX, int geoY, int worldZ)
	{
		return 0;
	}
	
	@Override
	public final int getIndexAboveOriginal(int geoX, int geoY, int worldZ)
	{
		return 0;
	}
	
	@Override
	public final int getIndexBelow(int geoX, int geoY, int worldZ)
	{
		return 0;
	}
	
	@Override
	public final int getIndexBelowOriginal(int geoX, int geoY, int worldZ)
	{
		return 0;
	}
	
	@Override
	public final short getHeight(int index)
	{
		return 0;
	}
	
	@Override
	public final short getHeightOriginal(int index)
	{
		return 0;
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
	}
	
	@Override
	public final void saveBlock(BufferedOutputStream stream)
	{
	}
}
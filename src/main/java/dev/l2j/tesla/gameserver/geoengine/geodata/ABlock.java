package dev.l2j.tesla.gameserver.geoengine.geodata;

import java.io.BufferedOutputStream;
import java.io.IOException;

public abstract class ABlock
{
	/**
	 * Checks the block for having geodata.
	 * @return boolean : True, when block has geodata (Flat, Complex, Multilayer).
	 */
	public abstract boolean hasGeoPos();
	
	/**
	 * Returns the height of cell, which is closest to given coordinates.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return short : Cell geodata Z coordinate, nearest to given coordinates.
	 */
	public abstract short getHeightNearest(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns the height of cell, which is closest to given coordinates.<br>
	 * Geodata without {@link IGeoObject} are taken in consideration.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return short : Cell geodata Z coordinate, nearest to given coordinates.
	 */
	public abstract short getHeightNearestOriginal(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns the height of cell, which is first above given coordinates.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return short : Cell geodata Z coordinate, above given coordinates.
	 */
	public abstract short getHeightAbove(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns the height of cell, which is first below given coordinates.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return short : Cell geodata Z coordinate, below given coordinates.
	 */
	public abstract short getHeightBelow(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns the NSWE flag byte of cell, which is closest to given coordinates.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return short : Cell NSWE flag byte, nearest to given coordinates.
	 */
	public abstract byte getNsweNearest(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns the NSWE flag byte of cell, which is closest to given coordinates.<br>
	 * Geodata without {@link IGeoObject} are taken in consideration.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return short : Cell NSWE flag byte, nearest to given coordinates.
	 */
	public abstract byte getNsweNearestOriginal(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns the NSWE flag byte of cell, which is first above given coordinates.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return short : Cell NSWE flag byte, nearest to given coordinates.
	 */
	public abstract byte getNsweAbove(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns the NSWE flag byte of cell, which is first below given coordinates.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return short : Cell NSWE flag byte, nearest to given coordinates.
	 */
	public abstract byte getNsweBelow(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns index to data of the cell, which is closes layer to given coordinates.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return {@code int} : Cell index.
	 */
	public abstract int getIndexNearest(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns index to data of the cell, which is first layer above given coordinates.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return {@code int} : Cell index. -1..when no layer available below given Z coordinate.
	 */
	public abstract int getIndexAbove(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns index to data of the cell, which is first layer above given coordinates.<br>
	 * Geodata without {@link IGeoObject} are taken in consideration.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return {@code int} : Cell index. -1..when no layer available below given Z coordinate.
	 */
	public abstract int getIndexAboveOriginal(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns index to data of the cell, which is first layer below given coordinates.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return {@code int} : Cell index. -1..when no layer available below given Z coordinate.
	 */
	public abstract int getIndexBelow(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns index to data of the cell, which is first layer below given coordinates.<br>
	 * Geodata without {@link IGeoObject} are taken in consideration.
	 * @param geoX : Cell geodata X coordinate.
	 * @param geoY : Cell geodata Y coordinate.
	 * @param worldZ : Cell world Z coordinate.
	 * @return {@code int} : Cell index. -1..when no layer available below given Z coordinate.
	 */
	public abstract int getIndexBelowOriginal(int geoX, int geoY, int worldZ);
	
	/**
	 * Returns the height of cell given by cell index.
	 * @param index : Index of the cell.
	 * @return short : Cell geodata Z coordinate, below given coordinates.
	 */
	public abstract short getHeight(int index);
	
	/**
	 * Returns the height of cell given by cell index.<br>
	 * Geodata without {@link IGeoObject} are taken in consideration.
	 * @param index : Index of the cell.
	 * @return short : Cell geodata Z coordinate, below given coordinates.
	 */
	public abstract short getHeightOriginal(int index);
	
	/**
	 * Returns the NSWE flag byte of cell given by cell index.
	 * @param index : Index of the cell.
	 * @return short : Cell geodata Z coordinate, below given coordinates.
	 */
	public abstract byte getNswe(int index);
	
	/**
	 * Returns the NSWE flag byte of cell given by cell index.<br>
	 * Geodata without {@link IGeoObject} are taken in consideration.
	 * @param index : Index of the cell.
	 * @return short : Cell geodata Z coordinate, below given coordinates.
	 */
	public abstract byte getNsweOriginal(int index);
	
	/**
	 * Sets the NSWE flag byte of cell given by cell index.
	 * @param index : Index of the cell.
	 * @param nswe : New NSWE flag byte.
	 */
	public abstract void setNswe(int index, byte nswe);
	
	/**
	 * Saves the block in L2D format to {@link BufferedOutputStream}. Used only for L2D geodata conversion.
	 * @param stream : The stream.
	 * @throws IOException : Can't save the block to steam.
	 */
	public abstract void saveBlock(BufferedOutputStream stream) throws IOException;
}
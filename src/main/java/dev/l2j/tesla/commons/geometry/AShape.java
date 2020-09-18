package dev.l2j.tesla.commons.geometry;

import dev.l2j.tesla.gameserver.model.location.Location;

/**
 * @author Hasha
 */
public abstract class AShape
{
	/**
	 * Returns size of the AShape floor projection.
	 * @return int : Size.
	 */
	public abstract int getSize();
	
	/**
	 * Returns surface area of the AShape.
	 * @return double : Surface area.
	 */
	public abstract double getArea();
	
	/**
	 * Returns enclosed volume of the AShape.
	 * @return double : Enclosed volume.
	 */
	public abstract double getVolume();
	
	/**
	 * Checks if given X, Y coordinates are laying inside the AShape.
	 * @param x : World X coordinates.
	 * @param y : World Y coordinates.
	 * @return boolean : True, when if coordinates are inside this AShape.
	 */
	public abstract boolean isInside(int x, int y);
	
	/**
	 * Checks if given X, Y, Z coordinates are laying inside the AShape.
	 * @param x : World X coordinates.
	 * @param y : World Y coordinates.
	 * @param z : World Z coordinates.
	 * @return boolean : True, when if coordinates are inside this AShape.
	 */
	public abstract boolean isInside(int x, int y, int z);
	
	/**
	 * Returns {@link Location} of random point inside AShape.<br>
	 * In case AShape is only in 2D space, Z is set as 0.
	 * @return {@link Location} : Random location inside AShape.
	 */
	public abstract Location getRandomLocation();
}
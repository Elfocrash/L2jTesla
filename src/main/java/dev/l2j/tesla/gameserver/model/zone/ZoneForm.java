package dev.l2j.tesla.gameserver.model.zone;

import dev.l2j.tesla.gameserver.idfactory.IdFactory;
import dev.l2j.tesla.gameserver.data.manager.ZoneManager;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;

/**
 * Abstract base class for any zone form.
 */
public abstract class ZoneForm
{
	protected static final int STEP = 50;
	
	public abstract boolean isInsideZone(int x, int y, int z);
	
	public abstract boolean intersectsRectangle(int x1, int x2, int y1, int y2);
	
	public abstract double getDistanceToZone(int x, int y);
	
	public abstract int getLowZ(); // Support for the ability to extract the z coordinates of zones.
	
	public abstract int getHighZ(); // New fishing patch makes use of that to get the Z for the hook
	
	public abstract void visualizeZone(int id, int z);
	
	protected boolean lineSegmentsIntersect(int ax1, int ay1, int ax2, int ay2, int bx1, int by1, int bx2, int by2)
	{
		return java.awt.geom.Line2D.linesIntersect(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2);
	}
	
	protected static final void dropDebugItem(int id, int x, int y, int z)
	{
		final ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), 57);
		item.setCount(id);
		item.spawnMe(x, y, z + 5);
		
		ZoneManager.getInstance().addDebugItem(item);
	}
}
package dev.l2j.tesla.commons.geometry;

import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.commons.random.Rnd;

/**
 * Tri-sided polygon in 3D, while having bottom and top area flat (in Z coordinate).<br>
 * It is <b>not</b> 3D oriented triangle.
 * @author Hasha
 */
public class Triangle3D extends Triangle
{
	// min and max Z coorinates
	private final int _minZ;
	private final int _maxZ;
	
	// total length of all sides
	private final double _length;
	
	/**
	 * Triangle constructor.
	 * @param A : Point A of the triangle.
	 * @param B : Point B of the triangle.
	 * @param C : Point C of the triangle.
	 */
	public Triangle3D(int[] A, int[] B, int[] C)
	{
		super(A, B, C);
		
		_minZ = Math.min(A[2], Math.min(B[2], C[2]));
		_maxZ = Math.max(A[2], Math.max(B[2], C[2]));
		
		final int CBx = _CAx - _BAx;
		final int CBy = _CAy - _BAy;
		_length = Math.sqrt(_BAx * _BAx + _BAy * _BAy) + Math.sqrt(_CAx * _CAx + _CAy * _CAy) + Math.sqrt(CBx * CBx + CBy * CBy);
	}
	
	@Override
	public double getArea()
	{
		return _size * 2 + _length * (_maxZ - _minZ);
	}
	
	@Override
	public double getVolume()
	{
		return _size * (_maxZ - _minZ);
	}
	
	@Override
	public final boolean isInside(int x, int y, int z)
	{
		if (z < _minZ || z > _maxZ)
			return false;
		
		return super.isInside(x, y, z);
	}
	
	@Override
	public final Location getRandomLocation()
	{
		// get relative length of AB and AC vectors
		double ba = Rnd.nextDouble();
		double ca = Rnd.nextDouble();
		
		// adjust length if too long
		if (ba + ca > 1)
		{
			ba = 1 - ba;
			ca = 1 - ca;
		}
		
		// calc coords (take A, add AB and AC)
		final int x = _Ax + (int) (ba * _BAx + ca * _CAx);
		final int y = _Ay + (int) (ba * _BAy + ca * _CAy);
		
		// return
		return new Location(x, y, Rnd.get(_minZ, _maxZ));
	}
}
package dev.l2j.tesla.gameserver.geoengine.geodata;

import dev.l2j.tesla.gameserver.model.World;

public final class GeoStructure
{
	// cells
	public static final byte CELL_FLAG_E = 1 << 0;
	public static final byte CELL_FLAG_W = 1 << 1;
	public static final byte CELL_FLAG_S = 1 << 2;
	public static final byte CELL_FLAG_N = 1 << 3;
	public static final byte CELL_FLAG_SE = 1 << 4;
	public static final byte CELL_FLAG_SW = 1 << 5;
	public static final byte CELL_FLAG_NE = 1 << 6;
	public static final byte CELL_FLAG_NW = (byte) (1 << 7);
	public static final byte CELL_FLAG_S_AND_E = CELL_FLAG_S | CELL_FLAG_E;
	public static final byte CELL_FLAG_S_AND_W = CELL_FLAG_S | CELL_FLAG_W;
	public static final byte CELL_FLAG_N_AND_E = CELL_FLAG_N | CELL_FLAG_E;
	public static final byte CELL_FLAG_N_AND_W = CELL_FLAG_N | CELL_FLAG_W;
	
	public static final int CELL_SIZE = 16;
	public static final int CELL_HEIGHT = 8;
	public static final int CELL_IGNORE_HEIGHT = CELL_HEIGHT * 6;
	
	// blocks
	public static final byte TYPE_FLAT_L2J_L2OFF = 0;
	public static final byte TYPE_FLAT_L2D = (byte) 0xD0;
	public static final byte TYPE_COMPLEX_L2J = 1;
	public static final byte TYPE_COMPLEX_L2OFF = 0x40;
	public static final byte TYPE_COMPLEX_L2D = (byte) 0xD1;
	public static final byte TYPE_MULTILAYER_L2J = 2;
	// public static final byte TYPE_MULTILAYER_L2OFF = 0x41; // officially not does exist, is anything above complex block (0x41 - 0xFFFF)
	public static final byte TYPE_MULTILAYER_L2D = (byte) 0xD2;
	
	public static final int BLOCK_CELLS_X = 8;
	public static final int BLOCK_CELLS_Y = 8;
	public static final int BLOCK_CELLS = BLOCK_CELLS_X * BLOCK_CELLS_Y;
	
	// regions
	public static final int REGION_BLOCKS_X = 256;
	public static final int REGION_BLOCKS_Y = 256;
	public static final int REGION_BLOCKS = REGION_BLOCKS_X * REGION_BLOCKS_Y;
	
	public static final int REGION_CELLS_X = REGION_BLOCKS_X * BLOCK_CELLS_X;
	public static final int REGION_CELLS_Y = REGION_BLOCKS_Y * BLOCK_CELLS_Y;
	
	// global geodata
	public static final int GEO_REGIONS_X = (World.TILE_X_MAX - World.TILE_X_MIN + 1);
	public static final int GEO_REGIONS_Y = (World.TILE_Y_MAX - World.TILE_Y_MIN + 1);
	
	public static final int GEO_BLOCKS_X = GEO_REGIONS_X * REGION_BLOCKS_X;
	public static final int GEO_BLOCKS_Y = GEO_REGIONS_Y * REGION_BLOCKS_Y;
	
	public static final int GEO_CELLS_X = GEO_BLOCKS_X * BLOCK_CELLS_X;
	public static final int GEO_CELLS_Y = GEO_BLOCKS_Y * BLOCK_CELLS_Y;
}
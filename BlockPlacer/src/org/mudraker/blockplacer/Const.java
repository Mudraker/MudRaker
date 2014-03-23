/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.blockplacer;

/**
 * BlockPlacer Mod internal constants to centralise definition across all mod code.
 * <p>Currently only used by core BlockPlacer module.</p>
 * 
 * <p>Pattern: Enforced Static Immutable Constants</p>
 * 
 * @author MudRaker 
 */
public class Const {
	private Const() {}	// Static: Prevent instantiation

	//******************************************************
	// Block sides and relative directions
	//******************************************************
	
	// Block Side Definitions - see also net.minecraft.util.Facing
	public static final int SIDE_BOTTOM = 0;	// y-1
	public static final int SIDE_TOP = 1;		// y+1
	public static final int SIDE_NORTH = 2;		// z-1
	public static final int SIDE_SOUTH = 3;		// z+1
	public static final int SIDE_WEST = 4;		// x-1
	public static final int SIDE_EAST = 5;		// x+1
	
	// Relative Directions based on player facing
	public static final int DIR_BELOW = 0;
	public static final int DIR_ABOVE = 1;
	public static final int DIR_FRONT = 2;
	public static final int DIR_BACK  = 3;
	public static final int DIR_LEFT  = 4;
	public static final int DIR_RIGHT = 5;
	
	/** Direction names, indexed by DIR_* constants */
	public static final String[] DIRECTION_NAME = new String[] {
		"Below", "Above", "Front", "Back", "Left", "Right",
	};
	
	/** 
	 * Convert block SIDE to 2D relative direction (front, back, left, right).
	 * <p>Usage: SIDE_TO_REL_DIR[playerFacing][placeSide]</p>
	 * Output is the equivalent relative direction.
	 * <p>playerFacing is as per getPlayerDirection() - NESW only.</p>
	 */
	public static final int[][] SIDE_TO_REL_DIR = new int[][] { // PlayerFacing, BlockSide
	//	 Bottom		 Top		North	   South	  West		East 	 <-- block side
		{}, // player looking DOWN
		{}, // player looking UP
		{DIR_BELOW, DIR_ABOVE, DIR_FRONT, DIR_BACK, DIR_LEFT, DIR_RIGHT}, // player facing NORTH
		{DIR_BELOW, DIR_ABOVE, DIR_BACK, DIR_FRONT, DIR_RIGHT, DIR_LEFT}, // player facing SOUTH
		{DIR_BELOW, DIR_ABOVE, DIR_RIGHT, DIR_LEFT, DIR_FRONT, DIR_BACK}, // player facing WEST
		{DIR_BELOW, DIR_ABOVE, DIR_LEFT, DIR_RIGHT, DIR_BACK, DIR_FRONT}, // player facing EAST
	};
	
	/** 
	 * Convert 2D relative direction (front, back, left, right) to block SIDE 
	 * <p>Usage: REL_DIR_TO_SIDE[playerFacing][relativeDirection]</p>
	 * Output is the equivalent block side.
	 * <p>playerFacing is as per getPlayerDirection() - NESW only.</p>
	 */
	public static final int[][] REL_DIR_TO_SIDE = new int[][] { // PlayerFacing, Relative Direction
	//	 Below		 Above		Front	   Back	  	Left		Right 	 <-- relative direction
		{}, // player looking DOWN
		{}, // player looking UP
		{SIDE_BOTTOM, SIDE_TOP, SIDE_NORTH,SIDE_SOUTH,SIDE_WEST, SIDE_EAST}, // player facing NORTH
		{SIDE_BOTTOM, SIDE_TOP, SIDE_SOUTH,SIDE_NORTH,SIDE_EAST, SIDE_WEST}, // player facing SOUTH
		{SIDE_BOTTOM, SIDE_TOP, SIDE_WEST, SIDE_EAST, SIDE_SOUTH, SIDE_NORTH}, // player facing WEST
		{SIDE_BOTTOM, SIDE_TOP, SIDE_EAST, SIDE_WEST, SIDE_NORTH, SIDE_SOUTH}, // player facing EAST
	};

	//******************************************************
	// Plane based rotations for ROTATE3D mode
	//******************************************************

	// Plane based rotation constants
	public static final int ROT_X_CLOCK = 0;
	public static final int ROT_X_ANTI 	= 1;
	public static final int ROT_Y_CLOCK	= 2;
	public static final int ROT_Y_ANTI	= 3;
	public static final int ROT_Z_CLOCK	= 4;
	public static final int ROT_Z_ANTI	= 5;
	
	/**
	 *  Base pitch angle used to split the player head rotation into pitch zones.
	 *  Level pitch is considered to be - PITCH_ANGLE to + PITCH_ANGLE.  
	 */
	public static final float PITCH_ANGLE = 30F;
	
	// Pitch zone constants
	public static final int PITCH_DOWN  = 0;
	public static final int PITCH_UP  	= 1;
	public static final int PITCH_LEVEL = 2;
	
	/** Pitch zone descriptions, indexed by PITCH_* constants */
	public static final String[] PITCH_NAME = new String[] {"Down", "Up", "Level"};
	
	/**
	 * Translates a rotation key into a specific plane rotation based on the player pitch
	 * and direction.
	 * <p>Usage: PLANE_TRANSLATE[pitch][rotateDirection][playerFacing]</p>
	 * Output is a plane based rotation constant - X/Y/Z Clockwise/Anti clockwise
	 * <p>Where pitch is the player pitch zone (as per playerPitch()),
	 * playerFacing is as per getPlayerDirection() - NESW only, and
	 * rotateDirection is a relative plane rotation key - vertical/horizontal clock/anti</p>
	 */
    public static final int[][][] PLANE_TRANSLATE = new int[][][] { // Pitch, Key, Direction
    	{//	Y-,	Y+		Z-			Z+			X-			X+   	<-- Looking Down  
    		{-1,-1,		ROT_X_ANTI,	ROT_X_CLOCK,ROT_Z_ANTI,	ROT_Z_CLOCK},	// ROT_VERT_CLOCK (up)
    		{-1,-1,		ROT_X_CLOCK,ROT_X_ANTI,	ROT_Z_CLOCK,ROT_Z_ANTI},	// ROT_VERT_ANTI (down)
    		{-1,-1,		ROT_Z_ANTI,	ROT_Z_CLOCK,ROT_X_CLOCK,ROT_X_ANTI},	// ROT_HORIZ_CLOCK (left)
    		{-1,-1,		ROT_Z_CLOCK,ROT_Z_ANTI,	ROT_X_ANTI,	ROT_X_CLOCK},	// ROT_HORIZ_ANTI (right)
    	},    
    	{//	Y-,	Y+		Z-			Z+			X-			X+   	<-- Looking UP  
    		{-1,-1,		ROT_X_ANTI, ROT_X_CLOCK,ROT_Z_ANTI,	ROT_Z_CLOCK}, 	// ROT_VERT_CLOCK (up)
    		{-1,-1,		ROT_X_CLOCK,ROT_X_ANTI, ROT_Z_CLOCK,ROT_Z_ANTI}, 	// ROT_VERT_ANTI (down)
    		{-1,-1,		ROT_Z_CLOCK,ROT_Z_ANTI,	ROT_X_ANTI,	ROT_X_CLOCK}, 	// ROT_HORIZ_CLOCK (left)
    		{-1,-1,		ROT_Z_ANTI,	ROT_Z_CLOCK,ROT_X_CLOCK,ROT_X_ANTI}, 	// ROT_HORIZ_ANTI (right)
    	},    
    	{//	Y-, Y+		Z-			Z+			X-			X+   	<-- Level Pitch  
    		{-1,-1,		ROT_X_ANTI,	ROT_X_CLOCK,ROT_Z_ANTI,	ROT_Z_CLOCK},	// ROT_VERT_CLOCK (up)
    		{-1,-1,		ROT_X_CLOCK,ROT_X_ANTI,	ROT_Z_CLOCK,ROT_Z_ANTI},	// ROT_VERT_ANTI (down)
    		{-1,-1,		ROT_Y_ANTI,	ROT_Y_ANTI,	ROT_Y_ANTI,	ROT_Y_ANTI},	// ROT_HORIZ_CLOCK (left)
    		{-1,-1,		ROT_Y_CLOCK,ROT_Y_CLOCK,ROT_Y_CLOCK,ROT_Y_CLOCK},	// ROT_HORIZ_ANTI (right)
    	},
	};
    
    /** 
     * Rotates in a specific plane from a given starting side
     * <p>Usage: PLANE_ROTATE[plane][side]</p>
     * Outputs the new block side after rotation, or -1 if the block side
     * is not on the plane of rotation.
     * <p>Where plane is the plane rotation constant (see {@link PLANE_TRANSLATE})
     * and side is the block side to rotate from.</p>  
     */
    public static final int[][] PLANE_ROTATE = new int[][] { // Plane, Block Side
  	//	Bottom		Top			North		South	  	West		East 	 <-- block side
    	{SIDE_NORTH,SIDE_SOUTH, SIDE_TOP, 	SIDE_BOTTOM,-1, 		-1			}, // ROT_X_CLOCK = bnts
    	{SIDE_SOUTH,SIDE_NORTH, SIDE_BOTTOM,SIDE_TOP,	-1, 		-1			}, // ROT_X_ANTI  = bstn
    	{-1,		-1,			SIDE_WEST, 	SIDE_EAST, 	SIDE_SOUTH,	SIDE_NORTH	}, // ROT_Y_CLOCK = nesw
    	{-1,		-1,			SIDE_EAST, 	SIDE_WEST, 	SIDE_NORTH, SIDE_SOUTH	}, // ROT_Y_ANTI  = nwse
    	{SIDE_WEST,	SIDE_EAST, 	-1, 		-1,			SIDE_TOP, 	SIDE_BOTTOM	}, // ROT_Z_CLOCK = tebw
    	{SIDE_EAST, SIDE_WEST,	-1, 		-1,			SIDE_BOTTOM,SIDE_TOP	}, // ROT_Z_ANTI  = etwb
    };
    
    /** 
     * Provides the default starting side for rotation based on player pitch zone
     * if rotating a side on the third axis (which is neither the horizontal nor
     * vertical rotation plane). Used when PLANE_ROTATE is -1 to set the initial
     * position on the plane of rotation.
     * <p>Usage: PLANE_DEFAULT_REL_DIR[pitch]</p>
     * Output is the relative direction for the initial side to rotate onto.
     * <p>Where pitch is the player pitch zone as per {@link playerPitch}</p>
     */
    public static final int[] PLANE_DEFAULT_REL_DIR = new int [] { // Pitch
    	DIR_BELOW, // PITCH_DOWN
    	DIR_ABOVE, // PITCH_UP
    	DIR_FRONT, // PITCH_LEVEL    	
    };

	//******************************************************
	// Forward / Backward multi-plane rotations
	//******************************************************

    /** 
     * Rotate forward or backward from current relative direction through all
     * six block sides.
     * <p>Usage: REL_DIR_ROTATE[forwardOrBack][relativeDirection]</p>
     * Outputs the new relative direction of the side to rotate to.
     * <p>Where forwardOrBack is 0 for forward & 1 for backwards, and
     * relativeDirection is the current side relative to the player facing.</p>
     */
    public static final int[][] REL_DIR_ROTATE = new int[][] { // fwdBack, Relative Direction
	//	 Below		Above		Front		Back		Left		Right 	 <-- relative direction
    	{DIR_RIGHT,	DIR_FRONT,	DIR_BELOW,	DIR_ABOVE,	DIR_BACK,	DIR_LEFT	}, // Forward
    	{DIR_FRONT,	DIR_BACK,	DIR_ABOVE,	DIR_LEFT,	DIR_RIGHT,	DIR_BELOW	}, // Backward
    };

	//******************************************************
	// 'Smart Start' preferred starting positions
	//******************************************************
    
    /**
     * 'Intelligent' default place side after activation. 
     * Provides up to three prioritised relative directions as the 'smart' starting
     * positions based on player pitch and relative direction.
     * <p>Usage: DEFAULT_PLACE [attempt#][pitch][relative direction]</p>
     * Outputs the preferred start relative direction or -1 if no more recommendations.
     * <p>Where attempt# is the priority 0-2,
     * pitch is the player pitch as per {@link playerPitch}, and
     * relative direction is the relative direction of the side the player is pointing at.</p>
     * The logic behind the first choice is basically this:
	 * If player is above & hits top (or bottom), choose forward.
	 * If player is above & hits side, choose below
	 * If player is above & hits back, choose front (& visa-versa)
	 * If player is below & hits bottom (or top), choose forward.
	 * If player is below & hits side, choose above
	 * If player is below & hits back, choose front (& visa-versa)
	 * If player is level & hits top or bottom, choose forward
	 * If player is level & hits back (or front), choose below
	 * If player is level & hits side, choose above
	 */
    public static final int[][][] DEFAULT_PLACE = new int[][][] { // attempt#, pitch, relative direction
    	//	 Below		Above		Front		Back		Left		Right 	 <-- relative direction
    	{	{DIR_FRONT,	DIR_FRONT,	DIR_BACK,	DIR_FRONT,	DIR_BELOW,	DIR_BELOW	}, // Pitch Down
    		{DIR_FRONT,	DIR_FRONT,	DIR_BACK,	DIR_ABOVE,	DIR_BELOW,	DIR_BELOW	}, // Pitch Up
    		{DIR_FRONT,	DIR_FRONT,	DIR_BELOW,	DIR_ABOVE,	DIR_ABOVE,	DIR_ABOVE	}, // Pitch Level
    	}, // second try
    	{	{-1,		DIR_BELOW,	DIR_BELOW,	DIR_BELOW,	DIR_FRONT,	DIR_FRONT	}, // Pitch Down
    		{DIR_ABOVE,	-1,			DIR_ABOVE,	DIR_FRONT,	DIR_FRONT,	DIR_FRONT	}, // Pitch Up
    		{DIR_ABOVE,	DIR_BELOW,	DIR_ABOVE,	DIR_BELOW,	DIR_BELOW,	DIR_BELOW	}, // Pitch Level
    	}, // third try
    	{	{-1,		DIR_RIGHT,	-1,			-1,			DIR_BACK,	DIR_BACK	}, // Pitch Down
    		{DIR_RIGHT,	-1,			DIR_RIGHT,	DIR_RIGHT,	DIR_BACK,	DIR_BACK	}, // Pitch Up
    		{DIR_BACK,	DIR_BACK,	DIR_BACK,	DIR_FRONT,	DIR_FRONT,	DIR_BACK	}, // Pitch Level
    	},
    };

	//******************************************************
	// Mouse control translations
	//******************************************************

    /**
     * Converts mouse movement direction to a relative direction.
     * <p>Usage MOUSE_TO_DIR[mouseDirX][mouseDirY]</p>
     * Outputs the relative direction of the block side to move to (or -1 if no movement).
     * <p>Where mouseDirX is relative shift 0=left, 1=centre, 2=right, and
     * mouseDirY is relative shift 0=down, 1=centre, 2=up.</p> 
	 */
	public static final int MOUSE_TO_DIR[][] = { // dx, dy
			// down			centre		up
			{	DIR_BACK,	DIR_LEFT,	DIR_BACK,	}, // left
			{	DIR_BELOW,	-1,			DIR_ABOVE,	}, // centre
			{	DIR_FRONT,	DIR_RIGHT,	DIR_FRONT,	}, // right
		};
}
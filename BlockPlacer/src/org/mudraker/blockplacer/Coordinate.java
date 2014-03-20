/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.blockplacer;

import net.minecraft.util.Facing;
import net.minecraft.util.MovingObjectPosition;

/**
 * Handles Minecraft World Coordinates as a single object.
 * <p>Designed as a non-pooled reusable object with public world coordinates.</p>
 * 
 * <p>Provides facilities to create from a {@link MovingObjectPosition} and to
 * find the adjacent location on a given {@link Facing} or side of the current location.</p>
 * 
 * <p>Pattern: Non-pooled Reusable Structure</p>
 * 
 * @author MudRaker
 */
public class Coordinate implements Cloneable {
	/** X integer world coordinate */
	public int x;
	/** Y integer world coordinate */
	public int y;
	/** Z integer world coordinate */
	public int z;

	/**
	 * Construct using individual coordinates.
	 * @param x coordinate
	 * @param y coordinate
	 * @param z coordinate
	 */
	public Coordinate(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Construct using a {@link MovingObjectPosition}
	 * @param mop is the MovingObjectPosition to source the location from.
	 */
	public Coordinate(MovingObjectPosition mop) {
		this.setFromMop(mop);
	}

	/**
	 * Formats the coordinate as an (x,y,z) tuple
	 * @return String representation of the coordinate
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + this.x + "," + this.y + "," + this.z + ")";
	}

	/**
	 * Sets the coordinate to the location that is adjacent on a side 
	 * @param side to be adjacent from
	 * @return this coordinate after setting to be adjacent
	 */
	public Coordinate setAdjacentOnSide(int side) {
		// Shift to adjacent block from that side so we are ready to draw.
		x += Facing.offsetsXForSide[side];
		y += Facing.offsetsYForSide[side];
		z += Facing.offsetsZForSide[side];
		return this;
	}

	/**
	 * Sets the coordinate to be the location adjacent to the provided coordinate on a side 
	 * @param coordinate to be adjacent to
	 * @param side to be adjacent from the coordinate
	 * @return this coordinate after setting to be the adjacent location
	 */
	public Coordinate setAdjacentOnSide(Coordinate c, int side) {
		x = c.x + Facing.offsetsXForSide[side];
		y = c.y + Facing.offsetsYForSide[side];
		z = c.z + Facing.offsetsZForSide[side];
		return this;
	}

	/**
	 * Returns a new coordinate adjacent to this location on the given side. 
	 * @param side to be adjacent from this coordinate
	 * @return new coordinate adjacent to this location on the given side
	 */
	public Coordinate adjacentOnSide(int side) {
		return new Coordinate(x, y, z).setAdjacentOnSide(side);
	}

	/**
	 * Set the coordinate to the location contained in the {@link MovingObjectPosition}
	 * @param mop to copy the location from
	 */
	public void setFromMop(MovingObjectPosition mop) {
		this.x = mop.blockX;
		this.y = mop.blockY;
		this.z = mop.blockZ;
	}

	/**
	 * Compare two coordinates
	 * @param c coordinate to compare against
	 * @return true if same integer world location else false
	 */
	public boolean equals(Coordinate c) {
		return this.x == c.x && this.y == c.y && this.z == c.z;
	}

	/**
	 * Compare coordinate to a {@link MovingObjectPosition}
	 * @param mop to compare against
	 * @return true if the MovingObjectPosition is the same world location as the coordinate else false
	 */
	public boolean equals(MovingObjectPosition mop) {
		return (this.x == mop.blockX && this.y == mop.blockY && this.z == mop.blockZ);
	}
}
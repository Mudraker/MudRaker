/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.blockplacer;

import java.lang.reflect.Method;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import org.mudraker.Lang;
import org.mudraker.Log;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * BlockPlacer mod state class.
 * <p>Maintains the block placer state and provides the core mod functions.
 * Only a single instance is required for each player so it is implemented
 * as a client only static class.</P>
 * 
 * <p>Pattern: Static Mutable.</p>
 * 
 * @author MudRaker
 */
@SideOnly(Side.CLIENT)
public class BlockPlacer {
	private BlockPlacer() {} // Static: Prevent instantiation

	// **************************************
	// internal state variables
	// **************************************

	/** Has the player been welcomed with a chat message? */
	private static boolean playerWelcomed = false;
	
	/** Main on/off flag for the blockplacer mod */ 
	private static boolean placeEnabled = false;
	
	/** Do we need to reinitialise the place location? */
	private static boolean placeReinit = true;
	
	/** Should we draw the overlay text next time we draw? */
	private static boolean drawText = false;
	
	/** Block the mouse is pointing at - used to detect movement */
	private static MovingObjectPosition placeMop = null;
	
	/** 
	 * XYZ co-ordinate of the block we are placing against
	 * May not be what mouse is pointing at if autoRpt is on.
	 * A single coordinate object is reused to minimise heap churn. 
	 */
	private static Coordinate placePosition = new Coordinate(0, 0, 0); // reusable structure object
	
	/** Which side of the placePosition should we place on? */
	private static int placeSide = 0;
	
	/** Is the placePosition replaceable? */
	private static boolean placeReplaceable;

	// **************************************
	// Getters and Setters
	// **************************************

	/** @returns true if BlockPlacer is enabled else false */
	public static boolean isPlaceEnabled() {
		return placeEnabled;
	}
	
	/** 
	 * Welcomes the player if first time via a chat message.
	 * @returns true if BlockPlacer is enabled else false 
	 */
	public static boolean isPlaceEnabledWithWelcome() {
		if (!playerWelcomed) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc != null && mc.thePlayer != null) {
				mc.thePlayer.addChatMessage(ModInfo.LONG_NAME + " Version: " + ModInfo.VERSION);
				playerWelcomed = true;
			}
		}
		return placeEnabled;
	}

	/** @param enable BlockPlacer if true or disable if false */
	public static void setPlaceEnabled(boolean enable) {
		if (enable != placeEnabled) {
			placeEnabled = enable;
			Log.info("BlockPlacer mode " + (placeEnabled ? "Enabled" : "Disabled"));
			placeReinit = true;
		}
	}

	/**
	 * Toggle the current BlockPlacer enable status
	 * @return the enable status after toggling
	 */
	public static boolean togglePlaceEnabled() {
		placeEnabled = !placeEnabled;
		Log.info("BlockPlacer mode " + (placeEnabled ? "Enabled" : "Disabled"));
		placeReinit = true;
		return placeEnabled;
	}
	
	/** @returns true if need to draw relative position text. Auto resets flag to false.  */
	public static boolean shouldDrawText() {
		boolean b = drawText;
		drawText = false;
		return b;
	}
	
	// **************************************
	// Public Interface Utility methods
	// **************************************

	/**
	 * @param entityPlayer the player to be checked
	 * @return true if this player is holding a valid {@link ItemBlock} else false
	 */
	public static boolean isHoldingItemBlock(EntityPlayer entityPlayer) {
		return (entityPlayer.getHeldItem() != null && (entityPlayer.getHeldItem().getItem() instanceof ItemBlock));
	}

	/**
	 * Return the 2D direction (NSEW) that the player is facing
	 * @param entityPlayer is the player in question
	 * @return the nearest direction / side facing of the player
	 */
	private static int getPlayerDirection(EntityPlayer entityPlayer) {
		return Direction.directionToFacing[MathHelper.floor_float((entityPlayer.rotationYaw / 90F) + 0.5F) & 3];
	}
	
	/**
	 * Return the player pitch (up / level / down)
	 * @param entityPlayer is the player in question
	 * @return the pitch zone that the player is looking in 
	 */
	private static int playerPitch(EntityPlayer entityPlayer) {
		int pitch;
		if (entityPlayer.rotationPitch > Const.PITCH_ANGLE) { // Looking down
			pitch = Const.PITCH_DOWN;
		} else if (entityPlayer.rotationPitch < - Const.PITCH_ANGLE) { // Looking up
			pitch = Const.PITCH_UP;
		} else {
			pitch = Const.PITCH_LEVEL;
		}
		//Log.finer(entityPlayer, "Player pitch " + Const.PITCH_NAME[pitch] + "(" + pitch + "), angle=" + entityPlayer.rotationPitch);
		return pitch;
	}
	
	// ********************************
	// BlockPlacer Public Entry Points
	// ********************************

	/**
	 * Return the placement text.
	 * <p>Normally the position as a relative direction to the player facing. 
	 * Above, Below, Front, Back, Left, Right and adds the compass point in parenthesis after it</p>
	 * <p>Returns "Replace" if the position is a replaceable block</p>
	 * @param entityPlayer is the player to find the placement text for
	 * @return the placement text string
	 */
	public static String getPlacementText(EntityPlayer entityPlayer) {
		if (placeReplaceable) {
			return Lang.getPhrase(ModInfo.ID, "replace");
		} else {
			int facing = getPlayerDirection(entityPlayer);
			int relativeDirection = Const.SIDE_TO_REL_DIR[facing][placeSide];
			return Const.DIRECTION_NAME[relativeDirection] + " (" + Facing.facings[placeSide] + ")";
		}
	}
	
	/**
	 * Establishes the correct position to draw the selection wire frame, 
	 * or disables BlockPlacer mode if it should no longer be drawn.
	 * @param entityPlayer is the {@link EntityPlayer} the selection wire frame is being drawn for.
	 * @param mop is the {@link MovingObjectPosition} of the selection event ray trace.
	 * @param drawPosition (OUT) updated with wire frame draw coordinate (if valid).
	 * @return the wire frame draw coordinate if it should be drawn, or null if no wireframe should be drawn
	 */
	public static Coordinate establishPlacement(Minecraft mc, EntityPlayer entityPlayer, MovingObjectPosition mop, Coordinate drawPosition) {
		Config config = Config.getInstance();
		boolean isValid = isCurrentPlaceValid(mc.theWorld);
		boolean shifted = false;
		
		// check if the place location needs to be reset
		if (placeReinit || (shifted = mouseShifted(mop)) || !isValid) {
			if (placeReinit) {
				Log.fine("Reset placing - Forced reinitialise to " + mop.blockX + "," + mop.blockY + "," + mop.blockZ + "/" + mop.sideHit);
			} else if (config.placeAutoOff) {
				Log.info("Place mode auto disabled - selection changed ");
				placeEnabled = false;
				return null; // FORCED EARLY EXIT
			} else if (!shifted && !isValid) {
				Log.fine("Block at placePosition " + placePosition + " absent - Reset");
				placeReset(mc);
			} else {
				Log.fine("Reset placing - Mouse shift " + placeMop.blockX + ","
						+ placeMop.blockY + "," + placeMop.blockZ + "/" + placeMop.sideHit + " to "
						+ mop.blockX + "," + mop.blockY + "," + mop.blockZ + "/" + mop.sideHit);
			}
			placeReinit = false;
			placeMop = mop;
			placePosition.setFromMop(mop);
			placeSide = mop.sideHit;
			placeReplaceable = checkIfPositionIsReplaceable(mc.theWorld);
			if (!placeReplaceable && config.placeSmartStart) {
				setDefaultPlace (placeSide); // ignore failure, just leave on sideHit if can't find anything better
			}
		}

		// Check placement location and fix if not valid - exit if no valid position
		if (!canPlaceOnThisSide(mc.theWorld, entityPlayer, placePosition, placeSide)) {
			Log.fine("Initial start location is illegal");
			if (!adjustPlace(false, true)) {
				Log.fine("Can't find ANY location & can't place what we are holding here");
				if (!entityPlayer.isSneaking()) return null; // FORCED EXIT
			}
		}
		
		// If detect devices is enabled, check if block can exit if it does/might.
		if (!entityPlayer.isSneaking() && config.placeDetectDevices && canBlockActivate(mc, placePosition)) {
			Log.fine("Can't find ANY location & can't place what we are holding here");
			return null; // FORCED EXIT
		}
			
		// Record that relative position text should be drawn next render if enabled in config
		drawText = config.drawFacingText;
		
		// Set draw position
		if (placeReplaceable)
			return drawPosition.set(placePosition);
		else
			return drawPosition.setAdjacentOnSide(placePosition, placeSide);
	}

	/**
	 * Call the standard player right click handler, but adjusting the side clicked.
	 * Caller is expected to check block placer is enabled before calling. 
	 * Function to duplicate net.minecraft.client.Minecraft.java/clickMouse()
	 * @@MCVERSION164
	 */
	public static boolean doRightClick(EntityPlayer entityPlayer) {
		Minecraft mc = Minecraft.getMinecraft();
		int effectiveSide = findEffectiveReplaceableSide(entityPlayer.worldObj);

		Log.fine("Simulating rightclick at " + placePosition + " on side " + placeSide);
		if (mc.playerController.onPlayerRightClick(entityPlayer, entityPlayer.worldObj,
				entityPlayer.getHeldItem(), placePosition.x, placePosition.y, placePosition.z,
				placeSide, mc.objectMouseOver.hitVec)) {
			/*
			 * Right Click place can be successful on the client side but still fail on the
			 * server. This appears to be a weird difference in the reach logic. Handle this
			 * condition by double checking that we have a block to place against and going
			 * into reset mode if missing. 
			 */
			if (!isCurrentPlaceValid(mc.theWorld)) {
				Log.fine("Block at placePosition " + placePosition + " absent - Reset");
				placeReset(mc);
				return false;
			} else {
				if (didItPlaceABlock(mc.theWorld)) {
					Log.fine((placeReplaceable ? "Replaceable " : "") + "Block actually placed!");
					placeComplete(effectiveSide);
				}
				entityPlayer.swingItem();
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * Rotate the position that block placer will place in around the sides.
	 * <p>The rotate direction corresponds to the 4 rotation keys - DONT CHANGE.
	 * Uses the main lookup tables based on relative facing to make the rotation
	 * seem natural to the player regardless of the compass points involved.
	 * Keeps rotating until a valid empty position is found, or Resets if none
	 * are possible.</p>
	 * <p>Supports collapsing replaceable positions.</p>
	 * 
	 * @param rotateDirection is the rotation direction
	 * 		  0 = Rotate Vertical Clockwise
	 * 		  1 = Rotate Vertical AntiClockwise
	 * 		  2 = Rotate Horizontal Clockwise
	 * 		  3 = Rotate Horizontal AntiClockwise
	 */
	public static void rotatePlace(int rotateDirection) {
		if (placeEnabled) {
			Minecraft mc = Minecraft.getMinecraft();
			World theWorld = mc.theWorld;
			int newSide;
			
			// For replaceable positions, try collapsing first to see if that works
			if (placeReplaceable) {
				Coordinate collapsedPos = new Coordinate(0, 0, 0);
				int collapsedSide = collapseReplaceablePosition (theWorld, collapsedPos);
				if (collapsedSide != -1) {
					if ((newSide = doRotatePosition(mc, collapsedPos, collapsedSide, rotateDirection)) != -1) {
						placePosition.set(collapsedPos);
						placeSide = newSide;
						placeReplaceable = checkIfPositionIsReplaceable(theWorld);
						Log.fine("RotatePlace confirmed collapse to "+placePosition);
					}
				}
				
			// Otherwise we rotate around the place position
			} else if ((newSide = doRotatePosition(mc, placePosition, placeSide, rotateDirection)) != -1) {
				placeSide = newSide;
			}
		}
	}
	
	/**
	 * Adjust placement side based on the forward / backward keys.
	 * <p>Uses the relative direction of the player to rotate through a standard
	 * list of relative sides so it makes sense to the player no matter what
	 * compass points are involved.</p>
	 * <p>Supports collapsing replaceable positions.</p>
	 * 
	 * @param autoOn indicates if block placer should auto turn on if currently off
	 * @param forward is set true for forward or false for backward
	 * @return true if a valid place is found otherwise false
	 */
	public static boolean adjustPlace (boolean autoOn, boolean forward) {
		if (!placeEnabled) {
			if (autoOn) setPlaceEnabled(true);
			return true;
		}

		Minecraft mc = Minecraft.getMinecraft();
		World theWorld = mc.theWorld;
		int newSide;
		
		// For replaceable positions, try collapsing first to see if that works
		if (placeReplaceable) {
			Coordinate collapsedPos = new Coordinate(0, 0, 0);
			int collapsedSide = collapseReplaceablePosition (theWorld, collapsedPos);
			if (collapsedSide != -1) {
				if ((newSide = doAdjustPosition(mc, collapsedPos, collapsedSide, forward)) != -1) {
					placePosition.set(collapsedPos);
					placeSide = newSide;
					placeReplaceable = checkIfPositionIsReplaceable(theWorld);
					Log.fine("AdjustPlace confirmed collapse to "+placePosition);
					return true;
				}
			}
		
		// Otherwise we adjust from the current place position & side
		} else if ((newSide = doAdjustPosition(mc, placePosition, placeSide, forward)) != -1) {
			placeSide = newSide;
			return true;
		}
		return false;
	}

	/**
	 * Set place side based on mouse movements.
	 * <p>Check for a mouse movement bigger than the configured threshold, and then
	 * analyse it for the direction of movement. The 'wobble' configuration is used
	 * to assist in pure vertical or horizontal movement.</p>
	 * 
	 * <p>A translation matrix is used with relative directions to set the place side 
	 * in the direction of mouse movement relative to the player. Note that diagonal
	 * LEFT is BACK, and diagonal RIGHT is FRONT.</p>
	 * 
	 * @param event is the mouse movement event to process 
	 */
	public static void mouseShiftPlace(int dx, int dy) {
		Config config = Config.getInstance();
		Minecraft mc = Minecraft.getMinecraft();
		int relDir;
		int newSide;

		// Check if mouse event is enough to change sides
		// note: 0,0 is bottom left corner of screen with X horizontal and Y vertical.
		if (Math.abs(dx) + Math.abs(dy) >= config.mouseThreshold) {
			int fx = (dx < - config.mouseWobble) ? 0 : (dx > config.mouseWobble) ? 2 : 1;
			int fy = (dy < - config.mouseWobble) ? 0 : (dy > config.mouseWobble) ? 2 : 1;
			if ((relDir = Const.MOUSE_TO_DIR [fx][fy]) >= 0) {
				
				Log.fine("MouseShiftPlace dxy("+dx+","+dy+") fxy("+fx+","+fy+") relDir "+
						Const.DIRECTION_NAME[relDir]+" ("+relDir+")");

				// For replaceable positions, try collapsing first to see if that works
				if (placeReplaceable) {
					Coordinate collapsedPos = new Coordinate(0, 0, 0);
					int collapsedSide = collapseReplaceablePosition (mc.theWorld, collapsedPos);
					if (collapsedSide != -1) {
						if ((newSide = doMouseShiftPosition(mc, collapsedPos, collapsedSide, relDir)) != -1) {
							placePosition.set(collapsedPos);
							placeSide = newSide;
							placeReplaceable = checkIfPositionIsReplaceable(mc.theWorld);
							Log.fine("MouseShiftPlace confirmed collapse to "+placePosition);
						}
					}
				
				// Otherwise we adjust from the current place position & side
				} else if ((newSide = doMouseShiftPosition(mc, placePosition, placeSide, relDir)) != -1) {
					placeSide = newSide;
				}
			}
		}
	}

	// ********************
	// BlockPlacer Privates
	// ********************

	/**
	 * Check if mouse has shifted since the previous render?
	 * @param mop is the moving object position from the ray trace
	 * @return true if mouse ray trace XYZ or side has changed.
	 */
	private static boolean mouseShifted(MovingObjectPosition mop) {
		return (mop.blockX != placeMop.blockX || mop.blockY != placeMop.blockY
				|| mop.blockZ != placeMop.blockZ || mop.sideHit != placeMop.sideHit);
	}
	
	/**
	 * 3D rotation worker that attempts to rotate from the given position and side
	 * in the given rotation direction looking for a valid position to place.
	 * <p>The rotate direction corresponds to the 4 rotation keys - DONT CHANGE.
	 * Uses the main lookup tables based on relative facing to make the rotation
	 * seem natural to the player regardless of the compass points involved.
	 * Keeps rotating until a valid empty position is found, or fails.</p>
	 *
	 * @param mc is the minecraft object
	 * @param position is the position to rotate around
	 * @param side is the current side to rotate from
	 * @param rotateDirection is the rotation direction
	 * 		  0 = Rotate Vertical Clockwise
	 * 		  1 = Rotate Vertical AntiClockwise
	 * 		  2 = Rotate Horizontal Clockwise
	 * 		  3 = Rotate Horizontal AntiClockwise
	 * @returns the new side if rotation succeeded else -1
	 */
	private static int doRotatePosition(Minecraft mc, Coordinate position, int side, int rotateDirection) {
		EntityPlayer entityPlayer = mc.thePlayer;
		World theWorld = mc.theWorld;
		int count = 0;
		
		// Acquire facing and plane information
		int direction = getPlayerDirection(entityPlayer);
		int pitch = playerPitch(entityPlayer);
		int plane = Const.PLANE_TRANSLATE[pitch][rotateDirection][direction];
		assert (plane != -1);
		
		// Attempt initial rotation
		int newSide = Const.PLANE_ROTATE[plane][side];
		if (newSide < 0) {
			newSide = Const.REL_DIR_TO_SIDE[direction][Const.PLANE_DEFAULT_REL_DIR[pitch]];						
			Log.finer("RotatePosition " + position + " dir=" + rotateDirection + ", from="
					+ side + " side not on rotation path, goto pitch default= " + newSide);
		} else {
			Log.finer("RotatePosition " + position + " dir=" + rotateDirection + ", from="
					+ side + ",to=" + newSide);
		}
		
		// Check rotation is valid & repeat rotation until it is
		while (newSide != side && !canPlaceOnThisSide(theWorld, entityPlayer, position, newSide) && ++count < 5) {
			Log.finer("RotatePosition " + position + " collision at side " + newSide);
			newSide = Const.PLANE_ROTATE[plane][newSide];
			assert (newSide >= 0);
			Log.finer("RotatePosition " + position + " retry side " + newSide);
		}

		// Log results and return chosen side or failure.
		if (newSide != side && count < 5) {
			Log.fine("RotatePosition " + position + " chosen side is " + newSide);
			return newSide;
		} else {
			Log.fine("RotatePosition " + position + " failed to rotate dir= " + rotateDirection 
					+ ", from=" + side + ", last attempt=" + newSide + ", attempts=" + count);
			return -1;
		}
	}
	
	/**
	 * Forward/backward adjust worker that attempts to adjust from the given position and side
	 * in the given direction looking for a valid side to place on.
	 * <p>Uses the relative direction of the player to rotate through a standard
	 * list of relative sides so it makes sense to the player no matter what
	 * compass points are involved.</p>
	 *
	 * @param mc is the minecraft object
	 * @param position is the position to rotate around
	 * @param side is the current side to rotate from
	 * @param forward is set true for forward or false for backward
	 * @returns the new side if adjust side succeeded else -1
	 */
	private static int doAdjustPosition (Minecraft mc, Coordinate position, int side, boolean forward) {
		EntityPlayer entityPlayer = mc.thePlayer;
		World theWorld = mc.theWorld;
		
		// Setup and acquire facings
		int count = 0;
		int forwardOrBack = (forward) ? 0 : 1;
		int facing = getPlayerDirection(entityPlayer);
		
		// Adjust position to find new side
		int relativeDirection = Const.SIDE_TO_REL_DIR [facing][side];
		int newRelDir = Const.REL_DIR_ROTATE[forwardOrBack][relativeDirection];
		int newSide = Const.REL_DIR_TO_SIDE [facing][newRelDir];
		Log.finer("AdjustPosition "+position+" Side("+side+") Facing("+facing+") Reldir("
				+relativeDirection+"->"+newRelDir+") NewSide("+newSide+")");
	
		// Check if side is valid & repeat adjust until it is (or fail)
		while (!canPlaceOnThisSide(theWorld, entityPlayer, position, newSide) && ++count < 7) {
			Log.finer("AdjustPosition " + position + " collision at side " + newSide);
			relativeDirection = Const.SIDE_TO_REL_DIR [facing][newSide];
			newRelDir = Const.REL_DIR_ROTATE[forwardOrBack][relativeDirection];
			newSide = Const.REL_DIR_TO_SIDE [facing][newRelDir];
			Log.finer("AdjustPosition "+position+" RETRY Facing("+facing+") Reldir("
					+relativeDirection+"->"+newRelDir+") NewSide("+newSide+")");
		}
		
		// Log results and return chosen side or failure.		
		if (newSide != side && count < 7) {
			Log.fine("AdjustPosition " + position + " chosen side is " + newSide);
			return newSide;
		} else {
			// Happens if only one side is free, or when block can only be placed on top (like snow or grass)
			Log.fine("AdjustPosition " + position + " unable to adjust key= " + forwardOrBack 
					+ ", from=" + placeSide + ", last attempt=" + newSide + ", attempts=" + count);
			return -1;
		}
	}

	/**
	 * Mouse shift worker that attempts to change side based on mouse movements.
	 * @param mc is the minecraft object
	 * @param position is the position to rotate around
	 * @param side is the current side to rotate from
	 * @param relDir is the relative direction corresponding to the mouse movement
	 * @returns the new side if side shift succeeded else -1
	 */	
	private static int doMouseShiftPosition(Minecraft mc, Coordinate position, int side, int relDir) {
		int facing = getPlayerDirection(mc.thePlayer);
		int newSide = Const.REL_DIR_TO_SIDE [facing][relDir];
		
		if (newSide == side) {
			Log.finer("MouseShiftPosition facing("+facing+") relDir("+relDir+") newSide ("+newSide+") is unchanged");
		} else if (canPlaceOnThisSide(mc.theWorld, mc.thePlayer, position, newSide)) {
			Log.finer("MouseShiftPosition facing("+facing+") relDir "+Const.DIRECTION_NAME[relDir]+" ("+relDir+
					") newSide ("+newSide+") is valid");
			return newSide;
		} else {
			Log.finer("MouseShiftPosition facing("+facing+") relDir "+Const.DIRECTION_NAME[relDir]+" ("+relDir+
					") newSide ("+newSide+") is INVALID");
		}
		return -1;	
	}
	
	/**
	 * Worker function that handles successful placement of a block.
	 * <p>Implements the core auto-off and auto-repeat functions.</p>
	 * <p>Handles setting position after replacing a replaceable block.</p>
	 * <p>Also checks to see if the newly placed block is obscuring the existing ray 
	 * trace and if so, shifts the current recorded ray trace accordingly - this
	 * stops block placer from thinking that the mouse has moved and reinitialising.</p>
	 * <p>If not repeating, a new valid side is selected as the current position if
	 * possible, otherwise it does a Reset - allowing for AutoEnd and sounds.</p>
	 * @param effectiveSide is the effective side of a replaceable block placement (for autoRpt)
	 */
	private static void placeComplete (int effectiveSide) {
		Minecraft mc = Minecraft.getMinecraft();
		Config config = Config.getInstance();
		
		// AutoOff if required & not repeating
		if (config.placeAutoOff && !config.placeAutoRpt) {
			placeEnabled = false;
			placeReinit = true;
			Log.info("place mode auto-disabled");
			
		// Replaceable blocks when not repeating
		} else if (placeReplaceable && !config.placeAutoRpt) {
			placeReinit = true;
			placeReplaceable = false;
			Log.fine("Replaced block but no autoRpt so re-initialise");

		// Reset ray trace and reinitialise if nothing in reach now
		} else if (!resetRayTrace(mc)) {
			Log.fine("Nothing in reach now - was it a door??");
			placeReinit = true;
		
		// Handle auto-repeat after a replaceable block
		} else if (placeReplaceable && config.placeAutoRpt) {
			placeReplaceable = checkIfPositionIsReplaceable(mc.theWorld);
			placeSide = effectiveSide;
			Log.fine("Replaced block, so auto-repeat at same block on side "+Facing.facings[placeSide]+" ("+placeSide+")");
			if (!canPlaceOnThisSide(mc.theWorld, mc.thePlayer, placePosition, placeSide)) {
				Log.fine("Place mode auto-repeat terminated due to obstruction");
				placeReset(mc);
			}
			
		// Handle normal auto-repeat
		} else if (config.placeAutoRpt) {
			Coordinate newC = placePosition.adjacentOnSide(placeSide);
			Log.fine("Place mode auto-repeat from " + placePosition + " to " + newC
					+ " side " + placeSide);
			if (canPlaceOnThisSide(mc.theWorld, mc.thePlayer, newC, placeSide)) {
				placePosition = newC;
				placeReplaceable = checkIfPositionIsReplaceable(mc.theWorld);
			} else {
				Log.fine("Place mode auto-repeat terminated due to obstruction");
				placeReset(mc);
			}
			
		// Find next place position around the same block if not repeating	
		} else if (setDefaultPlace (placeSide) < 0) {
			// this happens with no auto repeat if place on all sides of a block!!
			Log.fine("Place mode force-reset - no valid place sides");
			placeReset(mc);
		}
	}
	
	/**
	 * Reset the placement {@link MovingObjectPosition} ray trace to reflect the 
	 * placed block IF it alters the ray trace. Only valid during the right-click 
	 * place action when the mouse is not moving. Also detects if the no blocks are
	 * in reach after the right click (e.g. if a door is opened).
	 * @param mc is minecraft
	 * @return true if a valid ray trace exists or false if nothing within reach anymore.
	 */
	private static boolean resetRayTrace (Minecraft mc) { 
	    double reach = (double)mc.playerController.getBlockReachDistance();
	    MovingObjectPosition mop = mc.renderViewEntity.rayTrace(reach, 1.0F);
	    if (mop != null && mouseShifted(mop)) {
			Log.fine("Placed block is in ray trace, refocus at "+ mop.blockX + "," + mop.blockY + "," + mop.blockZ + "/" + mop.sideHit);
			placeMop = mop;
	    }
		return (mop != null);
	}
	
	/** A constant dirt itemstack for use as a default if needed when checking if can place. */  
	private static final ItemBlock DIRT = (ItemBlock) Item.itemsList[Block.dirt.blockID]; // local constant
			
	/**
	 * Worker to check if it is legal to place a block on the given side of the given position
	 * location. Used for testing alternate placement sides to see if they are valid.
	 * @param theWorld is the current world
	 * @param entityPlayer is the entity player that would do the placement
	 * @param pos is the block coordinate to place against
	 * @param side is the block side we want to place on.
	 * @return true if the current player item could be placed here else false
	 */
	private static boolean canPlaceOnThisSide(World theWorld, EntityPlayer entityPlayer, Coordinate pos, int side) {
		ItemStack itemStack = entityPlayer.getHeldItem();
		ItemBlock itemBlock = ((itemStack != null && itemStack.getItem() instanceof ItemBlock) 
				? (ItemBlock) itemStack.getItem() : DIRT);
		boolean valid = itemBlock.canPlaceItemBlockOnSide(theWorld, pos.x,
				pos.y, pos.z, side, entityPlayer, itemStack);
		//Log.finer("canPlaceOnThisSide " + pos + " @ side " + side + " --> " + valid);
		return valid;
	}

	/**
	 * Check that the current place position itself is a valid block.
	 * @param theWorld is the current world
	 * @return true if the place position contains a block to place against
	 */
	private static boolean isCurrentPlaceValid (World theWorld) {
		return (theWorld.getBlockId (placePosition.x, placePosition.y, placePosition.z) != 0);
	}
	
	/**
	 * Check if a block actually placed when the player right clicked or did the block just activate?
	 * @param theWorld is the current world
	 * @return true if a replaceable block was replaced or there is a block on the place side of the place position
	 */
	private static boolean didItPlaceABlock (World theWorld) {
		if (placeReplaceable) {
			return (!checkIfPositionIsReplaceable(theWorld));
		} else {
			Coordinate newC = placePosition.adjacentOnSide(placeSide);
			return (theWorld.getBlockId (newC.x, newC.y, newC.z) != 0);
		}
	}
	
	/**
	 * 'Intelligently' chose a default place to start the placement cycle from.
	 * <p>Uses a 'default place' array to try to top 3 options to see if they are
	 * valid place locations. If none are, if switches to using adjustPlace to find
	 * any valid side, if one exists.
	 * @param sideHit is the compass block side hit by the player.
	 * @return the compass block side for initial placement OR -1 if impossible.
	 */
	private static int setDefaultPlace (int sideHit) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer entityPlayer = mc.thePlayer;
		World theWorld = mc.theWorld;
		
		boolean isValid = false;
		int defaultDir;
		int facing = getPlayerDirection(entityPlayer);
		int relativeDirection = Const.SIDE_TO_REL_DIR [facing][sideHit];
		int pitch = playerPitch(entityPlayer);
		for (int attempt = 0; attempt < Const.DEFAULT_PLACE.length && !isValid; attempt++) {
			if ((defaultDir = Const.DEFAULT_PLACE [attempt][pitch][relativeDirection]) < 0)
				break;
			placeSide = Const.REL_DIR_TO_SIDE [facing][defaultDir];
			isValid = (canPlaceOnThisSide(theWorld, entityPlayer, placePosition, placeSide));
			Log.finer("DefaultPlace ("+attempt+"/"+Const.DEFAULT_PLACE.length+") "
					+placePosition+" Side("+sideHit+") Facing("+facing+") Pitch("
					+pitch+") Reldir("+relativeDirection+"->"+defaultDir+") NewSide("+placeSide+") VALID="+isValid);
		} 
			
		if (!isValid) {
			Log.finer("DefaultPlaces are not-valid - use Side hit");
			placeSide = sideHit;
			if (!adjustPlace(false, true)) {
				return -1;
			}
		}
		return placeSide;
	}
	
	/**
	 * Reset the block placer state once we reach a position where there is no
	 * valid options for the current place location.
	 * @param mc is the Minecraft instance
	 */
	private static void placeReset (Minecraft mc) {
		Config config = Config.getInstance();
		// Turn off block placer instead of Resetting if autoEnd is on.
		if (config.placeAutoEnd) {
			placeEnabled = false;
		}
		
		// Warn the user we are resetting by playing an unusual sound.
		if (config.placeResetSound) {
			mc.sndManager.playSound("note.bass", (float)placePosition.x + 0.5F, 
	        		(float)placePosition.y + 0.5F, (float)placePosition.z + 0.5F, 10.0F, 1.0F);
		}
		
		// Mark place position to be re-initialised.
		placeReinit = true;
	}
	
	/**
	 * Check if the place position is a replaceable block
	 * @param theWorld is the world
	 * @return true if the place position is a replaceable block
	 */
	private static boolean checkIfPositionIsReplaceable (World theWorld) {
        int blockId = theWorld.getBlockId(placePosition.x, placePosition.y, placePosition.z);
		Block block = Block.blocksList[blockId];
		if (block != null && block.isBlockReplaceable(theWorld, placePosition.x, placePosition.y, placePosition.z)) {
        	Log.fine("Block is replaceable at "+placePosition+", id("+blockId+")");
			return true;
		}
		return false;
	}
	
	/**
	 * Find the effective side that we are placing on if we ignored the replaceable block
	 * Result also implies the opposite direction to find the expected adjacent block location
	 * @param theWorld is the world
	 * @return the effective side of the underlying block or -1 if not replaceable
	 */
	private static int findEffectiveReplaceableSide (World theWorld) {
        int blockId = theWorld.getBlockId(placePosition.x, placePosition.y, placePosition.z);
		int blockMeta = theWorld.getBlockMetadata(placePosition.x, placePosition.y, placePosition.z);
		Block block = Block.blocksList[blockId];
		
		if (block != null && block.isBlockReplaceable(theWorld, placePosition.x, placePosition.y, placePosition.z)) {
    		int side;
        	if (!(block instanceof BlockVine)) side = Const.SIDE_TOP;
    		//see BlockVine.java canVineStay() and setBlockBoundsBasedOnState() for why this order
        	else if ((blockMeta & 2) != 0) 	side = Const.SIDE_EAST;
            else if ((blockMeta & 8) != 0) 	side = Const.SIDE_WEST;
            else if ((blockMeta & 4) != 0) 	side = Const.SIDE_SOUTH;
            else if ((blockMeta & 1) != 0) 	side = Const.SIDE_NORTH;
            else 							side = Const.SIDE_BOTTOM;
        	Log.fine("Replaceable block at "+placePosition+", id("+blockId+") Meta("+blockMeta+") eff-Side "+Facing.facings[side]+"("+side+")");
        	return side;
        } else {
    		return -1;
        }		
	}
	
	/**
	 * Attempts to collapse a currently replaceable position to the underlying block
	 * that the replaceable block is against. If possible, sets the collapsed position
	 * parameter and returns the effective placement side.
	 * @param theWorld is the world
	 * @param collapsedPos (OUT) is the returned collapsed position
	 * @returns collapsed place side if can collapse else -1
	 */
	private static int collapseReplaceablePosition (World theWorld, Coordinate collapsedPos) {
		int side = findEffectiveReplaceableSide(theWorld);
		if (placeReplaceable && side != -1) {
			collapsedPos.set(placePosition);
			collapsedPos.setAdjacentOnSide(Facing.oppositeSide[side]);
	        if (theWorld.getBlockId(collapsedPos.x, collapsedPos.y, collapsedPos.z) != 0) {
	        	Log.fine("Collapse replaceable at "+placePosition+" eff-Side "+Facing.facings[side]+"("+side+") to "+collapsedPos);
	        	return side;
	        }
	    	Log.fine("Unable to collapse replaceable at "+placePosition);
		}
		return -1;
	}
	
	/**
	 * Attempts to detect if a world block can activate or not.
	 * <p>Uses reflection to detect if the block is inheriting the base {@link block.java} class method for
	 * {@link onBlockActivate}. If it is the block cannot activate. Reflected results are cached to avoid
	 * the repeated reflection overheads.</p>
	 * @param mc is the minecraft instance
	 * @param position is the world position coordinate
	 * @return true if the block could activate, or false if it definitely can't OR an error occurs
	 * @@MCVERSION 164
	 */
	private static boolean canBlockActivate (Minecraft mc, Coordinate position) {
        int blockId = mc.theWorld.getBlockId(position.x, position.y, position.z);
        HashMap<Integer,Boolean> cache = new HashMap(128);

        if (!cache.containsKey(blockId)) {
            Block block = Block.blocksList[blockId];
            Method method = null;
            final Class [] parms = new Class[] {World.class, int.class, int.class, int.class, EntityPlayer.class, int.class, float.class, float.class, float.class};
	        try {
	        	method = block.getClass().getMethod("func_71903_a", parms);
	        } catch (NoSuchMethodException e) {
	            try {
	            	method = block.getClass().getMethod("onBlockActivated", parms);
	            } catch (NoSuchMethodException e2) {
	            	Log.info("No such method exception - twice");
	            	cache.put(blockId, false);
	            	return false;
	            }
	        } catch (SecurityException e) {
	        	Log.info("Security exception");
            	cache.put(blockId, false);
	        	return false;
        	}
            boolean result = !(method.getDeclaringClass().equals(Block.class));
        	cache.put(blockId, result);
        	return result;
        } else {
        	return (cache.get(blockId));
        }
	}
}
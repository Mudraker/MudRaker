/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.blockplacer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
	 * Return the place position as a relative direction to the player facing.
	 * <p>Returns Above, Below, Front, Back, Left, Right and adds the compass point
	 * in parenthesis after it</p>
	 * @param entityPlayer is the player to find relative to
	 * @return the relative direction string
	 */
	public static String getPlaceRelativeDirection(EntityPlayer entityPlayer) {
		int facing = getPlayerDirection(entityPlayer);
		int relativeDirection = Const.SIDE_TO_REL_DIR[facing][placeSide];
		//Log.finer(entityPlayer, "Rel=" + Const.DIRECTION_NAME[relativeDirection] + " (" + relativeDirection + "), Player facing="
		//		+ Facing.facings[facing] + " (" + facing + "), Block Side=" + Facing.facings[placeSide] 
		//		+ " (" + placeSide + ")");
		return Const.DIRECTION_NAME[relativeDirection] + " (" + Facing.facings[placeSide] + ")";
	}
	
	/**
	 * Establishes the correct position to draw the selection wire frame, 
	 * or disables BlockPlacer mode if it should no longer be drawn.
	 * @param entityPlayer is the {@link EntityPlayer} the selection wire frame is being drawn for.
	 * @param mop is the {@link MovingObjectPosition} of the selection event ray trace.
	 * @param drawPosition (OUT) updated with wire frame draw coordinate (if valid). 
	 * @return the wire frame draw coordinate if it should be drawn, or null if BlockPlacer has been auto disabled.
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
						+ placeMop.blockY + "," + placeMop.blockZ + "/" + placeSide + " to "
						+ mop.blockX + "," + mop.blockY + "," + mop.blockZ + "/" + mop.sideHit);
			}
			placeReinit = false;
			placeMop = mop;
			placePosition.setFromMop(mop);
			placeSide = mop.sideHit;
			checkReplaceable();
			if (config.placeSmartStart) {
				setDefaultPlace (placeSide);
			}
		}
		
		// Record that relative position text should be drawn next render if enabled in config
		drawText = config.drawFacingText;
		
		// Check for replaceable blocks
        int blockId = mc.theWorld.getBlockId(placePosition.x, placePosition.y, placePosition.z);
		Block block = Block.blocksList[blockId];
		boolean blockIsReplaceable = (block == null) ? false : block.isBlockReplaceable(mc.theWorld, placePosition.x, placePosition.y, placePosition.z);
        if (blockId == Block.snow.blockID || blockId == Block.vine.blockID || blockId == Block.tallGrass.blockID ||	blockId == Block.deadBush.blockID) {
        	if (!blockIsReplaceable) Log.warn("Found special blockID but is not replaceable!!!!!!!!!!!!!!!!!!!!!!");
        }
		
		// Return adjacent block from that side for drawing.
        if (blockIsReplaceable) {
        	Log.fine("establishPlacement replaceable id("+blockId+")");
    		return drawPosition.set(placePosition);
        } else {
    		return drawPosition.setAdjacentOnSide(placePosition, placeSide);
        }
	}

	/**
	 * Call the standard player right click handler, but adjusting the side clicked.
	 * Caller is expected to check block placer is enabled before calling. 
	 * Function to duplicate minecraft.src.net.Minecraft.java/clickMouse()
	 * @@MCVERSION164
	 */
	public static boolean doRightClick(EntityPlayer entityPlayer) {
		Minecraft mc = Minecraft.getMinecraft();

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
					Log.fine("Block actually placed, so determine the next step");
					placeComplete();
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
			EntityPlayer entityPlayer = mc.thePlayer;
			World theWorld = mc.theWorld;
			
			int count = 0;
			
			int direction = getPlayerDirection(entityPlayer);
			int pitch = playerPitch(entityPlayer);
			int plane = Const.PLANE_TRANSLATE[pitch][rotateDirection][direction];
			assert (plane != -1);
			
			int newSide = Const.PLANE_ROTATE[plane][placeSide];
			if (newSide < 0) {
				newSide = Const.REL_DIR_TO_SIDE[direction][Const.PLANE_DEFAULT_REL_DIR[pitch]];						
				Log.finer("Rotateplace " + placePosition + " dir=" + rotateDirection + ", from="
						+ placeSide + " side not on rotation path, goto pitch default= " + newSide);
			} else {
				Log.finer("Rotateplace " + placePosition + " dir=" + rotateDirection + ", from="
						+ placeSide + ",to=" + newSide);
			}
			
			while (newSide != placeSide && !canPlaceOnThisSide(theWorld, entityPlayer, newSide) && ++count < 5) {
				Log.finer("Rotateplace " + placePosition + " collision at side " + newSide);
				newSide = Const.PLANE_ROTATE[plane][newSide];
				assert (newSide >= 0);
				Log.finer("Rotateplace " + placePosition + " retry side " + newSide);
			}
			if (count < 5) {
				Log.fine("Rotateplace " + placePosition + " chosen side is " + newSide);
				placeSide = newSide;
			} else {
				Log.warn("Rotateplace " + placePosition + " unable to rotate dir= " + rotateDirection 
						+ ", from=" + placeSide + ", last attempt=" + newSide + ", attempts=" + count);
			}
		}
	}
	
	/**
	 * Adjust placement side based on the forward / backward keys.
	 * <p>Uses the relative direction of the player to rotate through a standard
	 * list of relative sides so it makes sense to the player no matter what
	 * compass points are involved.</p>
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
		EntityPlayer entityPlayer = mc.thePlayer;
		World theWorld = mc.theWorld;
		
		int count = 0;
		int forwardOrBack = (forward) ? 0 : 1;
		int facing = getPlayerDirection(entityPlayer);
		int relativeDirection = Const.SIDE_TO_REL_DIR [facing][placeSide];
		int newRelDir = Const.REL_DIR_ROTATE[forwardOrBack][relativeDirection];
		int newSide = Const.REL_DIR_TO_SIDE [facing][newRelDir];
		Log.finer("Adjustplace "+placePosition+" Side("+placeSide+") Facing("+facing+") Reldir("
				+relativeDirection+"->"+newRelDir+") NewSide("+newSide+")");
	
		while (!canPlaceOnThisSide(theWorld, entityPlayer, newSide) && ++count < 7) {
			Log.finer("Adjustplace " + placePosition + " collision at side " + newSide);
			relativeDirection = Const.SIDE_TO_REL_DIR [facing][newSide];
			newRelDir = Const.REL_DIR_ROTATE[forwardOrBack][relativeDirection];
			newSide = Const.REL_DIR_TO_SIDE [facing][newRelDir];
			Log.finer("Adjustplace "+placePosition+" RETRY Facing("+facing+") Reldir("
					+relativeDirection+"->"+newRelDir+") NewSide("+newSide+")");
		}
		if (count < 7) {
			Log.fine("Adjustplace " + placePosition + " chosen side is " + newSide);
			placeSide = newSide;
			return true;
		} else {
			Log.warn("Adjustplace " + placePosition + " unable to adjust key= " + forwardOrBack 
					+ ", from=" + placeSide + ", last attempt=" + newSide + ", attempts=" + count);
			return false;
		}
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
				
		// 0,0 is bottom left corner of screen with X horizontal and Y vertical.
		if (Math.abs(dx) + Math.abs(dy) >= config.mouseThreshold) {
			int fx = (dx < - config.mouseWobble) ? 0 : (dx > config.mouseWobble) ? 2 : 1;
			int fy = (dy < - config.mouseWobble) ? 0 : (dy > config.mouseWobble) ? 2 : 1;
			if ((relDir = Const.MOUSE_TO_DIR [fx][fy]) >= 0) {
				int facing = getPlayerDirection(mc.thePlayer);
				int newSide = Const.REL_DIR_TO_SIDE [facing][relDir];
				if (newSide == placeSide) {
					Log.finer("MouseShiftPlace dxy("+dx+","+dy+") fxy("+fx+","+fy+") facing("
							+facing+") relDir("+relDir+") newSide ("+newSide+") is unchanged");
				} else if (canPlaceOnThisSide(mc.theWorld, mc.thePlayer, newSide)) {
					Log.finer("MouseShiftPlace dxy("+dx+","+dy+") fxy("+fx+","+fy+") facing("
							+facing+") relDir "+Const.DIRECTION_NAME[relDir]+" ("+relDir+") newSide ("+newSide
							+") is valid");
					placeSide = newSide;
				} else {
					Log.finer("MouseShiftPlace dxy("+dx+","+dy+") fxy("+fx+","+fy+") facing("
							+facing+") relDir "+Const.DIRECTION_NAME[relDir]+" ("+relDir+") newSide ("+newSide
							+") is INVALID");
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
	 * Worker function that handles successful placement of a block.
	 * <p>Implements the core auto-off and auto-repeat functions.</p>
	 * <p>Also checks to see if the newly placed block is obscuring the existing ray 
	 * trace and if so, shifts the current recorded ray trace accordingly - this
	 * stops block placer from thinking that the mouse has moved and reinitialising.</p>
	 * <p>If not repeating, a new valid side is selected as the current position if
	 * possible, otherwise it does a Reset - allowing for AutoEnd and sounds.</p>
	 */
	private static void placeComplete() {
		Config config = Config.getInstance();
		// AutoOff if required & not repeating
		if (config.placeAutoOff && !config.placeAutoRpt) {
			placeEnabled = false;
			placeReinit = true;
			Log.info("place mode auto-disabled");
		} else {
			Minecraft mc = Minecraft.getMinecraft();
	        double reach = (double)mc.playerController.getBlockReachDistance();
	        MovingObjectPosition mop = mc.renderViewEntity.rayTrace(reach, 1.0F);
	        
	        if (mop != null && mouseShifted(mop)) {
				Log.fine("Placed block is in the mouse line, mark as standard");
	        	placeMop = mop;
	        }
	
			if (mop == null) {
				Log.fine("Nothing in reach now - was it a door??");
				placeReinit = true;
			} else if (config.placeAutoRpt) {
				Coordinate newC = placePosition.adjacentOnSide(placeSide);
				Log.fine("Place mode auto-repeat from " + placePosition + " to " + newC
						+ " side " + placeSide);
				placePosition = newC;
				if (canPlaceOnThisSide(mc.theWorld, mc.thePlayer, placeSide)) {
					checkReplaceable();
				} else {
					Log.fine("Place mode auto-repeat terminated due to obstruction");
					placeReset(mc);
				}
			} else if (setDefaultPlace (placeSide) < 0) {
				// this happens with no auto repeat if place on all sides of a block!!
				Log.warn("Place mode force-disabled - no valid place sides");
				placeReset(mc);
			}
		}
	}
	
	/** A constant dirt itemstack for use as a default if needed when checking if can place. */  
	private static final ItemBlock DIRT = (ItemBlock) Item.itemsList[Block.dirt.blockID]; // local constant
			
	/**
	 * Worker to check if it is legal to place a block on the given side of the current place
	 * location. Used for testing alternate placement sides to see if they are valid.
	 * @param theWorld is the current world
	 * @param entityPlayer is the entity player that would do the placement
	 * @param side is the block side we want to place on.
	 * @return true if the current player item could be placed here else false
	 */
	private static boolean canPlaceOnThisSide(World theWorld, EntityPlayer entityPlayer, int side) {
		ItemStack itemStack = entityPlayer.getHeldItem();
		ItemBlock itemBlock = ((itemStack != null && itemStack.getItem() instanceof ItemBlock) 
				? (ItemBlock) itemStack.getItem() : DIRT);
		boolean valid = itemBlock.canPlaceItemBlockOnSide(theWorld, placePosition.x,
				placePosition.y, placePosition.z, side, entityPlayer, itemStack);
		//Log.finer("canPlaceOnThisSide " + placePosition + " @ side " + side + " --> " + valid);
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
	 * @return true if there is a block on the place side of the place position
	 */
	private static boolean didItPlaceABlock (World theWorld) {
		Coordinate newC = placePosition.adjacentOnSide(placeSide);
		return (theWorld.getBlockId (newC.x, newC.y, newC.z) != 0);
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
			isValid = (canPlaceOnThisSide(theWorld, entityPlayer, placeSide));
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
	 * Check if the place position is a replaceable block if adjust accordingly
	 * @return true if the place position was modified
	 */
	private static boolean checkReplaceable () {
		return false;
	}
}
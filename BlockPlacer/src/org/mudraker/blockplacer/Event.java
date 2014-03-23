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
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.lwjgl.opengl.GL11;
import org.mudraker.Log;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * BlockPlacer Client Side Event handler.
 * 
 * <p>Handles {@link PlayerInteractEvent} and {@link DrawBlockHighLightEvent} events
 * Overrides selection box to show the {@link BlockPlacer} place location and catches player 
 * right clicks to actually places the block at the BlockPlacer location.</p>
 *  
 * <p>Pattern: Singleton Event Subscriber</p>
 * 
 * <p>1.7.2 MovingObjectPosition and SubscribeEvent & render global changes</p>
 * 
 * @author MudRaker
 */
@SideOnly(Side.CLIENT)
public class Event {
	// Singleton instance enforcement
	private final static Event instance = new Event(); // Singleton instance
	public static Event getInstance() { return instance; } // Return singleton for access
	private Event() {} // Singleton: Prevent additional instantiation
	
	private World theWorld; // allows base minecraft functions to be used unchanged.
	private Coordinate drawPosition = new Coordinate(0,0,0); // reusable structure object

	/** MINECRAFT constant: Dimension number of the nether */
	public static final int DIMENSION_NETHER = -1;
	/**
	 * Override default right click action if {@link BlockPlacer} is enabled and the player 
	 * is holding a valid {@link ItemBlock} that can be placed.
	 * @param event details provided by Forge.
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onPlayerInteraction(PlayerInteractEvent event) {
		EntityPlayer entityPlayer = event.entityPlayer;

		// Ensure only runs on client side when BlockPlacer is enabled
		if (!entityPlayer.worldObj.isRemote || !BlockPlacer.isPlaceEnabled())
			return;

		if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
			// Nothing to do if empty hands or not an ItemBlock
			if (!BlockPlacer.isHoldingItemBlock(entityPlayer)) {
				Log.finer("Default rightclick at " + event.x + "," + event.y + ","
						+ event.z + " on side " + event.face + " - no item or not ItemBlock");
				return; // let the standard code deal with it
			}

			// Cancel this event to stop the block being placed by the standard code
			event.setCanceled(true);

			// OK, place a block here then
			BlockPlacer.doRightClick (entityPlayer);
		}
	}
	
	/**
	 * Override default selection box rendering if {@link BlockPlacer} is enabled and user is not
	 * pressing the Attack or PickBlock keys (normally left and middle mouse buttons)
	 * and the user is holding an {@link ItemBlock} that can be placed.
	 * @param event details provided by Forge.
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onDrawBlockSelectionBox(DrawBlockHighlightEvent event) {
		// Draw nothing unless placeMode is activated & we are selecting a tile
		if (BlockPlacer.isPlaceEnabled() && event.target.typeOfHit == MovingObjectType.BLOCK) {
			Minecraft mc = Minecraft.getMinecraft();
			this.theWorld = mc.theWorld; // used by minecraft base functions
			
			// only render if holding ItemBlock and no other mouse buttons pressed
			if (!GameSettings.isKeyDown(mc.gameSettings.keyBindAttack)
					&& !GameSettings.isKeyDown(mc.gameSettings.keyBindPickBlock)
					&& BlockPlacer.isHoldingItemBlock(event.player)) {
				
				// establish draw position & only proceed if BlockPlacer has not auto-turned-off 
				if (BlockPlacer.establishPlacement (mc, event.player, event.target, drawPosition) != null) {
					drawSelectionBox(event.player, event.target, 0, event.partialTicks);
					event.setCanceled(true); // cancel the standard draw
				}
			}
		}
	}

	/**
	 * Draws the selection box for the player. 
	 * <p>Args: entityPlayer, rayTraceHit, i, partialTickTime 
	 * <p>Based on net.minecraft.client.renderer.RenderGlobal.java. 
	 * Which is called by net.minecraft.client.renderer.entityRenderer.java.
	 * @@MCVERSION164@@
	 */
    public void drawSelectionBox(EntityPlayer par1EntityPlayer, MovingObjectPosition par2MovingObjectPosition, int par3, float par4)
    {
		// Add config instance declaration
		Config config = Config.getInstance();
        if (par3 == 0 && par2MovingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        {
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			// Replaced to configure colour and width
            //GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
            //GL11.glLineWidth(2.0F);
			if (config.frameSwapInNether && par1EntityPlayer.dimension == DIMENSION_NETHER) {
				GL11.glColor4f(config.frameGreenF, config.frameRedF, config.frameBlueF, config.frameAlphaF);
			} else {
				GL11.glColor4f(config.frameRedF, config.frameGreenF, config.frameBlueF, config.frameAlphaF);
			}
			GL11.glLineWidth((float)config.frameLineWidth);
			// end change
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(false);
			// Replaced to configure expansion
            //float f1 = 0.002F;
			float f1 = config.frameExpand;
			// end change
            Block block = this.theWorld.getBlock(par2MovingObjectPosition.blockX, par2MovingObjectPosition.blockY, par2MovingObjectPosition.blockZ);

            if (block.getMaterial() != Material.air)
            {
				// Removed - Don't need to get block specific bounds
                // block.setBlockBoundsBasedOnState(this.theWorld, par2MovingObjectPosition.blockX, par2MovingObjectPosition.blockY, par2MovingObjectPosition.blockZ);
                double d0 = par1EntityPlayer.lastTickPosX + (par1EntityPlayer.posX - par1EntityPlayer.lastTickPosX) * (double)par4;
                double d1 = par1EntityPlayer.lastTickPosY + (par1EntityPlayer.posY - par1EntityPlayer.lastTickPosY) * (double)par4;
                double d2 = par1EntityPlayer.lastTickPosZ + (par1EntityPlayer.posZ - par1EntityPlayer.lastTickPosZ) * (double)par4;
				// Replaced - Don't need to get block specific box
				// But do need to use the BlockPlacer position instead.
                // drawOutlinedBoundingBox(block.getSelectedBoundingBoxFromPool(this.theWorld, par2MovingObjectPosition.blockX, par2MovingObjectPosition.blockY, par2MovingObjectPosition.blockZ).expand((double)f1, (double)f1, (double)f1).getOffsetBoundingBox(-d0, -d1, -d2), -1);
				this.drawOutlinedBoundingBox(this.getFullBoundingBoxFromPool(drawPosition.x, drawPosition.y, drawPosition.z)
						.expand((double) f1, (double) f1, (double) f1)
						.getOffsetBoundingBox(-d0, -d1, -d2), -1);
				// end change
                
            }
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

	/**
	 * Draws lines for the edges of the bounding box. 
	 * <p>UNCHANGED private method from net.minecraft.client.renderer.RenderGlobal.java.</p>
	 * @@MCVERSION164@@
	 */
    public static void drawOutlinedBoundingBox(AxisAlignedBB p_147590_0_, int p_147590_1_)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(3);

        if (p_147590_1_ != -1)
        {
            tessellator.setColorOpaque_I(p_147590_1_);
        }

        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.minY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.minY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.minY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.minY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.minY, p_147590_0_.minZ);
        tessellator.draw();
        tessellator.startDrawing(3);

        if (p_147590_1_ != -1)
        {
            tessellator.setColorOpaque_I(p_147590_1_);
        }

        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.maxY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.maxY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.maxY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.maxY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.maxY, p_147590_0_.minZ);
        tessellator.draw();
        tessellator.startDrawing(1);

        if (p_147590_1_ != -1)
        {
            tessellator.setColorOpaque_I(p_147590_1_);
        }

        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.minY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.maxY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.minY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.maxY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.minY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.maxY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.minY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.maxY, p_147590_0_.maxZ);
        tessellator.draw();
    }


	/**
	 * Returns the FULL block bounding box of the wired rectangular prism to render. 
	 * <p>See net.minecraft.block.Block.java/getSelectedBoundingBoxFromPool for insight.
	 * @param x is the integer world X coordinate
	 * @param y is the integer world Y coordinate
	 * @param z is the integer world Z coordinate
	 * @returns FULL cubic block bounding box for the coordinates
	 */
	private AxisAlignedBB getFullBoundingBoxFromPool(int x, int y, int z) {
		// Optimises bounding box to be full block cube (min=0, max=1.0F)
		return AxisAlignedBB.getAABBPool().getAABB((double) x, (double) y, (double) z,
				(double) (x + 1), (double) (y + 1), (double) (z + 1));
	}
}
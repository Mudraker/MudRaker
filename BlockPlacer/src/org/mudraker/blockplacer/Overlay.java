/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.blockplacer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;

import org.lwjgl.opengl.GL11;
import org.mudraker.Util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * BlockPlacer GUI overlay class.
 * <p>Handles drawing the BlockPlacer icon and text when BlockPlacer is enabled</p>
 * 
 * <p>Pattern: Singleton Event Subscriber </p>
 * 
 * @author MudRaker
 */
@SideOnly(Side.CLIENT)
public class Overlay extends Gui {
	// Singleton instance enforcement
	private final static Overlay instance = new Overlay(); // Singleton instance
	public static Overlay getInstance() { return instance; } // Return singleton for access
	private Overlay() {} // Singleton: Prevent additional instantiation

	/** Location of the BlockPlacer icon */
	private static final ResourceLocation ICON = new ResourceLocation(
			ModInfo.ID.toLowerCase(), ModInfo.GUI_PATH+"PlaceIcon.png");

	// UI HARD Constants - must match content of texture file
	public static final int SMALL_ICON_SIZE = 18;
	public static final int SMALL_ICON_OFFSET = 0;
	public static final int LARGE_ICON_SIZE = 27;
	public static final int LARGE_ICON_OFFSET = 20;
	
	// Mouse buttons constants
	public static final int MOUSE_NONE = -1;
	public static final int MOUSE_LEFT = 0;
	public static final int MOUSE_RIGHT = 1;
	public static final int MOUSE_MIDDLE = 2;
	public static final String[] MOUSE_BUTTON_NAMES_PLUS1={"None","Left","Right","Middle"};
	
	/**
	 * Handle mouse events to detect movement for MOUSE Control mode
	 * @param event is the details of the mouse event
	 */
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onMouseEvent(MouseEvent event) {
		// Get out quick if not placing and using Mouse
		if (BlockPlacer.isPlaceEnabled() && Config.getInstance().placeControlMode == Config.ControlMode.MOUSE) {
			//Log.finer("Mouse ("+event.x+","+event.y+") delta("+event.dx+","+event.dy+") wheel="+event.dwheel+((event.button<0)?"":", "+Const.MOUSE_BUTTON_NAMES_PLUS1[event.button+1]+" "+(event.buttonstate?"DOWN":"Release"))+", nano="+event.nanoseconds);
			if (event.dx != 0 || event.dy != 0) {
				BlockPlacer.mouseShiftPlace(event.dx, event.dy);
			}
		}
	}

	
	/**
	 * Render BlockPlacer icon on POST(ALL) event if BlockPlacer is enabled.
	 * <p>Calculates position based on {@link Config} icon size and scaled location + offset.
	 * This event is called by GuiIngameForge during each frame by pre() and post()</p>
	 * @param event details provided by Forge used to identify POST(ALL) phase.
	 */
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onRenderOverlay(RenderGameOverlayEvent event) {
		Config config = Config.getInstance();

		// We draw after everything else has drawn - POST(ALL).
		// PRE events are cancelable so we ignore them.
		if (event.isCancelable() || event.type != ElementType.ALL) {
			return;
		}

		// Don't draw anything different unless in Place mode
		if (BlockPlacer.isPlaceEnabledWithWelcome()) {
			Minecraft mc = Minecraft.getMinecraft();
			int iconSize = (config.largeIcon ? LARGE_ICON_SIZE : SMALL_ICON_SIZE);
			int iconOffset = (config.largeIcon ? LARGE_ICON_OFFSET : SMALL_ICON_OFFSET);
			int screenWidth = event.resolution.getScaledWidth();
			int screenHeight = event.resolution.getScaledHeight();

			// Set position for icon based on % of screen size +/- offset.
			int xPos = Util.bound((int) (config.xScaled
					* (double) (screenWidth - iconSize) / 100.0)
					+ config.xOffset, 0, screenWidth - iconSize);
			int yPos = Util.bound((int)(config.yScaled 
					* (double)(screenHeight - iconSize) / 100.0)
					+ config.yOffset, 0, screenHeight - iconSize);

			// Draw Icon
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_LIGHTING);
			mc.renderEngine.bindTexture(ICON);
			this.drawTexturedModalRect(xPos, yPos, 0, iconOffset, iconSize, iconSize);

			// Draw relative direction
			if (BlockPlacer.shouldDrawText()) {
				// Get the string to draw
				String relativeDirection = BlockPlacer.getPlacementText(mc.thePlayer);
				
				// Figure out where to draw it
				int stringWidth = MathHelper.ceiling_float_int(mc.fontRenderer.getStringWidth(relativeDirection) * config.textScaling);
				int stringHeight = MathHelper.ceiling_float_int(mc.fontRenderer.FONT_HEIGHT * config.textScaling);
				xPos = Util.bound((int) (config.xScaled
						* (double) (screenWidth - stringWidth) / 100.0)
						+ config.xOffset, 0, screenWidth - stringWidth);
				int yPosText = yPos + iconSize + config.textSpacingGap;
				if (yPosText + stringHeight >= screenHeight) {
					yPosText = yPos - stringHeight - config.textSpacingGap;
				}
				
				// Draw the text, saving current state before changing translation & scaling matrices 
				GL11.glPushMatrix();
	            GL11.glTranslatef((float)(xPos), (float)(yPosText), 0.0F);	// sets new origin		
				GL11.glScalef(config.textScaling, config.textScaling, 1);	// scales everything drawn
				this.drawString (mc.fontRenderer, relativeDirection, 0, 0, config.textColour);
				GL11.glPopMatrix();
			}
		}
	}
}
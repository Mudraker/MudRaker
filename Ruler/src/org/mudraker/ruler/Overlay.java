package org.mudraker.ruler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
 * Ruler GUI overlay class.
 * <p>Handles drawing the Ruler co-ordinates when Ruler is held</p>
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
			ModInfo.ID.toLowerCase(), ModInfo.GUI_PATH+"RulerIcon.png");

	// UI HARD Constants - must match content of texture file
	public static final int ICON_SIZE = 32;
	public static final int ICON_OFFSET = 0;
	
	/**
	 * Handle mouse events to detect movement for drawing
	 * @param event is the details of the mouse event
	 */
	@SideOnly(Side.CLIENT)
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onMouseEvent(MouseEvent event) {
	}

	
	/**
	 * Render Ruler co-ordinates on POST(ALL) event if Ruler is held.
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

		// Check if holding ruler
		Minecraft mc = Minecraft.getMinecraft(); // client-safe
		EntityPlayer entityPlayer = mc.thePlayer;
		ItemStack heldItem = entityPlayer.getHeldItem();
		if (heldItem != null && (heldItem.getItem() instanceof RulerItem)) {
			
			//int iconSize = (config.largeIcon ? LARGE_ICON_SIZE : SMALL_ICON_SIZE);
			//int iconOffset = (config.largeIcon ? LARGE_ICON_OFFSET : SMALL_ICON_OFFSET);
			int screenWidth = event.resolution.getScaledWidth();
			int screenHeight = event.resolution.getScaledHeight();

			// Set position for icon based on % of screen size +/- offset.
			int xPos = Util.bound((int) (config.xScaled
					* (double) (screenWidth - ICON_SIZE) / 100.0)
					+ config.xOffset, 0, screenWidth - ICON_SIZE);
			int yPos = Util.bound((int)(config.yScaled 
					* (double)(screenHeight - ICON_SIZE) / 100.0)
					+ config.yOffset, 0, screenHeight - ICON_SIZE);

			// Draw Icon
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_LIGHTING);
			mc.renderEngine.bindTexture(ICON);
			this.drawTexturedModalRect(xPos, yPos, 0, ICON_OFFSET, ICON_SIZE, ICON_SIZE);
			
			NBTTagCompound ruler = RulerItem.getRuler(mc.theWorld, heldItem);
			int [] measure = RulerItem.getActiveMeasure(ruler);
			if (measure != null && measure.length > 0) {
				int n = 0;
				
				// Setup base text position
				int stringHeight = MathHelper.ceiling_float_int(mc.fontRenderer.FONT_HEIGHT * config.textScaling);
				int yPosText = yPos + ICON_SIZE + config.textSpacingGap;
				int yOffset = stringHeight + config.textSpacingGap;
				if (yPosText + stringHeight >= screenHeight) {
					yOffset = - yOffset;
					yPosText = yPos + yOffset;
				}
				
				// Draw coordinate
				for (int i = 1; i <= measure[0]; i +=3) { 
					// Get the string to draw
					String drawString = String.format("%d(%d,%d,%d)", ++n, measure[i], measure[i+1], measure[i+2]);
	
					// Figure out where to draw text
					int stringWidth = MathHelper.ceiling_float_int(mc.fontRenderer.getStringWidth(drawString) * config.textScaling);
					xPos = Util.bound((int) (config.xScaled
							* (double) (screenWidth - stringWidth) / 100.0)
							+ config.xOffset, 0, screenWidth - stringWidth);	
					
					// Draw the text, saving current state before changing translation & scaling matrices 
					GL11.glPushMatrix();
		            GL11.glTranslatef((float)(xPos), (float)(yPosText), 0.0F);	// sets new origin		
					GL11.glScalef(config.textScaling, config.textScaling, 1);	// scales everything drawn
					this.drawString (mc.fontRenderer, drawString, 0, 0, config.textColour);
					GL11.glPopMatrix();
					
					// Shift to next text line
					yPosText += yOffset;
				}
			}
		}
	}
}
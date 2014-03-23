package org.mudraker.ruler;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRulerMarker extends Render {
	// Singleton instance enforcement
	private static final RenderRulerMarker instance = new RenderRulerMarker(); // Singleton instance
	public static RenderRulerMarker getInstance() { return instance; } // Return singleton for access
	private RenderRulerMarker () { // Singleton: Prevent additional instantiation
		super();
	}
	
	private static final ResourceLocation flagTextures = new ResourceLocation(
			ModInfo.ID.toLowerCase(), ModInfo.ENTITY_PATH+"Marker.png");
	protected RulerMarkerModel markerModel = new RulerMarkerModel();
	
	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return flagTextures;
	}
    
	@Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
		this.renderMarker((EntityRulerMarker)par1Entity, par2, par4, par6, par8, par9);
	}

    public void renderMarker(EntityRulerMarker entityMarker, double x, double y, double z, float yaw, float partialTickTime)
    {
        // save matrix, bind texture & set origin
        GL11.glPushMatrix();
        this.bindEntityTexture(entityMarker);
        GL11.glTranslatef((float)x, (float)y, (float)z);// set origin where we are told
        GL11.glTranslatef(0.5F, 1.5F, 0.5F);        	// adjust to centre of block above
        GL11.glDisable(GL11.GL_CULL_FACE);				// seems to stop the flickering near other blocks
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);			// no idea what this does
        GL11.glScalef(1.0F, -1.0F, 1.0F); 				// flip it over, this seems to drop it a block
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);			// use full texture colours
        //GL11.glEnable(GL11.GL_ALPHA_TEST);
        float f2 = 0.0625F;								// scale 16 pixels to 1 block == 1/16
        this.markerModel.render(entityMarker, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f2);
        GL11.glPopMatrix();        
    }
    
}


package org.mudraker.ruler;

import net.minecraft.world.World;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ProxyClient extends ProxyCommon {
	@Override
	public void registerRendering() {
		// MinecraftForgeClient.preloadTexture(texture);
		RenderingRegistry.registerEntityRenderingHandler(EntityRulerMarker.class, RenderRulerMarker.getInstance());
		// registerBlockHandler
	}
	
	//public EntityRulerMarker newMarkerEntity(World world, String uuid, int xPos, int yPos, int zPos) {return null;}

	//public void destroyMarkerEntity(World world, String uuid) { }
	
	//public EntityRulerMarker getMarkerEntity(String uuid) { return null; }
}
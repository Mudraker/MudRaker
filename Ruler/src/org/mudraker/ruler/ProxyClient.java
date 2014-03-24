/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
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
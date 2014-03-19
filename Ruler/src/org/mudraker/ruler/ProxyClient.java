package org.mudraker.ruler;

import net.minecraft.entity.Entity;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ProxyClient extends ProxyCommon {
	@Override
	public void registerRendering() {
		// MinecraftForgeClient.preloadTexture(texture);
		RenderingRegistry.registerEntityRenderingHandler(EntitySlot.class, RenderSlot.getInstance());
		// registerBlockHandler
	}
}
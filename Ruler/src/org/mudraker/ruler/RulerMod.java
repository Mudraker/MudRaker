/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.ruler;

import net.minecraftforge.common.MinecraftForge;

import org.mudraker.Log;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

/**
 * Ruler mod base class.
 * <p>Forge mod initialisation and registration only.
 * Core state and function is defined in {@link Ruler}.
 * Non-enforced singleton as it is instantiated once only by Forge Mod Loader.</p>
 *
 * <p>Pattern: Forge proxyless mod.</p>
 * 
 * @author MudRaker
 */
@Mod(modid = ModInfo.ID, name = ModInfo.LONG_NAME, version = ModInfo.VERSION)
@NetworkMod(channels = { ModInfo.ID }, packetHandler = NetworkHandler.class, clientSideRequired = true, serverSideRequired = true)
public class RulerMod {

	// **************************************
	// Module housekeeping and initialisation
	// **************************************
	
	/** Forge instance variable */
	@Instance(ModInfo.ID)
	public static RulerMod instance;
	
	/** Client proxy */
	@SidedProxy(clientSide = ModInfo.CLIENT_PROXY, serverSide = ModInfo.COMMON_PROXY)
	public static ProxyCommon proxy;
	/**
	 * Forge pre-initialisation
	 * <p>Initialises log, configuration, key bindings and commands.
	 * @param event is the Forge event details
	 */
	@EventHandler
	public void prenit(FMLPreInitializationEvent event) {
		Config config = Config.getInstance();
		
		// Setup log and load configuration
		Log.init(event.getModLog());
Log.setMinInfo(true); // TODO: remove
		config.loadConfig(event.getSuggestedConfigurationFile());
		config.dumpConfig();
		
		// Key handler Initialisation
		//KeyBind.init(config.placeControlMode);
		
		// Command initialisation
		Command.getInstance();
	}

	/**
	 * Forge post-initialisation
	 * <p>Registers items and event handlers
	 * @param event is the Forge event details
	 */
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		 Config config = Config.getInstance();
		 
		 // Register items
		 RulerItem rulerItem;
		 GameRegistry.registerItem(rulerItem = new RulerItem(config.rulerItemId), "Ruler");
		 
		// Event handler registration
		MinecraftForge.EVENT_BUS.register(Overlay.getInstance());
		MinecraftForge.EVENT_BUS.register(rulerItem);
		
		// Register entities
		EntityRegistry.registerGlobalEntityID(EntityRulerMarker.class, "ruler.marker", EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerModEntity(EntityRulerMarker.class, "ruler.marker", 1, this, 64, 1, false);
		LanguageRegistry.instance().addStringLocalization("entity.mudraker.ruler.ruler.marker.name", "Ruler Marker");
		proxy.registerRendering();
	}
}

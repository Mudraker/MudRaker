/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.blockplacer;

import net.minecraftforge.common.MinecraftForge;

import org.mudraker.Log;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;

/**
 * BlockPlacer mod base class.
 * <p>Forge mod initialisation and registration only.
 * Core state and function is defined in {@link BlockPlacer}.
 * Non-enforced singleton as it is instantiated once only by Forge Mod Loader.</p>
 *
 * <p>Pattern: Forge proxyless mod.</p>
 * 
 * <p>1.6.2 Client command not intercepted by forge - convert to server command</p>
 * 
 * @author MudRaker
 */
@Mod(modid = ModInfo.ID, name = ModInfo.LONG_NAME, version = ModInfo.VERSION)
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class BlockPlacerMod {

	// **************************************
	// Module housekeeping and initialisation
	// **************************************
	
	/** Forge instance variable */
	@Instance(ModInfo.ID)
	public static BlockPlacerMod instance;
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		// Command initialisation
		event.registerServerCommand(Command.getInstance());
	}
	
	/**
	 * Forge pre-initialisation
	 * <p>Initialises log, configuration, key bindings and commands.
	 * @param event is the Forge event details
	 */
	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		Config config = Config.getInstance();
		
		// Setup log and load configuration
		Log.init(event.getModLog());
		config.loadConfig(event.getSuggestedConfigurationFile());
		config.dumpConfig();
		
		// Key handler Initialisation
		KeyBind.init(config.placeControlMode);
		
		// Command initialisation
		// Command.getInstance();
	}

	/**
	 * Forge post-initialisation
	 * <p>Registers event handlers
	 * @param event is the Forge event details
	 */
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// Event handler registration
		MinecraftForge.EVENT_BUS.register(Event.getInstance());
		MinecraftForge.EVENT_BUS.register(Overlay.getInstance());
	}
}
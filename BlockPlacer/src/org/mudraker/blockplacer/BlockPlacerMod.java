package org.mudraker.blockplacer;

import net.minecraftforge.common.MinecraftForge;

import org.mudraker.Log;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

/**
 * BlockPlacer mod base class.
 * <p>Forge mod initialisation and registration only.
 * Core state and function is defined in {@link BlockPlacer}.
 * Non-enforced singleton as it is instantiated once only by Forge Mod Loader.</p>
 *
 * <p>Pattern: Forge proxyless mod.</p>
 * 
 * @author MudRaker
 * @version %I%, %G%
 */
@Mod(modid = ModInfo.ID, name = ModInfo.NAME, version = ModInfo.VERSION)
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class BlockPlacerMod {

	// **************************************
	// Module housekeeping and initialisation
	// **************************************
	
	/** Forge instance variable */
	@Instance(ModInfo.ID)
	public static BlockPlacerMod instance;

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
		config.loadConfig(event.getSuggestedConfigurationFile());
		config.dumpConfig();
		
		// Key handler Initialisation
		KeyBind.init(config.placeControlMode);
		
		// Command initialisation
		Command.getInstance();
	}

	/**
	 * Forge post-initialisation
	 * <p>Registers event handlers
	 * @param event is the Forge event details
	 */@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// Event handler registration
		MinecraftForge.EVENT_BUS.register(Event.getInstance());
		MinecraftForge.EVENT_BUS.register(Overlay.getInstance());
	}
}
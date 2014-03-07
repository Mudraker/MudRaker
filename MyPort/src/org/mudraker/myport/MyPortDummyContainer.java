package org.mudraker.myport;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.util.HttpUtil;
import net.minecraftforge.common.Configuration;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class MyPortDummyContainer extends DummyModContainer {
	
	// Constants
	public static final String ID = "MudRaker.MyPort";
	public static final String NAME = "MyPort by MudRaker";
	public static final String VERSION = "0.2.0";
	
	// Static Mutables
	public static Logger logger;
	private static int portNumber = 51234;

	public MyPortDummyContainer() {

		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = ID;
		meta.name = NAME;
		meta.version = VERSION;
		meta.credits = "Toshba for beta testing";
		meta.authorList = Arrays.asList("MudRaker");
		meta.description = "";
		meta.url = "";
		meta.updateUrl = "";
		meta.screenshots = new String[0];
		meta.logoFile = "";
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}
	
	@Subscribe
	public void preInit(FMLPreInitializationEvent event) {

		// Setup logger
		logger = event.getModLog();
		logger.setParent(FMLLog.getLogger());		
		// Load configuration
		configLoad(event.getSuggestedConfigurationFile());
		// Say Hello
		logger.log(Level.INFO,NAME+": Override port to "+portNumber);

		/*
		// Force HttpUtil module to be loaded now - so snooper does not whinge later.
		try {
			HttpUtil.func_76181_a();
			EventHandler.class
		} catch (Exception e) {
			// HUSH
		}
		*/
	}
	
	// *******************
	// Getters and Setters
	// *******************
	/** @returns the configured LAN port number */
	public static int getPortNumber() {
		return portNumber;
	}
	
	// *************
	// Configuration
	// *************
	/**
	 * Load Forge-style configuration file
	 * Provides default values for any missing configuration items and will rewrite the
	 * configuration file to reflect these defaults if necessary.
	 * @param fileName for the configuration file
	 */
	private static void configLoad(File fileName) {
		Configuration cfg = new Configuration(fileName);

		try {
			// Load any stored configuration from its file
			cfg.load();
			portNumber = cfg.get(cfg.CATEGORY_GENERAL, "PortNumber", portNumber, 
							"Fixed port number for LAN port").getInt();			
		} catch (Exception e) {
			logger.log(Level.WARNING,"Configuration load failed");
			e.printStackTrace();
		} finally {
			// Write back any changes due to default values
			if (cfg.hasChanged()) {
				cfg.save();
			}
		}
	}
}
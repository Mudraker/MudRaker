package org.mudraker.ruler;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;

import net.minecraft.item.Item;
import net.minecraftforge.common.Property;

import org.mudraker.ConfigBase;
import org.mudraker.Log;

/**
 * Ruler Mod global configuration. 
 * <p> Loads and holds the module wide configuration for the BlockPlacer mod</p> 
 * 
 * <p>Pattern: Enforced Singleton Mutable</p>
 * 
 * @extends ConfigManager
 * @author MudRaker
 */
public class Config extends ConfigBase {
	// Singleton instance enforcement
	private static final Config instance = new Config(); // Singleton instance
	public static Config getInstance() { return instance; } // Return singleton for access
	private Config() { // Singleton: Prevent additional instantiation
		super(ModInfo.ID, "ruler", true);
	} 

	/** Configuration category name for UI related fields */
	//private static final String UI = "Ruler User Interface";
	/** Configuration category name for Control related fields */
	//private static final String CTL = "Ruler Controls";

	// ****************************************************************
	// General Configuration
	// ****************************************************************
	/** 
	 * Overrides the default logging level if not empty. 
	 * See {@link check_logLevelOverride} 
	 */
	@Cfg(value = String.class, check=true) 
	private String logLevelOverride = "";
	
	@Cfg(value = Item.class, loadOnly=true)
	public int rulerItemId = 9120;
	
	// ****************************************************************
	// UI configuration
	// ****************************************************************
	
	/** Use the large icon instead of the small one? */
	//@Cfg(cat = UI, value = boolean.class) 
	//public boolean largeIcon = true;

	// ****************************************************************
	// CONTROL configuration
	// ****************************************************************
	
	/** Chosen method for controlling BlockPlacer. See {@link ControlMode} */
	//@Cfg(cat = CTL, value = ControlMode.class)
	//public ControlMode placeControlMode = ControlMode.TOGGLEFWDBACK;
	
	// ****************************************************************
	// PUBLIC INTERFACE Methods
	// ****************************************************************

	/**
	 * Load the Forge-style configuration file for this mod. Provides default
	 * values for any missing configuration items and will rewrite the
	 * configuration file to reflect these defaults if necessary.
	 * @param fileName for the configuration file
	 */
	@Override
	public void loadConfig(File fileName) {
		Log.info("*** " + ModInfo.LONG_NAME + " Version: " + ModInfo.VERSION + " ***");
		super.loadConfig(fileName, null);
		convertConfig();
	}

	/**
	 * Reload the current Forge-style configuration file Requires that the
	 * configuration has previously been loaded.
	 * @throws IllegalStateException if configuration has not been previously loaded
	 */
	@Override
	public void reloadConfig() {
		super.reloadConfig();
		convertConfig();
	}
	
	/**
	 * Set a configuration field with the provided value
	 * @param searchName is the field name (with or without the "place" prefix)
	 * @param value is the value to set the field to
	 * @return true if the field was updated successfully, false if the value is not valid.
	 * @throws NoSuchFieldException
	 */
	@Override
	public boolean setCfgField(String searchName, String value) throws NoSuchFieldException {
		boolean b = super.setCfgField(searchName, value);
		convertConfig();
		return b;
	}

	// ****************************************************************
	// CHECK Methods
	// ****************************************************************	
	
	/**
	 * Check the value of {@link logLevelOverride} and override Logging level if set.
	 * <p>See {@link ConfigBase.doCheck}</p>
	 * @param field is the reflected field reference
	 * @param p is the Forge property information
	 * @return true if field is okay or false if invalid.
	 */
	@SuppressWarnings("unused")
	private boolean check_logLevelOverride (Field field, Property p) {
		Level newLevel;
		logLevelOverride = p.getString();
		if (!logLevelOverride.isEmpty()) {
			try {
				newLevel = Level.parse(logLevelOverride.trim().toUpperCase());
				Log.setLevel(newLevel);
				Log.fine("Config: LogLevel override to "+logLevelOverride);
				return true;
	        } catch(IllegalArgumentException e) {
				Log.fine("Config: LogLevel override "+logLevelOverride+" invalid - rewrite config as empty");
	        	p.set(logLevelOverride = "");
	        	return false;
	        }
		}
		return true;
	}
	
	// ****************************************************************
	// WORKER Methods
	// ****************************************************************
	
	/**
	 * Converts config values loaded in one format to the internal format required.
	 * <p>Must be called each time configuration is loaded.
	 */
	private void convertConfig () {
	}
}

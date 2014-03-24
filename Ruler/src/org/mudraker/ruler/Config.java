/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.ruler;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;

import net.minecraft.item.Item;
import net.minecraftforge.common.Property;

import org.mudraker.ConfigBase;
import org.mudraker.Log;
import org.mudraker.Util;

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
	private static final String UI = "Ruler User Interface";
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
	
	/** Horizontal screen location as percentage of screen width */
	@Cfg(cat = UI, value = double.class, min = 0.0D, max = 100.0D)
	public double xScaled = 100;

	/** Vertical screen location as percentage of screen height */
	@Cfg(cat = UI, value = double.class, min = 0.0D, max = 100.0D)
	public double yScaled = 20;

	/** Pixel offset to scaled horizontal location */
	@Cfg(cat = UI, value = int.class, min = -100, max = +100)
	public int xOffset = -2;

	/** Pixel offset to scaled vertical location */
	@Cfg(cat = UI, value = int.class, min = -100, max = +100)
	public int yOffset = 0;

	/** Scaling factor for ruler text (0-1) */
	public float textScaling;
	/** Internal Scaling percentage for ruler text */
	@Cfg(cat = UI, value = int.class, min = 1, max = 100)
	private int textScalingPercent = 50;

	/** Pixel gap between icon and text */
	@Cfg(cat = UI, value = int.class, min = 0, max = 20)
	public int textSpacingGap = 2;
	
	/** Ruler text colour - using hexadecimal RGBA notation */
	public int textColour;
	/** Internal text Red colour value */
	@Cfg(cat = UI, value = int.class, min = 0, max = 255)
	private int textRed = 0xff;
	/** Internal text Green colour value */
	@Cfg(cat = UI, value = int.class, min = 0, max = 255)
	private int textGreen = 0xff;
	/** Internal text Blue colour value */
	@Cfg(cat = UI, value = int.class, min = 0, max = 255)
	private int textBlue = 0xff;
	/** Internal text Alpha transparency value */
	@Cfg(cat = UI, value = int.class, min = 0, max = 255)
	private int textAlpha = 0xa0;

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
		super.loadConfig(fileName, new String[] {UI});
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
		textScaling = textScalingPercent / 100F;
		textColour = Util.rgbaColour (textRed, textGreen, textBlue, textAlpha);
	}
}

/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.blockplacer;

import java.io.File;
import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Property;

import org.apache.logging.log4j.Level;
import org.mudraker.ConfigBase;
import org.mudraker.Lang;
import org.mudraker.Log;
import org.mudraker.Util;

/**
 * BlockPlacer Mod global configuration. 
 * <p> Loads and holds the module wide configuration for the BlockPlacer mod</p> 
 * 
 * <p>Pattern: Enforced Singleton Mutable</p>
 * 
 * <p>1.7.2 Config class changes & chat messages</p>
 * 
 * @extends ConfigManager
 * @author MudRaker
 */
public class Config extends ConfigBase {
	// Singleton instance enforcement
	private static final Config instance = new Config(); // Singleton instance
	public static Config getInstance() { return instance; } // Return singleton for access
	private Config() { // Singleton: Prevent additional instantiation
		super(ModInfo.ID, "place", true);
	}

	/** Configuration category name for UI related fields */
	private static final String UI = "BlockPlacer User Interface";
	/** Configuration category name for Control related fields */
	private static final String CTL = "BlockPlacer Controls";

	/**
	 * BlockPlacer control modes. 
	 * <p>See values {@link #SINGLEKEY}, {@link #TOGGLEFWD}, {@link #FORWARDBACK}, 
	 * {@link #TOGGLEFWDBACK}, {@link #ROTATE3D}, {@link #MOUSE}</p>
	 */
	public enum ControlMode {
		/** One key both activates BlockPlacer and switches place side (forces AutoOff option) */
		SINGLEKEY,
		/** One key toggles BlockPlacer mode on/off, another switches place side */
		TOGGLEFWD,
		/**
		 * One key activates BlockPlacer and switches place side 'forwards'.
		 * Another key activates BlockPlacer and switches place side 'backwards'. 
		 * (forces AutoOff option)
		 */
		FORWARDBACK,
		/**
		 * One key toggles BlockPlacer mode on/off, one switches place side
		 * 'forwards', and a third switches place side 'backwards'
		 */
		TOGGLEFWDBACK,
		/**
		 * One key toggles BlockPlacer mode on/off, 2 keys rotate location
		 * horizontally and 2 keys rotate location vertically
		 */
		ROTATE3D,
		/** One key toggles BlockPlacer mode on/off, mouse movement sets place location */
		MOUSE,
	}

	// ****************************************************************
	// General Configuration
	// ****************************************************************
	
	/** 
	 * Overrides the default logging level if not empty. 
	 * See {@link check_logLevelOverride} 
	 */
	@Cfg(value = String.class, check=true) 
	private String logLevelOverride = "";
	
	// ****************************************************************
	// UI configuration
	// ****************************************************************
	
	/** Use the large icon instead of the small one? */
	@Cfg(cat = UI, value = boolean.class) 
	public boolean largeIcon = true;

	/** Horizontal screen location as percentage of screen width */
	@Cfg(cat = UI, value = double.class, min = 0.0D, max = 100.0D)
	public double xScaled = 0;

	/** Vertical screen location as percentage of screen height */
	@Cfg(cat = UI, value = double.class, min = 0.0D, max = 100.0D)
	public double yScaled = 7.5;

	/** Pixel offset to scaled horizontal location */
	@Cfg(cat = UI, value = int.class, min = -100, max = +100)
	public int xOffset = 2;

	/** Pixel offset to scaled vertical location */
	@Cfg(cat = UI, value = int.class, min = -100, max = +100)
	public int yOffset = 0;

	/** Draw the placement facing and relative direction text? */
	@Cfg(cat = UI, value = boolean.class)
	public boolean drawFacingText = true;

	/** Scaling factor for placement text (0-1) */
	public float textScaling;
	/** Internal Scaling percentage for placement text */
	@Cfg(cat = UI, value = int.class, min = 1, max = 100)
	private int textScalingPercent = 50;

	/** Pixel gap between icon and text */
	@Cfg(cat = UI, value = int.class, min = 0, max = 20)
	public int textSpacingGap = 2;

	/** Placer text colour - using hexadecimal RGBA notation */
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

	/** Placer frame colour - fractional Red element (0-1) */
	public float frameRedF;
	/** Internal frame Red colour value */
	@Cfg(cat = UI, value = int.class, min = 0, max = 255)
	private int frameRed = 0xff;

	/** Placer frame colour - fractional green element (0-1) */
	public float frameGreenF;
	/** Internal frame Green colour value */
	@Cfg(cat = UI, value = int.class, min = 0, max = 255)
	private int frameGreen = 0x00;

	/** Placer frame colour - fractional blue element (0-1) */
	public float frameBlueF;
	/** Internal frame Blue colour value */
	@Cfg(cat = UI, value = int.class, min = 0, max = 255)
	private int frameBlue = 0x00;

	/** Placer frame colour - fractional alpha/transparency element (0-1) */
	public float frameAlphaF;
	/** Internal frame Alpha colour value */
	@Cfg(cat = UI, value = int.class, min = 0, max = 255)
	private int frameAlpha = 0x80;

	/** Placer frame line width - effectively integer values only */
	@Cfg(cat = UI, value = int.class, min = 1, max = 10)
	public int frameLineWidth = 2;

	/** Expansion factor for Placer frame as a fraction of one block width */
	@Cfg(cat = UI, value = float.class, min = 0.0F, max = 0.05F)
	public float frameExpand = 0.002F;

	/** Swap Red and Green colour values when in the Nether? */
	@Cfg(cat = UI, value = boolean.class)
	public boolean frameSwapInNether = true;

	// ****************************************************************
	// CONTROL configuration
	// ****************************************************************
	
	/** Chosen method for controlling BlockPlacer. See {@link ControlMode} */
	@Cfg(cat = CTL, value = ControlMode.class)
	public ControlMode placeControlMode = ControlMode.TOGGLEFWDBACK;
	
	/**
	 * After placing a block, AutoRepeat mode causes BlockPlacer to
	 * automatically set the default place location to be the same side of the
	 * block just placed. This allows a row or column of blocks to be placed
	 * without having to move the cursor. Note that it is still limited by
	 * player reach.
	 */
	@Cfg(cat = CTL, value = boolean.class)
	public boolean placeAutoRpt = true;
	
	/**
	 * BlockPlacer mode is automatically turned off when the cursor is moved to
	 * a different block. Also applies after a block is successfully placed
	 * unless {@link placeAutoRpt} mode is enabled.
	 * <p>Forced ON by control modes that do not have a toggle key!</p>
	 */
	@Cfg(cat = CTL, value = boolean.class, check=true)
	public boolean placeAutoOff = false;
		
	/**
	 * BlockPlacer mode is automatically ended instead of just Reset when there are 
	 * no more sides of the original block to place on (if autorpt is off) 
	 * or the placing has repeated to the extreme of the player reach (if autorpt is on). 
	 * <p>Only relevant if {@link placeAutoOff} is not enabled.
	 */
	@Cfg(cat = CTL, value = boolean.class)
	public boolean placeAutoEnd = false;

	/**
	 * Play a sound if forced to Reset BlockPlacer because there is no valid place location. 
	 * Only relevant if {@link placeAutoOff} is not enabled.
	 */
	@Cfg(cat = CTL, value = boolean.class)
	public boolean placeResetSound = true;
	
	/**
	 * BlockPlacer mode automatically chooses the starting place side based on
	 * likely scenarios to give an 'intelligent' default.
	 */
	@Cfg(cat = CTL, value = boolean.class)
	public boolean placeSmartStart = true;

	/**
	 * Defines the size of small 'wobble' mouse movements that are ignored when
	 * the {@link placeControlMode} is MOUSE. If the shift on a mouse axis is this or less,
	 * it is treated as not moving on that axis. This makes it easier to do purely
	 * vertical or horizontal movements.
	 */
	@Cfg(cat = CTL, value = int.class, min=0, max=10)
	public int mouseWobble = 1;

	/**
	 * Minimum movement threshold for the mouse to trigger a change in the
	 * placement location when the control mode is MOUSE. If the sum of movement
	 * on both axes within 1 tick is less than this, it is ignored.
	 */
	@Cfg(cat = CTL, value = int.class, min=0, max=15)
	public int mouseThreshold = 4;
	
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
		super.loadConfig(fileName, new String[] {UI, CTL});
		convertConfig();
	}

	/**
	 * Reload the current Forge-style configuration file.
	 * Requires that the configuration has previously been loaded.
	 * Reloads key bindings if placeControlMode is changed.
	 * @throws IllegalStateException if configuration has not been previously loaded
	 */
	@Override
	public void reloadConfig() {
		BlockPlacer.setPlaceEnabled(false);
		super.reloadConfig();
		convertConfig();
		if (KeyBind.reInit(placeControlMode)) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.thePlayer != null) 
				Util.addChat(mc.thePlayer, Lang.getMsgParams(ModInfo.ID, "reload.keybind", ModInfo.SHORT_NAME));
		}
	}

	/**
	 * Set a configuration field with the provided value
	 * Reloads key bindings if placeControlMode is changed.
	 * @param searchName is the field name (with or without the "place" prefix)
	 * @param value is the value to set the field to
	 * @return true if the field was updated successfully, false if the value is not valid.
	 * @throws NoSuchFieldException
	 */
	@Override
	public boolean setCfgField(String searchName, String value) throws NoSuchFieldException {
		boolean b = super.setCfgField(searchName, value);
		convertConfig();
		
		// handle changes in placeControlMode here
		if (b && searchName.equalsIgnoreCase("controlMode")) {
			if (!placeAutoOff && (placeControlMode == ControlMode.SINGLEKEY || placeControlMode == ControlMode.FORWARDBACK)) {
				placeAutoOff = true;
				Log.fine("Config: placeAutoOff forced ON for control mode SINGLEKEY or FORWARDBACK");
			}
			if (KeyBind.reInit(placeControlMode)) {
				Minecraft mc = Minecraft.getMinecraft();
				if (mc.thePlayer != null) 
					Util.addChat(mc.thePlayer, Lang.getMsgParams(ModInfo.ID, "reload.keybind", ModInfo.SHORT_NAME));
			}
		}
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
			if (Log.setLevel(logLevelOverride)) {
				Log.fine("Config: LogLevel override to "+logLevelOverride);
				return true;
			} else {
				Log.fine("Config: LogLevel override "+logLevelOverride+" invalid - rewrite config as empty");
	        	p.set(logLevelOverride = "");
	        	return false;
	        }
		}
		return true;
	}


	/**
	 * Check the value of {@link placeAutoOff} and force true if needed based on control mode
	 * <p>See {@link ConfigBase.doCheck}</p>
	 * @param field is the reflected field reference
	 * @param p is the Forge property information
	 * @return true if field is okay or false if invalid.
	 */
	@SuppressWarnings("unused")
	private boolean check_placeAutoOff (Field field, Property p) {
		placeAutoOff = p.getBoolean(placeAutoOff);
		if (!placeAutoOff && (placeControlMode == ControlMode.SINGLEKEY || placeControlMode == ControlMode.FORWARDBACK)) {
			p.set(placeAutoOff = true);
			Log.fine("Config: placeAutoOff must be ON for control mode SINGLEKEY or FORWARDBACK - rewrite config");
			return false;
		}
		return true;
	}
	
	// ****************************************************************
	// WORKER Methods
	// ****************************************************************
	
	/**
	 * Converts config values loaded in one format to the internal format required.
	 * <p>Must be called each time configuration is loaded, reloaded or changed.
	 */
	private void convertConfig () {
		textScaling = textScalingPercent / 100F;
		textColour = Util.rgbaColour (textRed, textGreen, textBlue, textAlpha);
		frameRedF = frameRed / 255.0F;
		frameGreenF = frameGreen / 255.0F;
		frameBlueF = frameBlue / 255.0F;
		frameAlphaF = frameAlpha / 255.0F;
	}
}
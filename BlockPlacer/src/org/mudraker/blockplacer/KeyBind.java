/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.blockplacer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;
import org.mudraker.Lang;
import org.mudraker.Log;
import org.mudraker.blockplacer.Config.ControlMode;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * BlockPlacer keyboard handler
 * <p>Registers key bindings according to the control mode {@link Config.Mode}
 * and handles the incoming keystrokes.</p>
 * 
 * <p>Pattern: Singleton Event handler</p>
 * 
 * <p>1.7.2 No forge keyRegistry - total rewrite
 * 
 * @author MudRaker
 */
@SideOnly(Side.CLIENT)
public class KeyBind {
	// Singleton instance enforcement
	private final static KeyBind instance = new KeyBind(); // Singleton instance
	public static KeyBind getInstance() { return instance; } // Return singleton for access
	
	/** Currently initialised control mode */
	private ControlMode boundControlMode;

	/** Key bindings for this mod */
	private KeyBinding keyRotvc;
	private KeyBinding keyRotva;
	private KeyBinding keyRothc;
	private KeyBinding keyRotha;
	private KeyBinding key3dToggle;
	private KeyBinding keyNext;
	private KeyBinding keyPrevious;
	private KeyBinding keyToggle; 
	private KeyBinding modKeyBindings[];
	private boolean modKeyRegistered[];
	
    // Keyboard controls in order loaded into the array
	// WARNING: The ROTxx controls are used to directly lookup arrays and must not be changed.
	public static final byte KEY_ROTVC	  = 0;
	public static final byte KEY_ROTVA    = 1;
	public static final byte KEY_ROTHC    = 2;
	public static final byte KEY_ROTHA    = 3;
	public static final byte KEY_3DTOGGLE = 4;
	public static final byte KEY_FORWARD  = 5;
	public static final byte KEY_BACKWARD = 6;
	public static final byte KEY_TOGGLE   = 7;
	
	/**
	 * Singleton constructor
	 * Build potential key binding list
	 */
	private KeyBind() {
		String category = Lang.getKeyCategory(ModInfo.ID, "main");
		keyRotvc 	= new KeyBinding(Lang.getKeyName(ModInfo.ID, "rotvc"), Keyboard.KEY_UP, category);
		keyRotva 	= new KeyBinding(Lang.getKeyName(ModInfo.ID, "rotva"), Keyboard.KEY_DOWN, category);
		keyRothc 	= new KeyBinding(Lang.getKeyName(ModInfo.ID, "rothc"), Keyboard.KEY_LEFT, category);
		keyRotha 	= new KeyBinding(Lang.getKeyName(ModInfo.ID, "rotha"), Keyboard.KEY_RIGHT, category);
		key3dToggle = new KeyBinding(Lang.getKeyName(ModInfo.ID, "3dtoggle"), Keyboard.KEY_PRIOR, category);
		keyNext  	= new KeyBinding(Lang.getKeyName(ModInfo.ID, "next"), Keyboard.KEY_F, category);
		keyPrevious	= new KeyBinding(Lang.getKeyName(ModInfo.ID, "previous"), Keyboard.KEY_G, category);
		keyToggle	= new KeyBinding(Lang.getKeyName(ModInfo.ID, "toggle"), Keyboard.KEY_R, category); 		
		modKeyBindings = new KeyBinding[] {keyRotvc, keyRotva, keyRothc, keyRotha, key3dToggle, keyNext, keyPrevious, keyToggle};
		modKeyRegistered = new boolean[] {false, false, false, false, false, false, false, false};
	}
	
	/**
	 * Initialises the key handler for the provided mode {@link Config.ControlMode}
	 * @param controlMode determines the keys bindings and method of control used
	 */
	public static void init(Config.ControlMode controlMode) {
		instance.prepareBindings (controlMode);
	}

	/**
	 * REInitialises the key handler for the provided mode {@link Config.ControlMode}
	 * <p>No longer unloads previous key bindings but will not use them</p>
	 * @param controlMode determines the keys bindings and method of control used
	 * @returns true if new key bindings were loaded
	 */
	public static boolean reInit(Config.ControlMode controlMode) {
		KeyBind kb = instance;
		if (controlMode != kb.boundControlMode) {
			Minecraft mc = Minecraft.getMinecraft();
			Log.info("Re-initialise key bindings");
			kb.prepareBindings (controlMode);
			return true;
		}
		return false;
	}
		
	/**
	 * Builds the BlockPlacer key bindings 
	 * @param controlMode is the control mode to build key bindings for 
	 */
	private void prepareBindings(Config.ControlMode controlMode) {
		
		this.boundControlMode = controlMode;
		Log.fine("BlockPlacer control mode initialised: " + controlMode.toString());
		
		switch (controlMode) {
		case SINGLEKEY:
			registerKeys (KEY_FORWARD);
			break;
		case TOGGLEFWD:
			registerKeys (KEY_FORWARD, KEY_TOGGLE);
			break;
		case FORWARDBACK:
			registerKeys (KEY_FORWARD, KEY_BACKWARD);
			break;
		case TOGGLEFWDBACK:
			registerKeys (KEY_FORWARD, KEY_BACKWARD, KEY_TOGGLE);
			break;
		case ROTATE3D:
			registerKeys (KEY_ROTVC, KEY_ROTVA, KEY_ROTHC, KEY_ROTHA, KEY_3DTOGGLE);
			break;
		case MOUSE:
			registerKeys (KEY_TOGGLE);			
			break;
		default:
			Log.warn ("No valid control mode configuration - functions inaccessible!");
		}
	}
	
	/**
	 * Registers the provided list of keys
	 * @param keys are the keys to be registered
	 */
	private void registerKeys (byte ... keys) {
		for (byte key : keys) {
			if (!modKeyRegistered[key]) {
				ClientRegistry.registerKeyBinding(modKeyBindings[key]);
				modKeyRegistered[key] = true;
			}
		}
	}
	
	/**
	 * Identifies control function of the released key from the key map
	 * and invokes the processing of that control.
	 * @see cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler#keyUp(java.util.EnumSet, net.minecraft.client.settings.KeyBinding, boolean)
	 */
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onKeyInputEvent (KeyInputEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc == null || !mc.inGameHasFocus || mc.currentScreen != null) {
			return; // ignore if not in main game screen
		}

		Log.finer("KeyInputEvent");
		switch (boundControlMode) {
		case SINGLEKEY:
		case TOGGLEFWD:
		case FORWARDBACK:
		case TOGGLEFWDBACK:
			boolean autoOn = (boundControlMode == Config.ControlMode.SINGLEKEY 
						   || boundControlMode == Config.ControlMode.FORWARDBACK);
			if (keyToggle.isPressed())
				BlockPlacer.togglePlaceEnabled();
			else if (keyNext.isPressed())
				BlockPlacer.adjustPlace(autoOn, true);
			else if (keyPrevious.isPressed())
				BlockPlacer.adjustPlace(autoOn, false);
			break;
		case ROTATE3D:
			if (key3dToggle.isPressed())
				BlockPlacer.togglePlaceEnabled();
			else if (keyRotvc.isPressed())
				BlockPlacer.rotatePlace(KEY_ROTVC);
			else if (keyRotva.isPressed())
				BlockPlacer.rotatePlace(KEY_ROTVA);
			else if (keyRothc.isPressed())
				BlockPlacer.rotatePlace(KEY_ROTHC);
			else if (keyRotha.isPressed())
				BlockPlacer.rotatePlace(KEY_ROTHA);
			break;
		case MOUSE:
			if (keyToggle.isPressed())
				BlockPlacer.togglePlaceEnabled();
			break;
		default:
			assert(false);
		}
	}
}
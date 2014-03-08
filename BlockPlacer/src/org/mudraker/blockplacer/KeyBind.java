package org.mudraker.blockplacer;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;
import org.mudraker.Lang;
import org.mudraker.Log;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * BlockPlacer keyboard handler
 * <p>Registers key bindings according to the control mode {@link Config.Mode}
 * and handles the incoming keystrokes.</p>
 * <p>Abuses the Forge key handler to allow keys to be REloaded</p>
 * 
 * <p>Pattern: Singleton Event handler</p>
 * 
 * @author MudRaker
 */
@SideOnly(Side.CLIENT)
public class KeyBind extends KeyHandler {
	// Singleton instance enforcement
	private final static KeyBind instance = new KeyBind(); // Singleton instance
	public static KeyBind getInstance() { return instance; } // Return singleton for access
	
	/** Singleton constructor: Instantiate without key bindings and fix later */
	private KeyBind() { super(new KeyBinding[] {}, new boolean [] {}); } 
	
	/** Only process client loop ticks */
	private static final EnumSet tickTypes = EnumSet.of(TickType.CLIENT);
	
	/** Translates key names into a key number for lookup */
	private Map<String, Byte> keyMap = new HashMap<String, Byte>();
	
	/** Current controlMode that was used to bind the keys */
	private Config.ControlMode boundControlMode;
	
	/** Copy of the base Minecraft key bindings that can be restored when reloading. */
	private KeyBinding baseKeyBindings[];

    // Keyboard controls
	// WARNING: The ROTxx controls are used to directly lookup arrays and must not be changed.
	public static final byte KEY_ROTVC	 = 0;
	public static final byte KEY_ROTVA    = 1;
	public static final byte KEY_ROTHC    = 2;
	public static final byte KEY_ROTHA    = 3;
	public static final byte KEY_PLACE    = 4;
	public static final byte KEY_FORWARD  = 5;
	public static final byte KEY_BACKWARD = 6;
	public static final byte KEY_TOGGLE   = 7;
	
	/** Keyboard control names for lookup in the language files */
	private static final String keyNames[] = {
		"rotvc", "rotva", "rothc", "rotha", "3dtoggle", "next", "previous", "toggle", 
	};
	
	/** Default key codes for the keyboard controls */
	private static final int keyCodes[] = {
		Keyboard.KEY_UP, Keyboard.KEY_DOWN, Keyboard.KEY_LEFT, Keyboard.KEY_RIGHT, Keyboard.KEY_PRIOR,
		Keyboard.KEY_F, Keyboard.KEY_G, Keyboard.KEY_R,
	};
	
	/**
	 * Initialises the key handler for the provided mode {@link Config.ControlMode}
	 * @param controlMode determines the keys bindings and method of control used
	 */
	@SideOnly(Side.CLIENT)
	public static void init(Config.ControlMode controlMode) {
		Minecraft mc = Minecraft.getMinecraft();
		KeyBind kb = instance;
		kb.baseKeyBindings = mc.gameSettings.keyBindings; // save base bindings
		kb.buildBindings (controlMode);
		KeyBindingRegistry.registerKeyBinding(kb);
	}

	/**
	 * REInitialises the key handler for the provided mode {@link Config.ControlMode}
	 * <p>*WARNING* Forces reload of ALL key bindings into Minecraft settings!</p>
	 * @param controlMode determines the keys bindings and method of control used
	 * @returns true if key bindings were reloaded
	 */
    @SuppressWarnings("deprecation")
    @SideOnly(Side.CLIENT)
	public static boolean reInit(Config.ControlMode controlMode) {
		KeyBind kb = instance;
		if (controlMode != kb.boundControlMode) {
			Minecraft mc = Minecraft.getMinecraft();
			Log.info("Re-initialise key bindings");
			mc.gameSettings.keyBindings = kb.baseKeyBindings; // restore originals first
			kb.buildBindings (controlMode);
			KeyBindingRegistry.instance().uploadKeyBindingsToGame(mc.gameSettings);
			return true;
		}
		return false;
	}
		
	/**
	 * Builds the BlockPlacer key bindings 
	 * @param controlMode is the control mode to build key bindings for 
	 */
	@SideOnly(Side.CLIENT)
	private void buildBindings(Config.ControlMode controlMode) {
		
		// Initialise in case reloading
		this.keyMap.clear();
		this.keyBindings = null;
		this.repeatings = null;
		this.boundControlMode = controlMode;
		Log.fine("BlockPlacer control mode initialised: " + controlMode.toString());
		
		switch (controlMode) {
		case SINGLEKEY:
			bindKeys (KEY_FORWARD);
			break;
		case TOGGLEFWD:
			bindKeys (KEY_FORWARD, KEY_TOGGLE);
			break;
		case FORWARDBACK:
			bindKeys (KEY_FORWARD, KEY_BACKWARD);
			break;
		case TOGGLEFWDBACK:
			bindKeys (KEY_FORWARD, KEY_BACKWARD, KEY_TOGGLE);
			break;
		case ROTATE3D:
			bindKeys (KEY_ROTVC, KEY_ROTVA, KEY_ROTHC, KEY_ROTHA, KEY_PLACE);
			break;
		case MOUSE:
			bindKeys (KEY_TOGGLE);			
			break;
		default:
			Log.warn ("No valid control mode configuration - functions inaccessible!");
		}
		
		// Forcibly reset the length of KeyHandler keyDown array
		this.keyDown = new boolean[keyBindings.length];
	}

	/** Null method to override the abstract one */
	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {}
	
	/**
	 * Identifies control function of the released key from the key map
	 * and invokes the processing of that control.
	 * @see cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler#keyUp(java.util.EnumSet, net.minecraft.client.settings.KeyBinding, boolean)
	 */
	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		Minecraft mc = Minecraft.getMinecraft();
		if (!tickEnd || mc == null || !mc.inGameHasFocus || mc.currentScreen != null) {
			return; // ignore tickStart & if not in main game screen
		}
		
		Byte key = keyMap.get(kb.keyDescription);
		Log.finer("KeyboardUp "+kb.keyDescription+" ("+key+")");
		
		switch (boundControlMode) {
		case SINGLEKEY:
		case TOGGLEFWD:
		case FORWARDBACK:
		case TOGGLEFWDBACK:
			boolean autoOn = (boundControlMode == Config.ControlMode.SINGLEKEY 
						   || boundControlMode == Config.ControlMode.FORWARDBACK);
			switch (key) {
			case KEY_FORWARD:
				BlockPlacer.adjustPlace(autoOn, true);
				break;
			case KEY_BACKWARD:
				BlockPlacer.adjustPlace(autoOn, false);
				break;
			case KEY_TOGGLE:
				BlockPlacer.togglePlaceEnabled();
				break;
			}
			break;
		case ROTATE3D:
			switch (key) {
			case KEY_ROTVC:
			case KEY_ROTVA:
			case KEY_ROTHC:
			case KEY_ROTHA:
				BlockPlacer.rotatePlace((int) key);
				break;
			case KEY_PLACE:
				BlockPlacer.togglePlaceEnabled();
				break;
			}
			break;
		case MOUSE:
			switch (key) {
			case KEY_TOGGLE:
				BlockPlacer.togglePlaceEnabled();
				break;
			}
			break;
		default:
			assert(false);
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return tickTypes;
	}

	@Override
	public String getLabel() {
		return "BlockPlacer.KeyBind";
	}
	
	/**
	 * Generates the key bindings from the provided list of key numbers
	 * Also stores the key details into the {@link keyMap} for later translation
	 * @param keys are the keys to be bound
	 */
	private void bindKeys (byte ... keys) {
		String name;
		int i = 0;
		
		// Setup empty arrays
		this.keyBindings = new KeyBinding [keys.length];
		this.repeatings = new boolean [keys.length];
		
		// Load in each key in turn
		for (byte key : keys) {
			name = Lang.getKeyName(ModInfo.ID, keyNames[key]);
			keyBindings[i] = new KeyBinding(name, keyCodes[key]);
			repeatings[i] = false;
			keyMap.put(name, key);
			i++;
		}
	}
}
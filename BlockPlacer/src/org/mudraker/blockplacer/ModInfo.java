package org.mudraker.blockplacer;

/**
 * Mod information constants to centralise definition across all mod code.
 * 
 * <p>Pattern: Enforced Static Immutable</p>
 * 
 * @author MudRaker
 */
public class ModInfo {
	private ModInfo() {} // Static: Prevent instantiation

	public static final String ID = "MudRaker.BlockPlacer";
	public static final String PKG = "org.mudraker.blockplacer";
	public static final String SHORT_NAME = "BlockPlacer";
	public static final String LONG_NAME = "Block Placer by MudRaker";
	public static final String VERSION = "1.0.0";

	// DO NOT PUT A LEADING SLASH ON THE PATH!!!
	public static final String GUI_PATH = "textures/gui/";

	// BlockPlacer does not use proxies.
	// public static final String CLIENT_PROXY = PKG + ".ClientProxy";
	// public static final String COMMON_PROXY = PKG + ".CommonProxy";
}

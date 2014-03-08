package org.mudraker.ruler;

/**
 * Mod information constants to centralise definition across all mod code.
 * 
 * <p>Pattern: Enforced Static Immutable</p>
 * 
 * @author MudRaker
 * @version %I%, %G%
 */
public class ModInfo {
	private ModInfo() {}; // Static: Prevent instantiation

	public static final String ID = "MudRaker.Ruler";
	public static final String PKG = "org.mudraker.ruler";
	public static final String TITLE = "Ruler";
	public static final String NAME = TITLE + " by MudRaker";
	public static final String VERSION = "0.0.0";

	// DO NOT PUT A LEADING SLASH ON THE PATH!!!
	public static final String GUI_PATH = "textures/gui/";

	// BlockPlacer does not use proxies.
	// public static final String CLIENT_PROXY = PKG + ".ClientProxy";
	// public static final String COMMON_PROXY = PKG + ".CommonProxy";
}

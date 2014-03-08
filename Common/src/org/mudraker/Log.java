package org.mudraker;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;

/** 
 * MudRaker Logging functions.
 * <p>Contains various helper functions for Minecraft Forge logging.</p>
 * 
 * <p>Pattern: Enforced Static Mutable</p> 
 * 
 * @author MudRaker
 * @version %I%, %G%
 */
public class Log {
	private Log() {} // Static: Prevent instantiation
	
	/** DEBUG Control - set true to force INFO as the minimum log type */
	private static boolean minInfo = false;

	/** Logger instance */
	private static Logger logger = null;

	/**
	 * Initialise logging using the provided logger and sets it as a child of the FML logger
	 * @param l identifies the logger to use
	 */
	public static void init(Logger l) {
		logger = l;
		logger.setParent(FMLLog.getLogger());
	}

	/**
	 * Override default logging level
	 * @param level for logging
	 */
	public static void setLevel(Level level) {
		logger.setLevel(level);
	}

	/**
	 * Override default logging level
	 * @param level as string for logging
	 * @return true if a valid level was provided
	 */
	public static boolean setLevel(String level) {
		try {
			Level newLevel = Level.parse(level.trim().toUpperCase());
			logger.setLevel(newLevel);
			return true;
        } catch(IllegalArgumentException e) {
        	return false;
        }
	}
	/**
	 * Force minimum level of INFO so logs to console
	 * @param level for logging
	 */
	public static void setMinInfo(boolean state) {
		minInfo = state;
	}
	
	/**
	 * Log a {@link Level.SEVERE} message
	 * <p>client/server side prefix is determined internally</p>
	 * @param message is the string to log
	 */
	public static void severe(String message) {
		logger.log(Level.SEVERE, getSide() + message);
	}

	/**
	 * Log a {@link Level.WARN} message
	 * <p>client/server side prefix is determined internally</p>
	 * @param message is the string to log
	 */
	public static void warn(String message) {
		logger.log(Level.WARNING, getSide() + message);
	}
	
	/**
	 * Log an {@link Level.INFO} message
	 * <p>client/server side prefix is determined internally</p>
	 * @param message is the string to log
	 */
	public static void info(String message) {
		logger.log(Level.INFO, getSide() + message);
	}
	
	/**
	 * Log a {@link Level.CONFIG} message
	 * <p>client/server side prefix is determined internally</p>
	 * <p>Internal configuration may override to {@link Level.INFO} for console usage</p>
	 * @param message is the string to log
	 */
	public static void cfg(String message) {
		logger.log((minInfo ? Level.INFO : Level.CONFIG), getSide() + message);
	}
	
	/**
	 * Log a {@link Level.FINE} message
	 * <p>client/server side prefix is determined internally</p>
	 * <p>Internal configuration may override to {@link Level.INFO} for console usage</p>
	 * @param message is the string to log
	 */
	public static void fine(String message) {
		logger.log((minInfo ? Level.INFO : Level.FINE), getSide() + message);
	}

	/**
	 * Log a {@link Level.FINER} message
	 * <p>client/server side prefix is determined internally</p>
	 * <p>Internal configuration may override to {@link Level.INFO} for console usage</p>
	 * @param message is the string to log
	 */
	public static void finer(String message) {
		logger.log((minInfo ? Level.INFO : Level.FINER), getSide() + message);
	}

	/**
	 * Log a {@link Level.FINEST} message
	 * <p>client/server side prefix is determined internally</p>
	 * <p>Internal configuration may override to {@link Level.INFO} for console usage</p>
	 * @param message is the string to log
	 */
	public static void finest(String message) {
		logger.log((minInfo ? Level.INFO : Level.FINEST), getSide() + message);
	}
	
	/**
	 * Used to determine the client/server side when not provided by the caller.
	 * @return a string of either "Client: " or "Server: " depending upon the side
	 */
	private static String getSide() {
		String side;
		Minecraft mc = Minecraft.getMinecraft();
		
		if (mc == null || mc.theWorld == null) {
			try {
				side = (FMLCommonHandler.instance().getSide().isClient() ? "Client: " : "Server: ");
			} catch (Exception e) {
				side = "UnknownSide: ";
			}
		} else {
			side = (mc.theWorld.isRemote ? "Client: " : "Server: ");
		}
		return side;
	}
}
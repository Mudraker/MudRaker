package org.mudraker;

import net.minecraft.client.Minecraft;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import cpw.mods.fml.common.FMLCommonHandler;

/** 
 * MudRaker Logging functions.
 * <p>Contains various helper functions for Minecraft Forge logging.</p>
 * 
 * <p>Pattern: Enforced Static Mutable</p> 
 * 
 * <p>1.7.2 converted to Log4J interface</p>
 * 
 * @author MudRaker
 */
public class Log {
	private Log() {} // Static: Prevent instantiation
	
	/** DEBUG Control - set true to force INFO as the minimum log type */
	private static boolean minInfo = false;

	/** Logger instance */
	private static Logger logger = null;

	/**
	 * Initialise logging using the provided logger
	 * @param l identifies the logger to use
	 */
	public static void init(Logger l) {
		logger = l;
	}

	/**
	 * Override default logging level
	 * @param level for logging
	 */
	public static void setLevel(Level level) {
		setLevel (logger, level);
	}

	/**
	 * Override default logging level
	 * @param level as string for logging
	 * @return true if a valid level was provided
	 */
	public static boolean setLevel(String level) {
		Level newLevel = Level.toLevel(level.trim().toUpperCase(), (Level) null);
		if (newLevel != null) setLevel(logger, newLevel);
		return (newLevel != null);
	}
	
	/**
	 * Force minimum level of INFO so logs to console
	 * @param level for logging
	 */
	public static void setMinInfo(boolean state) {
		minInfo = state;
	}
	
	/**
	 * Log a {@link Level.ERROR} message
	 * <p>client/server side prefix is determined internally</p>
	 * @param message is the string to log
	 */
	public static void error(String message) {
		logger.log(Level.ERROR, getSide() + message);
	}

	/**
	 * Log a {@link Level.WARN} message
	 * <p>client/server side prefix is determined internally</p>
	 * @param message is the string to log
	 */
	public static void warn(String message) {
		logger.log(Level.WARN, getSide() + message);
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
	 * Log a configuration message {@link Level.INFO}
	 * <p>client/server side prefix is determined internally</p>
	 * @param message is the string to log
	 */
	public static void cfg(String message) {
		logger.log(Level.INFO, getSide() + message);
	}
	
	/**
	 * Log a debug {@link Level.DEBUG} message (for compatibility)
	 * <p>client/server side prefix is determined internally</p>
	 * <p>Internal configuration may override to {@link Level.INFO} for console usage</p>
	 * @param message is the string to log
	 */
	public static void fine(String message) {
		logger.log((minInfo ? Level.INFO : Level.DEBUG), getSide() + message);
	}
	
	/**
	 * Log a debug {@link Level.DEBUG} message
	 * <p>client/server side prefix is determined internally</p>
	 * <p>Internal configuration may override to {@link Level.INFO} for console usage</p>
	 * @param message is the string to log
	 */
	public static void debug(String message) {
		logger.log((minInfo ? Level.INFO : Level.DEBUG), getSide() + message);
	}

	/**
	 * Log a detailed trace {@link Level.TRACE} message (for compatibility)
	 * <p>client/server side prefix is determined internally</p>
	 * <p>Internal configuration may override to {@link Level.INFO} for console usage</p>
	 * @param message is the string to log
	 */
	public static void finer(String message) {
		logger.log((minInfo ? Level.INFO : Level.TRACE), getSide() + message);
	}

	/**
	 * Log a detailed trace {@link Level.TRACE} message
	 * <p>client/server side prefix is determined internally</p>
	 * <p>Internal configuration may override to {@link Level.INFO} for console usage</p>
	 * @param message is the string to log
	 */
	public static void trace(String message) {
		logger.log((minInfo ? Level.INFO : Level.TRACE), getSide() + message);
	}

	/**
	 * Log a detailed trace {@link Level.FINEST} message (for compatibility)
	 * <p>client/server side prefix is determined internally</p>
	 * <p>Internal configuration may override to {@link Level.INFO} for console usage</p>
	 * @param message is the string to log
	 */
	public static void finest(String message) {
		logger.log((minInfo ? Level.INFO : Level.TRACE), getSide() + message);
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
	
	/** 
	 * Override the logging level of a given logger, return the previous level
	 * @param log is the log4J logger
	 * @param level is the log4J level
	 * @returns the logging level prior to changing it. 
	 */
	private static Level setLevel(Logger log, Level level) {
	  LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
	  Configuration conf = ctx.getConfiguration();
	  LoggerConfig lconf = conf.getLoggerConfig(log.getName());
	  Level oldLevel = lconf.getLevel();
	  lconf.setLevel(level);
	  ctx.updateLoggers(conf);
	  return oldLevel;
	}
	
	/** 
	 * Retrieve the logging level of a given logger 
	 * @param log is the log4J logger
	 * @returns the current logging level for that logger 
	 */
	private static Level getLevel(Logger log) {
	  LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
	  Configuration conf = ctx.getConfiguration();
	  LoggerConfig lconf = conf.getLoggerConfig(log.getName());
	  return lconf.getLevel();
	}
}
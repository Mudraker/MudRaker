package org.mudraker;

import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

/**
 * MudRaker MOD-MAIN Command Handler
 * <p>Provides base command handling & configuration management </p>
 * <p>Requires language strings:</p>
 * &ltmodPrefix&gt.cmd.&ltcommandName&gt.usage
 * &ltmodPrefix&gt.msg.main.player
 * <p>Pattern: Abstract Command Handler</p>
 * 
 * @author MudRaker
 */
public abstract class ClientCommandMain extends ClientCommandBase {
	
	// ****************************************************************
	// Constructors 
	// ****************************************************************
	/**
	 * Protected Constructor to set MOD and command info & configuration instance
	 * @param modPrefix is the module prefix for language strings
	 * @param modName is the module name shown to the user
	 * @param commandName is the command name to register with minecraft
	 * @param config is the configuration instance to use for reload/get/set operations
	 */
	protected ClientCommandMain (String modPrefix, String modName, String commandName, ConfigBase config) {
		super(modPrefix, modName, commandName);
		this.config = config;
	} 

	// ****************************************************************
	// Protected Data 
	// ****************************************************************
	/** Configuration instance for this command to use for reload/get/set operations */
	protected ConfigBase config;

	/** Array of log level names for tab completion. */
	protected static final String[] logLevels = {"off", "severe", "warning", "info", "config", "fine", "finer", "finest", "all"};
	
	protected static final String logOverrideField = "logleveloverride";
	
	// ****************************************************************
	// Interface Overrides 
	// ****************************************************************
	/**
	 * Processes MudRaker MOD MAIN command options:
	 * <p>CONSOLE [on | off] - forces logging to console</p>
	 * <p>LOG <log level> - sets the logging level for the session
	 * <p>RELOAD - reloads configuration from disk</p>
	 * <p>GET or ? <field> - print a configuration field value</p>
	 * <p>SET <field> <value> - sets a configuration field for the session</p>
	 * @param sender is the command sender
	 * @param aString is the string array of the parameters
	 */
	@Override
	public void processCommand(ICommandSender sender, String[] aString) {
		// Log the command
		Log.fine("ProcessCommand: "+getCommandName()+" "+Util.flattenArray(aString));

		// CONSOLE
		if (aString.length > 0 && aString[0].equalsIgnoreCase("console")) {
			Boolean state = !(aString.length > 1 && aString[1].equalsIgnoreCase("off"));
			Log.setMinInfo(state);
			outputMsg (sender, Lang.getMsgParams (modPrefix, "main.console", modName, (state?"on":"off")));
			
		// LOG
		} else if (aString.length > 0 && aString[0].equalsIgnoreCase("log")) {
			if (aString.length > 1 && Log.setLevel(aString[1])) {
				outputMsg (sender, Lang.getMsgParams(modPrefix, "main.log", modName, aString[1]));
			} else {
				throw new WrongUsageException (Lang.getCommandUsage(modPrefix, "main", "log"), new Object[] {commandName});
			}
			
		// RELOAD
		} else if (aString.length > 0 && aString[0].equalsIgnoreCase("reload")) {
			config.reloadConfig();
			config.dumpConfig();
			outputMsg (sender, Lang.getMsgParams (modPrefix, "main.reload", modName));
			
		// GET or ?
		} else if (aString.length > 0 && (aString[0].equals("?") || aString[0].equalsIgnoreCase("get"))) {
			if (aString.length < 2) {
				throw new WrongUsageException (Lang.getCommandUsage(modPrefix, "main", "get"), new Object[] {commandName});
			}
			outputMsg (sender, doGetCfgField (sender, aString));
			
		// SET
		} else if (aString.length > 0 && aString[0].equalsIgnoreCase("set")) {
			if (aString.length < 2) {
				throw new WrongUsageException (Lang.getCommandUsage(modPrefix, "main", "set"), new Object[] {commandName});
			}
			outputMsg (sender, doSetCfgField (sender, aString));
		
		// UNKNOWN HERE - hand to super to deal with!
		} else {
			super.processCommand (sender,  aString);
		}
	}

	/**
	 * Returns a list of valid tab completion options
 	 * <p>Subclasses should override and merge the super list the subclass list.
 	 * see {@link Util.mergeLists}</p>
	 * @param sender is the command sender
	 * @param aString is the array of command words
	 * @returns a List of all words that are candidate completion options for the last word
	 */
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] aString) {
		if (aString.length == 1) {
			return Util.mergeLists (super.addTabCompletionOptions(sender, aString),
					Util.getMatchesOnLastWord(aString, "console", "log", "reload", "?", "get", "set"));
		} else if (aString.length == 2 && aString[0].equalsIgnoreCase("console")) { 
        	return Util.getMatchesOnLastWord(aString, "on", "off");
		} else if (aString.length == 2 && aString[0].equalsIgnoreCase("log")) { 
	        	return Util.getMatchesOnLastWord(aString, logLevels);
		} else if (aString.length == 2 && (aString[0].equals("?") || aString[0].equalsIgnoreCase("get"))) {
    		return Util.getMatchesOnLastWord(aString, config.getCfgFields());
        } else if (aString.length >= 2 && aString[0].equalsIgnoreCase("set")) {
        	if (aString.length == 2)
        		return Util.getMatchesOnLastWord(aString, config.getCfgFields());
        	else if (aString.length == 3 && aString[1].equalsIgnoreCase(logOverrideField)) 
        		return Util.getMatchesOnLastWord(aString, logLevels);
        	else if (aString.length == 3) 
        		return Util.getMatchesOnLastWord(aString, config.getCfgFieldValues(aString[1]));
        }
    	return null;
	}
	
	// ****************************************************************
	// PROTECTED Configuration functions 
	// ****************************************************************
	/**
	 * Process a configuration field get command.
	 * @param sender is the command sender
	 * @param aString is the command word array (including the set keyword)
	 * @return a message to send to the user or null
	 * @throws usage exceptions where appropriate
	 */
	protected String doGetCfgField(ICommandSender sender, String[] aString) {
		String msg = null;
		if (aString.length > 1) {
			try {
				String value = config.getCfgField(aString[1]);
				if (value != null && !value.isEmpty())
					msg = Lang.getMsgParams(modPrefix, "main.get", modName, aString[1], value);
				else
					msg = Lang.getMsgParams(modPrefix, "main.get.empty", modName, aString[1]);
			} catch (NoSuchFieldException e) {
				throw new WrongUsageException (Lang.getCommandUsage(modPrefix, "main", "get.nofield"), 
						new Object[] {commandName, aString[1]});
			}
		}
		return msg;
	}
	
	/**
	 * Process a configuration field SET command.
	 * @param sender is the command sender
	 * @param aString is the command word array (including the set keyword)
	 * @return a message to send to the user or null
	 * @throws usage exceptions where appropriate
	 */
	protected String doSetCfgField(ICommandSender sender, String[] aString) {
		String msg = null;
		if (aString.length >= 2) { 
			try {
				String newValue = ((aString.length > 2) ? aString[2] : "");
				if (!config.setCfgField(aString[1], newValue)) {
					// good field but bad value
					throw new WrongUsageException (Lang.getCommandUsage(modPrefix, "main", "set.fieldvalue"), 
							new Object[] {commandName, newValue, aString[1], config.getCfgFieldType(aString[1])});			
				} else {
					String value = config.getCfgField(aString[1]);
					msg = Lang.getMsgParams(modPrefix, "main.set", modName, aString[1], value);
				}
			} catch (NoSuchFieldException e) {
				throw new WrongUsageException (Lang.getCommandUsage(modPrefix, "main", "set.nofield"), 
						new Object[] {commandName, aString[1]});

			}
		}
		return msg;
	}
}
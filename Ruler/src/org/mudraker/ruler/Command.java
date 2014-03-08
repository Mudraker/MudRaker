package org.mudraker.ruler;

import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraftforge.client.ClientCommandHandler;

import org.mudraker.Lang;
import org.mudraker.Log;
import org.mudraker.Util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * BlockPlacer Command Handler
 * <p>Provides /mrrl command for debugging purposes></p>
 * 
 * <p>Pattern: Singleton Registered Handler</p>
 * 
 * @author MudRaker
 * @version %I%, %G%
 */
@SideOnly(Side.CLIENT)
public class Command implements ICommand {
	// Singleton instance enforcement
	private final static Command instance = new Command(); // Singleton instance
	public static Command getInstance() { return instance; } // Return singleton for access
	
	/**
	 * Constructor: Register this class as a command handler when instantiated
	 * <p>Singleton: Prevent additional instantiation
	 */
	@SideOnly(Side.CLIENT)
	private Command() {
		ClientCommandHandler.instance.registerCommand(this);
	}; 
	
	/** Array of log level names for tab completion. */
	static String[] logLevels = {"off", "severe", "warning", "info", "config", "fine", "finer", "finest", "all"};
	
	@Override
	@SideOnly(Side.CLIENT)
    public int compareTo(Object par1Obj)
    {
		// compare based on name only
        return this.getCommandName().compareTo(((ICommand)par1Obj).getCommandName());
    }
	
	/** Defines the command handled by this class as /mrrl */
	@Override
	@SideOnly(Side.CLIENT)
	public String getCommandName() {
		return "mrrl";
	}
	
	/** 
	 * Returns the command usage internationalisation key 
	 * (used for /help on server only, and WrongUsage exceptions).
	 * @param  icommandsender is the command sender to return the usage for.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public String getCommandUsage(ICommandSender icommandsender) {
		// Note: must return an internationalisation key
		return ModInfo.ID.toLowerCase() + ".command."+this.getCommandName()+".usage";
	}
	
	/** Return aliases for this command - but there are none. */
	@Override
	@SideOnly(Side.CLIENT)
	public List getCommandAliases() {
		return null;
	}
	
	/**
	 * Processes the MRRL command options:
	 * <p>RELOAD - reloads configuration from disk</p>
	 * <p>CONSOLE [on | off] - forces logging to console</p>
	 * <p>LOG <log level> - sets the logging level for the session
	 * <p>? or GET <field> - prints a configuration field value</p>
	 * <p>SET <field> <value> - sets a configuration field for the session</p>
	 * @param sender is the command sender
	 * @param aString is the string array of the parameters
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void processCommand(ICommandSender sender, String[] aString) {
		Config config = Config.getInstance();
		EntityPlayer entityPlayer = getSenderAsPlayer(sender);
		String msg = "";
		
		// Log the command
		Log.fine("Process command "+getCommandName());
		for (int i = 0; i < aString.length; i++) {
			Log.fine("Command Args["+i+"]=<"+aString[i]+">");
		}
		
		// Process the command - RELOAD
		if (aString.length > 0 && aString[0].equalsIgnoreCase("reload")) {
			config.reloadConfig();
			config.dumpConfig();
			// KeyBind.reInit(config.placeControlMode);	//TODO
			msg = Lang.getMsg(ModInfo.ID, "reload");
			
		// CONSOLE
		} else if (aString.length > 0 && aString[0].equalsIgnoreCase("console")) {
			Boolean state = !(aString.length > 1 && aString[1].equalsIgnoreCase("off"));
			Log.setMinInfo(state);
			msg = Lang.getMsgParams(ModInfo.ID, "console", (state?"on":"off"));
			
		// LOG
		} else if (aString.length > 0 && aString[0].equalsIgnoreCase("log")) {
			if (aString.length > 1 && Log.setLevel(aString[1])) {
				msg = Lang.getMsgParams(ModInfo.ID, "log", aString[1]);				
			} else {
				throw new WrongUsageException (Lang.getCommandUsage(ModInfo.ID, "log"), new Object[0]);
			}
			
		// SET
		} else if (aString.length > 0 && aString[0].equalsIgnoreCase("set")) {
			if (aString.length < 2) {
				throw new WrongUsageException (Lang.getCommandUsage(ModInfo.ID, "set"), new Object[0]);
			}
			msg = doSetCfgField (sender, aString);
			
		// UNKNOWN
		} else {
			Log.info("Unknown command: "+this.getCommandName()+" "+Util.flattenArray(aString));
			throw new WrongUsageException (Lang.getString(getCommandUsage(sender)), new Object[0]);
		}
		
		// Give message to user if one is needed
		if (msg != null && !msg.isEmpty()) {
			entityPlayer.addChatMessage(msg);
			Log.info(msg);
		}
	}
	
	/**
	 * Only players can use the MRRL command
	 * @param sender is the command sender (player, command block or other entity)
	 * @return true if sender can use command
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
        if (sender instanceof EntityPlayer) {
        	return true;
        } else if (sender instanceof TileEntityCommandBlock) { 
        	return false;
        } else {
        	return false;
        }
	}
	
	/**
	 * @param sender is the command sender
	 * @param aString is the array of command words
	 * @returns a List of all words that are candidate completion options for the last word
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public List addTabCompletionOptions(ICommandSender sender, String[] aString) {
		Config config = Config.getInstance();

		if (aString.length == 1) 
        	return Util.getMatchesOnLastWord(aString, "console", "log", "reload", "set");
        else if (aString.length == 2 && aString[0].equalsIgnoreCase("console")) 
        	return Util.getMatchesOnLastWord(aString, "on", "off");
        else if (aString.length == 2 && aString[0].equalsIgnoreCase("log")) 
        	return Util.getMatchesOnLastWord(aString, logLevels);
        else if (aString.length >= 2 && aString[0].equalsIgnoreCase("set")) {
        	if (aString.length == 2)
        		return Util.getMatchesOnLastWord(aString, config.getCfgFields());
        	else if (aString.length == 3 && aString[1].equalsIgnoreCase("logleveloverride")) 
        		return Util.getMatchesOnLastWord(aString, logLevels);
        	else if (aString.length == 3) 
        		return Util.getMatchesOnLastWord(aString, config.getCfgFieldValues(aString[1]));
        }
    	return null;
	}
	
	/**
	 * @param aString is the array of command words
	 * @param i is the entry being checked to see if it is a user
	 * @return true if that word of the command represents a user name.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isUsernameIndex(String[] aString, int i) {
		return false;
	}
	
    /**
     * Returns the given ICommandSender as a EntityPlayer or throw an exception.
     * @param sender is the command sender
     * @return sender as an entity player if possible
     * @throws PlayerNotFoundException if not a player
     */
	@SideOnly(Side.CLIENT)
    protected EntityPlayer getSenderAsPlayer(ICommandSender sender)
    {
        if (sender instanceof EntityPlayer) {
            return (EntityPlayer)sender;
        } else {
            throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.", new Object[0]);
        }
    }
	
	/**
	 * Process a configuration field set command.
	 * @param sender is the command sender
	 * @param aString is the command word array (including the set keyword)
	 * @return a message to send to the user or null
	 * @throws usage exceptions where appropriate
	 */
	@SideOnly(Side.CLIENT)
	protected String doSetCfgField(ICommandSender sender, String[] aString) {
		Config config = Config.getInstance();
		String msg = null;
		
		if (aString.length > 2) {
			try {
				if (!config.setCfgField(aString[1], aString[2])) {
					// good field but bad value
					throw new WrongUsageException (Lang.getCommandUsage(ModInfo.ID, "set.value"), 
							new Object[] {aString[2], aString[1], config.getCfgFieldType(aString[1])});			
				} else {
					msg = Lang.getMsgParams (ModInfo.ID, "set", aString[1], aString[2]);
				}
			} catch (NoSuchFieldException e) {
				throw new WrongUsageException (Lang.getCommandUsage(ModInfo.ID, "set.nofield"), 
						new Object[] {aString[1]});
			}
		} else {
			throw new WrongUsageException (Lang.getCommandUsage(ModInfo.ID, "set.novalue"), 
					new Object[] {aString[1], config.getCfgFieldType(aString[1])});
		}
		return msg;
	}
}
/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker;

import static net.minecraft.util.EnumChatFormatting.RED;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.client.ClientCommandHandler;

/**
 * MudRaker BASE Command Handler
 * <p>Provides base command handling</p>
 * <p>Requires language strings:</p>
 * &ltmodPrefix&gt.cmd.&ltcommandName&gt.usage - top level usage for the command
 * &ltmodPrefix&gt.msg.main.player - exception if sender is not a player
 * <p>Pattern: Abstract Command Handler</p>
 * 
 * @author MudRaker
 */
public abstract class ClientCommandBase implements ICommand {
	
	// ****************************************************************
	// Constructors 
	// ****************************************************************
	/**
	 * Protected Constructor to set MOD and command info
	 * @param modPrefix is the module prefix for language strings
	 * @param modName is the module name shown to the user
	 * @param commandName is the command name to register with minecraft
	 */
	protected ClientCommandBase (String modPrefix, String modName, String commandName) {
		this.modPrefix = modPrefix.toLowerCase();
		this.modName = modName;
		this.commandName = commandName;
		ClientCommandHandler.instance.registerCommand(this);
	} 

	// ****************************************************************
	// Protected Data 
	// ****************************************************************
	/** Mod specific prefix for language strings */
	protected String modPrefix;

	/** Mod name shown to the user */
	protected String modName;

	/** Command name to use */
	protected String commandName;
	
	// ****************************************************************
	// Public Interface 
	// ****************************************************************
	/** 
	 * Compare commands based on command name only
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
    public int compareTo(Object par1Obj) {
        return this.getCommandName().compareTo(((ICommand)par1Obj).getCommandName());
    }
	
	/** Defines the command handled by this class */
	@Override
	public String getCommandName() {
		return commandName;
	}
	
	/** 
	 * Returns the standard command usage internationalisation key 
	 * (used for /help on server only, and WrongUsage exceptions).
	 * @param  sender is the command sender to return the usage for.
	 */
	@Override
	public String getCommandUsage(ICommandSender sender) {
		// Note: must return an internationalisation key
		return modPrefix + ".cmd." + commandName + ".usage";
	}
	
	/** Return aliases for this command - default none. */
	@Override
	public List getCommandAliases() {
		return null;
	}
	
	/**
	 * Handle unknown commands with a usage exception
	 * <p>Subclasses are not permitted to override this version - use processClientCommand</p>
	 * @param sender is the command sender
	 * @param aString is the string array of the parameters
	 */
	@Override
	public final void processCommand(ICommandSender sender, String[] aString) {
		/* 
		 * Forge ClientCommandHandler will pass any command with an exception back to the server
		 * to process which causes an invalid command message. So, if there is an EXPECTED exception
		 * we handle the chat message here so it won't do that.
		 */
		try {
			this.processClientCommand (sender, aString);
		} catch (WrongUsageException wue) {
            sender.sendChatToPlayer(format("commands.generic.usage", format(wue.getMessage(), wue.getErrorOjbects())).setColor(RED));
        } catch (CommandException ce) {
            sender.sendChatToPlayer(format(ce.getMessage(), ce.getErrorOjbects()).setColor(RED));
        } 
	}
	
	/**
	 * Handle unknown commands with a usage exception
	 * <p>Subclasses should override and pass unknown commands to super</p>
	 * @param sender is the command sender
	 * @param aString is the string array of the parameters
	 */
	public void processClientCommand(ICommandSender sender, String[] aString) {
		Log.info("Unknown command: " + commandName + " " + Util.flattenArray(aString));
		throw new WrongUsageException (Lang.getString(getCommandUsage(sender)), new Object[0]);
	}
	
	/**
	 * Can sender use the command?
	 * <p>By default only players can use the command.</p> 
	 * @param sender is the command sender (player, command block or other entity)
	 * @return true if sender can use command
	 */
	@Override
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
	 * Returns a list of valid tab completion options
	 * <p>Subclasses should override and merge the super list the subclass list</p>
	 * @param sender is the command sender
	 * @param aString is the array of command words
	 * @returns a List of all words that are candidate completion options for the last word
	 */
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] aString) {
    	return null;
	}
	
	/**
	 * Is a command word a user name?
	 * <p>Subclasses should override and pass any unknown commands to super</p>
	 * @param aString is the array of command words
	 * @param i is the entry being checked to see if it is a user
	 * @return true if that word of the command represents a user name.
	 */
	@Override
	public boolean isUsernameIndex(String[] aString, int i) {
		return false;
	}

	// ****************************************************************
	// PROTECTED Helper Functions 
	// ****************************************************************
    /**
     * Returns the given ICommandSender as a EntityPlayer or throw an exception.
     * @param sender is the command sender
     * @return sender as an entity player if possible
     * @throws PlayerNotFoundException if not a player
     */
    protected EntityPlayer getSenderAsPlayer(ICommandSender sender)
    {
        if (sender instanceof EntityPlayer) {
            return (EntityPlayer)sender;
        } else {
            throw new PlayerNotFoundException(Lang.getCommandUsage (modPrefix, commandName, "main.player"), new Object[0]);
        }
    }
	
	/**
	 * Retrieve the command usage from the language file.
	 * @param subCommand are the optional sub command name(s)
	 * @return is the translated command usage.
	 */
	protected String getCommandUsage (String subCommand) {
		return Lang.getCommandUsage (modPrefix, commandName, subCommand);
	}
	
	/**
	 * Retrieves a command related message text with parameters from the language file.
	 * @param sender is the command sender
	 * @param msg is the message id
	 * @param objects are the parameter objects
	 * @return is the translated message text with parameters inserted
	 */
	protected String getMsgParams (String msg, Object ... objects) {
		return Lang.getMsgParams (modPrefix, ".cmd."+commandName+"."+msg, objects);
	}	
	
	/**
	 * Retrieves and outputs a command related message text from the language file.
	 * @param sender is the command sender
	 * @param msg is the message id
	 */
	protected void giveMsg (ICommandSender sender, String msg) {
		outputMsg (sender, Lang.getMsg (modPrefix, commandName+"."+msg));
	}
	
	/**
	 * Retrieves and outputs a command related message text with parameters from the language file.
	 * @param sender is the command sender
	 * @param msg is the message id
	 * @param objects are the parameter objects
	 */
	protected void giveMsgParams (ICommandSender sender, String msg, Object ... objects) {
		outputMsg (sender, Lang.getMsgParams (modPrefix, ".cmd."+commandName+"."+msg, objects));
	}
	
	/**
	 * Outputs an already localised message to the command sender.
	 * <p>Messages are logged as information messages, and issued as a chat message if sender is a player.</p>
	 * @param sender is the command sender
	 * @param msg is the formatted message to issue
	 */
	protected void outputMsg (ICommandSender sender, String msg) { 
		if (msg != null && !msg.isEmpty()) {
			if (sender instanceof EntityPlayer) {
				((EntityPlayer) sender).addChatMessage(msg);
			}
			Log.info(msg);
		}
	}
	
    // Couple of helpers because the mcp names are stupid and long... 
    private ChatMessageComponent format(String str, Object... args)
    {
        return ChatMessageComponent.createFromTranslationWithSubstitutions(str, args);
    }
}
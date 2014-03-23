package org.mudraker.ruler;

import java.util.List;

import net.minecraft.command.ICommandSender;

import org.mudraker.ClientCommandMain;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Ruler Command Handler
 * <p>Provides /mrrl command for debugging purposes></p>
 * <p>Pattern: Singleton Registered Handler</p>
 * 
 * @author MudRaker
 */
@SideOnly(Side.CLIENT)
public class Command extends ClientCommandMain {
	
	// ****************************************************************
	// Singleton Constructors 
	// ****************************************************************
	private final static Command instance = new Command(); // Singleton instance
	public static Command getInstance() { return instance; } // Return singleton for access
	
	/** Singleton constructor */ 
	@SideOnly(Side.CLIENT)
	private Command() {
		super (ModInfo.ID, ModInfo.SHORT_NAME, "mrrl", Config.getInstance());
	}
	
	// ****************************************************************
	// Interface Overrides
	// Only required if want to extend the commands involved
	// ****************************************************************
	/**
	 * Processes the MRBP command options:
	 * Standard logging and config commands from superclass
	 * @param sender is the command sender
	 * @param aString is the string array of the parameters
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void processClientCommand(ICommandSender sender, String[] aString) {
		super.processCommand (sender, aString);
	}
	
	/**
	 * @param sender is the command sender
	 * @param aString is the array of command words
	 * @returns a List of all words that are candidate completion options for the last word
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public List addTabCompletionOptions(ICommandSender sender, String[] aString) {
		return super.addTabCompletionOptions(sender, aString);
	}	
}
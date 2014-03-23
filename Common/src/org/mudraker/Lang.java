/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker;

import net.minecraft.client.resources.I18n;

/** 
 * MudRaker Language file functions.
 * <p>Contains various helper functions for Language file retrieval.</p>
 * 
 * <p>Pattern: Enforced Static Immutable</p>
 * 
 * <p>1.7.2 Added compatibility methods. New key category translation</p> 
 *  
 * @author MudRaker
 */
public class Lang extends I18n {
	private Lang() {} // Static: Prevent instantiation
	
	/**
	 * Standardise a string into a language file key
	 * <p>Converts spaces to underscores and converts to lower case.
	 * @param key is the input key value
	 * @return is the standardised key value.
	 */
	public static String getStandardKey (String key) {
		return key.replace(' ', '_').toLowerCase();
	}
	
	/**
	 * Retrieve a phrase from the language file.
	 * @param mod is the mod prefix
	 * @param phrase is the phrase name
	 * @return is the translated phrase text.
	 */
	public static String getPhrase (String mod, String phraseName) {
		return getString(mod.toLowerCase()+".phrase."+phraseName);
	}
	
	/**
	 * Retrieve a key name from the language file.
	 * @param mod is the mod prefix
	 * @param keyName is the key short name
	 * @return is the translated key description for the binding.
	 */
	public static String getKeyName (String mod, String keyName) {
		return getString(mod.toLowerCase()+".key."+keyName+".name");
	}
	
	/**
	 * Retrieve a key category name suitable for later language lookup.
	 * @param mod is the mod prefix
	 * @param categoryName is the key category name
	 * @return is the key category description without tranlsation.
	 */
	public static String getKeyCategory (String mod, String categoryName) {
		return mod.toLowerCase()+".key.category."+categoryName;
	}
	
	/**
	 * Retrieve a configuration category comment from the language file.
	 * @param mod is the mod prefix
	 * @param category is the category name
	 * @return is the translated category comment.
	 */
	public static String getCategoryComment (String mod, String Category) {
		return Util.newLines(getString(mod.toLowerCase()+".category."+Category+".comment"));
	}
	
	/**
	 * Retrieve a configuration field comment from the language file.
	 * @param mod is the mod prefix
	 * @param fieldName is the field name
	 * @return is the translated field name.
	 */
	public static String getFieldName (String mod, String fieldName) {
		return getString(mod.toLowerCase()+".field."+fieldName+".name");
	}
	
	/**
	 * Retrieve a configuration field comment from the language file.
	 * @param mod is the mod prefix
	 * @param fieldName is the field name
	 * @return is the translated field comment.
	 */
	public static String getFieldComment (String mod, String fieldName) {
		return Util.newLines(getString(mod.toLowerCase()+".field."+fieldName+".comment"));
	}
	
	/**
	 * Retrieve a command usage from the language file.
	 * @param mod is the mod prefix
	 * @param command is the command name
	 * @param subCommand are the optional sub command name(s)
	 * @return is the translated command usage.
	 */
	public static String getCommandUsage (String mod, String command, String subCommand) {
		return getString(mod.toLowerCase()+".cmd."+command
				+((subCommand != null && !subCommand.isEmpty())?"."+subCommand:"")
				+".usage");
	}
	
	/**
	 * Retrieve message text from the language file.
	 * @param mod is the mod prefix
	 * @param msg is the message id
	 * @return is the translated message text
	 */
	public static String getMsg (String mod, String msg) {
		return getString(mod.toLowerCase()+".msg."+msg);
	}
	
	/**
	 * Retrieve message text with parameters from the language file.
	 * @param mod is the mod prefix
	 * @param msg is the message id
	 * @param objects are the parameter objects
	 * @return is the translated message text with parameters inserted
	 */
	public static String getMsgParams (String mod, String msg, Object ... objects) {
		return getStringParams(mod.toLowerCase()+".msg."+msg, objects);
	}
	
	/**
	 * 1.7.2 compatibility function
	 * @param s is the language key to retrieve the string for 
	 * @return the translated language string
	 */
	public static String getString(String s) {
		return format(s);
	}
	
	/**
	 * 1.7.2 compatibility function
	 * @param s is the language key to retrieve the string for 
	 * @param objects are the parameter objects for the string formatting
	 * @return the translated and formatted language string
	 */
	public static String getStringParams(String s, Object ... objects) {
		return format(s, objects);
	}
}

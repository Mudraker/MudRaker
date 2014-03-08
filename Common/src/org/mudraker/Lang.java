package org.mudraker;

import net.minecraft.client.resources.I18n;

/** 
 * MudRaker Language file functions.
 * <p>Contains various helper functions for Language file retrieval.</p>
 * 
 * <p>Pattern: Enforced Static Immutable</p>
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
		return I18n.getString(mod.toLowerCase()+".phrase."+phraseName);
	}
	
	/**
	 * Retrieve a key name from the language file.
	 * @param mod is the mod prefix
	 * @param keyName is the key short name
	 * @return is the translated key description for the binding.
	 */
	public static String getKeyName (String mod, String keyName) {
		return I18n.getString(mod.toLowerCase()+".key."+keyName+".name");
	}
	
	/**
	 * Retrieve a configuration category comment from the language file.
	 * @param mod is the mod prefix
	 * @param category is the category name
	 * @return is the translated category comment.
	 */
	public static String getCategoryComment (String mod, String Category) {
		return Util.newLines(I18n.getString(mod.toLowerCase()+".category."+Category+".comment"));
	}
	
	/**
	 * Retrieve a configuration field comment from the language file.
	 * @param mod is the mod prefix
	 * @param fieldName is the field name
	 * @return is the translated field name.
	 */
	public static String getFieldName (String mod, String fieldName) {
		return I18n.getString(mod.toLowerCase()+".field."+fieldName+".name");
	}
	
	/**
	 * Retrieve a configuration field comment from the language file.
	 * @param mod is the mod prefix
	 * @param fieldName is the field name
	 * @return is the translated field comment.
	 */
	public static String getFieldComment (String mod, String fieldName) {
		return Util.newLines(I18n.getString(mod.toLowerCase()+".field."+fieldName+".comment"));
	}
	
	/**
	 * Retrieve a command usage from the language file.
	 * @param mod is the mod prefix
	 * @param command is the command name
	 * @param subCommand are the optional sub command name(s)
	 * @return is the translated command usage.
	 */
	public static String getCommandUsage (String mod, String command, String subCommand) {
		return I18n.getString(mod.toLowerCase()+".cmd."+command
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
		return I18n.getString(mod.toLowerCase()+".msg."+msg);
	}
	
	/**
	 * Retrieve message text with parameters from the language file.
	 * @param mod is the mod prefix
	 * @param msg is the message id
	 * @param objects are the parameter objects
	 * @return is the translated message text with parameters inserted
	 */
	public static String getMsgParams (String mod, String msg, Object ... objects) {
		return I18n.getStringParams(mod.toLowerCase()+".msg."+msg, objects);
	}
}
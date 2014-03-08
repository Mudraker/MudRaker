package org.mudraker;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

/** 
 * MudRaker Mod Configuration manager
 * <p>Provides an annotation and reflection based configuration file loader.</p>
 * 
 * <p>Pattern: Abstract Singleton Mutable</p>
 * 
 * Config classes must extend this class and add fields to hold the configuration.
 * The {@link Cfg} annotation indicates the fields to be loaded from the configuration.
 * 
 * <p>The annotation indicates the field type and (optionally) the configuration category,
 * minimum and maximum values (numeric only) and whether a "check" method is invoked to
 * validate the data being loaded.</p>
 * 
 * <p>Note that the configuration is automatically saved if changed during any load or reload.</p>
 * 
 * @author MudRaker
 */
public abstract class ConfigBase {
	
	// ****************************************************************
	// Constructors 
	// ****************************************************************
	/**
	 * Protected Constructor to set modPrefix and fieldPrefix info
	 * @param modPrefix is the module prefix for language strings
	 * @param fieldPrefix is an optional prefix to remove from configuration fields when used manually
	 * @param stripPrefix indicates if the optional prefix be stripped when returning valid field names
	 */
	protected ConfigBase(String modPrefix, String fieldPrefix, boolean stripPrefix) {
		this.modPrefix = modPrefix;
		this.fieldPrefix = fieldPrefix;
		this.stripPrefix = stripPrefix;
	} 

	// ****************************************************************
	// Protected Data 
	// ****************************************************************
	/** Configuration file details */
	protected File file;
	
	/** Mod specific prefix for language strings */
	protected String modPrefix;
	
	/** Optional prefix to remove from configuration fields when used manually */
	protected String fieldPrefix;
	
	/** Should the optional prefix be stripped when returning valid field names */
	protected boolean stripPrefix;
	
	// ****************************************************************
	// ANNOTATIONS
	// ****************************************************************
	
	/**
	 * Configuration annotation Used to describe how configuration fields are to
	 * be loaded from the master config file and processed.
	 * <p>Supports boolean, string, int, float, double and Enum fields.</p>
	 * 
	 * @param class is the field data class
	 * @param Category is the config file category (default: general)
	 * @param min is the minimum allowed value
	 * @param max is the maximum allowed value
	 * @param check indicates if a "check" method is to be called to validate
	 * 				the data being loaded (see below).
	 * 
	 * <p>See {@link ConfigBase}</p>
	 * 
	 * A check method must be named "check_"+fieldName, with a signature:
	 * 
	 * <p>&nbsp&nbsp&nbsp boolean check_fieldName (Field field, Property property)</p>
	 * 
	 * but may have any visibility including private.
	 * <p>The field parameter is the reflected field details and the Forge property
	 * parameter is the configuration property details. The method may update
	 * the field directly and modify the property as appropriate.</p>
	 * If the method returns true, the data in property will be saved to the field
	 * otherwise it will not.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Cfg {
		Class value (); 
		String cat () default Configuration.CATEGORY_GENERAL;
		double min () default 0;
		double max () default -1;
		boolean loadOnly () default false;
		boolean check () default false;
	}
	
	// ****************************************************************
	// PUBLIC INTERFACE
	// ****************************************************************

	/**
	 * Load the Forge-style configuration file for this mod and registers the
	 * provided categories with comments from the language files.
	 * <p>Provides default values for any missing configuration items and will rewrite the
	 * configuration file to reflect these defaults if necessary.</p>
	 * @param fileName for the configuration file
	 * @param Categories is array of categories to register comments for 
	 */
	protected void loadConfig(File fileName, String[] Categories) {
		file = fileName;
		Configuration cfg = new Configuration(fileName);
		if (Categories != null) {
			for (String s : Categories) {
				registerCategory(cfg, s);
			}
		}
		Log.info("Configuration loading...");
		doLoad(cfg, true);
	}
	
	/**
	 * Load the Forge-style configuration file for this mod.
	 * <p>Provides default values for any missing configuration items and will rewrite the
	 * configuration file to reflect these defaults if necessary.</p>
	 * @param fileName for the configuration file
	 */
	public void loadConfig(File fileName) {
		loadConfig(fileName, null);
	}
	
	/**
	 * Reload the current Forge-style configuration file
	 * Requires that the configuration has previously been loaded.
	 * @param fileName for the configuration file
	 * @throws IllegalStateException if configuration has not been previously loaded
	 */
	public void reloadConfig() {
		if (file == null) {
			throw new IllegalStateException ("Can only reload after initial load");
		}
		Configuration cfg = new Configuration(file);
		Log.info("Configuration RELOADING...");
		doLoad(cfg, false);
	}
	
	/** 
	 * Dump details of the loaded configuration as information messages to the log.
	 */
	public void dumpConfig() {
		Log.cfg("--- "+modPrefix+" Configuration dump ---");
		doDump();
		Log.cfg("--- End Configuration ---");		
	}
	
	/**
	 * Returns a list of valid configuration fields that can be set.
	 * Allow for an optional prefix at the start of the actual field name, and
	 * can either strip the prefix or return both names.
	 * @return a string array of the valid field names.
	 */
	public String[] getCfgFields() {
		Cfg annotation;
		ArrayList<String> result = new ArrayList<String>();
		String name;

		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			if ((annotation = field.getAnnotation(Cfg.class)) != null && !annotation.loadOnly()) {
				name = field.getName();
				if (Util.stringHasPrefix(name, fieldPrefix)) {
					result.add(name.substring(fieldPrefix.length()));
					if (!stripPrefix)
						result.add (name);
				} else {
					result.add (name);
				}
			}
		}
		return result.toArray(new String[result.size()]); 
	}

	/**
	 * Retrieve a description of the data type of a configuration field.
	 * Allow for an optional prefix at the start of the actual field name.
	 * May be Boolean, String, Int, Float, Double or Enum.
	 * @param searchName is the configuration field to retrieve the type of
	 * @return a string representing the class recorded by the {@link Cfg} annotation
	 * or null if the configuration field is not found.
	 */
	public String getCfgFieldType(String searchName) {
		Field field;
		if ((field = findField (searchName, fieldPrefix)) != null) {
			String typeName="";
			Cfg annotation = field.getAnnotation(Cfg.class);
			String checked = (annotation.check() ? " (checked)" : ""); 
			if (annotation.loadOnly())						return null;
			else if (annotation.value() == boolean.class)	typeName = "boolean";
			else if (annotation.value() == String.class) 	typeName = "string";
			else if (annotation.value() == int.class)		typeName = "int";
			else if (annotation.value() == float.class)		typeName = "float";
			else if (annotation.value() == double.class)	typeName = "double";
			else if (annotation.value().isEnum())			typeName = "enum";
			return typeName + checked;
		}
		return null;
	}

	/**
	 * Retrieve valid values for a configuration field.
	 * Allow for an optional prefix at the start of the actual field name.
	 * @param searchName is the configuration field to retrieve values for
	 * @return an array of valid values if known. For numeric fields with a min/max
	 * range, a single value is returned identifying the min ... max values.
	 */
	public String[] getCfgFieldValues(String searchName) {
		Field field;
		if ((field = findField (searchName, fieldPrefix)) != null) {
			Cfg annotation = field.getAnnotation(Cfg.class);
			if (annotation.loadOnly())
				return null;
			else if (annotation.value() == boolean.class)
				return new String[] {"true", "false"};
			else if (annotation.value() == String.class)
				return null;
			else if (annotation.value() == int.class) {
				if (annotation.min() <= annotation.max())
					return new String [] {"" + (int) annotation.min() + "..." + (int) annotation.max()};
			} else if (annotation.value() == float.class) {
				if (annotation.min() <= annotation.max())
					return new String [] {"" + (float) annotation.min() + "..." + (float) annotation.max()};
			} else if (annotation.value() == double.class) {
				if (annotation.min() <= annotation.max())
					return new String [] {"" + (double) annotation.min() + "..." + (double) annotation.max()};
			} else if (annotation.value().isEnum())
				return Util.enumValues(annotation.value());
		}
		return null;
	}
	
	/**
	 * Get a configuration field by name and return the value as a string.
	 * Allow for an optional prefix at the start of the actual field name.
	 * @param searchName is the configuration field to set
	 * @return the value of the field as a string
	 * @throws NoSuchFieldException if the field cannot be found (with or without prefix)
	 */
	public String getCfgField(String searchName) throws NoSuchFieldException {
		Cfg annotation;

		// locate the field, with or without prefix
		Field field = findField (searchName, fieldPrefix);
		
		// locate field & check for annotation & process if valid
		if (field != null && (annotation = field.getAnnotation(Cfg.class)) != null) {
			String value = null;
			field.setAccessible(true);
			try {
				if (annotation.value() == boolean.class)		value = ""+field.getBoolean(this);
				else if (annotation.value() == String.class) 	value = (String) field.get(this);
				else if (annotation.value() == int.class) 		value = ""+field.getInt(this);
				else if (annotation.value() == float.class) 	value = ""+field.getFloat(this);
				else if (annotation.value() == double.class) 	value = ""+field.getDouble(this);
				else if (annotation.value().isEnum()) {
					value = (field.get(this) != null) ? field.get(this).toString() : "";
				}
			} catch (Exception e) {
				// HUSH
			}
			return value;
		}
		throw new NoSuchFieldException ();
	}

	/**
	 * Set a configuration field by name using the provided string value.
	 * Allow for an optional prefix at the start of the actual field name.
	 * @param searchName is the configuration field to set
	 * @param value is the string version of the value to set
	 * @return true if the field was set to the value or false if the value is not valid
	 * @throws NoSuchFieldException if the field cannot be found (with or without prefix)
	 */
	public boolean setCfgField(String searchName, String value) throws NoSuchFieldException {
		Field field = null;
		Cfg annotation;
		String name;
		boolean ok = false;

		// locate the field, with or without prefix
		field = findField (searchName, fieldPrefix);
		
		// locate field & check for annotation & process if valid
		if (field != null && (annotation = field.getAnnotation(Cfg.class)) != null && !annotation.loadOnly()) {
			field.setAccessible(true);
			name = field.getName();
			Property p = new Property (name, value, Property.Type.STRING);
			if (annotation.value() == boolean.class) {
				try {
					boolean b = parseBoolean(value); // be generous with boolean parsing
					p.set(Boolean.toString(b));
					ok = setBooleanFromProperty(name, annotation, field, p);
				} catch (IllegalArgumentException e) {
					ok = false; // not a boolean, even with generous parsing
				}
			} else if (annotation.value() == String.class) {
				ok = setStringFromProperty(name, annotation, field, p);
			} else if (annotation.value() == int.class) {
				ok = setIntFromProperty(name, annotation, field, p);
			} else if (annotation.value() == float.class) {
				ok = setFloatFromProperty(name, annotation, field, p);
			} else if (annotation.value() == double.class) {
				ok = setDoubleFromProperty(name, annotation, field, p);
			} else if (annotation.value().isEnum()) {
				ok = setEnumFromProperty(name, annotation, field, p);
			} else {
				throw new NoSuchFieldException ();
			}
			return ok;
		}
		throw new NoSuchFieldException ();
	}

	// ****************************************************************
	// INTERAL WORKERS
	// ****************************************************************
	
	/**
	 * Register comments for this category
	 * @param cfg is the Forge configuration
	 * @param category is the category name
	 */
	private void registerCategory(Configuration cfg, String category) {
		cfg.addCustomCategoryComment(category, Lang.getCategoryComment(modPrefix, Lang.getStandardKey(category)));
	}
	
	/**
	 * Worker to load and process the current Forge-style configuration file
	 * Provides default values for any missing configuration items and will rewrite the
	 * configuration file to reflect these defaults if necessary.
	 * @param cfg is the Forge configuration to be processed. 
	 * @param firstTime indicates if this is the first time config has been loaded. 
	 */
	private void doLoad(Configuration cfg, boolean firstTime) {
		try {
			// Load any stored configuration from its file
			cfg.load();
			processCfg(cfg, firstTime);
		} catch (Exception e) {
			Log.warn("Configuration load failed");
			e.printStackTrace();
		} finally {
			// Write back any changes due to default values
			if (cfg.hasChanged()) {
				Log.info("Configuration updates saved");
				cfg.save();
			}
		}
	}
	
	/**
	 * Process configuration against the declared fields in the config class
	 * Annotations ({@link Cfg}) are required on the fields to be processed.
	 * @param cfg is the Forge configuration information to process.
	 * @param firstTime indicates if this is the first time config has been loaded. 
	 */
	private void processCfg(Configuration cfg, boolean firstTime) {
		Cfg annotation;
		Property p;
		String name, bounds;

		try {
			Field[] fields = this.getClass().getDeclaredFields();
			for (Field field : fields) {
				if ((annotation = field.getAnnotation(Cfg.class)) != null) {
					name = field.getName();
					field.setAccessible(true);
					// FIRST TIME ONLY
					if (annotation.loadOnly() && !firstTime) {
						Log.fine("Config: Skip load only field " + name);
					// ITEM
					} else if (annotation.value() == Item.class) {
						p = cfg.getItem (name, field.getInt(this), Lang.getFieldComment(modPrefix, name));
						if (!setIntFromProperty(name, annotation, field, p)) p.set(field.getInt(this));
					// BLOCK
					} else if (annotation.value() == Block.class) {
						p = cfg.getBlock (name, field.getInt(this), Lang.getFieldComment(modPrefix, name));
						if (!setIntFromProperty(name, annotation, field, p)) p.set(field.getInt(this));
					// BOOLEAN
					} else if (annotation.value() == boolean.class) {
						p = cfg.get(annotation.cat(), name, field.getBoolean(this), Lang.getFieldComment(modPrefix, name));
						if (!setBooleanFromProperty(name, annotation, field, p)) p.set(field.getBoolean(this));
					// STRING
					} else if (annotation.value() == String.class) {
						p = cfg.get(annotation.cat(), name, (String) field.get(this), Lang.getFieldComment(modPrefix, name));
						if (!setStringFromProperty(name, annotation, field, p)) p.set((String) field.get(this));
					// INT
					} else if (annotation.value() == int.class) {
						bounds = (annotation.min() > annotation.max()) ? "" : " ("
								+ (int) annotation.min() + "-" + (int) annotation.max() + ")";
						p = cfg.get(annotation.cat(), name, field.getInt(this), Lang.getFieldComment(modPrefix, name) + bounds);
						if (!setIntFromProperty(name, annotation, field, p)) p.set(field.getInt(this));
					// FLOAT
					} else if (annotation.value() == float.class) {
						bounds = (annotation.min() > annotation.max()) ? "" : " ("
								+ (float) annotation.min() + "-" + (float) annotation.max() + ")";
						p = cfg.get(annotation.cat(), name, field.getFloat(this), Lang.getFieldComment(modPrefix, name) + bounds);
						if (!setFloatFromProperty(name, annotation, field, p)) p.set(field.getFloat(this));
					// DOUBLE
					} else if (annotation.value() == double.class) {
						bounds = (annotation.min() > annotation.max()) ? "" : " ("
								+ (double) annotation.min() + "-" + (double) annotation.max() + ")";
						p = cfg.get(annotation.cat(), name, field.getDouble(this), Lang.getFieldComment(modPrefix, name) + bounds);
						if (!setDoubleFromProperty(name, annotation, field, p)) p.set(field.getDouble(this));
					// ENUM
					} else if (annotation.value().isEnum()) {
						String value = (field.get(this) != null) ? field.get(this).toString() : "";
						bounds = Util.enumValueList(field.getType());
						p = cfg.get(annotation.cat(), name, value, Lang.getFieldComment(modPrefix, name)
								+ Configuration.NEW_LINE + Lang.getPhrase(modPrefix, "values") + ": " + bounds);
						if (!setEnumFromProperty(name, annotation, field, p) && p.hasChanged()) {
							value = (field.get(this) != null) ? field.get(this).toString() : "";
				        	Log.warn("Config: Invalid config value "+p.getString()+" for "+name+", rewrite with default "+value);
						}
					// UNKNOWN
					} else {
						Log.warn("Config: Unsupported field type for field " + name + " - skipped!");
					}
				}
			} // for
		} catch (Exception e) {
        	Log.warn("Config: Failed to process configuration due to Exception: "+e);
        	e.printStackTrace();
		}
	}
	
	/**
	 * Execute the check function for this field if possible
	 * @param name is the name of the field to check
	 * @param field is the reflected field details to pass 
	 * @param p is the Forge property value being loaded
	 * @return true if data is valid to save otherwise false
	 */
	private boolean doCheck (String name, Field field, Property p) {
		Boolean valid = true;
		Log.fine("Config: Attempt field check for "+name);
		try {
			Method m = this.getClass().getDeclaredMethod("check_"+name, Field.class, Property.class);
			m.setAccessible(true);
			Object obj = m.invoke(this, field, p);
			valid = !(obj != null && obj.equals(false));
			Log.fine("Config: Field check " + (valid ? "completed successfully" : "failed - reject data"));
		} catch (NoSuchMethodException e) {
			Log.info("Config: No field check method found for "+name);
		} catch (Exception e) {
        	Log.warn("Config: Exception executing doCheck function for "+name);
			e.printStackTrace();
		}
		return valid;
	}
	
	/**
	 * Dump the current configuration values from the declared fields in the config class
	 * Annotations ({@link Cfg}) are required on the fields to be dumped
	 */
	private void doDump() {
		Cfg annotation;
		String name;
		String value="?";

		try {
			Field[] fields = this.getClass().getDeclaredFields();
			for (Field field : fields) {
				if ((annotation = field.getAnnotation(Cfg.class)) != null) {
					name = field.getName();
					field.setAccessible(true);
					if (annotation.value() == Item.class) {
						value = ""+field.getInt(this);
					} else if (annotation.value() == boolean.class) {
						value = ""+field.getBoolean(this);
					} else if (annotation.value() == String.class) {
						value = (String) field.get(this);
					} else if (annotation.value() == int.class) {
						value = ""+field.getInt(this);
					} else if (annotation.value() == float.class) {
						value = ""+ field.getFloat(this);
					} else if (annotation.value() == double.class) {
						value = ""+ field.getDouble(this);
					} else if (annotation.value().isEnum()) {
						value = (field.get(this) != null) ? field.get(this).toString() : "";
					}
					Log.cfg ("Config "+name+"=("+value+")");
				}
			} // for
		} catch (Exception e) {
        	Log.warn("Config: Failed to dump configuration due to Exception: "+e);
        	e.printStackTrace();
		}
	}
	
	/**
	 * Locates a named configuration field, allowing for an optional name prefix
	 * The field must have the {@link Cfg} annotation that is not loadOnly.
	 * @param searchName is the configuration field name to search for
	 * @param optPrefix is the optional prefix that may be on the actual field name
	 * @return the reflected field if found otherwise null
	 */
	private Field findField(String searchName, String optPrefix) {
		Cfg annotation;
		String name;
		String searchWithPrefix = optPrefix+searchName;
		// locate the field, with or without prefix
		try {
			Field[] fields = this.getClass().getDeclaredFields();
			for (Field field : fields) {
				name = field.getName();
				if ((name.equalsIgnoreCase(searchName) 
					|| name.equalsIgnoreCase(searchWithPrefix))
					&& (annotation = field.getAnnotation(Cfg.class)) != null 
					&& !annotation.loadOnly()) {
					return field;
				}
			}
		} catch (Exception  e) {
			// HUSH
		}
		return null;
	}

	/**
	 * Set boolean configuration field from a property
	 * @param name is the configuration field name
	 * @param annotation is the annotation on the field
	 * @param field is the reflected field
	 * @param p is the property containing the new value
	 * @return true if field successfully updated, false otherwise.
	 */
	private boolean setBooleanFromProperty (String name, Cfg annotation, Field field, Property p) {
		if (p.isBooleanValue()) {
			try {
				boolean b = p.getBoolean(field.getBoolean(this));
				if (!annotation.check() || doCheck(name, field, p)) {
					field.setBoolean(this, b);
					Log.fine("Config: Boolean field " + name + " value: "+ field.getBoolean(this));
					return true;
				}				
			} catch (Exception e) {
				// HUSH 
			}
		}
		return false;
	}

	/**
	 * Set string configuration field from a property
	 * @param name is the configuration field name
	 * @param annotation is the annotation on the field
	 * @param field is the reflected field
	 * @param p is the property containing the new value
	 * @return true if field successfully updated, false otherwise.
	 */
	private boolean setStringFromProperty (String name, Cfg annotation, Field field, Property p) {
		try {
			String s = p.getString();
			if (!annotation.check() || doCheck(name, field, p)) {
				field.set(this, s);
				Log.fine("Config: String field " + name + " value: "+ (String) field.get(this));
				return true;			
			}
		} catch (Exception e) {
			// HUSH
		}
		return false;
	}

	/**
	 * Set integer configuration field from a property
	 * @param name is the configuration field name
	 * @param annotation is the annotation on the field
	 * @param field is the reflected field
	 * @param p is the property containing the new value
	 * @return true if field successfully updated, false otherwise.
	 */
	private boolean setIntFromProperty (String name, Cfg annotation, Field field, Property p) {
		if (p.isIntValue()) {
			try {
				int i = p.getInt(field.getInt(this));
				if (annotation.min() <= annotation.max()) {
					int i2 = Util.bound(i, (int) annotation.min(), (int) annotation.max());
					if (i2 != i) p.set(i = i2); 
				}
				if (!annotation.check() || doCheck(name, field, p)) {
					field.setInt(this, i);
					Log.fine("Config: Int field " + name + " value: " + field.getInt(this));
					return true;
				}
			} catch (Exception e) {
				// HUSH
			}
		}
		return false;
	}

	/**
	 * Set float configuration field from a property
	 * @param name is the configuration field name
	 * @param annotation is the annotation on the field
	 * @param field is the reflected field
	 * @param p is the property containing the new value
	 * @return true if field successfully updated, false otherwise.
	 */
	private boolean setFloatFromProperty (String name, Cfg annotation, Field field, Property p) {
		if (p.isDoubleValue()) {
			try {
				// Properties don't support Float, so treated as a double.
				float f = (float) p.getDouble(field.getFloat(this));
				if (annotation.min() <= annotation.max()) {
					float f2 = Util.bound(f, (float) annotation.min(), (float) annotation.max());
					if (f2 != f) p.set(f = f2); 
				}
				if (!annotation.check() || doCheck(name, field, p)) {
					field.setFloat(this, f);
					Log.fine("Config: Float field " + name + " value: " + field.getFloat(this));
					return true;
				}
			} catch (Exception e) {
				// HUSH
			}
		}
		return false;
	}

	/**
	 * Set double configuration field from a property
	 * @param name is the configuration field name
	 * @param annotation is the annotation on the field
	 * @param field is the reflected field
	 * @param p is the property containing the new value
	 * @return true if field successfully updated, false otherwise.
	 */
	private boolean setDoubleFromProperty (String name, Cfg annotation, Field field, Property p) {
		if (p.isDoubleValue()) {
			try {
				double d = p.getDouble(field.getDouble(this));
				if (annotation.min() <= annotation.max()) {
					double d2 = Util.bound(d, (double) annotation.min(), (double) annotation.max());
					if (d2 != d) p.set(d = d2); 
				}
				if (!annotation.check() || doCheck(name, field, p)) {
					field.setDouble(this, d);
					Log.fine("Config: Double field " + name + " value: " + field.getDouble(this));
					return true;
				}
			} catch (Exception e) {
				// HUSH
			}
		}
		return false;
	}

	/**
	 * Set Enum configuration field from a property
	 * @param name is the configuration field name
	 * @param annotation is the annotation on the field
	 * @param field is the reflected field
	 * @param p is the property containing the new value
	 * @return true if field successfully updated, false otherwise.
	 */
	private boolean setEnumFromProperty (String name, Cfg annotation, Field field, Property p) {
		try {
			String value = (field.get(this) != null) ? field.get(this).toString() : "";
			if (!annotation.check() || doCheck(name, field, p)) {
				try {
					Object o = Enum.valueOf((Class)field.getType(), p.getString().trim().toUpperCase());
					field.set(this, o);
			        value = (field.get(this) != null) ? field.get(this).toString() : "";
					Log.fine("Config: Enum field " + name + " value: " + value);
					return true;
				} catch(IllegalArgumentException e) {
		        	p.set(value);
		        	return false;
		        }							
			}
		} catch (Exception e) {
			// HUSH
		}
		return false;
	}

	/**
	 * Generous boolean parser support 0/1, true/false, yes/no allowing any case and substrings of these as well.
	 * @param value is the string to be parsed
	 * @return the boolean value
	 * @throws IllegalArguementException if value does not match criteria.
	 */
	public boolean parseBoolean (String value) {
		if (value.equals("1") || Util.stringHasPrefix("true", value) || Util.stringHasPrefix("yes", value))
			return true;
		else if (value.equals("0") || Util.stringHasPrefix("false", value) || Util.stringHasPrefix("no", value))
			return false;
		else
			throw new IllegalArgumentException ("Invalid boolean value ("+value+")");
	}
}
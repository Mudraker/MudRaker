package org.mudraker;

import java.util.ArrayList;
import java.util.List;

/** 
 * MudRaker Utility functions.
 * <p>Contains various helper functions for strings, enums, math and colours.</p>
 * 
 * <p>Pattern: Enforced Static Immutable</p>
 *  
 * @author MudRaker
 */
public class Util {
	private Util() {} // Static: Prevent instantiation
	
	/** System line separator */
	private static String NEW_LINE = System.getProperty("line.separator");

	/**
	 * Bounds int value to be within range (min, max)
	 * @param value to be bounded
	 * @param min value acceptable
	 * @param max value acceptable
	 * @return bounded value
	 */
	 public static int bound(int value, int min, int max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	/**
	 * Bounds float value to be within range (min, max)
	 * @param value to be bounded
	 * @param min value acceptable
	 * @param max value acceptable
	 * @return bounded value
	 */
	public static float bound(float value, float min, float max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	/**
	 * Bounds double value to be within range (min, max)
	 * @param value to be bounded
	 * @param min value acceptable
	 * @param max value acceptable
	 * @return bounded value
	 */
	public static double bound(double value, double min, double max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}
	
	/**
	 * Converts separate RGBA values into a hexadecimal RGBA colour
	 * @param red component of colour
	 * @param green component of colour
	 * @param blue component of colour
	 * @param alpha component of colour
	 * @return hexadecimal RGBA colour
	 */
	public static int rgbaColour (int red, int green, int blue, int alpha) {
		return ((blue  & 0xFF) << 0)
			 | ((green & 0xFF) << 8)
			 | ((red   & 0xFF) << 16)
			 | ((alpha & 0xFF) << 24);
	}
	
	/**
	 * Retrieve alpha component from an RGBA colour
	 * @param colour is the RGBA colour
	 * @return the alpha component
	 */
	public static int getAlphaFromColour (int colour) {
		return (colour & 0xFF000000) >>> 24;  // note: logical shift
	}
	
	/**
	 * Retrieve Red component from an RGBA colour
	 * @param colour is the RGBA colour
	 * @return the Red component
	 */
	public static int getRedFromColour (int colour) {
		return (colour & 0x00FF0000) >> 16;
	}
	
	/**
	 * Retrieve Green component from an RGBA colour
	 * @param colour is the RGBA colour
	 * @return the Green component
	 */
	public static int getGreenFromColour (int colour) {
		return (colour & 0x0000FF00) >> 8;
	}
	
	/**
	 * Retrieve Blue component from an RGBA colour
	 * @param colour is the RGBA colour
	 * @return the Blue component
	 */
	public static int getBlueFromColour (int colour) {
		return (colour & 0x000000FF);
	}
		
	/**
	 * Convert "\n" characters to the system line separator
	 * @param in is the string to be converted
	 * @return the converted string
	 */
	public static String newLines(String in) {
		return in.replace("\\n", NEW_LINE);
	}
	
	/**
	 * Returns a string array containing the values of an Enum Class
	 * @param ec the Enum class to obtain the values for
	 * @return the Enum values in a string array.
	 */
	public static String[] enumValues (Class ec) {
		if (!ec.isEnum()) {
			throw new IllegalArgumentException("Not an Enum");
		}
		Object[] values = ec.getEnumConstants();
		ArrayList<String> result = new ArrayList<String>();
		for (Object value : values) {
			result.add(value.toString());
		}
		return result.toArray(new String[result.size()]);
	}	
	
	/**
	 * Returns a string array containing the values of an Enum Type
	 * @param e the Enum to obtain the values for
	 * @return the Enum values in a string array.
	 */
	public static String[] enumValues (Enum<?> e) {
		Object[] values = e.getDeclaringClass().getEnumConstants();
		ArrayList<String> result = new ArrayList<String>();
		for (Object value : values) {
			result.add(value.toString());
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Returns a string containing the values of an Enum Type
	 * @param e the Enum to obtain the values for
	 * @return the Enum values formatted as string with commas.
	 */
	public static String enumValueList (Enum<?> e) {
		Object[] values = e.getDeclaringClass().getEnumConstants();
		StringBuilder sb = new StringBuilder();
		for (Object value : values) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(value.toString());
		}
		return sb.toString();
	}
	
	/**
	 * Returns a string containing the values of an Enum Class
	 * @param ec the Enum class to obtain the values for
	 * @return the Enum values formatted as string with commas.
	 */
	public static String enumValueList (Class ec) {
		if (!ec.isEnum()) {
			throw new IllegalArgumentException("Not an Enum");
		}
		Object[] values = ec.getEnumConstants();
		StringBuilder sb = new StringBuilder();
		for (Object value : values) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(value.toString());
		}
		return sb.toString();
	}
	
	/**
	 * Merge two lists into a single list
	 * @param first is the first list
	 * @param second is the second list
	 * @return a list containing both of the two lists
	 */
	public static List mergeLists (List first, List second) {
		if (first == null)
			return second;
		else if (second == null)
			return first;
		else {
			first.addAll(second);
			return first;
		}
	}

	/**
	 * Flattens a string array for printing with NO commas
	 * @param aString the string array to flatten
	 * @return the string for printing.
	 */
	public static String flattenArray (String[] aString) {
		StringBuilder sb = new StringBuilder();
		for (String s : aString) {
			if (sb.length() > 0) sb.append(" ");
			sb.append(s.toString());
		}
		return sb.toString();
	}
	
	/**
	 * Flattens a string array for printing with commas
	 * @param aString the string array to flatten
	 * @return the string for printing.
	 */
	public static String flattenArrayWithCommas (String[] aString) {
		StringBuilder sb = new StringBuilder();
		for (String s : aString) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(s.toString());
		}
		return sb.toString();
	}
	
    /**
     * Check for a prefix ignoring case
     * @param s is the string to check for a prefix
     * @param prefix is the prefix to check for
     * @return true if s starts with the prefix
     */
    public static boolean stringHasPrefix(String s, String prefix) {
    	return s.regionMatches(true, 0, prefix, 0, prefix.length());
    }
	
    /**
     * Returns a List of strings (chosen from the provided option strings) 
     * which the last entry in the word array is a beginning-match for.
     * <p>Case insensitive. Designed for Tab completion.</p>
     * @param words is the array whose last entry is to be matched
     * @param optionArray are the options to match against
     * @returns a List of all options that match the prefix given in the last entry of words
     */
    public static List<String> getMatchesOnLastWord(String[] words, String ... optionArray)
    {
        String s1 = words[words.length - 1];
        ArrayList<String> result = new ArrayList<String>();
        String[] options = optionArray;
        int i = (optionArray == null) ? 0 : optionArray.length;

        for (int j = 0; j < i; ++j)
        {
            String s2 = options[j];
            if (stringHasPrefix(s2, s1)) {            
                result.add(s2);
            }
        }
        return result;
    }
}
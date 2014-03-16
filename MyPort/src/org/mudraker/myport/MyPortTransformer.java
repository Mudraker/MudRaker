package org.mudraker.myport;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.launchwrapper.IClassTransformer;

public class MyPortTransformer implements IClassTransformer {

	/**
	 * Class transformer which is called for each class loaded allowing the 
	 * class byte-code to be modified on the fly
	 * @param className is the class being loaded
	 * @param transformedName is unused and purpose is unclear
	 * @param bytes is the original class byte-code
	 * @return the desired class byte-code (which may be the original or a replacement)
	 * @see net.minecraft.launchwrapper.IClassTransformer#transform(java.lang.String, java.lang.String, byte[])
	 */
	@Override
	public byte[] transform(String className, String transformedName, byte[] bytes) {
		// Check if the JVM is about to process the class we want to be replace
		if (className.equals("lh") || className.equals("net.minecraft.util.HttpUtil")) {
			System.out.println("********* INSIDE MYPORT: " + className);
			System.out.println("*** Found class "+className+", size "+bytes.length+", preparing to replace...");
			bytes = patchClassFromJar(className, bytes, MyPortPlugin.location);
		}
		return bytes;
	}

	/**
	 * Patches a minecraft base class being loaded with the copy from a JAR/ZIP file.
	 * @param className is the name of the class being loaded which MAY be obfuscated
	 * @param bytes is the original class byte-code
	 * @param location is the JAR file to load the class from
	 * @return the byte array of the replaced class byte-code
	 */
	private byte[] patchClassFromJar(String className, byte[] bytes, File location) {
		Logger l = MyPortDummyContainer.logger;
		
		try {
			// open the jar as zip and get entry to replace this class
			ZipFile zip = new ZipFile(location);
			ZipEntry entry = zip.getEntry(className.replace('.', '/') + ".class");
			if (entry == null) {
				l.log(Level.WARNING,"Unable to locate class "+className+" in jar "+location.getName());
				// System.out.println(name + " not found in " + location.getName());
			} else {
				// serialise the class file from the JAR into the bytes array
				InputStream zin = zip.getInputStream(entry);
				bytes = new byte[(int) entry.getSize()];
				zin.read(bytes);
				zin.close();
				l.log(Level.INFO,"Class "+className+" patched from jar "+location.getName()+" newsize "+bytes.length);
			}
			zip.close();
		} catch (Exception e) {
			throw new RuntimeException("Error overriding class " + className + " from " 
						+ (location == null ? "(null)" : location.getName()), e);
		}
		
		// return the new bytes
		return bytes;
	}
}

/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.myport;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

/**
 * MyPort FML Plugin
 * Pattern: FML Loading Plugin
 * @author MudRaker
 * @version %I%, %G%
 */
@MCVersion(value = "1.6.2")
@TransformerExclusions(value={"org.mudraker.myport"})
public class MyPortPlugin implements IFMLLoadingPlugin {

	// Location of the MyPort JAR file
	public static File location;

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{MyPortTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return MyPortDummyContainer.class.getName();
	}

	@Override
	public void injectData(Map<String, Object> data) {
		//This will retrieve the jar file of this mod
		location = (File) data.get("coremodLocation");
		System.out.println("*** MyPortPlugin jar location: " +(location==null ? "(null)" : location.getName()));
	}
	
	@Override
	public String[] getLibraryRequestClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}
}

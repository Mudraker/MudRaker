package org.mudraker.myport;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

/**
 * MyPort FML Plugin
 * Pattern: FML Loading Plugin
 * @author MudRaker
 * @version %I%, %G%
 */
@MCVersion(value = "1.6.4")
@TransformerExclusions(value={"mudraker.myport"})
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
		//System.out.println("*** MyPortPlugin jar location: " +location.getName());
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

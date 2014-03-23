package org.mudraker.ruler;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.World;

import org.mudraker.Log;

public class ProxyCommon {
	
	protected Map<String,EntityRulerMarker> markerEntities = new HashMap<String,EntityRulerMarker>();
	
	public void registerRendering() {
	}
	
	public EntityRulerMarker newMarkerEntity(World world, String uuid, int xPos, int yPos, int zPos) {
		EntityRulerMarker marker = new EntityRulerMarker (world, (double) xPos, (double) yPos, (double) zPos);
		Log.info("spawning new SERVER RulerMarker entity - uuid="+uuid+", entityId="+marker.entityId);
		world.spawnEntityInWorld(marker);
		markerEntities.put(uuid, marker);
		return marker;
	}

	public void destroyMarkerEntity(World world, String uuid) {
/*		
		EntityRulerMarker marker = getMarkerEntity (uuid);
		if (marker != null) {
			Log.info("killing RulerMarker entity "+uuid);
			marker.setDead();
			markerEntities.remove(uuid);
		}
*/			
	}	
	
	public EntityRulerMarker getMarkerEntity(World world, String uuid) {
		EntityRulerMarker marker = markerEntities.get(uuid);
		if (marker == null) {
			// damn - lost it, search the hard way
		}
		return marker;
	}
}
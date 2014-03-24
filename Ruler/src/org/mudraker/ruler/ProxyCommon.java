/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.ruler;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.mudraker.Log;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProxyCommon {
	
	/**
	 * Tracks association between player, ruler item and marker entity
	 * Remember this is created on BOTH client and server sides.
	 */
	protected HashMap<String, RulerRegistration> rulerMarkerMap;
	
	public void registerRendering() {
	}
	
	/**
	 * Create a new Marker Entity - SERVER side only
	 * @param world is the world to create in
	 * @param uuid is the unique id of the ruler item
	 * @param xPos is the current X location of the entity
	 * @param yPos is the current Y location of the entity
	 * @param zPos is the current Z location of the entity
	 * @return the marker entity reference
	 */
	@SideOnly(Side.SERVER)
	public EntityRulerMarker newMarkerEntity(World world, String uuid, int xPos, int yPos, int zPos) {
		EntityRulerMarker marker = new EntityRulerMarker (world, (double) xPos, (double) yPos, (double) zPos);
		Log.info("spawning new SERVER RulerMarker entity - uuid="+uuid+", entityId="+marker.entityId);
		world.spawnEntityInWorld(marker);
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
		RulerRegistration rulerRegistration = rulerMarkerMap.get(uuid);
		if (rulerRegistration == null) {
			// damn - lost it, search the hard way
		}
		return null;
	}

	public void registerRuler(World world, EntityPlayer entityPlayer, String uuid, ItemStack itemStack) {
		RulerItem rulerItem = (RulerItem) itemStack.getItem();
		RulerRegistration rulerRegistration = new RulerRegistration(entityPlayer, rulerItem);
		rulerMarkerMap.put(uuid, rulerRegistration);
	}
}
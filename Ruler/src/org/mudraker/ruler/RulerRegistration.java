/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package org.mudraker.ruler;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Ruler Marker structure
 * <p>Tracks player, ruler item, marker entity relationships.
 *
 * <p>Pattern: Forge proxyless mod.</p>
 * 
 * @author MudRaker
 */
public class RulerRegistration {
	private EntityPlayer player;
	private RulerItem rulerItem;
	private EntityRulerMarker entityRulerMarker;
	
	RulerRegistration (EntityPlayer player, RulerItem rulerItem, EntityRulerMarker entityRulerMarker) {
		this.player = player;
		this.rulerItem = rulerItem;
		this.entityRulerMarker = entityRulerMarker;
	}

	RulerRegistration (EntityPlayer player, RulerItem rulerItem) {
		this.player = player;
		this.rulerItem = rulerItem;
		this.entityRulerMarker = null;
	}

	public EntityPlayer getPlayer() {
		return player;
	}

	public RulerItem getRulerItem() {
		return rulerItem;
	}

	public EntityRulerMarker getEntityRulerMarker() {
		return entityRulerMarker;
	}

	public void setEntityRulerMarker(EntityRulerMarker entityRulerMarker) {
		this.entityRulerMarker = entityRulerMarker;
	}
}
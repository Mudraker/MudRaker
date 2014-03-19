package org.mudraker.ruler;

import java.util.HashMap;
import java.util.Map;

import org.mudraker.Log;

import net.minecraft.world.World;

public class ProxyCommon {
	
	private static final String DXYZFMT = "%d:%d:%d:%d";
	protected Map<String,EntitySlot> slotEntities = new HashMap<String,EntitySlot>();
	
	public void registerRendering() {
	}
	
	public EntitySlot newSlotEntity(World world, int xPos, int yPos, int zPos) {
		EntitySlot slot = new EntitySlot (world, (double) xPos, (double) yPos, (double) zPos);
		Log.info("new slot entity - putting in world");
		world.spawnEntityInWorld(slot);
		slotEntities.put(String.format(DXYZFMT, world.provider.dimensionId, xPos, yPos, zPos), slot);
		return slot;
	}
	
	public EntitySlot getSlotEntity(World world, int xPos, int yPos, int zPos) {
		return slotEntities.get(String.format(DXYZFMT, world.provider.dimensionId, xPos, yPos, zPos));
	}
}
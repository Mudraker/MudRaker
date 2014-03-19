package org.mudraker.ruler;

import org.mudraker.Log;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntitySlot extends Entity {

	public int[] slot = new int[] {0};

	public EntitySlot (World world) {
		super(world);
	}

	public EntitySlot (World world, double xPos, double yPos, double zPos) {
		super(world);
		setPositionAndRotation(xPos, yPos, zPos, 0, 0);
	}

	@Override
	protected void entityInit() {
		preventEntitySpawning = false;
		noClip = true;
		isImmuneToFire = true;
		this.motionX = 0.0;
		this.motionY = 0.0;
		this.motionZ = 0.0;	
		this.setSize (0.5F, 1.0F);
		Log.info("EntitySlot init pos("+this.posX+","+this.posY+","+this.posZ+")");
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound data) {
		Log.info("Read entitySlot");
		this.slot = data.getIntArray ("RulerSlot");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound data) {
		Log.info("Write entitySlot");
		data.setIntArray ("RulerSlot", this.slot);
	}
}

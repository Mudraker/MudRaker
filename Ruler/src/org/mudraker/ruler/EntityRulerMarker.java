/**
 * Copyright (C) 2014  MudRaker
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package org.mudraker.ruler;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.mudraker.Log;

public class EntityRulerMarker extends Entity {

	public int[] measure = new int[] { 0 };

	public EntityRulerMarker(World world) {
		super(world);
		Log.info("EntityRulerMarker instantiate at current pos(" + this.posX + "," + this.posY + "," + this.posZ + ")");
	}

	public EntityRulerMarker(World world, double xPos, double yPos, double zPos) {
		super(world);
		setPositionAndRotation(xPos, yPos, zPos, 0, 0);
		Log.info("EntityRulerMarker instantiate at forced pos(" + this.posX + "," + this.posY + "," + this.posZ + ")");
	}

	@Override
	protected void entityInit() {
		Log.info("EntityRulerMarker initialise");
		preventEntitySpawning = false;
		noClip = true;
		isImmuneToFire = true;
		this.motionX = 0.0;
		this.motionY = 0.0;
		this.motionZ = 0.0;	
		this.setSize (0.5F, 1.0F);
	}

	private static final String TAG_MEASURE = "RulerMarkerMeasure"; 
	
	@Override
	public void readEntityFromNBT(NBTTagCompound data) {
		Log.info("ReadEntityFromNBT");
		this.measure = data.getIntArray(TAG_MEASURE);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound data) {
		Log.info("WriteEntityToMBT");
		data.setIntArray(TAG_MEASURE, this.measure);
	}
/*
	@Override
	public boolean writeToNBTOptional(NBTTagCompound par1NBTTagCompound) {
		Log.info("writeToNBTOptional entityRulerMarker - in chunk? "+ this.addedToChunk);
		par1NBTTagCompound.setIntArray(TAG_MEASURE, this.measure);
		super.writeToNBTOptional(par1NBTTagCompound);
		return true;
	}

	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
		Log.info("writeToNBT entityRulerMarker");
		super.writeToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setIntArray(TAG_MEASURE, this.measure);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		Log.info("Read from NBT RulerMarker - in chunk? "+ this.addedToChunk);
		super.readFromNBT(data);
		this.measure = data.getIntArray(TAG_MEASURE);
	}
*/	
	@Override
    public boolean canBePushed()
    {
        return false;
    }
	
	@Override
    public void onUpdate()
    {
        super.onUpdate();
        //Log.info("on update pos("+posX+","+posY+","+posZ+") motion("+motionX+","+motionY+","+motionZ+") in chunk="+this.addedToChunk);
    }
/*	
	@Override
    public void onChunkLoad()
    {
        super.onChunkLoad();
       // Log.info("**** onChunkLoad pos("+posX+","+posY+","+posZ+") motion("+motionX+","+motionY+","+motionZ+") in chunk="+this.addedToChunk);
    }
*/
}
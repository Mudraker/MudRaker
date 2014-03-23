package org.mudraker.ruler;

import java.util.UUID;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;

import org.lwjgl.input.Keyboard;
import org.mudraker.Log;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

/**
 * Ruler Item definition
 * 
 * <p>Pattern: Item</p>
 * 
 * @author MudRaker
 */
public class RulerItem extends Item {

	public RulerItem (int itemId) {
		super(itemId);
        this.setHasSubtypes(false);
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setUnlocalizedName("genericItem");
        this.setTextureName(ModInfo.ID.toLowerCase()+":Ruler16");
        LanguageRegistry.addName(this, "Ruler");
        GameRegistry.addRecipe(new ItemStack(this, 1), new Object[]{
            "  A", "BA ", "A  ", 
            Character.valueOf('A'), Item.stick, 
            Character.valueOf('B'), new ItemStack(Item.dyePowder, 1).getItem()});
	}
	
    /**
     * Ensure item NBT is shared
     * @see net.minecraft.item.Item#getShareTag()
     */
    @Override
	public boolean getShareTag()
    {
        return true;
    }
	
    /**
     * Callback for item creation
     */
    @Override
    public void onCreated(ItemStack itemStack, World world, EntityPlayer entityPlayer) {
    	NBTTagCompound ruler = initRuler(world, itemStack);
    	Log.info ("Initialised ruler:" + rulerToString(ruler));		
    }

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
	@Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side, float xOffset, float yOffset, float zOffset) {
        if (!world.isRemote) {
        	boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);        	
        	boolean ctrlDown = Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);        	
        	boolean altDown = Keyboard.isKeyDown(Keyboard.KEY_RMENU) || Keyboard.isKeyDown(Keyboard.KEY_LMENU);        	
        	Log.fine ("Ruler used ("+x+","+y+","+z+"@"+Facing.facings[side]+") "
        			+(shiftDown?"SHIFT ":"")+(ctrlDown?"CTRL ":"")+(altDown?"ALT ":"")
        			+"offset("+xOffset+","+yOffset+","+zOffset+")");
        	
        	if (altDown) {
        		// alt right click clears the ruler details
        		clearRuler(world, itemStack);
        		//RulerMod.proxy.destroyMarkerEntity(world, getUUID(itemStack));
        	} else {
            	NBTTagCompound ruler = getRuler(world, itemStack);
	        	int[] aMeasure = getActiveMeasure(ruler);
	        	if (aMeasure == null || aMeasure.length < 1) aMeasure = newMeasure(ruler);
	        	aMeasure = setActiveMeasure(ruler, appendPoint(aMeasure, x+Facing.offsetsXForSide[side], 
	        			y+Facing.offsetsYForSide[side], z+Facing.offsetsZForSide[side]));
	        	if (getMeasureSize(aMeasure) == 1) {
	        		String uuid = getUUID (ruler);
	        		Log.info("Creating RulerMarkerEntity "+uuid+" at ("+aMeasure[1]+","+aMeasure[2]+","+aMeasure[3]+")");
	        		RulerMod.proxy.newMarkerEntity(world, uuid, aMeasure[1], aMeasure[2], aMeasure[3]);
	        	}
        	}
        }
        return true;
    }

	// **************************************************************************

	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void OnEntityConstructing (EntityConstructing event) {
		if (event.entity instanceof EntityRulerMarker) {
			EntityRulerMarker marker = (EntityRulerMarker) event.entity;
			Log.info("RulerMarkerEntity constructing: " + marker.entityId);
		}
	}
	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void OnEntityEnteringChunk (EnteringChunk event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			Log.info("Player Entity (" + player.username+") leave chunk("
			+event.oldChunkX+","+event.oldChunkZ+") enter ("
			+event.newChunkX+","+event.newChunkZ+")");
		} else if (event.entity instanceof EntityRulerMarker) {
			EntityRulerMarker marker = (EntityRulerMarker) event.entity;
			Log.info("RulerMarkerEntity (" + marker.entityId + ") leave chunk("
					+event.oldChunkX+","+event.oldChunkZ+") enter ("
					+event.newChunkX+","+event.newChunkZ+")");
		}
	}

	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void OnEntityJoinWorld (EntityJoinWorldEvent event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			Log.info("Player Entity (" + player.username+") join world");
		} else if (event.entity instanceof EntityRulerMarker) {
			EntityRulerMarker marker = (EntityRulerMarker) event.entity;
			Log.info("RulerMarkerEntity (" + marker.entityId + ") join world");
		}
	}

	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void OnItemTossEvent (ItemTossEvent event) {
		ItemStack itemStack = event.entityItem.getEntityItem();
		if (itemStack != null && itemStack.getItem() instanceof RulerItem) {
			NBTTagCompound ruler = getRuler(event.player.worldObj, itemStack);
			Log.info("Player Entity (" + event.player.username +") tossed ruler: "+rulerToString(ruler));
		}
	}
	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void OnLivingDropsEvent (LivingDropsEvent event) {
		if (event.entity instanceof EntityPlayer) {
			for (EntityItem drops : event.drops) {
				ItemStack itemStack = drops.getEntityItem();
				if (itemStack != null && itemStack.getItem() instanceof RulerItem) {
					EntityPlayer player = (EntityPlayer) event.entity;
					NBTTagCompound ruler = getRuler(event.entityLiving.worldObj, itemStack);
					Log.info("Player Died (" + player.username +") dropping ruler: "+rulerToString(ruler));
				}
			}
		}
	}

	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void OnEntityItemPickupEvent (EntityItemPickupEvent event) {
		ItemStack itemStack = event.item.getEntityItem();
		if (itemStack != null && itemStack.getItem() instanceof RulerItem) {
			NBTTagCompound ruler = getRuler(event.entityPlayer.worldObj, itemStack);
			Log.info("Player Entity (" + event.entityPlayer.username +") picked up ruler: "+rulerToString(ruler));
		}
	}

	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void OnPlayerDestroyItemEvent (PlayerDestroyItemEvent event) {
		ItemStack itemStack = event.original;
		if (itemStack != null && itemStack.getItem() instanceof RulerItem) {
			NBTTagCompound ruler = getRuler(event.entityPlayer.worldObj, itemStack);
			Log.info("Player Entity (" + event.entityPlayer.username +") destroyed ruler: "+rulerToString(ruler));
		}
	}
	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void OnPlayerDropsEvent (PlayerDropsEvent event) {
		for (EntityItem drops : event.drops) {
			ItemStack itemStack = drops.getEntityItem();
			if (itemStack != null && itemStack.getItem() instanceof RulerItem) {
				NBTTagCompound ruler = getRuler(event.entityPlayer.worldObj, itemStack);
				Log.info("Player Died (" + event.entityPlayer.username +") dropping ruler: "+rulerToString(ruler));
			}
		}
	}

/*	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onCheckSpawn (CheckSpawn event) {
		//Log.info("Check spawn");
		//event.setResult(Result.DENY);
	}

	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onLivingUpdateEvent (LivingUpdateEvent event) {
		//Log.info("Living update");
		//if (!(event.entityLiving instanceof EntityPlayer))
		//	event.entityLiving.setDead();
	}
*/
/*	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onChunkLoad (ChunkEvent.Load event) {
		Chunk chunk = event.getChunk();
		Log.info("Chunk load ("+chunk.xPosition+","+chunk.zPosition+") "+chunk);
		if (chunk.xPosition == markerChunkX && chunk.zPosition == markerChunkZ) {
			Log.info("Marker chunk");
		}
	}
	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onChunkUnload (ChunkEvent.Unload event) {
		Chunk chunk = event.getChunk();
		Log.info("Chunk unload ("+chunk.xPosition+","+chunk.zPosition+") "+chunk);
		if (chunk.xPosition == markerChunkX && chunk.zPosition == markerChunkZ) {
			Log.info("Marker chunk");
		}
	}

	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onChunkDataLoad (ChunkDataEvent.Load event) {
		Chunk chunk = event.getChunk();
		NBTTagCompound nbt = event.getData();
		if ((chunk.xPosition == markerChunkX && chunk.zPosition == markerChunkZ) ||
			(chunk.xPosition == 10 && chunk.zPosition == 15)) {
			Log.info("Marker Chunk data load ("+chunk.xPosition+","+chunk.zPosition+") "+chunk);
			Log.info("Marker chunk");
		}
	}
	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onChunkDataSave (ChunkDataEvent.Save event) {
		Chunk chunk = event.getChunk();
		NBTTagCompound nbt = event.getData();
		if (chunk.xPosition == markerChunkX && chunk.zPosition == markerChunkZ) {
			Log.info("Saving Marker chunk ("+chunk.xPosition+","+chunk.zPosition+")");
		}
	}
	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onWorldLoad (WorldEvent.Load event) {
		Log.info("World load");
	}
	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onWorldUnload (WorldEvent.Unload event) {
		Log.info("World unload");
	}
	
	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onWorldSave (WorldEvent.Save event) {
		Log.info("World save");
	}
*/	
	// **************************************************************************
	
	private static final String RULER   = "Ruler";
	private static final String UNIQUE  = "UUID";
	private static final String COUNT   = "MeasureCount";
	private static final String ACTIVE  = "MeasureActive";
	private static final String MEASURE_FMT = "Measure#%02d";
    
	public static NBTTagCompound getRuler (World world, ItemStack itemStack) {
    	if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbtItem = itemStack.getTagCompound();
		if (!nbtItem.hasKey(RULER)) {
			return initRuler(world, itemStack);
		} else {
			return nbtItem.getCompoundTag(RULER);
		}
	}
	
	public static NBTTagCompound initRuler (World world, ItemStack itemStack) {
    	if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbtItem = itemStack.getTagCompound();		
		NBTTagCompound nbt = new NBTTagCompound();
		if (!world.isRemote) {
			UUID uuid = UUID.randomUUID();
			nbt.setString(UNIQUE, uuid.toString());
		}
		nbt.setInteger(COUNT, 0);
		nbt.setInteger(ACTIVE, 0);
		nbtItem.setCompoundTag(RULER, nbt);
		return nbt;
	}
	
	public static NBTTagCompound clearRuler (World world, ItemStack itemStack) {
		NBTTagCompound ruler = getRuler(world, itemStack);
		ruler.setInteger(COUNT, 0);
		ruler.setInteger(ACTIVE, 0);
		return ruler;
	}
	
	public static String getUUID (NBTTagCompound ruler) {
		return (ruler == null) ? null : ruler.getString(UNIQUE);
	}
	
	public static String getUUID (World world, ItemStack itemStack) {
		return getUUID (getRuler(world, itemStack));
	}
	
	public static int getNumMeasures (NBTTagCompound ruler) {
		return (ruler == null) ? 0 : ruler.getInteger(COUNT);
	}
	
	public static int getActiveMeasureId (NBTTagCompound ruler) {
		return (ruler == null) ? 0 : ruler.getInteger(ACTIVE);
	}
	
	public static void setMeasureActiveId (NBTTagCompound ruler, int n) {
		ruler.setInteger(ACTIVE, n);
	}
	
	public static int[] getActiveMeasure (NBTTagCompound ruler) {
		return (ruler == null) ? null : getMeasure (ruler, ruler.getInteger(ACTIVE));
	}

	public static int[] newMeasure (NBTTagCompound ruler) {
		int n = getNumMeasures(ruler)+1;
		String s = String.format(MEASURE_FMT, n);
		ruler.setInteger(COUNT, n);
		ruler.setInteger(ACTIVE, n);
		ruler.setIntArray (s, new int[] {0});
		return ruler.getIntArray (s);
	}
	
	public static int getMeasureSize (NBTTagCompound ruler, int measureId) {
		return getMeasureSize(ruler.getIntArray(String.format(MEASURE_FMT, measureId)));
	}
	
	public static int getMeasureSize (int[] aMeasure) {
		return (aMeasure == null || aMeasure.length < 1) ? 0 : aMeasure[0] / 3; 
	}
	
	public static int[] getMeasure (NBTTagCompound ruler, int measureId) {
		return ruler.getIntArray(String.format(MEASURE_FMT, measureId));
	}
	
	public static int[] setMeasure (NBTTagCompound ruler, int measureId, int ... aMeasure) {
		ruler.setIntArray(String.format(MEASURE_FMT, measureId), aMeasure);
		Log.fine("setMeasure #"+measureId+": "+rulerToString(ruler));
		return aMeasure;
	}
	
	public static int[] setActiveMeasure (NBTTagCompound ruler, int ... aInt) {
		return setMeasure(ruler, ruler.getInteger(ACTIVE), aInt);
	}
	
	public static int[] appendPoint (int[] aInt, int ... aPoint) {
		int size = aInt.length;
		int add = aPoint.length;
		int [] aResult = new int[size + add];
		aResult[0] = size + add - 1; // don't count the length at the beginning
		System.arraycopy (aInt, 1, aResult, 1, size-1);
		System.arraycopy (aPoint, 0, aResult, size, add);
		return aResult;
	}
	
	public static String rulerToString (NBTTagCompound ruler) {
		if (ruler == null)
			return "<uninitialised>";
		else {
			int max = getNumMeasures(ruler);
			StringBuilder sb = new StringBuilder();
			sb.append('<');
			sb.append (getUUID(ruler));
			sb.append("> <");
			sb.append (getActiveMeasureId(ruler));
			sb.append ('/');
			sb.append (max);
			if (max > 0) sb.append(':');
			for (int n = 1; n <= max; n++) {
				int[] aMeasure = getMeasure(ruler, n);
				sb.append(" #");
				sb.append(n);
				sb.append('*');
				sb.append(aMeasure[0]);
				if (aMeasure[0] > 0) sb.append(" [");
				for (int i = 1; i <= aMeasure[0]; i += 3) {
					sb.append('(');
					sb.append(aMeasure[i]);
					sb.append(',');
					sb.append(aMeasure[i+1]);
					sb.append(',');
					sb.append(aMeasure[i+2]);
					sb.append(')');
				}
				if (aMeasure[0] > 0) sb.append(']');
			}
			sb.append('>');
			return sb.toString();
		}
	}
}

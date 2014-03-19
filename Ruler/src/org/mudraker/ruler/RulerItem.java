package org.mudraker.ruler;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Facing;
import net.minecraft.world.World;

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
    	NBTTagCompound ruler = initRuler(itemStack);
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
        		clearRuler(itemStack);
        	} else {
            	NBTTagCompound ruler = getRuler(itemStack);
	        	int[] aSlot = getActiveSlot(ruler);
	        	if (aSlot == null || aSlot.length < 1) aSlot = newSlot(ruler);
	        	aSlot = setActiveSlot(ruler, appendPoint(aSlot, x+Facing.offsetsXForSide[side], 
	        			y+Facing.offsetsYForSide[side], z+Facing.offsetsZForSide[side]));
	        	if (getSlotSize(aSlot) == 1) {
	        		Minecraft mc = Minecraft.getMinecraft();
	        		Log.info("Creating slotEntity at ("+aSlot[1]+","+aSlot[2]+","+aSlot[3]+")");
	        		RulerMod.proxy.newSlotEntity(mc.theWorld, aSlot[1], aSlot[2], aSlot[3]);
	        	}
        	}
        }
        return true;
    }
	
	private static final String RULER   = "Ruler";
	private static final String UNIQUE  = "UUID";
	private static final String COUNT   = "SlotCount";
	private static final String ACTIVE  = "SlotActive";
	private static final String SLOTFMT = "Slot#%02d";
    
	public static NBTTagCompound getRuler (ItemStack itemStack) {
    	if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbtItem = itemStack.getTagCompound();
		if (!nbtItem.hasKey(RULER)) {
			return initRuler(itemStack);
		} else {
			return nbtItem.getCompoundTag(RULER);
		}
	}
	
	public static NBTTagCompound initRuler (ItemStack itemStack) {
    	if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbtItem = itemStack.getTagCompound();		
		NBTTagCompound nbt = new NBTTagCompound();
		Minecraft mc = Minecraft.getMinecraft();
		if (mc != null && mc.theWorld != null && !mc.theWorld.isRemote) {
			UUID uuid = UUID.randomUUID();
			nbt.setString(UNIQUE, uuid.toString());
		}
		nbt.setInteger(COUNT, 0);
		nbt.setInteger(ACTIVE, 0);
		nbtItem.setCompoundTag(RULER, nbt);
		return nbt;
	}
	
	public static NBTTagCompound clearRuler (ItemStack itemStack) {
		NBTTagCompound ruler = getRuler(itemStack);
		ruler.setInteger(COUNT, 0);
		ruler.setInteger(ACTIVE, 0);
		return ruler;
	}
	
	public static String getUUID (NBTTagCompound ruler) {
		return (ruler == null) ? null : ruler.getString(UNIQUE);
	}
	
	public static int getNumSlots (NBTTagCompound ruler) {
		return (ruler == null) ? 0 : ruler.getInteger(COUNT);
	}
	
	public static int getSlotActive (NBTTagCompound ruler) {
		return (ruler == null) ? 0 : ruler.getInteger(ACTIVE);
	}
	
	public static void setSlotActive (NBTTagCompound ruler, int n) {
		ruler.setInteger(ACTIVE, n);
	}
	
	public static int[] getActiveSlot (NBTTagCompound ruler) {
		return (ruler == null) ? null : getSlot (ruler, ruler.getInteger(ACTIVE));
	}

	public static int[] newSlot (NBTTagCompound ruler) {
		int n = getNumSlots(ruler)+1;
		String s = String.format(SLOTFMT, n);
		ruler.setInteger(COUNT, n);
		ruler.setInteger(ACTIVE, n);
		ruler.setIntArray (s, new int[] {0});
		return ruler.getIntArray (s);
	}
	
	public static int getSlotSize (NBTTagCompound ruler, int slot) {
		return getSlotSize(ruler.getIntArray(String.format(SLOTFMT, slot)));
	}
	
	public static int getSlotSize (int[] aSlot) {
		return (aSlot == null || aSlot.length < 1) ? 0 : aSlot[0] / 3; 
	}
	
	public static int[] getSlot (NBTTagCompound ruler, int slot) {
		return ruler.getIntArray(String.format(SLOTFMT, slot));
	}
	
	public static int[] setSlot (NBTTagCompound ruler, int slot, int ... aInt) {
		ruler.setIntArray(String.format(SLOTFMT, slot), aInt);
		Log.fine("setSlot #"+slot+": "+rulerToString(ruler));
		return aInt;
	}
	
	public static int[] setActiveSlot (NBTTagCompound ruler, int ... aInt) {
		return setSlot(ruler, ruler.getInteger(ACTIVE), aInt);
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
			int max = getNumSlots(ruler);
			StringBuilder sb = new StringBuilder();
			sb.append('<');
			sb.append (getUUID(ruler));
			sb.append("> <");
			sb.append (getSlotActive(ruler));
			sb.append ('/');
			sb.append (max);
			if (max > 0) sb.append(':');
			for (int n = 1; n <= max; n++) {
				int[] slot = getSlot(ruler, n);
				sb.append(" #");
				sb.append(n);
				sb.append('*');
				sb.append(slot[0]);
				if (slot[0] > 0) sb.append(" [");
				for (int i = 1; i <= slot[0]; i += 3) {
					sb.append('(');
					sb.append(slot[i]);
					sb.append(',');
					sb.append(slot[i+1]);
					sb.append(',');
					sb.append(slot[i+2]);
					sb.append(')');
				}
				if (slot[0] > 0) sb.append(']');
			}
			sb.append('>');
			return sb.toString();
		}
	}
}

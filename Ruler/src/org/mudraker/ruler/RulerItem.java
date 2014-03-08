package org.mudraker.ruler;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
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
        this.maxStackSize = 1;
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setUnlocalizedName("genericItem");
        this.setTextureName(ModInfo.ID.toLowerCase()+":Ruler16");
        LanguageRegistry.addName(this, "Ruler");
	}
}

package com.tighug.lolipickaxe.item.Tool;

import com.tighug.lolipickaxe.inventory.LoliInventory;
import net.minecraft.item.ItemStack;

public interface IContainer {
	
	boolean hasInventory(ItemStack stack);

	default int getLevel(ItemStack stack){
		return 0;
	}

	LoliInventory getInventory(ItemStack stack);

}

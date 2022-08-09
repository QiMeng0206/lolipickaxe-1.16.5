package com.tighug.lolipickaxe.item.Tool;

import com.tighug.lolipickaxe.inventory.LoliInventory;
import net.minecraft.item.ItemStack;

public interface IContainer {
	
	boolean hasInventory(ItemStack stack);

	LoliInventory getInventory(ItemStack stack);

}

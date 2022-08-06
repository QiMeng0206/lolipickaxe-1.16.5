package com.tighug.lolipickaxe.item.Tool;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface ILoli {

	String CONFIG = "LoliConfig";

	boolean hasOwner(ItemStack stack);

	boolean isOwner(ItemStack stack, PlayerEntity player);


}

package com.tighug.lolipickaxe.item;

import com.tighug.lolipickaxe.item.addon.ItemEntitySoul;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ModItemGroup {
    public static final ItemGroup ITEM_GROUP_LOLI = new ItemGroup("loli") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return ModItems.ITEM_LOLI_PICKAXE.get().getDefaultInstance();
        }
    };
    public static final ItemGroup ITEM_GROUP_LOLI_ADDON = new ItemGroup("loli_addon") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return ItemEntitySoul.getItemEntitySoul(9).getDefaultInstance();
        }
    };
}

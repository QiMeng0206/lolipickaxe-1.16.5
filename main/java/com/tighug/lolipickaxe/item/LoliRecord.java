package com.tighug.lolipickaxe.item;

import com.tighug.lolipickaxe.util.ModSoundEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.NonNullList;
import org.jetbrains.annotations.NotNull;

public class LoliRecord extends MusicDiscItem {

    public LoliRecord() {
        super(15, ModSoundEvents.lolirecord, new Properties());
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    @Override
    public void fillItemCategory(@NotNull ItemGroup p_150895_1_, @NotNull NonNullList<ItemStack> p_150895_2_) {
        if (p_150895_1_ == ModItemGroup.ITEM_GROUP_LOLI) {
            p_150895_2_.add(this.getDefaultInstance());
        }
    }
}

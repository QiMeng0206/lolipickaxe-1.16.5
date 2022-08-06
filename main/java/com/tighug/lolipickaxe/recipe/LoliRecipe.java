package com.tighug.lolipickaxe.recipe;

import com.google.common.collect.Lists;
import com.tighug.lolipickaxe.item.ModItems;
import com.tighug.lolipickaxe.item.Tool.ItemSmallLoliPickaxe;
import com.tighug.lolipickaxe.item.addon.Addon;
import com.tighug.lolipickaxe.item.addon.ItemEntitySoul;
import com.tighug.lolipickaxe.item.addon.ItemLoliAddon;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LoliRecipe extends SpecialRecipe {
    public LoliRecipe(ResourceLocation p_i48169_1_) {
        super(p_i48169_1_);
    }

    @Override
    public boolean matches(@NotNull CraftingInventory p_77569_1_, @NotNull World p_77569_2_) {
        return this.matches(p_77569_1_);
    }

    private boolean matches(@NotNull CraftingInventory p_77569_1_) {
        return !this.assemble(p_77569_1_).isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInventory p_77572_1_) {
        ItemStack loli = ItemStack.EMPTY;
        List<ItemStack> list = Lists.newArrayList();
        for (int i = 0; i < p_77572_1_.getContainerSize(); ++i){
            ItemStack itemstack1 = p_77572_1_.getItem(i);
            if (!itemstack1.isEmpty()){
                if (itemstack1.getItem() instanceof ItemSmallLoliPickaxe){
                    if (loli.isEmpty()) loli = itemstack1.copy();
                    else return ItemStack.EMPTY;
                }
                else if (itemstack1.getItem() instanceof ItemLoliAddon || itemstack1.getItem() instanceof ItemEntitySoul.SoulEnd){
                    list.add(itemstack1.copy());
                }
                else return ItemStack.EMPTY;
            }
        }
        if (!list.isEmpty() && !loli.isEmpty()) {
            if (!loli.getOrCreateTag().contains(ItemSmallLoliPickaxe.SMALL_LOLI_TAG, 10)){
                loli.getOrCreateTag().put(ItemSmallLoliPickaxe.SMALL_LOLI_TAG, new CompoundNBT());
            }
            CompoundNBT nbt = loli.getOrCreateTag().getCompound(ItemSmallLoliPickaxe.SMALL_LOLI_TAG);
            if (list.size() == 1 && list.get(0).getItem() instanceof ItemEntitySoul.SoulEnd){
                for (ItemLoliAddon.Type type : ItemLoliAddon.Type.values()) {
                    if (ItemSmallLoliPickaxe.getLevel(type, loli) < type.getMax()) {
                        return ItemStack.EMPTY;
                    }
                }
                return ModItems.ITEM_LOLI_PICKAXE.get().getDefaultInstance();
            }
            for (ItemStack itemStack : list){
                if (itemStack.isEmpty()) return ItemStack.EMPTY;
                ItemLoliAddon.Type value = ItemLoliAddon.getType(itemStack);
                if (value == null) return ItemStack.EMPTY;
                int i = Addon.getLevel(itemStack);
                if (i == 0){
                    if (value.hasLevel(loli)) return ItemStack.EMPTY;
                    else {
                        nbt.putInt(value.getName(), 0);
                    }
                }
                else {
                    if (!value.hasLevel(loli) || value.getLevel(loli) != i - 1) return ItemStack.EMPTY;
                    else {
                        nbt.putInt(value.getName(), i);
                    }
                }
            }
        }
        return loli;
    }

    @Override
    public boolean canCraftInDimensions(int p_194133_1_, int p_194133_2_) {
        return p_194133_1_ * p_194133_2_ >= 1;
    }

    @Override
    public @NotNull IRecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SERIALIZER_REGISTRY_OBJECT.get();
    }
}

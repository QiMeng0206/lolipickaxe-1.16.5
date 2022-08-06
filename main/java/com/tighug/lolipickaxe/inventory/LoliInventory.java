package com.tighug.lolipickaxe.inventory;

import com.tighug.lolipickaxe.item.Tool.ILoli;
import com.tighug.lolipickaxe.item.Tool.ItemSmallLoliPickaxe;
import com.tighug.lolipickaxe.item.addon.ItemLoliAddon;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IRecipeHelperPopulator;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class LoliInventory implements IInventory, IRecipeHelperPopulator {
    public static final LoliInventory EMPTY = new LoliInventory();
    public static final String INVENTORY = "loliInventory";
    private final ItemStack itemStack;
    private final int size;
    private final NonNullList<ItemStack> items;
    private final int maxStackSize;
    public final int maxPage;

    public LoliInventory(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
        if (itemStack.getItem() instanceof ILoli){
            this.maxStackSize = Integer.MAX_VALUE;
            this.maxPage = 64;
        }
        else if (itemStack.getItem() instanceof ItemSmallLoliPickaxe){
            this.maxPage = (int) ItemSmallLoliPickaxe.getValue(ItemLoliAddon.Type.STORAGE_CAPACITY, itemStack);
            this.maxStackSize = this.maxPage * 32;
        }
        else {
            this.maxPage = 1;
            this.maxStackSize = this.maxPage * 32;
        }
        this.size = this.maxPage * 81;
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
        CompoundNBT tag = itemStack.getOrCreateTag();
        if (!tag.contains(INVENTORY, 9)) {
            tag.put(INVENTORY, new ListNBT());
        }
        this.readTag(tag);
    }

    private LoliInventory() {
        this.itemStack = ItemStack.EMPTY;
        this.size = 0;
        this.items = NonNullList.create();
        this.maxStackSize = 0;
        this.maxPage = 0;
    }


    @Nonnull
    public ItemStack getItem(int p_70301_1_) {
        return p_70301_1_ >= 0 && p_70301_1_ < this.items.size() ? this.items.get(p_70301_1_) : ItemStack.EMPTY;
    }

    public List<ItemStack> removeAllItems() {
        List<ItemStack> list = this.items.stream().filter((p_233544_0_) -> !p_233544_0_.isEmpty()).collect(Collectors.toList());
        this.clearContent();
        return list;
    }

    @Nonnull
    public ItemStack removeItem(int p_70298_1_, int p_70298_2_) {
        ItemStack itemstack = ItemStackHelper.removeItem(this.items, p_70298_1_, p_70298_2_);
        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    public ItemStack removeItemType(Item p_223374_1_, int p_223374_2_) {
        ItemStack itemstack = new ItemStack(p_223374_1_, 0);

        for(int i = this.size - 1; i >= 0; --i) {
            ItemStack itemstack1 = this.getItem(i);
            if (itemstack1.getItem().equals(p_223374_1_)) {
                int j = p_223374_2_ - itemstack.getCount();
                ItemStack itemstack2 = itemstack1.split(j);
                itemstack.grow(itemstack2.getCount());
                if (itemstack.getCount() == p_223374_2_) {
                    break;
                }
            }
        }

        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    public ItemStack addItem(ItemStack p_174894_1_) {
        ItemStack itemstack = p_174894_1_.copy();
        this.moveItemToOccupiedSlotsWithSameType(itemstack);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.moveItemToEmptySlots(itemstack);
            return itemstack.isEmpty() ? ItemStack.EMPTY : itemstack;
        }
    }

    public boolean canAddItem(ItemStack p_233541_1_) {
        boolean flag = false;

        for(ItemStack itemstack : this.items) {
            if (itemstack.isEmpty() || this.isSameItem(itemstack, p_233541_1_) && itemstack.getCount() < maxStackSize) {
                flag = true;
                break;
            }
        }

        return flag;
    }

    @Nonnull
    public ItemStack removeItemNoUpdate(int p_70304_1_) {
        ItemStack itemstack = this.items.get(p_70304_1_);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.items.set(p_70304_1_, ItemStack.EMPTY);
            return itemstack;
        }
    }

    public void setItem(int p_70299_1_, @Nonnull ItemStack p_70299_2_) {
        this.items.set(p_70299_1_, p_70299_2_);
        if (!p_70299_2_.isEmpty() && p_70299_2_.getCount() > this.getMaxStackSize()) {
            p_70299_2_.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return maxStackSize;
    }

    public int getContainerSize() {
        return this.size;
    }

    public boolean isEmpty() {
        for(ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public void setChanged() {
        this.saveTag();
    }

    public boolean stillValid(@Nonnull PlayerEntity p_70300_1_) {
        return true;
    }

    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    public void fillStackedContents(@Nonnull RecipeItemHelper p_194018_1_) {
        for(ItemStack itemstack : this.items) {
            p_194018_1_.accountStack(itemstack);
        }

    }

    public String toString() {
        return this.items.stream().filter((p_223371_0_) -> !p_223371_0_.isEmpty()).collect(Collectors.toList()).toString();
    }

    private void moveItemToEmptySlots(ItemStack p_223375_1_) {
        for(int i = 0; i < this.size; ++i) {
            ItemStack itemstack = this.getItem(i);
            if (itemstack.isEmpty()) {
                this.setItem(i, p_223375_1_.copy());
                p_223375_1_.setCount(0);
                return;
            }
        }

    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack p_223372_1_) {
        for(int i = 0; i < this.size; ++i) {
            ItemStack itemstack = this.getItem(i);
            if (this.isSameItem(itemstack, p_223372_1_)) {
                this.moveItemsBetweenStacks(p_223372_1_, itemstack);
                if (p_223372_1_.isEmpty()) {
                    return;
                }
            }
        }

    }

    private boolean isSameItem(ItemStack p_233540_1_, ItemStack p_233540_2_) {
        return p_233540_1_.getItem() == p_233540_2_.getItem() && ItemStack.tagMatches(p_233540_1_, p_233540_2_);
    }

    private void moveItemsBetweenStacks(ItemStack p_223373_1_, ItemStack p_223373_2_) {
        int i = this.getMaxStackSize();
        int j = Math.min(p_223373_1_.getCount(), i - p_223373_2_.getCount());
        if (j > 0) {
            p_223373_2_.grow(j);
            p_223373_1_.shrink(j);
            this.setChanged();
        }

    }

    public void readTag(CompoundNBT nbt) {
        ListNBT listNBT = nbt.getList(INVENTORY,10);
        for(int i = 0; i < listNBT.size(); ++i) {
            CompoundNBT compoundnbt = listNBT.getCompound(i);
            int j = compoundnbt.getShort("Slot");
            int k = compoundnbt.getInt("loliCount");
            if (j >= 0 && j < this.items.size()) {
                ItemStack itemStack1 = ItemStack.of(compoundnbt);
                itemStack1.setCount(k);
                this.items.set(j, itemStack1);
            }
        }

    }

    public void saveTag() {
        CompoundNBT nbt = this.itemStack.getOrCreateTag();
        ListNBT listNBT = new ListNBT();
        for (int i = 0 ; i < this.items.size() ; ++i){
            ItemStack itemStack1 = this.items.get(i);
            if (!itemStack1.isEmpty()){
                CompoundNBT c = new CompoundNBT();
                c.putShort("Slot", (short) i);
                c.putInt("loliCount",itemStack1.getCount());
                ItemStack itemStack2 = itemStack1.copy();
                itemStack2.setCount(1);
                itemStack2.save(c);
                listNBT.add(c);
            }
        }
        if (!listNBT.isEmpty()) nbt.put(INVENTORY, listNBT);
        else nbt.remove(INVENTORY);
    }

    public static class LoliSlot extends Slot{

        public LoliSlot(LoliInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
            super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        }


    }

}

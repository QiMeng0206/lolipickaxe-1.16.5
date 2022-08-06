package com.tighug.lolipickaxe.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tighug.lolipickaxe.item.Tool.IContainer;
import com.tighug.lolipickaxe.item.Tool.ILoli;
import com.tighug.lolipickaxe.player.LoliPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

import static com.tighug.lolipickaxe.inventory.ModContainerType.lolipickaxeContainerType;

public class LoliPickaxeContainer extends Container {
    public LoliInventory INVENTORY;
    public final PlayerInventory PLAYER;
    private int PAGE = 0;
    private final int maxStackSize;
    private final Set<Slot> quickcraftEmptySlots = Sets.newHashSet();
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();

    public LoliPickaxeContainer(int id, PlayerInventory playerInventory) {
        super(lolipickaxeContainerType.get(), id);
        PLAYER = playerInventory;
        ItemStack itemStack = PLAYER.getSelected();
        IContainer iContainer = (IContainer) itemStack.getItem();
        if (playerInventory.player instanceof ServerPlayerEntity && itemStack.getItem() instanceof ILoli) {
            playerInventory.player.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
                if (iLoliPlayer instanceof LoliPlayer) {
                    INVENTORY = ((LoliPlayer) iLoliPlayer).getLoliInventory();
                }
            });
        }
        if (INVENTORY == null) INVENTORY = iContainer.getInventory(itemStack);
        this.addSlot(INVENTORY);
        this.INVENTORY.startOpen(PLAYER.player);
        this.addPlayerInventorySlot(PLAYER);
        maxStackSize = this.INVENTORY.getMaxStackSize();
    }

    @Nonnull
    @Override
    public ItemStack clicked(int slot, int mouseclick, @Nonnull ClickType clickType, @Nonnull PlayerEntity player) {
        return this.Click(slot, mouseclick, clickType, player);
    }

    @Override
    protected void resetQuickCraft() {
        this.quickcraftSlots.clear();
        this.quickcraftEmptySlots.clear();
    }

    private ItemStack Click(int slot, int mouseclick, ClickType clickType, PlayerEntity player) {
        if (player == null) return ItemStack.EMPTY;
        ItemStack carried = player.inventory.getCarried();
        if (clickType != ClickType.CLONE && player.inventory.selected == slot -108 || ((slot <81 && slot >= 0) && carried.getItem() instanceof IContainer)){
            return ItemStack.EMPTY;
        }
        else if (clickType == ClickType.QUICK_CRAFT) {
            if (mouseclick == 1 || mouseclick == 5){
                if (slot < 0 || slot > 117 || carried.isEmpty()) return ItemStack.EMPTY;
                Slot slot1 = this.getSlot(slot);
                ItemStack itemStack = slot1.getItem();
                int i = slot <81 ? this.maxStackSize : itemStack.getMaxStackSize();
                if (!slot1.hasItem()){
                    this.quickcraftEmptySlots.add(slot1);
                }else if(consideredTheSameItem(itemStack,carried) && itemStack.getCount() < i){
                    this.quickcraftSlots.add(slot1);
                }
            }
            else if (mouseclick == 2 || mouseclick == 6)
                if (!this.quickcraftSlots.isEmpty() || !this.quickcraftEmptySlots.isEmpty()) {
                    int i = this.quickcraftSlots.size() + this.quickcraftEmptySlots.size();
                    if (i < carried.getCount()) {
                        ItemStack is = carried.copy();
                        if (mouseclick == 2) {
                            int j = carried.getCount() / i;
                            carried.shrink(j * i);
                            is.setCount(j);
                            if (!this.quickcraftEmptySlots.isEmpty())
                                this.quickcraftEmptySlots.forEach((t) -> t.set(is.copy()));
                            if (!this.quickcraftSlots.isEmpty()) {
                                Set<Slot> set = Sets.newHashSet(this.quickcraftSlots);
                                set.removeIf((t) -> {
                                    int i1 = t instanceof LoliInventory.LoliSlot ? maxStackSize : t.getItem().getMaxStackSize();
                                    return i1 - t.getItem().getCount() < j;
                                });
                                if (!set.isEmpty()) set.forEach((t) -> t.getItem().grow(j));
                                this.quickcraftSlots.removeAll(set);
                                if (!this.quickcraftSlots.isEmpty()) {
                                    int i1 = this.quickcraftSlots.size() * j;
                                    for (Slot slot1 : this.quickcraftSlots) {
                                        int i2 = slot1 instanceof LoliInventory.LoliSlot ? maxStackSize : slot1.getItem().getMaxStackSize();
                                        i1 -= i2 - slot1.getItem().getCount();
                                        slot1.getItem().setCount(i2);
                                    }
                                    carried.grow(i1);
                                }
                            }
                        } else {
                            carried.shrink(i);
                            is.setCount(1);
                            if (!this.quickcraftEmptySlots.isEmpty())
                                this.quickcraftEmptySlots.forEach((t) -> t.set(is.copy()));
                            if (!this.quickcraftSlots.isEmpty())
                                this.quickcraftSlots.forEach((t) -> t.getItem().grow(1));
                        }
                    }
                    this.INVENTORY.setChanged();
                    this.resetQuickCraft();
                    return carried;
                }
        } else if(!quickcraftSlots.isEmpty()) this.resetQuickCraft();
        if (clickType == ClickType.PICKUP) {
            if (slot == -999){
                if (!carried.isEmpty()) {
                    if (mouseclick == 0) {
                        player.drop(carried, true);
                        player.inventory.setCarried(ItemStack.EMPTY);
                        return carried;
                    }
                    else if (mouseclick == 1) {
                        player.drop(carried.split(1), true);
                        return carried;
                    }
                }
            }
            else {
                if (slot < 0 || slot > 117) return ItemStack.EMPTY;
                Slot slot1 = this.getSlot(slot);
                ItemStack itemStack = slot1.getItem();
                if (mouseclick == 0) {
                    if ((slot < 117 && slot >= 81)) {
                        if (!itemStack.isEmpty() && consideredTheSameItem(itemStack, carried)) {
                            int i = itemStack.getCount() + carried.getCount();
                            int maxStackSize = itemStack.getMaxStackSize();
                            if (i > maxStackSize) {
                                carried.setCount(i - maxStackSize);
                                itemStack.setCount(maxStackSize);
                            } else {
                                itemStack.setCount(i);
                                player.inventory.setCarried(ItemStack.EMPTY);
                            }
                            return itemStack;
                        }
                        player.inventory.setCarried(itemStack);
                        slot1.set(carried);
                        return carried;
                    }
                    if (slot < 81) {
                        if (!itemStack.isEmpty() && consideredTheSameItem(itemStack, carried)) {
                            int i = itemStack.getCount() + carried.getCount();
                            if (i > maxStackSize) {
                                carried.setCount(i - maxStackSize);
                                itemStack.setCount(maxStackSize);
                            } else {
                                itemStack.setCount(i);
                                player.inventory.setCarried(ItemStack.EMPTY);
                            }
                            this.INVENTORY.setChanged();
                            return itemStack;
                        }
                        if (itemStack.getCount() > itemStack.getMaxStackSize()) {
                            if (carried.isEmpty()) {
                                ItemStack i = itemStack.copy();
                                i.setCount(i.getMaxStackSize());
                                player.inventory.setCarried(i);
                                itemStack.setCount(itemStack.getCount() - itemStack.getMaxStackSize());
                                this.INVENTORY.setChanged();
                                return itemStack;
                            }
                            return ItemStack.EMPTY;
                        }
                        player.inventory.setCarried(itemStack);
                        slot1.set(carried);
                        return carried;
                    }
                }
                else if (mouseclick == 1) {
                    if (carried.isEmpty() && !itemStack.isEmpty()) {
                        int i;
                        if (itemStack.getCount() > itemStack.getMaxStackSize()) {
                            i = (itemStack.getMaxStackSize() + 1) / 2;
                        } else {
                            i = (itemStack.getCount() + 1) / 2;
                        }
                        ItemStack j = itemStack.copy();
                        j.setCount(i);
                        player.inventory.setCarried(j);
                        itemStack.setCount(itemStack.getCount() - i);
                        this.INVENTORY.setChanged();
                        return itemStack;
                    } else {
                        if (!carried.isEmpty()) {
                            if (!itemStack.isEmpty()) {
                                if (!consideredTheSameItem(itemStack, carried)) {
                                    if (itemStack.getCount() > itemStack.getMaxStackSize()) {
                                        return ItemStack.EMPTY;
                                    } else {
                                        player.inventory.setCarried(itemStack);
                                        slot1.set(carried);
                                        return carried;
                                    }
                                } else {
                                    if (slot < 81) {
                                        if (itemStack.getCount() < maxStackSize) {
                                            carried.setCount(carried.getCount() - 1);
                                            itemStack.setCount(itemStack.getCount() + 1);
                                            this.INVENTORY.setChanged();
                                            return itemStack;
                                        } else {
                                            return ItemStack.EMPTY;
                                        }
                                    } else if (slot < 117) {
                                        if (itemStack.getCount() < itemStack.getMaxStackSize()) {
                                            carried.setCount(carried.getCount() - 1);
                                            itemStack.setCount(itemStack.getCount() + 1);
                                            return itemStack;
                                        } else {
                                            return ItemStack.EMPTY;
                                        }
                                    }
                                }
                            }
                            if (itemStack.isEmpty()) {
                                itemStack = carried.copy();
                                itemStack.setCount(1);
                                carried.setCount(carried.getCount() - 1);
                                slot1.set(itemStack);
                                return itemStack;
                            }
                        }
                    }
                }
            }
        }
        else if (clickType == ClickType.CLONE) {
            if (slot < 0 || slot > 117) return ItemStack.EMPTY;
            Slot slot1 = this.getSlot(slot);
            ItemStack itemStack = slot1.getItem();
            if (mouseclick == 2 && carried.isEmpty() && !itemStack.isEmpty()) {
                ItemStack i = itemStack.copy();
                i.setCount(i.getMaxStackSize());
                player.inventory.setCarried(i);
                return i;
            }
            else {
                return ItemStack.EMPTY;
            }
        }
        else if (clickType == ClickType.QUICK_MOVE) {
            if (slot < 0 || slot > 117) return ItemStack.EMPTY;
            Slot slot1 = this.getSlot(slot);
            ItemStack itemStack = slot1.getItem();
            if((mouseclick == 0 || mouseclick == 1) && !itemStack.isEmpty()){
                List<Slot> air = Lists.newArrayList();
                List<Slot> same = Lists.newArrayList();
                if (slot <81){
                    for (int i = 108; i < 117 ; ++i){
                        Slot s = this.getSlot(i);
                        if(!s.hasItem()){
                            air.add(s);
                        }else if(consideredTheSameItem(itemStack,s.getItem())){
                            same.add(s);
                        }
                    }
                    for (int i = 81; i < 108 ; ++i){
                        Slot s = this.getSlot(i);
                        if(!s.hasItem()){
                            air.add(s);
                        }else if(consideredTheSameItem(itemStack,s.getItem())){
                            same.add(s);
                        }
                    }
                }else if(slot <108){
                    for (int i = 0; i < 81 ; ++i){
                        Slot s = this.getSlot(i);
                        if(!s.hasItem()){
                            air.add(s);
                        }else if(consideredTheSameItem(itemStack,s.getItem())){
                            same.add(s);
                        }
                    }
                    for (int i = 108; i < 117 ; ++i){
                        Slot s = this.getSlot(i);
                        if(!s.hasItem()){
                            air.add(s);
                        }else if(consideredTheSameItem(itemStack,s.getItem())){
                            same.add(s);
                        }
                    }
                }else if(slot <117){
                    for (int i = 0; i < 108 ; ++i){
                        Slot s = this.getSlot(i);
                        if(!s.hasItem()){
                            air.add(s);
                        }else if(consideredTheSameItem(itemStack,s.getItem())){
                            same.add(s);
                        }
                    }
                }
                int i = Math.min(itemStack.getCount(),itemStack.getMaxStackSize());
                if (!same.isEmpty()){
                    for (Slot j : same){
                        if (i > 0){
                            int k;
                            if (j.container instanceof LoliInventory){
                                k = this.maxStackSize - j.getItem().getCount();
                            }else {
                                k = j.getItem().getMaxStackSize() - j.getItem().getCount();
                            }
                            if (k > 0) {
                                if (k < i) {
                                    i = i - k;
                                    j.getItem().setCount(j.getItem().getMaxStackSize());
                                } else {
                                    j.getItem().setCount(j.getItem().getCount() + i);
                                    i = 0;
                                }
                                this.INVENTORY.setChanged();
                            }
                        }
                    }
                }
                if (!air.isEmpty() && i > 0){
                    Slot j = air.get(0);
                    ItemStack k = itemStack.copy();
                    k.setCount(i);
                    j.set(k);
                    i = 0;
                }
                if (i > 0){
                    itemStack.setCount(itemStack.getCount() -Math.min(itemStack.getCount(),itemStack.getMaxStackSize())+ i);
                    this.INVENTORY.setChanged();
                }else {
                    itemStack.setCount(itemStack.getCount() -Math.min(itemStack.getCount(),itemStack.getMaxStackSize()));
                    if (itemStack.isEmpty()){
                        slot1.set(ItemStack.EMPTY);
                        return ItemStack.EMPTY;
                    }
                    this.INVENTORY.setChanged();
                }
                return itemStack;
            }else {
                return ItemStack.EMPTY;
            }
        }
        else if (clickType == ClickType.THROW) {
            if (slot < 0 || slot > 117) return ItemStack.EMPTY;
            Slot slot1 = this.getSlot(slot);
            ItemStack itemStack = slot1.getItem();
            if (!itemStack.isEmpty()){
                if (mouseclick == 0) {
                    player.drop(itemStack.split(1), true);
                    this.INVENTORY.setChanged();
                    return itemStack;
                }
                if (mouseclick == 1) {
                    int i = Math.min(itemStack.getMaxStackSize(),itemStack.getCount());
                    player.drop(itemStack.split(i), true);
                    if (itemStack.isEmpty()) slot1.set(ItemStack.EMPTY);
                    else this.INVENTORY.setChanged();
                    return itemStack;
                }
            }
        }
        else if (clickType == ClickType.SWAP) {
            Slot slot1 = this.getSlot(slot);
            ItemStack itemStack = player.inventory.getItem(mouseclick);
            if (slot1.hasItem()) {
                ItemStack stack = slot1.getItem();
                if (itemStack.isEmpty()) {
                    ItemStack s = stack.split(stack.getMaxStackSize());
                    if (slot1 instanceof LoliInventory.LoliSlot) {
                        INVENTORY.setChanged();
                    }
                    player.inventory.setItem(mouseclick, s);
                    return s;
                }
                else {
                    if (consideredTheSameItem(itemStack, stack) && itemStack.getCount() < itemStack.getMaxStackSize()) {
                        int i = itemStack.getMaxStackSize() - itemStack.getCount();
                        ItemStack s = stack.split(i);
                        if (slot1 instanceof LoliInventory.LoliSlot) {
                            INVENTORY.setChanged();
                        }
                        itemStack.grow(s.getCount());
                        return s;
                    }
                    else return ItemStack.EMPTY;
                }
            }
            else {
                if (itemStack.isEmpty() || itemStack.getItem() instanceof IContainer) return ItemStack.EMPTY;
                else {
                    slot1.set(itemStack);
                    player.inventory.setItem(mouseclick, ItemStack.EMPTY);
                    return itemStack;
                }
            }
        }
        else if (clickType == ClickType.PICKUP_ALL) return super.clicked(slot,mouseclick,clickType,player);
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull PlayerEntity p_82846_1_, int p_82846_2_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_82846_2_);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            if (itemstack1.getItem() instanceof IContainer){
                return ItemStack.EMPTY;
            }
            itemstack = itemstack1.copy();
            int containerRows = 9;
            if (p_82846_2_ < containerRows * 9) {
                if (!this.moveItemStackTo(itemstack1, containerRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, containerRows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return player.getItemBySlot(EquipmentSlotType.MAINHAND).getItem() instanceof IContainer;
    }

    private void addSlot(LoliInventory inventory){
        int x = 8;int y = 8;int i = this.PAGE * 81; int z = i + 81;
        while (i < z) {
            for (int k = 0; k < 162; k += 18) {
                for (int j = 0; j < 162; j += 18) {
                    this.addSlot(new LoliInventory.LoliSlot(inventory, i++, x + j, y + k));}}}
    }

    private void addPlayerInventorySlot(PlayerInventory playerInventory){
        int x = 8;int y = 174; int i = 9;
        while (i < 36) {
            for (int k = 0; k < 54; k += 18)  {
                for (int j = 0; j < 162; j += 18){
                    this.addSlot(new Slot(playerInventory, i++, x + j, y + k));}}}
        i = 0; y = 232;
        while (i < 9){
            for (int j = 0; j < 162; j += 18) {
                this.addSlot(new Slot(playerInventory, i++, x + j, y));
            }
        }
    }

    public int getPAGE(){
        return this.PAGE + 1;
    }

    public void setPAGE(int i){
        this.PAGE = i - 1;
        this.slots.clear();
        this.addSlot(this.INVENTORY);
        this.addPlayerInventorySlot(PLAYER);
    }

}

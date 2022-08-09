package com.tighug.lolipickaxe.item.Tool;

import com.google.common.collect.Sets;
import com.tighug.lolipickaxe.inventory.LoliInventory;
import com.tighug.lolipickaxe.inventory.LoliPickaxeContainer;
import com.tighug.lolipickaxe.item.LoliItem;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public abstract class ItemLoliPickaxeTool extends LoliItem implements IContainer, INamedContainerProvider {

    public static final String LOLI_DESTROY_RANGE = "loliDestroyRange";

    public static boolean isWhitelist(Entity entity, @NotNull PlayerEntity player) {
        if (player.getLastHurtByMob() == entity || player.getLastHurtMob() == entity || (entity instanceof LivingEntity && ((LivingEntity) entity).getKillCredit() == player)) return false;
        return (entity instanceof PlayerEntity || entity instanceof ArmorStandEntity || entity instanceof AmbientEntity || (entity instanceof CreatureEntity && !(entity instanceof IMob)));
    }

    @Override
    public @NotNull ActionResultType useOn(@NotNull ItemUseContext p_195939_1_) {
        PlayerEntity player = p_195939_1_.getPlayer();
        World world = p_195939_1_.getLevel();
        ItemStack itemStack = p_195939_1_.getItemInHand();
        if (!world.isClientSide() && player != null && player.isDiscrete()){
            TileEntity b = world.getBlockEntity(p_195939_1_.getClickedPos());
            if (b == null) return ActionResultType.PASS;
            LazyOptional<IItemHandler> optional = b.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            if (itemStack.getItem() instanceof IContainer && ((IContainer) itemStack.getItem()).hasInventory(itemStack)) {
                LoliInventory inventory = ((IContainer) itemStack.getItem()).getInventory(itemStack);
                if (b instanceof IInventory){
                    IInventory box = (IInventory) b;
                    if (!box.isEmpty()) {
                        for (int i = 0; i < box.getContainerSize(); ++i) {
                            ItemStack is = box.getItem(i);
                            if (is.isEmpty()) continue;
                            if (inventory.canAddItem(is)) {
                                is = box.removeItem(i, is.getCount());
                                inventory.addItem(is);
                            }
                        }
                    } else if (!inventory.isEmpty()) {
                        boolean b1 = true;
                        for (int i = 0; i < inventory.getContainerSize() && b1 && !inventory.isEmpty(); ++i) {
                            for (int j = 0; j < box.getContainerSize(); ++j) {
                                ItemStack is = inventory.getItem(i);
                                if (is.isEmpty() || !box.getItem(j).isEmpty()) continue;
                                int k = box.getMaxStackSize() == 64 ? is.getMaxStackSize() : box.getMaxStackSize();
                                is = is.copy();
                                is.setCount(k);
                                if (box.canPlaceItem(j, is)) {
                                    is = inventory.removeItem(i, k);
                                    box.setItem(j, is);
                                    if (j == box.getContainerSize() - 1) {
                                        b1 = false;
                                    }
                                }
                            }
                        }
                    }
                }
                else if(optional.isPresent()){
                    optional.ifPresent(iItemHandler -> {
                        boolean b1 = true;
                        for (int i = 0; i < iItemHandler.getSlots() && b1; ++i){
                            if (!iItemHandler.getStackInSlot(i).isEmpty()) b1 = false;
                        }
                        if (b1){
                            for (int i = 0; i < inventory.getContainerSize() && !inventory.isEmpty(); ++i){
                                for (int j = 0; j < iItemHandler.getSlots(); ++j){
                                    ItemStack itemStack1 = inventory.getItem(i);
                                    if (itemStack1.isEmpty()) break;
                                    ItemStack itemStack2 = iItemHandler.getStackInSlot(j);
                                    if (!itemStack2.isEmpty() && !Container.consideredTheSameItem(itemStack1, itemStack2)) continue;
                                    int k = iItemHandler.getSlotLimit(j) == 64 ? itemStack1.getMaxStackSize() : iItemHandler.getSlotLimit(j);
                                    itemStack1 = inventory.removeItem(i, k);
                                    itemStack2 = iItemHandler.insertItem(j, itemStack1, false);
                                    if (!itemStack2.isEmpty()) inventory.addItem(itemStack2);
                                    if (j == iItemHandler.getSlots() - 1) return;
                                }
                            }
                        }
                        else {
                            for (int i = 0; i < iItemHandler.getSlots(); ++i){
                                ItemStack itemStack1 = iItemHandler.getStackInSlot(i);
                                if (itemStack1.isEmpty()) continue;
                                if (inventory.canAddItem(itemStack1)){
                                    itemStack1 = iItemHandler.extractItem(i, itemStack1.getCount(), false);
                                    inventory.addItem(itemStack1);
                                }
                            }
                        }
                    });
                }
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, World world) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public @NotNull Set<ToolType> getToolTypes(@NotNull ItemStack stack) {
        return Sets.newHashSet(ToolType.HOE, ToolType.PICKAXE, ToolType.AXE, ToolType.SHOVEL);
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return true;
    }

    @Override
    public boolean canBeHurtBy(@NotNull DamageSource p_234685_1_) {
        return false;
    }

    @Nullable
    @Override
    public Container createMenu(int id, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity player) {
        return new LoliPickaxeContainer(id, playerInventory);
    }

    @Override
    public LoliInventory getInventory(ItemStack stack) {
        return this.hasInventory(stack) ? new LoliInventory(stack) : LoliInventory.EMPTY;
    }

    public static byte getDestroyRange(@NotNull ItemStack stack) {
        if (stack.getTag() == null) return 0;
        else {
            return stack.getTag().getByte(LOLI_DESTROY_RANGE);
        }
    }

    public abstract void setDestroyRange(ItemStack stack);
}

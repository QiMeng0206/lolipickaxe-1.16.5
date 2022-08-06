package com.tighug.lolipickaxe.event;

import com.google.common.collect.Sets;
import com.tighug.lolipickaxe.Lolipickaxe;
import com.tighug.lolipickaxe.client.event.LoliPickaxeKeyEvent;
import com.tighug.lolipickaxe.item.ModItems;
import com.tighug.lolipickaxe.item.Tool.ItemSmallLoliPickaxe;
import com.tighug.lolipickaxe.item.addon.ItemLoliAddon;
import com.tighug.lolipickaxe.network.NetworkHandler;
import com.tighug.lolipickaxe.player.LoliPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class SmallLoliPickaxeEvent implements Lolipickaxe.LoliEvent {
    final Set<Entity> entities = Sets.newHashSet();
    byte count;

    private ItemStack getItemSmallLoliPickaxe(ItemLoliAddon.Type type, @NotNull PlayerEntity player) {
        if (!player.inventory.contains(ModItems.ITEM_SMALL_LOLI_PICKAXE.get().getDefaultInstance())) return ItemStack.EMPTY;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i1 = 0; i1 < player.inventory.getContainerSize(); ++i1) {
            ItemStack stack = player.inventory.getItem(i1);
            if (ItemSmallLoliPickaxe.hasLevel(type, stack)) {
                if (itemStack.isEmpty()) itemStack = stack;
                else if (ItemSmallLoliPickaxe.getLevel(type, stack) > ItemSmallLoliPickaxe.getLevel(type, itemStack)) itemStack = stack;
            }
        }
        return itemStack;
    }

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent(receiveCanceled = true)
    public void onLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        ItemStack itemStack = event.getItemStack();
        if (ItemSmallLoliPickaxe.hasLevel(ItemLoliAddon.Type.ATTACK_RANGE, itemStack)) {
            LoliPickaxeKeyEvent.playSound();
            NetworkHandler.Pack.SMALL_ATTACK_FACING.sendToServer();
        }
    }

    @SubscribeEvent()
    public void onLivingHurtEvent(LivingHurtEvent event) {
        ServerPlayerEntity player = event.getEntityLiving() instanceof ServerPlayerEntity ? (ServerPlayerEntity) event.getEntityLiving() : null;
        if (player != null && !(event.getSource() instanceof LoliPlayer.LoliDamageSource)) {
            ItemStack itemStack = getItemSmallLoliPickaxe(ItemLoliAddon.Type.DEFENSE, player);
            if (!itemStack.isEmpty() && ItemSmallLoliPickaxe.getValue(ItemLoliAddon.Type.DEFENSE, itemStack) >= 1) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onLivingAttackEvent(LivingAttackEvent event) {
        ServerPlayerEntity player = event.getEntityLiving() instanceof ServerPlayerEntity ? (ServerPlayerEntity) event.getEntityLiving() : null;
        if (player != null && !(event.getSource() instanceof LoliPlayer.LoliDamageSource)) {
            ItemStack itemStack = getItemSmallLoliPickaxe(ItemLoliAddon.Type.THORNS, player);
            Entity entity = event.getSource().getEntity();
            if (!itemStack.isEmpty() && entity instanceof LivingEntity && ItemSmallLoliPickaxe.getValue(ItemLoliAddon.Type.THORNS, itemStack) >= 1) {
                float f = 1;
                if (ItemSmallLoliPickaxe.hasLevel(ItemLoliAddon.Type.ATTACK_DAMAGE, itemStack)) f += ItemSmallLoliPickaxe.getValue(ItemLoliAddon.Type.ATTACK_DAMAGE, itemStack);
                if (!entities.contains(entity)) {
                    entity.hurt(((EntityDamageSource) DamageSource.playerAttack(player)).setThorns(), f);
                    entities.add(entity);
                }
                player.setHealth(player.getHealth() + f / 2);
            }
        }
    }

    @SubscribeEvent
    public void onServerTickEvent(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        if (++count > 10) {
            entities.clear();
            count = 0;
        }
    }

}

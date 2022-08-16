package com.tighug.lolipickaxe.event;

import com.google.common.collect.Sets;
import com.tighug.lolipickaxe.Lolipickaxe;
import com.tighug.lolipickaxe.client.event.LoliPickaxeKeyEvent;
import com.tighug.lolipickaxe.item.Tool.ILoli;
import com.tighug.lolipickaxe.item.Tool.ItemLoliPickaxe;
import com.tighug.lolipickaxe.network.NetworkHandler;
import com.tighug.lolipickaxe.player.ILoliPlayer;
import com.tighug.lolipickaxe.player.LoliPlayer;
import com.tighug.lolipickaxe.player.LoliPlayerCapabilityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;

import static com.tighug.lolipickaxe.util.Utils.MODID;

public class LoliPlayerEvent implements Lolipickaxe.LoliEvent {
    private final Set<ServerPlayerEntity> loliPlayer = Sets.newHashSet();
    public final Set<Class<? extends Entity>> classes = Sets.newHashSet();

    @SubscribeEvent
    public void onAttachCapabilityEvent(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof ServerPlayerEntity){
            event.addCapability(new ResourceLocation(MODID, "loli"), new LoliPlayerCapabilityProvider((ServerPlayerEntity) entity));
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onRightClick(PlayerInteractEvent.RightClickItem event) {
        ItemStack itemStack = event.getItemStack();
        if (!(itemStack.getItem() instanceof ILoli)) return;
        World world = event.getWorld();
        PlayerEntity player = event.getPlayer();
        boolean b = player.isDiscrete();
        if (world.isClientSide()) {
            if (!ItemLoliPickaxe.isRemoved(itemStack))
                if (!b || itemStack.getOrCreateTag().getCompound(ILoli.CONFIG).getBoolean("attackRange"))
                    LoliPickaxeKeyEvent.playSound();
        }
        else {
            if (b) {
                event.getPlayer().getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
                    if (iLoliPlayer.isLoli() && iLoliPlayer instanceof LoliPlayer) {
                        ((LoliPlayer) iLoliPlayer).attackRangeEntity(false);
                    }
                });
            }
        }
    }

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent(receiveCanceled = true)
    public void onLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        ItemStack itemStack = event.getItemStack();
        if (!(itemStack.getItem() instanceof ILoli)) return;
        if (itemStack.getOrCreateTag().getCompound(ILoli.CONFIG).getBoolean("loliPickaxeKillFacing") && !ItemLoliPickaxe.isRemoved(itemStack)) {
            NetworkHandler.Pack.ATTACK_FACING.sendToServer();
            LoliPickaxeKeyEvent.playSound();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onPlayerHurtEvent(LivingHurtEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof ServerPlayerEntity && loliPlayer.contains(entity)) {
            entity.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> iLoliPlayer.onPlayerHurt(event.getSource()));
            event.setCanceled(true);
        }
        else if (event.getSource() instanceof LoliPlayer.LoliDamageSource) {
            entity.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> iLoliPlayer.onPlayerHurt(event.getSource()));
            event.setCanceled(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onLivingDamageEvent(LivingDamageEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof ServerPlayerEntity && loliPlayer.contains(entity)) {
            entity.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> iLoliPlayer.onPlayerHurt(event.getSource()));
            event.setCanceled(true);
        }
        else if (event.getSource() instanceof LoliPlayer.LoliDamageSource) {
            event.setAmount(((LoliPlayer.LoliDamageSource) event.getSource()).getAmount());
            event.setCanceled(false);
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onLivingAttackEvent(LivingAttackEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof ServerPlayerEntity && loliPlayer.contains(entity)) {
            entity.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> iLoliPlayer.onPlayerHurt(event.getSource()));
            event.setCanceled(true);
        }
        else if (event.getSource() instanceof LoliPlayer.LoliDamageSource) event.setCanceled(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onLivingDeathEvent(LivingDeathEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof ServerPlayerEntity && loliPlayer.contains(entity)) {
            entity.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> iLoliPlayer.onPlayerHurt(event.getSource()));
            event.setCanceled(true);
        }
        else if (event.getSource() instanceof LoliPlayer.LoliDamageSource) {
            event.setCanceled(false);
            entity.setHealth(0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onPlayerUpdateEvent(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (LoliPlayer.isLoli(entity)) {
            entity.getCapability(LoliPlayer.loliPlayer).ifPresent(ILoliPlayer::onPlayerUpdate);
            loliPlayer.add((ServerPlayerEntity) entity);
            event.setCanceled(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        for (Class<? extends Entity> clazz : classes) {
            if (clazz.isInstance(entity)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone event) {
        PlayerEntity original = event.getOriginal();
        LazyOptional<ILoliPlayer> oldCap = original.getCapability(LoliPlayer.loliPlayer);
        if (original instanceof ServerPlayerEntity) loliPlayer.remove(original);
        PlayerEntity player = event.getPlayer();
        LazyOptional<ILoliPlayer> newCap = player.getCapability(LoliPlayer.loliPlayer);
        if (oldCap.isPresent() && newCap.isPresent()){
            CompoundNBT nbt = new CompoundNBT();
            oldCap.ifPresent(loliPlayer1 -> nbt.put("clone", loliPlayer1.serializeNBT()));
            newCap.ifPresent(loliPlayer1 -> loliPlayer1.deserializeNBT(nbt.getCompound("clone")));
        }
        if (LoliPlayer.isLoli(player)) {
            LoliTickEvent.addTask(new LoliTickEvent.TickStartTask(5, () -> player.getCapability(LoliPlayer.loliPlayer).ifPresent(ILoliPlayer::recover)), TickEvent.Phase.START);
            loliPlayer.add((ServerPlayerEntity) player);
        }
    }

    @SubscribeEvent
    public void onPlayerOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) this.loliPlayer.remove((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent
    public void onServerTickEvent(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        loliPlayer.removeIf(player -> !LoliPlayer.isLoli(player));
        loliPlayer.forEach(player -> player.getCapability(LoliPlayer.loliPlayer).ifPresent(ILoliPlayer::tick));
    }
}

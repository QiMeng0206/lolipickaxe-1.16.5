package com.tighug.lolipickaxe.client.event;

import com.tighug.lolipickaxe.Lolipickaxe;
import com.tighug.lolipickaxe.client.gui.LoliPickaxeConfigScreen;
import com.tighug.lolipickaxe.client.gui.LoliPickaxeEnchantScreen;
import com.tighug.lolipickaxe.client.gui.LoliPickaxePotionScreen;
import com.tighug.lolipickaxe.client.key.KeyLoader;
import com.tighug.lolipickaxe.item.Tool.*;
import com.tighug.lolipickaxe.item.addon.ItemLoliAddon;
import com.tighug.lolipickaxe.network.NetworkHandler;
import com.tighug.lolipickaxe.util.Config;
import com.tighug.lolipickaxe.util.ModSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@OnlyIn(value = Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class LoliPickaxeKeyEvent implements Lolipickaxe.LoliEvent {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static boolean isAutomaticAttack = false;
    private static byte tick = 0;

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.KeyInputEvent event) {
        if (minecraft.player == null) return;
        PlayerEntity player = minecraft.player;
        ItemStack mainHandItem = player.getMainHandItem();
        if (KeyLoader.RECOVER_KEY.isDown()) {
            if (mainHandItem.getItem() instanceof ILoli && ItemLoliPickaxe.isRemoved(mainHandItem)) return;
            NetworkHandler.Pack.RECOVER.sendToServer();
        }
        else {
            if (KeyLoader.INVENTORY_KEY.isDown()) {
                if (mainHandItem.getItem() instanceof IContainer && ((IContainer) mainHandItem.getItem()).hasInventory(mainHandItem)) {
                    if (player.isDiscrete()) NetworkHandler.Pack.DROP_INVENTORY.sendToServer();
                    else NetworkHandler.Pack.OPEN_GUI.sendToServer();
                }
            }
            else if (mainHandItem.getItem() instanceof ILoli) {
                if (KeyLoader.CONFIG_KEY.isDown()) {
                    NetworkHandler.Pack.LOLI_CONFIG.sendToServer();
                    DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> LoliPickaxeConfigScreen.OpenGui::new);
                }
                if (!ItemLoliPickaxe.isRemoved(mainHandItem)) {
                    if (KeyLoader.ENCHANTMENT_KEY.isDown()) {
                        DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> LoliPickaxeEnchantScreen.OpenGui::new);
                    }
                    else if (KeyLoader.POTION_KEY.isDown()) {
                        DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> LoliPickaxePotionScreen.OpenGui::new);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
        if (minecraft.player == null || event.phase == TickEvent.Phase.END) return;
        if (++LoliGUIEvent.tick >= 3) {
            LoliGUIEvent.tick = 0;
            if (--LoliGUIEvent.curColor < 0) {
                LoliGUIEvent.curColor = LoliGUIEvent.colors.length - 1;
            }
        }
        if (Config.LOLIPICKAXE_AUTOMATIC_ATTACK.get()) {
            if (tick > 0) {
                --tick;
                isAutomaticAttack = false;
            }
            else if (minecraft.options.keyAttack.isDown() && Objects.requireNonNull(minecraft.hitResult).getType() != RayTraceResult.Type.BLOCK) {
                PlayerEntity player = minecraft.player;
                ItemStack mainHandItem = player.getMainHandItem();
                if (mainHandItem.getItem() instanceof ItemLoliPickaxeTool) {
                    tick = (byte)(9 - (ItemSmallLoliPickaxe.getLevel(ItemLoliAddon.Type.ATTACK_DAMAGE, mainHandItem) + 1));
                    if (!minecraft.options.keyAttack.consumeClick()) isAutomaticAttack = true;
                    KeyBinding.click(minecraft.options.keyAttack.getKey());
                }
            }
        }
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void playSound() {
        if (!isAutomaticAttack) {
            assert minecraft.player != null;
            minecraft.player.playSound(ModSoundEvents.lolisuccess.get(), 0.5f, 1);
        }
    }
}

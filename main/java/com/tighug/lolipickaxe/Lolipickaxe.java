package com.tighug.lolipickaxe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tighug.lolipickaxe.client.LoliCardUtil;
import com.tighug.lolipickaxe.client.event.LoliGUIEvent;
import com.tighug.lolipickaxe.client.gui.LoliPickaxeCContainerScreen;
import com.tighug.lolipickaxe.client.key.KeyLoader;
import com.tighug.lolipickaxe.common.loot.ModLootModifierSerializers;
import com.tighug.lolipickaxe.enchantment.ModEnchantments;
import com.tighug.lolipickaxe.event.LoliFlightEvent;
import com.tighug.lolipickaxe.event.LoliPlayerEvent;
import com.tighug.lolipickaxe.event.SmallLoliPickaxeEvent;
import com.tighug.lolipickaxe.inventory.ModContainerType;
import com.tighug.lolipickaxe.item.ModItems;
import com.tighug.lolipickaxe.item.addon.ItemEntitySoul;
import com.tighug.lolipickaxe.item.addon.ItemLoliAddon;
import com.tighug.lolipickaxe.network.NetworkHandler;
import com.tighug.lolipickaxe.player.LoliPlayer;
import com.tighug.lolipickaxe.recipe.ModRecipeSerializers;
import com.tighug.lolipickaxe.util.Config;
import com.tighug.lolipickaxe.util.ModSoundEvents;
import com.tighug.lolipickaxe.util.Utils;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.Map;

import static com.tighug.lolipickaxe.inventory.ModContainerType.lolipickaxeContainerType;
import static com.tighug.lolipickaxe.util.Utils.MODID;

@Mod(MODID)
public class Lolipickaxe {
    public static final LoliPlayerEvent LOLI_PLAYER_EVENT = new LoliPlayerEvent();
    public static final LoliFlightEvent LOLI_FLIGHT_EVENT = new LoliFlightEvent();
    public static final SmallLoliPickaxeEvent SMALL_LOLI_PICKAXE_EVENT = new SmallLoliPickaxeEvent();

    public Lolipickaxe() {
        MinecraftForge.EVENT_BUS.register(LOLI_PLAYER_EVENT);
        MinecraftForge.EVENT_BUS.register(LOLI_FLIGHT_EVENT);
        MinecraftForge.EVENT_BUS.register(SMALL_LOLI_PICKAXE_EVENT);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
        ModSoundEvents.SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModContainerType.CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModEnchantments.ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLootModifierSerializers.LOOT_MODIFIER_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class onModEventBus {

        @SubscribeEvent
        public static void onSetupEvent(FMLCommonSetupEvent event) {
            event.enqueueWork(LoliPlayer::registerCapability);
        }

        @OnlyIn(value = Dist.CLIENT)
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ClientRegistry.registerKeyBinding(KeyLoader.CONFIG_KEY);
                ClientRegistry.registerKeyBinding(KeyLoader.ENCHANTMENT_KEY);
                ClientRegistry.registerKeyBinding(KeyLoader.INVENTORY_KEY);
                ClientRegistry.registerKeyBinding(KeyLoader.POTION_KEY);
                ClientRegistry.registerKeyBinding(KeyLoader.RECOVER_KEY);
                ScreenManager.register(lolipickaxeContainerType.get(), LoliPickaxeCContainerScreen::new);
                for (Enchantment enchantment : Lists.newArrayList(ForgeRegistries.ENCHANTMENTS.getValues())){
                    LoliGUIEvent.enchantment_id.add(enchantment.getDescriptionId());
                }
                LoliCardUtil.init();
            });
        }

        @SubscribeEvent
        public static void onfMLCommonSetupEvent(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                NetworkHandler.registerMessage();
                Utils.EFFECTS.addAll(ForgeRegistries.POTIONS.getValues());
                if (Config.REVISE_ATTACK_DAMAGE_MAX_VALUE.get()) {
                    Class<RangedAttribute> attackDamage = RangedAttribute.class;
                    Field[] attackDamageField = attackDamage.getDeclaredFields();
                    Map<Double, Field> map = Maps.newHashMap();
                    for (Field field : attackDamageField) {
                        if (field.getType() == double.class) {
                            try {
                                field.setAccessible(true);
                                map.put(field.getDouble(Attributes.ATTACK_DAMAGE), field);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    if (!map.isEmpty()) {
                        double d = 0;
                        for (double d1 : map.keySet()) {
                            if (d1 > d) d = d1;
                        }
                        try {
                            map.get(d).set(Attributes.ATTACK_DAMAGE, Float.MAX_VALUE);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } finally {
                            map.values().forEach(field -> field.setAccessible(false));
                        }
                    }
                }
            });
        }

        @SubscribeEvent
        public static void registerItem(RegistryEvent.Register<Item> event) {
            ItemEntitySoul.init();
            for (ItemEntitySoul soul : ItemEntitySoul.getItemEntitySouls()) {
                event.getRegistry().register(soul);
            }
            ItemLoliAddon.init();
            for (ItemLoliAddon addon : ItemLoliAddon.getItemLoliAddons()) {
                event.getRegistry().register(addon);
            }
        }
    }

    public interface LoliEvent {

        static boolean isLoliEvent(Object o) {
            if (o == null) return false;
            if (o.getClass() == Class.class) {
                for (Class<?> c : o.getClass().getInterfaces()) {
                    if (c == LoliEvent.class) return true;
                }
            }
            else {
                return o instanceof LoliEvent;
            }
            return false;
        }

    }
}

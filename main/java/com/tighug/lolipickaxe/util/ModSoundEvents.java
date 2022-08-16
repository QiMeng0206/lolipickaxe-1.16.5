package com.tighug.lolipickaxe.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.tighug.lolipickaxe.util.Utils.MODID;

public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final RegistryObject<SoundEvent> lolisuccess = SOUND_EVENTS.register("lolisuccess", () -> new SoundEvent(new ResourceLocation(MODID, "lolisuccess")));
    public static final RegistryObject<SoundEvent> lolirecord = SOUND_EVENTS.register("lolirecord", () -> new SoundEvent(new ResourceLocation(MODID, "lolirecord")));
    public static final RegistryObject<SoundEvent> testify = SOUND_EVENTS.register("testify", () -> new SoundEvent(new ResourceLocation(MODID, "testify")));
}

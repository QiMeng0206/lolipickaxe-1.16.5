package com.tighug.lolipickaxe.common.loot;

import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.tighug.lolipickaxe.util.Utils.MODID;

public class ModLootModifierSerializers {
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, MODID);
    public static final RegistryObject<AutoFurnace.ModifierSerializer> AUTO_FURNACE = LOOT_MODIFIER_SERIALIZERS.register("auto_furnace", AutoFurnace.ModifierSerializer::new);
    public static final RegistryObject<EntityLootModifier.ModifierSerializer> ENTITY_LOOT_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("entity_soul", EntityLootModifier.ModifierSerializer::new);
}

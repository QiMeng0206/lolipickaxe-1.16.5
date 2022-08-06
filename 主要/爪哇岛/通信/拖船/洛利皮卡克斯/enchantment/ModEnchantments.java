package com.tighug.lolipickaxe.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.tighug.lolipickaxe.util.Utils.MODID;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MODID);
    public static final RegistryObject<autoFurnace> AUTO_FURNACE = ENCHANTMENTS.register("auto_furnace", autoFurnace::new);

    public static class autoFurnace extends Enchantment {
        private autoFurnace() {
            super(Rarity.VERY_RARE, EnchantmentType.create("loli", item -> false), new EquipmentSlotType[]{EquipmentSlotType.MAINHAND});
        }
    }
}

package com.tighug.lolipickaxe.recipe;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.tighug.lolipickaxe.util.Utils.MODID;

public class ModRecipeSerializers {
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final RegistryObject<SpecialRecipeSerializer<LoliRecipe>> SERIALIZER_REGISTRY_OBJECT = RECIPE_SERIALIZERS.register("loli_recipe_serializer", () -> new SpecialRecipeSerializer<>(LoliRecipe::new));
}

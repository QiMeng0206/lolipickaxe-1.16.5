package com.tighug.lolipickaxe.client.key;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

@OnlyIn(value = Dist.CLIENT)
public class KeyLoader {
    private static final String category = "key.category.lolipickaxe";
    public static final KeyBinding INVENTORY_KEY = new KeyBinding("key.lolipickaxe.loli_inventory",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            category);
    public static final KeyBinding CONFIG_KEY = new KeyBinding("key.lolipickaxe.loli_config",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            category);
    public static final KeyBinding POTION_KEY = new KeyBinding("key.lolipickaxe.loli_potion",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            category);
    public static final KeyBinding ENCHANTMENT_KEY = new KeyBinding("key.lolipickaxe.loli_enchantment",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            category);
    public static final KeyBinding RECOVER_KEY = new KeyBinding("key.lolipickaxe.loli_recover",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            category);
}

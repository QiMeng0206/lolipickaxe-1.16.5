package com.tighug.lolipickaxe.item.addon;

import com.google.common.collect.Lists;
import com.tighug.lolipickaxe.item.Tool.ItemLoliPickaxeTool;
import com.tighug.lolipickaxe.item.Tool.ItemSmallLoliPickaxe;
import com.tighug.lolipickaxe.item.ModItemGroup;
import com.tighug.lolipickaxe.util.Utils;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class ItemLoliAddon extends Addon {
    private final static String[] name = {"loli_nether_star_addon", "loli_obsidian_addon", "loli_coal_addon", "loli_gold_addon", "loli_quartz_addon", "loli_redstone_addon", "loli_lapis_addon", "loli_glow_addon", "loli_diamond_addon", "loli_emerald_addon", "loli_iron_addon", "loli_auto_furnace_addon", "loli_fly_addon"};
    private final static List<ItemLoliAddon> ITEM_LOLI_ADDONS = Lists.newArrayList();
    private final Type type;

    private ItemLoliAddon(@NotNull Type type, byte b) {
        super(new Properties(), type.max, b);
        this.type = type;
        this.setRegistryName("lolipickaxe", name[type.ordinal()] + "_" + b);
    }

    public Type getType() {
        return type;
    }

    @Override
    public void fillItemCategory(@NotNull ItemGroup p_150895_1_, @NotNull NonNullList<ItemStack> p_150895_2_) {
        if (p_150895_1_ == ModItemGroup.ITEM_GROUP_LOLI_ADDON){
            p_150895_2_.add(this.getDefaultInstance());
        }
    }

    @Override
    public @NotNull ITextComponent getName(@NotNull ItemStack p_200295_1_) {
        TranslationTextComponent component = new TranslationTextComponent("item." + name[type.ordinal()] + ".name");
        if (getMaxLevel() != 0){
            if (getLevel() != getMaxLevel()) {
                component.append(" ").append(Utils.getTextComponent(getLevel() + 1));
            }
            else {
                component.append(new TranslationTextComponent("item.loliMaterial.end"));
            }
        }
        return component;
    }

    public static void init() {
        if (ITEM_LOLI_ADDONS.isEmpty()) {
            for (Type type1 : Type.values()) {
                for (byte b = 0; b <= type1.getMax(); ++b) {
                    ITEM_LOLI_ADDONS.add(new ItemLoliAddon(type1, b));
                }
            }
        }
    }

    public static @NotNull List<ItemLoliAddon> getItemLoliAddons() {
        return Lists.newArrayList(ITEM_LOLI_ADDONS);
    }

    public static @Nullable Type getType(@NotNull ItemStack itemStack){
        if (itemStack.getItem() instanceof ItemLoliAddon) {
           return ((ItemLoliAddon) itemStack.getItem()).getType();
        }
        return null;
    }

    public static @NotNull ItemStack getItemLoliAddon(Type type, byte level) {
        List<ItemLoliAddon> list = getItemLoliAddons();
        list.removeIf(itemLoliAddon -> !(itemLoliAddon.getType() == type && itemLoliAddon.getLevel() == level));
        if (!list.isEmpty()) {
            return list.get(0).getDefaultInstance();
        }
        else return ItemStack.EMPTY;
    }

    public static @NotNull ItemStack upgrade(ItemStack itemStack) {
        Type type1 = getType(itemStack);
        if (type1 != null) {
            return getItemLoliAddon(type1, (byte) (getLevel(itemStack) + 1));
        }
        return ItemStack.EMPTY;
    }

    public enum Type {
        STORAGE_CAPACITY(i -> {
            int i1 = 2;
            for (int j = 0 ; j < i; ++j){
                i1 = i1 * 2;
            }
            return Double.valueOf(i1);
        }, 4, "storageCapacity"),
        THORNS(i -> (Math.random() + ((double) i + 1) / 10), 9, "thorns"),
        DEFENSE(i -> (Math.random() + ((double) i + 1) / 10), 9, "defense"),
        ATTACK_DAMAGE(i -> {
            float i2 = 1;
            int i3 = 1;
            for (int j = 0; j < i; ++j){
                i3 = i3 * 2;
            }
            for (int j = 0; j < i3; ++j){
                i2 = i2 * 2;
            }
            return Double.valueOf(Math.min(4 + i2, Float.MAX_VALUE));
        }, 6, "attackDamage"),
        ATTACK_RANGE(i -> Double.valueOf(3 + i * 5), 2, "attackRange"),
        ATTACK_SPEED(i -> {
            int i1 = 2;
            for (int j = 0; j < i; ++j){
                i1 = i1 * 2;
            }
            return Double.valueOf(i1);
        }, 3, "attackSpeed"),
        FORTUNE(i -> {
            int i1 = 1;
            for (int j = 0; j < i; ++j){
                i1 = i1 * 2;
            }
            return Double.valueOf(i1);
        }, 5, "fortune"),
        EFFECT(i -> Double.valueOf(i), 2, "effect"),
        HARVEST_LEVEL(i -> {
            switch(i){
                case 0 : return 1d;
                case 1 : return 3d;
                case 2 : return 7d;
                case 3 : return 13d;
                case 4 : return 21d;
                case 5 : return 32d;
            }
            return Double.valueOf(-1);
        }, 5, "harvestLevel"),
        DESTROY_RANGE(i -> Double.valueOf(i + 1), 4, ItemLoliPickaxeTool.LOLI_DESTROY_RANGE),
        DESTROY_SPEED(i -> {
            int i1 = 2;
            for (int i2 = 0; i2 < i + 1; ++i2){
                i1 = i1 * 2;
            }
            return Double.valueOf(i1);
        }, 9, "destroySpeed"),
        AUTO_FURNACE(i -> Double.valueOf(i), 0, "autoFurnace"),
        MAYFLY(i -> Double.valueOf(i), 0, "mayfly");

        final Function<Byte, Double> value;
        final byte max;
        final String name;

        Type(Function<Byte, Double> i, int i1, String s) {
            this.max = (byte) i1;
            this.value = i;
            this.name = s;
        }

        public double getValue(ItemStack itemStack){
            if (!this.hasLevel(itemStack)) return -1;
            return this.value.apply(this.getLevel(itemStack));
        }

        public byte getLevel(ItemStack itemStack){
            CompoundNBT nbt = itemStack.getOrCreateTag().getCompound(ItemSmallLoliPickaxe.SMALL_LOLI_TAG);
            return nbt.getByte(this.name);
        }

        public String getName() {
            return name;
        }

        public boolean hasLevel(ItemStack itemStack){
            return itemStack.getTag() != null
                    && itemStack.getTag().getCompound(ItemSmallLoliPickaxe.SMALL_LOLI_TAG).contains(this.name, 3)
                    && itemStack.getTag().getCompound(ItemSmallLoliPickaxe.SMALL_LOLI_TAG).getInt(this.name) <= this.max
                    && itemStack.getTag().getCompound(ItemSmallLoliPickaxe.SMALL_LOLI_TAG).getInt(this.name) >= 0;
        }

        public byte getMax(){
            return this.max;
        }
    }

}


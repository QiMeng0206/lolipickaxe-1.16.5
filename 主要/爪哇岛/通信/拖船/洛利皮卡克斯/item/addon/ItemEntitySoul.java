package com.tighug.lolipickaxe.item.addon;

import com.google.common.collect.Lists;
import com.tighug.lolipickaxe.item.ModItemGroup;
import com.tighug.lolipickaxe.util.Utils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.tighug.lolipickaxe.util.Utils.MODID;

public class ItemEntitySoul extends Addon {
    private static final List<ItemEntitySoul> itemEntitySouls = Lists.newArrayList();

    private ItemEntitySoul(byte level) {
        super(new Properties(), (byte) 8, level);
    }

    @Override
    public @NotNull ITextComponent getName(@NotNull ItemStack p_200295_1_) {
        TranslationTextComponent component = new TranslationTextComponent("item.loliEntitySoulAddon.name");
        component.append(" ").append(Utils.getTextComponent(getLevel() + 1));
        return component;
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, World world) {
        return 1000;
    }

    public static void init() {
        if (itemEntitySouls.isEmpty()) {
            for (byte b = 0; b < 9; ++b) {
                itemEntitySouls.add((ItemEntitySoul) new ItemEntitySoul(b).setRegistryName(MODID, "loli_entity_soul_addon_" + b));
            }
            itemEntitySouls.add(new SoulEnd());
        }
    }

    public static @NotNull List<ItemEntitySoul> getItemEntitySouls() {
        return Lists.newArrayList(itemEntitySouls);
    }

    public static @NotNull ItemEntitySoul getItemEntitySoul(int i) {
        return itemEntitySouls.get(Utils.clamp(i, 0, 9));
    }

    public static @NotNull ItemStack upgrade(@NotNull ItemStack itemStack) {
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof ItemEntitySoul) || itemStack.getItem() instanceof SoulEnd) return ItemStack.EMPTY;
        return getItemEntitySoul(getLevel(itemStack) + 1).getDefaultInstance();
    }

    @Override
    public void fillItemCategory(@NotNull ItemGroup p_150895_1_, @NotNull NonNullList<ItemStack> p_150895_2_) {
        if (p_150895_1_ == ModItemGroup.ITEM_GROUP_LOLI_ADDON){
            p_150895_2_.add(this.getDefaultInstance());
        }
    }

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack p_77624_1_, @Nullable World p_77624_2_, @NotNull List<ITextComponent> p_77624_3_, @NotNull ITooltipFlag p_77624_4_) {
        p_77624_3_.add(new TranslationTextComponent("item.loliMaterial.recipe"));
    }

    public static class SoulEnd extends ItemEntitySoul{

        private SoulEnd() {
            super((byte) 0);
            this.setRegistryName(MODID, "loli_entity_soul_addon_end");
        }

        @Override
        public @NotNull ITextComponent getName(@NotNull ItemStack p_200295_1_) {
            return new TranslationTextComponent("item.loliEntitySoulAddon.end.name");
        }

        @Override
        public byte getLevel() {
            return (byte) 9;
        }

        @Override
        public void appendHoverText(@NotNull ItemStack p_77624_1_, @Nullable World p_77624_2_, @NotNull List<ITextComponent> p_77624_3_, @NotNull ITooltipFlag p_77624_4_) {
            p_77624_3_.add(new TranslationTextComponent("item.loliEntitySoulAddon.end.recipe"));
        }
    }

}

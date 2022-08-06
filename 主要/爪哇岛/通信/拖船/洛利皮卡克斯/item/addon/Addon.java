package com.tighug.lolipickaxe.item.addon;

import com.tighug.lolipickaxe.util.Utils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class Addon extends Item implements IUpgradeable {
    protected final byte maxLevel;
    protected final byte level;

    public Addon(Properties properties, byte maxLevel, byte level) {
        super(properties);
        this.maxLevel = maxLevel;
        this.level = (byte) Utils.clamp(level, 0, this.maxLevel);
    }

    @Override
    public byte getMaxLevel() {
        return maxLevel;
    }

    @Override
    public byte getLevel() {
        return level;
    }

    public static byte getLevel(@NotNull ItemStack itemStack) {
        if (itemStack.getItem() instanceof IUpgradeable) {
            return ((IUpgradeable) itemStack.getItem()).getLevel();
        }
        else return -1;
    }

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack p_77624_1_, @Nullable World p_77624_2_, @NotNull List<ITextComponent> p_77624_3_, @NotNull ITooltipFlag p_77624_4_) {
        if (getLevel() < this.getMaxLevel()){
            p_77624_3_.add(new TranslationTextComponent("item.loliMaterial.recipe"));
        }
    }

}

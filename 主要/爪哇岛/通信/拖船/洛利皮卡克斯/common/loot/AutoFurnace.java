package com.tighug.lolipickaxe.common.loot;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class AutoFurnace extends LootModifier{
    private final Map<Item, ItemStack> FURNACE = Maps.newHashMap();
    private final ILootCondition[] conditionsIn;

    protected AutoFurnace(ILootCondition[] conditionsIn) {
        super(conditionsIn);
        this.conditionsIn = conditionsIn;
    }

    public int calculateNewCount(int i, int fortune) {
        Random rand = new Random();
        int i2 = 0;
        for (int i3 = 0; i3 < i; ++i3){
            int i1 = rand.nextInt(fortune + 2) + 1;
            if (i1 == fortune + 2) i2 += 1;
            else i2 += i1;
        }
        return i2;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        ItemStack loli = context.getParamOrNull(LootParameters.TOOL);
        assert loli != null;
        List<ItemStack> list = Lists.newArrayList(generatedLoot);
        int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, loli);
        for (int i = 0; i <list.size(); ++i) {
            ItemStack itemStack = list.get(i);
            if (!FURNACE.containsKey(itemStack.getItem())) {
                Optional<FurnaceRecipe> optional = context.getLevel().getRecipeManager().getRecipeFor(IRecipeType.SMELTING, new Inventory(itemStack), context.getLevel());
                if (optional.isPresent()) {
                    FURNACE.put(itemStack.getItem(), optional.get().getResultItem());
                }
                else FURNACE.put(itemStack.getItem(), ItemStack.EMPTY);

            }
            ItemStack itemstack1 = FURNACE.get(itemStack.getItem()).copy();
            if (!itemstack1.isEmpty()) {
                itemstack1.setCount(itemstack1.getCount() * itemStack.getCount());
                if (fortune > 0 && !(itemstack1.getItem() instanceof BlockItem) && itemStack.getItem() instanceof BlockItem){
                    itemstack1.setCount(this.calculateNewCount(itemstack1.getCount(), fortune));
                }
                list.set(i, itemstack1);
            }
        }
        return list;
    }

    public static class ModifierSerializer extends GlobalLootModifierSerializer<AutoFurnace> {

        @Override
        public AutoFurnace read(ResourceLocation location, JsonObject object, ILootCondition[] ailootcondition) {
            return new AutoFurnace(ailootcondition);
        }

        @Override
        public JsonObject write(AutoFurnace instance) {
            return this.makeConditions(instance.conditionsIn);
        }
    }
}



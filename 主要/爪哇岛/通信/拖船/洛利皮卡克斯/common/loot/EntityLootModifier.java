package com.tighug.lolipickaxe.common.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.tighug.lolipickaxe.item.ModItems;
import com.tighug.lolipickaxe.item.addon.ItemEntitySoul;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EntityLootModifier extends net.minecraftforge.common.loot.LootModifier {
    private final ILootCondition[] conditionsIn;

    protected EntityLootModifier(ILootCondition[] conditionsIn) {
        super(conditionsIn);
        this.conditionsIn = conditionsIn;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        List<ItemStack> list = Lists.newArrayList(generatedLoot);
        LivingEntity entity = context.getParamOrNull(LootParameters.THIS_ENTITY) instanceof LivingEntity ? (LivingEntity) context.getParamOrNull(LootParameters.THIS_ENTITY) : null;
        PlayerEntity player = context.getParamOrNull(LootParameters.LAST_DAMAGE_PLAYER);
        if (entity != null && !(entity instanceof PlayerEntity)){
            double d = 0.001 + entity.getMaxHealth() / 10000;
            double i = 0;
            if (player != null) i =  EnchantmentHelper.getEnchantmentLevel(Enchantments.MOB_LOOTING, player);
            d = d * (1 + i / 10);
            if (Math.random() <= d) list.add(ItemEntitySoul.getItemEntitySoul(0).getDefaultInstance());
            if (Math.random() <= 0.001) {
                list.add(ModItems.LOLI_CARD.get().getDefaultInstance());
            }
        }
        return list;
    }

    public static class ModifierSerializer extends GlobalLootModifierSerializer<EntityLootModifier> {

        @Override
        public EntityLootModifier read(ResourceLocation location, JsonObject object, ILootCondition[] ailootcondition) {
            return new EntityLootModifier(ailootcondition);
        }

        @Override
        public JsonObject write(EntityLootModifier instance) {
            return this.makeConditions(instance.conditionsIn);
        }
    }
}



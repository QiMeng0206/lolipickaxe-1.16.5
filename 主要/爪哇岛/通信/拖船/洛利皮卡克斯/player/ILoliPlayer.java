package com.tighug.lolipickaxe.player;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.function.BiConsumer;

public interface ILoliPlayer extends INBTSerializable<CompoundNBT> {

    boolean isOwner(ItemStack stack);

    void tick();

    default boolean isRemoved() {return false;}

    default boolean isLoli(){
        return false;
    }

    void onPlayerHurt(DamageSource damageSource);

    void onPlayerUpdate();

    void recover();

    void attack(Entity entity);

    void attackEntities(Collection<Entity> entities);

    void setLoliConfig(CompoundNBT nbt);

    void openLoliConfig();

    interface IAttackType <T extends ILoliPlayer> extends BiConsumer<Entity, T>{

        void accept(Entity entity, T loliPlayer);
    }
}

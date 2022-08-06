package com.tighug.lolipickaxe.player;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoliPlayerCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {
    private ILoliPlayer loliPlayer;
    private final ServerPlayerEntity player;

    public LoliPlayerCapabilityProvider(ServerPlayerEntity player) {
        this.player = player;
    }


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == LoliPlayer.loliPlayer ? LazyOptional.of(this::getOrCreateCapability).cast() : LazyOptional.empty();
    }

    @Nonnull
    ILoliPlayer getOrCreateCapability(){
        if (loliPlayer == null){
            loliPlayer = new LoliPlayer(player);
        }
        return loliPlayer;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return getOrCreateCapability().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (loliPlayer == null && nbt != null && nbt.contains("uuid", 11)) loliPlayer = new LoliPlayer(player, nbt);
        else getOrCreateCapability().deserializeNBT(nbt);
    }
}

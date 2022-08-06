package com.tighug.lolipickaxe.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.tighug.lolipickaxe.util.Utils.MODID;

public class ModContainerType {
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
    public static final RegistryObject<ContainerType<LoliPickaxeContainer>> lolipickaxeContainerType = CONTAINERS.register("lolipickaxe_container", () -> IForgeContainerType.create((int windowId, PlayerInventory inv, PacketBuffer data) -> new LoliPickaxeContainer(windowId,inv)));
}

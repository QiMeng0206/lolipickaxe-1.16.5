package com.tighug.lolipickaxe.network;

import com.google.common.collect.Sets;
import com.tighug.lolipickaxe.client.gui.LoliPickaxeConfigScreen;
import com.tighug.lolipickaxe.inventory.LoliInventory;
import com.tighug.lolipickaxe.inventory.LoliPickaxeContainer;
import com.tighug.lolipickaxe.item.lolicard.LoliCard;
import com.tighug.lolipickaxe.item.Tool.IContainer;
import com.tighug.lolipickaxe.item.Tool.ILoli;
import com.tighug.lolipickaxe.item.ModItems;
import com.tighug.lolipickaxe.item.Tool.ItemLoliPickaxeTool;
import com.tighug.lolipickaxe.item.Tool.ItemSmallLoliPickaxe;
import com.tighug.lolipickaxe.item.addon.ItemLoliAddon;
import com.tighug.lolipickaxe.player.ILoliPlayer;
import com.tighug.lolipickaxe.player.LoliPlayer;
import com.tighug.lolipickaxe.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.tighug.lolipickaxe.util.Utils.MODID;

public class NetworkHandler {
    private static int ID = 0;
    public static final String VERSION = "1.0";
    public static SimpleChannel INSTANCE;

    public static int nextID() {
        return ID++;
    }

    public static void registerMessage() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MODID, "loli_networking"),
                () -> VERSION,
                (version) -> version.equals(VERSION),
                (version) -> version.equals(VERSION)
        );
        INSTANCE.messageBuilder(Pack.class, nextID())
                .encoder(Pack::toBytes)
                .decoder(Pack::getActionPack)
                .consumer(Pack::handler)
                .add();
    }

    public enum Pack {
        EMPTY((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {}),
        FLIGHT((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            boolean b = nbt.getBoolean("mayfly");
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null){
                    if (!b) player.abilities.flying = false;
                    player.abilities.mayfly = b;
                }
            });
        }),
        RECOVER((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null){
                    player.getCapability(LoliPlayer.loliPlayer).ifPresent(ILoliPlayer::recover);
                }
            });
        }),
        LOLI_CONFIG((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null){
                    player.getCapability(LoliPlayer.loliPlayer).ifPresent(ILoliPlayer::openLoliConfig);
                }
                else {
                    UUID uuid = nbt.getUUID("uuid");
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LoliPickaxeConfigScreen.OpenGui.openGui(uuid));
                }
            });
        }),
        SET_CONFIG((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null){
                    player.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> iLoliPlayer.setLoliConfig(nbt));
                }
            });
        }),
        OPEN_GUI((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null) {
                    Item item = player.getMainHandItem().getItem();
                    if (item instanceof IContainer && item instanceof INamedContainerProvider) NetworkHooks.openGui(player, (INamedContainerProvider) item);
                }
            });
        }),
        ATTACK_FACING((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null){
                    player.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
                        if (iLoliPlayer instanceof LoliPlayer) {
                            ((LoliPlayer) iLoliPlayer).attackFacing();
                        }
                    });
                }
            });
        }),
        PAGE((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                int i = nbt.getInt("int");
                if (player != null && i != 0 && player.containerMenu instanceof LoliPickaxeContainer) ((LoliPickaxeContainer) player.containerMenu).setPAGE(i);
            });
        }),
        REMOVED_CLIENT_PLAYER((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player == null) {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                        ClientPlayerEntity player1 = Minecraft.getInstance().player;
                        if (player1 != null && !player1.inventory.contains(ModItems.ITEM_LOLI_PICKAXE.get().getDefaultInstance())) {
                            ClientWorld world = player1.clientLevel;
                            world.removeEntity(player1.getId());
                        }
                    });
                }
            });
        }),
        ENCHANT((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null){
                    ItemStack itemStack = player.getMainHandItem();
                    if (itemStack.getItem() instanceof ILoli && LoliPlayer.isLoli(player)) {
                        player.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
                            if (iLoliPlayer instanceof LoliPlayer) {
                                ((LoliPlayer) iLoliPlayer).enchant(nbt);
                            }
                        });
                        itemStack.getOrCreateTag().put("Enchantments", nbt.getList("Enchantments", 10));
                    }
                }
            });
        }),
        EFFECT((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null){
                    ItemStack itemStack = player.getMainHandItem();
                    if (itemStack.getItem() instanceof ILoli && LoliPlayer.isLoli(player)) {
                        itemStack.getOrCreateTag().remove(Utils.NBT_EFFECTS);
                        itemStack.getOrCreateTag().put(Utils.NBT_EFFECTS, nbt.getList(Utils.NBT_EFFECTS, 10));
                        player.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
                            if (iLoliPlayer instanceof LoliPlayer) {
                                ((LoliPlayer) iLoliPlayer).setEffect(itemStack);
                            }
                        });
                    }
                }
            });
        }),
        EFFECT_BLACKLIST((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null){
                    ItemStack itemStack = player.getMainHandItem();
                    if (itemStack.getItem() instanceof ILoli && LoliPlayer.isLoli(player)) {
                        itemStack.getOrCreateTag().remove(Utils.NBT_EFFECT_BLACKLIST);
                        itemStack.getOrCreateTag().put(Utils.NBT_EFFECT_BLACKLIST, nbt.getList(Utils.NBT_EFFECT_BLACKLIST, 3));
                        player.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
                            if (iLoliPlayer instanceof LoliPlayer) {
                                ((LoliPlayer) iLoliPlayer).setEffect(itemStack);
                            }
                        });
                    }
                }
            });
        }),
        DROP_INVENTORY((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null) {
                    ItemStack mainHandItem = player.getMainHandItem();
                    if (mainHandItem.getItem() instanceof IContainer) {
                        AtomicReference<LoliInventory> inventory = new AtomicReference<>();
                        if (mainHandItem.getItem() instanceof ILoli) {
                            player.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
                                if (iLoliPlayer instanceof LoliPlayer) {
                                    inventory.set(((LoliPlayer) iLoliPlayer).getLoliInventory());
                                }
                            });
                        } else inventory.set(((IContainer) mainHandItem.getItem()).getInventory(mainHandItem));
                        List<ItemStack> list = inventory.get().removeAllItems();
                        if (!list.isEmpty()) list.forEach(itemStack -> player.drop(itemStack, false));
                    }
                }
            });
        }),
        SMALL_ATTACK_FACING((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null) {
                    ItemStack mainHandItem = player.getMainHandItem();
                    double slope = ItemSmallLoliPickaxe.getLevel(ItemLoliAddon.Type.ATTACK_RANGE, mainHandItem) / 10d;
                    if (slope >= 0) {
                        World world = player.level;
                        int range = (int) ItemSmallLoliPickaxe.getValue(ItemLoliAddon.Type.ATTACK_RANGE, mainHandItem);
                        Set<Entity> entitySet = Sets.newHashSet();
                        Vector3d vec = player.getLookAngle();
                        for (int dist = 0; dist <= range; dist += 2) {
                            vec = vec.normalize();
                            AxisAlignedBB bb = player.getBoundingBox();
                            bb = bb.inflate(slope * dist + 2.0, slope * dist + 0.25, slope * dist + 2.0);
                            bb = bb.move(vec.x * dist, vec.y * dist, vec.z * dist);
                            List<Entity> list = world.getEntities(player,bb);
                            entitySet.addAll(list);
                        }
                        entitySet.removeIf(entity -> ItemLoliPickaxeTool.isWhitelist(entity, player) || !entity.isAttackable() || !entity.isAlive());
                        for (Entity entity : entitySet) {
                            ItemSmallLoliPickaxe.attack(mainHandItem, player, entity);
                            player.attack(entity);
                        }
                    }
                }
            });
        }),
        SET_LOLI_CARD((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null && !nbt.isEmpty()) {
                    ItemStack mainHandItem = player.getMainHandItem();
                    if (mainHandItem.getItem() instanceof LoliCard) {
                        ItemStack itemStack = ModItems.LOLI_CARD.get().getDefaultInstance();
                        itemStack.setTag(nbt);
                        player.inventory.setItem(player.inventory.selected, itemStack);
                    }
                }
            });
        }),
        DESTROY((Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt) -> {
            long[] longs = nbt.getLongArray("list");
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null) {
                    for (long l : longs){
                        player.gameMode.destroyBlock(BlockPos.of(l));
                    }
                }});
        });

        final CompoundNBT nbt;
        final Ihandler handler;
        Pack(Ihandler i) {
            this.handler = i;
            nbt = new CompoundNBT();
            nbt.putInt("id", this.ordinal());
        }

        void handler(Supplier<NetworkEvent.Context> supplier){
           this.handler.run(supplier, this.nbt.getCompound("nbt"));
           supplier.get().setPacketHandled(true);
        }

        public void setNbt(CompoundNBT nbt){
            this.nbt.put("nbt", nbt);
        }

        static Pack getActionPack(@NotNull PacketBuffer packetBuffer){
            CompoundNBT compoundNBT = packetBuffer.readNbt();
            assert compoundNBT != null;
            int i = compoundNBT.getInt("id");
            if (i >= Pack.values().length) return Pack.EMPTY;
            Pack a = Pack.values()[i];
            a.setNbt(compoundNBT.getCompound("nbt"));
            return a;
        }

        void toBytes(PacketBuffer packetBuffer){
            packetBuffer.writeNbt(this.nbt);
        }

        public void sendToServer(){
            NetworkHandler.INSTANCE.sendToServer(this);
        }

        public void sendToServer(CompoundNBT nbt){
            this.setNbt(nbt);
            NetworkHandler.INSTANCE.sendToServer(this);
        }

        public void send(PlayerEntity player){
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(
                            () -> (ServerPlayerEntity) player
                    ),
                    this
            );
        }

        public void send(PacketDistributor.TargetPoint point){
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.NEAR.with(
                            () -> point
                    ),
                    this
            );
        }
    }

    interface Ihandler {
        void run(Supplier<NetworkEvent.Context> ctx, CompoundNBT nbt);
    }
}


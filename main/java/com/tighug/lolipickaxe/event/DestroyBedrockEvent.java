package com.tighug.lolipickaxe.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tighug.lolipickaxe.Lolipickaxe;
import com.tighug.lolipickaxe.inventory.LoliInventory;
import com.tighug.lolipickaxe.item.Tool.ILoli;
import com.tighug.lolipickaxe.item.Tool.ItemLoliPickaxe;
import com.tighug.lolipickaxe.item.Tool.ItemLoliPickaxeTool;
import com.tighug.lolipickaxe.player.LoliPlayer;
import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unchecked")
@Mod.EventBusSubscriber()
public class DestroyBedrockEvent implements Lolipickaxe.LoliEvent {
    private static final Set<Class<? extends Block>> set = Sets.newHashSet(Block.class);
    private static final Set<Class<? extends Block>> anSet = Sets.newHashSet(BreakableBlock.class, TallGrassBlock.class, EndPortalFrameBlock.class);

    @SubscribeEvent
    public static void onPlayerMine(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack itemStack = event.getItemStack();
        if ((!(event.getPlayer() instanceof ServerPlayerEntity) || itemStack.isEmpty()) || event.getPlayer() instanceof FakePlayer)return;
        if (itemStack.getItem() instanceof ILoli && !ItemLoliPickaxe.isRemoved(itemStack)) {
            breakBlock(itemStack, event.getPos(), (ServerPlayerEntity) event.getPlayer(), event.getFace());
        }
    }

    private static void breakBlock(ItemStack itemStack, BlockPos pos, ServerPlayerEntity player, Direction face) {
        AtomicReference<LoliInventory> loliInventory = new AtomicReference<>();
        player.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
            if (iLoliPlayer instanceof LoliPlayer) {
                loliInventory.set(((LoliPlayer) iLoliPlayer).getLoliInventory());
            }
        });
        if (loliInventory.get() == null) return;
        ServerWorld world = (ServerWorld) player.level;
        BlockState blockState = world.getBlockState(pos);
        if (blockState.is(Blocks.AIR)) return;
        byte range = ItemLoliPickaxeTool.getDestroyRange(itemStack);
        int exp = 0;
        NonNullList<ItemStack> itemStacks = NonNullList.create();
        if (range > 0 && !player.isDiscrete()) {
            List<BlockPos> list = Lists.newArrayList();
            int i; int j; int k;
            if (face == Direction.DOWN || face == Direction.UP){
                for (i = -range; i <= range; i++) {
                        for (k = -range; k <= range; k++) {
                            list.add(pos.offset( i, 0, k));
                        }
                    }
                }
            else if (face == Direction.NORTH || face == Direction.SOUTH){
                for (i = -range; i <= range; i++) {
                    for (j = -range; j <= range; j++) {
                            list.add(pos.offset( i, j, 0));
                    }
                }
            }
            else {
                    for (j = -range; j <= range; j++) {
                        for (k = -range; k <= range; k++) {
                            list.add(pos.offset( 0, j, k));
                    }
                }
            }
            for (BlockPos blockPos : list){
                blockState = world.getBlockState(blockPos);
                if (!blockState.is(Blocks.AIR)){
                    if (player.isCreative()){
                        PlayerInteractionManager gameMod = player.gameMode;
                        gameMod.destroyBlock(blockPos);
                        continue;
                    }
                    List<ItemStack> list1 = Block.getDrops(blockState, world, pos, blockState.getBlockState().createTileEntity(world), player, player.getMainHandItem());
                    if (!list1.isEmpty()) {
                        itemStacks.addAll(list1);
                        exp += blockState.getExpDrop(world, blockPos, EnchantmentHelper.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE, player), 0);
                    }
                    else if (canHarvestBlock(blockState)){
                        Item item = blockState.getBlock().asItem();
                        itemStacks.add(new ItemStack(item));
                    }
                    world.removeBlock(blockPos,true);
                }
            }
        }
        else {
            if (player.isCreative()) return;
            List<ItemStack> list1 = Block.getDrops(blockState, world, pos, blockState.getBlockState().createTileEntity(world), player, player.getMainHandItem());
            if (!list1.isEmpty()) {
                itemStacks.addAll(list1);
                exp += blockState.getExpDrop(world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE, player), EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player));
            }
            else if (canHarvestBlock(blockState)){
                Item item = blockState.getBlock().asItem();
                itemStacks.add(new ItemStack(item));
            }
            world.removeBlock(pos,true);
        }
        player.giveExperiencePoints(exp);
        itemStacks.removeIf(ItemStack::isEmpty);
        if (!itemStack.isEmpty()) itemStacks.forEach(itemStack1 -> {
            if (!itemStack1.isEmpty() && loliInventory.get().canAddItem(itemStack1)) loliInventory.get().addItem(itemStack1);
            else if (!itemStack1.isEmpty()){
                ItemEntity itemEntity = player.drop(itemStack1, true);
                if (itemEntity != null) {
                    itemEntity.setNoPickUpDelay();
                    itemEntity.playerTouch(player);
                }
            }
        });
    }

    private static boolean canHarvestBlock(BlockState blockState) {
        for (Class<? extends Block> c : set) {
            if (blockState.getBlock().getClass() == c) return true;
        }
        for (Class<? extends Block> c : anSet) {
            if (c.isInstance(blockState.getBlock())) return true;
        }
        return false;
    }
}

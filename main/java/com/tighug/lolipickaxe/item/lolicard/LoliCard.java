package com.tighug.lolipickaxe.item.lolicard;

import com.google.common.collect.Sets;
import com.tighug.lolipickaxe.client.LoliCardUtil;
import com.tighug.lolipickaxe.client.event.LoliPickaxeKeyEvent;
import com.tighug.lolipickaxe.client.gui.GUILoliCard;
import com.tighug.lolipickaxe.client.gui.GUILoliCardAlbum;
import com.tighug.lolipickaxe.client.gui.GUIOptionalLoliCard;
import com.tighug.lolipickaxe.item.LoliItem;
import com.tighug.lolipickaxe.item.ModItemGroup;
import com.tighug.lolipickaxe.network.NetworkHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class LoliCard extends LoliItem {
    public static final String LoliCardID = "loliCard";

    public LoliCard() {
        super();
    }

    @Override
    public @NotNull ActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity player, @NotNull Hand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (world.isClientSide() && hand == Hand.MAIN_HAND) {
            if (itemStack.hasTag() && itemStack.getOrCreateTag().contains(LoliCardID, 8)) DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> GUILoliCard.OpenGui::new);
            else {
                List<String> list = LoliCardUtil.getCustomArtNames();
                if (!list.isEmpty()) {
                    CompoundNBT nbt = new CompoundNBT();
                    int i = random.nextInt(list.size());
                    String s = list.get(i);
                    if (!s.isEmpty()) {
                        nbt.putString(LoliCardID, s);
                        NetworkHandler.Pack.SET_LOLI_CARD.sendToServer(nbt);
                        itemStack.setTag(nbt);
                        LoliPickaxeKeyEvent.playSound();
                    }
                }
            }
        }
        return ActionResult.pass(itemStack);
    }

    @NotNull
    @Override
    public String getDescriptionId(@NotNull ItemStack p_77667_1_) {
        return "item.loliCard.name";
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack p_77624_1_, World p_77624_2_, @NotNull List<ITextComponent> tooltip, @NotNull ITooltipFlag p_77624_4_) {
        if (p_77624_1_.hasTag() && p_77624_1_.getOrCreateTag().contains(LoliCardID, 8)) {
            tooltip.add(new TranslationTextComponent("item.LoliCard." + p_77624_1_.getOrCreateTag().getString(LoliCardID)).withStyle(TextFormatting.GRAY));
        }
    }

    public static class OptionalLoliCard extends LoliCard {

        @Override
        public @NotNull String getDescriptionId(@NotNull ItemStack p_77667_1_) {
            return "item.optionalLoliCard.name";
        }

        @Override
        public @NotNull ActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity player, @NotNull Hand hand) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (world.isClientSide() && hand == Hand.MAIN_HAND) {
                DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> GUIOptionalLoliCard.OpenGui::new);
            }
            return ActionResult.pass(itemStack);
        }

    }

    public static class LoliCardAlbum extends LoliItem {

        public LoliCardAlbum() {
            super();
        }

        @Override
        public int getItemStackLimit(ItemStack stack) {
            return 1;
        }

        @NotNull
        @Override
        public String getDescriptionId(@NotNull ItemStack p_77667_1_) {
            return "item.loliCardAlbum.name";
        }

        @Override
        public void fillItemCategory(@NotNull ItemGroup p_150895_1_, @NotNull NonNullList<ItemStack> p_150895_2_) {
            if (p_150895_1_ == ModItemGroup.ITEM_GROUP_LOLI) {
                p_150895_2_.add(this.getDefaultInstance());
            }
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void appendHoverText(@NotNull ItemStack p_77624_1_, World p_77624_2_, @NotNull List<ITextComponent> tooltip, @NotNull ITooltipFlag p_77624_4_) {
            if (p_77624_1_.hasTag() && p_77624_1_.getOrCreateTag().contains(LoliCardID, 9)) {
                ListNBT nbt = p_77624_1_.getOrCreateTag().getList(LoliCardID, 8);
                for (int i = 0; i < nbt.size(); ++i) {
                    String s = nbt.getString(i);
                    tooltip.add(new TranslationTextComponent("item.LoliCard." + s).withStyle(TextFormatting.GRAY));
                }
            }
        }

        @Override
        public @NotNull ActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity player, @NotNull Hand hand) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (hand == Hand.MAIN_HAND) {
                if (world.isClientSide()) {
                    if (!player.isDiscrete()) {
                        if (itemStack.hasTag() && itemStack.getOrCreateTag().contains(LoliCardID, 9)) {
                            DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> GUILoliCardAlbum.OpenGui::new);
                        }
                    }
                }
                else if (player.isDiscrete()) {
                    CompoundNBT nbt = itemStack.getOrCreateTag();
                    ListNBT nbt1 = nbt.getList(LoliCardID, 8);
                    Set<String> list = Sets.newHashSet();
                    for (int i = 0; i < nbt1.size(); ++i) {
                        list.add(nbt1.getString(i));
                    }
                    for (ItemStack itemStack1 : player.inventory.items) {
                        if (itemStack1.getItem() instanceof LoliCard) {
                            if (itemStack1.hasTag() && itemStack1.getOrCreateTag().contains(LoliCardID, 8)) {
                                list.add(itemStack1.getOrCreateTag().getString(LoliCardID));
                                player.inventory.removeItem(itemStack1);
                            }
                        }
                    }
                    if (!list.isEmpty()) {
                        ListNBT nbt2 = new ListNBT();
                        list.forEach(s -> nbt2.add(StringNBT.valueOf(s)));
                        nbt.put(LoliCardID, nbt2);
                    }
                }
            }
            return ActionResult.pass(itemStack);
        }
    }

    public static class LoliAllCardAlbum extends LoliCardAlbum {

        @OnlyIn(Dist.CLIENT)
        @Override
        public void appendHoverText(@NotNull ItemStack p_77624_1_, World p_77624_2_, @NotNull List<ITextComponent> tooltip, @NotNull ITooltipFlag p_77624_4_) {
            tooltip.add(new TranslationTextComponent("item.LoliCard.all").withStyle(TextFormatting.GRAY));
        }

        @Override
        public @NotNull ActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity player, @NotNull Hand hand) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (hand == Hand.MAIN_HAND) {
                if (world.isClientSide()) {
                    if (!player.isDiscrete()) {
                        DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> GUILoliCardAlbum.OpenGui::new);
                    }
                }
                else if (player.isDiscrete()) {
                    for (ItemStack itemStack1 : player.inventory.items) {
                        if (itemStack1.getItem() instanceof LoliCard) {
                            player.inventory.removeItem(itemStack1);
                        }
                    }
                }
            }
            return ActionResult.pass(itemStack);
        }
    }
}
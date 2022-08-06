package com.tighug.lolipickaxe.item.Tool;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.tighug.lolipickaxe.inventory.LoliInventory;
import com.tighug.lolipickaxe.player.LoliConfig;
import com.tighug.lolipickaxe.player.LoliPlayer;
import com.tighug.lolipickaxe.util.Config;
import com.tighug.lolipickaxe.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ItemLoliPickaxe extends ItemLoliPickaxeTool implements ILoli {

    public ItemLoliPickaxe() {
        super();
    }

    public static void enchant(@NotNull ItemStack itemStack, List<Pair<Enchantment, Integer>> list) {
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof ILoli)) return;
        itemStack.getOrCreateTag().remove("Enchantments");
        ListNBT listNBT = new ListNBT();
        for (Pair<Enchantment, Integer> pair : list){
            CompoundNBT compoundnbt = new CompoundNBT();
            compoundnbt.putString("id", String.valueOf(ForgeRegistries.ENCHANTMENTS.getKey(pair.getFirst())));
            compoundnbt.putShort("lvl", (short) Utils.clamp(pair.getSecond(), 0, 255));
            listNBT.add(compoundnbt);
        }
        itemStack.getOrCreateTag().put("Enchantments", listNBT);
    }

    public static @NotNull List<Effect> getEffectBlacklist(@NotNull ItemStack itemStack) {
        CompoundNBT compound = itemStack.getOrCreateTag();
        if (compound.contains(Utils.NBT_EFFECT_BLACKLIST)) {
            ListNBT listNBT = compound.getList(Utils.NBT_EFFECT_BLACKLIST, 3);
            if (listNBT.isEmpty()) return Lists.newArrayList();
            else {
                List<Effect> list = Lists.newArrayList();
                for (int i = 0; i < listNBT.size(); ++i){
                    int j = listNBT.getInt(i);
                    Effect effect = Effect.byId(j);
                    if (effect != null) list.add(effect);
                }
                return list;
            }
        } else return Lists.newArrayList();
    }

    public static @NotNull List<EffectInstance> getEffects(@NotNull ItemStack itemStack) {
        CompoundNBT compound = itemStack.getOrCreateTag();
        if (compound.contains(Utils.NBT_EFFECTS)) {
            ListNBT listNBT = compound.getList(Utils.NBT_EFFECTS, 10);
            if (listNBT.isEmpty()) return Lists.newArrayList();
            else {
                List<EffectInstance> list = Lists.newArrayList();
                for (int i = 0; i < listNBT.size(); ++i){
                    CompoundNBT nbt = listNBT.getCompound(i);
                    int j = nbt.getInt("effect");
                    short s = nbt.getShort("level");
                    Effect effect = Effect.byId(j);
                    if (effect != null) list.add(new EffectInstance(effect, 400, s));
                }
                return list;
            }
        } else return Lists.newArrayList();
    }

    public static void setEffects(@NotNull ItemStack itemStack, @NotNull List<EffectInstance> effects){
        CompoundNBT compound = itemStack.getOrCreateTag();
        compound.remove(Utils.NBT_EFFECTS);
        ListNBT listNBT = new ListNBT();
        for (EffectInstance instance : effects){
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("effect", Effect.getId(instance.getEffect()));
            nbt.putShort("level", (short) instance.getAmplifier());
            listNBT.add(nbt);
        }
        compound.put(Utils.NBT_EFFECTS, listNBT);
    }

    public static void setEffectBlacklist(@NotNull ItemStack itemStack, @NotNull List<Effect> effects) {
        CompoundNBT compound = itemStack.getOrCreateTag();
        compound.remove(Utils.NBT_EFFECT_BLACKLIST);
        ListNBT listNBT = new ListNBT();
        for (Effect effect : effects){
            IntNBT intNBT = IntNBT.valueOf(Effect.getId(effect));
            listNBT.add(intNBT);
        }
        compound.put(Utils.NBT_EFFECT_BLACKLIST, listNBT);
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack p_150893_1_, @NotNull BlockState p_150893_2_) {
        return 0;
    }

    @Override
    public @NotNull ActionResult<ItemStack> use(@NotNull World p_77659_1_, @NotNull PlayerEntity p_77659_2_, @NotNull Hand p_77659_3_) {
        ItemStack itemStack = p_77659_2_.getItemInHand(p_77659_3_);
        if (p_77659_3_ == Hand.MAIN_HAND) {
            if (isRemoved(itemStack) || p_77659_2_.isDiscrete()) return ActionResult.pass(itemStack);
            if (p_77659_1_.isClientSide()) {
                this.setDestroyRange(itemStack);
                byte b = getDestroyRange(itemStack);
                b = (byte) (b * 2 + 1);
                String s = String.format("%d * %d", b, b);
                p_77659_2_.sendMessage(new TranslationTextComponent("loliPickaxe.range").append(new StringTextComponent(s)), p_77659_2_.getUUID());
            }
            else {
                p_77659_2_.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
                    if (iLoliPlayer instanceof LoliPlayer) {
                        ((LoliPlayer) iLoliPlayer).setDestroyRange();
                    }
                });
            }
        }
        return ActionResult.pass(itemStack);
    }

    @Override
    protected @NotNull String getOrCreateDescriptionId() {
        return "item.loliPickaxe.name";
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack p_77663_1_,@Nonnull World p_77663_2_,@Nonnull Entity p_77663_3_, int p_77663_4_, boolean p_77663_5_) {
        if (p_77663_1_.isEmpty() || p_77663_2_.isClientSide()) return;
        PlayerEntity player = p_77663_3_ instanceof PlayerEntity ? (PlayerEntity) p_77663_3_ : null;
        if (player == null){
            ((ServerWorld) p_77663_2_).despawn(p_77663_3_);
        }
        else {
            if (!hasOwner(p_77663_1_)){
                player.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
                    if (iLoliPlayer instanceof LoliPlayer){
                        LoliPlayer loliPlayer = (LoliPlayer) iLoliPlayer;
                        loliPlayer.setItemLoliPickaxe(p_77663_1_);
                    }
                });
            }
            else if (!isOwner(p_77663_1_, player)) {
                player.inventory.removeItem(p_77663_1_);
            }
        }
    }

    @Override
    public void onCraftedBy(@NotNull ItemStack p_77622_1_, @NotNull World p_77622_2_, @NotNull PlayerEntity p_77622_3_) {
        if (p_77622_3_ instanceof ServerPlayerEntity && !(p_77622_3_ instanceof FakePlayer)){
            p_77622_3_.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
                if (iLoliPlayer instanceof LoliPlayer){
                    LoliPlayer loliPlayer = (LoliPlayer) iLoliPlayer;
                    loliPlayer.setItemLoliPickaxe(p_77622_1_);
                }
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack p_77624_1_, World p_77624_2_, @NotNull List<ITextComponent> tooltip, @NotNull ITooltipFlag p_77624_4_) {
        if (isRemoved(p_77624_1_)) return;
        List<IFormattableTextComponent> list = Lists.newArrayList();
        LoliConfig loliConfig = LoliConfig.of(p_77624_1_.getOrCreateTag().getCompound(ILoli.CONFIG));
        IFormattableTextComponent component = new TranslationTextComponent("loliPickaxe.attackMod").append(" ");
        if ((boolean) loliConfig.getValue(LoliConfig.Type.ATTACK_MOD)) component.append(new TranslationTextComponent("loliPickaxe.attackMod.hurt"));
        else component.append(new TranslationTextComponent("loliPickaxe.attackMod.kill"));
        list.add(component);
        int i = getDestroyRange(p_77624_1_);
        i = i * 2 + 1;
        String str = String.format("%d * %d", i, i);
        list.add(new TranslationTextComponent("loliPickaxe.curRange").append(" " + str));
        if ((boolean) loliConfig.getValue(LoliConfig.Type.THORNS)) list.add(new TranslationTextComponent("loliPickaxe.thorns"));
        if ((boolean) loliConfig.getValue(LoliConfig.Type.IS_RANGE_ATTACK)){
            i = ((Number) loliConfig.getValue(LoliConfig.Type.ATTACK_RANGE)).intValue() * 2;
            str = String.format("%d * %d * %d", i, i, i);
            list.add(new TranslationTextComponent("loliPickaxe.killRange").append(" " + str));
        }
        if ((boolean) loliConfig.getValue(LoliConfig.Type.IS_AUTO_ATTACK)){
            i = ((Number) loliConfig.getValue(LoliConfig.Type.AUTO_ATTACK_RANGE)).intValue() * 2;
            str = String.format("%d * %d * %d", i, i, i);
            list.add(new TranslationTextComponent("loliPickaxe.autoAttack").append(" " + str));
        }
        if ((boolean) loliConfig.getValue(LoliConfig.Type.VALID_TO_AMITY_ENTITY)) list.add(new TranslationTextComponent("loliPickaxe.validToAmityEntity"));
        if ((boolean) loliConfig.getValue(LoliConfig.Type.VALID_TO_NOT_LIVING_ENTITY_ENTITY)) list.add(new TranslationTextComponent("loliPickaxe.validToAllEntity"));
        if ((boolean) loliConfig.getValue(LoliConfig.Type.CLEAR_INVENTORY)) list.add(new TranslationTextComponent("loliPickaxe.clearInventory"));
        if ((boolean) loliConfig.getValue(LoliConfig.Type.DROP_ITEMS)) list.add(new TranslationTextComponent("loliPickaxe.dropItems"));
        if ((boolean) loliConfig.getValue(LoliConfig.Type.KICK_PLAYER) && Config.ALLOWABLE_KICK_PLAYER.get()) list.add(new TranslationTextComponent("loliPickaxe.kickPlayer"));
        if ((boolean) loliConfig.getValue(LoliConfig.Type.IS_ATTACK_FACING)){
            list.add(new TranslationTextComponent("loliPickaxe.killFacing").append(" " +
                    ((Number) loliConfig.getValue(LoliConfig.Type.ATTACK_FACING_RANGE)).intValue() + ", " +
                    ((Number) loliConfig.getValue(LoliConfig.Type.ATTACK_FACING_SLOPE)).doubleValue()
            ));
        }
        if ((boolean) loliConfig.getValue(LoliConfig.Type.REMOVED_ENTITY)){
            list.add(new TranslationTextComponent("loliPickaxe.removedEntity"));
        }
        list.forEach(b -> b.withStyle(TextFormatting.GRAY));
        tooltip.addAll(list);
    }

    @Override
    public boolean hasOwner(@NotNull ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains("loli_player",10);
    }

    @Override
    public boolean isOwner(ItemStack stack, @NotNull PlayerEntity player) {
        AtomicBoolean b = new AtomicBoolean(false);
        player.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
            if (iLoliPlayer.isOwner(stack)) b.set(true);
        });
        return b.get();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        if (slot == EquipmentSlotType.MAINHAND){
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 0, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", 0, AttributeModifier.Operation.ADDITION));
            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        if (player instanceof ServerPlayerEntity) {
            player.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> iLoliPlayer.attack(entity));
        }
        return true;
    }

    @Override
    public @NotNull ActionResultType useOn(@NotNull ItemUseContext p_195939_1_) {
        PlayerEntity player = p_195939_1_.getPlayer();
        World world = p_195939_1_.getLevel();
        ItemStack itemStack = p_195939_1_.getItemInHand();
        if (!world.isClientSide() && player != null && player.isDiscrete()) {
            if (!LoliPlayer.isLoli(player)) return ActionResultType.PASS;
            TileEntity b = world.getBlockEntity(p_195939_1_.getClickedPos());
            if (b == null) return ActionResultType.PASS;
            LazyOptional<IItemHandler> optional = b.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            if (itemStack.getItem() instanceof IContainer && ((IContainer) itemStack.getItem()).hasInventory(itemStack)) {
                AtomicReference<LoliInventory> inventory = new AtomicReference<>();
                player.getCapability(LoliPlayer.loliPlayer).ifPresent(iLoliPlayer -> {
                    if (iLoliPlayer instanceof LoliPlayer) {
                        inventory.set(((LoliPlayer) iLoliPlayer).getLoliInventory());
                    }
                });
                if (b instanceof IInventory){
                    IInventory box = (IInventory) b;
                    if (!box.isEmpty()) {
                        for (int i = 0; i < box.getContainerSize(); ++i) {
                            ItemStack is = box.getItem(i);
                            if (is.isEmpty()) continue;
                            if (inventory.get().canAddItem(is)) {
                                is = box.removeItem(i, is.getCount());
                                inventory.get().addItem(is);
                            }
                        }
                    } else if (!inventory.get().isEmpty()) {
                        boolean b1 = true;
                        for (int i = 0; i < inventory.get().getContainerSize() && b1 && !inventory.get().isEmpty(); ++i) {
                            for (int j = 0; j < box.getContainerSize(); ++j) {
                                ItemStack is = inventory.get().getItem(i);
                                if (is.isEmpty() || !box.getItem(j).isEmpty()) continue;
                                int k = box.getMaxStackSize() == 64 ? is.getMaxStackSize() : box.getMaxStackSize();
                                is = is.copy();
                                is.setCount(k);
                                if (box.canPlaceItem(j, is)) {
                                    is = inventory.get().removeItem(i, k);
                                    box.setItem(j, is);
                                    if (j == box.getContainerSize() - 1) {
                                        b1 = false;
                                    }
                                }
                            }
                        }
                    }
                }
                else if(optional.isPresent()){
                    optional.ifPresent(iItemHandler -> {
                        boolean b1 = true;
                        for (int i = 0; i < iItemHandler.getSlots() && b1; ++i){
                            if (!iItemHandler.getStackInSlot(i).isEmpty()) b1 = false;
                        }
                        if (b1){
                            for (int i = 0; i < inventory.get().getContainerSize() && !inventory.get().isEmpty(); ++i){
                                for (int j = 0; j < iItemHandler.getSlots(); ++j){
                                    ItemStack itemStack1 = inventory.get().getItem(i);
                                    if (itemStack1.isEmpty()) break;
                                    ItemStack itemStack2 = iItemHandler.getStackInSlot(j);
                                    if (!itemStack2.isEmpty() && !Container.consideredTheSameItem(itemStack1, itemStack2)) continue;
                                    int k = iItemHandler.getSlotLimit(j) == 64 ? itemStack1.getMaxStackSize() : iItemHandler.getSlotLimit(j);
                                    itemStack1 = inventory.get().removeItem(i, k);
                                    itemStack2 = iItemHandler.insertItem(j, itemStack1, false);
                                    if (!itemStack2.isEmpty()) inventory.get().addItem(itemStack2);
                                    if (j == iItemHandler.getSlots() - 1) return;
                                }
                            }
                        }
                        else {
                            for (int i = 0; i < iItemHandler.getSlots(); ++i){
                                ItemStack itemStack1 = iItemHandler.getStackInSlot(i);
                                if (itemStack1.isEmpty()) continue;
                                if (inventory.get().canAddItem(itemStack1)){
                                    itemStack1 = iItemHandler.extractItem(i, itemStack1.getCount(), false);
                                    inventory.get().addItem(itemStack1);
                                }
                            }
                        }
                    });
                }
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, World world) {
        return this.hasOwner(itemStack) ? 1 : 6000;
    }

    @Override
    public void setDestroyRange(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof ILoli) {
            byte b = (byte) (getDestroyRange(stack) + 1);
            if (b > 5) b = 0;
            stack.getOrCreateTag().putByte(LOLI_DESTROY_RANGE, b);
        }
    }

    @Override
    public boolean hasInventory(ItemStack stack) {
        return true;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("container.loliPickaxe");
    }

    public static boolean isRemoved(@NotNull ItemStack itemStack) {
        return itemStack.getOrCreateTag().getCompound(ILoli.CONFIG).getBoolean("removed");
    }

}

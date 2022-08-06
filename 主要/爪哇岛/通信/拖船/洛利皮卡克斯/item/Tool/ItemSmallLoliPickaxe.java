package com.tighug.lolipickaxe.item.Tool;

import com.google.common.collect.*;
import com.tighug.lolipickaxe.Lolipickaxe;
import com.tighug.lolipickaxe.client.event.LoliPickaxeKeyEvent;
import com.tighug.lolipickaxe.enchantment.ModEnchantments;
import com.tighug.lolipickaxe.item.ModItemGroup;
import com.tighug.lolipickaxe.item.addon.ItemLoliAddon;
import com.tighug.lolipickaxe.network.NetworkHandler;
import com.tighug.lolipickaxe.player.LoliPlayer;
import com.tighug.lolipickaxe.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ItemSmallLoliPickaxe extends ItemLoliPickaxeTool {
    private static final UUID ATTRIBUTE_MODIFIER_UUID = UUID.fromString("9153ACB7-39EC-2FBF-67C5-6C7883524FE8");
    public static final String SMALL_LOLI_TAG = "smallLoli";
    private final IEffect Effect0 = (int anInt, PlayerEntity player) -> {
        if (anInt < 0) return;
        if (player.getEffect(Effects.NIGHT_VISION) == null || Objects.requireNonNull(player.getEffect(Effects.NIGHT_VISION)).getDuration() < 250)
            player.addEffect(new EffectInstance(Effects.NIGHT_VISION, 400));
    };
    private final IEffect Effect1 = (int anInt, PlayerEntity player) -> {
        if (anInt < 1) return;
        if ((player.getEffect(Effects.WATER_BREATHING) == null || Objects.requireNonNull(player.getEffect(Effects.WATER_BREATHING)).getDuration() < 20) && player.isInWater())
            player.addEffect(new EffectInstance(Effects.WATER_BREATHING, 100));
    };
    private final IEffect Effect2 = (int anInt, PlayerEntity player) -> {
        if (anInt < 2) return;
        if (player.level.getDayTime() % 200 == 0) {
            player.getFoodData().eat(20, 20);
        }
    };
    private final Set<IEffect> EFFECT_LIST = Sets.newHashSet(this.Effect0, this.Effect1, this.Effect2);

    public ItemSmallLoliPickaxe() {
        super();
    }

    public static boolean isSmallLoliPickaxe(@NotNull ItemStack itemStack) {
        return itemStack.getItem() instanceof ItemSmallLoliPickaxe;
    }

    public static void enchant(@NotNull ItemStack itemStack, int i) {
        itemStack.enchant(Enchantments.BLOCK_FORTUNE, i);
        itemStack.enchant(Enchantments.MOB_LOOTING, i);
    }

    public static boolean hasLevel(ItemLoliAddon.@NotNull Type type, @NotNull ItemStack itemStack) {
        if (isSmallLoliPickaxe(itemStack)) return type.hasLevel(itemStack);
        else return false;
    }

    public static byte getLevel(ItemLoliAddon.@NotNull Type type, @NotNull ItemStack itemStack) {
        if (isSmallLoliPickaxe(itemStack) && hasLevel(type, itemStack)) return type.getLevel(itemStack);
        else return -1;
    }

    public static double getValue(ItemLoliAddon.@NotNull Type type, @NotNull ItemStack stack) {
        if (hasLevel(type, stack)) return type.getValue(stack);
        return -1;
    }

    public ItemStack getSmallLoli() {
        ItemStack itemStack = this.getDefaultInstance();
        CompoundNBT nbt = new CompoundNBT();
        for (ItemLoliAddon.Type value : ItemLoliAddon.Type.values()) {
            nbt.putInt(value.getName(), value.getMax());
        }
        itemStack.getOrCreateTag().put(SMALL_LOLI_TAG, nbt);
        enchant(itemStack, 32);
        itemStack.enchant(ModEnchantments.AUTO_FURNACE.get(), 1);
        return itemStack;
    }

    public void addEffectList(IEffect iEffect) {
        this.EFFECT_LIST.add(iEffect);
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack p_150893_1_, @NotNull BlockState p_150893_2_) {
        if (hasLevel(ItemLoliAddon.Type.DESTROY_SPEED, p_150893_1_) && this.canHarvestBlock(p_150893_1_, p_150893_2_)) {
            return (float) getValue(ItemLoliAddon.Type.DESTROY_SPEED, p_150893_1_);
        } else if (this.canHarvestBlock(p_150893_1_, p_150893_2_)) {
            return 2;
        }
        return 1;
    }

    @Override
    public @NotNull ActionResult<ItemStack> use(@NotNull World p_77659_1_, @NotNull PlayerEntity p_77659_2_, @NotNull Hand p_77659_3_) {
        if (p_77659_3_ != Hand.MAIN_HAND) return ActionResult.pass(p_77659_2_.getItemInHand(p_77659_3_));
        ItemStack mainHandItem = p_77659_2_.getMainHandItem();
        boolean b1 = !p_77659_2_.isDiscrete() && hasLevel(ItemLoliAddon.Type.DESTROY_RANGE, mainHandItem);
        boolean b2 = p_77659_2_.isDiscrete() && hasLevel(ItemLoliAddon.Type.ATTACK_RANGE, mainHandItem);
        if (p_77659_1_.isClientSide()) {
            if (b1) {
                LoliPickaxeKeyEvent.playSound();
                byte b = getDestroyRange(mainHandItem);
                if (++b > getValue(ItemLoliAddon.Type.DESTROY_RANGE, mainHandItem)) b = 0;
                int i = b * 2 + 1;
                String s = String.format("%d * %d", i, i);
                p_77659_2_.sendMessage(new TranslationTextComponent("loliPickaxe.range").append(new StringTextComponent(s)), p_77659_2_.getUUID());
            } else if (b2) {
                LoliPickaxeKeyEvent.playSound();
            }
        }
        else {
            if (b1) {
                setDestroyRange(mainHandItem);
            }
            else if (b2) {
                int i = (int) getValue(ItemLoliAddon.Type.ATTACK_RANGE, mainHandItem);
                List<Entity> list = p_77659_1_.getEntities(p_77659_2_, p_77659_2_.getBoundingBox().inflate(i));
                list.removeIf(entity -> isWhitelist(entity, p_77659_2_) || !entity.isAttackable() || !entity.isAlive());
                for (Entity entity : list) {
                    attack(mainHandItem, p_77659_2_, entity);
                    p_77659_2_.attack(entity);
                }
                p_77659_2_.resetAttackStrengthTicker();
            }
        }
        return ActionResult.pass(p_77659_2_.getItemInHand(p_77659_3_));
    }

    @Override
    protected @NotNull String getOrCreateDescriptionId() {
        return "item.smallLoliPickaxe.name";
    }

    @Override
    public void inventoryTick(@NotNull ItemStack p_77663_1_, @NotNull World p_77663_2_, @NotNull Entity p_77663_3_, int p_77663_4_, boolean p_77663_5_) {
        if (!p_77663_2_.isClientSide() && p_77663_3_ instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) p_77663_3_;
            if (hasLevel(ItemLoliAddon.Type.EFFECT, p_77663_1_)) {
                int i = (int) getValue(ItemLoliAddon.Type.EFFECT, p_77663_1_);
                this.EFFECT_LIST.forEach(iEffect -> iEffect.accept(i, player));
            }
            if (p_77663_2_.getDayTime() % 30 == 0 && hasLevel(ItemLoliAddon.Type.MAYFLY, p_77663_1_) && !LoliPlayer.isLoli(player)) {
                Lolipickaxe.LOLI_FLIGHT_EVENT.addFlyingPlayer(((StringTextComponent) player.getName()).getText());
            }
        }
    }

    @Override
    public void onCraftedBy(@NotNull ItemStack p_77622_1_, @NotNull World p_77622_2_, @NotNull PlayerEntity p_77622_3_) {
        boolean b = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.AUTO_FURNACE.get(), p_77622_1_) == 0;
        p_77622_1_.getOrCreateTag().remove("Enchantments");
        p_77622_1_.getOrCreateTag().remove("AttributeModifiers");
        if (hasLevel(ItemLoliAddon.Type.FORTUNE, p_77622_1_)) {
            enchant(p_77622_1_, (int) getValue(ItemLoliAddon.Type.FORTUNE, p_77622_1_));
        }
        if (hasLevel(ItemLoliAddon.Type.AUTO_FURNACE, p_77622_1_) && b) {
            p_77622_1_.enchant(ModEnchantments.AUTO_FURNACE.get(), 1);
        }
    }

    @Override
    public int getUseDuration(@NotNull ItemStack p_77626_1_) {
        int i = 4;
        if (hasLevel(ItemLoliAddon.Type.ATTACK_SPEED, p_77626_1_))
            i += getValue(ItemLoliAddon.Type.ATTACK_SPEED, p_77626_1_);
        return 20 + (80 / i);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack p_77624_1_, @Nullable World p_77624_2_, @NotNull List<ITextComponent> p_77624_3_, @NotNull ITooltipFlag p_77624_4_) {
        for (ItemLoliAddon.Type value : ItemLoliAddon.Type.values()) {
            if (hasLevel(value, p_77624_1_)) {
                TranslationTextComponent textComponent = new TranslationTextComponent("smallLoliPickaxe." + value.getName());
                if (value.getLevel(p_77624_1_) == value.getMax() && value.getMax() != 0) {
                    textComponent.append(" ").append(new TranslationTextComponent("item.loliMaterial.end"));
                } else if (value.getMax() != 0)
                    textComponent.append(" ").append(Utils.getTextComponent(1 + value.getLevel(p_77624_1_)));
                textComponent.withStyle(TextFormatting.GRAY);
                p_77624_3_.add(textComponent);
            }
        }
    }

    @Override
    public int getHarvestLevel(@NotNull ItemStack stack, @NotNull ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState) {
        return this.getHarvestLevel(stack);
    }

    private int getHarvestLevel(@NotNull ItemStack stack) {
        if (hasLevel(ItemLoliAddon.Type.HARVEST_LEVEL, stack)) {
            return (int) getValue(ItemLoliAddon.Type.HARVEST_LEVEL, stack);
        }
        return -1;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, PlayerEntity player) {
        if (player instanceof ClientPlayerEntity && hasLevel(ItemLoliAddon.Type.DESTROY_RANGE, itemstack)) {
            byte range = getDestroyRange(itemstack);
            if (range <= 0 || player.isDiscrete()) return false;
            ClientWorld world = (ClientWorld) player.level;
            BlockRayTraceResult blockRayTraceResult = Item.getPlayerPOVHitResult(world, player, RayTraceContext.FluidMode.NONE);
            Direction face = blockRayTraceResult.getDirection();
            if (pos.equals(blockRayTraceResult.getBlockPos())) {
                List<BlockPos> list = Lists.newArrayList();
                int i;
                int j;
                int k;
                if (face == Direction.DOWN || face == Direction.UP) {
                    for (i = -range; i <= range; i++) {
                        for (k = -range; k <= range; k++) {
                            list.add(pos.offset(i, 0, k));
                        }
                    }
                } else if (face == Direction.NORTH || face == Direction.SOUTH) {
                    for (i = -range; i <= range; i++) {
                        for (j = -range; j <= range; j++) {
                            list.add(pos.offset(i, j, 0));
                        }
                    }
                } else {
                    for (j = -range; j <= range; j++) {
                        for (k = -range; k <= range; k++) {
                            list.add(pos.offset(0, j, k));
                        }
                    }
                }
                list.remove(pos);
                if (player.isCreative()) list.removeIf(pos1 -> world.getBlockState(pos1).is(Blocks.AIR));
                else
                    list.removeIf(pos1 -> world.getBlockState(pos1).is(Blocks.AIR) || !this.canHarvestBlock(itemstack, world.getBlockState(pos1)));
                CompoundNBT nbt = new CompoundNBT();
                List<Long> longs = Lists.newArrayList();
                for (BlockPos pos1 : list) {
                    longs.add(pos1.asLong());
                }
                nbt.putLongArray("list", longs);
                NetworkHandler.Pack.DESTROY.sendToServer(nbt);
            }
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean canHarvestBlock(ItemStack stack, BlockState state) {
        return state.getHarvestLevel() <= this.getHarvestLevel(stack) && state.getDestroySpeed(null, null) >= 0;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        if (slot == EquipmentSlotType.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            CompoundNBT nbt = stack.getOrCreateTag().getCompound(SMALL_LOLI_TAG);
            if (nbt.contains(ItemLoliAddon.Type.ATTACK_DAMAGE.getName()))
                builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getValue(ItemLoliAddon.Type.ATTACK_DAMAGE, stack), AttributeModifier.Operation.ADDITION));
            else
                builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 0, AttributeModifier.Operation.ADDITION));
            if (nbt.contains(ItemLoliAddon.Type.ATTACK_SPEED.getName()))
                builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", getValue(ItemLoliAddon.Type.ATTACK_SPEED, stack), AttributeModifier.Operation.ADDITION));
            else
                builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", 0, AttributeModifier.Operation.ADDITION));
            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public void fillItemCategory(@NotNull ItemGroup p_150895_1_, @NotNull NonNullList<ItemStack> p_150895_2_) {
        if (p_150895_1_ == ModItemGroup.ITEM_GROUP_LOLI) {
            p_150895_2_.addAll(Lists.newArrayList(new ItemStack(this), getSmallLoli()));
        }
    }

    @Override
    public void setDestroyRange(ItemStack stack) {
        byte b = (byte) (getDestroyRange(stack) + 1);
        if (b > getValue(ItemLoliAddon.Type.DESTROY_RANGE, stack)) b = 0;
        stack.getOrCreateTag().putByte(LOLI_DESTROY_RANGE, b);
    }

    @Override
    public boolean hasInventory(ItemStack stack) {
        return hasLevel(ItemLoliAddon.Type.STORAGE_CAPACITY, stack);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        attack(stack, player, entity);
        return false;
    }

    public static void attack(ItemStack stack, @NotNull PlayerEntity player, Entity entity) {
        if (!player.level.isClientSide() && entity instanceof LivingEntity) {
            double d = ((double) getLevel(ItemLoliAddon.Type.ATTACK_DAMAGE, stack) + 1) / 10;
            if (d > 0) {
                LivingEntity livingEntity = (LivingEntity) entity;
                double i = livingEntity.invulnerableTime;
                livingEntity.invulnerableTime = (int) (i * (1 - d));
                Multimap<Attribute, AttributeModifier> multimap = Multimaps.newMultimap(Maps.newHashMap(), Sets::newHashSet);
                multimap.put(Attributes.ARMOR, new AttributeModifier(ATTRIBUTE_MODIFIER_UUID,"loli", -d, AttributeModifier.Operation.MULTIPLY_TOTAL));
                multimap.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "loli", -d, AttributeModifier.Operation.MULTIPLY_TOTAL));
                multimap.put(Attributes.MAX_HEALTH, new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "loli", -d, AttributeModifier.Operation.MULTIPLY_TOTAL));
                livingEntity.getAttributes().addTransientAttributeModifiers(multimap);
            }
        }
    }

    @Override
    public @NotNull ITextComponent getDisplayName() {
        return new TranslationTextComponent("container.smallLoliPickaxe");
    }

    public interface IEffect {

        void accept(int i, PlayerEntity player);
    }
}

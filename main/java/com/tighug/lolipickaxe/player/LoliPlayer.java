package com.tighug.lolipickaxe.player;

import com.google.common.collect.*;
import com.tighug.lolipickaxe.Lolipickaxe;
import com.tighug.lolipickaxe.inventory.LoliInventory;
import com.tighug.lolipickaxe.item.ModItems;
import com.tighug.lolipickaxe.item.Tool.ILoli;
import com.tighug.lolipickaxe.item.Tool.ItemLoliPickaxe;
import com.tighug.lolipickaxe.item.Tool.ItemLoliPickaxeTool;
import com.tighug.lolipickaxe.network.NetworkHandler;
import com.tighug.lolipickaxe.util.Config;
import com.tighug.lolipickaxe.util.Utils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SRemoveEntityEffectPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.tighug.lolipickaxe.Lolipickaxe.LOLI_FLIGHT_EVENT;
import static com.tighug.lolipickaxe.Lolipickaxe.LOLI_PLAYER_EVENT;

public class LoliPlayer implements ILoliPlayer {
    @SuppressWarnings("unchecked")
    private static final Set<Consumer<ServerPlayerEntity>> staticOnPlayerUpdate = Sets.newHashSet(player1 -> {
        player1.setRemainingFireTicks(0);
        if (player1.level.getDayTime() % 200 == 0) {
            player1.getFoodData().eat(20, 20);
            AttributeModifierManager modifierManager = player1.getAttributes();
            modifierManager.getDirtyAttributes().forEach(modifiableAttributeInstance -> modifiableAttributeInstance.getModifiers().forEach(attributeModifier -> {
                if (attributeModifier.getAmount() < 0 || Double.isNaN(attributeModifier.getAmount())) modifiableAttributeInstance.removeModifier(attributeModifier);
            }));
            modifierManager.getSyncableAttributes().forEach(modifiableAttributeInstance -> modifiableAttributeInstance.getModifiers().forEach(attributeModifier -> {
                if (attributeModifier.getAmount() < 0 || Double.isNaN(attributeModifier.getAmount())) modifiableAttributeInstance.removeModifier(attributeModifier);
            }));
        }
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < player1.inventory.getContainerSize(); ++i){
            ItemStack itemStack1 = player1.inventory.getItem(i);
            if (!itemStack1.isEmpty() && itemStack1.getItem() instanceof ILoli){
                if (itemStack.isEmpty()) itemStack = itemStack1;
                else player1.inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    });
    private static Field entityData;
    private static Field itemsById;
    private static Field listeners;
    private static Field dataHealthId;
    @CapabilityInject(ILoliPlayer.class)
    public static Capability<ILoliPlayer> loliPlayer;
    private final Set<Consumer<ServerPlayerEntity>> onPlayerUpdate = Sets.newHashSet();
    private final Set<Entity> attackEntities = Sets.newHashSet();
    private final LoliConfig loliConfig = LoliConfig.getDefaultConfig();
    @NotNull
    private final ServerPlayerEntity player;
    @NotNull
    private final UUID uuid;
    private final List<EffectInstance> effects = Lists.newArrayList();
    private final List<Effect> effectBlacklist = Lists.newArrayList();
    private boolean aBoolean = false;
    private ItemStack itemLoliPickaxe = ItemStack.EMPTY;
    private AttackType attackType = DefaultAttackType.kill;
    private LoliInventory loliInventory;
    private UUID message;

    static {
        Arrays.stream(EventBus.class.getDeclaredFields())
                .filter(field -> field.getType() == ConcurrentHashMap.class)
                .forEach(field -> listeners = field);
        Arrays.stream(Entity.class.getDeclaredFields())
                .filter(field -> field.getType() == EntityDataManager.class)
                .forEach(field -> entityData = field);
        Arrays.stream(LivingEntity.class.getDeclaredFields())
                .filter(field -> field.getType() == DataParameter.class)
                .filter(field -> field.getGenericType() instanceof ParameterizedType)
                .filter(field -> ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0] == Float.class)
                .forEach(field -> dataHealthId = field);
        Arrays.stream(EntityDataManager.class.getDeclaredFields())
                .filter(field -> field.getType() == Map.class)
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .forEach(field -> itemsById = field);
    }

    private LoliPlayer(@NotNull ServerPlayerEntity player, @NotNull UUID uuid) {
        this.player = player;
        this.uuid = uuid;
        this.init();
    }

    public LoliPlayer(@NotNull ServerPlayerEntity player) {
        this(player, UUID.randomUUID());
    }

    public LoliPlayer(@NotNull ServerPlayerEntity player, @NotNull CompoundNBT nbt) {
        this(player, nbt.getUUID("uuid"));
        deserializeNBT(nbt);
    }

    public LoliInventory getLoliInventory() {
        if (itemLoliPickaxe.isEmpty() || loliInventory == null) return null;
        ItemStack itemStack = player.getMainHandItem();
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ILoli) {
            setItemLoliPickaxe(itemStack);
        }
        return this.loliInventory;
    }

    protected void init() {
        onPlayerUpdate.addAll(staticOnPlayerUpdate);
        if ((boolean) loliConfig.getValue(LoliConfig.Type.REMOVED_ENTITY)) {
            attackType = DefaultAttackType.removed;
        }
        else {
            attackType = (boolean) loliConfig.getValue(LoliConfig.Type.ATTACK_MOD) ? DefaultAttackType.hurt : DefaultAttackType.kill;
        }
        refreshAttributeModifier();
        setEffect(itemLoliPickaxe);
    }

    public void setItemLoliPickaxe(@NotNull ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ILoli)) return;
        if (!itemLoliPickaxe.isEmpty() && itemLoliPickaxe.getItem() instanceof ILoli) {
            itemStack.setCount(1);
            itemStack.setTag(itemLoliPickaxe.getTag());
        }
        else {
            itemStack.setCount(1);
            CompoundNBT nbt = itemStack.getOrCreateTag();
            CompoundNBT nbt1 = new CompoundNBT();
            nbt1.putUUID("uuid", uuid);
            nbt1.putString("player", ((StringTextComponent)player.getName()).getText());
            nbt.put("loli_player", nbt1);
            nbt.put(ILoli.CONFIG, loliConfig.save());
            itemStack.getOrCreateTag().remove("AttributeModifiers");
            itemLoliPickaxe = itemStack.copy();
            loliInventory = new LoliInventory(itemLoliPickaxe);

        }
    }

    public void attackRangeEntity(final boolean isAutoAttack) {
        if (isLoli()) {
            if (!(isAutoAttack ? (boolean) loliConfig.getValue(LoliConfig.Type.IS_AUTO_ATTACK) : (boolean) loliConfig.getValue(LoliConfig.Type.IS_RANGE_ATTACK))) return;
            int range = isAutoAttack ? ((Number) loliConfig.getValue(LoliConfig.Type.AUTO_ATTACK_RANGE)).intValue() : ((Number) loliConfig.getValue(LoliConfig.Type.ATTACK_RANGE)).intValue();
            List<Entity> entities = player.level.getEntities(player, player.getBoundingBox().inflate(range));
            if (!entities.isEmpty()) {
                this.attackEntities(entities);
            }
        }
    }

    public void attackFacing() {
        if (isLoli() && (boolean) loliConfig.getValue(LoliConfig.Type.IS_ATTACK_FACING)) {
            World world = player.level;
            int range = ((Number) loliConfig.getValue(LoliConfig.Type.ATTACK_FACING_RANGE)).intValue();
            double slope = ((Number) loliConfig.getValue(LoliConfig.Type.ATTACK_FACING_SLOPE)).doubleValue();
            Set<Entity> set = Sets.newHashSet();
            Vector3d vec = player.getLookAngle();
            for (int dist = 0; dist <= range; dist += 2) {
                vec = vec.normalize();
                AxisAlignedBB bb = player.getBoundingBox();
                bb = bb.inflate(slope * dist + 2.0, slope * dist + 0.25, slope * dist + 2.0);
                bb = bb.move(vec.x * dist, vec.y * dist, vec.z * dist);
                List<Entity> list = world.getEntities(player,bb);
                set.addAll(list);
            }
            this.attackEntities(set);
        }
    }

    public void setDestroyRange() {
        ModItems.ITEM_LOLI_PICKAXE.get().setDestroyRange(itemLoliPickaxe);
    }

    public void enchant(@NotNull CompoundNBT nbt) {
        itemLoliPickaxe.getOrCreateTag().put("Enchantments", nbt.getList("Enchantments", 10));
    }

    public void setEffect(@NotNull ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof ILoli) || itemLoliPickaxe.isEmpty()) return;
        effects.clear();
        effectBlacklist.clear();
        effects.addAll(ItemLoliPickaxe.getEffects(itemStack));
        effectBlacklist.addAll(ItemLoliPickaxe.getEffectBlacklist(itemStack));
        if (itemStack != itemLoliPickaxe) {
            ItemLoliPickaxe.setEffects(itemLoliPickaxe, effects);
            ItemLoliPickaxe.setEffectBlacklist(itemLoliPickaxe, effectBlacklist);
        }
    }

    @Override
    public boolean isOwner(@NotNull ItemStack stack) {
        return stack.sameItem(itemLoliPickaxe) && stack.getOrCreateTag().getCompound("loli_player").equals(itemLoliPickaxe.getOrCreateTag().getCompound("loli_player"));
    }

    @Override
    public void tick() {
        attackEntities.clear();
    }

    @Override
    public boolean isRemoved() {
        return (boolean) loliConfig.getValue(LoliConfig.Type.IS_REMOVED);
    }

    @Override
    public boolean isLoli() {
        return !isRemoved() && (!itemLoliPickaxe.isEmpty() && itemLoliPickaxe.getItem() instanceof ILoli);
    }

    @Override
    public void onPlayerHurt(@NotNull DamageSource damageSource) {
        if (!isLoli()) return;
        if (!((boolean) loliConfig.getValue(LoliConfig.Type.THORNS))) return;
        Entity entity = damageSource.getEntity();
        if (!(isLoli(entity) || !(entity instanceof LivingEntity) || entity instanceof FakePlayer)){
            this.attack(entity);
        }
        else if (entity == null) {
            this.attackRangeEntity(false);
        }
    }

    @Override
    public void onPlayerUpdate() {
        if (!isLoli()) return;
        onPlayerUpdate.forEach(iOnPlayer -> iOnPlayer.accept(player));
        if (player.isDeadOrDying()) aBoolean = false;
        if (!aBoolean) {
            aBoolean = true;
            entityData.setAccessible(true);
            itemsById.setAccessible(true);
            dataHealthId.setAccessible(true);
            try {
                EntityDataManager entityDataManager = ((EntityDataManager) entityData.get(player));
                Map<Integer, EntityDataManager.DataEntry<?>> map = ((Map<Integer, EntityDataManager.DataEntry<?>>) itemsById.get(entityDataManager));
                DataParameter<Float> floatDataParameter = (DataParameter<Float>) dataHealthId.get(player);
                map.put(floatDataParameter.getId(), new LoliEntry(floatDataParameter, player));
            }
            catch (IllegalAccessException e) {
                dataHealthId.setAccessible(false);
                entityData.setAccessible(false);
                itemsById.setAccessible(false);
                throw new RuntimeException(e);
            }
            dataHealthId.setAccessible(false);
            entityData.setAccessible(false);
            itemsById.setAccessible(false);
        }
        player.deathTime = 0;
        for (Effect effect : effectBlacklist) {
            if (player.hasEffect(effect)) {
                removeEffect(effect, player);
            }
        }
        if ((boolean) loliConfig.getValue(LoliConfig.Type.IS_AUTO_ATTACK)) {
            this.attackRangeEntity(true);
        }
        if (player.level.getDayTime() % 30 == 0) {
            float f = ((Number) loliConfig.getValue(LoliConfig.Type.FLYING_SPEED)).floatValue();
            f = 0.05f * f;
            LOLI_FLIGHT_EVENT.addFlyingPlayer(((StringTextComponent) player.getName()).getText(), f);
            for (EffectInstance instance : effects) {
                player.addEffect(instance);
            }
        }
        if (player.level.getDayTime() % 200 == 0) {
            refreshAttributeModifier();
        }
    }

    private static void removeEffect(Effect effect, @NotNull ServerPlayerEntity player) {
        if (player.removeEffect(effect) || !player.hasEffect(effect)) return;
        EffectInstance effectinstance = player.removeEffectNoUpdate(effect);
        if (effectinstance != null) {
            effectinstance.getEffect().removeAttributeModifiers(player, player.getAttributes(), effectinstance.getAmplifier());
            player.connection.send(new SRemoveEntityEffectPacket(player.getId(), effectinstance.getEffect()));
            CriteriaTriggers.EFFECTS_CHANGED.trigger(player);
        }
    }

    private void refreshAttributeModifier() {
        Multimap<Attribute, AttributeModifier> multimap = Multimaps.newMultimap(Maps.newHashMap(), Sets::newHashSet);
        multimap.put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(this.uuid, "loli", ((Number) loliConfig.getValue(LoliConfig.Type.REACH_DISTANCE)).doubleValue() - 5, AttributeModifier.Operation.ADDITION));
        player.getAttributes().addTransientAttributeModifiers(multimap);
    }

    @Override
    public void recover() {
        if (!itemLoliPickaxe.isEmpty() && !player.inventory.contains(itemLoliPickaxe)) {
            if (player.inventory.items.contains(ItemStack.EMPTY)) {
                player.inventory.add(itemLoliPickaxe.copy());
            } else {
                player.inventory.setItem(player.inventory.selected, itemLoliPickaxe.copy());
            }
        }
    }

    @Override
    public void attack(Entity entity) {
        this.attack(entity, this.attackType);
    }

    @Override
    public void attackEntities(Collection<Entity> entities) {
        if (isLoli()) {
            if (!(boolean) loliConfig.getValue(LoliConfig.Type.VALID_TO_AMITY_ENTITY)) entities.removeIf(entity -> ItemLoliPickaxeTool.isWhitelist(entity, player));
            if (!(boolean) loliConfig.getValue(LoliConfig.Type.VALID_TO_NOT_LIVING_ENTITY_ENTITY)) entities.removeIf(entity -> !entity.isAttackable());
            if (!entities.isEmpty()) {
                entities.forEach(this::attack);
            }
        }
    }

    protected final void attack(Entity entity, AttackType attackType) {
        if (!isLoli() || entity instanceof FakePlayer || attackEntities.contains(entity) || isLoli(entity)) return;
        attackEntities.add(entity);
        attackType.accept(entity, this);
    }

    @Override
    public void setLoliConfig(CompoundNBT nbt) {
        if (itemLoliPickaxe.isEmpty()) return;
        UUID uuid = nbt.getUUID("uuid");
        if (this.message != null && this.message.equals(uuid)) {
            this.message = null;
            ListNBT nbt1 = nbt.getList(ILoli.CONFIG, 10);
            if (!nbt1.isEmpty()) {
                for (int i = 0; i < nbt1.size(); ++i) {
                    CompoundNBT nbt2 = nbt1.getCompound(i);
                    LoliConfig.Type type = LoliConfig.Type.values()[nbt2.getInt("id")];
                    if (type.getValue() == Boolean.class) {
                        loliConfig.setValue(type, nbt2.getBoolean("value"));
                    }
                    else if (type.getValue() == Number.class) {
                        loliConfig.setValue(type, nbt2.getDouble("value"));
                    }
                    else {
                        loliConfig.setValue(type, nbt2.getString("value"));
                    }
                }
                boolean re = (boolean) loliConfig.getValue(LoliConfig.Type.REMOVED_ENTITY);
                if (re) {
                    attackType = DefaultAttackType.removed;
                }
                else {
                    attackType = (boolean) loliConfig.getValue(LoliConfig.Type.ATTACK_MOD) ? DefaultAttackType.hurt : DefaultAttackType.kill;
                }
                itemLoliPickaxe.getOrCreateTag().put(ILoli.CONFIG, loliConfig.save());
                ItemStack itemStack = player.getMainHandItem();
                if (!itemStack.isEmpty() && itemStack.getItem() instanceof ILoli) {
                    setItemLoliPickaxe(itemStack);
                }
                if (isRemoved()) {
                    player.getAttributes().getSyncableAttributes().forEach(modifiableAttributeInstance -> modifiableAttributeInstance.removeModifier(this.uuid));
                    entityData.setAccessible(true);
                    itemsById.setAccessible(true);
                    try {
                        EntityDataManager entityDataManager = ((EntityDataManager) entityData.get(player));
                        Map<Integer, EntityDataManager.DataEntry<?>> map = ((Map<Integer, EntityDataManager.DataEntry<?>>) itemsById.get(entityDataManager));
                        map.keySet().forEach(integer -> {
                            EntityDataManager.DataEntry<?> dataEntry = map.get(integer);
                            if (dataEntry instanceof LoliEntry) {
                                map.put(integer, new EntityDataManager.DataEntry<>((DataParameter<Float>) dataEntry.getAccessor(), 20f));
                            }
                        });
                    } catch (IllegalAccessException e) {
                        entityData.setAccessible(false);
                        itemsById.setAccessible(false);
                        throw new RuntimeException(e);
                    }
                    entityData.setAccessible(false);
                    itemsById.setAccessible(false);
                }
                else {
                    refreshAttributeModifier();
                }
            }
        }
    }

    @Override
    public void openLoliConfig() {
        if (itemLoliPickaxe.isEmpty()) return;
        CompoundNBT nbt = new CompoundNBT();
        message = UUID.randomUUID();
        nbt.putUUID("uuid", message);
        NetworkHandler.Pack pack = NetworkHandler.Pack.LOLI_CONFIG;
        pack.setNbt(nbt);
        pack.send(player);
    }

    public static boolean isLoli(Entity entity){
        if (!(entity instanceof ServerPlayerEntity) || entity instanceof FakePlayer) return false;
        AtomicBoolean b = new AtomicBoolean(false);
        entity.getCapability(loliPlayer).ifPresent(iLoliPlayer -> {
            if (iLoliPlayer.isLoli()) b.set(true);
        });
        return b.get();
    }

    public static void addonPlayerUpdate(Consumer<ServerPlayerEntity> onPlayer){
        staticOnPlayerUpdate.add(onPlayer);
    }

    public static void registerCapability() {
        CapabilityManager.INSTANCE.register(ILoliPlayer.class, new Storage(), () -> null);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putUUID("uuid", uuid);
        if (!this.itemLoliPickaxe.isEmpty()) {
            CompoundNBT compoundNBT1 = new CompoundNBT();
            itemLoliPickaxe.save(compoundNBT1);
            compoundNBT.put("item", compoundNBT1);
        }
        if (!loliConfig.isDefault()) {
            CompoundNBT nbt = loliConfig.save();
            compoundNBT.put("loliConfig", nbt);
        }
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt == null || nbt.isEmpty()) return;
        if (!nbt.getCompound("loliConfig").isEmpty()) {
            loliConfig.replacement(LoliConfig.of(nbt.getCompound("loliConfig")));
        }
        if (!nbt.getCompound("item").isEmpty()) {
            ItemStack itemStack = ItemStack.of(nbt.getCompound("item"));
            if (itemStack.getItem() instanceof ILoli) setItemLoliPickaxe(itemStack);
        }
    }

    static class Storage implements Capability.IStorage<ILoliPlayer> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<ILoliPlayer> capability, ILoliPlayer instance, Direction side) {
            return null;
        }

        @Override
        public void readNBT(Capability<ILoliPlayer> capability, ILoliPlayer instance, Direction side, INBT nbt) {
        }
    }

    public interface AttackType extends IAttackType<LoliPlayer> {

        @Contract(pure = true)
        static @NotNull AttackType of(@NotNull BiConsumer<Entity, LoliPlayer> biConsumer){
            return biConsumer::accept;
        }
    }

    private enum DefaultAttackType implements AttackType {
        hurt((Entity entity, LoliPlayer loliPlayer) -> {
            if (entity.isAlive()) {
                DamageSource ds = new LoliDamageSource(loliPlayer.player);
                boolean b = (boolean) loliPlayer.loliConfig.getValue(LoliConfig.Type.DROP_ITEMS);
                float f = ((Number) loliPlayer.loliConfig.getValue(LoliConfig.Type.ATTACK_DAMAGE)).floatValue();
                int i = ((Number) loliPlayer.loliConfig.getValue(LoliConfig.Type.ATTACK_SPEED)).intValue();
                if (entity instanceof PlayerEntity) {
                    PlayerEntity player1 = (PlayerEntity) entity;
                    if ((boolean) loliPlayer.loliConfig.getValue(LoliConfig.Type.CLEAR_INVENTORY)) {
                        player1.inventory.clearContent();
                        player1.getEnderChestInventory().clearContent();
                        LazyOptional<IItemHandler> optional = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                        optional.ifPresent(iItemHandler -> {
                            for (int i1 = 0; i1 < iItemHandler.getSlots(); ++i1){
                                ItemStack stackInSlot = iItemHandler.getStackInSlot(i1);
                                if (stackInSlot.isEmpty()) continue;
                                iItemHandler.extractItem(i1 , Integer.MAX_VALUE, false);
                                stackInSlot = iItemHandler.getStackInSlot(i1);
                                if (!stackInSlot.isEmpty()) stackInSlot.setCount(0);
                            }
                        });
                    }
                    else if (b) {
                        player1.inventory.dropAll();
                    }
                }
                else if (entity instanceof MobEntity && b) {
                    MobEntity mobEntity = (MobEntity) entity;
                    ((List<ItemStack>) mobEntity.getArmorSlots()).clear();
                    ((List<ItemStack>) mobEntity.getHandSlots()).clear();
                }
                for (int i1 = 0 ; i1 < i && entity.isAlive(); ++i1){
                    entity.invulnerableTime = 0;
                    entity.hurt(ds, f);
                }
            }
        }),
        kill((Entity entity, LoliPlayer loliPlayer) -> {
            if (!entity.isAlive()) return;
            if (entity instanceof PlayerEntity) {
                PlayerEntity player1 = (PlayerEntity) entity;
                if ((boolean) loliPlayer.loliConfig.getValue(LoliConfig.Type.CLEAR_INVENTORY)) {
                    player1.inventory.clearContent();
                    player1.getEnderChestInventory().clearContent();
                    LazyOptional<IItemHandler> optional = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    optional.ifPresent(iItemHandler -> {
                        for (int i = 0; i < iItemHandler.getSlots(); ++i){
                            ItemStack stackInSlot = iItemHandler.getStackInSlot(i);
                            if (stackInSlot.isEmpty()) continue;
                            iItemHandler.extractItem(i , Integer.MAX_VALUE, false);
                            stackInSlot = iItemHandler.getStackInSlot(i);
                            if (!stackInSlot.isEmpty()) stackInSlot.setCount(0);
                        }
                    });
                }
                else if ((boolean) loliPlayer.loliConfig.getValue(LoliConfig.Type.DROP_ITEMS) || !player1.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                    player1.inventory.dropAll();
                }
                player1.closeContainer();
                player1.inventoryMenu.removed(player1);
                player1.getCombatTracker().recordDamage(LoliDamageSource.NullDamageSource, Float.MAX_VALUE, Float.MAX_VALUE);
                entityData.setAccessible(true);
                itemsById.setAccessible(true);
                dataHealthId.setAccessible(true);
                try {
                    EntityDataManager entityDataManager = ((EntityDataManager) entityData.get(player1));
                    Map<Integer, EntityDataManager.DataEntry<?>> map = ((Map<Integer, EntityDataManager.DataEntry<?>>) itemsById.get(entityDataManager));
                    DataParameter<Float> floatDataParameter = (DataParameter<Float>) dataHealthId.get(player1);
                    map.put(floatDataParameter.getId(), new KillEntry(floatDataParameter));
                }
                catch (IllegalAccessException e) {
                    dataHealthId.setAccessible(false);
                    entityData.setAccessible(false);
                    itemsById.setAccessible(false);
                    throw new RuntimeException(e);
                }
                dataHealthId.setAccessible(false);
                entityData.setAccessible(false);
                itemsById.setAccessible(false);
                player1.die(LoliDamageSource.NullDamageSource);
                if (Config.ALLOWABLE_KICK_PLAYER.get() && (boolean) loliPlayer.loliConfig.getValue(LoliConfig.Type.KICK_PLAYER)) {
                    ((ServerPlayerEntity) player1).connection.disconnect(new StringTextComponent((String) loliPlayer.loliConfig.getValue(LoliConfig.Type.KICK_PLAYER_MESSAGE)));
                }
            }
            else if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                PlayerEntity player1 = loliPlayer.player;
                DamageSource ds = new LoliDamageSource(player1);
                livingEntity.setLastHurtByPlayer(player1);
                livingEntity.getCombatTracker().recordDamage(ds, Float.MAX_VALUE, Float.MAX_VALUE);
                entityData.setAccessible(true);
                itemsById.setAccessible(true);
                try {
                    EntityDataManager entityDataManager = ((EntityDataManager) entityData.get(livingEntity));
                    Map<Integer, EntityDataManager.DataEntry<?>> map = ((Map<Integer, EntityDataManager.DataEntry<?>>) itemsById.get(entityDataManager));
                    map.keySet().forEach(integer -> {
                        EntityDataManager.DataEntry<?> dataEntry = map.get(integer);
                        if (dataEntry.getValue().getClass() == Float.class && ((Float) dataEntry.getValue()) == livingEntity.getHealth()) {
                            map.put(integer, new KillEntry((DataParameter<Float>) dataEntry.getAccessor()));
                        }
                    });
                }
                catch (IllegalAccessException e) {
                    entityData.setAccessible(false);
                    itemsById.setAccessible(false);
                    throw new RuntimeException(e);
                }
                entityData.setAccessible(false);
                itemsById.setAccessible(false);
                Class<? extends LivingEntity> clazz = livingEntity.getClass();
                LOLI_PLAYER_EVENT.classes.add(clazz);
                livingEntity.die(ds);
                LOLI_PLAYER_EVENT.classes.remove(clazz);
            }
            else ((ServerWorld) loliPlayer.player.level).despawn(entity);
        }),
        @SuppressWarnings("deprecation")
        removed((Entity entity, LoliPlayer loliPlayer) -> {
            ServerWorld world = (ServerWorld) loliPlayer.player.level;
            if (entity instanceof ServerPlayerEntity) {
                removedServerPlayer((ServerPlayerEntity) entity);
            }
            else {
                Class<? extends Entity> clazz = entity.getClass();
                unregisterEvent(clazz);
                LOLI_PLAYER_EVENT.classes.add(clazz);
                world.despawn(entity);
                entity.removed = true;
                LOLI_PLAYER_EVENT.classes.remove(clazz);
            }
        });

        final BiConsumer<Entity, LoliPlayer> results;
        DefaultAttackType(BiConsumer<Entity, LoliPlayer> o) {
            results = o;
        }

        @Override
        public void accept(Entity entity, LoliPlayer loliPlayer) {
            results.accept(entity, loliPlayer);
        }

    }

    private static void unregisterEvent(@NotNull Class<?> clazz) {
        if (!Config.ALLOWABLE_UNREGISTER_EVENT.get()) return;
        StringBuilder sb = Utils.getStringBuilder(clazz);
        if (!sb.toString().equals(Utils.forge_package_name) && !sb.toString().equals(Utils.minecraft_package_name) && !sb.toString().equals(Utils.package_name)) {
            listeners.setAccessible(true);
            try {
                        ((ConcurrentHashMap<?, ?>) listeners.get(MinecraftForge.EVENT_BUS))
                                .keySet()
                                .stream()
                                .filter(o -> {
                                    if (Lolipickaxe.LoliEvent.isLoliEvent(o)) return false;
                                    if (o.getClass() == Class.class) {
                                        return ((Class<?>) o).getName().contains(sb.toString());
                                    }
                                    return o.getClass().getName().contains(sb.toString());
                                })
                                .forEach(MinecraftForge.EVENT_BUS::unregister);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
            listeners.setAccessible(false);
        }
    }

    public static void removedServerPlayer(ServerPlayerEntity entity) {
        if (isLoli(entity)) return;
        for (int i = 0; i < entity.inventory.getContainerSize(); ++i) {
            ItemStack itemStack = entity.inventory.getItem(i);
            if (itemStack.getItem() != Items.AIR) {
                unregisterEvent(itemStack.getItem().getClass());
                entity.inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        entity.getEnderChestInventory().clearContent();
        for (Field field : CapabilityProvider.class.getDeclaredFields()) {
            if (field.getType() == CapabilityDispatcher.class) {
                for (Field field1 : CapabilityDispatcher.class.getDeclaredFields()) {
                    if (field1.getType() == ICapabilityProvider[].class) {
                        try {
                            field1.setAccessible(true);
                            field.setAccessible(true);
                            Object obj = field.get(entity);
                            if (obj != null) {
                                if (Config.ALLOWABLE_UNREGISTER_EVENT.get() && obj.getClass() != LoliPlayerCapabilityProvider.class ) for (ICapabilityProvider ic : (ICapabilityProvider[]) field1.get(obj)) {
                                    unregisterEvent(ic.getClass());
                                }
                                field1.set(obj, new ICapabilityProvider[0]);
                            }
                            field1.setAccessible(false);
                            field.setAccessible(false);
                        } catch (IllegalAccessException e) {
                            field1.setAccessible(false);
                            field.setAccessible(false);
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        entity.setGameMode(GameType.SURVIVAL);
        entity.abilities.instabuild = false;
        entity.setExperienceLevels(0);
        ((ServerWorld) entity.level).removePlayer(entity, true);
        NetworkHandler.Pack.REMOVED_CLIENT_PLAYER.send(entity);
    }

    public static class LoliDamageSource extends EntityDamageSource {
        public static LoliDamageSource NullDamageSource = new LoliDamageSource(null);

        public LoliDamageSource(@Nullable Entity p_i1567_2_) {
            super("loli", p_i1567_2_);
        }

        @Override
        public boolean isBypassArmor() {
            return true;
        }

        @Override
        public boolean isBypassInvul() {
            return true;
        }

        @Override
        public boolean isBypassMagic() {
            return true;
        }

        @Override
        public @NotNull ITextComponent getLocalizedDeathMessage(@NotNull LivingEntity entity) {
            if (!(entity instanceof PlayerEntity)) return super.getLocalizedDeathMessage(entity);
            StringTextComponent str = (StringTextComponent) entity.getDisplayName();
            return str.append(new TranslationTextComponent("loliPickaxe.attack.message"));
        }
    }

    private static class LoliEntry extends EntityDataManager.DataEntry<Float> {
        private final Random random = new Random();
        private final PlayerEntity player;

        public LoliEntry(DataParameter<Float> p_i47010_1_, PlayerEntity player) {
            super(p_i47010_1_, player.getMaxHealth());
            this.player = player;
        }

        @Override
        public void setValue(@NotNull Float p_187210_1_) {
        }

        @Override
        public @NotNull Float getValue() {
            return Utils.clamp((random.nextFloat() + 1) * player.getMaxHealth(), 1, Float.MAX_VALUE);
        }

    }

    private static class KillEntry extends EntityDataManager.DataEntry<Float> {

        public KillEntry(DataParameter<Float> p_i47010_1_) {
            super(p_i47010_1_, 0F);
        }

        @Override
        public void setValue(@NotNull Float p_187210_1_) {}

        @Override
        public @NotNull Float getValue() {
            return 0F;
        }

    }
}

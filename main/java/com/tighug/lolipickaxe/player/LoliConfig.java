package com.tighug.lolipickaxe.player;

import com.google.common.collect.Maps;
import com.tighug.lolipickaxe.util.Utils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Map;

public class LoliConfig {
    private final Map<String, Serializable> map = Maps.newHashMap();
    private boolean isDefault;

    private LoliConfig() {
        for (Type v : Type.values()) {
            if (v.getValue() == String.class) {
                map.put(v.getName(), new TranslationTextComponent((String) v.value).getString());
                continue;
            }
            map.put(v.getName(), v.value);
        }
        isDefault = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        else if (obj instanceof LoliConfig) {
            LoliConfig loliConfig = (LoliConfig) obj;
            for (Type type : Type.values()) {
                if (!loliConfig.getValue(type).equals(this.getValue(type))) return false;
            }
            return true;
        }
        return false;
    }

    private LoliConfig(CompoundNBT nbt) {
        for (Type v : Type.values()) {
            if (!nbt.contains(v.getName())) {
                setForDefault(v);
                continue;
            }
            if (Boolean.class == v.getValue()) {
                setValue(v, nbt.getBoolean(v.getName()));
            }
            else if (Number.class == v.getValue()) {
                setValue(v, nbt.getDouble(v.getName()));
            }
            else {
                setValue(v, nbt.getString(v.getName()));
            }
        }
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setValue(@NotNull Type type, @NotNull Serializable value) {
        if (!type.getValue().isInstance(value)) return;
        if (value instanceof Number) {
            Number value1 = (Number) value;
            value1 = Utils.clamp(value1.doubleValue(), type.min, type.max);
            if (type.value instanceof Integer) value1 = value1.intValue();
            map.put(type.getName(), value1);
        }
        else map.put(type.getName(), value);
        isDefault = false;
    }

    public Serializable setForDefault(@NotNull Type type) {
        if (type.getValue() == String.class) {
            String string = new TranslationTextComponent((String) type.value).getString();
            map.put(type.getName(), string);
            return string;
        }
        map.put(type.getName(), type.value);
        return type.value;
    }

    public Serializable getValue(@NotNull Type type){
        return map.get(type.getName());
    }

    public void replacement(LoliConfig loliConfig){
        if (isDefault()){
            for (Type v : Type.values()) {
                setValue(v, loliConfig.map.get(v.getName()));
            }
        }
    }

    public CompoundNBT save() {
        CompoundNBT nbt = new CompoundNBT();
        for (Type v : Type.values()) {
            if (Boolean.class == v.getValue()) {
                nbt.putBoolean(v.getName(), (Boolean) map.get(v.getName()));
            }
            else if (Number.class == v.getValue()) {
                nbt.putDouble(v.getName(), ((Number) map.get(v.getName())).doubleValue());
            }
            else {
                nbt.putString(v.getName(), (String) map.get(v.getName()));
            }
        }
        return nbt;
    }

    @Contract(" -> new")
    public static @NotNull LoliConfig getDefaultConfig() {
        return new LoliConfig();
    }

    @Contract("_ -> new")
    public static @NotNull LoliConfig of(CompoundNBT nbt) {
        return new LoliConfig(nbt);
    }

    public enum Type {
        ATTACK_MOD("attack", false),
        ATTACK_DAMAGE("loliPickaxeAttack_damage", 10f, Float.MAX_VALUE),
        ATTACK_SPEED("loliPickaxeAttack_speed", 10, 20),
        IS_ATTACK_FACING("loliPickaxeKillFacing", true),
        ATTACK_FACING_RANGE("loliPickaxeAttackFacingRange", 50),
        ATTACK_FACING_SLOPE("loliPickaxeAttackFacingSlope", 0.1, 2d, 0d),
        IS_RANGE_ATTACK("attackRange", true),
        ATTACK_RANGE("loliPickaxeAttackRANGE", 50),
        IS_AUTO_ATTACK("autoAttack", false),
        AUTO_ATTACK_RANGE("loliPickaxeAutoAttackRange", 5, 50),
        THORNS("thorns", true),
        VALID_TO_AMITY_ENTITY("validToAmityEntity", true),
        VALID_TO_NOT_LIVING_ENTITY_ENTITY("validToNotLivingEntityEntity", false),
        DROP_ITEMS("dropItems", false),
        CLEAR_INVENTORY("clearInventory", false),
        KICK_PLAYER("kickPlayer", false),
        KICK_PLAYER_MESSAGE("kickPlayerMessage", "loliPickaxe.kickPlayer.message"),
        FLYING_SPEED("loliPickaxeFlyingSpeed", 5f, 20f),
        REACH_DISTANCE("loliPickaxeReachDistance", 5, 256, 0),
        REMOVED_ENTITY("removedEntity", false),
        IS_REMOVED("removed", false);

        private final String name;
        private final Serializable value;
        private final double min;
        private final double max;

        Type(String str, Serializable i) {
            this.name = str;
            this.value = i;
            this.min = 1;
            this.max = 200;
        }

        Type(String str, Serializable i, double max) {
            this.name = str;
            this.value = i;
            this.min = 1;
            this.max = max;
        }

        Type(String str, Serializable i, double max, double min) {
            this.name = str;
            this.value = i;
            this.min = min;
            this.max = max;
        }

        public String getName() {
            return name;
        }

        public Class<? extends Serializable> getValue() {
            if (value instanceof Boolean) return Boolean.class;
            if (value instanceof String) return String.class;
            return Number.class;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }
    }
}

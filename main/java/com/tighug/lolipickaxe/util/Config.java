package com.tighug.lolipickaxe.util;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec COMMON_CONFIG;
    public static final ForgeConfigSpec.BooleanValue DISPLAY_TAGS;
    public static final ForgeConfigSpec.BooleanValue DISPLAY_COUNT;
    public static final ForgeConfigSpec.BooleanValue ALLOWABLE_KICK_PLAYER;
    public static final ForgeConfigSpec.BooleanValue ALLOWABLE_UNREGISTER_EVENT;
    public static final ForgeConfigSpec.BooleanValue LOLIPICKAXE_AUTOMATIC_ATTACK;
    public static final ForgeConfigSpec.BooleanValue REVISE_ATTACK_DAMAGE_MAX_VALUE;

    static {
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        CLIENT_BUILDER.comment("in client").push("display");
        DISPLAY_TAGS = CLIENT_BUILDER.define("display_tags", true);
        DISPLAY_COUNT = CLIENT_BUILDER.define("display_count", true);
        CLIENT_BUILDER.pop();
        CLIENT_BUILDER.push("lolipickaxe");
        ALLOWABLE_KICK_PLAYER = CLIENT_BUILDER.define("allowable_kick_player", true);
        ALLOWABLE_UNREGISTER_EVENT = CLIENT_BUILDER.define("allowable_unregister_event", false);
        REVISE_ATTACK_DAMAGE_MAX_VALUE = CLIENT_BUILDER.define("revise_attack_damage_max_value", true);
        LOLIPICKAXE_AUTOMATIC_ATTACK = CLIENT_BUILDER.comment("in client").define("lolipickaxe_automatic_attack", true);
        CLIENT_BUILDER.pop();
        COMMON_CONFIG = CLIENT_BUILDER.build();
    }
}

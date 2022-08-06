package com.tighug.lolipickaxe.util;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec.BooleanValue DISPLAY_TAGS;
    public static ForgeConfigSpec.BooleanValue DISPLAY_COUNT;
    public static ForgeConfigSpec.BooleanValue ALLOWABLE_KICK_PLAYER;
    public static ForgeConfigSpec.BooleanValue ALLOWABLE_UNREGISTER_EVENT;
    public static ForgeConfigSpec.BooleanValue LOLIPICKAXE_AUTOMATIC_ATTACK;

    static {
        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        CLIENT_BUILDER.comment("in client").push("display");
        DISPLAY_TAGS = CLIENT_BUILDER.define("display_tags", true);
        DISPLAY_COUNT = CLIENT_BUILDER.define("display_count", true);
        CLIENT_BUILDER.pop();
        CLIENT_BUILDER.push("lolipickaxe");
        ALLOWABLE_KICK_PLAYER = CLIENT_BUILDER.define("allowable_kick_player", true);
        ALLOWABLE_UNREGISTER_EVENT = CLIENT_BUILDER.define("allowable_unregister_event", false);
        LOLIPICKAXE_AUTOMATIC_ATTACK = CLIENT_BUILDER.comment("in client").define("lolipickaxe_automatic_attack", true);
        CLIENT_BUILDER.pop();
        COMMON_CONFIG = CLIENT_BUILDER.build();
    }
}

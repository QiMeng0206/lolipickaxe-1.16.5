package com.tighug.lolipickaxe.util;

import com.google.common.collect.Lists;
import net.minecraft.potion.Effect;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.world.World;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Utils {
    public static final String MODID = "lolipickaxe";
    public static final List<Effect> EFFECTS = Lists.newArrayList();
    public static final String NBT_EFFECT_BLACKLIST = "effectBlacklist";
    public static final String NBT_EFFECTS = "effects";
    public static final String package_name;
    public static final String forge_package_name;
    public static final String minecraft_package_name;

    static {
        package_name = getStringBuilder(Utils.class).toString();
        forge_package_name = getStringBuilder(ForgeVersion.class).toString();
        minecraft_package_name = getStringBuilder(World.class).toString();
    }

    @NotNull
    public static StringBuilder getStringBuilder(@NotNull Class<?> Class) {
        StringBuilder sb = new StringBuilder();
        String s = Class.getName();
        for (int i = 0; i < s.length() - 1; ++i) {
            char c = s.charAt(i);
            if (c == '.') {
                if (sb.toString().contains(".")) break;
            }
            sb.append(c);
        }
        return sb;
    }

    public static @NotNull TextComponent getTextComponent(int i) {
        if (i < 1) return new StringTextComponent(String.valueOf(i));
        else {
            Integer[] ints = {0, 0, 0, 0, 0,};
            int j = i;
            if (j >= 100){
                int i1 = j / 100;
                j -= 100 * i1;
                ints[0] += i1;
            }
            if (j >= 50){
                int i1 = j / 50;
                j -= 50 * i1;
                ints[1] += i1;
            }
            if (j >= 10){
                int i1 = j / 10;
                j -= 10 * i1;
                ints[2] += i1;
            }
            if (j >= 5){
                int i1 = j / 5;
                j -= 5 * i1;
                ints[3] += i1;
            }
            ints[4] += j;
            StringBuilder sb = new StringBuilder();
            for (int anInt = 0; anInt < 5; ++anInt){
                while (ints[anInt] > 0){
                    ints[anInt] -= 1;
                    switch(anInt){
                        case 0 : sb.append('C');
                            break;
                        case 1 : sb.append('L');
                            break;
                        case 2 : sb.append('X');
                            break;
                        case 3 : sb.append('V');
                            break;
                        case 4 : sb.append('I');
                            break;
                    }
                }
            }
            return new StringTextComponent(sb.toString());
        }
    }

    // if d1 isNaN return min
    public static double clamp(double d1, double min, double max) {
        if (d1 > max) return max;
        return d1 > min ? d1 : min;
    }

    public static float clamp(float d1, float min, float max) {
        if (d1 > max) return max;
        return d1 > min ? d1 : min;
    }

    public static long clamp(long d1, long min, long max) {
        if (d1 > max) return max;
        return d1 > min ? d1 : min;
    }

    public static int clamp(int d1, int min, int max) {
        if (d1 > max) return max;
        return d1 > min ? d1 : min;
    }

}

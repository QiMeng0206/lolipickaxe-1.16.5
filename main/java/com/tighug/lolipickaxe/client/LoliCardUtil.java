package com.tighug.lolipickaxe.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.tighug.lolipickaxe.util.Utils.MODID;
import static net.minecraft.util.ResourceLocation.validPathChar;

@OnlyIn(value = Dist.CLIENT)
public class LoliCardUtil {
    public static final String path = "lolicards";
    private static final List<String> customArtNames = Lists.newArrayList();
    private static final Map<String, ResourceLocation> map1 = Maps.newHashMap();
    private static final Map<String, Pair<Integer, Integer>> map = Maps.newHashMap();

    public static void init() {
        SimpleReloadableResourceManager rm = (SimpleReloadableResourceManager) Minecraft.getInstance().getResourceManager();
        Collection<ResourceLocation> locations = rm.listResources("lolicards", s -> !s.equals(path) && s.endsWith(".png") && s.length() > 4);
        for (ResourceLocation resourceLocation : locations) {
            StringBuilder sb = new StringBuilder(resourceLocation.getPath());
            sb.delete(0, 10);
            sb.delete(sb.length() - 4, sb.length());
            customArtNames.add(sb.toString());
            map1.put(sb.toString(), resourceLocation);
            try {
                IResource theThing = rm.getResource(resourceLocation);
                Image img = ImageIO.read(theThing.getInputStream());
                if (img != null) {
                    int height = img.getHeight(null);
                    int width = img.getWidth(null);
                    map.put(sb.toString(), Pair.of(height, width));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Collections.sort(customArtNames);
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation getResourceLocation (String s) {
        if (map1.containsKey(s)) return map1.get(s);
        else if (!map1.isEmpty()) return map1.get(customArtNames.get(0));
        return new ResourceLocation(MODID, path + "/" + s);
    }

    public static @NotNull List<String> getCustomArtNames() {
        return Lists.newArrayList(customArtNames);
    }

    public static Pair<Integer, Integer> getSize(String s) {
        if (map.isEmpty()) return Pair.of(0, 0);
        String str = map1.containsKey(s) ? s : customArtNames.get(0);
        return map.get(str);
    }

    public static boolean addCard(@NotNull ResourceLocation resourceLocation) {
        IResourceManager rm = Minecraft.getInstance().getResourceManager();
        if (resourceLocation.getPath().length() < 5) return false;
        try {
            IResource theThing = rm.getResource(resourceLocation);
            Image img = ImageIO.read(theThing.getInputStream());
            if (img != null) {
                StringBuilder sb = new StringBuilder(resourceLocation.getPath());
                sb.delete(0, 10);
                sb.delete(sb.length() - 4, sb.length());
                customArtNames.add(sb.toString());
                map1.put(sb.toString(), resourceLocation);
                int height = img.getHeight(null);
                int width = img.getWidth(null);
                map.put(sb.toString(), Pair.of(height, width));
                return true;
            }
            else return false;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static boolean isValidNamespace(@NotNull String p_217858_0_) {
        for(int i = 0; i < p_217858_0_.length(); ++i) {
            if (!validPathChar(p_217858_0_.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
